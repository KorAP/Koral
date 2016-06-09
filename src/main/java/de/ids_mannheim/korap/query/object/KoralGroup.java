package de.ids_mannheim.korap.query.object;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.ids_mannheim.korap.query.serialize.MapBuilder;
import de.ids_mannheim.korap.query.object.KoralObject;
import de.ids_mannheim.korap.query.object.KoralOperation;
import de.ids_mannheim.korap.query.object.KoralType;

/**
 * @author margaretha
 * 
 */
public class KoralGroup implements KoralObject {

    private static final KoralType type = KoralType.GROUP;

    private KoralOperation operation;

    private boolean inOrder = false;
    private List<KoralObject> operands;
    private List<Distance> distances;
    private List<Frame> frames;

    public KoralGroup (KoralOperation operation) {
        this.operation = operation;
    }

    public boolean isInOrder() {
        return inOrder;
    }

    public void setInOrder(boolean inOrder) {
        this.inOrder = inOrder;
    }

    public List<KoralObject> getOperands() {
		return operands;
	}
    
    public void setOperands(List<KoralObject> operands) {
		this.operands = operands;
	}

    public List<Distance> getDistances() {
        return distances;
    }

    public void setDistances(List<Distance> distances) {
        this.distances = distances;
    }
    
    public List<Frame> getFrames() {
		return frames;
	}

	public void setFrames(List<Frame> frames) {
		this.frames = frames;
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

    public enum Frame{
		SUCCEDS("succeeds"), SUCCEDS_DIRECTLY("succeedsDirectly"), OVERLAPS_RIGHT("overlapsRight"), 
		ALIGNS_RIGHT("alignsRight"), IS_WITHIN("isWithin"), STARTS_WITH("startsWith"), 
		MATCHES("matches"), ALIGNS_LEFT("alignsLeft"), IS_AROUND("isAround"), ENDS_WITH("endsWith"),
		OVERLAPS_LEFT("overlapsLeft"), PRECEEDS_DIRECTLY("precedesDirectly"), PRECEDES("precedes");
		
		private String value;
		Frame(String value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return "frame:"+value;
		}
	}
    
    public class Distance implements KoralObject {

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
