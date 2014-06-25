import static org.junit.Assert.*;

import org.junit.Test;

import de.ids_mannheim.korap.query.serialize.PoliqarpPlusTree;
import de.ids_mannheim.korap.util.QueryException;

public class PoliqarpPlusTreeTest {
	
	PoliqarpPlusTree ppt;
	String map;

	
	private boolean equalsQueryContent(String res, String query) throws QueryException {
		res = res.replaceAll(" ", "");
		ppt = new PoliqarpPlusTree(query);
		String queryMap = ppt.getRequestMap().get("query").toString().replaceAll(" ", "");
		return res.equals(queryMap);
	}
	
	@Test
	public void testContext() throws QueryException {
		String contextString = "http://ids-mannheim.de/ns/KorAP/json-ld/v0.1/context.jsonld";
		ppt = new PoliqarpPlusTree("Test");
		assertEquals(contextString.replaceAll(" ", ""), ppt.getRequestMap().get("@context").toString().replaceAll(" ", ""));
	}
	
	@Test
	public void testSingleTokens() throws QueryException {
		// [base=Mann]
		String token1 = "{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=lemma, match=match:eq}}";
		assertTrue(equalsQueryContent(token1, "[base=Mann]"));
		
		// [orth!=Frau]
		String token2 = "{@type=korap:token, wrap={@type=korap:term, key=Frau, layer=orth, match=match:ne}}";
		assertTrue(equalsQueryContent(token2, "[orth!=Frau]"));
		
		// [!p=NN]
		String token3 = "{@type=korap:token, wrap={@type=korap:term, key=NN, layer=p, match=match:ne}}";
		assertTrue(equalsQueryContent(token3, "[!p=NN]"));
		
		// [!p!=NN]
		String token4 = "{@type=korap:token, wrap={@type=korap:term, key=NN, layer=p, match=match:eq}}";
		assertTrue(equalsQueryContent(token4, "[!p!=NN]"));
	}
	
