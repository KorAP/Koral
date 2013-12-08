package de.ids_mannheim.korap.query.serialize;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author hanl
 * @date 04/12/2013
 */
public class MetaTypes {

    public static final String Y = "yyyy";
    public static final String YM = "yyyy-mm";
    public static final String YMD = "yyyy-mm-dd";
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
            kgroup.put("@field", "korap:field#" + field);
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
            term.put("@subtype", "korap:value#" + subtype);
        term.put("@value", value);
        return term;
    }

    public Map createTerm(String field, String value, String type) {
        return createTerm(field, null, value, type);
    }

    public Map createTerm(String field, String value) {
        return createTerm(field, value,  null);
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

    public String formatDate(long date, String format) {
        DateTime time = new DateTime(date);
        String month, day;

        if (time.getDayOfMonth() < 10)
            day = "0" + time.getDayOfMonth();
        else
            day = String.valueOf(time.getDayOfMonth());

        if (time.getMonthOfYear() < 10)
            month = "0" + time.getMonthOfYear();
        else
            month = String.valueOf(time.getMonthOfYear());

        switch (format) {
            case YM:
                return time.getYear() + "-" + month;
            case YMD:
                return time.getYear() + "-" + month + "-" + day;
            default:
                return String.valueOf(time.getYear());
        }
    }

    public Map mapify(String s) throws IOException {
        return mapper.readValue(s, Map.class);
    }

    public List listify(String s) throws IOException {
        return mapper.readValue(s, LinkedList.class);
    }

    public String stringify(Map m) throws JsonProcessingException {
        return mapper.writeValueAsString(m);
    }

}
