package de.ids_mannheim.korap.query.serialize;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ids_mannheim.korap.util.QueryException;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author bingel, hanl
 */
public class QuerySerializer {

    private ObjectMapper mapper;
    private AbstractSyntaxTree ast;
    private org.slf4j.Logger log = LoggerFactory
            .getLogger(QuerySerializer.class);

    public QuerySerializer() {
        mapper = new ObjectMapper();
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
                    "[base=foo]|([base=foo][base=bar])* meta author=Goethe&year=1815",
                    "([base=foo]|[base=bar])[base=foobar]",
                    "shrink({[base=Mann]})",
                    "shrink({[base=foo]}[orth=bar])",
                    "shrink(1:[base=Der]{1:[base=Mann]})",

                    "[base=Katze]",
                    "[base!=Katze]",
                    "[!base=Katze]",
                    "[base=Katze&orth=Katzen]",
                    "[base=Katze][orth=und][orth=Hunde]",
                    "[!(base=Katze&orth=Katzen)]",
                    "contains(<np>,[base=Mann])",
                    "startswith(<np>,[!pos=Det])",
                    "'vers{2,3}uch'",
                    "[orth='vers.*ch']",
                    "[(base=bar|base=foo)&orth=foobar]",

            };
        } else {
            queries = new String[]{args[0]};
        }

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
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     * @throws QueryException
     */
    public void run(String query, String queryLanguage, String outFile)
            throws JsonGenerationException, JsonMappingException, IOException, QueryException {
        if (queryLanguage.equals("poliqarp")) {
            ast = new PoliqarpPlusTree(query);
//		} else if (queryLanguage.equals("cosmas")) {
//			ast = new CosmasTree(query);
        } else if (queryLanguage.equals("poliqarpplus")) {
            ast = new PoliqarpPlusTree(query);
        } else {
            throw new QueryException(queryLanguage + " is not a supported query language!");
        }
        Map<String, Object> requestMap = ast.getRequestMap();
        mapper.writeValue(new File(outFile), requestMap);
    }

    public String buildQuery(String query, String ql, List<String> parents,
                             String cli, String cri, int cls, int crs,
                             int num, int page, boolean cutoff)
            throws QueryException {
        if (ql.toLowerCase().equals("poliqarp")) {
            ast = new PoliqarpPlusTree(query);
//		} else if (ql.toLowerCase().equals("cosmas")) {
//			ast = new CosmasTree(query);
        } else if (ql.toLowerCase().equals("poliqarpplus")) {
            ast = new PoliqarpPlusTree(query);
        } else {
            throw new QueryException(ql + " is not a supported query language!");
        }
        Map<String, Object> requestMap = ast.getRequestMap();
        MetaQuery metaQuery = new MetaQuery();
        metaQuery.addResources(parents);

        try {
            requestMap.put("meta", metaQuery.raw());
            requestMap = QueryUtils.addParameters(requestMap, page, num,
                    cli, cri, cls, crs, cutoff);
            String res = mapper.writeValueAsString(requestMap);
            return res;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }
}
