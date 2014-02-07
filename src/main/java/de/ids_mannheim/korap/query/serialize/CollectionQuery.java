package de.ids_mannheim.korap.query.serialize;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.util.*;

/**
 * @author hanl
 * @date 06/12/2013
 */
public class CollectionQuery {

    private JsonFactory factory;
    private CollectionsTypes types;
    private ObjectMapper serialzer;
    private List<Map> rq;
    private List<Map> mfil;
    private List<Map> mext;

    public CollectionQuery() {
        this.serialzer = new ObjectMapper();
        this.rq = new ArrayList<>();
        this.mfil = new ArrayList<>();
        this.mext = new ArrayList<>();
        this.factory = serialzer.getFactory();
        this.types = new CollectionsTypes();
    }

    public CollectionQuery addResource(String query) {
        try {
            JsonParser jp = factory.createParser(query);
            JsonNode m = jp.readValueAsTree();
            for (JsonNode n : m)
                this.rq.add(serialzer.treeToValue(n, Map.class));
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Conversion went wrong!");
        }
        return this;
    }

    public CollectionQuery addResources(List<String> queries) {
        for (String query : queries)
            addResource(query);
        return this;
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
                Map term = types.createTerm(key, null, map.get(key).toArray(new String[0])[0], null);
                value.add(term);
            } else {
                List g = new ArrayList();
                for (String v : map.get(key))
                    g.add(types.createTerm(null, v, null));
                Map group = types.createGroup("and", key, g);
                value.add(group);
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

    // fixme: map can only have one key/value pair. thus,
    // text class can only be added once. Multiple types are not possible!
    public CollectionQuery addMetaFilter(String queries) {
        Multimap<String, String> m = resEq(queries);
        boolean multypes = m.keys().size() > 1;
        String def_key = null;

        if (!multypes)
            def_key = m.keys().toArray(new String[0])[0];

        List value = this.createValue(m);
        // todo: missing: - takes only one resource, but resources can be chained!
        if (m.values().size() == 1)
            Collections.addAll(this.mfil, types.createMetaFilter((Map) value.get(0)));
        else {
            Map group;
            if (!multypes)
                group = types.createGroup("and", def_key, value);
            else
                group = types.createGroup("and", null, value);
            Collections.addAll(this.mfil, types.createMetaFilter(group));
        }
        return this;
    }

    public CollectionQuery addMetaExtend(String queries) {
        Multimap<String, String> m = resEq(queries);
        boolean multypes = m.keys().size() > 1;
        String def_key = null;

        if (!multypes)
            def_key = m.keys().toArray(new String[0])[0];

        List value = this.createValue(m);
        // todo: missing: - takes only one resource, but resources can be chained!
        if (m.values().size() == 1)
            Collections.addAll(this.mext, types.createMetaExtend((Map) value.get(0)));
        else {
            Map group;
            if (!multypes)
                group = types.createGroup("and", def_key, value);
            else
                group = types.createGroup("and", null, value);
            Collections.addAll(this.mext, types.createMetaExtend(group));
        }
        return this;
    }

    public CollectionQuery addMetaFilter(String attr, String val) {
        return addMetaFilter(attr + ":" + val);
    }

    public CollectionQuery addMetaExtend(String attr, String val) {
        return addMetaExtend(attr + ":" + val);
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

    public void clear() {
        this.rq.clear();
        this.mfil.clear();
        this.mext.clear();
    }

    private List<Map> join() {
        List<Map> cursor = new ArrayList<>(this.rq);
        cursor.addAll(this.mfil);
        cursor.addAll(this.mext);
        return cursor;
    }

    private List<Map> getCollectionsOnly() {
        List<Map> cursor = new ArrayList<>(this.mfil);
        cursor.addAll(this.mext);
        return cursor;
    }

    /**
     * returns the meta query only and does not contain parent dependencies
     *
     * @return
     */
    public String stringify() {
        List collection = getCollectionsOnly();
        if (collection.isEmpty())
            return "";

        try {
            return serialzer.writeValueAsString(collection);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * returns the List<Map> that contains all the meta queries and resource queries
     * added to the meta query container
     *
     * @return
     */
    public List<Map> raw() {
        return join();
    }

    /**
     * returns a JSON String representation that contains all information
     * (meta query and resource meta queries alike) in a root meta JSON node
     *
     * @return
     */
    public String toCollections() {
        Map meta = new LinkedHashMap();
        meta.put("collections", join());

        try {
            return serialzer.writeValueAsString(meta);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * resolves all queries as equal (hierarchy) AND relations
     *
     * @param queries
     * @return
     */
    private Multimap<String, String> resEq(String queries) {
        Multimap<String, String> qmap = ArrayListMultimap.create();
        String[] spl = queries.split(" AND ");
        for (String query : spl) {
            String[] q = query.split(":");
            String attr = q[0];
            String val = q[1];
            qmap.put(attr, val);
        }
        return qmap;
    }


    /**
     * resolves query string with AND and OR relations alike!
     *
     * @param queries
     * @return
     */
    private Multimap<String, String> resDep(String queries) {
        return null;
    }


}
