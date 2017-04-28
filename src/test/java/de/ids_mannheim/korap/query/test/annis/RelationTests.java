package de.ids_mannheim.korap.query.test.annis;

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
        System.out.println(res.asText());
    }
    
    @Test
    public void testTypedRelation ()
            throws JsonProcessingException, IOException {
        query = "corenlp/c=\"VP\" & corenlp/c=\"NP\" & #1 ->malt/d=\"PP\" #2";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
    }


    @Test
    public void testTypedRelationWithLabel ()
            throws JsonProcessingException, IOException {
        query = "corenlp/c=\"VP\" & corenlp/c=\"NP\" & #1 ->malt/d=\"PP\"[func=\"SB\"] #2";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
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
