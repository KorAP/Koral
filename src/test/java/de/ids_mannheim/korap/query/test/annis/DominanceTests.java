package de.ids_mannheim.korap.query.test.annis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ids_mannheim.korap.query.serialize.QuerySerializer;

/**
 * @author bingl, margaretha
 *
 */
public class DominanceTests {
    private String query;
    private QuerySerializer qs = new QuerySerializer();
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode res;


    @Test
    public void testDominanceWithArbitrarySpans ()
            throws JsonProcessingException, IOException {
        query = "node > node";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:hierarchy",
                res.at("/query/operation").asText());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:span", res.at("/query/operands/1/@type").asText());
    }


    @Test
    public void testDominanceWithAnnotationOnFirstOperand ()
            throws JsonProcessingException, IOException {
        query = "cnx/c=\"np\" > node";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:hierarchy",
                res.at("/query/operation").asText());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("np", res.at("/query/operands/0/key").asText());
        assertEquals("c", res.at("/query/operands/0/layer").asText());
        assertEquals("cnx", res.at("/query/operands/0/foundry").asText());
        assertEquals("koral:span", res.at("/query/operands/1/@type").asText());
    }


    @Test
    public void testDominanceWithAnnotationOnSecondOperand ()
            throws JsonProcessingException, IOException {
        query = "node > cnx/c=\"np\"";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:hierarchy",
                res.at("/query/operation").asText());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:span", res.at("/query/operands/1/@type").asText());
        assertEquals("np", res.at("/query/operands/1/key").asText());
        assertEquals("c", res.at("/query/operands/1/layer").asText());
        assertEquals("cnx", res.at("/query/operands/1/foundry").asText());
    }


    @Test
    public void testDominanceWithAnnotatedReferences ()
            throws JsonProcessingException, IOException {
        // cat=PP is a shorcut for const:cat="PP"
        query = "cat=\"NP\" & cat=\"PP\" > #1";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:hierarchy",
                res.at("/query/operation").asText());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("PP", res.at("/query/operands/0/key").asText());
        assertEquals("c", res.at("/query/operands/0/layer").asText());
        assertEquals("koral:span", res.at("/query/operands/1/@type").asText());
        assertEquals("NP", res.at("/query/operands/1/key").asText());
        assertEquals("c", res.at("/query/operands/1/layer").asText());
    }


    @Test
    public void testDominanceWithAbritraryReferences ()
            throws JsonProcessingException, IOException {
        query = "node & node & #2 > #1";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:hierarchy",
                res.at("/query/operation").asText());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:span", res.at("/query/operands/1/@type").asText());
    }


    @Test
    public void testDominanceWithToken ()
            throws JsonProcessingException, IOException {
        query = "\"Mann\" & node & #2 > #1";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:hierarchy",
                res.at("/query/operation").asText());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:token", res.at("/query/operands/1/@type").asText());
        assertEquals("Mann", res.at("/query/operands/1/wrap/key").asText());
    }


    @Test
    public void testDominanceFollowedByAnnotations ()
            throws JsonProcessingException, IOException {
        query = "#1 > #2 & cnx/cat=\"vp\" & cnx/cat=\"np\"";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:hierarchy",
                res.at("/query/operation").asText());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("vp", res.at("/query/operands/0/key").asText());
        assertEquals("c", res.at("/query/operands/0/layer").asText());
        assertEquals("cnx", res.at("/query/operands/0/foundry").asText());
        assertEquals("koral:span", res.at("/query/operands/1/@type").asText());
        assertEquals("np", res.at("/query/operands/1/key").asText());
        assertEquals("c", res.at("/query/operands/1/layer").asText());
        assertEquals("cnx", res.at("/query/operands/1/foundry").asText());
    }


    @Test
    public void testIndirectDominance ()
            throws JsonProcessingException, IOException {
        query = "node & node & #1 >2,4 #2";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());

        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:hierarchy",
                res.at("/query/operation").asText());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:span", res.at("/query/operands/1/@type").asText());
        assertEquals(2, res.at("/query/boundary/min").asInt());
        assertEquals(4, res.at("/query/boundary/max").asInt());
        assertTrue(res.at("/query/label").isMissingNode());

        query = "node & node & #1 >* #2";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
        assertEquals(0, res.at("/query/boundary/min").asInt());
        assertTrue(res.at("/query/boundary/max").isMissingNode());
    }


    @Test
    public void testDominanceWithLabel ()
            throws JsonProcessingException, IOException {
        query = "\"Mann\" & node & #2 >[func=\"SBJ\"] #1";
        //coordinates the func=SB term and requires a "c"-layer term (consituency relation/dominance)
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
        //        System.out.println(res.asText());
        assertEquals("operation:hierarchy",
                res.at("/query/operation").asText());
        assertEquals("koral:relation", res.at("/query/relType/@type").asText());
        assertEquals("koral:term",
                res.at("/query/relType/wrap/@type").asText());
        assertEquals("func", res.at("/query/relType/wrap/key").asText());
        assertEquals("SBJ", res.at("/query/relType/wrap/value").asText());
    }


    @Test
    public void testDominanceWithLayerAndLabel ()
            throws JsonProcessingException, IOException {
        query = "\"Mann\" & node & #2 >[c:func=\"SBJ\"] #1";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
        assertEquals("operation:hierarchy",
                res.at("/query/operation").asText());
        assertEquals("koral:relation", res.at("/query/relType/@type").asText());
        assertEquals("koral:term",
                res.at("/query/relType/wrap/@type").asText());
        assertEquals("c", res.at("/query/relType/wrap/layer").asText());
        assertEquals("func", res.at("/query/relType/wrap/key").asText());
        assertEquals("SBJ", res.at("/query/relType/wrap/value").asText());
    }


    @Test
    public void testDominanceWithMultipleLabels ()
            throws JsonProcessingException, IOException {
        query = "corenlp/c=\"VP\" & corenlp/c=\"NP\" & #1 >[corenlp/c:func=\"PP\" corenlp/c:func=\"PN\"] #2";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
        System.out.println(res.asText());
        assertEquals("operation:hierarchy",
                res.at("/query/operation").asText());
        assertEquals("koral:relation", res.at("/query/relType/@type").asText());
        assertEquals("koral:termGroup",
                res.at("/query/relType/wrap/@type").asText());
        assertEquals("relation:and",
                res.at("/query/relType/wrap/relation").asText());
        assertEquals("corenlp", res.at("/query/relType/wrap/operands/0/foundry").asText());
        assertEquals("c", res.at("/query/relType/wrap/operands/0/layer").asText());
        assertEquals("func", res.at("/query/relType/wrap/operands/0/key").asText());
        assertEquals("PP", res.at("/query/relType/wrap/operands/0/value").asText());
        
        assertEquals("corenlp", res.at("/query/relType/wrap/operands/1/foundry").asText());
        assertEquals("c", res.at("/query/relType/wrap/operands/1/layer").asText());
        assertEquals("func", res.at("/query/relType/wrap/operands/1/key").asText());
        assertEquals("PN", res.at("/query/relType/wrap/operands/1/value").asText());
    }


    @Test
    public void testMultipleDominance ()
            throws JsonProcessingException, IOException {
        query = "cat=\"CP\" & cat=\"VP\" & cat=\"NP\" & #1 > #2 > #3";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
        //        System.out.println(res.asText());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:hierarchy",
                res.at("/query/operation").asText());
        assertEquals("koral:reference",
                res.at("/query/operands/0/@type").asText());
        assertEquals("operation:focus",
                res.at("/query/operands/0/operation").asText());
        assertEquals(129, res.at("/query/operands/0/classRef/0").asInt());

        assertEquals("koral:group",
                res.at("/query/operands/0/operands/0/@type").asText());
        assertEquals("operation:hierarchy",
                res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals("koral:span", res
                .at("/query/operands/0/operands/0/operands/0/@type").asText());
        assertEquals("c", res
                .at("/query/operands/0/operands/0/operands/0/layer").asText());
        assertEquals("CP",
                res.at("/query/operands/0/operands/0/operands/0/key").asText());
        assertEquals("koral:group", res
                .at("/query/operands/0/operands/0/operands/1/@type").asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/0/operands/1/operation")
                        .asText());
        assertEquals(129,
                res.at("/query/operands/0/operands/0/operands/1/classOut")
                        .asInt());
        assertEquals("VP",
                res.at("/query/operands/0/operands/0/operands/1/operands/0/key")
                        .asText());
    }


    @Test
    public void testMultipleDominance2 ()
            throws JsonProcessingException, IOException {
        query = "cat=\"CP\" & cat=\"VP\" & cat=\"NP\" & cat=\"DP\" & #1 > #2 > #3 > #4";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
        System.out.println(res.asText());

        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:hierarchy",
                res.at("/query/operation").asText());
        assertEquals("koral:reference",
                res.at("/query/operands/0/@type").asText());
        assertEquals("operation:focus",
                res.at("/query/operands/0/operation").asText());
        assertEquals(130, res.at("/query/operands/0/classRef/0").asInt());

        assertEquals("c", res.at("/query/operands/1/layer").asText());
        assertEquals("DP", res.at("/query/operands/1/key").asText());

        assertEquals("koral:group",
                res.at("/query/operands/0/operands/0/@type").asText());
        assertEquals("operation:hierarchy",
                res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals("koral:reference", res
                .at("/query/operands/0/operands/0/operands/0/@type").asText());
        assertEquals(129,
                res.at("/query/operands/0/operands/0/operands/0/classRef/0")
                        .asInt());

        assertEquals("operation:class",
                res.at("/query/operands/0/operands/0/operands/1/operation")
                        .asText());
        assertEquals(130,
                res.at("/query/operands/0/operands/0/operands/1/classOut")
                        .asInt());
        assertEquals("NP",
                res.at("/query/operands/0/operands/0/operands/1/operands/0/key")
                        .asText());

        assertEquals("c",
                res.at("/query/operands/0/operands/0/operands/0/operands/0/operands/0/layer")
                        .asText());
        assertEquals("CP",
                res.at("/query/operands/0/operands/0/operands/0/operands/0/operands/0/key")
                        .asText());
        assertEquals("koral:group",
                res.at("/query/operands/0/operands/0/operands/0/operands/0/operands/1/@type")
                        .asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/0/operands/0/operands/0/operands/1/operation")
                        .asText());
        assertEquals(129,
                res.at("/query/operands/0/operands/0/operands/0/operands/0/operands/1/classOut")
                        .asInt());
        assertEquals("VP",
                res.at("/query/operands/0/operands/0/operands/0/operands/0/operands/1/operands/0/key")
                        .asText());

    }
}
