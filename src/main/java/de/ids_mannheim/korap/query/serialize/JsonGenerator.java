package de.ids_mannheim.korap.query.serialize;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import de.ids_mannheim.korap.query.serialize.AbstractSyntaxTree;
import de.ids_mannheim.korap.query.serialize.PoliqarpPlusTree;

public class JsonGenerator {

	ObjectMapper mapper;
	AbstractSyntaxTree ast;
	
	public JsonGenerator() {
		mapper = new ObjectMapper();
	}

	/**
	 * Runs the JsonGenerator by initializing the relevant AbstractSyntaxTree implementation (depending on specified query language)
	 * and transforms and writes the tree's requestMap to the specified output file.
	 * @param outFile The file to which the serialization is written
	 * @param query The query string
	 * @param queryLanguage The query language. As of 13/11/20, this must be either 'poliqarp' or 'poliqarpplus'. Some extra maven stuff needs to done to support CosmasII ('cosmas') [that maven stuff would be to tell maven how to build the cosmas grammar and where to find the classes]
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public void run(String query, String queryLanguage, String outFile) throws JsonGenerationException, JsonMappingException, IOException {
		if (queryLanguage.equals("poliqarp")) {
			ast = new PoliqarpPlusTree(query);
//		} else if (queryLanguage.equals("cosmas")) {
//			ast = new CosmasTree(query);
		} else if (queryLanguage.equals("poliqarpplus")) {
			ast = new PoliqarpPlusTree(query);
		} else {
			throw new IllegalArgumentException(queryLanguage+ " is not a supported query language!");
		}
		Map<String, Object> requestMap = ast.getRequestMap();
		mapper.writeValue(new File(outFile), requestMap);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		 * just for testing...
		 */
		JsonGenerator jg = new JsonGenerator();
		int i=0;
		String[] queries;
		if (args.length==0) {
			queries = new String[] {
					"shrink({[base=foo]})",
					"shrink({[base=foo]}[orth=bar])",
					"shrink(1:[base=Der]{1:[base=Mann]})",
				};
		} else {
			queries = new String[] {args[0]};
		}
		
		for (String q : queries) {
			i++;
			try {
				System.out.println(q);
				jg.run(q, "poliqarp", System.getProperty("user.home")+"/bsp"+i+".json");
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
}
