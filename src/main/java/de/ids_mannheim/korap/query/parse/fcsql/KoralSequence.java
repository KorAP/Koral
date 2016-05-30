package de.ids_mannheim.korap.query.parse.fcsql;

import java.util.List;

public class KoralSequence {

    private boolean inOrder = false;
    private List<Object> operands;
    private List<Distance> distances;

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

    public class Distance {
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

    }
}
