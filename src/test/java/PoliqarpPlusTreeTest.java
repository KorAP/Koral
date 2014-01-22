import static org.junit.Assert.*;

import org.junit.Test;

import de.ids_mannheim.korap.query.serialize.PoliqarpPlusTree;
import de.ids_mannheim.korap.util.QueryException;

public class PoliqarpPlusTreeTest {
	
	PoliqarpPlusTree ppt;
	String map;

	private boolean equalsContent(String str, Object map) {
		str = str.replaceAll(" ", "");
		String mapStr = map.toString().replaceAll(" ", "");
		return str.equals(mapStr);
	}
	
	private boolean equalsQueryContent(String res, String query) throws QueryException {
		res = res.replaceAll(" ", "");
		ppt = new PoliqarpPlusTree(query);
		String queryMap = ppt.getRequestMap().get("query").toString().replaceAll(" ", "");
		return res.equals(queryMap);
	}
	
	@Test
	public void testContext() throws QueryException {
		String contextString = "{korap=http://korap.ids-mannheim.de/ns/query, @language=de, operands={@id=korap:operands, @container=@list}, relation={@id=korap:relation, @type=korap:relation#types}, class={@id=korap:class, @type=xsd:integer}, query=korap:query, filter=korap:filter, meta=korap:meta}";
		ppt = new PoliqarpPlusTree("[base=test]");
		assertTrue(equalsContent(contextString, ppt.getRequestMap().get("@context")));
	}
	
	@Test
	public void testSingleTokens() throws QueryException {
		// [base=Mann]
		String token1 = "{@type=korap:token, @value={@type=korap:term, @value=base:Mann, relation==}}";
		assertTrue(equalsQueryContent(token1, "[base=Mann]"));
		
		// [orth!=Frau]
		String token2 = "{@type=korap:token, @value={@type=korap:term, @value=orth:Frau, relation=!=}}";
		assertTrue(equalsQueryContent(token2, "[orth!=Frau]"));
		
		// [!p=NN]
		String token3 = "{@type=korap:token, @value={@type=korap:term, @value=p:NN, relation=!=}}";
		assertTrue(equalsQueryContent(token3, "[!p=NN]"));
		
		// [!p!=NN]
		String token4 = "{@type=korap:token, @value={@type=korap:term, @value=p:NN, relation==}}";
		assertTrue(equalsQueryContent(token4, "[!p!=NN]"));
	}
	
	@Test
	public void testElements() throws QueryException {
		// <s>
		String elem1 = "{@type=korap:element, @value=s}";
		assertTrue(equalsQueryContent(elem1, "<s>"));
		
		// <vp>
		String elem2 = "{@type=korap:element, @value=vp}";
		assertTrue(equalsQueryContent(elem2, "<vp>"));
	}
	
