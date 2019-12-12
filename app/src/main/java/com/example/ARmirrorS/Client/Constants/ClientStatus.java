package com.example.ARmirrorS.Client.Constants;

/**
 * <h1>Class ClientStatus</h1>
 * Class <b>ClientStatus</b> defines values for the Status of Client Socket with the server.
 *
 * <p>
 *
 * @author Yussuf Khalil, Daniel King
 * @author ykhalil2@illinois.edu, dking32@illinois.edu
 *
 * @version 1.1
 * @since 2019-12-05
 */

public class ClientStatus {

    /**Client not connected to server.*/
    public static final String ID_NOTCONNECTED = "Starting";
    /**Trying to Establish Connection to the server.*/
    public static final String ID_CONNECTING   = "Trying to Establish Connection to the server";
    /**Client Connected but still no message back from server.*/
    public static final String ID_UNDEFINED    = "Client Connected but still no message back from server";
    /**Client Connected and receiving data from server.*/
    public static final String ID_ESTABLISHED  = "Client Connected and receiving data from server";
    /**Client Connected as a Master.*/
    public static final String ID_MASTER       = "Client Connected as a Master";
    /**Client Connected as a Slave.*/
    public static final String ID_SLAVE        = "Client Connected as a Slave";
    /**Client Not Connected to Server.*/
    public static final String ID_DISCONNECTED = "Client Not Connected to Server";
    /**Unable to connect to server at specified IP.*/
    public static final String ID_ERR1         = "Unable to connect to server at specified IP";
    /**Exception caught. Socket is released.*/
    public static final String ID_ERR2         = "Exception caught. Socket is released";
}
