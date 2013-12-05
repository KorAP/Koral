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


    private String metaString = "{\n" +
            "    \"meta\": [\n" +
            "        {\n" +
            "            \"@type\": \"korap:meta-filter\",\n" +
            "            \"@value\": {\n" +
            "                \"@type\": \"korap:group\",\n" +
            "                \"relation\": \"and\",\n" +
            "                \"operands\": [\n" +
            "                    {\n" +
            "                        \"@type\": \"korap:term\",\n" +
            "                        \"field\": \"korap:field#author\",\n" +
            "                        \"@value\": \"Goethe\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"@type\": \"korap:group\",\n" +
            "                        \"field\": \"korap:field#pubDate\",\n" +
            "                        \"relation\": \"between\",\n" +
            "                        \"operands\": [\n" +
            "                            {\n" +
            "                                \"@type\": \"korap:date\",\n" +
            "                                \"@value\": \"2013-12-5\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"@type\": \"korap:date\",\n" +
            "                                \"@value\": \"2013-12-5\"\n" +
            "                            }\n" +
            "                        ]\n" +
            "                    }\n" +
            "                ]\n" +
            "            }\n" +
            "        }\n" +
            "    ]\n" +
            "}";
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
    public List serializeQueries(Map<String, String> queries, TYPE type) {
        boolean extend, single = true;
        boolean multypes = queries.keySet().size() > 1;
        List metavalue;
        String def_key = null;
        if (queries.size() > 1)
            single = false;
        switch (type) {
            case EXTEND:
                extend = true;
                break;
            default:
                extend = false;
                break;
        }

        List value = new ArrayList<>();
        Map<String, String> dates = new LinkedHashMap<>();
        for (String key : queries.keySet()) {
            if (!multypes)
                def_key = key;
            if (key.contains("~") | key.contains(">") |
                    key.contains("<")) {
                dates.put(key, queries.get(key));
                continue;
            }

//            if (queries.get(key).contains("~")) {
//                dr = queries.get(key).split("~");
//                Map fd = types.createTerm(dr[0].trim(), "korap:date");
//                Map td = types.createTerm(dr[1].trim(), "korap:date");
//                Map dg = types.createGroup("between", key, Arrays.asList(fd, td));
//                value.add(dg);
//                continue;
//            } else if (queries.get(key).contains(">")) {
//                dr = queries.get(key).split(">");
//                Map fd = types.createTerm(dr[0].trim(), "korap:date");
//                Map td = types.createTerm(dr[1].trim(), "korap:date");
//                Map dg = types.createGroup("between", key, Arrays.asList(fd, td));
//                value.add(dg);
//                continue;
//            } else if (queries.get(key).contains("<")) {
//                dr = queries.get(key).split("<");
//                Map fd = types.createTerm(dr[0].trim(), "korap:date");
//                Map td = types.createTerm(dr[1].trim(), "korap:date");
//                Map dg = types.createGroup("between", key, Arrays.asList(fd, td));
//                value.add(dg);
//                continue;
//            }

            Map term;
            if (multypes)
                term = types.createTerm(key, null, queries.get(key).trim(), null);
            else
                term = types.createTerm(queries.get(key).trim(), null);
            value.add(term);
        }

        String[] proc = processDates(dates);
        int idx = 3;
        if (proc[0] != null && proc[0].equals("r")) {
            Map term1 = types.createTerm(proc[1], "korap:date");
            Map term2 = types.createTerm(proc[2], "korap:date");
            Map group = types.createGroup("between", "pubDate", Arrays.asList(term1, term2));
            value.add(group);
        }

        if (proc[1] != null) {
            Map term1 = types.createTerm(proc[1], "korap:date");
            Map group = types.createGroup("since", "pubDate", Arrays.asList(term1));
            value.add(group);
        }
        if (proc[2] != null) {
            Map term1 = types.createTerm(proc[2], "korap:date");
            Map group = types.createGroup("until", "pubDate", Arrays.asList(term1));
            value.add(group);
        }


        for (int i = idx; i < proc.length; i++) {
            if (proc[i] != null) {
                Map term = types.createTerm("pubDate", null, proc[i], "korap:date");
                value.add(term);
            }
        }


        // todo: missing: - takes only one resource, but resources can be chained!
        // only filters, no extension
        if (single) {
            if (extend)
                metavalue = Arrays.asList(types.createMetaExtend((Map) value.get(0)));
            else
                metavalue = Arrays.asList(types.createMetaFilter((Map) value.get(0)));
        } else {
            Map group;
            if (!multypes)
                group = types.createGroup("and", def_key, value);
            else
                group = types.createGroup("and", null, value);
            if (extend)
                metavalue = Arrays.asList(types.createMetaExtend(group));
            else
                metavalue = Arrays.asList(types.createMetaFilter(group));
        }
        return metavalue;
    }

    //todo: resource id must be added!
    public String stringify(Map<String, String> queries, TYPE type) throws IOException {
        Map f = new HashMap();
        f.put("meta", serializeQueries(queries, type));
        return mapper.writeValueAsString(f);
    }

    //fixme: only allows for one until and since entry!!
    private String[] processDates(Map<String, String> dates) {
        if (dates.isEmpty())
            return new String[3];
        boolean until = false, since = false;
        String[] el = new String[dates.keySet().size() + 3];
        int idx = 3;
        for (String key : dates.keySet()) {
            if (key.contains("<")) {
                since = true;
                el[1] = types.formatDate(Long.valueOf(dates.get(key)), MetaTypes.YMD);
            } else if (key.contains(">")) {
                until = true;
                el[2] = types.formatDate(Long.valueOf(dates.get(key)), MetaTypes.YMD);
            } else {
                el[idx] = types.formatDate(Long.valueOf(dates.get(key)), MetaTypes.YMD);
                idx++;
            }

        }
        if (since && until)
            el[0] = "r";
        return el;
    }

    public JsonNode jsonify(Map<String, String> queries) {
        List s = serializeQueries(queries, TYPE.FILTER);
        return mapper.valueToTree(s);
    }


    public enum TYPE {
        EXTEND, FILTER
    }


}
