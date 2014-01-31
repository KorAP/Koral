import static org.junit.Assert.*;

import org.junit.Test;

import de.ids_mannheim.korap.query.serialize.CosmasTree;
import de.ids_mannheim.korap.util.QueryException;

public class CosmasTreeTest {
	
	CosmasTree ppt;
	String map;
	String query;
	
	private boolean equalsContent(String str, Object map) {
		str = str.replaceAll(" ", "");
		String mapStr = map.toString().replaceAll(" ", "");
		return str.equals(mapStr);
	}
	
	private boolean equalsQueryContent(String res, String query) throws QueryException {
		res = res.replaceAll(" ", "");
		ppt = new CosmasTree(query);
		String queryMap = ppt.getRequestMap().get("query").toString().replaceAll(" ", "");
		return res.equals(queryMap);
	}
	
	@Test
	public void testContext() throws QueryException {
		String contextString = "{korap=http://korap.ids-mannheim.de/ns/query, @language=de, @operands={@id=korap:operands, @container=@list}, @relation={@id=korap:relation, @type=korap:relation#types}, class={@id=korap:class, @type=xsd:integer}, query=korap:query, filter=korap:filter, meta=korap:meta}";
		ppt = new CosmasTree("Test");
		assertTrue(equalsContent(contextString, ppt.getRequestMap().get("@context")));
	}
	
	
	@Test
	public void testSingleToken() throws QueryException {
		query="der";
		String single1 = 
					"{@type=korap:token, @value={@type=korap:term, @value=der, @attr=orth, @relation==}}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(single1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="Mann";
		String single2 = 
				"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=orth, @relation==}}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(single2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="&Mann";
		String single3 = 
				"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=lemma, @relation==}}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(single3.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testMORPH() throws QueryException {
		query="#MORPH(V)";
		String morph1 = 
					"{@type=korap:token, @value={@type=korap:term, @value=V, @relation==}}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(morph1.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testSequence() throws QueryException {
		query="der Mann";
		String seq1 = 
				"{@type=korap:sequence, @operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=der, @attr=orth, @relation==}}," +
					"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=orth, @relation==}}" +
				"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(seq1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="der Mann schläft";
		String seq2 = 
				"{@type=korap:sequence, @operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=der, @attr=orth, @relation==}}," +
					"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=orth, @relation==}}," +
					"{@type=korap:token, @value={@type=korap:term, @value=schläft, @attr=orth, @relation==}}" +
				"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(seq2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="der Mann schläft lang";
		String seq3 = 
				"{@type=korap:sequence, @operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=der, @attr=orth, @relation==}}," +
					"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=orth, @relation==}}," +
					"{@type=korap:token, @value={@type=korap:term, @value=schläft, @attr=orth, @relation==}}," +
					"{@type=korap:token, @value={@type=korap:term, @value=lang, @attr=orth, @relation==}}" +
				"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(seq3.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOPOR() throws QueryException {
		query="Sonne oder Mond";
		String disj1 = 
					"{@type=korap:group, @relation=or, @operands=[" +
						"{@type=korap:token, @value={@type=korap:term, @value=Sonne, @attr=orth, @relation==}}," +
						"{@type=korap:token, @value={@type=korap:term, @value=Mond, @attr=orth, @relation==}}" +
					"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(disj1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="(Sonne scheint) oder Mond";
		String disj2 = 
					"{@type=korap:group, @relation=or, @operands=[" +
						"{@type=korap:sequence, @operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=Sonne, @attr=orth, @relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=scheint, @attr=orth, @relation==}}" +
						"]}," +
						"{@type=korap:token, @value={@type=korap:term, @value=Mond, @attr=orth, @relation==}}" +
					"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(disj2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="(Sonne scheint) oder (Mond scheint)";
		String disj3 = 
				"{@type=korap:group, @relation=or, @operands=[" +
						"{@type=korap:sequence, @operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=Sonne, @attr=orth, @relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=scheint, @attr=orth, @relation==}}" +
						"]}," +
						"{@type=korap:sequence, @operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=Mond, @attr=orth, @relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=scheint, @attr=orth, @relation==}}" +
						"]}" +
					"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(disj3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
	}
	
