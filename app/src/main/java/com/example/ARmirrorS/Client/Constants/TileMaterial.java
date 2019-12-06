package com.example.ARmirrorS.Client.Constants;

import java.util.HashMap;
import java.util.Map;

public class TileMaterial {
    public static final int ID_UNDEFINED      = 0;
    public static final int ID_WOOD_OAK       = 1;
    public static final int ID_WOOD_BIRCH     = 2;
    public static final int ID_METAL_BRONZE   = 3;
    public static final int ID_METAL_COPPER   = 4;


    public static final Map<Integer, String> fileMap = new HashMap<Integer, String>();
    static {
        fileMap.put(ID_UNDEFINED,       "_"); // unimplemented
        fileMap.put(ID_WOOD_OAK,       "oak.sfb");
        fileMap.put(ID_WOOD_BIRCH,     "walnut.sfb");
        fileMap.put(ID_METAL_BRONZE,   "bronze.sfb");
        fileMap.put(ID_METAL_COPPER,   "copper.sfb");
    }
}
