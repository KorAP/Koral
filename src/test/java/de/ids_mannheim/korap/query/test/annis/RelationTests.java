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
 * @author margaretha
 *
 */
public class RelationTests {
    private String query;

    private QuerySerializer qs = new QuerySerializer();
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode res;


    @Test
    public void testTypedRelationWithArbritraryNodes ()
            throws JsonProcessingException, IOException {
        query = "node & node & #1 ->malt/d[func=\"PP\"] #2";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());

        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:relation", res.at("/query/operation").asText());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:span", res.at("/query/operands/1/@type").asText());

        assertEquals("koral:relation", res.at("/query/relType/@type").asText());
        assertEquals("koral:term",
                res.at("/query/relType/wrap/@type").asText());
        assertEquals("malt", res.at("/query/relType/wrap/foundry").asText());
        assertEquals("d", res.at("/query/relType/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/relType/wrap/match").asText());
        assertEquals("PP", res.at("/query/relType/wrap/key").asText());
        assertTrue(res.at("/query/relType/wrap/value").isMissingNode());
    }

    @Test
    public void testTypedRelationWithLayerInLabel ()
            throws JsonProcessingException, IOException {
        query = "node & node & #1 ->malt/d[c:func!=\"PP\"] #2";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());

        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:relation", res.at("/query/operation").asText());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:span", res.at("/query/operands/1/@type").asText());

        assertEquals("koral:relation", res.at("/query/relType/@type").asText());
        assertEquals("koral:term",
                res.at("/query/relType/wrap/@type").asText());
        assertEquals("malt", res.at("/query/relType/wrap/foundry").asText());
        assertEquals("d", res.at("/query/relType/wrap/layer").asText());
        assertEquals("match:ne", res.at("/query/relType/wrap/match").asText());
        assertEquals("PP", res.at("/query/relType/wrap/key").asText());
        assertTrue(res.at("/query/relType/wrap/value").isMissingNode());
    }
    
    @Test
    public void testTypedRelationWithoutLabel ()
            throws JsonProcessingException, IOException {
        query = "node & node & #1 ->malt/d #2";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());

        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:relation", res.at("/query/operation").asText());

        assertEquals("koral:relation", res.at("/query/relType/@type").asText());
        assertEquals("koral:term",
                res.at("/query/relType/wrap/@type").asText());
        assertEquals("malt", res.at("/query/relType/wrap/foundry").asText());
        assertEquals("d", res.at("/query/relType/wrap/layer").asText());
        assertTrue(res.at("/query/relType/wrap/key").isMissingNode());
        assertTrue(res.at("/query/relType/wrap/value").isMissingNode());
    }
    
    //EM: needs rewrite default foundry in Kustvakt
    @Test
    public void testTypedRelationWithoutFoundry ()
            throws JsonProcessingException, IOException {
        query = "node & node & #1 ->d #2";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());

        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:relation", res.at("/query/operation").asText());

        assertEquals("koral:relation", res.at("/query/relType/@type").asText());
        assertEquals("koral:term",
                res.at("/query/relType/wrap/@type").asText());
        assertEquals("d", res.at("/query/relType/wrap/layer").asText());
        assertTrue(res.at("/query/relType/wrap/foundry").isMissingNode());
        assertTrue(res.at("/query/relType/wrap/key").isMissingNode());
        assertTrue(res.at("/query/relType/wrap/value").isMissingNode());
    }

    @Test
    public void testTypedRelationWithTokenNodes ()
            throws JsonProcessingException, IOException {
        query = "tt/p=\"KOUI\" ->malt/d[func=\"KONJ\"] tt/p=\"NN\"";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());

        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:relation", res.at("/query/operation").asText());
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("KOUI", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("p", res.at("/query/operands/0/wrap/layer").asText());
        assertEquals("koral:token", res.at("/query/operands/1/@type").asText());
        assertEquals("NN", res.at("/query/operands/1/wrap/key").asText());
        assertEquals("p", res.at("/query/operands/1/wrap/layer").asText());

        assertEquals("koral:relation", res.at("/query/relType/@type").asText());
        assertEquals("koral:term",
                res.at("/query/relType/wrap/@type").asText());
        assertEquals("malt", res.at("/query/relType/wrap/foundry").asText());
        assertEquals("d", res.at("/query/relType/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/relType/wrap/match").asText());
        assertEquals("KONJ", res.at("/query/relType/wrap/key").asText());
        assertTrue(res.at("/query/relType/wrap/value").isMissingNode());
    }

    @Test
    public void testTypedRelationWithTokenNodesWithReferences ()
            throws JsonProcessingException, IOException {
        query = "tt/p=\"KOUI\" & tt/p=\"NN\" & #1 ->malt/d[func=\"KONJ\"] #2";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());

        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:relation", res.at("/query/operation").asText());
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("KOUI", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("p", res.at("/query/operands/0/wrap/layer").asText());
        assertEquals("koral:token", res.at("/query/operands/1/@type").asText());
        assertEquals("NN", res.at("/query/operands/1/wrap/key").asText());
        assertEquals("p", res.at("/query/operands/1/wrap/layer").asText());

        assertEquals("koral:relation", res.at("/query/relType/@type").asText());
        assertEquals("koral:term",
                res.at("/query/relType/wrap/@type").asText());
        assertEquals("malt", res.at("/query/relType/wrap/foundry").asText());
        assertEquals("d", res.at("/query/relType/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/relType/wrap/match").asText());
        assertEquals("KONJ", res.at("/query/relType/wrap/key").asText());
        assertTrue(res.at("/query/relType/wrap/value").isMissingNode());
    }
    
    @Test
    public void testTypedRelationWithAnnotationNodes ()
            throws JsonProcessingException, IOException {
        query = "corenlp/c=\"VP\" & corenlp/c=\"NP\" & #1 ->malt/d[func=\"PP\"] #2";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:relation", res.at("/query/operation").asText());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("VP", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("c", res.at("/query/operands/0/wrap/layer").asText());
        assertEquals("koral:span", res.at("/query/operands/1/@type").asText());
        assertEquals("NP", res.at("/query/operands/1/wrap/key").asText());
        assertEquals("c", res.at("/query/operands/1/wrap/layer").asText());

        assertEquals("koral:relation", res.at("/query/relType/@type").asText());
        assertEquals("koral:term",
                res.at("/query/relType/wrap/@type").asText());
        assertEquals("malt", res.at("/query/relType/wrap/foundry").asText());
        assertEquals("d", res.at("/query/relType/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/relType/wrap/match").asText());
        assertEquals("PP", res.at("/query/relType/wrap/key").asText());
        assertTrue(res.at("/query/relType/wrap/value").isMissingNode());
    }

    @Test
    public void testIndirectTypedRelation ()
            throws JsonProcessingException, IOException {
        query = "corenlp/c=\"VP\" & corenlp/c=\"NP\" & #1 ->malt/d[func=\"PP\"] * #2";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:relation", res.at("/query/relType/@type").asText());
        assertEquals("koral:term",
                res.at("/query/relType/wrap/@type").asText());
        assertEquals("malt", res.at("/query/relType/wrap/foundry").asText());
        assertEquals("d", res.at("/query/relType/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/relType/wrap/match").asText());
        assertEquals("PP", res.at("/query/relType/wrap/key").asText());
        assertTrue(res.at("/query/relType/wrap/value").isMissingNode());
        
        assertEquals("koral:boundary", res.at("/query/relType/boundary/@type").asText());
        assertEquals(0, res.at("/query/relType/boundary/min").asInt());
        assertTrue(res.at("/query/relType/boundary/max").isMissingNode());
    }
    
    @Test
    public void testSpecificIndirectTypedRelation ()
            throws JsonProcessingException, IOException {
        query = "corenlp/c=\"VP\" & corenlp/c=\"NP\" & #1 ->malt/d[func=\"PP\"] 2,4 #2";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:boundary", res.at("/query/relType/boundary/@type").asText());
        assertEquals(2, res.at("/query/relType/boundary/min").asInt());
        assertEquals(4, res.at("/query/relType/boundary/max").asInt());
    }

    

    @Test
    public void testTypedRelationWithMultipleLabels ()
            throws JsonProcessingException, IOException {
        query = "corenlp/c=\"VP\" & corenlp/c=\"NP\" & "
                + "#1 ->malt/d[func=\"SBJ\" func!=\"ADV\"] #2";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
        assertEquals(302, res.at("/errors/0/0").asInt());
    }
}
