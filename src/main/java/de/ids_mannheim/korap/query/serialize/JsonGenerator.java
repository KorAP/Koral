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

	public void run(String outFile, String query, String queryLanguage) throws JsonGenerationException, JsonMappingException, IOException {
		if (queryLanguage.equals("poliqarp")) {
			ast = new PoliqarpPlusTree(query);
//		} else if (queryLanguage.equals("cosmas")) {
//			ast = new CosmasTree(query);
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
				"[base=foo]|([base=foo][base=bar])* meta author=Goethe&year=1815",
				"([base=foo]|[base=foo])[base=foobar]",
				"[base=foo]([base=foo]|[base=foobar])",
				};
		} else {
			queries = new String[] {args[0]};
		}
		
		for (String q : queries) {
			i++;
			try {
				System.out.println(q);
				jg.run(System.getProperty("user.home")+"/test"+i+".json", q, "poliqarp");
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
