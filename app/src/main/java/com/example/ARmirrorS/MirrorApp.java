package com.example.ARmirrorS;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.ARmirrorS.Client.Constants.ClientStatus;
import com.example.ARmirrorS.Client.Constants.DefaultServerURI;
import com.example.ARmirrorS.Client.Constants.DetectionMode;
import com.example.ARmirrorS.Client.Constants.TileMaterial;
import com.example.ARmirrorS.Client.Constants.TileNo;
import com.example.ARmirrorS.Client.Constants.TileShape;
import com.example.ARmirrorS.Client.Socket.ARCoreClient;
import com.example.ARmirrorS.Constatnts.InteractionLevel;
import com.example.ARmirrorS.Constatnts.UserMode;
import com.example.ARmirrorS.Server.Socket.OpenCVServer;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.util.Enumeration;
import java.util.PriorityQueue;

import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.opencv.android.OpenCVLoader;

import static java.util.concurrent.TimeUnit.MILLISECONDS;


public class MirrorApp extends Application {

    private static final String TAG = MirrorApp.class.getSimpleName();

    private static Context sContext;

    //
    // Web Socket Server Handler
    // And associated Parameters
    ////////////////////////////////////////////////////////////////////////////////////////
    private static OpenCVServer webSocketServer;
    private static boolean timerRunning          = false;
    private static String serverStatus           = "Starting";
    // User modes either Server or Client and set defaults
    private static int userMode                  = UserMode.USER_MODE_UNDEFINED;
    private static int interactionLevel          = InteractionLevel.MODE_EXPERT;
    // Start streaming from server to client flag
    private static boolean startStream           = false;
    // use augmented images or vertical plane detection
    private static int mirrorPlacement           = DetectionMode.ID_UNDEFINED;
    private static boolean augmentedImage       = false;
    private static boolean verticalPlanes       = false;

    //
    // Web Socket Client Handler
    // And associated Parameters
    ////////////////////////////////////////////////////////////////////////////////////////
    private static ARCoreClient webSocketClient = null;
    private static URI serverURI                = null;
    private static Draft drafts                 = new Draft_6455();
    private static String clientStatus          = ClientStatus.ID_NOTCONNECTED;
    // Client Set parameters for number of tiles per row/col
    private static int noTiles                  = TileNo.ID_UNDEFINED;
    private static int tileMaterial             = TileMaterial.ID_UNDEFINED;
    private static int tileShape                = TileShape.ID_UNDEFINED;
    // Queue to hold all frames received from the server
    public static PriorityQueue<byte[]> framesQ = new PriorityQueue<>();


