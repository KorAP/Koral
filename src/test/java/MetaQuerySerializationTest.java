import de.ids_mannheim.korap.query.serialize.MetaCollectionSerializer;
import de.ids_mannheim.korap.query.serialize.MetaQuerySerializer;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author hanl
 * @date 04/12/2013
 */

@RunWith(JUnit4.class)
public class MetaQuerySerializationTest {

    private MetaQuerySerializer querySerializer;
    private MetaCollectionSerializer collSerializer;

    public MetaQuerySerializationTest() {
        querySerializer = new MetaQuerySerializer();
        collSerializer = new MetaCollectionSerializer();
    }

    @Test
    public void test() throws IOException {
        Map<String, String> j = new HashMap();
        j.put("author", "Goethe");
        j.put("pubPlace", "Erfurt");
        j.put("textClass", "wissenschaft");
        String s = querySerializer.stringify(j, MetaQuerySerializer.TYPE.FILTER);
//        System.out.println("value reference " + s);
    }

    @Test
    public void testSingle() throws IOException {
        Map<String, String> j = new HashMap();
        j.put("textClass", "wissenschaft");
        String s = querySerializer.stringify(j, MetaQuerySerializer.TYPE.FILTER);
//        System.out.println("value reference test single " + s);
    }

    @Test
    public void testResourceMeta() throws IOException {
        String s = collSerializer.serialize("25");
//        System.out.println(" --- RESULT JSON " + s);
    }

    @Test
    public void testDates() throws IOException {
        Map<String, String> queries = new LinkedHashMap<>();
        queries.put("<pubDate", String.valueOf(new DateTime().getMillis()));
        queries.put(">pubDate", String.valueOf(new DateTime().getMillis()+2));
        queries.put("author", "Goethe");
        String f = querySerializer.stringify(queries, MetaQuerySerializer.TYPE.FILTER);
        System.out.println("value : "+ f);
    }
}
