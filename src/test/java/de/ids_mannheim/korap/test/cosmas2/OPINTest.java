package de.ids_mannheim.korap.test.cosmas2;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ids_mannheim.korap.query.serialize.QuerySerializer;

// EM: OPIN always returns the combination span of both operands
// MAX groups all first operand spans that are in a same second operand span

public class OPINTest {
    private String query;

    private QuerySerializer qs = new QuerySerializer();
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode res;


    @Test
    public void testOPIN () throws JsonProcessingException, IOException {
        query = "wegen #IN <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());

        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position",
                res.at("/query/operation").asText());
        assertEquals(4, res.at("/query/frames").size());
        assertEquals("frames:alignsLeft", res.at("/query/frames/0").asText());
        assertEquals("frames:alignsRight", res.at("/query/frames/1").asText());
        assertEquals("frames:isWithin", res.at("/query/frames/2").asText());
        assertEquals("frames:matches", res.at("/query/frames/3").asText());
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:span", res.at("/query/operands/1/@type").asText());
    }


    @Test
    public void testOPINWithOptionN ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(N) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position",
                res.at("/query/operation").asText());
        assertEquals("frames:isWithin",
                res.at("/query/frames/0").asText());
        assertEquals("wegen",
                res.at("/query/operands/0/wrap/key").asText());
        assertEquals("s",
                res.at("/query/operands/1/wrap/key").asText());
    }
    
    
    @Test
    public void testOPINWithOptionL ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(L) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("operation:position",
                res.at("/query/operation").asText());
        assertEquals(2, res.at("/query/frames").size());
        assertEquals("frames:alignsLeft",
                res.at("/query/frames/0").asText());
        assertEquals("frames:matches",
                res.at("/query/frames/1").asText());

        assertEquals("wegen",
                res.at("/query/operands/0/wrap/key").asText());
        assertEquals("s",
                res.at("/query/operands/1/wrap/key").asText());
    }
    
    @Test
    public void testOPINWithOptionR ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(R) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("operation:position",
                res.at("/query/operation").asText());
        assertEquals("frames:alignsRight",
                res.at("/query/frames/0").asText());
        assertEquals("frames:matches",
                res.at("/query/frames/1").asText());
        assertEquals(2, res.at("/query/frames").size());
        assertEquals("wegen",
                res.at("/query/operands/0/wrap/key").asText());
        assertEquals("s",
                res.at("/query/operands/1/wrap/key").asText());
    }

    @Test
    public void testOPINwithOptionF ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(F) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals(true,
                res.at("/query/operands/0/classRefCheck").isMissingNode());
        assertEquals("frames:matches",
                res.at("/query/frames/0").asText());
        assertEquals(true,
                res.at("/query/frames/1").isMissingNode());
    }


    @Test
    public void testOPINwithOptionFI ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(FI) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("classRefCheck:unequals",
                res.at("/query/classRefCheck/0").asText());
        assertEquals("frames:matches",
                res.at("/query/operands/0/frames/0").asText());
        assertEquals(true, res.at("/query/operands/0/frames/1")
                .isMissingNode());
        
    }


    @Test
    public void testOPINwithOptionFE ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(FE) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("classRefCheck:equals",
                res.at("/query/classRefCheck/0").asText());
        assertEquals("frames:matches",
                res.at("/query/operands/0/frames/0").asText());
        assertEquals(true, res.at("/query/operands/0/frames/1")
                .isMissingNode());
    }

    @Test
    public void testOPINWithOptionN_ALL ()
            throws JsonProcessingException, IOException {
        // ALL is default in KorAP
        query = "sich #IN(N,ALL) (&gelten /w5:10 zurecht)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position",
                res.at("/query/operation").asText());
        assertEquals("frames:isWithin",
                res.at("/query/frames/0").asText());
        assertEquals("frames:matches",
                res.at("/query/frames/1").asText());
        assertEquals("sich",
                res.at("/query/operands/0/wrap/key").asText());
        assertEquals("gelten",
                res.at("/query/operands/1/operands/0/operands/0/wrap/key").asText());
        assertEquals("zurecht",
                res.at("/query/operands/1/operands/1/operands/0/wrap/key").asText());
    }
    
    @Test
    public void testOPINWithOptionN_HIT ()
            throws JsonProcessingException, IOException {
        // EM: KorAP does not support matching in multiple hits?   
        query = "gilt #IN(N,HIT) (&gelten /w5:10 zurecht)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position",
                res.at("/query/operation").asText());
        assertEquals("frames:isWithin",
                res.at("/query/frames/0").asText());
        assertEquals("frames:matches",
                res.at("/query/frames/1").asText());
        assertEquals("wegen",
                res.at("/query/operands/0/wrap/key").asText());
        assertEquals("s",
                res.at("/query/operands/1/wrap/key").asText());
    }
    
    @Test
    public void testOPINWithOptionN_MAX ()
            throws JsonProcessingException, IOException {
        // EM: Fix operation:merge
        query = "wegen #IN(N, MAX) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());

        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(130, res.at("/query/classRef/0").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:position",
                res.at("/query/operands/0/operation").asText());
        assertEquals("frames:isAround",
                res.at("/query/operands/0/frames/0").asText());
    }
}
