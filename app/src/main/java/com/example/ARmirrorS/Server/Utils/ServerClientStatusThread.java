package com.example.ARmirrorS.Server.Utils;

import android.app.Activity;
import android.widget.TextView;

import com.example.ARmirrorS.MirrorApp;
import com.example.ARmirrorS.R;

public class ServerClientStatusThread extends Thread {

    private Activity parentActivity;

    public ServerClientStatusThread(Activity parent) {
        parentActivity = parent;
    }

    public void run () {
        parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView serverT = parentActivity.findViewById(R.id.ServerStatusDisplay);
                TextView clientT = parentActivity.findViewById(R.id.ClienStatusDisplay);

                serverT.setText(MirrorApp.getWebSocketServerStatus());
                clientT.setText(MirrorApp.getClientStatus());

                try {
                    sleep(3 * 1000, 0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
