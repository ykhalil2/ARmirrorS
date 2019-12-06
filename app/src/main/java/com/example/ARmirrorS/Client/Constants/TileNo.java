package com.example.ARmirrorS.Client.Constants;

import java.util.HashMap;
import java.util.Map;

public class TileNo {
    public static final int ID_UNDEFINED    = 0;
    public static final int ID_64x64        = 64; // 2.5cm tiles smallest and heavy computation
    public static final int ID_32x32        = 32; // 5 cm  tiles (good performance and resolution)
    public static final int ID_26x26        = 26; // 6 cm  tiles
    public static final int ID_22x22        = 22; // 7.5cm tiles
    public static final int ID_16x16        = 16; // 10 cm tiles very bad resolution

    // dimensions in meter of each tile
    public static final Map<Integer, Float> sizeMap = new HashMap<Integer, Float>();
    static {
        sizeMap.put(ID_UNDEFINED, 0.0000f); // unimplemented
        sizeMap.put(ID_64x64,     0.0000f); // unimplemented
        sizeMap.put(ID_32x32,     0.0500f);
        sizeMap.put(ID_26x26,     0.0622f);
        sizeMap.put(ID_22x22,     0.0740f);
        sizeMap.put(ID_16x16,     0.1030f);
    }

    // scale factor for scaling imported 3d object which is 5cm x 5cm for 32x32 tiles mirror
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
