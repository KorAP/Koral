package de.ids_mannheim.korap.query.serialize;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

/**
 * @author hanl
 * @date 28/07/2015
 */
public class CollectionQueryDuplicateTest {

    @Test
    public void testCollectionQueryDuplicateThrowsNoException() {
        QuerySerializer serializer = new QuerySerializer();
        serializer.setQuery("[base=Haus]", "poliqarp");
        serializer.setCollection("textClass=politik & corpusID=WPD");
        ObjectMapper m = new ObjectMapper();
        try {
            JsonNode first = m.readTree(serializer.toJSON());
            JsonNode second = m.readTree(serializer.toJSON());

            assert first.at("/collection").equals(second.at("/collection"));

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

}
