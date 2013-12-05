package de.ids_mannheim.korap.query.serialize;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hanl
 * @date 04/12/2013
 */
public class MetaCollectionSerializer {

    public String meta1 = "{\n" +
            "            \"@type\": \"korap:meta-filter\",\n" +
            "            \"@id\": \"korap-filter#id-23\",\n" +
            "            \"@value\": {\n" +
            "                \"@type\": \"korap:term\",\n" +
            "                \"@value\": \"wissenschaft\"\n" +
            "            }\n" +
            "        }\n";
    public String meta2 = "{\n" +
            "        \"@type\": \"korap:meta-filter\",\n" +
            "        \"@id\": \"korap-filter#id-24\",\n" +
            "        \"@value\": {\n" +
            "            \"@type\": \"korap:group\",\n" +
            "            \"relation\": \"and\",\n" +
            "            \"operands\": [\n" +
            "                {\n" +
            "                    \"@type\": \"korap:term\",\n" +
            "                    \"@field\": \"korap:field#pubPlace\",\n" +
            "                    \"@value\": \"Erfurt\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"@type\": \"korap:term\",\n" +
            "                    \"@field\": \"korap:field#author\",\n" +
            "                    \"@value\": \"Hesse\"\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    }\n";
    private MetaTypes types;
    private ObjectMapper mapper;
    private String meta3 = "{\n" +
            "            \"@type\": \"korap:meta-extend\",\n" +
            "            \"@id\": \"korap-filter#id-25\",\n" +
            "            \"@value\": {\n" +
            "                \"@type\": \"korap:group\",\n" +
            "                \"relation\": \"and\",\n" +
            "                \"operands\": [\n" +
            "                    {\n" +
            "                        \"@type\": \"korap:group\",\n" +
            "                        \"relation\": \"comment: other values can be 'since','until' in combination with a simple korap:term\",\n" +
            "                        \"relation\": \"between\",\n" +
            "                        \"@field\": \"korap:field#pubDate\",\n" +
            "                        \"operands\": [\n" +
            "                            {\n" +
            "                                \"@type\": \"korap:date\",\n" +
            "                                \"@value\": \"comment: either long value or String representation '2013-04-29'\",\n" +
            "                                \"@value\": \"2011\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"@type\": \"korap:date\",\n" +
            "                                \"@value\": \"2013\"\n" +
            "                            }\n" +
            "                        ]\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"@type\": \"korap:term\",\n" +
            "                        \"@field\": \"korap:field#textClass\",\n" +
            "                        \"@value\": \"freizeit\"\n" +
            "                    }\n" +
            "                ]\n" +
            "            }\n" +
            "        }";
    private Map<String, Map> tester;

    public MetaCollectionSerializer() {
        this.types = new MetaTypes();
        this.mapper = new ObjectMapper();
        this.tester = tester();
    }

    //resources must be ordered: 0 without parent, 1 has 0 as parent, etc.
    // what about extend?! is that also part of the VC meta query?!
    public String serialize(String resource) throws IOException {
        Map metas = new HashMap();
        Map<String, String> pa = getParents(resource);
        List<Map> parids = new ArrayList<>();
        for (String key : pa.keySet()) {
            Map re = types.mapify(pa.get(key));
            parids.add(re);
        }
        metas.put("meta", parids);
        return mapper.writeValueAsString(metas);
    }

    private Map<String, String> getParents(String id) {
        Map<String, String> cursor = getResource(id);
        Map<String, String> parents = new HashMap<>();

        parents.put(id, cursor.get("query"));
        if (cursor.get("parent") != null && !cursor.get("parent").isEmpty())
            parents.putAll(getParents(cursor.get("parent")));
        return parents;

    }

    //todo: connection to database module!
    public Map<String, String> getResource(String id) {
        return tester.get(id);

    }

    private Map<String, Map> tester() {
        Map<String, Map> l = new HashMap<>();
        Map<String, String> s = new HashMap<>();
        s.put("id", "23");
        s.put("parent", "");
        s.put("query", meta1);

        Map<String, String> s2 = new HashMap<>();
        s2.put("id", "24");
        s2.put("parent", "23");
        s2.put("query", meta2);

        Map<String, String> s3 = new HashMap<>();
        s3.put("id", "25");
        s3.put("parent", "24");
        s3.put("query", meta3);
        l.put("23", s);
        l.put("24", s2);
        l.put("25", s3);
        return l;
    }

}
