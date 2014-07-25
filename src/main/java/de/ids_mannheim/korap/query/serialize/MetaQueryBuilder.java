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

    /**
     * context segment if context is either of type char or token.
     * size can differ for left and right span
     *
     * @param left
     * @param leftType
     * @param right
     * @param rightType
     * @return
     */
    public MetaQueryBuilder addContext(Integer left, String leftType,
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

    /**
     * context if of type paragraph or sentence where left and right size delimiters are irrelevant; or 2-token, 2-char
     * p/paragraph, s/sentence or token, char
     *
     * @param context
     * @return
     */
    public MetaQueryBuilder addContext(String context) {
        if (context.startsWith("s") | context.startsWith("p"))
            addEntry("context", context);
        else {
            String[] ct = context.split(",");
            String[] lc = ct[0].split("-");
            String[] rc = ct[1].split("-");
            addContext(Integer.valueOf(lc[0]), lc[1], Integer.valueOf(rc[0]), rc[1]);
        }
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
