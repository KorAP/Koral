package de.ids_mannheim.korap.query.test.cqp;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;
// import org.junit.Ignore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.common.collect.Lists;


import de.ids_mannheim.korap.query.serialize.QuerySerializer;

public class CQPSkipSentenceBoundaries{
    String query;
    ArrayList<JsonNode> operands;

    QuerySerializer qs = new QuerySerializer();
    ObjectMapper mapper = new ObjectMapper();
    JsonNode res;
    @Test
    public void skipendswith () throws JsonProcessingException, IOException {
  
          query =  "\"copil\" []{,5} \"cuminte\" </base/s=s>";
          qs.setQuery(query, "CQP");
          res = mapper.readTree(qs.toJSON());
          assertEquals("koral:group", res.at("/query/@type").asText());
          assertEquals("operation:sequence", res.at("/query/operation").asText());
          assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
          assertEquals("copil", res.at("/query/operands/0/wrap/key").asText());
          assertEquals("operation:repetition", res.at("/query/operands/1/operation").asText());
          assertEquals("koral:token", res.at("/query/operands/1/operands/0/@type").asText());
          assertEquals("0", res.at("/query/operands/1/boundary/min").asText());
          assertEquals("5", res.at("/query/operands/1/boundary/max").asText());
          assertEquals("operation:position", res.at("/query/operands/2/operation").asText());
          assertEquals("frames:endsWith", res.at("/query/operands/2/frames/0").asText());
          assertEquals("frames:matches", res.at("/query/operands/2/frames/1").asText());
          assertEquals("koral:span", res.at("/query/operands/2/operands/0/@type").asText());
          assertEquals("s", res.at("/query/operands/2/operands/0/wrap/key").asText());
          assertEquals("cuminte", res.at("/query/operands/2/operands/1/wrap/key").asText());
         
          
        
        }
 @Test
 public void skipstartswith () throws JsonProcessingException, IOException {
              query =   "<base/s=s> \"copil\" []{,5} \"cuminte\"";
              qs.setQuery(query, "CQP");
              res = mapper.readTree(qs.toJSON());
              assertEquals("koral:group", res.at("/query/@type").asText());
              assertEquals("operation:sequence", res.at("/query/operation").asText());
              
              assertEquals("operation:position", res.at("/query/operands/0/operation").asText());
              assertEquals("frames:startsWith", res.at("/query/operands/0/frames/0").asText());
              assertEquals("frames:matches", res.at("/query/operands/0/frames/1").asText());
              assertEquals("koral:span", res.at("/query/operands/0/operands/0/@type").asText());
              assertEquals("s", res.at("/query/operands/0/operands/0/wrap/key").asText());
              assertEquals("koral:token", res.at("/query/operands/1/operands/0/@type").asText());
              assertEquals("copil", res.at("/query/operands/0/operands/1/wrap/key").asText());
              
              
              assertEquals("operation:repetition", res.at("/query/operands/1/operation").asText());
              assertEquals("koral:token", res.at("/query/operands/1/operands/0/@type").asText());
              assertEquals("0", res.at("/query/operands/1/boundary/min").asText());
              assertEquals("5", res.at("/query/operands/1/boundary/max").asText());
              assertEquals("koral:token", res.at("/query/operands/2/@type").asText());
              assertEquals("cuminte", res.at("/query/operands/2/wrap/key").asText());
             
               }



}
