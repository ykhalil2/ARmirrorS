package com.example.ARmirrorS.Client.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * <h1>Class TileMaterial</h1>
 * Class <b>TileMaterial</b> define the Tile Material selected by the user to be rendered by
 * Sceneform. Also contains a Hash to map Tile Material to its corresponding geometry file name.
 *
 * <p>
 *
 * @author Yussuf Khalil, Daniel King
 * @author ykhalil2@illinois.edu, dking32@illinois.edu
 *
 * @version 1.1
 * @since 2019-12-05
 */

public class TileMaterial {

    /**Tile Material not yet set.*/
    public static final int ID_UNDEFINED      = 0;
    /**Wood Oak tile material.*/
    public static final int ID_WOOD_OAK       = 1;
    /**Walnut wood material for tiles.*/
    public static final int ID_WOOD_BIRCH     = 2;
    /**Brushed Bronze metal tile material.*/
    public static final int ID_METAL_BRONZE   = 3;
    /**Metal stained Copper Material for tiles.*/
    public static final int ID_METAL_COPPER   = 4;


    /**Hash to Map Tile Material to their corresponding geometry file name end to be concatenated
     * later on with the the rest of the file name based on the size and shape chosen for the
     * tiles.
     */
    public static final Map<Integer, String> fileMap = new HashMap<Integer, String>();
    static {
        fileMap.put(ID_UNDEFINED,       "_"); // unimplemented
        fileMap.put(ID_WOOD_OAK,       "oak.sfb");
        fileMap.put(ID_WOOD_BIRCH,     "walnut.sfb");
        fileMap.put(ID_METAL_BRONZE,   "bronze.sfb");
        fileMap.put(ID_METAL_COPPER,   "copper.sfb");
    }
}
