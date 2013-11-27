package de.ids_mannheim.korap.query.serialize;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;

/**
 *
 * serializes a map of meta queries to JSON-LD. Currently this only works for simple mode queries (simple AND relation, except for date ranges)
 * Expert mode requires a full blown parser (since different combinations of OR and AND relations are possible)
 * also normalizes dates to year-month-day
 *
 *
 * User: hanl
 * Date: 11/14/13
 * Time: 2:03 PM
 */
public class MetaSerializer {

    private ObjectMapper mapper;

    public MetaSerializer() {
        this.mapper = new ObjectMapper();
    }

    private Map createGroup(String relation, String field, List terms) {
        if (relation == null)
            return null;

        Map kgroup = new LinkedHashMap<>();
        kgroup.put("@type", "korap:group");
        if (field != null)
            kgroup.put("field", "korap:field#" + field);
        kgroup.put("relation", relation);
        kgroup.put("operands", terms);
        return kgroup;
    }


    private Map createTerm(String field, String subtype, String value, String type) {
        Map term = new LinkedHashMap<>();
        if (type == null)
            type = "korap:term";
        term.put("@type", type);
        if (field != null)
            term.put("@field", "korap:field#" + field);
        if (subtype != null)
            term.put("subtype", "korap:value#" + subtype);
        term.put("@value", value);
        return term;
    }

    private Map createMeta(String resource, Map value) {
        Map meta = new LinkedHashMap();
        meta.put("@type", "korap:meta-filter");
        meta.put("@id", "korap-filter#" + resource);
        meta.put("@value", value);
        return meta;
    }

    private Map createMetaFilter(String resource, Map value) {
        return null;
    }


    // construct pubdate range query as "pubDate:'<date>' ~ pubDate:'<date>'"
    //todo: how to handle regex types?
    // only handles AND relation between query attributes!
    // value pair : pubdate=<date>, pubPlace=<place>, etc.
    private Map serialize(String resource, Map<String, String> queries) {
        boolean single = true;
        boolean multypes = new HashSet<>(queries.keySet()).size() > 1;
        Map metavalue = new LinkedHashMap<>();
        String def_key = null;
        if (queries.size() > 1)
            single = false;

        List value = new ArrayList<>();
        for (String key : queries.keySet()) {
            if (!multypes)
                def_key = key;
            if (queries.get(key).contains("~")) {
                String[] dr = queries.get(key).split("~");
                Map fd = createTerm(null, null, dr[0].trim(), "korap:date");
                Map td = createTerm(null, null, dr[1].trim(), "korap:date");
                Map dg = createGroup("between", key, Arrays.asList(fd, td));
                value.add(dg);
                continue;
            }

            Map term;
            if (multypes)
                term = createTerm(key, null, queries.get(key).trim(), null);
            else
                term = createTerm(null, null, queries.get(key).trim(), null);
            value.add(term);
        }

        if (single)
            metavalue.put("meta", Arrays.asList(createMeta(resource, (Map) value.get(0))));
        else {
            Map group;
            if (!multypes)
                group = createGroup("and", def_key, value);
            else
                group = createGroup("and", null, value);
            metavalue.put("meta", Arrays.asList(createMeta(resource, group)));
        }
        return metavalue;
    }

    private String formatDate(String date) {
        return "";
    }


    public String stringify(String resource, Map<String, String> queries) throws IOException {
        Map s = serialize(resource, queries);
        return mapper.writeValueAsString(s);
    }





}
