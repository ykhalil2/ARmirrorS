package com.example.ARmirrorS.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ARmirrorS.R;

/**
 * <h1>Class SplashScreen</h1>
 * Class <b>SplashScreen</b> Splash Screen Class used to animate application logo and pauses an
 * extra 0.5 seconds after animation is completed for better visuals.
 * <p>
 *
 * @author Yussuf Khalil, Daniel King
 * @author ykhalil2@illinois.edu, dking32@illinois.edu
 *
 * @version 1.1
 * @since 2019-12-05
 *
 * @see android.app.Activity
 * @see AppCompatActivity
 */

public class SplashScreen extends AppCompatActivity {

    /** Application Logo to animate.*/
    private ImageView logo;
    /** Thread to pause an extra 1/2 a second after animation has completed for better visuals.*/
    private Thread pause;


    /**
     * Called by the Android system when the activity is created.
     *
     * @param savedInstanceState saved state from the previously terminated instance of this
     *                           activity (unused).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        logo = findViewById(R.id.logo);
        Animation logoAnimation = AnimationUtils.loadAnimation(this, R.anim.ic_splashtransition);
        logo.startAnimation(logoAnimation);

        final Intent intent = new Intent(this, ServerClientSelection.class);

        pause = new Thread() {
            public void run() {
                try {
                    sleep(4500);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    startActivity(intent);
                    finish();
                }
            }
        };

        pause.start();
    }
}
