package com.example.ARmirrorS.Client.ARProc.Nodes;

import android.content.Context;
import android.net.Uri;

import androidx.appcompat.app.AlertDialog;

import com.example.ARmirrorS.Client.Constants.TileMaterial;
import com.example.ARmirrorS.Client.Constants.TileNo;
import com.example.ARmirrorS.Client.Constants.TileShape;
import com.example.ARmirrorS.MirrorApp;
import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;

/**
 * <h1>Class TileNodes</h1>
 * Class <b>TileNodes</b> used to build the rendereable tiles and place the tiles inside the mirror.
 * We use completable futures here to simplify the error handling and asynchronous loading. It also
 * starts the process rotating the tiles based on the byteArray received from the server with
 * processed mask including normalized rotation angle of each tile between +30 and -30 degrees.
 *
 * <p>
 *
 * @author Yussuf Khalil, Daniel King
 * @author ykhalil2@illinois.edu, dking32@illinois.edu
 *
 * @version 1.1
 * @since 2019-12-05
 *
 * @see Anchor
 * @see AnchorNode
 * @see Node
 * @see Quaternion
 * @see Vector3
 * @see ModelRenderable
 */

public class TileNodes extends AnchorNode {

    private int tileNumber = 0;
    /**Parent Activity Running Context.*/
    private static Context context;
    /**Parent Anchor for mirror obtained from pose based on hit plane detection or augmented image.*/
    private static Anchor anchor;
    /**Parent Anchor Node for mirror which is tied to the anchor created from center pose of plane.*/
    private static AnchorNode anchorNode;
    /**2 dimensional array containing tiles nodes and associated local positions and rotation angles.*/
    public static Node[][] tile;
    /**number of tile columns. in case of triangle shapes there will be roughly twice the amount of
     * tiles in x direction than in the y direction
     */
    private static int cols;
    /**number of tile rows.*/
    private static int rows;
    /**Actual Mirror height in meters. Used to position tiles in local coordinates.*/
    private static final float MIRROR_HEIGHT = 1.696f;
    /**Actual Mirror Frame width in meters. Used to offset the tiles in Y direction when placing.*/
    private static final float FRAME_HEIGHT  = 0.198f;
    /**Gap between tiles is 3 millimeters.*/
    private static final float TILE_GAP      = 0.003f;
    /**position in X direction of the current tile to be placed in the scene.*/
    private static int   positionX = 0;
    /**position in Y direction of the current tile to be placed in the scene.*/
    private static int   positionY = 0;
    /**Actual Y local location in meters of current tile to be placed measured from center of Mirror.*/
    private static float locationY = (MIRROR_HEIGHT) / 2.0f;
    /**In case of triangle shape tiles there will be half filler triangle. This is the current X position.*/
    private static int   fillerPositionX = 0;
    /**In case of triangle shape tiles there will be half filler triangle. This is the current Y position.*/
    private static int   fillerPositionY = 0;
    /**Actual Y local location in meters of current filler tile to be placed measured from center of Mirror.*/
    private static float fillerLocationY = MIRROR_HEIGHT / 2.0f;
    /**Total number of tiles built and rendered to scene. Needed because of async calls.*/
    private static int   totalTilesBuilt = 0;
    /**In case of Triangle shapes we need to add an additional 180 degree rotation because of flips.*/
    private static float additionalRotation = 0;


