import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import de.ids_mannheim.korap.query.serialize.JsonGenerator;
import de.ids_mannheim.korap.query.serialize.MetaQuery;
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


    @Test
    public void test() throws IOException {
        Map<String, String> j = new HashMap();
        j.put("author", "Goethe");
        j.put("pubPlace", "Erfurt");
        j.put("textClass", "wissenschaft");
        MetaQuery qu = new MetaQuery().addMetaFilter(j);
        System.out.println("value reference " + qu.stringify());
        System.out.println();
    }

    @Test
    public void testSingle() throws IOException {
        Map<String, String> j = new HashMap();
        j.put("textClass", "wissenschaft");
        MetaQuery query = new MetaQuery().addMetaFilter("textClass", "wissenschaft");
        System.out.println("------ TEXT SINGLE " + query.stringify());
        System.out.println();
    }

    @Test
    public void testDates() throws IOException {
        Map<String, String> queries = new LinkedHashMap<>();
        queries.put("pubDate", String.valueOf(new DateTime().getMillis()) + "~"
                + String.valueOf(new DateTime().getMillis() + 2));
        queries.put("author", "Goethe");
        MetaQuery query = new MetaQuery().addMetaFilter(queries);
        System.out.println("value until/since : " + query.stringify());
        System.out.println();
    }

    @Test
    public void testUntil() throws IOException {
        Map<String, String> queries = new LinkedHashMap<>();
        queries.put("pubDate", ">" + String.valueOf(new DateTime().getMillis()));
        queries.put("author", "Hesse");
        MetaQuery query = new MetaQuery().addMetaFilter(queries);
        System.out.println("value until : " + query.stringify());
        System.out.println();
    }

    @Test
    public void testSince() throws IOException {
        Map<String, String> queries = new LinkedHashMap<>();
        queries.put("pubDate", "<" + String.valueOf(new DateTime().getMillis()));
        queries.put("author", "Kafka");
        MetaQuery query = new MetaQuery().addMetaFilter(queries);
        System.out.println("value since : " + query.stringify());
        System.out.println();
    }

    @Test
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
                System.out.println();
            } catch (JsonGenerationException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testLists() {
        Map<String, String> queries = new LinkedHashMap<>();
        queries.put("pubDate", "<" + String.valueOf(new DateTime().getMillis()));
        queries.put("author", "Kafka");
        MetaQuery query = new MetaQuery().addMetaFilter(queries);

        query.addMetaExtend("author", "Hesse");

        System.out.println("--- ALL " + query.stringify());
        System.out.println();

    }

    @Test
    public void testJSONArray() throws JsonProcessingException {
        Map<String, String> queries = new LinkedHashMap<>();
        queries.put("pubDate", "<" + String.valueOf(new DateTime().getMillis()));
        queries.put("author", "Kafka");
        MetaQuery q = new MetaQuery().addMetaExtend(queries);
        System.out.println("array repres " + q.toMeta());
        System.out.println();
    }

    @Test
    public void testCollections() throws IOException {
        MetaQuery q = new MetaQuery().addMetaFilter("corpusID", "A00");
        q.addMetaExtend("corpusID", "A01");

        System.out.println("results stringified " + q.stringify());
        System.out.println("results to meta" + q.toMeta());
        System.out.println();
    }

    /**
     * asserts equality. input should be equal to output,
     * since there is no other metadata added to the meta query
     *
     * @throws IOException
     */
    @Test
    public void testResources() throws IOException {
        String meta = "[{\"@type\":\"korap:meta-filter\",\"@value\":{\"@type\":\"korap:term\",\"@field\":\"korap:field#corpusID\",\"@value\":\"WPD\"}}]";
        MetaQuery q = new MetaQuery().addResource(meta);
        org.junit.Assert.assertEquals("String should be empty", "", q.stringify());
        System.out.println("meta string " + q.toMeta());
        org.junit.Assert.assertEquals(meta, q.toMeta());
    }

    @Test
    public void testA00() throws IOException {
        MetaQuery q = new MetaQuery().addMetaExtend("corpusID", "A00").addMetaExtend("corpusID", "A01");
        System.out.println("A meta: " + q.stringify());
        System.out.println();
    }

    @Test
    public void testnewMetaQuery() throws IOException {
        String meta = "[{\"@type\":\"korap:meta-filter\",\"@value\":{\"@type\":\"korap:term\",\"@field\":\"korap:field#corpusID\",\"@value\":\"WPD\"}}]";
        MetaQuery q = new MetaQuery().addResource(meta);
        q.addMetaFilter("textClass", "wissenschaft");
        System.out.println("meta string " + q.toMeta());
    }

}
