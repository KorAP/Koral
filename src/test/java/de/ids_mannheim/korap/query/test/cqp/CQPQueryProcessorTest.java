package de.ids_mannheim.korap.query.test.cqp;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;
import org.junit.Ignore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

//import de.ids_mannheim.korap.query.object.KoralFrame;
import de.ids_mannheim.korap.query.serialize.QuerySerializer;

/**
 * Tests for JSON-LD serialization of CQP queries.
 * 
 * @author Joachim Bingel (bingel@ids-mannheim.de)
 * @author Elena Irimia (elena@racai.ro) based on PoliqarpPlusQueryProcessorTest
 * @version 1.0
 */
public class CQPQueryProcessorTest {

    String query;
    ArrayList<JsonNode> operands;

    QuerySerializer qs = new QuerySerializer();
    ObjectMapper mapper = new ObjectMapper();
    JsonNode res;


    @Test
    public void testUnknownLayer () throws IOException {
        query = "[mate/b=\"Baum\"]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("b", res.at("/query/wrap/layer").asText());
        
        query = "[mate/bi='Baum']";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("bi", res.at("/query/wrap/layer").asText());
    }

    @Test
    public void testLayerWithFlag () throws IOException {
        query = "[base='Baum'%c]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("lemma", res.at("/query/wrap/layer").asText());
        assertEquals("flags:caseInsensitive", res.at("/query/wrap/flags/0").asText());

        query = "[mate/x=\"Baum\"%c]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("x", res.at("/query/wrap/layer").asText());
        assertEquals("flags:caseInsensitive", res.at("/query/wrap/flags/0").asText());
    }

    
    @Test
    public void testContext () throws JsonProcessingException, IOException {
        query = "\"foo\"";
        String contextString = "http://korap.ids-mannheim.de/ns/koral/0.3/context.jsonld";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals(contextString, res.get("@context").asText());
    }


