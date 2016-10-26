package de.ids_mannheim.korap.query.serialize;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author margaretha
 * 
 */
public class FCSQLComplexTest {

    String query;
    String jsonLd;
    List<Object> error;

    // -------------------------------------------------------------------------
    // simple-query ::= '(' main_query ')' /* grouping */
    // | implicit-query
    // | segment-query
    // -------------------------------------------------------------------------
    // implicit-query ::= flagged-regexp
    // segment-query ::= "[" expression? "]"
    // -------------------------------------------------------------------------
    // simple-query ::= '(' main_query ')' /* grouping */
    @Test
    public void testGroupQuery() throws IOException {
        query = "(\"blaue\"|\"grüne\")";
        jsonLd = "{@type:koral:group,"
                + "operation:operation:disjunction,"
                + "operands:["
                + "{@type:koral:token, wrap:{@type:koral:term,key:blaue,foundry:opennlp,layer:orth,type:type:regex,match:match:eq}},"
                + "{@type:koral:token, wrap:{@type:koral:term,key:grüne,foundry:opennlp,layer:orth,type:type:regex,match:match:eq}}]}";;
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);

        // group and disjunction
        query = "([pos=\"NN\"]|[cnx:pos=\"N\"]|[text=\"Mann\"])";
        jsonLd = "{@type:koral:token,wrap:{@type:koral:term,key:N,foundry:cnx,layer:p,type:type:regex,match:match:eq}}";
        FCSQLQueryProcessorTest
                .validateNode(query, "/query/operands/1", jsonLd);

