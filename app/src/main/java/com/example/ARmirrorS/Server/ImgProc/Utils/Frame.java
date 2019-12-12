package com.example.ARmirrorS.Server.ImgProc.Utils;

import android.app.Activity;

import com.example.ARmirrorS.Server.Constants.CameraParam;
import com.example.ARmirrorS.Server.Constants.ImageProcessParam;
import com.example.ARmirrorS.Server.ImgProc.Algorithms.Adaptive;
import com.example.ARmirrorS.Server.ImgProc.Algorithms.Binary;
import com.example.ARmirrorS.Server.ImgProc.Algorithms.Canny;
import com.example.ARmirrorS.Server.ImgProc.Algorithms.Diff;
import com.example.ARmirrorS.Server.ImgProc.Algorithms.Hsv;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.video.BackgroundSubtractorKNN;
import org.opencv.video.BackgroundSubtractorMOG2;

/**
 * <h1>Class Frame</h1>
 * Class <b>Frame</b> used to store all channels associated with the current frame being processed
 * including color, gray, HSV, etc. It is responsible for returning the appropriate mask after
 * frame processing is completed to be displayed on the screen (server side)
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
 */

public class Frame {

    /**Color image for current processed frame.*/
    public Mat mRgba;
    /**Gray image for current processed frame.*/
    public Mat mGray;
    /**buffer for processing work.*/
    public Mat mRgbaT;
    /**empty intermediate frame as buffer for processing work.*/
    public Mat mIntermediateMat;
    /**Resolution in X direction.*/
    private int frameWidthX;
    /**Resolution in Y direction.*/
    private int frameHeightY;
    /**Current processing method for foreground separation.*/
    private int processingMethod;
    /**Current Frame Display mode (Gray, color, masked, pixelated).*/
    private int cameraMode;
    // set special override parameters for Canny
    /**Override parameters for Canny Processing (Ignore Mean computed HSV value of image from Hue Channel.*/
    private boolean ignoreMeanHSV     = false;
    /**Override parameters for Canny Processing (Ignore Morphing kernel for smoothing edges).*/
    private boolean ignoreEnhancement = false;
    /**parameters used for MOG and KNN subtraction.*/
    private boolean detectShadows     = false;
    /**set special override parameters. Applies to all processing methods.*/
    private boolean ignoreMorphology  = true;
    /**Inverting the blacks and Whites are set to false for any processed mask by default.*/
    private boolean invertMask        = false;
    /**Absolute diff flag to indicate whether reference frame been captured or not.*/
    private static boolean capturedFirstFrame   = false;
    /**A place holder for previous frame in case we are doing absolute Sequential difference.*/
    public static Mat previousGray = null;
    /**Setup a place holder for previous frame in case we are doing absolute 1st Frame difference.*/
    public static Mat firstGray    = null;
    /**Background Subtraction Buffers for MOG2 subtraction method.*/
    public BackgroundSubtractorMOG2 backgroundBufferMOG2;
    /**Background Subtraction Buffers for KNN subtraction Method.*/
    public BackgroundSubtractorKNN backgroundBufferKNN;
    /**Parent Activity Running Context.*/
    Activity parentActivity;

    // constructor

    /**
     * Frame Constructors. Stores all required parameters associated with the frame received from
     * OpenCV onUpdate.
     *
     * @param width pixel size in horizontal direction (image width).
     * @param height pixel size in Vertical direction (image height).
     * @param setMethod Processing method selected by user from spinner.
     * @param setMode return frame display mode (gray, color, pixels, or masked image)
     * @param setParentActivity Parent Activity Running Context
     */
    public Frame(int width, int height, int setMethod, int setMode, Activity setParentActivity) {
        mRgba               = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat    = new Mat(height, width, CvType.CV_8UC4);
        mGray               = new Mat(height, width, CvType.CV_8UC1);
        mRgbaT              = new Mat(height, width, CvType.CV_8UC1);

        frameWidthX         = width;
        frameHeightY        = height;

        processingMethod    = setMethod;
        cameraMode          = setMode;
        parentActivity      = setParentActivity;

        previousGray        = null;
        firstGray           = null;
    }

