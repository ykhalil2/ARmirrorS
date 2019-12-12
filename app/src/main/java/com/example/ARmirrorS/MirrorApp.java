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

import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.opencv.android.OpenCVLoader;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Queue;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * <h1>Class MirrorApp</h1>
 * Class <b>MirrorApp</b> extends Application, run initially before any other activities in the
 * application and is used to store global variables and mainly handle web socket interactions in
 * case of server or client connection.
 * <p>
 *
 * @author Yussuf Khalil, Daniel King
 * @author ykhalil2@illinois.edu, dking32@illinois.edu
 *
 * @version 1.1
 * @since 2019-12-05
 *
 * @see Application
 * @see OpenCVLoader
 */

public class MirrorApp extends Application {

    /**TAG variable used for logging purposes.*/
    private static final String TAG = MirrorApp.class.getSimpleName();
    /**Application Context reference.*/
    private static Context sContext;

    //
    // Web Socket Server Handler
    // And associated Parameters
    ////////////////////////////////////////////////////////////////////////////////////////
    /**Server WebSocket object reference.*/
    private static OpenCVServer webSocketServer;
    /**Timer status for Absolute subtraction frame difference method.*/
    private static boolean timerRunning          = false;
    /**Server Status to be displayed to user.*/
    private static String serverStatus           = "Starting";
    // User modes either Server or Client and set defaults
    /**Application mode / server or client.*/
    private static int userMode                  = UserMode.USER_MODE_UNDEFINED;
    /**Server Interaction Level / expert or easy mode.*/
    private static int interactionLevel          = InteractionLevel.MODE_EXPERT;
    // Start streaming from server to client flag
    /**Flag to determine if server should stream processed frames to clients.*/
    private static boolean startStream           = false;
    // use augmented images or vertical plane detection
    /**Method for placing mirror on client side (choose plane type/ Horizontal - Vertical -
     * Augmented Image.*/
    private static int mirrorPlacement           = DetectionMode.ID_UNDEFINED;
    /**@deprecated as of version 1.1.*/
    private static boolean augmentedImage       = false;
    /**deprecated as of version 1.1.*/
    private static boolean verticalPlanes       = false;

