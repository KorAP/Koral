package de.ids_mannheim.korap.test.cosmas2;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ids_mannheim.korap.query.serialize.QuerySerializer;

public class OPOVWithExclusionTest {

    private String query;

    private QuerySerializer qs = new QuerySerializer();
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode res;


    @Test
    public void testOPOVWithExclusion ()
            throws JsonProcessingException, IOException {
        query = "wegen #OV(%) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
//        System.out.println(res);

        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:class", res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/classRefCheck").size());
        assertEquals("classRefCheck:disjoint",
                res.at("/query/classRefCheck/0").asText());
        
        JsonNode classRefCheckOperand = res.at("/query/operands/0");
        assertEquals("operation:exclusion",
                classRefCheckOperand.at("/operation").asText());
        assertEquals(0, classRefCheckOperand.at("/frames").size());
    }
    
    @Test
    public void testOPOVWithExclusionX ()
            throws JsonProcessingException, IOException {
        query = "wegen #OV(%, X) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
//        System.out.println(res);

        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:exclusion", res.at("/query/operation").asText());

        assertEquals(1, res.at("/query/frames").size());
        assertEquals("frames:isWithin", res.at("/query/frames/0").asText());
    }
    
    @Test
    public void testOPOVWithExclusionL ()
            throws JsonProcessingException, IOException {
        query = "wegen #OV(%, L) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        System.out.println(res);

        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:class", res.at("/query/operation").asText());
        assertEquals(2, res.at("/query/classRefCheck").size());
        assertEquals("classRefCheck:intersects",
                res.at("/query/classRefCheck/0").asText());
        assertEquals("classRefCheck:disjoint",
                res.at("/query/classRefCheck/1").asText());
        
        JsonNode classRefCheckOperand = res.at("/query/operands/0");
        assertEquals("operation:exclusion",
                classRefCheckOperand.at("/operation").asText());
        assertEquals(2, classRefCheckOperand.at("/frames").size());
        assertEquals("frames:alignsLeft",
                classRefCheckOperand.at("/frames/0").asText());
        assertEquals("frames:overlapsLeft",
                classRefCheckOperand.at("/frames/1").asText());
        
        JsonNode exclusionOperands = classRefCheckOperand.at("/operands/0");
        assertEquals("operation:class", exclusionOperands.at("/operation").asText());
        assertEquals("wegen", exclusionOperands.at("/operands/0/wrap/key").asText());
        assertEquals(129, exclusionOperands.at("/classOut").asInt());
        
        exclusionOperands = classRefCheckOperand.at("/operands/1");
        assertEquals("s", exclusionOperands.at("/operands/0/wrap/key").asText());
        assertEquals(130, exclusionOperands.at("/classOut").asInt());
        
    }
    
    
    @Test
    public void testOPOVWithExclusionR ()
            throws JsonProcessingException, IOException {
        query = "wegen #OV(%, R) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        System.out.println(res);

        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:class", res.at("/query/operation").asText());
        assertEquals(2, res.at("/query/classRefCheck").size());
        assertEquals("classRefCheck:intersects",
                res.at("/query/classRefCheck/0").asText());
        assertEquals("classRefCheck:disjoint",
                res.at("/query/classRefCheck/1").asText());
        
        JsonNode classRefCheckOperand = res.at("/query/operands/0");
        assertEquals("operation:exclusion",
                classRefCheckOperand.at("/operation").asText());
        assertEquals(2, classRefCheckOperand.at("/frames").size());
        assertEquals("frames:alignsRight",
                classRefCheckOperand.at("/frames/0").asText());
        assertEquals("frames:overlapsRight",
                classRefCheckOperand.at("/frames/1").asText());
    }
    
    @Test
    public void testOPOVWithExclusionF ()
            throws JsonProcessingException, IOException {
        query = "wegen #OV(%, F) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
//        System.out.println(res);

        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:exclusion", res.at("/query/operation").asText());

        assertEquals(1, res.at("/query/frames").size());
        assertEquals("frames:matches", res.at("/query/frames/0").asText());
    }
    
    @Test
    public void testOPOVWithExclusionFE ()
            throws JsonProcessingException, IOException {
        query = "wegen #OV(%, FE) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        System.out.println(res);

        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:class", res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/classRefCheck").size());
        assertEquals("classRefCheck:unequals",
                res.at("/query/classRefCheck/0").asText());
        
        JsonNode classRefCheckOperand = res.at("/query/operands/0");
        assertEquals("operation:exclusion",
                classRefCheckOperand.at("/operation").asText());
        assertEquals(1, classRefCheckOperand.at("/frames").size());
        assertEquals("frames:matches",
                classRefCheckOperand.at("/frames/0").asText());
    }
    
    @Test
    public void testOPOVWithExclusionFI ()
            throws JsonProcessingException, IOException {
        query = "wegen #OV(%, FI) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
//        System.out.println(res);

        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:class", res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/classRefCheck").size());
        assertEquals("classRefCheck:equals",
                res.at("/query/classRefCheck/0").asText());
        
        JsonNode classRefCheckOperand = res.at("/query/operands/0");
        assertEquals("operation:exclusion",
                classRefCheckOperand.at("/operation").asText());
        assertEquals(1, classRefCheckOperand.at("/frames").size());
        assertEquals("frames:matches",
                classRefCheckOperand.at("/frames/0").asText());
    }
    
    
}
