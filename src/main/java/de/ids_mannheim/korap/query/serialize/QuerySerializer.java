package de.ids_mannheim.korap.query.serialize;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import de.ids_mannheim.korap.util.QueryException;
import de.ids_mannheim.korap.utils.JsonUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * @author bingel, hanl
 */
public class QuerySerializer {

    public static String queryLanguageVersion;

    private AbstractSyntaxTree ast;
    private Object collection;
    private Object meta;
    private org.slf4j.Logger log = LoggerFactory
            .getLogger(QuerySerializer.class);


    /**
     * @param args
     * @throws QueryException
     */
    public static void main(String[] args) {
        /*
         * just for testing...
		 */
        QuerySerializer jg = new QuerySerializer();
        int i = 0;
        String[] queries;
        if (args.length == 0) {
            queries = new String[]{

            };
        } else {
            queries = new String[]{args[0]};
        }

        for (String q : queries) {
            i++;
            try {
                System.out.println(q);
                String ql = "cosmas2";
                jg.run(q, ql, System.getProperty("user.home") + "/" + ql + "_" + i + ".jsonld");
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
            } catch (QueryException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Runs the QuerySerializer by initializing the relevant AbstractSyntaxTree implementation (depending on specified query language)
     * and transforms and writes the tree's requestMap to the specified output file.
     *
     * @param outFile       The file to which the serialization is written
     * @param query         The query string
     * @param queryLanguage The query language. As of 13/11/20, this must be either 'poliqarp' or 'poliqarpplus'. Some extra maven stuff needs to done to support CosmasII ('cosmas') [that maven stuff would be to tell maven how to build the cosmas grammar and where to find the classes]
     * @throws IOException
     * @throws QueryException
     */
    public void run(String query, String queryLanguage, String outFile)
            throws IOException, QueryException {
        if (queryLanguage.equals("poliqarp")) {
            ast = new PoliqarpPlusTree(query);
        } else if (queryLanguage.toLowerCase().equals("cosmas2")) {
            ast = new CosmasTree(query);
        } else if (queryLanguage.toLowerCase().equals("poliqarpplus")) {
            ast = new PoliqarpPlusTree(query);
        } else if (queryLanguage.toLowerCase().equals("cql")) {
            ast = new CQLTree(query);
        } else if (queryLanguage.toLowerCase().equals("annis")) {
            ast = new AqlTree(query);
        } else {
            throw new QueryException(queryLanguage + " is not a supported query language!");
        }
        Map<String, Object> requestMap = ast.getRequestMap();
//        mapper.writeValue(new File(outFile), requestMap);
    }

    public QuerySerializer setQuery(String query, String ql, String version)
            throws QueryException {
        try {
            if (ql.equalsIgnoreCase("poliqarp")) {
                ast = new PoliqarpPlusTree(query);
            } else if (ql.equalsIgnoreCase("cosmas2")) {
                ast = new CosmasTree(query);
            } else if (ql.equalsIgnoreCase("poliqarpplus")) {
                ast = new PoliqarpPlusTree(query);
            } else if (ql.equalsIgnoreCase("cql")) {
//                queryLanguageVersion = "1.2"; // set me
                ast = new CQLTree(query, version);
            } else if (ql.equalsIgnoreCase("annis")) {
                ast = new AqlTree(query);
            } else {
                throw new QueryException(ql + " is not a supported query language!");
            }
        } catch (QueryException e) {
            throw e;
        } catch (Exception e) {
            throw new QueryException("UNKNOWN: Query could not be parsed (" + query + ")");
        }
        return this;
    }

    public QuerySerializer setQuery(String query, String ql) throws QueryException {
        return setQuery(query, ql, "");
    }

    public final String toJSON() {
        return JsonUtils.toJSON(raw());
    }

    public final Map build() {
        return raw();
    }

    private Map raw() {
        Map<String, Object> requestMap = ast.getRequestMap();
        if (collection != null)
            requestMap.put("collections", collection);
        if (meta != null)
            requestMap.put("meta", meta);
        return requestMap;
    }


    public QuerySerializer setMeta(
            String cli, String cri, int cls, int crs,
            int num, int pageIndex) {
        MetaQueryBuilder meta = new MetaQueryBuilder();
        meta.addContext(cls, cli, crs, cri);
        meta.addEntry("startIndex", pageIndex);
        meta.addEntry("count", num);
        this.meta = meta.raw();
        return this;
    }

    public QuerySerializer setMeta(MetaQueryBuilder meta) {
        this.meta = meta.raw();
        return this;
    }

    @Deprecated
    public QuerySerializer setCollectionSimple(String collection) {
        CollectionQueryBuilder qobj = new CollectionQueryBuilder();
        qobj.addResource(collection);
        this.collection = qobj.raw();
        return this;
    }

    public QuerySerializer setCollection(String collection) throws QueryException {
        CollectionQueryTree tree = new CollectionQueryTree();
        tree.process(collection);
        this.collection = tree.getRequestMap();
        return this;
    }

    public QuerySerializer setCollection(CollectionQueryBuilder2 collections) {
        this.collection = collections.raw();
        return this;
    }

    public QuerySerializer setDeprCollection(CollectionQueryBuilder collections) {
        this.collection = collections.raw();
        return this;
    }
}
