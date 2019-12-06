package com.example.ARmirrorS.Server.Constants;

public class ImageProcessParam {
    public static final int BG_SUBTRACT_UNDEFINED           = 0;

    public static final int BG_SUBTRACT_BINARY              = 1;
    public static final int BG_SUBTRACT_BINARY_ONLY         = 11;
    public static final int BG_SUBTRACT_BINARY_INV          = 12;
    public static final int BG_SUBTRACT_BINARY_TRUNCATE     = 13;
    public static final int BG_SUBTRACT_BINARY_SET2ZERO     = 14;
    public static final int BG_SUBTRACT_BINARY_SET2ZERO_INV = 15;

    public static final int BG_SUBTRACT_ADAPTIVE            = 2;
    public static final int BG_SUBTRACT_ADAPTIVE_MEAN       = 20;
    public static final int BG_SUBTRACT_ADAPTIVE_GAUSSIAN   = 21;

    public static final int BG_SUBTRACT_OTSU                = 3;

    public static final int BG_SUBTRACT_CANNY               = 4;
    public static final int BG_SUBTRACT_CANNY_CONT          = 41;
    public static final int BG_SUBTRACT_CANNY_BIN           = 42;
    public static final int BG_SUBTRACT_CANNY_BIN_INV       = 43;
    public static final int BG_SUBTRACT_CANNY_ADAPTIVE      = 44;
    public static final int BG_SUBTRACT_CANNY_ADAPTIVE_INV  = 45;

    public static final int BG_SUBTRACT_HSV                 = 5;

    public static final int BG_SUBTRACT_DIFF                = 6;
    public static final int BG_SUBTRACT_DIFF_ABS_1ST        = 61;
    public static final int BG_SUBTRACT_DIFF_ABS_SEQ        = 62;
    public static final int BG_SUBTRACT_DIFF_MOG2           = 63;
    public static final int BG_SUBTRACT_DIFF_KNN            = 64;
    public static final int BG_SUBTRACT_DIFF_GMG            = 65;

}
