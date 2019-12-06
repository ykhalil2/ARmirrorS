package com.example.ARmirrorS.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ARmirrorS.R;
import com.example.ARmirrorS.Server.Activities.CameraActivity;

public class SplashScreen extends AppCompatActivity {

    private ImageView logo;
    private Thread pause;


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
