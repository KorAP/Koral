package de.ids_mannheim.korap.query.serialize.util;

import java.util.ArrayList;
import java.util.List;

public class Converter {

    public static List<String> enumListToStringList(List<?> objects){
        List<String> list = new ArrayList<String>();
        for (Object o:objects){
            list.add(o.toString());
        }
        return list;
    }
}
