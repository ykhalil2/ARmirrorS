package com.example.ARmirrorS.Client.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * <h1>Class TileShape</h1>
 * Class <b>TileShape</b> defines the Tile shape selected by the user to be rendered by Sceneform.
 * Also contains a Hash to map Tile Shapes to their corresponding geometry file name.
 * <p>
 *
 * @author Yussuf Khalil, Daniel King
 * @author ykhalil2@illinois.edu, dking32@illinois.edu
 *
 * @version 1.1
 * @since 2019-12-05
 */

public class TileShape {

    /**Tile Shape not yet set.*/
    public static final int ID_UNDEFINED    = 0;
    /**Square Tile Shape.*/
    public static final int ID_SQUARE       = 1;
    /**Circle Tiles.*/
    public static final int ID_CIRCLE       = 2;
    /**Triangle Shape Tiles.*/
    public static final int ID_TRIANGLE     = 3;

    /**Hash to Map Tile Shapes to their corresponding geometry file name start to be concatenated
     * later on with the the rest of the file name based on the size and material chosen for the
     * tiles.
     */
    public static final Map<Integer, String> fileMap = new HashMap<Integer, String>();
    static {
        fileMap.put(ID_UNDEFINED, "_"); // unimplemented
        fileMap.put(ID_SQUARE,    "square_");
        fileMap.put(ID_CIRCLE,    "circle_");
        fileMap.put(ID_TRIANGLE,  "triangle_");
    }
}
