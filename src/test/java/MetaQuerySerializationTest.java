import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import de.ids_mannheim.korap.query.serialize.*;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.*;

/**
 * @author hanl
 * @date 04/12/2013
 */

@RunWith(JUnit4.class)
public class MetaQuerySerializationTest {

    private MetaQuerySerializer querySerializer;
    private MetaCollectionSerializer collSerializer;
    private Serializer ser;

    public MetaQuerySerializationTest() {
        querySerializer = new MetaQuerySerializer();
        collSerializer = new MetaCollectionSerializer();
        ser = new Serializer();
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
        System.out.println();
    }

    @Test
    public void testDates() throws IOException {
        Map<String, String> queries = new LinkedHashMap<>();
        queries.put("pubDate", String.valueOf(new DateTime().getMillis()) + "~"
                + String.valueOf(new DateTime().getMillis() + 2));
        queries.put("author", "Goethe");
        String f = querySerializer.stringify(queries, MetaQuerySerializer.TYPE.FILTER);
        System.out.println("value until/since : " + f);
        System.out.println();
    }

    @Test
    public void testUntil() throws IOException {
        Map<String, String> queries = new LinkedHashMap<>();
        queries.put("pubDate", ">" + String.valueOf(new DateTime().getMillis()));
        queries.put("author", "Hesse");
        String f = querySerializer.stringify(queries, MetaQuerySerializer.TYPE.FILTER);
        System.out.println("value until : " + f);
        System.out.println();
    }

    @Test
    public void testSince() throws IOException {
        Map<String, String> queries = new LinkedHashMap<>();
        queries.put("pubDate", "<" + String.valueOf(new DateTime().getMillis()));
        queries.put("author", "Kafka");
        String f = querySerializer.stringify(queries, MetaQuerySerializer.TYPE.FILTER);
        System.out.println("value since : " + f);
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
        List s = querySerializer.serializeQueries(queries, MetaQuerySerializer.TYPE.FILTER);

        queries.clear();
        queries.put("author", "Hesse");

        List f = querySerializer.serializeQueries(queries, MetaQuerySerializer.TYPE.EXTEND);
        s.addAll(f);
        System.out.println("--- ALL " + s);
        System.out.println();

    }

    @Test
    public void testJSONArray() throws JsonProcessingException {
        Map<String, String> queries = new LinkedHashMap<>();
        queries.put("pubDate", "<" + String.valueOf(new DateTime().getMillis()));
        queries.put("author", "Kafka");
        List s = querySerializer.serializeQueries(queries, MetaQuerySerializer.TYPE.FILTER);
        System.out.println("array repres " + ser.serializeToMeta(s));
        System.out.println();
    }

    @Test
    public void testCollections() throws IOException {
        Map<String, String> query = new LinkedHashMap<>();
        Serializer ser = new Serializer();
        query.put("corpusID", "A00");
        List<Map> l = ser.serializeQueries(query, MetaQuerySerializer.TYPE.FILTER);
        query.clear();
        query.put("corpusID", "A01");
        List<Map> u = ser.serializeQueries(query, MetaQuerySerializer.TYPE.EXTEND);
        l.addAll(u);
        String val = ser.stringify(l);
        System.out.println("results " + val);
        System.out.println();


        String meta = ser.queryToMeta(val);

        System.out.println("meta query " + meta);
//        List<String> list = new ArrayList<>();
//        list.add(val);
//        List s = collSerializer.serializeResource(list);
        System.out.println("printable list ");
        System.out.println();

    }

    @Test
    public void testResources() throws IOException {
        String meta = "[{\"@type\":\"korap:meta-filter\",\"@value\":{\"@type\":\"korap:term\",\"@field\":\"korap:field#corpusID\",\"@value\":\"WPD\"}}]";
        List s = new ArrayList();
        s.add(meta);
        List fin = ser.serializeResources(s);

        String string = ser.serializeToMeta(fin);
        System.out.println("meta string " + string);

    }

    @Test
    public void testA00() throws IOException {
        Map<String, String> query = new LinkedHashMap<>();
        Serializer ser = new Serializer();
        query.put("corpusID", "A00");
        List<Map> l = ser.serializeQueries(query, MetaQuerySerializer.TYPE.FILTER);
        query.clear();
        query.put("corpusID", "A01");
        List<Map> u = ser.serializeQueries(query, MetaQuerySerializer.TYPE.EXTEND);
        l.addAll(u);
        String val = ser.stringify(l);
        List s = new ArrayList();
        s.add(val);

        List fin = ser.serializeResources(s);
        System.out.println("A00 meta: " + fin);

    }


    @Test
    public void testnewMetaQuery() throws IOException {
        Map<String, String> qu = new LinkedHashMap<>();
        Serializer ser = new Serializer();
        qu.put("corpusID", "A00");
        List<Map> l = ser.serializeQueries(qu, MetaQuerySerializer.TYPE.FILTER);
        qu.clear();
        qu.put("corpusID", "A01");
        List<Map> u = ser.serializeQueries(qu, MetaQuerySerializer.TYPE.EXTEND);
        l.addAll(u);
        String val = ser.stringify(l);
        List s = new ArrayList();
        s.add(val);

        List fin = ser.serializeResources(s);


        System.out.println("=======   METAQUERY TESTING BEGIN =======");

        MetaQuery query = new MetaQuery();
        query.addMetaFilter("textClass", "wissenschaft");
        query.addResources(s);
        System.out.println("query "+ query.toMeta());
        System.out.println("string "+ query.stringify());
        System.out.println("=======   METAQUERY TESTING END =======");
    }

}
