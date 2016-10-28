package de.ids_mannheim.korap.query.test.poliqarpplus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import de.ids_mannheim.korap.query.serialize.QuerySerializer;

/**
 * Tests for JSON-LD serialization of empty/any tokens in PoliqarpPlus queries.
 * 
 * @author Joachim Bingel (bingel@ids-mannheim.de)
 * @author Eliza Margaretha (margaretha@ids-mannheim.de)
 * @author Nils Diewald (diewald@ids-mannheim.de)
 * @version 1.0
 */

public class EmptyTokenTest {

    String query;
    ArrayList<JsonNode> operands;

    QuerySerializer qs = new QuerySerializer();
    ObjectMapper mapper = new ObjectMapper();
    JsonNode res;
   
    @Test
    public void testSingle() throws JsonProcessingException, IOException {
        query = "[]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals(true, res.at("/query/key").isMissingNode());
    }

    @Test
    public void testQuantifier () throws JsonProcessingException, IOException {
        query = "[]{3}";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());

        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:repetition",
                res.at("/query/operation").asText());
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:boundary",
                res.at("/query/boundary/@type").asText());
        assertEquals(3, res.at("/query/boundary/min").asInt());
        assertEquals(3, res.at("/query/boundary/max").asInt());

        query = "[]{1,3}";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());

        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:repetition",
                res.at("/query/operation").asText());
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:boundary",
                res.at("/query/boundary/@type").asText());
        assertEquals(1, res.at("/query/boundary/min").asInt());
        assertEquals(3, res.at("/query/boundary/max").asInt());


        query = "[]{ , 3}";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());

        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:repetition",
                res.at("/query/operation").asText());
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:boundary",
                res.at("/query/boundary/@type").asText());
        assertEquals(0, res.at("/query/boundary/min").asInt());
        assertEquals(3, res.at("/query/boundary/max").asInt());
		assertTrue(res.at("/query/min").isMissingNode());

        query = "[]{4,}";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());

        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:repetition",
                res.at("/query/operation").asText());
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:boundary",
                res.at("/query/boundary/@type").asText());
        assertEquals(4, res.at("/query/boundary/min").asInt());
        assertTrue(res.at("/query/boundary/max").isMissingNode());
    }


    @Test
    public void testLeading1 () throws JsonProcessingException, IOException {
        query = "[][base=Mann]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals("koral:token", operands.get(0).at("/@type").asText());
        assertEquals(true, operands.get(0).at("/key").isMissingNode());
        assertEquals("koral:token", operands.get(1).at("/@type").asText());
        assertEquals("Mann", operands.get(1).at("/wrap/key").asText());

        query = "[][][base=Mann]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals(2, operands.size());
        assertEquals("koral:group", operands.get(0).at("/@type").asText());
        assertEquals("operation:repetition",
                operands.get(0).at("/operation").asText());
        assertEquals(2, operands.get(0).at("/boundary/min").asInt());
        assertEquals(2, operands.get(0).at("/boundary/max").asInt());
        operands = Lists
                .newArrayList(operands.get(0).at("/operands").elements());
        assertEquals("koral:token", operands.get(0).at("/@type").asText());
        assertEquals(true, operands.get(0).at("/key").isMissingNode());
    }


    @Test
    public void testLeading2 () throws JsonProcessingException, IOException {
        query = "[][][base=bose][base=Mann]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals("koral:group", operands.get(0).at("/@type").asText());
        assertEquals("operation:repetition",
                operands.get(0).at("/operation").asText());
        assertEquals(2, operands.get(0).at("/boundary/min").asInt());
        assertEquals(2, operands.get(0).at("/boundary/max").asInt());
        assertEquals("koral:token",
                operands.get(0).at("/operands/0/@type").asText());
        assertEquals(true,
                operands.get(0).at("/operands/0/key").isMissingNode());
        assertEquals("koral:token", operands.get(1).at("/@type").asText());
        assertEquals("bose", operands.get(1).at("/wrap/key").asText());
        assertEquals("koral:token", operands.get(2).at("/@type").asText());
        assertEquals("Mann", operands.get(2).at("/wrap/key").asText());
    }


    @Test
    public void testTrailing () throws JsonProcessingException, IOException {
        query = "[base=Mann][]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals("koral:token", operands.get(1).at("/@type").asText());
        assertEquals(true, operands.get(1).at("/key").isMissingNode());

        query = "[base=Mann][]{3}";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        res = operands.get(1);
        assertEquals("koral:group", res.at("/@type").asText());
        assertEquals(true, res.at("/wrap/key").isMissingNode());
        assertEquals("operation:repetition", res.at("/operation").asText());
        assertEquals(3, res.at("/boundary/min").asInt());
        assertEquals(3, res.at("/boundary/max").asInt());

        query = "[base=der][base=Mann][]{3}";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals(3, operands.size());
        assertEquals("koral:token", operands.get(0).at("/@type").asText());
        assertEquals("der", operands.get(0).at("/wrap/key").asText());
        assertEquals("koral:token", operands.get(1).at("/@type").asText());
        assertEquals("Mann", operands.get(1).at("/wrap/key").asText());
    }


    @Test
    public void testBetweenTokens1 ()
            throws JsonProcessingException, IOException {
        query = "[base=der][][base=Mann]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals(3, operands.size());
        assertEquals("koral:token", operands.get(0).at("/@type").asText());
        assertEquals("der", operands.get(0).at("/wrap/key").asText());
        assertEquals("lemma", operands.get(0).at("/wrap/layer").asText());
        assertEquals("match:eq", operands.get(0).at("/wrap/match").asText());
        assertEquals("koral:token", operands.get(1).at("/@type").asText());
        assertEquals(true, operands.get(1).at("/wrap/key").isMissingNode());
        assertEquals("koral:token", operands.get(2).at("/@type").asText());
        assertEquals("Mann", operands.get(2).at("/wrap/key").asText());
        assertEquals("lemma", operands.get(2).at("/wrap/layer").asText());
        assertEquals("match:eq", operands.get(2).at("/wrap/match").asText());
    }


    @Test
    public void testBetweenTokens2 ()
            throws JsonProcessingException, IOException {
        query = "[base=der][][][base=Mann]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals(3, operands.size());
        assertEquals("koral:group", operands.get(1).at("/@type").asText());
        assertEquals("operation:repetition",
                operands.get(1).at("/operation").asText());
        assertEquals("koral:boundary",
                operands.get(1).at("/boundary/@type").asText());
        assertEquals(2, operands.get(1).at("/boundary/min").asInt());
        assertEquals(2, operands.get(1).at("/boundary/max").asInt());
        operands = Lists
                .newArrayList(operands.get(1).at("/operands").elements());
        assertEquals(1, operands.size());
        assertEquals("koral:token", operands.get(0).at("/@type").asText());
        assertEquals(true, operands.get(0).at("/wrap/key").isMissingNode());

    }

    @Test
    public void testBetweenTokensAndOptionality ()
            throws JsonProcessingException, IOException {
        query = "[base=der][][base=Mann]?";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals(3, operands.size());
        
        assertEquals("koral:token", operands.get(1).at("/@type").asText());
        assertEquals(true, operands.get(1).at("/wrap/key").isMissingNode());
        
        assertEquals("koral:group", operands.get(2).at("/@type").asText());
        assertEquals("operation:repetition",
                operands.get(2).at("/operation").asText());
        assertEquals("Mann", operands.get(2).at("/operands/0/wrap/key").asText());
        assertEquals("koral:boundary",
                operands.get(2).at("/boundary/@type").asText());
        assertEquals(0, operands.get(2).at("/boundary/min").asInt());
        assertEquals(1, operands.get(2).at("/boundary/max").asInt());
    }
        
    @Test
    public void testBetweenTokensAndOptionality2 ()
            throws JsonProcessingException, IOException {
        query = "[base=der]?[][base=Mann]?";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals(3, operands.size());
        
        assertEquals("koral:group", operands.get(0).at("/@type").asText());
        assertEquals("operation:repetition",
                operands.get(0).at("/operation").asText());
        assertEquals("der", operands.get(0).at("/operands/0/wrap/key").asText());
        assertEquals("koral:boundary",
                operands.get(0).at("/boundary/@type").asText());
        assertEquals(0, operands.get(0).at("/boundary/min").asInt());
        assertEquals(1, operands.get(0).at("/boundary/max").asInt());
        
        assertEquals("koral:token", operands.get(1).at("/@type").asText());
        assertEquals(true, operands.get(1).at("/wrap/key").isMissingNode());
        
        assertEquals("koral:group", operands.get(2).at("/@type").asText());
        assertEquals("operation:repetition",
                operands.get(2).at("/operation").asText());
        assertEquals("Mann", operands.get(2).at("/operands/0/wrap/key").asText());
        assertEquals("koral:boundary",
                operands.get(2).at("/boundary/@type").asText());
        assertEquals(0, operands.get(2).at("/boundary/min").asInt());
        assertEquals(1, operands.get(2).at("/boundary/max").asInt());
    }
    
    @Test
    public void testQuantifierBetweenTokens1 ()
            throws JsonProcessingException, IOException {
        query = "[base=der][][]?[base=Mann]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals(3, operands.size());
        assertEquals("koral:group", operands.get(1).at("/@type").asText());
        assertEquals("operation:repetition",
                operands.get(1).at("/operation").asText());
        assertEquals("koral:boundary",
                operands.get(1).at("/boundary/@type").asText());
        assertEquals(1, operands.get(1).at("/boundary/min").asInt());
        assertEquals(2, operands.get(1).at("/boundary/max").asInt());
        operands = Lists
                .newArrayList(operands.get(1).at("/operands").elements());
        assertEquals(1, operands.size());
        assertEquals("koral:token", operands.get(0).at("/@type").asText());
        assertEquals(true, operands.get(0).at("/wrap/key").isMissingNode());
    }


    @Test
    public void testQuantifierBetweenTokens2 ()
            throws JsonProcessingException, IOException {
        query = "[base=der][]+[base=Mann]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals(3, operands.size());
        assertEquals("koral:group", operands.get(1).at("/@type").asText());
        assertEquals("operation:repetition",
                operands.get(1).at("/operation").asText());
        assertEquals("koral:boundary",
                operands.get(1).at("/boundary/@type").asText());
        assertEquals(1, operands.get(1).at("/boundary/min").asInt());
        assertEquals(true, operands.get(1).at("/boundary/max").isMissingNode());
        operands = Lists
                .newArrayList(operands.get(1).at("/operands").elements());
        assertEquals(1, operands.size());
        assertEquals("koral:token", operands.get(0).at("/@type").asText());
        assertEquals(true, operands.get(0).at("/wrap/key").isMissingNode());
    }


    @Test
    public void testQuantifierBetweenTokens3 ()
            throws JsonProcessingException, IOException {

        query = "[base=der][]*[base=Mann]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals(3, operands.size());
        assertEquals("koral:group", operands.get(1).at("/@type").asText());
        assertEquals("operation:repetition",
                operands.get(1).at("/operation").asText());
        assertEquals("koral:boundary",
                operands.get(1).at("/boundary/@type").asText());
        assertEquals(0, operands.get(1).at("/boundary/min").asInt());
        assertEquals(true, operands.get(1).at("/boundary/max").isMissingNode());
        operands = Lists
                .newArrayList(operands.get(1).at("/operands").elements());
        assertEquals(1, operands.size());
        assertEquals("koral:token", operands.get(0).at("/@type").asText());
        assertEquals(true, operands.get(0).at("/wrap/key").isMissingNode());
    }


    @Test
    public void testQuantifierBetweenTokens4 ()
            throws JsonProcessingException, IOException {
        query = "[base=der][]{2,5}[base=Mann][]?[][base=Frau]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals(5, operands.size());
        assertEquals("koral:token", operands.get(0).at("/@type").asText());
        assertEquals("der", operands.get(0).at("/wrap/key").asText());

        assertEquals("koral:group", operands.get(1).at("/@type").asText());
        assertEquals("operation:repetition",
                operands.get(1).at("/operation").asText());
        assertEquals("koral:boundary",
                operands.get(1).at("/boundary/@type").asText());
        assertEquals(2, operands.get(1).at("/boundary/min").asInt());
        assertEquals(5, operands.get(1).at("/boundary/max").asInt());
        assertEquals("koral:token",
                operands.get(1).at("/operands/0/@type").asText());
        assertEquals(true,
                operands.get(1).at("/operands/0/wrap/key").isMissingNode());

        assertEquals("koral:token", operands.get(2).at("/@type").asText());
        assertEquals("Mann", operands.get(2).at("/wrap/key").asText());

        assertEquals("koral:group", operands.get(3).at("/@type").asText());
        assertEquals("operation:repetition",
                operands.get(3).at("/operation").asText());
        assertEquals("koral:boundary",
                operands.get(3).at("/boundary/@type").asText());
        assertEquals(1, operands.get(3).at("/boundary/min").asInt());
        assertEquals(2, operands.get(3).at("/boundary/max").asInt());
        assertEquals("koral:token",
                operands.get(3).at("/operands/0/@type").asText());
        assertEquals(true,
                operands.get(3).at("/operands/0/wrap/key").isMissingNode());

        assertEquals("koral:token", operands.get(4).at("/@type").asText());
        assertEquals("Frau", operands.get(4).at("/wrap/key").asText());
    }


    @Test
    public void testQuantifierBetweenTokens5 ()
            throws JsonProcessingException, IOException {
        query = "[base=geht][base=der][]*contains(<s>,<np>)";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());

        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals(true, res.at("/query/inOrder").isMissingNode());
        assertEquals(true, res.at("/query/distances").isMissingNode());

        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals(4, operands.size());

        assertEquals("koral:token", operands.get(0).at("/@type").asText());
        assertEquals("geht", operands.get(0).at("/wrap/key").asText());
        assertEquals("lemma", operands.get(0).at("/wrap/layer").asText());
        assertEquals("match:eq", operands.get(0).at("/wrap/match").asText());

        assertEquals("koral:token", operands.get(1).at("/@type").asText());
        assertEquals("der", operands.get(1).at("/wrap/key").asText());
        assertEquals("lemma", operands.get(1).at("/wrap/layer").asText());
        assertEquals("match:eq", operands.get(1).at("/wrap/match").asText());

        assertEquals("koral:group", operands.get(2).at("/@type").asText());
        assertEquals("operation:repetition",
                operands.get(2).at("/operation").asText());
        assertEquals("koral:token",
                operands.get(2).at("/operands/0/@type").asText());
        assertEquals("koral:boundary",
                operands.get(2).at("/boundary/@type").asText());
        assertEquals(0, operands.get(2).at("/boundary/min").asInt());
        assertEquals(true, operands.get(2).at("/boundary/max").isMissingNode());

        assertEquals("koral:group", operands.get(3).at("/@type").asText());
        assertEquals("operation:position",
                operands.get(3).at("/operation").asText());
        assertEquals("frames:isAround",
                operands.get(3).at("/frames/0").asText());
        assertEquals("koral:span",
                operands.get(3).at("/operands/0/@type").asText());
        assertEquals("s", operands.get(3).at("/operands/0/wrap/key").asText());
        assertEquals("koral:span",
                operands.get(3).at("/operands/1/@type").asText());
        assertEquals("np", operands.get(3).at("/operands/1/wrap/key").asText());
    }

    @Test
    public void testPositionInSpans () throws JsonProcessingException, IOException {
        query = "contains(<s>, [])";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("frames:isAround", res.at("/query/frames/0").asText());
        assertEquals(true, res.at("/query/frames/1").isMissingNode());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:token", res.at("/query/operands/1/@type").asText());
        assertEquals(true, res.at("/query/operands/1/key").isMissingNode());

        query = "contains(<s>, []{3})";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:repetition",
                res.at("/query/operands/1/operation").asText());
        assertEquals("koral:token",
                res.at("/query/operands/1/operands/0/@type").asText());
        assertEquals(true,
                res.at("/query/operands/1/operands/0/key").isMissingNode());

        query = "contains(<s>, {1:[]{3}})";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:class",
                res.at("/query/operands/1/operation").asText());
        assertEquals("operation:repetition",
                res.at("/query/operands/1/operands/0/operation").asText());
        assertEquals("koral:token", res
                .at("/query/operands/1/operands/0/operands/0/@type").asText());
        assertEquals(true, res.at("/query/operands/1/operands/0/operands/0/key")
                .isMissingNode());

        query = "startswith(<s>, [][base=Mann])";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("frames:startsWith", res.at("/query/frames/0").asText());
        operands = Lists.newArrayList(res.at("/query/operands"));
        operands = Lists.newArrayList(operands.get(1).at("/operands"));
        assertEquals("koral:token", operands.get(0).at("/@type").asText());
        assertEquals(true, operands.get(0).at("/key").isMissingNode());
    }

    @Test
    public void testClass1 () throws JsonProcessingException, IOException {
        query = "[base=der]{[]}[base=Mann]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals(true, res.at("/query/inOrder").isMissingNode());
        assertEquals(true, res.at("/query/distances").isMissingNode());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals("der", operands.get(0).at("/wrap/key").asText());
        assertEquals("Mann", operands.get(2).at("/wrap/key").asText());
        assertEquals("koral:group", operands.get(1).at("/@type").asText());
        assertEquals("operation:class",
                operands.get(1).at("/operation").asText());
        assertEquals(1, operands.get(1).at("/classOut").asInt());
        operands = Lists
                .newArrayList(operands.get(1).at("/operands").elements());
        assertEquals("koral:token", operands.get(0).at("/@type").asText());
        assertEquals(true, operands.get(0).at("/wrap").isMissingNode());
        assertEquals(1, res.at("/meta/highlight/0").asInt());
    }


    @Test
    public void testClass2 () throws JsonProcessingException, IOException {
        query = "[base=der]{2:[]}[base=Mann]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals("operation:class",
                operands.get(1).at("/operation").asText());
        assertEquals(2, operands.get(1).at("/classOut").asInt());
        assertEquals(2, res.at("/meta/highlight/0").asInt());
    }


    @Test
    public void testClass3 () throws JsonProcessingException, IOException {
        query = "[base=der]{3:{2:[]}}";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals("operation:class",
                operands.get(1).at("/operation").asText());
        assertEquals(3, operands.get(1).at("/classOut").asInt());
        assertEquals(3, res.at("/meta/highlight/0").asInt());
        assertEquals("operation:class",
                operands.get(1).at("/operands/0/operation").asText());
        assertEquals(2, operands.get(1).at("/operands/0/classOut").asInt());
        assertEquals(3, res.at("/meta/highlight/0").asInt());
        assertEquals(2, res.at("/meta/highlight/1").asInt());

    }


    @Test
    public void testClass4 () throws JsonProcessingException, IOException {
        query = "{1:[]}[base=der][base=Mann]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals("operation:class",
                operands.get(0).at("/operation").asText());
        assertEquals(1, operands.get(0).at("/classOut").asInt());
        assertEquals(1, res.at("/meta/highlight/0").asInt());

        query = "{1:{2:der} {3:[]} Mann}";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals(1, operands.size());  // class operation may only have one operand (the sequence)
        operands = Lists
                .newArrayList(operands.get(0).at("/operands").elements());
        assertEquals(3, operands.size());  // the sequence has three operands ("der", "[]" and "Mann")
        assertEquals(1, res.at("/meta/highlight/0").asInt());
        assertEquals(2, res.at("/meta/highlight/1").asInt());
        assertEquals(3, res.at("/meta/highlight/2").asInt());
    }
}
