package de.ids_mannheim.korap.query.serialize;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author margaretha
 * 
 */
public class FCSQLQueryProcessorTest {

    static QuerySerializer qs = new QuerySerializer();
    static ObjectMapper mapper = new ObjectMapper();
    static JsonNode node;
    String query;
    String jsonLd;
    List<Object> error;

    public static void runAndValidate(String query, String jsonLd)
            throws JsonProcessingException {
        FCSQLQueryProcessor processor = new FCSQLQueryProcessor(query, "2.0");
        String serializedQuery = mapper.writeValueAsString(processor
                .getRequestMap().get("query"));
        assertEquals(jsonLd.replace(" ", ""), serializedQuery.replace("\"", ""));
    }

    public static void validateNode(String query, String path, String jsonLd)
            throws JsonProcessingException, IOException {
        qs.setQuery(query, "fcsql", "2.0");
        node = mapper.readTree(qs.toJSON());
        String serializedQuery = mapper.writeValueAsString(node.at(path));
        assertEquals(jsonLd.replace(" ", ""), serializedQuery.replace("\"", ""));
    }

    public static List<Object> getError(FCSQLQueryProcessor processor) {
        List<Object> errors = (List<Object>) processor.requestMap.get("errors");
        return (List<Object>) errors.get(0);
    }

    @Test
    public void testVersion() throws JsonProcessingException {
        error = getError(new FCSQLQueryProcessor("\"Sonne\"",
                "1.0"));
        assertEquals(309, error.get(0));
        assertEquals("SRU diagnostic 5: Only supports SRU version 2.0.",
                error.get(1));

        error = getError(new FCSQLQueryProcessor("\"Sonne\"", null));
        assertEquals(309, error.get(0));
        assertEquals("SRU diagnostic 7: Version number is missing.",
                error.get(1));
    }

    // regexp ::= quoted-string
    @Test
    public void testTermQuery() throws JsonProcessingException {
        query = "\"Sonne\"";
        jsonLd = "{@type:koral:token, wrap:{@type:koral:term, key:Sonne, "
                + "foundry:opennlp, layer:orth, type:type:regex, match:match:eq}}";
        runAndValidate(query, jsonLd);
    }

    @Test
    public void testRegex() throws JsonProcessingException {
        query = "[text=\"M(a|ä)nn(er)?\"]";
        jsonLd = "{@type:koral:token,wrap:{@type:koral:term,"
                + "key:M(a|ä)nn(er)?,foundry:opennlp,layer:orth,type:type:regex,match:match:eq}}";
        runAndValidate(query, jsonLd);

        query = "\".*?Mann.*?\"";
        jsonLd = "{@type:koral:token,wrap:{@type:koral:term,key:.*?Mann.*?,"
                + "foundry:opennlp,layer:orth,type:type:regex,match:match:eq}}";
        runAndValidate(query, jsonLd);

        query = "\"z.B.\"";
        jsonLd = "{@type:koral:token,wrap:{@type:koral:term,key:z.B.,"
                + "foundry:opennlp,layer:orth,type:type:regex,match:match:eq}}";
        runAndValidate(query, jsonLd);

        query = "\"Sonne&scheint\"";
        jsonLd = "{@type:koral:token,wrap:{@type:koral:term,key:Sonne&scheint,"
                + "foundry:opennlp,layer:orth,type:type:regex,match:match:eq}}";
        runAndValidate(query, jsonLd);

        // Not possible
        // query = "\"a\\.\"";
    }

    // flagged-regexp ::= regexp
    // | regexp "/" regexp-flag+
    @Test
    public void testTermQueryWithRegexFlag() throws IOException {
        query = "\"Fliegen\" /c";
        jsonLd = "{@type:koral:token, wrap:{@type:koral:term, caseInsensitive:true, "
                + "key:Fliegen, foundry:opennlp, layer:orth, type:type:regex, match:match:eq}}";
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);

