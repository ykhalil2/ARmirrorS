package com.example.ARmirrorS.Constatnts;

/**
 * <h1>Class UserMode</h1>
 * Class <b>UserMode</b> The mode in which the application will be running. This will be set by user
 * at start of the application and can be either a server or a client Application. Each choice, will
 * run completly different activities and methods.
 * <p>
 *
 * @author Yussuf Khalil, Daniel King
 * @author ykhalil2@illinois.edu, dking32@illinois.edu
 *
 * @version 1.1
 * @since 2019-12-05
 */

public class UserMode {

    /**Undefined Mode for application set by default at start time.*/
    public static final int USER_MODE_UNDEFINED = -1;
    /**Application is running as a Server.*/
    public static final int USER_MODE_SERVER    =  0;
    /**Application will be running as a client.*/
    public static final int USER_MODE_CLIENT    =  1;
}