    //
    // Web Socket Client Handler
    // And associated Parameters
    ////////////////////////////////////////////////////////////////////////////////////////
    /**Client WebSocket object reference.*/
    private static ARCoreClient webSocketClient = null;
    /**Server URI by default ws://192.168.43.1:12345.*/
    private static URI serverURI                = null;
    /**Draft used to connect client to server .*/
    private static Draft drafts                 = new Draft_6455();
    /**Client Status used by client side of the application and not server.*/
    private static String clientStatus          = ClientStatus.ID_NOTCONNECTED;
    // Client Set parameters for number of tiles per row/col
    /**Number of tiles in horizontal and vertical axis.*/
    private static int noTiles                  = TileNo.ID_UNDEFINED;
    /**Tile material to display to client.*/
    private static int tileMaterial             = TileMaterial.ID_UNDEFINED;
    /**Shape of tiles (circle, square or triangle).*/
    private static int tileShape                = TileShape.ID_UNDEFINED;
    // Queue to hold all frames received from the server
    /**byte array queue to store processed frames once received from server.*/
    public static Queue<byte[]> framesQ         = new LinkedList<>();


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
     * Attempts to start the server.
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
        } else {
            Log.e(TAG, "Unable to lookup IP address");
            serverStatus = "Unable to lookup IP address";
        }
    }

    /**
     * Retrieves the IP address of the telephone interface to start the server.
     *
     * @return InetAddress of running Phone Interface.
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

    /**
     * Attempts to connect to Server from Client Side at a specific IP address and Port. This
     * uses a blocking connect open method for 2 seconds.
     *
     * @param uri default URI for Samsung phones ws:192.168.43.1:12345
     */
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

    /**
     * Obtain the server Status. If it is running and if it is communicating with any clients.
     *
     * @return Server Status.
     */
    public static String getWebSocketServerStatus() {
        // this means that we have not finished yet and a call was made to this function to early
        if (serverStatus.equals("Starting")) {
            return serverStatus;
        } else {
            return webSocketServer.getServerStatus();
        }
    }

    /**
     * Obtains the client status. This is only used by the server to display a simplified version
     * of how many clients are connected at a certain time.
     *
     * @return number of clients connected to server if any.
     */
    public static String getClientStatus() {
        return webSocketServer.getClientStatus();
    }

    /**
     * Wrapper around startServer() method to start the server.
     *
     */
    public static void startWebSocketServer() {
        startServer();
    }

    /**
     * Sets the interaction level and if it is Easy or Expert Mode.
     *
     * @param value interactionLevel with the server subtraction algorithms | Expert or Easy.
     */
    public static void setInteractionLevel(int value) {
        interactionLevel = value;
    }

    /**
     * Sets the user mode of the application. i.e. Server or client application mode.
     *
     * @param setUserMode | server or client.
     */
    public static void setUserMode(int setUserMode) {
        userMode = setUserMode;
    }

    /**
     * Checks to see if any clients are connected to the server or not.
     *
     * @deprecated since version 1.1.
     * @return true or false if clients are connected to server.
     */
    public static boolean getIsClientConnected() {
        if ( webSocketServer.getCount() >= 1) {
            return true;
        }
        return false;
    }

    /**
     * Sets the 15 sec. Timer for capturing first frame in case used of the server selects Absolute
     * Difference subtraction Algorithm.
     *
     * @param value boolean flag to set the absolute Difference Subtraction Method Timer.
     */
    public static void setTimerStatus(boolean value) {
        timerRunning = value;
    }

    /**
     * Obtain the Server user interaction method used for displaying purposes for corresponding
     * activity.
     *
     * @return interactionLevel | Easy or Expert mode
     */
    public static int getInteractionLevel() {
        return interactionLevel;
    }

    /**
     * Obtain the user mode of the application. i.e. Application is running as a server or a client.
     *
     * @return userMode | Server or Client
     */
    public static int getUserMode() {
        return userMode;
    }

    /**
     * Retrieves Server WebSocket object reference.
     *
     * @deprecated since version 1.1.
     * @return webSocketServer object ref.
     */
    public static  OpenCVServer getServerSocket() {
        return webSocketServer;
    }

    /**
     * Retrieves the status of the timer for Frame subtraction absolute difference method.
     *
     * @return timerRunning boolean flag.
     */
    public static boolean getTimerStatus() {
        return timerRunning;
    }

    /**
     * Send a byte array message to all connected clients by the server with the masked processed
     * current frame stored in a byteBuffer object.
     *
     * @param blob ByteBuffer object to store all rotation values of tiles to be sent to client.
     */
    public static void sendMessage2Clients(ByteBuffer blob) {
        webSocketServer.broadcast(blob);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////HELPER FUNCTIONS///////////////////////////////////////
    //////////////////////////////////FOR CLIENT//////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    /**
     * Wrapper function around startClient() method to start Client WebSocket object with
     * a specific URI and IP address in case default one is not valid.
     *
     * @param uri default URI for Samsung Phones ws://192.168.43.1:12345
     */
    public static void startWebSocketClientConnection(URI uri) {
        startClient(uri);
    }

    /**
     * Non Blocking call to attempt to restart WebSocket Client in case we are unable to connect
     * to server on first blocking attempt.
     *
     */
    public static void startWebSocketClientREConnection() {
        try {
            // Block call for 3 seconds until we can connect to the server
            webSocketClient.close();
            // to block until we recieve a confirmation comment out
            // webSocketClient.connectBlocking(2000, MILLISECONDS);
            webSocketClient.connect();
        } catch (Exception e) {
            Log.e(TAG, "Unable to connect to server at specified IP address XXXXXXXXXXXXX");
        }
        Toast.makeText(getContext(), clientStatus, Toast.LENGTH_LONG).show();
    }

    /**
     * Return the current Status of the Clients connected to server.
     *
     * @return ClientStatus used by Client sied of application only and not the server
     */
    public static String getWebSocketClientStatus() {
        return webSocketClient.getClientStatus();
    }

    /**
     * Sends a String/Text message to server with Setup and Display parameters.
     *
     * @param message parameters message to send to server.
     */
    public static void sendMessage2Server(String message) {
        webSocketClient.send(message);
    }

    /**
     * Send a byte Array message to server from Client. Currently, server ignores all traffic from
     * clients that is not text based.
     *
     * @param blob byte array to send to server
     */
    public static void sendMessage2Server(ByteBuffer blob) {
        webSocketClient.send(blob);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////GENERAL HELPER FUNCTIONS///////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    /**
     * get the application main Context.
     *
     * @return sContext Application Running Context
     */
    public static Context getContext() {
        return sContext;
    }

    /**
     * get number of tiles to display on frame.
     *
     * @return noTiles number of tiles per row (32/ 26/ 22/ or 16)
     */
    public static int getNoOFTiles() {
        return noTiles;
    }

    /**
     * set the number of tiles to display in vertical and horizontal axis.
     * can be either 32, 26, 22, or 16 tiles per row and column.
     *
     * @param value number of tiles per row/column
     */
    public static void setNoOFTiles(int value) {
        noTiles = value;
    }

    /**
     * Get the tile material used for client display purpose.
     *
     * @return tileMaterial ( Walnut/ Oak/ Bronze/ Copper)
     */
    public static int getTileMaterial() {
        return tileMaterial;
    }

    /**
     * Set the Tile Material used for Displaying. it can be oak, walnut, bronze, or copper.
     *
     * @param value Material Type used for display of tiles
     */
    public static void setTileMaterial(int value) {
        tileMaterial = value;
    }

    /**
     * get the tile shapes selected by client.
     *
     * @return tileShape ( Square/ Circle/ Triangle)
     */
    public static int getTileShape() {
        return tileShape;
    }

    /**
     * Set the tiles shape to display to client.
     * Either Square, Triangle or Circle.
     *
     * @param value Tiles Shape (Square/ Circle/ Triangle)
     */
    public static void setTileShape(int value) {
        tileShape = value;
    }

    /**
     * Check to see if the Client is ready to stream processed frames and masks from the server.
     *
     * @return startStream boolean flag indicating whether to send frames to clients or not.
     */
    public static boolean getStartStream() {
        return startStream;
    }

    /**
     * Sets the startStream Flag if the client has indicated to server that it is ready to receive
     * processed masked frames to display.
     *
     * @param value true or false to set startStream flag.
     */
    public static void setStartStream(boolean value) {
        startStream = value;
    }

    /**
     * Retrieve the plane detection method used by ARCore to place tiles and mirror.
     *
     * @return mirrorPlacement (vertical / horizontal or Augmented Image)
     */
    public static int getDetectionMode() {
        return mirrorPlacement;
    }

    /**
     * Sets the detection mode of ARCore to track horizontal or vertical or Augmented image planes.
     *
     * @param value detection plane (horizontal/ Vertical/ Augmented-Image)
     */
    public static void setDetectionMode(int value) {
        mirrorPlacement = value;
    }

}
