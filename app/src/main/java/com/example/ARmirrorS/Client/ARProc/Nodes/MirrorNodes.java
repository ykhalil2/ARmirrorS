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

public class MirrorNodes extends AnchorNode {

    private static Context context;
    private static AnchorNode anchorNode;
    private static Anchor anchor;
    public static Node mirrorAnchorNode;
    public  TileNodes tiles;

    public MirrorNodes(Context setParent, AnchorNode setAnchorNode, Anchor setAnchor) {
        context = setParent;
        anchorNode = setAnchorNode;
        anchor = setAnchor;

        tiles = new TileNodes(context, anchorNode, anchor);

        placeMirror();

        setAnchor(anchor);
    }

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
