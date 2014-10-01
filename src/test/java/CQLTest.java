import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.z3950.zing.cql.CQLParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ids_mannheim.korap.query.serialize.CQLTree;
import de.ids_mannheim.korap.query.serialize.CosmasTree;
import de.ids_mannheim.korap.util.QueryException;


public class CQLTest {
	
	String query;
	String version ="1.2";
	ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void testExceptions() throws CQLParseException, IOException {
		query = "(Kuh) prox (Germ) ";
		try {
			CQLTree cqlTree = new CQLTree(query, version);
		} catch (QueryException e) {
			int errorCode = Integer.parseInt(e.getMessage().split(":")[0].replace("SRU diagnostic ", ""));
			assertEquals(48,errorCode);
		}
		
		query = "(Kuh) or/rel.combine=sum (Germ) ";		
		try {
			CQLTree cqlTree = new CQLTree(query, version);
		}catch (QueryException e) {			
			int errorCode = Integer.parseInt(e.getMessage().split(":")[0].replace("SRU diagnostic ", ""));
			assertEquals(20,errorCode);
		}
		
		query = "dc.title any Germ ";
		try {
			CQLTree cqlTree = new CQLTree(query, version);
		} catch (QueryException e) {
			int errorCode = Integer.parseInt(e.getMessage().split(":")[0].replace("SRU diagnostic ", ""));
			assertEquals(16,errorCode);
		}
		
		query = "cql.serverChoice any Germ ";
		try {
			CQLTree cqlTree = new CQLTree(query, version);
		} catch (QueryException e) {
			int errorCode = Integer.parseInt(e.getMessage().split(":")[0].replace("SRU diagnostic ", ""));
			assertEquals(19,errorCode);
		}
		
		query = "";
		try {
			CQLTree cqlTree = new CQLTree(query, version);
		} catch (QueryException e) {
			int errorCode = Integer.parseInt(e.getMessage().split(":")[0].replace("SRU diagnostic ", ""));
			assertEquals(27,errorCode);
		}
	}
	
	@Test
	public void testAndQuery() throws CQLParseException, IOException, QueryException{
		query="(Sonne) and (scheint)";	
		String jsonLd = 
			"{@type : korap:group, operation : operation:sequence, inOrder : false," +		
			"distances:[ "+
				"{@type : korap:distance, key : s, min : 0, max : 0 } ],"+
					"operands : ["+
						"{@type : korap:token, wrap : {@type : korap:term,key : Sonne, layer : orth, match : match:eq}}," + 
						"{@type : korap:token,wrap : {@type : korap:term,key : scheint,layer : orth,match : match:eq}" +
					"}]}";
			
			CQLTree cqlTree = new CQLTree(query, version);		
			String serializedQuery = mapper.writeValueAsString(cqlTree.getRequestMap().get("query"));
			assertEquals(jsonLd.replace(" ", ""), serializedQuery.replace("\"", ""));
//			/System.out.println(serializedQuery);
		//CosmasTree ct = new CosmasTree("Sonne und scheint");
		//serializedQuery = mapper.writeValueAsString(ct.getRequestMap().get("query"));
		//assertEquals(jsonLd.replace(" ", ""), serializedQuery.replace("\"", ""));
	}
	
	@Test
	public void testBooleanQuery() throws CQLParseException, IOException, QueryException{		
		query="((Sonne) or (Mond)) and (scheint)";		
		String jsonLd = 
			"{@type:korap:group, operation:operation:sequence, inOrder : false, distances:[" +
				"{@type:korap:distance, key:s, min:0, max:0}" +
				"], operands:[" +
					"{@type:korap:group, operation:operation:or, operands:[" +
						"{@type:korap:token, wrap:{@type:korap:term, key:Sonne, layer:orth, match:match:eq}}," +
						"{@type:korap:token, wrap:{@type:korap:term, key:Mond, layer:orth, match:match:eq}}" +
					"]}," +
					"{@type:korap:token, wrap:{@type:korap:term, key:scheint, layer:orth, match:match:eq}}" +
			"]}";
		CQLTree cqlTree = new CQLTree(query, version);		
		String serializedQuery = mapper.writeValueAsString(cqlTree.getRequestMap().get("query"));
		assertEquals(jsonLd.replace(" ", ""), serializedQuery.replace("\"", ""));
		
		
		query="(scheint) and ((Sonne) or (Mond))";
		jsonLd = 
				"{@type:korap:group, operation:operation:sequence, inOrder : false, distances:[" +
						"{@type:korap:distance, key:s, min:0, max:0}" +
					"], operands:[" +
						"{@type:korap:token, wrap:{@type:korap:term, key:scheint, layer:orth, match:match:eq}}," +
						"{@type:korap:group, operation:operation:or, operands:[" +
							"{@type:korap:token, wrap:{@type:korap:term, key:Sonne, layer:orth, match:match:eq}}," +
							"{@type:korap:token, wrap:{@type:korap:term, key:Mond, layer:orth, match:match:eq}}" +
					"]}" +
				"]}";
		cqlTree = new CQLTree(query, version);		
		serializedQuery = mapper.writeValueAsString(cqlTree.getRequestMap().get("query"));
		assertEquals(jsonLd.replace(" ", ""), serializedQuery.replace("\"", ""));
		
	}
	
