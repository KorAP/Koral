package de.ids_mannheim.korap.query.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ids_mannheim.korap.query.serialize.QuerySerializer;

import org.junit.Assert;
import org.junit.Test;
import java.util.List;
import java.util.ArrayList;


import static org.junit.Assert.assertEquals;

/**
 * @author hanl
 * @date 02/07/2015
 */
public class EmptyResultsTest {

    private static ObjectMapper mapper = new ObjectMapper();


    @Test
    public void testEmptyQueryObject () {
        QuerySerializer s = new QuerySerializer();
        s.setQuery("prox/unit=word/distance<=5", "cql");
        JsonNode node = mapper.valueToTree(s.build());
        assertEquals(node.has("query"), false);
        assertEquals(node.has("collection"), false);
    }


    @Test
    public void testEmptyCollectionObject () {
        QuerySerializer s = new QuerySerializer();
        s.setQuery("[base=Wort]", "poliqarp");

        JsonNode node = mapper.valueToTree(s.build());
        assertEquals(node.has("query"), true);
        assertEquals(node.has("collection"), false);
    }


    @Test
    public void testEmptyMetaObject () {
        QuerySerializer s = new QuerySerializer();
        s.setQuery("[base=Wort]", "poliqarp");

        JsonNode node = mapper.valueToTree(s.build());
        assertEquals(node.has("meta"), false);
    }

    @Test
    public void testWarnings () {
        QuerySerializer s = new QuerySerializer();
        s.setQuery("[base=Wort]", "poliqarp");
        List<String> l = new ArrayList<String>(1);
        l.add("Hui");
        s.addWarning(14, "Beispiel", l);
        s.addWarning(16, "Beispiel 2", null);

        JsonNode node = mapper.valueToTree(s.build());
        assertEquals(true, node.has("warnings"));
        assertEquals(14, node.at("/warnings/0/0").asInt());
        assertEquals("Beispiel", node.at("/warnings/0/1").asText());
        assertEquals("Hui", node.at("/warnings/0/2").asText());
        assertEquals(16, node.at("/warnings/1/0").asInt());
        assertEquals("Beispiel 2", node.at("/warnings/1/1").asText());
        assertEquals(false, node.has("/warnings/1/2"));
    }    
}
