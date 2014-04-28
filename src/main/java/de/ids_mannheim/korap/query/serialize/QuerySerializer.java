package de.ids_mannheim.korap.query.serialize;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.ids_mannheim.korap.util.QueryException;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author bingel, hanl
 */
public class QuerySerializer {

    public static String queryLanguageVersion;

    private ObjectMapper mapper;
    private AbstractSyntaxTree ast;
    private org.slf4j.Logger log = LoggerFactory
            .getLogger(QuerySerializer.class);

    public QuerySerializer() {
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

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
                    /*
                     * negation
					 * elemente
					 * within
					 * regex
					 * & field_group
					 */
                    "Buch",
                    "das Buch",
                    "das /+w1:3 Buch",
                    "das /+w1:3,s1 Buch",
                    "(das /+w1:3,s1 Buch) /+w5 Tisch",
                    "(das /+w1:3,s1 Buch) /-w5 Tisch",
                    "(das /+w1:3,s1 Buch) /+w5 (auf dem Tisch)",


                    "Institut für Deutsche Sprache",
                    "Institut für deutsche Sprache",
                    "Institut für $deutsche Sprache",
                    "Institut für &deutsch Sprache",
                    "Institut für /+w2 Sprache",
                    "Institut für %+w1 deutsche Sprache",
                    "Institut für MORPH(A) Sprache",

                    "wegen #IN(L) <s>",
                    "$wegen #IN(L) <s>",
                    "#BED($wegen , +sa)",
                    "#BEG(#ELEM(S))",
                    "MORPH(V) #IN(L) #ELEM(S)",
                    "MORPH(V) #IN(R) #ELEM(S)",

            };
        } else {
            queries = new String[]{args[0]};
        }

        for (String q : queries) {
            i++;
            try {
                System.out.println(q);
                String ql = "cosmas2";
                jg.run(q, ql, System.getProperty("user.home") + "/" + ql + "_" + i + ".json");
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
        } else {
            throw new QueryException(queryLanguage + " is not a supported query language!");
        }
        Map<String, Object> requestMap = ast.getRequestMap();
        mapper.writeValue(new File(outFile), requestMap);
    }

    public String buildQuery(String query, String ql, String collection,
                             String cli, String cri, int cls, int crs,
                             int num, int page, boolean cutoff, String version)
            throws QueryException {
        try {
            if (ql.toLowerCase().equals("poliqarp")) {
                ast = new PoliqarpPlusTree(query);
            } else if (ql.toLowerCase().equals("cosmas2")) {
                ast = new CosmasTree(query);
            } else if (ql.toLowerCase().equals("poliqarpplus")) {
                ast = new PoliqarpPlusTree(query);
            } else if (ql.toLowerCase().equals("cql")) {
//                queryLanguageVersion = "1.2"; // set me
                ast = new CQLTree(query, version);
            } else if (ql.toLowerCase().equals("annis")) {
                ast = new AqlTree(query);
            } else {
                throw new QueryException(ql + " is not a supported query language!");
            }
        } catch (QueryException e) {
            throw e;
        } catch (Exception e) {
            throw new QueryException("UNKNOWN: Query could not be parsed");
        }

        Map<String, Object> requestMap = ast.getRequestMap();

        MetaQuery meta = new MetaQuery();
        meta.addContext(cls, cli, crs, cri);
        meta.addEntry("cutOff", cutoff);
        meta.addEntry("startPage", page);
        meta.addEntry("count", num);

        try {
            requestMap.put("collections", collection);
            requestMap.put("meta", meta.raw());
            return mapper.writeValueAsString(requestMap);
        } catch (IOException e) {
            return "";
        }
    }
}
