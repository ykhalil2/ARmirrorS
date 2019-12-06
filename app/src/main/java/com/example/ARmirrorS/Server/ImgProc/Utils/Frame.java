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

public class Frame {

    // Used to get RGB image Gray Image and transpose as well as an empty intermediate frame as a
    // buffer for processing work.
    public Mat mRgba;
    public Mat mGray;
    public Mat mRgbaT;
    public Mat mIntermediateMat;

    // get camera resolution.
    private int frameWidthX;
    private int frameHeightY;

    // set the processing method and camera mode chosen by the user.
    private int processingMethod;
    private int cameraMode;

    // set special override parameters for Canny
    private boolean ignoreMeanHSV     = false;
    private boolean ignoreEnhancement = false;

    // parameters used for MOG and KNN subtraction
    private boolean detectShadows     = false;

    // set special override parameters. Applies to all processing methods
    private boolean ignoreMorphology  = true;
    private boolean invertMask        = false;

    // set special Absolute diff flages
    private static boolean capturedFirstFrame   = false;


    // Setup a place holder for previous frame in case we are doing absolute difference
    public static Mat previousGray = null;
    public static Mat firstGray    = null;

    // Setup Background Subtraction Buffers for diff methods.
    public BackgroundSubtractorMOG2 backgroundBufferMOG2;
    public BackgroundSubtractorKNN backgroundBufferKNN;


    // set Parent Activity
    Activity parentActivity;

    // constructor
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

    public void setResolution(int setX, int setY){
        frameWidthX  = setX;
        frameHeightY = setY;
    }




    public void    setCameraMode(int setMode) { cameraMode = setMode; }
    public int     getCameraMode() { return cameraMode; }

    public void    setProcMethod(int setMethod) { processingMethod = setMethod; }
    public int     getProcMethod() { return processingMethod; }

    public void    setDetectShadows(boolean value) { detectShadows = value; }
    public boolean getDetectShadows() { return  detectShadows; }

    public void    setIgnoreMeanHSV(boolean value) {
        ignoreMeanHSV = value;
    }
    public boolean getIgnoreMeanHSV() {
        return ignoreMeanHSV;
    }

    public void    setIgnoreEnhance(boolean value) {
        ignoreEnhancement = value;
    }
    public boolean getIgnoreEnhance() {
        return ignoreEnhancement;
    }

    public void    setIgnoreMorphology(boolean value) { ignoreMorphology = value; }
    public boolean getIgnoreMorphology() {
        return ignoreMorphology;
    }

    public void    setInvertMask(boolean value) { invertMask = value; }
    public boolean getInvertMask() { return invertMask; }

    public void    setCaptured1stFrame(boolean value) { capturedFirstFrame = value; }
    public boolean getCaptured1stFrame() { return capturedFirstFrame; }

    public void    setRgba(Mat current){
        mRgba = current;
    }
    public Mat     getRgbaT(){
        return mRgbaT;
    }

    public void    setGray(Mat current){
        mGray = current;
    }
    public Mat     getGray(){
        return mGray;
    }


    public void release() {
        mRgba.release();
        mGray.release();
        mRgbaT.release();
        mIntermediateMat.release();
    }

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