	@Test
	public void testRegex() throws QueryException {
		String query = "[orth=\"M(a|ä)nn(er)?\"]";
		String re1 = "{@type=korap:token, wrap={@type=korap:term, type=type:regex, key=M(a|ä)nn(er)?, layer=orth, match=match:eq}}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(re1.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testCaseSensitivityFlag() throws QueryException {
		String query="[orth=deutscher/i]";
		String cs1 = 
				"{@type=korap:token, wrap={@type=korap:term, key=deutscher, layer=orth, match=match:eq, caseInsensitive=true}}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(cs1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="[orth=deutscher/i][orth=Bundestag]";
		String cs2 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=deutscher, layer=orth, match=match:eq, caseInsensitive=true}}," +
						"{@type=korap:token, wrap={@type=korap:term, key=Bundestag, layer=orth, match=match:eq}}" +
					"]}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(cs2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="deutscher/i";
		String cs3 = 
				"{@type=korap:token, wrap={@type=korap:term, key=deutscher, layer=orth, match=match:eq, caseInsensitive=true}}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(cs3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="deutscher/i Bundestag";
		String cs4 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=deutscher, layer=orth, match=match:eq, caseInsensitive=true}}," +
						"{@type=korap:token, wrap={@type=korap:term, key=Bundestag, layer=orth, match=match:eq}}" +
					"]}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(cs4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="deutscher Bundestag/i";
		String cs5 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=deutscher, layer=orth, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, key=Bundestag, layer=orth, match=match:eq, caseInsensitive=true}}" +
					"]}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(cs5.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testElements() throws QueryException {
		String query;
		// <s>
		String elem1 = "{@type=korap:span, key=s}";
		assertTrue(equalsQueryContent(elem1, "<s>"));
		
		// <vp>
		String elem2 = "{@type=korap:span, key=vp}";
		assertTrue(equalsQueryContent(elem2, "<vp>"));
		
		// <cnx/c=vp>
		query = "<c=vp>";
		String span3 = "{@type=korap:span, layer=c, key=vp}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(span3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// <cnx/c=vp>
		query = "<cnx/c=vp>";
		String span4 = "{@type=korap:span, foundry=cnx, layer=c, key=vp}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(span4.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testEmptyTokens() throws QueryException {
		// [base=der][][base=Mann]
		String et1 = 
			"{@type=korap:group, operation=operation:sequence, inOrder=true, distances=[" +
				"{@type=korap:distance, key=w, min=2, max=2}" +
			"], " +
			"operands=[" +
				"{@type=korap:token, wrap={@type=korap:term, key=der, layer=lemma, match=match:eq}}," +
				"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=lemma, match=match:eq}}" +
			"]}";
		ppt = new PoliqarpPlusTree("[base=der][][base=Mann]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(et1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=der][][][base=Mann]
		String et2 = 
			"{@type=korap:group, operation=operation:sequence, inOrder=true, distances=[" +
				"{@type=korap:distance, key=w, min=3, max=3}" +
			"], " +
			"operands=[" +
				"{@type=korap:token, wrap={@type=korap:term, key=der, layer=lemma, match=match:eq}}," +
				"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=lemma, match=match:eq}}" +
			"]}";
		ppt = new PoliqarpPlusTree("[base=der][][][base=Mann]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(et2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=der][][]?[base=Mann]
		String et3 = 
			"{@type=korap:group, operation=operation:sequence, inOrder=true, distances=[" +
				"{@type=korap:distance, key=w, min=2, max=3}" +
			"], " +
			"operands=[" +
				"{@type=korap:token, wrap={@type=korap:term, key=der, layer=lemma, match=match:eq}}," +
				"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=lemma, match=match:eq}}" +
			"]}";
		ppt = new PoliqarpPlusTree("[base=der][][]?[base=Mann]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(et3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		
		// startswith(<s>, [][base=Mann]
		String et4 = 
			"{@type=korap:group, operation=operation:position, frame=frame:startswith, operands=[" +	
				"{@type=korap:span, key=s}," +
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=lemma, match=match:eq}}" +
				"], offset-min=1, offset-max=1}" +
			"]}";
		ppt = new PoliqarpPlusTree("startswith(<s>, [][base=Mann])");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(et4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=der][]{2,5}[base=Mann][]?[][base=Frau]   nested distances=
		String et5 = 
				"{@type=korap:group, operation=operation:sequence, inOrder=true, distances=[" +
					"{@type=korap:distance, key=w, min=3, max=6}" +
				"], " +
				"operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, key=der, layer=lemma, match=match:eq}}," +
					"{@type=korap:group, operation=operation:sequence, inOrder=true, distances=[" +
						"{@type=korap:distance, key=w, min=2, max=3}" +
					"], " +
					"operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=lemma, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, key=Frau, layer=lemma, match=match:eq}}" +
					"]}" +
				"]}";
		ppt = new PoliqarpPlusTree("[base=der][]{2,5}[base=Mann][]?[][base=Frau]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(et5.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=der][]*[base=Mann]
		String et6 = 
			"{@type=korap:group, operation=operation:sequence, inOrder=true, distances=[" +
				"{@type=korap:distance, key=w, min=1, max=100}" +
			"], " +
			"operands=[" +
				"{@type=korap:token, wrap={@type=korap:term, key=der, layer=lemma, match=match:eq}}," +
				"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=lemma, match=match:eq}}" +
			"]}";
		ppt = new PoliqarpPlusTree("[base=der][]*[base=Mann]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(et6.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=der][]*[base=Mann]
		String et7 = 
			"{@type=korap:group, operation=operation:sequence, inOrder=true, distances=[" +
				"{@type=korap:distance, key=w, min=2, max=100}" +
			"], " +
			"operands=[" +
				"{@type=korap:token, wrap={@type=korap:term, key=der, layer=lemma, match=match:eq}}," +
				"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=lemma, match=match:eq}}" +
			"]}";
		ppt = new PoliqarpPlusTree("[base=der][]+[base=Mann]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(et7.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}

	@Test
	public void testCoordinatedFields() throws QueryException {
		// [base=Mann&(cas=N|cas=A)]
		String cof1 = 
			"{@type=korap:token, wrap=" +
				"{@type=korap:termGroup, relation=relation:and, operands=[" +
					"{@type=korap:term, key=Mann, layer=lemma, match=match:eq}," +
					"{@type=korap:termGroup, relation=relation:or, operands=[" +
						"{@type=korap:term, key=N, layer=cas, match=match:eq}," +
						"{@type=korap:term, key=A, layer=cas, match=match:eq}" +
					"]}" +
				"]}" +
			"}";
		ppt = new PoliqarpPlusTree("[base=Mann&(cas=N|cas=A)]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(cof1.replaceAll(" ", ""), map.replaceAll(" ", ""));


		assertEquals(
		    new PoliqarpPlusTree(" [ base=Mann & ( cas=N | cas=A)] ").getRequestMap().get("query").toString(),
		    new PoliqarpPlusTree("[base=Mann &(cas=N|cas=A)]").getRequestMap().get("query").toString()
	        );
		
		// [base=Mann&cas=N&gen=m]
		String cof2 = 
			"{@type=korap:token, wrap=" +
				"{@type=korap:termGroup, relation=relation:and, operands=[" +
					"{@type=korap:term, key=Mann, layer=lemma, match=match:eq}," +
					"{@type=korap:term, key=N, layer=cas, match=match:eq}," +
					"{@type=korap:term, key=m, layer=gen, match=match:eq}" +
				"]}" +
			"}";
		ppt = new PoliqarpPlusTree("[base=Mann&cas=N&gen=m]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(cof2.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOccurrence() throws QueryException {
		// [base=foo]*
		String occ1 = "{@type=korap:group, operation=operation:repetition, operands=[" +
					     "{@type=korap:token, wrap={@type=korap:term, key=foo, layer=lemma, match=match:eq}}" +
					  "], min=0, max=100}"; 
		ppt = new PoliqarpPlusTree("[base=foo]*");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=foo]*[base=bar]
		String occ2 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:group, operation=operation:repetition, operands=[" +
					     "{@type=korap:token, wrap={@type=korap:term, key=foo, layer=lemma, match=match:eq}}" +
					"], min=0, max=100 }," +
					"{@type=korap:token, wrap={@type=korap:term, key=bar, layer=lemma, match=match:eq}}" +
				"]}"; 
		ppt = new PoliqarpPlusTree("[base=foo]*[base=bar]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=bar][base=foo]*
		String occ3 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, key=bar, layer=lemma, match=match:eq}}," +
					"{@type=korap:group, operation=operation:repetition, operands=[" +
					     "{@type=korap:token, wrap={@type=korap:term, key=foo, layer=lemma, match=match:eq}}" +
					"], min=0, max=100 }" +
				"]}"; 
		ppt = new PoliqarpPlusTree("[base=bar][base=foo]*");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// ([base=bar][base=foo])*
		String occ4 = 
				"{@type=korap:group, operands=[" +	
					"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=bar, layer=lemma, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, key=foo, layer=lemma, match=match:eq}}" +
					"]}" +
				"], operation=operation:repetition, min=0, max=100 }" ;
		ppt = new PoliqarpPlusTree("([base=bar][base=foo])*");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// <s>([base=bar][base=foo])*
		String occ5 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:span, key=s}," +
					"{@type=korap:group, operands=[" +	
						"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, key=bar, layer=lemma, match=match:eq}}," +
							"{@type=korap:token, wrap={@type=korap:term, key=foo, layer=lemma, match=match:eq}}" +
						"]}" +
					"], operation=operation:repetition, min=0, max=100 }" +
				"]}" ;
		ppt = new PoliqarpPlusTree("<s>([base=bar][base=foo])*");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ5.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// <s><np>([base=bar][base=foo])*
		String occ6 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:span, key=s}," +
					"{@type=korap:span, key=np}," +
					"{@type=korap:group, operands=[" +	
						"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, key=bar, layer=lemma, match=match:eq}}," +
							"{@type=korap:token, wrap={@type=korap:term, key=foo, layer=lemma, match=match:eq}}" +
						"]}" +
					"], operation=operation:repetition, min=0, max=100 }" +
				"]}" ;
		ppt = new PoliqarpPlusTree("<s><np>([base=bar][base=foo])*");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ6.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// <s><np>([base=bar][base=foo])*[p=NN]
		// comment: embedded sequence shouldn't really be here, but does not really hurt, either. (?)
		// really hard to get this behaviour out of the PQPlus grammar...
		String occ7 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:span, key=s}," +
					"{@type=korap:span, key=np}," +
					"{@type=korap:group, operands=[" +	
						"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, key=bar, layer=lemma, match=match:eq}}," +
							"{@type=korap:token, wrap={@type=korap:term, key=foo, layer=lemma, match=match:eq}}" +
						"]}" +
					"], operation=operation:repetition, min=0, max=100 }," +
					"{@type=korap:token, wrap={@type=korap:term, key=NN, layer=p, match=match:eq}}" +
				"]}" ;
		ppt = new PoliqarpPlusTree("<s><np>([base=bar][base=foo])*[p=NN]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ7.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// ([base=bar][base=foo])*[p=NN]
		String occ8 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:group, operands=[" +	
						"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, key=bar, layer=lemma, match=match:eq}}," +
							"{@type=korap:token, wrap={@type=korap:term, key=foo, layer=lemma, match=match:eq}}" +
						"]}" +
					"], operation=operation:repetition, min=0, max=100 }," +
					"{@type=korap:token, wrap={@type=korap:term, key=NN, layer=p, match=match:eq}}" +
				"]}" ;
		ppt = new PoliqarpPlusTree("([base=bar][base=foo])*[p=NN]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ8.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=foo]+
		String occ9 = "{@type=korap:group, operation=operation:repetition, operands=[" +
					     "{@type=korap:token, wrap={@type=korap:term, key=foo, layer=lemma, match=match:eq}}" +
					  "], min=1, max=100}"; 
		ppt = new PoliqarpPlusTree("[base=foo]+");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ9.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=foo]?
		String occ10 = "{@type=korap:group, operation=operation:repetition, operands=[" +
					     "{@type=korap:token, wrap={@type=korap:term, key=foo, layer=lemma, match=match:eq}}" +
					  "], min=0, max=1}"; 
		ppt = new PoliqarpPlusTree("[base=foo]?");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ10.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=foo]{2,5}
		String occ11 = "{@type=korap:group, operation=operation:repetition, operands=[" +
					     "{@type=korap:token, wrap={@type=korap:term, key=foo, layer=lemma, match=match:eq}}" +
					  "], min=2, max=5}"; 
		ppt = new PoliqarpPlusTree("[base=foo]{2,5}");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ11.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=foo]{2}
		String occ12 = "{@type=korap:group, operation=operation:repetition, operands=[" +
					     "{@type=korap:token, wrap={@type=korap:term, key=foo, layer=lemma, match=match:eq}}" +
					  "], min=2, max=2}"; 
		ppt = new PoliqarpPlusTree("[base=foo]{2}");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ12.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testTokenSequence() throws QueryException {
		// [base=Mann][orth=Frau]
		String seq1 = "{@type=korap:group, operation=operation:sequence, operands=[" +
				"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=lemma, match=match:eq}}, " +
				"{@type=korap:token, wrap={@type=korap:term, key=Frau, layer=orth, match=match:eq}}" +
				"]}";
		assertTrue(equalsQueryContent(seq1, "[base=Mann][orth=Frau]"));
		
		// [base=Mann][orth=Frau][p=NN]
		String seq2 = "{@type=korap:group, operation=operation:sequence, operands=[" +
				"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=lemma, match=match:eq}}, " +
				"{@type=korap:token, wrap={@type=korap:term, key=Frau, layer=orth, match=match:eq}}, " +
				"{@type=korap:token, wrap={@type=korap:term, key=NN,layer=p, match=match:eq}}" +
				"]}";
		assertTrue(equalsQueryContent(seq2, "[base=Mann][orth=Frau][p=NN]"));
	}
	
	@Test
	public void testDisjSegments() throws QueryException {
		// ([base=der]|[base=das])[base=Schild]
		String disj1 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:group, operation=operation:or, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=der, layer=lemma, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, key=das, layer=lemma, match=match:eq}}" +
					"]}," +
					"{@type=korap:token, wrap={@type=korap:term, key=Schild, layer=lemma, match=match:eq}}" +
				"]}";
		ppt = new PoliqarpPlusTree("([base=der]|[base=das])[base=Schild]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(disj1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=Schild]([base=der]|[base=das])
		String disj2 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, key=Schild, layer=lemma, match=match:eq}}," +
					"{@type=korap:group, operation=operation:or, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=der, layer=lemma, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, key=das, layer=lemma, match=match:eq}}" +
					"]}" +
				"]}";
		ppt = new PoliqarpPlusTree("[base=Schild]([base=der]|[base=das])");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(disj2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// "([orth=der][base=katze])|([orth=eine][base=baum])"
		String disj3 = 
				"{@type=korap:group, operation=operation:or, operands=[" +
					"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=der, layer=orth, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, key=katze, layer=lemma, match=match:eq}}" +
					"]}," +
					"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=eine, layer=orth, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, key=baum, layer=lemma, match=match:eq}}" +
					"]}" +
				"]}";
		ppt = new PoliqarpPlusTree("([orth=der][base=katze])|([orth=eine][base=baum])");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(disj3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// "[orth=der][base=katze]|[orth=eine][base=baum]"
		String disj4 = 
				"{@type=korap:group, operation=operation:or, operands=[" +
					"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=der, layer=orth, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, key=katze, layer=lemma, match=match:eq}}" +
					"]}," +
					"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=eine, layer=orth, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, key=baum, layer=lemma, match=match:eq}}" +
					"]}" +
				"]}";
		ppt = new PoliqarpPlusTree("[orth=der][base=katze]|[orth=eine][base=baum]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(disj4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		PoliqarpPlusTree ppt1 = new PoliqarpPlusTree("[orth=der][base=katze]|[orth=eine][base=baum]");
		PoliqarpPlusTree ppt2 = new PoliqarpPlusTree("([orth=der][base=katze])|([orth=eine][base=baum])");
		assertEquals(ppt1.getRequestMap().toString(), ppt2.getRequestMap().toString());
		
		// "[orth=der][base=katze]|[orth=der][base=hund]|[orth=der][base=baum]"
		String disj5 = 
				"{@type=korap:group, operation=operation:or, operands=[" +
					"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=der, layer=orth, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, key=katze, layer=lemma, match=match:eq}}" +
					"]}," +
					"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=der, layer=orth, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, key=hund, layer=lemma, match=match:eq}}" +
					"]}," +
					"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=der, layer=orth, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, key=baum, layer=lemma, match=match:eq}}" +
					"]}" +
				"]}";
		ppt = new PoliqarpPlusTree("[orth=der][base=katze]|[orth=der][base=hund]|[orth=der][base=baum]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(disj5.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [orth=der]([base=katze]|[base=hund]|[base=baum])
		String disj6 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, key=der, layer=orth, match=match:eq}}," +
					"{@type=korap:group, operation=operation:or, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=katze, layer=lemma, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, key=hund, layer=lemma, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, key=baum, layer=lemma, match=match:eq}}" +
					"]}" +
				"]}";
		ppt = new PoliqarpPlusTree("[orth=der]([base=katze]|[base=hund]|[base=baum])");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(disj6.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testTokenElemSequence() throws QueryException {
		// [base=Mann]<vp>
		String seq1 = "{@type=korap:group, operation=operation:sequence, operands=[" +
				"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=lemma, match=match:eq}}, " +
				"{@type=korap:span, key=vp}" +
				"]}";
		assertTrue(equalsQueryContent(seq1, "[base=Mann]<vp>"));
		
		// <vp>[base=Mann]
		String seq2 = "{@type=korap:group, operation=operation:sequence, operands=[" +
				"{@type=korap:span, key=vp}, "+
				"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=lemma, match=match:eq}} " +
				"]}";
		assertTrue(equalsQueryContent(seq2, "<vp>[base=Mann]"));
		
		// <vp>[base=Mann]<pp>
		String seq3 = "{@type=korap:group, operation=operation:sequence, operands=[" +
				"{@type=korap:span, key=vp}, "+
				"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=lemma, match=match:eq}}, " +
				"{@type=korap:span, key=pp} "+
				"]}";
		assertTrue(equalsQueryContent(seq3, "<vp>[base=Mann]<pp>"));
	}
	
	@Test
	public void testElemSequence() throws QueryException {
		// <np><vp>
		String seq1 = "{@type=korap:group, operation=operation:sequence, operands=[" +
				"{@type=korap:span, key=np}," +
				"{@type=korap:span, key=vp}" +
				"]}";
		assertTrue(equalsQueryContent(seq1, "<np><vp>"));
		
		// <np><vp><pp>
		String seq2 = "{@type=korap:group, operation=operation:sequence, operands=[" +
				"{@type=korap:span, key=np}," +
				"{@type=korap:span, key=vp}," +
				"{@type=korap:span, key=pp}" +
				"]}";
		assertTrue(equalsQueryContent(seq2, "<np><vp><pp>"));
	}
	
	@Test 
	public void testClasses() throws QueryException {
		// {[base=Mann]}
		String cls1 = "{@type=korap:group, operation=operation:class, class=0, operands=[" +
				"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=lemma, match=match:eq}}" +
				"]}";
		ppt = new PoliqarpPlusTree("{[base=Mann]}");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(cls1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// {[base=Mann][orth=Frau]}
		String cls2 = "{@type=korap:group, operation=operation:class, class=0, operands=[" +
				 "{@type=korap:group, operation=operation:sequence, operands=[" +
				  "{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=lemma, match=match:eq}}," +
				  "{@type=korap:token, wrap={@type=korap:term, key=Frau, layer=orth, match=match:eq}}" +
				 "]}" +
				"]}";
		assertTrue(equalsQueryContent(cls2, "{[base=Mann][orth=Frau]}"));
		
		// [p=NN]{[base=Mann][orth=Frau]}
		String cls3 = "{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=NN, layer=p, match=match:eq}}," +
						"{@type=korap:group, operation=operation:class, class=0, operands=[" +
							"{@type=korap:group, operation=operation:sequence, operands=[" +
								"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=lemma, match=match:eq}}," +
								"{@type=korap:token, wrap={@type=korap:term, key=Frau, layer=orth, match=match:eq}}" +
							"]}" +
						"]}" +
					  "]}";
		ppt = new PoliqarpPlusTree("[p=NN]{[base=Mann][orth=Frau]}");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(cls3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// {[base=Mann][orth=Frau]}[p=NN]
		String cls4 = "{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:group, operation=operation:class, class=0, operands=[" +
						   "{@type=korap:group, operation=operation:sequence, operands=[" +
						     "{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=lemma, match=match:eq}}," +
						     "{@type=korap:token, wrap={@type=korap:term, key=Frau, layer=orth, match=match:eq}}" +
						   "]}" +
						"]}," +
						"{@type=korap:token, wrap={@type=korap:term, key=NN, layer=p, match=match:eq}}" +
					  "]}";
		ppt = new PoliqarpPlusTree("{[base=Mann][orth=Frau]}[p=NN]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(cls4.replaceAll(" ", ""), map.replaceAll(" ", ""));

		// {2:{1:[tt/p=ADJA]}[mate/p=NN]}"
		String cls5 = "{@type=korap:group, operation=operation:class, class=2, operands=[" +
						"{@type=korap:group, operation=operation:sequence, operands=[" +
						   "{@type=korap:group, operation=operation:class, class=1, operands=[" +
						     "{@type=korap:token, wrap={@type=korap:term, key=ADJA, layer=p, foundry=tt, match=match:eq}}" +
						   "]}," +
						   "{@type=korap:token, wrap={@type=korap:term, key=NN, layer=p, foundry=mate, match=match:eq}}" + 
						"]}" +
					  "]}";
		ppt = new PoliqarpPlusTree("{2: {1:[tt/p=ADJA]}[mate/p=NN]}");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(cls5.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testPositions() throws QueryException {
		// contains(<s>,<np>)
		String pos1 = "{@type=korap:group, operation=operation:position, frame=frame:contains, operands=[" +
				  "{@type=korap:span, key=s}," +
				  "{@type=korap:span, key=np}" +
				"]}";
		assertTrue(equalsQueryContent(pos1, "contains(<s>,<np>)"));
		
		// contains(<s>,[base=Mann])
		String pos2 = "{@type=korap:group, operation=operation:position, frame=frame:contains, operands=[" +
				  "{@type=korap:span, key=s}," +
				  "{@type=korap:token, wrap= {@type=korap:term, key=Mann, layer=lemma, match=match:eq}}" +
				"]}";
		assertTrue(equalsQueryContent(pos2, "contains(<s>,[base=Mann])"));
		
		// contains(<s>,[orth=der][orth=Mann])
		String pos3 = "{@type=korap:group, operation=operation:position, frame=frame:contains, operands=[" +
				  	"{@type=korap:span, key=s}," +
				  	"{@type=korap:group, operation=operation:sequence, operands=[" +
				  		"{@type=korap:token, wrap={@type=korap:term, key=der, layer=orth, match=match:eq}}," +
				  		"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}" +
				  	"]}" +
				  "]}";
		ppt = new PoliqarpPlusTree("contains(<s>,[orth=der][orth=Mann])");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(pos3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=Auto]contains(<s>,[base=Mann])
		String pos4 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, key=Auto, layer=lemma, match=match:eq}}," +
					"{@type=korap:group, operation=operation:position, frame=frame:contains, operands=[" +
				  		"{@type=korap:span, key=s}," +
				  		"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=lemma, match=match:eq}}" +
				  	"]}" +
				"]}";
		ppt = new PoliqarpPlusTree("[base=Auto]contains(<s>,[base=Mann])");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(pos4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// contains(<s>,[pos=N]*)
		String pos5 = 
					"{@type=korap:group, operation=operation:position, frame=frame:contains, operands=[" +
				  		"{@type=korap:span, key=s}," +
				  		"{@type=korap:group, operation=operation:repetition, " +
				  			"operands=[{@type=korap:token, wrap={@type=korap:term, key=N, layer=pos, match=match:eq}}" +
				  			"], min=0, max=100" +
				  		"}" +
				  	"]}";
		ppt = new PoliqarpPlusTree("contains(<s>,[pos=N]*)");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(pos5.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=Auto]contains(<s>,[pos=N]*)
		String pos6 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, key=Auto, layer=lemma, match=match:eq}}," +
					"{@type=korap:group, operation=operation:position, frame=frame:contains, operands=[" +
				  		"{@type=korap:span, key=s}," +
				  		"{@type=korap:group, operation=operation:repetition, " +
				  			"operands=[{@type=korap:token, wrap={@type=korap:term, key=N, layer=pos, match=match:eq}}" +
				  			"], min=0, max=100" +
				  		"}" +
				  	"]}" +
				"]}";
		ppt = new PoliqarpPlusTree("[base=Auto]contains(<s>,[pos=N]*)");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(pos6.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testNestedPositions() throws QueryException {
		// contains(<s>,startswith(<np>,[orth=Der]))
		String npos1 = 
			"{@type=korap:group, operation=operation:position, frame=frame:contains, operands=[" +
				"{@type=korap:span, key=s}," +
				"{@type=korap:group, operation=operation:position, frame=frame:startswith, operands=[" +
					"{@type=korap:span, key=np}," +
					"{@type=korap:token, wrap={@type=korap:term, key=Der, layer=orth, match=match:eq}}" +
				"]}" +
			"]}";
		ppt = new PoliqarpPlusTree("contains(<s>, startswith(<np>,[orth=Der]))");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(npos1.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testShrinkSplit() throws QueryException {
		// shrink([orth=Der]{[orth=Mann]})
		String shr1 = 
			"{@type=korap:reference, classRef=[0], operands=[" +
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, key=Der, layer=orth, match=match:eq}}," +
					"{@type=korap:group, operation=operation:class, class=0, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}" +
					"]}" +
				"]}" +
			"]}";
		ppt = new PoliqarpPlusTree("focus([orth=Der]{[orth=Mann]})");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(shr1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// shrink([orth=Der]{[orth=Mann][orth=geht]})
		String shr2 = 
			"{@type=korap:reference, classRef=[0], operands=[" +
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, key=Der, layer=orth, match=match:eq}}," +
					"{@type=korap:group, operation=operation:class, class=0, operands=[" +
						"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}," +
							"{@type=korap:token, wrap={@type=korap:term, key=geht, layer=orth, match=match:eq}}" +
						"]}" +
					"]}" +
				"]}" +
			"]}";
		ppt = new PoliqarpPlusTree("focus([orth=Der]{[orth=Mann][orth=geht]})");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(shr2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// shrink(1:[orth=Der]{1:[orth=Mann][orth=geht]})
		String shr3 = 
			"{@type=korap:reference, classRef=[1], operands=[" +
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, key=Der, layer=orth, match=match:eq}}," +
					"{@type=korap:group, operation=operation:class, class=1, operands=[" +
						"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}," +
							"{@type=korap:token, wrap={@type=korap:term, key=geht, layer=orth, match=match:eq}}" +
						"]}" +
					"]}" +
				"]}" +
			"]}";
		ppt = new PoliqarpPlusTree("focus(1:[orth=Der]{1:[orth=Mann][orth=geht]})");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(shr3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// shrink(1:startswith(<s>,{1:<np>}))
		String shr4 = 
			"{@type=korap:reference, classRef=[1], operands=[" +
				"{@type=korap:group, operation=operation:position, frame=frame:startswith, operands=[" +
					"{@type=korap:span, key=s}," +
					"{@type=korap:group, operation=operation:class, class=1, operands=[" +
						"{@type=korap:span, key=np}" +
					"]}" +
				"]}" +
			"]}";
		ppt = new PoliqarpPlusTree("focus(1:startswith(<s>,{1:<np>}))");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(shr4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// shrink(3: startswith(<s>, {3:[base=der]{1:[mate/p=ADJA]{2:[tt/p=NN]}}})) 
		String shr5 = 
			"{@type=korap:reference, classRef=[3], operands=[" +
				"{@type=korap:group, operation=operation:position, frame=frame:startswith, operands=[" +
					"{@type=korap:span, key=s}," +
					"{@type=korap:group, operation=operation:class, class=3, operands=[" +
						"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, key=der, layer=lemma, match=match:eq}}," +
							"{@type=korap:group, operation=operation:class, class=1, operands=[" +
								"{@type=korap:group, operation=operation:sequence, operands=[" +
									"{@type=korap:token, wrap={@type=korap:term, key=ADJA, layer=p, foundry=mate, match=match:eq}}," +
									"{@type=korap:group, operation=operation:class, class=2, operands=[" +
										"{@type=korap:token, wrap={@type=korap:term, key=NN, layer=p, foundry=tt, match=match:eq}}" +
									"]}" + 
								"]}" +
							"]}" +
						"]}" +
					"]}" +
				"]}" +
			"]}";
		ppt = new PoliqarpPlusTree("focus(3:startswith(<s>,{3:[base=der]{1:[mate/p=ADJA]{2:[tt/p=NN]}}})) ");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(shr5.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// split(3: startswith(<s>, {3:[base=der]{1:[mate/p=ADJA]{2:[tt/p=NN]}}})) 
		String shr6 = 
			"{@type=korap:reference, classRef=[3], operation=operation:split, operands=[" +
				"{@type=korap:group, operation=operation:position, frame=frame:startswith, operands=[" +
					"{@type=korap:span, key=s}," +
					"{@type=korap:group, operation=operation:class, class=3, operands=[" +
						"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, key=der, layer=lemma, match=match:eq}}," +
							"{@type=korap:group, operation=operation:class, class=1, operands=[" +
								"{@type=korap:group, operation=operation:sequence, operands=[" +
									"{@type=korap:token, wrap={@type=korap:term, key=ADJA, layer=p, foundry=mate, match=match:eq}}," +
									"{@type=korap:group, operation=operation:class, class=2, operands=[" +
										"{@type=korap:token, wrap={@type=korap:term, key=NN, layer=p, foundry=tt, match=match:eq}}" +
									"]}" + 
								"]}" +
							"]}" +
						"]}" +
					"]}" +
				"]}" +
			"]}";
		ppt = new PoliqarpPlusTree("split(3:startswith(<s>,{3:[base=der]{1:[mate/p=ADJA]{2:[tt/p=NN]}}})) ");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(shr6.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// split(2|3: startswith(<s>, {3:[base=der]{1:[mate/p=ADJA]{2:[tt/p=NN]}}})) 
		String shr7 = 
			"{@type=korap:reference, classRef=[2, 3], operation=operation:split, classRefOp=classRefOp:intersection, operands=[" +
				"{@type=korap:group, operation=operation:position, frame=frame:startswith, operands=[" +
					"{@type=korap:span, key=s}," +
					"{@type=korap:group, operation=operation:class, class=3, operands=[" +
						"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, key=der, layer=lemma, match=match:eq}}," +
							"{@type=korap:group, operation=operation:class, class=1, operands=[" +
								"{@type=korap:group, operation=operation:sequence, operands=[" +
									"{@type=korap:token, wrap={@type=korap:term, key=ADJA, layer=p, foundry=mate, match=match:eq}}," +
									"{@type=korap:group, operation=operation:class, class=2, operands=[" +
										"{@type=korap:token, wrap={@type=korap:term, key=NN, layer=p, foundry=tt, match=match:eq}}" +
									"]}" + 
								"]}" +
							"]}" +
						"]}" +
					"]}" +
				"]}" +
			"]}";
		ppt = new PoliqarpPlusTree("split(2|3:startswith(<s>,{3:[base=der]{1:[mate/p=ADJA]{2:[tt/p=NN]}}})) ");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(shr7.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		
		String shr8 = 
			"{@type=korap:reference, classRef=[1], operands=[" +
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:group, operation=operation:class, class=0, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=der, layer=lemma, match=match:eq}}" +
					"]}," +
					"{@type=korap:group, operation=operation:class, class=1, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=ADJA, layer=pos, match=match:eq}}" +
					"]}" +
				"]}" +
			"]}";
		ppt = new PoliqarpPlusTree("focus(1:{[base=der]}{1:[pos=ADJA]})");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(shr8.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
	}
	
	
	@Test
	public void testFoundries() throws QueryException {
		// [tt/base=Mann]
		String layer1 = "{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=lemma, foundry=tt, match=match:eq}}";
		ppt = new PoliqarpPlusTree("[tt/base=Mann]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(layer1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
	}
	
	@Test
	public void testAlign() throws QueryException {
		// [orth=der]^[orth=Mann]
		String align1 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, key=der, layer=orth, match=match:eq}}," +
					"{@type=korap:group, align=left, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}" +
					"]}" +
				"]}";
		ppt = new PoliqarpPlusTree("[orth=der]^[orth=Mann]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(align1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [orth=der]^[orth=große][orth=Mann]
		String query = "[orth=der]^[orth=große][orth=Mann]";
		String align1b = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, key=der, layer=orth, match=match:eq}}," +
					"{@type=korap:group, align=left, operands=[" +
						"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, key=große, layer=orth, match=match:eq}}," +
							"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}" +
						"]}" +
					"]}" +
				"]}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(align1b.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// "([base=a]^[base=b])|[base=c]",
		String align2 = 
				"{@type=korap:group, operation=operation:or, operands=[" +
						"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, key=a, layer=lemma, match=match:eq}}," +
							"{@type=korap:group, align=left, operands=[" +
								"{@type=korap:token, wrap={@type=korap:term, key=b, layer=lemma, match=match:eq}}" +
							"]}" +
						"]}," +
						"{@type=korap:token, wrap={@type=korap:term, key=c, layer=lemma, match=match:eq}}" +
				"]}";
		ppt = new PoliqarpPlusTree("([base=a]^[base=b])|[base=c]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(align2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// "([base=a]^[base=b][base=c])|[base=d]",
		String align3 = 
				"{@type=korap:group, operation=operation:or, operands=[" +
						"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, key=a, layer=lemma, match=match:eq}}," +
							"{@type=korap:group, align=left, operands=[" +
								"{@type=korap:group, operation=operation:sequence, operands=[" +
									"{@type=korap:token, wrap={@type=korap:term, key=b, layer=lemma, match=match:eq}}," +
									"{@type=korap:token, wrap={@type=korap:term, key=c, layer=lemma, match=match:eq}}" +
								"]}" +
							"]}" +
						"]}," +
						"{@type=korap:token, wrap={@type=korap:term, key=d, layer=lemma, match=match:eq}}" +
				"]}";
		ppt = new PoliqarpPlusTree("([base=a]^[base=b][base=c])|[base=d]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(align3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// "([base=a]^[base=b]^[base=c])|[base=d]",
		String align4 = 
				"{@type=korap:group, operation=operation:or, operands=[" +
						"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, key=a, layer=lemma, match=match:eq}}," +
							"{@type=korap:group, align=left, operands=[" +
								"{@type=korap:group, operation=operation:sequence, operands=[" +
									"{@type=korap:token, wrap={@type=korap:term, key=b, layer=lemma, match=match:eq}}," +
									"{@type=korap:group, align=left, operands=[" +
										"{@type=korap:token, wrap={@type=korap:term, key=c, layer=lemma, match=match:eq}}" +
									"]}" +
								"]}" +
							"]}" +
						"]}," +
						"{@type=korap:token, wrap={@type=korap:term, key=d, layer=lemma, match=match:eq}}" +
				"]}";
		ppt = new PoliqarpPlusTree("([base=a]^[base=b]^[base=c])|[base=d]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(align4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		
	}
	
	@Test
	public void testSimpleQueries() throws QueryException {
		// Baum
		String simple1 = 
				"{@type=korap:token, wrap={@type=korap:term, key=Baum, layer=orth, match=match:eq}}";
		ppt = new PoliqarpPlusTree("Baum");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(simple1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// Der Baum
		String simple2 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, key=Der, layer=orth, match=match:eq}}, " +
					"{@type=korap:token, wrap={@type=korap:term, key=Baum, layer=orth, match=match:eq}}" +
				"]}";
		ppt = new PoliqarpPlusTree("Der Baum");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(simple2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// Der große Baum
		String simple3 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, key=Der, layer=orth, match=match:eq}}, " +
					"{@type=korap:token, wrap={@type=korap:term, key=große, layer=orth, match=match:eq}}, " +						
					"{@type=korap:token, wrap={@type=korap:term, key=Baum, layer=orth, match=match:eq}}" +
				"]}";
		ppt = new PoliqarpPlusTree("Der große Baum");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(simple3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// Baum | Stein
		String simple4 = 
				"{@type=korap:group, operation=operation:or, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, key=Baum, layer=orth, match=match:eq}}, " +						
					"{@type=korap:token, wrap={@type=korap:term, key=Stein, layer=orth, match=match:eq}}" +
				"]}";
		ppt = new PoliqarpPlusTree("Baum | Stein");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(simple4.replaceAll(" ", ""), map.replaceAll(" ", ""));		
		
		// Baum | Stein Haus
		String query = "(Baum | Stein) Haus";
		String simple5 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:group, operation=operation:or, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=Baum, layer=orth, match=match:eq}}, " +						
						"{@type=korap:token, wrap={@type=korap:term, key=Stein, layer=orth, match=match:eq}}" +
					"]}," +
					"{@type=korap:token, wrap={@type=korap:term, key=Haus, layer=orth, match=match:eq}} " +			
				"]}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(simple5.replaceAll(" ", ""), map.replaceAll(" ", ""));		
	}
}

