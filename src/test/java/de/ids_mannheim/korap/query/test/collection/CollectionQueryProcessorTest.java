package de.ids_mannheim.korap.query.test.collection;

import java.io.IOException;

import org.junit.Test;
import org.junit.Ignore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ids_mannheim.korap.query.serialize.QuerySerializer;
import de.ids_mannheim.korap.query.serialize.QueryUtils;

import static org.junit.Assert.*;

public class CollectionQueryProcessorTest {

    private String query = "foo";
    private String ql = "poliqarpplus";
    private String collection;

    public static final QuerySerializer qs = new QuerySerializer();
    public static final ObjectMapper mapper = new ObjectMapper();
    private JsonNode res;

    @Test
    public void testVCRef () throws IOException {
        collection = "referTo vc-filename";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:docGroupRef", res.at("/collection/@type").asText());
        assertEquals("vc-filename", res.at("/collection/ref").asText());
        
        collection = "referTo \"mickey/MyVC\"";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:docGroupRef", res.at("/collection/@type").asText());
        assertEquals("mickey/MyVC", res.at("/collection/ref").asText());

        collection = "referTo \"http://korap.ids-mannheim.de/user/vc/myCorpus\"";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:docGroupRef", res.at("/collection/@type").asText());
        assertEquals("http://korap.ids-mannheim.de/user/vc/myCorpus", res.at("/collection/ref").asText());
	}

    @Test
    public void testNestedVCRef () throws IOException {
        collection = "availability = /CC-BY.*/ & referTo \"DeReKo-CoRoLa-comp-subcorpus\"";
        qs.setQuery(query, ql);
        qs.setCollection(collection);

        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:docGroup", res.at("/collection/@type").asText());
        assertEquals("koral:doc", res.at("/collection/operands/0/@type").asText());

        assertEquals("type:regex", res.at("/collection/operands/0/type").asText());
        assertEquals("availability", res.at("/collection/operands/0/key").asText());
        assertEquals("CC-BY.*", res.at("/collection/operands/0/value").asText());
        
        assertEquals("koral:docGroupRef", res.at("/collection/operands/1/@type").asText());
        assertEquals("DeReKo-CoRoLa-comp-subcorpus", res.at("/collection/operands/1/ref").asText());

        collection = "(availability = /CC-BY.*/ & referTo \"DeReKo-CoRoLa-comp-subcorpus\")";
        qs.setQuery(query, ql);
        qs.setCollection(collection);

        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:docGroup", res.at("/collection/@type").asText());
        assertEquals("koral:doc", res.at("/collection/operands/0/@type").asText());

        assertEquals("type:regex", res.at("/collection/operands/0/type").asText());
        assertEquals("availability", res.at("/collection/operands/0/key").asText());
        assertEquals("CC-BY.*", res.at("/collection/operands/0/value").asText());
        
        assertEquals("koral:docGroupRef", res.at("/collection/operands/1/@type").asText());
        assertEquals("DeReKo-CoRoLa-comp-subcorpus", res.at("/collection/operands/1/ref").asText());


    }
    
    @Test
    public void testContext () throws JsonProcessingException, IOException {
        collection = "textClass=politik";
        String contextString = "http://korap.ids-mannheim.de/ns/koral/0.3/context.jsonld";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals(contextString, res.get("@context").asText());
    }


    @Test
    public void testSimple () throws JsonProcessingException, IOException {
        collection = "textClass=politik";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:doc", res.at("/collection/@type").asText());
        assertEquals("textClass", res.at("/collection/key").asText());
        assertEquals("politik", res.at("/collection/value").asText());
        assertNotEquals("type:regex", res.at("/collection/type").asText());
        assertEquals("match:eq", res.at("/collection/match").asText());

        collection = "textClass!=politik";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:doc", res.at("/collection/@type").asText());
        assertEquals("textClass", res.at("/collection/key").asText());
        assertEquals("politik", res.at("/collection/value").asText());
        assertNotEquals("type:regex", res.at("/collection/type").asText());
        assertEquals("match:ne", res.at("/collection/match").asText());
    }

    
    @Test
    public void testSpecialCharacters () throws JsonProcessingException,
            IOException {
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
    }


    @Test
    public void testContains () throws JsonProcessingException, IOException {
        collection = "title~Mannheim";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:doc", res.at("/collection/@type").asText());
        assertEquals("title", res.at("/collection/key").asText());
        assertEquals("Mannheim", res.at("/collection/value").asText());
        assertEquals("match:contains", res.at("/collection/match").asText());
    }

