package com.example.ARmirrorS.Client.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ARmirrorS.Client.ARProc.Nodes.MirrorNodes;
import com.example.ARmirrorS.Client.ARProc.Utils.AugmentedImageFragment;
import com.example.ARmirrorS.Client.Constants.ClientStatus;
import com.example.ARmirrorS.Client.Constants.DetectionMode;
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * <h1>Class AugmentedImageActivity</h1>
 * Class <b>AugmentedImageActivity</b> is the main Client Activity. It handles setting up the AR
 * core session. Mirror and Tile anchor placement. Tracking of hit planes and center poses to anchor
 * our frame, as well as tracking the augmented image in our database. For every frame received from
 * the camera, it attempts to rotate the tiles according to the server processed byte array in the
 * queue.
 *
 * <p>
 *
 * @author Yussuf Khalil, Daniel King
 * @author ykhalil2@illinois.edu, dking32@illinois.edu
 *
 * @version 1.1
 * @since 2019-12-05
 *
 * @see AppCompatActivity
 * @see Frame
 * @see Plane
 * @see Pose
 * @see TrackingState
 * @see AnchorNode
 * @see Anchor
 */

public class AugmentedImageActivity extends AppCompatActivity {

    private int start = 0;
    /**Number of Tiles String to be used in Extras of appropriate Intent.*/
    private static final String TILE_NO        = "TILE_NO";
    /**Tiles Material String to be used in Extras of appropriate Intent.*/
    private static final String TILE_MATERIAL  = "TILE_MATERIAL";
    /**Tiles Shape String to be used in Extras of appropriate Intent.*/
    private static final String TILE_SHAPE     = "TILE_SHAPE";
    /**Plane Detection Mode String to be used in Extras of appropriate Intent.*/
    private static final String DETECTION_MODE = "DETECTION_MODE";
    /**AR local variables used with AR Core and SceneForm.*/
    private MirrorNodes mirror;
    /**Flag to inform the server to start streaming the processed frames.*/
    private static boolean startStream = false;
    /**reference to ArFragment view in the main AR activity screen.*/
    private AugmentedImageFragment arFragment;
    /**Parent Anchor Node for mirror which is tied to the anchor created from center pose of plane.*/
    private static AnchorNode mainAnchorNode;
    /**Anchor for mirror obtained from pose based on hit plane detection or augmented image.*/
    private static Anchor anchor;
    /**Augmented image and its associated center pose anchor, keyed by the augmented image in the DB.*/
    private final Map<AugmentedImage, MirrorNodes> augmentedImageMap = new HashMap<>();


    /**
     * Called by the Android system when the activity is created. It gets the intent extras and
     * starts the AR Core session.
     *
     * @param savedInstanceState saved state from the previously terminated instance of this
     *                           activity (unused).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        start++;
        setContentView(R.layout.activity_augmented_image);


        // get intent extras passed by calling activity this method also sleeps in a different
        // thread if client is SLAVE and no DISPLAY_PARM message has been received from the
        // web socket server
        getExtras();

        // start AR session
        startAR();
    }


    /**
     * get the intent which has started our activity using the getIntent() method & Store data
     * appropriately. this method also sleeps in a different thread if client is SLAVE and no
     * DISPLAY_PARM message has been received from the web socket server. i.e. this means that we
     * have not received a DISP_PARAM message with the master client chosen value yet and we have
     * not yet setup our variables accordingly in this case we will just launch a thread that will
     * keep checking for the DISP_PARAM message from the server before going any further.
     *
     * No AR camera or session will be launched until such a message is received
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
                    // in this case we will just launch a thread that will keep checking for the
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
     * Start the AR main activity and handle anchor creation based on the center pose of the tracked
     * plane that was hit by the user. It also creates the Mirror anchor node and its subsequent
     * tile nodes and assign the parent to the anchor and adds the mirror and tiles as a child to
     * the scene.
     *
     * Note: It does not handle Augmented image tracking and placement of mirror. This is handled in
     * the onUpdate method where the mirror and tiles will be created once the image has been
     * tracked.
     *
     */
    private void startAR() {
        // first if we reached this point then all parameters has been set so remove the
        // params not yet set layout by setting its visibility to gone
        RelativeLayout paramsNotSetYet = findViewById(R.id.paramsNotSetYet);
        paramsNotSetYet.setVisibility(View.GONE);

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

    /**
     * Registered with the Sceneform Scene object, this method is called after the AR session has
     * been paused.
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Registered with the Sceneform Scene object, this method is called at the start of each frame.
     * In case no planes or images are tracked sends the server a STOP stream message. Otherwise in
     * case the image we have selected in our database is being tracked it creates the proper anchor
     * and renders the mirror and its tiles only once. Then it sends the server a Start Stream
     * message and rotates the tiles based on the server received byte array which contains the
     * actual rotation angle of each tile and its position in space.
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
