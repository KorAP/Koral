import static org.junit.Assert.*;

import org.junit.Test;

import de.ids_mannheim.korap.query.serialize.AqlTree;
import de.ids_mannheim.korap.query.serialize.PoliqarpPlusTree;
import de.ids_mannheim.korap.util.QueryException;

public class AqlTreeTest {
	
	AqlTree aqlt;
	String map;
	private String query;

	private boolean equalsQueryContent(String res, String query) throws QueryException {
		res = res.replaceAll(" ", "");
		aqlt = new AqlTree(query);
		String queryMap = aqlt.getRequestMap().get("query").toString().replaceAll(" ", "");
		return res.equals(queryMap);
	}
	
	@Test
	public void testContext() throws QueryException {
		String contextString = "http://ids-mannheim.de/ns/KorAP/json-ld/v0.1/context.jsonld";
		aqlt = new AqlTree("Test");
		assertEquals(contextString.replaceAll(" ", ""), aqlt.getRequestMap().get("@context").toString().replaceAll(" ", ""));
	}
	
	@Test
	public void testSingleTokens() throws QueryException {
		// "Mann"
		query = "\"Mann\"";
		String token1 = "{@type=korap:token, wrap={@type=korap:term, key=Mann, match=match:eq}}";
		assertTrue(equalsQueryContent(token1, query));
		
		// [orth!=Frau]
		query = "tok!=\"Frau\"";
		String token2 = "{@type=korap:token, wrap={@type=korap:term, key=Frau, match=match:ne}}";
		assertTrue(equalsQueryContent(token2, query));
		
		// Mann
		query = "Mann";
		String token4 = "{@type=korap:span, layer=Mann}";
		assertTrue(equalsQueryContent(token4, query));
	}
	
	@Test 
	public void testSpans() throws QueryException {
		query = "node";
		String span1 = 
				 "{@type=korap:span}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(span1.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testRegex() throws QueryException {
		query = "/Mann/";
		String regex1 = "{@type=korap:token, wrap={@type=korap:term, type=type:regex, key=Mann, match=match:eq}}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(regex1.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testLayers() throws QueryException {
		query = "cnx/cat=\"NP\"";
		String layers1 = "{@type=korap:span, foundry=cnx, layer=cat, key=NP, match=match:eq}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(layers1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "treetagger/pos=\"NN\"";
		String layers2 = "{@type=korap:token, wrap={@type=korap:term, foundry=treetagger, layer=pos, key=NN, match=match:eq}}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(layers2.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testSimpleDominance() throws QueryException {
		query = "node & node & #2 > #1";
		String dom1 = 
				"{@type=korap:group, operation=operation:treeRelation, operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:span}" +
				"], treeRelation={@type=korap:treeRelation, reltype=dominance}" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(dom1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "\"Mann\" & node & #2 > #1";
		String dom2 = 
				"{@type=korap:group, operation=operation:treeRelation, operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:token, wrap={@type=korap:term, key=Mann, match=match:eq}}" +
				"], treeRelation={@type=korap:treeRelation, reltype=dominance}" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(dom2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "\"Mann\" & node & #2 >[cnx/cat=\"NP\"] #1";
		String dom3 = 
				"{@type=korap:group, operation=operation:treeRelation, operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:token, wrap={@type=korap:term, key=Mann, match=match:eq}}" +
				"], treeRelation={@type=korap:treeRelation, reltype=dominance, foundry=cnx, layer=cat, key=NP, match=match:eq}" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(dom3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "\"Mann\" & node & #2 >@l[cnx/cat=\"NP\"] #1";
		String dom4 = 
				"{@type=korap:group, operation=operation:treeRelation, operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:token, wrap={@type=korap:term, key=Mann, match=match:eq}}" +
				"], treeRelation={@type=korap:treeRelation, reltype=dominance, index=0, foundry=cnx, layer=cat, key=NP, match=match:eq}" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(dom4.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	
}

