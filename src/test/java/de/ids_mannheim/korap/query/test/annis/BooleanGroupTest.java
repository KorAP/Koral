package de.ids_mannheim.korap.query.test.annis;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ids_mannheim.korap.query.serialize.QuerySerializer;

public class BooleanGroupTest {

    private QuerySerializer qs = new QuerySerializer();
    private ObjectMapper mapper = new ObjectMapper();
    
    private JsonNode runQuery (String query)
            throws JsonProcessingException, JsonMappingException {
        qs.setQuery(query, "annis");
        JsonNode result = mapper.readTree(qs.toJSON());
//        System.out.println(result.toPrettyString());
        return result;
    }
    
    
    @Test
    public void testEmptyGroup () throws Exception {
        JsonNode n = runQuery("/()/");
        assertEquals("koral:token", n.at("/query/@type").asText());
        assertEquals("koral:term", n.at("/query/wrap/@type").asText());
        assertEquals("match:eq", n.at("/query/wrap/match:eq").asText());
        assertEquals("type:regex", n.at("/query/wrap/@type").asText());
        assertEquals("orth", n.at("/query/wrap/layer").asText());
        assertEquals("()", n.at("/query/wrap/key").asText());
    }
    
    @Test
    public void testGroupInRegex () throws Exception {
        JsonNode n = runQuery("/(Kat.*)/");
        assertEquals("koral:token", n.at("/query/@type").asText());
        assertEquals("koral:term", n.at("/query/wrap/@type").asText());
        assertEquals("match:eq", n.at("/query/wrap/match:eq").asText());
        assertEquals("type:regex", n.at("/query/wrap/@type").asText());
        assertEquals("orth", n.at("/query/wrap/layer").asText());
        assertEquals("(Kat.*)", n.at("/query/wrap/key").asText());
    }

    @Test
    public void testOrGroup () throws Exception {
        JsonNode n = runQuery("(cat=\"S\" | cat=\"NP\")");
        assertEquals("koral:group", n.at("/query/@type").asText());
        assertEquals("operation:disjunction", n.at("/query/operation").asText());
        assertEquals("koral:span", n.at("/query/operands/0/@type").asText());
        assertEquals("koral:term", n.at("/query/operands/0/wrap/@type").asText());
        assertEquals("match:eq", n.at("/query/operands/0/wrap/match").asText());
        assertEquals("c", n.at("/query/operands/0/wrap/layer").asText());
        assertEquals("S", n.at("/query/operands/0/wrap/key").asText());
        assertEquals("c", n.at("/query/operands/1/wrap/layer").asText());
        assertEquals("NP", n.at("/query/operands/1/wrap/key").asText());
    }
    
    @Test
    public void testOrGroupRegex () throws Exception {
        JsonNode n = runQuery("/(be|have)/");
        assertEquals("koral:token", n.at("/query/@type").asText());
        assertEquals("koral:term", n.at("/query/wrap/@type").asText());
        assertEquals("match:eq", n.at("/query/wrap/match:eq").asText());
        assertEquals("type:regex", n.at("/query/wrap/@type").asText());
        assertEquals("orth", n.at("/query/wrap/layer").asText());
        assertEquals("(be|have)", n.at("/query/wrap/key").asText());
    }
    
    @Test
    public void testLemmaOrGroupRegex () throws Exception {
        JsonNode n = runQuery("lemma=/(be|have)/");
        assertEquals("koral:token", n.at("/query/@type").asText());
        assertEquals("koral:term", n.at("/query/wrap/@type").asText());
        assertEquals("match:eq", n.at("/query/wrap/match:eq").asText());
        assertEquals("type:regex", n.at("/query/wrap/@type").asText());
        assertEquals("l", n.at("/query/wrap/layer").asText());
        assertEquals("(be|have)", n.at("/query/wrap/key").asText());
    }

    
    @Test
    public void testAndGroup () throws Exception {
        runQuery("tok=\"Katze\" & pos=\"N\"");
        
        // EM: Not sure how the expected result is
        // Koral generates KQ only for the last operand:
        /*
         {
          "query" : {
            "@type" : "koral:token",
            "wrap" : {
              "@type" : "koral:term",
              "match" : "match:eq",
              "layer" : "p",
              "key" : "N"
            }
          },
          "@context" : "http://korap.ids-mannheim.de/ns/koral/0.3/context.jsonld"
        }
        */
    }
    
    @Test
    public void testRegexInGroup () throws Exception {
        JsonNode n = runQuery("(/Kat.*/)");
        
        assertEquals("koral:token", n.at("/query/@type").asText());
        assertEquals("koral:term", n.at("/query/wrap/@type").asText());
        assertEquals("match:eq", n.at("/query/wrap/match:eq").asText());
        assertEquals("type:regex", n.at("/query/wrap/@type").asText());
        assertEquals("orth", n.at("/query/wrap/layer").asText());
        assertEquals("Kat.*", n.at("/query/wrap/key").asText());
    }
    
    @Test
    public void testNestedGroupRegex () throws Exception {
        JsonNode n =runQuery("(/(/Kat.*/)/)");
        assertEquals("koral:token", n.at("/query/@type").asText());
        assertEquals("koral:term", n.at("/query/wrap/@type").asText());
        assertEquals("match:eq", n.at("/query/wrap/match:eq").asText());
        assertEquals("type:regex", n.at("/query/wrap/@type").asText());
        assertEquals("orth", n.at("/query/wrap/layer").asText());
        assertEquals("(Kat.*)", n.at("/query/wrap/key").asText());
        
        // EM: I think the nested slashes are not necessary here 
        // Please see fragment RE_group rule in AqlLexer.g4 #85
    }
}
