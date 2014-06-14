import de.ids_mannheim.korap.query.serialize.CollectionQueryBuilder;
import de.ids_mannheim.korap.query.serialize.CollectionQueryBuilder2;
import de.ids_mannheim.korap.query.serialize.CollectionQueryTree;
import de.ids_mannheim.korap.resource.Relation;
import de.ids_mannheim.korap.util.QueryException;
import de.ids_mannheim.korap.utils.JsonUtils;
import de.ids_mannheim.korap.utils.TimeUtils;
import org.junit.Test;

public class CollectionQueryTreeTest {

    CollectionQueryTree ef;
    String map;
    private String query;

    private boolean equalsQueryContent(String res, String query) throws QueryException {
        res = res.replaceAll(" ", "");
        ef = new CollectionQueryTree();
        ef.process(query);
        String queryMap = ef.getRequestMap().get("query").toString().replaceAll(" ", "");
        return res.equals(queryMap);
    }

    @Test
    public void testSimple() throws QueryException {
        query = "textClass=Sport";
        String regex1 = "{@type=korap:filter, filter={@type=korap:term, attribute=textClass, key=Sport, match=match:eq}}";
        ef = new CollectionQueryTree();
        ef.process(query);
        map = JsonUtils.toJSON(ef.getRequestMap());
//		assertEquals(regex1.replaceAll(" ", ""), map.replaceAll(" ", ""));
        System.out.println("THE QUERY: " + map);
    }


    @Test
    public void testComplex() throws QueryException {
        query = "(textClass=Sport | textClass=ausland) & corpusID=WPD";
        String regex1 = "{@type=korap:filter, filter={@type=korap:term, attribute=textClass, key=Sport, match=match:eq}}";
        ef = new CollectionQueryTree();
        ef.process(query);
        map = JsonUtils.toJSON(ef.getRequestMap());
//		assertEquals(regex1.replaceAll(" ", ""), map.replaceAll(" ", ""));
        System.out.println("THE QUERY 1: " + map);
    }

    @Test
    public void testBuilder() throws QueryException {
        CollectionQueryBuilder2 builder = new CollectionQueryBuilder2();
        builder.setQuery("(textClass=Sport | textClass=ausland) & corpusID=WPD");
        System.out.println("BUILDER RESULT: " + builder.toJSON());
    }

//    @Test
    public void testSimpleBuilder() {
        CollectionQueryBuilder b = new CollectionQueryBuilder();
        b.addMetaFilter("corpusID", "WPD");
        b.addMetaFilter("textClass", "wissenschaft");
        b.setFilterAttributeRelation(Relation.AND);
        System.out.println("SIMPLE BUILDER RESULT: " + b.toCollections());
    }

    // old builder pubDate query
//    @Test
    public void testDateQuery() {
        CollectionQueryBuilder b = new CollectionQueryBuilder();
        String query = "pubDate=>" + TimeUtils.getNow().getMillis();
        query = query + " AND pubDate=<" + TimeUtils.getNow().getMillis();
        b.addMetaFilterQuery(query);
        b.setFilterAttributeRelation(Relation.AND);
        System.out.println("FINAL RESOURCE: " + b.toCollections());
    }

    @Test
    public void testDateNewQuery() throws QueryException {
        // search for pubDate between 1990 and 2010!
        String query = "1990<pubDate<2010 & genre=Sport";
        CollectionQueryBuilder2 q = new CollectionQueryBuilder2();
        q.setQuery(query);
        System.out.println("DATE QUERY RESULT: " + q.toJSON());
    }


}

