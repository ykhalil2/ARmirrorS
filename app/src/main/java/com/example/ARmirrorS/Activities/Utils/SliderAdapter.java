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

public class SliderAdapter extends PagerAdapter {

    private Context context;

    private static int[] sliderImages;

    private static int[] overlayImages;

    private static int[] sliderBackground;

    private static String[] sliderHeadings;

    private static String[] slidersDescription;

    private static int userMode;

    private static ImageView[] selected;

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

    @Override
    public int getCount() {
        return sliderBackground.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

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

    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull  Object object) {
        container.removeAllViews();
    }
}
