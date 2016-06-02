package de.ids_mannheim.korap.query.elements;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author margaretha
 * 
 */
public class KoralToken implements Element {

    private final static KoralType type = KoralType.TOKEN;
    private Element child;

    public KoralToken (Element child) {
        this.child = child;
    }

    public Element getChild() {
        return child;
    }

    public void setChild(Element child) {
        this.child = child;
    }

    @Override
    public Map<String, Object> buildMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("@type", type.toString());
        map.put("wrap", child.buildMap());
        return map;
    }
}
