package de.ids_mannheim.korap.test.cosmas2;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ids_mannheim.korap.query.serialize.QuerySerializer;

public class OPINWithExclusionTest {
    private String query;

    private QuerySerializer qs = new QuerySerializer();
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode res;


    @Test
    public void testOPINWithExclusionN ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(%,N) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());

        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:exclusion",
                res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/frames").size());
        assertEquals("frames:isWithin", res.at("/query/frames/0").asText());
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:span", res.at("/query/operands/1/@type").asText());
    }


    @Test
    public void testOPINwithExclusionL ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(%, L) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());

        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:exclusion",
                res.at("/query/operation").asText());
        assertEquals(2, res.at("/query/frames").size());
        assertEquals("frames:alignsLeft", res.at("/query/frames/0").asText());
        assertEquals("frames:matches", res.at("/query/frames/1").asText());
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:span", res.at("/query/operands/1/@type").asText());
    }


    @Test
    public void testOPINwithExclusionR ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(%, R) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());

        assertEquals("operation:exclusion",
                res.at("/query/operation").asText());
        assertEquals(2, res.at("/query/frames").size());
        assertEquals("frames:alignsRight", res.at("/query/frames/0").asText());
        assertEquals("frames:matches", res.at("/query/frames/1").asText());
    }


    @Test
    public void testOPINwithExclusionF ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(%, F) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());

        assertEquals("operation:exclusion",
                res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/frames").size());
        assertEquals("frames:matches", res.at("/query/frames/0").asText());
    }


    @Test
    public void testOPINwithExclusionFE ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(%, FE) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());

        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals("130", res.at("/query/classRef/0").asText());
        
        assertEquals("operation:class", res.at("/query/operands/0/operation").asText());
        assertEquals("classRefCheck:unequals",
                res.at("/query/operands/0/classRefCheck/0").asText());

        JsonNode classRefCheckOperand = res.at("/query/operands/0/operands/0");
        assertEquals("operation:exclusion",
                classRefCheckOperand.at("/operation").asText());
        assertEquals(1, classRefCheckOperand.at("/frames").size());
        assertEquals("frames:matches",
                classRefCheckOperand.at("/frames/0").asText());
    }


    @Test
    public void testOPINwithExclusionFI ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(%, FI) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());

        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals("130", res.at("/query/classRef/0").asText());
        
        assertEquals("operation:class", res.at("/query/operands/0/operation").asText());
        assertEquals("classRefCheck:equals",
                res.at("/query/operands/0/classRefCheck/0").asText());

        JsonNode classRefCheckOperand = res.at("/query/operands/0/operands/0");
        assertEquals("operation:exclusion",
                classRefCheckOperand.at("/operation").asText());
        assertEquals(1, classRefCheckOperand.at("/frames").size());
        assertEquals("frames:matches",
                classRefCheckOperand.at("/frames/0").asText());
    }


    @Test
    public void testOPINwithMultipleExclusion1 ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(FE,%,MIN) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("classRefCheck:unequals",
                res.at("/query/operands/0/classRefCheck/0").asText());
        assertEquals("frames:matches",
                res.at("/query/operands/0/operands/0/frames/0").asText());
        //        assertEquals(true,                            res.at("/query/operands/0/operands/0/exclude").isMissingNode());
    }


    @Test
    public void testOPINwithMultipleExclusion2 ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(FE,ALL,%,MIN) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("operation:class", res.at("/query/operation").asText());
        assertEquals("classRefOp:delete", res.at("/query/classRefOp").asText());
        assertEquals(131, res.at("/query/classIn/0").asInt());
        assertEquals("classRefCheck:unequals",
                res.at("/query/operands/0/classRefCheck/0").asText());
        assertEquals("frames:matches",
                res.at("/query/operands/0/operands/0/frames/0").asText());
    }


    @Test
    public void testOPINwithMultipleExclusion3 ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(FE,ALL,%,MAX) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("operation:merge", res.at("/query/operation").asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operation").asText());
        assertEquals("classRefOp:delete",
                res.at("/query/operands/0/classRefOp").asText());
        assertEquals(131, res.at("/query/operands/0/classIn/0").asInt());
        assertEquals("classRefCheck:unequals", res
                .at("/query/operands/0/operands/0/classRefCheck/0").asText());
        assertEquals("frames:matches",
                res.at("/query/operands/0/operands/0/operands/0/frames/0")
                        .asText());
    }


}
