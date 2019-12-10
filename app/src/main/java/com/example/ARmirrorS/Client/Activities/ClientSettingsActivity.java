package com.example.ARmirrorS.Client.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.ARmirrorS.Client.Constants.ClientStatus;
import com.example.ARmirrorS.Client.Constants.DefaultServerURI;
import com.example.ARmirrorS.Client.Constants.TileMaterial;
import com.example.ARmirrorS.Client.Constants.TileNo;
import com.example.ARmirrorS.Client.Constants.TileShape;
import com.example.ARmirrorS.Client.Utils.SlideAdapterClientSettings;
import com.example.ARmirrorS.Constatnts.UserMode;
import com.example.ARmirrorS.MirrorApp;
import com.example.ARmirrorS.R;
import com.example.ARmirrorS.Server.Activities.CameraActivity;

import java.net.URI;


public class ClientSettingsActivity extends AppCompatActivity {

    private String clientStatus = "";

    private SlideAdapterClientSettings sliderAdapter;
    private ViewPager viewPager;

    // Bottom Layout to include the dotts and next and back buttons
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private Button nextButton;
    private Button previousButton;

    // set current page to 0 initially
    private int currentPage = 0;

    // New intent to start proper activity based on user selection to act as a client or a server.
    private Intent intent;

    // Intent Extra String Identifiers
    private static final String TILE_NO             = "TILE_NO";
    private static final String TILE_MATERIAL       = "TILE_MATERIAL";
    private static final String TILE_SHAPE          = "TILE_SHAPE";
    private static final String DETECTION_MODE      = "DETECTION_MODE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_settings);

        // Start By Connecting to the WebSocket Server and determine the client status if he is
        // master or slave
        startClientConnection();

        // find all views we will be using in current activity
        viewPager       = findViewById(R.id.sliderViewPagerClient);
        dotsLayout      = findViewById(R.id.dotsLayoutClientSettings);
        nextButton      = findViewById(R.id.nextButtonClientSettings);
        previousButton  = findViewById(R.id.previousButtonClientSettings);


        // Setup slidePager at bottom of Slides
        addDotsIndicator(0);

        // Initialize Slider Adapter and add slider adapter to slide Pager
        sliderAdapter = new SlideAdapterClientSettings(this);
        viewPager.setAdapter(sliderAdapter);
        viewPager.setOffscreenPageLimit(6);

        // Finally Setup the callback listener function for the slide adapter
        viewPager.addOnPageChangeListener(viewListner);

        /**
         * Onclick listener for NEXT / FINISH buttons.
         *
         * if a user has selected All required Parameters and he is a master client.
         * we will launch the next activity. Otherwise increment the item of the ViewPager
         * by one, and return.
         *
         */
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if we are on the final slide (current position is 4) then start the correct next
                // activity based on user selection
                if (viewPager.getCurrentItem() == dots.length - 1) {
                    // Check that the user entered and selected valid data for all options presented
                    int tileNo        = sliderAdapter.getTileNo();
                    int tileMaterial  = sliderAdapter.getTileMaterial();
                    int tileShape     = sliderAdapter.getTileShape();
                    int detectionMode = sliderAdapter.getDetectionMode();

                    // Check if the user is Master or Slave client.
                    if (MirrorApp.getWebSocketClientStatus().equals(ClientStatus.ID_MASTER)) {
                        // if the user is Master client and selected tile parameters properly then
                        // launch the AR intent, also check all values entered are valid
                        if (tileNo != TileNo.ID_UNDEFINED
                                && tileMaterial != TileMaterial.ID_UNDEFINED
                                && tileShape != -TileShape.ID_UNDEFINED) {

                            // Start a new intent and pass these values in the extras parameters to
                            // begi the AR main activity
                            intent = new Intent(getApplicationContext(), AugmentedImageActivity.class);
                            // Create Extras of intent
                            intent.putExtra(TILE_NO, tileNo);
                            intent.putExtra(TILE_MATERIAL, tileMaterial);
                            intent.putExtra(TILE_SHAPE, tileShape);
                            intent.putExtra(DETECTION_MODE, detectionMode);

                            // Send a message to the server to notify all other slave clients for the
                            // chosen parameters.
                            String setupMessage = "SETUP_PARAM:"
                                    + tileNo + ":"
                                    + tileMaterial + ":"
                                    + tileShape;

                            MirrorApp.sendMessage2Server(setupMessage);

                            // Start the intent Activity and finish
                            startActivity(intent);
                            finish();
                        } else {
                            // do nothing just increment the viewPager currentItem and return. Here clicking
                            // on finish will not perform any tasks. the user must select a mode first
                            viewPager.setCurrentItem(currentPage + 1);
                        }

                    } else if (MirrorApp.getWebSocketClientStatus().equals(ClientStatus.ID_SLAVE)) {
                        // we are not expecting any parameters from the Slave client just launch
                        // the new activity and wait there until Master client has completly setup
                        // the tile parameters
                        intent = new Intent(getApplicationContext(), AugmentedImageActivity.class);
                        intent.putExtra(DETECTION_MODE, detectionMode);

                        // Start the intent Activity and finish
                        startActivity(intent);
                        finish();
                    }

                } else {
                    // we are on an internal page and not the final one, increment current page
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
        dots = new TextView[5];
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
     * Call back for viewPager onchange listener. Called when slides are updated by swipes
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
                if (sliderAdapter.getCount() == 1) {
                    nextButton.setVisibility(View.INVISIBLE);
                } else {
                    nextButton.setVisibility(View.VISIBLE);
                }
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

    /**
     * Start the web socket server and update the status
     */
    private void startClientConnection() {
        // Setup our URI for default server
        try {
            String endPoint = DefaultServerURI.PROTOCOL
                            + DefaultServerURI.IPADDRESS
                            + ":"
                            + String.valueOf(DefaultServerURI.PORT);
            URI defaultServer = new URI(endPoint);
            // Start an attempt to connect to the server and see if it is working properly.
            MirrorApp.startWebSocketClientConnection(defaultServer);
        } catch (Exception e) {
            // do nothing
        }

        // Get the Client status to display to the user with IP address and Port
        clientStatus = MirrorApp.getWebSocketClientStatus();
    }

}
