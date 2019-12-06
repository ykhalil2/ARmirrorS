package com.example.ARmirrorS.Client.ARProc.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.ARmirrorS.Client.Constants.DetectionMode;
import com.example.ARmirrorS.MirrorApp;
import com.example.ARmirrorS.R;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.ux.ArFragment;

public class AugmentedImageFragment extends ArFragment {


    private static final String TAG = "AugmentedImageFragment";

    // This is the name of the image in the sample database. A copy of the image is in the assets
    // directory.
    private static final int DEFAULT_IMAGE_NAME = R.drawable.ic_tracked_image;


    @Override
    protected Config getSessionConfiguration(Session session) {

        // 1st create a configuration object
        Config config = super.getSessionConfiguration(session);

        // 2nd set our configuration parameters to use Auro Focus and Latest updated image
        // as well as use HDR lighting for rendering purposes
        config.setLightEstimationMode(Config.LightEstimationMode.ENVIRONMENTAL_HDR);

        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);

        if (MirrorApp.getDetectionMode() == DetectionMode.ID_VERTICAL_PLANE) {
            config.setPlaneFindingMode(Config.PlaneFindingMode.VERTICAL);
        } else if (MirrorApp.getDetectionMode() == DetectionMode.ID_AUGMENTED_IMG) {
            config.setPlaneFindingMode(Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL);
        } else {
            config.setPlaneFindingMode(Config.PlaneFindingMode.HORIZONTAL);
        }

        config.setFocusMode(Config.FocusMode.AUTO);

        // finally configure the session and tie the session to our ARSceneView
        session.configure(config);
        this.getArSceneView().setupSession(session);

        // setup augmented image database if user will be using this feature
        if (MirrorApp.getDetectionMode() == DetectionMode.ID_AUGMENTED_IMG) {
            if (!setupAugmentedImageDatabase(config, session)) {
                Log.e(TAG, "IO exception loading augmented image bitmap");
            }
        }

        return config;
    }


    private boolean setupAugmentedImageDatabase(Config config, Session session) {
        AugmentedImageDatabase augmentedImageDatabase;

        // configure an AugmentedImageDatabase, by adding Bitmap to DB directly
        Bitmap augmentedImageBitmap = BitmapFactory.decodeResource(getResources(), DEFAULT_IMAGE_NAME);
        if (augmentedImageBitmap == null) {
            return false;
        }

        // Create the augmented image database object and add the bitmap image to it

        // If the physical size of the image is known, you can instead use:
        //     augmentedImageDatabase.addImage("image_name", augmentedImageBitmap, widthInMeters);
        // This will improve the initial detection speed. ARCore will still actively estimate the
        augmentedImageDatabase = new AugmentedImageDatabase(session);
        augmentedImageDatabase.addImage("mirror", augmentedImageBitmap);

        // Finally update our configuration with the database and image and return
        config.setAugmentedImageDatabase(augmentedImageDatabase);
        return true;
    }

}
