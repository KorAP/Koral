package de.ids_mannheim.korap.query.object;

import java.util.LinkedHashMap;
import java.util.Map;

public class KoralBoundary implements KoralObject {

    private static final KoralType type = KoralType.BOUNDARY;

    private int min;
    private int max;

    public KoralBoundary (int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    @Override
    public Map<String, Object> buildMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("@type", type.toString());
        if (min > -1) {
            map.put("min", getMin());
        }
        if (max > -1) {
            map.put("max", getMax());
        }
        return map;
    }
}
