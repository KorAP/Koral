import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import de.ids_mannheim.korap.query.serialize.CollectionQuery;
import de.ids_mannheim.korap.query.serialize.QuerySerializer;
import de.ids_mannheim.korap.resource.Relation;
import de.ids_mannheim.korap.util.QueryException;
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
//        CollectionQuery qu = new CollectionQuery().addMetaFilter(b.toString());
        CollectionQuery query = new CollectionQuery().addMetaFilter(b.toString(),
                Relation.AND);
        System.out.println(query.buildString(Relation.AND, Relation.AND));
        System.out.println(query.toCollections(Relation.AND, Relation.AND));
//        System.out.println("value reference " + qu.stringify());
//        System.out.println();
    }

    @Test
    public void testSingle() throws IOException {
        CollectionQuery query = new CollectionQuery().addMetaFilter("textClass", "wissenschaft");
//        System.out.println("------ TEXT SINGLE " + query.stringify());
        System.out.println(query.buildString(Relation.AND, Relation.AND));
    }

    @Test
    public void testDates() throws IOException {
        StringBuffer b = new StringBuffer();
        b.append("pubDate:>2013-04-01");
        b.append(" AND ");
        b.append("pubDate:<2012-04-01");
        b.append(" AND ");
        b.append("author:Goethe");
        CollectionQuery query = new CollectionQuery().addMetaFilter(b.toString(), Relation.AND);
        System.out.println("value until/since : " + query.buildString(Relation.AND, Relation.AND));
        System.out.println("meta value until/since " + query.toCollections(Relation.AND, Relation.AND));
        Assert.assertEquals("[{\"@type\":\"korap:meta-filter\",\"@value\":{\"@type\":\"korap:group\",\"relation\":\"and\",\"operands\":[{\"@type\":\"korap:term\",\"@field\":\"korap:field#author\",\"@value\":\"Goethe\"},{\"@type\":\"korap:group\",\"@field\":\"korap:field#pubDate\",\"relation\":\"between\",\"operands\":[{\"@type\":\"korap:date\",\"@value\":\"2012-04-01\"},{\"@type\":\"korap:date\",\"@value\":\"2013-04-01\"}]}]}}]", query.buildString(Relation.AND, Relation.AND));
//        System.out.println();
    }

    @Test
    public void testUntil() throws IOException {
        StringBuffer b = new StringBuffer();
        b.append("pubDate:>2013-12-10");
        b.append(" AND ");
        b.append("author:Hesse");
        CollectionQuery query = new CollectionQuery().addMetaFilter(b.toString(), Relation.AND);
        System.out.println("Running date check (until) with additional attribute author");
        Assert.assertEquals("[{\"@type\":\"korap:meta-filter\",\"@value\":{\"@type\":\"korap:group\",\"relation\":\"and\",\"operands\":[{\"@type\":\"korap:term\",\"@field\":\"korap:field#author\",\"@value\":\"Hesse\"},{\"@type\":\"korap:group\",\"@field\":\"korap:field#pubDate\",\"relation\":\"until\",\"operands\":[{\"@type\":\"korap:date\",\"@value\":\"2013-12-10\"}]}]}}]", query.buildString(Relation.AND, Relation.AND));
//        System.out.println("value until : " + query.stringify());
//        System.out.println();
    }

    @Test
    public void testSince() throws IOException {
        StringBuffer b = new StringBuffer();
        b.append("pubDate:<2013-12-10");
        b.append(" AND ");
        b.append("author:Kafka");
        CollectionQuery query = new CollectionQuery().addMetaFilter(b.toString(), Relation.AND);
        System.out.println("value since : " + query.buildString(Relation.AND,
                Relation.AND));
        System.out.println("meta value since " + query.toCollections(Relation.AND, Relation.AND));
//        System.out.println();
        System.out.println("Running date check (since) with additional attribute author");
        Assert.assertEquals("[{\"@type\":\"korap:meta-filter\",\"@value\":{\"@type\":\"korap:group\",\"relation\":\"and\",\"operands\":[{\"@type\":\"korap:term\",\"@field\":\"korap:field#author\",\"@value\":\"Kafka\"},{\"@type\":\"korap:group\",\"@field\":\"korap:field#pubDate\",\"relation\":\"since\",\"operands\":[{\"@type\":\"korap:date\",\"@value\":\"2013-12-10\"}]}]}}]", query.buildString(Relation.AND, Relation.AND));
    }

    @Test
    public void testGenerator() throws QueryException {
                   /*
         * just for testing...
		 */
        QuerySerializer jg = new QuerySerializer();
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
        StringBuffer b = new StringBuffer();
        b.append("pubDate:<2013-12-10");
        b.append(" AND ");
        b.append("author:Kafka");
        CollectionQuery q = new CollectionQuery().addMetaFilter(b.toString(), Relation.AND);
        q.addMetaExtend("author", "Hesse");

        System.out.println("--- ALL " + q.buildString(Relation.AND, Relation.AND));
        System.out.println();

    }

    @Test
    public void testJSONArray() throws JsonProcessingException {
        StringBuffer b = new StringBuffer();
        b.append("pubDate:<2013-12-10");
        b.append(" AND ");
        b.append("author:Kafka");
        CollectionQuery q = new CollectionQuery().addMetaExtend(b.toString(), Relation.AND);
        System.out.println("array repres " + q.buildString(Relation.AND, Relation.AND));
        System.out.println();
    }

    @Test
    public void testCollections() throws IOException {
        CollectionQuery q = new CollectionQuery().addMetaFilter("corpusID", "A00");
        q.addMetaExtend("corpusID", "A01");

        System.out.println("results stringified " + q.buildString(Relation.AND, Relation.AND));
        System.out.println("results to meta" + q.toCollections(Relation.AND, Relation.AND));
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
        CollectionQuery q = new CollectionQuery().addResource(meta);
        System.out.println("Testing Resource Meta data");
        org.junit.Assert.assertEquals("{\"collections\":" + meta + "}", q.toCollections(Relation.AND, Relation.AND));
    }

    @Test
    public void testA00() throws IOException {
        CollectionQuery q = new CollectionQuery().addMetaExtend("corpusID", "A00").addMetaExtend("corpusID", "A01");
        System.out.println("A meta: " + q.buildString(Relation.AND, Relation.AND));
        System.out.println();
    }

    @Test
    public void testResources2() throws IOException {
        String meta = "[{\"@type\":\"korap:meta-filter\",\"@value\":{\"@type\":\"korap:term\",\"@field\":\"korap:field#corpusID\",\"@value\":\"WPD\"}}]";
        CollectionQuery q = new CollectionQuery().addResource(meta);
        q.addMetaFilter("textClass", "wissenschaft");
        System.out.println("stringified meta " + q.buildString(Relation.AND, Relation.AND));
        System.out.println("meta string " + q.toCollections(Relation.AND, Relation.AND));
    }

}
