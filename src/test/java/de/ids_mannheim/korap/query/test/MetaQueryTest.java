package de.ids_mannheim.korap.query.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import de.ids_mannheim.korap.query.serialize.MetaQueryBuilder;


public class MetaQueryTest {

    private static ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testContext () { 
        MetaQueryBuilder mqb = new MetaQueryBuilder();

        mqb.setSpanContext("2-t,4-t");
        JsonNode node = mapper.valueToTree(mqb.getSpanContext().raw());
        assertEquals(node.at("/context/left/0").asText(),"token");
        assertEquals(node.at("/context/right/0").asText(),"token");
        assertEquals(node.at("/context/left/1").asInt(),2);
        assertEquals(node.at("/context/right/1").asInt(),4);

        mqb.setSpanContext("2-token,4-token");
        node = mapper.valueToTree(mqb.getSpanContext().raw());
        assertEquals(node.at("/context/left/0").asText(),"token");
        assertEquals(node.at("/context/right/0").asText(),"token");
        assertEquals(node.at("/context/left/1").asInt(),2);
        assertEquals(node.at("/context/right/1").asInt(),4);

        mqb.setSpanContext("2-tokens,4-tokens");
        node = mapper.valueToTree(mqb.getSpanContext().raw());
        assertEquals(node.at("/context/left/0").asText(),"token");
        assertEquals(node.at("/context/right/0").asText(),"token");
        assertEquals(node.at("/context/left/1").asInt(),2);
        assertEquals(node.at("/context/right/1").asInt(),4);
        
        mqb.setSpanContext("2-c,4-c");
        node = mapper.valueToTree(mqb.getSpanContext().raw());
        assertEquals(node.at("/context/left/0").asText(),"char");
        assertEquals(node.at("/context/right/0").asText(),"char");
        assertEquals(node.at("/context/left/1").asInt(),2);
        assertEquals(node.at("/context/right/1").asInt(),4);

        mqb.setSpanContext("2-char,4-char");
        node = mapper.valueToTree(mqb.getSpanContext().raw());
        assertEquals(node.at("/context/left/0").asText(),"char");
        assertEquals(node.at("/context/right/0").asText(),"char");
        assertEquals(node.at("/context/left/1").asInt(),2);
        assertEquals(node.at("/context/right/1").asInt(),4);

        mqb.setSpanContext("2-chars,4-chars");
        node = mapper.valueToTree(mqb.getSpanContext().raw());
        assertEquals(node.at("/context/left/0").asText(),"char");
        assertEquals(node.at("/context/right/0").asText(),"char");
        assertEquals(node.at("/context/left/1").asInt(),2);
        assertEquals(node.at("/context/right/1").asInt(),4);       

        mqb.setSpanContext("2-r,4-r");
        node = mapper.valueToTree(mqb.getSpanContext().raw());
        assertEquals(node.at("/context").asText(),"2-r,4-r");
    }
}
