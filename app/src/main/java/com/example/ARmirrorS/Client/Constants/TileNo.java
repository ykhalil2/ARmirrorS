package com.example.ARmirrorS.Client.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * <h1>Class TileNo</h1>
 * Class <b>TileNo</b> define the number of Tiles in horizontal and vertical axis selected by
 * the user to be rendered by Sceneform.
 *
 * Also contains a two Hashs to map Tile sizes to its actual display size in centimeters. Another
 * Hash maps the tile size to a scale value to fit inside the marble mirror frame with 3 millimeter
 * gaps between each tile.
 *
 * <p>
 *
 * @author Yussuf Khalil, Daniel King
 * @author ykhalil2@illinois.edu, dking32@illinois.edu
 *
 * @version 1.1
 * @since 2019-12-05
 */

public class TileNo {

    /**No tile per row have yet been selected.*/
    public static final int ID_UNDEFINED    = 0;
    /**64 tiles per row and column. (not implemented due to dropped frames.).*/
    public static final int ID_64x64        = 64; // 2.5cm tiles smallest and heavy computation
    /**32 tiles per row and column. (5 cm  tiles).*/
    public static final int ID_32x32        = 32; // 5 cm  tiles (good performance and resolution)
    /**26 tiles per row and column. (6 cm  tiles).*/
    public static final int ID_26x26        = 26; // 6 cm  tiles
    /**22 tiles per row and column. (7.5 cm  tiles).*/
    public static final int ID_22x22        = 22; // 7.5cm tiles
    /**16 tiles per row and column. (10 cm  tiles).*/
    public static final int ID_16x16        = 16; // 10 cm tiles very bad resolution

    /**Hash Map between TileNo key and corresponding value representing dimensions in meter of the tile.*/
    public static final Map<Integer, Float> sizeMap = new HashMap<Integer, Float>();
    static {
        sizeMap.put(ID_UNDEFINED, 0.0000f); // unimplemented
        sizeMap.put(ID_64x64,     0.0000f); // unimplemented
        sizeMap.put(ID_32x32,     0.0500f);
        sizeMap.put(ID_26x26,     0.0622f);
        sizeMap.put(ID_22x22,     0.0740f);
        sizeMap.put(ID_16x16,     0.1030f);
    }

    /**Hash Map between TileNo key and corresponding value representing scale factor of tile from
     * 5cm to proper dimension. i.e. this is the scale factor for scaling imported 3d object which
     * is 5cm x 5cm for 32x32 tiles mirror.
     */
    public static final Map<Integer, Float> scaleMap = new HashMap<Integer, Float>();
    static {
        scaleMap.put(ID_UNDEFINED, 0.0000f); // unimplemented
        scaleMap.put(ID_64x64,     0.0000f); // unimplemented
        scaleMap.put(ID_32x32,     1.0000f);
        scaleMap.put(ID_26x26,     1.2440f);
        scaleMap.put(ID_22x22,     1.4800f);
        scaleMap.put(ID_16x16,     2.0600f);
    }

}
