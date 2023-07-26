package de.ids_mannheim.korap.query.test.cqp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import de.ids_mannheim.korap.query.test.BaseQueryTest;

public class CQPRepetitionTest extends BaseQueryTest {

    private JsonNode result;

    public CQPRepetitionTest () {
        super("CQP");
    }

    private void checkKoralTerm (JsonNode node) {
        assertEquals("koral:term", node.at("/@type").asText());
        assertEquals("match:eq", node.at("/match").asText());
        assertEquals("type:regex", node.at("/type").asText());
        assertEquals("orth", node.at("/layer").asText());
        assertEquals("der", node.at("/key").asText());
    }
    
    @Test
    public void testRepetition () throws JsonProcessingException, IOException {
        query = "'der'{3}";
        result = runQuery(query);
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:repetition", result.at("/query/operation")
                .asText());
        assertEquals(1, result.at("/query/operands").size());
        
        checkKoralTerm(result.at("/query/operands/0/wrap"));
        
        assertEquals("koral:boundary", result.at("/query/boundary/@type").asText());
        assertEquals(3, result.at("/query/boundary/min").asInt());
        assertEquals(3, result.at("/query/boundary/max").asInt());

        
        query = "\"der\"{,3}";
        result = runQuery(query);
        assertEquals(0, result.at("/query/boundary/min").asInt());
        assertEquals(3, result.at("/query/boundary/max").asInt());

        query = "'der'{3,}";
        result = runQuery(query);
        assertEquals(3, result.at("/query/boundary/min").asInt());
        assertTrue(result.at("/query/boundary/max").isMissingNode());

        query = "\"der\"{3,7}";
        result = runQuery(query);
        assertEquals(3, result.at("/query/boundary/min").asInt());
        assertEquals(7, result.at("/query/boundary/max").asInt());

        query = "'der'*";
        result = runQuery(query);
        assertEquals(0, result.at("/query/boundary/min").asInt());
        assertTrue(result.at("/query/boundary/max").isMissingNode());

        query = "'der'+";
        result = runQuery(query);
        assertEquals(1, result.at("/query/boundary/min").asInt());
        assertTrue(result.at("/query/boundary/max").isMissingNode());
    };

    @Test
    public void testRepetitionInSentence () throws JsonProcessingException{
        // this queries were written in PQ+ with contains()
        query = "<s> []+ (\"der\")* []+ </s>";
        result = runQuery(query);

        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:position", result.at("/query/operation")
                .asText());
        assertEquals("frames:isAround", result.at("/query/frames/0").asText());
        assertEquals(2, result.at("/query/operands").size());
        
        assertEquals("koral:span", result.at("/query/operands/0/@type").asText());
        assertEquals("koral:term", result.at("/query/operands/0/wrap/@type").asText());
        assertEquals("s", result.at("/query/operands/0/wrap/key").asText());
        
        assertEquals("koral:group", result.at("/query/operands/1/@type").asText());
        assertEquals("operation:repetition", result.at("/query/operands/1/operation")
                .asText());
        assertEquals(1, result.at("/query/operands/1/operands").size());
        
        checkKoralTerm(result.at("/query/operands/1/operands/0/wrap"));
        
        assertEquals(0, result.at("/query/operands/1/operands/0/boundary/min").asInt());
        assertTrue(result.at("/query/operands/1/operands/0/boundary/max").isMissingNode());
        assertTrue(result.at("/query/operands/1/operands/2/").isMissingNode()); // []* are not taken into account as tokens;
    }
    
    @Test
    public void testGroupRepetition () throws JsonProcessingException{
        query = "<s> []+ (\"der\"){3,} []+ </s>";
        result = runQuery(query);
        assertEquals(3, result.at("/query/operands/1/boundary/min").asInt());
        assertTrue(result.at("/query/operands/1/boundary/max")
                .isMissingNode());

        query = "<s> []+ (\"der\"){,3} []+ </s>";
        result = runQuery(query);
        assertEquals(0, result.at("/query/operands/1/boundary/min").asInt());
        assertEquals(3, result.at("/query/operands/1/boundary/max").asInt());

        query = "<s> []+ (\"der\"){3,7} []+ </s>";
        result = runQuery(query);
        assertEquals(3, result.at("/query/operands/1/boundary/min").asInt());
        assertEquals(7, result.at("/query/operands/1/boundary/max").asInt());
    };
}
