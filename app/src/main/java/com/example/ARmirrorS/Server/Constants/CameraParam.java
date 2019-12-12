package com.example.ARmirrorS.Server.Constants;

/**
 * <h1>Class CameraParam</h1>
 * Class <b>CameraParam</b> defines the Video display frame mode to the user. This can be either
 * color or gray scale. As well as masked images and pixelated version with a rectangular region
 * of interest superimposed to indicate the exact image to be transfered to the client.
 *
 * <p>
 *
 * @author Yussuf Khalil, Daniel King
 * @author ykhalil2@illinois.edu, dking32@illinois.edu
 *
 * @version 1.1
 * @since 2019-12-05
 */

public class CameraParam {
    /** Color image display of processed frame.*/
    public static final int VIEW_MODE_RGBA        = 0;
    /** Gray scale image display of processed frame.*/
    public static final int VIEW_MODE_GRAY        = 1;
    /** Masked image display of processed frame. This can be superimposed on gray and color image.*/
    public static final int VIEW_MODE_MASK        = 2;
    /** pixelated gray scale with bounding ROI rectangle.*/
    public static final int VIEW_MODE_PIXELATED   = 3;
}
