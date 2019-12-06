package com.example.ARmirrorS.Server.ImgProc.Algorithms;

import android.app.Activity;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.ARmirrorS.Server.Constants.ImageProcessParam;
import com.example.ARmirrorS.R;
import com.example.ARmirrorS.Server.ImgProc.Utils.Frame;
import com.example.ARmirrorS.Server.ImgProc.Utils.Utils;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class Canny {

    private static Activity parent;
    private static Frame frame;
    private static double maxVal;
    private static double thresholdVal;
    private static boolean overRideHSVMean;
    private static boolean overRideEnhancement;


    public static Mat subtract(final Activity parentActivity, Frame setFrame) {

        // First Set parent Activity and local frame reference so we don't pass them privately
        parent = parentActivity;
        frame = setFrame;

        // Get all values in the seekBar the user inputed and store locally
        getGuiParameters();

        // Start Processing the frame
        return process();

    }


    private static void getGuiParameters() {
        SeekBar seekBar1 = parent.findViewById(R.id.seekBarMaxValCanny);
        SeekBar seekbar2 = parent.findViewById(R.id.seekBarThresholdValueCanny);
        Switch  switch2  = parent.findViewById(R.id.switch2);

        maxVal = (double) seekBar1.getProgress();
        thresholdVal = (double) seekbar2.getProgress();
        overRideHSVMean = frame.getIgnoreMeanHSV();
        overRideEnhancement = frame.getIgnoreEnhance();
    }


    private static Mat process() {

        switch(frame.getProcMethod()) {
            case ImageProcessParam.BG_SUBTRACT_CANNY_CONT: {
                return contours();
            }
            case ImageProcessParam.BG_SUBTRACT_CANNY_BIN:
            case ImageProcessParam.BG_SUBTRACT_CANNY_BIN_INV:
            case ImageProcessParam.BG_SUBTRACT_CANNY_ADAPTIVE:
            case ImageProcessParam.BG_SUBTRACT_CANNY_ADAPTIVE_INV: {
                return binaryOrAdaptive(frame.getProcMethod());
            }
            case ImageProcessParam.BG_SUBTRACT_CANNY:
            default: {
                return frame.mRgba;
            }
        }
    }


    private static Mat contours() {
        // Canny Edge Detection with counters and no background removal.
        // 1. First blur the image by userInput value store in mIntermediateMat
        Imgproc.blur(frame.mGray, frame.mIntermediateMat, new Size(4, 4));

        // 2. apply canny filter on blured image and store it in mIntermediateMat
        Imgproc.Canny(frame.mIntermediateMat, frame.mIntermediateMat, 80, 100, 3, true);

        // 3. Create a destination frame initializing it to 0's (black)
        Mat dest = new Mat();
        Core.add(dest, Scalar.all(0), dest);

        //4. Copy the color frame mRgba to black dest frame using only the canny line mask
        frame.mRgbaT = frame.mRgba;
        frame.mRgbaT.copyTo(dest, frame.mIntermediateMat);
        return dest;
    }


    private static Mat binaryOrAdaptive(int method) {

        // Canny Edge Detection and with Background Removal
        // threshold the image with the average hue value

        Mat mHsv = new Mat();
        List<Mat> mHsvPlanes = new ArrayList<>();
        double averageHueVal;

        // 1. create a new frame of proper size to store HSV value
        mHsv.create(frame.mRgba.size(), CvType.CV_8U);

        // 2. Obtain HSV values by converting RGB frame
        Imgproc.cvtColor(frame.mRgba, mHsv, Imgproc.COLOR_BGR2HSV);

        // 3. Split the mHSV matrix and gets the HSV planes
        Core.split(mHsv, mHsvPlanes);

        // 4. get the average hue value of the image. If the background is uniform and fills most of
        // the frame, its value should be close to mean just calculated.
        averageHueVal = Utils.getHistAverage(mHsv, mHsvPlanes.get(0));
        TextView text = parent.findViewById(R.id.meanThresholdCannyText);
        text.setText(String.valueOf((int)averageHueVal));
        averageHueVal = !overRideHSVMean ? averageHueVal : thresholdVal;

        // 5. use binary or gaussian adaptive or inverse of for thresholding to separate background
        switch (method) {
            case ImageProcessParam.BG_SUBTRACT_CANNY_BIN: {
                Imgproc.threshold(
                        mHsvPlanes.get(0),
                        frame.mIntermediateMat,
                        averageHueVal,
                        maxVal,
                        Imgproc.THRESH_BINARY);
            } break;

            case ImageProcessParam.BG_SUBTRACT_CANNY_BIN_INV: {
                Imgproc.threshold(
                        mHsvPlanes.get(0),
                        frame.mIntermediateMat,
                        averageHueVal,
                        maxVal,
                        Imgproc.THRESH_BINARY_INV);

            } break;

            case ImageProcessParam.BG_SUBTRACT_CANNY_ADAPTIVE: {
                Imgproc.adaptiveThreshold(
                        mHsvPlanes.get(0),
                        frame.mIntermediateMat,
                        maxVal,
                        Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                        Imgproc.THRESH_BINARY,
                        11,
                        2);
            } break;

            case ImageProcessParam.BG_SUBTRACT_CANNY_ADAPTIVE_INV: {
                Imgproc.adaptiveThreshold(
                        mHsvPlanes.get(0),
                        frame.mIntermediateMat,
                        maxVal,
                        Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                        Imgproc.THRESH_BINARY_INV,
                        11,
                        2);
            } break;

            default:
                break;
        }

        // 6. dilate to fill gaps, erode to smooth edges after bluring the resulting image to clear
        // some of the noise
        Imgproc.blur(
                frame.mIntermediateMat,
                frame.mIntermediateMat,
                new Size(5, 5));

        Imgproc.dilate(
                frame.mIntermediateMat,
                frame.mIntermediateMat,
                new Mat(), new Point(-1, -1),
                1);

        Imgproc.erode(
                frame.mIntermediateMat,
                frame.mIntermediateMat,
                new Mat(),
                new Point(-1, -1),
                3);

        // 7. finally apply a new binary or adaptive threshold to enhance the image after blur and
        // erosion
        if (!overRideEnhancement) {
            switch (method) {
                case ImageProcessParam.BG_SUBTRACT_CANNY_BIN: {
                    Imgproc.threshold(
                            frame.mIntermediateMat,
                            frame.mIntermediateMat,
                            averageHueVal,
                            maxVal,
                            Imgproc.THRESH_BINARY);
                }
                break;

                case ImageProcessParam.BG_SUBTRACT_CANNY_BIN_INV: {
                    Imgproc.threshold(
                            frame.mIntermediateMat,
                            frame.mIntermediateMat,
                            averageHueVal,
                            maxVal,
                            Imgproc.THRESH_BINARY_INV);

                }
                break;

                case ImageProcessParam.BG_SUBTRACT_CANNY_ADAPTIVE: {
                    Imgproc.adaptiveThreshold(
                            frame.mIntermediateMat,
                            frame.mIntermediateMat,
                            maxVal,
                            Imgproc.ADAPTIVE_THRESH_MEAN_C,
                            Imgproc.THRESH_BINARY,
                            11,
                            2);
                }
                break;

                case ImageProcessParam.BG_SUBTRACT_CANNY_ADAPTIVE_INV: {
                    Imgproc.adaptiveThreshold(
                            frame.mIntermediateMat,
                            frame.mIntermediateMat,
                            maxVal,
                            Imgproc.ADAPTIVE_THRESH_MEAN_C,
                            Imgproc.THRESH_BINARY_INV,
                            11,
                            2);
                }
                break;

                default:
                    break;
            }
        }

        // 8. create the new image
        Mat foreground = new Mat(
                frame.mIntermediateMat.size(),
                CvType.CV_8UC3,
                new Scalar(255, 255, 255));

        frame.mRgba.copyTo(foreground, frame.mIntermediateMat);

        return foreground;
    }
}
