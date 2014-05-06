package de.ids_mannheim.korap.query.serialize;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
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
        this.meta = new LinkedHashMap();
    }

    public MetaQuery addContext(Integer left, String leftType,
                                Integer right, String rightType) {
        Map map = new LinkedHashMap();
        List l = new LinkedList();
        List r = new LinkedList();
        l.add(leftType);
        l.add(left);
        map.put("left", l);
        r.add(rightType);
        r.add(right);
        map.put("right", r);
        meta.put("context", map);
        return this;
    }

    public MetaQuery addEntry(String name, Object value) {
        meta.put(name, value);
        return this;
    }

    public Map raw() {
        return meta;
    }

    public String toMetaString() {
        try {
            return serialier.writeValueAsString(meta);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
