package de.ids_mannheim.korap.query.serialize;

import java.util.Map;

import de.ids_mannheim.korap.query.parse.fcsql.FCSSRUQueryParser;
import de.ids_mannheim.korap.query.serialize.util.KoralException;
import de.ids_mannheim.korap.query.serialize.util.StatusCodes;
import eu.clarin.sru.server.SRUQueryBase;
import eu.clarin.sru.server.fcs.Constants;
import eu.clarin.sru.server.fcs.parser.QueryNode;
import eu.clarin.sru.server.fcs.parser.QueryParser;
import eu.clarin.sru.server.fcs.parser.QueryParserException;

/**
 * @author margaretha
 * 
 */
public class FCSQLQueryProcessor extends AbstractQueryProcessor {

    public static final class FCSSRUQuery extends SRUQueryBase<QueryNode> {

        private FCSSRUQuery (String rawQuery, QueryNode parsedQuery) {
            super(rawQuery, parsedQuery);
        }

        @Override
        public String getQueryType() {
            return Constants.FCS_QUERY_TYPE_FCS;
        }
    }

    private static final String VERSION_2_0 = "2.0";

    private final QueryParser fcsParser = new QueryParser();
    private String version;

    public FCSQLQueryProcessor (String query, String version) {
        super();
        this.version = version;
        process(query);
    }

    @Override
    public Map<String, Object> getRequestMap() {
        return this.requestMap;
    }

    @Override
    public void process(String query) {
        if (isVersionValid()) {
            FCSSRUQuery fcsSruQuery = parseQueryStringtoFCSQuery(query);
            if (fcsSruQuery != null) {
                QueryNode fcsQueryNode = fcsSruQuery.getParsedQuery();
                try {
					parseFCSQueryToKoralQuery(fcsQueryNode);
				} catch (KoralException e) {
					addError(e.getStatusCode(), e.getMessage());
				}
            }
        }
    }

    private boolean isVersionValid() {
        if (version == null || version.isEmpty()) {
            addError(StatusCodes.MISSING_VERSION,
                    "SRU diagnostic 7: Version number is missing.");
            return false;
        }
        else if (!version.equals(VERSION_2_0)) {
            addError(StatusCodes.MISSING_VERSION,
                    "SRU diagnostic 5: Only supports SRU version 2.0.");
            return false;
        }
        return true;
    }

    private FCSSRUQuery parseQueryStringtoFCSQuery(String query) {
        if ((query == null) || query.isEmpty())
            addError(StatusCodes.NO_QUERY,
                    "SRU diagnostic 1: No query has been passed.");
        FCSSRUQuery fcsQuery = null;
        try {
            QueryNode parsedQuery = fcsParser.parse(query);
            fcsQuery = new FCSSRUQuery(query, parsedQuery);
            if (fcsQuery == null) {
                addError(StatusCodes.UNKNOWN_QUERY_ERROR,
                        "FCS diagnostic 10: Unexpected error while parsing query.");
            }
        }
        catch (QueryParserException e) {
            addError(
                    StatusCodes.UNKNOWN_QUERY_ERROR,
                    "FCS diagnostic 10: Query cannot be parsed, "
                            + e.getMessage());
        }
        catch (Exception e) {
            addError(StatusCodes.UNKNOWN_QUERY_ERROR, "FCS diagnostic 10: "
                    + "Unexpected error while parsing query.");
        }
        return fcsQuery;
    }

    private void parseFCSQueryToKoralQuery(QueryNode queryNode) throws KoralException {
        FCSSRUQueryParser parser = new FCSSRUQueryParser();
        Object o = parser.parseQueryNode(queryNode);
        Map<String, Object> queryMap = MapBuilder.buildQueryMap(o);
        if (queryMap != null) requestMap.put("query", queryMap);
    }
}
