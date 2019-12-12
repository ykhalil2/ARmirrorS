package com.example.ARmirrorS.Server.Constants;

import org.opencv.imgproc.Imgproc;

import java.util.HashMap;
import java.util.Map;

/**
 * <h1>Class BinaryCvMap</h1>
 * Class <b>BinaryCvMap</b> Map between locally defined binary processing methods and their counter
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

public class BinaryCvMap {

    /** Hash Map between internal Binary processing Key method and its OpenCV value.*/
    public static final Map<Integer, Integer> map = new HashMap<Integer, Integer>();

    static {
        map.put(ImageProcessParam.BG_SUBTRACT_BINARY_ONLY,          Imgproc.THRESH_BINARY);
        map.put(ImageProcessParam.BG_SUBTRACT_BINARY_INV,           Imgproc.THRESH_BINARY_INV);
        map.put(ImageProcessParam.BG_SUBTRACT_BINARY_SET2ZERO,      Imgproc.THRESH_TOZERO);
        map.put(ImageProcessParam.BG_SUBTRACT_BINARY_SET2ZERO_INV,  Imgproc.THRESH_TOZERO_INV);
        map.put(ImageProcessParam.BG_SUBTRACT_BINARY_TRUNCATE,      Imgproc.THRESH_TRUNC);
    }
}
