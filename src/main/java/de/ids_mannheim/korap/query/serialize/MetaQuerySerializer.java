package de.ids_mannheim.korap.query.serialize;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;

/**
 * serializes a map of meta queries to JSON-LD. Currently this only works for simple mode queries (simple AND relation, except for date ranges)
 * Expert mode requires a full blown parser (since different combinations of OR and AND relations are possible)
 * also normalizes dates to year-month-day
 * <p/>
 * <p/>
 * User: hanl
 * Date: 11/14/13
 * Time: 2:03 PM
 */
public class MetaQuerySerializer {

    private ObjectMapper mapper;
    private MetaTypes types;

    public MetaQuerySerializer() {
        this.mapper = new ObjectMapper();
        this.types = new MetaTypes();
    }

    // construct pubdate range query as "pubDate:'<date>' ~ pubDate:'<date>'"
    //todo: how to handle regex types?
    // only handles AND relation between query attributes and values!
    // value pair : pubdate=<date>, pubPlace=<place>, etc.
    public List serializeQueries(Map<String, String> queries) {
        boolean single = true;
        boolean multypes = queries.keySet().size() > 1;
        List metavalue;
        String def_key = null;
        if (queries.size() > 1)
            single = false;

        List value = new ArrayList<>();
        for (String key : queries.keySet()) {
            if (!multypes)
                def_key = key;
            String[] dr;
            if (queries.get(key).contains("~")) {
                dr = queries.get(key).split("~");
                Map fd = types.createTerm(null, null, dr[0].trim(), "korap:date");
                Map td = types.createTerm(null, null, dr[1].trim(), "korap:date");
                Map dg = types.createGroup("between", key, Arrays.asList(fd, td));
                value.add(dg);
                continue;
            } else if (queries.get(key).contains(">")) {
                dr = queries.get(key).split(">");
                Map fd = types.createTerm(null, null, dr[0].trim(), "korap:date");
                Map td = types.createTerm(null, null, dr[1].trim(), "korap:date");
                Map dg = types.createGroup("between", key, Arrays.asList(fd, td));
                value.add(dg);
                continue;
            } else if (queries.get(key).contains("<")) {
                dr = queries.get(key).split("<");
                Map fd = types.createTerm(null, null, dr[0].trim(), "korap:date");
                Map td = types.createTerm(null, null, dr[1].trim(), "korap:date");
                Map dg = types.createGroup("between", key, Arrays.asList(fd, td));
                value.add(dg);
                continue;
            }

            Map term;
            if (multypes)
                term = types.createTerm(key, null, queries.get(key).trim(), null);
            else
                term = types.createTerm(null, null, queries.get(key).trim(), null);
            value.add(term);
        }

        // todo: missing: - takes only one resource, but resources can be chained!
        // only filters, no extension
//        metavalue.put("meta", Arrays.asList(types.createMetaFilter(resource, (Map) value.get(0))));
        if (single)
            metavalue = Arrays.asList(types.createMetaFilter((Map) value.get(0)));
        else {
            Map group;
            if (!multypes)
                group = types.createGroup("and", def_key, value);
            else
                group = types.createGroup("and", null, value);
//            metavalue.put("meta", Arrays.asList(types.createMetaFilter(resource, group)));
            metavalue = Arrays.asList(types.createMetaFilter(group));
        }
        return metavalue;
    }

    public String stringify(Map<String, String> queries) throws IOException {
        Map f = new HashMap();
        f.put("meta", serializeQueries(queries));
        return mapper.writeValueAsString(f);
    }

    public JsonNode jsonify(Map<String, String> queries) {
        List s = serializeQueries(queries);
        return mapper.valueToTree(s);
    }


}
