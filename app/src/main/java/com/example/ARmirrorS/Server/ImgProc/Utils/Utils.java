package com.example.ARmirrorS.Server.ImgProc.Utils;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;

import com.example.ARmirrorS.MirrorApp;
import com.example.ARmirrorS.Server.Constants.CameraParam;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCamera2View;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {


    public static int[][] getCameraResolutions(CameraBridgeViewBase mCameraView, String cameraID) {

        JavaCamera2View.JavaCameraSizeAccessor accessor = new JavaCamera2View.JavaCameraSizeAccessor();
        int[][] returnArray;

        CameraManager manager = (CameraManager) mCameraView.getContext().getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics characteristics = null;// mCameraID);
        try {
            characteristics = manager.getCameraCharacteristics(String.valueOf(cameraID));
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        android.util.Size[] supportedRes = map.getOutputSizes(ImageReader.class);
        List<android.util.Size> sizes_list = Arrays.asList(supportedRes);

        returnArray = new int[sizes_list.size()][2];
        int index = 0;
        for (Object size : sizes_list) {
            int width = accessor.getWidth(size);
            int height = accessor.getHeight(size);
            returnArray[index][0] = width;
            returnArray[index][1] = height;
            index++;
        }
        return returnArray;
    }

    /**
     * Get the average hue value of the image starting from its Hue channel
     * histogram.
     *
     * @param hsvImg
     *            the current frame in HSV
     * @param hueValues
     *            the Hue component of the current frame
     * @return the average Hue value
     */
    public static double getHistAverage(Mat hsvImg, Mat hueValues)
    {
        // init
        double average = 0.0;
        Mat hist_hue = new Mat();
        // 0-180: range of Hue values
        MatOfInt histSize = new MatOfInt(180);
        List<Mat> hue = new ArrayList<>();
        hue.add(hueValues);

        // compute the histogram
        Imgproc.calcHist(hue, new MatOfInt(0), new Mat(), hist_hue, histSize, new MatOfFloat(0, 179));

        // get the average Hue value of the image
        // (sum(bin(h)*h))/(image-height*image-width)
        // -----------------
        // equivalent to get the hue of each pixel in the image, add them, and
        // divide for the image size (height and width)
        for (int h = 0; h < 180; h++)
        {
            // for each bin, get its value and multiply it for the corresponding
            // hue
            average += (hist_hue.get(h, 0)[0] * h);
        }

        // return the average hue of the image
        return average = average / hsvImg.size().height / hsvImg.size().width;
    }

    /**
     * Function to reduce noise on a provided mask to perform morphological eroding and
     * dilation to eliminate noise.
     *
     *@param mask
     *         the current masked frame to be morphed
     * @param kernelSize
     *          kernel Size used to erode and dilate mask
     *
     * @return black and white mask or masked RGB image
     */
    public static Mat morphImage(Mat mask, double kernelSize) {
        // Place holder for new morphed mask
        Mat morphMask = new Mat();

        // Use morphological operators to enhance the image if user selects this option
        Mat dilateKernel =
                Imgproc.getStructuringElement(
                        Imgproc.MORPH_RECT,
                        new Size(kernelSize, kernelSize)
                );
        Mat erodeKernel =
                Imgproc.getStructuringElement(
                        Imgproc.MORPH_RECT,
                        new Size(kernelSize/2, kernelSize/2)
                );

        // Erode first with
        Imgproc.erode(mask, morphMask, erodeKernel);
        Imgproc.erode(morphMask, morphMask, erodeKernel);

        // Then dilate
        Imgproc.dilate(morphMask, morphMask, dilateKernel);
        Imgproc.dilate(morphMask, morphMask, dilateKernel);

        return morphMask;
    }

    /**
     * Function to return an image to display on screen based on choice of Camera View Mode
     * chosen by user.
     *
     *@param currentFrame
     *         the current frame being processed with all its channels
     * @param currentMask
     *          the current processed frame
     *
     * @return black and white mask or masked RGB image
     */
    public static Mat returnDisplayImage(Frame currentFrame, Mat currentMask) {

        // Create a place holder for our new forground image to display
        Mat foreground = new Mat(currentFrame.mIntermediateMat.size(), CvType.CV_8U, new Scalar((3)));

        // Check the user has not requested that we invert the mask. If so invert it by making all
        // blacks white.
        boolean invertMask = currentFrame.getInvertMask();
        if (invertMask) {
            Core.bitwise_not(currentMask, currentMask);
        }

        // Check the Camera mode selected by the user and apply the mask to the RGB color channel of
        // the current frame
        switch(currentFrame.getCameraMode()) {

            // In case of RGB requested mask with color image
            case CameraParam.VIEW_MODE_RGBA: {
                Core.bitwise_and(currentFrame.mRgba, currentFrame.mRgba, foreground, currentMask);
                return foreground;
            }

            // In case of Gray scale request mask with Gray channel
            case CameraParam.VIEW_MODE_GRAY: {
                Core.bitwise_and(currentFrame.mGray, currentFrame.mGray, foreground, currentMask);
                return foreground;
            }

            // In case mask is requested return the black and white mask only
            case CameraParam.VIEW_MODE_MASK:  {

                // If the user is connected. Check the requested tile size
                if (/*MirrorApp.getNoOFTiles() != 0*/ true ) {
                    Mat image = currentMask.clone();

                    // Create Region of interest to crop image and send
                    createROI(image);
                }

                return currentMask;
            }

            case CameraParam.VIEW_MODE_PIXELATED: {
                // 1st Apply the mask to the Gray image
                Core.bitwise_and(currentFrame.mGray, currentFrame.mGray, foreground, currentMask);

                // Second resize the image to client selected dimensions obtained from
                // WebSocketServe
                Mat image = currentFrame.mGray.clone();

                // Create Region of interest to draw a rectangle on the screen of what will be
                // transfered to client
                Rect rectCrop = createROI(image);

                // Finally pixlate the image by reducing its size to number set by client
                // then upscale it again for display
                //
                // 1st get tile size from the main application if a client is connected this should be
                // any number greater that 0. other wise no client is connected choose 32 to display
                // to the user (32 is number of maximum pixels per row and column that will be
                // returned

                int noTiles = (MirrorApp.getNoOFTiles() > 0) ? MirrorApp.getNoOFTiles() : 32;
                Size downscaleSize = new Size(noTiles, noTiles);
                Size upscaleSize   = new Size(image.width(), image.height());

                Imgproc.resize(foreground, foreground, downscaleSize);
                Imgproc.resize(foreground, foreground, upscaleSize);

                // show a rectangle to display on the screen
                Imgproc.rectangle(foreground, rectCrop, new Scalar(255, 255, 255), 6);

                Mat resultBrightImage = new Mat();
                foreground.convertTo(resultBrightImage, -1, 1.3, 0);

                // Return the un-cropped image foreground pixelated gray image
                return resultBrightImage;
            }

            default:
                break;
        }

        // if all else fails return a black image.
        return currentFrame.mRgba;
    }

    private static Rect createROI(Mat image) {

        // create cropping rectangle
        double cropXSize = Math.round((image.width() - Math.round(image.height())) / 2.0);
        Point p1 = new Point(0.0, 0.0);
        Point p2 = new Point(cropXSize, 0.0);
        Point p3 = new Point((image.width() - cropXSize), image.height());
        Point p4 = new Point(image.width(), image.height());
        Rect rectCrop = new Rect(p2, p3);

        // create cropped and downsized image to send ro client
        Mat cropped = new Mat(image, rectCrop);

        //Fix rotation issue by rotating 90 clockwise
        Core.rotate(cropped, cropped, Core.ROTATE_90_CLOCKWISE);

        // Downscale the image to a maximum of 32 pixels and resize it
        int noTiles = (MirrorApp.getNoOFTiles() > 0) ? MirrorApp.getNoOFTiles() : 32;
        Size downscaleSize = new Size(noTiles, noTiles);
        Imgproc.resize(cropped, cropped, downscaleSize);

        // Convert the cropped masked image of black and whites to an array to send to client
        MatOfInt r = new MatOfInt(CvType.CV_32S);
        cropped.convertTo(r, CvType.CV_32S);
        int[] sendArray = new int[(int)(r.total()*r.channels())];
        r.get(0,0, sendArray);

        // Normalize values of the array to bytes from -45 degree to 45 degrees. Every pixel value
        // from 0 - 255 will be converted to an angle from -45 to 45 deg. where 127 will be equal
        // to zero

        ByteBuffer byteBuffer = ByteBuffer.allocate(sendArray.length * 4);
        for (int i = 0; i < sendArray.length; i++) {
            if (sendArray[i] < 128) {
                sendArray[i] = (byte) ((-128.0 / (double) sendArray[i] ) * 40.0);
            } else {
                sendArray[i] = (byte) ((128.0 / (double) sendArray[i] ) * 40.0);
            }
            byteBuffer.putInt(i, sendArray[i]);
        }
        byte sendBlob[];
        sendBlob = byteBuffer.array();

        // if we are streaming then go ahead and broadcast the byte buffer message to all clients
        if (MirrorApp.getStartStream()) {
            MirrorApp.sendMessage2Server(sendBlob);
        }

        /** remove comments to print or dump the cropped image to send to client for
         *  debugging only
         */
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++>" + Arrays.toString(sendArray));
        System.out.println( cropped.dump());

        return rectCrop;
    }
}


/**
 private int convertByteArrayToInt(byte[] intBytes){
 ByteBuffer byteBuffer = ByteBuffer.wrap(intBytes);
 return byteBuffer.getInt();
 }
 */