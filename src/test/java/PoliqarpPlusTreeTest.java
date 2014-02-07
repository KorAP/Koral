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
		String contextString = "{korap=http://korap.ids-mannheim.de/ns/query, @language=de, @operands={@id=korap:operands, @container=@list}, @relation={@id=korap:relation, @type=korap:relation#types}, class={@id=korap:class, @type=xsd:integer}, query=korap:query, filter=korap:filter, meta=korap:meta}";
		ppt = new PoliqarpPlusTree("[base=test]");
		assertTrue(equalsContent(contextString, ppt.getRequestMap().get("@context")));
	}
	
	@Test
	public void testSingleTokens() throws QueryException {
		// [base=Mann]
		String token1 = "{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=base, @relation==}}";
		assertTrue(equalsQueryContent(token1, "[base=Mann]"));
		
		// [orth!=Frau]
		String token2 = "{@type=korap:token, @value={@type=korap:term, @value=Frau, @attr=orth, @relation=!=}}";
		assertTrue(equalsQueryContent(token2, "[orth!=Frau]"));
		
		// [!p=NN]
		String token3 = "{@type=korap:token, @value={@type=korap:term, @value=NN, @attr=p, @relation=!=}}";
		assertTrue(equalsQueryContent(token3, "[!p=NN]"));
		
		// [!p!=NN]
		String token4 = "{@type=korap:token, @value={@type=korap:term, @value=NN, @attr=p, @relation==}}";
		assertTrue(equalsQueryContent(token4, "[!p!=NN]"));
	}
	
	@Test
	public void testElements() throws QueryException {
		// <s>
		String elem1 = "{@type=korap:span, @value=s}";
		assertTrue(equalsQueryContent(elem1, "<s>"));
		
		// <vp>
		String elem2 = "{@type=korap:span, @value=vp}";
		assertTrue(equalsQueryContent(elem2, "<vp>"));
	}
	
	@Test
	public void testEmptyTokens() throws QueryException {
		// [base=der][][base=Mann]
		String et1 = 
			"{@type=korap:sequence, @inOrder=true, @constraints=[" +
				"{@type=korap:distance, measure=w, min=1, max=1}" +
			"], " +
			"@operands=[" +
				"{@type=korap:token, @value={@type=korap:term, @value=der, @attr=base, @relation==}}," +
				"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=base, @relation==}}" +
			"]}";
		ppt = new PoliqarpPlusTree("[base=der][][base=Mann]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(et1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=der][][][base=Mann]
		String et2 = 
			"{@type=korap:sequence, @inOrder=true, @constraints=[" +
				"{@type=korap:distance, measure=w, min=2, max=2}" +
			"], " +
			"@operands=[" +
				"{@type=korap:token, @value={@type=korap:term, @value=der, @attr=base, @relation==}}," +
				"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=base, @relation==}}" +
			"]}";
		ppt = new PoliqarpPlusTree("[base=der][][][base=Mann]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(et2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=der][][]?[base=Mann]
		String et3 = 
			"{@type=korap:sequence, @inOrder=true, @constraints=[" +
				"{@type=korap:distance, measure=w, min=1, max=2}" +
			"], " +
			"@operands=[" +
				"{@type=korap:token, @value={@type=korap:term, @value=der, @attr=base, @relation==}}," +
				"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=base, @relation==}}" +
			"]}";
		ppt = new PoliqarpPlusTree("[base=der][][]?[base=Mann]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(et3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		
		// startswith(<s>, [][base=Mann]
		String et4 = 
			"{@type=korap:group, @relation=position, @position=startswith, @operands=[" +	
				"{@type=korap:span, @value=s}," +
				"{@type=korap:sequence, offset-min=1, offset-max=1, @operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=base, @relation==}}" +
				"]}" +
			"]}";
		ppt = new PoliqarpPlusTree("startswith(<s>, [][base=Mann])");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(et4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=der][]{2,5}[base=Mann][]?[][base=Frau]   nested distances
		String et5 = 
				"{@type=korap:sequence, @inOrder=true, @constraints=[" +
					"{@type=korap:distance, measure=w, min=2, max=5}" +
				"], " +
				"@operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=der, @attr=base, @relation==}}," +
					"{@type=korap:sequence, @inOrder=true, @constraints=[" +
						"{@type=korap:distance, measure=w, min=1, max=2}" +
					"], " +
					"@operands=[" +
						"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=base, @relation==}}," +
						"{@type=korap:token, @value={@type=korap:term, @value=Frau, @attr=base, @relation==}}" +
					"]}" +
				"]}";
		ppt = new PoliqarpPlusTree("[base=der][]{2,5}[base=Mann][]?[][base=Frau]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(et5.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
	}

	@Test
	public void testCoordinatedFields() throws QueryException {
		// [base=Mann&(cas=N|cas=A)]
		String cof1 = 
			"{@type=korap:token, @value=" +
				"{@type=korap:group, @operands=[" +
					"{@type=korap:term, @value=Mann, @attr=base, @relation==}," +
					"{@type=korap:group, @operands=[" +
						"{@type=korap:term, @value=N, @attr=cas, @relation==}," +
						"{@type=korap:term, @value=A, @attr=cas, @relation==}" +
					"], @relation=or}" +
				"], @relation=and}" +
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
			"{@type=korap:token, @value=" +
				"{@type=korap:group, @operands=[" +
					"{@type=korap:term, @value=Mann, @attr=base, @relation==}," +
					"{@type=korap:term, @value=N, @attr=cas, @relation==}," +
					"{@type=korap:term, @value=m, @attr=gen, @relation==}" +
				"], @relation=and}" +
			"}";
		ppt = new PoliqarpPlusTree("[base=Mann&cas=N&gen=m]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(cof2.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOccurrence() throws QueryException {
		// [base=foo]*
		String occ1 = "{@type=korap:group, @operands=[" +
					     "{@type=korap:token, @value={@type=korap:term, @value=foo, @attr=base, @relation==}}" +
					  "], @relation=repetition, @min=0, @max=100}"; 
		ppt = new PoliqarpPlusTree("[base=foo]*");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=foo]*[base=bar]
		String occ2 = 
				"{@type=korap:sequence, @operands=[" +
					"{@type=korap:group, @operands=[" +
					     "{@type=korap:token, @value={@type=korap:term, @value=foo, @attr=base, @relation==}}" +
					"], @relation=repetition, @min=0, @max=100 }," +
					"{@type=korap:token, @value={@type=korap:term, @value=bar, @attr=base, @relation==}}" +
				"]}"; 
		ppt = new PoliqarpPlusTree("[base=foo]*[base=bar]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=bar][base=foo]*
		String occ3 = 
				"{@type=korap:sequence, @operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=bar, @attr=base, @relation==}}," +
					"{@type=korap:group, @operands=[" +
					     "{@type=korap:token, @value={@type=korap:term, @value=foo, @attr=base, @relation==}}" +
					"], @relation=repetition, @min=0, @max=100 }" +
				"]}"; 
		ppt = new PoliqarpPlusTree("[base=bar][base=foo]*");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// ([base=bar][base=foo])*
		String occ4 = 
				"{@type=korap:group, @operands=[" +	
					"{@type=korap:sequence, @operands=[" +
						"{@type=korap:token, @value={@type=korap:term, @value=bar, @attr=base, @relation==}}," +
						"{@type=korap:token, @value={@type=korap:term, @value=foo, @attr=base, @relation==}}" +
					"]}" +
				"], @relation=repetition, @min=0, @max=100 }" ;
		ppt = new PoliqarpPlusTree("([base=bar][base=foo])*");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// <s>([base=bar][base=foo])*
		String occ5 = 
				"{@type=korap:sequence, @operands=[" +
					"{@type=korap:span, @value=s}," +
					"{@type=korap:group, @operands=[" +	
						"{@type=korap:sequence, @operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=bar, @attr=base, @relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=foo, @attr=base, @relation==}}" +
						"]}" +
					"], @relation=repetition, @min=0, @max=100 }" +
				"]}" ;
		ppt = new PoliqarpPlusTree("<s>([base=bar][base=foo])*");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ5.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// <s><np>([base=bar][base=foo])*
		String occ6 = 
				"{@type=korap:sequence, @operands=[" +
					"{@type=korap:span, @value=s}," +
					"{@type=korap:span, @value=np}," +
					"{@type=korap:group, @operands=[" +	
						"{@type=korap:sequence, @operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=bar, @attr=base, @relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=foo, @attr=base, @relation==}}" +
						"]}" +
					"], @relation=repetition, @min=0, @max=100 }" +
				"]}" ;
		ppt = new PoliqarpPlusTree("<s><np>([base=bar][base=foo])*");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ6.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// <s><np>([base=bar][base=foo])*[p=NN]
		// comment: embedded sequence shouldn't really be here, but does not really hurt, either. (?)
		// really hard to get this behaviour out of the PQPlus grammar...
		String occ7 = 
				"{@type=korap:sequence, @operands=[" +
					"{@type=korap:span, @value=s}," +
					"{@type=korap:span, @value=np}," +
					"{@type=korap:group, @operands=[" +	
						"{@type=korap:sequence, @operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=bar, @attr=base, @relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=foo, @attr=base, @relation==}}" +
						"]}" +
					"], @relation=repetition, @min=0, @max=100 }," +
					"{@type=korap:token, @value={@type=korap:term, @value=NN, @attr=p, @relation==}}" +
				"]}" ;
		ppt = new PoliqarpPlusTree("<s><np>([base=bar][base=foo])*[p=NN]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ7.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// ([base=bar][base=foo])*[p=NN]
		String occ8 = 
				"{@type=korap:sequence, @operands=[" +
					"{@type=korap:group, @operands=[" +	
						"{@type=korap:sequence, @operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=bar, @attr=base, @relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=foo, @attr=base, @relation==}}" +
						"]}" +
					"], @relation=repetition, @min=0, @max=100 }," +
					"{@type=korap:token, @value={@type=korap:term, @value=NN, @attr=p, @relation==}}" +
				"]}" ;
		ppt = new PoliqarpPlusTree("([base=bar][base=foo])*[p=NN]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ8.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=foo]+
		String occ9 = "{@type=korap:group, @operands=[" +
					     "{@type=korap:token, @value={@type=korap:term, @value=foo, @attr=base, @relation==}}" +
					  "], @relation=repetition, @min=1, @max=100}"; 
		ppt = new PoliqarpPlusTree("[base=foo]+");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ9.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=foo]?
		String occ10 = "{@type=korap:group, @operands=[" +
					     "{@type=korap:token, @value={@type=korap:term, @value=foo, @attr=base, @relation==}}" +
					  "], @relation=repetition, @min=0, @max=1}"; 
		ppt = new PoliqarpPlusTree("[base=foo]?");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ10.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=foo]{2,5}
		String occ11 = "{@type=korap:group, @operands=[" +
					     "{@type=korap:token, @value={@type=korap:term, @value=foo, @attr=base, @relation==}}" +
					  "], @relation=repetition, @min=2, @max=5}"; 
		ppt = new PoliqarpPlusTree("[base=foo]{2,5}");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ11.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testTokenSequence() throws QueryException {
		// [base=Mann][orth=Frau]
		String seq1 = "{@type=korap:sequence, @operands=[" +
				"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=base, @relation==}}, " +
				"{@type=korap:token, @value={@type=korap:term, @value=Frau, @attr=orth, @relation==}}" +
				"]}";
		assertTrue(equalsQueryContent(seq1, "[base=Mann][orth=Frau]"));
		
		// [base=Mann][orth=Frau][p=NN]
		String seq2 = "{@type=korap:sequence, @operands=[" +
				"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=base, @relation==}}, " +
				"{@type=korap:token, @value={@type=korap:term, @value=Frau, @attr=orth, @relation==}}, " +
				"{@type=korap:token, @value={@type=korap:term, @value=NN,@attr=p, @relation==}}" +
				"]}";
		assertTrue(equalsQueryContent(seq2, "[base=Mann][orth=Frau][p=NN]"));
	}
	
	@Test
	public void testDisjSegments() throws QueryException {
		// ([base=der]|[base=das])[base=Schild]
		String disj1 = 
				"{@type=korap:sequence, @operands=[" +
					"{@type=korap:group, @relation=or, @operands=[" +
						"{@type=korap:token, @value={@type=korap:term, @value=der, @attr=base, @relation==}}," +
						"{@type=korap:token, @value={@type=korap:term, @value=das, @attr=base, @relation==}}" +
					"]}," +
					"{@type=korap:token, @value={@type=korap:term, @value=Schild, @attr=base, @relation==}}" +
				"]}";
		ppt = new PoliqarpPlusTree("([base=der]|[base=das])[base=Schild]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(disj1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=Schild]([base=der]|[base=das])
		String disj2 = 
				"{@type=korap:sequence, @operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=Schild, @attr=base, @relation==}}," +
					"{@type=korap:group, @relation=or, @operands=[" +
						"{@type=korap:token, @value={@type=korap:term, @value=der, @attr=base, @relation==}}," +
						"{@type=korap:token, @value={@type=korap:term, @value=das, @attr=base, @relation==}}" +
					"]}" +
				"]}";
		ppt = new PoliqarpPlusTree("[base=Schild]([base=der]|[base=das])");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(disj2.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testTokenElemSequence() throws QueryException {
		// [base=Mann]<vp>
		String seq1 = "{@type=korap:sequence, @operands=[" +
				"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=base, @relation==}}, " +
				"{@type=korap:span, @value=vp}" +
				"]}";
		assertTrue(equalsQueryContent(seq1, "[base=Mann]<vp>"));
		
		// <vp>[base=Mann]
		String seq2 = "{@type=korap:sequence, @operands=[" +
				"{@type=korap:span, @value=vp}, "+
				"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=base, @relation==}} " +
				"]}";
		assertTrue(equalsQueryContent(seq2, "<vp>[base=Mann]"));
		
		// <vp>[base=Mann]<pp>
		String seq3 = "{@type=korap:sequence, @operands=[" +
				"{@type=korap:span, @value=vp}, "+
				"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=base, @relation==}}, " +
				"{@type=korap:span, @value=pp} "+
				"]}";
		assertTrue(equalsQueryContent(seq3, "<vp>[base=Mann]<pp>"));
	}
	
	@Test
	public void testElemSequence() throws QueryException {
		// <np><vp>
		String seq1 = "{@type=korap:sequence, @operands=[" +
				"{@type=korap:span, @value=np}," +
				"{@type=korap:span, @value=vp}" +
				"]}";
		assertTrue(equalsQueryContent(seq1, "<np><vp>"));
		
		// <np><vp><pp>
		String seq2 = "{@type=korap:sequence, @operands=[" +
				"{@type=korap:span, @value=np}," +
				"{@type=korap:span, @value=vp}," +
				"{@type=korap:span, @value=pp}" +
				"]}";
		assertTrue(equalsQueryContent(seq2, "<np><vp><pp>"));
	}
	
	@Test 
	public void testClasses() throws QueryException {
		// {[base=Mann]}
		String cls1 = "{@type=korap:group, @relation=class, class=0, @operands=[" +
				"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=base, @relation==}}" +
				"]}";
		ppt = new PoliqarpPlusTree("{[base=Mann]}");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(cls1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// {[base=Mann][orth=Frau]}
		String cls2 = "{@type=korap:group, @relation=class, class=0, @operands=[" +
				 "{@type=korap:sequence, @operands=[" +
				  "{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=base, @relation==}}," +
				  "{@type=korap:token, @value={@type=korap:term, @value=Frau, @attr=orth, @relation==}}" +
				 "]}" +
				"]}";
		assertTrue(equalsQueryContent(cls2, "{[base=Mann][orth=Frau]}"));
		
		// [p=NN]{[base=Mann][orth=Frau]}
		String cls3 = "{@type=korap:sequence, @operands=[" +
						"{@type=korap:token, @value={@type=korap:term, @value=NN, @attr=p, @relation==}}," +
						"{@type=korap:group, @relation=class, class=0, @operands=[" +
							"{@type=korap:sequence, @operands=[" +
								"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=base, @relation==}}," +
								"{@type=korap:token, @value={@type=korap:term, @value=Frau, @attr=orth, @relation==}}" +
							"]}" +
						"]}" +
					  "]}";
		assertTrue(equalsQueryContent(cls3, "[p=NN]{[base=Mann][orth=Frau]}"));
		
		// {[base=Mann][orth=Frau]}[p=NN]
		String cls4 = "{@type=korap:sequence, @operands=[" +
						"{@type=korap:group, @relation=class, class=0, @operands=[" +
						   "{@type=korap:sequence, @operands=[" +
						     "{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=base, @relation==}}," +
						     "{@type=korap:token, @value={@type=korap:term, @value=Frau, @attr=orth, @relation==}}" +
						   "]}" +
						"]}," +
						"{@type=korap:token, @value={@type=korap:term, @value=NN, @attr=p, @relation==}}" +
					  "]}";
		ppt = new PoliqarpPlusTree("{[base=Mann][orth=Frau]}[p=NN]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(cls4.replaceAll(" ", ""), map.replaceAll(" ", ""));

		// {2:{1:[tt/p=ADJA]}[mate/p=NN]}"
		String cls5 = "{@type=korap:group, @relation=class, class=2, @operands=[" +
						"{@type=korap:sequence, @operands=[" +
						   "{@type=korap:group, @relation=class, class=1, @operands=[" +
						     "{@type=korap:token, @value={@type=korap:term, @value=ADJA, @attr=p, @foundry=tt, @relation==}}" +
						   "]}," +
						   "{@type=korap:token, @value={@type=korap:term, @value=NN, @attr=p, @foundry=mate, @relation==}}" + 
						"]}" +
					  "]}";
		ppt = new PoliqarpPlusTree("{2: {1:[tt/p=ADJA]}[mate/p=NN]}");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(cls5.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testPositions() throws QueryException {
		// contains(<s>,<np>)
		String pos1 = "{@type=korap:group, @relation=position, @position=contains, @operands=[" +
				  "{@type=korap:span, @value=s}," +
				  "{@type=korap:span, @value=np}" +
				"]}";
		assertTrue(equalsQueryContent(pos1, "contains(<s>,<np>)"));
		
		// contains(<s>,[base=Mann])
		String pos2 = "{@type=korap:group, @relation=position, @position=contains, @operands=[" +
				  "{@type=korap:span, @value=s}," +
				  "{@type=korap:token, @value= {@type=korap:term, @value=Mann, @attr=base, @relation==}}" +
				"]}";
		assertTrue(equalsQueryContent(pos2, "contains(<s>,[base=Mann])"));
		
		// contains(<s>,[orth=der][orth=Mann])
		String pos3 = "{@type=korap:group, @relation=position, @position=contains, @operands=[" +
				  	"{@type=korap:span, @value=s}," +
				  	"{@type=korap:sequence, @operands=[" +
				  		"{@type=korap:token, @value={@type=korap:term, @value=der, @attr=orth, @relation==}}," +
				  		"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=orth, @relation==}}" +
				  	"]}" +
				  "]}";
		ppt = new PoliqarpPlusTree("contains(<s>,[orth=der][orth=Mann])");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(pos3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=Auto]contains(<s>,[base=Mann])
		String pos4 = 
				"{@type=korap:sequence, @operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=Auto, @attr=base, @relation==}}," +
					"{@type=korap:group, @relation=position, @position=contains, @operands=[" +
				  		"{@type=korap:span, @value=s}," +
				  		"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=base, @relation==}}" +
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
			"{@type=korap:group, @relation=position, @position=contains, @operands=[" +
				"{@type=korap:span, @value=s}," +
				"{@type=korap:group, @relation=position, @position=startswith, @operands=[" +
					"{@type=korap:span, @value=np}," +
					"{@type=korap:token, @value={@type=korap:term, @value=Der, @attr=orth, @relation==}}" +
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
			"{@type=korap:group, @relation=shrink, classRef=[0], @operands=[" +
				"{@type=korap:sequence, @operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=Der, @attr=orth, @relation==}}," +
					"{@type=korap:group, @relation=class, class=0, @operands=[" +
						"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=orth, @relation==}}" +
					"]}" +
				"]}" +
			"]}";
		ppt = new PoliqarpPlusTree("shrink([orth=Der]{[orth=Mann]})");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(shr1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// shrink([orth=Der]{[orth=Mann][orth=geht]})
		String shr2 = 
			"{@type=korap:group, @relation=shrink, classRef=[0], @operands=[" +
				"{@type=korap:sequence, @operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=Der, @attr=orth, @relation==}}," +
					"{@type=korap:group, @relation=class, class=0, @operands=[" +
						"{@type=korap:sequence, @operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=orth, @relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=geht, @attr=orth, @relation==}}" +
						"]}" +
					"]}" +
				"]}" +
			"]}";
		ppt = new PoliqarpPlusTree("shrink([orth=Der]{[orth=Mann][orth=geht]})");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(shr2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// shrink(1:[orth=Der]{1:[orth=Mann][orth=geht]})
		String shr3 = 
			"{@type=korap:group, @relation=shrink, classRef=[1], @operands=[" +
				"{@type=korap:sequence, @operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=Der, @attr=orth, @relation==}}," +
					"{@type=korap:group, @relation=class, class=1, @operands=[" +
						"{@type=korap:sequence, @operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=orth, @relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=geht, @attr=orth, @relation==}}" +
						"]}" +
					"]}" +
				"]}" +
			"]}";
		ppt = new PoliqarpPlusTree("shrink(1:[orth=Der]{1:[orth=Mann][orth=geht]})");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(shr3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// shrink(1:startswith(<s>,{1:<np>}))
		String shr4 = 
			"{@type=korap:group, @relation=shrink, classRef=[1], @operands=[" +
				"{@type=korap:group, @relation=position, @position=startswith, @operands=[" +
					"{@type=korap:span, @value=s}," +
					"{@type=korap:group, @relation=class, class=1, @operands=[" +
						"{@type=korap:span, @value=np}" +
					"]}" +
				"]}" +
			"]}";
		ppt = new PoliqarpPlusTree("shrink(1:startswith(<s>,{1:<np>}))");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(shr4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// shrink(3: startswith(<s>, {3:[base=der]{1:[mate/p=ADJA]{2:[tt/p=NN]}}})) 
		String shr5 = 
			"{@type=korap:group, @relation=shrink, classRef=[3], @operands=[" +
				"{@type=korap:group, @relation=position, @position=startswith, @operands=[" +
					"{@type=korap:span, @value=s}," +
					"{@type=korap:group, @relation=class, class=3, @operands=[" +
						"{@type=korap:sequence, @operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=der, @attr=base, @relation==}}," +
							"{@type=korap:group, @relation=class, class=1, @operands=[" +
								"{@type=korap:sequence, @operands=[" +
									"{@type=korap:token, @value={@type=korap:term, @value=ADJA, @attr=p, @foundry=mate, @relation==}}," +
									"{@type=korap:group, @relation=class, class=2, @operands=[" +
										"{@type=korap:token, @value={@type=korap:term, @value=NN, @attr=p, @foundry=tt, @relation==}}" +
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
		
		// split(3: startswith(<s>, {3:[base=der]{1:[mate/p=ADJA]{2:[tt/p=NN]}}})) 
		String shr6 = 
			"{@type=korap:group, @relation=split, classRef=[3], @operands=[" +
				"{@type=korap:group, @relation=position, @position=startswith, @operands=[" +
					"{@type=korap:span, @value=s}," +
					"{@type=korap:group, @relation=class, class=3, @operands=[" +
						"{@type=korap:sequence, @operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=der, @attr=base, @relation==}}," +
							"{@type=korap:group, @relation=class, class=1, @operands=[" +
								"{@type=korap:sequence, @operands=[" +
									"{@type=korap:token, @value={@type=korap:term, @value=ADJA, @attr=p, @foundry=mate, @relation==}}," +
									"{@type=korap:group, @relation=class, class=2, @operands=[" +
										"{@type=korap:token, @value={@type=korap:term, @value=NN, @attr=p, @foundry=tt, @relation==}}" +
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
			"{@type=korap:group, @relation=split, classRef=[2, 3], classRefOp=intersection, @operands=[" +
				"{@type=korap:group, @relation=position, @position=startswith, @operands=[" +
					"{@type=korap:span, @value=s}," +
					"{@type=korap:group, @relation=class, class=3, @operands=[" +
						"{@type=korap:sequence, @operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=der, @attr=base, @relation==}}," +
							"{@type=korap:group, @relation=class, class=1, @operands=[" +
								"{@type=korap:sequence, @operands=[" +
									"{@type=korap:token, @value={@type=korap:term, @value=ADJA, @attr=p, @foundry=mate, @relation==}}," +
									"{@type=korap:group, @relation=class, class=2, @operands=[" +
										"{@type=korap:token, @value={@type=korap:term, @value=NN, @attr=p, @foundry=tt, @relation==}}" +
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
	}
	
	
	@Test
	public void testFoundries() throws QueryException {
		// [tt/base=Mann]
		String layer1 = "{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=base, @foundry=tt, @relation==}}";
		ppt = new PoliqarpPlusTree("[tt/base=Mann]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(layer1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
	}
	
	@Test
	public void testAlign() throws QueryException {
		// [orth=der]^[orth=Mann]
		String align1 = 
				"{@type=korap:sequence, @operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=der, @attr=orth, @relation==}}," +
					"{@type=korap:group, @alignment=left, @operands=[" +
						"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=orth, @relation==}}" +
					"]}" +
				"]}";
		ppt = new PoliqarpPlusTree("[orth=der]^[orth=Mann]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(align1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [orth=der]^[orth=große][orth=Mann]
		String query = "[orth=der]^[orth=große][orth=Mann]";
		String align1b = 
				"{@type=korap:sequence, @operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=der, @attr=orth, @relation==}}," +
					"{@type=korap:group, @alignment=left, @operands=[" +
						"{@type=korap:sequence, @operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=große, @attr=orth, @relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=orth, @relation==}}" +
						"]}" +
					"]}" +
				"]}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(align1b.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// "([base=a]^[base=b])|[base=c]",
		String align2 = 
				"{@type=korap:group, @relation=or, @operands=[" +
						"{@type=korap:sequence, @operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=a, @attr=base, @relation==}}," +
							"{@type=korap:group, @alignment=left, @operands=[" +
								"{@type=korap:token, @value={@type=korap:term, @value=b, @attr=base, @relation==}}" +
							"]}" +
						"]}," +
						"{@type=korap:token, @value={@type=korap:term, @value=c, @attr=base, @relation==}}" +
				"]}";
		ppt = new PoliqarpPlusTree("([base=a]^[base=b])|[base=c]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(align2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// "([base=a]^[base=b][base=c])|[base=d]",
		String align3 = 
				"{@type=korap:group, @relation=or, @operands=[" +
						"{@type=korap:sequence, @operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=a, @attr=base, @relation==}}," +
							"{@type=korap:group, @alignment=left, @operands=[" +
								"{@type=korap:sequence, @operands=[" +
									"{@type=korap:token, @value={@type=korap:term, @value=b, @attr=base, @relation==}}," +
									"{@type=korap:token, @value={@type=korap:term, @value=c, @attr=base, @relation==}}" +
								"]}" +
							"]}" +
						"]}," +
						"{@type=korap:token, @value={@type=korap:term, @value=d, @attr=base, @relation==}}" +
				"]}";
		ppt = new PoliqarpPlusTree("([base=a]^[base=b][base=c])|[base=d]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(align3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// "([base=a]^[base=b]^[base=c])|[base=d]",
		String align4 = 
				"{@type=korap:group, @relation=or, @operands=[" +
						"{@type=korap:sequence, @operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=a, @attr=base, @relation==}}," +
							"{@type=korap:group, @alignment=left, @operands=[" +
								"{@type=korap:sequence, @operands=[" +
									"{@type=korap:token, @value={@type=korap:term, @value=b, @attr=base, @relation==}}," +
									"{@type=korap:group, @alignment=left, @operands=[" +
										"{@type=korap:token, @value={@type=korap:term, @value=c, @attr=base, @relation==}}" +
									"]}" +
								"]}" +
							"]}" +
						"]}," +
						"{@type=korap:token, @value={@type=korap:term, @value=d, @attr=base, @relation==}}" +
				"]}";
		ppt = new PoliqarpPlusTree("([base=a]^[base=b]^[base=c])|[base=d]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(align4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		
	}
	
	@Test
	public void testSimpleQueries() throws QueryException {
		// Baum
		String simple1 = 
				"{@type=korap:token, @value={@type=korap:term, @value=Baum, @attr=orth, @relation==}}";
		ppt = new PoliqarpPlusTree("Baum");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(simple1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// Der Baum
		String simple2 = 
				"{@type=korap:sequence, @operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=Der, @attr=orth, @relation==}}, " +
					"{@type=korap:token, @value={@type=korap:term, @value=Baum, @attr=orth, @relation==}}" +
				"]}";
		ppt = new PoliqarpPlusTree("Der Baum");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(simple2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// Der große Baum
		String simple3 = 
				"{@type=korap:sequence, @operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=Der, @attr=orth, @relation==}}, " +
					"{@type=korap:token, @value={@type=korap:term, @value=große, @attr=orth, @relation==}}, " +						
					"{@type=korap:token, @value={@type=korap:term, @value=Baum, @attr=orth, @relation==}}" +
				"]}";
		ppt = new PoliqarpPlusTree("Der große Baum");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(simple3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// Baum | Stein
		String simple4 = 
				"{@type=korap:group, @relation=or, @operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=Baum, @attr=orth, @relation==}}, " +						
					"{@type=korap:token, @value={@type=korap:term, @value=Stein, @attr=orth, @relation==}}" +
				"]}";
		ppt = new PoliqarpPlusTree("Baum | Stein");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(simple4.replaceAll(" ", ""), map.replaceAll(" ", ""));		
		
		// Baum | Stein Haus
		String query = "(Baum | Stein) Haus";
		String simple5 = 
				"{@type=korap:sequence, @operands=[" +
					"{@type=korap:group, @relation=or, @operands=[" +
						"{@type=korap:token, @value={@type=korap:term, @value=Baum, @attr=orth, @relation==}}, " +						
						"{@type=korap:token, @value={@type=korap:term, @value=Stein, @attr=orth, @relation==}}" +
					"]}," +
					"{@type=korap:token, @value={@type=korap:term, @value=Haus, @attr=orth, @relation==}} " +			
				"]}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(simple5.replaceAll(" ", ""), map.replaceAll(" ", ""));		
	}
}

