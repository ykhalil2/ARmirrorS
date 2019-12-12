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

/**
 * <h1>Class Canny</h1>
 * Class <b>Canny</b> Edge Detection is a popular edge detection algorithm. It performs the
 * following tasks:
 *
 * 1. Finding Intensity Gradient of the Image
 * 2. Thresholding stage decides which are all edges are really edges and which are not
 * 3. Morphing and eroding for better visuals.
 * <p>
 *
 * @author Yussuf Khalil, Daniel King
 * @author ykhalil2@illinois.edu, dking32@illinois.edu
 *
 * @version 1.1
 * @since 2019-12-05
 *
 * @see Activity
 * @see Mat
 * @see Imgproc
 */

public class Canny {

    /**Parent Activity Running Context.*/
    private static Activity parent;
    /**Current Frame Being Processed.*/
    private static Frame frame;
    /**High Threshold Value. any edge with intensity gradient more than maxVal are sure to be edges*/
    private static double maxVal;
    /**Low Threshold Value. any edge with intensity gradient less than thresholdVal are sure to be no edges*/
    private static double thresholdVal;
    /**Override parameters for Canny Processing (Ignore Mean computed HSV value of image from Hue Channel.*/
    private static boolean overRideHSVMean;
    /**Override parameters for Canny Processing (Ignore Morphing kernel for smoothing edges).*/
    private static boolean overRideEnhancement;

    /**
     * Get all values in the seekBar the user input and store locally. And start processing the
     * passed in frame based on Canny sub method selected.
     *
     * @param parentActivity Parent Activity Running Context.
     * @param setFrame Current Frame being processed.
     *
     * @return processed frame this can be masked color or gray image. or just black and white mask.
     *
     */
    public static Mat subtract(final Activity parentActivity, Frame setFrame) {

        // First Set parent Activity and local frame reference so we don't pass them privately
        parent = parentActivity;
        frame = setFrame;

        // Get all values in the seekBar the user input and store locally
        getGuiParameters();

        // Start Processing the frame
        return process();
    }

    /**
     * Retrieve maximum and minimum thresholds and whether to ignore mean HSV of the image and
     * set it manually as well as morphing enhancement flag.
     *
     */
    private static void getGuiParameters() {
        SeekBar seekBar1 = parent.findViewById(R.id.seekBarMaxValCanny);
        SeekBar seekbar2 = parent.findViewById(R.id.seekBarThresholdValueCanny);
        Switch  switch2  = parent.findViewById(R.id.switch2);

        maxVal = (double) seekBar1.getProgress();
        thresholdVal = (double) seekbar2.getProgress();
        overRideHSVMean = frame.getIgnoreMeanHSV();
        overRideEnhancement = frame.getIgnoreEnhance();
    }

    /**
     * Process the current frame based on sub-method selected by user and if we are to use Binary or
     * adaptive thresholding for edge detection.
     *
     * In case contours are selected. Display the contours of edges and do no further processing.
     *
     * @return processed frame (this can be masked color or gray image. or just black and white mask.
     */
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

    /**
     * Performs a Canny Edge Detection with counters and no background removal. By performing:
     *
     * 1. blur the image by userInput value store in mIntermediateMat
     * 2. apply canny filter on blured image and store it in mIntermediateMat
     * 3. Create a destination frame initializing it to 0's (black).
     * 4. Copy the color frame mRgba to black dest frame using only the canny line mask
     *
     * @return masked contours frame.
     */
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

    /**
     * Performs a Canny Edge Detection and with Background Removal thresholding the image with the
     * average hue value unless user decides to override it.
     *
     * 1. create a new frame of proper size to store HSV value
     * 2. Obtain HSV values by converting RGB frame
     * 3. Split the mHSV matrix and gets the HSV planes
     * 4. get the average hue value of the image. If the background is uniform and fills most of
     *    the frame, its value should be close to mean just calculated
     * 5. use binary or gaussian adaptive or inverse of for thresholding to separate background
     * 6. dilate to fill gaps, erode to smooth edges after bluring the resulting image to clear
     *    some of the noise
     * 7. finally apply a new binary or adaptive threshold to enhance the image after blur and erode
     *
     * @param method threshold method to be used.
     *
     * @return created new masked image
     */
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