    // Load OpenCV for debugging
    static {
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV Loaded Successfully");
        } else {
            Log.d(TAG,"OpenCV Failed to Load");
        }
    }

    /**
     *
     */
    @Override
    public void onCreate() {
        super.onCreate();
        sContext =   getApplicationContext();
        // for debug purpose comment out this line
        // startServer();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////SERVER////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    /**
     *
     */
    private static void startServer() {
        // Get Address of this phone acting as the server. If connected to the net
        // start our web socket server
        InetAddress inetAddress = getInetAddress();
        if (inetAddress != null) {
            // Call Constructor and pass address and port of server to start listening
            // for clients
            webSocketServer = new OpenCVServer(
                    new InetSocketAddress(inetAddress.getHostAddress(), DefaultServerURI.PORT)
            );
            // Start the web socket server
            webSocketServer.start();
            // Update the status of the WebServer
            serverStatus = webSocketServer.getServerStatus();
            // Register Even Bus for Broadcast Messages
            // EventBus.getDefault().unregister(this);
        } else {
            Log.e(TAG, "Unable to lookup IP address");
            serverStatus = "Unable to lookup IP address";
        }
    }

    /**
     *
     * @return
     */
    private static InetAddress getInetAddress() {
        try {
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface networkInterface = (NetworkInterface) en.nextElement();
                for (Enumeration IpAddr = networkInterface.getInetAddresses();
                     IpAddr.hasMoreElements();) {
                    InetAddress inetAddress = (InetAddress) IpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            Log.e(TAG, "Error getting the network interface information");
            serverStatus = "Error getting the network interface information";
        }
        return null;
    }




    //////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////CLIENT////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    private static void startClient(URI uri) {
        serverURI = uri;
        try {
            webSocketClient = new ARCoreClient(serverURI); // , drafts);
            // Block call for 2 seconds until we can connect to the server
            webSocketClient.connectBlocking(2000, MILLISECONDS);
        } catch (Exception e) {
            Log.e(TAG, "Unable to connect to server at specified IP address--------------");
        }

        // if we fail to connect
        if (webSocketClient.getConnection().isOpen() == false) {
            Log.e(TAG, "Unable to connect to server at specified IP address//////////////");
            clientStatus = ClientStatus.ID_ERR1;
            Toast.makeText(getContext(), clientStatus, Toast.LENGTH_LONG).show();
        } else {
            Log.e(TAG, "Connection Successful at Default IP address++++++++++++++++++++++");
            clientStatus = ClientStatus.ID_UNDEFINED;
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////HELPER FUNCTIONS///////////////////////////////////////
    //////////////////////////////////FOR SERVER//////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static String getWebSocketServerStatus() {
        // this means that we have not finished yet and a call was made to this function to early
        if (serverStatus.equals("Starting")) {
            return serverStatus;
        } else {
            return webSocketServer.getServerStatus();
        }
    }

    public static String getClientStatus() {
        return webSocketServer.getClientStatus();
    }

    public static void startWebSocketServer() {
        startServer();
    }

    public static void setInteractionLevel(int value) {
        interactionLevel = value;
    }

    public static void setUserMode(int setUserMode) {
        userMode = setUserMode;
    }

    public static boolean getIsClientConnected() {
        if ( webSocketServer.getCount() >= 1) {
            return true;
        }
        return false;
    }

    public static void setTimerStatus(boolean value) {
        timerRunning = value;
    }

    public static int getInteractionLevel() {
        return interactionLevel;
    }

    public static int getUserMode() {
        return userMode;
    }

    public static  OpenCVServer getServerSocket() {
        return webSocketServer;
    }

    public static boolean getTimerStatus() {
        return timerRunning;
    }


    //////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////HELPER FUNCTIONS///////////////////////////////////////
    //////////////////////////////////FOR CLIENT//////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static void startWebSocketClientConnection(URI uri) {
        startClient(uri);
    }

    public static void startWebSocketClientREConnection() {
        try {
            // Block call for 3 seconds until we can connect to the server
            webSocketClient.close();
            // to block untill we recieve a confirmation comment out
            // webSocketClient.connectBlocking(2000, MILLISECONDS);
            webSocketClient.connect();
        } catch (Exception e) {
            Log.e(TAG, "Unable to connect to server at specified IP address XXXXXXXXXXXXX");
        }
        Toast.makeText(getContext(), clientStatus, Toast.LENGTH_LONG).show();
    }

    public static String getWebSocketClientStatus() {
        return webSocketClient.getClientStatus();
    }

    public static void sendMessage2Server(String message) {
        webSocketClient.send(message);
    }
    public static void sendMessage2Server(byte[] blob) {
        webSocketClient.send(blob);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////GENERAL HELPER FUNCTIONS///////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////


    public static Context getContext() {
        return sContext;
    }

    public static int getNoOFTiles() { return noTiles; }
    public static void setNoOFTiles(int value) {  noTiles = value; }

    public static int getTileMaterial() { return tileMaterial; }
    public static void setTileMaterial(int value) {  tileMaterial = value; }

    public static int getTileShape() { return tileShape; }
    public static void setTileShape(int value) {  tileShape = value; }

    public static boolean getStartStream() { return startStream; }
    public static void setStartStream(boolean value) {  startStream = value; }

    public static int getDetectionMode() { return mirrorPlacement; }
    public static void setDetectionMode(int value) { mirrorPlacement = value; }

}
