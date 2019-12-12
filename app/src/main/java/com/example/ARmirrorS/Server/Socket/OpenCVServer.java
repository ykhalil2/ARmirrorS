package com.example.ARmirrorS.Server.Socket;

import android.util.Log;

import com.example.ARmirrorS.Client.Constants.TileMaterial;
import com.example.ARmirrorS.Client.Constants.TileNo;
import com.example.ARmirrorS.Client.Constants.TileShape;
import com.example.ARmirrorS.MirrorApp;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * <h1>Class OpenCVServer</h1>
 * Class <b>OpenCVServer</b> handles All processing related to Server opening, closing, errors, and
 * messages received from clients. It is also responsible for broadcasting Setup parameters for
 * mirror and tiles, and determining which client should be assigned as MASTEr and which is assigned
 * the role of SLAVE.
 * <p>
 *
 * @author Yussuf Khalil, Daniel King
 * @author ykhalil2@illinois.edu, dking32@illinois.edu
 *
 * @version 1.1
 * @since 2019-12-05
 *
 * @see ClientHandshake
 * @see WebSocket
 * @see WebSocketServer
 */

public class OpenCVServer extends WebSocketServer {

    private static final String TAG = OpenCVServer.class.getSimpleName();

    /**MASTER Client reference to Websocket Object.*/
    private static WebSocket mSocket = null;
    /**Number of connected clients.*/
    private static int counter = 0;
    /**Server Address.*/
    private InetSocketAddress Serveraddress;
    /**Server Status.*/
    private String serverStatus = " Server Not Running";
    /**Client Status.*/
    private String clientStatus = " No clients Connected Yet";

    /**
     * Constructor for the Server. Mainly store the IP address of the server and logs the info.
     *
     * @param address server address and port
     */
    public OpenCVServer(InetSocketAddress address) {
        super(address);
        Serveraddress = address;

        Log.d(TAG, address.getAddress().toString() + ": Server Address Set");
    }

    /**
     * called when a new connection with a client is established. It also determines if the client
     * connected should be treated as a Master or Slave based on the number of clients already
     * attached.
     *
     * @param conn reference to websocket connection to client.
     * @param handshake ---NA---
     */
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {

        // send a message to the new client
        conn.send("Welcome to the OpenCV Wooden Mirror Server [Hello, it is me. Luigi]");

        serverStatus = "Server Running - communicating with " + counter + " clients";

        // if this is the first client to connect then he is the master and is allowed to set
        // the parameters to be used in sending the frames latero on

        if (counter == 0 || mSocket == null) {
            // We have a new Master client that can set parameters of to be sent frames
            mSocket = conn;
            mSocket.send("MASTER");
            broadcast( "new connection: " + conn.getRemoteSocketAddress().getAddress() + "/ MASTER");
        } else {
            conn.send("SLAVE");
            // create parameters for slave and send so he can view wha the master has choosen
            String DISPLAY_PARAMS = "DISP_PARAM:"
                    + MirrorApp.getNoOFTiles() + ":"
                    + MirrorApp.getTileMaterial() + ":"
                    + MirrorApp.getTileShape();
            conn.send(DISPLAY_PARAMS);
            broadcast( "new connection: " + conn.getRemoteSocketAddress().getAddress() + "/ SLAVE");
        }
        counter++;
        clientStatus = "-----> " + String.valueOf(counter) + " : Clients Connected <-----------";

        Log.d(TAG, conn.getLocalSocketAddress().getAddress().getHostAddress() + " " + serverStatus);
        Log.d(TAG, conn.getLocalSocketAddress().getAddress() + " " + clientStatus);
    }

    /**
     * called when the socket with the client is closing. Used to update the number of clients
     * connected to the server and relese the reference to the Websocket object of the Master
     * client.
     *
     * @param conn reference to websocket connection to client.
     * @param code connection close code (normally 1000).
     * @param reason text description of the closing reason.
     * @param remote flag to indicate close initiation.
     */
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        clientStatus ="Client " + conn.getRemoteSocketAddress().getAddress() + " Disconnected";

