package com.example.ARmirrorS.Server.ImgProc.Algorithms;

import android.app.Activity;
import android.widget.SeekBar;

import com.example.ARmirrorS.R;
import com.example.ARmirrorS.Server.Constants.BinaryCvMap;
import com.example.ARmirrorS.Server.Constants.ImageProcessParam;
import com.example.ARmirrorS.Server.ImgProc.Utils.Frame;
import com.example.ARmirrorS.Server.ImgProc.Utils.Utils;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * <h1>Class Binary</h1>
 * Class <b>Binary</b> uses simple thresholding to extract forground from background.
 * For every pixel, the same threshold value is applied. If the pixel value is smaller than the
 * threshold, it is set to 0, otherwise it is set to a maximum value.
 *
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

public class Binary {

    /**Parent Activity Running Context.*/
    private static Activity parent;
    /**Current Frame Being Processed.*/
    private static Frame frame;
    /**Flag to test if SeekBars have initialized. Until then just return the RGBA color image.*/
    private static boolean wait        = true;
    /**Override parameters for Binary Processing (Ignore Morphing kernel for smoothing edges).*/
    private static boolean ignoreMorph = true;
    /**Kernel Size used in morphing and blurring final mask.*/
    private static double  kernelSize  = 12;
    /**the maximum value which is assigned to pixel values exceeding the threshold (max 255).*/
    private static double maxVal;
    /**threshold value which is used to classify the pixel values.*/
    private static double threshVal;

    /**
     * Get all values in the seekBar the user input and store locally. And start processing the
     * passed in frame based on the Binary sub method selected by user. Wait and just return the
     * gray or color channel of the frame in seekbars are not yet initialized.
     *
     * @param parentActivity Parent Activity Running Context.
     * @param setFrame Current Frame being processed.
     *
     * @return processed frame this can be masked color or gray image. or just black and white mask.
     */
    public static Mat subtract(final Activity parentActivity, Frame setFrame) {

        // First Set parent Activity and local frame reference so we don't pass them privately
        parent = parentActivity;
        frame = setFrame;

        // Get all values in the seekBar the user inputed and store locally
        getGuiParameters();

        // Start Processing the frame Wait until SeekBars initialize or we get null pointer
        // exception. Until then just return the RGBA color image.
        if (wait) {
            return frame.mRgba;
        } else {
            return process();
        }
    }

    /**
     * Retrieve maximum and minimum thresholds as well as morphing enhancement flag.
     *
     */
    private static void getGuiParameters() {
        SeekBar maxValSeek = parent.findViewById(R.id.seekBarMaxValBinary);
        SeekBar ThreshSeek = parent.findViewById(R.id.seekBarThresholdValue);
        SeekBar morphSeek = parent.findViewById(R.id.seekBarMorphKernelBinary);

        if (maxValSeek != null) {
            maxVal = (double) maxValSeek.getProgress();
            threshVal = (double) ThreshSeek.getProgress();
            kernelSize = (double) morphSeek.getProgress();
            ignoreMorph = frame.getIgnoreMorphology();
            wait = false;
        }

    }

    /**
     * Process the current frame based on Binary sub-method selected by user.
     *
     * 1. Check that the user selected one of the sub types of binary thresholding otherwise
     *    return color image without further processing
     * 2. Start by cleaning the original Image and removing noise to easier handle the segmentation
     * 3. Start thresholding the image using OpenCV API
     * 4. Use morphological operators to enhance the image if user selects this option
     *
     * @return processed frame this can be masked color or gray image. or just black and white mask.
     */
    private static Mat process() {
        Mat mask = new Mat();

        // Check that the user selected one of the sub types of binary thresholding otherwise
        // return color image without further processing
        if (frame.getProcMethod() != ImageProcessParam.BG_SUBTRACT_ADAPTIVE) {

            // 1. Start by cleaning the original Image and removing noise to easier handle the
            // segmentation
            Imgproc.blur(frame.mGray, frame.mIntermediateMat, new Size(6,6));

            // 2. use map to switch between our defined threshold and OpenCV thresholds
            int thresholdType = BinaryCvMap.map.get(frame.getProcMethod());

            // 3. Start thresholding the image usine OpenCV API
            Imgproc.threshold(frame.mIntermediateMat, mask, threshVal, maxVal, thresholdType);

            if (!ignoreMorph) {
                // 4. Use morphological operators to enhance the image if user selects this option
                Mat morphMask = Utils.morphImage(mask, kernelSize);
                return Utils.returnDisplayImage(frame, morphMask);
            }
            return Utils.returnDisplayImage(frame, mask);

        } else {
            mask = frame.mRgba;
        }

        return mask;
    }
}