   @Test
    public void testVerbatim () throws JsonProcessingException, IOException {
        collection = "title~\"IDS Mannheim\"";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:doc", res.at("/collection/@type").asText());
        assertEquals("title", res.at("/collection/key").asText());
        assertEquals("IDS Mannheim", res.at("/collection/value").asText());
        assertEquals("match:contains", res.at("/collection/match").asText());
		
        collection = "title~\"IDS:Mannheim\"";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:doc", res.at("/collection/@type").asText());
        assertEquals("title", res.at("/collection/key").asText());
        assertEquals("IDS:Mannheim", res.at("/collection/value").asText());
        assertEquals("match:contains", res.at("/collection/match").asText());

		// With escapes
		collection = "title~\"IDS \\\"Mon\\\\nem\\\"\"";
		qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:doc", res.at("/collection/@type").asText());
        assertEquals("title", res.at("/collection/key").asText());
        assertEquals("IDS \"Mon\\nem\"", res.at("/collection/value").asText());
        assertEquals("match:contains", res.at("/collection/match").asText());
   }

    @Test
    public void testVerbatimSpecial () throws JsonProcessingException, IOException {
        collection = "corpusAuthor=\"Goethe, Johann Wolfgang von\"";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:doc", res.at("/collection/@type").asText());
        assertEquals("corpusAuthor", res.at("/collection/key").asText());
        assertEquals("Goethe, Johann Wolfgang von", res.at("/collection/value").asText());
        assertEquals("match:eq", res.at("/collection/match").asText());
    }

    @Test
    public void testVerbatimNonGreedy () throws JsonProcessingException, IOException {
        collection = "foundries=\"corenlp/constituency\" & foundries=\"corenlp/morpho\"";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:docGroup", res.at("/collection/@type").asText());
        assertEquals("operation:and", res.at("/collection/operation").asText());
        assertEquals("koral:doc", res.at("/collection/operands/0/@type").asText());
        assertEquals("foundries", res.at("/collection/operands/0/key").asText());
        assertEquals("corenlp/constituency", res.at("/collection/operands/0/value").asText());
        assertEquals("koral:doc", res.at("/collection/operands/1/@type").asText());
        assertEquals("foundries", res.at("/collection/operands/1/key").asText());
        assertEquals("corenlp/morpho", res.at("/collection/operands/1/value").asText());
    }

    @Test
    public void testVerbatimApo () throws JsonProcessingException, IOException {
        collection = "corpusTitle=\"C't\"";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        System.err.println(qs.toJSON());
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:doc", res.at("/collection/@type").asText());
        assertEquals("corpusTitle", res.at("/collection/key").asText());
        assertEquals("C't", res.at("/collection/value").asText());
        assertEquals("match:eq", res.at("/collection/match").asText());
    }   
    
    @Test
    public void testFlag () throws JsonProcessingException, IOException {
        collection = "textClass=politik/i";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:doc", res.at("/collection/@type").asText());
        assertEquals("textClass", res.at("/collection/key").asText());
        assertEquals("politik", res.at("/collection/value").asText());
        assertEquals("match:eq", res.at("/collection/match").asText());
        
    }
    
	@Test
    public void testRegex () throws JsonProcessingException, IOException {
        collection = "textClass=/politik/";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:doc", res.at("/collection/@type").asText());
        assertEquals("textClass", res.at("/collection/key").asText());
        assertEquals("politik", res.at("/collection/value").asText());
        assertEquals("type:regex", res.at("/collection/type").asText());
        assertEquals("match:eq", res.at("/collection/match").asText());

        collection = "textClass=/politik/ or textClass=/kultur.*/";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:docGroup", res.at("/collection/@type").asText());
        assertEquals("operation:or", res.at("/collection/operation").asText());

        assertEquals("koral:doc", res.at("/collection/operands/0/@type").asText());
        assertEquals("textClass", res.at("/collection/operands/0/key").asText());
        assertEquals("politik", res.at("/collection/operands/0/value").asText());
        assertEquals("match:eq", res.at("/collection/operands/0/match").asText());
        assertEquals("type:regex", res.at("/collection/operands/0/type").asText());

		assertEquals("koral:doc", res.at("/collection/operands/1/@type").asText());
        assertEquals("textClass", res.at("/collection/operands/1/key").asText());
        assertEquals("kultur.*", res.at("/collection/operands/1/value").asText());
        assertEquals("match:eq", res.at("/collection/operands/1/match").asText());
        assertEquals("type:regex", res.at("/collection/operands/1/type").asText());
    }

