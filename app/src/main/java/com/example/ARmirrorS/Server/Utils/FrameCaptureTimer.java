package com.example.ARmirrorS.Server.Utils;

import android.app.Activity;
import android.os.CountDownTimer;
import android.widget.TextView;

import com.example.ARmirrorS.MirrorApp;
import com.example.ARmirrorS.R;
import com.example.ARmirrorS.Server.Constants.ImageProcessParam;
import com.example.ARmirrorS.Server.ImgProc.Utils.Frame;

public class FrameCaptureTimer {

    private static Activity parentActivity;
    private static CountDownTimer mTimer;


    public static void start(Activity parent, Frame frame) {

        parentActivity = parent;

        TextView editText = parentActivity.findViewById(R.id.timerCounter);


        mTimer = new CountDownTimer(15000, 1000) {

            public void onTick(long millisUntilFinished) {
                ((MirrorApp) parentActivity.getApplication()).setTimerStatus(true);
                long print = millisUntilFinished / 1000;
                editText.setText(String.valueOf(print) + " seconds (place phone on a tripod)");

                // check to see that the user still have this option checked and did not
                // click on another radio button otherwise cancel the timer
                if (frame.getProcMethod() != ImageProcessParam.BG_SUBTRACT_DIFF_ABS_1ST) {
                    editText.setText("");
                    ((MirrorApp) parentActivity.getApplication()).setTimerStatus(false);
                    cancel();
                }
            }

            public void onFinish() {
                // check to see that the user still have this option checked and did not
                // click on another radio button
                if (frame.getProcMethod() == ImageProcessParam.BG_SUBTRACT_DIFF_ABS_1ST) {
                    ((MirrorApp) parentActivity.getApplication()).setTimerStatus(false);
                    frame.setCaptured1stFrame(true);
                    editText.setText("First Frame Captured Successfully.");
                }
            }

        }.start();
    }

}
