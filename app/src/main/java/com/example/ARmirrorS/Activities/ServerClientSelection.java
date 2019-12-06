package com.example.ARmirrorS.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.ARmirrorS.Activities.Utils.SliderAdapter;
import com.example.ARmirrorS.Client.Activities.ClientSettingsActivity;
import com.example.ARmirrorS.Constatnts.UserMode;
import com.example.ARmirrorS.MirrorApp;
import com.example.ARmirrorS.R;
import com.example.ARmirrorS.Server.Activities.ServerSettingsActivity;

public class ServerClientSelection extends AppCompatActivity {

    private SliderAdapter sliderAdapter;
    private ViewPager     viewPager;

    // Bottom Layout to include the dotts and next and back buttons
    private LinearLayout  dotsLayout;
    private TextView[]    dots;
    private Button        nextButton;
    private Button        previousButton;

    // set current page to 0 initially
    private int           currentPage = 0;

    // New intent to start proper activity based on user selection to act as a client or a server.
    private Intent        intent;

    // User modes either Server or Client
    private int           userMode = UserMode.USER_MODE_UNDEFINED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_client_selection);

        // find all views we will be using in current activity
        viewPager       = findViewById(R.id.sliderViewPager);
        dotsLayout      = findViewById(R.id.dotsLayout);
        nextButton      = findViewById(R.id.nextButton);
        previousButton  = findViewById(R.id.previousButton);

        // Setup slidePager at bottom of Slides
        addDotsIndicator(0);

        // Initialize Slider Adapter and add slider adapter to slide Pager
        sliderAdapter = new SliderAdapter(this, userMode);
        viewPager.setAdapter(sliderAdapter);

        // Finally Setup the callback listener function for the slide adapter
        viewPager.addOnPageChangeListener(viewListner);

        /**
         * Onclick listener for NEXT / FINISH buttons.
         *
         * if a user has selected a mode of play. we will launch the next activity. Otherwise
         * increment the item of the ViewPager by one, and return.
         *
         */
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the current user mode from application context setup by user selection of the
                // slide
                userMode = MirrorApp.getUserMode();

                // if we are on the final slide (current position is 1) then start the correct next
                // activity based on user selection
                if (viewPager.getCurrentItem() == dots.length-1) {
                    // start our new activity if the user has selected Client or Server
                    if (userMode == UserMode.USER_MODE_SERVER) {
                        intent = new Intent(getApplicationContext(), ServerSettingsActivity.class);
                        startActivity(intent);
                        finish();
                    } else if (userMode == UserMode.USER_MODE_CLIENT) {
                        intent = new Intent(getApplicationContext(), ClientSettingsActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // do nothing just increment the viewPager currentItem and return. Here clicking
                        // on finish will not perform any tasks. the user must select a mode first
                        viewPager.setCurrentItem(currentPage + 1);
                    }
                } else {
                    viewPager.setCurrentItem(currentPage + 1);
                }
            }
        });

        /**
         * Onclick listener for Back buttons.
         *
         * Only dectrement the current page number for the slide by 1 for internal tracking by
         * Android.
         *
         */
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(currentPage-1);
            }
        });
    }

    /**
     *  Set the number of dots based on the number of slides we are displaying.
     *  Also change the color to highlite currently selected slide.
     *
     * @param position
     *              current page selected position 0, 1, 2, etc.
     */
    private void addDotsIndicator(int position) {

        // remove all previously created dots so we start clean. Otherwise we will keep adding
        // three dots every time this function is called from the OnPageChangeListener
        dotsLayout.removeAllViews();

        // create navigation dots at bottom of screen
        dots = new TextView[2];
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226", Html.FROM_HTML_MODE_LEGACY));
            dots[i].setTextSize(35);
            dots[i].setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            dots[i].setTextColor(getColor(R.color.colorTransparentWhite));
            dotsLayout.addView(dots[i]);
        }

        // set the current dot color to white indicating which page we are on.
        dots[position].setTextColor(getColor((R.color.colorWhite)));
    }

    /**
     * Call back for viewPager onchange listener. Called when slides are updated by swiptes
     * or other infractions.
     *
     */
    ViewPager.OnPageChangeListener viewListner = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {

            // add the dots again to this view since we have removed them on every slide update
            addDotsIndicator(position);

            // set current Page number
            currentPage = position;

            // display proper buttons
            if(position == 0) {
                // if we are on first slide remove the back button and set text of next button
                // accordingly
                nextButton.setVisibility(View.VISIBLE);
                nextButton.setText("NEXT");
                previousButton.setVisibility(View.INVISIBLE);
                previousButton.setText("");
            } else if (position == dots.length -1) {
                // if we are in an intermidiate slide display both back and next buttons
                nextButton.setVisibility(View.VISIBLE);
                nextButton.setText("FINISH");
                previousButton.setVisibility(View.VISIBLE);
                previousButton.setText("BACK");
            } else {
                // other wise hide the next button and display FInish instead to start our other
                // activities.
                nextButton.setVisibility(View.VISIBLE);
                nextButton.setText("NEXT");
                previousButton.setVisibility(View.VISIBLE);
                previousButton.setText("BACK");
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

    };

}
