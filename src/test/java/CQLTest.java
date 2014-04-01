import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.z3950.zing.cql.CQLParseException;

import de.ids_mannheim.korap.query.serialize.CQLTree;
import de.ids_mannheim.korap.util.QueryException;


public class CQLTest {
	
	String query;
	
	@Test
	public void testBooleanQuery() throws CQLParseException, IOException, QueryException{		
		query="((Sonne) or (Mond)) and (scheint)";		
		String jsonLd = 
			"{@type=korap:group, operation=operation:sequence, distances=[" +
				"{@type=korap:distance, key=t, min=0, max=0}" +
				"], operands=[" +
					"{@type=korap:group, operation=operation:or, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=Sonne, layer=orth, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, key=Mond, layer=orth, match=match:eq}}" +
					"]}," +
					"{@type=korap:token, wrap={@type=korap:term, key=scheint, layer=orth, match=match:eq}}" +
			"]}";
		CQLTree cqlTree = new CQLTree(query);		
		String serializedQuery = cqlTree.getRequestMap().get("query").toString();
		assertEquals(jsonLd.replace(" ", ""), serializedQuery.replace(" ", ""));
		
		
		query="(scheint) and ((Sonne) or (Mond))";
		jsonLd = 
				"{@type=korap:group, operation=operation:sequence, distances=[" +
						"{@type=korap:distance, key=t, min=0, max=0}" +
					"], operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=scheint, layer=orth, match=match:eq}}," +
						"{@type=korap:group, operation=operation:or, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, key=Sonne, layer=orth, match=match:eq}}," +
							"{@type=korap:token, wrap={@type=korap:term, key=Mond, layer=orth, match=match:eq}}" +
					"]}" +
				"]}";
		cqlTree = new CQLTree(query);		
		serializedQuery = cqlTree.getRequestMap().get("query").toString();
		assertEquals(jsonLd.replace(" ", ""), serializedQuery.replace(" ", ""));
		
	}
	
	@Test
	public void testOrQuery() throws CQLParseException, IOException, QueryException{
		query = "(Sonne) or (Mond)";		
		String jsonLd = 
			"{@type=korap:group, operation=operation:or, operands=[" +
				"{@type=korap:token, wrap={@type=korap:term, key=Sonne, layer=orth, match=match:eq}}," +
				"{@type=korap:token, wrap={@type=korap:term, key=Mond, layer=orth, match=match:eq}}" +
			"]}";		
		
		CQLTree cqlTree = new CQLTree(query);		
		String serializedQuery = cqlTree.getRequestMap().get("query").toString();
		assertEquals(jsonLd.replace(" ", ""), serializedQuery.replace(" ", ""));
		
		query="(\"Sonne scheint\") or (Mond)";		
		jsonLd = 
			"{@type=korap:group, operation=operation:or, operands=[" +
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, key=Sonne, layer=orth, match=match:eq}}," +
					"{@type=korap:token, wrap={@type=korap:term, key=scheint, layer=orth, match=match:eq}}" +
				"]}," +
				"{@type=korap:token, wrap={@type=korap:term, key=Mond, layer=orth, match=match:eq}}" +
			"]}";
		
		cqlTree = new CQLTree(query);		
		serializedQuery = cqlTree.getRequestMap().get("query").toString();
		assertEquals(jsonLd.replace(" ", ""), serializedQuery.replace(" ", ""));
				
		query="(\"Sonne scheint\") or (\"Mond scheint\")";		
		jsonLd = 
			"{@type=korap:group, operation=operation:or, operands=[" +
					"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=Sonne, layer=orth, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, key=scheint, layer=orth, match=match:eq}}" +
					"]}," +
					"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=Mond, layer=orth, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, key=scheint, layer=orth, match=match:eq}}" +
					"]}" +
				"]}";
		cqlTree = new CQLTree(query);		
		serializedQuery = cqlTree.getRequestMap().get("query").toString();
		assertEquals(jsonLd.replace(" ", ""), serializedQuery.replace(" ", ""));
	}
	
	@Test
	public void testTermQuery() throws CQLParseException, IOException, QueryException{
		query = "Sonne";		
		String jsonLd = "{@type=korap:token, wrap={@type=korap:term, key=Sonne, layer=orth, match=match:eq}}";		
		CQLTree cqlTree = new CQLTree(query);		
		String serializedQuery = cqlTree.getRequestMap().get("query").toString();		
		assertEquals(jsonLd.replace(" ", ""), serializedQuery.replace(" ", ""));
	}
	
	@Test
	public void testPhraseQuery() throws CQLParseException, IOException, QueryException{
		query="\"der Mann\"";				
		String jsonLd = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, key=der, layer=orth, match=match:eq}}," +
					"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}" +
				"]}";
		
		CQLTree cqlTree = new CQLTree(query);		
		String serializedQuery = cqlTree.getRequestMap().get("query").toString();
		assertEquals(jsonLd.replace(" ", ""), serializedQuery.replace(" ", ""));
		
		
		query="der Mann schläft";
		jsonLd = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, key=der, layer=orth, match=match:eq}}," +
					"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}," +
					"{@type=korap:token, wrap={@type=korap:term, key=schläft, layer=orth, match=match:eq}}" +
				"]}";
		
		cqlTree = new CQLTree(query);		
		serializedQuery = cqlTree.getRequestMap().get("query").toString();
		assertEquals(jsonLd.replace(" ", ""), serializedQuery.replace(" ", ""));
	}	
}
