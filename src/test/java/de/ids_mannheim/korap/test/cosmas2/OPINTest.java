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
        assertEquals("operation:position", res.at("/query/operation").asText());
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
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("frames:isWithin", res.at("/query/frames/0").asText());
        assertEquals("wegen", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("s", res.at("/query/operands/1/wrap/key").asText());
    }


    @Test
    public void testOPINWithOptionL ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(L) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());

        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals(2, res.at("/query/frames").size());
        assertEquals("frames:alignsLeft", res.at("/query/frames/0").asText());
        assertEquals("frames:matches", res.at("/query/frames/1").asText());

        assertEquals("wegen", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("s", res.at("/query/operands/1/wrap/key").asText());
    }


    @Test
    public void testOPINWithOptionR ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(R) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());

        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("frames:alignsRight", res.at("/query/frames/0").asText());
        assertEquals("frames:matches", res.at("/query/frames/1").asText());
        assertEquals(2, res.at("/query/frames").size());
        assertEquals("wegen", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("s", res.at("/query/operands/1/wrap/key").asText());
    }


    @Test
    public void testOPINwithOptionF ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(F) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals(true,
                res.at("/query/operands/0/classRefCheck").isMissingNode());
        assertEquals("frames:matches", res.at("/query/frames/0").asText());
        assertEquals(true, res.at("/query/frames/1").isMissingNode());
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
        assertEquals(true,
                res.at("/query/operands/0/frames/1").isMissingNode());

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
        assertEquals(true,
                res.at("/query/operands/0/frames/1").isMissingNode());
    }


    @Test
    public void testOPINWithOptionN_ALL ()
            throws JsonProcessingException, IOException {
        // ALL is default in KorAP
        query = "sich #IN(N,ALL) (&gelten /w5:10 zurecht)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("frames:isWithin", res.at("/query/frames/0").asText());
        assertEquals("frames:matches", res.at("/query/frames/1").asText());
        assertEquals("sich", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("gelten",
                res.at("/query/operands/1/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals("zurecht",
                res.at("/query/operands/1/operands/1/operands/0/wrap/key")
                        .asText());
    }


    // Default

    @Test
    public void testOPINWithOptionN_HIT ()
            throws JsonProcessingException, IOException {
        query = "gilt #IN(N,HIT) (&gelten /w5:10 zurecht)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
//        System.out.println(res);
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:class", res.at("/query/operation").asText());
        assertEquals("classRefCheck:includes",
                res.at("/query/classRefCheck/0").asText());
        assertEquals(1, res.at("/query/classRefCheck").size());
        assertEquals(129, res.at("/query/classIn/0").asInt());
        assertEquals(131, res.at("/query/classOut").asInt());

        assertEquals("operation:position",
                res.at("/query/operands/0/operation").asText());
        assertEquals("frames:isWithin",
                res.at("/query/operands/0/frames/0").asText());
        assertEquals("frames:matches",
                res.at("/query/operands/0/frames/1").asText());
        
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals("gilt",
                res.at("/query/operands/0/operands/0/operands/0/wrap/key").asText());
        
        assertEquals("operation:sequence",
                res.at("/query/operands/0/operands/1/operation").asText());

        // sequence operands
        res = res.at("/query/operands/0/operands/1/operands");
        assertEquals("gelten", res.at("/0/operands/0/wrap/key").asText());
        assertEquals(130, res.at("/0/classOut").asInt());
        assertEquals("zurecht", res.at("/1/operands/0/wrap/key").asText());
        assertEquals(130, res.at("/1/classOut").asInt());
    }
    
    @Test
    public void testOPINWithOptionFE_HIT ()
            throws JsonProcessingException, IOException {
        query = "gilt #IN(FE,HIT) (&gelten /w5:10 zurecht)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
//        System.out.println(res);
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:class", res.at("/query/operation").asText());
//        assertEquals("classRefCheck:equals",
//                res.at("/query/classRefCheck/0").asText());
        assertEquals("classRefCheck:includes",
                res.at("/query/classRefCheck/0").asText());
        assertEquals(1, res.at("/query/classRefCheck").size());
        assertEquals(2, res.at("/query/classIn").size());
        assertEquals(131, res.at("/query/classOut").asInt());

        assertEquals("operation:position",
                res.at("/query/operands/0/operation").asText());
        assertEquals("frames:matches",
                res.at("/query/operands/0/frames/0").asText());
        // positions operands
        res = res.at("/query/operands/0/operands");
        assertEquals("gilt",
                res.at("/0/operands/0/wrap/key").asText());
        assertEquals(129, res.at("/0/classOut").asInt());
        
        // sequence operands
        res = res.at("/1/operands");        
        assertEquals("gelten", res.at("/0/operands/0/wrap/key").asText());
        assertEquals(130, res.at("/0/classOut").asInt());
        assertEquals("zurecht", res.at("/1/operands/0/wrap/key").asText());
        assertEquals(130, res.at("/1/classOut").asInt());
    }
    
    @Test
    public void testOPINWithOptionFI_HIT ()
            throws JsonProcessingException, IOException {
        query = "gilt #IN(FI,HIT) (&gelten /w5:10 zurecht)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
//        System.out.println(res);
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:class", res.at("/query/operation").asText());
        assertEquals("classRefCheck:unequals",
                res.at("/query/classRefCheck/0").asText());
        assertEquals("classRefCheck:includes",
                res.at("/query/classRefCheck/1").asText());
        assertEquals(2, res.at("/query/classRefCheck").size());
        assertEquals(3, res.at("/query/classIn").size());
        assertEquals(132, res.at("/query/classOut").asInt());

        assertEquals("operation:position",
                res.at("/query/operands/0/operation").asText());
        assertEquals("frames:matches",
                res.at("/query/operands/0/frames/0").asText());
        // positions operands
        res = res.at("/query/operands/0/operands");
        assertEquals("gilt",
                res.at("/0/operands/0/wrap/key").asText());
        assertEquals(129, res.at("/0/classOut").asInt());
        assertEquals(130, res.at("/1/classOut").asInt());
        
        // sequence operands
        res = res.at("/1/operands/0/operands");        
        assertEquals("gelten", res.at("/0/operands/0/wrap/key").asText());
        assertEquals(131, res.at("/0/classOut").asInt());
        assertEquals("zurecht", res.at("/1/operands/0/wrap/key").asText());
        assertEquals(131, res.at("/1/classOut").asInt());
    }


    @Test
    @Ignore
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