	@Test
	public void testOPORAND() throws QueryException {
		query="(Sonne oder Mond) und scheint";
		String orand1 = 
				"{@type=korap:group, @relation=and, @operands=[" +
					"{@type=korap:group, @relation=or, @operands=[" +
						"{@type=korap:token, @value={@type=korap:term, @value=Sonne, @attr=orth, @relation==}}," +
						"{@type=korap:token, @value={@type=korap:term, @value=Mond, @attr=orth, @relation==}}" +
					"]}," +
					"{@type=korap:token, @value={@type=korap:term, @value=scheint, @attr=orth, @relation==}}" +
				"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(orand1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="scheint und (Sonne oder Mond)";
		String orand2 = 
				"{@type=korap:group, @relation=and, @operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=scheint, @attr=orth, @relation==}}," +
					"{@type=korap:group, @relation=or, @operands=[" +
						"{@type=korap:token, @value={@type=korap:term, @value=Sonne, @attr=orth, @relation==}}," +
						"{@type=korap:token, @value={@type=korap:term, @value=Mond, @attr=orth, @relation==}}" +
					"]}" +
				"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(orand2.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOPPROX() throws QueryException {
		query="Sonne /+w1:4 Mond";
		String prox1 = 
					"{@type=korap:sequence, @inOrder=true, " +
						"@constraints=[" +
							"{@type=korap:distance, @measure=w, @min=1, @max=4}" +
						"], " +
						"@operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=Sonne, @attr=orth, @relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=Mond, @attr=orth, @relation==}}" +
						"]" +
					"}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(prox1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="Sonne /+w1:4,s0,p1:3 Mond";
		String prox2 = 
					"{@type=korap:sequence, @inOrder=true, " +
						"@constraints=[" +
							"{@type=korap:group, @relation=and, @operands=[" +
								"{@type=korap:distance, @measure=w, @min=1, @max=4}," +
								"{@type=korap:distance, @measure=s, @min=0, @max=0}," +
								"{@type=korap:distance, @measure=p, @min=1, @max=3}" +
							"]}" +
						"], " +
						"@operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=Sonne, @attr=orth, @relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=Mond, @attr=orth, @relation==}}" +
						"]" +
					"}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(prox2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="Sonne %+w1:4,s0,p1:3 Mond";
		String prox3 = 
				"{@type=korap:sequence, @inOrder=true, " +
						"@constraints=[" +
							"{@type=korap:group, @relation=and, @operands=[" +
								"{@type=korap:distance, @measure=w, @min=1, @max=4, @exclude=true}," +
								"{@type=korap:distance, @measure=s, @min=0, @max=0, @exclude=true}," +
								"{@type=korap:distance, @measure=p, @min=1, @max=3, @exclude=true}" +
							"]}" +
						"], " +
						"@operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=Sonne, @attr=orth, @relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=Mond, @attr=orth, @relation==}}" +
						"]" +
					"}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(prox3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="Sonne /+w4 Mond";
		String prox4 = 
					"{@type=korap:sequence, @inOrder=true, " +
						"@constraints=[" +
							"{@type=korap:distance, @measure=w, @min=0, @max=4}" +
						"], " +
						"@operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=Sonne, @attr=orth, @relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=Mond, @attr=orth, @relation==}}" +
						"]" +
					"}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(prox4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="Sonne /-w4 Mond";
		String prox5 = 
					"{@type=korap:sequence, @inOrder=true, " +
						"@constraints=[" +
							"{@type=korap:distance, @measure=w, @min=0, @max=4}" +
						"], " +
						"@operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=Mond, @attr=orth, @relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=Sonne, @attr=orth, @relation==}}" +
						"]" +
					"}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(prox5.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOPPROXNested() throws QueryException {	
		query="Sonne /-w4 Mond /+w2 Sterne";
		String prox6 = 
					"{@type=korap:sequence, @inOrder=true, " +
						"@constraints=[" +
							"{@type=korap:distance, @measure=w, @min=0, @max=4}" +
						"], " +
						"@operands=[" +
							"{@type=korap:sequence, @inOrder=true, " +
								"@constraints=[" +
									"{@type=korap:distance, @measure=w, @min=0, @max=2}" +
								"], " +
								"@operands=[" +
									"{@type=korap:token, @value={@type=korap:term, @value=Mond, @attr=orth, @relation==}}," +
									"{@type=korap:token, @value={@type=korap:term, @value=Sterne, @attr=orth, @relation==}}" +
								"]}," +
							"{@type=korap:token, @value={@type=korap:term, @value=Sonne, @attr=orth, @relation==}}" +
						"]" +
					"}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(prox6.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOPIN() throws QueryException {
		query="wegen #IN <s>";
		String opin1 = 
					"{@type=korap:group, @relation=submatch, @classRef=1, @operands=[" +
						"{@type=korap:group, @relation=position, @position=contains, @operands=[" +
							"{@type=korap:span, @value=s}," +
							"{@type=korap:group, class=1, @operands=[" +
								"{@type=korap:token, @value={@type=korap:term, @value=wegen, @attr=orth, @relation==}}" +
							"]}" +
						"]}" +
					"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(opin1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="wegen #IN(L) <s>";
		String opin2 = 
					"{@type=korap:group, @relation=submatch, @classRef=1, @operands=[" +
						"{@type=korap:group, @relation=position, @position=startswith, @operands=[" +
							"{@type=korap:span, @value=s}," +
							"{@type=korap:group, class=1, @operands=[" +
								"{@type=korap:token, @value={@type=korap:term, @value=wegen, @attr=orth, @relation==}}" +
							"]}" +
						"]}" +
					"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(opin2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="wegen #IN(%, L) <s>";
		String opin3 = 
					"{@type=korap:group, @relation=submatch, @classRef=1, @operands=[" +
						"{@type=korap:group, @relation=position, @position=startswith, @exclude=true, @operands=[" +
							"{@type=korap:span, @value=s}," +
							"{@type=korap:group, class=1, @operands=[" +
								"{@type=korap:token, @value={@type=korap:term, @value=wegen, @attr=orth, @relation==}}" +
							"]}" +
						"]}" +
					"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(opin3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="wegen #IN('FE,ALL,%,MIN') <s>";
		String opin4 = 
					"{@type=korap:group, @relation=submatch, @classRef=1, @operands=[" +
						"{@type=korap:group, @relation=position, @position=matches, range=all, @exclude=true, grouping=false, @operands=[" +
							"{@type=korap:span, @value=s}," +
							"{@type=korap:group, class=1, @operands=[" +
								"{@type=korap:token, @value={@type=korap:term, @value=wegen, @attr=orth, @relation==}}" +
							"]}" +
						"]}" +
					"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(opin4.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOPOV() throws QueryException {
		query="wegen #OV <s>";
		String opov1 = 
					"{@type=korap:group, @relation=submatch, @classRef=1, @operands=[" +
						"{@type=korap:group, @relation=position, @position=overlaps, @operands=[" +
							"{@type=korap:span, @value=s}," +
							"{@type=korap:group, class=1, @operands=[" +
								"{@type=korap:token, @value={@type=korap:term, @value=wegen, @attr=orth, @relation==}}" +
							"]}" +
						"]}" +
					"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(opov1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="wegen #OV(L) <s>";
		String opov2 = 
					"{@type=korap:group, @relation=submatch, @classRef=1, @operands=[" +
						"{@type=korap:group, @relation=position, @position=overlaps-left, @operands=[" +
							"{@type=korap:span, @value=s}," +
							"{@type=korap:group, class=1, @operands=[" +
								"{@type=korap:token, @value={@type=korap:term, @value=wegen, @attr=orth, @relation==}}" +
							"]}" +
						"]}" +
					"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(opov2.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOPNOT() throws QueryException {
		query="Sonne nicht Mond";
		String opnot1 = 
					"{@type=korap:group, @relation=not, @operands=[" +
						"{@type=korap:token, @value={@type=korap:term, @value=Sonne, @attr=orth, @relation==}}," +
						"{@type=korap:token, @value={@type=korap:term, @value=Mond, @attr=orth, @relation==}}" +
					"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(opnot1.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testBEG_END() throws QueryException {
		// BEG and END operators
		// http://www.ids-mannheim.de/cosmas2/web-app/hilfe/suchanfrage/eingabe-zeile/syntax/links.html
		// http://www.ids-mannheim.de/cosmas2/web-app/hilfe/suchanfrage/eingabe-zeile/syntax/rechts.html
		// http://www.ids-mannheim.de/cosmas2/web-app/hilfe/suchanfrage/eingabe-zeile/thematische-bsp/bsp-satzlaenge.html
		query="#BEG(der /w3:5 Mann)";
		String beg1 = 
				"{@type=korap:group, @relation=submatch, @spanRef=[0,1], @operands=[" +
					"{@type=korap:sequence, @inOrder=false, @constraints=[" +
						"{@type=korap:distance, @measure=w, @min=3, @max=5}" +
					"]," +
					"@operands = [" +
						"{@type=korap:token, @value={@type=korap:term, @value=der, @attr=orth, @relation==}}," +
						"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=orth, @relation==}}" +
					"]}" +
				"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(beg1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="#BEG(der /w3:5 Mann) /+w10 kommt"; // nesting #BEG() in a distance group
		String beg2 = 
				"{@type=korap:sequence, @inOrder=true, @constraints=[" +
					"{@type=korap:distance, @measure=w, @min=0, @max=10}" +
				"], @operands=[" +
					"{@type=korap:group, @relation=submatch, @spanRef=[0,1], @operands=[" +
						"{@type=korap:sequence, @inOrder=false, @constraints=[" +
							"{@type=korap:distance, @measure=w, @min=3, @max=5}" +
						"]," +
						"@operands = [" +
							"{@type=korap:token, @value={@type=korap:term, @value=der, @attr=orth, @relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=orth, @relation==}}" +
						"]}" +
					"]}," +
					"{@type=korap:token, @value={@type=korap:term, @value=kommt, @attr=orth, @relation==}}" +
				"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(beg2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="#END(der /w3:5 Mann)";
		String end1 = 
				"{@type=korap:group, @relation=submatch, @spanRef=[-1,1], @operands=[" +
					"{@type=korap:sequence, @inOrder=false, @constraints=[" +
						"{@type=korap:distance, @measure=w, @min=3, @max=5}" +
					"], " +
					"@operands = [" +
						"{@type=korap:token, @value={@type=korap:term, @value=der, @attr=orth, @relation==}}," +
						"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=orth, @relation==}}" +
					"]}" +
				"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(end1.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	

	@Test
	public void testELEM() throws QueryException {
		// http://www.ids-mannheim.de/cosmas2/web-app/hilfe/suchanfrage/eingabe-zeile/syntax/elem.html
		query="#ELEM(S)";
		String elem1 = "{@type=korap:span, @value=s}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(elem1.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOPALL() throws QueryException {
		query="#ALL(gehen /w1:10 voran)";
		String all1 =
				"{@type=korap:sequence, @inOrder=false, " +
					"@constraints=[" +
						"{@type=korap:distance, @measure=w, @min=1, @max=10}" +
					"], " +
					"@operands=[" +
						"{@type=korap:token, @value={@type=korap:term, @value=gehen, @attr=orth, @relation==}}," +
						"{@type=korap:token, @value={@type=korap:term, @value=voran, @attr=orth, @relation==}}" +
					"]" +
				"}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(all1.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOPNHIT() throws QueryException {
		query="#NHIT(gehen /w1:10 voran)";
		String nhit1 = 
				"{@type=korap:sequence, @inOrder=false, " +
					"@constraints=[" +
						"{@type=korap:distance, @measure=w, @min=1, @max=10}" +
					"], " +
					"@operands=[" +
						"{@type=korap:token, @value={@type=korap:term, @value=gehen, @attr=orth, @relation==}}," +
						"{@type=korap:token, @value={@type=korap:term, @value=voran, @attr=orth, @relation==}}" +
					"]" +
				"}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(nhit1.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOPBED() throws QueryException {
		query = "#BED(der , sa)";
		String bed1 = 
				"{@type=korap:group, @relation=position, @position=startswith, @operands=[" +
					"{@type=korap:span, @value=s}," +
					"{@type=korap:token, @value={@type=korap:term, @value=der, @attr=orth, @relation==}}" +
				"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(bed1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "#BED(der Mann , +pe)";
		String bed2 = 
				"{@type=korap:group, @relation=position, @position=endswith, @operands=[" +
					"{@type=korap:span, @value=p}," +
					"{@type=korap:sequence, @operands=[" +
						"{@type=korap:token, @value={@type=korap:term, @value=der, @attr=orth, @relation==}}," +
						"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=orth, @relation==}}" +
					"]}" +
				"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(bed2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "#BED(der Mann , sa,-pa)";
		String bed3 = 
				"{@type=korap:group, @relation=and, @operands=[" +
					"{@type=korap:group, @relation=position, @position=startswith, @operands=[" +
						"{@type=korap:span, @value=s}," +
						"{@type=korap:sequence, @operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=der, @attr=orth, @relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=orth, @relation==}}" +
						"]}" +
					"]}," +
					"{@type=korap:group, @relation=position, @position=startswith, @exclude=true, @operands=[" +
						"{@type=korap:span, @value=p}," +
						"{@type=korap:sequence, @operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=der, @attr=orth, @relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=Mann, @attr=orth, @relation==}}" +
						"]}" +
					"]}" +
				"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(bed3.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
}