	@Test
    public void testRegexAlternative () throws JsonProcessingException, IOException {
        collection = "textClass=/(politik|kultur)/";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:doc", res.at("/collection/@type").asText());
        assertEquals("textClass", res.at("/collection/key").asText());
        assertEquals("(politik|kultur)", res.at("/collection/value").asText());
        assertEquals("type:regex", res.at("/collection/type").asText());
        assertEquals("match:eq", res.at("/collection/match").asText());
    }

	@Test
    public void testRegexEscape () throws JsonProcessingException, IOException {    
        collection = "textClass=/po\\/litik/";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:doc", res.at("/collection/@type").asText());
        assertEquals("textClass", res.at("/collection/key").asText());
        assertEquals("po/litik", res.at("/collection/value").asText());
        assertEquals("type:regex", res.at("/collection/type").asText());
        assertEquals("match:eq", res.at("/collection/match").asText());
    } 

	@Test
    public void testRegexApos () throws JsonProcessingException, IOException {    
        collection = "textClass=/po.'litik/";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:doc", res.at("/collection/@type").asText());
        assertEquals("textClass", res.at("/collection/key").asText());
        assertEquals("po.'litik", res.at("/collection/value").asText());
        assertEquals("type:regex", res.at("/collection/type").asText());
        assertEquals("match:eq", res.at("/collection/match").asText());
    } 
    
	@Test
    public void testRegexFailure () throws JsonProcessingException, IOException {    
        collection = "textClass=/po/litik/";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        qs.resetMsgs();
        assertEquals("302", res.at("/errors/0/0").asText());
        assertEquals("Could not parse query >>> textClass=/po/litik/ <<<.",
                     res.at("/errors/0/1").asText());
        assertEquals("", res.at("/errors/1/0").asText());
    }

    @Test
    public void testNotDate () throws JsonProcessingException, IOException {
        collection = "author=\"firefighter1974\"";
        qs.resetMsgs();
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:doc", res.at("/collection/@type").asText());
        assertEquals("author", res.at("/collection/key").asText());
        assertEquals("firefighter1974", res.at("/collection/value").asText());
        assertEquals("match:eq", res.at("/collection/match").asText());
        assertEquals("", res.at("/errors/0/0").asText());
        assertEquals("", res.at("/warnings/0/0").asText());
    }


    @Test
    public void testTwoConjuncts () throws JsonProcessingException, IOException {
        collection = "textClass=Sport & pubDate in 2014";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:docGroup", res.at("/collection/@type").asText());
        assertEquals("operation:and", res.at("/collection/operation").asText());
        assertEquals("koral:doc", res.at("/collection/operands/0/@type")
                .asText());
        assertEquals("textClass", res.at("/collection/operands/0/key").asText());
        assertEquals("Sport", res.at("/collection/operands/0/value").asText());
        assertEquals("match:eq", res.at("/collection/operands/0/match")
                .asText());
        assertEquals("koral:doc", res.at("/collection/operands/1/@type")
                .asText());
        assertEquals("pubDate", res.at("/collection/operands/1/key").asText());
        assertEquals("2014", res.at("/collection/operands/1/value").asText());
        assertEquals("type:date", res.at("/collection/operands/1/type")
                .asText());
        assertEquals("match:eq", res.at("/collection/operands/1/match")
                .asText());

        collection = "textClass=Sport & pubDate=2014";
        qs.setQuery(query, ql);
        qs.setCollection(collection);

        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:docGroup", res.at("/collection/@type").asText());
        assertEquals("operation:and", res.at("/collection/operation").asText());
        assertEquals("koral:doc", res.at("/collection/operands/0/@type")
                .asText());
        assertEquals("textClass", res.at("/collection/operands/0/key").asText());
        assertEquals("Sport", res.at("/collection/operands/0/value").asText());
        assertEquals("match:eq", res.at("/collection/operands/0/match")
                .asText());
        assertEquals("koral:doc", res.at("/collection/operands/1/@type")
                .asText());
        assertEquals("pubDate", res.at("/collection/operands/1/key").asText());
        assertEquals("2014", res.at("/collection/operands/1/value").asText());
        assertTrue(res.at("/collection/operands/1/type")
                .isMissingNode());
        assertEquals("match:eq", res.at("/collection/operands/1/match")
                .asText());
        assertTrue(res.at("/warnings/0").isMissingNode());
        //        assertTrue(res.at("/warnings/0/0").asText().startsWith(
        //                "The collection query contains a value that looks like a date"));
    }


