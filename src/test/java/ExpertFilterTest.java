import static org.junit.Assert.*;

import org.junit.Test;

import de.ids_mannheim.korap.query.serialize.ExpertFilter;
import de.ids_mannheim.korap.util.QueryException;

public class ExpertFilterTest {
	
	ExpertFilter ef;
	String map;
	private String query;

	private boolean equalsQueryContent(String res, String query) throws QueryException {
		res = res.replaceAll(" ", "");
		ef = new ExpertFilter();
		ef.process(query);
		String queryMap = ef.getRequestMap().get("query").toString().replaceAll(" ", "");
		return res.equals(queryMap);
	}
	
	@Test
	public void testSimple() throws QueryException {
		query = "textClass=Sport";
		String regex1 = "{@type=korap:filter, filter={@type=korap:term, attribute=textClass, key=Sport, match=match:eq}}";
		ef = new ExpertFilter();
		ef.process(query);
		map = ef.getRequestMap().toString();
		assertEquals(regex1.replaceAll(" ", ""), map.replaceAll(" ", ""));
	}
	
	
}

