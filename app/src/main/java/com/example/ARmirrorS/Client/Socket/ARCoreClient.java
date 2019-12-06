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


public class ARCoreClient extends WebSocketClient {

    private static final String TAG = ARCoreClient.class.getSimpleName();

    private String clientStatus = ClientStatus.ID_CONNECTING;

    public ARCoreClient(URI serverUri) {
        super(serverUri);
        Log.d(TAG, serverUri.getPath() + ": trying to establish connection - NO draft");
    }

    public ARCoreClient(URI serverUri, Draft draft) {
        super(serverUri, draft);
        Log.d(TAG, serverUri.getPath() + ": trying to establish connection - Draft_6455");
    }


    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.d(TAG, handshakedata.getHttpStatusMessage() + ": Openning socket <------------");
        send(MirrorApp.getContext().getResources().getString(R.string.client_Hello));
    }

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

        // if we recieve a DISP_PARAM message from server and we are slave client we update the
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


    @Override
    public void onMessage(ByteBuffer message ) {
        // We have received a frame/Image so we just add it to our queue to be processed by ARcore
        // on Frame updates

        MirrorApp.framesQ.add(message.array());
    }

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

    @Override
    public void onError(Exception ex) {
        String displayMessage = ClientStatus.ID_ERR2;
        Log.d(TAG, displayMessage + ex.getMessage() + " onError");
        clientStatus = displayMessage;
    }

    public String getClientStatus() {
        return clientStatus;
    }
}