        // a group contains a sequence
        query = "([text=\"blaue\"][pos=\"NN\"])";
        jsonLd = "{@type:koral:group,"
                + "operation:operation:sequence,"
                + "operands:["
                + "{@type:koral:token,wrap:{@type:koral:term,key:blaue,foundry:opennlp,layer:orth,type:type:regex,match:match:eq}},"
                + "{@type:koral:token,wrap:{@type:koral:term,key:NN,foundry:tt,layer:p,type:type:regex,match:match:eq}}"
                + "]}";
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);
    }

    // -------------------------------------------------------------------------
    // main-query ::= simple-query
    // | simple-query "|" main-query /* or */
    // | simple-query main-query /* sequence */
    // | simple-query quantifier /* quatification */
    // -------------------------------------------------------------------------

    // | simple-query "|" main-query /* or */
    @Test
    public void testOrQuery() throws IOException {
        query = "\"man\"|\"Mann\"";
        jsonLd = "{@type:koral:group,"
                + "operation:operation:disjunction,"
                + "operands:["
                + "{@type:koral:token,wrap:{@type:koral:term,key:man,foundry:opennlp,layer:orth,type:type:regex,match:match:eq}},"
                + "{@type:koral:token,wrap:{@type:koral:term,key:Mann,foundry:opennlp,layer:orth,type:type:regex,match:match:eq}}]}";
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);

        query = "[pos=\"NN\"]|\"Mann\"";
        jsonLd = "{@type:koral:token,wrap:{@type:koral:term,key:NN,foundry:tt,layer:p,type:type:regex,match:match:eq}}";
        FCSQLQueryProcessorTest
                .validateNode(query, "/query/operands/0", jsonLd);

        // group with two segment queries
        query = "[pos=\"NN\"]|[text=\"Mann\"]";
        jsonLd = "{@type:koral:group,"
                + "operation:operation:disjunction,"
                + "operands:["
                + "{@type:koral:token, wrap:{@type:koral:term,key:NN,foundry:tt,layer:p,type:type:regex,match:match:eq}},"
                + "{@type:koral:token, wrap:{@type:koral:term,key:Mann,foundry:opennlp,layer:orth,type:type:regex,match:match:eq}}]}";
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);
    }

    // | simple-query main-query /* sequence */
    @Test
    public void testSequenceQuery() throws IOException {
        query = "\"blaue|grüne\" [pos = \"NN\"]";
        jsonLd = "{@type:koral:group, "
                + "operation:operation:sequence, "
                + "operands:["
                + "{@type:koral:token, wrap:{@type:koral:term, key:blaue|grüne, foundry:opennlp, layer:orth, type:type:regex, match:match:eq}},"
                + "{@type:koral:token, wrap:{@type:koral:term, key:NN, foundry:tt, layer:p, type:type:regex, match:match:eq}}"
                + "]}";
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);

        query = "[text=\"blaue|grüne\"][pos = \"NN\"]";
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);

        query = "\"blaue\" \"grüne\" [pos = \"NN\"]";
        jsonLd = "{@type:koral:token, wrap:{@type:koral:term, key:grüne, foundry:opennlp, layer:orth, type:type:regex, match:match:eq}}";
        FCSQLQueryProcessorTest
                .validateNode(query, "/query/operands/1", jsonLd);

        // sequence and disjunction
        query = "([pos=\"NN\"]|[cnx:pos=\"N\"])[text=\"Mann\"]";
        jsonLd = "{@type:koral:group,"
                + "operation:operation:sequence,"
                + "operands:["
                + "{@type:koral:group,"
                + "operation:operation:disjunction,"
                + "operands:[{@type:koral:token,wrap:{@type:koral:term,key:NN,foundry:tt,layer:p,type:type:regex,match:match:eq}},"
                + "{@type:koral:token,wrap:{@type:koral:term,key:N,foundry:cnx,layer:p,type:type:regex,match:match:eq}}"
                + "]},"
                + "{@type:koral:token,wrap:{@type:koral:term,key:Mann,foundry:opennlp,layer:orth,type:type:regex,match:match:eq}}"
                + "]}";
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);
        
        query = "[text=\"Mann\"]([pos=\"NN\"]|[cnx:pos=\"N\"])";
        jsonLd = "{@type:koral:group,"
                + "operation:operation:disjunction,"
                + "operands:[{@type:koral:token,wrap:{@type:koral:term,key:NN,foundry:tt,layer:p,type:type:regex,match:match:eq}},"
                + "{@type:koral:token,wrap:{@type:koral:term,key:N,foundry:cnx,layer:p,type:type:regex,match:match:eq}}"
                + "]}";
        FCSQLQueryProcessorTest.validateNode(query, "/query/operands/1", jsonLd);
    }

    @Test
    public void testSequenceOfQueryGroups() throws IOException {
        query = "(\"blaue\"|\"grüne\")([pos=\"NN\"]|[cnx:pos=\"N\"])";
        FCSQLQueryProcessorTest.validateNode(query, "/query/@type",
                "koral:group");
        FCSQLQueryProcessorTest.validateNode(query, "/query/operation",
                "operation:sequence");

        jsonLd = "{@type:koral:group,"
                + "operation:operation:disjunction,"
                + "operands:["
                + "{@type:koral:token,wrap:{@type:koral:term,key:blaue,foundry:opennlp,layer:orth,type:type:regex,match:match:eq}},"
                + "{@type:koral:token,wrap:{@type:koral:term,key:grüne,foundry:opennlp,layer:orth,type:type:regex,match:match:eq}}"
                + "]}";
        FCSQLQueryProcessorTest
                .validateNode(query, "/query/operands/0", jsonLd);

        jsonLd = "{@type:koral:group,operation:operation:disjunction,"
                + "operands:["
                + "{@type:koral:token,wrap:{@type:koral:term,key:NN,foundry:tt,layer:p,type:type:regex,match:match:eq}},"
                + "{@type:koral:token,wrap:{@type:koral:term,key:N,foundry:cnx,layer:p,type:type:regex,match:match:eq}}"
                + "]}";
        FCSQLQueryProcessorTest
                .validateNode(query, "/query/operands/1", jsonLd);
    }

    // | simple-query quantifier /* quatification */
    @Test
    public void testSimpleQueryWithQuantifier() throws IOException {
        // repetition
        query = "\"die\"{2}";
        jsonLd = "{@type:koral:group,"
                + "operation:operation:repetition,"
                + "operands:["
                + "{@type:koral:token,wrap:{@type:koral:term,key:die,foundry:opennlp,layer:orth,type:type:regex,match:match:eq}}],"
                + "boundary:{@type:koral:boundary,min:2,max:2}}";
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);

        query = "\"die\"{1,2}";
        jsonLd = "{@type:koral:boundary,min:1,max:2}";
        FCSQLQueryProcessorTest.validateNode(query, "/query/boundary", jsonLd);

        query = "\"die\"{,2}";
        jsonLd = "{@type:koral:boundary,min:0,max:2}";
        FCSQLQueryProcessorTest.validateNode(query, "/query/boundary", jsonLd);

        query = "\"die\"{2,}";
        jsonLd = "{@type:koral:boundary,min:2}";
        FCSQLQueryProcessorTest.validateNode(query, "/query/boundary", jsonLd);

        query = "\"die\"+";
        jsonLd = "{@type:koral:boundary,min:1}";
        FCSQLQueryProcessorTest.validateNode(query, "/query/boundary", jsonLd);

        query = "\"die\"?";
        jsonLd = "{@type:koral:boundary,min:0, max:1}";
        FCSQLQueryProcessorTest.validateNode(query, "/query/boundary", jsonLd);

        query = "\"die\"*";
        jsonLd = "{@type:koral:boundary,min:0}";
        FCSQLQueryProcessorTest.validateNode(query, "/query/boundary", jsonLd);

        query = "\"die\"{0}";
        jsonLd = "{@type:koral:boundary,min:0, max:0}";
        FCSQLQueryProcessorTest.validateNode(query, "/query/boundary", jsonLd);

        // segment query with quantifier
        query = "[cnx:pos=\"A\"]*";
        jsonLd = "{@type:koral:group,"
                + "operation:operation:repetition,"
                + "operands:[{@type:koral:token,wrap:{@type:koral:term,key:A,foundry:cnx,layer:p,type:type:regex,match:match:eq}}],"
                + "boundary:{@type:koral:boundary,min:0}}";
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);
    }

    @Test
    public void testGroupQueryWithQuantifier() throws JsonProcessingException {
        // group with quantifier
        query = "(\"blaue\"|\"grüne\")*";
        jsonLd = "{@type:koral:group,"
                + "operation:operation:repetition,"
                + "operands:["
                + "{@type:koral:group,"
                + "operation:operation:disjunction,"
                + "operands:["
                + "{@type:koral:token, wrap:{@type:koral:term,key:blaue,foundry:opennlp,layer:orth,type:type:regex,match:match:eq}},"
                + "{@type:koral:token, wrap:{@type:koral:term,key:grüne,foundry:opennlp,layer:orth,type:type:regex,match:match:eq}}"
                + "]}]," + "boundary:{@type:koral:boundary,min:0}}";
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);
    }

    // wildcards
    @Test
    public void testQueryWithEmptyToken() throws IOException {
        // expansion query
        query = "[]{2}\"Hund\"";
        jsonLd = "{@type:koral:group, "
                + "operation:operation:sequence, "
                + "operands:["
                + "{@type:koral:group,"
                + "operation:operation:repetition,"
                + "operands:["
                + "{@type:koral:token}],"
                + "boundary:{@type:koral:boundary,min:2,max:2}},"
                + "{@type:koral:token, "
                + "wrap:{@type:koral:term, key:Hund, foundry:opennlp, layer:orth, type:type:regex, match:match:eq}}"
                + "]}";
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);

        query = "\"Hund\"[]{2}";
        jsonLd = "{@type:koral:group," + "operation:operation:repetition,"
                + "operands:[{@type:koral:token}],"
                + "boundary:{@type:koral:boundary,min:2,max:2}}";
        FCSQLQueryProcessorTest
                .validateNode(query, "/query/operands/1", jsonLd);

        // arbitrary tokens
        query = "[]{2}";
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);

        // sequence with extension
        query = "[cnx:pos=\"A\"] \"Hund\"[]{2}";
        jsonLd = "["
                + "{@type:koral:token,wrap:{@type:koral:term,key:A,foundry:cnx,layer:p,type:type:regex,match:match:eq}},"
                + "{@type:koral:token,wrap:{@type:koral:term,key:Hund,foundry:opennlp,layer:orth,type:type:regex,match:match:eq}},"
                + "{@type:koral:group,operation:operation:repetition,operands:["
                + "{@type:koral:token}],boundary:{@type:koral:boundary,min:2,max:2}}"
                + "]";
        FCSQLQueryProcessorTest.validateNode(query, "/query/operands", jsonLd);
    }

    @Test
    public void testDistanceQuery() throws IOException {
        // distance query
        query = "\"Katze\" [] \"Hund\"";
        jsonLd = "{@type:koral:group,operation:operation:sequence,inOrder:true,"
                + "distances:["
                + "{@type:koral:distance,key:w,boundary:{@type:koral:boundary,min:1,max:1}}"
                + "],"
                + "operands:["
                + "{@type:koral:token,wrap:{@type:koral:term,key:Katze,foundry:opennlp,layer:orth,type:type:regex,match:match:eq}},"
                + "{@type:koral:token,wrap:{@type:koral:term,key:Hund,foundry:opennlp,layer:orth,type:type:regex,match:match:eq}}]}";
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);
    }

    @Test
    public void testDistanceQueryWithQuantifier() throws IOException {
        query = "\"Katze\" []* \"Hund\"";
        jsonLd = "{@type:koral:distance,key:w,boundary:{@type:koral:boundary,min:0}}";
        FCSQLQueryProcessorTest.validateNode(query, "/query/distances/0",
                jsonLd);

        query = "\"Katze\" []+ \"Hund\"";
        jsonLd = "{@type:koral:distance,key:w,boundary:{@type:koral:boundary,min:1}}";
        FCSQLQueryProcessorTest.validateNode(query, "/query/distances/0",
                jsonLd);

        query = "\"Katze\" []{3} \"Hund\"";
        jsonLd = "{@type:koral:distance,key:w,boundary:{@type:koral:boundary,min:3,max:3}}";
        FCSQLQueryProcessorTest.validateNode(query, "/query/distances/0",
                jsonLd);

        query = "\"Katze\" []{2,3} \"Hund\"";
        jsonLd = "{@type:koral:distance,key:w,boundary:{@type:koral:boundary,min:2,max:3}}";
        FCSQLQueryProcessorTest.validateNode(query, "/query/distances/0",
                jsonLd);
    }

    @Test
    public void testDistanceQueryWithMultipleWildcards() throws IOException {
        // sequences of wildcards
        query = "\"Katze\" []{3}[] \"Hund\"";
        jsonLd = "{@type:koral:distance,key:w,boundary:{@type:koral:boundary,min:4,max:4}}";
        FCSQLQueryProcessorTest.validateNode(query, "/query/distances/0",
                jsonLd);

        query = "\"Katze\" []{3}[]? \"Hund\"";
        jsonLd = "{@type:koral:distance,key:w,boundary:{@type:koral:boundary,min:3,max:4}}";
        FCSQLQueryProcessorTest.validateNode(query, "/query/distances/0",
                jsonLd);

        query = "\"Katze\" []{2}[]{3}[] \"Hund\"";
        jsonLd = "{@type:koral:distance,key:w,boundary:{@type:koral:boundary,min:6,max:6}}";
        FCSQLQueryProcessorTest.validateNode(query, "/query/distances/0",
                jsonLd);

        // multiple occurrences of wildcards
        query = "\"Katze\" []{3} \"Hund\" []{1,2} [cnx:pos=\"V\"]";
        jsonLd = "{@type:koral:group,"
                + "operation:operation:sequence,"
                + "inOrder:true,"
                + "distances:["
                + "{@type:koral:distance,key:w,boundary:{@type:koral:boundary,min:3,max:3}}"
                + "],"
                + "operands:["
                + "{@type:koral:token,wrap:{@type:koral:term,key:Katze,foundry:opennlp,layer:orth,type:type:regex,match:match:eq}},"
                + "{@type:koral:group,"
                + "operation:operation:sequence,"
                + "inOrder:true,"
                + "distances:["
                + "{@type:koral:distance,key:w,boundary:{@type:koral:boundary,min:1,max:2}}"
                + "],"
                + "operands:["
                + "{@type:koral:token,wrap:{@type:koral:term,key:Hund,foundry:opennlp,layer:orth,type:type:regex,match:match:eq}},"
                + "{@type:koral:token,wrap:{@type:koral:term,key:V,foundry:cnx,layer:p,type:type:regex,match:match:eq}}]}"
                + "]}";
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);
    }

    // -------------------------------------------------------------------------
    // query ::= main-query within-part?
    // -------------------------------------------------------------------------
    // within-part ::= simple-within-part
    // simple-within-part ::= "within" simple-within-scope

    @Test
    public void testWithinQuery() throws IOException {
        query = "'grün' within s";
        jsonLd = "{@type:koral:group,"
                + "operation:operation:position,"
                + "operands:["
                + "{@type:koral:span,wrap:{@type:koral:term,key:s,foundry:base,layer:s}},"
                + "{@type:koral:token,wrap:{@type:koral:term,key:grün,foundry:opennlp,layer:orth,type:type:regex,match:match:eq}}"
                + "]}";
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);
        
        query = "[cnx:pos=\"VVFIN\"] within s";
        jsonLd = "{@type:koral:group,"
                + "operation:operation:position,"
                + "operands:["
                + "{@type:koral:span,wrap:{@type:koral:term,key:s,foundry:base,layer:s}},"
                + "{@type:koral:token,wrap:{@type:koral:term,key:VVFIN,foundry:cnx,layer:p,type:type:regex,match:match:eq}}"
                + "]}";
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);

        query = "[cnx:pos=\"VVFIN\"] within sentence";
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);

        query = "[cnx:pos=\"VVFIN\"] within p";
        jsonLd = "{@type:koral:span,wrap:{@type:koral:term,key:p,foundry:base,layer:s}}";
        FCSQLQueryProcessorTest
                .validateNode(query, "/query/operands/0", jsonLd);

        query = "[cnx:pos=\"VVFIN\"] within text";
        jsonLd = "{@type:koral:span,wrap:{@type:koral:term,key:t,foundry:base,layer:s}}";
        FCSQLQueryProcessorTest
                .validateNode(query, "/query/operands/0", jsonLd);

        query = "[cnx:pos=\"VVFIN\"] within u";
        error = FCSQLQueryProcessorTest.getError(new FCSQLQueryProcessor(query,
                "2.0"));
        assertEquals(311, error.get(0));
        assertEquals(
                "Within scope UTTERANCE is currently unsupported.",
                (String) error.get(1));
    }

    @Test
    public void testWithinQueryWithEmptyTokens() throws IOException {
        query = "[] within s";
        jsonLd = "{@type:koral:group,"
                + "operation:operation:position,"
                + "operands:["
                + "{@type:koral:span,wrap:{@type:koral:term,key:s,foundry:base,layer:s}},"
                + "{@type:koral:token}" + "]}";
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);

        query = "[]+ within s";
        jsonLd = "{@type:koral:group," + "operation:operation:repetition,"
                + "operands:[{@type:koral:token}],"
                + "boundary:{@type:koral:boundary,min:1}}";
        FCSQLQueryProcessorTest
                .validateNode(query, "/query/operands/1", jsonLd);
    }

    @Test
    public void testWithinQueryWithGroupQuery() throws IOException {
        query = "(\"blaue\"|\"grüne\")+ within s";
        jsonLd = "{@type:koral:span,wrap:{@type:koral:term,key:s,foundry:base,layer:s}}";
        FCSQLQueryProcessorTest
                .validateNode(query, "/query/operands/0", jsonLd);
        jsonLd = "{@type:koral:group,"
                + "operation:operation:repetition,"
                + "operands:["
                + "{@type:koral:group,"
                + "operation:operation:disjunction,"
                + "operands:["
                + "{@type:koral:token, wrap:{@type:koral:term,key:blaue,foundry:opennlp,layer:orth,type:type:regex,match:match:eq}},"
                + "{@type:koral:token, wrap:{@type:koral:term,key:grüne,foundry:opennlp,layer:orth,type:type:regex,match:match:eq}}"
                + "]}]," + "boundary:{@type:koral:boundary,min:1}}";
        FCSQLQueryProcessorTest
                .validateNode(query, "/query/operands/1", jsonLd);

    }

    @Test
    public void testWrongQuery() throws IOException {
        
        String errorMessage = "Query cannot be parsed, an unexpcected occured exception while parsing";
        
        // expression should always be within a segment
        query = "!(mate:lemma=\"sein\" | mate:pos=\"PPOSS\")";
        error = FCSQLQueryProcessorTest.getError(new FCSQLQueryProcessor(query,
                "2.0"));
        assertEquals(399, error.get(0));
        assertEquals(errorMessage, error.get(1).toString());

        query = "![mate:lemma=\"sein\" | mate:pos=\"PPOSS\"]";
        error = FCSQLQueryProcessorTest.getError(new FCSQLQueryProcessor(query,
                "2.0"));
        assertEquals(errorMessage, error.get(1).toString());

        query = "(\"blaue\"&\"grüne\")";
        error = FCSQLQueryProcessorTest.getError(new FCSQLQueryProcessor(query,
                "2.0"));
        assertEquals(errorMessage, error.get(1).toString());

        query = "[pos=\"NN\"]&[text=\"Mann\"]";
        error = FCSQLQueryProcessorTest.getError(new FCSQLQueryProcessor(query,
                "2.0"));
        assertEquals(399, error.get(0));
        assertEquals(errorMessage, error.get(1).toString());
    }
}
