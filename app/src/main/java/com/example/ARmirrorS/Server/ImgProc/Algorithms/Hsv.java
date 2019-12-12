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

/**
 * <h1>Class Hsv</h1>
 * Class <b>Hsv</b> uses simple thresholding to extract foreground from background. Best used to
 * track specific colors and tones based on the minimum and maximum values selected for Hue,
 * Saturation, and Value.
 *
 *  In HSV, it is more easier to represent a color than RGB color-space.
 *  -Take each frame of the video
 *  -Convert from BGR to HSV color-space
 *  -We threshold the HSV image for a range of color based on min and max of H/S/V
 *  -Now extract the foreground alone.
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

public class Hsv {

    /**Parent Activity Running Context.*/
    private static Activity parent;
    /**Current Frame Being Processed.*/
    private static Frame frame;
    /**Override parameters for HSV Processing (Ignore Morphing kernel for smoothing edges).*/
    private static boolean ignoreMorph   = true;
    /**Kernel Size used in morphing and blurring final mask.*/
    private static double kernelSize    = 12;
    /**Lower Hue value. Hue range is [0,179].*/
    private static double lh            = -1.0;
    /**Lower value Range. Value range is [0,255].*/
    private static double lv            = -1.0;
    /**Lower Saturation value. Hue range is [0,255].*/
    private static double ls            = -1.0;
    /**Upper Hue value. Hue range is [0,179].*/
    private static double uh            = -1.0;
    /**Upper value Range. Value range is [0,255].*/
    private static double uv            = -1.0;
    /**Upper Saturation value. Hue range is [0,255].*/
    private static double us            = -1.0;

    /**
     * Get all values in the seekBar the user input and store locally. And start processing the
     * passed in frame. It wait and just return the gray or color channel of the frame in seek bars
     * are not yet initialized.
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
        if (lh == -1.0) {
            return frame.mRgba;
        } else {
            return process();
        }
    }

    /**
     * Retrieve maximum and minimum thresholds as well as morphing enhancement flag. Also obtain
     * minimum and maximum hue, saturation and value ranges. This function is called on every frame
     * to read the seek bars and if the user has made any changes.
     *
     */
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

    /**
     * Process the current frame based on HSV thresholding method values selected by user.
     *
     * 1. Start by cleaning the original Image and removing noise to easier handle the segmentation
     *    (NOTE:- frame.mIntermediate will hold our hsv image)
     * 2. create a new frame of proper size to store HSV value & Obtain HSV image by converting RGB
     *    frame
     * 3. Set a scalar to maintain lower and upper threshold values from user input
     * 4. Apply HSV threshold on HSV image blurred previously and store in a new mask
     * 5. Use morphological operators to enhance the image if user selects this option
     *
     * @return processed frame this can be masked color or gray image. or just black and white mask.
     */
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
