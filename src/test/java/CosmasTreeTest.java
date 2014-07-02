import static org.junit.Assert.*;

import org.junit.Test;

import de.ids_mannheim.korap.query.serialize.CosmasTree;
import de.ids_mannheim.korap.util.QueryException;

public class CosmasTreeTest {
	
	CosmasTree ct;
	String map;
	String query;
	
	
	@Test
	public void testContext() throws QueryException {
		String contextString = "http://ids-mannheim.de/ns/KorAP/json-ld/v0.1/context.jsonld";
		ct = new CosmasTree("Test");
		assertEquals(contextString.replaceAll(" ", ""), ct.getRequestMap().get("@context").toString().replaceAll(" ", ""));
	}
	
	
	@Test
	public void testSingleToken() throws QueryException {
		query="der";
		String single1 = 
					"{@type=korap:token, wrap={@type=korap:term, key=der, layer=orth, match=match:eq}}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(single1.replaceAll(" ", ""), map.replaceAll(" ", ""));

		query="Mann";
		String single2 = 
				"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(single2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="&Mann";
		String single3 = 
				"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=lemma, match=match:eq}}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(single3.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testCaseSensitivityFlag() throws QueryException {
		query="$deutscher";
		String cs1 = 
				"{@type=korap:token, wrap={@type=korap:term, caseInsensitive=true, key=deutscher, layer=orth, match=match:eq}}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(cs1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="$deutscher Bundestag";
		String cs2 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, caseInsensitive=true, key=deutscher, layer=orth, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, key=Bundestag, layer=orth, match=match:eq}}" +
					"]}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(cs2.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testMORPH() throws QueryException {
		query="#MORPH(V)";
		String morph1 = 
					"{@type=korap:token, wrap={@type=korap:term, key=V, layer=pos, match=match:eq}}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(morph1.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testSequence() throws QueryException {
		query="der Mann";
		String seq1 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, key=der, layer=orth, match=match:eq}}," +
					"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}" +
				"]}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(seq1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="der Mann schläft";
		String seq2 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, key=der, layer=orth, match=match:eq}}," +
					"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}," +
					"{@type=korap:token, wrap={@type=korap:term, key=schläft, layer=orth, match=match:eq}}" +
				"]}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(seq2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="der Mann schläft lang";
		String seq3 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, key=der, layer=orth, match=match:eq}}," +
					"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}," +
					"{@type=korap:token, wrap={@type=korap:term, key=schläft, layer=orth, match=match:eq}}," +
					"{@type=korap:token, wrap={@type=korap:term, key=lang, layer=orth, match=match:eq}}" +
				"]}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(seq3.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOPOR() throws QueryException {
		query="Sonne oder Mond";
		String disj1 = 
					"{@type=korap:group, operation=operation:or, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=Sonne, layer=orth, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, key=Mond, layer=orth, match=match:eq}}" +
					"]}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(disj1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="(Sonne scheint) oder Mond";
		String disj2 = 
					"{@type=korap:group, operation=operation:or, operands=[" +
						"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, key=Sonne, layer=orth, match=match:eq}}," +
							"{@type=korap:token, wrap={@type=korap:term, key=scheint, layer=orth, match=match:eq}}" +
						"]}," +
						"{@type=korap:token, wrap={@type=korap:term, key=Mond, layer=orth, match=match:eq}}" +
					"]}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(disj2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="(Sonne scheint) oder (Mond scheint)";
		String disj3 = 
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
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(disj3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
	}
	
	@Test
	public void testOPORAND() throws QueryException {
		query="(Sonne oder Mond) und scheint";
		String orand1 = 
				"{@type=korap:group, operation=operation:sequence, distances=[" +
					"{@type=korap:distance, key=t, min=0, max=0}" +
					"], operands=[" +
						"{@type=korap:group, operation=operation:or, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, key=Sonne, layer=orth, match=match:eq}}," +
							"{@type=korap:token, wrap={@type=korap:term, key=Mond, layer=orth, match=match:eq}}" +
						"]}," +
						"{@type=korap:token, wrap={@type=korap:term, key=scheint, layer=orth, match=match:eq}}" +
				"]}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(orand1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="scheint und (Sonne oder Mond)";
		String orand2 = 
				"{@type=korap:group, operation=operation:sequence, distances=[" +
						"{@type=korap:distance, key=t, min=0, max=0}" +
					"], operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=scheint, layer=orth, match=match:eq}}," +
						"{@type=korap:group, operation=operation:or, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, key=Sonne, layer=orth, match=match:eq}}," +
							"{@type=korap:token, wrap={@type=korap:term, key=Mond, layer=orth, match=match:eq}}" +
					"]}" +
				"]}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(orand2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="Regen und scheint und (Sonne oder Mond)";
		String orand3 = 
				"{@type=korap:group, operation=operation:sequence, distances=[" +
						"{@type=korap:distance, key=t, min=0, max=0}" +
				"], operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, key=Regen, layer=orth, match=match:eq}}," +
					"{@type=korap:group, operation=operation:sequence, distances=[" +
							"{@type=korap:distance, key=t, min=0, max=0}" +
						"], operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, key=scheint, layer=orth, match=match:eq}}," +
							"{@type=korap:group, operation=operation:or, operands=[" +
								"{@type=korap:token, wrap={@type=korap:term, key=Sonne, layer=orth, match=match:eq}}," +
								"{@type=korap:token, wrap={@type=korap:term, key=Mond, layer=orth, match=match:eq}}" +
						"]}" +
					"]}" +
				"]}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(orand3.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOPPROX() throws QueryException {
		query="Sonne /+w1:4 Mond";
		String prox1 = 
					"{@type=korap:group, operation=operation:sequence, inOrder=true, " +
						"distances=[" +
							"{@type=korap:distance, key=w, min=1, max=4}" +
						"], " +
						"operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, key=Sonne, layer=orth, match=match:eq}}," +
							"{@type=korap:token, wrap={@type=korap:term, key=Mond, layer=orth, match=match:eq}}" +
						"]" +
					"}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(prox1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="Sonne /+w1:4,s0,p1:3 Mond";
		String prox2 = 
					"{@type=korap:group, operation=operation:sequence, inOrder=true, " +
						"distances=[" +
							"{@type=korap:distance, key=w, min=1, max=4}," +
							"{@type=korap:distance, key=s, min=0, max=0}," +
							"{@type=korap:distance, key=p, min=1, max=3}" +
						"], " +
						"operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, key=Sonne, layer=orth, match=match:eq}}," +
							"{@type=korap:token, wrap={@type=korap:term, key=Mond, layer=orth, match=match:eq}}" +
						"]" +
					"}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(prox2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="Sonne %+w1:4,s0,p1:3 Mond";
		String prox3 = 
				"{@type=korap:group, operation=operation:sequence, inOrder=true, " +
						"distances=[" +
							"{@type=korap:distance, key=w, min=1, max=4, exclude=true}," +
							"{@type=korap:distance, key=s, min=0, max=0, exclude=true}," +
							"{@type=korap:distance, key=p, min=1, max=3, exclude=true}" +
						"], " +
						"operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, key=Sonne, layer=orth, match=match:eq}}," +
							"{@type=korap:token, wrap={@type=korap:term, key=Mond, layer=orth, match=match:eq}}" +
						"]" +
					"}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(prox3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="Sonne /+w4 Mond";
		String prox4 = 
					"{@type=korap:group, operation=operation:sequence, inOrder=true, " +
						"distances=[" +
							"{@type=korap:distance, key=w, min=0, max=4}" +
						"], " +
						"operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, key=Sonne, layer=orth, match=match:eq}}," +
							"{@type=korap:token, wrap={@type=korap:term, key=Mond, layer=orth, match=match:eq}}" +
						"]" +
					"}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(prox4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="Sonne /-w4 Mond";
		String prox5 = 
					"{@type=korap:group, operation=operation:sequence, inOrder=true, " +
						"distances=[" +
							"{@type=korap:distance, key=w, min=0, max=4}" +
						"], " +
						"operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, key=Mond, layer=orth, match=match:eq}}," +
							"{@type=korap:token, wrap={@type=korap:term, key=Sonne, layer=orth, match=match:eq}}" +
						"]" +
					"}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(prox5.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOPPROXNested() throws QueryException {	
		query="Sonne /-w4 Mond /+w2 Sterne";
		String prox6 = 
					"{@type=korap:group, operation=operation:sequence, inOrder=true, " +
						"distances=[" +
							"{@type=korap:distance, key=w, min=0, max=4}" +
						"], " +
						"operands=[" +
							"{@type=korap:group, operation=operation:sequence, inOrder=true, " +
								"distances=[" +
									"{@type=korap:distance, key=w, min=0, max=2}" +
								"], " +
								"operands=[" +
									"{@type=korap:token, wrap={@type=korap:term, key=Mond, layer=orth, match=match:eq}}," +
									"{@type=korap:token, wrap={@type=korap:term, key=Sterne, layer=orth, match=match:eq}}" +
								"]}," +
							"{@type=korap:token, wrap={@type=korap:term, key=Sonne, layer=orth, match=match:eq}}" +
						"]" +
					"}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(prox6.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOPIN() throws QueryException {
		query="wegen #IN <s>";
		String opin1 = 
					"{@type=korap:reference, operation=operation:focus, classRef=[1], operands=[" +
						"{@type=korap:group, operation=operation:position, frame=frame:contains, operands=[" +
							"{@type=korap:span, key=s}," +
							"{@type=korap:group, operation=operation:class, class=1, operands=[" +
								"{@type=korap:token, wrap={@type=korap:term, key=wegen, layer=orth, match=match:eq}}" +
							"]}" +
						"]}" +
					"]}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(opin1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="wegen #IN(L) <s>";
		String opin2 = 
					"{@type=korap:reference, operation=operation:focus, classRef=[1], operands=[" +
						"{@type=korap:group, operation=operation:position, frame=frame:startswith, operands=[" +
							"{@type=korap:span, key=s}," +
							"{@type=korap:group, operation=operation:class, class=1, operands=[" +
								"{@type=korap:token, wrap={@type=korap:term, key=wegen, layer=orth, match=match:eq}}" +
							"]}" +
						"]}" +
					"]}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(opin2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="wegen #IN(%, L) <s>";
		String opin3 = 
					"{@type=korap:reference, operation=operation:focus, classRef=[1], operands=[" +
						"{@type=korap:group, operation=operation:position, frame=frame:startswith, operands=[" +
							"{@type=korap:span, key=s}," +
							"{@type=korap:group, operation=operation:class, class=1, operands=[" +
								"{@type=korap:token, wrap={@type=korap:term, key=wegen, layer=orth, match=match:eq}}" +
							"]}" +
						"], exclude=true}" +
					"]}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(opin3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="wegen #IN('FE,ALL,%,MIN') <s>";
		String opin4 = 
					"{@type=korap:reference, operation=operation:focus, classRef=[1], operands=[" +
						"{@type=korap:group, operation=operation:position, frame=frame:matches, operands=[" +
							"{@type=korap:span, key=s}," +
							"{@type=korap:group, operation=operation:class, class=1, operands=[" +
								"{@type=korap:token, wrap={@type=korap:term, key=wegen, layer=orth, match=match:eq}}" +
							"]}" +
						"], range=all, exclude=true, grouping=false}" +
					"]}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(opin4.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOPOV() throws QueryException {
		query="wegen #OV <s>";
		String opov1 = 
					"{@type=korap:reference, operation=operation:focus, classRef=[1], operands=[" +
						"{@type=korap:group, operation=operation:position, frame=frame:overlaps, operands=[" +
							"{@type=korap:span, key=s}," +
							"{@type=korap:group, operation=operation:class, class=1, operands=[" +
								"{@type=korap:token, wrap={@type=korap:term, key=wegen, layer=orth, match=match:eq}}" +
							"]}" +
						"]}" +
					"]}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(opov1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="wegen #OV(L) <s>";
		String opov2 = 
					"{@type=korap:reference, operation=operation:focus, classRef=[1], operands=[" +
						"{@type=korap:group, operation=operation:position, frame=frame:overlaps-left, operands=[" +
							"{@type=korap:span, key=s}," +
							"{@type=korap:group, operation=operation:class, class=1, operands=[" +
								"{@type=korap:token, wrap={@type=korap:term, key=wegen, layer=orth, match=match:eq}}" +
							"]}" +
						"]}" +
					"]}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(opov2.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOPNOT() throws QueryException {
		query="Sonne nicht Mond";
		String opnot1 = 
					"{@type=korap:group, operation=operation:sequence, distances=[" +
						"{@type=korap:distance, key=t, min=0, max=0, exclude=true}" +
					"], operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=Sonne, layer=orth, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, key=Mond, layer=orth, match=match:eq}}" +
					"]}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
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
				"{@type=korap:reference, operation=operation:focus, spanRef=[0,1], operands=[" +
					"{@type=korap:group, operation=operation:sequence, inOrder=false, distances=[" +
						"{@type=korap:distance, key=w, min=3, max=5}" +
					"]," +
					"operands = [" +
						"{@type=korap:token, wrap={@type=korap:term, key=der, layer=orth, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}" +
					"]}" +
				"]}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(beg1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="#BEG(der /w3:5 Mann) /+w10 kommt"; // nesting #BEG() in a distance group
		String beg2 = 
				"{@type=korap:group, operation=operation:sequence, inOrder=true, distances=[" +
					"{@type=korap:distance, key=w, min=0, max=10}" +
				"], operands=[" +
					"{@type=korap:reference, operation=operation:focus, spanRef=[0,1], operands=[" +
						"{@type=korap:group, operation=operation:sequence, inOrder=false, distances=[" +
							"{@type=korap:distance, key=w, min=3, max=5}" +
						"]," +
						"operands = [" +
							"{@type=korap:token, wrap={@type=korap:term, key=der, layer=orth, match=match:eq}}," +
							"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}" +
						"]}" +
					"]}," +
					"{@type=korap:token, wrap={@type=korap:term, key=kommt, layer=orth, match=match:eq}}" +
				"]}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(beg2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="#END(der /w3:5 Mann)";
		String end1 = 
				"{@type=korap:reference, operation=operation:focus, spanRef=[-1,1], operands=[" +
					"{@type=korap:group, operation=operation:sequence, inOrder=false, distances=[" +
						"{@type=korap:distance, key=w, min=3, max=5}" +
					"], " +
					"operands = [" +
						"{@type=korap:token, wrap={@type=korap:term, key=der, layer=orth, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}" +
					"]}" +
				"]}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(end1.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	

	@Test
	public void testELEM() throws QueryException {
		// http://www.ids-mannheim.de/cosmas2/web-app/hilfe/suchanfrage/eingabe-zeile/syntax/elem.html
		query="#ELEM(S)";
		String elem1 = "{@type=korap:span, key=s}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(elem1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="#ELEM(W ANA=N)";
		String elem2 = 
			"{@type=korap:span, key=w, attr=" +
				"{@type=korap:termGroup, relation=relation:and, operands=[" +
					"{@type=korap:term, layer=pos, key=N, match=match:eq}" +
				"]}" +
			"}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(elem2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="#ELEM(W ANA != 'N V')";
		String elem3 = 
			"{@type=korap:span, key=w, attr=" +
				"{@type=korap:termGroup, relation=relation:and, operands=[" +
					"{@type=korap:term, layer=pos, key=N, match=match:ne}," +
					"{@type=korap:term, layer=pos, key=V, match=match:ne}" +
				"]}" +
			"}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(elem3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="#ELEM(W ANA != 'N A V' Genre = Sport)";
		String elem4 = 
			"{@type=korap:span, key=w, attr=" +
				"{@type=korap:termGroup, relation=relation:and, operands=[" +
					"{@type=korap:termGroup, relation=relation:and, operands=[" +
						"{@type=korap:term, layer=pos, key=N, match=match:ne}," +
						"{@type=korap:term, layer=pos, key=A, match=match:ne}," +
						"{@type=korap:term, layer=pos, key=V, match=match:ne}" +
					"]}," +
					"{@type=korap:term, layer=Genre, key=Sport, match=match:eq}" +
				"]}" +
			"}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(elem4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="#ELEM(W ANA != 'N V' Genre != 'Sport Politik')";
		String elem5 = 
			"{@type=korap:span, key=w, attr=" +
				"{@type=korap:termGroup, relation=relation:and, operands=[" +
					"{@type=korap:termGroup, relation=relation:and, operands=[" +
						"{@type=korap:term, layer=pos, key=N, match=match:ne}," +
						"{@type=korap:term, layer=pos, key=V, match=match:ne}" +
					"]}," +
					"{@type=korap:termGroup, relation=relation:and, operands=[" +
						"{@type=korap:term, layer=Genre, key=Sport, match=match:ne}," +
						"{@type=korap:term, layer=Genre, key=Politik, match=match:ne}" +
					"]}" +
				"]}" +
			"}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(elem5.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOPALL() throws QueryException {
		query="#ALL(gehen /w1:10 voran)";
		String all1 =
				"{@type=korap:group, operation=operation:sequence, inOrder=false, " +
					"distances=[" +
						"{@type=korap:distance, key=w, min=1, max=10}" +
					"], " +
					"operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, key=gehen, layer=orth, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, key=voran, layer=orth, match=match:eq}}" +
					"]" +
				"}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(all1.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOPNHIT() throws QueryException {
		query="#NHIT(gehen /w1:10 voran)";
		String nhit1 = 
				"{@type=korap:reference, operation=operation:focus, classRef=[1,2], classRefOp=classRefOp:intersection, operands=[" +
					"{@type=korap:group, operation=operation:sequence, inOrder=false, " +
						"distances=[" +
							"{@type=korap:distance, key=w, min=1, max=10}" +
						"], " +
						"operands=[" +
							"{@type=korap:group, operation=operation:class, class=1, operands=[" +
								"{@type=korap:token, wrap={@type=korap:term, key=gehen, layer=orth, match=match:eq}}" +
							"]}," +
							"{@type=korap:group, operation=operation:class, class=2, operands=[" +
								"{@type=korap:token, wrap={@type=korap:term, key=voran, layer=orth, match=match:eq}}" +
							"]}" +
						"]" +
					"}" +
				"]}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(nhit1.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOPBED() throws QueryException {
		query = "#BED(der , sa)";
		String bed1 = 
				"{@type=korap:reference, operation=operation:focus, classRef=[1], operands= [" +
					"{@type=korap:group, operation=operation:position, frame=frame:startswith, operands=[" +
						"{@type=korap:span, key=s}," +
						"{@type=korap:group, operation=operation:class, class=1, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, key=der, layer=orth, match=match:eq}}" +
						"]}" +
					"]}" +
				"]}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(bed1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "#BED(der Mann , +pe)";
		String bed2 = 
				"{@type=korap:reference, operation=operation:focus, classRef=[1], operands= [" +
						"{@type=korap:group, operation=operation:position, frame=frame:endswith, operands=[" +
							"{@type=korap:span, key=p}," +
							"{@type=korap:group, operation=operation:class, class=1, operands=[" +
								"{@type=korap:group, operation=operation:sequence, operands=[" +
									"{@type=korap:token, wrap={@type=korap:term, key=der, layer=orth, match=match:eq}}," +
									"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}" +
								"]}" +
							"]}" +
						"]}" +
					"]}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(bed2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "#BED(der Mann , sa,-pa)";
		String bed3 = 
				"{@type=korap:reference, operation=operation:focus, classRef=[1], operands=[" +
					"{@type=korap:group, operation=operation:sequence, distances=[" +
						"{@type=korap:distance, key=w, min=0, max=0}" +
					"], operands=[" +
						"{@type=korap:group, operation=operation:position, frame=frame:startswith, operands=[" +
							"{@type=korap:span, key=s}," +
							"{@type=korap:group, operation=operation:class, class=1, operands=[" +
								"{@type=korap:group, operation=operation:sequence, operands=[" +
									"{@type=korap:token, wrap={@type=korap:term, key=der, layer=orth, match=match:eq}}," +
									"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}" +
								"]}" +
							"]}" +
						"]}," +
						"{@type=korap:group, operation=operation:position, frame=frame:startswith, exclude=true, operands=[" +
							"{@type=korap:span, key=p}," +
							"{@type=korap:group, operation=operation:class, class=2, operands=[" +
								"{@type=korap:group, operation=operation:sequence, operands=[" +
									"{@type=korap:token, wrap={@type=korap:term, key=der, layer=orth, match=match:eq}}," +
									"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}" +
								"]}" +
							"]}" +
						"]}" +
					"]}" +
				"]}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(bed3.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testColonSeparatedConditions() throws QueryException {
		
		query = "Der:sa";
		String col1 = 
				"{@type=korap:reference, operation=operation:focus, classRef=[1], operands=[" +
					"{@type=korap:group, operation=operation:position, frame=frame:startswith, operands=[" +
						"{@type=korap:span, key=s}," +
						"{@type=korap:group, operation=operation:class, class=1, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, key=Der, layer=orth, match=match:eq}}" +
						"]}" +
					"]}" +
				"]}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(col1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "Mann:sa,-pa,+te)";
		String col2 = 
				"{@type=korap:reference, operation=operation:focus, classRef=[1], operands=[" +
					"{@type=korap:group, operation=operation:sequence, distances=[" +
						"{@type=korap:distance, key=w, min=0, max=0}" +
					"], operands=[" +
						"{@type=korap:group, operation=operation:position, frame=frame:startswith, operands=[" +
							"{@type=korap:span, key=s}," +
							"{@type=korap:group, operation=operation:class, class=1, operands=[" +
									"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}" +
							"]}" +
						"]}," +
						"{@type=korap:group, operation=operation:position, frame=frame:startswith, exclude=true, operands=[" +
							"{@type=korap:span, key=p}," +
							"{@type=korap:group, operation=operation:class, class=2, operands=[" +
									"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}" +
							"]}" +
						"]}," +
						"{@type=korap:group, operation=operation:position, frame=frame:endswith, operands=[" +
							"{@type=korap:span, key=t}," +
							"{@type=korap:group, operation=operation:class, class=3, operands=[" +
									"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}" +
						"]}" +
					"]}" +
					"]}" +
				"]}";
		ct = new CosmasTree(query);
		map = ct.getRequestMap().get("query").toString();
		assertEquals(col2.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
}

