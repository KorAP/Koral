import static org.junit.Assert.*;

import org.junit.Test;

import de.ids_mannheim.korap.query.serialize.CosmasTree;
import de.ids_mannheim.korap.query.serialize.PoliqarpPlusTree;
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
		String contextString = "{korap=http://korap.ids-mannheim.de/ns/query, @language=de, operands={@id=korap:operands, @container=@list}, relation={@id=korap:relation, @type=korap:relation#types}, class={@id=korap:class, @type=xsd:integer}, query=korap:query, filter=korap:filter, meta=korap:meta}";
		ppt = new CosmasTree("Test");
		assertTrue(equalsContent(contextString, ppt.getRequestMap().get("@context")));
	}
	
	
	@Test
	public void testSingleToken() {
		query="der";
		String single1 = 
					"{@type=korap:token, @value={@type=korap:term, @value=orth:der, relation==}}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(single1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="Mann";
		String single2 = 
				"{@type=korap:token, @value={@type=korap:term, @value=orth:Mann, relation==}}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(single2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="&Mann";
		String single3 = 
				"{@type=korap:token, @value={@type=korap:term, @value=base:Mann, relation==}}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(single3.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testSequence() {
		query="der Mann";
		String seq1 = 
				"{@type=korap:sequence, operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=orth:der, relation==}}," +
					"{@type=korap:token, @value={@type=korap:term, @value=orth:Mann, relation==}}" +
				"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(seq1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="der Mann schläft";
		String seq2 = 
				"{@type=korap:sequence, operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=orth:der, relation==}}," +
					"{@type=korap:token, @value={@type=korap:term, @value=orth:Mann, relation==}}," +
					"{@type=korap:token, @value={@type=korap:term, @value=orth:schläft, relation==}}" +
				"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(seq2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="der Mann schläft lang";
		String seq3 = 
				"{@type=korap:sequence, operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=orth:der, relation==}}," +
					"{@type=korap:token, @value={@type=korap:term, @value=orth:Mann, relation==}}," +
					"{@type=korap:token, @value={@type=korap:term, @value=orth:schläft, relation==}}," +
					"{@type=korap:token, @value={@type=korap:term, @value=orth:lang, relation==}}" +
				"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(seq3.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOPOR() throws QueryException {
		query="Sonne oder Mond";
		String disj1 = 
					"{@type=korap:group, relation=or, operands=[" +
						"{@type=korap:token, @value={@type=korap:term, @value=orth:Sonne, relation==}}," +
						"{@type=korap:token, @value={@type=korap:term, @value=orth:Mond, relation==}}" +
					"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(disj1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="(Sonne scheint) oder Mond";
		String disj2 = 
					"{@type=korap:group, relation=or, operands=[" +
						"{@type=korap:sequence, operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=orth:Sonne, relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=orth:scheint, relation==}}" +
						"]}," +
						"{@type=korap:token, @value={@type=korap:term, @value=orth:Mond, relation==}}" +
					"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(disj2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="(Sonne scheint) oder (Mond scheint)";
		String disj3 = 
				"{@type=korap:group, relation=or, operands=[" +
						"{@type=korap:sequence, operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=orth:Sonne, relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=orth:scheint, relation==}}" +
						"]}," +
						"{@type=korap:sequence, operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=orth:Mond, relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=orth:scheint, relation==}}" +
						"]}" +
					"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(disj3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
	}
	
	@Test
	public void testOPORAND() {
		query="(Sonne oder Mond) und scheint";
		String orand1 = 
				"{@type=korap:group, relation=and, operands=[" +
					"{@type=korap:group, relation=or, operands=[" +
						"{@type=korap:token, @value={@type=korap:term, @value=orth:Sonne, relation==}}," +
						"{@type=korap:token, @value={@type=korap:term, @value=orth:Mond, relation==}}" +
					"]}," +
					"{@type=korap:token, @value={@type=korap:term, @value=orth:scheint, relation==}}" +
				"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(orand1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="scheint und (Sonne oder Mond)";
		String orand2 = 
				"{@type=korap:group, relation=and, operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=orth:scheint, relation==}}," +
					"{@type=korap:group, relation=or, operands=[" +
						"{@type=korap:token, @value={@type=korap:term, @value=orth:Sonne, relation==}}," +
						"{@type=korap:token, @value={@type=korap:term, @value=orth:Mond, relation==}}" +
					"]}" +
				"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(orand2.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOPPROX() {
		query="Sonne /+w1:4 Mond";
		String prox1 = 
					"{@type=korap:group, relation=distance, @subtype=incl, " +
						"constraint=[" +
							"{@type=korap:distance, measure=w, direction=plus, min=1, max=4}" +
						"], " +
						"operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=orth:Sonne, relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=orth:Mond, relation==}}" +
						"]" +
					"}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(prox1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="Sonne /+w1:4,s0,p1:3 Mond";
		String prox2 = 
					"{@type=korap:group, relation=distance, @subtype=incl, " +
						"constraint=[" +
							"{@type=korap:group, relation=and, operands=[" +
								"{@type=korap:distance, measure=w, direction=plus, min=1, max=4}," +
								"{@type=korap:distance, measure=s, direction=both, min=0, max=0}," +
								"{@type=korap:distance, measure=p, direction=both, min=1, max=3}" +
							"]}" +
						"], " +
						"operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=orth:Sonne, relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=orth:Mond, relation==}}" +
						"]" +
					"}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(prox2.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOPIN() {
		query="wegen #IN <s>";
		String opin1 = 
					"{@type=korap:group, relation=shrink, shrink=1, operands=[" +
						"{@type=korap:group, relation=position, position=contains, operands=[" +
							"{@type=korap:elem, @value=s}," +
							"{@type=korap:group, class=1, operands=[" +
								"{@type=korap:token, @value={@type=korap:term, @value=orth:wegen, relation==}}" +
							"]}" +
						"]}" +
					"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(opin1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="wegen #IN(L) <s>";
		String opin2 = 
					"{@type=korap:group, relation=shrink, shrink=1, operands=[" +
						"{@type=korap:group, relation=position, position=startswith, operands=[" +
							"{@type=korap:elem, @value=s}," +
							"{@type=korap:group, class=1, operands=[" +
								"{@type=korap:token, @value={@type=korap:term, @value=orth:wegen, relation==}}" +
							"]}" +
						"]}" +
					"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(opin2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
//		// position argument is optional 
//		query="wegen #IN <s>";
//		String opin2 = 
//					"{@type=korap:group, relation=include, operands=[" +
//						"{@type=korap:token, @value={@type=korap:term, @value=orth:wegen, relation==}}," +
//						"{@type=korap:elem, @value=s}" +
//					"]}";
//		ppt = new CosmasTree(query);
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(opin2.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// parentheses around 'wegen mir' are optional
//		query="wegen #IN (wegen mir)";
//		String opin3 = 
//					"{@type=korap:group, relation=include, operands=[" +
//						"{@type=korap:token, @value={@type=korap:term, @value=orth:wegen, relation==}}," +
//						"{@type=korap:sequence, operands=[" +
//							"{@type=korap:token, @value={@type=korap:term, @value=orth:wegen, relation==}}," +
//							"{@type=korap:token, @value={@type=korap:term, @value=orth:mir, relation==}}" +
//						"]}" +
//					"]}";
//		ppt = new CosmasTree(query);
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(opin3.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOPOV() {
		query="wegen #OV <s>";
		String opov1 = 
					"{@type=korap:group, relation=shrink, shrink=1, operands=[" +
						"{@type=korap:group, relation=overlap, position=full, operands=[" +
							"{@type=korap:elem, @value=s}," +
							"{@type=korap:group, class=1, operands=[" +
								"{@type=korap:token, @value={@type=korap:term, @value=orth:wegen, relation==}}" +
							"]}" +
						"]}" +
					"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(opov1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="wegen #OV(L) <s>";
		String opov2 = 
					"{@type=korap:group, relation=shrink, shrink=1, operands=[" +
						"{@type=korap:group, relation=overlap, position=startswith, operands=[" +
							"{@type=korap:elem, @value=s}," +
							"{@type=korap:group, class=1, operands=[" +
								"{@type=korap:token, @value={@type=korap:term, @value=orth:wegen, relation==}}" +
							"]}" +
						"]}" +
					"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(opov2.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOPNOT() {
		query="Sonne nicht Mond";
		String opnot1 = 
					"{@type=korap:group, relation=not, operands=[" +
						"{@type=korap:token, @value={@type=korap:term, @value=orth:Sonne, relation==}}," +
						"{@type=korap:token, @value={@type=korap:term, @value=orth:Mond, relation==}}" +
					"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(opnot1.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testBEG_END() {
		// BEG and END operators
		// http://www.ids-mannheim.de/cosmas2/web-app/hilfe/suchanfrage/eingabe-zeile/syntax/links.html
		// http://www.ids-mannheim.de/cosmas2/web-app/hilfe/suchanfrage/eingabe-zeile/syntax/rechts.html
		// http://www.ids-mannheim.de/cosmas2/web-app/hilfe/suchanfrage/eingabe-zeile/thematische-bsp/bsp-satzlaenge.html
		query="";
	}
	

	@Test
	public void testELEM() {
		// http://www.ids-mannheim.de/cosmas2/web-app/hilfe/suchanfrage/eingabe-zeile/syntax/elem.html
		query="#ELEM(S)";
		String elem1 = "{@type=korap:elem, @value=s}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(elem1.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOPALL() {
		query="#ALL(gehen /w1:10 voran)";
		String all1 = 
				"{@type=korap:group, relation=all, operands=[" +
					"{@type=korap:group, relation=distance, @subtype=incl, " +
						"constraint=[" +
							"{@type=korap:distance, measure=w, direction=both, min=1, max=10}" +
						"], " +
						"operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=orth:gehen, relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=orth:voran, relation==}}" +
						"]" +
					"}" +
				"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(all1.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOPNHIT() {
		query="#NHIT(gehen /w1:10 voran)";
		String nhit1 = 
				"{@type=korap:group, relation=nhit, operands=[" +
					"{@type=korap:group, relation=distance, @subtype=incl, " +
						"constraint=[" +
							"{@type=korap:distance, measure=w, direction=both, min=1, max=10}" +
						"], " +
						"operands=[" +
							"{@type=korap:token, @value={@type=korap:term, @value=orth:gehen, relation==}}," +
							"{@type=korap:token, @value={@type=korap:term, @value=orth:voran, relation==}}" +
						"]" +
					"}" +
				"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(nhit1.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOPBED() {
		query = "#BED(der , sa)";
		String bed1 = 
				"{@type=korap:group, relation=position, position=startswith, operands=[" +
					"{@type=korap:token, @value={@type=korap:term, @value=orth:der, relation==}}," +
					"{@type=korap:elem, @value=s}" +
				"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(bed1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "#BED(der Mann , +pe)";
		String bed2 = 
				"{@type=korap:group, relation=position, position=endswith, operands=[" +
					"{@type=korap:sequence, operands=[" +
						"{@type=korap:token, @value={@type=korap:term, @value=orth:der, relation==}}," +
						"{@type=korap:token, @value={@type=korap:term, @value=orth:Mann, relation==}}" +
					"]}," +
					"{@type=korap:elem, @value=p}" +
				"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(bed2.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
}

