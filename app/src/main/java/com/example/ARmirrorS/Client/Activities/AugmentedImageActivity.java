package com.example.ARmirrorS.Client.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.example.ARmirrorS.Client.ARProc.Nodes.MirrorNodes;
import com.example.ARmirrorS.Client.ARProc.Utils.AugmentedImageFragment;
import com.example.ARmirrorS.Client.Constants.ClientStatus;
import com.example.ARmirrorS.Client.Constants.DetectionMode;
import com.example.ARmirrorS.Client.Constants.TileMaterial;
import com.example.ARmirrorS.Client.Constants.TileNo;
import com.example.ARmirrorS.MirrorApp;
import com.example.ARmirrorS.R;
import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.math.Vector3;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AugmentedImageActivity extends AppCompatActivity {

    // Intent Extra String Identifiers
    private static final String TILE_NO        = "TILE_NO";
    private static final String TILE_MATERIAL  = "TILE_MATERIAL";
    private static final String TILE_SHAPE     = "TILE_SHAPE";
    private static final String DETECTION_MODE = "DETECTION_MODE";


    // AR local variables used with ARcore and SceneForm
    private MirrorNodes mirror;
    private static boolean startStream = false;

    /* reference to ArFragment view in the main AR activity screen.*/
    private AugmentedImageFragment arFragment;
    private static AnchorNode mainAnchorNode;
    private static Anchor anchor;

    // Augmented image and its associated center pose anchor, keyed by the augmented image in
    // the database.
    private final Map<AugmentedImage, MirrorNodes> augmentedImageMap = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_augmented_image);


        // get intent extras passed by calling activity this method also sleeps in a different
        // thread if client is SLAVE and no DISPLAY_PARM message has been recieved from the
        // web socket server
        getExtras();

        // start AR session
        startAR();
    }


    /**
     * get the intent which has started our activity using the getIntent() method & Store data
     * appropriately.
     */
    private void getExtras() {
        Intent intent = getIntent();
        int tileNo                = intent.getIntExtra(TILE_NO, 0);
        int tileMaterial          = intent.getIntExtra(TILE_MATERIAL, 0);
        int tileShape             = intent.getIntExtra(TILE_SHAPE, 0);
        int detectionMethod       = intent.getIntExtra(DETECTION_MODE, 0);

        // update our global variables to be accessed from other classes later by ARcore and
        // Sceneform for displaying the mirror tiles

        // First Check if the user is a Slave client. One who can watch only and not set parameters
        if (MirrorApp.getWebSocketClientStatus().equals(ClientStatus.ID_SLAVE)) {
            // first set the detection method for placing the mirror and set it appropriatly
            MirrorApp.setDetectionMode(detectionMethod);

            // Now test if any of the intent values are 0 meaning UNDEFINED or the Master client
            // has not finished setup yet and we have not recieved a DISP_PARAM message from the
            // server

            if (tileNo == TileNo.ID_UNDEFINED) { // means (we hit Finish before the master client)

                if (MirrorApp.getNoOFTiles() == TileNo.ID_UNDEFINED) {
                    // this means that we have not received a DISP_PARAM message with the master
                    // client chosen value yet and we have not yet setup our variables accordingly
                    // in this case we will just launch a thred that will keep checking for the
                    // DISP_PARAM message from the server before going any further. No AR camera
                    // will be launched until such a message is received

                    new Thread((Runnable) () -> {
                        // inflate our resource xml chunk and attach it to this view and context
                        View chunk = getLayoutInflater().inflate(
                                        R.layout.chunk_client_no_params_error,
                                        findViewById(R.id.paramsNotSetYet),
                                        false);
                        ProgressBar mProgress = chunk.findViewById(R.id.progressBar);
                        ((RelativeLayout) findViewById(R.id.paramsNotSetYet)).addView(chunk);

                        int progressStatus = 0;

                        while (MirrorApp.getNoOFTiles() == TileNo.ID_UNDEFINED) {
                            mProgress.setProgress(progressStatus);
                            // Sleep for 400 milliseconds. Just to display the progress slowly
                            try {
                                Thread.sleep(400);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            progressStatus = (progressStatus > 99) ? 0 : progressStatus + 1;
                        }

                        // if we exit the loop that means the Configration message has been
                        // received from the server so proceed to the AR main setup.
                        // Hide the error and progress view and seup AR fragments like normal

                        runOnUiThread((Runnable) () -> {
                            ((RelativeLayout) findViewById(R.id.paramsNotSetYet)).removeView(chunk);
                        });

                    }).start();
                }
            }
        } else if (MirrorApp.getWebSocketClientStatus().equals(ClientStatus.ID_MASTER)) {
            // The user is a master client who has chosen to select the mirror parameters
            // Set the parameters locally for what we selected on previous activity
            MirrorApp.setNoOFTiles(tileNo);
            MirrorApp.setTileMaterial(tileMaterial);
            MirrorApp.setTileShape(tileShape);
            MirrorApp.setDetectionMode(detectionMethod);
        }
    }


    /**
     * Start the AR main activity
     */
    private void startAR() {

        arFragment = (AugmentedImageFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);
        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdate);

        // if the user did not select to use augmented image and he will hit planes to anchor
        // the mirror
        if (MirrorApp.getDetectionMode() != DetectionMode.ID_AUGMENTED_IMG) {

            arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {

                if (mirror == null) {
                    if (MirrorApp.getDetectionMode() == DetectionMode.ID_VERTICAL_PLANE
                            && plane.getType() == Plane.Type.VERTICAL
                            && plane.getTrackingState() == TrackingState.TRACKING
                    ) {

                        // create an anchor node based on the center of tracked plane
                        float[] rotation = {0.707f, 0, 0, 0.707f};
                        Pose pose = new Pose(
                                new float[]{
                                        plane.getCenterPose().tx(),
                                        plane.getCenterPose().ty(),
                                        plane.getCenterPose().tz()
                                },
                                rotation
                        );

                        anchor = arFragment.getArSceneView().getSession().createAnchor(pose);

                    } else if (MirrorApp.getDetectionMode() == DetectionMode.ID_HORIZONTAL_PLANE
                            && plane.getType() == Plane.Type.HORIZONTAL_UPWARD_FACING
                            && plane.getTrackingState() == TrackingState.TRACKING) {

                        // create an anchor node based on the center of tracked plane
                        float[] rotation = {0.707f, 0, 0, 0.707f};
                        Pose pose = new Pose(
                                new float[]{
                                        hitResult.getHitPose().extractTranslation().tx(),
                                        hitResult.getHitPose().extractTranslation().ty()+(1.696f/2.0f)+0.198f,
                                        hitResult.getHitPose().extractTranslation().tz()
                                },
                                rotation
                        );


                        anchor = arFragment.getArSceneView().getSession().createAnchor(pose);
                    }

                    // set our mainAnchorNode to the above anchor
                    mainAnchorNode = new AnchorNode(anchor);
                    mainAnchorNode.setParent(arFragment.getArSceneView().getScene());

                    // create our model and set our anchor to the middle of plane
                    mirror = new MirrorNodes(this, mainAnchorNode, anchor);

                    // add mirror and tiles as a child to the AR scene
                    arFragment.getArSceneView().getScene().addChild(mirror);

                }
            });

        }
    }



    @Override
    protected void onResume() {
        super.onResume();
    }


    /**
     * Registered with the Sceneform Scene object, this method is called at the start of each frame.
     *
     * @param frameTime - time since last frame.
     */
    public void onUpdate(FrameTime frameTime) {
        // Store our frame into a Frame object to be used later for creating our mirror anchors
        Frame frame = arFragment.getArSceneView().getArFrame();

        // If there is no frame or ARCore is not tracking yet, just return.
        if (frame == null || frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
            // send message to server to inform it that we are not tracking
            if (startStream) {
                MirrorApp.sendMessage2Server("STOP_STREAM");
                // Clearing the PriorityQueue using clear() method
                MirrorApp.framesQ.clear();
                startStream = false;
            }
            return;
        }

        if (MirrorApp.getDetectionMode() == DetectionMode.ID_AUGMENTED_IMG) {
            // Collect all images that are being tracked and store them
            Collection<AugmentedImage> updatedImages = frame.getUpdatedTrackables(AugmentedImage.class);

            // Now we will go through all images and check if our image is being tracked by the engine
            // if so we will place our mirror and tiles at the center of the tracked image

            for (AugmentedImage augmentedImage : updatedImages) {
                switch (augmentedImage.getTrackingState()) {
                    case PAUSED:
                        // When an image is in PAUSED state, but the camera is not PAUSED, it has been
                        // detected, but not yet tracked.
                        break;

                    case TRACKING:
                        // Create a new anchor for newly found images.
                        if (augmentedImage.getName().equals("mirror")) {
                            if (!augmentedImageMap.containsKey(augmentedImage)) {
                                // create an anchor node based on the center of tracked image
                                Anchor anchor = augmentedImage.createAnchor(augmentedImage.getCenterPose());

                                // set our mainAnchorNode to the above anchor
                                mainAnchorNode = new AnchorNode(anchor);
                                mainAnchorNode.setParent(arFragment.getArSceneView().getScene());

                                // create our model and set our anchor to the middle of photo
                                mirror = new MirrorNodes(this, mainAnchorNode, anchor);

                                // add mirror and tiles as a child to the AR scene
                                arFragment.getArSceneView().getScene().addChild(mirror);

                                // update hash map with the image tracked and the mirror node and all
                                // tiles
                                augmentedImageMap.put(augmentedImage, mirror);
                            } else {
                                // send message to server to inform it that we are now tracking and
                                // to send data
                                if (!startStream) {
                                    // Clearing the PriorityQueue using clear() method
                                    MirrorApp.framesQ.clear();
                                    MirrorApp.sendMessage2Server("START_STREAM");
                                    startStream = true;
                                }

                                // perform the rotation of the tiles
                                mirror.tiles.rotate();
                            }
                        }
                        break;

                    case STOPPED:
                        augmentedImageMap.remove(augmentedImage);
                        break;
                }
            }
        } else {
            // send message to server to inform it that we are now tracking and to send data
            if (!startStream) {
                // Clearing the PriorityQueue using clear() method
                MirrorApp.framesQ.clear();
                MirrorApp.sendMessage2Server("START_STREAM");
                startStream = true;
            }

            // perform the rotation of the tiles
            if (mirror != null) {
                mirror.tiles.rotate();
            }
        }
    }
}
