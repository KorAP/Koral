import static org.junit.Assert.*;

import org.junit.Test;

import de.ids_mannheim.korap.query.serialize.AqlTree;
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
	public void testDirectDeclarationRelations() throws QueryException {
		query = "node > node";
		String ddr1 = 
				"{@type=korap:group, operation=operation:relation, operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:span}" +
				"], relation={@type=korap:treeRelation, reltype=dominance}" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(ddr1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "node > cnx/cat=\"NP\"";
		String ddr2 = 
				"{@type=korap:group, operation=operation:relation, operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:span, foundry=cnx, layer=cat, key=NP, match=match:eq}" +
				"], relation={@type=korap:treeRelation, reltype=dominance}" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(ddr2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
	}
	
	@Test
	public void testSimpleDominance() throws QueryException {
		query = "node & node & #2 > #1";
		String dom1 = 
				"{@type=korap:group, operation=operation:relation, operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:span}" +
				"], relation={@type=korap:treeRelation, reltype=dominance}" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(dom1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "\"Mann\" & node & #2 > #1";
		String dom2 = 
				"{@type=korap:group, operation=operation:relation, operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:token, wrap={@type=korap:term, key=Mann, match=match:eq}}" +
				"], relation={@type=korap:treeRelation, reltype=dominance}" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(dom2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "\"Mann\" & node & #2 >[cat=\"NP\"] #1";
		String dom3 = 
				"{@type=korap:group, operation=operation:relation, operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:token, wrap={@type=korap:term, key=Mann, match=match:eq}}" +
				"], relation={@type=korap:treeRelation, reltype=dominance, wrap=[{@type=korap:term, layer=cat, key=NP, match=match:eq}]}" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(dom3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "\"Mann\" & node & #2 >@l[cat=\"NP\"] #1";
		String dom4 = 
				"{@type=korap:group, operation=operation:relation, operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:token, wrap={@type=korap:term, key=Mann, match=match:eq}}" +
				"], relation={@type=korap:treeRelation, reltype=dominance, index=0, wrap=[{@type=korap:term, layer=cat, key=NP, match=match:eq}]}" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(dom4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "\"Mann\" & node & #2 >2,4 #1";
		String dom5 = 
				"{@type=korap:group, operation=operation:relation, operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:token, wrap={@type=korap:term, key=Mann, match=match:eq}}" +
				"], relation={@type=korap:treeRelation, reltype=dominance, " +
						"distance={@type=korap:distance, key=r, min=2, max=4}}" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(dom5.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testPointingRelations() throws QueryException {
		query = "node & node & #2 ->label[coref=\"true\"] #1";
		String dom1 = 
				"{@type=korap:group, operation=operation:relation, operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:span}" +
				"], relation={@type=korap:relation, reltype=label, wrap=[{@type=korap:term, layer=coref, key=true, match=match:eq}]}" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(dom1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "node & node & #2 ->label[mate/coref=\"true\"] #1";
		String dom2 = 
				"{@type=korap:group, operation=operation:relation, operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:span}" +
				"], relation={@type=korap:relation, reltype=label, wrap=[{@type=korap:term, foundry=mate, layer=coref, key=true, match=match:eq}]}" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(dom2.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testSequence() throws QueryException {
		query = "node & node & #1 . #2";
		String dom1 = 
				"{@type=korap:group, operation=operation:sequence, " +
					"operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:span}" +
					"], inOrder=true" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(dom1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "node & node & #1 .* #2";
		String dom2 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:span}" +
					"], distances=[" +
						"{@type=korap:distance, key=w, min=0, max=100}" +
					"], inOrder=true" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(dom2.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testPositions() throws QueryException {
		query = "node & node & #2 _=_ #1";
		String pos1 = 
				"{@type=korap:group, operation=operation:position, operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:span}" +
				"], frame=frame:matches" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(pos1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
//		query = "node & node & #2 _i_ #1";
//		String pos2 = 
//				"{@type=korap:group, operation=operation:position, operands=[" +
//						"{@type=korap:span}," +
//						"{@type=korap:span}" +
//				"], frame=frame:contains" +
//				"}";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(pos2.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		query = "node & node & #2 _l_ #1";
//		String pos3 = 
//				"{@type=korap:group, operation=operation:position, operands=[" +
//						"{@type=korap:span}," +
//						"{@type=korap:span}" +
//				"], frame=frame:startswith" +
//				"}";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(pos3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "node & \"Mann\" & #2 _r_ #1";
		String pos4 = 
				"{@type=korap:group, operation=operation:or, operands=[" +
					"{@type=korap:group, operation=operation:position, operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:token, wrap={@type=korap:term, type=type:regex, key=Mann, match=match:eq}}" +
						"], frame=frame:endswith" +
					"}," +
					"{@type=korap:group, operation=operation:position, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, type=type:regex, key=Mann, match=match:eq}}," +
						"{@type=korap:span}" +
						"], frame=frame:endswith" +
					"}" +
				"]}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(pos4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
	}
	
	
}

