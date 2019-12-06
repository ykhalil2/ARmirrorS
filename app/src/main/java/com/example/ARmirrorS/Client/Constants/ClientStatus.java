package com.example.ARmirrorS.Client.Constants;

public class ClientStatus {
    public static final String ID_NOTCONNECTED = "Starting";
    public static final String ID_CONNECTING   = "Trying to Establish Connection to the server";
    public static final String ID_UNDEFINED    = "Client Connected but still no message back from server";
    public static final String ID_ESTABLISHED  = "Client Connected and receiving data from server";
    public static final String ID_MASTER       = "Client Connected as a Master";
    public static final String ID_SLAVE        = "Client Connected as a Slave";
    public static final String ID_DISCONNECTED = "Client Not Connected to Server";
    public static final String ID_ERR1         = "Unable to connect to server at specified IP";
    public static final String ID_ERR2         = "Exception caught. Socket is released";
}
