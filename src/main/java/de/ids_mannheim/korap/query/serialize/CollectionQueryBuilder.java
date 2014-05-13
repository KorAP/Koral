package de.ids_mannheim.korap.query.serialize;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import de.ids_mannheim.korap.resource.Relation;
import de.ids_mannheim.korap.utils.JsonUtils;
import lombok.Data;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author hanl
 * @date 06/12/2013
 */
public class CollectionQueryBuilder {

    private CollectionTypes types;
    private List<Map> rq;
    private Multimap<String, String> mfilter;
    private Multimap<String, String> mextension;
    private Relation simpleFilterRel = Relation.AND;
    private Relation simpleExtendRel = Relation.AND;


    public CollectionQueryBuilder() {
        this.rq = new ArrayList<>();
        this.mfilter = ArrayListMultimap.create();
        this.mextension = ArrayListMultimap.create();
        this.types = new CollectionTypes();
    }

    public CollectionQueryBuilder addResource(String query) {
        try {
            List v = JsonUtils.read(query, LinkedList.class);
            this.rq.addAll(v);
        } catch (IOException e) {
            throw new IllegalArgumentException("Conversion went wrong!");
        }
        return this;
    }

    public CollectionQueryBuilder addResources(List<String> queries) {
        for (String query : queries)
            addResource(query);
        return this;
    }

    public CollectionQueryBuilder addMetaFilter(String key, String value) {
        this.mfilter.put(key, value);
        return this;
    }

    public CollectionQueryBuilder addMetaFilterQuery(String queries) {
        this.mfilter.putAll(resRel(queries));
        return this;
    }

    public CollectionQueryBuilder addMetaExtend(String key, String value) {
        this.mextension.put(key, value);
        return this;
    }


    public CollectionQueryBuilder setFilterAttributeRelation(Relation rel) {
        simpleFilterRel = rel;
        return this;
    }


    public CollectionQueryBuilder setExtendAttributeRelation(Relation rel) {
        simpleExtendRel = rel;
        return this;
    }

    public CollectionQueryBuilder addMetaExtendQuery(String queries) {
        this.mextension.putAll(resRel(queries));
        return this;
    }


    private List<Map> createFilter(Relation rel) {
        String relation = rel == Relation.AND ? "and" : "or";
        List<Map> mfil = new ArrayList();
        boolean multypes = this.mfilter.keySet().size() > 1;
        String def_key = null;

        if (!multypes) {
            Multiset<String> keys = this.mfilter.keys();
            def_key = keys.toArray(new String[keys.size()])[0];
        }

        List value = this.createValue(this.mfilter);

        if (mfilter.values().size() == 1)
            Collections.addAll(mfil, types.createMetaFilter((Map) value.get(0)));
        else {
            Map group;
            if (!multypes)
                group = types.createGroup(relation, def_key, value);
            else
                group = types.createGroup(relation, null, value);
            Collections.addAll(mfil, types.createMetaFilter(group));
        }
        return mfil;
    }

    private List<Map> createExtender(Relation rel) {
        String relation = rel == Relation.AND ? "and" : "or";
        List<Map> mex = new ArrayList();
        boolean multypes = this.mextension.keys().size() > 1;
        String def_key = null;

        if (!multypes)
            def_key = this.mextension.keys().toArray(new String[0])[0];

        List value = this.createValue(this.mextension);
        // todo: missing: - takes only one resource, but resources can be chained!
        if (this.mextension.values().size() == 1)
            Collections.addAll(mex, types.createMetaExtend((Map) value.get(0)));
        else {
            Map group;
            if (!multypes)
                group = types.createGroup(relation, def_key, value);
            else
                group = types.createGroup(relation, null, value);
            Collections.addAll(mex, types.createMetaExtend(group));
        }
        return mex;
    }

    private List<Map> join() {
        List<Map> cursor = new ArrayList<>(this.rq);
        if (!this.mfilter.isEmpty())
            cursor.addAll(this.createFilter(simpleFilterRel));
        if (!this.mextension.isEmpty())
            cursor.addAll(this.createExtender(simpleExtendRel));
        return cursor;
    }

