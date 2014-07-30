import static org.junit.Assert.*;

import org.junit.Test;

import de.ids_mannheim.korap.query.serialize.PoliqarpPlusTree;
import de.ids_mannheim.korap.util.QueryException;

public class PoliqarpPlusTreeTest {
	
	PoliqarpPlusTree ppt;
	String map;
	String expected;
	String query;
	
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
		String query;
		// [base=Mann]
		String token1 = "{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}";
		assertTrue(equalsQueryContent(token1, "[base=Mann]"));
		
		// [orth!=Frau]
		String token2 = "{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Frau, match=match:ne}}";
		assertTrue(equalsQueryContent(token2, "[orth!=Frau]"));
		
		// [!p=NN]
		query = "[!p=NN]";
		String token3 = "{@type=korap:token, wrap={@type=korap:term, layer=p, key=NN, match=match:ne}}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(token3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [!p!=NN]
		query = "[!p!=NN]";
		String token4 = "{@type=korap:token, wrap={@type=korap:term, layer=p, key=NN, match=match:eq}}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(token4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "[base=schland/x]";
		String token5 = "{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=.*?\\Qschland\\E.*?, match=match:eq, type=type:regex}}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(token5.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testValue() throws QueryException {
		String query;
		
		query = "[mate/m=temp:pres]";
		String value1 = "{@type=korap:token, wrap={@type=korap:term, foundry=mate, layer=m, key=temp, value=pres, match=match:eq}}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(value1.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testRegex() throws QueryException {
		String query = "[orth=\"M(a|ä)nn(er)?\"]";
		String re1 = "{@type=korap:token, wrap={@type=korap:term, layer=orth, type=type:regex, key=M(a|ä)nn(er)?, match=match:eq}}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(re1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "[orth=\"M(a|ä)nn(er)?\"/x]";
		String re2 = "{@type=korap:token, wrap={@type=korap:term, layer=orth, type=type:regex, key=.*?M(a|ä)nn(er)?.*?, match=match:eq}}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(re2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "\"M(a|ä)nn(er)?\"/x";
		String re3 = "{@type=korap:token, wrap={@type=korap:term, type=type:regex, layer=orth, key=.*?M(a|ä)nn(er)?.*?, match=match:eq}}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(re3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "schland/x";
		String re4 = "{@type=korap:token, wrap={@type=korap:term, layer=orth, key=.*?\\Qschland\\E.*?, match=match:eq, type=type:regex}}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(re4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "\".*?Mann.*?\"";
		String re5 = "{@type=korap:token, wrap={@type=korap:term, type=type:regex, layer=orth, key=.*?Mann.*?, match=match:eq}}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(re5.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testCaseSensitivityFlag() throws QueryException {
		String query="[orth=deutscher/i]";
		String cs1 = 
				"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=deutscher, match=match:eq, caseInsensitive=true}}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(cs1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="[orth=deutscher/i][orth=Bundestag]";
		String cs2 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=deutscher, match=match:eq, caseInsensitive=true}}," +
						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Bundestag, match=match:eq}}" +
					"]}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(cs2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="deutscher/i";
		String cs3 = 
				"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=deutscher, match=match:eq, caseInsensitive=true}}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(cs3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="deutscher/i Bundestag";
		String cs4 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=deutscher, match=match:eq, caseInsensitive=true}}," +
						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Bundestag, match=match:eq}}" +
					"]}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(cs4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query="deutscher Bundestag/i";
		String cs5 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=deutscher, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Bundestag, match=match:eq, caseInsensitive=true}}" +
					"]}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(cs5.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testSpans() throws QueryException {
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
		
		// span negation
		query = "<cnx/c!=vp>";
		expected = "{@type=korap:span, foundry=cnx, layer=c, key=vp, match=match:ne}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// span negation
		query = "<cnx/c!=vp>";
		expected = "{@type=korap:span, foundry=cnx, layer=c, key=vp, match=match:ne}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "<cnx/c=vp class!=header>";
		expected = "{@type=korap:span, foundry=cnx, layer=c, key=vp, attr={@type=korap:term, key=class, value=header, match=match:ne}}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "<cnx/c=vp !(class=header&id=7)>";
		expected = 
			"{@type=korap:span, foundry=cnx, layer=c, key=vp, attr=" +
				"{@type=korap:termGroup, relation=relation:and, operands=[" +
					"{@type=korap:term, key=class, value=header, match=match:ne}," +
					"{@type=korap:term, key=id, value=7, match=match:ne}" +
				"]}" +
			"}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "<cnx/c!=vp (class=header&id=7)>";
		expected = 
			"{@type=korap:span, foundry=cnx, layer=c, key=vp, match=match:ne, attr=" +
				"{@type=korap:termGroup, relation=relation:and, operands=[" +
					"{@type=korap:term, key=class, value=header, match=match:eq}," +
					"{@type=korap:term, key=id, value=7, match=match:eq}" +
				"]}" +
			"}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "<cnx/c=vp !!class=header>";
		expected = "{@type=korap:span, foundry=cnx, layer=c, key=vp, attr={@type=korap:term, key=class, value=header, match=match:eq}}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "<cnx/c!=vp (foundry/class=header&id=7)>";
		expected = 
			"{@type=korap:span, foundry=cnx, layer=c, key=vp, match=match:ne, attr=" +
				"{@type=korap:termGroup, relation=relation:and, operands=[" +
					"{@type=korap:term, foundry=foundry, key=class, value=header, match=match:eq}," +
					"{@type=korap:term, key=id, value=7, match=match:eq}" +
				"]}" +
			"}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testDistances() throws QueryException {
		// [base=der][][base=Mann]
		String et1 = 
			"{@type=korap:group, operation=operation:sequence, " +
			"operands=[" +
				"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=der, match=match:eq}}," +
				"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}" +
			"], inOrder=true, distances=[" +
				"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=2, max=2}, min=2, max=2}" +
			"]}";
		ppt = new PoliqarpPlusTree("[base=der][][base=Mann]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(et1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=der][][][base=Mann]
		String et2 = 
			"{@type=korap:group, operation=operation:sequence, " +
			"operands=[" +
				"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=der, match=match:eq}}," +
				"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}" +
			"], inOrder=true, distances=[" +
				"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=3, max=3}, min=3, max=3}" +
			"]}";
		ppt = new PoliqarpPlusTree("[base=der][][][base=Mann]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(et2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=der][][]?[base=Mann]
		String et3 = 
			"{@type=korap:group, operation=operation:sequence, " +
			"operands=[" +
				"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=der, match=match:eq}}," +
				"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}" +
			"], inOrder=true, distances=[" +
				"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=2, max=3}, min=2, max=3}" +
			"]}";
		ppt = new PoliqarpPlusTree("[base=der][][]?[base=Mann]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(et3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		
		
		
		// [base=der][]{2,5}[base=Mann][]?[][base=Frau]   nested distances=
		String et5 = 
				"{@type=korap:group, operation=operation:sequence," +
				"operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=der, match=match:eq}}," +
					"{@type=korap:group, operation=operation:sequence, " +
					"operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Frau, match=match:eq}}" +
					"], inOrder=true, distances=[" +
						"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=2, max=3}, min=2, max=3}" +
					"]}" +
				"], inOrder=true, distances=[" +
					"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=3, max=6}, min=3, max=6}" +
				"]}";
		ppt = new PoliqarpPlusTree("[base=der][]{2,5}[base=Mann][]?[][base=Frau]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(et5.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=der][]*[base=Mann]
		String et6 = 
			"{@type=korap:group, operation=operation:sequence, " +
			"operands=[" +
				"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=der, match=match:eq}}," +
				"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}" +
			"], inOrder=true, distances=[" +
				"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=1}, min=1}" +
			"]}";
		ppt = new PoliqarpPlusTree("[base=der][]*[base=Mann]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(et6.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=der][]+[base=Mann]
		String et7 = 
			"{@type=korap:group, operation=operation:sequence, " +
			"operands=[" +
				"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=der, match=match:eq}}," +
				"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}" +
			"], inOrder=true, distances=[" +
				"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=2}, min=2}" +
			"]}";
		ppt = new PoliqarpPlusTree("[base=der][]+[base=Mann]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(et7.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=der][]+[base=Mann]
		String et8 = 
			"{@type=korap:group, operation=operation:sequence, " +
			"operands=[" +
				"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=der, match=match:eq}}," +
				"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}" +
			"], inOrder=true, distances=[" +
				"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=2, max=103}, min=2, max=103}" +
			"]}";
		ppt = new PoliqarpPlusTree("[base=der][]{1,102}[base=Mann]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(et8.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=geht][base=der][]*[base=Mann]
		String et9 = 
			"{@type=korap:group, operation=operation:sequence, operands=[" +
				"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=geht, match=match:eq}}," +
				"{@type=korap:group, operation=operation:sequence, " +
				"operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=der, match=match:eq}}," +
					"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}" +
				"], inOrder=true, distances=[" +
					"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=1}, min=1}" +
				"]}" +
			"]}";
		ppt = new PoliqarpPlusTree("[base=geht][base=der][]*[base=Mann]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(et9.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "[base=geht][base=der][]*[base=Mann][base=da]";
		expected = 
			"{@type=korap:group, operation=operation:sequence, operands=[" +
				"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=geht, match=match:eq}}," +
				"{@type=korap:group, operation=operation:sequence, " +
				"operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=der, match=match:eq}}," +
					"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=da, match=match:eq}}" +
					"]}" +
				"], inOrder=true, distances=[" +
					"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=1}, min=1}" +
				"]}" +
			"]}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "[base=geht][base=der][]*contains(<s>,<np>)";
		expected = 
			"{@type=korap:group, operation=operation:sequence, operands=[" +
				"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=geht, match=match:eq}}," +
				"{@type=korap:group, operation=operation:sequence, " +
				"operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=der, match=match:eq}}," +
					"{@type=korap:group, operation=operation:position, frame=frame:contains, operands=[" +
					  "{@type=korap:span, key=s}," +
					  "{@type=korap:span, key=np}" +
					"]}" +
				"], inOrder=true, distances=[" +
					"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=1}, min=1}" +
				"]}" +
			"]}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}

	@Test
	public void testLeadingTrailingEmptyTokens() throws QueryException {
		// startswith(<s>, [][base=Mann]
		String et1 = 
			"{@type=korap:group, operation=operation:position, frame=frame:startswith, operands=[" +	
				"{@type=korap:span, key=s}," +
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token}," +
					"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}" +
				"]}" +
			"]}";
		ppt = new PoliqarpPlusTree("startswith(<s>, [][base=Mann])");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(et1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "[][base=Mann]";
		expected = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token}," +
					"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}" +
				"]}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "[][][base=Mann]";
		expected = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:group, operation=operation:repetition, operands=[" +
						"{@type=korap:token}" +
					"], boundary={@type=korap:boundary, min=2, max=2}, min=2, max=2}," +
					"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}" +
				"]}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "[][]*[base=Mann]";
		expected = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:group, operation=operation:repetition, operands=[" +
						"{@type=korap:token}" +
					"], boundary={@type=korap:boundary, min=1}, min=1}," +
					"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}" +
				"]}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "[][]*[base=Mann][][]";
		expected = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:group, operation=operation:repetition, operands=[" +
						"{@type=korap:token}" +
					"], boundary={@type=korap:boundary, min=1}, min=1}," +
					"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}," +
					"{@type=korap:group, operation=operation:repetition, operands=[" +
						"{@type=korap:token}" +
					"], boundary={@type=korap:boundary, min=2, max=2}, min=2, max=2}" +
				"]}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "[][]*contains(<s>, <np>)[][]";
		expected = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:group, operation=operation:repetition, operands=[" +
						"{@type=korap:token}" +
					"], boundary={@type=korap:boundary, min=1}, min=1}," +
					"{@type=korap:group, operation=operation:position, frame=frame:contains, operands=[" +
					  "{@type=korap:span, key=s}," +
					  "{@type=korap:span, key=np}" +
					"]}," +
					"{@type=korap:group, operation=operation:repetition, operands=[" +
						"{@type=korap:token}" +
					"], boundary={@type=korap:boundary, min=2, max=2}, min=2, max=2}" +
				"]}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testCoordinatedFields() throws QueryException {
		// [base=Mann&(cas=N|cas=A)]
		String cof1 = 
			"{@type=korap:token, wrap=" +
				"{@type=korap:termGroup, relation=relation:and, operands=[" +
					"{@type=korap:term, layer=lemma, key=Mann, match=match:eq}," +
					"{@type=korap:termGroup, relation=relation:or, operands=[" +
						"{@type=korap:term, layer=cas, key=N, match=match:eq}," +
						"{@type=korap:term, layer=cas, key=A, match=match:eq}" +
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
					"{@type=korap:term, layer=lemma, key=Mann, match=match:eq}," +
					"{@type=korap:termGroup, relation=relation:and, operands=[" +
						"{@type=korap:term, layer=cas, key=N, match=match:eq}," +
						"{@type=korap:term, layer=gen, key=m, match=match:eq}" +
					"]}" +
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
					     "{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=foo, match=match:eq}}" +
					  "], boundary={@type=korap:boundary, min=0}, min=0}"; 
		ppt = new PoliqarpPlusTree("[base=foo]*");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=foo]*[base=bar]
		String occ2 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:group, operation=operation:repetition, operands=[" +
					     "{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=foo, match=match:eq}}" +
					"], boundary={@type=korap:boundary, min=0}, min=0 }," +
					"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=bar, match=match:eq}}" +
				"]}"; 
		ppt = new PoliqarpPlusTree("[base=foo]*[base=bar]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=bar][base=foo]*
		String occ3 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=bar, match=match:eq}}," +
					"{@type=korap:group, operation=operation:repetition, operands=[" +
					     "{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=foo, match=match:eq}}" +
					"], boundary={@type=korap:boundary, min=0}, min=0 }" +
				"]}"; 
		ppt = new PoliqarpPlusTree("[base=bar][base=foo]*");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// ([base=bar][base=foo])*
		String occ4 = 
				"{@type=korap:group, operation=operation:repetition, operands=[" +	
					"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=bar, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=foo, match=match:eq}}" +
					"]}" +
				"], boundary={@type=korap:boundary, min=0}, min=0}" ;
		ppt = new PoliqarpPlusTree("([base=bar][base=foo])*");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// <s>([base=bar][base=foo])*
		String occ5 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:span, key=s}," +
					"{@type=korap:group, operation=operation:repetition, operands=[" +	
						"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=bar, match=match:eq}}," +
							"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=foo, match=match:eq}}" +
						"]}" +
					"], boundary={@type=korap:boundary, min=0}, min=0 }" +
				"]}" ;
		ppt = new PoliqarpPlusTree("<s>([base=bar][base=foo])*");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ5.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// <s><np>([base=bar][base=foo])*
		String occ6 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:span, key=s}," +
					"{@type=korap:span, key=np}," +
					"{@type=korap:group, operation=operation:repetition, operands=[" +	
						"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=bar, match=match:eq}}," +
							"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=foo, match=match:eq}}" +
						"]}" +
					"], boundary={@type=korap:boundary, min=0}, min=0 }" +
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
					"{@type=korap:group, operation=operation:repetition, operands=[" +	
						"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=bar, match=match:eq}}," +
							"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=foo, match=match:eq}}" +
						"]}" +
					"], boundary={@type=korap:boundary, min=0}, min=0 }," +
					"{@type=korap:token, wrap={@type=korap:term, layer=p, key=NN, match=match:eq}}" +
				"]}" ;
		ppt = new PoliqarpPlusTree("<s><np>([base=bar][base=foo])*[p=NN]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ7.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// ([base=bar][base=foo])*[p=NN]
		String occ8 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:group, operation=operation:repetition, operands=[" +	
						"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=bar, match=match:eq}}," +
							"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=foo, match=match:eq}}" +
						"]}" +
					"], boundary={@type=korap:boundary, min=0}, min=0 }," +
					"{@type=korap:token, wrap={@type=korap:term, layer=p, key=NN, match=match:eq}}" +
				"]}" ;
		ppt = new PoliqarpPlusTree("([base=bar][base=foo])*[p=NN]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ8.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=foo]+
		String occ9 = "{@type=korap:group, operation=operation:repetition, operands=[" +
					     "{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=foo, match=match:eq}}" +
					  "], boundary={@type=korap:boundary, min=1}, min=1}"; 
		ppt = new PoliqarpPlusTree("[base=foo]+");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ9.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=foo]?
		String occ10 = "{@type=korap:group, operation=operation:repetition, operands=[" +
					     "{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=foo, match=match:eq}}" +
					  "], boundary={@type=korap:boundary, min=0, max=1}, min=0, max=1}"; 
		ppt = new PoliqarpPlusTree("[base=foo]?");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ10.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=foo]{2,5}
		String occ11 = "{@type=korap:group, operation=operation:repetition, operands=[" +
					     "{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=foo, match=match:eq}}" +
					  "], boundary={@type=korap:boundary, min=2, max=5}, min=2, max=5}"; 
		ppt = new PoliqarpPlusTree("[base=foo]{2,5}");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ11.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=foo]{2}
		String occ12 = "{@type=korap:group, operation=operation:repetition, operands=[" +
					     "{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=foo, match=match:eq}}" +
					  "], boundary={@type=korap:boundary, min=2, max=2}, min=2, max=2}"; 
		ppt = new PoliqarpPlusTree("[base=foo]{2}");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ12.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=foo]{2}
		String occ13 = "{@type=korap:group, operation=operation:repetition, operands=[" +
					     "{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=foo, match=match:eq}}" +
					  "], boundary={@type=korap:boundary, min=2}, min=2}"; 
		ppt = new PoliqarpPlusTree("[base=foo]{2,}");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ13.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=foo]{2}
		String occ14 = "{@type=korap:group, operation=operation:repetition, operands=[" +
					     "{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=foo, match=match:eq}}" +
					  "], boundary={@type=korap:boundary, min=0, max=2}, min=0, max=2}"; 
		ppt = new PoliqarpPlusTree("[base=foo]{,2}");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(occ14.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testTokenSequence() throws QueryException {
		// [base=Mann][orth=Frau]
		String seq1 = "{@type=korap:group, operation=operation:sequence, operands=[" +
				"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}, " +
				"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Frau, match=match:eq}}" +
				"]}";
		assertTrue(equalsQueryContent(seq1, "[base=Mann][orth=Frau]"));
		
		// [base=Mann][orth=Frau][p=NN]
		String seq2 = "{@type=korap:group, operation=operation:sequence, operands=[" +
				"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}, " +
				"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Frau, match=match:eq}}, " +
				"{@type=korap:token, wrap={@type=korap:term, layer=p, key=NN, match=match:eq}}" +
				"]}";
		assertTrue(equalsQueryContent(seq2, "[base=Mann][orth=Frau][p=NN]"));
	}
	
	@Test
	public void testDisjSegments() throws QueryException {
		// ([base=der]|[base=das])[base=Schild]
		String disj1 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:group, operation=operation:or, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=der, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=das, match=match:eq}}" +
					"]}," +
					"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Schild, match=match:eq}}" +
				"]}";
		ppt = new PoliqarpPlusTree("([base=der]|[base=das])[base=Schild]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(disj1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=Schild]([base=der]|[base=das])
		String disj2 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Schild, match=match:eq}}," +
					"{@type=korap:group, operation=operation:or, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=der, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=das, match=match:eq}}" +
					"]}" +
				"]}";
		ppt = new PoliqarpPlusTree("[base=Schild]([base=der]|[base=das])");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(disj2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// "([orth=der][base=katze])|([orth=eine][base=baum])"
		String disj3 = 
				"{@type=korap:group, operation=operation:or, operands=[" +
					"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=der, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=katze, match=match:eq}}" +
					"]}," +
					"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=eine, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=baum, match=match:eq}}" +
					"]}" +
				"]}";
		ppt = new PoliqarpPlusTree("([orth=der][base=katze])|([orth=eine][base=baum])");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(disj3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// "[orth=der][base=katze]|[orth=eine][base=baum]"
		String disj4 = 
				"{@type=korap:group, operation=operation:or, operands=[" +
					"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=der, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=katze, match=match:eq}}" +
					"]}," +
					"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=eine, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=baum, match=match:eq}}" +
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
						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=der, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=katze, match=match:eq}}" +
					"]}," +
					"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=der, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=hund, match=match:eq}}" +
					"]}," +
					"{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=der, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=baum, match=match:eq}}" +
					"]}" +
				"]}";
		ppt = new PoliqarpPlusTree("[orth=der][base=katze]|[orth=der][base=hund]|[orth=der][base=baum]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(disj5.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [orth=der]([base=katze]|[base=hund]|[base=baum])
		String disj6 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=der, match=match:eq}}," +
					"{@type=korap:group, operation=operation:or, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=katze, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=hund, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=baum, match=match:eq}}" +
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
				"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}, " +
				"{@type=korap:span, key=vp}" +
				"]}";
		assertTrue(equalsQueryContent(seq1, "[base=Mann]<vp>"));
		
		// <vp>[base=Mann]
		String seq2 = "{@type=korap:group, operation=operation:sequence, operands=[" +
				"{@type=korap:span, key=vp}, "+
				"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}} " +
				"]}";
		assertTrue(equalsQueryContent(seq2, "<vp>[base=Mann]"));
		
		// <vp>[base=Mann]<pp>
		String seq3 = "{@type=korap:group, operation=operation:sequence, operands=[" +
				"{@type=korap:span, key=vp}, "+
				"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}, " +
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
		String query;
		// {[base=Mann]}
		String cls1 = "{@type=korap:group, operation=operation:class, class=0, operands=[" +
				"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}" +
				"]}";
		ppt = new PoliqarpPlusTree("{[base=Mann]}");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(cls1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// {[base=Mann][orth=Frau]}
		query = "{[base=Mann][orth=Frau]}";
		String cls2 = "{@type=korap:group, operation=operation:class, class=0, operands=[" +
				 "{@type=korap:group, operation=operation:sequence, operands=[" +
				  "{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}," +
				  "{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Frau, match=match:eq}}" +
				 "]}" +
				"]}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(cls2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [p=NN]{[base=Mann][orth=Frau]}
		String cls3 = "{@type=korap:group, operation=operation:sequence, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, layer=p, key=NN, match=match:eq}}," +
						"{@type=korap:group, operation=operation:class, class=0, operands=[" +
							"{@type=korap:group, operation=operation:sequence, operands=[" +
								"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}," +
								"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Frau, match=match:eq}}" +
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
						     "{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}," +
						     "{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Frau, match=match:eq}}" +
						   "]}" +
						"]}," +
						"{@type=korap:token, wrap={@type=korap:term, layer=p, key=NN, match=match:eq}}" +
					  "]}";
		ppt = new PoliqarpPlusTree("{[base=Mann][orth=Frau]}[p=NN]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(cls4.replaceAll(" ", ""), map.replaceAll(" ", ""));

		// {2:{1:[tt/p=ADJA]}[mate/p=NN]}"
		String cls5 = "{@type=korap:group, operation=operation:class, class=2, operands=[" +
						"{@type=korap:group, operation=operation:sequence, operands=[" +
						   "{@type=korap:group, operation=operation:class, class=1, operands=[" +
						     "{@type=korap:token, wrap={@type=korap:term, foundry=tt, layer=p, key=ADJA, match=match:eq}}" +
						   "]}," +
						   "{@type=korap:token, wrap={@type=korap:term, foundry=mate, layer=p, key=NN, match=match:eq}}" + 
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
				  "{@type=korap:token, wrap= {@type=korap:term, layer=lemma, key=Mann, match=match:eq}}" +
				"]}";
		assertTrue(equalsQueryContent(pos2, "contains(<s>,[base=Mann])"));
		
		// contains(<s>,[orth=der][orth=Mann])
		String pos3 = "{@type=korap:group, operation=operation:position, frame=frame:contains, operands=[" +
				  	"{@type=korap:span, key=s}," +
				  	"{@type=korap:group, operation=operation:sequence, operands=[" +
				  		"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=der, match=match:eq}}," +
				  		"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mann, match=match:eq}}" +
				  	"]}" +
				  "]}";
		ppt = new PoliqarpPlusTree("contains(<s>,[orth=der][orth=Mann])");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(pos3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=Auto]contains(<s>,[base=Mann])
		String pos4 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Auto, match=match:eq}}," +
					"{@type=korap:group, operation=operation:position, frame=frame:contains, operands=[" +
				  		"{@type=korap:span, key=s}," +
				  		"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}" +
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
				  			"operands=[{@type=korap:token, wrap={@type=korap:term, layer=pos, key=N, match=match:eq}}" +
				  			"], boundary={@type=korap:boundary, min=0}, min=0" +
				  		"}" +
				  	"]}";
		ppt = new PoliqarpPlusTree("contains(<s>,[pos=N]*)");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(pos5.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [base=Auto]contains(<s>,[pos=N]*)
		String pos6 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Auto, match=match:eq}}," +
					"{@type=korap:group, operation=operation:position, frame=frame:contains, operands=[" +
				  		"{@type=korap:span, key=s}," +
				  		"{@type=korap:group, operation=operation:repetition, " +
				  			"operands=[{@type=korap:token, wrap={@type=korap:term, layer=pos, key=N, match=match:eq}}" +
				  			"], boundary={@type=korap:boundary, min=0}, min=0" +
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
					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Der, match=match:eq}}" +
				"]}" +
			"]}";
		ppt = new PoliqarpPlusTree("contains(<s>, startswith(<np>,[orth=Der]))");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(npos1.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testShrinkSplit() throws QueryException {
		// focus([orth=Der]{[orth=Mann]})
		String shr1 = 
			"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Der, match=match:eq}}," +
					"{@type=korap:group, operation=operation:class, class=0, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mann, match=match:eq}}" +
					"]}" +
				"]}" +
			"]}";
		ppt = new PoliqarpPlusTree("focus([orth=Der]{[orth=Mann]})");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(shr1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// focus([orth=Der]{[orth=Mann][orth=geht]})
		String shr2 = 
			"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Der, match=match:eq}}," +
					"{@type=korap:group, operation=operation:class, class=0, operands=[" +
						"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mann, match=match:eq}}," +
							"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=geht, match=match:eq}}" +
						"]}" +
					"]}" +
				"]}" +
			"]}";
		ppt = new PoliqarpPlusTree("focus([orth=Der]{[orth=Mann][orth=geht]})");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(shr2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// focus(1:[orth=Der]{1:[orth=Mann][orth=geht]})
		String shr3 = 
			"{@type=korap:reference, operation=operation:focus, classRef=[1], operands=[" +
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Der, match=match:eq}}," +
					"{@type=korap:group, operation=operation:class, class=1, operands=[" +
						"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mann, match=match:eq}}," +
							"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=geht, match=match:eq}}" +
						"]}" +
					"]}" +
				"]}" +
			"]}";
		ppt = new PoliqarpPlusTree("focus(1:[orth=Der]{1:[orth=Mann][orth=geht]})");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(shr3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// focus(1:startswith(<s>,{1:<np>}))
		String shr4 = 
			"{@type=korap:reference, operation=operation:focus, classRef=[1], operands=[" +
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
		
		// focus(3: startswith(<s>, {3:[base=der]{1:[mate/p=ADJA]{2:[tt/p=NN]}}})) 
		String shr5 = 
			"{@type=korap:reference, operation=operation:focus, classRef=[3], operands=[" +
				"{@type=korap:group, operation=operation:position, frame=frame:startswith, operands=[" +
					"{@type=korap:span, key=s}," +
					"{@type=korap:group, operation=operation:class, class=3, operands=[" +
						"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=der, match=match:eq}}," +
							"{@type=korap:group, operation=operation:class, class=1, operands=[" +
								"{@type=korap:group, operation=operation:sequence, operands=[" +
									"{@type=korap:token, wrap={@type=korap:term, foundry=mate, layer=p, key=ADJA, match=match:eq}}," +
									"{@type=korap:group, operation=operation:class, class=2, operands=[" +
										"{@type=korap:token, wrap={@type=korap:term, foundry=tt, layer=p, key=NN, match=match:eq}}" +
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
			"{@type=korap:reference, operation=operation:split, classRef=[3], operands=[" +
				"{@type=korap:group, operation=operation:position, frame=frame:startswith, operands=[" +
					"{@type=korap:span, key=s}," +
					"{@type=korap:group, operation=operation:class, class=3, operands=[" +
						"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=der, match=match:eq}}," +
							"{@type=korap:group, operation=operation:class, class=1, operands=[" +
								"{@type=korap:group, operation=operation:sequence, operands=[" +
									"{@type=korap:token, wrap={@type=korap:term, foundry=mate, layer=p, key=ADJA, match=match:eq}}," +
									"{@type=korap:group, operation=operation:class, class=2, operands=[" +
										"{@type=korap:token, wrap={@type=korap:term, foundry=tt, layer=p, key=NN, match=match:eq}}" +
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
			"{@type=korap:reference, operation=operation:split, classRef=[2, 3], classRefOp=classRefOp:intersection, operands=[" +
				"{@type=korap:group, operation=operation:position, frame=frame:startswith, operands=[" +
					"{@type=korap:span, key=s}," +
					"{@type=korap:group, operation=operation:class, class=3, operands=[" +
						"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=der, match=match:eq}}," +
							"{@type=korap:group, operation=operation:class, class=1, operands=[" +
								"{@type=korap:group, operation=operation:sequence, operands=[" +
									"{@type=korap:token, wrap={@type=korap:term, foundry=mate, layer=p, key=ADJA, match=match:eq}}," +
									"{@type=korap:group, operation=operation:class, class=2, operands=[" +
										"{@type=korap:token, wrap={@type=korap:term, foundry=tt, layer=p, key=NN, match=match:eq}}" +
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
			"{@type=korap:reference, operation=operation:focus, classRef=[1], operands=[" +
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:group, operation=operation:class, class=0, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=der, match=match:eq}}" +
					"]}," +
					"{@type=korap:group, operation=operation:class, class=1, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=ADJA, match=match:eq}}" +
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
		String layer1 = "{@type=korap:token, wrap={@type=korap:term, foundry=tt, layer=lemma, key=Mann, match=match:eq}}";
		ppt = new PoliqarpPlusTree("[tt/base=Mann]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(layer1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
	}
	
	@Test
	public void testAlign() throws QueryException {
		// [orth=der]^[orth=Mann]
		String align1 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=der, match=match:eq}}," +
					"{@type=korap:group, operation=operation:alignment, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mann, match=match:eq}}" +
					"], align=align:left}" +
				"]}";
		ppt = new PoliqarpPlusTree("[orth=der]^[orth=Mann]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(align1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// [orth=der]^[orth=große][orth=Mann]
		String query = "[orth=der]^[orth=große][orth=Mann]";
		String align1b = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=der, match=match:eq}}," +
					"{@type=korap:group, operation=operation:alignment, operands=[" +
						"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=große, match=match:eq}}," +
							"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mann, match=match:eq}}" +
						"]}" +
					"], align=align:left}" +
				"]}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(align1b.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// "([base=a]^[base=b])|[base=c]",
		String align2 = 
				"{@type=korap:group, operation=operation:or, operands=[" +
						"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=a, match=match:eq}}," +
							"{@type=korap:group, operation=operation:alignment, operands=[" +
								"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=b, match=match:eq}}" +
							"], align=align:left}" +
						"]}," +
						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=c, match=match:eq}}" +
				"]}";
		ppt = new PoliqarpPlusTree("([base=a]^[base=b])|[base=c]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(align2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// "([base=a]^[base=b][base=c])|[base=d]",
		String align3 = 
				"{@type=korap:group, operation=operation:or, operands=[" +
						"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=a, match=match:eq}}," +
							"{@type=korap:group, operation=operation:alignment, operands=[" +
								"{@type=korap:group, operation=operation:sequence, operands=[" +
									"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=b, match=match:eq}}," +
									"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=c, match=match:eq}}" +
								"]}" +
							"], align=align:left}" +
						"]}," +
						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=d, match=match:eq}}" +
				"]}";
		ppt = new PoliqarpPlusTree("([base=a]^[base=b][base=c])|[base=d]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(align3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// "([base=a]^[base=b]^[base=c])|[base=d]",
		String align4 = 
				"{@type=korap:group, operation=operation:or, operands=[" +
						"{@type=korap:group, operation=operation:sequence, operands=[" +
							"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=a, match=match:eq}}," +
							"{@type=korap:group, operation=operation:alignment, operands=[" +
								"{@type=korap:group, operation=operation:sequence, operands=[" +
									"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=b, match=match:eq}}," +
									"{@type=korap:group, operation=operation:alignment, operands=[" +
										"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=c, match=match:eq}}" +
									"], align=align:left}" +
								"]}" +
							"], align=align:left}" +
						"]}," +
						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=d, match=match:eq}}" +
				"]}";
		ppt = new PoliqarpPlusTree("([base=a]^[base=b]^[base=c])|[base=d]");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(align4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		
	}
	
	@Test
	public void testSimpleQueries() throws QueryException {
		// Baum
		String simple1 = 
				"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Baum, match=match:eq}}";
		ppt = new PoliqarpPlusTree("Baum");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(simple1.replaceAll(" ", ""), map.replaceAll(" ", ""));

		// Baum/i
		String simple1b = 
				"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Baum, match=match:eq, caseInsensitive=true}}";
		ppt = new PoliqarpPlusTree("Baum/i");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(simple1b.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// Der Baum
		String simple2 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Der, match=match:eq}}, " +
					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Baum, match=match:eq}}" +
				"]}";
		ppt = new PoliqarpPlusTree("Der Baum");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(simple2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// Der Baum/i
		String simple2b = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Der, match=match:eq}}, " +
					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Baum, match=match:eq, caseInsensitive=true}}" +
				"]}";
		ppt = new PoliqarpPlusTree("Der Baum/i");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(simple2b.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// Der große Baum
		String simple3 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Der, match=match:eq}}, " +
					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=große, match=match:eq}}, " +						
					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Baum, match=match:eq}}" +
				"]}";
		ppt = new PoliqarpPlusTree("Der große Baum");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(simple3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		// Baum | Stein
		String simple4 = 
				"{@type=korap:group, operation=operation:or, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Baum, match=match:eq}}, " +						
					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Stein, match=match:eq}}" +
				"]}";
		ppt = new PoliqarpPlusTree("Baum | Stein");
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(simple4.replaceAll(" ", ""), map.replaceAll(" ", ""));		
		
		// Baum | Stein Haus
		String query = "(Baum | Stein) Haus";
		String simple5 = 
				"{@type=korap:group, operation=operation:sequence, operands=[" +
					"{@type=korap:group, operation=operation:or, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Baum, match=match:eq}}, " +						
						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Stein, match=match:eq}}" +
					"]}," +
					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Haus, match=match:eq}} " +			
				"]}";
		ppt = new PoliqarpPlusTree(query);
		map = ppt.getRequestMap().get("query").toString();
		assertEquals(simple5.replaceAll(" ", ""), map.replaceAll(" ", ""));		
	}
}

