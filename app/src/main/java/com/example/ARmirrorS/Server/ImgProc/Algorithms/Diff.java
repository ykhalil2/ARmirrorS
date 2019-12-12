package com.example.ARmirrorS.Server.ImgProc.Algorithms;

import android.app.Activity;
import android.widget.SeekBar;

import com.example.ARmirrorS.Constatnts.InteractionLevel;
import com.example.ARmirrorS.MirrorApp;
import com.example.ARmirrorS.R;
import com.example.ARmirrorS.Server.Constants.ImageProcessParam;
import com.example.ARmirrorS.Server.ImgProc.Utils.Frame;
import com.example.ARmirrorS.Server.ImgProc.Utils.Utils;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * <h1>Class Diff</h1>
 * Class <b>Diff</b> uses simple Background subtraction (BS) is a common and widely used technique
 * for generating a foreground mask (namely, a binary image containing the pixels belonging to
 * moving objects in the scene) by using static cameras.
 *
 * As the name suggests, BS calculates the foreground mask performing a subtraction between the
 * current frame and a background model, containing the static part of the scene or, more in
 * general, everything that can be considered as background given the characteristics of the
 * observed scene.
 *
 * Background modeling consists of two main steps:
 *
 * Background Initialization;
 * Background Update.
 * In the first step, an initial model of the background is computed, while in the second step that
 * model is updated in order to adapt to possible changes in the scene.
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

public class Diff {

    /**Parent Activity Running Context.*/
    private static Activity parent;
    /**Current Frame Being Processed.*/
    private static Frame frame;
    /**Flag to test if SeekBars have initialized. Until then just return the RGBA color image.*/
    private static boolean wait                 = true;
    /**Override parameters for Binary Processing (Ignore Morphing kernel for smoothing edges).*/
    private static boolean ignoreMorph          = true;
    /**Kernel Size used in morphing and blurring final mask.*/
    private static double  kernelSize           = 12;
    /**Learning rate for MOG2 and KNN subtraction method.*/
    private static double  learningRate         =.003;
    private static double  backgroundRatio      = 0.5;
    /**Number of frames to keep in history to determine what is background and segment it from foreground.*/
    private static int     maxHistoryFrames     = 250;
    /**Complexity reduction ratio of the background.*/
    private static double  complexityReduction  = 0.05;
    /**ignore or detect shadows in subtraction processing.*/
    private static boolean detectShadows        = false;


    /**
     * Get all values in the seekBar the user input and store locally. And start processing the
     * passed in frame based on the sub subtraction method selected by user. Wait and just return
     * the gray or color channel of the frame in seekbars are not yet initialized.
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
     * Retrieve The learning rate, maximum frames to track, background complexity reduction
     * constant. Morphing kernel size and whether to ignore it or not, and finally the background
     * ratio to foreground parameters.
     *
     * This function is called on every frame to read the seek bars and if the user has made any
     * changes.
     */
    private static void getGuiParameters() {
        SeekBar learningRateSeek    = parent.findViewById(R.id.seekBarSubtractionLearningRate);
        SeekBar maxHistSeek         = parent.findViewById(R.id.seekBarHistoryFramesNo);
        SeekBar morphSeek           = parent.findViewById(R.id.seekBarMorphKernelDiff);
        SeekBar complexityRedSeek   = parent.findViewById(R.id.seekBarComplexityReduction);
        SeekBar backgroundRatioSeek = parent.findViewById(R.id.seekBarBackgroundRatio);

        int interactionLevel = ((MirrorApp) parent.getApplication()).getInteractionLevel();

        // If user selected easy mode then use default parameters
        if (interactionLevel == InteractionLevel.MODE_EASY) {
            wait = false;
        }

        if (maxHistSeek != null) {
            maxHistoryFrames = maxHistSeek.getProgress();
            kernelSize = morphSeek.getProgress();
            ignoreMorph = frame.getIgnoreMorphology();
            detectShadows = frame.getDetectShadows();

            learningRate = (double) learningRateSeek.getProgress() / 3000.0;
            complexityReduction = (double) complexityRedSeek.getProgress() / 3000.0;
            backgroundRatio = (double) backgroundRatioSeek.getProgress() / 20.0;

            if (frame.getProcMethod() == ImageProcessParam.BG_SUBTRACT_DIFF_ABS_1ST) {
                if (frame.getCaptured1stFrame() && frame.firstGray == null) {
                    frame.firstGray = frame.mGray.clone();
                    wait = false;
                }
            } else {
                wait = false;
            }
        }
    }

