package de.ids_mannheim.korap.query.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.z3950.zing.cql.CQLParseException;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ids_mannheim.korap.query.serialize.CqlQueryProcessor;

public class CqlQueryProcessorTest {

    String query;
    String VERSION = "1.2";
    ObjectMapper mapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    private List<Object> getError(CqlQueryProcessor processor) {
        List<Object> errors = (List<Object>) processor.getRequestMap().get("errors");
        return (List<Object>) errors.get(0);
    }

    @Test
    public void testExceptions() throws CQLParseException, IOException {
        query = "(Kuh) prox (Germ) ";
        CqlQueryProcessor cqlTree = new CqlQueryProcessor(query, VERSION);
        List<Object> error = getError(cqlTree);
        assertEquals(
                "SRU diagnostic 48: Only basic search including term-only "
                        + "and boolean (AND,OR) operator queries are currently supported.",
                error.get(1));

        query = "(Kuh) or/rel.combine=sum (Germ) ";
        error = getError(new CqlQueryProcessor(query, VERSION));
        assertEquals(
                "SRU diagnostic 20: Relation modifier rel.combine = sum is not supported.",
                error.get(1));

        query = "dc.title any Germ ";
        error = getError(new CqlQueryProcessor(query, VERSION));
        assertEquals("SRU diagnostic 16: Index dc.title is not supported.",
                error.get(1));

        query = "cql.serverChoice any Germ ";
        error = getError(new CqlQueryProcessor(query, VERSION));
        assertEquals("SRU diagnostic 19: Relation any is not supported.",
                error.get(1));

        query = "";
        error = getError(new CqlQueryProcessor(query, VERSION));
        assertEquals("SRU diagnostic 27: Empty query is unsupported.",
                error.get(1));
    }

    @Test
    public void testAndQuery() throws CQLParseException, IOException, Exception {
        query = "(Sonne) and (scheint)";
        String jsonLd = "{@type : koral:group, operation : operation:sequence, inOrder : false,"
                + "distances:[ "
                + "{@type : cosmas:distance, key : s, boundary:{type:koral:boundary,min:0,max:0}} ],"
                + "operands : ["
                + "{@type : koral:token, wrap : {@type : koral:term,key : Sonne, layer : orth, match : match:eq}},"
                + "{@type : koral:token,wrap : {@type : koral:term,key : scheint,layer : orth,match : match:eq}"
                + "}]}";

        CqlQueryProcessor cqlTree = new CqlQueryProcessor(query, VERSION);
        String serializedQuery = mapper.writeValueAsString(cqlTree
                .getRequestMap().get("query"));
        assertEquals(jsonLd.replace(" ", ""), serializedQuery.replace("\"", ""));
        // /System.out.println(serializedQuery);
        // CosmasTree ct = new CosmasTree("Sonne und scheint");
        // serializedQuery =
        // mapper.writeValueAsString(ct.getRequestMap().get("query"));
        // assertEquals(jsonLd.replace(" ", ""),
        // serializedQuery.replace("\"", ""));
    }

    @Test
    public void testBooleanQuery() throws CQLParseException, IOException,
            Exception {
        query = "((Sonne) or (Mond)) and (scheint)";
        String jsonLd = "{@type:koral:group, operation:operation:sequence, inOrder : false, distances:["
                + "{@type:cosmas:distance, key:s, boundary:{type:koral:boundary,min:0,max:0}}"
                + "], operands:["
                + "{@type:koral:group, operation:operation:or, operands:["
                + "{@type:koral:token, wrap:{@type:koral:term, key:Sonne, layer:orth, match:match:eq}},"
                + "{@type:koral:token, wrap:{@type:koral:term, key:Mond, layer:orth, match:match:eq}}"
                + "]},"
                + "{@type:koral:token, wrap:{@type:koral:term, key:scheint, layer:orth, match:match:eq}}"
                + "]}";
        CqlQueryProcessor cqlTree = new CqlQueryProcessor(query, VERSION);
        String serializedQuery = mapper.writeValueAsString(cqlTree
                .getRequestMap().get("query"));
        assertEquals(jsonLd.replace(" ", ""), serializedQuery.replace("\"", ""));

        query = "(scheint) and ((Sonne) or (Mond))";
        jsonLd = "{@type:koral:group, operation:operation:sequence, inOrder : false, distances:["
                + "{@type:cosmas:distance, key:s, boundary:{type:koral:boundary,min:0,max:0}}"
                + "], operands:["
                + "{@type:koral:token, wrap:{@type:koral:term, key:scheint, layer:orth, match:match:eq}},"
                + "{@type:koral:group, operation:operation:or, operands:["
                + "{@type:koral:token, wrap:{@type:koral:term, key:Sonne, layer:orth, match:match:eq}},"
                + "{@type:koral:token, wrap:{@type:koral:term, key:Mond, layer:orth, match:match:eq}}"
                + "]}" + "]}";
        cqlTree = new CqlQueryProcessor(query, VERSION);
        serializedQuery = mapper.writeValueAsString(cqlTree.getRequestMap()
                .get("query"));
        assertEquals(jsonLd.replace(" ", ""), serializedQuery.replace("\"", ""));

    }

