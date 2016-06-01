package de.ids_mannheim.korap.query.elements;

import java.util.LinkedHashMap;
import java.util.Map;

public class KoralTerm implements Element {

    public enum KoralTermType {
        STRING("type:string"), REGEX("type:regex"), WILDCARD("type:wildcard"), PUNCT(
                "type:punct");

        String value;

        KoralTermType (String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private static final KoralType koralType = KoralType.TERM;

    private String layer;
    private String foundry;
    private String operator;
    private String key;
    private KoralTermType type;
    private boolean caseSensitive = true;
    private boolean invalid = false;

    public String getLayer() {
        return layer;
    }

    public void setLayer(String layer) {
        this.layer = layer;
    }

    public String getFoundry() {
        return foundry;
    }

    public void setFoundry(String foundry) {
        this.foundry = foundry;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public KoralTermType getType() {
        return type;
    }

    public void setType(KoralTermType regex) {
        this.type = regex;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean isCaseSensitive) {
        this.caseSensitive = isCaseSensitive;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    @Override
    public Map<String, Object> buildMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("@type", koralType.toString());
        if (!isCaseSensitive()) {
            map.put("caseInsensitive", "true");
        }
        map.put("key", getKey());
        map.put("foundry", getFoundry());
        map.put("layer", getLayer());
        map.put("type", getType().toString());
        map.put("match", getOperator());

        return map;
    }
}
