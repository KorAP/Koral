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

//import de.ids_mannheim.korap.query.object.KoralFrame;
import de.ids_mannheim.korap.query.serialize.QuerySerializer;


public class CQPMeetProcessorTest {
	String query;
    ArrayList<JsonNode> operands;

    QuerySerializer qs = new QuerySerializer();
    ObjectMapper mapper = new ObjectMapper();
    JsonNode res;
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
   public void focus () throws JsonProcessingException, IOException {	  
	   	query="focus({'in'} 'due')";
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
   	}
   
   
   @Test
   public void testMeetVsFocus1 () throws JsonProcessingException, IOException { 
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
   public void testMeetVsFocus2 () throws JsonProcessingException, IOException {
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
	   
   }
	   
   @Test
   public void TestMeetVsFocus3 () throws JsonProcessingException, IOException {
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
   public void testMeetVsFocus4 () throws JsonProcessingException, IOException {
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
   public void testMeetVsFocus5 () throws JsonProcessingException, IOException {
	   
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
   public void testMeetVsFocus6 () throws JsonProcessingException, IOException {
	   	 
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
   @Test
   public void testMeetVsFocus7 () throws JsonProcessingException, IOException {
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
   public void testMeetVsFocus8 () throws JsonProcessingException, IOException {
   		// color de piel, focus on "de";
	query = "MU(meet (meet \"de\" \"piel\" 1 1) \"color\" -1 -1);";
	qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
   	assertEquals("operation:focus", res.at("/query/operation").asText());
   	assertEquals(1, res.at("/query/classRef/0").asInt());
   	assertEquals("operation:sequence", res.at("/query/operands/0/operation").asText());
   	assertEquals("color", res.at("/query/operands/0/operands/0/wrap/key").asText());
   	assertEquals("de", res.at("/query/operands/0/operands/1/operands/0/operands/0/wrap/key").asText());
   	assertEquals("operation:class",res.at("/query/operands/0/operands/1/operands/0/operation").asText());
  	assertEquals(1, res.at("/query/operands/0/operands/1/operands/0/classOut").asInt());
   	assertEquals("piel", res.at("/query/operands/0/operands/1/operands/1/wrap/key").asText());
   	assertEquals("operation:sequence", res.at("/query/operands/0/operands/1/operation").asText());
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
	    
    @Test
    public void testMeetVsFocus10 () throws JsonProcessingException, IOException {
   	query = "MU(meet \"in\" \"due\" 0 -1);";
	qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("The MeetUnion offsets cannot be 0!!", res.at("/warnings/0/0").asText());
        
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
        public void testMeetVsFocus13 () throws JsonProcessingException, IOException {
	   
	   // this test has to fail in the end, eliminate focus from the grammar! 
    	query = "focus({'color'} 'de' 'piel')";
    	qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/classRef/0").asInt());
        assertEquals("operation:sequence", res.at("/query/operands/0/operation").asText());
        assertEquals("operation:class", res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals(1, res.at("/query/operands/0/operands/0/classOut").asInt());
        assertEquals("color", res.at("/query/operands/0/operands/0/operands/0/wrap/key").asText());
        assertEquals("de", res.at("/query/operands/0/operands/1/wrap/key").asText());
        assertEquals("piel", res.at("/query/operands/0/operands/2/wrap/key").asText());   
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
        public void testMeetPos () throws JsonProcessingException, IOException {
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
        
        @Test
        public void testMeetVsFocus17 () throws JsonProcessingException, IOException {
        
       // delete the following at the end!!
        query="focus(contains(<base/s=s>, {'de'} 'piel'))";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        
        
        query = "focus([orth='Der']{[orth='Mann']})";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/classRef/0").asInt());
        assertEquals("operation:sequence", res.at("/query/operands/0/operation").asText());
        assertEquals("operation:class", res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals(1, res.at("/query/operands/0/operands/1/classOut").asInt());
        assertEquals("Mann", res.at("/query/operands/0/operands/1/operands/0/wrap/key").asText());
        assertEquals(1, res.at("/meta/highlight/0").asInt());

        query = "focus([orth='Der']{[orth='Mann'][orth='geht']})";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("operation:sequence",
        res.at("/query/operands/0/operands/1/operands/0/operation").asText());
        assertEquals("Mann", res.at("/query/operands/0/operands/1/operands/0/operands/0/wrap/key").asText());
        assertEquals("geht", res.at("/query/operands/0/operands/1/operands/0/operands/1/wrap/key").asText());
        }
  

}
