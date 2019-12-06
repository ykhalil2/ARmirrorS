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


public class Diff {

    private static Activity parent;
    private static Frame frame;

    private static boolean wait                 = true;
    private static boolean ignoreMorph          = true;
    private static double  kernelSize           = 12;

    private static double  learningRate         =.003;
    private static double  backgroundRatio      = 0.5;
    private static int     maxHistoryFrames     = 250;
    private static double  complexityReduction  = 0.05;
    private static boolean detectShadows        = false;


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


    private static Mat process() {
        Mat mask       = new Mat(frame.mGray.size(), CvType.CV_8UC1);
        Mat absDiffImg = new Mat(frame.mGray.size(), CvType.CV_8UC1);
        Mat refFrame;

        // Check that the user selected one of the sub types of binary thresholding otherwise
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
