package com.example.ARmirrorS.Client.ARProc.Nodes;

import android.content.Context;
import android.net.Uri;

import androidx.appcompat.app.AlertDialog;

import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;

/**
 * <h1>Class MirrorNodes</h1>
 * Class <b>MirrorNodes</b> used to build the rendereable mirror and place the mirror in the scene
 * based on hitResults of tracked planes or augmented image. We use completable futures here to
 * simplify the error handling and asynchronous loading. It also starts the process of adding child
 * tile nodes to the mirror.
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
public class MirrorNodes extends AnchorNode {

    private int starter;
    /**Parent Activity Running Context.*/
    private static Context context;
    /**Parent Anchor Node for mirror which is tied to the anchor created from center pose of
     * detected plane.
     */
    private static AnchorNode anchorNode;
    /**Parent Anchor for mirror obtained from pose based on hit plane detection or augmented image.*/
    private static Anchor anchor;
    /**Mirror Anchor Node in scene.*/
    public static Node mirrorAnchorNode;
    /**Tile Nodes, which will be a child of this mirror Anchor node.*/
    public  TileNodes tiles;

    /**
     * Constructor for building the marble mirror frame and setting its anchor and parent anchor node.
     * It performs the task of placing the mirror in the scene as well as constructing children
     * tiles.
     *
     * @param setParent Parent Activity Running Context.
     * @param setAnchorNode Parent Anchor Node for mirror which is tied to the anchor created from
     *                      center pose of detected plane or augmented image.
     * @param setAnchor Parent Anchor for mirror obtained from pose based on hit plane detection or
     *                  augmented image
     */
    public MirrorNodes(Context setParent, AnchorNode setAnchorNode, Anchor setAnchor) {
        context = setParent;
        anchorNode = setAnchorNode;
        anchor = setAnchor;

        tiles = new TileNodes(context, anchorNode, anchor);

        placeMirror();

        setAnchor(anchor);
    }

    /**
     * Models of the marble mirror.  We use completable futures here to simplify the error handling
     * and asynchronous loading.  The loading is started with the first construction of an instance,
     * and then used when the image is set.
     *
     * The method will Build the renderable and wait until its ready to place it in the scene by
     * calling function addMirrorNode ToScene.
     */
    private void placeMirror() {
        // name of file to render
        Uri MirrorParse = Uri.parse("MirrorFrame.sfb");

        // Build the renderable and wait until its ready to place it in the scene by calling function
        // addMirrorNode ToScene
        ModelRenderable.builder()
                .setSource(context, MirrorParse)
                .build()
                .thenAccept(modelRenderable -> addMirrorNodeToScene(modelRenderable))
                .exceptionally(throwable -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(throwable.getMessage()).show();
                    return null;
                });
    }

    /**
     * Called when the AugmentedImage is detected and should be rendered. A Sceneform node tree is
     * created based on an Anchor created from the image or the center pose of the detected plane.
     *
     * The mirror is then positioned based on the extents of the image or center plane.
     *
     * There is no need to worry about world coordinates since everything is relative to the
     * center of the image, which is the parent node of the mirror and subsequently the tiles.
     *
     * Here we create a new node for the mirror, then Set its parent to the main anchorNode which
     * will be the center of the picture once it is being tracked.
     *
     * Finally we Fix the rotation of imported obj file in the y direction.
     *
     * @param modelRenderable mirror reference to renderable object.
     */
    private void addMirrorNodeToScene(ModelRenderable modelRenderable) {
        // Create a new node for the mirror

        mirrorAnchorNode = new Node();
        mirrorAnchorNode.setRenderable(modelRenderable);

        // Set its parent to the main anchorNode which will be the center of the picture once it is
        // being tracked
        mirrorAnchorNode.setParent(this);

        // Fix the rotation of imported obj file in the y direction
        mirrorAnchorNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 1f, 0f), 90f));

        mirrorAnchorNode.setLocalPosition(new Vector3(0f, -.0165f, 0f));
        //mirrorAnchorNode.setLocalRotation(Quaternion.axisAngle(new Vector3(1f, 0f, 0f), 90f));

    }

}
