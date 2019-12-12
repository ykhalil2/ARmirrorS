package com.example.ARmirrorS.Client.Utils;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.ARmirrorS.Client.Constants.ClientStatus;
import com.example.ARmirrorS.Client.Constants.DetectionMode;
import com.example.ARmirrorS.Client.Constants.TileMaterial;
import com.example.ARmirrorS.Client.Constants.TileNo;
import com.example.ARmirrorS.Client.Constants.TileShape;
import com.example.ARmirrorS.MirrorApp;
import com.example.ARmirrorS.R;

import java.net.URI;

/**
 * <h1>Class SlideAdapterClientSettings</h1>
 * Class <b>SlideAdapterClientSettings</b> used to handle collection of data from user on all pages
 * associated with the client Parameters including:-
 * <p>
 * 1. Tile Selection number of times in x and y direction
 * 2. Tile shapes (square, circle or triangle)
 * 3. Material of tiles (wood or metal)
 * 4. Plane Detection Method for placing the mirror (Augmented/Vert./Horz.)
 * <p>
 * it is also responsible for trying to reconnect to server in case first attempt failed.
 * <p>
 *
 * @author Yussuf Khalil, Daniel King
 * @author ykhalil2@illinois.edu, dking32@illinois.edu
 *
 * @version 1.1
 * @since 2019-12-05
 *
 * @see PagerAdapter
 * @see Activity
 */

public class SlideAdapterClientSettings extends PagerAdapter {

    /**Parent Activity Running Context.*/
    private Context context;
    /**Array storing Integer value of Drawable resource to display at top of each page.*/
    private static int[] sliderImages;
    /**Array storing Integer value of string resources for heading text of each page.*/
    private static String[] sliderHeadings;
    /**Array storing Integer value of string resources for bottom description of a page.*/
    private static String[] slidersDescription;
    /**Tile Number in x and y direction to be passed in Extras of intent to next Activity.*/
    private static int tileNo        = TileNo.ID_UNDEFINED;
    /**Tile Material to be passed in Extras of intent to next Activity.*/
    private static int tileMaterial  = TileMaterial.ID_UNDEFINED;
    /**Tile Shape to be passed in Extras of intent to next Activity.*/
    private static int tileShape     = TileShape.ID_UNDEFINED;
    /**Plane detection method to be passed in Extras of intent to next Activity.*/
    private static int detectionMode = DetectionMode.ID_UNDEFINED;
    /**Flag to see if user connected to server and initiate a new attempt to connect in case of failure.*/
    public boolean userConnectedToServer = false;

    /**
     *  Constructs a SlideAdapterServerSettings to be used when slides/items are instantiated
     *  according to user input.
     *
     * @param setContext parent activity context.
     */
    public SlideAdapterClientSettings(Context setContext) {

        // setup the internal variables and image resource arrays for the two slides
        // as well as the highlighted selection in case user decides to proceed.

        context  = setContext;

        sliderImages = new int[]  {
                R.drawable.ic_clients_settings0,
                R.drawable.ic_clients_settings1,
                R.drawable.ic_clients_settings2,
                R.drawable.ic_clients_settings3,
                R.drawable.ic_clients_settings4,
        };

        sliderHeadings = new String[] {
                context.getString(R.string.clientstatus),
                context.getString(R.string.tilesize),
                context.getString(R.string.tilematerial),
                context.getString(R.string.tileshape),
                context.getString(R.string.detection_method)
        };

        slidersDescription = new String[] {
                MirrorApp.getWebSocketClientStatus(),
                context.getString(R.string.tilesizedescription),
                context.getString(R.string.tilematerialdescription),
                context.getString(R.string.tileshapedescription),
                context.getString(R.string.detection_method_description)
        };
    }

