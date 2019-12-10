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

public class TileNodes extends AnchorNode {

    private int tileNumber;
    private static Context context;
    private static Anchor anchor;
    private static AnchorNode anchorNode;
    public static Node[][] tile;

    private static int cols;
    private static int rows;

    // used to position tiles in local coordinates
    private static final float MIRROR_HEIGHT = 1.696f;
    private static final float FRAME_HEIGHT  = 0.198f;
    private static final float TILE_GAP      = 0.003f;

    private static int   positionX = 0;
    private static int   positionY = 0;
    private static float locationY = (MIRROR_HEIGHT) / 2.0f;
    private static int   fillerPositionX = 0;
    private static int   fillerPositionY = 0;
    private static float fillerLocationY = MIRROR_HEIGHT / 2.0f;

    private static int   totalTilesBuilt = 0;


    private static float additionalRotation = 0;

    TileNodes(Context setParent, AnchorNode setAnchorNode, Anchor setAnchor) {
        context = setParent;
        anchorNode = setAnchorNode;
        anchor = setAnchor;
        setAnchor(anchor);

        // Set the node array based on the size of the tiles requested by the user. Note that for
        // triangle shapes the rows will not change however the colums will be increased to account
        // for flipped triangles and padding at both ends. So for an n tiles we will effectively
        // have n [norma] + (n-1) [flipped] + 2 halfs at the end
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
        // overlap with half of previoys tile
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
