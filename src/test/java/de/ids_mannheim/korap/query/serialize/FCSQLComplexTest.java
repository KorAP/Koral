package de.ids_mannheim.korap.query.serialize;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

/**
 * @author margaretha
 * 
 */
public class FCSQLComplexTest {

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
        String query = "(\"blaue\"|\"grüne\")";
        String jsonLd = "{@type:koral:group,"
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

        // group and sequence
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
        String query = "\"man\"|\"Mann\"";
        String jsonLd = "{@type:koral:group,"
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

        query = "[pos=\"NN\"]&[text=\"Mann\"]";
        List<Object> error = FCSQLQueryProcessorTest
                .getError(new FCSQLQueryProcessor(query, "2.0"));
        assertEquals(399, error.get(0));
        String msg = (String) error.get(1);
        assertEquals(true, msg.startsWith("FCS diagnostic 10"));
    }

    // | simple-query main-query /* sequence */
    @Test
    public void testSequenceQuery() throws IOException {
        String query = "\"blaue|grüne\" [pos = \"NN\"]";
        String jsonLd = "{@type:koral:group, "
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

    }

    // | simple-query quantifier /* quatification */
    @Test
    public void testQueryWithQuantifier() throws IOException {

    }

    // -------------------------------------------------------------------------
    // query ::= main-query within-part?
    // -------------------------------------------------------------------------
    // within-part ::= simple-within-part
    // simple-within-part ::= "within" simple-within-scope

    @Test
    public void testWithinQuery() throws IOException {
        String query = "[cnx:pos=\"VVFIN\"] within s";
        String jsonLd = "{@type:koral:group,"
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
        List<Object> error = FCSQLQueryProcessorTest
                .getError(new FCSQLQueryProcessor(query, "2.0"));
        assertEquals(310, error.get(0));
        assertEquals(
                "FCS diagnostic 11: Within scope UTTERANCE is currently unsupported.",
                (String) error.get(1));
    }
    
}
