package com.example.ARmirrorS.Server.Constants;

import org.opencv.imgproc.Imgproc;

import java.util.HashMap;
import java.util.Map;

public class BinaryCvMap {

    public static final Map<Integer, Integer> map = new HashMap<Integer, Integer>();

    static {
        map.put(ImageProcessParam.BG_SUBTRACT_BINARY_ONLY,          Imgproc.THRESH_BINARY);
        map.put(ImageProcessParam.BG_SUBTRACT_BINARY_INV,           Imgproc.THRESH_BINARY_INV);
        map.put(ImageProcessParam.BG_SUBTRACT_BINARY_SET2ZERO,      Imgproc.THRESH_TOZERO);
        map.put(ImageProcessParam.BG_SUBTRACT_BINARY_SET2ZERO_INV,  Imgproc.THRESH_TOZERO_INV);
        map.put(ImageProcessParam.BG_SUBTRACT_BINARY_TRUNCATE,      Imgproc.THRESH_TRUNC);
    }
}
