package de.ids_mannheim.korap.query.elements;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class KoralSpan implements Element {

    private static final KoralType koralType = KoralType.SPAN;

    private String key;
    private String foundry;
    private String layer;
    private String matchOperator;
    private List<KoralTerm> attr;

    public KoralSpan (String key, String foundry, String layer,
            MatchOperator operator) {
        this.key = key;
        this.foundry = foundry;
        this.layer = layer;
        this.matchOperator = operator.toString();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getFoundry() {
        return foundry;
    }

    public void setFoundry(String foundry) {
        this.foundry = foundry;
    }

    public String getLayer() {
        return layer;
    }

    public void setLayer(String layer) {
        this.layer = layer;
    }

    public String getMatchOperator() {
        return matchOperator;
    }

    public void setMatchOperator(String matchOperator) {
        this.matchOperator = matchOperator;
    }

    public List<KoralTerm> getAttr() {
        return attr;
    }

    public void setAttr(List<KoralTerm> attr) {
        this.attr = attr;
    }

    @Override
    public Map<String, Object> buildMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("@type", koralType.toString());
        map.put("key", getKey());
        map.put("foundry", getFoundry());
        map.put("layer", getLayer());
        map.put("match", getMatchOperator());
        return map;
    }
}
