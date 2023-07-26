package de.ids_mannheim.korap.query.test.cqp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

//import de.ids_mannheim.korap.query.object.KoralFrame;
import de.ids_mannheim.korap.query.serialize.QuerySerializer;
import de.ids_mannheim.korap.query.test.BaseQueryTest;

/**
 * Tests for JSON-LD serialization of CQP queries.
 * 
 * @author Joachim Bingel (bingel@ids-mannheim.de)
 * @author Elena Irimia (elena@racai.ro) based on PoliqarpPlusQueryProcessorTest
 * @author Eliza Margaretha 
 */
public class CQPQueryProcessorTest extends BaseQueryTest {

    private String query;
    private ArrayList<JsonNode> operands;

    private QuerySerializer qs = new QuerySerializer();
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode result;

    public CQPQueryProcessorTest () {
        super("CQP");
    }

    @Test
    public void testUnknownLayer () throws IOException {
        result = runQuery("[mate/b=\"Baum\"]");
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("Baum", result.at("/query/wrap/key").asText());
        assertEquals("b", result.at("/query/wrap/layer").asText());
        
        result = runQuery("[mate/bi='Baum']");
        assertEquals("bi", result.at("/query/wrap/layer").asText());
    }

    @Test
    public void testLayerWithFlag () throws IOException {
        result = runQuery("[base='Baum'%c]");
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("flags:caseInsensitive", result.at("/query/wrap/flags/0").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("Baum", result.at("/query/wrap/key").asText());
        assertEquals("lemma", result.at("/query/wrap/layer").asText());

        result = runQuery("[mate/x=\"Baum\"%c]");   query = "[mate/x=Baum/i]";
        assertEquals("mate", result.at("/query/wrap/foundry").asText());
        assertEquals("x", result.at("/query/wrap/layer").asText());
        assertEquals("flags:caseInsensitive", result.at("/query/wrap/flags/0").asText());

        result = runQuery("[mate/x=\"\"%c]");
        assertEquals("mate", result.at("/query/wrap/foundry").asText());
        assertEquals("x", result.at("/query/wrap/layer").asText());
        assertEquals("flags:caseInsensitive", result.at("/query/wrap/flags/0").asText());
    }

    
    @Test
    public void testContext () throws JsonProcessingException {
        String contextString = "http://korap.ids-mannheim.de/ns/koral/0.3/context.jsonld";
        result = runQuery("\"foo\"");
        assertEquals(contextString, result.get("@context").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("foo", result.at("/query/wrap/key").asText());
    }

    @Test
    public void testUnbalancedBrackets () throws JsonProcessingException {
        query = "[base=Mann";
        qs.setQuery(query, "CQP");
        assertTrue(qs.hasErrors());
        result = mapper.readTree(qs.toJSON());
        assertEquals(302, result.at("/errors/0/0").asInt());
        assertEquals(302, result.at("/errors/1/0").asInt());
	}


    @Test
    public void testSingleTokens () throws JsonProcessingException {
        query = "[base=\"Mann\"]";
        qs.setQuery(query, "CQP");
        assertFalse(qs.hasErrors());
        result = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("Mann", result.at("/query/wrap/key").asText());
        assertEquals("lemma", result.at("/query/wrap/layer").asText());
    }
    

    @Test
    public void testSingleTokenNegation () throws JsonProcessingException {

        query = "[!base=\"Mann\"]";
        qs.setQuery(query, "CQP");
		assertFalse(qs.hasErrors());
        result = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("Mann", result.at("/query/wrap/key").asText());
        assertEquals("lemma", result.at("/query/wrap/layer").asText());
        assertEquals("match:ne", result.at("/query/wrap/match").asText());
        
        query = "![base=\"Mann\"]";
        qs.setQuery(query, "CQP");
		assertFalse(qs.hasErrors());
        result = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("Mann", result.at("/query/wrap/key").asText());
        assertEquals("lemma", result.at("/query/wrap/layer").asText());
        assertEquals("match:ne", result.at("/query/wrap/match").asText());

        query = "[orth!=\"Frau\"]";
        qs.setQuery(query, "CQP");
        assertFalse(qs.hasErrors());
        result = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("Frau", result.at("/query/wrap/key").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:ne", result.at("/query/wrap/match").asText());

        query = "[p!=\"NN\"]";
        qs.setQuery(query, "CQP");
        assertFalse(qs.hasErrors());
        result = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("NN", result.at("/query/wrap/key").asText());
        assertEquals("p", result.at("/query/wrap/layer").asText());
        assertEquals("match:ne", result.at("/query/wrap/match").asText());
    }
    
    @Test
    public void testSingleTokenDoubleNegation () throws JsonProcessingException {
        query = "[!p!=\"NN\"]";
        qs.setQuery(query, "CQP");
        assertFalse(qs.hasErrors());
        result = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("NN", result.at("/query/wrap/key").asText());
        assertEquals("p", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());

        query = "![p!=\"NN\"]";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertTrue(result.at("/errors").isMissingNode());
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("NN", result.at("/query/wrap/key").asText());
        assertEquals("p", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
    }

    @Test
    public void testNegatedTerms () throws JsonProcessingException{
    	query = "[!(word=\"ido\"%c)]";
        result = runQuery(query);
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("match:ne", result.at("/query/wrap/match").asText());
        assertEquals("flags:caseInsensitive", result.at("/query/wrap/flags/0").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("ido", result.at("/query/wrap/key").asText());
        assertEquals("word", result.at("/query/wrap/layer").asText());
    }
    

    @Test
    public void testNegatedConjunctionAndDisjunction ()
            throws JsonProcessingException {

        // negating conjunction and disjunction testing
        // relation or shoud become and because of negation! 
        // see De Morgan's laws;  implemented correctly in PQ+ too...
        query = "[(lemma=\"ir\") & !(word=\"ido\"%c | word=\"voy\"%c)];";
        result = runQuery(query);
        assertEquals("koral:termGroup",
                result.at("/query/wrap/@type").asText());
        assertEquals("relation:and",
                result.at("/query/wrap/relation").asText());
        operands = Lists
                .newArrayList(result.at("/query/wrap/operands").elements());
        assertEquals("koral:term", operands.get(0).at("/@type").asText());
        assertEquals("ir", operands.get(0).at("/key").asText());
        assertEquals("lemma", operands.get(0).at("/layer").asText());
        assertEquals("match:eq", operands.get(0).at("/match").asText());
        
        assertEquals("koral:termGroup", operands.get(1).at("/@type").asText());
        assertEquals("relation:and", operands.get(1).at("/relation").asText());
        
        assertEquals("koral:term",
                operands.get(1).at("/operands/0/@type").asText());
        assertEquals("koral:term",
                operands.get(1).at("/operands/1/@type").asText());
        assertEquals("ido", operands.get(1).at("/operands/0/key").asText());
        assertEquals("word", operands.get(1).at("/operands/0/layer").asText());
        assertEquals("match:ne",
                operands.get(1).at("/operands/0/match").asText());
        
        assertEquals("voy", operands.get(1).at("/operands/1/key").asText());
        assertEquals("word", operands.get(1).at("/operands/1/layer").asText());
        assertEquals("match:ne",
                operands.get(1).at("/operands/1/match").asText());
    }
    
    @Test
    public void testNegatedConjunctionAndDisjunction2 ()
            throws JsonProcessingException {
        // !(a&!b) = !a | !!b = !a | b
        query = "![(lemma=\"ir\") & !(word=\"ido\"%c | word=\"voy\"%c)];";
        result = runQuery(query);

        assertEquals("koral:termGroup",
                result.at("/query/wrap/@type").asText());
        assertEquals("relation:or", result.at("/query/wrap/relation").asText());
        operands = Lists
                .newArrayList(result.at("/query/wrap/operands").elements());
        assertEquals("koral:term", operands.get(0).at("/@type").asText());
        assertEquals("ir", operands.get(0).at("/key").asText());
        assertEquals("lemma", operands.get(0).at("/layer").asText());
        assertEquals("match:ne", operands.get(0).at("/match").asText());
        
        assertEquals("koral:termGroup", operands.get(1).at("/@type").asText());
        assertEquals("relation:or", operands.get(1).at("/relation").asText());
        
        assertEquals("koral:term",
                operands.get(1).at("/operands/0/@type").asText());
        assertEquals("koral:term",
                operands.get(1).at("/operands/1/@type").asText());
        assertEquals("ido", operands.get(1).at("/operands/0/key").asText());
        assertEquals("word", operands.get(1).at("/operands/0/layer").asText());
        assertEquals("match:eq",
                operands.get(1).at("/operands/0/match").asText());
        assertEquals("voy", operands.get(1).at("/operands/1/key").asText());
        assertEquals("word", operands.get(1).at("/operands/1/layer").asText());
        assertEquals("match:eq",
                operands.get(1).at("/operands/1/match").asText());

    }


    @Test
    public void testValue () throws JsonProcessingException, IOException {
        query = "[mate/m='temp':'pres']";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("temp", result.at("/query/wrap/key").asText());
        assertEquals("pres", result.at("/query/wrap/value").asText());
        assertEquals("m", result.at("/query/wrap/layer").asText());
        assertEquals("mate", result.at("/query/wrap/foundry").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());

        query = "[mate/m=\"number\":\"pl\"]";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("number", result.at("/query/wrap/key").asText());
        assertEquals("pl", result.at("/query/wrap/value").asText());
        assertEquals("m", result.at("/query/wrap/layer").asText());
        assertEquals("mate", result.at("/query/wrap/foundry").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
    }

    @Test
    public void testPunct () throws JsonProcessingException, IOException {
        query = "[punct=\".\"]";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals(".", result.at("/query/wrap/key").asText());
        assertEquals("type:punct", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());

        query = "[punct=\"\\.\"]";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("\\.", result.at("/query/wrap/key").asText());
        assertEquals("type:punct", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
    }

   
    @Test
    public void testCoordinatedFields () throws JsonProcessingException,
            IOException {
        query = "[base='Mann'&(cas='N'|cas='A')]";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("relation:and", result.at("/query/wrap/relation").asText());
        assertEquals("Mann", result.at("/query/wrap/operands/0/key").asText());
        assertEquals("lemma", result.at("/query/wrap/operands/0/layer").asText());
        assertEquals("koral:termGroup", result.at("/query/wrap/operands/1/@type")
                .asText());
        assertEquals("relation:or", result.at("/query/wrap/operands/1/relation")
                .asText());
        assertEquals("N", result.at("/query/wrap/operands/1/operands/0/key")
                .asText());
        assertEquals("cas", result.at("/query/wrap/operands/1/operands/0/layer")
                .asText());
        assertEquals("A", result.at("/query/wrap/operands/1/operands/1/key")
                .asText());
        assertEquals("cas", result.at("/query/wrap/operands/1/operands/1/layer")
                .asText());

        query = "[base=\"Mann\"&cas=\"N\"&gen=\"m\"]";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("relation:and", result.at("/query/wrap/relation").asText());
        assertEquals("Mann", result.at("/query/wrap/operands/0/key").asText());
        assertEquals("lemma", result.at("/query/wrap/operands/0/layer").asText());
        assertEquals("koral:termGroup", result.at("/query/wrap/operands/1/@type")
                .asText());
        assertEquals("relation:and", result.at("/query/wrap/operands/1/relation")
                .asText());
        assertEquals("N", result.at("/query/wrap/operands/1/operands/0/key")
                .asText());
        assertEquals("cas", result.at("/query/wrap/operands/1/operands/0/layer")
                .asText());
        assertEquals("m", result.at("/query/wrap/operands/1/operands/1/key")
                .asText());
        assertEquals("gen", result.at("/query/wrap/operands/1/operands/1/layer")
                .asText());

		query = "[(cas=\"N\"|cas=\"A\")&base='Mann']";
		result = runQuery(query);	
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("relation:and", result.at("/query/wrap/relation").asText());
        assertEquals("koral:termGroup", result.at("/query/wrap/operands/0/@type")
                .asText());
        assertEquals("relation:or", result.at("/query/wrap/operands/0/relation")
                .asText());
        assertEquals("N", result.at("/query/wrap/operands/0/operands/0/key")
                .asText());
        assertEquals("cas", result.at("/query/wrap/operands/0/operands/0/layer")
                .asText());
        assertEquals("A", result.at("/query/wrap/operands/0/operands/1/key")
                .asText());
        assertEquals("cas", result.at("/query/wrap/operands/0/operands/1/layer")
                .asText());
		assertEquals("Mann", result.at("/query/wrap/operands/1/key").asText());
        assertEquals("lemma", result.at("/query/wrap/operands/1/layer").asText());
	}

	
    @Test
    public void testUnnecessaryParentheses () throws JsonProcessingException,
            IOException {
        query = "[(base='Mann')]";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("Mann", result.at("/query/wrap/key").asText());
        assertEquals("lemma", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());

        query = "[(((base='Mann')))]";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("Mann", result.at("/query/wrap/key").asText());
        assertEquals("lemma", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());

        query = "[(base=\"Mann\"&cas=\"N\")];";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:termGroup", result.at("/query/wrap/@type")
                .asText());
        assertEquals("relation:and", result.at("/query/wrap/relation")
                     .asText());
        assertEquals("Mann", result.at("/query/wrap/operands/0/key")
                     .asText());
        assertEquals("lemma", result.at("/query/wrap/operands/0/layer")
                     .asText());
        assertEquals("N", result.at("/query/wrap/operands/1/key")
                     .asText());
        assertEquals("cas", result.at("/query/wrap/operands/1/layer")
                     .asText());


        query = "[(((base='Mann'&cas='N')))]";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:termGroup", result.at("/query/wrap/@type")
                .asText());
        assertEquals("relation:and", result.at("/query/wrap/relation")
                     .asText());
        assertEquals("Mann", result.at("/query/wrap/operands/0/key")
                     .asText());
        assertEquals("lemma", result.at("/query/wrap/operands/0/layer")
                     .asText());
        assertEquals("N", result.at("/query/wrap/operands/1/key")
                     .asText());
        assertEquals("cas", result.at("/query/wrap/operands/1/layer")
                     .asText());


        query = "[(((base='Mann'&((cas='N')))))]";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:termGroup", result.at("/query/wrap/@type")
                .asText());
        assertEquals("relation:and", result.at("/query/wrap/relation")
                     .asText());
        assertEquals("Mann", result.at("/query/wrap/operands/0/key")
                     .asText());
        assertEquals("lemma", result.at("/query/wrap/operands/0/layer")
                     .asText());
        assertEquals("N", result.at("/query/wrap/operands/1/key")
                     .asText());
        assertEquals("cas", result.at("/query/wrap/operands/1/layer")
                     .asText());

		query = "[((cas='N'|cas='A'))&base='Mann']";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());		
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("relation:and", result.at("/query/wrap/relation").asText());
        assertEquals("koral:termGroup", result.at("/query/wrap/operands/0/@type")
                .asText());
        assertEquals("relation:or", result.at("/query/wrap/operands/0/relation")
                .asText());
        assertEquals("N", result.at("/query/wrap/operands/0/operands/0/key")
                .asText());
        assertEquals("cas", result.at("/query/wrap/operands/0/operands/0/layer")
                .asText());
        assertEquals("A", result.at("/query/wrap/operands/0/operands/1/key")
                .asText());
        assertEquals("cas", result.at("/query/wrap/operands/0/operands/1/layer")
                .asText());
		assertEquals("Mann", result.at("/query/wrap/operands/1/key").asText());
        assertEquals("lemma", result.at("/query/wrap/operands/1/layer").asText());
    };

    @Test
    public void testTokenSequence () throws JsonProcessingException,
            IOException {
        query = "[base=\"Mann\"][orth=\"Frau\"];";
        result = runQuery(query);
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:sequence", result.at("/query/operation").asText());
        assertEquals("Mann", result.at("/query/operands/0/wrap/key").asText());
        assertEquals("lemma", result.at("/query/operands/0/wrap/layer").asText());
        assertEquals("Frau", result.at("/query/operands/1/wrap/key").asText());
        assertEquals("orth", result.at("/query/operands/1/wrap/layer").asText());

        query = "[base=\"Mann\"][orth=\"Frau\"][p=\"NN\"];";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("NN", result.at("/query/operands/2/wrap/key").asText());
        assertEquals("p", result.at("/query/operands/2/wrap/layer").asText());

        query = "[base=\"Mann\"][orth=\"Frau\"][p=\"NN\"][foo=\"bar\"];";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("bar", result.at("/query/operands/3/wrap/key").asText());
        assertEquals("foo", result.at("/query/operands/3/wrap/layer").asText());
    }


    @Test
    public void testDisjSegments () throws JsonProcessingException, IOException {
        query = "[base=\"der\"]|[base=\"das\"]";
        result = runQuery(query);
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:disjunction", result.at("/query/operation")
                .asText());
        assertEquals("koral:token", result.at("/query/operands/0/@type").asText());
        assertEquals("koral:token", result.at("/query/operands/1/@type").asText());
        assertEquals("der", result.at("/query/operands/0/wrap/key").asText());
        assertEquals("lemma", result.at("/query/operands/0/wrap/layer").asText());
        assertEquals("das", result.at("/query/operands/1/wrap/key").asText());
        assertEquals("lemma", result.at("/query/operands/1/wrap/layer").asText());

        query = "([base='der']|[base='das'])[base='Schild']";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:sequence", result.at("/query/operation").asText());
        assertEquals("Schild", result.at("/query/operands/1/wrap/key").asText());
        assertEquals("koral:group", result.at("/query/operands/0/@type").asText());
        assertEquals("operation:disjunction",
                result.at("/query/operands/0/operation").asText());

        query = "[base='Schild']([base='der']|[base='das'])";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("Schild", result.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:group", result.at("/query/operands/1/@type").asText());
        assertEquals("operation:disjunction",
                result.at("/query/operands/1/operation").asText());

        query = "([orth=\"der\"][base=\"katze\"])|([orth=\"eine\"][base=\"baum\"])";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:disjunction", result.at("/query/operation")
                .asText());
        assertEquals("koral:group", result.at("/query/operands/0/@type").asText());
        assertEquals("operation:sequence", result
                .at("/query/operands/0/operation").asText());
        assertEquals("koral:token", result
                .at("/query/operands/0/operands/0/@type").asText());
        assertEquals("der", result.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("katze", result.at("/query/operands/0/operands/1/wrap/key")
                .asText());
        assertEquals("eine", result.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("baum", result.at("/query/operands/1/operands/1/wrap/key")
                .asText());

        query = "[orth='der'][base=\"katze\"]|[orth='eine'][base=\"baum\"]";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:disjunction", result.at("/query/operation")
                .asText());
        assertEquals("koral:group", result.at("/query/operands/0/@type").asText());
        assertEquals("operation:sequence", result
                .at("/query/operands/0/operation").asText());
        assertEquals("koral:token", result
                .at("/query/operands/0/operands/0/@type").asText());
        assertEquals("der", result.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("katze", result.at("/query/operands/0/operands/1/wrap/key")
                .asText());
        assertEquals("eine", result.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("baum", result.at("/query/operands/1/operands/1/wrap/key")
                .asText());

        query = "[orth='der']([base='katze']|[orth='eine'])[base='baum']";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:sequence", result.at("/query/operation").asText());
        assertEquals("koral:group", result.at("/query/operands/1/@type").asText());
        assertEquals("operation:disjunction",
                result.at("/query/operands/1/operation").asText());
        assertEquals("koral:token", result.at("/query/operands/0/@type").asText());
        assertEquals("koral:token", result.at("/query/operands/2/@type").asText());
        assertEquals("der", result.at("/query/operands/0/wrap/key").asText());
        assertEquals("katze", result.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("eine", result.at("/query/operands/1/operands/1/wrap/key")
                .asText());
        assertEquals("baum", result.at("/query/operands/2/wrap/key").asText());

        query = "[orth='der'][base='katze']|[orth='der'][base='hund']|[orth='der'][base='baum']";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("der", result.at("/query/operands/2/operands/0/wrap/key")
                .asText());
        assertEquals("baum", result.at("/query/operands/2/operands/1/wrap/key")
                .asText());

        query = "[orth='der']([base='katze']|[base='hund']|[base='baum'])";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:sequence", result.at("/query/operation").asText());
        assertEquals("koral:group", result.at("/query/operands/1/@type").asText());
        assertEquals("operation:disjunction",
                result.at("/query/operands/1/operation").asText());
        assertEquals("koral:token", result.at("/query/operands/0/@type").asText());
        assertEquals("koral:token", result
                .at("/query/operands/1/operands/0/@type").asText());
        assertEquals("koral:token", result
                .at("/query/operands/1/operands/1/@type").asText());
        assertEquals("koral:token", result
                .at("/query/operands/1/operands/2/@type").asText());
        assertEquals("katze", result.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("hund", result.at("/query/operands/1/operands/1/wrap/key")
                .asText());
        assertEquals("baum", result.at("/query/operands/1/operands/2/wrap/key")
                .asText());
    }


    @Test
    public void testClasses () throws JsonProcessingException, IOException {
        // equivalent with  query = "{[base=Mann]}" in PQ+
    	query = "@[base='Mann']";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:class", result.at("/query/operation").asText());
        assertEquals(1, result.at("/query/classOut").asInt());
        assertTrue(result.at("/query/classIn").isMissingNode());
        assertEquals("Mann", result.at("/query/operands/0/wrap/key").asText());
    	
        query = "@([base='Mann'][orth='Frau'])";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:class", result.at("/query/operation").asText());
        assertEquals(1, result.at("/query/classOut").asInt());
        assertTrue(result.at("/query/classIn").isMissingNode());
        assertEquals("Mann", result.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("Frau", result.at("/query/operands/0/operands/1/wrap/key")
                .asText());

        
        query = "@1(@[tt/p='ADJA'][mate/p='NN'])";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:class", result.at("/query/operation").asText());
        assertEquals("operation:sequence", result
                .at("/query/operands/0/operation").asText());
        assertEquals(2, result.at("/query/classOut").asInt());
        assertEquals("operation:class", result.at("/query/operands/0/operands/0/operation").asText());
        assertEquals(1, result.at("/query/operands/0/operands/0/classOut").asInt());
        assertEquals(2, result.at("/meta/highlight/0").asInt());
        assertEquals(1, result.at("/meta/highlight/1").asInt());
    	

        query = "@[base='Mann']@1[orth='Frau']";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:sequence", result.at("/query/operation").asText());
        assertEquals("operation:class", result.at("/query/operands/0/operation")
                .asText());
        assertEquals(1, result.at("/query/operands/0/classOut").asInt());
        assertEquals("operation:class", result.at("/query/operands/1/operation")
                .asText());
        assertEquals(2, result.at("/query/operands/1/classOut").asInt());
        assertTrue(result.at("/query/classIn").isMissingNode());
        assertEquals("Mann", result.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("Frau", result.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals(1, result.at("/meta/highlight/0").asInt());
        assertEquals(2, result.at("/meta/highlight/1").asInt());
        
        
        query = "[p='NN']@([base='Mann'][orth='Frau'])";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:sequence", result.at("/query/operation").asText());
        assertEquals("koral:group", result.at("/query/operands/1/@type").asText());
        assertEquals("operation:class", result.at("/query/operands/1/operation")
                .asText());
        assertEquals(1, result.at("/query/operands/1/classOut").asInt());
        assertTrue(result.at("/query/operands/1/classIn").isMissingNode());
        assertEquals("Mann",
                result.at("/query/operands/1/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals("Frau",
                result.at("/query/operands/1/operands/0/operands/1/wrap/key")
                        .asText());

        query = "@([base='Mann'][orth='Frau'])[p='NN']";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:sequence", result.at("/query/operation").asText());
        assertEquals("koral:group", result.at("/query/operands/0/@type").asText());
        assertEquals("operation:class", result.at("/query/operands/0/operation")
                .asText());
        assertEquals(1, result.at("/query/operands/0/classOut").asInt());
        assertTrue(result.at("/query/operands/0/classIn").isMissingNode());
        assertEquals("Mann",
                result.at("/query/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals("Frau",
                result.at("/query/operands/0/operands/0/operands/1/wrap/key")
                        .asText());
        assertEquals(1, result.at("/meta/highlight/0").asInt());
       
 
    }

    

    @Ignore // CQP doesn't support relational queries like that
    @Test
    public void testRelations () throws JsonProcessingException, IOException {
        query = "dominates(<s>,<np>)";  // span s dominates span np. ; 
        // possible equivalent: <s>[]*<np>[]*</np>[]*</s>; or using within? -- operation relation is different;
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:relation", result.at("/query/operation").asText());
        assertEquals("koral:relation", result.at("/query/relation/@type").asText());
        assertEquals("c", result.at("/query/relation/wrap/layer").asText());
        assertEquals("s", result.at("/query/operands/0/wrap/key").asText());
        assertEquals("np", result.at("/query/operands/1/wrap/key").asText());

        query = "relatesTo([base=\"Baum\"],<np>)"; // Baum dominates np;
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:relation", result.at("/query/operation").asText());
        assertEquals("koral:relation", result.at("/query/relation/@type").asText());
        assertEquals("Baum", result.at("/query/operands/0/wrap/key").asText());
        assertEquals("np", result.at("/query/operands/1/wrap/key").asText());

        query = "relatesTo(\"Baum\",<np>)";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("orth", result.at("/query/operands/0/wrap/layer").asText());
        assertEquals("Baum", result.at("/query/operands/0/wrap/key").asText());

        query = "relatesTo(mate/d=HEAD:<np>,[base=\"Baum\"])";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("lemma", result.at("/query/operands/1/wrap/layer").asText());
        assertEquals("Baum", result.at("/query/operands/1/wrap/key").asText());
        assertEquals("koral:relation", result.at("/query/relation/@type").asText());
        assertEquals("mate", result.at("/query/relation/wrap/foundry").asText());
        assertEquals("d", result.at("/query/relation/wrap/layer").asText());
        assertEquals("HEAD", result.at("/query/relation/wrap/key").asText());

        query = "dependency([base=\"fällen\"],[base=\"Baum\"])";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("lemma", result.at("/query/operands/0/wrap/layer").asText());
        assertEquals("fällen", result.at("/query/operands/0/wrap/key").asText());
        assertEquals("lemma", result.at("/query/operands/1/wrap/layer").asText());
        assertEquals("Baum", result.at("/query/operands/1/wrap/key").asText());
        assertEquals("koral:relation", result.at("/query/relation/@type").asText());
        assertEquals("d", result.at("/query/relation/wrap/layer").asText());

        query = "dominates(\"Baum\",<np>)";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("orth", result.at("/query/operands/0/wrap/layer").asText());
        assertEquals("Baum", result.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:relation", result.at("/query/relation/@type").asText());
        assertEquals("c", result.at("/query/relation/wrap/layer").asText());

        query = "dominates(cnx/c:<vp>,<np>)";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("cnx", result.at("/query/relation/wrap/foundry").asText());
        assertEquals("c", result.at("/query/relation/wrap/layer").asText());

        query = "dominates(cnx/c*:<vp>,<np>)";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("cnx", result.at("/query/relation/wrap/foundry").asText());
        assertEquals("c", result.at("/query/relation/wrap/layer").asText());
        assertEquals(0, result.at("/query/relation/boundary/min").asInt());
        assertTrue(result.at("/query/relation/boundary/max")
                .isMissingNode());

        query = "dominates(cnx/c{1,5}:<vp>,<np>)";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals(1, result.at("/query/relation/boundary/min").asInt());
        assertEquals(5, result.at("/query/relation/boundary/max").asInt());

        query = "dominates(cnx/c{,5}:<vp>,<np>)";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals(0, result.at("/query/relation/boundary/min").asInt());
        assertEquals(5, result.at("/query/relation/boundary/max").asInt());

        query = "dominates(cnx/c{5}:<vp>,<np>)";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals(5, result.at("/query/relation/boundary/min").asInt());
        assertEquals(5, result.at("/query/relation/boundary/max").asInt());
    }

    @Ignore
    @Test
    public void testAlign () throws JsonProcessingException, IOException {
        query = "[orth=\"der\"]^[orth=\"Mann\"]";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("operation:sequence", result.at("/query/operation").asText());
        assertEquals("der", result.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals(1, result.at("/query/operands/0/classOut").asInt());
        assertEquals("Mann", result.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("operation:class", result.at("/query/operands/1/operation")
                .asText());
        assertEquals(2, result.at("/query/operands/1/classOut").asInt());
        assertEquals(1, result.at("/meta/alignment/0/0").asInt());
        assertEquals(2, result.at("/meta/alignment/0/1").asInt());

        query = "[orth='der']^[orth='große'][orth='Mann']";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("operation:sequence", result.at("/query/operation").asText());
        assertEquals("der", result.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals(1, result.at("/query/operands/0/classOut").asInt());
        assertEquals("große", result.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("operation:class", result.at("/query/operands/1/operation")
                .asText());
        assertEquals(2, result.at("/query/operands/1/classOut").asInt());
        assertEquals("Mann", result.at("/query/operands/2/wrap/key").asText());
        assertEquals(1, result.at("/meta/alignment/0/0").asInt());
        assertEquals(2, result.at("/meta/alignment/0/1").asInt());


        query = "([base=\"a\"]^[base=\"b\"])|[base=\"c\"]";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("operation:disjunction", result.at("/query/operation")
                .asText());
        assertEquals("operation:sequence", result
                .at("/query/operands/0/operation").asText());
        assertEquals("operation:class",
                result.at("/query/operands/0/operands/1/operation").asText());
        assertEquals("a",
                result.at("/query/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals("b",
                result.at("/query/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals("c", result.at("/query/operands/1/wrap/key").asText());
        assertEquals(1, result.at("/query/operands/0/operands/0/classOut").asInt());
        assertEquals(2, result.at("/query/operands/0/operands/1/classOut").asInt());
        assertEquals(1, result.at("/meta/alignment/0/0").asInt());
        assertEquals(2, result.at("/meta/alignment/0/1").asInt());

        query = "([base='a']^[base='b'][base='c'])|[base='d']";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("a",
                result.at("/query/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals("b",
                result.at("/query/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals("c", result.at("/query/operands/0/operands/2/wrap/key")
                .asText());
        assertEquals("d", result.at("/query/operands/1/wrap/key").asText());

        query = "([base='a']^[base='b']^[base='c'])|[base='d']";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("a",
                result.at("/query/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(1, result.at("/query/operands/0/operands/0/classOut").asInt());
        assertEquals("b",
                result.at("/query/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals(2, result.at("/query/operands/0/operands/1/classOut").asInt());
        assertEquals("c",
                result.at("/query/operands/0/operands/2/operands/0/wrap/key")
                        .asText());
        assertEquals(3, result.at("/query/operands/0/operands/2/classOut").asInt());
        assertEquals("d", result.at("/query/operands/1/wrap/key").asText());
        assertEquals(1, result.at("/meta/alignment/0/0").asInt());
        assertEquals(2, result.at("/meta/alignment/0/1").asInt());
        assertEquals(2, result.at("/meta/alignment/1/0").asInt());
        assertEquals(3, result.at("/meta/alignment/1/1").asInt());

        query = "^ 'Mann'";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("Mann", result.at("/query/operands/0/wrap/key").asText());
        assertEquals("operation:class", result.at("/query/operation").asText());
        assertEquals(1, result.at("/query/classOut").asInt());
        assertEquals(-1, result.at("/meta/alignment/0/0").asInt());
        assertEquals(1, result.at("/meta/alignment/0/1").asInt());

        query = "'Mann' ^";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("Mann", result.at("/query/operands/0/wrap/key").asText());
        assertEquals("operation:class", result.at("/query/operation").asText());
        assertEquals(1, result.at("/query/classOut").asInt());
        assertEquals(1, result.at("/meta/alignment/0/0").asInt());
        assertEquals(-1, result.at("/meta/alignment/0/1").asInt());
    }


    @Test
    public void testSimpleQueries () throws JsonProcessingException,
            IOException {
        query = "\"Baum\"";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("Baum", result.at("/query/wrap/key").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());

        query = "'Der' 'Baum'";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:sequence", result.at("/query/operation").asText());
        assertEquals("koral:token", result.at("/query/operands/0/@type").asText());
        assertEquals("koral:term", result.at("/query/operands/0/wrap/@type")
                .asText());
        assertEquals("Der", result.at("/query/operands/0/wrap/key").asText());
        assertEquals("Baum", result.at("/query/operands/1/wrap/key").asText());
        assertEquals("orth", result.at("/query/operands/0/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/operands/0/wrap/match")
                .asText());
        assertEquals("orth", result.at("/query/operands/1/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/operands/1/wrap/match")
                .asText());

        query = "\"Der\" \"große\" \"Baum\"";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("Der", result.at("/query/operands/0/wrap/key").asText());
        assertEquals("große", result.at("/query/operands/1/wrap/key").asText());
        assertEquals("Baum", result.at("/query/operands/2/wrap/key").asText());

        query = "\"Der\" ('große'|'kleine') 'Baum'";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("Der", result.at("/query/operands/0/wrap/key").asText());
        assertEquals("operation:disjunction",
                result.at("/query/operands/1/operation").asText());
        assertEquals("große", result.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("kleine", result.at("/query/operands/1/operands/1/wrap/key")
                .asText());
        assertEquals("Baum", result.at("/query/operands/2/wrap/key").asText());

        query = "'der' 'große' 'Baum' | 'der' 'kleine' 'Baum'";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("operation:disjunction", result.at("/query/operation")
                .asText());
        assertEquals("der", result.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("große", result.at("/query/operands/0/operands/1/wrap/key")
                .asText());
        assertEquals("Baum", result.at("/query/operands/0/operands/2/wrap/key")
                .asText());
        assertEquals("der", result.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("kleine", result.at("/query/operands/1/operands/1/wrap/key")
                .asText());
        assertEquals("Baum", result.at("/query/operands/1/operands/2/wrap/key")
                .asText());

        query = "\"Der\" [p=\"ADJA\"] \"Baum\"";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("Der", result.at("/query/operands/0/wrap/key").asText());
        assertEquals("ADJA", result.at("/query/operands/1/wrap/key").asText());
        assertEquals("p", result.at("/query/operands/1/wrap/layer").asText());
        assertEquals("Baum", result.at("/query/operands/2/wrap/key").asText());
    }


    @Test
    public void testWithin () throws JsonProcessingException, IOException {
        
        query= "[pos=\"NN\"] []* [pos=\"NN\"] within np;";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("operation:position", result.at("/query/operation").asText());
        assertEquals("frames:isAround", result.at("/query/frames/0").asText());
        assertEquals("np", result.at("/query/operands/0/wrap/key").asText());
        assertEquals("NN", result.at("/query/operands/1/operands/0/wrap/key").asText());
        assertEquals("operation:repetition", result.at("/query/operands/1/operands/1/operation").asText());
        assertEquals("NN", result.at("/query/operands/1/operands/2/wrap/key").asText());
        assertEquals("operation:sequence", result.at("/query/operands/1/operation").asText());
    	
    	query = "[p='VVFIN'] within s";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("operation:position", result.at("/query/operation").asText());
        assertEquals("frames:isAround", result.at("/query/frames/0").asText());
        assertEquals("s", result.at("/query/operands/0/wrap/key").asText());
     
        assertEquals("VVFIN", result.at("/query/operands/1/wrap/key").asText());
        
        
    }

   @Test
   public void testWithinElement () throws JsonProcessingException, IOException {
	   // within implemented with span now!
        query = "[p='VVFIN'] within <base/s=s>";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("operation:position", result.at("/query/operation").asText());
        assertEquals("frames:isAround", result.at("/query/frames/0").asText());
        assertEquals("s", result.at("/query/operands/0/wrap/key").asText());
        assertEquals("VVFIN", result.at("/query/operands/1/wrap/key").asText());
        }


    @Test
    public void testSpanSerialization () throws JsonProcessingException,
            IOException {

      //  query = "<s> []+ 'der' []+ </s>)"; and query = "'der' within s"; should serialize identically
      
        query = "<s> []+ 'der' []+ </s>)";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());

        String result1 = result.toString();
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:position", result.at("/query/operation").asText());
        assertEquals("frames:isAround", result.at("/query/frames/0").asText());
        assertEquals(1, result.at("/query/frames").size());
        
        assertEquals("koral:span", result.at("/query/operands/0/@type").asText());
        assertEquals("s", result.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:token", result.at("/query/operands/1/@type").asText());


        query = "'der' within s";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        String result2 = result.toString();
        
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:position", result.at("/query/operation").asText());
        assertEquals("frames:isAround", result.at("/query/frames/0").asText());
        assertEquals(1, result.at("/query/frames").size());
        
        assertEquals("koral:span", result.at("/query/operands/0/@type").asText());
        assertEquals("s", result.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:token", result.at("/query/operands/1/@type").asText());
        
        assertEquals(result1, result2);
    }

    @Ignore
    @Test
    public void testQueryReferences () throws JsonProcessingException, IOException {
        query = "{#test}";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("koral:queryRef", result.at("/query/@type").asText());
        assertEquals("test", result.at("/query/ref").asText());

        query = "{#admin/example}";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("koral:queryRef", result.at("/query/@type").asText());
        assertEquals("admin/example", result.at("/query/ref").asText());

        query = "Der {#admin/example} [orth=Baum]";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());

        assertEquals("koral:token", result.at("/query/operands/0/@type").asText());
        assertEquals("koral:queryRef", result.at("/query/operands/1/@type").asText());
        assertEquals("admin/example", result.at("/query/operands/1/ref").asText());
        assertEquals("koral:token", result.at("/query/operands/2/@type").asText());

        query = "[orth=Der]{#admin/example}{1,}[orth=Baum]";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());

        assertEquals("koral:token", result.at("/query/operands/0/@type").asText());
        assertEquals("koral:group", result.at("/query/operands/1/@type").asText());
        assertEquals("koral:queryRef", result.at("/query/operands/1/operands/0/@type").asText());
        assertEquals("admin/example", result.at("/query/operands/1/operands/0/ref").asText());
        assertEquals("koral:token", result.at("/query/operands/2/@type").asText());
    }

    @Test
    public void testMeta () throws JsonProcessingException, IOException {
        query = "'x' meta textClass='Sport'";
        qs.setQuery(query, "CQP");
        result = mapper.readTree(qs.toJSON());
        assertEquals("x", result.at("/query/wrap/key").asText());
        assertEquals("koral:doc", result.at("/collection/@type").asText());
        assertEquals("textClass", result.at("/collection/key").asText());
        assertEquals("Sport", result.at("/collection/value").asText());

        query = "\"x\" meta textClass='Sport'";
        qs.setQuery(query, "CQP");
        qs.setCollection("author=Smith");
        result = mapper.readTree(qs.toJSON());
        assertEquals("x", result.at("/query/wrap/key").asText());
        assertEquals("koral:docGroup", result.at("/collection/@type").asText());
        assertEquals("operation:and", result.at("/collection/operation").asText());
        assertEquals("textClass", result.at("/collection/operands/0/key").asText());
        assertEquals("Sport", result.at("/collection/operands/0/value").asText());
        assertEquals("author", result.at("/collection/operands/1/key").asText());
        assertEquals("Smith", result.at("/collection/operands/1/value").asText());
    }
   
 
}