    /**
     * Constructor for building the wood/metal tiles and setting their anchor and parent anchor node.
     * It performs the task of placing the tiles in the scene.
     *
     * First it sets the node array based on the size of the tiles requested by the user.
     * Note that for triangle shapes the rows will not change however the colums will be increased
     * to account for flipped triangles and padding at both ends. So for an n tiles we will
     * effectively have n [norma] + (n-1) [flipped] + 2 halves at the end.
     *
     * Finally set the first tile position is top left hand corner of the Mirror Frame.
     *
     * @param setParent Parent Activity Running Context.
     * @param setAnchorNode Parent Anchor Node for Tiles which is tied to the anchor created from
     *                      center pose of detected plane or augmented image.
     * @param setAnchor Parent Anchor for Tiles obtained from pose based on hit plane detection or
     *                  augmented image
     */
    TileNodes(Context setParent, AnchorNode setAnchorNode, Anchor setAnchor) {
        context = setParent;
        anchorNode = setAnchorNode;
        anchor = setAnchor;
        setAnchor(anchor);

        // Set the node array based on the size of the tiles requested by the user. Note that for
        // triangle shapes the rows will not change however the colums will be increased to account
        // for flipped triangles and padding at both ends. So for an n tiles we will effectively
        // have n [norma] + (n-1) [flipped] + 2 halves at the end
        cols = MirrorApp.getNoOFTiles();
        rows = cols;

        if (MirrorApp.getTileShape() == TileShape.ID_TRIANGLE) {
            cols = 2 * cols + 1;
        }

        tile = new Node[rows][cols];

        // first tile position is top left hand corner of the Mirror Frame
        locationY = locationY + (TileNo.sizeMap.get(MirrorApp.getNoOFTiles()) + TILE_GAP) / 2.0f;
        fillerLocationY = locationY + .005f;

        // setup additional rotation required for triangle tiles to account for the flipped even
        // tiles
        if (MirrorApp.getTileShape() == TileShape.ID_TRIANGLE) {
            additionalRotation = 180.0f;
        }

        placeTiles();
    }


