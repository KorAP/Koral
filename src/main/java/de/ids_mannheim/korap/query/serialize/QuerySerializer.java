package de.ids_mannheim.korap.query.serialize;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.ids_mannheim.korap.query.serialize.util.KoralObjectGenerator;
import de.ids_mannheim.korap.query.serialize.util.StatusCodes;
import de.ids_mannheim.korap.utils.JsonUtils;
import de.ids_mannheim.korap.utils.KorAPLogger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bingel, hanl
 */
public class QuerySerializer {

    static HashMap<String, Class<? extends AbstractQueryProcessor>> qlProcessorAssignment;

    static {
        qlProcessorAssignment = 
                new HashMap<String, Class<? extends AbstractQueryProcessor>>();
        qlProcessorAssignment.put("poliqarpplus", PoliqarpPlusQueryProcessor.class);
        qlProcessorAssignment.put("cosmas2", Cosmas2QueryProcessor.class);
        qlProcessorAssignment.put("annis", AnnisQueryProcessor.class);
        qlProcessorAssignment.put("cql", CqlQueryProcessor.class);
    }

    private Logger qllogger = KorAPLogger.initiate("ql");
    public static String queryLanguageVersion;

    private AbstractQueryProcessor ast;
    private Map<String, Object> collection = new LinkedHashMap<String, Object>();
    private Map<String, Object> meta;
    private List<Object> errors;
    private List<Object> warnings;
    private List<Object> messages;
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
            queries = new String[] {
                    "tok=\"corpus\" & tok=\"query\" & tok=\"language\"& #2 . #3 & #1 . #2 "
                    
            };
        }
        else
            queries = new String[] { args[0] };

        for (String q : queries) {
            i++;
            try {
                System.out.println(q);
                String ql = "cosmas2";
                jg.setCollection("pubDate=2014");
                jg.run(q, ql);
                System.out.println();
            }
            catch (NullPointerException npe) {
                npe.printStackTrace();
                System.out.println("null\n");
            }
            catch (JsonGenerationException e) {
                e.printStackTrace();
            }
            catch (JsonMappingException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Runs the QuerySerializer by initializing the relevant
     * AbstractSyntaxTree implementation (depending on specified query
     * language) and transforms and writes the tree's requestMap to
     * the specified output file.
     *
     * @param outFile
     *            The file to which the serialization is written
     * @param query
     *            The query string
     * @param queryLanguage
     *            The query language. As of 17 Dec 2014, this must be
     *            one of 'poliqarpplus', 'cosmas2', 'annis' or 'cql'.
     * @throws IOException
     * @throws QueryException
     */
    public void run(String query, String queryLanguage)
            throws IOException {
        if (queryLanguage.equalsIgnoreCase("poliqarp")) {
            ast = new PoliqarpPlusQueryProcessor(query);
        }
        else if (queryLanguage.equalsIgnoreCase("cosmas2")) {
            ast = new Cosmas2QueryProcessor(query);
        }
        else if (queryLanguage.equalsIgnoreCase("poliqarpplus")) {
            ast = new PoliqarpPlusQueryProcessor(query);
        }
        else if (queryLanguage.equalsIgnoreCase("cql")) {
            ast = new CqlQueryProcessor(query);
        }
        else if (queryLanguage.equalsIgnoreCase("annis")) {
            ast = new AnnisQueryProcessor(query);
        }
        else {
            throw new IllegalArgumentException(queryLanguage
                    + " is not a supported query language!");
        }
        toJSON();
    }

    public QuerySerializer setQuery(String query, String ql, String version) {
        ast = new DummyQueryProcessor();
        if (query == null || query.isEmpty()) {
            ast.addError(StatusCodes.NO_QUERY, "You did not specify a query!");
        }
        else if (ql == null || ql.isEmpty()) {
            ast.addError(StatusCodes.NO_QUERY,
                    "You did not specify any query language!");
        }
        else if (ql.equalsIgnoreCase("poliqarp")) {
            ast = new PoliqarpPlusQueryProcessor(query);
        }
        else if (ql.equalsIgnoreCase("cosmas2")) {
            ast = new Cosmas2QueryProcessor(query);
        }
        else if (ql.equalsIgnoreCase("poliqarpplus")) {
            ast = new PoliqarpPlusQueryProcessor(query);
        }
        else if (ql.equalsIgnoreCase("cql")) {
            if (version == null)
                ast = new CqlQueryProcessor(query);
            else
                ast = new CqlQueryProcessor(query, version);
        }
        else if (ql.equalsIgnoreCase("annis")) {
            ast = new AnnisQueryProcessor(query);
        }
        else {
            ast.addError(StatusCodes.UNKNOWN_QL, ql
                    + " is not a supported query language!");
        }
        return this;
    }

    public QuerySerializer setQuery(String query, String ql) {
        return setQuery(query, ql, "");
    }

    public void setVerbose(boolean verbose) {
        AbstractQueryProcessor.verbose = verbose;
    }
    
    public final String toJSON() {
        String ser = JsonUtils.toJSON(raw());
        qllogger.info("Serialized query: " + ser);
        return ser;
    }

    public final Map build() {
        return raw();
    }

    private Map raw() {
        if (ast != null) {
            Map<String, Object> requestMap = ast.getRequestMap();
            Map meta = (Map) requestMap.get("meta");
            Map collection = (Map) requestMap.get("collection");
            List errors = (List) requestMap.get("errors");
            List warnings = (List) requestMap.get("warnings");
            List messages = (List) requestMap.get("messages");
            this.collection = mergeCollection(collection, this.collection);
            requestMap.put("collection", this.collection);
            if (this.meta != null) {
                meta.putAll(this.meta);
                requestMap.put("meta", meta);
            }
            if (this.errors != null) {
                errors.addAll(this.errors);
                requestMap.put("errors", errors);
            }
            if (this.warnings != null) {
                warnings.addAll(this.warnings);
                requestMap.put("warnings", warnings);
            }
            if (this.messages != null) {
                messages.addAll(this.messages);
                requestMap.put("messages", messages);
            }

            return requestMap;
        }
        return new HashMap<>();
    }

    private Map<String, Object> mergeCollection(Map<String, Object> collection1, Map<String, Object> collection2) {
        LinkedHashMap<String, Object> docGroup = KoralObjectGenerator.makeDocGroup("and");
        ArrayList<Object> operands = (ArrayList<Object>) docGroup.get("operands");
        if (collection1 == null || collection1.isEmpty()) {
            return collection2;
        } else if (collection2 == null || collection2.isEmpty()) {
            return collection1;
        } else {
            operands.add(collection1);
            operands.add(collection2);
            return docGroup;   
        }
    }
    
    public QuerySerializer addMeta(String cli, String cri, int cls, int crs,
            int num, int pageIndex) {
        MetaQueryBuilder meta = new MetaQueryBuilder();
        meta.setSpanContext(cls, cli, crs, cri);
        meta.addEntry("startIndex", pageIndex);
        meta.addEntry("count", num);
        this.meta = meta.raw();
        return this;
    }

    public QuerySerializer addMeta(String context, int num, int pageidx) {
        MetaQueryBuilder meta = new MetaQueryBuilder();
        meta.addEntry("startIndex", pageidx);
        meta.addEntry("count", num);
        meta.setSpanContext(context);
        this.meta = meta.raw();
        return this;
    }

    public QuerySerializer addMeta(MetaQueryBuilder meta) {
        this.meta = meta.raw();
        return this;
    }

    @Deprecated
    public QuerySerializer setCollectionSimple(String collection) {
        CollectionQueryBuilder qobj = new CollectionQueryBuilder();
        qobj.addResource(collection);
        this.collection = (Map<String, Object>) qobj.raw();
        return this;
    }

    public QuerySerializer setCollection(String collection) {
        CollectionQueryProcessor tree = new CollectionQueryProcessor();
        Map<String, Object> collectionRequest = tree.getRequestMap();
        tree.process(collection);
        this.collection = (Map<String, Object>) collectionRequest.get("collection");
        this.errors = (List) collectionRequest.get("errors");
        this.warnings = (List) collectionRequest.get("warnings");
        this.messages = (List) collectionRequest.get("messages");
        return this;
    }

    public QuerySerializer setCollection(CollectionQueryBuilder2 collections) {
        this.collection = (Map<String, Object>) collections.raw();
        return this;
    }

    public QuerySerializer setDeprCollection
            (CollectionQueryBuilder collections) {
        this.collection = (Map<String, Object>) collections.raw();
        return this;
    }
}
