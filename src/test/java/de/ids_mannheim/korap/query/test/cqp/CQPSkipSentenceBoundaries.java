package de.ids_mannheim.korap.query.test.cqp;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;
// import org.junit.Ignore;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.JsonMappingException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.common.collect.Lists;


import de.ids_mannheim.korap.query.serialize.QuerySerializer;

import de.ids_mannheim.korap.query.test.BaseQueryTest;

public class CQPSkipSentenceBoundaries extends BaseQueryTest {

    String query;
    ArrayList<JsonNode> operands;

    QuerySerializer qs = new QuerySerializer();
    ObjectMapper mapper = new ObjectMapper();
    JsonNode res;


    public CQPSkipSentenceBoundaries () {
        super("CQP");
    }
    
    @Test
    public void spansegment () throws JsonProcessingException, IOException {
    query = "<corenlp/c=NP>;";
    qs.setQuery(query, "CQP");
    res = mapper.readTree(qs.toJSON());
    assertEquals("koral:span", res.at("/query/@type").asText());
    assertEquals("corenlp", res.at("/query/wrap/foundry").asText());
    assertEquals("koral:term", res.at("/query/wrap/@type").asText());
    assertEquals("c", res.at("/query/wrap/layer").asText());
    assertEquals("NP", res.at("/query/wrap/key").asText());
  
  
    // this is too complicated to implement in a stable way; 
    //query = "[(pos = \"NNS?\") & !np ]";
    //qs.setQuery(query, "CQP");
    //res = mapper.readTree(qs.toJSON());
    //assertNotEquals("koral:group", res.at("/query/@type").asText());
}
    
    
    @Test

