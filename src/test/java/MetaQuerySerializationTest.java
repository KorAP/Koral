import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import de.ids_mannheim.korap.query.serialize.CollectionQueryBuilder;
import de.ids_mannheim.korap.query.serialize.QuerySerializer;
import de.ids_mannheim.korap.query.serialize.util.QueryException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;

/**
 * @author hanl
 * @date 04/12/2013
 */

@RunWith(JUnit4.class)
public class MetaQuerySerializationTest {


    @Test
    public void test() throws IOException {
        StringBuffer b = new StringBuffer();
        b.append("author:Goethe");
        b.append(" AND ");
        b.append("pubPlace:Erfurt");
        b.append(" AND ");
        b.append("textClass:wissenschaft");
//        CollectionQuery qu = new CollectionQuery().addMetaFilterQuery(b.toString());
        CollectionQueryBuilder query = new CollectionQueryBuilder().addMetaFilterQuery(b.toString());
        System.out.println(query.toJSON());
        System.out.println(query.toCollections());
//        System.out.println("value reference " + qu.stringify());
//        System.out.println();
    }

    @Test
    public void testSingle() throws IOException {
        CollectionQueryBuilder query = new CollectionQueryBuilder().addMetaFilter("textClass", "wissenschaft");
//        System.out.println("------ TEXT SINGLE " + query.stringify());
        System.out.println(query.toJSON());
    }

//    @Test
    public void testDates() throws IOException {
        StringBuffer b = new StringBuffer();
        b.append("pubDate:>2013-04-01");
        b.append(" AND ");
        b.append("pubDate:<2012-04-01");
        b.append(" AND ");
        b.append("author:Goethe");
        CollectionQueryBuilder query = new CollectionQueryBuilder().addMetaFilterQuery(b.toString());
        System.out.println("value until/since : " + query.toJSON());
        System.out.println("meta value until/since " + query.toCollections());
        Assert.assertEquals("[{\"@type\":\"korap:meta-filter\",\"@value\":{\"@type\":\"korap:group\",\"relation\":\"and\",\"operands\":[{\"@type\":\"korap:term\",\"@field\":\"korap:field#author\",\"@value\":\"Goethe\"},{\"@type\":\"korap:group\",\"@field\":\"korap:field#pubDate\",\"relation\":\"between\",\"operands\":[{\"@type\":\"korap:date\",\"@value\":\"2012-04-01\"},{\"@type\":\"korap:date\",\"@value\":\"2013-04-01\"}]}]}}]", query.toJSON());
//        System.out.println();
    }

//    @Test
    public void testUntil() throws IOException {
        StringBuffer b = new StringBuffer();
        b.append("pubDate:>2013-12-10");
        b.append(" AND ");
        b.append("author:Hesse");
        CollectionQueryBuilder query = new CollectionQueryBuilder().addMetaFilterQuery(b.toString());
        System.out.println("Running date check (until) with additional attribute author");
        Assert.assertEquals("[{\"@type\":\"korap:meta-filter\",\"@value\":{\"@type\":\"korap:group\",\"relation\":\"and\",\"operands\":[{\"@type\":\"korap:term\",\"@field\":\"korap:field#author\",\"@value\":\"Hesse\"},{\"@type\":\"korap:group\",\"@field\":\"korap:field#pubDate\",\"relation\":\"until\",\"operands\":[{\"@type\":\"korap:date\",\"@value\":\"2013-12-10\"}]}]}}]", query.toJSON());
//        System.out.println("value until : " + query.stringify());
//        System.out.println();
    }

//    @Test
    public void testSince() throws IOException {
        StringBuffer b = new StringBuffer();
        b.append("pubDate:<2013-12-10");
        b.append(" AND ");
        b.append("author:Kafka");
        CollectionQueryBuilder query = new CollectionQueryBuilder().addMetaFilterQuery(b.toString());
        System.out.println("value since : " + query.toJSON());
        System.out.println("meta value since " + query.toCollections());
//        System.out.println();
        System.out.println("Running date check (since) with additional attribute author");
        Assert.assertEquals("[{\"@type\":\"korap:meta-filter\",\"@value\":{\"@type\":\"korap:group\",\"relation\":\"and\",\"operands\":[{\"@type\":\"korap:term\",\"@field\":\"korap:field#author\",\"@value\":\"Kafka\"},{\"@type\":\"korap:group\",\"@field\":\"korap:field#pubDate\",\"relation\":\"since\",\"operands\":[{\"@type\":\"korap:date\",\"@value\":\"2013-12-10\"}]}]}}]", query.toJSON());
    }


