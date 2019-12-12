package com.example.ARmirrorS.Client.Socket;


import android.util.Log;

import com.example.ARmirrorS.Client.Constants.ClientStatus;
import com.example.ARmirrorS.MirrorApp;
import com.example.ARmirrorS.R;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;

/**
 * <h1>Class ARCoreClient</h1>
 * Class <b>ARCoreClient</b> handles All processing related to client opening, closing, errors, and
 * messages received from server. It is also responsible for sending Setup parameters for mirror
 * and tiles to server.
 * <p>
 *
 * @author Yussuf Khalil, Daniel King
 * @author ykhalil2@illinois.edu, dking32@illinois.edu
 *
 * @version 1.1
 * @since 2019-12-05
 *
 * @see org.java_websocket.handshake.ClientHandshake
 * @see org.java_websocket.WebSocket
 * @see WebSocketClient
 */

public class ARCoreClient extends WebSocketClient {
    private int ARint;
    private static final String TAG = ARCoreClient.class.getSimpleName();

    /**Client Status.*/
    private String clientStatus = ClientStatus.ID_CONNECTING;

    /**
     * Constructor for the Client. Mainly stores the ip and port to the server and logs a message.
     *
     * @param serverUri URI of server to connect to by default it is ws://192.168.43.1:12345.
     */
    public ARCoreClient(URI serverUri) {
        super(serverUri);
        Log.d(TAG, serverUri.getPath() + ": trying to establish connection - NO draft");
    }

    /**
     * Constructor for the Client overloaded in case a different draft is required to connect to
     * server. By default Draft_6455 is used.
     *
     * @param serverUri URI of server to connect to by default it is ws://192.168.43.1:12345.
     * @param draft draft to connect to the server
     *
     */
    public ARCoreClient(URI serverUri, Draft draft) {
        super(serverUri, draft);
        Log.d(TAG, serverUri.getPath() + ": trying to establish connection - Draft_6455");
    }


    /**
     * called when a new connection with a server is established. Sends a greeting message to the
     * server, and checks later for response.
     *
     * @param handshakedata ---NA---
     */
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.d(TAG, handshakedata.getHttpStatusMessage() + ": Openning socket <------------");
        send(MirrorApp.getContext().getResources().getString(R.string.client_Hello));
    }

    /**
     * Handle String messages received from Server. Update the client status to Connection Established
     * if we receive handshake message from server. Also sets the user mode to Master or Slave based
     * on the setup message from server. Moreover, in case of a slave client handle the DISP_PARAM
     * message to set tile size, material and shape for proper ARCore rendering.
     *
     * @param message text message for handshake or setup received from server.
     */
    @Override
    public void onMessage(String message) {
        // Received a message from the server this is a text message so handle different from
        // image/frames in byte format

        // if message contains first messge sent by server [Hello, it is me. Luigi] the set the
        // socket status to opened.
        if (message.contains("[Hello, it is me. Luigi]")) {
            clientStatus = ClientStatus.ID_ESTABLISHED;
        }
        // Handel if user is going to set the mirror parameters or some one else has taken
        // the lead
        if(message.equals("MASTER")) {
            clientStatus = ClientStatus.ID_MASTER;
        } else if (message.equals("SLAVE")) {
            clientStatus = ClientStatus.ID_SLAVE;
        }

        // if we receive a DISP_PARAM message from server and we are slave client we update the
        // values, otherwise ignore [ DISP_PARAM:32:2:1 ]
        if(message.contains("DISP_PARAM:")) {
            // if we are a slave the setup the key values needed for AR rendering later on
            // we receive those from the server
            if (clientStatus == ClientStatus.ID_SLAVE) {
                String[] parts = message.split(":");
                MirrorApp.setNoOFTiles(Integer.parseInt(parts[1]));
                MirrorApp.setTileMaterial(Integer.parseInt(parts[2]));
                MirrorApp.setTileShape(Integer.parseInt(parts[3]));
            }
        }
    }

    /**
     * Called when the server sends a processed frame byteBuffer. We have received a frame/Image so
     * we just add it to our queue to be processed by ARcore on Frame updates and convert the
     * frame bytebuffer representing rotation angles to a byte array.
     *
     * @param message ByteBuffer with rotation angles of all mirror tiles.
     */
    @Override
    public void onMessage(ByteBuffer message ) {
        // We have received a frame/Image so we just add it to our queue to be processed by ARcore
        // on Frame updates

        MirrorApp.framesQ.add(message.array());
    }

    /**
     * called when the socket with the server is closing. Used to update the client status to
     * disconnected and displays a message with closing code and reason.
     *
     * @param code connection close code (normally 1000).
     * @param reason text description of the closing reason.
     * @param remote flag to indicate close initiation.
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        String displayMessage = ": disconnected from "
                + getURI()
                + "; Code: "
                + code
                + " "
                + reason
                + "\n";
        clientStatus = ClientStatus.ID_DISCONNECTED;
    }

    /**
     * Called when an error is detected on an open socket with the server.
     *
     * @param ex exception thrown to be logged
     */
    @Override
    public void onError(Exception ex) {
        String displayMessage = ClientStatus.ID_ERR2;
        Log.d(TAG, displayMessage + ex.getMessage() + " onError");
        clientStatus = displayMessage;
    }

    /**
     * Return client status to display to Server user.
     *
     * @return clientStatus | used to determine if connection to server is established or not.
     *
     * @see ClientStatus
     */
    public String getClientStatus() {
        return clientStatus;
    }
}