    /**
     * Perform a frame reset when the user changes the resolution while a frame is being
     * processed.
     *
     * @deprecated since version 1.1.
     *
     * @param width pixels in X direction.
     * @param height pixels in Y direction.
     */
    public void frameReset (int width, int height) {
        mRgba               = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat    = new Mat(height, width, CvType.CV_8UC4);
        mGray               = new Mat(height, width, CvType.CV_8UC1);
        mRgbaT              = new Mat(height, width, CvType.CV_8UC1);

        frameWidthX         = width;
        frameHeightY        = height;

        previousGray        = null;
        firstGray           = null;
    }

    /**
     * Set the resolution of following frames.
     *
     * @deprecated since version 1.1.
     *
     * @param setX pixels in X direction.
     * @param setY pixels in Y direction.
     */
    public void setResolution(int setX, int setY){
        frameWidthX  = setX;
        frameHeightY = setY;
    }

    /**
     * Sets the Camera Frame preview mode.
     *
     * @param setMode gray | color | masked | pixelated
     */
    public void    setCameraMode(int setMode) {
        cameraMode = setMode;
    }

    /**
     * Retrieves the current frame display mode
     *
     * @return cameraMode | color| gray | pixels | mask
     */
    public int     getCameraMode() { return cameraMode; }

    /**
     * Set the current Processing method of the frame.
     *
     * @param setMethod Selected processing method of current frame.
     */
    public void    setProcMethod(int setMethod) {
        processingMethod = setMethod;
    }

    /**
     * Retrieve the selected foreground extraction method.
     *
     * @return processing method chosen for current frame.
     */
    public int     getProcMethod() {
        return processingMethod;
    }

    /**
     * Set the mask processing and if shadows if to be detected or not.
     * MOG and KNN subtraction.
     *
     * @param value flag for shadow detection to display alongside with mask.
     */
    public void    setDetectShadows(boolean value) {
        detectShadows = value;
    }

    /**
     *  Obtain if mask is to be processed and if shadows to be detected or not.
     *  used for MOG and KNN subtraction.
     *
     * @return detectShadows | boolean flag.
     */
    public boolean getDetectShadows() { return  detectShadows; }

    /**
     * Override parameters for Canny Processing (Ignore Mean computed HSV value of image from Hue
     * Channel or not).
     *
     * @param value boolean flag to ignore the mean HSV value of the image.
     */
    public void    setIgnoreMeanHSV(boolean value) {
        ignoreMeanHSV = value;
    }

    /**
     * Get parameters for Canny Processing and weather to ignore the Mean computed HSV value of
     * image from Hue Channel.
     *
     * @return boolean flag to ignore the mean HSV value of the image.
     */
    public boolean getIgnoreMeanHSV() {
        return ignoreMeanHSV;
    }

    /**
     * Ignore Morphing kernel for smoothing edges for Canny Processing method.
     *
     * @param value set true to ignore the mean HSV value of the image
     */
    public void    setIgnoreEnhance(boolean value) {
        ignoreEnhancement = value;
    }

    /**
     * Obtain flag to check if we are to Ignore Morphing kernel for smoothing edges (Canny processing
     * method).
     *
     * @return boolean
     */
    public boolean getIgnoreEnhance() {
        return ignoreEnhancement;
    }

    /**
     * Set if morphing of the mask is to be ignored or not.
     *
     * @param value boolean flag to indicate if processing should not enhance the masked image.
     */
    public void    setIgnoreMorphology(boolean value) {
        ignoreMorphology = value;
    }

    /**
     * Retrieve the morphing kernel ignore flag.
     *
     * @return true if mask enhancement is to be ignored or not.
     */
    public boolean getIgnoreMorphology() {
        return ignoreMorphology;
    }

    /**
     * Set if mask inversion is to be used flipping black to whites.
     *
     * @param value true to invert the mask.
     */
    public void    setInvertMask(boolean value) {
        invertMask = value;
    }

    /**
     * Retrieve mask inversion flag.
     *
     * @return bollean flag indicated if mask is to be inverted or not.
     */
    public boolean getInvertMask() {
        return invertMask;
    }

    /**
     * In case of Absolute diff set a flag to indicate whether reference frame been captured or not
     *
     * @param value true if reference first frame has been captured to perform subsequent difference.
     */
    public void    setCaptured1stFrame(boolean value) {
        capturedFirstFrame = value;
    }

