package de.ids_mannheim.korap.query.test.collection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ids_mannheim.korap.query.serialize.QuerySerializer;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author hanl
 * @date 28/07/2015
 */
public class CollectionQueryDuplicateTest {

    @Test
    public void testCollectionQueryDuplicateThrowsAssertionException ()
            throws IOException {
        QuerySerializer serializer = new QuerySerializer();
        serializer.setQuery("[base=Haus]", "poliqarp");
        serializer.setCollection("textClass=politik & corpusID=WPD");
        ObjectMapper m = new ObjectMapper();
        JsonNode first = m.readTree(serializer.toJSON());
        assertNotNull(first);
        assertEquals(first.at("/collection"), m.readTree(serializer.toJSON())
                .at("/collection"));
        assertEquals(first.at("/collection"), m.readTree(serializer.toJSON())
                .at("/collection"));
        assertEquals(first.at("/collection"), m.readTree(serializer.toJSON())
                .at("/collection"));
    }

}
