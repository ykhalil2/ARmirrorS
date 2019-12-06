package com.example.ARmirrorS.Client.Constants;

import java.util.HashMap;
import java.util.Map;

public class TileShape {
    public static final int ID_UNDEFINED    = 0;
    public static final int ID_SQUARE       = 1;
    public static final int ID_CIRCLE       = 2;
    public static final int ID_TRIANGLE     = 3;

    public static final Map<Integer, String> fileMap = new HashMap<Integer, String>();
    static {
        fileMap.put(ID_UNDEFINED, "_"); // unimplemented
        fileMap.put(ID_SQUARE,    "square_");
        fileMap.put(ID_CIRCLE,    "circle_");
        fileMap.put(ID_TRIANGLE,  "triangle_");
    }
}
