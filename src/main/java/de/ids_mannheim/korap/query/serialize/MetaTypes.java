package de.ids_mannheim.korap.query.serialize;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hanl
 * @date 04/12/2013
 */
public class MetaTypes {

    private ObjectMapper mapper;

    public MetaTypes() {
        this.mapper = new ObjectMapper();
    }

    public Map createGroup(String relation, String field, List terms) {
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

    public Map createTerm(String field, String subtype, String value, String type) {
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

    public Map createResourceFilter(String resource, Map value) {
        Map meta = new LinkedHashMap();
        meta.put("@type", "korap:meta-filter");
        meta.put("@id", "korap-filter#" + resource);
        meta.put("@value", value);
        return meta;
    }

    public Map createResourceFilter(String resource, String value) throws IOException {
        return createResourceFilter(resource, mapify(value));
    }

    public Map createResourceExtend(String resource, Map value) {
        Map meta = new LinkedHashMap();
        meta.put("@type", "korap:meta-extend");
        meta.put("@id", "korap-filter#" + resource);
        meta.put("@value", value);
        return meta;
    }

    public Map createMetaFilter(Map value) {
        Map meta = new LinkedHashMap();
        meta.put("@type", "korap:meta-filter");
        meta.put("@value", value);
        return meta;
    }

    public Map createMetaExtend(Map value) {
        Map meta = new LinkedHashMap();
        meta.put("@type", "korap:meta-extend");
        meta.put("@value", value);
        return meta;
    }

    public String formatDate(String date) {
        return "";
    }

    public Map mapify(String s) throws IOException {
        return mapper.readValue(s, Map.class);
    }

    public String stringify(Map m) throws JsonProcessingException {
        return mapper.writeValueAsString(m);
    }

}
