import static org.junit.Assert.*;
import de.ids_mannheim.korap.query.serialize.CollectionQueryTree;
import de.ids_mannheim.korap.util.QueryException;
import org.junit.Test;

public class CollectionQueryTreeTest {

	CollectionQueryTree cqt;
	String map;
	private String query;
	private String expected;

	@Test
	public void testSimple() throws QueryException {
		query = "textClass=Sport";
		//      String regex1 = "{@type=korap:filter, filter={@type=korap:doc, attribute=textClass, key=Sport, match=match:eq}}";
		expected = "{@type=korap:doc, key=textClass, value=Sport, match=match:eq}";
		cqt = new CollectionQueryTree();
		cqt.process(query);
		map = cqt.getRequestMap().get("collection").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));

		query = "textClass!=Sport";
		//	      String regex1 = "{@type=korap:filter, filter={@type=korap:doc, attribute=textClass, key=Sport, match=match:eq}}";
		expected = "{@type=korap:doc, key=textClass, value=Sport, match=match:ne}";
		cqt = new CollectionQueryTree();
		cqt.process(query);
		map = cqt.getRequestMap().get("collection").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testContains() throws QueryException {
		query = "title~Mannheim";
		expected = 
			"{@type=korap:doc, key=title, value=Mannheim, match=match:contains}";
		cqt = new CollectionQueryTree();
		cqt.process(query);
		map = cqt.getRequestMap().get("collection").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "title~\"IDS Mannheim\"";
		expected = 
			"{@type=korap:doc, key=title, value=IDS Mannheim, match=match:contains}";
		cqt = new CollectionQueryTree();
		cqt.process(query);
		map = cqt.getRequestMap().get("collection").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testTwoConjuncts() throws QueryException {
		query = "textClass=Sport & year=2014";
		expected = 
				"{@type=korap:docGroup, operation=operation:and, operands=[" +
					"{@type=korap:doc, key=textClass, value=Sport, match=match:eq}," +
					"{@type=korap:doc, key=year, type=type:date, value=2014, match=match:eq}" +
				"]}";
		cqt = new CollectionQueryTree();
		cqt.process(query);
		map = cqt.getRequestMap().get("collection").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}

    //todo year type is not yet serialized!
	@Test
	public void testThreeConjuncts() throws QueryException {
		query = "textClass=Sport & year=2014 & corpusID=WPD";
		expected = 
				"{@type=korap:docGroup, operation=operation:and, operands=[" +
					"{@type=korap:doc, key=textClass, value=Sport, match=match:eq}," +
					"{@type=korap:docGroup, operation=operation:and, operands=[" +
						"{@type=korap:doc, key=year, type=type:date, value=2014, match=match:eq}," +
						"{@type=korap:doc, key=corpusID, value=WPD, match=match:eq}" +
					"]}" +
				"]}";
		cqt = new CollectionQueryTree();
		cqt.process(query);
		map = cqt.getRequestMap().get("collection").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	

	@Test
	public void testTwoDisjuncts() throws QueryException {
		query = "textClass=Sport | year=2014";
		expected = 
				"{@type=korap:docGroup, operation=operation:or, operands=[" +
					"{@type=korap:doc, key=textClass, value=Sport, match=match:eq}," +
					"{@type=korap:doc, key=year, type=type:date, value=2014, match=match:eq}" +
				"]}";
		cqt = new CollectionQueryTree();
		cqt.process(query);
		map = cqt.getRequestMap().get("collection").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testThreeDisjuncts() throws QueryException {
		query = "textClass=Sport | year=2014 | corpusID=WPD";
		expected = 
				"{@type=korap:docGroup, operation=operation:or, operands=[" +
					"{@type=korap:doc, key=textClass, value=Sport, match=match:eq}," +
					"{@type=korap:docGroup, operation=operation:or, operands=[" +
						"{@type=korap:doc, key=year, type=type:date, value=2014, match=match:eq}," +
						"{@type=korap:doc, key=corpusID, value=WPD, match=match:eq}" +
					"]}" +
				"]}";
		cqt = new CollectionQueryTree();
		cqt.process(query);
		map = cqt.getRequestMap().get("collection").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}


	@Test
	public void testMixed() throws QueryException {
		query = "(textClass=Sport | textClass=ausland) & corpusID=WPD";
		expected = 
			
				"{@type=korap:docGroup, operation=operation:and, operands=[" +
					"{@type=korap:docGroup, operation=operation:or, operands=[" +
						"{@type=korap:doc, key=textClass, value=Sport, match=match:eq}," +
						"{@type=korap:doc, key=textClass, value=ausland, match=match:eq}" +
					"]}," +
					"{@type=korap:doc, key=corpusID, value=WPD, match=match:eq}" +
				"]}";
		cqt = new CollectionQueryTree();
		cqt.process(query);
		map = cqt.getRequestMap().get("collection").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "(textClass=Sport & textClass=ausland) & corpusID=WPD";
		expected = 
			
				"{@type=korap:docGroup, operation=operation:and, operands=[" +
					"{@type=korap:docGroup, operation=operation:and, operands=[" +
						"{@type=korap:doc, key=textClass, value=Sport, match=match:eq}," +
						"{@type=korap:doc, key=textClass, value=ausland, match=match:eq}" +
					"]}," +
					"{@type=korap:doc, key=corpusID, value=WPD, match=match:eq}" +
				"]}";
		cqt = new CollectionQueryTree();
		cqt.process(query);
		map = cqt.getRequestMap().get("collection").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "(textClass=Sport & textClass=ausland) | (corpusID=WPD & author=White)";
		expected = 
			
				"{@type=korap:docGroup, operation=operation:or, operands=[" +
					"{@type=korap:docGroup, operation=operation:and, operands=[" +
						"{@type=korap:doc, key=textClass, value=Sport, match=match:eq}," +
						"{@type=korap:doc, key=textClass, value=ausland, match=match:eq}" +
					"]}," +
					"{@type=korap:docGroup, operation=operation:and, operands=[" +
						"{@type=korap:doc, key=corpusID, value=WPD, match=match:eq}," +
						"{@type=korap:doc, key=author, value=White, match=match:eq}" +
					"]}" +
				"]}";
		cqt = new CollectionQueryTree();
		cqt.process(query);
		map = cqt.getRequestMap().get("collection").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "(textClass=Sport & textClass=ausland) | (corpusID=WPD & author=White & year=2010)";
		expected = 
				"{@type=korap:docGroup, operation=operation:or, operands=[" +
					"{@type=korap:docGroup, operation=operation:and, operands=[" +
						"{@type=korap:doc, key=textClass, value=Sport, match=match:eq}," +
						"{@type=korap:doc, key=textClass, value=ausland, match=match:eq}" +
					"]}," +
					"{@type=korap:docGroup, operation=operation:and, operands=[" +
						"{@type=korap:doc, key=corpusID, value=WPD, match=match:eq}," +
						"{@type=korap:docGroup, operation=operation:and, operands=[" +
							"{@type=korap:doc, key=author, value=White, match=match:eq}," +
							"{@type=korap:doc, key=year, type=type:date, value=2010, match=match:eq}" +
						"]}" +
					"]}" +
				"]}";
		cqt = new CollectionQueryTree();
		cqt.process(query);
		map = cqt.getRequestMap().get("collection").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}

	@Test
	public void testDate() throws QueryException {
		// search for pubDate between 1990 and 2010!
		query = "1990<pubDate<2010";
		expected = 
				"{@type=korap:docGroup, operation=operation:and, operands=[" +
					"{@type=korap:doc, key=pubDate, type=type:date, value=1990, match=match:gt}," +
					"{@type=korap:doc, key=pubDate, type=type:date, value=2010, match=match:lt}" +
				"]}";
		cqt = new CollectionQueryTree();
		cqt.process(query);
		map = cqt.getRequestMap().get("collection").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "pubDate>=1990";
		expected = 
				"{@type=korap:doc, key=pubDate, type=type:date, value=1990, match=match:geq}";
		cqt = new CollectionQueryTree();
		cqt.process(query);
		map = cqt.getRequestMap().get("collection").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "pubDate>=1990-05";
		expected = 
				"{@type=korap:doc, key=pubDate, type=type:date, value=1990-05, match=match:geq}";
		cqt = new CollectionQueryTree();
		cqt.process(query);
		map = cqt.getRequestMap().get("collection").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "pubDate>=1990-05-01";
		expected = 
				"{@type=korap:doc, key=pubDate, type=type:date, value=1990-05-01, match=match:geq}";
		cqt = new CollectionQueryTree();
		cqt.process(query);
		map = cqt.getRequestMap().get("collection").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}

	@Test
	public void testRegex() throws QueryException {
		query = "author=/Go.*he/";
		expected = 
				"{@type=korap:doc, key=author, value=Go.*he, type=type:regex, match=match:eq}";
		cqt = new CollectionQueryTree();
		cqt.process(query);
		map = cqt.getRequestMap().get("collection").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testContentFilter() throws QueryException {
		query = "[base=Schwalbe]";
		expected = 
				"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Schwalbe, match=match:eq}}";
		cqt = new CollectionQueryTree();
		cqt.process(query);
		map = cqt.getRequestMap().get("collection").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "[cnx/base=Schwalbe]";
		expected = 
				"{@type=korap:token, wrap={@type=korap:term, foundry=cnx, layer=lemma, key=Schwalbe, match=match:eq}}";
		cqt = new CollectionQueryTree();
		cqt.process(query);
		map = cqt.getRequestMap().get("collection").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "[base!=Schwalbe]";
		expected = 
				"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Schwalbe, match=match:ne}}";
		cqt = new CollectionQueryTree();
		cqt.process(query);
		map = cqt.getRequestMap().get("collection").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "[base=Schwalbe] & [orth=Foul]";
		expected = 
				"{@type=korap:docGroup, operation=operation:and, operands=[" +
						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Schwalbe, match=match:eq}}," +
						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Foul, match=match:eq}}" +
					"]}";
		cqt = new CollectionQueryTree();
		cqt.process(query);
		map = cqt.getRequestMap().get("collection").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testContentMetaMixed() throws QueryException {
		query = "textClass=Sport & [base=Schwalbe]";
		expected = 
				"{@type=korap:docGroup, operation=operation:and, operands=[" +
					"{@type=korap:doc, key=textClass, value=Sport, match=match:eq}," +
					"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Schwalbe, match=match:eq}}" +
				"]}";
		cqt = new CollectionQueryTree();
		cqt.process(query);
		map = cqt.getRequestMap().get("collection").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "[base=Schwalbe] & textClass=Sport";
		expected = 
				"{@type=korap:docGroup, operation=operation:and, operands=[" +
					"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Schwalbe, match=match:eq}}," +
					"{@type=korap:doc, key=textClass, value=Sport, match=match:eq}" +
				"]}";
		cqt = new CollectionQueryTree();
		cqt.process(query);
		map = cqt.getRequestMap().get("collection").toString();
		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}

}

