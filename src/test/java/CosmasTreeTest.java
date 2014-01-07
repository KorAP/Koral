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
		
	}
	
	@Test
	public void testOPPROX() {
		query="Sonne /+w1:4 Mond";
		String prox1 = 
					"{@type=korap:group, relation=distance, @subtype=w, min=1, max=4, operands=[" +
						"{@type=korap:token, @value={@type=korap:term, @value=orth:Sonne, relation==}}," +
						"{@type=korap:token, @value={@type=korap:term, @value=orth:Mond, relation==}}" +
					"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(prox1.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testOPIN() {
		query="wegen #IN(L) <s>";
		String opin1 = 
					"{@type=korap:group, relation=in, position=L, operands=[" +
						"{@type=korap:token, @value={@type=korap:term, @value=orth:wegen, relation==}}," +
						"{@type=korap:element, @value=s}" +
					"]}";
		ppt = new CosmasTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(opin1.replaceAll(" ", ""), map.replaceAll(" ", ""));
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
	}
}

