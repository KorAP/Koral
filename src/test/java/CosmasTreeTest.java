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
	
//	@Test
	public void testContext() throws QueryException {
		String contextString = "{korap = http://ids-mannheim.de/ns/KorAP/json-ld/v0.1/, " +
							"boundary = korap:boundary/,"+
							"group = korap:group/,"+ 
							"operation = {@id = group:operation/, @type = @id},"+
							"class = {@id = group:class, @type = xsd:integer},"+
							"operands = {@id = group:operands, @container = @list},"+
							"frame = {@id = group:frame/, @type = xsd:integer},"+
							"classRef = {@id = group:classRef, @type = xsd:integer},"+
							"spanRef = {@id = group:spanRef, @type = xsd:integer},"+
							"classRefOp = {@id = group:classRefOp, @type = @id},"+
							"min = {@id = boundary:min, @type = xsd:integer},"+
							"max = {@id = boundary:max, @type = xsd:integer},"+
							"exclude = {@id = group:exclude, @type = xsd:boolean},"+
							"distances = {@id = group:distances, @container = @list},"+
							"inOrder = {@id = group:inOrder, @type = xsd:boolean},"+
							"}";
		ppt = new CosmasTree("Test");
//		assertTrue(equalsContent(contextString, ppt.getRequestMap().get("@context")));
		assertEquals(contextString.replaceAll(" ", ""), ppt.getRequestMap().get("@context"));
	}
	
	
	@Test
	public void testSingleToken() throws QueryException {
		query="der";
		String single1 = 
					"{type=token, key={type=term, key=der, layer=orth, match=eq}}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(single1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="Mann";
		String single2 = 
				"{type=token, key={type=term, key=Mann, layer=orth, match=eq}}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(single2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="&Mann";
		String single3 = 
				"{type=token, key={type=term, key=Mann, layer=lemma, match=eq}}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(single3.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testCaseSensitivityFlag() throws QueryException {
		//TODO ignorieroperator $ http://www.ids-mannheim.de/cosmas2/web-app/hilfe/suchanfrage/eingabe-zeile/syntax/ignorierung.html
	}
	
	@Test
	public void testMORPH() throws QueryException {
		query="#MORPH(V)";
		String morph1 = 
					"{type=token, key={type=term, key=V, match=eq}}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(morph1.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testSequence() throws QueryException {
		query="der Mann";
		String seq1 = 
				"{type=group, operation=sequence, operands=[" +
					"{type=token, key={type=term, key=der, layer=orth, match=eq}}," +
					"{type=token, key={type=term, key=Mann, layer=orth, match=eq}}" +
				"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(seq1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="der Mann schläft";
		String seq2 = 
				"{type=group, operation=sequence, operands=[" +
					"{type=token, key={type=term, key=der, layer=orth, match=eq}}," +
					"{type=token, key={type=term, key=Mann, layer=orth, match=eq}}," +
					"{type=token, key={type=term, key=schläft, layer=orth, match=eq}}" +
				"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(seq2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="der Mann schläft lang";
		String seq3 = 
				"{type=group, operation=sequence, operands=[" +
					"{type=token, key={type=term, key=der, layer=orth, match=eq}}," +
					"{type=token, key={type=term, key=Mann, layer=orth, match=eq}}," +
					"{type=token, key={type=term, key=schläft, layer=orth, match=eq}}," +
					"{type=token, key={type=term, key=lang, layer=orth, match=eq}}" +
				"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(seq3.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOPOR() throws QueryException {
		query="Sonne oder Mond";
		String disj1 = 
					"{type=group, operation=or, operands=[" +
						"{type=token, key={type=term, key=Sonne, layer=orth, match=eq}}," +
						"{type=token, key={type=term, key=Mond, layer=orth, match=eq}}" +
					"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(disj1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="(Sonne scheint) oder Mond";
		String disj2 = 
					"{type=group, operation=or, operands=[" +
						"{type=group, operation=sequence, operands=[" +
							"{type=token, key={type=term, key=Sonne, layer=orth, match=eq}}," +
							"{type=token, key={type=term, key=scheint, layer=orth, match=eq}}" +
						"]}," +
						"{type=token, key={type=term, key=Mond, layer=orth, match=eq}}" +
					"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(disj2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="(Sonne scheint) oder (Mond scheint)";
		String disj3 = 
				"{type=group, operation=or, operands=[" +
						"{type=group, operation=sequence, operands=[" +
							"{type=token, key={type=term, key=Sonne, layer=orth, match=eq}}," +
							"{type=token, key={type=term, key=scheint, layer=orth, match=eq}}" +
						"]}," +
						"{type=group, operation=sequence, operands=[" +
							"{type=token, key={type=term, key=Mond, layer=orth, match=eq}}," +
							"{type=token, key={type=term, key=scheint, layer=orth, match=eq}}" +
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
				"{type=group, operation=and, operands=[" +
					"{type=group, operation=or, operands=[" +
						"{type=token, key={type=term, key=Sonne, layer=orth, match=eq}}," +
						"{type=token, key={type=term, key=Mond, layer=orth, match=eq}}" +
					"]}," +
					"{type=token, key={type=term, key=scheint, layer=orth, match=eq}}" +
				"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(orand1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="scheint und (Sonne oder Mond)";
		String orand2 = 
				"{type=group, operation=and, operands=[" +
					"{type=token, key={type=term, key=scheint, layer=orth, match=eq}}," +
					"{type=group, operation=or, operands=[" +
						"{type=token, key={type=term, key=Sonne, layer=orth, match=eq}}," +
						"{type=token, key={type=term, key=Mond, layer=orth, match=eq}}" +
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
					"{type=group, operation=sequence, inOrder=true, " +
						"distances=[" +
							"{type=distance, measure=w, min=1, max=4}" +
						"], " +
						"operands=[" +
							"{type=token, key={type=term, key=Sonne, layer=orth, match=eq}}," +
							"{type=token, key={type=term, key=Mond, layer=orth, match=eq}}" +
						"]" +
					"}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(prox1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="Sonne /+w1:4,s0,p1:3 Mond";
		String prox2 = 
					"{type=group, operation=sequence, inOrder=true, " +
						"distances=[" +
							"{type=group, operation=and, operands=[" +
								"{type=distance, measure=w, min=1, max=4}," +
								"{type=distance, measure=s, min=0, max=0}," +
								"{type=distance, measure=p, min=1, max=3}" +
							"]}" +
						"], " +
						"operands=[" +
							"{type=token, key={type=term, key=Sonne, layer=orth, match=eq}}," +
							"{type=token, key={type=term, key=Mond, layer=orth, match=eq}}" +
						"]" +
					"}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(prox2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="Sonne %+w1:4,s0,p1:3 Mond";
		String prox3 = 
				"{type=group, operation=sequence, inOrder=true, " +
						"distances=[" +
							"{type=group, operation=and, operands=[" +
								"{type=distance, measure=w, min=1, max=4, exclude=true}," +
								"{type=distance, measure=s, min=0, max=0, exclude=true}," +
								"{type=distance, measure=p, min=1, max=3, exclude=true}" +
							"]}" +
						"], " +
						"operands=[" +
							"{type=token, key={type=term, key=Sonne, layer=orth, match=eq}}," +
							"{type=token, key={type=term, key=Mond, layer=orth, match=eq}}" +
						"]" +
					"}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(prox3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="Sonne /+w4 Mond";
		String prox4 = 
					"{type=group, operation=sequence, inOrder=true, " +
						"distances=[" +
							"{type=distance, measure=w, min=0, max=4}" +
						"], " +
						"operands=[" +
							"{type=token, key={type=term, key=Sonne, layer=orth, match=eq}}," +
							"{type=token, key={type=term, key=Mond, layer=orth, match=eq}}" +
						"]" +
					"}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(prox4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="Sonne /-w4 Mond";
		String prox5 = 
					"{type=group, operation=sequence, inOrder=true, " +
						"distances=[" +
							"{type=distance, measure=w, min=0, max=4}" +
						"], " +
						"operands=[" +
							"{type=token, key={type=term, key=Mond, layer=orth, match=eq}}," +
							"{type=token, key={type=term, key=Sonne, layer=orth, match=eq}}" +
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
					"{type=group, operation=sequence, inOrder=true, " +
						"distances=[" +
							"{type=distance, measure=w, min=0, max=4}" +
						"], " +
						"operands=[" +
							"{type=group, operation=sequence, inOrder=true, " +
								"distances=[" +
									"{type=distance, measure=w, min=0, max=2}" +
								"], " +
								"operands=[" +
									"{type=token, key={type=term, key=Mond, layer=orth, match=eq}}," +
									"{type=token, key={type=term, key=Sterne, layer=orth, match=eq}}" +
								"]}," +
							"{type=token, key={type=term, key=Sonne, layer=orth, match=eq}}" +
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
					"{type=group, operation=submatch, classRef=1, operands=[" +
						"{type=group, operation=position, frame=contains, operands=[" +
							"{type=span, key=s}," +
							"{type=group, operation=class, class=1, operands=[" +
								"{type=token, key={type=term, key=wegen, layer=orth, match=eq}}" +
							"]}" +
						"]}" +
					"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(opin1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="wegen #IN(L) <s>";
		String opin2 = 
					"{type=group, operation=submatch, classRef=1, operands=[" +
						"{type=group, operation=position, frame=startswith, operands=[" +
							"{type=span, key=s}," +
							"{type=group, operation=class, class=1, operands=[" +
								"{type=token, key={type=term, key=wegen, layer=orth, match=eq}}" +
							"]}" +
						"]}" +
					"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(opin2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="wegen #IN(%, L) <s>";
		String opin3 = 
					"{type=group, operation=submatch, classRef=1, operands=[" +
						"{type=group, operation=position, frame=startswith, exclude=true, operands=[" +
							"{type=span, key=s}," +
							"{type=group, operation=class, class=1, operands=[" +
								"{type=token, key={type=term, key=wegen, layer=orth, match=eq}}" +
							"]}" +
						"]}" +
					"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(opin3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="wegen #IN('FE,ALL,%,MIN') <s>";
		String opin4 = 
					"{type=group, operation=submatch, classRef=1, operands=[" +
						"{type=group, operation=position, frame=matches, range=all, exclude=true, grouping=false, operands=[" +
							"{type=span, key=s}," +
							"{type=group, operation=class, class=1, operands=[" +
								"{type=token, key={type=term, key=wegen, layer=orth, match=eq}}" +
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
					"{type=group, operation=submatch, classRef=1, operands=[" +
						"{type=group, operation=position, frame=overlaps, operands=[" +
							"{type=span, key=s}," +
							"{type=group, operation=class, class=1, operands=[" +
								"{type=token, key={type=term, key=wegen, layer=orth, match=eq}}" +
							"]}" +
						"]}" +
					"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(opov1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="wegen #OV(L) <s>";
		String opov2 = 
					"{type=group, operation=submatch, classRef=1, operands=[" +
						"{type=group, operation=position, frame=overlaps-left, operands=[" +
							"{type=span, key=s}," +
							"{type=group, operation=class, class=1, operands=[" +
								"{type=token, key={type=term, key=wegen, layer=orth, match=eq}}" +
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
					"{type=group, operation=not, operands=[" +
						"{type=token, key={type=term, key=Sonne, layer=orth, match=eq}}," +
						"{type=token, key={type=term, key=Mond, layer=orth, match=eq}}" +
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
				"{type=group, operation=submatch, @spanRef=[0,1], operands=[" +
					"{type=group, operation=sequence, inOrder=false, distances=[" +
						"{type=distance, measure=w, min=3, max=5}" +
					"]," +
					"operands = [" +
						"{type=token, key={type=term, key=der, layer=orth, match=eq}}," +
						"{type=token, key={type=term, key=Mann, layer=orth, match=eq}}" +
					"]}" +
				"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(beg1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="#BEG(der /w3:5 Mann) /+w10 kommt"; // nesting #BEG() in a distance group
		String beg2 = 
				"{type=group, operation=sequence, inOrder=true, distances=[" +
					"{type=distance, measure=w, min=0, max=10}" +
				"], operands=[" +
					"{type=group, operation=submatch, @spanRef=[0,1], operands=[" +
						"{type=group, operation=sequence, inOrder=false, distances=[" +
							"{type=distance, measure=w, min=3, max=5}" +
						"]," +
						"operands = [" +
							"{type=token, key={type=term, key=der, layer=orth, match=eq}}," +
							"{type=token, key={type=term, key=Mann, layer=orth, match=eq}}" +
						"]}" +
					"]}," +
					"{type=token, key={type=term, key=kommt, layer=orth, match=eq}}" +
				"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(beg2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="#END(der /w3:5 Mann)";
		String end1 = 
				"{type=group, operation=submatch, @spanRef=[-1,1], operands=[" +
					"{type=group, operation=sequence, inOrder=false, distances=[" +
						"{type=distance, measure=w, min=3, max=5}" +
					"], " +
					"operands = [" +
						"{type=token, key={type=term, key=der, layer=orth, match=eq}}," +
						"{type=token, key={type=term, key=Mann, layer=orth, match=eq}}" +
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
		String elem1 = "{type=span, key=s}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(elem1.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOPALL() throws QueryException {
		query="#ALL(gehen /w1:10 voran)";
		String all1 =
				"{type=group, operation=sequence, inOrder=false, " +
					"distances=[" +
						"{type=distance, measure=w, min=1, max=10}" +
					"], " +
					"operands=[" +
						"{type=token, key={type=term, key=gehen, layer=orth, match=eq}}," +
						"{type=token, key={type=term, key=voran, layer=orth, match=eq}}" +
					"]" +
				"}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(all1.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
//	@Test
	public void testOPNHIT() throws QueryException {
		query="#NHIT(gehen /w1:10 voran)";
		String nhit1 = 
				"{type=group, operation=sequence, inOrder=false, " +
					"distances=[" +
						"{type=distance, measure=w, min=1, max=10}" +
					"], " +
					"operands=[" +
						"{type=token, key={type=term, key=gehen, layer=orth, match=eq}}," +
						"{type=token, key={type=term, key=voran, layer=orth, match=eq}}" +
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
				"{type=group, operation=position, frame=startswith, operands=[" +
					"{type=span, key=s}," +
					"{type=token, key={type=term, key=der, layer=orth, match=eq}}" +
				"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(bed1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "#BED(der Mann , +pe)";
		String bed2 = 
				"{type=group, operation=position, frame=endswith, operands=[" +
					"{type=span, key=p}," +
					"{type=group, operation=sequence, operands=[" +
						"{type=token, key={type=term, key=der, layer=orth, match=eq}}," +
						"{type=token, key={type=term, key=Mann, layer=orth, match=eq}}" +
					"]}" +
				"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(bed2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "#BED(der Mann , sa,-pa)";
		String bed3 = 
				"{type=group, operation=and, operands=[" +
					"{type=group, operation=position, frame=startswith, operands=[" +
						"{type=span, key=s}," +
						"{type=group, operation=sequence, operands=[" +
							"{type=token, key={type=term, key=der, layer=orth, match=eq}}," +
							"{type=token, key={type=term, key=Mann, layer=orth, match=eq}}" +
						"]}" +
					"]}," +
					"{type=group, operation=position, frame=startswith, exclude=true, operands=[" +
						"{type=span, key=p}," +
						"{type=group, operation=sequence, operands=[" +
							"{type=token, key={type=term, key=der, layer=orth, match=eq}}," +
							"{type=token, key={type=term, key=Mann, layer=orth, match=eq}}" +
						"]}" +
					"]}" +
				"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(bed3.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
}

