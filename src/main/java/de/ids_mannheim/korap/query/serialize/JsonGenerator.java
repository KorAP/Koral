package de.ids_mannheim.korap.query.serialize;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author bingel, hanl
 */
public class JsonGenerator {

    ObjectMapper mapper;
    AbstractSyntaxTree ast;
    private Serializer serializer;
    private org.slf4j.Logger log = LoggerFactory
            .getLogger(JsonGenerator.class);

    public JsonGenerator() {
        mapper = new ObjectMapper();
        serializer = new Serializer();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        /*
         * just for testing...
		 */
        JsonGenerator jg = new JsonGenerator();
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
            }
        }
    }

    /**
     * Runs the JsonGenerator by initializing the relevant AbstractSyntaxTree implementation (depending on specified query language)
     * and transforms and writes the tree's requestMap to the specified output file.
     *
     * @param outFile       The file to which the serialization is written
     * @param query         The query string
     * @param queryLanguage The query language. As of 13/11/20, this must be either 'poliqarp' or 'poliqarpplus'. Some extra maven stuff needs to done to support CosmasII ('cosmas') [that maven stuff would be to tell maven how to build the cosmas grammar and where to find the classes]
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    public void run(String query, String queryLanguage, String outFile)
            throws JsonGenerationException, JsonMappingException, IOException {
        if (queryLanguage.equals("poliqarp")) {
            ast = new PoliqarpPlusTree(query);
//		} else if (queryLanguage.equals("cosmas")) {
//			ast = new CosmasTree(query);
        } else if (queryLanguage.equals("poliqarpplus")) {
            ast = new PoliqarpPlusTree(query);
        } else {
            throw new IllegalArgumentException(queryLanguage + " is not a supported query language!");
        }
        Map<String, Object> requestMap = ast.getRequestMap();
        mapper.writeValue(new File(outFile), requestMap);
    }

    public String run(String query, String ql, List<String> parents,
                      String cli, String cri, int cls, int crs, int page, int num) {
        if (ql.toLowerCase().equals("poliqarp")) {
            ast = new PoliqarpPlusTree(query);
//		} else if (queryLanguage.equals("cosmas")) {
//			ast = new CosmasTree(query);
        } else if (ql.toLowerCase().equals("poliqarpplus")) {
            ast = new PoliqarpPlusTree(query);
        } else {
            throw new IllegalArgumentException(ql + " is not a supported query language!");
        }
        Map<String, Object> requestMap = ast.getRequestMap();
        try {
            List<Map> meta_re = serializer.serializeResources(parents);
            requestMap.put("meta", meta_re);
            requestMap = serializer.addParameters(requestMap, page, num,
                    cli, cri, cls, crs);
            String res = mapper.writeValueAsString(requestMap);
            return res;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }
}
