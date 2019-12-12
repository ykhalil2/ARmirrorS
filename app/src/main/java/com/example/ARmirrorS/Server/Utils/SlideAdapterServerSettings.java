package com.example.ARmirrorS.Server.Utils;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.ARmirrorS.MirrorApp;
import com.example.ARmirrorS.R;
import com.example.ARmirrorS.Server.Constants.CameraID;
import com.example.ARmirrorS.Server.Constants.CameraParam;

/**
 * <h1>Class SlideAdapterServerSettings</h1>
 * Class <b>SlideAdapterServerSettings</b> used to handle collection of data from user on all pages
 * associated with the Server Parameters including:-
 * <p>
 * 1. Display the ip address and server status
 * 2. Camera Selection Front and Back
 * 3. Camera Resolution (640x480 or Maximum)
 * 4. Camera Display ( color / gray / or mask)
 * 5. User Interaction Level (Expert or Easy Mode)
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

public class SlideAdapterServerSettings extends PagerAdapter{

    /**Parent Activity Running Context.*/
    private Context context;
    /**Array storing Integer value of Drawable resource to display at top of each page.*/
    private static int[] sliderImages;
    /**Array storing Integer value of string resources for heading text of each page.*/
    private static String[] sliderHeadings;
    /**Array storing Integer value of string resources for bottom description of a page.*/
    private static String[] slidersDescription;

    // Values to be passed as part of the extra content for the new Camera main Activity

    /**Camera Index (Rear or Front) to be passed in Extras of intent to next Activity.*/
    private static String cameraIndex      = null;
    /**Camera Resolution passed in Extras of intent to next Activity.*/
    private static int    cameraResolution = -1;
    /**Camera Color Mode or Mask to be passed in Extras of intent to next Activity.*/
    private static int    cameraMode       = -1;
    /**Expert or Easy Interaction Mode to be passed in Extras of intent to next Activity.*/
    private static int    userMode         = -1;

    /**
     *  Constructs a SlideAdapterServerSettings to be used when slides/items are instantiated
     *  according to user input.
     *
     * @param setContext parent activity context.
     */
    public SlideAdapterServerSettings(Context setContext) {

        // setup the internal variables and image resource arrays for the two slides
        // as well as the highlighted selection in case user decides to proceed.

        context  = setContext;

        sliderImages = new int[]  {
                R.drawable.ic_server_settings0,
                R.drawable.ic_server_settings1,
                R.drawable.ic_server_settings2,
                R.drawable.ic_server_settings4,
                R.drawable.ic_server_settings5
        };

        sliderHeadings = new String[] {
                context.getString(R.string.serverstatus)
        };

        slidersDescription = new String[] {
                MirrorApp.getWebSocketServerStatus()
        };
    }

    /**
     * Retrieves the number of pages in the slider.
     *
     * @return number of pages to display.
     */
    @Override
    public int getCount() {
        return sliderImages.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    /**
     * Called to instantiate a page. And expands all appropriate chunks, setup buttons, radio Groups
     * scroll Views, etc. In addition all onClick callbacks will be handled within.
     * <p>
     * Note that:
     *
     * First Slide : Display the ip address and server status
     * Second Slide: Camera Selection Front and Back
     * Third Slide: Camera Resolution
     * Fourth Slide: Camera Display ( color / gray / or mask)
     * Fifth Slide: Expert or Easy Mode.
     * <p>
     *
     * </p>
     * @param container parent view to expand and inflate appropriate chunk to.
     * @param position slide position (0, 1, etc.)
     * @return Chunk view to expand and add to parent container.
     */
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        switch (position) {
            // First Slide : Display the ip address and server status
            case 0: {
                // inflate our resource xml chunk and attach it to this view and context
                View chunk = ((Activity) context).getLayoutInflater()
                        .inflate(
                                R.layout.chunk_server_settings1,
                                container,
                                false
                        );

                // set up the text heading and description for the current slide
                TextView heading = chunk.findViewById(R.id.ServerSettingsSliderHeading);
                TextView description = chunk.findViewById(R.id.ServerSettingsSliderText);
                ImageView imageView = chunk.findViewById(R.id.serverSettingsSliderImageTopChunk);

                heading.setText(sliderHeadings[position]);
                heading.setVisibility(View.VISIBLE);
                description.setText(slidersDescription[position]);

                imageView.setImageResource(R.drawable.ic_server_settings0);
                imageView.setVisibility(View.VISIBLE);

                // Wait for the server to start and update the status in the description
                CountDownTimer mTimer = new CountDownTimer(2000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        // do nothing
                    }

                    public void onFinish() {
                        // check to see that the user still have this option checked and did not
                        // click on another radio button
                        description.setText(MirrorApp.getWebSocketServerStatus()
                                        + "\n\n"
                                        + context.getString(R.string.serverstatus_description)
                        );
                    }
                }.start();
                // add the chunck to the view slide
                container.addView(chunk);
                return chunk;
            }

            // Second Slide: Camera Selection Front and Back
            case 1: {
                // inflate our resource xml chunk and attach it to this view and context
                View chunk = ((Activity) context).getLayoutInflater()
                        .inflate(
                                R.layout.chunk_server_settings2,
                                container,
                                false
                        );

                // set up the radio buttons and animate them based on user selection with the on
                // click listener function
                RadioGroup radioGroup = chunk.findViewById(R.id.radioGroupCameraForB);
                radioGroup.setOnCheckedChangeListener((unused, checkedId) -> {
                    RadioButton front1 = ((Activity)context).findViewById(R.id.frontCamera);
                    RadioButton back1 = ((Activity)context).findViewById(R.id.backCamera);
                    back1.setButtonDrawable(android.R.color.transparent);
                    front1.setButtonDrawable(android.R.color.transparent);
                    back1.setPadding(0, 0, 0, 0);
                    front1.setPadding(0, 0, 0, 0);
                    switch (checkedId) {
                        case R.id.frontCamera:
                            front1.setButtonDrawable(R.drawable.ic_camera_front);
                            back1.setButtonDrawable(android.R.color.transparent);
                            back1.setPadding((int) (
                                    48 * context.getResources()
                                            .getDisplayMetrics()
                                            .density + 0.5f)
                                    , 0
                                    , 0
                                    , 0
                            );
                            // set camera index
                            cameraIndex = CameraID.CAMERA_FRONT_ID;
                            break;
                        case R.id.backCamera:
                            back1.setButtonDrawable(R.drawable.ic_camera_rear);
                            front1.setButtonDrawable(android.R.color.transparent);
                            front1.setPadding((int) (
                                            48 * context.getResources()
                                                    .getDisplayMetrics()
                                                    .density + 0.5f)
                                    , 0
                                    , 0
                                    , 0
                            );
                            // set camera Index
                            cameraIndex = CameraID.CAMERA_REAR_ID;
                            break;
                    }
                });
                // add the chunk to the view slide
                container.addView(chunk);
                return chunk;
            }

            // Third Slide: Camera Resolution
            case 2: {
                // inflate our resource xml chunk and attach it to this view and context
                View chunk = ((Activity) context).getLayoutInflater()
                        .inflate(
                                R.layout.chunk_server_settings3,
                                container,
                                false
                        );

                // set up the radio buttons and animate them based on user selection with the on
                // click listener function
                RadioGroup radioGroup = chunk.findViewById(R.id.radioGroupCameraForB3);
                radioGroup.setOnCheckedChangeListener((unused, checkedId) -> {
                    RadioButton lowQ  = ((Activity)context).findViewById(R.id.res640x480);
                    RadioButton highQ = ((Activity)context).findViewById(R.id.resMax);
                    lowQ.setButtonDrawable(android.R.color.transparent);
                    highQ.setButtonDrawable(android.R.color.transparent);
                    lowQ.setPadding(0, 0, 0, 0);
                    highQ.setPadding(0, 0, 0, 0);
                    switch (checkedId) {
                        case R.id.res640x480:
                            lowQ.setButtonDrawable(R.drawable.ic_camera_hq);
                            highQ.setButtonDrawable(android.R.color.transparent);
                            highQ.setPadding((int) (
                                            48 * context.getResources()
                                                    .getDisplayMetrics()
                                                    .density + 0.5f)
                                    , 0
                                    , 0
                                    , 0
                            );
                            // set Camera Resolution to minimum
                            cameraResolution = 0;
                            break;
                        case R.id.resMax:
                            highQ.setButtonDrawable(R.drawable.ic_camera_hd);
                            lowQ.setButtonDrawable(android.R.color.transparent);
                            lowQ.setPadding((int) (
                                            48 * context.getResources()
                                                    .getDisplayMetrics()
                                                    .density + 0.5f)
                                    , 0
                                    , 0
                                    , 0
                            );
                            // set Camera Resolution to Maximum Supported
                            cameraResolution = 1;
                            break;
                    }
                });
                // add the chunk to the view slide
                container.addView(chunk);
                return chunk;
            }

            // Fourth Slide: Camera Display ( color / gray / or mask)
            case 3: {
                // inflate our resource xml chunk and attach it to this view and context
                View chunk = ((Activity) context).getLayoutInflater()
                        .inflate(
                                R.layout.chunk_server_settings4,
                                container,
                                false
                        );

                // set up the radio buttons and animate them based on user selection with the on
                // click listener function
                RadioGroup radioGroup = chunk.findViewById(R.id.radioGroupCameraForB4);
                radioGroup.setOnCheckedChangeListener((unused, checkedId) -> {
                    RadioButton color  = ((Activity)context).findViewById(R.id.colorImage);
                    RadioButton gray   = ((Activity)context).findViewById(R.id.grayImage);
                    RadioButton mask   = ((Activity)context).findViewById(R.id.maskImage);
                    color.setButtonDrawable(android.R.color.transparent);
                    gray.setButtonDrawable(android.R.color.transparent);
                    mask.setButtonDrawable(android.R.color.transparent);
                    color.setPadding(0, 0, 0, 0);
                    gray.setPadding(0, 0, 0, 0);
                    mask.setPadding(0, 0, 0, 0);
                    switch (checkedId) {
                        case R.id.colorImage:
                            color.setButtonDrawable(R.drawable.ic_camera_color);
                            gray.setButtonDrawable(android.R.color.transparent);
                            gray.setPadding((int) (
                                            48 * context.getResources()
                                                    .getDisplayMetrics()
                                                    .density + 0.5f)
                                    , 0
                                    , 0
                                    , 0
                            );
                            mask.setButtonDrawable(android.R.color.transparent);
                            mask.setPadding((int) (
                                            48 * context.getResources()
                                                    .getDisplayMetrics()
                                                    .density + 0.5f)
                                    , 0
                                    , 0
                                    , 0
                            );
                            // Set Camera Mode
                            cameraMode = CameraParam.VIEW_MODE_RGBA;
                            break;
                        case R.id.grayImage:
                            gray.setButtonDrawable(R.drawable.ic_camera_gray);
                            color.setButtonDrawable(android.R.color.transparent);
                            color.setPadding((int) (
                                            48 * context.getResources()
                                                    .getDisplayMetrics()
                                                    .density + 0.5f)
                                    , 0
                                    , 0
                                    , 0
                            );
                            mask.setButtonDrawable(android.R.color.transparent);
                            mask.setPadding((int) (
                                            48 * context.getResources()
                                                    .getDisplayMetrics()
                                                    .density + 0.5f)
                                    , 0
                                    , 0
                                    , 0
                            );
                            // Set Camera Mode
                            cameraMode = CameraParam.VIEW_MODE_GRAY;
                            break;
                        case R.id.maskImage:
                            mask.setButtonDrawable(R.drawable.ic_camera_mask);
                            color.setButtonDrawable(android.R.color.transparent);
                            color.setPadding((int) (
                                            48 * context.getResources()
                                                    .getDisplayMetrics()
                                                    .density + 0.5f)
                                    , 0
                                    , 0
                                    , 0
                            );
                            gray.setButtonDrawable(android.R.color.transparent);
                            gray.setPadding((int) (
                                            48 * context.getResources()
                                                    .getDisplayMetrics()
                                                    .density + 0.5f)
                                    , 0
                                    , 0
                                    , 0
                            );
                            // Set Camera Mode
                            cameraMode = CameraParam.VIEW_MODE_MASK;
                            break;
                    }
                });
                // add the chunk to the view slide
                container.addView(chunk);
                return chunk;
            }

            // Fifth Slide: Expert or Easy Mode
            case 4: {
                // inflate our resource xml chunk and attach it to this view and context
                View chunk = ((Activity) context).getLayoutInflater()
                        .inflate(
                                R.layout.chunk_server_settings5,
                                container,
                                false
                        );

                // set up the radio buttons and animate them based on user selection with the on
                // click listener function
                RadioGroup radioGroup = chunk.findViewById(R.id.radioGroupCameraForB5);
                radioGroup.setOnCheckedChangeListener((unused, checkedId) -> {
                    RadioButton easy   = ((Activity)context).findViewById(R.id.easyMode);
                    RadioButton expert = ((Activity)context).findViewById(R.id.expertMode);
                    easy.setButtonDrawable(android.R.color.transparent);
                    expert.setButtonDrawable(android.R.color.transparent);
                    easy.setPadding(0, 0, 0, 0);
                    expert.setPadding(0, 0, 0, 0);
                    switch (checkedId) {
                        case R.id.expertMode:
                            expert.setButtonDrawable(R.drawable.ic_camera_expert);
                            easy.setButtonDrawable(android.R.color.transparent);
                            easy.setPadding((int) (
                                            48 * context.getResources()
                                                    .getDisplayMetrics()
                                                    .density + 0.5f)
                                    , 0
                                    , 0
                                    , 0
                            );
                            // Set User Mode
                            userMode = 0;
                            break;
                        case R.id.easyMode:
                            easy.setButtonDrawable(R.drawable.ic_camera_easy);
                            expert.setButtonDrawable(android.R.color.transparent);
                            expert.setPadding((int) (
                                            48 * context.getResources()
                                                    .getDisplayMetrics()
                                                    .density + 0.5f)
                                    , 0
                                    , 0
                                    , 0
                            );
                            // Set User Mode
                            userMode = 1;
                            break;
                    }
                });
                // add the chunk to the view slide
                container.addView(chunk);
                return chunk;
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
     * Retrieve the Camera Index.
     *
     * @return cameraIndex | rear or front camera.
     * @see CameraID
     */
    public String getCameraIndex() {
        return cameraIndex;
    }

    /**
     * Get Requested Camera Resolution.
     *
     * @return cameraResolution | 640x480 or Maximum resolution.
     * @see CameraParam
     */
    public int getCameraResolution() {
        return cameraResolution;
    }

    /**
     * Obtain frame display mode.
     *
     * @return cameraMode | color frames, gray scale, or masked frames.
     * @see CameraParam
     */
    public int getCamearMode() {
        return cameraMode;
    }

    /**
     * Returns the Interaction level with the server requested by the user.
     *
     * @return userMode | easy or expert interaction with server.
     */
    public int getUserMode() {
        return userMode;
    }

}