	@Test
	public void testOrQuery() throws CQLParseException, IOException, QueryException{
		query = "(Sonne) or (Mond)";		
		String jsonLd = 
			"{@type:korap:group, operation:operation:or, operands:[" +
				"{@type:korap:token, wrap:{@type:korap:term, key:Sonne, layer:orth, match:match:eq}}," +
				"{@type:korap:token, wrap:{@type:korap:term, key:Mond, layer:orth, match:match:eq}}" +
			"]}";		
		
		CQLTree cqlTree = new CQLTree(query, version);		
		String serializedQuery = mapper.writeValueAsString(cqlTree.getRequestMap().get("query"));
		assertEquals(jsonLd.replace(" ", ""), serializedQuery.replace("\"", ""));
		
		query="(\"Sonne scheint\") or (Mond)";		
		jsonLd = 
			"{@type:korap:group, operation:operation:or, operands:[" +
				"{@type:korap:group, operation:operation:sequence, operands:[" +
					"{@type:korap:token, wrap:{@type:korap:term, key:Sonne, layer:orth, match:match:eq}}," +
					"{@type:korap:token, wrap:{@type:korap:term, key:scheint, layer:orth, match:match:eq}}" +
				"]}," +
				"{@type:korap:token, wrap:{@type:korap:term, key:Mond, layer:orth, match:match:eq}}" +
			"]}";
		
		cqlTree = new CQLTree(query, version);		
		serializedQuery = mapper.writeValueAsString(cqlTree.getRequestMap().get("query"));
		assertEquals(jsonLd.replace(" ", ""), serializedQuery.replace("\"", ""));
				
		query="(\"Sonne scheint\") or (\"Mond scheint\")";		
		jsonLd = 
			"{@type:korap:group, operation:operation:or, operands:[" +
					"{@type:korap:group, operation:operation:sequence, operands:[" +
						"{@type:korap:token, wrap:{@type:korap:term, key:Sonne, layer:orth, match:match:eq}}," +
						"{@type:korap:token, wrap:{@type:korap:term, key:scheint, layer:orth, match:match:eq}}" +
					"]}," +
					"{@type:korap:group, operation:operation:sequence, operands:[" +
						"{@type:korap:token, wrap:{@type:korap:term, key:Mond, layer:orth, match:match:eq}}," +
						"{@type:korap:token, wrap:{@type:korap:term, key:scheint, layer:orth, match:match:eq}}" +
					"]}" +
				"]}";
		cqlTree = new CQLTree(query, version);		
		serializedQuery = mapper.writeValueAsString(cqlTree.getRequestMap().get("query"));
		assertEquals(jsonLd.replace(" ", ""), serializedQuery.replace("\"", ""));
	}
	
	@Test
	public void testTermQuery() throws CQLParseException, IOException, QueryException{
		query = "Sonne";		
		String jsonLd = "{@type:korap:token, wrap:{@type:korap:term, key:Sonne, layer:orth, match:match:eq}}";		
		CQLTree cqlTree = new CQLTree(query, version);		
		String serializedQuery = mapper.writeValueAsString(cqlTree.getRequestMap().get("query"));		
		assertEquals(jsonLd.replace(" ", ""), serializedQuery.replace("\"", ""));
	}
	
	@Test
	public void testPhraseQuery() throws CQLParseException, IOException, QueryException{
		query="\"der Mann\"";				
		String jsonLd = 
			"{@type:korap:group, operation:operation:sequence, operands:[" +
				"{@type:korap:token, wrap:{@type:korap:term, key:der, layer:orth, match:match:eq}}," +
				"{@type:korap:token, wrap:{@type:korap:term, key:Mann, layer:orth, match:match:eq}}" +
			"]}";
		
		CQLTree cqlTree = new CQLTree(query, version);		
		String serializedQuery = mapper.writeValueAsString(cqlTree.getRequestMap().get("query"));
		assertEquals(jsonLd.replace(" ", ""), serializedQuery.replace("\"", ""));
		
		
		query="der Mann schläft";
		jsonLd = 
			"{@type:korap:group, operation:operation:sequence, operands:[" +
				"{@type:korap:token, wrap:{@type:korap:term, key:der, layer:orth, match:match:eq}}," +
				"{@type:korap:token, wrap:{@type:korap:term, key:Mann, layer:orth, match:match:eq}}," +
				"{@type:korap:token, wrap:{@type:korap:term, key:schläft, layer:orth, match:match:eq}}" +
			"]}";
		
		cqlTree = new CQLTree(query, version);		
		serializedQuery = mapper.writeValueAsString(cqlTree.getRequestMap().get("query"));
		assertEquals(jsonLd.replace(" ", ""), serializedQuery.replace("\"", ""));
	}	
}
