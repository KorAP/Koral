package de.ids_mannheim.korap.test.cosmas2;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ids_mannheim.korap.query.serialize.QuerySerializer;

public class DistanceQueryTest {

	String query;
    QuerySerializer qs = new QuerySerializer();
    ObjectMapper mapper = new ObjectMapper();
    JsonNode res;
    
    @Test
    public void testExcludeLeft () throws JsonProcessingException, IOException {
        query = "Vatileaks %-w1 neue ";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        res = res.at("/query");
        assertEquals("koral:group", res.at("/@type").asText());
        assertEquals("operation:sequence", res.at("/operation").asText());
        
        res = res.at("/distances/0");
        assertEquals("cosmas:distance", res.at("/@type").asText());
        assertEquals(true, res.at("/exclude").asBoolean());
        assertEquals("w", res.at("/key").asText());
        
        res = res.at("/boundary");
        assertEquals("koral:boundary", res.at("/@type").asText());
        assertEquals(0, res.at("/min").asInt());
        assertEquals(1, res.at("/max").asInt());
        
        
        
    }
}
