package de.ids_mannheim.korap.query.serialize;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ids_mannheim.korap.query.serialize.util.KoralObjectGenerator;
import de.ids_mannheim.korap.query.serialize.util.StatusCodes;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Main class for Koral, serializes queries from concrete QLs to
 * KoralQuery
 * 
 * @author Joachim Bingel (bingel@ids-mannheim.de),
 *         Michael Hanl (hanl@ids-mannheim.de),
 *         Eliza Margaretha (margaretha@ids-mannheim.de)
 * @version 0.3.0
 * @since 0.1.0
 */
public class QuerySerializer {

    // fixme: not used in any way!
    @Deprecated
    static HashMap<String, Class<? extends AbstractQueryProcessor>> qlProcessorAssignment;



    static {
        qlProcessorAssignment = new HashMap<String, Class<? extends AbstractQueryProcessor>>();
        qlProcessorAssignment.put("poliqarpplus",
                PoliqarpPlusQueryProcessor.class);
        qlProcessorAssignment.put("cosmas2", Cosmas2QueryProcessor.class);
        qlProcessorAssignment.put("annis", AnnisQueryProcessor.class);
        qlProcessorAssignment.put("cql", CqlQueryProcessor.class);
    }

    private static ObjectMapper mapper = new ObjectMapper();
    private Logger qllogger = LoggerFactory.getLogger("ql");
    public static String queryLanguageVersion;

    private AbstractQueryProcessor ast;
    private Map<String, Object> collection = new LinkedHashMap<>();
    private Map<String, Object> meta;
    private List<Object> errors;
    private List<Object> warnings;
    private List<Object> messages;
    private org.slf4j.Logger log = LoggerFactory
            .getLogger(QuerySerializer.class);


    public QuerySerializer () {
        this.errors = new LinkedList<>();
        this.warnings = new LinkedList<>();
        this.messages = new LinkedList<>();
    }