    private List createValue(Multimap<String, String> map) {
        List value = new ArrayList<>();
        String[] dates = new String[3];
        for (String key : map.keySet()) {
            if (key.equals("pubDate")) {
                dates = processDates((List<String>) map.get(key));
                continue;
            }

            if (map.get(key).size() == 1) {
                Map term = types.createTerm(key, null,
                        map.get(key).toArray(new String[0])[0], null);
                value.add(term);
            } else {
                boolean multypes = map.keySet().size() > 1;
                List g = new ArrayList();
                for (String v : map.get(key))
                    g.add(types.createTerm(null, v, null));

                if (multypes) {
                    Map group = types.createGroup("and", key, g);
                    value.add(group);
                } else
                    value.addAll(g);

            }
        }

        int idx = 3;
        if (dates[0] != null && dates[0].equals("r")) {
            Map term1 = types.createTerm(null, dates[1], "korap:date");
            Map term2 = types.createTerm(null, dates[2], "korap:date");
            Map group = types.createGroup("between", "pubDate", Arrays.asList(term1, term2));
            value.add(group);
        } else if (dates[1] != null) {
            Map term1 = types.createTerm(null, dates[1], "korap:date");
            Map group = types.createGroup("since", "pubDate", Arrays.asList(term1));
            value.add(group);
        } else if (dates[2] != null) {
            Map term1 = types.createTerm(null, dates[2], "korap:date");
            Map group = types.createGroup("until", "pubDate", Arrays.asList(term1));
            value.add(group);
        }

        for (int i = idx; i < dates.length; i++) {
            if (dates[i] != null) {
                Map term1 = types.createTerm(dates[i], "korap:date");
                Map group = types.createGroup("exact", "pubDate", Arrays.asList(term1));
                value.add(group);
            }
        }
        return value;
    }

    private String[] processDates(List<String> dates) {
        if (dates.isEmpty())
            return new String[3];
        String[] el = new String[dates.size() + 3];
        int idx = 3;
        for (String value : dates) {
            if (value.contains("<")) {
                String[] sp = value.split("<");
                el[1] = sp[1];
            } else if (value.contains(">")) {
                String[] sp = value.split(">");
                el[2] = sp[1];
            } else {
                el[idx] = value;
                idx++;
            }
        }
        if (el[1] != null && el[2] != null)
            el[0] = "r";
        return el;
    }

    public List<Map> raw() {
        return join();
    }

    public String toCollections() {
        Map meta = new LinkedHashMap();
        meta.put("collections", join());
        return JsonUtils.toJSON(meta);
    }

    /**
     * returns all references to parents and meta query as string representation
     *
     * @return
     */
    public JsonNode buildNode() {
        return JsonUtils.valueToTree(join());
    }

    public String buildString() {
        return JsonUtils.toJSON(join());
    }


    /**
     * resolves all queries as equal (hierarchy) AND/OR relations
     * grouping is not supported!
     *
     * @param queries
     * @return
     */
    private Multimap<String, String> resRel(String queries) {
        Multimap<String, String> qmap = ArrayListMultimap.create();
        String rel = queries.contains("AND") ? "AND" : "OR";

        String[] spl = queries.trim().split(rel);
        for (String query : spl) {
            String[] q = query.split("=");
            if (q.length > 1) {
                String attr = q[0].trim();
                String val = q[1].trim();
                qmap.put(attr, val);
            }
            // todo: return error when query not well-formed
        }
        return qmap;
    }

    /**
     * resolve relations and allow grouping of attributes: (tc1 and tc1) or (tc3)
     *
     * @param queries
     * @param filter  flag if either filter or extend collection
     * @return
     */
    private void resRelation(String queries, boolean filter) {
        Pattern p = Pattern.compile("\\(([\\w\\s:]+)\\)");
        List _fill = new ArrayList();
        Matcher m = p.matcher(queries);
        while (m.find()) {
            String gr = m.group(1);
            _fill.add(gr);
            String whole = "(" + gr + ")";
            int fin = queries.lastIndexOf(whole);
            String sub = queries.substring(queries.indexOf(whole), queries.lastIndexOf(whole));
            queries.replace(whole, "");
        }
    }

    private void v(String queries, boolean filter) {
        // and exclude sub-groups?? : ((tc=121))
        Pattern p = Pattern.compile("\\(([\\w\\s=]+)\\)");
        List _fill = new ArrayList();
        Matcher m = p.matcher(queries);
        while (m.find()) {
            String gr = m.group(1);

        }

    }


    public void clear() {
        this.rq.clear();
        this.mfilter.clear();
        this.mextension.clear();
    }


    private static interface Value {
    }

    @Data
    private static class Group implements Value {
        private Relation relation;
        private List<Term> _terms;

    }

    @Data
    private static class Term implements Value {

        private String _value;
    }
}