	@Test
	public void testEmptyTokens() throws QueryException {
		// [base=der][][base=Mann]
		String et1 = 
			"{@type=korap:group, relation=distance, @subtype=incl, constraint=[" +
				"{@type=korap:distance, measure=w, direction=plus, min=1, max=1}" +
			"], " +
			"operands=[" +
				"{@type=korap:token, @value={@type=korap:term, @value=base:der, relation==}}," +
				"{@type=korap:token, @value={@type=korap:term, @value=base:Mann, relation==}}" +
			"]}";
		ppt = new PoliqarpPlusTree("[base=der][][base=Mann]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(et1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=der][][][base=Mann]
		String et2 = 
			"{@type=korap:group, relation=distance, @subtype=incl, constraint=[" +
				"{@type=korap:distance, measure=w, direction=plus, min=2, max=2}" +
			"], " +
			"operands=[" +
				"{@type=korap:token, @value={@type=korap:term, @value=base:der, relation==}}," +
				"{@type=korap:token, @value={@type=korap:term, @value=base:Mann, relation==}}" +
			"]}";
		ppt = new PoliqarpPlusTree("[base=der][][][base=Mann]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(et2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=der][][]?[base=Mann]
		String et3 = 
			"{@type=korap:group, relation=distance, @subtype=incl, constraint=[" +
				"{@type=korap:distance, measure=w, direction=plus, min=1, max=2}" +
			"], " +
			"operands=[" +
				"{@type=korap:token, @value={@type=korap:term, @value=base:der, relation==}}," +
				"{@type=korap:token, @value={@type=korap:term, @value=base:Mann, relation==}}" +
			"]}";
		ppt = new PoliqarpPlusTree("[base=der][][]?[base=Mann]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(et3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		
		// startswith(<s>, [][base=Mann]
		String et4 = 
			"{@type=korap:group, relation=position, position=startswith, operands=[" +
				"{@type=korap:element, @value=s}," +
				"{@type=korap:sequence, offset-min=1, offset-max=1, operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=base:Mann, relation==}}" +
				"]}" +
			"]}";
		ppt = new PoliqarpPlusTree("startswith(<s>, [][base=Mann])");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(et4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		String et5 = 
				"{@type=korap:group, relation=distance, @subtype=incl, constraint=[" +
					"{@type=korap:distance, measure=w, direction=plus, min=1, max=1}" +
				"], " +
				"operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=base:der, relation==}}," +
					"{@type=korap:group, relation=distance, @subtype=incl, constraint=[" +
						"{@type=korap:distance, measure=w, direction=plus, min=2, max=2}" +
					"], " +
					"operands=[" +
						"{@type=korap:token, @value={@type=korap:term, @value=base:Mann, relation==}}," +
						"{@type=korap:token, @value={@type=korap:term, @value=base:Frau, relation==}}" +
					"]}" +
				"]}";
		ppt = new PoliqarpPlusTree("[base=der][][base=Mann][][][base=Frau]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(et5.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
	}

	@Test
	public void testCoordinatedFields() throws QueryException {
		// [base=Mann&(cas=N|cas=A)]
		String cof1 = 
			"{@type=korap:token, @value=" +
				"{@type=korap:group, operands=[" +
					"{@type=korap:term, @value=base:Mann, relation==}," +
					"{@type=korap:group, operands=[" +
						"{@type=korap:term, @value=cas:N, relation==}," +
						"{@type=korap:term, @value=cas:A, relation==}" +
					"], relation=or}" +
				"], relation=and}" +
			"}";
		ppt = new PoliqarpPlusTree("[base=Mann&(cas=N|cas=A)]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(cof1.replaceAll(" ", ""), map.replaceAll(" ", ""));


		assertEquals(
		    new PoliqarpPlusTree(" [ base=Mann & ( cas=N | cas=A)] ").getRequestMap().get("query").toString(),
		    new PoliqarpPlusTree("[base=Mann &(cas=N|cas=A)]").getRequestMap().get("query").toString()
	        );
	}
	
	@Test
	public void testOccurrence() throws QueryException {
		// [base=foo]*
		String occ1 = "{@type=korap:group, operands=[" +
					     "{@type=korap:token, @value={@type=korap:term, @value=base:foo, relation==}}" +
					  "], relation=repetition, quantifier=* }"; 
		ppt = new PoliqarpPlusTree("[base=foo]*");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=foo]*[base=bar]
		String occ2 = 
				"{@type=korap:sequence, operands=[" +
					"{@type=korap:group, operands=[" +
					     "{@type=korap:token, @value={@type=korap:term, @value=base:foo, relation==}}" +
					"], relation=repetition, quantifier=* }," +
					"{@type=korap:token, @value={@type=korap:term, @value=base:bar, relation==}}" +
				"]}"; 
		ppt = new PoliqarpPlusTree("[base=foo]*[base=bar]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=bar][base=foo]*
		String occ3 = 
				"{@type=korap:sequence, operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=base:bar, relation==}}," +
					"{@type=korap:group, operands=[" +
					     "{@type=korap:token, @value={@type=korap:term, @value=base:foo, relation==}}" +
					"], relation=repetition, quantifier=* }" +
				"]}"; 
		ppt = new PoliqarpPlusTree("[base=bar][base=foo]*");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// ([base=bar][base=foo])*
		String occ4 = 
				"{@type=korap:group, operands=[" +	
					"{@type=korap:sequence, operands=[" +
						"{@type=korap:token, @value={@type=korap:term, @value=base:bar, relation==}}," +
						"{@type=korap:token, @value={@type=korap:term, @value=base:foo, relation==}}" +
					"]}" +
				"], relation=repetition, quantifier=* }" ;
		ppt = new PoliqarpPlusTree("([base=bar][base=foo])*");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// <s>([base=bar][base=foo])*
		String occ5 = 
				"{@type=korap:sequence, operands=[" +
					"{@type=korap:element, @value=s}," +
					"{@type=korap:group, operands=[" +	
						"{@type=korap:sequence, operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=base:bar, relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=base:foo, relation==}}" +
						"]}" +
					"], relation=repetition, quantifier=* }" +
				"]}" ;
		ppt = new PoliqarpPlusTree("<s>([base=bar][base=foo])*");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ5.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// <s><np>([base=bar][base=foo])*
		String occ6 = 
				"{@type=korap:sequence, operands=[" +
					"{@type=korap:element, @value=s}," +
					"{@type=korap:element, @value=np}," +
					"{@type=korap:group, operands=[" +	
						"{@type=korap:sequence, operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=base:bar, relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=base:foo, relation==}}" +
						"]}" +
					"], relation=repetition, quantifier=* }" +
				"]}" ;
		ppt = new PoliqarpPlusTree("<s><np>([base=bar][base=foo])*");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ6.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// <s><np>([base=bar][base=foo])*[p=NN]
		// comment: embedded sequence shouldn't really be here, but does not really hurt, either. (?)
		// really hard to get this behaviour out of the PQPlus grammar...
		String occ7 = 
				"{@type=korap:sequence, operands=[" +
					"{@type=korap:element, @value=s}," +
					"{@type=korap:element, @value=np}," +
					"{@type=korap:group, operands=[" +	
						"{@type=korap:sequence, operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=base:bar, relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=base:foo, relation==}}" +
						"]}" +
					"], relation=repetition, quantifier=* }," +
					"{@type=korap:token, @value={@type=korap:term, @value=p:NN, relation==}}" +
				"]}" ;
		ppt = new PoliqarpPlusTree("<s><np>([base=bar][base=foo])*[p=NN]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ7.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// ([base=bar][base=foo])*[p=NN]
		String occ8 = 
				"{@type=korap:sequence, operands=[" +
					"{@type=korap:group, operands=[" +	
						"{@type=korap:sequence, operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=base:bar, relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=base:foo, relation==}}" +
						"]}" +
					"], relation=repetition, quantifier=* }," +
					"{@type=korap:token, @value={@type=korap:term, @value=p:NN, relation==}}" +
				"]}" ;
		ppt = new PoliqarpPlusTree("([base=bar][base=foo])*[p=NN]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ8.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testTokenSequence() throws QueryException {
		// [base=Mann][orth=Frau]
		String seq1 = "{@type=korap:sequence, operands=[" +
				"{@type=korap:token, @value={@type=korap:term, @value=base:Mann, relation==}}, " +
				"{@type=korap:token, @value={@type=korap:term, @value=orth:Frau, relation==}}" +
				"]}";
		assertTrue(equalsQueryContent(seq1, "[base=Mann][orth=Frau]"));
		
		// [base=Mann][orth=Frau][p=NN]
		String seq2 = "{@type=korap:sequence, operands=[" +
				"{@type=korap:token, @value={@type=korap:term, @value=base:Mann, relation==}}, " +
				"{@type=korap:token, @value={@type=korap:term, @value=orth:Frau, relation==}}, " +
				"{@type=korap:token, @value={@type=korap:term, @value=p:NN, relation==}}" +
				"]}";
		assertTrue(equalsQueryContent(seq2, "[base=Mann][orth=Frau][p=NN]"));
	}
	
	@Test
	public void testDisjSegments() throws QueryException {
		// ([base=der]|[base=das])[base=Schild]
		String disj1 = 
				"{@type=korap:sequence, operands=[" +
					"{@type=korap:group, relation=or, operands=[" +
						"{@type=korap:token, @value={@type=korap:term, @value=base:der, relation==}}," +
						"{@type=korap:token, @value={@type=korap:term, @value=base:das, relation==}}" +
					"]}," +
					"{@type=korap:token, @value={@type=korap:term, @value=base:Schild, relation==}}" +
				"]}";
		ppt = new PoliqarpPlusTree("([base=der]|[base=das])[base=Schild]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(disj1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=Schild]([base=der]|[base=das])
		String disj2 = 
				"{@type=korap:sequence, operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=base:Schild, relation==}}," +
					"{@type=korap:group, relation=or, operands=[" +
						"{@type=korap:token, @value={@type=korap:term, @value=base:der, relation==}}," +
						"{@type=korap:token, @value={@type=korap:term, @value=base:das, relation==}}" +
					"]}" +
				"]}";
		ppt = new PoliqarpPlusTree("[base=Schild]([base=der]|[base=das])");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(disj2.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testTokenElemSequence() throws QueryException {
		// [base=Mann]<vp>
		String seq1 = "{@type=korap:sequence, operands=[" +
				"{@type=korap:token, @value={@type=korap:term, @value=base:Mann, relation==}}, " +
				"{@type=korap:element, @value=vp}" +
				"]}";
		assertTrue(equalsQueryContent(seq1, "[base=Mann]<vp>"));
		
		// <vp>[base=Mann]
		String seq2 = "{@type=korap:sequence, operands=[" +
				"{@type=korap:element, @value=vp}, "+
				"{@type=korap:token, @value={@type=korap:term, @value=base:Mann, relation==}} " +
				"]}";
		assertTrue(equalsQueryContent(seq2, "<vp>[base=Mann]"));
		
		// <vp>[base=Mann]<pp>
		String seq3 = "{@type=korap:sequence, operands=[" +
				"{@type=korap:element, @value=vp}, "+
				"{@type=korap:token, @value={@type=korap:term, @value=base:Mann, relation==}}, " +
				"{@type=korap:element, @value=pp} "+
				"]}";
		assertTrue(equalsQueryContent(seq3, "<vp>[base=Mann]<pp>"));
	}
	
	@Test
	public void testElemSequence() throws QueryException {
		// <np><vp>
		String seq1 = "{@type=korap:sequence, operands=[" +
				"{@type=korap:element, @value=np}," +
				"{@type=korap:element, @value=vp}" +
				"]}";
		assertTrue(equalsQueryContent(seq1, "<np><vp>"));
		
		// <np><vp><pp>
		String seq2 = "{@type=korap:sequence, operands=[" +
				"{@type=korap:element, @value=np}," +
				"{@type=korap:element, @value=vp}," +
				"{@type=korap:element, @value=pp}" +
				"]}";
		assertTrue(equalsQueryContent(seq2, "<np><vp><pp>"));
	}
	
	@Test 
	public void testClasses() throws QueryException {
		// {[base=Mann]}
		String cls1 = "{@type=korap:group, class=0, operands=[" +
				"{@type=korap:token, @value={@type=korap:term, @value=base:Mann, relation==}}" +
				"]}";
		assertTrue(equalsQueryContent(cls1, "{[base=Mann]}"));
		
		// {[base=Mann][orth=Frau]}
		String cls2 = "{@type=korap:group, class=0, operands=[" +
				 "{@type=korap:sequence, operands=[" +
				  "{@type=korap:token, @value={@type=korap:term, @value=base:Mann, relation==}}," +
				  "{@type=korap:token, @value={@type=korap:term, @value=orth:Frau, relation==}}" +
				 "]}" +
				"]}";
		assertTrue(equalsQueryContent(cls2, "{[base=Mann][orth=Frau]}"));
		
		// [p=NN]{[base=Mann][orth=Frau]}
		String cls3 = "{@type=korap:sequence, operands=[" +
						"{@type=korap:token, @value={@type=korap:term, @value=p:NN, relation==}}," +
						"{@type=korap:group, class=0, operands=[" +
							"{@type=korap:sequence, operands=[" +
								"{@type=korap:token, @value={@type=korap:term, @value=base:Mann, relation==}}," +
								"{@type=korap:token, @value={@type=korap:term, @value=orth:Frau, relation==}}" +
							"]}" +
						"]}" +
					  "]}";
		assertTrue(equalsQueryContent(cls3, "[p=NN]{[base=Mann][orth=Frau]}"));
		
		// {[base=Mann][orth=Frau]}[p=NN]
		String cls4 = "{@type=korap:sequence, operands=[" +
						"{@type=korap:group, class=0, operands=[" +
						   "{@type=korap:sequence, operands=[" +
						     "{@type=korap:token, @value={@type=korap:term, @value=base:Mann, relation==}}," +
						     "{@type=korap:token, @value={@type=korap:term, @value=orth:Frau, relation==}}" +
						   "]}" +
						"]}," +
						"{@type=korap:token, @value={@type=korap:term, @value=p:NN, relation==}}" +
					  "]}";
		assertTrue(equalsQueryContent(cls4, "{[base=Mann][orth=Frau]}[p=NN]"));

		// {2:{1:[tt/p=ADJA]}[mate/p=NN]}"
		String cls5 = "{@type=korap:group, class=2, operands=[" +
						"{@type=korap:sequence, operands=[" +
						   "{@type=korap:group, class=1, operands=[" +
						     "{@type=korap:token, @value={@type=korap:term, @value=tt/p:ADJA, relation==}}" +
						   "]}," +
						   "{@type=korap:token, @value={@type=korap:term, @value=mate/p:NN, relation==}}" + 
						"]}" +
					  "]}";
		ppt = new PoliqarpPlusTree("{2: {1:[tt/p=ADJA]}[mate/p=NN]}");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(cls5.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testPositions() throws QueryException {
		// contains(<s>,<np>)
		String pos1 = "{@type=korap:group, relation=position, position=contains, operands=[" +
				  "{@type=korap:element, @value=s}," +
				  "{@type=korap:element, @value=np}" +
				"]}";
		assertTrue(equalsQueryContent(pos1, "contains(<s>,<np>)"));
		
		// contains(<s>,[base=Mann])
		String pos2 = "{@type=korap:group, relation=position, position=contains, operands=[" +
				  "{@type=korap:element, @value=s}," +
				  "{@type=korap:token, @value= {@type=korap:term, @value=base:Mann, relation==}}" +
				"]}";
		assertTrue(equalsQueryContent(pos2, "contains(<s>,[base=Mann])"));
		
		// contains(<s>,[orth=der][orth=Mann])
		String pos3 = "{@type=korap:group, relation=position, position=contains, operands=[" +
				  	"{@type=korap:element, @value=s}," +
				  	"{@type=korap:sequence, operands=[" +
				  		"{@type=korap:token, @value={@type=korap:term, @value=orth:der, relation==}}," +
				  		"{@type=korap:token, @value={@type=korap:term, @value=orth:Mann, relation==}}" +
				  	"]}" +
				  "]}";
		ppt = new PoliqarpPlusTree("contains(<s>,[orth=der][orth=Mann])");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(pos3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=Auto]contains(<s>,[base=Mann])
		String pos4 = 
				"{@type=korap:sequence, operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=base:Auto, relation==}}," +
					"{@type=korap:group, relation=position, position=contains, operands=[" +
				  		"{@type=korap:element, @value=s}," +
				  		"{@type=korap:token, @value={@type=korap:term, @value=base:Mann, relation==}}" +
				  	"]}" +
				"]}";
		ppt = new PoliqarpPlusTree("[base=Auto]contains(<s>,[base=Mann])");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(pos4.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testNestedPositions() throws QueryException {
		// contains(<s>,startswith(<np>,[orth=Der]))
		String npos1 = 
			"{@type=korap:group, relation=position, position=contains, operands=[" +
				"{@type=korap:element, @value=s}," +
				"{@type=korap:group, relation=position, position=startswith, operands=[" +
					"{@type=korap:element, @value=np}," +
					"{@type=korap:token, @value={@type=korap:term, @value=orth:Der, relation==}}" +
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
			"{@type=korap:group, relation=shrink, shrink=0, operands=[" +
				"{@type=korap:sequence, operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=orth:Der, relation==}}," +
					"{@type=korap:group, class=0, operands=[" +
						"{@type=korap:token, @value={@type=korap:term, @value=orth:Mann, relation==}}" +
					"]}" +
				"]}" +
			"]}";
		ppt = new PoliqarpPlusTree("shrink([orth=Der]{[orth=Mann]})");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(shr1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// shrink([orth=Der]{[orth=Mann][orth=geht]})
		String shr2 = 
			"{@type=korap:group, relation=shrink, shrink=0, operands=[" +
				"{@type=korap:sequence, operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=orth:Der, relation==}}," +
					"{@type=korap:group, class=0, operands=[" +
						"{@type=korap:sequence, operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=orth:Mann, relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=orth:geht, relation==}}" +
						"]}" +
					"]}" +
				"]}" +
			"]}";
		ppt = new PoliqarpPlusTree("shrink([orth=Der]{[orth=Mann][orth=geht]})");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(shr2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// shrink(1:[orth=Der]{1:[orth=Mann][orth=geht]})
		String shr3 = 
			"{@type=korap:group, relation=shrink, shrink=1, operands=[" +
				"{@type=korap:sequence, operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=orth:Der, relation==}}," +
					"{@type=korap:group, class=1, operands=[" +
						"{@type=korap:sequence, operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=orth:Mann, relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=orth:geht, relation==}}" +
						"]}" +
					"]}" +
				"]}" +
			"]}";
		ppt = new PoliqarpPlusTree("shrink(1:[orth=Der]{1:[orth=Mann][orth=geht]})");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(shr3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// shrink(1:startswith(<s>,{1:<np>}))
		String shr4 = 
			"{@type=korap:group, relation=shrink, shrink=1, operands=[" +
				"{@type=korap:group, relation=position, position=startswith, operands=[" +
					"{@type=korap:element, @value=s}," +
					"{@type=korap:group, class=1, operands=[" +
						"{@type=korap:element, @value=np}" +
					"]}" +
				"]}" +
			"]}";
		ppt = new PoliqarpPlusTree("shrink(1:startswith(<s>,{1:<np>}))");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(shr4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// shrink(3: startswith(<s>, {3:[base=der]{1:[mate/p=ADJA]{2:[tt/p=NN]}}})) 
		String shr5 = 
			"{@type=korap:group, relation=shrink, shrink=3, operands=[" +
				"{@type=korap:group, relation=position, position=startswith, operands=[" +
					"{@type=korap:element, @value=s}," +
					"{@type=korap:group, class=3, operands=[" +
						"{@type=korap:sequence, operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=base:der, relation==}}," +
							"{@type=korap:group, class=1, operands=[" +
								"{@type=korap:sequence, operands=[" +
									"{@type=korap:token, @value={@type=korap:term, @value=mate/p:ADJA, relation==}}," +
									"{@type=korap:group, class=2, operands=[" +
										"{@type=korap:token, @value={@type=korap:term, @value=tt/p:NN, relation==}}" +
									"]}" + 
								"]}" +
							"]}" +
						"]}" +
					"]}" +
				"]}" +
			"]}";
		ppt = new PoliqarpPlusTree("shrink(3:startswith(<s>,{3:[base=der]{1:[mate/p=ADJA]{2:[tt/p=NN]}}})) ");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(shr5.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	
	@Test
	public void testLayers() throws QueryException {
		// [base=Mann]
		String layer1 = "{@type=korap:token, @value={@type=korap:term, @value=tt/base:Mann, relation==}}";
		assertTrue(equalsQueryContent(layer1, "[tt/base=Mann]"));
		
	}
	
	@Test
	public void testAlign() {
		// [orth=der]^[orth=Mann]
		String align1 = 
				"{@type=korap:sequence, operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=orth:der, relation==}}," +
					"{@type=korap:group, relation=left-align, operands=[" +
						"{@type=korap:token, @value={@type=korap:term, @value=orth:Mann, relation==}}" +
					"]}" +
				"]}";
		ppt = new PoliqarpPlusTree("[orth=der]^[orth=Mann]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(align1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// "([base=a]^[base=b])|[base=c]",
		String align2 = 
				"{@type=korap:group, relation=or, operands=[" +
						"{@type=korap:sequence, operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=base:a, relation==}}," +
							"{@type=korap:group, relation=left-align, operands=[" +
								"{@type=korap:token, @value={@type=korap:term, @value=base:b, relation==}}" +
							"]}" +
						"]}," +
						"{@type=korap:token, @value={@type=korap:term, @value=base:c, relation==}}" +
				"]}";
		ppt = new PoliqarpPlusTree("([base=a]^[base=b])|[base=c]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(align2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// "([base=a]^[base=b][base=c])|[base=d]",
		String align3 = 
				"{@type=korap:group, relation=or, operands=[" +
						"{@type=korap:sequence, operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=base:a, relation==}}," +
							"{@type=korap:group, relation=left-align, operands=[" +
								"{@type=korap:token, @value={@type=korap:term, @value=base:b, relation==}}" +
							"]}," +
							"{@type=korap:token, @value={@type=korap:term, @value=base:c, relation==}}" +
						"]}," +
						"{@type=korap:token, @value={@type=korap:term, @value=base:d, relation==}}" +
				"]}";
		ppt = new PoliqarpPlusTree("([base=a]^[base=b][base=c])|[base=d]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(align3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// "([base=a]^[base=b]^[base=c])|[base=d]",
		String align4 = 
				"{@type=korap:group, relation=or, operands=[" +
						"{@type=korap:sequence, operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=base:a, relation==}}," +
							"{@type=korap:group, relation=left-align, operands=[" +
								"{@type=korap:token, @value={@type=korap:term, @value=base:b, relation==}}" +
							"]}," +
							"{@type=korap:group, relation=left-align, operands=[" +
								"{@type=korap:token, @value={@type=korap:term, @value=base:c, relation==}}" +
							"]}" +
						"]}," +
						"{@type=korap:token, @value={@type=korap:term, @value=base:d, relation==}}" +
				"]}";
		ppt = new PoliqarpPlusTree("([base=a]^[base=b]^[base=c])|[base=d]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(align4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		
	}
	
	@Test
	public void testSimpleQueries() {
		// Baum
		String simple1 = 
				"{@type=korap:token, @value={@type=korap:term, @value=orth:Baum, relation==}}";
		ppt = new PoliqarpPlusTree("Baum");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(simple1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// Der Baum
		String simple2 = 
				"{@type=korap:sequence, operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=orth:Der, relation==}}, " +
					"{@type=korap:token, @value={@type=korap:term, @value=orth:Baum, relation==}}" +
				"]}";
		ppt = new PoliqarpPlusTree("Der Baum");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(simple2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// Der große Baum
		String simple3 = 
				"{@type=korap:sequence, operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=orth:Der, relation==}}, " +
					"{@type=korap:token, @value={@type=korap:term, @value=orth:große, relation==}}, " +						
					"{@type=korap:token, @value={@type=korap:term, @value=orth:Baum, relation==}}" +
				"]}";
		ppt = new PoliqarpPlusTree("Der große Baum");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(simple3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// Baum | Stein
		String simple4 = 
				"{@type=korap:group, relation=or, operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=orth:Baum, relation==}}, " +						
					"{@type=korap:token, @value={@type=korap:term, @value=orth:Stein, relation==}}" +
				"]}";
		ppt = new PoliqarpPlusTree("Baum | Stein");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(simple4.replaceAll(" ", ""), map.replaceAll(" ", ""));		
	}
}

