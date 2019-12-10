package com.example.ARmirrorS.Server.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toolbar;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.ARmirrorS.MirrorApp;
import com.example.ARmirrorS.R;
import com.example.ARmirrorS.Server.Constants.CameraID;
import com.example.ARmirrorS.Server.Constants.CameraParam;
import com.example.ARmirrorS.Server.Constants.ImageProcessParam;
import com.example.ARmirrorS.Constatnts.InteractionLevel;
import com.example.ARmirrorS.Server.ImgProc.Utils.Frame;
import com.example.ARmirrorS.Server.ImgProc.Utils.Utils;
import com.example.ARmirrorS.Server.Utils.ServerClientStatusThread;
import com.example.ARmirrorS.Server.Utils.ServerGUISetup;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.video.Video;

import java.util.Collections;
import java.util.List;

public class CameraActivity extends org.opencv.android.CameraActivity implements CvCameraViewListener2 {

    // Used for logging success or failure messages
    private static final String TAG = CameraActivity.class.getSimpleName();

    // Update the Server and Client Status every 3 mSeconds so we will not affect image processing
    private ServerClientStatusThread statusUpdateThread;

    // Set initial Display mode to color RGBA until user selects otherwise or start a background
    // extraction method
    private boolean cameraStarted  = false;
    private int cameraMode         = CameraParam.VIEW_MODE_RGBA;
    private int requestedCameraResolution;   // 0 for 640x460  - 1 for heigher resolutions
    private int[][] cameraSupportedResolutions;

    // Passed in intent for user mode (expert or easy).
    private int interactionLevel;

    // Set the Camera by Default to Back Camera
    public static String cameraID = CameraID.CAMERA_FRONT_ID;

    // Loads camera view of OpenCV for us to use. This lets us see using OpenCV
    private CameraBridgeViewBase mOpenCvCameraView;

    // Used to get RGB image and transpose it to fix camera orientation from 270 to 0 degrees
    private Frame frame;

    // Spinner to select Best Background Subtraction Method
    Spinner bgSubtractionSpinner;

    // Background extraction method (undefined until users selects a method)
    private int bgExtractionMode = ImageProcessParam.BG_SUBTRACT_UNDEFINED;

    // Setup GUI interface
    private ServerGUISetup ui = new ServerGUISetup(this);


    // Intent Extra String Identifiers
    private static final String CAM_INDEX      = "CAM_INDEX";
    private static final String CAM_RESOLUTION = "CAM_RESOLUTION";
    private static final String CAM_MODE       = "CAM_MODE";
    private static final String USER_MODE      = "SERVER_USER_MODE";

    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////  Load OpenCV Package ////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Load OpenCV package
     */
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default: {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };





    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////  Setup Initial View /////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_process_new);

        // Make sure screen is always on and no action bar, since we will replace it with out own
        // tool bar.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // get intent extras passed by calling activity
        getExtras();

        // Setup toolbar instead of normal action bar because we want to remove it from activity
        // in case user selects easy mode
        setupToolbar();

        // Setup Camera for Image Processing
        setupCamera();

        // Setup Thread for updating Web Socket Server Status
        setupServerThread();

