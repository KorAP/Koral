import static org.junit.Assert.*;

import org.junit.Test;

import de.ids_mannheim.korap.query.serialize.ExpertFilter;
import de.ids_mannheim.korap.util.QueryException;

public class ExpertFilterTest {
	
	ExpertFilter ef;
	String map;
	private String query;
	
	@Test
	public void testSimple() throws QueryException {
		query = "textClass=Sport";
		String q1 = "{@type=korap:filter, filter={@type=korap:term, attribute=textClass, key=Sport, match=match:eq}}";
		ef = new ExpertFilter();
		ef.process(query);
		map = ef.getRequestMap().toString();
		assertEquals(q1.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testMultiwordValue() throws QueryException {
		query = "title=\"Ein langer langer Titel\"";
		String q1 = "{@type=korap:filter, filter={@type=korap:term, attribute=title, key=Ein langer langer Titel, match=match:eq}}";
		ef = new ExpertFilter();
		ef.process(query);
		map = ef.getRequestMap().toString();
		assertEquals(q1.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testRegex() throws QueryException {
		query = "title=/Sp.*rt/";
		String q1 = "{@type=korap:filter, filter={@type=korap:term, attribute=title, key=Sp.*rt, type=type:regex, match=match:eq}}";
		ef = new ExpertFilter();
		ef.process(query);
		map = ef.getRequestMap().toString();
		assertEquals(q1.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testNotEqual() throws QueryException {
		query = "textClass!=Sport";
		String q1 = "{@type=korap:filter, filter={@type=korap:term, attribute=textClass, key=Sport, match=match:ne}}";
		ef = new ExpertFilter();
		ef.process(query);
		map = ef.getRequestMap().toString();
		assertEquals(q1.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	
	@Test
	public void testConj() throws QueryException {
		query = "textClass=Drama&author=Goethe";
		String q1 = 
				"{@type=korap:filter, filter=" +
					"{@type=korap:termGroup, relation=relation:and, operands=[" +
						"{@type=korap:term, attribute=textClass, key=Drama, match=match:eq}," +
						"{@type=korap:term, attribute=author, key=Goethe, match=match:eq}" +
					"]}" +
				"}";
		ef = new ExpertFilter();
		ef.process(query);
		map = ef.getRequestMap().toString();
		assertEquals(q1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "textClass=Drama&author=Goethe&year=1815";
		String q2 = 
				"{@type=korap:filter, filter=" +
					"{@type=korap:termGroup, relation=relation:and, operands=[" +
						"{@type=korap:term, attribute=textClass, key=Drama, match=match:eq}," +
						"{@type=korap:term, attribute=author, key=Goethe, match=match:eq}," +
						"{@type=korap:term, attribute=year, key=1815, match=match:eq}" +
					"]}" +
				"}";
		ef = new ExpertFilter();
		ef.process(query);
		map = ef.getRequestMap().toString();
		assertEquals(q2.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testDisj() throws QueryException {
		query = "textClass=Drama|author=Goethe";
		String q1 = 
				"{@type=korap:filter, filter=" +
					"{@type=korap:termGroup, relation=relation:or, operands=[" +
						"{@type=korap:term, attribute=textClass, key=Drama, match=match:eq}," +
						"{@type=korap:term, attribute=author, key=Goethe, match=match:eq}" +
					"]}" +
				"}";
		ef = new ExpertFilter();
		ef.process(query);
		map = ef.getRequestMap().toString();
		assertEquals(q1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "textClass=Drama|author=Goethe|year=1815";
		String q2 = 
				"{@type=korap:filter, filter=" +
					"{@type=korap:termGroup, relation=relation:or, operands=[" +
						"{@type=korap:term, attribute=textClass, key=Drama, match=match:eq}," +
						"{@type=korap:term, attribute=author, key=Goethe, match=match:eq}," +
						"{@type=korap:term, attribute=year, key=1815, match=match:eq}" +
					"]}" +
				"}";
		ef = new ExpertFilter();
		ef.process(query);
		map = ef.getRequestMap().toString();
		assertEquals(q2.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testComplex() throws QueryException {
		query = "textClass=Drama|(author=Goethe&year=1815)";
		String q1 = 
				"{@type=korap:filter, filter=" +
					"{@type=korap:termGroup, relation=relation:or, operands=[" +
						"{@type=korap:term, attribute=textClass, key=Drama, match=match:eq}," +
						"{@type=korap:termGroup, relation=relation:and, operands=[" +
							"{@type=korap:term, attribute=author, key=Goethe, match=match:eq}," +
							"{@type=korap:term, attribute=year, key=1815, match=match:eq}" +
						"]}" +
					"]}" +
				"}";
		ef = new ExpertFilter();
		ef.process(query);
		map = ef.getRequestMap().toString();
		assertEquals(q1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "textClass=Drama|(author=Goethe&year=1815)|textClass=Politik";
		String q2 = 
				"{@type=korap:filter, filter=" +
					"{@type=korap:termGroup, relation=relation:or, operands=[" +
						"{@type=korap:term, attribute=textClass, key=Drama, match=match:eq}," +
						"{@type=korap:termGroup, relation=relation:and, operands=[" +
							"{@type=korap:term, attribute=author, key=Goethe, match=match:eq}," +
							"{@type=korap:term, attribute=year, key=1815, match=match:eq}" +
						"]}," +
						"{@type=korap:term, attribute=textClass, key=Politik, match=match:eq}" +
					"]}" +
				"}";
		ef = new ExpertFilter();
		ef.process(query);
		map = ef.getRequestMap().toString();
		assertEquals(q2.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "(author=Goethe&year=1815)|textClass=Drama";
		String q3 = 
				"{@type=korap:filter, filter=" +
					"{@type=korap:termGroup, relation=relation:or, operands=[" +
						
						"{@type=korap:termGroup, relation=relation:and, operands=[" +
							"{@type=korap:term, attribute=author, key=Goethe, match=match:eq}," +
							"{@type=korap:term, attribute=year, key=1815, match=match:eq}" +
						"]}," +
						"{@type=korap:term, attribute=textClass, key=Drama, match=match:eq}" +
					"]}" +
				"}";
		ef = new ExpertFilter();
		ef.process(query);
		map = ef.getRequestMap().toString();
		assertEquals(q3.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "(author=Goethe|year=1815)&textClass=Drama";
		String q4 = 
				"{@type=korap:filter, filter=" +
					"{@type=korap:termGroup, relation=relation:and, operands=[" +
						
						"{@type=korap:termGroup, relation=relation:or, operands=[" +
							"{@type=korap:term, attribute=author, key=Goethe, match=match:eq}," +
							"{@type=korap:term, attribute=year, key=1815, match=match:eq}" +
						"]}," +
						"{@type=korap:term, attribute=textClass, key=Drama, match=match:eq}" +
					"]}" +
				"}";
		ef = new ExpertFilter();
		ef.process(query);
		map = ef.getRequestMap().toString();
		assertEquals(q4.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "(author=Goethe&year=1815)&textClass=Drama";
		String q5 = 
				"{@type=korap:filter, filter=" +
					"{@type=korap:termGroup, relation=relation:and, operands=[" +
						
						"{@type=korap:termGroup, relation=relation:and, operands=[" +
							"{@type=korap:term, attribute=author, key=Goethe, match=match:eq}," +
							"{@type=korap:term, attribute=year, key=1815, match=match:eq}" +
						"]}," +
						"{@type=korap:term, attribute=textClass, key=Drama, match=match:eq}" +
					"]}" +
				"}";
		ef = new ExpertFilter();
		ef.process(query);
		map = ef.getRequestMap().toString();
		assertEquals(q5.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "(textClass=wissenschaft & textClass=politik) & (corpusID=A00 | corpusID=WPD)";
		String q6 = 
				"{@type=korap:filter, filter=" +
					"{@type=korap:termGroup, relation=relation:and, operands=[" +
						
						"{@type=korap:termGroup, relation=relation:and, operands=[" +
							"{@type=korap:term, attribute=textClass, key=wissenschaft, match=match:eq}," +
							"{@type=korap:term, attribute=textClass, key=politik, match=match:eq}" +
						"]}," +
						"{@type=korap:termGroup, relation=relation:or, operands=[" +
							"{@type=korap:term, attribute=corpusID, key=A00, match=match:eq}," +
							"{@type=korap:term, attribute=corpusID, key=WPD, match=match:eq}" +
						"]}" +
					"]}" +
				"}";
		ef = new ExpertFilter();
		ef.process(query);
		map = ef.getRequestMap().toString();
		assertEquals(q6.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	@Test
	public void testDateRange() throws QueryException {
		query = "1990<year<2010";
		String q1 = 
				"{@type=korap:filter, filter=" +
					"{@type=korap:termGroup, relation=relation:and, operands=[" +
						"{@type=korap:term, attribute=year, key=1990, match=match:gt}," +
						"{@type=korap:term, attribute=year, key=2010, match=match:lt}" +
					"]}" +
				"}";
		ef = new ExpertFilter();
		ef.process(query);
		map = ef.getRequestMap().toString();
		assertEquals(q1.replaceAll(" ", ""), map.replaceAll(" ", ""));
		
		query = "1990<year<=2010";
		String q2 = 
				"{@type=korap:filter, filter=" +
					"{@type=korap:termGroup, relation=relation:and, operands=[" +
						"{@type=korap:term, attribute=year, key=1990, match=match:gt}," +
						"{@type=korap:term, attribute=year, key=2010, match=match:leq}" +
					"]}" +
				"}";
		ef = new ExpertFilter();
		ef.process(query);
		map = ef.getRequestMap().toString();
		assertEquals(q2.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
}

