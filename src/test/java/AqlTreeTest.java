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
		String token2 = "{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Frau, match=match:ne}}";
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
	public void testDefPredicationInversion() throws QueryException {
		query = " #1 > #2 & cnx/cat=\"VP\" & cnx/cat=\"NP\"";
		String dom1 = 
				"{@type=korap:group, operation=operation:relation, operands=[" +
						"{@type=korap:span, foundry=cnx, layer=cat, key=VP, match=match:eq}," +
						"{@type=korap:span, foundry=cnx, layer=cat, key=NP, match=match:eq}" +
				"], relation={@type=korap:treeRelation, reltype=dominance}" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(dom1.replaceAll(" ", ""), map.replaceAll(" ", ""));
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
	public void testMultipleDominance() throws QueryException {
		query = "cat=\"CP\" & cat=\"VP\" & cat=\"NP\" & #1 > #2 > #3";
		String dom1 = 
				"{@type=korap:group, operation=operation:relation, operands=[" +
						"{@type=korap:group, operation=operation:focus, operands=[" +
							"{@type=korap:group, operation=operation:relation, operands=[" +
								"{@type=korap:span, layer=cat, key=CP, match=match:eq}," +
								"{@type=korap:group, operation=operation:class, class=0, operands=[" +
									"{@type=korap:span, layer=cat, key=VP, match=match:eq}" +
								"]}" +
							"], relation={@type=korap:treeRelation, reltype=dominance}}" +
						"], classRef=[0]}," +
						"{@type=korap:span, layer=cat, key=NP, match=match:eq}" +
				"], relation={@type=korap:treeRelation, reltype=dominance}" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(dom1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "cat=\"CP\" & cat=\"VP\" & cat=\"NP\" & cat=\"DP\" & #1 > #2 > #3 > #4";
		String dom2 = 
				"{@type=korap:group, operation=operation:relation, operands=[" +
						"{@type=korap:group, operation=operation:focus, operands=[" +
							"{@type=korap:group, operation=operation:relation, operands=[" +
								"{@type=korap:group, operation=operation:focus, operands=[" +
									"{@type=korap:group, operation=operation:relation, operands=[" +
										"{@type=korap:span, layer=cat, key=CP, match=match:eq}," +
										"{@type=korap:group, operation=operation:class, class=0, operands=[" +
											"{@type=korap:span, layer=cat, key=VP, match=match:eq}" +
										"]}" +
									"], relation={@type=korap:treeRelation, reltype=dominance}}" +
								"], classRef=[0]}," +
								"{@type=korap:group, operation=operation:class, class=1, operands=[" +
									"{@type=korap:span, layer=cat, key=NP, match=match:eq}" +
								"]}" +
							"], relation={@type=korap:treeRelation, reltype=dominance}}" +
						"], classRef=[1]}," +
						"{@type=korap:span, layer=cat, key=DP, match=match:eq}" +
					"], relation={@type=korap:treeRelation, reltype=dominance}" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(dom2.replaceAll(" ", ""), map.replaceAll(" ", ""));
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
		String seq1 = 
				"{@type=korap:group, operation=operation:sequence, " +
					"operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:span}" +
					"], inOrder=true" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(seq1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "node & node & #1 .* #2";
		String seq2 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:span}" +
					"], distances=[" +
						"{@type=korap:distance, key=w, min=0, max=100}" +
					"], inOrder=true" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(seq2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "node & node & #1 .2,3 #2";
		String seq3 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:span}" +
					"], distances=[" +
						"{@type=korap:distance, key=w, min=2, max=3}" +
					"], inOrder=true" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(seq3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
	}
	
	@Test
	public void testMultipleSequence() throws QueryException {
		query = "tok=\"Sonne\" & tok=\"Mond\" & tok=\"Sterne\" & #1 .0,2 #2 .0,4 #3";
		String seq4 = 
				"{@type=korap:group, operation=operation:sequence," +
					"operands=[" +
						"{@type=korap:group, operation=operation:focus, operands=[" +
							"{@type=korap:group, operation=operation:sequence, operands=[" +
								"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Sonne, match=match:eq}}," +
								"{@type=korap:group, operation=operation:class, class=0, operands=[" +
									"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mond, match=match:eq}}" +
								"]}" +
							"], distances=[" +
								"{@type=korap:distance, key=w, min=0, max=2}" +
							"], inOrder=true}" +
						"], classRef=[0]}," +	
						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Sterne, match=match:eq}}" +
					"],distances=[" +
						"{@type=korap:distance, key=w, min=0, max=4}" +
					"], inOrder=true" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(seq4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "node & node & node & #1 . #2 .1,3 #3";
		String seq5 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:group, operation=operation:focus, operands=[" +
							"{@type=korap:group, operation=operation:sequence, operands=[" +
								"{@type=korap:span}," +
								"{@type=korap:group, operation=operation:class, class=0, operands=[" +
									"{@type=korap:span}" +
								"]} "+
							"], inOrder=true}" +
						"], classRef=[0]}," +
						"{@type=korap:span}" +
					"], distances=[" +
							"{@type=korap:distance, key=w, min=1, max=3}" +
						"], inOrder=true" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(seq5.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testMultipleMixedOperators() throws QueryException {
		query = "tok=\"Sonne\" & tok=\"Mond\" & tok=\"Sterne\" & #1 > #2 .0,4 #3";
		String seq4 = 
					"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:group, operation=operation:focus, operands=[" +
									"{@type=korap:group, operation=operation:relation, operands=[" +
										"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Sonne, match=match:eq}}," +
										"{@type=korap:group, operation=operation:class, class=0, operands=[" +
											"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mond, match=match:eq}}" +
										"]}" +
									"], relation={@type=korap:treeRelation, reltype=dominance}}" +
								"], classRef=[0]}," +
								"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Sterne, match=match:eq}}" +
						"], distances=[" +
							"{@type=korap:distance, key=w, min=0, max=4}" +
						"], inOrder=true" +
					"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(seq4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "tok=\"Sonne\" & tok=\"Mond\" & #1 > #2 .0,4  tok=\"Sterne\"";
		String seq5 = 
					"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:group, operation=operation:focus, operands=[" +
									"{@type=korap:group, operation=operation:relation, operands=[" +
										"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Sonne, match=match:eq}}," +
										"{@type=korap:group, operation=operation:class, class=0, operands=[" +
											"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mond, match=match:eq}}" +
										"]}" +
									"], relation={@type=korap:treeRelation, reltype=dominance}}" +
								"], classRef=[0]}," +
								"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Sterne, match=match:eq}}" +
						"], distances=[" +
							"{@type=korap:distance, key=w, min=0, max=4}" +
						"], inOrder=true" +
					"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(seq5.replaceAll(" ", ""), map.replaceAll(" ", ""));
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
		
		query = "node & node & #2 _i_ #1";
		String pos2 = 
				"{@type=korap:group, operation=operation:position, operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:span}" +
				"], frame=frame:contains" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(pos2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "node & node & #2 _l_ #1";
		String pos3 = 
				"{@type=korap:group, operation=operation:position, operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:span}" +
				"], inOrder=false, frame=frame:startswith" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(pos3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "node & \"Mann\" & #1 _r_ #2";
		String pos4 = 
					"{@type=korap:group, operation=operation:position, operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:token, wrap={@type=korap:term, key=Mann, match=match:eq}}" +
						"], inOrder=false, frame=frame:endswith" +
					"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(pos4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "node & \"Mann\" & #2 _r_ #1";
		String pos5 = 
					"{@type=korap:group, operation=operation:position, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=Mann, match=match:eq}}," +
						"{@type=korap:span}" +
						"], inOrder=false, frame=frame:endswith" +
					"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(pos5.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testMultiplePredications() throws QueryException {
		// a noun before a verb before a preposition
		query = "pos=\"N\" & pos=\"V\" & pos=\"P\" & #1 . #2 & #2 . #3"; 
		String mult1 = 
		"{@type=korap:group, operation=operation:sequence, operands=[" +
			"{@type=korap:group, operation=operation:focus, operands=[" +
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=N, match=match:eq}}," +
					"{@type=korap:group, operation=operation:class, class=0, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=V, match=match:eq}}" +
					"]}" +
				"], inOrder=true}" +
			"], classRef=[0]}," +
			"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=P, match=match:eq}}" +
		"], inOrder=true}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(mult1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// a noun before a verb before a preposition
		query = "pos=\"N\" & pos=\"V\" & #1 . #2 & #2 . pos=\"P\""; 
		String mult2 = 
		"{@type=korap:group, operation=operation:sequence, operands=[" +
			"{@type=korap:group, operation=operation:focus, operands=[" +
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=N, match=match:eq}}," +
					"{@type=korap:group, operation=operation:class, class=0, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=V, match=match:eq}}" +
					"]}" +
				"], inOrder=true}" +
			"], classRef=[0]}," +
			"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=P, match=match:eq}}" +
		"], inOrder=true}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(mult2.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}	
	
	@Test
	public void testUnaryRelations() throws QueryException {
		query = "node & #1:tokenarity=2";
		String unary1 = 
				"{@type=korap:span, attr={@type=korap:term, tokenarity={@type=korap:boundary,min=2,max=2}}}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(unary1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "cnx/cat=\"NP\" & #1:tokenarity=2";
		String unary2 = 
				"{@type=korap:span, foundry=cnx, layer=cat, key=NP, match=match:eq, attr={@type=korap:term, tokenarity={@type=korap:boundary,min=2,max=2}}}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(unary2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "cnx/cat=\"NP\" & #1:root";
		String unary3 = 
				"{@type=korap:span, foundry=cnx, layer=cat, key=NP, match=match:eq, attr={@type=korap:term, root=true}}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(unary3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "cnx/cat=\"NP\" & node & #1>#2 & #1:tokenarity=2";
		String unary4 = 
					"{@type=korap:group, operation=operation:relation, operands=[" +
						"{@type=korap:span, foundry=cnx, layer=cat, key=NP, match=match:eq, attr={@type=korap:term, tokenarity={@type=korap:boundary,min=2,max=2}}}," +
						"{@type=korap:span}" +
					"], relation={@type=korap:treeRelation, reltype=dominance}" +
					"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(unary4.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}	
	
	// TODO commonparent, commonancestor
	
}