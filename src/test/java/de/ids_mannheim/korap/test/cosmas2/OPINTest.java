package de.ids_mannheim.korap.test.cosmas2;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ids_mannheim.korap.query.serialize.QuerySerializer;

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
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(130, res.at("/query/classRef/0").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operation").asText());
        assertEquals("classRefCheck:includes",
                res.at("/query/operands/0/classRefCheck/0").asText());
        assertEquals("koral:group",
                res.at("/query/operands/0/operands/0/@type").asText());
        assertEquals("operation:position",
                res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals(true, res.at("/query/operands/0/operands/0/frames/0")
                .isMissingNode());
        assertEquals(129, res.at("/query/operands/0/classIn/0").asInt());
        assertEquals(130, res.at("/query/operands/0/classIn/1").asInt());
        assertEquals(131, res.at("/query/operands/0/classOut").asInt());
        assertEquals("koral:group",
                res.at("/query/operands/0/operands/0/@type").asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/0/operands/0/operation")
                        .asText());
        assertEquals(129,
                res.at("/query/operands/0/operands/0/operands/0/classOut")
                        .asInt());
        assertEquals("koral:span",
                res.at("/query/operands/0/operands/0/operands/0/operands/0/@type")
                        .asText());
        assertEquals("s",
                res.at("/query/operands/0/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals("koral:group", res
                .at("/query/operands/0/operands/0/operands/1/@type").asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/0/operands/1/operation")
                        .asText());
        assertEquals(130,
                res.at("/query/operands/0/operands/0/operands/1/classOut")
                        .asInt());
        assertEquals("koral:token",
                res.at("/query/operands/0/operands/0/operands/1/operands/0/@type")
                        .asText());
        assertEquals("wegen",
                res.at("/query/operands/0/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
    }


    @Test
    public void testOPINWithOptionN ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(N) <s>";
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
    
    
    @Test
    public void testOPINWithOptionL ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(L) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(130, res.at("/query/classRef/0").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:position",
                res.at("/query/operands/0/operation").asText());
        assertEquals("frames:startsWith",
                res.at("/query/operands/0/frames/0").asText());
        assertEquals("frames:matches",
                res.at("/query/operands/0/frames/1").asText());
        assertEquals(true,
                res.at("/query/operands/0/frames/2").isMissingNode());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals(129,
                res.at("/query/operands/0/operands/0/classOut").asInt());
        assertEquals("koral:span", res
                .at("/query/operands/0/operands/0/operands/0/@type").asText());
        assertEquals("s",
                res.at("/query/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals("koral:group",
                res.at("/query/operands/0/operands/1/@type").asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals(130,
                res.at("/query/operands/0/operands/1/classOut").asInt());
        assertEquals("koral:token", res
                .at("/query/operands/0/operands/1/operands/0/@type").asText());
        assertEquals("wegen",
                res.at("/query/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
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
                res.at("/query/operands/0/frames/0").asText());
        assertEquals(true,
                res.at("/query/operands/0/frames/1").isMissingNode());
    }


    @Test
    public void testOPINwithOptionFI ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(FI) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("classRefCheck:unequals",
                res.at("/query/operands/0/classRefCheck/0").asText());
        assertEquals("frames:matches",
                res.at("/query/operands/0/operands/0/frames/0").asText());
        assertEquals(true, res.at("/query/operands/0/operands/0/frames/1")
                .isMissingNode());
        
    }


    @Test
    public void testOPINwithOptionFE ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(FE) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("classRefCheck:equals",
                res.at("/query/operands/0/classRefCheck/0").asText());
        assertEquals("frames:matches",
                res.at("/query/operands/0/operands/0/frames/0").asText());
        assertEquals(true, res.at("/query/operands/0/operands/0/frames/1")
                .isMissingNode());
    }


}