    /**
     * Retrieves the number of pages in the slider.
     *
     * @return number of pages to display.
     */
    @Override
    public int getCount() {
        String clientStatus = MirrorApp.getWebSocketClientStatus();
        int items;

        // we start with one slide until connection to the server takes place.

        if ((clientStatus.equals(ClientStatus.ID_MASTER)
            || clientStatus.equals(ClientStatus.ID_SLAVE))
                && !userConnectedToServer) {

            userConnectedToServer = true;
            items = sliderImages.length;
            Button nextButton      = ((Activity) context).findViewById(R.id.nextButtonClientSettings);
            nextButton.setVisibility(View.VISIBLE);
            nextButton.setText("NEXT");
            // Notify the adapter we will change the number of slides
            SlideAdapterClientSettings.this.notifyDataSetChanged();

        } else if ((clientStatus.equals(ClientStatus.ID_MASTER)
                || clientStatus.equals(ClientStatus.ID_SLAVE))
                && userConnectedToServer ){

            // No need to notify the adapter of anything. we only do it once.
            items = sliderImages.length;
        } else {
            items = 1;
        }
        return items;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    /**
     * Called to instantiate a page. And expands all appropriate chunks, setup buttons, radio Groups
     * scroll Views, etc. In addition all onClick callbacks will be handled within.
     *
     * @param container parent view to expand and inflate appropriate chunk to.
     * @param position slide position (0, 1, etc.)
     * @return Chunk view to expand and add to parent container.
     */
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        switch (position) {

            // First Slide : Display the status of the client connection to server if connection
            // fails try manually to enter an ip address and submit again
            case 0: {
                return setupPositionOne(container, position);
            }

            // Second Slide: Tile Selection number of times in x and y direction
            case 1: {
                return setupPositionTwo(container, position);
            }

            // Third Slide: tile shapes (square, circle or triangle)
            case 2: {
                return setupPositionThree(container, position);
            }

            // Fourth Slide: Material of tiles (wood or metal)
            case 3: {
                return setupPositionFour(container, position);
            }

            // Fifth Slide: Plane Detection Method for placing the mirror (Augmented/Vert./Horz.)
            case 4: {
                return setupPositionFive(container, position);
            }

        }
        return null;
    }