    @Test
    public void testMeta() {
        QuerySerializer s = new QuerySerializer();
        try {
            s.setQuery("shrink({[base=foo]})", "poliqarp");
            s.addMeta("2-token, 2-token", 10, 0);
        } catch (QueryException e) {
            e.printStackTrace();
        }
        System.out.println("THE RESULTING QUERY: " + s.toJSON());
    }

//    //    @Test
//    public void testGenerator() throws QueryException {
//         /*
//         * just for testing...
//		 */
//        QuerySerializer jg = new QuerySerializer();
//        int i = 0;
//        String[] queries;
//        queries = new String[]{
//                "shrink({[base=foo]})",
//                "shrink({[base=foo]}[orth=bar])",
//                "shrink(1:[base=Der]{1:[base=Mann]})",
//        };
//
//        for (String q : queries) {
//            i++;
//            try {
//                System.out.println(q);
//                jg.run(q, "poliqarp", System.getProperty("user.home") + "/bsp" + i + ".json");
//                System.out.println();
//            } catch (NullPointerException npe) {
//                npe.printStackTrace();
//                System.out.println("null\n");
//                System.out.println();
//            } catch (JsonGenerationException e) {
//                e.printStackTrace();
//            } catch (JsonMappingException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    @Test
    public void testLists() {
        StringBuffer b = new StringBuffer();
        b.append("pubDate:<2013-12-10");
        b.append(" AND ");
        b.append("author:Kafka");
        CollectionQueryBuilder q = new CollectionQueryBuilder().addMetaFilterQuery(b.toString());
        q.addMetaExtend("author", "Hesse");

        System.out.println("--- ALL " + q.toJSON());
        System.out.println();

    }

    @Test
    public void testJSONArray() throws JsonProcessingException {
        StringBuffer b = new StringBuffer();
        b.append("pubDate:<2013-12-10");
        b.append(" AND ");
        b.append("author:Kafka");
        CollectionQueryBuilder q = new CollectionQueryBuilder().addMetaExtendQuery(b.toString());
        System.out.println("array repres " + q.toJSON());
        System.out.println();
    }

    @Test
    public void testCollections() throws IOException {
        CollectionQueryBuilder q = new CollectionQueryBuilder().addMetaFilter("corpusID", "A00");
        q.addMetaExtend("corpusID", "A01");

        System.out.println("results stringified " + q.toJSON());
        System.out.println("results to meta" + q.toCollections());
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
        CollectionQueryBuilder q = new CollectionQueryBuilder().addResource(meta);
        System.out.println("Testing Resource Meta data");
        org.junit.Assert.assertEquals("{\"collections\":" + meta + "}", q.toCollections());
    }

    @Test
    public void testA00() throws IOException {
        CollectionQueryBuilder q = new CollectionQueryBuilder().addMetaExtend("corpusID", "A00").addMetaExtend("corpusID", "A01");
        System.out.println("A meta: " + q.toJSON());
        System.out.println();
    }

    @Test
    public void testResources2() throws IOException {
        String meta = "[{\"@type\":\"korap:meta-filter\",\"@value\":{\"@type\":\"korap:term\",\"@field\":\"korap:field#corpusID\",\"@value\":\"WPD\"}}]";
        CollectionQueryBuilder q = new CollectionQueryBuilder().addResource(meta);
        q.addMetaFilter("textClass", "wissenschaft");
        System.out.println("stringified meta " + q.toJSON());
        System.out.println("meta string " + q.toCollections());
    }

}