    /**
     * Check to see if first reference frame has been captured for absolute frame difference
     * foreground extraction methods (1st and sequential).
     *
     * @return true if reference first frame has been captured to perform subsequent difference.
     */
    public boolean getCaptured1stFrame() {
        return capturedFirstFrame;
    }

    /**
     * Sets the color channel of the current frame being processed.
     *
     * @param current the current color channel of the frame being processed
     */
    public void    setRgba(Mat current){
        mRgba = current;
    }

    /**
     * Retrieve Transpose of the Color Frame under processing.
     *
     * @deprecated since version 1.1
     *
     * @return transpose of color frame.
     */
    public Mat     getRgbaT(){
        return mRgbaT;
    }

    /**
     * Sets the gray scale channel of the current frame being processed.
     *
     * @param current the current gray channel of the frame being processed
     */
    public void    setGray(Mat current){
        mGray = current;
    }

    /**
     * Retrieve the Gray image of the Frame being processed.
     *
     * @deprecated since version 1.1
     *
     * @return gray frame.
     */
    public Mat     getGray(){
        return mGray;
    }

    /**
     * deallocates the memory that was associated with the current frame. Mainly the color and Gray
     * scale images as well as intermediate frames used internally for processing.
     */
    public void release() {
        mRgba.release();
        mGray.release();
        mRgbaT.release();
        mIntermediateMat.release();
    }

    /**
     * Depending on chosen image processing method to extract the foreground from background call
     * corresponding algorithm to perform extraction. In case only a general method is selected
     * from spinner and no sub-method is chosen from Radio buttons, return either gray or color
     * frame to be displayed and perform no processing.
     *
     * @return black and white mask or masked RGB or gray image
     */
    public Mat process() {

        switch (processingMethod) {

            case ImageProcessParam.BG_SUBTRACT_HSV: {
                return Hsv.subtract(parentActivity, this);
            }

            case ImageProcessParam.BG_SUBTRACT_CANNY_CONT:
            case ImageProcessParam.BG_SUBTRACT_CANNY_BIN:
            case ImageProcessParam.BG_SUBTRACT_CANNY_BIN_INV:
            case ImageProcessParam.BG_SUBTRACT_CANNY_ADAPTIVE:
            case ImageProcessParam.BG_SUBTRACT_CANNY_ADAPTIVE_INV: {
                return Canny.subtract(parentActivity, this);
            }

            case ImageProcessParam.BG_SUBTRACT_BINARY_ONLY:
            case ImageProcessParam.BG_SUBTRACT_BINARY_INV:
            case ImageProcessParam.BG_SUBTRACT_BINARY_SET2ZERO:
            case ImageProcessParam.BG_SUBTRACT_BINARY_SET2ZERO_INV:
            case ImageProcessParam.BG_SUBTRACT_BINARY_TRUNCATE: {
                return Binary.subtract(parentActivity, this);
            }

            case ImageProcessParam.BG_SUBTRACT_ADAPTIVE_MEAN:
            case ImageProcessParam.BG_SUBTRACT_ADAPTIVE_GAUSSIAN: {
                return Adaptive.subtract(parentActivity, this);
            }

            case ImageProcessParam.BG_SUBTRACT_DIFF_ABS_1ST:
            case ImageProcessParam.BG_SUBTRACT_DIFF_ABS_SEQ:
            case ImageProcessParam.BG_SUBTRACT_DIFF_MOG2:
            case ImageProcessParam.BG_SUBTRACT_DIFF_KNN:
            case ImageProcessParam.BG_SUBTRACT_DIFF_GMG: {
                return Diff.subtract(parentActivity, this);
            }

            case ImageProcessParam.BG_SUBTRACT_OTSU: {
//               return Otsu.subtract(parentActivity, this);
            } break;

            case ImageProcessParam.BG_SUBTRACT_DIFF:
            case ImageProcessParam.BG_SUBTRACT_BINARY:
            case ImageProcessParam.BG_SUBTRACT_ADAPTIVE:
            case ImageProcessParam.BG_SUBTRACT_CANNY:
            case ImageProcessParam.BG_SUBTRACT_UNDEFINED:
            default:
                if (cameraMode == CameraParam.VIEW_MODE_GRAY) {
                    return mGray;
                }
                break;
        }

        return mRgba;
    }
}
