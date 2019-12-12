package com.example.ARmirrorS.Server.Constants;

/**
 * <h1>Class ImageProcessParam</h1>
 * Class <b>ImageProcessParam</b> defines the Background extraction algorithm selected by the user
 * from the server side.
 * <p>
 *
 * @author Yussuf Khalil, Daniel King
 * @author ykhalil2@illinois.edu, dking32@illinois.edu
 *
 * @version 1.1
 * @since 2019-12-05
 */

public class ImageProcessParam {
    /**No Background processing method is selected yet.*/
    public static final int BG_SUBTRACT_UNDEFINED           = 0;

    /**Binary Threshold processing algorithm is selected but no sub-method.*/
    public static final int BG_SUBTRACT_BINARY              = 1;
    /**Binary processing Threshold algorithm only.*/
    public static final int BG_SUBTRACT_BINARY_ONLY         = 11;
    /**Inverse Binary Threshold processing algorithm.*/
    public static final int BG_SUBTRACT_BINARY_INV          = 12;
    /**Truncation Binary Threshold processing algorithm.*/
    public static final int BG_SUBTRACT_BINARY_TRUNCATE     = 13;
    /**Set-To-Zero Binary Threshold processing algorithm.*/
    public static final int BG_SUBTRACT_BINARY_SET2ZERO     = 14;
    /**Inverse Set-Ro-Zero Binary Threshold processing algorithm.*/
    public static final int BG_SUBTRACT_BINARY_SET2ZERO_INV = 15;

    /**Adaptive Threshold processing algorithm is selected but no sub-method.*/
    public static final int BG_SUBTRACT_ADAPTIVE            = 2;
    /**Adaptive MEAN Threshold processing algorithm.*/
    public static final int BG_SUBTRACT_ADAPTIVE_MEAN       = 20;
    /**Adaptive GAUSSIAN Threshold processing algorithm.*/
    public static final int BG_SUBTRACT_ADAPTIVE_GAUSSIAN   = 21;

    /**OTSU Threshold processing algorithm is selected. (currently not implemented).*/
    public static final int BG_SUBTRACT_OTSU                = 3;

    /**Canny Edge Detection Threshold processing algorithm is selected but no sub-method.*/
    public static final int BG_SUBTRACT_CANNY               = 4;
    /**Canny Edge Detection Threshold processing algorithm for Contours only (no foreground Extraction).*/
    public static final int BG_SUBTRACT_CANNY_CONT          = 41;
    /**Canny Edge Detection processing algorithm with Binary threshold.*/
    public static final int BG_SUBTRACT_CANNY_BIN           = 42;
    /**Canny Edge Detection processing algorithm with Binary Inverse threshold.*/
    public static final int BG_SUBTRACT_CANNY_BIN_INV       = 43;
    /**Canny Edge Detection processing algorithm with Adaptive Mean threshold.*/
    public static final int BG_SUBTRACT_CANNY_ADAPTIVE      = 44;
    /**Canny Edge Detection processing algorithm with Inverse Adaptive Mean threshold.*/
    public static final int BG_SUBTRACT_CANNY_ADAPTIVE_INV  = 45;

    /**HSV Color detection Threshold processing algorithm.*/
    public static final int BG_SUBTRACT_HSV                 = 5;

    /**Frame Subtraction processing with no sub-method selected.*/
    public static final int BG_SUBTRACT_DIFF                = 6;
    /**Frame Subtraction processing with reference 1st frame captured.*/
    public static final int BG_SUBTRACT_DIFF_ABS_1ST        = 61;
    /**Frame Subtraction processing with previous frame captured (sequential subtraction).*/
    public static final int BG_SUBTRACT_DIFF_ABS_SEQ        = 62;
    /**Frame Subtraction processing with MOG2 algorithm.*/
    public static final int BG_SUBTRACT_DIFF_MOG2           = 63;
    /**Frame Subtraction processing with KNN algorithm.*/
    public static final int BG_SUBTRACT_DIFF_KNN            = 64;
    /**Frame Subtraction processing with GMG algorithm. (not implemented in version 1.1)*/
    public static final int BG_SUBTRACT_DIFF_GMG            = 65;
}