    @Test
    public void testOrQuery() throws CQLParseException, IOException, Exception {
        query = "(Sonne) or (Mond)";
        String jsonLd = "{@type:koral:group, operation:operation:or, operands:["
                + "{@type:koral:token, wrap:{@type:koral:term, key:Sonne, layer:orth, match:match:eq}},"
                + "{@type:koral:token, wrap:{@type:koral:term, key:Mond, layer:orth, match:match:eq}}"
                + "]}";

        CqlQueryProcessor cqlTree = new CqlQueryProcessor(query, VERSION);
        String serializedQuery = mapper.writeValueAsString(cqlTree
                .getRequestMap().get("query"));
        assertEquals(jsonLd.replace(" ", ""), serializedQuery.replace("\"", ""));

        query = "(\"Sonne scheint\") or (Mond)";
        jsonLd = "{@type:koral:group, operation:operation:or, operands:["
                + "{@type:koral:group, operation:operation:sequence, operands:["
                + "{@type:koral:token, wrap:{@type:koral:term, key:Sonne, layer:orth, match:match:eq}},"
                + "{@type:koral:token, wrap:{@type:koral:term, key:scheint, layer:orth, match:match:eq}}"
                + "]},"
                + "{@type:koral:token, wrap:{@type:koral:term, key:Mond, layer:orth, match:match:eq}}"
                + "]}";

        cqlTree = new CqlQueryProcessor(query, VERSION);
        serializedQuery = mapper.writeValueAsString(cqlTree.getRequestMap()
                .get("query"));
        assertEquals(jsonLd.replace(" ", ""), serializedQuery.replace("\"", ""));

        query = "(\"Sonne scheint\") or (\"Mond scheint\")";
        jsonLd = "{@type:koral:group, operation:operation:or, operands:["
                + "{@type:koral:group, operation:operation:sequence, operands:["
                + "{@type:koral:token, wrap:{@type:koral:term, key:Sonne, layer:orth, match:match:eq}},"
                + "{@type:koral:token, wrap:{@type:koral:term, key:scheint, layer:orth, match:match:eq}}"
                + "]},"
                + "{@type:koral:group, operation:operation:sequence, operands:["
                + "{@type:koral:token, wrap:{@type:koral:term, key:Mond, layer:orth, match:match:eq}},"
                + "{@type:koral:token, wrap:{@type:koral:term, key:scheint, layer:orth, match:match:eq}}"
                + "]}" + "]}";
        cqlTree = new CqlQueryProcessor(query, VERSION);
        serializedQuery = mapper.writeValueAsString(cqlTree.getRequestMap()
                .get("query"));
        assertEquals(jsonLd.replace(" ", ""), serializedQuery.replace("\"", ""));
    }

    @Test
    public void testTermQuery() throws CQLParseException, IOException,
            Exception {
        query = "Sonne";
        String jsonLd = "{@type:koral:token, wrap:{@type:koral:term, key:Sonne, layer:orth, match:match:eq}}";
        CqlQueryProcessor cqlTree = new CqlQueryProcessor(query, VERSION);
        String serializedQuery = mapper.writeValueAsString(cqlTree
                .getRequestMap().get("query"));
        assertEquals(jsonLd.replace(" ", ""), serializedQuery.replace("\"", ""));
    }

    @Test
    public void testPhraseQuery() throws CQLParseException, IOException,
            Exception {
        query = "\"der Mann\"";
        String jsonLd = "{@type:koral:group, operation:operation:sequence, operands:["
                + "{@type:koral:token, wrap:{@type:koral:term, key:der, layer:orth, match:match:eq}},"
                + "{@type:koral:token, wrap:{@type:koral:term, key:Mann, layer:orth, match:match:eq}}"
                + "]}";

        CqlQueryProcessor cqlTree = new CqlQueryProcessor(query, VERSION);
        String serializedQuery = mapper.writeValueAsString(cqlTree
                .getRequestMap().get("query"));
        assertEquals(jsonLd.replace(" ", ""), serializedQuery.replace("\"", ""));

        query = "der Mann schläft";
        jsonLd = "{@type:koral:group, operation:operation:sequence, operands:["
                + "{@type:koral:token, wrap:{@type:koral:term, key:der, layer:orth, match:match:eq}},"
                + "{@type:koral:token, wrap:{@type:koral:term, key:Mann, layer:orth, match:match:eq}},"
                + "{@type:koral:token, wrap:{@type:koral:term, key:schläft, layer:orth, match:match:eq}}"
                + "]}";

        cqlTree = new CqlQueryProcessor(query, VERSION);
        serializedQuery = mapper.writeValueAsString(cqlTree.getRequestMap()
                .get("query"));
        assertEquals(jsonLd.replace(" ", ""), serializedQuery.replace("\"", ""));
    }
}
