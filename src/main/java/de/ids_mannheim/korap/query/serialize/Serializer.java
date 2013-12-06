package de.ids_mannheim.korap.query.serialize;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;

/**
 * @author hanl
 * @date 05/12/2013
 */
@Deprecated
public class Serializer {


    private MetaQuerySerializer qs;
    private MetaCollectionSerializer cs;
    private ObjectMapper mapper;


    public Serializer() {
        this.qs = new MetaQuerySerializer();
        this.cs = new MetaCollectionSerializer();
        this.mapper = new ObjectMapper();
    }

    public String serializeToMeta(List m_queries) throws JsonProcessingException {
        Map metas = new HashMap();
        metas.put("meta", m_queries);
        return mapper.writeValueAsString(metas);
    }

    public String stringify(Object object) throws JsonProcessingException {
        return mapper.writeValueAsString(object);
    }

    public List<Map> serializeResources(List<String> r_queries) throws IOException {
        return cs.serializeResource(r_queries);
    }

    public String queryToMeta(String s) throws IOException {
        Map f = new HashMap();
        List e = mapper.readValue(s, ArrayList.class);
        f.put("meta", e);
        return mapper.writeValueAsString(f);
    }

    public List<Map> serializeQueries(Map<String, String> queries, MetaQuerySerializer.TYPE type) {
        return qs.serializeQueries(queries, type);
    }

    public Map addParameters(Map request, int page, int num, String cli, String cri,
                             int cls, int crs) {
        Map ctx = new LinkedHashMap();
        List left = new ArrayList();
        left.add(cli);
        left.add(cls);
        List right = new ArrayList();
        right.add(cri);
        right.add(crs);
        ctx.put("left", left);
        ctx.put("right", right);

        request.put("startPage", page);
        request.put("count", num);
        request.put("context", ctx);

        return request;

    }

}
