package de.ids_mannheim.korap.query.serialize;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author hanl
 * @date 07/02/2014
 */
public class MetaQuery {

    private ObjectMapper serialier;
    private Map meta;

    public MetaQuery() {
        this.serialier = new ObjectMapper();
        meta = new HashMap();
    }

    public MetaQuery addContext(Object left, Object right, String type) {
        Map map = new HashMap();
        List l = new LinkedList();
        List r = new LinkedList();
        l.add(type);
        l.add(left);
        map.put("left", l);
        r.add(type);
        r.add(right);
        map.put("right", r);
        meta.put("context", map);
        return this;
    }

    public MetaQuery addEntry(String name, Object value) {
        meta.put(name, value);
        return this;
    }

    public String toMeta() {
        try {
            return serialier.writeValueAsString(meta);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
