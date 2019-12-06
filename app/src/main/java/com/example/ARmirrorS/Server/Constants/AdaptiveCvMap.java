package com.example.ARmirrorS.Server.Constants;

import org.opencv.imgproc.Imgproc;

import java.util.HashMap;
import java.util.Map;

public class AdaptiveCvMap {

    public static final Map<Integer, Integer> map = new HashMap<Integer, Integer>();

    static {
        map.put(ImageProcessParam.BG_SUBTRACT_ADAPTIVE_GAUSSIAN, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C);
        map.put(ImageProcessParam.BG_SUBTRACT_ADAPTIVE_MEAN,     Imgproc.ADAPTIVE_THRESH_MEAN_C);
    }
}
