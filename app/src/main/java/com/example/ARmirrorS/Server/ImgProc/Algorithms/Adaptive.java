package com.example.ARmirrorS.Server.ImgProc.Algorithms;

import android.app.Activity;
import android.widget.SeekBar;

import com.example.ARmirrorS.R;
import com.example.ARmirrorS.Server.Constants.AdaptiveCvMap;
import com.example.ARmirrorS.Server.Constants.ImageProcessParam;
import com.example.ARmirrorS.Server.ImgProc.Utils.Frame;
import com.example.ARmirrorS.Server.ImgProc.Utils.Utils;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * <h1>Class Adaptive</h1>
 * Class <b>Adaptive</b> uses region thresholding to extract forground from background. Here, the
 * algorithm determines the threshold for a pixel based on a small region around it. So we get
 * different thresholds for different regions of the same image which gives better results for
 * images with varying illumination.
 * <p>
 *
 * The adaptiveMethod decides how the threshold value is calculated:
 *      ADAPTIVE_THRESH_MEAN: The threshold value is the mean of the neighbourhood area minus
 *                            the constant C.
 *      ADAPTIVE_THRESH_GAUSSIAN: The threshold value is a gaussian-weighted sum of the
 *                            neighbourhood values minus the constant C.
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

public class Adaptive {

    /**Parent Activity Running Context.*/
    private static Activity parent;
    /**Current Frame Being Processed.*/
    private static Frame frame;
    /**Flag to test if SeekBars have initialized. Until then just return the RGBA color image.*/
    private static boolean wait        = true;
    /**Override parameters for Adaptive Processing (Ignore Morphing kernel for smoothing edges).*/
    private static boolean ignoreMorph = true;
    /**Kernel Size used in morphing and blurring final mask.*/
    private static double  kernelSize  = 12;
    /**the maximum value which is assigned to pixel values exceeding the threshold (max 255).*/
    private static double maxVal;
    /**blockSize determines the size of the neighbourhood area to average.*/
    private static int    blockSize;
    /**C is a constant that is subtracted from the mean or weighted sum of the neighbourhood pixels.*/
    private static double subtractC;

    /**
     * Get all values in the seekBar the user input and store locally. And start processing the
     * passed in frame based on the Adaptive sub method selected by user. Wait and just return the
     * gray or color channel of the frame in seek bars are not yet initialized.
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
     * Retrieve maximum and minimum thresholds, block size, C constant as well as morphing
     * enhancement flag.
     */
    private static void getGuiParameters() {
        SeekBar maxValSeek   = parent.findViewById(R.id.seekBarMaxValAdaptive);
        SeekBar blockSeek    = parent.findViewById(R.id.seekBarBlockSize);
        SeekBar subtractSeek = parent.findViewById(R.id.seekSubtractionConstant);
        SeekBar morphSeek    = parent.findViewById(R.id.seekBarMorphKernelAdaptive);

        if (maxValSeek != null) {
            maxVal      = (double) maxValSeek.getProgress();
            subtractC   = (double) subtractSeek.getProgress();
            kernelSize  = (double) morphSeek.getProgress();
            ignoreMorph = frame.getIgnoreMorphology();

            blockSize   =  blockSeek.getProgress();
            if ( blockSize%2 == 0) {
                ++blockSize;
            }
            blockSeek.setProgress(blockSize);

            wait = false;
        }
    }

    /**
     * Process the current frame based on Adaptive sub-method selected by user.
     *
     * 1. Check that the user selected one of the sub types of Adaptive thresholding otherwise
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
            int thresholdType = AdaptiveCvMap.map.get(frame.getProcMethod());

            // 3. Start thresholding the image using OpenCV API
            Imgproc.adaptiveThreshold
                    (
                            frame.mGray,
                            mask,
                            maxVal,
                            thresholdType,
                            Imgproc.THRESH_BINARY,
                            blockSize,
                            subtractC
                    );

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
