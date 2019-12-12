package com.example.ARmirrorS.Activities.Utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.ARmirrorS.MirrorApp;
import com.example.ARmirrorS.R;

/**
 * <h1>Class SliderAdapter</h1>
 * Class <b>SliderAdapter</b> used to handle collection of data from user on all pages associated
 * with the application mode selection (Client/Server).
 *
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

public class SliderAdapter extends PagerAdapter {

    /**Parent Activity Running Context.*/
    private Context context;
    /**Array storing Integer value of Drawable resource to display at top of each page.*/
    private static int[] sliderImages;
    /**Array storing Integer value of Drawable resources to overlap the image and highlight current
     * selection.*/
    private static int[] overlayImages;
    /**Array storing Integer value of Drawable resource of each page background image.*/
    private static int[] sliderBackground;
    /**Array storing Integer value of string resources for heading text of each page.*/
    private static String[] sliderHeadings;
    /**Array storing Integer value of string resources for bottom description of a page.*/
    private static String[] slidersDescription;
    /**Selected User Mode.*/
    private static int userMode;
    /**----NA----.*/
    private static ImageView[] selected;

    /**
     * Constructor for the Client/Server slider Adapter activity.
     *
     * @param setContext parent activity context.
     * @param setUserMode selected user mode of application ( server or client ).
     */
    public SliderAdapter(Context setContext, int setUserMode) {

        // setup the internal variables and image resource arrays for the two slides
        // as well as the highlighted selection in case user decides to proceed.

        context  = setContext;
        userMode = setUserMode;
        selected = new ImageView[2];

        sliderImages = new int[]  {
                R.drawable.ic_server2,
                R.drawable.ic_client
        };

        overlayImages = new int[]  {
                R.drawable.ic_selected_green,
                R.drawable.ic_selected_green
        };

        sliderBackground = new int[] { // was 7 & 8
                 R.drawable.ic_splashscreen9,
                 R.drawable.ic_splashscreen9
        };

        sliderHeadings = new String[] {
                context.getString(R.string.serverHeading),
                context.getString(R.string.clientHeading)
        };

        slidersDescription = new String[] {
                context.getString(R.string.serverTextView),
                context.getString(R.string.clientTextView)
        };
    }

    /**
     * Retrieves the number of pages in the slider.
     *
     * @return number of pages to display.
     */
    @Override
    public int getCount() {
        return sliderBackground.length;
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

        // inflate our resource xml chunk and attach it to this view and context
        View chunk = ((Activity) context).getLayoutInflater()
                .inflate(
                        R.layout.chunk_slider,
                        container,
                        false
                );

        // set up the text heading and description for the current slide
        TextView  heading      = chunk.findViewById(R.id.sliderHeading);
        TextView  description  = chunk.findViewById(R.id.sliderText);
        heading.setText(sliderHeadings[position]);
        description.setText(slidersDescription[position]);

        // setup the images for background and server or client
        //ImageView imageView1    = chunk.findViewById(R.id.background);
        ImageView imageView2    = chunk.findViewById(R.id.sliderImage);
        //imageView1.setImageResource(sliderBackground[position]);
        imageView2.setImageResource(sliderImages[position]);

        // setup heighlighted selection with an overlay image for each slide
        // but make sure it is invisible until user selects otherwise
        selected[position] = chunk.findViewById(R.id.imageSelected);
        selected[position].setImageResource(overlayImages[position]);
        selected[position].setVisibility(View.INVISIBLE);

        /**
         * Once user makes a selection for server or client the onClickListener for the image
         * is executed and sets up the usermode and handel overlay visibilities of images.
         *
         */
        imageView2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                // highlight the current selection by overlaying a new image on top of it
                // If user Selected to act as a Server
                selected[position].setVisibility(View.VISIBLE);
                userMode = position;

                // make sure we remove the previously overlayed image on the other slide
                if (position == 0) {
                    selected[1].setVisibility(View.INVISIBLE);
                } else {
                    selected[0].setVisibility(View.INVISIBLE);
                }

                // Finally set the user mode (server or client) to be used through out the rest
                // of application
                MirrorApp.setUserMode(userMode);
            }
        });

        // add the chunck to the view slide
        container.addView(chunk);

        return chunk;
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
        container.removeAllViews();
    }
}
