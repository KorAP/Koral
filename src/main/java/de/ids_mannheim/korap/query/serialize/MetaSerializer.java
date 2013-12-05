package de.ids_mannheim.korap.query.serialize;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hanl
 * @date 05/12/2013
 */
public class MetaSerializer {


    private MetaQuerySerializer qs;
    private MetaCollectionSerializer cs;
    private ObjectMapper mapper;


    public MetaSerializer() {
        this.qs = new MetaQuerySerializer();
        this.cs = new MetaCollectionSerializer();
        this.mapper = new ObjectMapper();
    }

    public String serializeMeta(List m_queries) throws JsonProcessingException {
        Map metas = new HashMap();
        metas.put("meta", m_queries);
        return mapper.writeValueAsString(metas);
    }

    public List<Map> serialzeResources(List<String> r_queries) throws IOException {
        return cs.serialize(r_queries);
    }

    public List<Map> serializeQueries(Map<String, String> queries, MetaQuerySerializer.TYPE type) {
        return qs.serializeQueries(queries, type);
    }
}