        // if the Master client is closing allow other people to join and set parameters
        // reset all internal values to default.
        if (conn == mSocket) {
            MirrorApp.setNoOFTiles(TileNo.ID_UNDEFINED);
            MirrorApp.setTileMaterial(TileMaterial.ID_UNDEFINED);
            MirrorApp.setTileShape(TileShape.ID_UNDEFINED);
            mSocket = null;
        }
        counter--;
        if (counter == 0) {
            clientStatus = "No clients connected";
        } else {
            clientStatus = "-----> " + String.valueOf(counter) + " : Clients Connected";
        }

        Log.d(TAG, conn.getLocalSocketAddress().getAddress() + clientStatus);

        broadcast( conn.getRemoteSocketAddress().getAddress() + " has left us :-( !" );
    }

    /**
     * Handle String messages received from Client.
     * It broadcasts the new parameters for slaves and send so they can view what the master
     * client has chosen
     *
     * @param conn reference to websocket connection to client.
     * @param message setup message received from client.
     */
    @Override
    public void onMessage(WebSocket conn, String message) {
        // a string message is received check if it is from a master client and set appropriate
        // parameters. if it is a setup message

        if (conn == mSocket) {
            if (message.contains("SETUP_PARAM:")) {
                String[] parts = message.split(":");
                MirrorApp.setNoOFTiles(Integer.parseInt(parts[1]));
                MirrorApp.setTileMaterial(Integer.parseInt(parts[2]));
                MirrorApp.setTileShape(Integer.parseInt(parts[3]));

                // broadcast the new  parameters for slaves and send so they can view what the
                // master client has chosen
                String DISPLAY_PARAMS = "DISP_PARAM:"
                        + MirrorApp.getNoOFTiles() + ":"
                        + MirrorApp.getTileMaterial() + ":"
                        + MirrorApp.getTileShape();
                broadcast(DISPLAY_PARAMS);
                Log.d(TAG,conn.getRemoteSocketAddress().getAddress()
                        + ": " + message + ": Setup Message received on Event Bus");
            } else if (message.contains("START_STREAM")) {
                MirrorApp.setStartStream(true);
                Log.d(TAG,conn.getRemoteSocketAddress().getAddress()
                        + ": " + message + ": Start Stream received on Event Bus");
            } else if (message.contains("STOP_STREAM")) {
                MirrorApp.setStartStream(false);
                Log.d(TAG, conn.getRemoteSocketAddress().getAddress()
                        + ": " + message + ": Stop Stream received on Event Bus");
            }
        } else { // ignore any other messages from Master or slave clients
            Log.d(TAG, conn.getRemoteSocketAddress().getAddress()
                    + ": " + message + ": Ignored message received on Event Bus");
        }
    }

    /**
     * Method does nothing.
     * We should not be getting byte arrays from clients.
     *
     * @param conn websocket connection to client.
     * @param data byte array received from client.
     */
    @Override
    public void onMessage(WebSocket conn, ByteBuffer data) {

        //do nothing We should not be getting byte arrays from clients
        Log.d(TAG, conn.getRemoteSocketAddress().getAddress()
                + ": byte array Message received to Event Buss and is ignored");
    }

    /**
     * Called when an error is detected on an open socket with a client.
     *
     * @param conn websocket connection to client.
     * @param ex exception thrown to be logged
     */
    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        serverStatus = "Server Error / port binding failed: " + ex.getMessage();
        if ( conn != null ) {
            // some errors like port binding failed may not be assignable to a specific web socket
            Log.d(TAG, conn.getRemoteSocketAddress().getAddress() + ": Server Error");
        }
    }

    /**
     * Called when the server has started. Only used to log a message and update the server status.
     */
    @Override
    public void onStart() {
        Log.d(TAG,": Started. Registering Event Bus");
        serverStatus = "Server Started " + Serveraddress.getAddress() + " Port "
                + Serveraddress.getPort();
        setConnectionLostTimeout(2500);
    }

    /**
     * Displays the initial ip address and port of the server. It also displays the status of the
     * streaming masked frames from server to client.
     *
     * @return serverStatus | status of the server.
     */
    public String getServerStatus() {
        return serverStatus;
    }

    /**
     * Return client status to display to Server user.
     *
     * @return clientStatus | brief description of how many clients connected to server.
     */
    public String getClientStatus() {
        return clientStatus;
    }

    /**
     * returns the number of connected clients to the server.
     *
     * @return total number of connected clients.
     */
    public int getCount() {
        return counter;
    }

}