    /**
     * Process the current frame based on Difference sub-method selected by user.
     *
     * 1. Check that the user selected one of the sub types of thresholding otherwise return color
     *    image without further processing
     * 2. Start by cleaning the original Image and removing noise to easier handle the segmentation
     *    also normalize the pixel values for sudden lighting changes
     * 3. update the background model frame.
     * 4. In case absolute Sequential difference is selected
     *    - Take the absolute difference between each consecutive frames and threshold the resulting
     *      mask. After converting both frames to gray scale
     *    - Threshold the intensity image at a given intensity value
     *    - blur one more time to get rid of noise
     *    - threshold blured mask to obtain our final mask
     * 5. In case absolute 1st Frame difference is selected
     *    - perform the previous task but subtract current frame from original frame not previous
     *      one.
     * 6. Use morphological operators to enhance the image if user selects this option
     * <p>
     *
     * @return processed frame this can be masked color or gray image. or just black and white mask.
     */
    private static Mat process() {
        Mat mask       = new Mat(frame.mGray.size(), CvType.CV_8UC1);
        Mat absDiffImg = new Mat(frame.mGray.size(), CvType.CV_8UC1);
        Mat refFrame;

        // Check that the user selected one of the sub types of thresholding otherwise
        // return color image without further processing
        if (frame.getProcMethod() != ImageProcessParam.BG_SUBTRACT_DIFF) {

            // 1. Start by cleaning the original Image and removing noise to easier handle the
            // segmentation also normalize the pixel values for sudden lighting changes
            Imgproc.blur(frame.mGray, frame.mIntermediateMat, new Size(8,8));

            //Imgproc.GaussianBlur(frame.mRgba, frame.mIntermediateMat, new Size(6,6), 0);
            //frame.mIntermediateMat.convertTo(frame.mIntermediateMat, CV_32F, 1.0 / 255, 0);

            // 2. update the background model frame.
            switch (frame.getProcMethod()) {
                case ImageProcessParam.BG_SUBTRACT_DIFF_MOG2: {
                    // set parameters and apply diff
                    frame.backgroundBufferMOG2.setHistory(maxHistoryFrames);
                    frame.backgroundBufferMOG2.setDetectShadows(detectShadows);
                    frame.backgroundBufferMOG2.setBackgroundRatio(backgroundRatio);
                    frame.backgroundBufferMOG2.setVarThreshold(127.0);
                    frame.backgroundBufferMOG2.setComplexityReductionThreshold(complexityReduction);
                    frame.backgroundBufferMOG2.apply(frame.mIntermediateMat, mask, learningRate);
                } break;

                case ImageProcessParam.BG_SUBTRACT_DIFF_KNN: {
                    // set parameters and apply diff
                    frame.backgroundBufferKNN.setDetectShadows(detectShadows);
                    frame.backgroundBufferKNN.setHistory(maxHistoryFrames);
                    frame.backgroundBufferKNN.apply(frame.mIntermediateMat, mask, learningRate);
                } break;

                case ImageProcessParam.BG_SUBTRACT_DIFF_ABS_SEQ:
                case ImageProcessParam.BG_SUBTRACT_DIFF_ABS_1ST: {
                    if (frame.getProcMethod() == ImageProcessParam.BG_SUBTRACT_DIFF_ABS_1ST) {
                        refFrame = frame.firstGray;
                    } else {
                        refFrame = frame.previousGray;
                    }

                    // Take the absolute difference between each consecutive frames and threshold the
                    // resulting mask. After converting both frames to gray scale
                    if (refFrame != null) {
                        // Take absolute difference between the two gray images
                        Core.absdiff(frame.mGray, refFrame, absDiffImg);

                        // Threshold the intensity image at a given intensity value
                        Imgproc.threshold(absDiffImg, mask, 22, 255, Imgproc.THRESH_BINARY);

                        // blur one more time to get rid of noise
                        Imgproc.blur(mask, mask, new Size(8, 8));

                        // threshold blured mask to obtain our final mask
                        Imgproc.threshold(mask, mask, 22, 255, Imgproc.THRESH_BINARY);
                    }

                    // if processing method is sequential set the previous gray frame to current one
                    // in preparation for next frame processing
                    if (frame.getProcMethod() == ImageProcessParam.BG_SUBTRACT_DIFF_ABS_SEQ) {
                        // set the previous frame to current frame for next loop
                        frame.previousGray = frame.mGray.clone();
                    }
                } break;
            }

            if (!ignoreMorph) {
                // 3. Use morphological operators to enhance the image if user selects this option
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