        // Setup spinner for method of background subtraction
        setupSpinner();


    }

    /**
     * get the intent which has started our activity using the getIntent() method & Store data
     * appropriately.
     */
    private void getExtras() {
        Intent intent = getIntent();
        cameraMode                = intent.getIntExtra(CAM_MODE, 0);
        interactionLevel          = intent.getIntExtra(USER_MODE, 0);
        requestedCameraResolution = intent.getIntExtra(CAM_RESOLUTION, 0);
        cameraID                  = intent.getStringExtra(CAM_INDEX);

        // update our global variables to be accessed from other classes later on when doing the
        // frame subtraction
        MirrorApp.setInteractionLevel(interactionLevel);
    }

    /**
     * Setup a tool bar instead of an action bar to hide it when needed
     */
    private void setupToolbar() {
        // Get The toolbar view and set it to replace the standard action bar
        Toolbar toolbar = findViewById(R.id.toolbar);

        // Set the proper font style to match rest of application
        toolbar.setTitleTextAppearance(this, R.style.SliderTheme);
        toolbar.setTitle("So you are an Expert!");

        setActionBar(toolbar);

        //If user selected the easy mode.
        if (interactionLevel == InteractionLevel.MODE_EASY) {
            // Don't allow for interactions. Remove the title bar
            toolbar.setVisibility(View.GONE);


            // Remove all other Views except that of the camera
            ((Spinner) findViewById(R.id.bgSubtractionSpinner)).setVisibility(View.GONE);
            ((View) findViewById(R.id.line)).setVisibility(View.GONE);
            ((LinearLayout) findViewById(R.id.test)).setVisibility(View.GONE);

            // Expand the OpenCV camera view to take all screen width and height by changing the
            // constraints
            ((CameraBridgeViewBase) findViewById(R.id.javaCam)).setLayoutParams(
                    new ConstraintLayout.LayoutParams(
                            WindowManager.LayoutParams.FILL_PARENT,
                            WindowManager.LayoutParams.FILL_PARENT
                    )
            );

            // Choose MOG2 background subtraction with default parameters
            bgExtractionMode = ImageProcessParam.BG_SUBTRACT_DIFF_MOG2;
        }
    }

    /**
     * Setup OpenCV Camera.
     */
    private void setupCamera() {
        mOpenCvCameraView = findViewById(R.id.javaCam);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        // Set the camera to front or back
        if (cameraID.equals(CameraID.CAMERA_FRONT_ID)) {
            mOpenCvCameraView.setCameraIndex(1);
        } else {
            mOpenCvCameraView.setCameraIndex(0);
        }

        // Setup the camera resolution according to user input
        if (requestedCameraResolution == 0) {
            mOpenCvCameraView.setMaxFrameSize(640, 480);
        } else {
            // go for maximum if possible and let openCV decide
            mOpenCvCameraView.setMaxFrameSize(4032, 3024);
        }

        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    /**
     * Setup WebSocket Server Thread.
     */
    private void setupServerThread() {
        statusUpdateThread = new ServerClientStatusThread(this);
        statusUpdateThread.start();
    }

    /**
     * Setup Spinner for Subtraction Method.
     */
    private void setupSpinner() {
        bgSubtractionSpinner = findViewById(R.id.bgSubtractionSpinner);
        bgSubtractionSpinner.setSelection(0);

        bgSubtractionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view,
                                       final int position, final long id) {
                bgExtractionMode = position;
                frame.setProcMethod(bgExtractionMode);
                ui.run(bgExtractionMode, frame);
            }
            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
            }
        });
    }

    /**
     * Setup the Menu Items
     * @param menu - menue for choosing camera resolution and type of display
     * @return returns always true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        Log.i(TAG, "called onCreateOptionsMenu");

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem item = menu.findItem(R.id.cameraResolution);
        SubMenu subMenu = item.getSubMenu();

        if (cameraStarted && cameraSupportedResolutions == null) {
            cameraSupportedResolutions = Utils.getCameraResolutions(mOpenCvCameraView, cameraID);
        }
        else {
            if ( cameraSupportedResolutions != null) {
                int id = 0;
                for (int i = 0; i < cameraSupportedResolutions.length; i++) {
                    int x = cameraSupportedResolutions[i][0];
                    int y = cameraSupportedResolutions[i][1];
                    subMenu.add(R.id.cameraResolutionGroup, id, i, x + "x" + y);
                    id++;
                }
            }
        }
        return true;
    }

    /**
     * Setup action when Menu item is selected
     * @param item menu item selected
     * @return true
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        int groupID = item.getGroupId();
        int itemID  = item.getItemId();

        // Check to see if the response is part of the resolution group and hence
        // disable the camera set the x,y resolution and restart again.
        if (groupID == R.id.cameraResolutionGroup) {
            // Check we are not responding to click on the main Submenu items
            if (itemID != R.id.cameraOptions && itemID != R.id.cameraResolution) {
                int x = cameraSupportedResolutions[itemID][0];
                int y = cameraSupportedResolutions[itemID][1];

                mOpenCvCameraView.disableView();
                mOpenCvCameraView.setMaxFrameSize(x,y);
                mOpenCvCameraView.enableView();
            }
        } // Else we are responding to a camera view option selection from the menu
        else {
            switch (itemID) {
                // Check on Camera Viewing mode first
                case R.id.rgba: {
                    cameraMode = CameraParam.VIEW_MODE_RGBA;
                } break;
                case R.id.gray: {
                    cameraMode = CameraParam.VIEW_MODE_GRAY;
                } break;
                case R.id.pixelated: {
                    cameraMode = CameraParam.VIEW_MODE_PIXELATED;
                } break;
                case R.id.mask: {
                    cameraMode = CameraParam.VIEW_MODE_MASK;
                } break;

                // Now check the resolutions desired
                case 0:
                default: {
                    return super.onOptionsItemSelected(item);
                }
            }
            frame.setCameraMode(cameraMode);
        }
        return true;
    }





    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////  Process Camera Frames //////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     *
     */
    public CameraActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     *
     */
    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    /**
     *
     */
    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    /**
     *
     */
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    /**
     *
     * @param width -  the width of the frames that will be delivered
     * @param height - the height of the frames that will be delivered
     */
    public void onCameraViewStarted(int width, int height) {
        cameraStarted  = true;
        if (cameraSupportedResolutions == null) {
            cameraSupportedResolutions = Utils.getCameraResolutions(mOpenCvCameraView, cameraID);
        }

        // SAVE CURRENT PROCESSING METHOD
        int currentMethod;
        if (frame == null) { // first time to initialize
            currentMethod = bgExtractionMode;
            frame = new Frame(width, height, currentMethod, cameraMode, this);
            if (interactionLevel == InteractionLevel.MODE_EASY) {
                frame.backgroundBufferMOG2 = Video.createBackgroundSubtractorMOG2();
            }
        } else {
            // Set the spinner back to default
            bgSubtractionSpinner = findViewById(R.id.bgSubtractionSpinner);
            bgSubtractionSpinner.setSelection(0);

            // create a new frame with proper width and height
            currentMethod = ImageProcessParam.BG_SUBTRACT_UNDEFINED;
            frame = new Frame(width, height, currentMethod, cameraMode, this);
            ui.run(bgExtractionMode, frame);
            if (bgExtractionMode == ImageProcessParam.BG_SUBTRACT_DIFF_MOG2 ) {
                frame.backgroundBufferMOG2 = Video.createBackgroundSubtractorMOG2();
            }
        }

        System.out.println(width + " " + height);
    }

    /**
     *
     */
    public void onCameraViewStopped() {
        frame.release();
    }

    /**
     *
     * @return camera view list
     */
    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    /**
     *
     * @param inputFrame CvCameraViewFrame frame to process
     * @return final processed image to display in activity view
     */
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        Mat returnFrame;

        // fix the orientation display problem in portrait mode of openCV. openCV rotates the image
        // 90degrees so we need to display it correctly by rotating in oposite direction
        if (cameraID.equals(CameraID.CAMERA_FRONT_ID)) {
            // flip the image
            Core.flip(inputFrame.rgba(), frame.mRgba, 0);
            Core.flip(inputFrame.gray(), frame.mGray, 0);
        } else {
            // do nothing and start processing
            frame.setGray(inputFrame.gray());
            frame.setRgba(inputFrame.rgba());
        }

        // Process the image
        returnFrame = frame.process();

        return returnFrame;


    }
}