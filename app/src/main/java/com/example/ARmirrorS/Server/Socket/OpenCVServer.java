package com.example.ARmirrorS.Server.Socket;

import android.content.res.Resources;
import android.util.Log;

import com.example.ARmirrorS.Client.Constants.TileMaterial;
import com.example.ARmirrorS.Client.Constants.TileNo;
import com.example.ARmirrorS.Client.Constants.TileShape;
import com.example.ARmirrorS.MirrorApp;
import com.example.ARmirrorS.R;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import de.greenrobot.event.EventBus;


public class OpenCVServer extends WebSocketServer {

    private static final String TAG = OpenCVServer.class.getSimpleName();


    private static WebSocket mSocket = null;

    // Number of connected clients
    private static int counter = 0;

    private InetSocketAddress Serveraddress;
    private String serverStatus = " Server Not Running";
    private String clientStatus = " No clients Connected Yet";


    /**
     *
     * @param address
     */
    public OpenCVServer(InetSocketAddress address) {
        super(address);
        Serveraddress = address;

        Log.d(TAG, address.getAddress().toString() + ": Server Address Set");
    }

    /**
     *
     * @param conn
     * @param handshake
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
     *
     * @param conn
     * @param code
     * @param reason
     * @param remote
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
     *
     * @param conn
     * @param message
     */
    @Override
    public void onMessage(WebSocket conn, String message) {
        // a string message is recieved check if it is from a master client and set appropriate
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

    @Override
    public void onMessage(WebSocket conn, ByteBuffer data) {

        //do nothing We should not be getting byte arrays from clients
        Log.d(TAG, conn.getRemoteSocketAddress().getAddress()
                + ": byte array Message received to Event Buss and is ignored");
    }

    /**
     *
     * @param conn
     * @param ex
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
     *
     */
    @Override
    public void onStart() {
        Log.d(TAG,": Started. Registering Event Bus");
        serverStatus = "Server Started " + Serveraddress.getAddress() + " Port "
                + Serveraddress.getPort();
        setConnectionLostTimeout(2500);
    }


    public String getServerStatus() {
        return serverStatus;
    }

    public String getClientStatus() {
        return clientStatus;
    }

    public int getCount() {
        return counter;
    }

}
