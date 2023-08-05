package de.ids_mannheim.korap.query.test.cqp;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;
// import org.junit.Ignore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.common.collect.Lists;

//import de.ids_mannheim.korap.query.object.KoralFrame;
import de.ids_mannheim.korap.query.serialize.QuerySerializer;


public class CQPMeetProcessorTest {
	String query;
    ArrayList<JsonNode> operands;

    QuerySerializer qs = new QuerySerializer();
    ObjectMapper mapper = new ObjectMapper();
    JsonNode res;


    
    @Test
    public void testMeetErrors () throws JsonProcessingException, IOException {
      // both error and warnings + query tree are returned;
     
     
      // tests for recursive queries with different offsets;     
       query = "MU(meet (meet \"in\" \"due\" 1 1) \"course\" -3 1))";
       qs.setQuery(query, "CQP");
       assertTrue(qs.hasErrors());
       res = mapper.readTree(qs.toJSON());
       assertEquals(302, res.at("/errors/0/0").asInt());;
       assertEquals("Recursiveness is not stable for meet queries with different offsets.", res.at("/errors/0/1").asText());
       // assertEquals("Recursiveness is not stable for meet queries with different offsets.", res.at("/warnings/0/0").asText());
       // query is returned
       assertEquals(0, res.at("/query/operands/0/operands/0/distances/0/boundary/min").asInt());
       
       query = "MU(meet (meet \"in\" \"due\" -1 1) \"course\" -3 1))";
       qs.setQuery(query, "CQP");
       assertTrue(qs.hasErrors());
       res = mapper.readTree(qs.toJSON());
       assertEquals(302, res.at("/errors/0/0").asInt());;
       assertEquals("Recursiveness is not stable for meet queries with different offsets.", res.at("/errors/0/1").asText()); 
       // assertEquals("Recursiveness is not stable for meet queries with different offsets.", res.at("/warnings/0/0").asText());
       // query is returned
       assertEquals(0, res.at("/query/operands/0/operands/0/distances/0/boundary/min").asInt());

       query = "MU(meet (meet \"in\" \"due\" -1 1) \"course\" -3 -3))";
       qs.setQuery(query, "CQP");
       assertTrue(qs.hasErrors());
       res = mapper.readTree(qs.toJSON());
       assertEquals(302, res.at("/errors/0/0").asInt());;
       assertEquals("Recursiveness is not stable for meet queries with different offsets.", res.at("/errors/0/1").asText()); 
       // assertEquals("Recursiveness is not stable for meet queries with different offsets.", res.at("/warnings/0/0").asText());
       // query is returned
       assertEquals(0, res.at("/query/operands/0/operands/0/distances/0/boundary/min").asInt());
       
       query = "MU(meet (meet \"x\" \"y\" -1 1) \"due\" 1 1) \"course\" -3 -3))";
       qs.setQuery(query, "CQP");
       assertTrue(qs.hasErrors());
       res = mapper.readTree(qs.toJSON());
       assertEquals(302, res.at("/errors/0/0").asInt());;
       assertEquals("Recursiveness is not stable for meet queries with different offsets.", res.at("/errors/0/1").asText()); 
       // query is returned
       assertEquals(0, res.at("/query/operands/0/operands/0/distances/0/boundary/min").asInt());

       query = "MU(meet (meet \"in\" \"due\" -3 2) (meet \"time\" \".*\" -1 1) -4 4);";
       qs.setQuery(query, "CQP");
       //assertTrue(qs.hasErrors());
       res = mapper.readTree(qs.toJSON());
       assertEquals(0, res.at("/query/operands/0/operands/0/distances/0/boundary/min").asInt());
       assertEquals("Recursiveness is not stable for meet queries with different offsets.", res.at("/errors/0/1").asText()); 
     
     // tests for zero offsets
     
       query = "MU(meet \"in\" \"due\" 0 -1);";
       qs.setQuery(query, "CQP");
       assertTrue(qs.hasErrors());
       res = mapper.readTree(qs.toJSON());
       assertEquals(302, res.at("/errors/0/0").asInt());;
       assertEquals("The MeetUnion offsets cannot be 0.", res.at("/errors/0/1").asText());

       // first processes the outer meet and calls computedistance on it, adding "icompatible offset values" error
       // and only later processes the firt inner meet and adds "The MeetUnion offsets cannot be 0" error.
       query = "MU(meet (meet \"x\" \"y\" 0 1) (meet \"z\" \"w\" 1 1) -1 -1);";
       qs.setQuery(query, "CQP");
       assertTrue(qs.hasErrors());
       res = mapper.readTree(qs.toJSON());
       assertEquals(302, res.at("/errors/1/0").asInt());;
       assertEquals("The MeetUnion offsets cannot be 0.", res.at("/errors/1/1").asText());
       assertEquals(302, res.at("/errors/0/0").asInt());;
       assertEquals("Incompatible offset values. The difference between the absolute values of the outer offsets and the second inner offsets should not be smaller than 1.", res.at("/errors/0/1").asText());

       
       query = "MU(meet (meet \"x\" \"y\" 0 0) (meet \"z\" \"w\" 1 1) -1 -1);";
       qs.setQuery(query, "CQP");
       assertTrue(qs.hasErrors());
       res = mapper.readTree(qs.toJSON());
       assertEquals(302, res.at("/errors/1/0").asInt());;
       assertEquals("The MeetUnion offsets cannot be 0.", res.at("/errors/1/1").asText());
       assertEquals(302, res.at("/errors/0/0").asInt());;
       assertEquals("Incompatible offset values. The difference between the absolute values of the outer offsets and the second inner offsets should not be smaller than 1.", res.at("/errors/0/1").asText());

       // does'n get to computeDistance for the outer offsets
       query = "MU(meet (meet \"x\" \"y\" 1 1) (meet \"z\" \"w\" 1 1) 0 -1);";
       qs.setQuery(query, "CQP");
       assertTrue(qs.hasErrors());
       res = mapper.readTree(qs.toJSON());
       assertEquals(302, res.at("/errors/0/0").asInt());;
       assertEquals("The MeetUnion offsets cannot be 0.", res.at("/errors/0/1").asText());
       assertNotEquals("Incompatible offset values. The difference between the absolute values of the outer offsets and the second inner offsets should not be smaller than 1.", res.at("/errors/1/1").asText());
       
       query = "MU(meet (meet \"x\" \"y\" 2 2) (meet \"z\" \"w\" 1 1) 0 0);";
       qs.setQuery(query, "CQP");
       assertTrue(qs.hasErrors());
       res = mapper.readTree(qs.toJSON());
       assertNotEquals(302, res.at("/errors/1/0").asInt());;
       assertNotEquals("Incompatible offset values. The difference between the absolute values of the outer offsets and the first inner offsets should not be smaller than 1.", res.at("/errors/1/1").asText());
       assertEquals(302, res.at("/errors/0/0").asInt());;
       assertEquals("The MeetUnion offsets cannot be 0.", res.at("/errors/0/1").asText());
     
       query = "MU(meet (meet \"x\" \"y\" 2 2) (meet \"z\" \"w\" -1 -1) -1 -1);";
       qs.setQuery(query, "CQP");
       assertFalse(qs.hasErrors());

       query = "MU(meet (meet \"x\" \"y\" 2 2) (meet \"z\" \"w\" 0 0) -1 -1);";
       qs.setQuery(query, "CQP");
       assertTrue(qs.hasErrors());
       res = mapper.readTree(qs.toJSON());
       assertEquals(302, res.at("/errors/0/0").asInt());;
       assertEquals("The MeetUnion offsets cannot be 0.", res.at("/errors/0/1").asText());

       
       // tests for equal offsets; distance incompatibilities, see computeSegmentDistance()
       // foffs = first ofsetss; soffs = second offsets; oofs = outer offsets;
     
     // Math.abs(ooffs1) - Math.abs(foffs1) - Math.abs(soffs1) - 1 >= 0  
       query = "MU(meet (meet \"x\" \"y\" -2 -2) (meet \"z\" \"w\" 1 1) -1 -1);";
       qs.setQuery(query, "CQP");
       assertTrue(qs.hasErrors());
       res = mapper.readTree(qs.toJSON());
       assertEquals(302, res.at("/errors/0/0").asInt());;
       assertEquals("Incompatible offset values. The difference between the absolute values of the outer offsets and the sum of the first inner and the second inner offsets should not be smaller than 1.", res.at("/errors/0/1").asText());
      
       query = "MU(meet (meet \"x\" \"y\" 2 2) (meet \"z\" \"w\" -1 -1) 1 1);";
       qs.setQuery(query, "CQP");
       assertTrue(qs.hasErrors());
       res = mapper.readTree(qs.toJSON());
       assertEquals(302, res.at("/errors/0/0").asInt());;
       assertEquals("Incompatible offset values. The difference between the absolute values of the outer offsets and the sum of the first inner and the second inner offsets should not be smaller than 1.", res.at("/errors/0/1").asText());
     
       // (Math.abs(ooffs1) - Math.abs(soffs1) - 1 >= 0)
       query = "MU(meet \"x\" (meet \"z\" \"w\" 1 1) -1 -1);";
       qs.setQuery(query, "CQP");
       assertTrue(qs.hasErrors());
       res = mapper.readTree(qs.toJSON());
       assertEquals(302, res.at("/errors/0/0").asInt());;
       assertEquals("Incompatible offset values. The difference between the absolute values of the outer offsets and the second inner offsets should not be smaller than 1.", res.at("/errors/0/1").asText());

       query = "MU(meet (meet \"x\" \"y\" 2 2) (meet \"z\" \"w\" 1 1) -1 -1);";
       qs.setQuery(query, "CQP");
       assertTrue(qs.hasErrors());
       res = mapper.readTree(qs.toJSON());
       assertEquals(302, res.at("/errors/0/0").asInt());;
       assertEquals("Incompatible offset values. The difference between the absolute values of the outer offsets and the second inner offsets should not be smaller than 1.", res.at("/errors/0/1").asText());

       query = "MU(meet (meet \"x\" \"y\" -2 -2) (meet \"z\" \"w\" -1 -1) 1 1);";
       qs.setQuery(query, "CQP");
       assertTrue(qs.hasErrors());
       res = mapper.readTree(qs.toJSON());
       assertEquals(302, res.at("/errors/0/0").asInt());;
       assertEquals("Incompatible offset values. The difference between the absolute values of the outer offsets and the second inner offsets should not be smaller than 1.", res.at("/errors/0/1").asText());

       // (Math.abs(ooffs1) - Math.abs(foffs1) - 1 >= 0)

       query = "MU(meet (meet \"x\" \"y\" 2 2) (meet \"z\" \"w\" 1 1) 1 1);";
       qs.setQuery(query, "CQP");
       assertTrue(qs.hasErrors());
       res = mapper.readTree(qs.toJSON());
       assertEquals(302, res.at("/errors/0/0").asInt());;
       assertEquals("Incompatible offset values. The difference between the absolute values of the outer offsets and the first inner offsets should not be smaller than 1.", res.at("/errors/0/1").asText());

       query = "MU(meet (meet \"x\" \"y\" -2 -2) (meet \"z\" \"w\" -1 -1) -1 -1);";
       qs.setQuery(query, "CQP");
       assertTrue(qs.hasErrors());
       res = mapper.readTree(qs.toJSON());
       assertEquals(302, res.at("/errors/0/0").asInt());;
       assertEquals("Incompatible offset values. The difference between the absolute values of the outer offsets and the first inner offsets should not be smaller than 1.", res.at("/errors/0/1").asText());

       query = "MU(meet (meet \"x\" \"y\" -2 -2) \"z\" -1 -1);";
       qs.setQuery(query, "CQP");
       assertTrue(qs.hasErrors());
       res = mapper.readTree(qs.toJSON());
       assertEquals(302, res.at("/errors/0/0").asInt());;
       assertEquals("Incompatible offset values. The difference between the absolute values of the outer offsets and the first inner offsets should not be smaller than 1.", res.at("/errors/0/1").asText());

       query = "MU(meet (meet \"x\" \"y\" 2 2) \"z\" 1 1);";
       qs.setQuery(query, "CQP");
       assertTrue(qs.hasErrors());
       res = mapper.readTree(qs.toJSON());
       assertEquals(302, res.at("/errors/0/0").asInt());;
       assertEquals("Incompatible offset values. The difference between the absolute values of the outer offsets and the first inner offsets should not be smaller than 1.", res.at("/errors/0/1").asText());

       


  }
    
    
    @Test
    public void testFocusMeetDisjunction() throws JsonProcessingException, IOException {     
    	
       
     
        /*query = "focus('der' {'Baum'}) | focus ({'Baum'} 'der');";*/
        query = "MU(meet \"Baum\" \"der\" -1 1);";
    	   qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/classRef/0").asInt());
        assertEquals("operation:disjunction", res.at("/query/operands/0/operation").asText());
        assertEquals("operation:sequence", res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals("operation:sequence", res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals(1, res.at("/query/operands/0/operands/0/operands/0/classOut").asInt());
        assertEquals("Baum", res.at("/query/operands/0/operands/0/operands/0/operands/0/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/operands/0/operands/0/operands/0/operands/0/wrap/type").asText());
        assertEquals("der", res.at("/query/operands/0/operands/0/operands/1/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/operands/0/operands/0/operands/1/wrap/type").asText());
        assertEquals("Baum", res.at("/query/operands/0/operands/1/operands/1/operands/0/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/operands/0/operands/1/operands/1/operands/0/wrap/type").asText());
        assertEquals("der", res.at("/query/operands/0/operands/1/operands/0/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/operands/0/operands/1/operands/0/wrap/type").asText());
        assertEquals(1, res.at("/meta/highlight/0").asInt());
        assertEquals(1, res.at("/meta/highlight/1").asInt());
        
        query = "MU(meet \"Baum\" \"der\" -2 1);";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/classRef/0").asInt());
        assertEquals("operation:disjunction", res.at("/query/operands/0/operation").asText());
        assertEquals("operation:sequence", res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals("operation:sequence", res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals(1, res.at("/query/operands/0/operands/0/operands/0/classOut").asInt());
        assertEquals("Baum", res.at("/query/operands/0/operands/0/operands/0/operands/0/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/operands/0/operands/0/operands/0/operands/0/wrap/type").asText());
        assertEquals("der", res.at("/query/operands/0/operands/0/operands/1/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/operands/0/operands/0/operands/1/wrap/type").asText());
        assertEquals("Baum", res.at("/query/operands/0/operands/1/operands/1/operands/0/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/operands/0/operands/1/operands/1/operands/0/wrap/type").asText());
        assertEquals("der", res.at("/query/operands/0/operands/1/operands/0/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/operands/0/operands/1/operands/0/wrap/type").asText());
        assertEquals(0, res.at("/query/operands/0/operands/1/distances/0/boundary/min").asInt());
        assertEquals(1, res.at("/query/operands/0/operands/1/distances/0/boundary/max").asInt());
        assertEquals(1, res.at("/meta/highlight/0").asInt());
        assertEquals(1, res.at("/meta/highlight/1").asInt());
        
        
        query = "MU(meet \"Baum\" \"der\" -2 3);";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/classRef/0").asInt());
        assertEquals("operation:disjunction", res.at("/query/operands/0/operation").asText());
        assertEquals("operation:sequence", res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals("operation:sequence", res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals(1, res.at("/query/operands/0/operands/0/operands/0/classOut").asInt());
        assertEquals("Baum", res.at("/query/operands/0/operands/0/operands/0/operands/0/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/operands/0/operands/0/operands/0/operands/0/wrap/type").asText());
        assertEquals("der", res.at("/query/operands/0/operands/0/operands/1/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/operands/0/operands/0/operands/1/wrap/type").asText());
        assertEquals("Baum", res.at("/query/operands/0/operands/1/operands/1/operands/0/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/operands/0/operands/1/operands/1/operands/0/wrap/type").asText());
        assertEquals("der", res.at("/query/operands/0/operands/1/operands/0/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/operands/0/operands/1/operands/0/wrap/type").asText());
        assertEquals(0, res.at("/query/operands/0/operands/0/distances/0/boundary/min").asInt());
        assertEquals(2, res.at("/query/operands/0/operands/0/distances/0/boundary/max").asInt());
        assertEquals(0, res.at("/query/operands/0/operands/1/distances/0/boundary/min").asInt());
        assertEquals(1, res.at("/query/operands/0/operands/1/distances/0/boundary/max").asInt());
        assertEquals(1, res.at("/meta/highlight/0").asInt());
        assertEquals(1, res.at("/meta/highlight/1").asInt());
      
        
        query = "MU(meet \"Baum\" \"der\" -1 2);";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/classRef/0").asInt());
        assertEquals("operation:disjunction", res.at("/query/operands/0/operation").asText());
        assertEquals("operation:sequence", res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals("operation:sequence", res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals(1, res.at("/query/operands/0/operands/0/operands/0/classOut").asInt());
        assertEquals("Baum", res.at("/query/operands/0/operands/0/operands/0/operands/0/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/operands/0/operands/0/operands/0/operands/0/wrap/type").asText());
        assertEquals("der", res.at("/query/operands/0/operands/0/operands/1/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/operands/0/operands/0/operands/1/wrap/type").asText());
        assertEquals("Baum", res.at("/query/operands/0/operands/1/operands/1/operands/0/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/operands/0/operands/1/operands/1/operands/0/wrap/type").asText());
        assertEquals("der", res.at("/query/operands/0/operands/1/operands/0/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/operands/0/operands/1/operands/0/wrap/type").asText());
        assertEquals(0, res.at("/query/operands/0/operands/0/distances/0/boundary/min").asInt());
        assertEquals(1, res.at("/query/operands/0/operands/0/distances/0/boundary/max").asInt());  
         
   }
  
   @Test
   public void testMeetSpan1 () throws JsonProcessingException, IOException { 
	    query=  "MU(meet (meet \"cambios\" \"climáticos\" 2 2) (meet \"la\" \"piel\" 1 1) s)";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:position", res.at("/query/operands/0/operation").asText());
        assertEquals("frames:isAround", res.at("/query/operands/0/frames/0").asText());
        assertEquals("koral:span", res.at("/query/operands/0/operands/0/@type").asText());
        assertEquals("s", res.at("/query/operands/0/operands/0/wrap/key").asText());
        assertEquals("koral:term", res.at("/query/operands/0/operands/0/wrap/@type").asText());
        assertEquals("koral:group", res.at("/query/operands/0/operands/1/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operands/0/operands/1/operation").asText());
	    assertEquals("false", res.at("/query/operands/0/operands/1/inOrder").asText());
	    assertEquals(0, res.at("/query/operands/0/operands/1/distances/0/boundary/min").asInt());
	    assertEquals("operation:sequence", res.at("/query/operands/0/operands/1/operands/0/operation").asText());
	    assertEquals(1, res.at("/query/operands/0/operands/1/operands/0/distances/0/boundary/min").asInt());
	    assertEquals(1, res.at("/query/operands/0/operands/1/operands/0/distances/0/boundary/max").asInt());
	    assertEquals("operation:class", res.at("/query/operands/0/operands/1/operands/0/operands/0/operation").asText());
        assertEquals(1, res.at("/query/operands/0/operands/1/operands/0/operands/0/classOut").asInt());
        assertEquals("cambios", res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/0/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/0/wrap/type").asText());
        assertEquals("climáticos", res.at("/query/operands/0/operands/1/operands/0/operands/1/wrap/key").asText());
	    assertEquals("type:regex", res.at("/query/operands/0/operands/1/operands/0/operands/1/wrap/type").asText());
        assertEquals("operation:sequence", res.at("/query/operands/0/operands/1/operands/1/operation").asText());
	    assertEquals("la", res.at("/query/operands/0/operands/1/operands/1/operands/0/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/operands/0/operands/1/operands/1/operands/0/wrap/type").asText());
	    assertEquals("piel", res.at("/query/operands/0/operands/1/operands/1/operands/1/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/operands/0/operands/1/operands/1/operands/1/wrap/type").asText());
   }
   	
   @Test
   public void testMeetSpan2 () throws JsonProcessingException, IOException {
     
     //"in due" | "due in" []* time in a span s
     // for this type of query, at the moment it throws a warning, an error and also returns the tree; 
     // this could be an useful type of query (this specific tree is correct), but also if the user complicates it with more level of imbrication, it becomes unstable/uncovered in the code;
     // is it necessary to have both an error and warning message?
     query = "MU(meet (meet \"in\" \"due\" -1 1) \"time\" s)";  
     qs.setQuery(query, "CQP");
     res = mapper.readTree(qs.toJSON());
     assertEquals("Recursiveness is not stable for meet queries with different offsets.", res.at("/errors/0/1").asText());
     // assertEquals("Recursiveness is not stable for meet queries with different offsets.", res.at("/warnings/0/0").asText());
     assertEquals("koral:reference", res.at("/query/@type").asText());
     assertEquals("operation:focus", res.at("/query/operation").asText());
     assertEquals(1, res.at("/query/classRef/0").asInt());
     assertEquals("operation:position", res.at("/query/operands/0/operation").asText());
     assertEquals("frames:isAround", res.at("/query/operands/0/frames/0").asText());
     assertEquals("koral:span", res.at("/query/operands/0/operands/0/@type").asText());
     assertEquals("s", res.at("/query/operands/0/operands/0/wrap/key").asText());
     assertEquals("operation:sequence", res.at("/query/operands/0/operands/1/operation").asText());
     assertEquals("operation:disjunction", res.at("/query/operands/0/operands/1/operands/0/operation").asText());
     assertEquals("operation:sequence", res.at("/query/operands/0/operands/1/operands/0/operands/0/operation").asText());
     assertEquals("operation:sequence", res.at("/query/operands/0/operands/1/operands/0/operands/1/operation").asText()); 
     assertEquals(1, res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/0/classOut").asInt());
     assertEquals("in", res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/0/operands/0/wrap/key").asText());
     assertEquals("type:regex", res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/0/operands/0/wrap/type").asText());
     assertEquals("due", res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/1/wrap/key").asText());
     assertEquals("type:regex", res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/1/wrap/type").asText());
     assertEquals("due", res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/0/wrap/key").asText());
     assertEquals("type:regex", res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/0/wrap/type").asText());
     assertEquals(1, res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/1/classOut").asInt());
     assertEquals("in", res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/1/operands/0/wrap/key").asText());
     assertEquals("type:regex", res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/1/operands/0/wrap/type").asText());
     assertEquals(0, res.at("/query/operands/0/operands/1/distances/0/boundary/min").asInt());
     assertEquals(0, res.at("/query/operands/0/operands/1/distances/0/boundary/max").asInt());
     assertEquals("time", res.at("/query/operands/0/operands/1/operands/1/wrap/key").asText());
     assertTrue(res.at("/query/operands/0/operands/1/distances/0/boundary/max").isMissingNode());
     assertEquals(1, res.at("/meta/highlight/0").asInt());
     assertEquals(1, res.at("/meta/highlight/1").asInt());
     
     
     //"piel" []* "azul" in a span np,  folowed by "de" at any distance, in a span s, inOrder = false on both levels
         query = "MU(meet (meet \"piel\" \"azul\" np)  \"de\" s)";
         qs.setQuery(query, "CQP");
         res = mapper.readTree(qs.toJSON());
         assertEquals("koral:reference", res.at("/query/@type").asText());
	    assertEquals("operation:focus", res.at("/query/operation").asText());
	    assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
	    assertEquals("operation:position", res.at("/query/operands/0/operation").asText());
	    assertEquals("frames:isAround", res.at("/query/operands/0/frames/0").asText());
	    assertEquals("koral:span", res.at("/query/operands/0/operands/0/@type").asText());
	    assertEquals("s", res.at("/query/operands/0/operands/0/wrap/key").asText());
         assertEquals("koral:group", res.at("/query/operands/0/operands/1/@type").asText());
         assertEquals("operation:sequence", res.at("/query/operands/0/operands/1/operation").asText());
	    assertEquals("false", res.at("/query/operands/0/operands/1/inOrder").asText());
	    assertEquals(0, res.at("/query/operands/0/operands/1/distances/0/boundary/min").asInt());
         assertTrue(res.at("/query/operands/0/operands/1/distances/0/boundary/max").isMissingNode());
         assertEquals("operation:position", res.at("/query/operands/0/operands/1/operands/0/operation").asText());
         assertEquals("frames:isAround", res.at("/query/operands/0/operands/1/operands/0/frames/0").asText());
         assertEquals("koral:span", res.at("/query/operands/0/operands/1/operands/0/operands/0/@type").asText());
	    assertEquals("np", res.at("/query/operands/0/operands/1/operands/0/operands/0/wrap/key").asText());
         assertEquals("operation:sequence", res.at("/query/operands/0/operands/1/operands/0/operands/1/operation").asText());
         assertEquals(1, res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/0/classOut").asInt());
	    assertEquals("piel", res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/0/operands/0/wrap/key").asText());
         assertEquals("type:regex", res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/0/operands/0/wrap/type").asText()); 
         assertEquals("azul", res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/1/wrap/key").asText());
         assertEquals("type:regex", res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/1/wrap/type").asText()); 
	    assertEquals("de", res.at("/query/operands/0/operands/1/operands/1/wrap/key").asText());
         assertEquals("type:regex", res.at("/query/operands/0/operands/1/operands/1/wrap/type").asText());

         query=  "MU(meet \"de\" \"piel\" <base/s=s>)";
	    qs.setQuery(query, "CQP");
	    res = mapper.readTree(qs.toJSON());
	    assertEquals("koral:reference", res.at("/query/@type").asText());
	    assertEquals("operation:focus", res.at("/query/operation").asText());
	    assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
	    assertEquals("operation:position", res.at("/query/operands/0/operation").asText());
	    assertEquals("frames:isAround", res.at("/query/operands/0/frames/0").asText());
	    assertEquals("koral:span", res.at("/query/operands/0/operands/0/@type").asText());
	    assertEquals("s", res.at("/query/operands/0/operands/0/wrap/key").asText());
	    assertEquals("s", res.at("/query/operands/0/operands/0/wrap/layer").asText());
	    assertEquals("base", res.at("/query/operands/0/operands/0/wrap/foundry").asText());
	    assertEquals("koral:group", res.at("/query/operands/0/operands/1/@type").asText());
	    assertEquals("operation:sequence", res.at("/query/operands/0/operands/1/operation").asText());
	    assertEquals("false", res.at("/query/operands/0/operands/1/inOrder").asText());
	    assertEquals(0, res.at("/query/operands/0/operands/1/distances/0/boundary/min").asInt());
	    assertEquals("operation:class", res.at("/query/operands/0/operands/1/operands/0/operation").asText());
	    assertEquals(1, res.at("/query/operands/0/operands/1/operands/0/classOut").asInt());
	    assertEquals("de", res.at("/query/operands/0/operands/1/operands/0/operands/0/wrap/key").asText());
         assertEquals("type:regex", res.at("/query/operands/0/operands/1/operands/0/operands/0/wrap/type").asText()); 
	    assertEquals("piel", res.at("/query/operands/0/operands/1/operands/1/wrap/key").asText());
         assertEquals("type:regex", res.at("/query/operands/0/operands/1/operands/1/wrap/type").asText());


         query=  "MU(meet \"de\"%l \"piel\"%l <base/s=s>)";
	    qs.setQuery(query, "CQP");
	    res = mapper.readTree(qs.toJSON());
	    assertEquals("koral:reference", res.at("/query/@type").asText());
	    assertEquals("operation:focus", res.at("/query/operation").asText());
	    assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
	    assertEquals("operation:position", res.at("/query/operands/0/operation").asText());
	    assertEquals("frames:isAround", res.at("/query/operands/0/frames/0").asText());
	    assertEquals("koral:span", res.at("/query/operands/0/operands/0/@type").asText());
	    assertEquals("s", res.at("/query/operands/0/operands/0/wrap/key").asText());
	    assertEquals("s", res.at("/query/operands/0/operands/0/wrap/layer").asText());
	    assertEquals("base", res.at("/query/operands/0/operands/0/wrap/foundry").asText());
	    assertEquals("koral:group", res.at("/query/operands/0/operands/1/@type").asText());
	    assertEquals("operation:sequence", res.at("/query/operands/0/operands/1/operation").asText());
	    assertEquals("false", res.at("/query/operands/0/operands/1/inOrder").asText());
	    assertEquals(0, res.at("/query/operands/0/operands/1/distances/0/boundary/min").asInt());
	    assertEquals("operation:class", res.at("/query/operands/0/operands/1/operands/0/operation").asText());
	    assertEquals(1, res.at("/query/operands/0/operands/1/operands/0/classOut").asInt());
	    assertEquals("de", res.at("/query/operands/0/operands/1/operands/0/operands/0/wrap/key").asText()); 
         assertEquals("type:string", res.at("/query/operands/0/operands/1/operands/0/operands/0/wrap/type").asText());
	    assertEquals("piel", res.at("/query/operands/0/operands/1/operands/1/wrap/key").asText());
         assertEquals("type:string", res.at("/query/operands/0/operands/1/operands/1/wrap/type").asText());
	   
           // the querry tree is not correct here! this situations are covered just with warning/error;
          query = "MU(meet (meet \"in\" \"due\" s) \"time\" 2 2)";  
          qs.setQuery(query, "CQP");
          res = mapper.readTree(qs.toJSON());
          assertEquals("Recursiveness is not stable for span meet queries as an inner query.", res.at("/errors/0/1").asText());
          // assertEquals("Recursiveness is not stable for span meet queries as an inner query.", res.at("/warnings/0/0").asText());
          assertEquals("koral:reference", res.at("/query/@type").asText());
         
          query = "MU(meet (meet \"in\" \"due\" s) \"time\" -2 2)";  
          qs.setQuery(query, "CQP");
          res = mapper.readTree(qs.toJSON());
          assertEquals("302", res.at("/errors/0/0").asText());
          assertEquals("Recursiveness is not stable for meet queries with different offsets.", res.at("/errors/0/1").asText());
          // assertEquals("Recursiveness is not stable for meet queries with different offsets.", res.at("/warnings/0/0").asText());
          assertEquals("302", res.at("/errors/1/0").asText());
          // the span warning/error is thrown for each term of the disjunction
          assertEquals("Recursiveness is not stable for span meet queries as an inner query.", res.at("/errors/1/1").asText());
          // assertEquals("Recursiveness is not stable for span meet queries as an inner query.", res.at("/warnings/1/0").asText());
          assertEquals("302", res.at("/errors/2/0").asText());
          assertEquals("Recursiveness is not stable for span meet queries as an inner query.", res.at("/errors/2/1").asText());
          // assertEquals("Recursiveness is not stable for span meet queries as an inner query.", res.at("/warnings/2/0").asText());

          assertEquals("koral:reference", res.at("/query/@type").asText()); 
   }

   // looking for 2 words with different/variable distances in between
   @Test
   public void testMeetVsFocus10 () throws JsonProcessingException, IOException {
 
       // 'due' []{0,2} 'in'; focus on "in";
       query = "MU(meet \"in\" \"due\" -3 -1);";
       qs.setQuery(query, "CQP");
       res = mapper.readTree(qs.toJSON());
       assertEquals("koral:reference", res.at("/query/@type").asText());
       assertEquals("operation:focus", res.at("/query/operation").asText());
       assertEquals(1, res.at("/query/classRef/0").asInt());
       assertEquals("operation:sequence", res.at("/query/operands/0/operation").asText());
       assertEquals("due", res.at("/query/operands/0/operands/0/wrap/key").asText());
       assertEquals("operation:class", res.at("/query/operands/0/operands/1/operation").asText());
       assertEquals(1, res.at("/query/operands/0/operands/1/classOut").asInt());
       assertEquals("in", res.at("/query/operands/0/operands/1/operands/0/wrap/key").asText());
       assertEquals(0, res.at("/query/operands/0/distances/0/boundary/min").asInt());
       assertEquals(2, res.at("/query/operands/0/distances/0/boundary/max").asInt());
       assertEquals(1, res.at("/meta/highlight/0").asInt());
     
        // 'in' []{0,2} 'due'; focus on "in";
         query = "MU(meet \"in\" \"due\" 1 3);";
      qs.setQuery(query, "CQP");
       res = mapper.readTree(qs.toJSON());
       assertEquals("koral:reference", res.at("/query/@type").asText());
       assertEquals("operation:focus", res.at("/query/operation").asText());
       assertEquals(1, res.at("/query/classRef/0").asInt());
       assertEquals("operation:sequence", res.at("/query/operands/0/operation").asText());
       assertEquals("operation:class", res.at("/query/operands/0/operands/0/operation").asText());
       assertEquals(1, res.at("/query/operands/0/operands/0/classOut").asInt());
       assertEquals("in", res.at("/query/operands/0/operands/0/operands/0/wrap/key").asText());
       assertEquals("due", res.at("/query/operands/0/operands/1/wrap/key").asText());
       assertEquals(0, res.at("/query/operands/0/distances/0/boundary/min").asInt());
       assertEquals(2, res.at("/query/operands/0/distances/0/boundary/max").asInt());
       assertEquals(1, res.at("/meta/highlight/0").asInt());
       
       // 'due' []{1,4} 'in'; focus on "in";
       query = "MU(meet \"in\" \"due\" -5 -2);";
           qs.setQuery(query, "CQP");
       res = mapper.readTree(qs.toJSON());
       assertEquals("koral:reference", res.at("/query/@type").asText());
       assertEquals("operation:focus", res.at("/query/operation").asText());
       assertEquals(1, res.at("/query/classRef/0").asInt());
       assertEquals("operation:sequence", res.at("/query/operands/0/operation").asText());
       assertEquals(1, res.at("/query/operands/0/operands/1/classOut").asInt());
       assertEquals("due", res.at("/query/operands/0/operands/0/wrap/key").asText());
       assertEquals("operation:class", res.at("/query/operands/0/operands/1/operation").asText());
       assertEquals("in", res.at("/query/operands/0/operands/1/operands/0/wrap/key").asText());
       assertEquals(1, res.at("/query/operands/0/distances/0/boundary/min").asInt());
       assertEquals(4, res.at("/query/operands/0/distances/0/boundary/max").asInt());
       assertEquals(1, res.at("/meta/highlight/0").asInt());
       
       // 'in' []{1,4} 'due'; focus on "in";
       query = "MU(meet \"in\" \"due\" 2 5);";
           qs.setQuery(query, "CQP");
       res = mapper.readTree(qs.toJSON());
       assertEquals("koral:reference", res.at("/query/@type").asText());
       assertEquals("operation:focus", res.at("/query/operation").asText());
       assertEquals(1, res.at("/query/classRef/0").asInt());
       assertEquals("operation:sequence", res.at("/query/operands/0/operation").asText());
       assertEquals("operation:class", res.at("/query/operands/0/operands/0/operation").asText());
       assertEquals(1, res.at("/query/operands/0/operands/0/classOut").asInt());
       assertEquals("in", res.at("/query/operands/0/operands/0/operands/0/wrap/key").asText());
       assertEquals("due", res.at("/query/operands/0/operands/1/wrap/key").asText());
       assertEquals(1, res.at("/query/operands/0/distances/0/boundary/min").asInt());
       assertEquals(4, res.at("/query/operands/0/distances/0/boundary/max").asInt());
       assertEquals(1, res.at("/meta/highlight/0").asInt());
    }  
// 3 word sequences
  

@Test
public void testMeetVsFocus11 () throws JsonProcessingException, IOException {
   // 'color' 'de' 'piel'; focus on 'color';
    query =  "MU(meet (meet \"color\" \"de\" 1 1) \"piel\" 2 2);";
    qs.setQuery(query, "CQP");
    res = mapper.readTree(qs.toJSON());
    assertEquals("koral:reference", res.at("/query/@type").asText());
    assertEquals("operation:focus", res.at("/query/operation").asText());
    assertEquals(1, res.at("/query/classRef/0").asInt());
    assertEquals("operation:sequence", res.at("/query/operands/0/operation").asText());
    assertEquals("operation:sequence", res.at("/query/operands/0/operands/0/operation").asText());
    assertEquals("operation:class", res.at("/query/operands/0/operands/0/operands/0/operation").asText());
    assertEquals(1, res.at("/query/operands/0/operands/0/operands/0/classOut").asInt());
    assertEquals("color", res.at("/query/operands/0/operands/0/operands/0/operands/0/wrap/key").asText());
    assertEquals("de",res.at("/query/operands/0/operands/0/operands/1/wrap/key").asText());
    assertEquals("piel", res.at("/query/operands/0/operands/1/wrap/key").asText());
    assertEquals(1, res.at("/meta/highlight/0").asInt());
}

@Test
public void testMeetVsFocus12 () throws JsonProcessingException, IOException {
    
    // 'color' 'de' 'piel'; focus on 'color'
     query =  "MU(meet \"color\" (meet \"de\" \"piel\" 1 1) 1 1);";
     qs.setQuery(query, "CQP");
     res = mapper.readTree(qs.toJSON());
     assertEquals("koral:reference", res.at("/query/@type").asText());
     assertEquals("operation:focus", res.at("/query/operation").asText());
     assertEquals(1, res.at("/query/classRef/0").asInt());
     assertEquals("operation:sequence", res.at("/query/operands/0/operation").asText());
     assertEquals("operation:class",res.at("/query/operands/0/operands/0/operation").asText());
     assertEquals(1, res.at("/query/operands/0/operands/0/classOut").asInt());
     assertEquals("color", res.at("/query/operands/0/operands/0/operands/0/wrap/key").asText());
     assertEquals("de", res.at("/query/operands/0/operands/1/operands/0/wrap/key").asText());
     assertEquals("piel", res.at("/query/operands/0/operands/1/operands/1/wrap/key").asText());
     assertEquals("operation:sequence", res.at("/query/operands/0/operands/1/operation").asText());
     assertEquals(1, res.at("/meta/highlight/0").asInt());
}
      

@Test        
public void testMeetVsFocus14 () throws JsonProcessingException, IOException {
 
    // color [] de piel; the focus is on "piel"
    query =  "MU(meet \"piel\" (meet \"color\" \"de\" 2 2) -3 -3);";
    qs.setQuery(query, "CQP");
    res = mapper.readTree(qs.toJSON());
    assertEquals("koral:reference", res.at("/query/@type").asText());
    assertEquals("operation:focus", res.at("/query/operation").asText());
    assertEquals(1, res.at("/query/classRef/0").asInt());
    assertEquals("operation:sequence", res.at("/query/operands/0/operation").asText());
    assertEquals("operation:sequence", res.at("/query/operands/0/operands/0/operation").asText());
    assertEquals("color", res.at("/query/operands/0/operands/0/operands/0/wrap/key").asText());
    assertEquals(1, res.at("/query/operands/0/operands/0/distances/0/boundary/min").asInt());
    assertEquals(1, res.at("/query/operands/0/operands/0/distances/0/boundary/max").asInt());
    assertEquals("de", res.at("/query/operands/0/operands/0/operands/1/wrap/key").asText());
    assertEquals("piel", res.at("/query/operands/0/operands/1/operands/0/wrap/key").asText());
    assertEquals("operation:class", res.at("/query/operands/0/operands/1/operation").asText());
    assertEquals(1, res.at("/query/operands/0/operands/1/classOut").asInt());
    assertEquals(1, res.at("/meta/highlight/0").asInt());
}
  
@Test
public void testMeetVsFocus15 () throws JsonProcessingException, IOException {
   // color de piel; the focus is on "piel";
     query =  "MU(meet \"piel\" (meet \"color\" \"de\" 1 1) -2 -2);";
     qs.setQuery(query, "CQP");
     res = mapper.readTree(qs.toJSON());
     assertEquals("koral:reference", res.at("/query/@type").asText());
     assertEquals("operation:focus", res.at("/query/operation").asText());
     assertEquals(1, res.at("/query/classRef/0").asInt());
     assertEquals("operation:sequence", res.at("/query/operands/0/operation").asText());
     assertEquals("operation:sequence", res.at("/query/operands/0/operands/0/operation").asText());
     assertEquals("color", res.at("/query/operands/0/operands/0/operands/0/wrap/key").asText());
     assertEquals("de",res.at("/query/operands/0/operands/0/operands/1/wrap/key").asText());
     assertEquals("piel", res.at("/query/operands/0/operands/1/operands/0/wrap/key").asText());
     assertEquals("operation:class", res.at("/query/operands/0/operands/1/operation").asText());
     assertEquals(1, res.at("/query/operands/0/operands/1/classOut").asInt());
     assertEquals(1, res.at("/meta/highlight/0").asInt());
}
@Test
public void testMeetVsFocus9 () throws JsonProcessingException, IOException {
     // color de piel, focus on "piel";
     query = "MU(meet (meet \"piel\" \"de\" -1 -1) \"color\" -2 -2);";
     qs.setQuery(query, "CQP");
     res = mapper.readTree(qs.toJSON());
     assertEquals("koral:reference", res.at("/query/@type").asText());
     assertEquals("operation:focus", res.at("/query/operation").asText());
     assertEquals(1, res.at("/query/classRef/0").asInt());
     assertEquals("operation:sequence", res.at("/query/operands/0/operation").asText());
     assertEquals("color", res.at("/query/operands/0/operands/0/wrap/key").asText());
     assertEquals("de", res.at("/query/operands/0/operands/1/operands/0/wrap/key").asText());
     assertEquals("piel", res.at("/query/operands/0/operands/1/operands/1/operands/0/wrap/key").asText());
     assertEquals("operation:class",res.at("/query/operands/0/operands/1/operands/1/operation").asText());
     assertEquals(1, res.at("/query/operands/0/operands/1/operands/1/classOut").asInt());
     assertEquals("operation:sequence", res.at("/query/operands/0/operands/1/operation").asText());
     assertEquals(1, res.at("/meta/highlight/0").asInt());
}
 

   // looking for long sequences of 4 words with focus on different positions

     @Test
     public void TestMeetFocusPos4 () throws JsonProcessingException, IOException {
       // color de piel oscuro; focus on "oscuro";
	    
	   query = "MU(meet (meet 'oscuro' 'piel' -1 -1) (meet \"color\" \"de\" 1 1) -3 -3)";
	   qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:group", res.at("/query/operands/0/operands/1/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operands/0/operation").asText());
        assertEquals("operation:sequence", res.at("/query/operands/0/operands/0/operation").asText());  
        assertEquals("color", res.at("/query/operands/0/operands/0/operands/0/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/operands/0/operands/0/operands/0/wrap/type").asText());
        assertEquals("de", res.at("/query/operands/0/operands/0/operands/1/wrap/key").asText());
        assertEquals("operation:sequence", res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals("piel", res.at("/query/operands/0/operands/1/operands/0/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/operands/0/operands/1/operands/0/wrap/type").asText());
        assertEquals("operation:class", res.at("/query/operands/0/operands/1/operands/1/operation").asText());
        assertEquals(1, res.at("/query/operands/0/operands/1/operands/1/classOut").asInt());
        assertEquals("oscuro", res.at("/query/operands/0/operands/1/operands/1/operands/0/wrap/key").asText()); 
	   
   }
   
   @Test
   public void testMeetFocusPos3 () throws JsonProcessingException, IOException {
	     // color de piel oscuro; focus on "piel";
	   query = "MU(meet (meet \"piel\" \"oscuro\" 1 1) (meet \"color\" \"de\" 1 1) -2 -2)";
	   qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:group", res.at("/query/operands/0/operands/1/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operands/0/operation").asText());
        assertEquals("operation:sequence", res.at("/query/operands/0/operands/0/operation").asText());  
        assertEquals("color", res.at("/query/operands/0/operands/0/operands/0/wrap/key").asText());
        assertEquals("de", res.at("/query/operands/0/operands/0/operands/1/wrap/key").asText());
        assertEquals("operation:sequence", res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals("piel", res.at("/query/operands/0/operands/1/operands/0/operands/0/wrap/key").asText());
        assertEquals("operation:class", res.at("/query/operands/0/operands/1/operands/0/operation").asText());
        assertEquals(1, res.at("/query/operands/0/operands/1/operands/0/classOut").asInt());
        assertEquals("oscuro", res.at("/query/operands/0/operands/1/operands/1/wrap/key").asText()); 
   }
   
   @Test
   public void testMeetFocusPos2 () throws JsonProcessingException, IOException {
	   
	   // color de piel oscuro; focus on "de";
	   query = "MU(meet (meet \"de\" \"color\" -1 -1) (meet \"piel\" \"oscuro\" 1 1) 1 1)";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:group", res.at("/query/operands/0/operands/1/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operands/0/operation").asText());
        assertEquals("operation:sequence", res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals("operation:class", res.at("/query/operands/0/operands/0/operands/1/operation").asText());
        assertEquals(1, res.at("/query/operands/0/operands/0/operands/1/classOut").asInt());
        assertEquals("color", res.at("/query/operands/0/operands/0/operands/0/wrap/key").asText());
        assertEquals("de", res.at("/query/operands/0/operands/0/operands/1/operands/0/wrap/key").asText());
        assertEquals("operation:sequence", res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals("piel", res.at("/query/operands/0/operands/1/operands/0/wrap/key").asText());
        assertEquals("oscuro", res.at("/query/operands/0/operands/1/operands/1/wrap/key").asText()); 
   }

   @Test
   public void testMeetFocusPos1 () throws JsonProcessingException, IOException {
     // color de piel oscuro; focus on "color"
          query = "MU(meet (meet \"color\" \"de\" 1 1) (meet \"piel\" \"oscuro\" 1 1) 2 2)";
          qs.setQuery(query, "CQP");
          res = mapper.readTree(qs.toJSON());
          assertEquals("koral:reference", res.at("/query/@type").asText());
          assertEquals("operation:focus", res.at("/query/operation").asText());
          assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
          assertEquals("koral:group", res.at("/query/operands/0/operands/1/@type").asText());
          assertEquals("operation:sequence", res.at("/query/operands/0/operation").asText());
          assertEquals("operation:sequence", res.at("/query/operands/0/operands/0/operation").asText());
          assertEquals("operation:class", res.at("/query/operands/0/operands/0/operands/0/operation").asText());
          assertEquals(1, res.at("/query/operands/0/operands/0/operands/0/classOut").asInt());
          assertEquals("color", res.at("/query/operands/0/operands/0/operands/0/operands/0/wrap/key").asText());
          assertEquals("de", res.at("/query/operands/0/operands/0/operands/1/wrap/key").asText());
          assertEquals("operation:sequence", res.at("/query/operands/0/operands/1/operation").asText());
          assertEquals("piel", res.at("/query/operands/0/operands/1/operands/0/wrap/key").asText());
          assertEquals("oscuro", res.at("/query/operands/0/operands/1/operands/1/wrap/key").asText());     
}   
   
   @Test
   public void testMeetFocusDistPos1 () throws JsonProcessingException, IOException {
	   	
       	 // color de []{2} piel oscuro; focus on "color";
        query = "MU(meet (meet \"color\" \"de\" 1 1) (meet \"piel\" \"oscuro\" 1 1) 4 4)";
	   qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:group", res.at("/query/operands/0/operands/1/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operands/0/operation").asText());
        assertEquals("operation:sequence", res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals("operation:class", res.at("/query/operands/0/operands/0/operands/0/operation").asText());
        assertEquals(1, res.at("/query/operands/0/operands/0/operands/0/classOut").asInt());
        assertEquals("color", res.at("/query/operands/0/operands/0/operands/0/operands/0/wrap/key").asText());
        assertEquals("de", res.at("/query/operands/0/operands/0/operands/1/wrap/key").asText());
        assertEquals("2", res.at("/query/operands/0/distances/0/boundary/min").asText());
        assertEquals("2", res.at("/query/operands/0/distances/0/boundary/max").asText());
        assertEquals("operation:sequence", res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals("piel", res.at("/query/operands/0/operands/1/operands/0/wrap/key").asText());
        assertEquals("oscuro", res.at("/query/operands/0/operands/1/operands/1/wrap/key").asText()); 
   }   
	    

        // // equal offsets mean exact distance
   @Test
   public void testMeetEqOffs () throws JsonProcessingException, IOException {
 	  	
   	// 'in' 'due'; focus on 'in'
        query = "MU(meet \"in\" \"due\" 1 1);";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/classRef/0").asInt());
        assertEquals("operation:sequence", res.at("/query/operands/0/operation").asText());
        assertEquals("operation:class", res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals(1, res.at("/query/operands/0/operands/0/classOut").asInt());
        assertEquals("in", res.at("/query/operands/0/operands/0/operands/0/wrap/key").asText());
        assertEquals("due", res.at("/query/operands/0/operands/1/wrap/key").asText());
        assertEquals(1, res.at("/meta/highlight/0").asInt());
   	
        // 'due' 'in'; focus on 'in';
        query = "MU(meet \"in\" \"due\" -1 -1);";
      	qs.setQuery(query, "CQP");
      	res = mapper.readTree(qs.toJSON());
      	assertEquals("koral:reference", res.at("/query/@type").asText());
      	assertEquals("operation:focus", res.at("/query/operation").asText());
      	assertEquals(1, res.at("/query/classRef/0").asInt());
      	assertEquals("operation:sequence", res.at("/query/operands/0/operation").asText());
      	assertEquals("due", res.at("/query/operands/0/operands/0/wrap/key").asText());
      	assertEquals("operation:class", res.at("/query/operands/0/operands/1/operation").asText());
      	assertEquals(1, res.at("/query/operands/0/operands/1/classOut").asInt());
      	assertEquals("in", res.at("/query/operands/0/operands/1/operands/0/wrap/key").asText());
      	assertEquals(1, res.at("/meta/highlight/0").asInt());
          
      	// 'in' [] 'due'; focus on 'in';
      	query = "MU(meet \"in\" \"due\" 2 2);";
      	qs.setQuery(query, "CQP");
      	res = mapper.readTree(qs.toJSON());
      	assertEquals("koral:reference", res.at("/query/@type").asText());
      	assertEquals("operation:focus", res.at("/query/operation").asText());
      	assertEquals(1, res.at("/query/classRef/0").asInt());
      	assertEquals("operation:sequence", res.at("/query/operands/0/operation").asText());
      	assertEquals("operation:class", res.at("/query/operands/0/operands/0/operation").asText());
      	assertEquals(1, res.at("/query/operands/0/operands/0/classOut").asInt());
      	assertEquals("in", res.at("/query/operands/0/operands/0/operands/0/wrap/key").asText());
      	assertEquals("due", res.at("/query/operands/0/operands/1/wrap/key").asText());
      	assertEquals(1, res.at("/query/operands/0/distances/0/boundary/min").asInt());
      	assertEquals(1, res.at("/query/operands/0/distances/0/boundary/max").asInt());
      	assertEquals(1, res.at("/meta/highlight/0").asInt());
       
      	// 'due' []{2} 'in'; focus on in;
      	query = "MU(meet \"in\" \"due\" -3 -3);";
      	qs.setQuery(query, "CQP");
      	res = mapper.readTree(qs.toJSON());
      	assertEquals("koral:reference", res.at("/query/@type").asText());
      	assertEquals("operation:focus", res.at("/query/operation").asText());
      	assertEquals(1, res.at("/query/classRef/0").asInt());
      	assertEquals("operation:sequence", res.at("/query/operands/0/operation").asText());
      	assertEquals("due", res.at("/query/operands/0/operands/0/wrap/key").asText());
    	     assertEquals("operation:class", res.at("/query/operands/0/operands/1/operation").asText());
      	assertEquals(1, res.at("/query/operands/0/operands/1/classOut").asInt());
      	assertEquals("in", res.at("/query/operands/0/operands/1/operands/0/wrap/key").asText());
      	assertEquals(2, res.at("/query/operands/0/distances/0/boundary/min").asInt());
      	assertEquals(2, res.at("/query/operands/0/distances/0/boundary/max").asInt());
      	assertEquals(1, res.at("/meta/highlight/0").asInt());
	
}
   

        @Test
     public void testMeetPos () throws JsonProcessingException, IOException {
          // [pos="NN.*"] of virtue; the focus is on [pos="NN.*"]
          query=  "MU(meet (meet [pos=\"NN.*\"] \"of\" 1 1) \"virtue\" 2 2);";
          qs.setQuery(query, "CQP");
          res = mapper.readTree(qs.toJSON());
          assertEquals("koral:reference", res.at("/query/@type").asText());
      	assertEquals("operation:focus", res.at("/query/operation").asText());
      	assertEquals(1, res.at("/query/classRef/0").asInt());
      	assertEquals("operation:sequence", res.at("/query/operands/0/operation").asText());
      	assertEquals("operation:sequence", res.at("/query/operands/0/operands/0/operation").asText());
      	assertEquals("operation:class", res.at("/query/operands/0/operands/0/operands/0/operation").asText());
      	assertEquals(1, res.at("/query/operands/0/operands/0/operands/0/classOut").asInt());
      	assertEquals("NN.*", res.at("/query/operands/0/operands/0/operands/0/operands/0/wrap/key").asText());
      	assertEquals("pos", res.at("/query/operands/0/operands/0/operands/0/operands/0/wrap/layer").asText());
      	assertEquals("of",res.at("/query/operands/0/operands/0/operands/1/wrap/key").asText());
      	assertEquals("virtue", res.at("/query/operands/0/operands/1/wrap/key").asText());
      	assertEquals(1, res.at("/meta/highlight/0").asInt());
        }   	    	
        
       
  

}
