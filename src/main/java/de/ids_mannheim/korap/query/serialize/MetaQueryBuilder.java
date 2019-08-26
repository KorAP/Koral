package de.ids_mannheim.korap.query.serialize;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author hanl, ndiewald
 */
public class MetaQueryBuilder {

    private static Pattern p = Pattern
            .compile("\\s*(\\d+)-(c(?:hars?)?|t(?:okens?)?)\\s*,\\s*(\\d+)-(c(?:hars?)?|t(?:okens?)?)\\s*");
    private Map meta;
    private SpanContext spanContext;


    public MetaQueryBuilder () {
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
    public MetaQueryBuilder setSpanContext (Integer left, String leftType,
            Integer right, String rightType) {
        this.spanContext = new SpanContext(left, leftType, right, rightType);
        return this;
    }


    public SpanContext getSpanContext () {
        return this.spanContext;
    }


    /**
     * context if of type paragraph or sentence where left and right
     * size delimiters are irrelevant; or 2-token, 2-char p/paragraph,
     * s/sentence or token, char.
     * Distinguish
     * 
     * @param context
     * @return
     */
    public MetaQueryBuilder setSpanContext (String context) {
        if (context != null) {

            Matcher m = p.matcher(context);
            
            if (!m.matches())
                this.spanContext = new SpanContext(context);
            else {
                int lcLen = Integer.valueOf(m.group(1));
                String lcType = m.group(2);
                int rcLen = Integer.valueOf(m.group(3));
                String rcType = m.group(4);

                if (lcType.startsWith("t")) {
                    lcType = "token";
                } else if (lcType.startsWith("c")) {
                    lcType = "char";
                }

                if (rcType.startsWith("t")) {
                    rcType = "token";
                } else if (rcType.startsWith("c")) {
                    rcType = "char";
                }
                
                this.spanContext = new SpanContext(lcLen,
                        lcType, rcLen, rcType);
            }
        }
        return this;
    }


    public MetaQueryBuilder addEntry (String name, Object value) {
        if (value != null)
            meta.put(name, value);
        return this;
    }


    public Map raw () {
        if (this.spanContext != null)
            meta.putAll(this.spanContext.raw());
        return meta;
    }

    public class SpanContext {
        private String left_type;
        private String right_type;
        private int left_size;
        private int right_size;
        private String context = null;


        /**
         * context segment if context is either of type char or token.
         * size can differ for left and right span
         * 
         * @param ls
         * @param lt
         * @param rs
         * @param rt
         * @return
         */
        public SpanContext (int ls, String lt, int rs, String rt) {
            this.left_type = lt;
            this.left_size = ls;
            this.right_type = rt;
            this.right_size = rs;
        }


        public SpanContext (String context) {
            this.context = context;
        }

        public String getRightType() {
            return this.right_type;
        }

        public String getLeftType() {
            return this.left_type;
        }

        public Integer getLeftSize() {
            return this.left_size;
        }

        public Integer getRightSize() {
            return this.right_size;
        }

        public Map raw() {
            Map meta = new LinkedHashMap();
            if (this.context == null) {
                Map map = new LinkedHashMap();
                List l = new LinkedList();
                List r = new LinkedList();
                l.add(this.left_type);
                l.add(this.left_size);
                map.put("left", l);
                r.add(this.right_type);
                r.add(this.right_size);
                map.put("right", r);
                meta.put("context", map);
            }
            else
                meta.put("context", this.context);
            return meta;
        }
    }
}
