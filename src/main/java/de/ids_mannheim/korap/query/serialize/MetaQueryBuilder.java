package de.ids_mannheim.korap.query.serialize;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author hanl
 * @date 07/02/2014
 */
public class MetaQueryBuilder {

    private Map meta;

    public MetaQueryBuilder() {
        this.meta = new LinkedHashMap();
    }

    public MetaQueryBuilder addContext(Integer left, String leftType,
                                       Integer right, String rightType) {
        if (leftType.equalsIgnoreCase("sentence") | leftType.equalsIgnoreCase("paragraph")) {
            addEntry("context", leftType);
            return this;
        } else if (rightType.equalsIgnoreCase("sentence") | rightType.equalsIgnoreCase("paragraph")) {
            addEntry("context", rightType);
            return this;
        }
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

    public MetaQueryBuilder addEntry(String name, Object value) {
        meta.put(name, value);
        return this;
    }

    public Map raw() {
        return meta;
    }
}