    @Test
    public void testSingleTokens () throws JsonProcessingException, IOException {
        query = "[base=\"Mann\"]";
        qs.setQuery(query, "CQP");
        assertFalse(qs.hasErrors());
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("Mann", res.at("/query/wrap/key").asText());
        assertEquals("lemma", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
        
        
        query = "[base=\"Mann\"]";
        qs.setQuery(query, "CQP");
		assertFalse(qs.hasErrors());
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("Mann", res.at("/query/wrap/key").asText());
        assertEquals("lemma", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "[orth!=\"Frau\"]";
        qs.setQuery(query, "CQP");
	assertFalse(qs.hasErrors());
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("Frau", res.at("/query/wrap/key").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:ne", res.at("/query/wrap/match").asText());

        query = "[p!=\"NN\"]";
        qs.setQuery(query, "CQP");
	assertFalse(qs.hasErrors());
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("NN", res.at("/query/wrap/key").asText());
        assertEquals("p", res.at("/query/wrap/layer").asText());
        assertEquals("match:ne", res.at("/query/wrap/match").asText());

        query = "[!p!=\"NN\"]";
        qs.setQuery(query, "CQP");
	assertFalse(qs.hasErrors());
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("NN", res.at("/query/wrap/key").asText());
        assertEquals("p", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

       /* query = "[base=\"schland/x\"]";
        qs.setQuery(query, "CQP");
		assertFalse(qs.hasErrors());
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals(".*?schland.*?", res.at("/query/wrap/key").asText());
        assertEquals("lemma", res.at("/query/wrap/layer").asText());
        assertEquals("type:regex", res.at("/query/wrap/type").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());*/
    }

	@Test
    public void testFailure () throws JsonProcessingException, IOException {
        query = "[base=Mann";
        qs.setQuery(query, "CQP");
	assertTrue(qs.hasErrors());
        res = mapper.readTree(qs.toJSON());
        assertEquals(302, res.at("/errors/0/0").asInt());
        assertEquals(302, res.at("/errors/1/0").asInt());
	}

    @Test
    public void testNegatedTokens () throws JsonProcessingException,
            IOException {
        query = "![p!=\"NN\"]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("NN", res.at("/query/wrap/key").asText());
        assertEquals("p", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
        assertEquals("", res.at("/errors/0/0").asText());
        assertEquals("", res.at("/errors/1/0").asText());
    }
    
    
    @Test
    public void testNegatedTerms () throws JsonProcessingException,
    IOException {
    
    	query = "[!(word=\"ido\"%c)]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("ido", res.at("/query/wrap/key").asText());
        assertEquals("word", res.at("/query/wrap/layer").asText());
        assertEquals("match:ne", res.at("/query/wrap/match").asText());
        assertEquals("", res.at("/errors/0/0").asText());
        assertEquals("", res.at("/errors/1/0").asText());
        
        query = "[(lemma=\"ir\") & !(word=\"ido\"%c | word=\"voy\"%c)];";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:termGroup", res.at("/query/wrap/@type").asText());
        operands = Lists.newArrayList(res.at("/query/wrap/operands").elements());
        assertEquals("koral:term", operands.get(0).at("/@type").asText());
        assertEquals("koral:termGroup", operands.get(1).at("/@type").asText());
        assertEquals("ir", operands.get(0).at("/key").asText());
        assertEquals("lemma", operands.get(0).at("/layer").asText());
        assertEquals("match:eq", operands.get(0).at("/match").asText());
        assertEquals("koral:term", operands.get(1).at("/operands/0/@type").asText());
		assertEquals("koral:term", operands.get(1).at("/operands/1/@type").asText());
		assertEquals("ido", operands.get(1).at("/operands/0/key").asText());
        assertEquals("word", operands.get(1).at("/operands/0/layer").asText());
        assertEquals("match:ne", operands.get(1).at("/operands/0/match").asText());
        assertEquals("voy", operands.get(1).at("/operands/1/key").asText());
        assertEquals("word", operands.get(1).at("/operands/1/layer").asText());
        assertEquals("match:ne", operands.get(1).at("/operands/1/match").asText());
       
    }


    @Test
    public void testValue () throws JsonProcessingException, IOException {
        query = "[mate/m='temp':'pres']";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("temp", res.at("/query/wrap/key").asText());
        assertEquals("pres", res.at("/query/wrap/value").asText());
        assertEquals("m", res.at("/query/wrap/layer").asText());
        assertEquals("mate", res.at("/query/wrap/foundry").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
    }

	@Test
    public void testFlags () throws JsonProcessingException, IOException {
        // why do i need an escape for ' ?
        query = "[mate/b='D\\'Ma \\\\nn'%l]";
        qs.setQuery(query, "CQP");
	res = mapper.readTree(qs.toJSON());
        assertEquals("D'Ma \\nn", res.at("/query/wrap/key").asText()); 
       
       
        query = "[mate/b=\"D'Ma\nn\"%l]";
        qs.setQuery(query, "CQP");
	//assertFalse(qs.hasErrors());
	res = mapper.readTree(qs.toJSON());  
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("D'Ma\nn", res.at("/query/wrap/key").asText());
        assertEquals("b", res.at("/query/wrap/layer").asText());
        assertEquals("mate", res.at("/query/wrap/foundry").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
        assertEquals("type:string", res.at("/query/wrap/type").asText());


        query = "[mate/b=\"D'Ma\\\\nn\"%l]";
        qs.setQuery(query, "CQP");
	//assertFalse(qs.hasErrors());
	res = mapper.readTree(qs.toJSON());  
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("D'Ma\\nn", res.at("/query/wrap/key").asText());
        assertEquals("b", res.at("/query/wrap/layer").asText());
        assertEquals("mate", res.at("/query/wrap/foundry").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
        assertEquals("type:string", res.at("/query/wrap/type").asText());


        query = "[mate/b=\"D'Ma\\\\nn\"]"; 
        qs.setQuery(query, "CQP");
	//assertFalse(qs.hasErrors());
	res = mapper.readTree(qs.toJSON());  
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("D'Ma\\\\nn", res.at("/query/wrap/key").asText());
        assertEquals("b", res.at("/query/wrap/layer").asText());
        assertEquals("mate", res.at("/query/wrap/foundry").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
        assertEquals("type:regex", res.at("/query/wrap/type").asText());

    

      
        query = "'http://racai.ro'%l";
        qs.setQuery(query, "CQP");
	res = mapper.readTree(qs.toJSON());
        assertEquals("http://racai.ro", res.at("/query/wrap/key").asText());
        assertEquals("type:string", res.at("/query/wrap/type").asText());
       
       
       
       
        query = "[mate/b=\"Der + Mann\"%l]";
        qs.setQuery(query, "CQP");
	assertFalse(qs.hasErrors());
	res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("Der + Mann", res.at("/query/wrap/key").asText());
        assertEquals("b", res.at("/query/wrap/layer").asText());
        assertEquals("mate", res.at("/query/wrap/foundry").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
        assertEquals("type:string", res.at("/query/wrap/type").asText());
        
        
        
        query = "\"Der + Mann\"%lcd";
        qs.setQuery(query, "CQP");
	assertFalse(qs.hasErrors());
	res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("Der + Mann", res.at("/query/wrap/key").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
        assertEquals("type:string", res.at("/query/wrap/type").asText());
        assertEquals("flags:caseInsensitive", res.at("/query/wrap/flags/0").asText());
        assertEquals("flags:diacriticsInsensitive", res.at("/query/wrap/flags/1").asText());

        query = "\"Der + Mann\"%cd";
        qs.setQuery(query, "CQP");
	assertFalse(qs.hasErrors());
	res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
        assertEquals("Der + Mann", res.at("/query/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/wrap/type").asText());
        assertEquals("flags:caseInsensitive", res.at("/query/wrap/flags/0").asText());
        assertEquals("flags:diacriticsInsensitive", res.at("/query/wrap/flags/1").asText());
        
      
		

	}


    // todo:
    @Test
    public void testRegex () throws JsonProcessingException, IOException {
        query = "[orth=\"M(a|ä)nn(er)?\"]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("M(a|ä)nn(er)?", res.at("/query/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/wrap/type").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "\".*?Mann.*?\"";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals(".*?Mann.*?", res.at("/query/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/wrap/type").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        // issue #56
        query = "„.*?Mann.*?“";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals(".*?Mann.*?", res.at("/query/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/wrap/type").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "“.*?Mann.*?”";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals(".*?Mann.*?", res.at("/query/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/wrap/type").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
        
        query = "\"z.B.\""; // /x is not implemented
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("z.B.", res.at("/query/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/wrap/type").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
    }

    @Test
    public void testRegexEscape () throws JsonProcessingException, IOException {
        // Escape regex symbols
    	// test doubling squoutes and dquoutes; not working now!

 
        
            query = "\"a\\.\"";
          qs.setQuery(query, "CQP");
          res = mapper.readTree(qs.toJSON());
          assertEquals("koral:token", res.at("/query/@type").asText());
          assertEquals("koral:term", res.at("/query/wrap/@type").asText());
          assertEquals("type:regex", res.at("/query/wrap/type").asText());
          assertEquals("orth", res.at("/query/wrap/layer").asText());
          assertEquals("match:eq", res.at("/query/wrap/match").asText());
          assertEquals("a\\.", res.at("/query/wrap/key").asText());
    	
    	      // escape doublequoutes and singlequoutes
          query = "'\"';";
          qs.setQuery(query, "CQP");
          res = mapper.readTree(qs.toJSON());
          assertEquals("koral:token", res.at("/query/@type").asText());
          assertEquals("koral:term", res.at("/query/wrap/@type").asText());
          assertEquals("type:regex", res.at("/query/wrap/type").asText());
          assertEquals("orth", res.at("/query/wrap/layer").asText());
          assertEquals("match:eq", res.at("/query/wrap/match").asText());
          assertEquals("\"", res.at("/query/wrap/key").asText());

    	query = "'copil\"';";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("type:regex", res.at("/query/wrap/type").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
        assertEquals("copil\"", res.at("/query/wrap/key").asText());

        query = "\"copil'\";";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("type:regex", res.at("/query/wrap/type").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
        assertEquals("copil'", res.at("/query/wrap/key").asText());
       
        query = "\"copil\"\"\";";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("type:regex", res.at("/query/wrap/type").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
        assertEquals("copil\"", res.at("/query/wrap/key").asText());
        
        query = "'copil''';";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("type:regex", res.at("/query/wrap/type").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
        assertEquals("copil'", res.at("/query/wrap/key").asText());

        query = "\"22\\\"-inch\";";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("type:regex", res.at("/query/wrap/type").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
        assertEquals("22\\\"-inch", res.at("/query/wrap/key").asText());
        
        
        // pe aici am ramas! lamureste te cu ghilimelele! 
        
    	query = "'a''.+?';";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("type:regex", res.at("/query/wrap/type").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
        assertEquals("a'.+?", res.at("/query/wrap/key").asText());
  
    	
    	
    	
    	query = "\"a\"\".+?\";";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("type:regex", res.at("/query/wrap/type").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
        assertEquals("a\".+?", res.at("/query/wrap/key").asText());
        
    
    	
        query = "\"a.+?\"";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("type:regex", res.at("/query/wrap/type").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
        assertEquals("a.+?", res.at("/query/wrap/key").asText());

        query = "\"a\\.\"";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("type:regex", res.at("/query/wrap/type").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
        assertEquals("a\\.", res.at("/query/wrap/key").asText());

        query = "\"a\\.\\+\\?\\\\\"";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("type:regex", res.at("/query/wrap/type").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
        assertEquals("a\\.\\+\\?\\\\", res.at("/query/wrap/key").asText());
    }

    @Test
    public void testRegexWhiteSpace () throws JsonProcessingException, IOException {
        // Escape regex symbols
        query = "\"a b\"";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("type:regex", res.at("/query/wrap/type").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
        assertEquals("a b", res.at("/query/wrap/key").asText());
    }


    @Test
    public void testPunct () throws JsonProcessingException, IOException {
        query = "[punct=\".\"]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals(".", res.at("/query/wrap/key").asText());
        assertEquals("type:punct", res.at("/query/wrap/type").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "[punct=\"\\.\"]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("\\.", res.at("/query/wrap/key").asText());
        assertEquals("type:punct", res.at("/query/wrap/type").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
    }


    @Test
    public void testCaseSensitivityFlag () throws JsonProcessingException,
            IOException {
        query = "[orth=\"deutscher\" %c]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("deutscher", res.at("/query/wrap/key").asText());
        assertTrue(res.at("/query/wrap/flags:caseInsensitive")
                .isMissingNode());
        assertEquals("flags:caseInsensitive", res.at("/query/wrap/flags/0")
                .asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "\"deutscher\"%d";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("deutscher", res.at("/query/wrap/key").asText());
        assertEquals("flags:diacriticsInsensitive", res.at("/query/wrap/flags/0")
                .asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "\"deutscher\"%cd";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("deutscher", res.at("/query/wrap/key").asText());
        assertFalse(res.at("/query/wrap/flags/0").isMissingNode());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
        assertEquals("flags:caseInsensitive",
                res.at("/query/wrap/flags/0").asText());
        assertEquals("flags:diacriticsInsensitive",
                res.at("/query/wrap/flags/1").asText());

        query = "[orth=\"deutscher\"%c][orth=\"Bundestag\"]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals("koral:token", operands.get(0).at("/@type").asText());
        assertEquals("deutscher", operands.get(0).at("/wrap/key").asText());
        assertEquals("orth", operands.get(0).at("/wrap/layer").asText());
        assertEquals("match:eq", operands.get(0).at("/wrap/match").asText());
        assertEquals("flags:caseInsensitive",
                operands.get(0).at("/wrap/flags/0").asText());
        assertEquals("koral:token", operands.get(1).at("/@type").asText());
        assertEquals("Bundestag", operands.get(1).at("/wrap/key").asText());
        assertEquals("orth", operands.get(1).at("/wrap/layer").asText());
        assertEquals("match:eq", operands.get(1).at("/wrap/match").asText());
        assertTrue(operands.get(1).at("/wrap/flags/0").isMissingNode());

        query = "\"deutscher\"%lc \"Bundestag\"";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals("koral:token", operands.get(0).at("/@type").asText());
        assertEquals("deutscher", operands.get(0).at("/wrap/key").asText());
        assertEquals("orth", operands.get(0).at("/wrap/layer").asText());
        assertEquals("match:eq", operands.get(0).at("/wrap/match").asText());
        assertEquals("flags:caseInsensitive",
                operands.get(0).at("/wrap/flags/0").asText());
        assertEquals("koral:token", operands.get(1).at("/@type").asText());
        assertEquals("Bundestag", operands.get(1).at("/wrap/key").asText());
        assertEquals("orth", operands.get(1).at("/wrap/layer").asText());
        assertEquals("match:eq", operands.get(1).at("/wrap/match").asText());
        assertTrue(operands.get(1).at("/wrap/flags:caseInsensitive")
                .isMissingNode());
    }


    @Test
    public void testSpans () throws JsonProcessingException, IOException {
   	
        query="/region[<s (class=\"header\")]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        
        query =  "/region[<cnx/c!=vp (class!=\"header\" & id=\"7\")>]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("vp", res.at("/query/wrap/key").asText());
        assertEquals("cnx", res.at("/query/wrap/foundry").asText());
        assertEquals("c", res.at("/query/wrap/layer").asText());
        assertEquals("match:ne", res.at("/query/wrap/match").asText());
        assertEquals("koral:termGroup", res.at("/query/attr/@type").asText());
        assertEquals("relation:and", res.at("/query/attr/relation").asText());
        operands = Lists
                .newArrayList(res.at("/query/attr/operands").elements());
        assertEquals("koral:term", operands.get(0).at("/@type").asText());
        assertEquals("class", operands.get(0).at("/key").asText());
        assertEquals("header", operands.get(0).at("/value").asText());
        assertEquals("match:ne", operands.get(0).at("/match").asText());
        assertEquals("koral:term", operands.get(1).at("/@type").asText());
        assertEquals("id", operands.get(1).at("/key").asText());
        assertEquals(7, operands.get(1).at("/value").asInt());
        assertEquals("match:eq", operands.get(1).at("/match").asText());
        
        query=  "/region[<cnx/c=vp (class!=\"header\" & id=\"7\")>]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("vp", res.at("/query/wrap/key").asText());
        assertEquals("cnx", res.at("/query/wrap/foundry").asText());
        assertEquals("c", res.at("/query/wrap/layer").asText());
       // assertEquals("match:eq", res.at("/query/wrap/match").asText());
        assertEquals("koral:termGroup", res.at("/query/attr/@type").asText());
        assertEquals("relation:and", res.at("/query/attr/relation").asText());
        operands = Lists
                .newArrayList(res.at("/query/attr/operands").elements());
        assertEquals("koral:term", operands.get(0).at("/@type").asText());
        assertEquals("class", operands.get(0).at("/key").asText());
        assertEquals("header", operands.get(0).at("/value").asText());
        assertEquals("match:ne", operands.get(0).at("/match").asText());
        assertEquals("koral:term", operands.get(1).at("/@type").asText());
        assertEquals("id", operands.get(1).at("/key").asText());
        assertEquals(7, operands.get(1).at("/value").asInt());
        assertEquals("match:eq", operands.get(1).at("/match").asText());

       query="/region[<cnx/c=vp (class=\"header\")>]";

       qs.setQuery(query, "CQP");
       res = mapper.readTree(qs.toJSON());
       assertEquals("koral:span", res.at("/query/@type").asText());
       assertEquals("vp", res.at("/query/wrap/key").asText());
       assertEquals("cnx", res.at("/query/wrap/foundry").asText());
       assertEquals("c", res.at("/query/wrap/layer").asText());
       //assertEquals("match:ne", res.at("/query/wrap/match").asText());
       assertEquals("class", res.at("/query/attr/key").asText());
       assertEquals("header", res.at("/query/attr/value").asText());
       assertEquals("match:eq", res.at("/query/attr/match").asText());

        // matches all sentences
    	query = "/region[s]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("s", res.at("/query/wrap/key").asText());

        // matches all vps;
        query = "/region[<vp>]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("vp", res.at("/query/wrap/key").asText());


        query = "/region[<cnx/c=vp>]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("vp", res.at("/query/wrap/key").asText());
        assertEquals("cnx", res.at("/query/wrap/foundry").asText());
        assertEquals("c", res.at("/query/wrap/layer").asText());
     

        query = "/region[<cnx/c!=vp>]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("vp", res.at("/query/wrap/key").asText());
        assertEquals("cnx", res.at("/query/wrap/foundry").asText());
        assertEquals("c", res.at("/query/wrap/layer").asText());
        assertEquals("match:ne", res.at("/query/wrap/match").asText());
        
        
        query = "/region[<cnx/c!=vp class!=\"header\">]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("vp", res.at("/query/wrap/key").asText());
        assertEquals("cnx", res.at("/query/wrap/foundry").asText());
        assertEquals("c", res.at("/query/wrap/layer").asText());
        assertEquals("match:ne", res.at("/query/wrap/match").asText());
        assertEquals("class", res.at("/query/attr/key").asText());
        assertEquals("header", res.at("/query/attr/value").asText());
        assertEquals("match:ne", res.at("/query/attr/match").asText());


  

        query = "/region[<cnx/c!=vp !(class!=\"header\")>]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("vp", res.at("/query/wrap/key").asText());
        assertEquals("cnx", res.at("/query/wrap/foundry").asText());
        assertEquals("c", res.at("/query/wrap/layer").asText());
        assertEquals("match:ne", res.at("/query/wrap/match").asText());
        assertEquals("class", res.at("/query/attr/key").asText());
        assertEquals("header", res.at("/query/attr/value").asText());
        assertEquals("match:eq", res.at("/query/attr/match").asText());
        
        query = "/region[<cnx/c!=vp !(class=\"header\" & id=\"7\")>]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("vp", res.at("/query/wrap/key").asText());
        assertEquals("cnx", res.at("/query/wrap/foundry").asText());
        assertEquals("c", res.at("/query/wrap/layer").asText());
        assertEquals("match:ne", res.at("/query/wrap/match").asText());
        assertEquals("koral:termGroup", res.at("/query/attr/@type").asText());
        assertEquals("relation:and", res.at("/query/attr/relation").asText());
        operands = Lists
                .newArrayList(res.at("/query/attr/operands").elements());
        assertEquals("koral:term", operands.get(0).at("/@type").asText());
        assertEquals("class", operands.get(0).at("/key").asText());
        assertEquals("header", operands.get(0).at("/value").asText());
        assertEquals("match:ne", operands.get(0).at("/match").asText());
        assertEquals("koral:term", operands.get(1).at("/@type").asText());
        assertEquals("id", operands.get(1).at("/key").asText());
        assertEquals(7, operands.get(1).at("/value").asInt());
        assertEquals("match:ne", operands.get(1).at("/match").asText());

     
    }
    
    @Test
    public void testRepetition () throws JsonProcessingException, IOException {
        query = "'der'{3}";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:repetition", res.at("/query/operation")
                .asText());
        assertEquals("der", res.at("/query/operands/0/wrap/key").asText());
        assertEquals(3, res.at("/query/boundary/min").asInt());
        assertEquals(3, res.at("/query/boundary/max").asInt());

        query = "\"der\"{,3}";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals(0, res.at("/query/boundary/min").asInt());
        assertEquals(3, res.at("/query/boundary/max").asInt());

        query = "'der'{3,}";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals(3, res.at("/query/boundary/min").asInt());
        assertTrue(res.at("/query/boundary/max").isMissingNode());

        query = "\"der\"{3,7}";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals(3, res.at("/query/boundary/min").asInt());
        assertEquals(7, res.at("/query/boundary/max").asInt());

        query = "'der'*";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals(0, res.at("/query/boundary/min").asInt());
        assertTrue(res.at("/query/boundary/max").isMissingNode());

        query = "'der'+";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals(1, res.at("/query/boundary/min").asInt());
        assertTrue(res.at("/query/boundary/max").isMissingNode());
    };


    @Test
    public void testGroupRepetition () throws JsonProcessingException,
            IOException {
        
    	// this queries were written in PQ+ with contains()
    	query = "<s> []* (\"der\")* []* </s>";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("frames:isAround", res.at("/query/frames/0").asText());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals(0, res.at("/query/operands/1/operands/0/boundary/min").asInt());
        assertTrue(res.at("/query/operands/1/operands/0/boundary/max").isMissingNode());
        assertTrue(res.at("/query/operands/1/operands/2/").isMissingNode()); // []* are not taken into account as tokens;

        query = "<s> []* (\"der\"){3,} []* </s>";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals(3, res.at("/query/operands/1/boundary/min").asInt());
        assertTrue(res.at("/query/operands/1/boundary/max")
                .isMissingNode());

        query = "<s> []* (\"der\"){,3} []* </s>";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals(0, res.at("/query/operands/1/boundary/min").asInt());
        assertEquals(3, res.at("/query/operands/1/boundary/max").asInt());

        query = "<s> []* (\"der\"){3,7} []* </s>";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals(3, res.at("/query/operands/1/boundary/min").asInt());
        assertEquals(7, res.at("/query/operands/1/boundary/max").asInt());

        
    };


    

    @Test
    public void testCoordinatedFields () throws JsonProcessingException,
            IOException {
        query = "[base='Mann'&(cas='N'|cas='A')]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("relation:and", res.at("/query/wrap/relation").asText());
        assertEquals("Mann", res.at("/query/wrap/operands/0/key").asText());
        assertEquals("lemma", res.at("/query/wrap/operands/0/layer").asText());
        assertEquals("koral:termGroup", res.at("/query/wrap/operands/1/@type")
                .asText());
        assertEquals("relation:or", res.at("/query/wrap/operands/1/relation")
                .asText());
        assertEquals("N", res.at("/query/wrap/operands/1/operands/0/key")
                .asText());
        assertEquals("cas", res.at("/query/wrap/operands/1/operands/0/layer")
                .asText());
        assertEquals("A", res.at("/query/wrap/operands/1/operands/1/key")
                .asText());
        assertEquals("cas", res.at("/query/wrap/operands/1/operands/1/layer")
                .asText());

        query = "[base=\"Mann\"&cas=\"N\"&gen=\"m\"]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("relation:and", res.at("/query/wrap/relation").asText());
        assertEquals("Mann", res.at("/query/wrap/operands/0/key").asText());
        assertEquals("lemma", res.at("/query/wrap/operands/0/layer").asText());
        assertEquals("koral:termGroup", res.at("/query/wrap/operands/1/@type")
                .asText());
        assertEquals("relation:and", res.at("/query/wrap/operands/1/relation")
                .asText());
        assertEquals("N", res.at("/query/wrap/operands/1/operands/0/key")
                .asText());
        assertEquals("cas", res.at("/query/wrap/operands/1/operands/0/layer")
                .asText());
        assertEquals("m", res.at("/query/wrap/operands/1/operands/1/key")
                .asText());
        assertEquals("gen", res.at("/query/wrap/operands/1/operands/1/layer")
                .asText());

		query = "[(cas=\"N\"|cas=\"A\")&base='Mann']";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());		
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("relation:and", res.at("/query/wrap/relation").asText());
        assertEquals("koral:termGroup", res.at("/query/wrap/operands/0/@type")
                .asText());
        assertEquals("relation:or", res.at("/query/wrap/operands/0/relation")
                .asText());
        assertEquals("N", res.at("/query/wrap/operands/0/operands/0/key")
                .asText());
        assertEquals("cas", res.at("/query/wrap/operands/0/operands/0/layer")
                .asText());
        assertEquals("A", res.at("/query/wrap/operands/0/operands/1/key")
                .asText());
        assertEquals("cas", res.at("/query/wrap/operands/0/operands/1/layer")
                .asText());
		assertEquals("Mann", res.at("/query/wrap/operands/1/key").asText());
        assertEquals("lemma", res.at("/query/wrap/operands/1/layer").asText());
	}

	
    @Test
    public void testUnnecessaryParentheses () throws JsonProcessingException,
            IOException {
        query = "[(base='Mann')]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("Mann", res.at("/query/wrap/key").asText());
        assertEquals("lemma", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "[(((base='Mann')))]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("Mann", res.at("/query/wrap/key").asText());
        assertEquals("lemma", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "[(base=\"Mann\"&cas=\"N\")];";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:termGroup", res.at("/query/wrap/@type")
                .asText());
        assertEquals("relation:and", res.at("/query/wrap/relation")
                     .asText());
        assertEquals("Mann", res.at("/query/wrap/operands/0/key")
                     .asText());
        assertEquals("lemma", res.at("/query/wrap/operands/0/layer")
                     .asText());
        assertEquals("N", res.at("/query/wrap/operands/1/key")
                     .asText());
        assertEquals("cas", res.at("/query/wrap/operands/1/layer")
                     .asText());


        query = "[(((base='Mann'&cas='N')))]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:termGroup", res.at("/query/wrap/@type")
                .asText());
        assertEquals("relation:and", res.at("/query/wrap/relation")
                     .asText());
        assertEquals("Mann", res.at("/query/wrap/operands/0/key")
                     .asText());
        assertEquals("lemma", res.at("/query/wrap/operands/0/layer")
                     .asText());
        assertEquals("N", res.at("/query/wrap/operands/1/key")
                     .asText());
        assertEquals("cas", res.at("/query/wrap/operands/1/layer")
                     .asText());


        query = "[(((base='Mann'&((cas='N')))))]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:termGroup", res.at("/query/wrap/@type")
                .asText());
        assertEquals("relation:and", res.at("/query/wrap/relation")
                     .asText());
        assertEquals("Mann", res.at("/query/wrap/operands/0/key")
                     .asText());
        assertEquals("lemma", res.at("/query/wrap/operands/0/layer")
                     .asText());
        assertEquals("N", res.at("/query/wrap/operands/1/key")
                     .asText());
        assertEquals("cas", res.at("/query/wrap/operands/1/layer")
                     .asText());

		query = "[((cas='N'|cas='A'))&base='Mann']";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());		
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("relation:and", res.at("/query/wrap/relation").asText());
        assertEquals("koral:termGroup", res.at("/query/wrap/operands/0/@type")
                .asText());
        assertEquals("relation:or", res.at("/query/wrap/operands/0/relation")
                .asText());
        assertEquals("N", res.at("/query/wrap/operands/0/operands/0/key")
                .asText());
        assertEquals("cas", res.at("/query/wrap/operands/0/operands/0/layer")
                .asText());
        assertEquals("A", res.at("/query/wrap/operands/0/operands/1/key")
                .asText());
        assertEquals("cas", res.at("/query/wrap/operands/0/operands/1/layer")
                .asText());
		assertEquals("Mann", res.at("/query/wrap/operands/1/key").asText());
        assertEquals("lemma", res.at("/query/wrap/operands/1/layer").asText());
    };

    @Test
    public void testTokenSequence () throws JsonProcessingException,
            IOException {
        query = "[base=\"Mann\"][orth=\"Frau\"];";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("Mann", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("lemma", res.at("/query/operands/0/wrap/layer").asText());
        assertEquals("Frau", res.at("/query/operands/1/wrap/key").asText());
        assertEquals("orth", res.at("/query/operands/1/wrap/layer").asText());

        query = "[base=\"Mann\"][orth=\"Frau\"][p=\"NN\"];";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("NN", res.at("/query/operands/2/wrap/key").asText());
        assertEquals("p", res.at("/query/operands/2/wrap/layer").asText());

        query = "[base=\"Mann\"][orth=\"Frau\"][p=\"NN\"][foo=\"bar\"];";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("bar", res.at("/query/operands/3/wrap/key").asText());
        assertEquals("foo", res.at("/query/operands/3/wrap/layer").asText());
    }


    @Test
    public void testDisjSegments () throws JsonProcessingException, IOException {
        query = "[base=\"der\"]|[base=\"das\"]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:disjunction", res.at("/query/operation")
                .asText());
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:token", res.at("/query/operands/1/@type").asText());
        assertEquals("der", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("lemma", res.at("/query/operands/0/wrap/layer").asText());
        assertEquals("das", res.at("/query/operands/1/wrap/key").asText());
        assertEquals("lemma", res.at("/query/operands/1/wrap/layer").asText());

        query = "([base='der']|[base='das'])[base='Schild']";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("Schild", res.at("/query/operands/1/wrap/key").asText());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:disjunction",
                res.at("/query/operands/0/operation").asText());

        query = "[base='Schild']([base='der']|[base='das'])";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("Schild", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:disjunction",
                res.at("/query/operands/1/operation").asText());

        query = "([orth=\"der\"][base=\"katze\"])|([orth=\"eine\"][base=\"baum\"])";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:disjunction", res.at("/query/operation")
                .asText());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/0/operation").asText());
        assertEquals("koral:token", res
                .at("/query/operands/0/operands/0/@type").asText());
        assertEquals("der", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("katze", res.at("/query/operands/0/operands/1/wrap/key")
                .asText());
        assertEquals("eine", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("baum", res.at("/query/operands/1/operands/1/wrap/key")
                .asText());

        query = "[orth='der'][base=\"katze\"]|[orth='eine'][base=\"baum\"]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:disjunction", res.at("/query/operation")
                .asText());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/0/operation").asText());
        assertEquals("koral:token", res
                .at("/query/operands/0/operands/0/@type").asText());
        assertEquals("der", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("katze", res.at("/query/operands/0/operands/1/wrap/key")
                .asText());
        assertEquals("eine", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("baum", res.at("/query/operands/1/operands/1/wrap/key")
                .asText());

        query = "[orth='der']([base='katze']|[orth='eine'])[base='baum']";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:disjunction",
                res.at("/query/operands/1/operation").asText());
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:token", res.at("/query/operands/2/@type").asText());
        assertEquals("der", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("katze", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("eine", res.at("/query/operands/1/operands/1/wrap/key")
                .asText());
        assertEquals("baum", res.at("/query/operands/2/wrap/key").asText());

        query = "[orth='der'][base='katze']|[orth='der'][base='hund']|[orth='der'][base='baum']";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("der", res.at("/query/operands/2/operands/0/wrap/key")
                .asText());
        assertEquals("baum", res.at("/query/operands/2/operands/1/wrap/key")
                .asText());

        query = "[orth='der']([base='katze']|[base='hund']|[base='baum'])";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:disjunction",
                res.at("/query/operands/1/operation").asText());
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:token", res
                .at("/query/operands/1/operands/0/@type").asText());
        assertEquals("koral:token", res
                .at("/query/operands/1/operands/1/@type").asText());
        assertEquals("koral:token", res
                .at("/query/operands/1/operands/2/@type").asText());
        assertEquals("katze", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("hund", res.at("/query/operands/1/operands/1/wrap/key")
                .asText());
        assertEquals("baum", res.at("/query/operands/1/operands/2/wrap/key")
                .asText());
    }


    @Test
    public void testTokenSpanSequence () throws JsonProcessingException,
            IOException {
    	// vezi ca asta e de la modificarea cu span-ul de ieri, cand ai schimbat ordinea in operators list!
        query = "[base='Mann'] /region[vp]"; // in PQ+ "[base=Mann]<vp>"
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("Mann", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:span", res.at("/query/operands/1/@type").asText());
        assertEquals("vp", res.at("/query/operands/1/wrap/key").asText());

        query = "/region[<coreNLP/c=NP>] [base='Mann']"; // region with foundry and layer
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("NP", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("coreNLP", res.at("/query/operands/0/wrap/foundry").asText());
        assertEquals("c", res.at("/query/operands/0/wrap/layer").asText());
        assertEquals("koral:token", res.at("/query/operands/1/@type").asText());
        assertEquals("Mann", res.at("/query/operands/1/wrap/key").asText());
        

        query = "/region[vp] [base=\"Mann\"] /region[pp] /region[np]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("pp", res.at("/query/operands/2/wrap/key").asText());
        assertEquals("np", res.at("/query/operands/3/wrap/key").asText());
       
    }


    @Test
    public void testClasses () throws JsonProcessingException, IOException {
        
    	query = "@[base='Mann']";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:class", res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/classOut").asInt());
        assertTrue(res.at("/query/classIn").isMissingNode());
        assertEquals("Mann", res.at("/query/operands/0/wrap/key").asText());
    	
        query = "@([base='Mann'][orth='Frau'])";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:class", res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/classOut").asInt());
        assertTrue(res.at("/query/classIn").isMissingNode());
        assertEquals("Mann", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("Frau", res.at("/query/operands/0/operands/1/wrap/key")
                .asText());

        
        query = "@1(@[tt/p='ADJA'][mate/p='NN'])";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:class", res.at("/query/operation").asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/0/operation").asText());
        assertEquals(2, res.at("/query/classOut").asInt());
        assertEquals("operation:class", res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals(1, res.at("/query/operands/0/operands/0/classOut").asInt());
        assertEquals(2, res.at("/meta/highlight/0").asInt());
        assertEquals(1, res.at("/meta/highlight/1").asInt());
    	

        query = "@[base='Mann']@1[orth='Frau']";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("operation:class", res.at("/query/operands/0/operation")
                .asText());
        assertEquals(1, res.at("/query/operands/0/classOut").asInt());
        assertEquals("operation:class", res.at("/query/operands/1/operation")
                .asText());
        assertEquals(2, res.at("/query/operands/1/classOut").asInt());
        assertTrue(res.at("/query/classIn").isMissingNode());
        assertEquals("Mann", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("Frau", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals(1, res.at("/meta/highlight/0").asInt());
        assertEquals(2, res.at("/meta/highlight/1").asInt());
        
        
        query = "[p='NN']@([base='Mann'][orth='Frau'])";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/1/operation")
                .asText());
        assertEquals(1, res.at("/query/operands/1/classOut").asInt());
        assertTrue(res.at("/query/operands/1/classIn").isMissingNode());
        assertEquals("Mann",
                res.at("/query/operands/1/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals("Frau",
                res.at("/query/operands/1/operands/0/operands/1/wrap/key")
                        .asText());

        query = "@([base='Mann'][orth='Frau'])[p='NN']";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/0/operation")
                .asText());
        assertEquals(1, res.at("/query/operands/0/classOut").asInt());
        assertTrue(res.at("/query/operands/0/classIn").isMissingNode());
        assertEquals("Mann",
                res.at("/query/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals("Frau",
                res.at("/query/operands/0/operands/0/operands/1/wrap/key")
                        .asText());
        assertEquals(1, res.at("/meta/highlight/0").asInt());
       
 
    }

    @Ignore
    @Test
    public void testFocusSplit () throws JsonProcessingException, IOException {
        query = "focus([orth='Der']{[orth='Mann']})";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/classRef/0").asInt());
        assertEquals("operation:sequence", res
                .at("/query/operands/0/operation").asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals(1, res.at("/query/operands/0/operands/1/classOut").asInt());
        assertEquals("Mann",
                res.at("/query/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals(1, res.at("/meta/highlight/0").asInt());

        query = "focus([orth='Der']{[orth='Mann'][orth='geht']})";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("operation:sequence",
                res.at("/query/operands/0/operands/1/operands/0/operation")
                        .asText());
        assertEquals(
                "Mann",
                res.at("/query/operands/0/operands/1/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(
                "geht",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/wrap/key")
                        .asText());

        query = "focus(2:[orth=Der]{2:[orth=Mann][orth=geht]})";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals(2, res.at("/query/classRef/0").asInt());
        assertEquals(2, res.at("/query/operands/0/operands/1/classOut").asInt());
        assertEquals(2, res.at("/meta/highlight/0").asInt());

        query = "focus(3:startswith(<s>,{3:<np>}))";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals(3, res.at("/query/classRef/0").asInt());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals("operation:position", res
                .at("/query/operands/0/operation").asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals(3, res.at("/query/operands/0/operands/1/classOut").asInt());
        assertEquals("frames:startsWith", res.at("/query/operands/0/frames/0")
                .asText());
        assertEquals(3, res.at("/meta/highlight/0").asInt());

        query = "focus(1000:startswith(<s>,{1000:<np>}))";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals(1000, res.at("/query/classRef/0").asInt());
        assertEquals(1000, res.at("/query/operands/0/operands/1/classOut")
                .asInt());
        assertEquals(1000, res.at("/meta/highlight/0").asInt());

        query = "focus(3: startswith(<s>, {3:[base=der]{1:[mate/p=ADJA]{2:[tt/p=NN]}}}))";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals(3, res.at("/query/classRef/0").asInt());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals("operation:position", res
                .at("/query/operands/0/operation").asText());
        assertEquals("frames:startsWith", res.at("/query/operands/0/frames/0")
                .asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals(3, res.at("/query/operands/0/operands/1/classOut").asInt());
        assertEquals("operation:sequence",
                res.at("/query/operands/0/operands/1/operands/0/operation")
                        .asText());
        assertEquals(
                "operation:class",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operation")
                        .asText());
        assertEquals(
                1,
                res.at("/query/operands/0/operands/1/operands/0/operands/1/classOut")
                        .asInt());
        assertEquals(
                "operation:sequence",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/0/operation")
                        .asText());
        assertEquals(
                "operation:class",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/0/operands/1/operation")
                        .asText());
        assertEquals(
                2,
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/0/operands/1/classOut")
                        .asInt());

        query = "split(3: startswith(<s>, {3:[base=der]{1:[mate/p=ADJA]{2:[tt/p=NN]}}}))";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals(3, res.at("/query/classRef/0").asInt());
        assertTrue(res.at("/query/classRef/1").isMissingNode());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:split", res.at("/query/operation").asText());

        query = "split(2|3: startswith(<s>, {3:[base=der]{1:[mate/p=ADJA]{2:[tt/p=NN]}}}))";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals(2, res.at("/query/classRef/0").asInt());
        assertEquals(3, res.at("/query/classRef/1").asInt());
        assertEquals("classRefOp:intersection", res.at("/query/classRefOp")
                .asText());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:split", res.at("/query/operation").asText());
        assertEquals(3, res.at("/meta/highlight/0").asInt());
        assertEquals(1, res.at("/meta/highlight/1").asInt());
        assertEquals(2, res.at("/meta/highlight/2").asInt());

        query = "focus(1:{[base=der]}{1:[pos=ADJA]})";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals(1, res.at("/query/classRef/0").asInt());
        assertEquals(1, res.at("/query/operands/0/operands/0/classOut").asInt());
        assertEquals(1, res.at("/query/operands/0/operands/1/classOut").asInt());
    }

    
    @Test
    public void testPositionsSpan () throws JsonProcessingException, IOException {
      
        query = "<base/s=s> []* \"copil\" [] \"cuminte\" []* </base/s=s>";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("frames:isAround", res.at("/query/frames/0").asText());
        assertEquals("frames:startsWith", res.at("/query/frames/1").asText());
        assertEquals("frames:endsWith", res.at("/query/frames/2").asText());
        assertEquals("frames:matches", res.at("/query/frames/3").asText());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operands/1/operation").asText());
        assertEquals("koral:token", res.at("/query/operands/1/operands/0/@type").asText());
        // first element in sequence is "copil", not empty token
        assertEquals("copil", res.at("/query/operands/1/operands/0/wrap/key").asText());
        assertEquals("koral:token", res.at("/query/operands/1/operands/1/@type").asText());
        assertTrue(res.at("/query/operands/1/operands/1/wrap/key").isMissingNode());
        assertEquals("koral:token", res.at("/query/operands/1/operands/2/@type").asText());
        assertEquals("cuminte", res.at("/query/operands/1/operands/2/wrap/key").asText());
        //last element in sequence is "copil", not empty token
        assertTrue(res.at("/query/operands/1/operands/3/@type").isMissingNode());

        
        query = "<base/s=s> []* \"copil\" []* </base/s=s>;";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("frames:isAround", res.at("/query/frames/0").asText());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:token", res.at("/query/operands/1/@type").asText());
        assertEquals("copil", res.at("/query/operands/1/wrap/key").asText());
        
        query ="\"copil\" <base/s=s>;"; //KoralFrame.ENDS_WITH
    	qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
    	assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("frames:endsWith", res.at("/query/frames/0").asText());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("base", res.at("/query/operands/0/wrap/foundry").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/layer").asText());
        assertEquals("koral:token", res.at("/query/operands/1/@type").asText());
        assertEquals("copil", res.at("/query/operands/1/wrap/key").asText());	
        
        
    	query ="\"copil\" \"cuminte\" <base/s=s>;"; //KoralFrame.ENDS_WITH
    	qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
    	assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("frames:endsWith", res.at("/query/frames/0").asText());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("base", res.at("/query/operands/0/wrap/foundry").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/layer").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operands/1/operation").asText());
        assertEquals("koral:token", res.at("/query/operands/1/operands/0/@type").asText());
        assertEquals("copil", res.at("/query/operands/1/operands/0/wrap/key").asText());	
        assertEquals("koral:token", res.at("/query/operands/1/operands/1/@type").asText());
        assertEquals("cuminte", res.at("/query/operands/1/operands/1/wrap/key").asText());
       
        query =" <base/s=s> \"copil\" ;"; //KoralFrame.STARTS_WITH
   	 	qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("frames:startsWith", res.at("/query/frames/0").asText());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("base", res.at("/query/operands/0/wrap/foundry").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/layer").asText());
        assertEquals("koral:token", res.at("/query/operands/1/@type").asText());
        assertEquals("copil", res.at("/query/operands/1/wrap/key").asText());
    	
        query =" <base/s=s> \"copil\" </base/s=s>;";  // KoralFrame.MATCHES
   	    qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
   	    assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("frames:matches", res.at("/query/frames/0").asText());
        assertTrue(res.at("/query/frames/1").isMissingNode());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("base", res.at("/query/operands/0/wrap/foundry").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/layer").asText());
        assertEquals("koral:token", res.at("/query/operands/1/@type").asText());
        assertEquals("copil", res.at("/query/operands/1/wrap/key").asText());
    	
    
        
        query = "<base/s=s> []* \"copil\" \"cuminte\" []* </base/s=s>;";
        // a sequence with two tokens is generated in the serialization tree; 
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("frames:isAround", res.at("/query/frames/0").asText());
        assertEquals("frames:startsWith", res.at("/query/frames/1").asText());
        assertEquals("frames:endsWith", res.at("/query/frames/2").asText());
        assertEquals("frames:matches", res.at("/query/frames/3").asText());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operands/1/operation").asText());
    	
    	
    	query = "<np> []* ([pos=\"JJ.*\"] []*){3,} </np>;"; //KoralFrame.MATCHES
    	qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
    	assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("frames:matches", res.at("/query/frames/0").asText());
        assertTrue(res.at("/query/frames/1").isMissingNode());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("np", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:token", res.at("/query/operands/1/operands/0/operands/0/@type").asText());
        assertEquals(0, res.at("/query/operands/1/operands/0/boundary/min").asInt());
        assertEquals("operation:repetition", res.at("/query/operands/1/operands/0/operation").asText());
        assertEquals("koral:group", res.at("/query/operands/1/operands/1/@type").asText());
        assertEquals("operation:repetition", res.at("/query/operands/1/operands/1/operation").asText());
        assertEquals("operation:sequence", res.at("/query/operands/1/operands/1/operands/0/operation").asText());
        assertEquals("JJ.*", res.at("/query/operands/1/operands/1/operands/0/operands/0/wrap/key").asText());
    	
    	query =" <np> []* ([pos=\"JJ.*\"]){3,} []* </np>; #contains (NP, sequence)"; //KoralFrame.IS_AROUND
    	// the []* segments are not serialesd;
    	qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
    	assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("frames:isAround", res.at("/query/frames/0").asText());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("np", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:token", res.at("/query/operands/1/operands/0/@type").asText());
        assertEquals("JJ.*", res.at("/query/operands/1/operands/0/wrap/key").asText());
        assertEquals(3, res.at("/query/operands/1/boundary/min").asInt());
        assertEquals("operation:repetition", res.at("/query/operands/1/operation").asText());
    	
    }
    
    @Test
    public void testPositionsOps () throws JsonProcessingException, IOException {
    	//aici am ramas!! vezi ca nu parseaza bine!!
    	query ="[\"copil\"  & rbound(<base/s=s>)];"; 
   	 	qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("frames:endsWith", res.at("/query/frames/0").asText());
        assertEquals("frames:matches", res.at("/query/frames/1").asText());
        //assertTrue(res.at("/query/frames/1").isMissingNode());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("base", res.at("/query/operands/0/wrap/foundry").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/layer").asText());
        assertEquals("koral:token", res.at("/query/operands/1/@type").asText());
        assertEquals("copil", res.at("/query/operands/1/wrap/key").asText());
    	
       
    	query ="[(base=\"copil\") & lbound(<base/s=s>)];"; 
    	qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
    	assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("frames:startsWith", res.at("/query/frames/0").asText());
        assertEquals("frames:matches", res.at("/query/frames/1").asText());
        //assertTrue(res.at("/query/frames/1").isMissingNode());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("base", res.at("/query/operands/0/wrap/foundry").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/layer").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:token", res.at("/query/operands/1/@type").asText());
        assertEquals("copil", res.at("/query/operands/1/wrap/key").asText());
        assertEquals("lemma", res.at("/query/operands/1/wrap/layer").asText());
    	

        query ="[word = \"acest\"][\"copil\" & rbound(<base/s=s>)];"; 
    	   qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("acest", res.at("/query/operands/0/wrap/key").asText());	
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:position", res.at("/query/operands/1/operation").asText());
        assertEquals("frames:endsWith", res.at("/query/operands/1/frames/0").asText());
        assertEquals("frames:matches", res.at("/query/operands/1/frames/1").asText());
        //assertTrue(res.at("/query/frames/1").isMissingNode());
        assertEquals("koral:span", res.at("/query/operands/1/operands/0/@type").asText());
        assertEquals("base", res.at("/query/operands/1/operands/0/wrap/foundry").asText());
        assertEquals("s", res.at("/query/operands/1/operands/0/wrap/layer").asText());
        assertEquals("s", res.at("/query/operands/1/operands/0/wrap/key").asText());
        assertEquals("koral:token", res.at("/query/operands/1/operands/1/@type").asText());
        assertEquals("copil", res.at("/query/operands/1/operands/1/wrap/key").asText());	
     	
    

       
    }
    	
    	
 /*      @Test	
    	
       public void testcontains () throws JsonProcessingException, IOException {
    	
 

        query = "contains(<s>,<np>)";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("np", res.at("/query/operands/1/wrap/key").asText());

        query = "contains(<s>,[orth=der][orth=Mann])";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("frames:isAround", res.at("/query/frames/0").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/1/operation").asText());
        assertEquals("der", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("Mann", res.at("/query/operands/1/operands/1/wrap/key")
                .asText());

        query = "contains(<s>,[orth=der][orth=Mann]*)";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("frames:isAround", res.at("/query/frames/0").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/1/operation").asText());
        assertEquals("der", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("operation:repetition",
                res.at("/query/operands/1/operands/1/operation").asText());
        assertEquals(0, res.at("/query/operands/1/operands/1/boundary/min")
                .asInt());
        assertTrue(res.at("/query/operands/1/operands/1/boundary/max")
                .isMissingNode());
        assertEquals("Mann",
                res.at("/query/operands/1/operands/1/operands/0/wrap/key")
                        .asText());

        query = "contains(<s>,startswith(<np>,<pp>))";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("frames:startsWith", res.at("/query/operands/1/frames/0")
                .asText());
        assertEquals("operation:position", res
                .at("/query/operands/1/operation").asText());
        assertEquals("np", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("pp", res.at("/query/operands/1/operands/1/wrap/key")
                .asText());

        query = "[base=Auto]overlaps(<s>, der)";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:position", res
                .at("/query/operands/1/operation").asText());
        assertEquals("frames:overlapsLeft", res
                .at("/query/operands/1/frames/0").asText());
        assertEquals("frames:overlapsRight",
                res.at("/query/operands/1/frames/1").asText());
        assertEquals("koral:span", res.at("/query/operands/1/operands/0/@type")
                .asText());
        assertEquals("s", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("koral:token", res
                .at("/query/operands/1/operands/1/@type").asText());

        query = "[base=Auto]            overlaps(<s>, der)";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:position", res
                .at("/query/operands/1/operation").asText());
        assertEquals("frames:overlapsLeft", res
                .at("/query/operands/1/frames/0").asText());
        assertEquals("frames:overlapsRight",
                res.at("/query/operands/1/frames/1").asText());
        assertEquals("koral:span", res.at("/query/operands/1/operands/0/@type")
                .asText());
        assertEquals("s", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("koral:token", res
                .at("/query/operands/1/operands/1/@type").asText());
    };

    
    
    @Test
    public void testSubmatch () throws JsonProcessingException, IOException {
        query = "submatch(1:<s>)";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/spanRef/0").asInt());
        assertTrue(res.at("/query/spanRef/1").isMissingNode());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());

        query = "submatch(1,4:<s>)";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/spanRef/0").asInt());
        assertEquals(4, res.at("/query/spanRef/1").asInt());

        query = "submatch(1,4:contains(<s>,[base=Haus]))";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/spanRef/0").asInt());
        assertEquals(4, res.at("/query/spanRef/1").asInt());
        assertEquals("frames:isAround", res.at("/query/operands/0/frames/0")
                .asText());
        assertEquals("s", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("Haus", res.at("/query/operands/0/operands/1/wrap/key")
                .asText());
    }
*/

    @Ignore
    @Test
    public void testRelations () throws JsonProcessingException, IOException {
        query = "dominates(<s>,<np>)";  // span s dominates span np. ; 
        // possible equivalent: <s>[]*<np>[]*</np>[]*</s>; or using within? -- operation relation is different;
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:relation", res.at("/query/operation").asText());
        assertEquals("koral:relation", res.at("/query/relation/@type").asText());
        assertEquals("c", res.at("/query/relation/wrap/layer").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("np", res.at("/query/operands/1/wrap/key").asText());

        query = "relatesTo([base=Baum],<np>)"; // Baum dominates np;
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:relation", res.at("/query/operation").asText());
        assertEquals("koral:relation", res.at("/query/relation/@type").asText());
        assertEquals("Baum", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("np", res.at("/query/operands/1/wrap/key").asText());

        query = "relatesTo(Baum,<np>)";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("orth", res.at("/query/operands/0/wrap/layer").asText());
        assertEquals("Baum", res.at("/query/operands/0/wrap/key").asText());

        query = "relatesTo(mate/d=HEAD:<np>,[base=Baum])";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("lemma", res.at("/query/operands/1/wrap/layer").asText());
        assertEquals("Baum", res.at("/query/operands/1/wrap/key").asText());
        assertEquals("koral:relation", res.at("/query/relation/@type").asText());
        assertEquals("mate", res.at("/query/relation/wrap/foundry").asText());
        assertEquals("d", res.at("/query/relation/wrap/layer").asText());
        assertEquals("HEAD", res.at("/query/relation/wrap/key").asText());

        query = "dependency([base=fällen],[base=Baum])";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("lemma", res.at("/query/operands/0/wrap/layer").asText());
        assertEquals("fällen", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("lemma", res.at("/query/operands/1/wrap/layer").asText());
        assertEquals("Baum", res.at("/query/operands/1/wrap/key").asText());
        assertEquals("koral:relation", res.at("/query/relation/@type").asText());
        assertEquals("d", res.at("/query/relation/wrap/layer").asText());

        query = "dominates(Baum,<np>)";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("orth", res.at("/query/operands/0/wrap/layer").asText());
        assertEquals("Baum", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:relation", res.at("/query/relation/@type").asText());
        assertEquals("c", res.at("/query/relation/wrap/layer").asText());

        query = "dominates(cnx/c:<vp>,<np>)";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("cnx", res.at("/query/relation/wrap/foundry").asText());
        assertEquals("c", res.at("/query/relation/wrap/layer").asText());

        query = "dominates(cnx/c*:<vp>,<np>)";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("cnx", res.at("/query/relation/wrap/foundry").asText());
        assertEquals("c", res.at("/query/relation/wrap/layer").asText());
        assertEquals(0, res.at("/query/relation/boundary/min").asInt());
        assertTrue(res.at("/query/relation/boundary/max")
                .isMissingNode());

        query = "dominates(cnx/c{1,5}:<vp>,<np>)";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals(1, res.at("/query/relation/boundary/min").asInt());
        assertEquals(5, res.at("/query/relation/boundary/max").asInt());

        query = "dominates(cnx/c{,5}:<vp>,<np>)";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals(0, res.at("/query/relation/boundary/min").asInt());
        assertEquals(5, res.at("/query/relation/boundary/max").asInt());

        query = "dominates(cnx/c{5}:<vp>,<np>)";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals(5, res.at("/query/relation/boundary/min").asInt());
        assertEquals(5, res.at("/query/relation/boundary/max").asInt());
    }


    @Test
    public void testAlign () throws JsonProcessingException, IOException {
        query = "[orth=\"der\"]^[orth=\"Mann\"]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("der", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals(1, res.at("/query/operands/0/classOut").asInt());
        assertEquals("Mann", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("operation:class", res.at("/query/operands/1/operation")
                .asText());
        assertEquals(2, res.at("/query/operands/1/classOut").asInt());
        assertEquals(1, res.at("/meta/alignment/0/0").asInt());
        assertEquals(2, res.at("/meta/alignment/0/1").asInt());

        query = "[orth='der']^[orth='große'][orth='Mann']";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("der", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals(1, res.at("/query/operands/0/classOut").asInt());
        assertEquals("große", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("operation:class", res.at("/query/operands/1/operation")
                .asText());
        assertEquals(2, res.at("/query/operands/1/classOut").asInt());
        assertEquals("Mann", res.at("/query/operands/2/wrap/key").asText());
        assertEquals(1, res.at("/meta/alignment/0/0").asInt());
        assertEquals(2, res.at("/meta/alignment/0/1").asInt());


        query = "([base=\"a\"]^[base=\"b\"])|[base=\"c\"]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("operation:disjunction", res.at("/query/operation")
                .asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/0/operation").asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals("a",
                res.at("/query/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals("b",
                res.at("/query/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals("c", res.at("/query/operands/1/wrap/key").asText());
        assertEquals(1, res.at("/query/operands/0/operands/0/classOut").asInt());
        assertEquals(2, res.at("/query/operands/0/operands/1/classOut").asInt());
        assertEquals(1, res.at("/meta/alignment/0/0").asInt());
        assertEquals(2, res.at("/meta/alignment/0/1").asInt());

        query = "([base='a']^[base='b'][base='c'])|[base='d']";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("a",
                res.at("/query/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals("b",
                res.at("/query/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals("c", res.at("/query/operands/0/operands/2/wrap/key")
                .asText());
        assertEquals("d", res.at("/query/operands/1/wrap/key").asText());

        query = "([base='a']^[base='b']^[base='c'])|[base='d']";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("a",
                res.at("/query/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(1, res.at("/query/operands/0/operands/0/classOut").asInt());
        assertEquals("b",
                res.at("/query/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals(2, res.at("/query/operands/0/operands/1/classOut").asInt());
        assertEquals("c",
                res.at("/query/operands/0/operands/2/operands/0/wrap/key")
                        .asText());
        assertEquals(3, res.at("/query/operands/0/operands/2/classOut").asInt());
        assertEquals("d", res.at("/query/operands/1/wrap/key").asText());
        assertEquals(1, res.at("/meta/alignment/0/0").asInt());
        assertEquals(2, res.at("/meta/alignment/0/1").asInt());
        assertEquals(2, res.at("/meta/alignment/1/0").asInt());
        assertEquals(3, res.at("/meta/alignment/1/1").asInt());

        query = "^ 'Mann'";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("Mann", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("operation:class", res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/classOut").asInt());
        assertEquals(-1, res.at("/meta/alignment/0/0").asInt());
        assertEquals(1, res.at("/meta/alignment/0/1").asInt());

        query = "'Mann' ^";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("Mann", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("operation:class", res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/classOut").asInt());
        assertEquals(1, res.at("/meta/alignment/0/0").asInt());
        assertEquals(-1, res.at("/meta/alignment/0/1").asInt());
    }


    @Test
    public void testSimpleQueries () throws JsonProcessingException,
            IOException {
        query = "\"Baum\"";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("Baum", res.at("/query/wrap/key").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "'Der' 'Baum'";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:term", res.at("/query/operands/0/wrap/@type")
                .asText());
        assertEquals("Der", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("Baum", res.at("/query/operands/1/wrap/key").asText());
        assertEquals("orth", res.at("/query/operands/0/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/operands/0/wrap/match")
                .asText());
        assertEquals("orth", res.at("/query/operands/1/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/operands/1/wrap/match")
                .asText());

        query = "\"Der\" \"große\" \"Baum\"";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("Der", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("große", res.at("/query/operands/1/wrap/key").asText());
        assertEquals("Baum", res.at("/query/operands/2/wrap/key").asText());

        query = "\"Der\" ('große'|'kleine') 'Baum'";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("Der", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("operation:disjunction",
                res.at("/query/operands/1/operation").asText());
        assertEquals("große", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("kleine", res.at("/query/operands/1/operands/1/wrap/key")
                .asText());
        assertEquals("Baum", res.at("/query/operands/2/wrap/key").asText());

        query = "'der' 'große' 'Baum' | 'der' 'kleine' 'Baum'";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("operation:disjunction", res.at("/query/operation")
                .asText());
        assertEquals("der", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("große", res.at("/query/operands/0/operands/1/wrap/key")
                .asText());
        assertEquals("Baum", res.at("/query/operands/0/operands/2/wrap/key")
                .asText());
        assertEquals("der", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("kleine", res.at("/query/operands/1/operands/1/wrap/key")
                .asText());
        assertEquals("Baum", res.at("/query/operands/1/operands/2/wrap/key")
                .asText());

        query = "\"Der\" [p=\"ADJA\"] \"Baum\"";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("Der", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("ADJA", res.at("/query/operands/1/wrap/key").asText());
        assertEquals("p", res.at("/query/operands/1/wrap/layer").asText());
        assertEquals("Baum", res.at("/query/operands/2/wrap/key").asText());
    }


    @Test
    public void testWithin () throws JsonProcessingException, IOException {
        
        query= "[pos=\"NN\"] []* [pos=\"NN\"] within np;";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("frames:isAround", res.at("/query/frames/0").asText());
        assertEquals("np", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("NN", res.at("/query/operands/1/operands/0/wrap/key").asText());
        assertEquals("operation:repetition", res.at("/query/operands/1/operands/1/operation").asText());
        assertEquals("NN", res.at("/query/operands/1/operands/2/wrap/key").asText());
        assertEquals("operation:sequence", res.at("/query/operands/1/operation").asText());
    	
    	query = "[p='VVFIN'] within s";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("frames:isAround", res.at("/query/frames/0").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
     
        assertEquals("VVFIN", res.at("/query/operands/1/wrap/key").asText());
        
        
    }
    
//    @Test
//    public void testWithinElement () throws JsonProcessingException, IOException {
//        query = "[p=VVFIN] within <base/s=s>";
//        qs.setQuery(query, "CQP");
//        res = mapper.readTree(qs.toJSON());
//        System.out.println(res);
//        assertEquals("operation:position", res.at("/query/operation").asText());
//        assertEquals("frames:isAround", res.at("/query/frames/0").asText());
//        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
//        assertEquals("VVFIN", res.at("/query/operands/1/wrap/key").asText());
//    }


    @Test
    public void testSpanSerialization () throws JsonProcessingException,
            IOException {

        // Both constructs should be serialized identically when the problem with []* is solved!
        query = "<s> []* 'der' []* </s>)";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("frames:isAround", res.at("/query/frames/0").asText());
     //   assertTrue(res.at("/query/frames/1").isMissingNode());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:token", res.at("/query/operands/1/@type").asText());

        query = "'der' within s";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("frames:isAround", res.at("/query/frames/0").asText());
 //       assertTrue(res.at("/query/frames/1").isMissingNode());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:token", res.at("/query/operands/1/@type").asText());
    }

    @Ignore
    @Test
    public void testQueryReferences () throws JsonProcessingException, IOException {
        query = "{#test}";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:queryRef", res.at("/query/@type").asText());
        assertEquals("test", res.at("/query/ref").asText());

        query = "{#admin/example}";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:queryRef", res.at("/query/@type").asText());
        assertEquals("admin/example", res.at("/query/ref").asText());

        query = "Der {#admin/example} [orth=Baum]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());

        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:queryRef", res.at("/query/operands/1/@type").asText());
        assertEquals("admin/example", res.at("/query/operands/1/ref").asText());
        assertEquals("koral:token", res.at("/query/operands/2/@type").asText());

        query = "[orth=Der]{#admin/example}{1,}[orth=Baum]";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());

        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("koral:queryRef", res.at("/query/operands/1/operands/0/@type").asText());
        assertEquals("admin/example", res.at("/query/operands/1/operands/0/ref").asText());
        assertEquals("koral:token", res.at("/query/operands/2/@type").asText());
    }

    @Test
    public void testMeta () throws JsonProcessingException, IOException {
        query = "'x' meta textClass='Sport'";
        qs.setQuery(query, "CQP");
        res = mapper.readTree(qs.toJSON());
        assertEquals("x", res.at("/query/wrap/key").asText());
        assertEquals("koral:doc", res.at("/collection/@type").asText());
        assertEquals("textClass", res.at("/collection/key").asText());
        assertEquals("Sport", res.at("/collection/value").asText());

        query = "\"x\" meta textClass='Sport'";
        qs.setQuery(query, "CQP");
        qs.setCollection("author=Smith");
        res = mapper.readTree(qs.toJSON());
        assertEquals("x", res.at("/query/wrap/key").asText());
        assertEquals("koral:docGroup", res.at("/collection/@type").asText());
        assertEquals("operation:and", res.at("/collection/operation").asText());
        assertEquals("textClass", res.at("/collection/operands/0/key").asText());
        assertEquals("Sport", res.at("/collection/operands/0/value").asText());
        assertEquals("author", res.at("/collection/operands/1/key").asText());
        assertEquals("Smith", res.at("/collection/operands/1/value").asText());

        // TODO more tests
    }
   
 
    }
