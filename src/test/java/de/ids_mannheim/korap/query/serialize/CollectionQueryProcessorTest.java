package de.ids_mannheim.korap.query.serialize;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CollectionQueryProcessorTest {

    String query = "foo";
    String ql = "poliqarpplus";
    String collection;
    ArrayList<JsonNode> operands;

    QuerySerializer qs = new QuerySerializer();
    ObjectMapper mapper = new ObjectMapper();
    JsonNode res;

    @Test
    public void testContext() throws JsonProcessingException, IOException {
        collection = "textClass=politik";
        String contextString = "http://korap.ids-mannheim.de/ns/koral/0.3/context.jsonld";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals(contextString, res.get("@context").asText());
    }

    @Test
    public void testSimple() throws JsonProcessingException, IOException {
        collection = "textClass=politik";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:doc", res.at("/collection/@type").asText());
        assertEquals("textClass", res.at("/collection/key").asText());
        assertEquals("politik", res.at("/collection/value").asText());
        assertEquals("match:eq", res.at("/collection/match").asText());

        collection = "textClass!=politik";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:doc", res.at("/collection/@type").asText());
        assertEquals("textClass", res.at("/collection/key").asText());
        assertEquals("politik", res.at("/collection/value").asText());
        assertEquals("match:ne", res.at("/collection/match").asText());
    }

    @Test
    public void testSpecialCharacters() throws JsonProcessingException, IOException {
        collection = "[base/n=alt]";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());

        assertEquals("koral:token", res.at("/collection/@type").asText());
        assertEquals("koral:term", res.at("/collection/wrap/@type").asText());
        assertEquals("base", res.at("/collection/wrap/foundry").asText());
        assertEquals("n", res.at("/collection/wrap/layer").asText());
        assertEquals("alt", res.at("/collection/wrap/key").asText());
        assertEquals("match:eq", res.at("/collection/wrap/match").asText());
    };

    @Test
    public void testContains() throws JsonProcessingException, IOException {
        collection = "title~Mannheim";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:doc", res.at("/collection/@type").asText());
        assertEquals("title", res.at("/collection/key").asText());
        assertEquals("Mannheim", res.at("/collection/value").asText());
        assertEquals("match:contains", res.at("/collection/match").asText());

        collection = "title~\"IDS Mannheim\"";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:doc", res.at("/collection/@type").asText());
        assertEquals("title", res.at("/collection/key").asText());
        assertEquals("IDS Mannheim", res.at("/collection/value").asText());
        assertEquals("match:contains", res.at("/collection/match").asText());
    }

    @Test
    public void testTwoConjuncts() throws JsonProcessingException, IOException {
        collection = "textClass=Sport & pubDate in 2014";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:docGroup", res.at("/collection/@type").asText());
        assertEquals("operation:and", res.at("/collection/operation").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/0/@type").asText());
        assertEquals("textClass",
                res.at("/collection/operands/0/key").asText());
        assertEquals("Sport", res.at("/collection/operands/0/value").asText());
        assertEquals("match:eq",
                res.at("/collection/operands/0/match").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/1/@type").asText());
        assertEquals("pubDate", res.at("/collection/operands/1/key").asText());
        assertEquals("2014", res.at("/collection/operands/1/value").asText());
        assertEquals("type:date",
                res.at("/collection/operands/1/type").asText());
        assertEquals("match:eq",
                res.at("/collection/operands/1/match").asText());

        collection = "textClass=Sport & pubDate=2014";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:docGroup", res.at("/collection/@type").asText());
        assertEquals("operation:and", res.at("/collection/operation").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/0/@type").asText());
        assertEquals("textClass",
                res.at("/collection/operands/0/key").asText());
        assertEquals("Sport", res.at("/collection/operands/0/value").asText());
        assertEquals("match:eq",
                res.at("/collection/operands/0/match").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/1/@type").asText());
        assertEquals("pubDate", res.at("/collection/operands/1/key").asText());
        assertEquals("2014", res.at("/collection/operands/1/value").asText());
        assertEquals(true,
                res.at("/collection/operands/1/type").isMissingNode());
        assertEquals("match:eq",
                res.at("/collection/operands/1/match").asText());
        assertTrue(res.at("/warnings/0/0").asText().startsWith(
                "The collection query contains a value that looks like a date"));
    }

    @Test
    public void testThreeConjuncts()
            throws JsonProcessingException, IOException {
        collection = "textClass=Sport & pubDate in 2014 & corpusId=WPD";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:docGroup", res.at("/collection/@type").asText());
        assertEquals("operation:and", res.at("/collection/operation").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/0/@type").asText());
        assertEquals("textClass",
                res.at("/collection/operands/0/key").asText());
        assertEquals("Sport", res.at("/collection/operands/0/value").asText());
        assertEquals("match:eq",
                res.at("/collection/operands/0/match").asText());
        assertEquals("koral:docGroup",
                res.at("/collection/operands/1/@type").asText());
        assertEquals("operation:and",
                res.at("/collection/operands/1/operation").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/1/operands/0/@type").asText());
        assertEquals("pubDate",
                res.at("/collection/operands/1/operands/0/key").asText());
        assertEquals("2014",
                res.at("/collection/operands/1/operands/0/value").asText());
        assertEquals("type:date",
                res.at("/collection/operands/1/operands/0/type").asText());
        assertEquals("match:eq",
                res.at("/collection/operands/1/operands/0/match").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/1/operands/1/@type").asText());
        assertEquals("corpusId",
                res.at("/collection/operands/1/operands/1/key").asText());
        assertEquals("WPD",
                res.at("/collection/operands/1/operands/1/value").asText());
        assertEquals("match:eq",
                res.at("/collection/operands/1/operands/1/match").asText());
    }

    @Test
    public void testTwoDisjuncts() throws JsonProcessingException, IOException {
        collection = "textClass=Sport | pubDate in 2014";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:docGroup", res.at("/collection/@type").asText());
        assertEquals("operation:or", res.at("/collection/operation").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/0/@type").asText());
        assertEquals("textClass",
                res.at("/collection/operands/0/key").asText());
        assertEquals("Sport", res.at("/collection/operands/0/value").asText());
        assertEquals("match:eq",
                res.at("/collection/operands/0/match").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/1/@type").asText());
        assertEquals("pubDate", res.at("/collection/operands/1/key").asText());
        assertEquals("2014", res.at("/collection/operands/1/value").asText());
        assertEquals("type:date",
                res.at("/collection/operands/1/type").asText());
        assertEquals("match:eq",
                res.at("/collection/operands/1/match").asText());
    }

    @Test
    public void testThreeDisjuncts()
            throws JsonProcessingException, IOException {
        collection = "textClass=Sport | pubDate in 2014 | corpusId=WPD";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:docGroup", res.at("/collection/@type").asText());
        assertEquals("operation:or", res.at("/collection/operation").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/0/@type").asText());
        assertEquals("textClass",
                res.at("/collection/operands/0/key").asText());
        assertEquals("Sport", res.at("/collection/operands/0/value").asText());
        assertEquals("match:eq",
                res.at("/collection/operands/0/match").asText());
        assertEquals("koral:docGroup",
                res.at("/collection/operands/1/@type").asText());
        assertEquals("operation:or",
                res.at("/collection/operands/1/operation").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/1/operands/0/@type").asText());
        assertEquals("pubDate",
                res.at("/collection/operands/1/operands/0/key").asText());
        assertEquals("2014",
                res.at("/collection/operands/1/operands/0/value").asText());
        assertEquals("type:date",
                res.at("/collection/operands/1/operands/0/type").asText());
        assertEquals("match:eq",
                res.at("/collection/operands/1/operands/0/match").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/1/operands/1/@type").asText());
        assertEquals("corpusId",
                res.at("/collection/operands/1/operands/1/key").asText());
        assertEquals("WPD",
                res.at("/collection/operands/1/operands/1/value").asText());
        assertEquals("match:eq",
                res.at("/collection/operands/1/operands/1/match").asText());
    }

    @Test
    public void testMixed() throws JsonProcessingException, IOException {
        collection = "textClass=Sport | (pubDate in 2014 & corpusId=WPD)";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:docGroup", res.at("/collection/@type").asText());
        assertEquals("operation:or", res.at("/collection/operation").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/0/@type").asText());
        assertEquals("textClass",
                res.at("/collection/operands/0/key").asText());
        assertEquals("Sport", res.at("/collection/operands/0/value").asText());
        assertEquals("match:eq",
                res.at("/collection/operands/0/match").asText());
        assertEquals("koral:docGroup",
                res.at("/collection/operands/1/@type").asText());
        assertEquals("operation:and",
                res.at("/collection/operands/1/operation").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/1/operands/0/@type").asText());
        assertEquals("pubDate",
                res.at("/collection/operands/1/operands/0/key").asText());
        assertEquals("2014",
                res.at("/collection/operands/1/operands/0/value").asText());
        assertEquals("type:date",
                res.at("/collection/operands/1/operands/0/type").asText());
        assertEquals("match:eq",
                res.at("/collection/operands/1/operands/0/match").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/1/operands/1/@type").asText());
        assertEquals("corpusId",
                res.at("/collection/operands/1/operands/1/key").asText());
        assertEquals("WPD",
                res.at("/collection/operands/1/operands/1/value").asText());
        assertEquals("match:eq",
                res.at("/collection/operands/1/operands/1/match").asText());

        collection = "textClass=Sport | pubDate in 2014 & corpusId=WPD";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:docGroup", res.at("/collection/@type").asText());
        assertEquals("operation:or", res.at("/collection/operation").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/0/@type").asText());
        assertEquals("textClass",
                res.at("/collection/operands/0/key").asText());
        assertEquals("Sport", res.at("/collection/operands/0/value").asText());
        assertEquals("match:eq",
                res.at("/collection/operands/0/match").asText());
        assertEquals("koral:docGroup",
                res.at("/collection/operands/1/@type").asText());
        assertEquals("operation:and",
                res.at("/collection/operands/1/operation").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/1/operands/0/@type").asText());
        assertEquals("pubDate",
                res.at("/collection/operands/1/operands/0/key").asText());
        assertEquals("2014",
                res.at("/collection/operands/1/operands/0/value").asText());
        assertEquals("type:date",
                res.at("/collection/operands/1/operands/0/type").asText());
        assertEquals("match:eq",
                res.at("/collection/operands/1/operands/0/match").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/1/operands/1/@type").asText());
        assertEquals("corpusId",
                res.at("/collection/operands/1/operands/1/key").asText());
        assertEquals("WPD",
                res.at("/collection/operands/1/operands/1/value").asText());
        assertEquals("match:eq",
                res.at("/collection/operands/1/operands/1/match").asText());

        collection = "(textClass=Sport | pubDate in 2014) & corpusId=WPD";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:docGroup", res.at("/collection/@type").asText());
        assertEquals("operation:and", res.at("/collection/operation").asText());
        assertEquals("koral:docGroup",
                res.at("/collection/operands/0/@type").asText());
        assertEquals("operation:or",
                res.at("/collection/operands/0/operation").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/0/operands/0/@type").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/0/operands/1/@type").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/1/@type").asText());

        collection = "(textClass=Sport & pubDate in 2014) & corpusId=WPD";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:docGroup", res.at("/collection/@type").asText());
        assertEquals("operation:and", res.at("/collection/operation").asText());
        assertEquals("koral:docGroup",
                res.at("/collection/operands/0/@type").asText());
        assertEquals("operation:and",
                res.at("/collection/operands/0/operation").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/0/operands/0/@type").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/0/operands/1/@type").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/1/@type").asText());

        collection = "(textClass=Sport & textClass=ausland) | (corpusID=WPD & author=White)";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:docGroup", res.at("/collection/@type").asText());
        assertEquals("operation:or", res.at("/collection/operation").asText());
        assertEquals("koral:docGroup",
                res.at("/collection/operands/0/@type").asText());
        assertEquals("operation:and",
                res.at("/collection/operands/0/operation").asText());
        assertEquals("koral:docGroup",
                res.at("/collection/operands/1/@type").asText());
        assertEquals("operation:and",
                res.at("/collection/operands/1/operation").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/0/operands/0/@type").asText());
        assertEquals("Sport",
                res.at("/collection/operands/0/operands/0/value").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/0/operands/1/@type").asText());
        assertEquals("ausland",
                res.at("/collection/operands/0/operands/1/value").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/1/operands/0/@type").asText());
        assertEquals("WPD",
                res.at("/collection/operands/1/operands/0/value").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/1/operands/1/@type").asText());
        assertEquals("White",
                res.at("/collection/operands/1/operands/1/value").asText());

        collection = "(textClass=Sport & textClass=ausland) | (corpusID=WPD & author=White & pubDate in 2000)";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:docGroup", res.at("/collection/@type").asText());
        assertEquals("operation:or", res.at("/collection/operation").asText());
        assertEquals("koral:docGroup",
                res.at("/collection/operands/0/@type").asText());
        assertEquals("operation:and",
                res.at("/collection/operands/0/operation").asText());
        assertEquals("koral:docGroup",
                res.at("/collection/operands/1/@type").asText());
        assertEquals("operation:and",
                res.at("/collection/operands/1/operation").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/0/operands/0/@type").asText());
        assertEquals("Sport",
                res.at("/collection/operands/0/operands/0/value").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/0/operands/1/@type").asText());
        assertEquals("ausland",
                res.at("/collection/operands/0/operands/1/value").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/1/operands/0/@type").asText());
        assertEquals("WPD",
                res.at("/collection/operands/1/operands/0/value").asText());
        assertEquals("koral:docGroup",
                res.at("/collection/operands/1/operands/1/@type").asText());
        assertEquals("operation:and",
                res.at("/collection/operands/1/operands/1/operation").asText());
        assertEquals("White",
                res.at("/collection/operands/1/operands/1/operands/0/value")
                        .asText());
        assertEquals("2000",
                res.at("/collection/operands/1/operands/1/operands/1/value")
                        .asText());
    }

    @Test
    public void testDateYear() throws JsonProcessingException, IOException {
        collection = "pubDate in 2000";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:doc", res.at("/collection/@type").asText());
        assertEquals("pubDate", res.at("/collection/key").asText());
        assertEquals("2000", res.at("/collection/value").asText());
        assertEquals("type:date", res.at("/collection/type").asText());
        assertEquals("match:eq", res.at("/collection/match").asText());

        collection = "pubDate = 2000";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:doc", res.at("/collection/@type").asText());
        assertEquals("pubDate", res.at("/collection/key").asText());
        assertEquals("2000", res.at("/collection/value").asText());
        assertEquals(true, res.at("/collection/type").isMissingNode());
        assertEquals("match:eq", res.at("/collection/match").asText());

        collection = "pubDate since 2000";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:doc", res.at("/collection/@type").asText());
        assertEquals("pubDate", res.at("/collection/key").asText());
        assertEquals("2000", res.at("/collection/value").asText());
        assertEquals("type:date", res.at("/collection/type").asText());
        assertEquals("match:geq", res.at("/collection/match").asText());

        collection = "pubDate until 2000";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:doc", res.at("/collection/@type").asText());
        assertEquals("pubDate", res.at("/collection/key").asText());
        assertEquals("2000", res.at("/collection/value").asText());
        assertEquals("type:date", res.at("/collection/type").asText());
        assertEquals("match:leq", res.at("/collection/match").asText());
    }

    @Test
    public void testDateMonthDay() throws JsonProcessingException, IOException {
        collection = "pubDate in 2000-02";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:doc", res.at("/collection/@type").asText());
        assertEquals("pubDate", res.at("/collection/key").asText());
        assertEquals("2000-02", res.at("/collection/value").asText());
        assertEquals("type:date", res.at("/collection/type").asText());
        assertEquals("match:eq", res.at("/collection/match").asText());

        collection = "pubDate = 2000-12";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:doc", res.at("/collection/@type").asText());
        assertEquals("pubDate", res.at("/collection/key").asText());
        assertEquals("2000-12", res.at("/collection/value").asText());
        assertEquals(true, res.at("/collection/type").isMissingNode());
        assertEquals("match:eq", res.at("/collection/match").asText());

        collection = "pubDate since 2000-02-01";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:doc", res.at("/collection/@type").asText());
        assertEquals("pubDate", res.at("/collection/key").asText());
        assertEquals("2000-02-01", res.at("/collection/value").asText());
        assertEquals("type:date", res.at("/collection/type").asText());
        assertEquals("match:geq", res.at("/collection/match").asText());

        collection = "pubDate until 2000-01-01";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:doc", res.at("/collection/@type").asText());
        assertEquals("pubDate", res.at("/collection/key").asText());
        assertEquals("2000-01-01", res.at("/collection/value").asText());
        assertEquals("type:date", res.at("/collection/type").asText());
        assertEquals("match:leq", res.at("/collection/match").asText());
    }
}
