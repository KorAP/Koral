package de.ids_mannheim.korap.query.serialize;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FCSQLQueryProcessorTest {

    ObjectMapper mapper = new ObjectMapper();

    private void runAndValidate(String query, String jsonLD)
            throws JsonProcessingException {
        FCSQLQueryProcessor processor = new FCSQLQueryProcessor(query, "2.0");
        String serializedQuery = mapper.writeValueAsString(processor
                .getRequestMap().get("query"));
        assertEquals(jsonLD.replace(" ", ""), serializedQuery.replace("\"", ""));
    }

    private List<Object> getError(FCSQLQueryProcessor processor) {
        List<Object> errors = (List<Object>) processor.requestMap.get("errors");
        return (List<Object>) errors.get(0);
    }

    @Test
    public void testTermQuery() throws JsonProcessingException {
        String query = "\"Sonne\"";
        String jsonLd = "{@type:koral:token, wrap:{@type:koral:term, key:Sonne, "
                + "foundry:opennlp, layer:orth, match:match:eq}}";
        runAndValidate(query, jsonLd);
    }

    @Test
    public void testTermQueryWithRegexFlag() throws JsonProcessingException {
        String query = "\"Fliegen\" /c";
        String jsonLd = "{@type:koral:token, wrap:{@type:koral:term, caseInsensitive:true, "
                + "key:Fliegen, foundry:opennlp, layer:orth, match:match:eq}}";
        runAndValidate(query, jsonLd);
    }

    @Test
    public void testTermQueryWithSpecificLayer() throws JsonProcessingException {
        String query = "[text = \"Sonne\"]";
        String jsonLd = "{@type:koral:token, wrap:{@type:koral:term, key:Sonne, "
                + "foundry:opennlp, layer:orth, match:match:eq}}";
        runAndValidate(query, jsonLd);

        query = "[lemma = \"sein\"]";
        jsonLd = "{@type:koral:token, wrap:{@type:koral:term, key:sein, "
                + "foundry:tt, layer:l, match:match:eq}}";
        runAndValidate(query, jsonLd);

        query = "[pos = \"NN\"]";
        jsonLd = "{@type:koral:token, wrap:{@type:koral:term, key:NN, "
                + "foundry:tt, layer:p, match:match:eq}}";
        runAndValidate(query, jsonLd);
    }

    @Test
    public void testTermQueryWithQualifier() throws JsonProcessingException {
        String query = "[mate:lemma = \"sein\"]";
        String jsonLd = "{@type:koral:token, wrap:{@type:koral:term, key:sein, "
                + "foundry:mate, layer:l, match:match:eq}}";
        runAndValidate(query, jsonLd);

        query = "[cnx:pos = \"N\"]";
        jsonLd = "{@type:koral:token, wrap:{@type:koral:term, key:N, "
                + "foundry:cnx, layer:p, match:match:eq}}";
        runAndValidate(query, jsonLd);
    }

    @Test
    public void testTermQueryException() throws JsonProcessingException {
        String query = "[opennlp:lemma = \"sein\"]";
        List<Object> error = getError(new FCSQLQueryProcessor(query, "2.0"));
        assertEquals(306, error.get(0));
        assertEquals(
                "SRU diagnostic 48: Layer lemma with qualifier opennlp is unsupported.",
                error.get(1));

        query = "[malt:lemma = \"sein\"]";
        error = getError(new FCSQLQueryProcessor(query, "2.0"));
        assertEquals(306, error.get(0));
        assertEquals("SRU diagnostic 48: Qualifier malt is unsupported.",
                error.get(1));

        query = "[cnx:morph = \"heit\"]";
        error = getError(new FCSQLQueryProcessor(query, "2.0"));
        assertEquals(306, error.get(0));
        assertEquals("SRU diagnostic 48: Layer morph is unsupported.",
                error.get(1));

    }

    @Test
    public void testMatchOperation() throws JsonProcessingException {
        String query = "[cnx:pos != \"N\"]";
        String jsonLd = "{@type:koral:token, wrap:{@type:koral:term, key:N, "
                + "foundry:cnx, layer:p, match:match:ne}}";
        runAndValidate(query, jsonLd);
    }

    @Test
    public void testSequenceQuery() throws JsonProcessingException {
        String query = "\"blaue\" [pos = \"NN\"]";
        String jsonLd = "{@type:koral:group, operation:operation:sequence, operands:["
                + "{@type:koral:token, wrap:{@type:koral:term, key:blaue, foundry:opennlp, layer:orth, match:match:eq}},"
                + "{@type:koral:token, wrap:{@type:koral:term, key:NN, foundry:tt, layer:p, match:match:eq}}"
                + "]}";
        runAndValidate(query, jsonLd);
    }

    @Test
    public void testAndQuery() throws JsonProcessingException {
        String query = "[text=\"Sonne\" & text=\"scheint\"]";
        String jsonLd = "{@type:koral:group, operation:operation:sequence, inOrder:false,"
                + "distances:["
                + "{@type:koral:distance, key:s, min:0, max:0}],"
                + "operands:["
                + "{@type:koral:token, wrap:{@type:koral:term, key:Sonne, foundry: opennlp, layer:orth, match:match:eq}},"
                + "{@type:koral:token,wrap:{@type:koral:term, key:scheint, foundry: opennlp, layer:orth,match:match:eq}"
                + "}]}";
        runAndValidate(query, jsonLd);

        // sru parser doesnt work for the following queries:
        // String query = "\"Sonne & scheint\"";
        // String query = "\"Sonne&scheint\"";
    }

    @Test
    public void testVersion() throws JsonProcessingException {
        List<Object> error = getError(new FCSQLQueryProcessor("\"Sonne\"",
                "1.0"));
        assertEquals(309, error.get(0));
        assertEquals("SRU diagnostic 5: Only supports SRU version 2.0.",
                error.get(1));

        error = getError(new FCSQLQueryProcessor("\"Sonne\"", null));
        assertEquals(309, error.get(0));
        assertEquals("SRU diagnostic 7: Version number is missing.",
                error.get(1));
    }

}
