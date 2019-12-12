package com.example.ARmirrorS.Server.Utils;

import android.app.Activity;
import android.os.CountDownTimer;
import android.widget.TextView;

import com.example.ARmirrorS.MirrorApp;
import com.example.ARmirrorS.R;
import com.example.ARmirrorS.Server.Constants.ImageProcessParam;
import com.example.ARmirrorS.Server.ImgProc.Utils.Frame;

/**
 * <h1>Class FrameCaptureTimer</h1>
 * Class <b>FrameCaptureTimer</b> Timer class used to give the user 15 seconds to place the camera
 * statically in case he has selected the Absolute 1st frame Difference subtraction processing
 * method.
 * <p>
 *
 * @author Yussuf Khalil, Daniel King
 * @author ykhalil2@illinois.edu, dking32@illinois.edu
 *
 * @version 1.1
 * @since 2019-12-05
 *
 * @see Activity
 * @see CountDownTimer
 */

public class FrameCaptureTimer {

    /**Parent Activity Running Context.*/
    private static Activity parentActivity;
    /**Count Down Timer object reference.*/
    private static CountDownTimer mTimer;

    /**
     * starts a count down timer that ticks every second for 15 seconds, and in case the user has
     * not changed the processing method, captures the reference frame and starts algorithm.
     *
     * @param parent parent activity context.
     * @param frame current frame being processed
     */
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
