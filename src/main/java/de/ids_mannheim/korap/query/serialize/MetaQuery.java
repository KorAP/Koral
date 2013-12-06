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
public class MetaQuery {

    private JsonFactory factory;
    private MetaTypes types;
    private ObjectMapper serialzer;
    private List<Map> rq;
    private List<Map> mfil;
    private List<Map> mext;
    private Multimap<Integer, Integer> track;

    public MetaQuery() {
        this.serialzer = new ObjectMapper();
        this.rq = new ArrayList<>();
        this.mfil = new ArrayList<>();
        this.mext = new ArrayList<>();
        this.factory = serialzer.getFactory();
        this.types = new MetaTypes();
        this.track = ArrayListMultimap.create();
    }

    public MetaQuery addResource(String query) throws IOException {
        JsonParser jp = factory.createParser(query);
        JsonNode m = jp.readValueAsTree();
        for (JsonNode n : m)
            this.rq.add(serialzer.treeToValue(n, Map.class));
        return this;
    }

    public MetaQuery addResources(List<String> queries) throws IOException {
        for (String query : queries) {
            JsonParser jp = factory.createParser(query);
            JsonNode m = jp.readValueAsTree();
            for (JsonNode n : m)
                this.rq.add(serialzer.treeToValue(n, Map.class));
        }
        return this;
    }

