package de.ids_mannheim.korap.test.cosmas2;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ids_mannheim.korap.query.serialize.QuerySerializer;
import de.ids_mannheim.korap.query.serialize.util.StatusCodes;

import static org.junit.Assert.*;

/**
 * Tests for JSON-LD serialization of Cosmas II queries.
 * 
 * @author Joachim Bingel (bingel@ids-mannheim.de)
 * @author Nils Diewald
 * @version 1.1
 */
public class Cosmas2QueryProcessorTest {


    String query;
    ArrayList<JsonNode> operands;

    QuerySerializer qs = new QuerySerializer();
    ObjectMapper mapper = new ObjectMapper();
    JsonNode res;


    @Test
    public void testContext () throws JsonProcessingException, IOException {
        String contextString = "http://korap.ids-mannheim.de/ns/koral/0.3/context.jsonld";
        query = "foo";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals(contextString, res.get("@context").asText());
    }


    @Test
    public void testSingleToken () throws JsonProcessingException, IOException {
        query = "der";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("der", res.at("/query/wrap/key").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "&Mann";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("Mann", res.at("/query/wrap/key").asText());
        assertEquals("lemma", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
    }



    @Test
    public void testWildcardToken () throws JsonProcessingException,
            IOException {
        query = "*der";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("type:regex", res.at("/query/wrap/type").asText());
        assertEquals(".*der", res.at("/query/wrap/key").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "*de?r";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals(".*de.r", res.at("/query/wrap/key").asText());
        
        query = "*de+r";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals(".*de.?r", res.at("/query/wrap/key").asText());

        query = "*de+?r";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals(".*de.?.r", res.at("/query/wrap/key").asText());
    }


    //	
    @Test
    public void testCaseSensitivityFlag () throws JsonProcessingException,
            IOException {
        query = "$deutscher";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("deutscher", res.at("/query/wrap/key").asText());
        assertEquals("flags:caseInsensitive", res.at("/query/wrap/flags/0")
                .asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "$deutscher Bundestag";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("koral:term", res.at("/query/operands/0/wrap/@type")
                .asText());
        assertEquals("deutscher", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("flags:caseInsensitive",
                res.at("/query/operands/0/wrap/flags/0").asText());
        assertEquals("orth", res.at("/query/operands/0/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/operands/0/wrap/match")
                .asText());
        assertEquals("Bundestag", res.at("/query/operands/1/wrap/key").asText());
    }


    @Test
    public void testMORPH () throws JsonProcessingException, IOException {
        query = "MORPH(p=V)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("V", res.at("/query/wrap/key").asText());
        assertEquals("p", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "MORPH(V)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("V", res.at("/query/wrap/key").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "MORPH(tt/p=V)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("V", res.at("/query/wrap/key").asText());
        assertEquals("p", res.at("/query/wrap/layer").asText());
        assertEquals("tt", res.at("/query/wrap/foundry").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "MORPH(tt/p=\"V.*\")";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("type:regex", res.at("/query/wrap/type").asText());
        assertEquals("V.*", res.at("/query/wrap/key").asText());
        assertEquals("p", res.at("/query/wrap/layer").asText());
        assertEquals("tt", res.at("/query/wrap/foundry").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "MORPH(mate/m=temp:pres)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("temp", res.at("/query/wrap/key").asText());
        assertEquals("pres", res.at("/query/wrap/value").asText());
        assertEquals("m", res.at("/query/wrap/layer").asText());
        assertEquals("mate", res.at("/query/wrap/foundry").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "MORPH(tt/p=V & mate/m!=temp:pres)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:termGroup", res.at("/query/wrap/@type").asText());
        assertEquals("V", res.at("/query/wrap/operands/0/key").asText());
        assertEquals("p", res.at("/query/wrap/operands/0/layer").asText());
        assertEquals("tt", res.at("/query/wrap/operands/0/foundry").asText());
        assertEquals("match:eq", res.at("/query/wrap/operands/0/match")
                .asText());
        assertEquals("temp", res.at("/query/wrap/operands/1/key").asText());
        assertEquals("pres", res.at("/query/wrap/operands/1/value").asText());
        assertEquals("m", res.at("/query/wrap/operands/1/layer").asText());
        assertEquals("mate", res.at("/query/wrap/operands/1/foundry").asText());
        assertEquals("match:ne", res.at("/query/wrap/operands/1/match")
                .asText());
    }


    @Test
    public void testSequence () throws JsonProcessingException, IOException {
        query = "der Mann";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("der", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("Mann", res.at("/query/operands/1/wrap/key").asText());
        assertTrue(res.at("/query/operands/2").isMissingNode());

        query = "der Mann schläft";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("der", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("Mann", res.at("/query/operands/1/wrap/key").asText());
        assertEquals("schläft", res.at("/query/operands/2/wrap/key").asText());
        assertTrue(res.at("/query/operands/3").isMissingNode());

        query = "der Mann schläft lang";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("der", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("Mann", res.at("/query/operands/1/wrap/key").asText());
        assertEquals("schläft", res.at("/query/operands/2/wrap/key").asText());
        assertEquals("lang", res.at("/query/operands/3/wrap/key").asText());
        assertTrue(res.at("/query/operands/4").isMissingNode());

        query = "#ELEM(s)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("s", res.at("/query/wrap/key").asText());
        assertTrue(res.at("/query/key").isMissingNode());
		
        query = "der #ELEM(W)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("der", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("w", res.at("/query/operands/1/wrap/key").asText());
        assertEquals("koral:span", res.at("/query/operands/1/@type").asText());
        assertTrue(res.at("/query/operands/2").isMissingNode());

        query = "der #ELEM(W) Mann";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("der", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("w", res.at("/query/operands/1/wrap/key").asText());
        assertEquals("koral:span", res.at("/query/operands/1/@type").asText());
        assertEquals("Mann", res.at("/query/operands/2/wrap/key").asText());
        assertTrue(res.at("/query/operands/3").isMissingNode());

        query = "der MORPH(p=ADJA) Mann";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("der", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("ADJA", res.at("/query/operands/1/wrap/key").asText());
        assertEquals("p", res.at("/query/operands/1/wrap/layer").asText());
        assertEquals("Mann", res.at("/query/operands/2/wrap/key").asText());
        assertTrue(res.at("/query/operands/3").isMissingNode());
    }


    @Test
    public void testOPOR () throws JsonProcessingException, IOException {
        query = "Sonne oder Mond";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:disjunction", res.at("/query/operation")
                .asText());
        assertEquals("Sonne", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("Mond", res.at("/query/operands/1/wrap/key").asText());
        assertTrue(res.at("/query/operands/2").isMissingNode());

        query = "(Sonne scheint) oder Mond";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:disjunction", res.at("/query/operation")
                .asText());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/0/operation").asText());
        assertEquals("Sonne", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("scheint", res.at("/query/operands/0/operands/1/wrap/key")
                .asText());
        assertEquals("Mond", res.at("/query/operands/1/wrap/key").asText());
        assertTrue(res.at("/query/operands/2").isMissingNode());

        query = "(Sonne scheint) oder (Mond scheint)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:disjunction", res.at("/query/operation")
                .asText());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/0/operation").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/1/operation").asText());
        assertEquals("Sonne", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("scheint", res.at("/query/operands/0/operands/1/wrap/key")
                .asText());
        assertEquals("Mond", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("scheint", res.at("/query/operands/1/operands/1/wrap/key")
                .asText());
        assertTrue(res.at("/query/operands/2").isMissingNode());
    }


    @Test
    public void testOPORAND () throws JsonProcessingException, IOException {

        // Query
        query = "(Sonne oder Mond) und scheint";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());

        assertFalse(res.at("/query/inOrder").isMissingNode());
        assertFalse(res.at("/query/inOrder").asBoolean());

        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("t", res.at("/query/distances/0/key").asText());
        assertEquals(0, res.at("/query/distances/0/min").asInt());
        assertEquals(0, res.at("/query/distances/0/max").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:disjunction",
                res.at("/query/operands/0/operation").asText());

        assertFalse(res.at("/query/operands/0/inOrder").isMissingNode());
        assertFalse(res.at("/query/operands/0/inOrder").asBoolean());

        assertEquals("Sonne", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("Mond", res.at("/query/operands/0/operands/1/wrap/key")
                .asText());
        assertEquals("koral:token", res.at("/query/operands/1/@type").asText());
        assertEquals("scheint", res.at("/query/operands/1/wrap/key").asText());

        // Query
        query = "scheint und (Sonne oder Mond)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());

        assertFalse(res.at("/query/inOrder").isMissingNode());
        assertFalse(res.at("/query/inOrder").asBoolean());

        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("t", res.at("/query/distances/0/key").asText());
        assertEquals(0, res.at("/query/distances/0/min").asInt());
        assertEquals(0, res.at("/query/distances/0/max").asInt());
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("scheint", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:disjunction",
                res.at("/query/operands/1/operation").asText());

        assertFalse(res.at("/query/operands/1/inOrder").isMissingNode());
        assertFalse(res.at("/query/operands/1/inOrder").asBoolean());

        assertEquals("Sonne", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("Mond", res.at("/query/operands/1/operands/1/wrap/key")
                .asText());

        // Query
        query = "Regen und scheint und (Sonne oder Mond)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());

        assertFalse(res.at("/query/inOrder").isMissingNode());
        assertFalse(res.at("/query/inOrder").asBoolean());

        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("t", res.at("/query/distances/0/key").asText());
        assertEquals(0, res.at("/query/distances/0/min").asInt());
        assertEquals(0, res.at("/query/distances/0/max").asInt());
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("Regen", res.at("/query/operands/0/wrap/key").asText());

        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/1/operation").asText());

        assertFalse(res.at("/query/operands/1/inOrder").isMissingNode());
        assertFalse(res.at("/query/operands/1/inOrder").asBoolean());

        assertEquals("cosmas:distance",
                res.at("/query/operands/1/distances/0/@type").asText());
        assertEquals("t", res.at("/query/operands/1/distances/0/key").asText());
        assertEquals(0, res.at("/query/operands/1/distances/0/min").asInt());
        assertEquals(0, res.at("/query/operands/1/distances/0/max").asInt());
        assertEquals("scheint", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("koral:group", res
                .at("/query/operands/1/operands/1/@type").asText());

        assertFalse(res.at("/query/operands/1/operands/1/inOrder")
                .isMissingNode());
        assertFalse(res.at("/query/operands/1/operands/1/inOrder")
                .asBoolean());

        assertEquals("operation:disjunction",
                res.at("/query/operands/1/operands/1/operation").asText());
        assertEquals("Sonne",
                res.at("/query/operands/1/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals("Mond",
                res.at("/query/operands/1/operands/1/operands/1/wrap/key")
                        .asText());
    }


    @Test
    public void testOPNOT () throws JsonProcessingException, IOException {
        query = "Sonne nicht Mond";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("t", res.at("/query/distances/0/key").asText());
        assertEquals(0, res.at("/query/distances/0/min").asInt());
        assertEquals(0, res.at("/query/distances/0/max").asInt());
        assertTrue(res.at("/query/distances/0/exclude").asBoolean());
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("Sonne", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("Mond", res.at("/query/operands/1/wrap/key").asText());

        query = "Sonne nicht Mond nicht Sterne";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("t", res.at("/query/distances/0/key").asText());
        assertEquals(0, res.at("/query/distances/0/min").asInt());
        assertEquals(0, res.at("/query/distances/0/max").asInt());
        assertTrue(res.at("/query/distances/0/exclude").asBoolean());
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("Sonne", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/1/operation").asText());
        assertEquals("cosmas:distance",
                res.at("/query/operands/1/distances/0/@type").asText());
        assertEquals("t", res.at("/query/operands/1/distances/0/key").asText());
        assertEquals(0, res.at("/query/operands/1/distances/0/min").asInt());
        assertEquals(0, res.at("/query/operands/1/distances/0/max").asInt());
        assertTrue(res.at("/query/operands/1/distances/0/exclude")
                .asBoolean());
        assertEquals("Mond", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("Sterne", res.at("/query/operands/1/operands/1/wrap/key")
                .asText());

        query = "(Sonne nicht Mond) nicht Sterne";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("t", res.at("/query/distances/0/key").asText());
        assertEquals(0, res.at("/query/distances/0/min").asInt());
        assertEquals(0, res.at("/query/distances/0/max").asInt());
        assertTrue(res.at("/query/distances/0/exclude").asBoolean());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/0/operation").asText());
        assertEquals("cosmas:distance",
                res.at("/query/operands/0/distances/0/@type").asText());
        assertEquals("t", res.at("/query/operands/0/distances/0/key").asText());
        assertEquals(0, res.at("/query/operands/0/distances/0/min").asInt());
        assertEquals(0, res.at("/query/operands/0/distances/0/max").asInt());
        assertTrue(res.at("/query/operands/0/distances/0/exclude")
                .asBoolean());
        assertEquals("Sonne", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("Mond", res.at("/query/operands/0/operands/1/wrap/key")
                .asText());
        assertEquals("koral:token", res.at("/query/operands/1/@type").asText());
        assertEquals("Sterne", res.at("/query/operands/1/wrap/key").asText());
    }


    @Test
    public void testOPPROX () throws JsonProcessingException, IOException {
        query = "Sonne /+w1:4 Mond";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("w", res.at("/query/distances/0/key").asText());
        assertEquals(1, res.at("/query/distances/0/boundary/min").asInt());
        assertEquals(4, res.at("/query/distances/0/boundary/max").asInt());
        assertTrue(res.at("/query/inOrder").asBoolean());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/0/operation")
                .asText());
        assertEquals(129, res.at("/query/operands/0/classOut").asInt());
        assertEquals(129, res.at("/query/operands/1/classOut").asInt());
        assertEquals("koral:token", res
                .at("/query/operands/0/operands/0/@type").asText());
        assertEquals("Sonne", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("Mond", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());

        query = "Sonne /+w1:4,s0,p1:3 Mond";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("w", res.at("/query/distances/0/key").asText());
        assertEquals(1, res.at("/query/distances/0/boundary/min").asInt());
        assertEquals(4, res.at("/query/distances/0/boundary/max").asInt());
        assertEquals("s", res.at("/query/distances/1/key").asText());
        assertEquals(0, res.at("/query/distances/1/boundary/min").asInt());
        assertEquals("p", res.at("/query/distances/2/key").asText());
        assertEquals(1, res.at("/query/distances/2/boundary/min").asInt());
        assertEquals(3, res.at("/query/distances/2/boundary/max").asInt());
        assertTrue(res.at("/query/inOrder").asBoolean());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/0/operation")
                .asText());
        assertEquals(129, res.at("/query/operands/0/classOut").asInt());
        assertEquals(129, res.at("/query/operands/1/classOut").asInt());
        assertEquals("koral:token", res
                .at("/query/operands/0/operands/0/@type").asText());
        assertEquals("Sonne", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("Mond", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());

        // Order of min and max is irrelevant in C2
        query = "Sonne /+w4:1,s0,p3:1 Mond";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("w", res.at("/query/distances/0/key").asText());
        assertEquals(1, res.at("/query/distances/0/boundary/min").asInt());
        assertEquals(4, res.at("/query/distances/0/boundary/max").asInt());
        assertEquals("s", res.at("/query/distances/1/key").asText());
        assertEquals(0, res.at("/query/distances/1/boundary/min").asInt());
        assertEquals("p", res.at("/query/distances/2/key").asText());
        assertEquals(1, res.at("/query/distances/2/boundary/min").asInt());
        assertEquals(3, res.at("/query/distances/2/boundary/max").asInt());
        assertTrue(res.at("/query/inOrder").asBoolean());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/0/operation")
                .asText());
        assertEquals(129, res.at("/query/operands/0/classOut").asInt());
        assertEquals(129, res.at("/query/operands/1/classOut").asInt());
        assertEquals("koral:token", res
                .at("/query/operands/0/operands/0/@type").asText());
        assertEquals("Sonne", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("Mond", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());

        
        query = "Sonne /+w4 Mond";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("w", res.at("/query/distances/0/key").asText());
        assertEquals(0, res.at("/query/distances/0/boundary/min").asInt());
        assertEquals(4, res.at("/query/distances/0/boundary/max").asInt());

        query = "Sonne /-w4 Mond";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("w", res.at("/query/distances/0/key").asText());
        assertEquals(0, res.at("/query/distances/0/boundary/min").asInt());
        assertEquals(4, res.at("/query/distances/0/boundary/max").asInt());
        assertEquals("Sonne", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("Mond", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());

        query = "Sonne /w4 Mond";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("w", res.at("/query/distances/0/key").asText());
        assertEquals(0, res.at("/query/distances/0/boundary/min").asInt());
        assertEquals(4, res.at("/query/distances/0/boundary/max").asInt());
        assertEquals("Sonne", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("Mond", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertFalse(res.at("/query/inOrder").asBoolean());
    }


    @Test
    public void testOPPROXNested () throws JsonProcessingException, IOException {
        query = "Sonne /+w1:4 Mond /+w1:7 Sterne";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("w", res.at("/query/distances/0/key").asText());
        assertEquals(1, res.at("/query/distances/0/boundary/min").asInt());
        assertEquals(4, res.at("/query/distances/0/boundary/max").asInt());
        assertTrue(res.at("/query/inOrder").asBoolean());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/0/operation")
                .asText());
        assertEquals(129, res.at("/query/operands/0/classOut").asInt());
        assertEquals("Sonne", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals(129, res.at("/query/operands/1/classOut").asInt());
        assertEquals("operation:sequence",
                res.at("/query/operands/1/operands/0/operation").asText());
        assertEquals("w", res
                .at("/query/operands/1/operands/0/distances/0/key").asText());
        assertEquals(1,
                res.at("/query/operands/1/operands/0/distances/0/boundary/min")
                        .asInt());
        assertEquals(7,
                res.at("/query/operands/1/operands/0/distances/0/boundary/max")
                        .asInt());
        assertEquals(130,
                res.at("/query/operands/1/operands/0/operands/0/classOut")
                        .asInt());
        assertEquals(
                "Mond",
                res.at("/query/operands/1/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(130,
                res.at("/query/operands/1/operands/0/operands/1/classOut")
                        .asInt());
        assertEquals(
                "Sterne",
                res.at("/query/operands/1/operands/0/operands/1/operands/0/wrap/key")
                        .asText());

        query = "Sonne /+w1:4 Mond /-w1:7 Sterne";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("Sonne", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals(
                "Mond",
                res.at("/query/operands/1/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(
                "Sterne",
                res.at("/query/operands/1/operands/0/operands/1/operands/0/wrap/key")
                        .asText());

        query = "Sonne /-w4 Mond /+w2 Sterne";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("w", res.at("/query/distances/0/key").asText());
        assertEquals(0, res.at("/query/distances/0/boundary/min").asInt());
        assertEquals(4, res.at("/query/distances/0/boundary/max").asInt());
        assertTrue(res.at("/query/inOrder").asBoolean());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/1/operation")
                .asText());
        assertEquals(129, res.at("/query/operands/0/classOut").asInt());
        assertEquals("Sonne", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals(129, res.at("/query/operands/0/classOut").asInt());
        assertEquals("operation:sequence",
                res.at("/query/operands/1/operands/0/operation").asText());
        assertEquals("w", res
                .at("/query/operands/1/operands/0/distances/0/key").asText());
        assertEquals(0,
                res.at("/query/operands/1/operands/0/distances/0/boundary/min")
                        .asInt());
        assertEquals(2,
                res.at("/query/operands/1/operands/0/distances/0/boundary/max")
                        .asInt());
        assertEquals(130,
                res.at("/query/operands/1/operands/0/operands/0/classOut")
                        .asInt());
        assertEquals(
                "Mond",
                res.at("/query/operands/1/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(130,
                res.at("/query/operands/1/operands/0/operands/1/classOut")
                        .asInt());
        assertEquals(
                "Sterne",
                res.at("/query/operands/1/operands/0/operands/1/operands/0/wrap/key")
                        .asText());

    }


    @Test
    public void testBEG_END () throws JsonProcessingException, IOException {
        query = "#BEG(der /w3:5 Mann)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(0, res.at("/query/spanRef/0").asInt());
        assertEquals(1, res.at("/query/spanRef/1").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/0/operation").asText());
        assertEquals("cosmas:distance",
                res.at("/query/operands/0/distances/0/@type").asText());
        assertEquals("w", res.at("/query/operands/0/distances/0/key").asText());
        assertEquals(3, res.at("/query/operands/0/distances/0/boundary/min")
                .asInt());
        assertEquals(5, res.at("/query/operands/0/distances/0/boundary/max")
                .asInt());
        assertFalse(res.at("/query/operands/0/inOrder").asBoolean());
        assertEquals("koral:token", res
                .at("/query/operands/0/operands/0/@type").asText());
        assertEquals("der", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("Mann", res.at("/query/operands/0/operands/1/wrap/key")
                .asText());

        query = "#BEG(der /w3:5 Mann) /+w10 kommt";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("w", res.at("/query/distances/0/key").asText());
        assertEquals(0, res.at("/query/distances/0/boundary/min").asInt());
        assertEquals(10, res.at("/query/distances/0/boundary/max").asInt());
        assertTrue(res.at("/query/inOrder").asBoolean());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/0/operation")
                .asText());
        assertEquals(129, res.at("/query/operands/0/classOut").asInt());
        assertEquals("koral:reference",
                res.at("/query/operands/0/operands/0/@type").asText());
        assertEquals("operation:focus",
                res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals(0, res.at("/query/operands/0/operands/0/spanRef/0")
                .asInt());
        assertEquals(1, res.at("/query/operands/0/operands/0/spanRef/1")
                .asInt());
        assertEquals("koral:group",
                res.at("/query/operands/0/operands/0/operands/0/@type")
                        .asText());
        assertEquals("operation:sequence",
                res.at("/query/operands/0/operands/0/operands/0/operation")
                        .asText());
        assertEquals(
                "cosmas:distance",
                res.at("/query/operands/0/operands/0/operands/0/distances/0/@type")
                        .asText());
        assertEquals(
                "w",
                res.at("/query/operands/0/operands/0/operands/0/distances/0/key")
                        .asText());
        assertEquals(
                3,
                res.at("/query/operands/0/operands/0/operands/0/distances/0/boundary/min")
                        .asInt());
        assertEquals(
                5,
                res.at("/query/operands/0/operands/0/operands/0/distances/0/boundary/max")
                        .asInt());
        assertFalse(res.at("/query/operands/0/operands/0/operands/0/inOrder")
                .asBoolean());
        assertEquals(
                "koral:token",
                res.at("/query/operands/0/operands/0/operands/0/operands/0/@type")
                        .asText());
        assertEquals(
                "der",
                res.at("/query/operands/0/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(
                "Mann",
                res.at("/query/operands/0/operands/0/operands/0/operands/1/wrap/key")
                        .asText());
        assertEquals("operation:class", res.at("/query/operands/1/operation")
                .asText());
        assertEquals(129, res.at("/query/operands/1/classOut").asInt());
        assertEquals("koral:token", res
                .at("/query/operands/1/operands/0/@type").asText());
        assertEquals("kommt", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());

        query = "kommt /+w10 #BEG(der /w3:5 Mann)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("w", res.at("/query/distances/0/key").asText());
        assertEquals(0, res.at("/query/distances/0/boundary/min").asInt());
        assertEquals(10, res.at("/query/distances/0/boundary/max").asInt());
        assertTrue(res.at("/query/inOrder").asBoolean());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/1/operation")
                .asText());
        assertEquals(129, res.at("/query/operands/1/classOut").asInt());
        assertEquals("koral:reference",
                res.at("/query/operands/1/operands/0/@type").asText());
        assertEquals("operation:focus",
                res.at("/query/operands/1/operands/0/operation").asText());
        assertEquals(0, res.at("/query/operands/1/operands/0/spanRef/0")
                .asInt());
        assertEquals(1, res.at("/query/operands/1/operands/0/spanRef/1")
                .asInt());
        assertEquals("koral:group",
                res.at("/query/operands/1/operands/0/operands/0/@type")
                        .asText());
        assertEquals("operation:sequence",
                res.at("/query/operands/1/operands/0/operands/0/operation")
                        .asText());
        assertEquals(
                "cosmas:distance",
                res.at("/query/operands/1/operands/0/operands/0/distances/0/@type")
                        .asText());
        assertEquals(
                "w",
                res.at("/query/operands/1/operands/0/operands/0/distances/0/key")
                        .asText());
        assertEquals(
                3,
                res.at("/query/operands/1/operands/0/operands/0/distances/0/boundary/min")
                        .asInt());
        assertEquals(
                5,
                res.at("/query/operands/1/operands/0/operands/0/distances/0/boundary/max")
                        .asInt());
        assertFalse(res.at("/query/operands/1/operands/0/operands/0/inOrder")
                .asBoolean());
        assertEquals(
                "koral:token",
                res.at("/query/operands/1/operands/0/operands/0/operands/0/@type")
                        .asText());
        assertEquals(
                "der",
                res.at("/query/operands/1/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(
                "Mann",
                res.at("/query/operands/1/operands/0/operands/0/operands/1/wrap/key")
                        .asText());
        assertEquals("operation:class", res.at("/query/operands/0/operation")
                .asText());
        assertEquals(129, res.at("/query/operands/0/classOut").asInt());
        assertEquals("koral:token", res
                .at("/query/operands/0/operands/0/@type").asText());
        assertEquals("kommt", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());

        query = "#END(der /w3:5 Mann)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(-1, res.at("/query/spanRef/0").asInt());
        assertEquals(1, res.at("/query/spanRef/1").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/0/operation").asText());
        assertEquals("cosmas:distance",
                res.at("/query/operands/0/distances/0/@type").asText());
        assertEquals("w", res.at("/query/operands/0/distances/0/key").asText());
        assertEquals(3, res.at("/query/operands/0/distances/0/boundary/min")
                .asInt());
        assertEquals(5, res.at("/query/operands/0/distances/0/boundary/max")
                .asInt());
        assertFalse(res.at("/query/operands/0/inOrder").asBoolean());
        assertEquals("koral:token", res
                .at("/query/operands/0/operands/0/@type").asText());
        assertEquals("der", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("Mann", res.at("/query/operands/0/operands/1/wrap/key")
                .asText());
    }


    @Test
    public void testELEM () throws JsonProcessingException, IOException {
        query = "#ELEM(S)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("s", res.at("/query/wrap/key").asText());

		query = "#ELEM(base/c=NP)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("base", res.at("/query/wrap/foundry").asText());
        assertEquals("c", res.at("/query/wrap/layer").asText());
        assertEquals("NP", res.at("/query/wrap/key").asText());

        query = "#ELEM(W ANA=N)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("w", res.at("/query/wrap/key").asText());
		
        assertEquals("koral:term", res.at("/query/attr/@type").asText());
        assertEquals("N", res.at("/query/attr/key").asText());
        assertEquals("p", res.at("/query/attr/layer").asText());
        assertEquals("match:eq", res.at("/query/attr/match").asText());

        query = "#ELEM(W ANA != 'N V')";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("w", res.at("/query/wrap/key").asText());
        assertEquals("koral:termGroup", res.at("/query/attr/@type").asText());
        assertEquals("relation:and", res.at("/query/attr/relation").asText());
        assertEquals("koral:term", res.at("/query/attr/operands/0/@type")
                .asText());
        assertEquals("N", res.at("/query/attr/operands/0/key").asText());
        assertEquals("p", res.at("/query/attr/operands/0/layer").asText());
        assertEquals("match:ne", res.at("/query/attr/operands/0/match")
                .asText());
        assertEquals("koral:term", res.at("/query/attr/operands/1/@type")
                .asText());
        assertEquals("V", res.at("/query/attr/operands/1/key").asText());
        assertEquals("p", res.at("/query/attr/operands/1/layer").asText());
        assertEquals("match:ne", res.at("/query/attr/operands/1/match")
                .asText());

        query = "#ELEM(W ANA != 'N A V' Genre = Sport)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("w", res.at("/query/wrap/key").asText());
        assertEquals("koral:termGroup", res.at("/query/attr/@type").asText());
        assertEquals("relation:and", res.at("/query/attr/relation").asText());
        assertEquals("koral:termGroup", res.at("/query/attr/operands/0/@type")
                .asText());
        assertEquals("relation:and", res.at("/query/attr/operands/0/relation")
                .asText());
        assertEquals("N", res.at("/query/attr/operands/0/operands/0/key")
                .asText());
        assertEquals("A", res.at("/query/attr/operands/0/operands/1/key")
                .asText());
        assertEquals("V", res.at("/query/attr/operands/0/operands/2/key")
                .asText());
        assertEquals("Genre", res.at("/query/attr/operands/1/layer").asText());
        assertEquals("Sport", res.at("/query/attr/operands/1/key").asText());

        query = "#ELEM(W ANA != 'N A V' Genre != 'Sport Politik')";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("w", res.at("/query/wrap/key").asText());
        assertEquals("koral:termGroup", res.at("/query/attr/@type").asText());
        assertEquals("relation:and", res.at("/query/attr/relation").asText());
        assertEquals("koral:termGroup", res.at("/query/attr/operands/0/@type")
                .asText());
        assertEquals("relation:and", res.at("/query/attr/operands/0/relation")
                .asText());
        assertEquals("koral:termGroup", res.at("/query/attr/operands/1/@type")
                .asText());
        assertEquals("relation:and", res.at("/query/attr/operands/1/relation")
                .asText());
        assertEquals("N", res.at("/query/attr/operands/0/operands/0/key")
                .asText());
        assertEquals("A", res.at("/query/attr/operands/0/operands/1/key")
                .asText());
        assertEquals("V", res.at("/query/attr/operands/0/operands/2/key")
                .asText());
        assertEquals("match:ne",
                res.at("/query/attr/operands/0/operands/2/match").asText());
        assertEquals("Genre", res.at("/query/attr/operands/1/operands/0/layer")
                .asText());
        assertEquals("Sport", res.at("/query/attr/operands/1/operands/0/key")
                .asText());
        assertEquals("Genre", res.at("/query/attr/operands/1/operands/1/layer")
                .asText());
        assertEquals("Politik", res.at("/query/attr/operands/1/operands/1/key")
                .asText());
        assertEquals("match:ne",
                res.at("/query/attr/operands/1/operands/1/match").asText());
    }


    @Test
    public void testOPALL () throws JsonProcessingException, IOException {
        query = "#ALL(gehen /w1:10 voran)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("gehen", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("voran", res.at("/query/operands/1/wrap/key").asText());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("w", res.at("/query/distances/0/key").asText());
        assertEquals(1, res.at("/query/distances/0/boundary/min").asInt());
        assertEquals(10, res.at("/query/distances/0/boundary/max").asInt());

        query = "#ALL(gehen /w1:10 (voran /w1:4 schnell))";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("w", res.at("/query/distances/0/key").asText());
        assertEquals(1, res.at("/query/distances/0/boundary/min").asInt());
        assertEquals(10, res.at("/query/distances/0/boundary/max").asInt());
        assertEquals("gehen", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/1/operation").asText());
        assertEquals("cosmas:distance",
                res.at("/query/operands/1/distances/0/@type").asText());
        assertEquals("w", res.at("/query/operands/1/distances/0/key").asText());
        assertEquals(1, res.at("/query/operands/1/distances/0/boundary/min")
                .asInt());
        assertEquals(4, res.at("/query/operands/1/distances/0/boundary/max")
                .asInt());
        assertEquals("voran", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("schnell", res.at("/query/operands/1/operands/1/wrap/key")
                .asText());


    }


    @Test
    public void testOPNHIT () throws JsonProcessingException, IOException {
        query = "#NHIT(gehen /w1:10 voran)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(129, res.at("/query/classRef/0").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/0/operation")
                .asText());
        assertEquals("classRefOp:inversion",
                res.at("/query/operands/0/classRefOp").asText());
        assertEquals(130, res.at("/query/operands/0/classIn/0").asInt());
        assertEquals(131, res.at("/query/operands/0/classIn/1").asInt());
        assertEquals(129, res.at("/query/operands/0/classOut").asInt());
        assertEquals("koral:group", res
                .at("/query/operands/0/operands/0/@type").asText());
        assertEquals("operation:sequence",
                res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals("cosmas:distance",
                res.at("/query/operands/0/operands/0/distances/0/@type")
                        .asText());
        assertEquals("w", res
                .at("/query/operands/0/operands/0/distances/0/key").asText());
        assertEquals(1,
                res.at("/query/operands/0/operands/0/distances/0/boundary/min")
                        .asInt());
        assertEquals(10,
                res.at("/query/operands/0/operands/0/distances/0/boundary/max")
                        .asInt());
        assertEquals("koral:group",
                res.at("/query/operands/0/operands/0/operands/0/@type")
                        .asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/0/operands/0/operation")
                        .asText());
        assertEquals(130,
                res.at("/query/operands/0/operands/0/operands/0/classOut")
                        .asInt());
        assertEquals(131,
                res.at("/query/operands/0/operands/0/operands/1/classOut")
                        .asInt());
        assertEquals(
                "koral:token",
                res.at("/query/operands/0/operands/0/operands/0/operands/0/@type")
                        .asText());
        assertEquals(
                "gehen",
                res.at("/query/operands/0/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(
                "voran",
                res.at("/query/operands/0/operands/0/operands/1/operands/0/wrap/key")
                        .asText());

        query = "#NHIT(gehen /w1:10 voran /w1:10 Beispiel)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(129, res.at("/query/classRef/0").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/0/operation")
                .asText());
        assertEquals("classRefOp:inversion",
                res.at("/query/operands/0/classRefOp").asText());
        assertEquals(130, res.at("/query/operands/0/classIn/0").asInt());
        assertEquals(131, res.at("/query/operands/0/classIn/1").asInt());
        assertEquals(129, res.at("/query/operands/0/classOut").asInt());
        assertEquals("koral:group", res
                .at("/query/operands/0/operands/0/@type").asText());
        assertEquals("operation:sequence",
                res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals("cosmas:distance",
                res.at("/query/operands/0/operands/0/distances/0/@type")
                        .asText());
        assertEquals("w", res
                .at("/query/operands/0/operands/0/distances/0/key").asText());
        assertEquals(1,
                res.at("/query/operands/0/operands/0/distances/0/boundary/min")
                        .asInt());
        assertEquals(10,
                res.at("/query/operands/0/operands/0/distances/0/boundary/max")
                        .asInt());
        assertEquals("koral:group",
                res.at("/query/operands/0/operands/0/operands/0/@type")
                        .asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/0/operands/0/operation")
                        .asText());
        assertEquals(130,
                res.at("/query/operands/0/operands/0/operands/0/classOut")
                        .asInt());
        assertEquals(
                "gehen",
                res.at("/query/operands/0/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(131,
                res.at("/query/operands/0/operands/0/operands/1/classOut")
                        .asInt());
        //        assertEquals("classRefOp:merge",    res.at("/query/operands/0/operands/0/operands/1/classRefOp").asText());
        assertEquals(
                "operation:sequence",
                res.at("/query/operands/0/operands/0/operands/1/operands/0/operation")
                        .asText());
        assertEquals(
                132,
                res.at("/query/operands/0/operands/0/operands/1/operands/0/operands/0/classOut")
                        .asInt());
        assertEquals(
                "voran",
                res.at("/query/operands/0/operands/0/operands/1/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(
                132,
                res.at("/query/operands/0/operands/0/operands/1/operands/0/operands/1/classOut")
                        .asInt());
        assertEquals(
                "Beispiel",
                res.at("/query/operands/0/operands/0/operands/1/operands/0/operands/1/operands/0/wrap/key")
                        .asText());

    }


    @Test
    public void testOPBED () throws JsonProcessingException, IOException {
        query = "#BED(der , sa)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(129, res.at("/query/classRef/0").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:position", res
                .at("/query/operands/0/operation").asText());
        assertEquals("frames:startsWith", res.at("/query/operands/0/frames/0")
                .asText());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals(129, res.at("/query/operands/0/operands/1/classOut")
                .asInt());
        assertEquals("koral:token",
                res.at("/query/operands/0/operands/1/operands/0/@type")
                        .asText());
        assertEquals("der",
                res.at("/query/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals("koral:span", res.at("/query/operands/0/operands/0/@type")
                .asText());
        assertEquals("s", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());

        query = "#COND(der , sa)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(129, res.at("/query/classRef/0").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:position", res
                .at("/query/operands/0/operation").asText());
        assertEquals("frames:startsWith", res.at("/query/operands/0/frames/0")
                .asText());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals(129, res.at("/query/operands/0/operands/1/classOut")
                .asInt());
        assertEquals("koral:token",
                res.at("/query/operands/0/operands/1/operands/0/@type")
                        .asText());
        assertEquals("der",
                res.at("/query/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals("koral:span", res.at("/query/operands/0/operands/0/@type")
                .asText());
        assertEquals("s", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());

        
        query = "#BED(der Mann , +pe)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(129, res.at("/query/classRef/0").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:position", res
                .at("/query/operands/0/operation").asText());
        assertEquals("frames:matches", res.at("/query/operands/0/frames/0")
                .asText());
        assertEquals("koral:reference",
                res.at("/query/operands/0/operands/0/@type").asText());
        assertEquals("operation:focus",
                res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals(-1, res.at("/query/operands/0/operands/0/spanRef/0")
                .asInt());
        assertEquals(1, res.at("/query/operands/0/operands/0/spanRef/1")
                .asInt());
        assertEquals("koral:span",
                res.at("/query/operands/0/operands/0/operands/0/@type")
                        .asText());
        assertEquals("p",
                res.at("/query/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals("koral:reference",
                res.at("/query/operands/0/operands/1/@type").asText());
        assertEquals("operation:focus",
                res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals(0, res.at("/query/operands/0/operands/1/spanRef/0")
                .asInt());
        assertEquals(1, res.at("/query/operands/0/operands/1/spanRef/1")
                .asInt());
        assertEquals("koral:group",
                res.at("/query/operands/0/operands/1/operands/0/@type")
                        .asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/1/operands/0/operation")
                        .asText());
        assertEquals(129,
                res.at("/query/operands/0/operands/1/operands/0/classOut")
                        .asInt());
        assertEquals(
                "operation:sequence",
                res.at("/query/operands/0/operands/1/operands/0/operands/0/operation")
                        .asText());
        assertEquals(
                "der",
                res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(
                "Mann",
                res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/1/wrap/key")
                        .asText());

        query = "#BED(der Mann , sa,-pa)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(129, res.at("/query/classRef/0").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:position", res
                .at("/query/operands/0/operation").asText());
        assertEquals("frames:matches", res.at("/query/operands/0/frames/0")
                .asText());

        assertEquals("koral:group", res
                .at("/query/operands/0/operands/0/@type").asText());
        assertEquals("operation:position",
                res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals("frames:startsWith",
                res.at("/query/operands/0/operands/0/frames/0").asText());
        assertEquals("koral:span",
                res.at("/query/operands/0/operands/0/operands/0/@type")
                        .asText());
        assertEquals("s",
                res.at("/query/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals("koral:group",
                res.at("/query/operands/0/operands/0/operands/1/@type")
                        .asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/0/operands/1/operation")
                        .asText());
        assertEquals(129,
                res.at("/query/operands/0/operands/0/operands/1/classOut")
                        .asInt());
        assertEquals(
                "operation:sequence",
                res.at("/query/operands/0/operands/0/operands/1/operands/0/operation")
                        .asText());
        assertEquals(
                "der",
                res.at("/query/operands/0/operands/0/operands/1/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(
                "Mann",
                res.at("/query/operands/0/operands/0/operands/1/operands/0/operands/1/wrap/key")
                        .asText());

        assertEquals("koral:group", res
                .at("/query/operands/0/operands/1/@type").asText());
        assertEquals("operation:position",
                res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals("frames:startsWith",
                res.at("/query/operands/0/operands/1/frames/0").asText());
        assertTrue(res.at("/query/operands/0/operands/1/exclude")
                .asBoolean());
        assertEquals("koral:span",
                res.at("/query/operands/0/operands/1/operands/0/@type")
                        .asText());
        assertEquals("p",
                res.at("/query/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals("koral:group",
                res.at("/query/operands/0/operands/1/operands/1/@type")
                        .asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/1/operands/1/operation")
                        .asText());
        assertEquals(130,
                res.at("/query/operands/0/operands/1/operands/1/classOut")
                        .asInt());
        assertEquals(
                "operation:sequence",
                res.at("/query/operands/0/operands/1/operands/1/operands/0/operation")
                        .asText());
        assertEquals(
                "der",
                res.at("/query/operands/0/operands/1/operands/1/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(
                "Mann",
                res.at("/query/operands/0/operands/1/operands/1/operands/0/operands/1/wrap/key")
                        .asText());

    }


    @Test
    public void testColonSeparatedConditions () throws JsonProcessingException,
            IOException {
        query = "der:sa";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(129, res.at("/query/classRef/0").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:position", res
                .at("/query/operands/0/operation").asText());
        assertEquals("frames:startsWith", res.at("/query/operands/0/frames/0")
                .asText());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals(129, res.at("/query/operands/0/operands/1/classOut")
                .asInt());
        assertEquals("koral:token",
                res.at("/query/operands/0/operands/1/operands/0/@type")
                        .asText());
        assertEquals("der",
                res.at("/query/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals("koral:span", res.at("/query/operands/0/operands/0/@type")
                .asText());
        assertEquals("s", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());

        query = "der:sa,-pa";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(129, res.at("/query/classRef/0").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:position", res
                .at("/query/operands/0/operation").asText());
        assertEquals("frames:matches", res.at("/query/operands/0/frames/0")
                .asText());
        assertEquals("koral:group", res
                .at("/query/operands/0/operands/0/@type").asText());
        assertEquals("operation:position",
                res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals("frames:startsWith",
                res.at("/query/operands/0/operands/0/frames/0").asText());
        assertEquals("koral:span",
                res.at("/query/operands/0/operands/0/operands/0/@type")
                        .asText());
        assertEquals("s",
                res.at("/query/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals("koral:group",
                res.at("/query/operands/0/operands/0/operands/1/@type")
                        .asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/0/operands/1/operation")
                        .asText());
        assertEquals(129,
                res.at("/query/operands/0/operands/0/operands/1/classOut")
                        .asInt());
        assertEquals(
                "der",
                res.at("/query/operands/0/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals("koral:group", res
                .at("/query/operands/0/operands/1/@type").asText());
        assertEquals("operation:position",
                res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals("frames:startsWith",
                res.at("/query/operands/0/operands/1/frames/0").asText());
        assertTrue(res.at("/query/operands/0/operands/1/exclude")
                .asBoolean());
        assertEquals("koral:span",
                res.at("/query/operands/0/operands/1/operands/0/@type")
                        .asText());
        assertEquals("p",
                res.at("/query/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals("koral:group",
                res.at("/query/operands/0/operands/1/operands/1/@type")
                        .asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/1/operands/1/operation")
                        .asText());
        assertEquals(130,
                res.at("/query/operands/0/operands/1/operands/1/classOut")
                        .asInt());
        assertEquals(
                "der",
                res.at("/query/operands/0/operands/1/operands/1/operands/0/wrap/key")
                        .asText());

        query = "der:sa,-pa,+te";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(129, res.at("/query/classRef/0").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:position", res
                .at("/query/operands/0/operation").asText());
        assertEquals("frames:matches", res.at("/query/operands/0/frames/0")
                .asText());
        assertEquals("koral:group", res
                .at("/query/operands/0/operands/0/@type").asText());
        assertEquals("operation:position",
                res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals("frames:startsWith",
                res.at("/query/operands/0/operands/0/frames/0").asText());
        assertEquals("koral:span",
                res.at("/query/operands/0/operands/0/operands/0/@type")
                        .asText());
        assertEquals("s",
                res.at("/query/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals("koral:group",
                res.at("/query/operands/0/operands/0/operands/1/@type")
                        .asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/0/operands/1/operation")
                        .asText());
        assertEquals(129,
                res.at("/query/operands/0/operands/0/operands/1/classOut")
                        .asInt());
        assertEquals(
                "der",
                res.at("/query/operands/0/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals("koral:reference",
                res.at("/query/operands/0/operands/1/@type").asText());
        assertEquals("operation:focus",
                res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals(130, res.at("/query/operands/0/operands/1/classRef/0")
                .asInt());
        assertEquals("koral:group",
                res.at("/query/operands/0/operands/1/operands/0/@type")
                        .asText());
        assertEquals("operation:position",
                res.at("/query/operands/0/operands/1/operands/0/operation")
                        .asText());
        assertEquals("frames:matches",
                res.at("/query/operands/0/operands/1/operands/0/frames/0")
                        .asText());
        assertEquals(
                "koral:group",
                res.at("/query/operands/0/operands/1/operands/0/operands/0/@type")
                        .asText());
        assertEquals(
                "operation:position",
                res.at("/query/operands/0/operands/1/operands/0/operands/0/operation")
                        .asText());
        assertEquals(
                "frames:startsWith",
                res.at("/query/operands/0/operands/1/operands/0/operands/0/frames/0")
                        .asText());
        assertTrue(res.at("/query/operands/0/operands/1/operands/0/operands/0/exclude")
                .asBoolean());
        assertEquals(
                "koral:span",
                res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/0/@type")
                        .asText());
        assertEquals(
                "p",
                res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(
                "koral:group",
                res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/1/@type")
                        .asText());
        assertEquals(
                "operation:class",
                res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/1/operation")
                        .asText());
        assertEquals(
                130,
                res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/1/classOut")
                        .asInt());
        assertEquals(
                "der",
                res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals(
                "koral:group",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/@type")
                        .asText());
        assertEquals(
                "operation:position",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operation")
                        .asText());
        assertEquals(
                "frames:matches",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/frames/0")
                        .asText());
        assertEquals(
                "koral:reference",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/0/@type")
                        .asText());
        assertEquals(
                "operation:focus",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/0/operation")
                        .asText());
        assertEquals(
                -1,
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/0/spanRef/0")
                        .asInt());
        assertEquals(
                1,
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/0/spanRef/1")
                        .asInt());
        assertEquals(
                "koral:span",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/0/operands/0/@type")
                        .asText());
        assertEquals(
                "t",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(
                "koral:reference",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/1/@type")
                        .asText());
        assertEquals(
                "operation:focus",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/1/operation")
                        .asText());
        assertEquals(
                0,
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/1/spanRef/0")
                        .asInt());
        assertEquals(
                1,
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/1/spanRef/1")
                        .asInt());
        assertEquals(
                "koral:group",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/1/operands/0/@type")
                        .asText());
        assertEquals(
                "operation:class",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/1/operands/0/operation")
                        .asText());
        assertEquals(
                131,
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/1/operands/0/classOut")
                        .asInt());
        assertEquals(
                "der",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/1/operands/0/operands/0/wrap/key")
                        .asText());
    }

    @Test
    public void testWildcard () throws JsonProcessingException, IOException {
        query = "meine* /+w1:2,s0 &Erfahrung";
        qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
        assertEquals("type:regex",
					 res.at("/query/operands/0/operands/0/wrap/type").asText());
        assertEquals("meine.*",
					 res.at("/query/operands/0/operands/0/wrap/key").asText());
	};	

    @Test
    public void testErrors () throws JsonProcessingException, IOException {
        query = "MORPH(tt/p=\"\")";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertTrue(res.at("/query/@type").isMissingNode());
        assertEquals(StatusCodes.INCOMPATIBLE_OPERATOR_AND_OPERAND,
                res.at("/errors/0/0").asInt());
        assertTrue(res
                .at("/errors/0/1")
                .asText()
                .startsWith(
                        "Something went wrong parsing the argument in MORPH()"));

        query = "MORPH(tt/p=\"foo)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertTrue(res.at("/query/@type").isMissingNode());
        assertEquals(StatusCodes.MALFORMED_QUERY, res.at("/errors/0/0").asInt());
        assertTrue(res.at("/errors/0/1").asText()
                .startsWith("Early closing parenthesis"));

        query = "MORPH(tt/p=)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertTrue(res.at("/query/@type").isMissingNode());
        assertEquals(StatusCodes.MALFORMED_QUERY, res.at("/errors/0/0").asInt());
        assertTrue(res.at("/errors/0/1").asText()
                .startsWith("Early closing parenthesis"));
    }

    @Test
    public void testMultipleParenthesis () throws JsonProcessingException, IOException {
        query = "(Pop-up OR Pop-ups) %s0 (Internet OR  Programm)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("Pop-up", res.at("/query/operands/0/operands/0/operands/0/wrap/key").asText());
        assertEquals("Pop-ups", res.at("/query/operands/0/operands/0/operands/1/wrap/key").asText());
        assertEquals("operation:disjunction", res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals(129, res.at("/query/operands/0/classOut").asInt());
        assertEquals("operation:disjunction", res.at("/query/operands/1/operands/0/operation").asText());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type").asText());
        assertTrue(res.at("/query/distances/0/exclude").asBoolean());
        assertEquals("s", res.at("/query/distances/0/key").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
    }
}