    /**
     * @param args
     */
    public static void main (String[] args) {
        /*
         * just for testing...
         */
        BasicConfigurator.configure();
        QuerySerializer jg = new QuerySerializer();
        int i = 0;
        String[] queries = null;
        String ql = "poliqarpplus";
        if (args.length < 2) {
            System.err
                    .println("Usage: QuerySerializer \"query\" queryLanguage");
            System.exit(1);
        }
        else {
            queries = new String[] { args[0] };
            ql = args[1];
        }
        for (String q : queries) {
            i++;
            try {
                jg.run(q, ql);
                System.out.println();
            }
            catch (NullPointerException npe) {
                npe.printStackTrace();
                System.out.println("null\n");
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
     * @param query
     *            The query string
     * @param queryLanguage
     *            The query language. As of 17 Dec 2014, this must be
     *            one of 'poliqarpplus', 'cosmas2', 'annis' or 'cql'.
     * @throws IOException
     */
    public void run (String query, String queryLanguage) throws IOException {
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


    public QuerySerializer setQuery (String query, String ql, String version) {
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
			if (version == null) {
                ast = new CqlQueryProcessor(query);
			} else {
                ast = new CqlQueryProcessor(query, version);
            }
        } else if (ql.equalsIgnoreCase("fcsql")) {
			if (version == null) {
				ast.addError(StatusCodes.MISSING_VERSION,
						"SRU Version is missing!");
			} else {
				ast = new FCSQLQueryProcessor(query, version);
			}
        }else if (ql.equalsIgnoreCase("annis")) {
            ast = new AnnisQueryProcessor(query);

        }else {
			ast.addError(StatusCodes.UNKNOWN_QUERY_LANGUAGE,
                    ql + " is not a supported query language!");
        }
        return this;
    }


    public QuerySerializer setQuery (String query, String ql) {
        return setQuery(query, ql, "");
    }


    public void setVerbose (boolean verbose) {
        AbstractQueryProcessor.verbose = verbose;
    }


    public final String toJSON () {
        String ser;
        try {
            ser = mapper.writeValueAsString(raw());
            qllogger.info("Serialized query: " + ser);
        }
        catch (JsonProcessingException e) {
            return "";
        }
        return ser;
    }


    public final Map build () {
        return raw();
    }


    private Map raw () {
        if (ast != null) {
            Map<String, Object> requestMap = new HashMap<>(ast.getRequestMap());
            Map meta = (Map) requestMap.get("meta");
            Map collection = (Map) requestMap.get("collection");
            List errors = (List) requestMap.get("errors");
            List warnings = (List) requestMap.get("warnings");
            List messages = (List) requestMap.get("messages");
            collection = mergeCollection(collection, this.collection);
            requestMap.put("collection", collection);
            
            if (meta == null)
                meta = new HashMap();
            if (errors == null)
                errors = new LinkedList();
            if (warnings == null)
                warnings = new LinkedList();
            if (messages == null)
                messages = new LinkedList();

            if (this.meta != null) {
                meta.putAll(this.meta);
                requestMap.put("meta", meta);
            }
            if (this.errors != null && !this.errors.isEmpty()) {
                errors.addAll(this.errors);
                requestMap.put("errors", errors);
            }
            if (this.warnings != null && !this.warnings.isEmpty()) {
                warnings.addAll(this.warnings);
                requestMap.put("warnings", warnings);
            }
            if (this.messages != null && !this.messages.isEmpty()) {
                messages.addAll(this.messages);
                requestMap.put("messages", messages);
            }
            return cleanup(requestMap);
        }
        return new HashMap<>();
    }


    private Map<String, Object> cleanup (Map<String, Object> requestMap) {
        Iterator<Map.Entry<String, Object>> set = requestMap.entrySet()
                .iterator();
        while (set.hasNext()) {
            Map.Entry<String, Object> entry = set.next();
            if (entry.getValue() instanceof List
                    && ((List) entry.getValue()).isEmpty())
                set.remove();
            else if (entry.getValue() instanceof Map
                    && ((Map) entry.getValue()).isEmpty())
                set.remove();
            else if (entry.getValue() instanceof String
                    && ((String) entry.getValue()).isEmpty())
                set.remove();
        }
        return requestMap;
    }


    private Map<String, Object> mergeCollection (
            Map<String, Object> collection1, Map<String, Object> collection2) {
        if (collection1 == null || collection1.isEmpty()) {
            return collection2;
        }
        else if (collection2 == null || collection2.isEmpty()) {
            return collection1;
        }
        else if (collection1.equals(collection2)) {
            return collection1;
        }
        else {
            LinkedHashMap<String, Object> docGroup = KoralObjectGenerator
                    .makeDocGroup("and");
            ArrayList<Object> operands = (ArrayList<Object>) docGroup
                    .get("operands");
            operands.add(collection1);
            operands.add(collection2);
            return docGroup;
        }
    }


    @Deprecated
    public QuerySerializer addMeta (String cli, String cri, int cls, int crs,
            int num, int pageIndex) {
        MetaQueryBuilder meta = new MetaQueryBuilder();
        meta.setSpanContext(cls, cli, crs, cri);
        meta.addEntry("startIndex", pageIndex);
        meta.addEntry("count", num);
        this.meta = meta.raw();
        return this;
    }


    public QuerySerializer setMeta (Map<String, Object> meta) {
        this.meta = meta;
        return this;
    }


    public QuerySerializer setCollection (String collection) {
        CollectionQueryProcessor tree = new CollectionQueryProcessor();
        tree.process(collection);
        Map<String, Object> collectionRequest = tree.getRequestMap();
        if (collectionRequest.get("errors") != null)
            this.errors.addAll((List) collectionRequest.get("errors"));
        if (collectionRequest.get("warnings") != null)
            this.warnings.addAll((List) collectionRequest.get("warnings"));
        if (collectionRequest.get("messages") != null)
            this.messages.addAll((List) collectionRequest.get("messages"));
        this.collection = (Map<String, Object>) collectionRequest
                .get("collection");
        return this;
    }
}