    /**
     * Models of the tiles.  We use completable futures here to simplify the error handling
     * and asynchronous loading.  The loading is started with the first construction of an instance,
     * and then used when the image is set.
     *
     * The method will Build the renderable and wait until its ready to place it in the scene by
     * calling function addMirrorNode ToScene.
     */
    private void placeTiles() {
        // Create the name of the 3d object file by concatenating the strings from the constants
        // class
        String fileName = TileShape.fileMap.get(MirrorApp.getTileShape())
                + TileMaterial.fileMap.get(MirrorApp.getTileMaterial());

        Uri tileParse = Uri.parse(fileName);

        // in case of square or circle we will perform the same operation since the layout is
        // identical
        int noPadding = MirrorApp.getTileShape() == TileShape.ID_TRIANGLE ? 2 : 0;

        for (int i = 0; i < rows; i++) {
            // Create Renderable model and build
            // Build and wait until its ready to place it in the scene by calling function
            // addMirrorNode ToScene. We can have three shapes so we will build based on the selected
            // shape type

            // Here we have two cases. First all tiles are the same (in case of square and circle
            for (int j = 0; j < cols - noPadding; j++) {
                ModelRenderable.builder()
                        .setSource(context, tileParse)
                        .build()
                        .thenAccept(modelRenderable -> addTileToScene(modelRenderable))
                        .exceptionally(throwable -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setMessage(throwable.getMessage()).show();
                            return null;
                        });
            }

            // Secondly in case we have a trianles the we need to create two extra half triangles for
            // padding at the ends of each row of the mirror tiles
            if (MirrorApp.getTileShape() == TileShape.ID_TRIANGLE) {
                Uri tileParseHalfTriangle = Uri.parse("half_" + fileName);

                for (int k = 0; k < noPadding; k++) {
                    ModelRenderable.builder()
                            .setSource(context, tileParseHalfTriangle)
                            .build()
                            .thenAccept(modelRenderable -> addFillerTileToScene(modelRenderable))
                            .exceptionally(throwable -> {
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setMessage(throwable.getMessage()).show();
                                return null;
                            });
                }
            }
        }
    }


    /**
     * Called when the AugmentedImage is detected and should be rendered. A Sceneform node tree is
     * created based on an Anchor created from the image or the center pose of the detected plane.
     *
     * The tiles are then positioned based on the center of the parent node (the mirror renderable).
     *
     * There is no need to worry about world coordinates since everything is relative to the
     * center of the image, which is the parent node of the mirror and subsequently the tiles.
     *
     * Here we create a new node for each tile, then Set its parent to the main anchorNode which
     * will be the center of the mirror node.
     *
     * - We then Fix the rotation of imported obj file in the y and x direction.
     * - Do a rotation for tiles that are odd in the mirror grid and fix the x transition to
     *   overlap with half of previous tile
     * - Finally set scale and rotation of tile initially
     *
     * @param modelRenderable current tile reference to renderable object.
     */
    private void addTileToScene (ModelRenderable modelRenderable) {

        if (MirrorApp.getTileShape() == TileShape.ID_TRIANGLE  && positionX == 0) {
            positionX = 1;
        }

        // Create a new node for the current tile
        tile[positionY][positionX] = new Node();
        tile[positionY][positionX].setRenderable(modelRenderable);


        // Set its parent to the main anchorNode which will be the center of the picture once it is
        // being tracked
        tile[positionY][positionX].setParent(anchorNode);


        // Fix the local position in relation with parent mirror for imported obj file in the
        // y and x direction
        float tileSizePlusGap = TileNo.sizeMap.get(MirrorApp.getNoOFTiles()) + TILE_GAP;
        float xTransition;
        if (MirrorApp.getTileShape() == TileShape.ID_TRIANGLE) {
            xTransition = (-MIRROR_HEIGHT + tileSizePlusGap) / 2.0f + (positionX- 1 ) / 2.0f * tileSizePlusGap;
        } else {
            xTransition = (-MIRROR_HEIGHT + tileSizePlusGap) / 2.0f + (positionX) * tileSizePlusGap;
        }

        float zTransition = locationY - tileSizePlusGap;
        float yTransition = 0.14f;
        float scaleXYZ = TileNo.scaleMap.get(MirrorApp.getNoOFTiles());
        float scaleX = scaleXYZ;
        // Do a rotation for tiles that are odd in the mirror grid and fix the x transition to
        // overlap with half of previous tile
        float yRotation = 0;
        if (MirrorApp.getTileShape() == TileShape.ID_TRIANGLE ) {
            if ( positionX % 2 == 0 ) {
                yRotation = 180.0f; // to fill upper triangles
            }
            scaleX = scaleXYZ*0.90f;
        }

        // set scale and rotation of tile initially
        tile[positionY][positionX].setLocalPosition(new Vector3(xTransition, yTransition, zTransition));
        tile[positionY][positionX].setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 1f, 0f), yRotation));
        tile[positionY][positionX].setLocalScale(new Vector3(scaleX, scaleXYZ, scaleXYZ));
        //tile[positionY][positionX].setWorldRotation(Quaternion.axisAngle(new Vector3(1f, 0f, 0f), 8f * positionX));


        positionX++;
        totalTilesBuilt++;
        if ( (MirrorApp.getTileShape() == TileShape.ID_TRIANGLE  && positionX == (cols - 1)) || positionX == cols) {
            positionX = 0;
            positionY++;
            locationY = locationY - tileSizePlusGap;
        }
    }


    /**
     * Similar to function addTileToScene but to handle special triangle shaped case fillers at right
     * and left most of each row of tiles. 3D objects require different scaling and rotation angles.
     *
     * @param modelRenderable current tile reference to renderable object.
     */
    private void addFillerTileToScene(ModelRenderable modelRenderable) {

        // Create a new node for the current tile
        tile[fillerPositionY][fillerPositionX] = new Node();
        tile[fillerPositionY][fillerPositionX].setRenderable(modelRenderable);

        // Set its parent to the main anchorNode which will be the center of the picture once it is
        // being tracked
        tile[fillerPositionY][fillerPositionX].setParent(anchorNode);

        // Fix the local position in relation with parent mirror for imported obj file in the
        // y and x direction
        float tileSizePlusGap = TileNo.sizeMap.get(MirrorApp.getNoOFTiles()) + TILE_GAP;
        int incrementCounter = fillerPositionX == 0 ? 0 : fillerPositionX -1;

        float xTransition = (-MIRROR_HEIGHT + tileSizePlusGap/2.0f) / 2.0f + (incrementCounter / 2.0f) * tileSizePlusGap;
        float zTransition = fillerLocationY - tileSizePlusGap;
        float yTransition = 0.14f;
        float scaleXYZ = TileNo.scaleMap.get(MirrorApp.getNoOFTiles()) * 0.90f;

        // Do a rotation for tiles that are odd in the mirror grid and fix the x transition to
        // overlap with half of previoys tile
        float rotation = 0;
        if (fillerPositionX == 0) { // last filler in a row flip it around y axis -90 degree
            rotation = -180.0f;
        }

        // set scale and rotation of tile initially
        tile[fillerPositionY][fillerPositionX].setLocalPosition(new Vector3(xTransition, yTransition, zTransition));
        tile[fillerPositionY][fillerPositionX].setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 0f, 1f), rotation));
        tile[fillerPositionY][fillerPositionX].setLocalScale(new Vector3(scaleXYZ*0.8f, scaleXYZ, scaleXYZ));
        //tile[positionY][positionX].setLocalRotation(Quaternion.axisAngle(new Vector3(1f, 0f, 0f), 10f * positionX));

        fillerPositionY++;
        totalTilesBuilt++;
        fillerLocationY = fillerLocationY - tileSizePlusGap;
        if (fillerPositionY == rows) {
            fillerPositionY = 0;
            fillerPositionX = cols - 1;
            fillerLocationY = (MIRROR_HEIGHT + tileSizePlusGap) /2.0f + 0.005f;
        }
    }

    /**
     * rotate() is called on every frame update and it reads the byte array sent by server of processed
     * image which contains normalized rotation angle values of all the tiles, and performs the
     * local rotation based on the read value.
     *
     * Note: The function will poll() the que for any backlogged frames to processes until no
     * further frames are available.
     *
     * Again special attention need to apply the even tiles in the x direction in case of Triangle
     * pattern.
     *
     */
    public void rotate() {

        float rotationAngle;
        byte[] byteArray = MirrorApp.framesQ.poll();

        // Start rotating if we have rendered all tiles only.
        if (totalTilesBuilt >= cols*rows) {

            while (byteArray != null) {
                int byteArrayIndex = 0;
                for (int j = 0; j < rows; j++) {
                    for (int i = 0; i < cols; i++) {
                        rotationAngle = (float) byteArray[byteArrayIndex]; //(float) (Math.random() * 30f * (Math.random() > 0.5f ? 1f : -1f));

                        if (i % 2 == 1 || i == cols - 1) {
                            tile[j][i].setLocalRotation(
                                    Quaternion.axisAngle(
                                            new Vector3(1f, 0f, 0f),
                                            rotationAngle
                                    )
                            );
                        } else {
                            // For special case of triangle fillers at index 0 of each row do two rotations
                            // using the multiply operation
                            if (i == 0 && MirrorApp.getTileShape() == TileShape.ID_TRIANGLE) {
                                tile[j][i].setLocalRotation(
                                        Quaternion.multiply(
                                                Quaternion.axisAngle(
                                                        new Vector3(1f, 0f, 0f),
                                                        rotationAngle + additionalRotation
                                                ),
                                                Quaternion.axisAngle(
                                                        new Vector3(0f, 1f, 0f),
                                                        180f
                                                )
                                        )
                                );
                            } else {
                                tile[j][i].setLocalRotation(
                                        Quaternion.axisAngle(
                                                new Vector3(1f, 0f, 0f),
                                                rotationAngle + additionalRotation
                                        )
                                );
                            }
                        }
                        byteArrayIndex++;
                    }
                }
                byteArray = MirrorApp.framesQ.poll();
            }
        }
    }
}