    public MetaQuery addMetaFilter(Map<String, String> queries) {
        //single is redundant!
        boolean single = true;
        boolean multypes = queries.keySet().size() > 1;
        String def_key = null;
        if (queries.size() > 1)
            single = false;

        List value = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        for (String key : queries.keySet()) {
            if (!multypes)
                def_key = key;
            if (queries.get(key).contains("~") | queries.get(key).contains(">") |
                    queries.get(key).contains("<")) {
                dates.add(queries.get(key));
                continue;
            }

            Map term;
            term = types.createTerm(key, null, queries.get(key).trim(), null);
            value.add(term);
        }

        String[] proc = processDates(dates);
        int idx = 3;
        if (proc[0] != null && proc[0].equals("r")) {
            Map term1 = types.createTerm(proc[1], "korap:date");
            Map term2 = types.createTerm(proc[2], "korap:date");
            Map group = types.createGroup("between", "pubDate", Arrays.asList(term1, term2));
            value.add(group);
        } else if (proc[1] != null) {
            Map term1 = types.createTerm(proc[1], "korap:date");
            Map group = types.createGroup("since", "pubDate", Arrays.asList(term1));
            value.add(group);
        } else if (proc[2] != null) {
            Map term1 = types.createTerm(proc[2], "korap:date");
            Map group = types.createGroup("until", "pubDate", Arrays.asList(term1));
            value.add(group);
        }


        for (int i = idx; i < proc.length; i++) {
            if (proc[i] != null) {
                Map term1 = types.createTerm(proc[i], "korap:date");
                Map group = types.createGroup("until", "pubDate", Arrays.asList(term1));
                value.add(group);
            }
        }

        // todo: missing: - takes only one resource, but resources can be chained!
        if (single)
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

    public MetaQuery addMetaExtend(Map<String, String> queries) {
        //single is redundant!
        boolean single = true;
        boolean multypes = queries.keySet().size() > 1;

        //!an extend to a non-existing filter is not possible
        if (this.mfil.isEmpty())
            throw new IllegalArgumentException("Extending Query " +
                    "cannot be added before Filter!");

        String def_key = null;
        if (queries.size() > 1)
            single = false;

        List value = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        for (String key : queries.keySet()) {
            if (!multypes)
                def_key = key;
            if (queries.get(key).contains("~") | queries.get(key).contains(">") |
                    queries.get(key).contains("<")) {
                dates.add(queries.get(key));
                continue;
            }
            value.add(types.createTerm(key, null, queries.get(key).trim(), null));
        }

        String[] proc = processDates(dates);
        int idx = 3;
        if (proc[0] != null && proc[0].equals("r")) {
            Map term1 = types.createTerm(proc[1], "korap:date");
            Map term2 = types.createTerm(proc[2], "korap:date");
            Map group = types.createGroup("between", "pubDate", Arrays.asList(term1, term2));
            value.add(group);
        } else if (proc[1] != null) {
            Map term1 = types.createTerm(proc[1], "korap:date");
            Map group = types.createGroup("since", "pubDate", Arrays.asList(term1));
            value.add(group);
        } else if (proc[2] != null) {
            Map term1 = types.createTerm(proc[2], "korap:date");
            Map group = types.createGroup("until", "pubDate", Arrays.asList(term1));
            value.add(group);
        }


        for (int i = idx; i < proc.length; i++) {
            if (proc[i] != null) {
                Map term1 = types.createTerm(proc[i], "korap:date");
                Map group = types.createGroup("until", "pubDate", Arrays.asList(term1));
                value.add(group);
            }
        }
        // todo: missing: - takes only one resource, but resources can be chained!
        if (single)
            Collections.addAll(this.mext, types.createMetaExtend((Map) value.get(0)));
        else {
            Map group;
            if (!multypes)
                group = types.createGroup("and", def_key, value);
            else
                group = types.createGroup("and", null, value);
            Collections.addAll(this.mext, types.createMetaExtend(group));
        }
        track.put(this.mfil.size() - 1, this.mext.size() - 1);

        return this;
    }

    public MetaQuery addMetaFilter(String attr, String val) {
        Map y = new HashMap<>();
        y.put(attr, val);
        return addMetaFilter(y);
    }

    public MetaQuery addMetaExtend(String attr, String val) {
        Map y = new HashMap<>();
        y.put(attr, val);
        return addMetaExtend(y);
    }

    private String[] processDates(List<String> dates) {
        if (dates.isEmpty())
            return new String[3];
        boolean range = false;
        String[] el = new String[dates.size() + 3];
        int idx = 3;
        for (String value : dates) {
            if (value.contains("<")) {
                String[] sp = value.split("<");
                el[1] = types.formatDate(Long.valueOf(sp[1]), MetaTypes.YMD);
            } else if (value.contains(">")) {
                String[] sp = value.split(">");
                el[2] = types.formatDate(Long.valueOf(sp[1]), MetaTypes.YMD);
            } else if (value.contains("~")) {
                range = true;
                String[] sp = value.split("~");
                el[1] = types.formatDate(Long.valueOf(sp[0]), MetaTypes.YMD);
                el[2] = types.formatDate(Long.valueOf(sp[1]), MetaTypes.YMD);
            } else {
                el[idx] = types.formatDate(Long.valueOf(value), MetaTypes.YMD);
                idx++;
            }
        }
        if (range)
            el[0] = "r";
        return el;
    }

    public void clear() {
        this.rq.clear();
        this.mfil.clear();
    }

    @Deprecated
    //todo: ordering irrelevant
    private List<Map> join() {
        List<Map> cursor = new ArrayList<>(this.rq);
        List<Map> copy = new ArrayList<>();
        if (!this.mext.isEmpty()) {
            for (int idx = 0; idx < this.mfil.size(); idx++) {
                copy.add(idx, this.mfil.get(idx));
                if (!this.track.get(idx).isEmpty()) {
                    Collection<Integer> ext = this.track.get(idx);
                    for (Integer i : ext)
                        copy.add(this.mext.get(i));
                }
            }
        } else
            copy = this.mfil;
        cursor.addAll(copy);
        return cursor;
    }

    private List<Map> join2() {
        List<Map> cursor = new ArrayList<>(this.rq);
        cursor.addAll(this.mfil);
        cursor.addAll(this.mext);
        return cursor;
    }

    public String stringify() {
        try {
            return serialzer.writeValueAsString(join2());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public JsonNode jsonify() {
        return serialzer.valueToTree(join2());
    }

    public String toMeta() {
        Map meta = new LinkedHashMap();
        meta.put("meta", join2());

        try {
            return serialzer.writeValueAsString(meta);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }


}
