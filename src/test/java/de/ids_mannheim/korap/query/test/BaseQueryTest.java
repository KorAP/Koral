package de.ids_mannheim.korap.query.test;

import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ids_mannheim.korap.query.serialize.QuerySerializer;

/**
 * @author margaretha
 *
 */
public abstract class BaseQueryTest {
    
    protected String query;
    protected ArrayList<JsonNode> operands;
    
    private QuerySerializer qs = new QuerySerializer();
    private ObjectMapper mapper = new ObjectMapper();
    private String queryLanguage;

    public BaseQueryTest (String queryLanguage) {
        this.queryLanguage = queryLanguage;
    }
    
    protected JsonNode runQuery (String query)
            throws JsonMappingException, JsonProcessingException {
        qs.setQuery(query, queryLanguage);
        return mapper.readTree(qs.toJSON());
    }
}