    @Test
    public void testThreeConjuncts () throws JsonProcessingException,
            IOException {
        collection = "textClass=Sport & pubDate in 2014 & corpusId=WPD";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:docGroup", res.at("/collection/@type").asText());
        assertEquals("operation:and", res.at("/collection/operation").asText());
        assertEquals("koral:doc", res.at("/collection/operands/0/@type")
                .asText());
        assertEquals("textClass", res.at("/collection/operands/0/key").asText());
        assertEquals("Sport", res.at("/collection/operands/0/value").asText());
        assertEquals("match:eq", res.at("/collection/operands/0/match")
                .asText());
        assertEquals("koral:docGroup", res.at("/collection/operands/1/@type")
                .asText());
        assertEquals("operation:and", res
                .at("/collection/operands/1/operation").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/1/operands/0/@type").asText());
        assertEquals("pubDate", res.at("/collection/operands/1/operands/0/key")
                .asText());
        assertEquals("2014", res.at("/collection/operands/1/operands/0/value")
                .asText());
        assertEquals("type:date",
                res.at("/collection/operands/1/operands/0/type").asText());
        assertEquals("match:eq",
                res.at("/collection/operands/1/operands/0/match").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/1/operands/1/@type").asText());
        assertEquals("corpusId", res
                .at("/collection/operands/1/operands/1/key").asText());
        assertEquals("WPD", res.at("/collection/operands/1/operands/1/value")
                .asText());
        assertEquals("match:eq",
                res.at("/collection/operands/1/operands/1/match").asText());
    }


    @Test
    public void testTwoDisjuncts () throws JsonProcessingException, IOException {
        collection = "textClass=Sport | pubDate in 2014";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:docGroup", res.at("/collection/@type").asText());
        assertEquals("operation:or", res.at("/collection/operation").asText());
        assertEquals("koral:doc", res.at("/collection/operands/0/@type")
                .asText());
        assertEquals("textClass", res.at("/collection/operands/0/key").asText());
        assertEquals("Sport", res.at("/collection/operands/0/value").asText());
        assertEquals("match:eq", res.at("/collection/operands/0/match")
                .asText());
        assertEquals("koral:doc", res.at("/collection/operands/1/@type")
                .asText());
        assertEquals("pubDate", res.at("/collection/operands/1/key").asText());
        assertEquals("2014", res.at("/collection/operands/1/value").asText());
        assertEquals("type:date", res.at("/collection/operands/1/type")
                .asText());
        assertEquals("match:eq", res.at("/collection/operands/1/match")
                .asText());
    }


    @Test
    public void testThreeDisjuncts () throws JsonProcessingException,
            IOException {
        collection = "textClass=Sport | pubDate in 2014 | corpusId=WPD";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:docGroup", res.at("/collection/@type").asText());
        assertEquals("operation:or", res.at("/collection/operation").asText());
        assertEquals("koral:doc", res.at("/collection/operands/0/@type")
                .asText());
        assertEquals("textClass", res.at("/collection/operands/0/key").asText());
        assertEquals("Sport", res.at("/collection/operands/0/value").asText());
        assertEquals("match:eq", res.at("/collection/operands/0/match")
                .asText());
        assertEquals("koral:docGroup", res.at("/collection/operands/1/@type")
                .asText());
        assertEquals("operation:or", res.at("/collection/operands/1/operation")
                .asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/1/operands/0/@type").asText());
        assertEquals("pubDate", res.at("/collection/operands/1/operands/0/key")
                .asText());
        assertEquals("2014", res.at("/collection/operands/1/operands/0/value")
                .asText());
        assertEquals("type:date",
                res.at("/collection/operands/1/operands/0/type").asText());
        assertEquals("match:eq",
                res.at("/collection/operands/1/operands/0/match").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/1/operands/1/@type").asText());
        assertEquals("corpusId", res
                .at("/collection/operands/1/operands/1/key").asText());
        assertEquals("WPD", res.at("/collection/operands/1/operands/1/value")
                .asText());
        assertEquals("match:eq",
                res.at("/collection/operands/1/operands/1/match").asText());
    }


    @Test
    public void testMixed () throws JsonProcessingException, IOException {
        collection = "textClass=Sport | (pubDate in 2014 & corpusId=WPD)";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:docGroup", res.at("/collection/@type").asText());
        assertEquals("operation:or", res.at("/collection/operation").asText());
        assertEquals("koral:doc", res.at("/collection/operands/0/@type")
                .asText());
        assertEquals("textClass", res.at("/collection/operands/0/key").asText());
        assertEquals("Sport", res.at("/collection/operands/0/value").asText());
        assertEquals("match:eq", res.at("/collection/operands/0/match")
                .asText());
        assertEquals("koral:docGroup", res.at("/collection/operands/1/@type")
                .asText());
        assertEquals("operation:and", res
                .at("/collection/operands/1/operation").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/1/operands/0/@type").asText());
        assertEquals("pubDate", res.at("/collection/operands/1/operands/0/key")
                .asText());
        assertEquals("2014", res.at("/collection/operands/1/operands/0/value")
                .asText());
        assertEquals("type:date",
                res.at("/collection/operands/1/operands/0/type").asText());
        assertEquals("match:eq",
                res.at("/collection/operands/1/operands/0/match").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/1/operands/1/@type").asText());
        assertEquals("corpusId", res
                .at("/collection/operands/1/operands/1/key").asText());
        assertEquals("WPD", res.at("/collection/operands/1/operands/1/value")
                .asText());
        assertEquals("match:eq",
                res.at("/collection/operands/1/operands/1/match").asText());

        collection = "textClass=Sport | pubDate in 2014 & corpusId=WPD";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:docGroup", res.at("/collection/@type").asText());
        assertEquals("operation:or", res.at("/collection/operation").asText());
        assertEquals("koral:doc", res.at("/collection/operands/0/@type")
                .asText());
        assertEquals("textClass", res.at("/collection/operands/0/key").asText());
        assertEquals("Sport", res.at("/collection/operands/0/value").asText());
        assertEquals("match:eq", res.at("/collection/operands/0/match")
                .asText());
        assertEquals("koral:docGroup", res.at("/collection/operands/1/@type")
                .asText());
        assertEquals("operation:and", res
                .at("/collection/operands/1/operation").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/1/operands/0/@type").asText());
        assertEquals("pubDate", res.at("/collection/operands/1/operands/0/key")
                .asText());
        assertEquals("2014", res.at("/collection/operands/1/operands/0/value")
                .asText());
        assertEquals("type:date",
                res.at("/collection/operands/1/operands/0/type").asText());
        assertEquals("match:eq",
                res.at("/collection/operands/1/operands/0/match").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/1/operands/1/@type").asText());
        assertEquals("corpusId", res
                .at("/collection/operands/1/operands/1/key").asText());
        assertEquals("WPD", res.at("/collection/operands/1/operands/1/value")
                .asText());
        assertEquals("match:eq",
                res.at("/collection/operands/1/operands/1/match").asText());

        collection = "(textClass=Sport | pubDate in 2014) & corpusId=WPD";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:docGroup", res.at("/collection/@type").asText());
        assertEquals("operation:and", res.at("/collection/operation").asText());
        assertEquals("koral:docGroup", res.at("/collection/operands/0/@type")
                .asText());
        assertEquals("operation:or", res.at("/collection/operands/0/operation")
                .asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/0/operands/0/@type").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/0/operands/1/@type").asText());
        assertEquals("koral:doc", res.at("/collection/operands/1/@type")
                .asText());

        collection = "(textClass=Sport & pubDate in 2014) & corpusId=WPD";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:docGroup", res.at("/collection/@type").asText());
        assertEquals("operation:and", res.at("/collection/operation").asText());
        assertEquals("koral:docGroup", res.at("/collection/operands/0/@type")
                .asText());
        assertEquals("operation:and", res
                .at("/collection/operands/0/operation").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/0/operands/0/@type").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/0/operands/1/@type").asText());
        assertEquals("koral:doc", res.at("/collection/operands/1/@type")
                .asText());

        collection = "(textClass=Sport & textClass=ausland) | (corpusID=WPD & author=White)";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:docGroup", res.at("/collection/@type").asText());
        assertEquals("operation:or", res.at("/collection/operation").asText());
        assertEquals("koral:docGroup", res.at("/collection/operands/0/@type")
                .asText());
        assertEquals("operation:and", res
                .at("/collection/operands/0/operation").asText());
        assertEquals("koral:docGroup", res.at("/collection/operands/1/@type")
                .asText());
        assertEquals("operation:and", res
                .at("/collection/operands/1/operation").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/0/operands/0/@type").asText());
        assertEquals("Sport", res.at("/collection/operands/0/operands/0/value")
                .asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/0/operands/1/@type").asText());
        assertEquals("ausland",
                res.at("/collection/operands/0/operands/1/value").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/1/operands/0/@type").asText());
        assertEquals("WPD", res.at("/collection/operands/1/operands/0/value")
                .asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/1/operands/1/@type").asText());
        assertEquals("White", res.at("/collection/operands/1/operands/1/value")
                .asText());

        collection = "(textClass=Sport & textClass=ausland) | (corpusID=WPD & author=White & pubDate in 2000)";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:docGroup", res.at("/collection/@type").asText());
        assertEquals("operation:or", res.at("/collection/operation").asText());
        assertEquals("koral:docGroup", res.at("/collection/operands/0/@type")
                .asText());
        assertEquals("operation:and", res
                .at("/collection/operands/0/operation").asText());
        assertEquals("koral:docGroup", res.at("/collection/operands/1/@type")
                .asText());
        assertEquals("operation:and", res
                .at("/collection/operands/1/operation").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/0/operands/0/@type").asText());
        assertEquals("Sport", res.at("/collection/operands/0/operands/0/value")
                .asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/0/operands/1/@type").asText());
        assertEquals("ausland",
                res.at("/collection/operands/0/operands/1/value").asText());
        assertEquals("koral:doc",
                res.at("/collection/operands/1/operands/0/@type").asText());
        assertEquals("WPD", res.at("/collection/operands/1/operands/0/value")
                .asText());
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
    public void testDateYear () throws JsonProcessingException, IOException {
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
        assertTrue(res.at("/collection/type").isMissingNode());
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
    public void testDateMonthDay () throws JsonProcessingException, IOException {
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
        assertTrue(res.at("/collection/type").isMissingNode());
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

	@Test
    public void testRegexInGroup () throws JsonProcessingException, IOException {
        collection = "corpusSigle = /HMP[0-9][0-9]/ AND (textTypeArt=/.*Kommentar.*/ or textTypeArt=/.*Leitartikel.*/)";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("corpusSigle", res.at("/collection/operands/0/key").asText());
        assertEquals("HMP[0-9][0-9]", res.at("/collection/operands/0/value").asText());
        assertEquals("operation:or", res.at("/collection/operands/1/operation").asText());
        assertEquals("textTypeArt", res.at("/collection/operands/1/operands/0/key").asText());
        assertEquals(".*Kommentar.*", res.at("/collection/operands/1/operands/0/value").asText());
        assertEquals("operation:and", res.at("/collection/operation").asText());

        collection = "corpusSigle = /HMP[0-9][0-9]/ and (textTypeArt=/.*Kommentar.*/ OR textTypeArt=/.*Leitartikel.*/)";
        qs.setQuery(query, ql);
        qs.setCollection(collection);
        res = mapper.readTree(qs.toJSON());
        assertEquals("corpusSigle", res.at("/collection/operands/0/key").asText());
        assertEquals("HMP[0-9][0-9]", res.at("/collection/operands/0/value").asText());
        assertEquals("operation:or", res.at("/collection/operands/1/operation").asText());
        assertEquals("textTypeArt", res.at("/collection/operands/1/operands/0/key").asText());
        assertEquals(".*Kommentar.*", res.at("/collection/operands/1/operands/0/value").asText());
        assertEquals("operation:and", res.at("/collection/operation").asText());
	}	

    @Test
    public void testDateValidate () {
        String fake_date = "fireStorm2014";
        String fake_date_2 = "2014-12Date";
        String date = "2015";
        String date_1 = "2015-05";
        String date_2 = "2015-05-13";
        String date_3 = "2015-23-01";
        assertFalse(QueryUtils.checkDateValidity(fake_date));
        assertFalse(QueryUtils.checkDateValidity(fake_date_2));
        assertTrue(QueryUtils.checkDateValidity(date));
        assertTrue(QueryUtils.checkDateValidity(date_1));
        assertTrue(QueryUtils.checkDateValidity(date_2));
        assertFalse(QueryUtils.checkDateValidity(date_3));
    }
}
