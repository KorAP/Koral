package de.ids_mannheim.korap.query.test.annis;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ids_mannheim.korap.query.serialize.QuerySerializer;

public class QnameWithRegexTest {
    private String query;

    private QuerySerializer qs = new QuerySerializer(1.1);
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode res;

    @Test
    public void testRegex ()
            throws JsonProcessingException, IOException {
        query = "p = /V.*/";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
        assertEquals("p", res.at("/query/wrap/layer").asText());
        assertEquals("V.*", res.at("/query/wrap/key").asText());
    }

    @Test
    public void testRegexWithFoundry () throws JsonProcessingException, IOException {
        query = "tt/p = /V.*/";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
        assertEquals("tt", res.at("/query/wrap/foundry").asText());
        assertEquals("p", res.at("/query/wrap/layer").asText());
        assertEquals("V.*", res.at("/query/wrap/key").asText());
    }
}
