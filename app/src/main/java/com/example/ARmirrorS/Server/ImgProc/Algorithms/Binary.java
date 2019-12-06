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

public class Binary {
    private static Activity parent;
    private static Frame frame;

    private static boolean wait        = true;
    private static boolean ignoreMorph = true;
    private static double  kernelSize  = 12;

    private static double maxVal;
    private static double threshVal;

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
