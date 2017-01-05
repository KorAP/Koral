package de.ids_mannheim.korap.query.test.annis;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ids_mannheim.korap.query.serialize.QuerySerializer;

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
        //        assertEquals("koral:group", res.at("/query/@type").asText());
        //        assertEquals("operation:relation", res.at("/query/operation").asText());
        //        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        //        assertEquals("koral:span", res.at("/query/operands/1/@type").asText());
        //        assertEquals("koral:relation", res.at("/query/relation/@type").asText());
        //        assertEquals("koral:term", res.at("/query/relation/wrap/@type")
        //                .asText());
        //        assertEquals("c", res.at("/query/relation/wrap/layer").asText());
    }


    @Test
    public void testDominanceWithAnnotation1 ()
            throws JsonProcessingException, IOException {
        query = "node > cnx/c=\"np\"";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
        //        assertEquals("koral:group", res.at("/query/@type").asText());
        //        assertEquals("operation:relation", res.at("/query/operation").asText());
        //        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        //        assertEquals("koral:span", res.at("/query/operands/1/@type").asText());
        //        assertEquals("np", res.at("/query/operands/1/key").asText());
        //        assertEquals("c", res.at("/query/operands/1/layer").asText());
        //        assertEquals("cnx", res.at("/query/operands/1/foundry").asText());
        //        assertEquals("koral:relation", res.at("/query/relation/@type").asText());
        //        assertEquals("koral:term", res.at("/query/relation/wrap/@type")
        //                .asText());
        //        assertEquals("c", res.at("/query/relation/wrap/layer").asText());
    }


    @Test
    public void testDominanceWithAnnotation2 ()
            throws JsonProcessingException, IOException {
        query = "cnx/c=\"np\" > node";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
    }
    
    @Test
    public void testDominanceWithReference ()
            throws JsonProcessingException, IOException {
        query = "cat=/NP/ & cat=/PP/ > #1";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
    }

    @Test
    public void testDominanceWithReference2 () throws JsonProcessingException,
            IOException {
        query = "node & node & #2 > #1";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
//        assertEquals("koral:group", res.at("/query/@type").asText());
//        assertEquals("operation:relation", res.at("/query/operation").asText());
//        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
//        assertEquals("koral:span", res.at("/query/operands/1/@type").asText());
//        assertEquals("koral:relation", res.at("/query/relation/@type").asText());
//        assertEquals("koral:term", res.at("/query/relation/wrap/@type")
//                .asText());
//        assertEquals("c", res.at("/query/relation/wrap/layer").asText());
    }
    
    @Test
    public void testDominanceWithToken ()
            throws JsonProcessingException, IOException {
        query = "\"Mann\" & node & #2 > #1";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
//        assertEquals("koral:group", res.at("/query/@type").asText());
//        assertEquals("operation:relation", res.at("/query/operation").asText());
//        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
//        assertEquals("koral:token", res.at("/query/operands/1/@type").asText());
//        assertEquals("Mann", res.at("/query/operands/1/wrap/key").asText());
//        assertEquals("koral:relation", res.at("/query/relation/@type").asText());
//        assertEquals("koral:term", res.at("/query/relation/wrap/@type")
//                .asText());
//        assertEquals("c", res.at("/query/relation/wrap/layer").asText());
    }
       
    
    @Test
    public void testDominanceFollowedByAnnotations () throws JsonProcessingException,
            IOException {
        query = "#1 > #2 & cnx/cat=\"vp\" & cnx/cat=\"np\"";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
//        assertEquals("koral:group", res.at("/query/@type").asText());
//        assertEquals("operation:relation", res.at("/query/operation").asText());
//        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
//        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
//        assertEquals("vp", res.at("/query/operands/0/key").asText());
//        assertEquals("c", res.at("/query/operands/0/layer").asText());
//        assertEquals("cnx", res.at("/query/operands/0/foundry").asText());
//        assertEquals("koral:span", res.at("/query/operands/1/@type").asText());
//        assertEquals("np", res.at("/query/operands/1/key").asText());
//        assertEquals("c", res.at("/query/operands/1/layer").asText());
//        assertEquals("cnx", res.at("/query/operands/1/foundry").asText());
//        assertEquals("koral:relation", res.at("/query/relation/@type").asText());
//        assertEquals("koral:term", res.at("/query/relation/wrap/@type")
//                .asText());
//        assertEquals("c", res.at("/query/relation/wrap/layer").asText());
    }
   
    @Test
    public void testIndirectDominance () throws JsonProcessingException,
            IOException {
        query = "node & node & #1 >2,4 #2";
        qs.setQuery(query, "annis");
//        res = mapper.readTree(qs.toJSON());
//        assertEquals("koral:group", res.at("/query/@type").asText());
//        assertEquals("operation:relation", res.at("/query/operation").asText());
//        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
//        assertEquals("koral:span", res.at("/query/operands/1/@type").asText());
//        assertEquals("koral:relation", res.at("/query/relation/@type").asText());
//        assertEquals(2, res.at("/query/relation/boundary/min").asInt());
//        assertEquals(4, res.at("/query/relation/boundary/max").asInt());
//        assertEquals("koral:term", res.at("/query/relation/wrap/@type")
//                .asText());
//        assertEquals("c", res.at("/query/relation/wrap/layer").asText());

        query = "node & node & #1 >* #2";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
//        assertEquals(0, res.at("/query/relation/boundary/min").asInt());
//        assertEquals(true, res.at("/query/relation/boundary/max")
//                .isMissingNode());
    }

    
    @Test
    public void testDominanceWithType ()
            throws JsonProcessingException, IOException {
        query = "\"Mann\" & node & #2 >[func=\"SB\"] #1";  //coordinates the func=SB term and requires a "c"-layer term (consituency relation/dominance)
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
//        assertEquals("koral:relation", res.at("/query/relation/@type").asText());
//        assertEquals("koral:termGroup", res.at("/query/relation/wrap/@type")
//                .asText());
//        assertEquals("relation:and", res.at("/query/relation/wrap/relation")
//                .asText());
//        assertEquals("c", res.at("/query/relation/wrap/operands/1/layer")
//                .asText());
//        assertEquals("func", res.at("/query/relation/wrap/operands/0/layer")
//                .asText());
//        assertEquals("SB", res.at("/query/relation/wrap/operands/0/key")
//                .asText());
    }
    
    @Test
    public void testDominanceWithMultipleTypes ()
            throws JsonProcessingException, IOException {
        query = "corenlp/c=\"VP\" & corenlp/c=\"NP\" & #1 >[malt/d=\"PP\" malt/d=\"PN\"] #2";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
    }
    
    @Test
    public void testMultipleDominance () throws JsonProcessingException,
            IOException {
        query = "cat=\"CP\" & cat=\"VP\" & cat=\"NP\" & #1 > #2 > #3";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:relation", res.at("/query/operation").asText());
        assertEquals("koral:reference", res.at("/query/operands/0/@type")
                .asText());
        assertEquals("operation:focus", res.at("/query/operands/0/operation")
                .asText());
        assertEquals(129, res.at("/query/operands/0/classRef/0").asInt());
        assertEquals("koral:group", res
                .at("/query/operands/0/operands/0/@type").asText());
        assertEquals("operation:relation",
                res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals("koral:relation",
                res.at("/query/operands/0/operands/0/relation/@type").asText());
        assertEquals("c",
                res.at("/query/operands/0/operands/0/relation/wrap/layer")
                        .asText());
        assertEquals("koral:span",
                res.at("/query/operands/0/operands/0/operands/0/@type")
                        .asText());
        assertEquals("c",
                res.at("/query/operands/0/operands/0/operands/0/layer")
                        .asText());
        assertEquals("CP", res
                .at("/query/operands/0/operands/0/operands/0/key").asText());
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
                "VP",
                res.at("/query/operands/0/operands/0/operands/1/operands/0/key")
                        .asText());
    }

    //      query = "cat=\"CP\" & cat=\"VP\" & cat=\"NP\" & #1 > #2 > #3";
    //      String dom1 = 
    //              "{@type=koral:group, operation=operation:relation, operands=[" +
    //                      "{@type=koral:reference, operation=operation:focus, classRef=[0], operands=[" +
    //                          "{@type=koral:group, operation=operation:relation, operands=[" +
    //                              "{@type=koral:span, layer=cat, key=CP, match=match:eq}," +
    //                              "{@type=koral:group, operation=operation:class, class=1, classOut=1, operands=[" +
    //                                  "{@type=koral:span, layer=cat, key=VP, match=match:eq}" +
    //                              "]}" +
    //                          "], relation={@type=koral:relation, wrap={@type=koral:term, layer=c}}}" +
    //                      "]}," +
    //                      "{@type=koral:span, layer=cat, key=NP, match=match:eq}" +
    //              "], relation={@type=koral:relation, wrap={@type=koral:term, layer=c}}" +
    //              "}";
    //      aqlt = new AqlTree(query);
    //      map = aqlt.getRequestMap().get("query").toString();
    //      assertEquals(dom1.replaceAll(" ", ""), map.replaceAll(" ", ""));
    //      
    //      query = "cat=\"CP\" & cat=\"VP\" & cat=\"NP\" & cat=\"DP\" & #1 > #2 > #3 > #4";
    //      String dom2 = 
    //              "{@type=koral:group, operation=operation:relation, operands=[" +
    //                      "{@type=koral:reference, operation=operation:focus, classRef=[1], operands=[" +
    //                          "{@type=koral:group, operation=operation:relation, operands=[" +
    //                              "{@type=koral:reference, operation=operation:focus, classRef=[0], operands=[" +
    //                                  "{@type=koral:group, operation=operation:relation, operands=[" +
    //                                      "{@type=koral:span, layer=cat, key=CP, match=match:eq}," +
    //                                      "{@type=koral:group, operation=operation:class, class=1, classOut=1, operands=[" +
    //                                          "{@type=koral:span, layer=cat, key=VP, match=match:eq}" +
    //                                      "]}" +
    //                                  "], relation={@type=koral:relation, wrap={@type=koral:term, layer=c}}}" +
    //                              "]}," +
    //                              "{@type=koral:group, operation=operation:class, class=2, classOut=2, operands=[" +
    //                                  "{@type=koral:span, layer=cat, key=NP, match=match:eq}" +
    //                              "]}" +
    //                          "], relation={@type=koral:relation, wrap={@type=koral:term, layer=c}}}" +
    //                      "]}," +
    //                      "{@type=koral:span, layer=cat, key=DP, match=match:eq}" +
    //                  "], relation={@type=koral:relation, wrap={@type=koral:term, layer=c}}" +
    //              "}";
    //      aqlt = new AqlTree(query);
    //      map = aqlt.getRequestMap().get("query").toString();
    //      assertEquals(dom2.replaceAll(" ", ""), map.replaceAll(" ", ""));
    //  }
}
