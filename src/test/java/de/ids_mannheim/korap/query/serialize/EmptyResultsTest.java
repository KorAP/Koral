package de.ids_mannheim.korap.query.serialize;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author hanl
 * @date 02/07/2015
 */
public class EmptyResultsTest {

    private static ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testEmptyQueryObject() {
        QuerySerializer s = new QuerySerializer();
        s.setQuery("prox/unit=word/distance<=5", "cql");
        JsonNode node = mapper.valueToTree(s.build());
        assertEquals(node.has("query"), false);
        assertEquals(node.has("collection"), false);
    }

    @Test
    public void testEmptyCollectionObject() {
        QuerySerializer s = new QuerySerializer();
        s.setQuery("[base=Wort]", "poliqarp");

        JsonNode node = mapper.valueToTree(s.build());
        assertEquals(node.has("query"), true);
        assertEquals(node.has("collection"), false);
    }

    @Test
    public void testEmptyMetaObject() {
        QuerySerializer s = new QuerySerializer();
        s.setQuery("[base=Wort]", "poliqarp");

        JsonNode node = mapper.valueToTree(s.build());
        assertEquals(node.has("meta"), false);
    }

}
