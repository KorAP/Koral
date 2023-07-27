package de.ids_mannheim.korap.query.test.cqp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

import de.ids_mannheim.korap.query.serialize.QuerySerializer;
import de.ids_mannheim.korap.query.test.BaseQueryTest;

public class CQPFlagTest extends BaseQueryTest {

    private JsonNode result;

    public CQPFlagTest () {
        super("CQP");
       
    }
  

    @Test
    public void testLiteral () throws JsonProcessingException {
        query = "'22\\'-inch'%l";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:string", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("22\'-inch", result.at("/query/wrap/key").asText()); 

        query = "\"22\\\"-inch\"%l;"; // query = "22\"-inch"; 
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:string", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("22\"-inch", result.at("/query/wrap/key").asText()); 

        query = "[mate/b='22\\'-inch'%l]";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:string", result.at("/query/wrap/type").asText());
        assertEquals("b", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("22\'-inch", result.at("/query/wrap/key").asText()); 

        query = "[mate/b=\"22\\\"-inch\"%l];"; 
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:string", result.at("/query/wrap/type").asText());
        assertEquals("b", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("22\"-inch", result.at("/query/wrap/key").asText()); // (no regex escape)
        
        
        query = "[mate/b=\"Der + Mann\"%l]";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("Der + Mann", result.at("/query/wrap/key").asText());
        assertEquals("b", result.at("/query/wrap/layer").asText());
        assertEquals("mate", result.at("/query/wrap/foundry").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("type:string", result.at("/query/wrap/type").asText());
    }

    @Test
    public void testLiteralWithEscape () throws JsonProcessingException {
        query = "[mate/b='D\\'Ma \\\\nn'%l]";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("type:string", result.at("/query/wrap/type").asText());
        assertEquals("D'Ma \\nn", result.at("/query/wrap/key").asText());
        assertEquals("b", result.at("/query/wrap/layer").asText());
        // escape by doubling + verbatim--> the doubling stays!
        query = "[mate/b='D''Ma \\\\nn'%l]";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("type:string", result.at("/query/wrap/type").asText());
        assertEquals("D'Ma \\nn", result.at("/query/wrap/key").asText());
        assertEquals("b", result.at("/query/wrap/layer").asText());
    }


    @Test
    public void testLiteralWithSlash () throws JsonProcessingException {
        query = "[mate/b=\"D'Ma\nn\"%l]";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("D'Ma\nn", result.at("/query/wrap/key").asText());
        assertEquals("b", result.at("/query/wrap/layer").asText());
        assertEquals("mate", result.at("/query/wrap/foundry").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("type:string", result.at("/query/wrap/type").asText());
    }


    @Test
    public void testSingleQuoteWithinDoubleQuote ()
            throws JsonProcessingException {
        query = "[mate/b=\"D'Ma\\\\nn\"]";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("D'Ma\\nn", result.at("/query/wrap/key").asText());
        assertEquals("b", result.at("/query/wrap/layer").asText());
        assertEquals("mate", result.at("/query/wrap/foundry").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());

        // with literal
        query = "[mate/b=\"D'Ma\\\\nn\"%l]";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("D'Ma\\nn", result.at("/query/wrap/key").asText());
        assertEquals("b", result.at("/query/wrap/layer").asText());
        assertEquals("mate", result.at("/query/wrap/foundry").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("type:string", result.at("/query/wrap/type").asText());
    }


    @Test
    public void testDoubleQuoteWithinSingleQuote ()
            throws JsonProcessingException {
        query = "'D\"Ma\\\\nn'";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("D\"Ma\\nn", result.at("/query/wrap/key").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());

        
        query = "[mate/b='D\"Ma\\\\nn']";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("D\"Ma\\nn", result.at("/query/wrap/key").asText());
        assertEquals("b", result.at("/query/wrap/layer").asText());
        assertEquals("mate", result.at("/query/wrap/foundry").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());

         // with literal/verbatim
        query = "'D\"Ma\\\\nn'%l";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("D\"Ma\\nn", result.at("/query/wrap/key").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("type:string", result.at("/query/wrap/type").asText());


        // with literal/verbatim
        query = "[mate/b='D\"Ma\\\\nn'%l]";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("D\"Ma\\nn", result.at("/query/wrap/key").asText());
        assertEquals("b", result.at("/query/wrap/layer").asText());
        assertEquals("mate", result.at("/query/wrap/foundry").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("type:string", result.at("/query/wrap/type").asText());  
    }

    @Test
    public void testURL () throws JsonProcessingException {
        query = "'http://racai.ro'%l";
        result = runQuery(query);
        assertEquals("http://racai.ro", result.at("/query/wrap/key").asText());
        assertEquals("type:string", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());

        query = "\"http://racai.ro\"%l";
        result = runQuery(query);
        assertEquals("http://racai.ro", result.at("/query/wrap/key").asText());
        assertEquals("type:string", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());

        query = "[mate/b='http://racai.ro'%l]";
        result = runQuery(query);
        assertEquals("http://racai.ro", result.at("/query/wrap/key").asText());
        assertEquals("type:string", result.at("/query/wrap/type").asText());
        assertEquals("b", result.at("/query/wrap/layer").asText());
        assertEquals("mate", result.at("/query/wrap/foundry").asText());
    }


    @Test
    public void testDiacritics () throws JsonProcessingException {
        query = "\"deutscher\"%d";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("deutscher", result.at("/query/wrap/key").asText());
        assertEquals("flags:diacriticsInsensitive",
                result.at("/query/wrap/flags/0").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
    }


    @Test
    public void testCaseSensitivityFlag () throws JsonProcessingException {
        query = "[orth=\"deutscher\"%c]";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("deutscher", result.at("/query/wrap/key").asText());
        assertTrue(
                result.at("/query/wrap/flags:caseInsensitive").isMissingNode());
        assertEquals("flags:caseInsensitive",
                result.at("/query/wrap/flags/0").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        
        query = "\"deutscher\"%cd";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("deutscher", result.at("/query/wrap/key").asText());
        assertFalse(result.at("/query/wrap/flags/0").isMissingNode());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("flags:caseInsensitive",
                result.at("/query/wrap/flags/0").asText());
        assertEquals("flags:diacriticsInsensitive",
                result.at("/query/wrap/flags/1").asText());
    }


    @Test
    public void testCaseSensitivitySequence ()
            throws JsonProcessingException {
        query = "[orth=\"deutscher\"%c][orth=\"Bundestag\"]";
        result = runQuery(query);
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:sequence",
                result.at("/query/operation").asText());
        operands = Lists.newArrayList(result.at("/query/operands").elements());
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
        result = runQuery(query);
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:sequence",
                result.at("/query/operation").asText());
        operands = Lists.newArrayList(result.at("/query/operands").elements());
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
    public void testMultipleFlags () throws JsonProcessingException {
        query = "\"Der + Mann\"%lcd";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("Der + Mann", result.at("/query/wrap/key").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("type:string", result.at("/query/wrap/type").asText());
        assertEquals("flags:caseInsensitive",
                result.at("/query/wrap/flags/0").asText());
        assertEquals("flags:diacriticsInsensitive",
                result.at("/query/wrap/flags/1").asText());

        query = "\"Der + Mann\"%cd";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("Der + Mann", result.at("/query/wrap/key").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("flags:caseInsensitive",
                result.at("/query/wrap/flags/0").asText());
        assertEquals("flags:diacriticsInsensitive",
                result.at("/query/wrap/flags/1").asText());
    }

}
