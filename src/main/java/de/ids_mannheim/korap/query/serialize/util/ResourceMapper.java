package de.ids_mannheim.korap.query.serialize.util;

import java.util.HashMap;


public class ResourceMapper {

	public static String descriptor2policy(String descriptor) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("surface", "t");
		map.put("lemma", "l");
		map.put("pos", "p");
		map.put("const", "c");
		map.put("dep", "d");
		map.put("morph", "msd");
		if (map.containsKey(descriptor)) 
			return map.get(descriptor);
		else
			return descriptor;
	}

}