    public void skipendswith () throws JsonProcessingException, IOException {

        query = "\"copil\" []{,5} \"cuminte\" </base/s=s>";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals(2, res.at("/query/operands").size());
        assertEquals(2, res.at("/query/frames").size());
        assertEquals("frames:endsWith", res.at("/query/frames/0").asText());
        assertEquals("frames:matches", res.at("/query/frames/1").asText());
 
        
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("base", res.at("/query/operands/0/wrap/foundry").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/layer").asText());
        
        assertEquals("koral:reference", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:focus", res.at("/query/operands/1/operation").asText());

        assertEquals("koral:group", res.at("/query/operands/1/operands/0/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operands/1/operands/0/operation").asText());

       
        assertEquals("koral:group", res.at("/query/operands/1/operands/0/operands/0/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/1/operands/0/operands/0/operation").asText());
        assertEquals("koral:token", res.at("/query/operands/1/operands/0/operands/0/operands/0/@type").asText());
        assertEquals("copil", res.at("/query/operands/1/operands/0/operands/0/operands/0/wrap/key").asText());
        
        assertEquals("koral:group", res.at("/query/operands/1/operands/0/operands/1/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/1/operands/0/operands/1/operation").asText());
        assertEquals("operation:repetition",
                res.at("/query/operands/1/operands/0/operands/1/operands/0/operation").asText());
        assertEquals("koral:token",
                res.at("/query/operands/1/operands/0/operands/1/operands/0/operands/0/@type").asText());
        assertEquals("0", res.at("/query/operands/1/operands/0/operands/1/operands/0/boundary/min").asText());
        assertEquals("5", res.at("/query/operands/1/operands/0/operands/1/operands/0/boundary/max").asText());        

        
        assertEquals("koral:group", res.at("/query/operands/1/operands/0/operands/2/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/1/operands/0/operands/2/operation").asText());
        assertEquals("koral:token", res.at("/query/operands/1/operands/0/operands/2/operands/0/@type").asText());
        assertEquals("cuminte", res.at("/query/operands/1/operands/0/operands/2/operands/0/wrap/key").asText());



    }


    @Test
    public void skipstartswith () throws JsonProcessingException, IOException {
        query = "<base/s=s> \"copil\" []{,5} \"cuminte\"";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals(2, res.at("/query/operands").size());
        assertEquals(2, res.at("/query/frames").size());
        assertEquals("frames:startsWith", res.at("/query/frames/0").asText());
        assertEquals("frames:matches", res.at("/query/frames/1").asText());
 
        
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("base", res.at("/query/operands/0/wrap/foundry").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/layer").asText());
        
        assertEquals("koral:reference", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:focus", res.at("/query/operands/1/operation").asText());

        assertEquals("koral:group", res.at("/query/operands/1/operands/0/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operands/1/operands/0/operation").asText());

       
        assertEquals("koral:group", res.at("/query/operands/1/operands/0/operands/0/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/1/operands/0/operands/0/operation").asText());
        assertEquals("koral:token", res.at("/query/operands/1/operands/0/operands/0/operands/0/@type").asText());
        assertEquals("copil", res.at("/query/operands/1/operands/0/operands/0/operands/0/wrap/key").asText());
        
        assertEquals("koral:group", res.at("/query/operands/1/operands/0/operands/1/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/1/operands/0/operands/1/operation").asText());
        assertEquals("operation:repetition",
                res.at("/query/operands/1/operands/0/operands/1/operands/0/operation").asText());
        assertEquals("koral:token",
                res.at("/query/operands/1/operands/0/operands/1/operands/0/operands/0/@type").asText());
        assertEquals("0", res.at("/query/operands/1/operands/0/operands/1/operands/0/boundary/min").asText());
        assertEquals("5", res.at("/query/operands/1/operands/0/operands/1/operands/0/boundary/max").asText());        

        
        assertEquals("koral:group", res.at("/query/operands/1/operands/0/operands/2/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/1/operands/0/operands/2/operation").asText());
        assertEquals("koral:token", res.at("/query/operands/1/operands/0/operands/2/operands/0/@type").asText());
        assertEquals("cuminte", res.at("/query/operands/1/operands/0/operands/2/operands/0/wrap/key").asText());


    }

    @Test
    public void testSequenceQueryWithSentenceStart ()  throws JsonMappingException, JsonProcessingException {
           query = "\"copil\" []{,5} \"cuminte\" <base/s=s>";
           qs.setQuery(query, "CQP");
           res = mapper.readTree(qs.toJSON());
           assertEquals("koral:reference", res.at("/query/@type").asText());
           assertEquals("operation:focus", res.at("/query/operation").asText());
           assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
           assertEquals("operation:sequence", res.at("/query/operands/0/operation").asText());  
           assertEquals("koral:group", res.at("/query/operands/0/operands/0/@type").asText());
           assertEquals("operation:class", res.at("/query/operands/0/operands/0/operation").asText());
           assertEquals("koral:token", res.at("/query/operands/0/operands/0/operands/0/@type").asText());
           assertEquals("type:regex", res.at("/query/operands/0/operands/0/operands/0/wrap/type").asText());
           assertEquals("copil", res.at("/query/operands/0/operands/0/operands/0/wrap/key").asText());
        assertEquals("koral:group", res.at("/query/operands/0/operands/1/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals("koral:group", res.at("/query/operands/0/operands/1/operands/0/@type").asText());
        assertEquals("operation:repetition", res.at("/query/operands/0/operands/1/operands/0/operation").asText());
        assertEquals("koral:token", res.at("/query/operands/0/operands/1/operands/0/operands/0/@type").asText());
        assertEquals("0", res.at("/query/operands/0/operands/1/operands/0/boundary/min").asText());
        assertEquals("5", res.at("/query/operands/0/operands/1/operands/0/boundary/max").asText());
       
        assertEquals("type:regex", res.at("/query/operands/0/operands/2/operands/0/wrap/type").asText());
        assertEquals("cuminte", res.at("/query/operands/0/operands/2/operands/0/wrap/key").asText());
        
        assertEquals("koral:span", res.at("/query/operands/0/operands/3/@type").asText());
        
       
    }


}