    /**
     * Called to destroy a page from the slider. Currently not being used since we need to keep
     * track of all user inputs and not have the user reselect parameters again.
     *
     * @param container parent view to remove appropriate chunk from.
     * @param position slide position (0, 1, etc.)
     * @param object View to be removed.
     */
    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull  Object object) {
        //container.removeView((ConstraintLayout) object);
    }

    /**
     * Method is used to setup position 1 of the slider pages, and handles initiation of additional
     * attempts to connect to the server in case first blocking connect has failed.
     *
     * @param container parent view to expand and inflate appropriate chunk to.
     * @param position slide position (0, 1, etc.)
     * @return Chunk view to expand and add to parent container.
     */
    private Object setupPositionOne(@NonNull ViewGroup container, int position) {

        // inflate our resource xml chunk and attach it to this view and context
        View chunk = ((Activity) context).getLayoutInflater()
                .inflate(
                        R.layout.chunk_client_settings1,
                        container,
                        false
                );

        // set up the text heading and description for the current slide
        TextView heading = chunk.findViewById(R.id.clientSettingsSliderHeading);
        TextView description = chunk.findViewById(R.id.clientSettingsSliderText);
        ImageView imageView = chunk.findViewById(R.id.clientSettingsSliderImageTopChunk);

        heading.setText(sliderHeadings[position]);
        heading.setVisibility(View.VISIBLE);
        description.setText(slidersDescription[position]);

        imageView.setImageResource(sliderImages[position]);
        imageView.setVisibility(View.VISIBLE);

        // Wait for the server to start and update the status in the description
        CountDownTimer mTimer = new CountDownTimer(5000, 2500) {

            public void onTick(long millisUntilFinished) {
                String displayStatus = MirrorApp.getWebSocketClientStatus();
                String status = displayStatus;

                // check to see that the user still have this option checked and did not
                // click on another radio button
                if (displayStatus.equals(ClientStatus.ID_MASTER)) {
                    displayStatus = displayStatus
                            + "\n\n"
                            + context.getString(R.string.clientstatus_descOK_1);
                } else if (displayStatus.equals(ClientStatus.ID_SLAVE)){
                    displayStatus = displayStatus
                            + "\n\n" + context.getString(R.string.clientstatus_descOK_2);
                } else if (displayStatus.equals(ClientStatus.ID_DISCONNECTED)
                        || displayStatus.equals(ClientStatus.ID_ERR1)
                        || displayStatus.equals(ClientStatus.ID_UNDEFINED)
                        || displayStatus.equals(ClientStatus.ID_CONNECTING)) {
                    // Start an attempt to reconnect to the default server and see if it is
                    // working properly.
                    MirrorApp.startWebSocketClientREConnection();
                }
                description.setText(displayStatus);
            }

            public void onFinish() {
                String displayStatus = MirrorApp.getWebSocketClientStatus();
                String status = displayStatus;
                // if error occured then setup manual connection attempt
                if (status.equals(ClientStatus.ID_DISCONNECTED)
                        || status.equals(ClientStatus.ID_ERR1)
                        || status.equals(ClientStatus.ID_UNDEFINED)
                        || status.equals(ClientStatus.ID_CONNECTING)) {

                    // display ip address and set on click listener for trying again button.
                    displayStatus = context.getString(R.string.clientstatus_desc_ERR);
                    description.setText(displayStatus);

                    TextView t1 = chunk.findViewById(R.id.textE1);
                    TextView t2 = chunk.findViewById(R.id.textE2);
                    TextView t3 = chunk.findViewById(R.id.textE3);
                    TextView t4 = chunk.findViewById(R.id.textE4);
                    TextView d1 = chunk.findViewById(R.id.dot1);
                    TextView d2 = chunk.findViewById(R.id.dot2);
                    TextView d3 = chunk.findViewById(R.id.dot3);
                    t1.setVisibility(View.VISIBLE);
                    t2.setVisibility(View.VISIBLE);
                    t3.setVisibility(View.VISIBLE);
                    t4.setVisibility(View.VISIBLE);
                    d1.setVisibility(View.VISIBLE);
                    d2.setVisibility(View.VISIBLE);
                    d3.setVisibility(View.VISIBLE);

                    Button tryAgain = chunk.findViewById(R.id.tryAgain);
                    tryAgain.setVisibility(View.VISIBLE);
                    tryAgain.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                URI uri = new URI(
                                        "ws://"
                                                + t3.getText().toString() + "."
                                                + t2.getText().toString() + "."
                                                + t4.getText().toString() + "."
                                                + t1.getText().toString() + ":12345"
                                );
                                // Start an attempt to connect to the server and see if it is
                                // working properly.
                                MirrorApp.startWebSocketClientConnection(uri);
                                String displayStatus = MirrorApp.getWebSocketClientStatus();
                                description.setText(displayStatus);
                                t1.setVisibility(View.INVISIBLE);
                                t2.setVisibility(View.INVISIBLE);
                                t3.setVisibility(View.INVISIBLE);
                                t4.setVisibility(View.INVISIBLE);
                                d1.setVisibility(View.INVISIBLE);
                                d2.setVisibility(View.INVISIBLE);
                                d3.setVisibility(View.INVISIBLE);
                                tryAgain.setVisibility(View.GONE);
                            } catch (Exception e) {
                                // do nothing
                                System.out.println("URI exception");
                            }

                            // Get the Client status to display to the user with IP address
                            // and Port

                            CountDownTimer mTimer = new CountDownTimer(5000, 500) {

                                public void onTick(long millisUntilFinished) {
                                    String displayStatus = MirrorApp.getWebSocketClientStatus();
                                    if (displayStatus.equals(ClientStatus.ID_MASTER)) {
                                        displayStatus = displayStatus
                                                + "\n\n"
                                                + context.getString(R.string.clientstatus_descOK_1);
                                    } else if (displayStatus.equals(ClientStatus.ID_SLAVE)) {
                                        displayStatus = displayStatus
                                                + "\n\n"
                                                + context.getString(R.string.clientstatus_descOK_2);
                                    }
                                    description.setText(displayStatus);
                                }

                                public void onFinish() {
                                    String clientStatus = MirrorApp.getWebSocketClientStatus();
                                    String text = "";

                                    if (clientStatus.equals(ClientStatus.ID_MASTER)) {
                                        text = clientStatus
                                                + "\n\n"
                                                + context.getString(R.string.clientstatus_descOK_1);
                                        // hide all buttons and ip address text fields if we are
                                        // connected successfully
                                        t1.setVisibility(View.INVISIBLE);
                                        t2.setVisibility(View.INVISIBLE);
                                        t3.setVisibility(View.INVISIBLE);
                                        t4.setVisibility(View.INVISIBLE);
                                        d1.setVisibility(View.INVISIBLE);
                                        d2.setVisibility(View.INVISIBLE);
                                        d3.setVisibility(View.INVISIBLE);
                                        tryAgain.setVisibility(View.INVISIBLE);
                                        //SlideAdapterClientSettings.this.notifyDataSetChanged();
                                    } else if (clientStatus.equals(ClientStatus.ID_SLAVE)) {
                                        text = clientStatus
                                                + "\n\n" + context.getString(R.string.clientstatus_descOK_2);
                                        // hide all buttons and ip address text fields if we are
                                        // connected successfully
                                        t1.setVisibility(View.INVISIBLE);
                                        t2.setVisibility(View.INVISIBLE);
                                        t3.setVisibility(View.INVISIBLE);
                                        t4.setVisibility(View.INVISIBLE);
                                        d1.setVisibility(View.INVISIBLE);
                                        d2.setVisibility(View.INVISIBLE);
                                        d3.setVisibility(View.INVISIBLE);
                                        tryAgain.setVisibility(View.INVISIBLE);
                                    } else if (clientStatus.equals(ClientStatus.ID_DISCONNECTED)
                                            || clientStatus.equals(ClientStatus.ID_ERR1)
                                            || status.equals(ClientStatus.ID_UNDEFINED)
                                            || status.equals(ClientStatus.ID_CONNECTING)) {
                                        text = context.getString(R.string.clientstatus_desc_ERR);
                                        tryAgain.setVisibility(View.VISIBLE);
                                        t1.setVisibility(View.VISIBLE);
                                        t2.setVisibility(View.VISIBLE);
                                        t3.setVisibility(View.VISIBLE);
                                        t4.setVisibility(View.VISIBLE);
                                        d1.setVisibility(View.VISIBLE);
                                        d2.setVisibility(View.VISIBLE);
                                        d3.setVisibility(View.VISIBLE);
                                    } else {
                                        // for future user states if any
                                    }
                                    description.setText(text);
                                }
                            }.start();
                        }
                    });
                }
            }
        }.start();

        // add the chunk to the view slide
        container.addView(chunk);
        return chunk;
    }

    /**
     * Method used to setup the horizontal scroll view to obtain tile numbers and create onClick
     * listeners for all images to overlay when item is selected.
     *
     * @param container parent view to expand and inflate appropriate chunk to.
     * @param position slide position (0, 1, etc.)
     * @return Chunk view to expand and add to parent container.
     */
    private Object setupPositionTwo(@NonNull ViewGroup container, int position) {

        // inflate our resource xml chunk and attach it to this view and context
        View chunk = ((Activity) context).getLayoutInflater()
                .inflate(
                        R.layout.chunk_client_settings2,
                        container,
                        false
                );

        // set up images in gallery and their overlays
        ImageView s32 = chunk.findViewById(R.id.imageBox4);
        ImageView s26 = chunk.findViewById(R.id.imageBox3);
        ImageView s22 = chunk.findViewById(R.id.imageBox2);
        ImageView s16 = chunk.findViewById(R.id.imageBox1);

        ImageView s32Overlay = chunk.findViewById(R.id.imageOverlayBox4);
        ImageView s26Overlay = chunk.findViewById(R.id.imageOverlayBox3);
        ImageView s22Overlay = chunk.findViewById(R.id.imageOverlayBox2);
        ImageView s16Overlay = chunk.findViewById(R.id.imageOverlayBox1);

        ImageView s32Disable = chunk.findViewById(R.id.imageDisabledBox4);
        ImageView s26Disable = chunk.findViewById(R.id.imageDisabledBox3);
        ImageView s22Disable = chunk.findViewById(R.id.imageDisabledBox2);
        ImageView s16Disable = chunk.findViewById(R.id.imageDisabledBox1);

        // Get client user status and disable all selections if user is not Master
        String clientStatus = MirrorApp.getWebSocketClientStatus();

        if (clientStatus.equals(ClientStatus.ID_MASTER)) {
            // Set click listener for 1st item in the gallery 32x32 size tiles
            s32.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // highlight the current selection by overlaying a new image on top of it
                    // If user Selected this option and make sure all other options that may have
                    // been selected previously by an overlay have been removed
                    s32Overlay.setVisibility(View.VISIBLE);
                    s26Overlay.setVisibility(View.INVISIBLE);
                    s22Overlay.setVisibility(View.INVISIBLE);
                    s16Overlay.setVisibility(View.INVISIBLE);

                    // Finally set the No of tiles to be stored
                    tileNo = TileNo.ID_32x32;
                }
            });

            // Set click listener for 1st item in the gallery 26x26 size tiles
            s26.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // highlight the current selection by overlaying a new image on top of it
                    // If user Selected this option and make sure all other options that may have
                    // been selected previously by an overlay have been removed
                    s32Overlay.setVisibility(View.INVISIBLE);
                    s26Overlay.setVisibility(View.VISIBLE);
                    s22Overlay.setVisibility(View.INVISIBLE);
                    s16Overlay.setVisibility(View.INVISIBLE);

                    // Finally set the No of tiles to be stored
                    tileNo = TileNo.ID_26x26;
                }
            });

            // Set click listener for 3rd item in the gallery 22x22 size tiles
            s22.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // highlight the current selection by overlaying a new image on top of it
                    // If user Selected this option and make sure all other options that may have
                    // been selected previously by an overlay have been removed
                    s32Overlay.setVisibility(View.INVISIBLE);
                    s26Overlay.setVisibility(View.INVISIBLE);
                    s22Overlay.setVisibility(View.VISIBLE);
                    s16Overlay.setVisibility(View.INVISIBLE);

                    // Finally set the No of tiles to be stored
                    tileNo = TileNo.ID_22x22;
                }
            });

            // Set click listener for 4th item in the gallery 16x16 size tiles
            s16.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // highlight the current selection by overlaying a new image on top of it
                    // If user Selected this option and make sure all other options that may have
                    // been selected previously by an overlay have been removed
                    s32Overlay.setVisibility(View.INVISIBLE);
                    s26Overlay.setVisibility(View.INVISIBLE);
                    s22Overlay.setVisibility(View.INVISIBLE);
                    s16Overlay.setVisibility(View.VISIBLE);

                    // Finally set the No of tiles to be stored
                    tileNo = TileNo.ID_16x16;
                }
            });
        } else {
            // Show disabled selections on all gallery items
            s32Disable.setVisibility(View.VISIBLE);
            s26Disable.setVisibility(View.VISIBLE);
            s22Disable.setVisibility(View.VISIBLE);
            s16Disable.setVisibility(View.VISIBLE);
        }

        // add the chunk to the view slide
        container.addView(chunk);
        return chunk;
    }

    /**
     * Method used to setup the horizontal scroll view to obtain tile Shape and create onClick
     * listeners for all images to overlay when item is selected.
     *
     * @param container parent view to expand and inflate appropriate chunk to.
     * @param position slide position (0, 1, etc.)
     * @return Chunk view to expand and add to parent container.
     */
    private Object setupPositionThree(@NonNull ViewGroup container, int position) {

        // inflate our resource xml chunk and attach it to this view and context
        View chunk = ((Activity) context).getLayoutInflater()
                .inflate(
                        R.layout.chunk_client_settings3,
                        container,
                        false
                );

        // set up images in gallery and their overlays
        ImageView wood1   = chunk.findViewById(R.id.imageBox4);
        ImageView wood2   = chunk.findViewById(R.id.imageBox3);
        ImageView metal1  = chunk.findViewById(R.id.imageBox2);
        ImageView metal2  = chunk.findViewById(R.id.imageBox1);

        ImageView wood1Overlay  = chunk.findViewById(R.id.imageOverlayBox4);
        ImageView wood2Overlay  = chunk.findViewById(R.id.imageOverlayBox3);
        ImageView metal1Overlay = chunk.findViewById(R.id.imageOverlayBox2);
        ImageView metal2Overlay = chunk.findViewById(R.id.imageOverlayBox1);

        ImageView wood1Disable  = chunk.findViewById(R.id.imageDisabledBox4);
        ImageView wood2Disable  = chunk.findViewById(R.id.imageDisabledBox3);
        ImageView metal1Disable = chunk.findViewById(R.id.imageDisabledBox2);
        ImageView metal2Disable = chunk.findViewById(R.id.imageDisabledBox1);

        // Get client user status and disable all selections if user is not Master
        String clientStatus = MirrorApp.getWebSocketClientStatus();

        if (clientStatus.equals(ClientStatus.ID_MASTER)) {
            // Set click listener for 1st item in the gallery1st wood Material tiles
            wood1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // highlight the current selection by overlaying a new image on top of it
                    // If user Selected this option and make sure all other options that may have
                    // been selected previously by an overlay have been removed
                    wood1Overlay.setVisibility(View.VISIBLE);
                    wood2Overlay.setVisibility(View.INVISIBLE);
                    metal1Overlay.setVisibility(View.INVISIBLE);
                    metal2Overlay.setVisibility(View.INVISIBLE);

                    // Finally set the No of tiles to be stored
                    tileMaterial = TileMaterial.ID_WOOD_OAK;
                }
            });

            // Set click listener for 1st item in the gallery  2nd wood Material  tiles
            wood2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // highlight the current selection by overlaying a new image on top of it
                    // If user Selected this option and make sure all other options that may have
                    // been selected previously by an overlay have been removed
                    wood1Overlay.setVisibility(View.INVISIBLE);
                    wood2Overlay.setVisibility(View.VISIBLE);
                    metal1Overlay.setVisibility(View.INVISIBLE);
                    metal2Overlay.setVisibility(View.INVISIBLE);

                    // Finally set the No of tiles to be stored
                    tileMaterial = TileMaterial.ID_WOOD_BIRCH;
                }
            });

            // Set click listener for 3rd item in the gallery 1st Metal Material tiles
            metal1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // highlight the current selection by overlaying a new image on top of it
                    // If user Selected this option and make sure all other options that may have
                    // been selected previously by an overlay have been removed
                    wood1Overlay.setVisibility(View.INVISIBLE);
                    wood2Overlay.setVisibility(View.INVISIBLE);
                    metal1Overlay.setVisibility(View.VISIBLE);
                    metal2Overlay.setVisibility(View.INVISIBLE);

                    // Finally set the No of tiles to be stored
                    tileMaterial = TileMaterial.ID_METAL_COPPER;
                }
            });

            // Set click listener for 4th item in the gallery 2nd Metal Material tiles
            metal2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // highlight the current selection by overlaying a new image on top of it
                    // If user Selected this option and make sure all other options that may have
                    // been selected previously by an overlay have been removed
                    wood1Overlay.setVisibility(View.INVISIBLE);
                    wood2Overlay.setVisibility(View.INVISIBLE);
                    metal1Overlay.setVisibility(View.INVISIBLE);
                    metal2Overlay.setVisibility(View.VISIBLE);

                    // Finally set the No of tiles to be stored
                    tileMaterial = TileMaterial.ID_METAL_BRONZE;
                }
            });
        } else {
            // set all views for gallery items for material selection to be disabled
            wood1Disable.setVisibility(View.VISIBLE);
            wood2Disable.setVisibility(View.VISIBLE);
            metal1Disable.setVisibility(View.VISIBLE);
            metal2Disable.setVisibility(View.VISIBLE);
        }

        // add the chunk to the view slide
        container.addView(chunk);
        return chunk;
    }

    /**
     * Method used to setup the horizontal scroll view to obtain tile Material and create onClick
     * listeners for all images to overlay when item is selected.
     *
     * @param container parent view to expand and inflate appropriate chunk to.
     * @param position slide position (0, 1, etc.)
     * @return Chunk view to expand and add to parent container.
     */
    private Object setupPositionFour(@NonNull ViewGroup container, int position) {

        // inflate our resource xml chunk and attach it to this view and context
        View chunk = ((Activity) context).getLayoutInflater()
                .inflate(
                        R.layout.chunk_client_settings4,
                        container,
                        false
                );

        // set up images in gallery and their overlays
        ImageView square   = chunk.findViewById(R.id.imageBox4);
        ImageView circle   = chunk.findViewById(R.id.imageBox3);
        ImageView triangle = chunk.findViewById(R.id.imageBox2);

        ImageView squareOverlay   = chunk.findViewById(R.id.imageOverlayBox4);
        ImageView circleOverlay   = chunk.findViewById(R.id.imageOverlayBox3);
        ImageView triangleOverlay = chunk.findViewById(R.id.imageOverlayBox2);

        ImageView squareDisable = chunk.findViewById(R.id.imageDisabledBox4);
        ImageView circleDisable = chunk.findViewById(R.id.imageDisabledBox3);
        ImageView triangleDisable = chunk.findViewById(R.id.imageDisabledBox2);

        // Get client user status and disable all selections if user is not Master
        String clientStatus = MirrorApp.getWebSocketClientStatus();

        if (clientStatus.equals(ClientStatus.ID_MASTER)) {
            // Set click listener for 1st item in the gallery 32x32 size tiles
            square.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // highlight the current selection by overlaying a new image on top of it
                    // If user Selected this option and make sure all other options that may have
                    // been selected previously by an overlay have been removed
                    squareOverlay.setVisibility(View.VISIBLE);
                    circleOverlay.setVisibility(View.INVISIBLE);
                    triangleOverlay.setVisibility(View.INVISIBLE);

                    // Finally set the No of tiles to be stored
                    tileShape = TileShape.ID_SQUARE;
                }
            });

            // Set click listener for 1st item in the gallery 26x26 size tiles
            circle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // highlight the current selection by overlaying a new image on top of it
                    // If user Selected this option and make sure all other options that may have
                    // been selected previously by an overlay have been removed
                    squareOverlay.setVisibility(View.INVISIBLE);
                    circleOverlay.setVisibility(View.VISIBLE);
                    triangleOverlay.setVisibility(View.INVISIBLE);

                    // Finally set the No of tiles to be stored
                    tileShape = TileShape.ID_CIRCLE;
                }
            });

            // Set click listener for 3rd item in the gallery 22x22 size tiles
            triangle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // highlight the current selection by overlaying a new image on top of it
                    // If user Selected this option and make sure all other options that may have
                    // been selected previously by an overlay have been removed
                    squareOverlay.setVisibility(View.INVISIBLE);
                    circleOverlay.setVisibility(View.INVISIBLE);
                    triangleOverlay.setVisibility(View.VISIBLE);

                    // Finally set the No of tiles to be stored
                    tileShape = TileShape.ID_TRIANGLE;
                }
            });
        } else {
            // disable all items in gallery for shape selection
            squareDisable.setVisibility(View.VISIBLE);
            circleDisable.setVisibility(View.VISIBLE);
            triangleDisable.setVisibility(View.VISIBLE);
        }

        // add the chunk to the view slide
        container.addView(chunk);
        return chunk;
    }

    /**
     * Method used to setup the horizontal scroll view to obtain requested plane detection mode
     * and create onClick listeners for all radio buttons and groups.
     *
     * @param container parent view to expand and inflate appropriate chunk to.
     * @param position slide position (0, 1, etc.)
     * @return Chunk view to expand and add to parent container.
     */
    private Object setupPositionFive(@NonNull ViewGroup container, int position) {

        // inflate our resource xml chunk and attach it to this view and context
        View chunk = ((Activity) context).getLayoutInflater()
                .inflate(
                        R.layout.chunk_client_settings5,
                        container,
                        false
                );

        // set up the text heading and description for the current slide
        TextView heading = chunk.findViewById(R.id.clientSettings2SliderHeading);
        TextView description = chunk.findViewById(R.id.clientSettings2SliderText);
        ImageView imageView = chunk.findViewById(R.id.clientSettings2SliderImageTopChunk);

        heading.setText(sliderHeadings[position]);
        heading.setVisibility(View.VISIBLE);
        description.setText(slidersDescription[position]);

        imageView.setImageResource(sliderImages[position]);
        imageView.setVisibility(View.VISIBLE);



        RadioGroup radioGroup = chunk.findViewById(R.id.radioGroupDetection);
        radioGroup.setOnCheckedChangeListener((unused, checkedId) -> {
            RadioButton augmented  = ((Activity)context).findViewById(R.id.augmentedImage);
            RadioButton vertical   = ((Activity)context).findViewById(R.id.verticalPlane);
            RadioButton horizontal = ((Activity)context).findViewById(R.id.horizontalPlane);
            augmented.setButtonDrawable(android.R.color.transparent);
            vertical.setButtonDrawable(android.R.color.transparent);
            horizontal.setButtonDrawable(android.R.color.transparent);
            augmented.setPadding(0, 0, 0, 0);
            vertical.setPadding(0, 0, 0, 0);
            horizontal.setPadding(0, 0, 0, 0);

            switch (checkedId) {
                case R.id.augmentedImage:
                    augmented.setButtonDrawable(R.drawable.augmented_image);
                    vertical.setButtonDrawable(android.R.color.transparent);
                    vertical.setPadding((int) (
                                    48 * context.getResources()
                                            .getDisplayMetrics()
                                            .density + 0.5f)
                            , 0
                            , 0
                            , 0
                    );
                    horizontal.setButtonDrawable(android.R.color.transparent);
                    horizontal.setPadding((int) (
                                    48 * context.getResources()
                                            .getDisplayMetrics()
                                            .density + 0.5f)
                            , 0
                            , 0
                            , 0
                    );
                    // Set Camera Mode
                    detectionMode = DetectionMode.ID_AUGMENTED_IMG;
                    break;
                case R.id.verticalPlane:
                    vertical.setButtonDrawable(R.drawable.vertical_plane);
                    augmented.setButtonDrawable(android.R.color.transparent);
                    augmented.setPadding((int) (
                                    48 * context.getResources()
                                            .getDisplayMetrics()
                                            .density + 0.5f)
                            , 0
                            , 0
                            , 0
                    );
                    horizontal.setButtonDrawable(android.R.color.transparent);
                    horizontal.setPadding((int) (
                                    48 * context.getResources()
                                            .getDisplayMetrics()
                                            .density + 0.5f)
                            , 0
                            , 0
                            , 0
                    );
                    // Set Camera Mode
                    detectionMode = DetectionMode.ID_VERTICAL_PLANE;
                    break;
                case R.id.horizontalPlane:
                    horizontal.setButtonDrawable(R.drawable.horizontal_plane);
                    augmented.setButtonDrawable(android.R.color.transparent);
                    augmented.setPadding((int) (
                                    48 * context.getResources()
                                            .getDisplayMetrics()
                                            .density + 0.5f)
                            , 0
                            , 0
                            , 0
                    );
                    vertical.setButtonDrawable(android.R.color.transparent);
                    vertical.setPadding((int) (
                                    48 * context.getResources()
                                            .getDisplayMetrics()
                                            .density + 0.5f)
                            , 0
                            , 0
                            , 0
                    );
                    // Set Camera Mode
                    detectionMode = DetectionMode.ID_HORIZONTAL_PLANE;
                    break;
            }
        });

        // add the chunk to the view slide
        container.addView(chunk);
        return chunk;
    }


    /**
     * Get the Number of tiles in horizontal and vertical plane.
     *
     * @return tileNo | number of tiles per row and column.
     */
    public int getTileNo() {
        return tileNo;
    }

    /**
     * Retrieve the Tile material selected by user.
     *
     * @return tileMaterial | Tile material | wood or metal.
     */
    public int getTileMaterial() {
        return tileMaterial;
    }

    /**
     * Obtain the user chosen Tile Shape.
     *
     * @return tileShape | Tile Shape | Square, Circle, or Triangle.
     */
    public int getTileShape() {
        return tileShape;
    }

    /**
     * Rerturns the user selected Plane detection Method.
     *
     * @return detectionMode | Plane detection Mode | Horizontal, Vertical, or Augmented Image.
     */
    public int getDetectionMode() {
        return detectionMode;
    }

}
