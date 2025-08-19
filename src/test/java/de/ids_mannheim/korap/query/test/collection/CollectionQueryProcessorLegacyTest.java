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

public class CollectionQueryProcessorLegacyTest {

    private String query = "foo";
    private String ql = "poliqarpplus";
    private String collection;

    public static final QuerySerializer qs = new QuerySerializer(1.0);
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

}
