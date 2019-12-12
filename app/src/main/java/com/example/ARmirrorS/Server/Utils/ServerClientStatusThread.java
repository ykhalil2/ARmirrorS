package com.example.ARmirrorS.Server.Utils;

import android.app.Activity;
import android.widget.TextView;

import com.example.ARmirrorS.MirrorApp;
import com.example.ARmirrorS.R;

/**
 * <h1>Class ServerClientStatusThread</h1>
 * Class <b>ServerClientStatusThread</b> called after user selects server mode to initiate a new
 * background thread to update the user with the number of clients connected to the server and if
 * any of them has initiated a stream from the server.
 * <p>
 *
 * @author Yussuf Khalil, Daniel King
 * @author ykhalil2@illinois.edu, dking32@illinois.edu
 *
 * @version 1.1
 * @since 2019-12-05
 *
 * @see Activity
 * @see Thread
 */

public class ServerClientStatusThread extends Thread {

    private Activity parentActivity;

    public ServerClientStatusThread(Activity parent) {
        parentActivity = parent;
    }

    /**
     * Waits 5 seconds before client and server status updates. Runs on Parent UI thread to
     * update proper TextViews with the status of how many clients connected and if the server
     * is streaming and frames to clients.
     */
    public void run () {
        while (true) {
            parentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView serverT = parentActivity.findViewById(R.id.ServerStatusDisplay);
                    TextView clientT = parentActivity.findViewById(R.id.ClienStatusDisplay);

                    serverT.setText(MirrorApp.getWebSocketServerStatus());
                    clientT.setText(MirrorApp.getClientStatus());
                }
            });

            try {
                sleep(5 * 1000, 0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
