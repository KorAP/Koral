package de.ids_mannheim.korap.query.test.annis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ids_mannheim.korap.query.serialize.QuerySerializer;

public class RelationTests {
    private String query;

    private QuerySerializer qs = new QuerySerializer();
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode res;


    @Test
    public void testTypedRelationWithArbritraryNodes ()
            throws JsonProcessingException, IOException {
        query = "node & node & #1 ->malt/d=\"PP\" #2";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
        //        System.out.println(res.asText());

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
    public void testFoundryLayerTypedRelationWithArbritraryNodes ()
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


    @Test
    public void testTypedRelation ()
            throws JsonProcessingException, IOException {
        query = "corenlp/c=\"VP\" & corenlp/c=\"NP\" & #1 ->malt/d=\"PP\" #2";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());

        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:relation", res.at("/query/operation").asText());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("VP", res.at("/query/operands/0/key").asText());
        assertEquals("c", res.at("/query/operands/0/layer").asText());
        assertEquals("koral:span", res.at("/query/operands/1/@type").asText());
        assertEquals("NP", res.at("/query/operands/1/key").asText());
        assertEquals("c", res.at("/query/operands/1/layer").asText());

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
        query = "corenlp/c=\"VP\" & corenlp/c=\"NP\" & #1 ->malt/d=\"PP\" * #2";
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
        query = "corenlp/c=\"VP\" & corenlp/c=\"NP\" & #1 ->malt/d=\"PP\" 2,4 #2";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:boundary", res.at("/query/relType/boundary/@type").asText());
        assertEquals(2, res.at("/query/relType/boundary/min").asInt());
        assertEquals(4, res.at("/query/relType/boundary/max").asInt());
    }

    @Test
    public void testTypedRelationWithLabel ()
            throws JsonProcessingException, IOException {
        query = "corenlp/c=\"VP\" & corenlp/c=\"NP\" & "
                + "#1 ->malt/d=\"PP\"[func=\"SBJ\"] #2";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:relation", res.at("/query/relType/@type").asText());
        assertEquals("koral:termGroup",
                res.at("/query/relType/wrap/@type").asText());
        assertEquals("relation:and",
                res.at("/query/relType/wrap/relation").asText());
        assertEquals("malt", res.at("/query/relType/wrap/operands/0/foundry").asText());
        assertEquals("d", res.at("/query/relType/wrap/operands/0/layer").asText());
        assertEquals("PP", res.at("/query/relType/wrap/operands/0/key").asText());
        assertTrue(res.at("/query/relType/wrap/operands/0/value").isMissingNode());
        
        assertEquals("match:eq", res.at("/query/relType/wrap/operands/1/match").asText());
        assertTrue(res.at("/query/relType/wrap/operands/1/layer").isMissingNode());
        assertEquals("func", res.at("/query/relType/wrap/operands/1/key").asText());
        assertEquals("SBJ", res.at("/query/relType/wrap/operands/1/value").asText());
    }

    // fix me
    @Test
    public void testTypedRelationWithMultipleLabels ()
            throws JsonProcessingException, IOException {
        query = "corenlp/c=\"VP\" & corenlp/c=\"NP\" & "
                + "#1 ->malt/d=\"PP\"[func=\"SBJ\" func!=\"ADV\"] #2";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
        
//        assertEquals("koral:relation", res.at("/query/relType/@type").asText());
//        assertEquals("koral:termGroup",
//                res.at("/query/relType/wrap/@type").asText());
//        assertEquals("relation:and",
//                res.at("/query/relType/wrap/relation").asText());
//        assertEquals("malt", res.at("/query/relType/wrap/operands/0/foundry").asText());
//        assertEquals("d", res.at("/query/relType/wrap/operands/0/layer").asText());
//        assertEquals("PP", res.at("/query/relType/wrap/operands/0/key").asText());
//        assertTrue(res.at("/query/relType/wrap/operands/0/value").isMissingNode());
//        
//        assertEquals("match:eq", res.at("/query/relType/wrap/operands/1/match").asText());
//        assertTrue(res.at("/query/relType/wrap/operands/1/layer").isMissingNode());
//        assertEquals("func", res.at("/query/relType/wrap/operands/1/key").asText());
//        assertEquals("SBJ", res.at("/query/relType/wrap/operands/1/value").asText());
    }
    
    //  
    //  @Test
    //  public void testPointingRelations() throws Exception {
    //      query = "node & node & #2 ->coref[val=\"true\"] #1";
    //      String dom1 = 
    //              "{@type=koral:group, operation=operation:relation, operands=[" +
    //                      "{@type=koral:span}," +
    //                      "{@type=koral:span}" +
    //              "], relation={@type=koral:relation, wrap={@type=koral:term, layer=coref, key=true, match=match:eq}}" +
    //              "}";
    //      aqlt = new AqlTree(query);
    //      map = aqlt.getRequestMap().get("query").toString();
    //      assertEquals(dom1.replaceAll(" ", ""), map.replaceAll(" ", ""));
    //      
    //      query = "node & node & #2 ->mate/coref[val=\"true\"] #1";
    //      String dom2 = 
    //              "{@type=koral:group, operation=operation:relation, operands=[" +
    //                      "{@type=koral:span}," +
    //                      "{@type=koral:span}" +
    //              "], relation={@type=koral:relation, wrap={@type=koral:term, foundry=mate, layer=coref, key=true, match=match:eq}}" +
    //              "}";
    //      aqlt = new AqlTree(query);
    //      map = aqlt.getRequestMap().get("query").toString();
    //      assertEquals(dom2.replaceAll(" ", ""), map.replaceAll(" ", ""));
    //  }

}
