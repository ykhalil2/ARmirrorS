package com.example.ARmirrorS.Server.ImgProc.Algorithms;

import android.app.Activity;
import android.widget.SeekBar;

import com.example.ARmirrorS.R;
import com.example.ARmirrorS.Server.ImgProc.Utils.Frame;
import com.example.ARmirrorS.Server.ImgProc.Utils.Utils;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class Hsv {

    private static Activity parent;
    private static Frame frame;

    private static boolean ignoreMorph   = true;

    private static double kernelSize    = 12;
    private static double lh            = -1.0;
    private static double lv            = -1.0;
    private static double ls            = -1.0;
    private static double uh            = -1.0;
    private static double uv            = -1.0;
    private static double us            = -1.0;

    public static Mat subtract(final Activity parentActivity, Frame setFrame) {

        // First Set parent Activity and local frame reference so we don't pass them privately
        parent = parentActivity;
        frame = setFrame;

        // Get all values in the seekBar the user inputed and store locally
        getGuiParameters();

        // Start Processing the frame Wait until SeekBars initialize or we get null pointer
        // exception. Until then just return the RGBA color image.
        if (lh == -1.0) {
            return frame.mRgba;
        } else {
            return process();
        }
    }


    private static void getGuiParameters() {
        SeekBar lhSeek      = parent.findViewById(R.id.seekBarLh);
        SeekBar lvSeek      = parent.findViewById(R.id.seekBarLv);
        SeekBar lsSeek      = parent.findViewById(R.id.seekBarLs);
        SeekBar uhSeek      = parent.findViewById(R.id.seekBarUh);
        SeekBar uvSeek      = parent.findViewById(R.id.seekBarUv);
        SeekBar usSeek      = parent.findViewById(R.id.seekBarUs);
        SeekBar morphSeek   = parent.findViewById(R.id.seekBarMorphKernel);

        if (lhSeek != null) {
            lh              = (double) lhSeek.getProgress();
            lv              = (double) lvSeek.getProgress();
            ls              = (double) lsSeek.getProgress();
            uh              = (double) uhSeek.getProgress();
            uv              = (double) uvSeek.getProgress();
            us              = (double) usSeek.getProgress();
            kernelSize      = (double) morphSeek.getProgress();
            ignoreMorph     = frame.getIgnoreMorphology();
        }
    }


    private static Mat process() {
        Mat mask = new Mat();

        // 1. Start by cleaning the original Image and removing noise to easier handle the
        // segmentation (NOTE:- frame.mIntermediate will hold our hsv image)
        Imgproc.blur(frame.mRgba, frame.mIntermediateMat, new Size(6,6));

        // 2. create a new frame of proper size to store HSV value &
        //    Obtain HSV image by converting RGB frame
        Imgproc.cvtColor(frame.mIntermediateMat, frame.mIntermediateMat, Imgproc.COLOR_BGR2HSV);

        // 3. Set a scalar to maintain lower and upper threshold values from user input
        Scalar minThresholdLHS = new Scalar(lh, ls, lv);
        Scalar maxThresholdLHS = new Scalar(uh, us, uv);

        // 4. Apply HSV threshold on HSV image blurred previously and store in a new mask
        Core.inRange(frame.mIntermediateMat, minThresholdLHS, maxThresholdLHS, mask);

        if (!ignoreMorph) {
            // 5. Use morphological operators to enhance the image if user selects this option
            Mat morphMask = Utils.morphImage(mask, kernelSize);
            return Utils.returnDisplayImage(frame, morphMask);
        }

        return Utils.returnDisplayImage(frame, mask);
    }
}