        query = "\"Fliegen\" /i";
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);

        query = "\"Fliegen\" /C";
        jsonLd = "{@type:koral:term, key:Fliegen, foundry:opennlp, layer:orth, type:type:regex, match:match:eq}";
        FCSQLQueryProcessorTest.validateNode(query, "/query/wrap", jsonLd);

        query = "\"Fliegen\" /I";
        FCSQLQueryProcessorTest.validateNode(query, "/query/wrap", jsonLd);

        query = "\"Fliegen\" /l";
        error = FCSQLQueryProcessorTest
                .getError(new FCSQLQueryProcessor(query, "2.0"));
        assertEquals(306, error.get(0));
        String msg = (String) error.get(1);
        assertEquals(true, msg.startsWith("SRU diagnostic 48: Regexflags"));

        query = "\"Fliegen\" /d";
        error = FCSQLQueryProcessorTest.getError(new FCSQLQueryProcessor(query,
                "2.0"));
        assertEquals(306, error.get(0));
        assertEquals(
                "SRU diagnostic 48: Regexflag: IGNORE_DIACRITICS is unsupported.",
                (String) error.get(1));
    }

    // operator ::= "=" /* equals */
    // | "!=" /* non-equals */
    @Test
    public void testOperator() throws IOException {
        query = "[cnx:pos != \"N\"]";
        jsonLd = "{@type:koral:token, wrap:{@type:koral:term, key:N, "
                + "foundry:cnx, layer:p, type:type:regex, match:match:ne}}";
        runAndValidate(query, jsonLd);
    }


    // attribute operator flagged-regexp
    // -------------------------------------------------------------------------
    // attribute ::= simple-attribute | qualified-attribute
    // -------------------------------------------------------------------------

    // simple-attribute ::= identifier
    @Test
    public void testTermQueryWithSpecificLayer() throws JsonProcessingException {
        query = "[text = \"Sonne\"]";
        jsonLd = "{@type:koral:token, wrap:{@type:koral:term, key:Sonne, "
                + "foundry:opennlp, layer:orth, type:type:regex, match:match:eq}}";
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);

        query = "[lemma = \"sein\"]";
        jsonLd = "{@type:koral:token, wrap:{@type:koral:term, key:sein, "
                + "foundry:tt, layer:l, type:type:regex, match:match:eq}}";
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);

        query = "[pos = \"NN\"]";
        jsonLd = "{@type:koral:token, wrap:{@type:koral:term, key:NN, "
                + "foundry:tt, layer:p, type:type:regex, match:match:eq}}";
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);
    }

    // qualified-attribute ::= identifier ":" identifier
    @Test
    public void testTermQueryWithQualifier() throws JsonProcessingException {
        query = "[mate:lemma = \"sein\"]";
        jsonLd = "{@type:koral:token, wrap:{@type:koral:term, key:sein, "
                + "foundry:mate, layer:l, type:type:regex, match:match:eq}}";
        runAndValidate(query, jsonLd);

        query = "[cnx:pos = \"N\"]";
        jsonLd = "{@type:koral:token, wrap:{@type:koral:term, key:N, "
                + "foundry:cnx, layer:p, type:type:regex, match:match:eq}}";
        runAndValidate(query, jsonLd);
    }


    // segment-query ::= "[" expression? "]"
    // -------------------------------------------------------------------------
    // expression ::= basic-expression
    // | expression "|" expression /* or */
    // | expression "&" expression /* and */
    // -------------------------------------------------------------------------

    // | expression "|" expression /* or */
    @Test
    public void testExpressionOr() throws IOException {
        query = "[mate:lemma=\"sein\" | mate:pos=\"PPOSS\"]";
        jsonLd = "{@type: koral:token,"
                + " wrap: { @type: koral:termGroup,"
                + "relation: relation:or,"
                + " operands:["
                + "{@type: koral:term, key: sein, foundry: mate, layer: l, type:type:regex, match: match:eq},"
                + "{@type: koral:term, key: PPOSS, foundry: mate, layer: p, type:type:regex, match: match:eq}]}}";
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);

        query = "[cnx:lemma=\"sein\" | mate:lemma=\"sein\" | mate:pos=\"PPOSS\"]";
        jsonLd = "{@type: koral:term, key: sein, foundry: cnx, layer: l, type:type:regex, match: match:eq}";
        FCSQLQueryProcessorTest.validateNode(query, "/query/wrap/operands/0",
                jsonLd);

    }

    // | expression "&" expression /* and */
    @Test
    public void testExpressionAnd() throws IOException {
        query = "[mate:lemma=\"sein\" & mate:pos=\"PPOSS\"]";
        jsonLd = "{@type: koral:token,"
                + " wrap: { @type: koral:termGroup,"
                + "relation: relation:and,"
                + " operands:["
                + "{@type: koral:term, key: sein, foundry: mate, layer: l, type:type:regex, match: match:eq},"
                + "{@type: koral:term, key: PPOSS, foundry: mate, layer: p, type:type:regex, match: match:eq}]}}";
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);
    }

    // -------------------------------------------------------------------------
    // basic-expression ::= '(' expression ')' /* grouping */
    // | "!" expression /* not */
    // | attribute operator flagged-regexp
    // -------------------------------------------------------------------------

    // basic-expression ::= '(' expression ')' /* grouping */

    @Test
    public void testExpressionGroup() throws JsonProcessingException {
        query = "[(text=\"blau\"|pos=\"ADJ\")]";
        jsonLd = "{@type: koral:token,"
                + "wrap: {@type: koral:termGroup,"
                + "relation: relation:or,"
                + "operands: ["
                + "{@type: koral:term, key: blau, foundry: opennlp, layer: orth, type:type:regex,match: match:eq},"
                + "{@type: koral:term, key: ADJ, foundry: tt, layer: p, type:type:regex, match: match:eq}]}}";
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);
    }

    // "!" expression /* not */
    @Test
    public void testExpressionNot() throws IOException {
        jsonLd = "{@type:koral:token, wrap:{@type:koral:term, key:NN, "
                + "foundry:tt, layer:p, type:type:regex, match:match:eq}}";
        query = "[!pos != \"NN\"]";
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);
        query = "[!!pos = \"NN\"]";
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);
        query = "[!!!pos != \"NN\"]";
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);

        query = "[mate:lemma=\"sein\" & !mate:pos=\"PPOSS\"]";
        jsonLd = "{@type: koral:token,"
                + " wrap: { "
                + "@type: koral:termGroup,"
                + "relation: relation:and,"
                + " operands:["
                + "{@type: koral:term, key: sein, foundry: mate, layer: l, type:type:regex, match: match:eq},"
                + "{@type: koral:term, key: PPOSS, foundry: mate, layer: p, type:type:regex, match: match:ne}]}}";
        FCSQLQueryProcessorTest.runAndValidate(query, jsonLd);
    }

    @Test
    public void testExceptions() throws JsonProcessingException {
        // unsupported lemma und qualifier
        query = "[opennlp:lemma = \"sein\"]";
        error = getError(new FCSQLQueryProcessor(query, "2.0"));
        assertEquals(306, error.get(0));
        assertEquals(
                "SRU diagnostic 48: Layer lemma with qualifier opennlp is unsupported.",
                error.get(1));

        query = "[tt:morph = \"sein\"]";
        error = getError(new FCSQLQueryProcessor(query, "2.0"));
        assertEquals(306, error.get(0));
        assertEquals(
                "SRU diagnostic 48: Layer morph is unsupported.",
                error.get(1));
        
        // unsupported qualifier
        query = "[malt:lemma = \"sein\"]";
        error = getError(new FCSQLQueryProcessor(query, "2.0"));
        assertEquals(306, error.get(0));
        assertEquals("SRU diagnostic 48: Qualifier malt is unsupported.",
                error.get(1));

        // unsupported layer
        query = "[cnx:morph = \"heit\"]";
        error = getError(new FCSQLQueryProcessor(query, "2.0"));
        assertEquals(306, error.get(0));
        assertEquals("SRU diagnostic 48: Layer morph is unsupported.",
                error.get(1));

        // missing layer
        query = "[cnx=\"V\"]";
        error = getError(new FCSQLQueryProcessor(query, "2.0"));
        assertEquals(306, error.get(0));
        assertEquals("SRU diagnostic 48: Layer cnx is unsupported.",
                error.get(1));
    }
}
