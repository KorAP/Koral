import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import de.ids_mannheim.korap.query.serialize.JsonGenerator;
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
        System.out.println("------ TEXT SINGLE " + s);
    }

    @Test
    public void testResourceMeta() throws IOException {
        String s = collSerializer.serialize("25");
        System.out.println(" --- RESULT JSON " + s);
    }

    @Test
    public void testDates() throws IOException {
        Map<String, String> queries = new LinkedHashMap<>();
        queries.put("pubDate", String.valueOf(new DateTime().getMillis()) + "~"
                + String.valueOf(new DateTime().getMillis() + 2));
        queries.put("author", "Goethe");
        String f = querySerializer.stringify(queries, MetaQuerySerializer.TYPE.FILTER);
        System.out.println("value until/since : " + f);
    }

    @Test
    public void testUntil() throws IOException {
        Map<String, String> queries = new LinkedHashMap<>();
        queries.put("pubDate", ">" + String.valueOf(new DateTime().getMillis()));
        queries.put("author", "Hesse");
        String f = querySerializer.stringify(queries, MetaQuerySerializer.TYPE.FILTER);
        System.out.println("value until : " + f);
    }

    @Test
    public void testSince() throws IOException {
        Map<String, String> queries = new LinkedHashMap<>();
        queries.put("pubDate", "<" + String.valueOf(new DateTime().getMillis()));
        queries.put("author", "Kafka");
        String f = querySerializer.stringify(queries, MetaQuerySerializer.TYPE.FILTER);
        System.out.println("value since : " + f);
    }

    //@Test
    public void testGenerator() {
                   /*
         * just for testing...
		 */
        JsonGenerator jg = new JsonGenerator();
        int i = 0;
        String[] queries;
        queries = new String[]{
                "shrink({[base=foo]})",
                "shrink({[base=foo]}[orth=bar])",
                "shrink(1:[base=Der]{1:[base=Mann]})",
        };

        for (String q : queries) {
            i++;
            try {
                System.out.println(q);
                jg.run(q, "poliqarp", System.getProperty("user.home") + "/bsp" + i + ".json");
                System.out.println();
            } catch (NullPointerException npe) {
                npe.printStackTrace();
                System.out.println("null\n");
            } catch (JsonGenerationException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
