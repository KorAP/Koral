package de.ids_mannheim.korap.query.serialize.util;

import java.util.HashMap;
import java.util.Map;


public class ResourceMapper {

    private static final Map<String, String> map = new HashMap<>();

    static {
        map.put("surface", "t");
        map.put("lemma", "l");
        map.put("pos", "p");
        map.put("const", "c");
        map.put("dep", "d");
        map.put("morph", "msd");
    }

    public static String descriptor2policy(String descriptor) {
        if (map.containsKey(descriptor))
            return map.get(descriptor);
        else
            return descriptor;
    }

}
