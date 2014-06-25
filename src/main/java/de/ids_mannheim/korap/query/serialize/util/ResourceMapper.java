package de.ids_mannheim.korap.query.serialize.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author bingel
 * @date 9/05/2014
 */
public class ResourceMapper {

    private static final Map<String, String> map = new HashMap<>();

    static {
        map.put("surface", "t");
        map.put("lemma", "l");
        map.put("pos", "p");
        map.put("const", "c");
        map.put("dep", "d");
        map.put("morph", "msd");
        map.put("ANA", "pos");
    }

    public static String descriptor2policy(String descriptor) {
        if (map.containsKey(descriptor))
            return map.get(descriptor);
        else
            return descriptor;
    }

}
