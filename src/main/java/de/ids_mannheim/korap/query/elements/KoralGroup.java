package de.ids_mannheim.korap.query.elements;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.ids_mannheim.korap.query.serialize.MapBuilder;

public class KoralGroup implements Element {

    private static final KoralType type = KoralType.GROUP;

    private KoralOperation operation;;

    private boolean inOrder = false;
    private List<Object> operands;
    private List<Distance> distances;

    public KoralGroup (KoralOperation operation) {
        this.operation = operation;
    }

    public boolean isInOrder() {
        return inOrder;
    }

    public void setInOrder(boolean inOrder) {
        this.inOrder = inOrder;
    }

    public List<Object> getOperands() {
        return operands;
    }

    public void setOperands(List<Object> operands) {
        this.operands = operands;
    }

    public List<Distance> getDistances() {
        return distances;
    }

    public void setDistances(List<Distance> distances) {
        this.distances = distances;
    }

    @Override
    public Map<String, Object> buildMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("@type", type.toString());
        map.put("operation", operation.toString());

        if (getDistances() != null) {
            map.put("inOrder", isInOrder());
            List<Map<String, Object>> distanceList = new ArrayList<Map<String, Object>>();
            for (Distance d : getDistances()) {
                distanceList.add(d.buildMap());
            }
            map.put("distances", distanceList);
        }

        List<Map<String, Object>> operandList = new ArrayList<Map<String, Object>>();
        for (Object o : getOperands()) {
            operandList.add(MapBuilder.buildQueryMap(o));
        }
        map.put("operands", operandList);
        return map;
    }

    public class Distance implements Element {

        private final KoralType type = KoralType.DISTANCE;
        private String key;
        private String min;
        private String max;

        public Distance (String key, int min, int max) {
            this.key = key;
            this.min = String.valueOf(min);
            this.max = String.valueOf(max);
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getMin() {
            return min;
        }

        public void setMin(String min) {
            this.min = min;
        }

        public String getMax() {
            return max;
        }

        public void setMax(String max) {
            this.max = max;
        }

        @Override
        public Map<String, Object> buildMap() {
            Map<String, Object> distanceMap = new LinkedHashMap<String, Object>();
            distanceMap.put("@type", type.toString());
            distanceMap.put("key", getKey());
            distanceMap.put("min", getMin());
            distanceMap.put("max", getMax());
            return distanceMap;

        }

    }
}
