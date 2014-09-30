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
		String token1 = "{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mann, match=match:eq}}";
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
		String regex1 = "{@type=korap:token, wrap={@type=korap:term, layer=orth, type=type:regex, key=Mann, match=match:eq}}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(regex1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "/.*?Mann.*?/";
		String regex2 = "{@type=korap:token, wrap={@type=korap:term, layer=orth, type=type:regex, key=.*?Mann.*?, match=match:eq}}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(regex2.replaceAll(" ", ""), map.replaceAll(" ", ""));
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
				"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(ddr1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "node > cnx/cat=\"NP\"";
		String ddr2 = 
				"{@type=korap:group, operation=operation:relation, operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:span, foundry=cnx, layer=cat, key=NP, match=match:eq}" +
				"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}" +
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
				"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}" +
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
				"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(dom1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "\"Mann\" & node & #2 > #1";
		String dom2 = 
				"{@type=korap:group, operation=operation:relation, operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mann, match=match:eq}}" +
				"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(dom2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "\"Mann\" & node & #2 >[cat=\"NP\"] #1";
		String dom3 = 
				"{@type=korap:group, operation=operation:relation, operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mann, match=match:eq}}" +
				"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c, key=NP, match=match:eq}}" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(dom3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "\"Mann\" & node & #2 >@l[cat=\"NP\"] #1";
		String dom4 = 
				"{@type=korap:group, operation=operation:relation, operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mann, match=match:eq}}" +
				"], relation={@type=korap:relation, index=0, wrap={@type=korap:term, layer=c, key=NP, match=match:eq}}" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(dom4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "\"Mann\" & node & #2 >2,4 #1";
		String dom5 = 
				"{@type=korap:group, operation=operation:relation, operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mann, match=match:eq}}" +
				"], relation={@type=korap:relation, boundary={@type=korap:boundary, min=2, max=4}, wrap={@type=korap:term, layer=c}}" +
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
						"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
							"{@type=korap:group, operation=operation:relation, operands=[" +
								"{@type=korap:span, layer=cat, key=CP, match=match:eq}," +
								"{@type=korap:group, operation=operation:class, class=0, operands=[" +
									"{@type=korap:span, layer=cat, key=VP, match=match:eq}" +
								"]}" +
							"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
						"]}," +
						"{@type=korap:span, layer=cat, key=NP, match=match:eq}" +
				"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(dom1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "cat=\"CP\" & cat=\"VP\" & cat=\"NP\" & cat=\"DP\" & #1 > #2 > #3 > #4";
		String dom2 = 
				"{@type=korap:group, operation=operation:relation, operands=[" +
						"{@type=korap:reference, operation=operation:focus, classRef=[1], operands=[" +
							"{@type=korap:group, operation=operation:relation, operands=[" +
								"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
									"{@type=korap:group, operation=operation:relation, operands=[" +
										"{@type=korap:span, layer=cat, key=CP, match=match:eq}," +
										"{@type=korap:group, operation=operation:class, class=0, operands=[" +
											"{@type=korap:span, layer=cat, key=VP, match=match:eq}" +
										"]}" +
									"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
								"]}," +
								"{@type=korap:group, operation=operation:class, class=1, operands=[" +
									"{@type=korap:span, layer=cat, key=NP, match=match:eq}" +
								"]}" +
							"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
						"]}," +
						"{@type=korap:span, layer=cat, key=DP, match=match:eq}" +
					"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(dom2.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testPointingRelations() throws QueryException {
		query = "node & node & #2 ->coref[val=\"true\"] #1";
		String dom1 = 
				"{@type=korap:group, operation=operation:relation, operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:span}" +
				"], relation={@type=korap:relation, wrap={@type=korap:term, layer=coref, key=true, match=match:eq}}" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(dom1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "node & node & #2 ->mate/coref[val=\"true\"] #1";
		String dom2 = 
				"{@type=korap:group, operation=operation:relation, operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:span}" +
				"], relation={@type=korap:relation, wrap={@type=korap:term, foundry=mate, layer=coref, key=true, match=match:eq}}" +
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
						"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=0}, min=0}" +
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
						"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=2, max=3}, min=2, max=3}" +
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
						"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
							"{@type=korap:group, operation=operation:sequence, operands=[" +
								"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Sonne, match=match:eq}}," +
								"{@type=korap:group, operation=operation:class, class=0, operands=[" +
									"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mond, match=match:eq}}" +
								"]}" +
							"], distances=[" +
								"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=0, max=2}, min=0, max=2}" +
							"], inOrder=true}" +
						"]}," +	
						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Sterne, match=match:eq}}" +
					"],distances=[" +
						"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=0, max=4}, min=0, max=4}" +
					"], inOrder=true" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(seq4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "node & node & node & #1 . #2 .1,3 #3";
		String seq5 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
							"{@type=korap:group, operation=operation:sequence, operands=[" +
								"{@type=korap:span}," +
								"{@type=korap:group, operation=operation:class, class=0, operands=[" +
									"{@type=korap:span}" +
								"]} "+
							"], inOrder=true}" +
						"]}," +
						"{@type=korap:span}" +
					"], distances=[" +
							"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=1, max=3}, min=1, max=3}" +
						"], inOrder=true" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(seq5.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "tok=\"Sonne\" & tok=\"Mond\" & tok=\"Sterne\" & tok=\"Himmel\" & #1 .0,2 #2 .0,4 #3 . #4";
		String seq6 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:reference, operation=operation:focus, classRef=[1], operands=[" +
						"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
								"{@type=korap:group, operation=operation:sequence, operands=[" +
									"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Sonne, match=match:eq}}," +
									"{@type=korap:group, operation=operation:class, class=0, operands=[" +
										"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mond, match=match:eq}}" +
									"]}" +
								"], distances=[" +
									"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=0, max=2}, min=0, max=2}" +
								"], inOrder=true}" +
							"]}," +	
							"{@type=korap:group, operation=operation:class, class=1, operands=[" +
								"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Sterne, match=match:eq}}" +
							"]}" +
						"],distances=[" +
							"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=0, max=4}, min=0, max=4}" +
						"], inOrder=true}" +
					"]}," +
					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Himmel, match=match:eq}}" +
				"], inOrder=true}" ;
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(seq6.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testMultipleMixedOperators() throws QueryException {
		query = "tok=\"Sonne\" & tok=\"Mond\" & tok=\"Sterne\" & #1 > #2 .0,4 #3";
		String seq4 = 
					"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
									"{@type=korap:group, operation=operation:relation, operands=[" +
										"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Sonne, match=match:eq}}," +
										"{@type=korap:group, operation=operation:class, class=0, operands=[" +
											"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mond, match=match:eq}}" +
										"]}" +
									"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
								"]}," +
								"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Sterne, match=match:eq}}" +
						"], distances=[" +
							"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=0, max=4}, min=0, max=4}" +
						"], inOrder=true" +
					"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(seq4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "tok=\"Sonne\" & tok=\"Mond\" & #1 > #2 .0,4  tok=\"Sterne\"";
		String seq5 = 
					"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
									"{@type=korap:group, operation=operation:relation, operands=[" +
										"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Sonne, match=match:eq}}," +
										"{@type=korap:group, operation=operation:class, class=0, operands=[" +
											"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mond, match=match:eq}}" +
										"]}" +
									"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
								"]}," +
								"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Sterne, match=match:eq}}" +
						"], distances=[" +
							"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=0, max=4}, min=0, max=4}" +
						"], inOrder=true" +
					"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(seq5.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "cat=\"NP\" & cat=\"VP\" & cat=\"PP\" & #1 $ #2 > #3";
		String cp2 =
				"{@type=korap:group, operation=operation:relation, operands=[" +
					"{@type=korap:reference, operation=operation:focus, classRef=[1], operands=[" +
						"{@type=korap:group, operation=operation:relation, operands=[" +
							"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
								"{@type=korap:group, operation=operation:relation, operands=[" +
									"{@type=korap:group, operation=operation:class, class=0, operands=[" +
										"{@type=korap:span}" +
									"]}," +
									"{@type=korap:span, layer=cat, key=NP, match=match:eq}" +
								"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
							"]}," +
							"{@type=korap:group, operation=operation:class, class=1, operands=[" +
								"{@type=korap:span, layer=cat, key=VP, match=match:eq}" +
							"]}" +
						"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}" +
						"}" +
					"]}," +
					"{@type=korap:span, layer=cat, key=PP, match=match:eq}" +
				"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(cp2.replaceAll(" ", ""), map.replaceAll(" ", ""));		
	}
	/*
	@Test
	public void testMultipleOperatorsWithSameOperands() throws QueryException {
		
		query = "cat=\"NP\" > cat=\"VP\" & #1 _l_ #2";
		String eq2 =
				"{@type=korap:group, operation=operation:position, frames=[frame:startswith], sharedClasses=[sharedClasses:includes], operands=[" +
						"{@type=korap:group, operation=operation:relation, operands=[" +
							"{@type=korap:group, operation=operation:class, class=1, operands=[" +
								"{@type=korap:span, layer=cat, key=NP, match=match:eq}" +
							"]}," +
							"{@type=korap:group, operation=operation:class, class=1, operands=[" +
								"{@type=korap:span, layer=cat, key=VP, match=match:eq}" +
							"]}" +
						"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}," +
					"{@type=korap:reference, operation=operation:focus, classRef=[2]}" +
				"]" +
				"}"; // ???
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(eq2.replaceAll(" ", ""), map.replaceAll(" ", ""));		
	}
	*/
	@Test
	public void testPositions() throws QueryException {
		query = "node & node & #2 _=_ #1";
		String pos1 = 
				"{@type=korap:group, operation=operation:position, frames=[frame:matches], sharedClasses=[sharedClasses:equals], operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:span}" +
				"], frame=frame:matches}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(pos1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "node & node & #2 _i_ #1";
		String pos2 = 
				"{@type=korap:group, operation=operation:position, frames=[frame:contains], sharedClasses=[sharedClasses:includes], operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:span}" +
				"], frame=frame:contains" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(pos2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "node & node & #2 _l_ #1";
		String pos3 = 
				"{@type=korap:group, operation=operation:position, frames=[frame:startswith], sharedClasses=[sharedClasses:includes], operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:span}" +
				"], frame=frame:startswith" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(pos3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "node & \"Mann\" & #1 _r_ #2";
		String pos4 = 
					"{@type=korap:group, operation=operation:position, frames=[frame:endswith], sharedClasses=[sharedClasses:includes], operands=[" +
						"{@type=korap:span}," +
						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mann, match=match:eq}}" +
						"], frame=frame:endswith" +
					"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(pos4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "node & \"Mann\" & #2 _r_ #1";
		String pos5 = 
					"{@type=korap:group, operation=operation:position, frames=[frame:endswith], sharedClasses=[sharedClasses:includes], operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mann, match=match:eq}}," +
						"{@type=korap:span}" +
						"], frame=frame:endswith" +
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
			"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=N, match=match:eq}}," +
					"{@type=korap:group, operation=operation:class, class=0, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=V, match=match:eq}}" +
					"]}" +
				"], inOrder=true}" +
			"]}," +
			"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=P, match=match:eq}}" +
		"], inOrder=true}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(mult1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// a noun before a verb before a preposition
		query = "pos=\"N\" & pos=\"V\" & #1 . #2 & #2 . pos=\"P\""; 
		String mult2 = 
		"{@type=korap:group, operation=operation:sequence, operands=[" +
			"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=N, match=match:eq}}," +
					"{@type=korap:group, operation=operation:class, class=0, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=V, match=match:eq}}" +
					"]}" +
				"], inOrder=true}" +
			"]}," +
			"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=P, match=match:eq}}" +
		"], inOrder=true}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(mult2.replaceAll(" ", ""), map.replaceAll(" ", ""));

		query = "pos=\"N\" & pos=\"V\" & pos=\"P\" & #1 > #2 & #1 > #3";
		String mult3 = 
			"{@type=korap:group, operation=operation:relation, operands=[" +
				"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
					"{@type=korap:group, operation=operation:relation, operands=[" +
						"{@type=korap:group, operation=operation:class, class=0, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=N, match=match:eq}}" +
						"]}," +
						"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=V, match=match:eq}}" +
					"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
				"]}," +
				"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=P, match=match:eq}}" +
			"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(mult3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "cat=\"NP\" & pos=\"V\" & pos=\"P\" & #1 > #2 & #1 > #3 & #2 . #3";
		String mult4 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					// reduce dominance relations "#1 > #2 & #1 > #3" to operand #2 in order to make it accessible for #2 . #3 (the last/outermost relation)  
					"{@type=korap:reference, operation=operation:focus, classRef=[1], operands=[" +
						"{@type=korap:group, operation=operation:relation, operands=[" +
							// dominance relation #1 > #2 is reduced to #1, for expressing #1 > #3
							"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
								"{@type=korap:group, operation=operation:relation, operands=[" +
									"{@type=korap:group, operation=operation:class, class=0, operands=[" +
										"{@type=korap:span, layer=cat, key=NP, match=match:eq}" +
									"]}," +
									"{@type=korap:group, operation=operation:class, class=1, operands=[" +
										"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=V, match=match:eq}}" +
									"]}" +
								"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
							"]}," +
							// establish class 2 around P for later reference
							"{@type=korap:group, operation=operation:class, class=2, operands=[" +
								"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=P, match=match:eq}}" +
							"]}" +
						"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
					"]}," +
					// refer back to class 2 as second operand
					"{@type=korap:reference, operation=operation:focus, classRef=[2]}" +
				"], inOrder=true}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(mult4.replaceAll(" ", ""), map.replaceAll(" ", ""));
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
					"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}" +
					"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(unary4.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}	
	
	@Test
	public void testCommonParent() throws QueryException {
		query = "cat=\"NP\" & cat=\"VP\" & #1 $ #2";
		String cp1 =
				"{@type=korap:group, operation=operation:relation, operands=[" +
						"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
							"{@type=korap:group, operation=operation:relation, operands=[" +
								"{@type=korap:group, operation=operation:class, class=0, operands=[" +
									"{@type=korap:span}" +
								"]}," +
								"{@type=korap:span, layer=cat, key=NP, match=match:eq}" +
							"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
						"]}," +
						"{@type=korap:span, layer=cat, key=VP, match=match:eq}" +
					"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
					"";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(cp1.replaceAll(" ", ""), map.replaceAll(" ", ""));		
		
		query = "cat=\"NP\" & cat=\"VP\" & cat=\"PP\" & #1 $ #2 $ #3";
		String cp2 =
				"{@type=korap:group, operation=operation:relation, operands=[" +
					"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
						"{@type=korap:group, operation=operation:relation, operands=[" +
							"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
								"{@type=korap:group, operation=operation:relation, operands=[" +
									"{@type=korap:group, operation=operation:class, class=0, operands=[" +
										"{@type=korap:span}" +
									"]}," +
									"{@type=korap:span, layer=cat, key=NP, match=match:eq}" +
								"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
							"]}," +
							"{@type=korap:span, layer=cat, key=VP, match=match:eq}" +
						"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}" +
						"}" +
					"]}," +
					"{@type=korap:span, layer=cat, key=PP, match=match:eq}" +
				"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}" +
				"}";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(cp2.replaceAll(" ", ""), map.replaceAll(" ", ""));		
		
		query = "cat=\"NP\" & cat=\"VP\" & cat=\"PP\" & cat=\"CP\" & #1 $ #2 $ #3 $ #4";
		String cp3 =
				"{@type=korap:group, operation=operation:relation, operands=[" +
					"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
						"{@type=korap:group, operation=operation:relation, operands=[" +
							"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
								"{@type=korap:group, operation=operation:relation, operands=[" +
									"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
										"{@type=korap:group, operation=operation:relation, operands=[" +
											"{@type=korap:group, operation=operation:class, class=0, operands=[" +
												"{@type=korap:span}" +
											"]}," +
											"{@type=korap:span, layer=cat, key=NP, match=match:eq}" +
										"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
									"]}," +
									"{@type=korap:span, layer=cat, key=VP, match=match:eq}" +
								"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
							"]}," +
							"{@type=korap:span, layer=cat, key=PP, match=match:eq}" +
						"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
					"]}," +
					"{@type=korap:span, layer=cat, key=CP, match=match:eq}" +
				"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}" +
				"}" +
				"";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(cp3.replaceAll(" ", ""), map.replaceAll(" ", ""));		
		
		query = "cat=\"NP\" & cat=\"VP\" & #1 $* #2";
		String cp4 =
				"{@type=korap:group, operation=operation:relation, operands=[" +
						"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
							"{@type=korap:group, operation=operation:relation, operands=[" +
								"{@type=korap:group, operation=operation:class, class=0, operands=[" +
									"{@type=korap:span}" +
								"]}," +
								"{@type=korap:span, layer=cat, key=NP, match=match:eq}" +
							"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c},boundary={@type=korap:boundary,min=1}}}" +
						"]}," +
						"{@type=korap:span, layer=cat, key=VP, match=match:eq}" +
					"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c},boundary={@type=korap:boundary,min=1}}}" +
					"";
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(cp4.replaceAll(" ", ""), map.replaceAll(" ", ""));		
	}
	
	/*		
	@Test
	public void testEqualNotequalValue() throws QueryException {
		query = "cat=\"NP\" & cat=\"VP\" & #1 == #2";
		String eq1 =
				"{}"; // ???
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(eq1.replaceAll(" ", ""), map.replaceAll(" ", ""));		
	}
	*/
	
}