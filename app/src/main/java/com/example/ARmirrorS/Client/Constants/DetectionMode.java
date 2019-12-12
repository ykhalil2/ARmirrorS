package com.example.ARmirrorS.Client.Constants;

/**
 * <h1>Class DetectionMode</h1>
 * Class <b>DetectionMode</b> define the Plane detection mode for AR Core configuration that is
 * selected by the user. Either Horizontal or Vertical plane detection is allowed, in addition to
 * Augmented image tracking is supported.
 *
 * <p>
 *
 * @author Yussuf Khalil, Daniel King
 * @author ykhalil2@illinois.edu, dking32@illinois.edu
 *
 * @version 1.1
 * @since 2019-12-05
 */

public class DetectionMode {

    /**No plane detection mode for AR Core has been selected.*/
    public static final int ID_UNDEFINED        = 0;
    /**Augmented Image Detection Mode.*/
    public static final int ID_AUGMENTED_IMG    = 1;
    /**Vertical Plane Detection Mode.*/
    public static final int ID_VERTICAL_PLANE   = 2;
    /**Horizontal Plane detection processing.*/
    public static final int ID_HORIZONTAL_PLANE = 3;
}
