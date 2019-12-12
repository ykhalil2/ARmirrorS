package com.example.ARmirrorS.Server.Constants;

import org.opencv.imgproc.Imgproc;

import java.util.HashMap;
import java.util.Map;

/**
 * <h1>Class AdaptiveCvMap</h1>
 * Class <b>AdaptiveCvMap</b> Map between locally defined binary processing methods and their counter
 * in OpenCV library to easily translate between the two.
 *
 * <p>
 *
 * @author Yussuf Khalil, Daniel King
 * @author ykhalil2@illinois.edu, dking32@illinois.edu
 *
 * @version 1.1
 * @since 2019-12-05
 */

public class AdaptiveCvMap {

    /** Hash Map between internal Adaptive processing Key method and its OpenCV value.*/
    public static final Map<Integer, Integer> map = new HashMap<Integer, Integer>();

    static {
        map.put(ImageProcessParam.BG_SUBTRACT_ADAPTIVE_GAUSSIAN, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C);
        map.put(ImageProcessParam.BG_SUBTRACT_ADAPTIVE_MEAN,     Imgproc.ADAPTIVE_THRESH_MEAN_C);
    }
}
