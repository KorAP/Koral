package de.ids_mannheim.korap.query.serialize;

import java.util.Map;

import de.ids_mannheim.korap.query.object.KoralObject;
import de.ids_mannheim.korap.query.parse.fcsql.FCSSRUQueryParser;
import de.ids_mannheim.korap.query.serialize.util.KoralException;
import de.ids_mannheim.korap.query.serialize.util.StatusCodes;
import eu.clarin.sru.server.SRUQueryBase;
import eu.clarin.sru.server.fcs.Constants;
import eu.clarin.sru.server.fcs.parser.QueryNode;
import eu.clarin.sru.server.fcs.parser.QueryParser;
import eu.clarin.sru.server.fcs.parser.QueryParserException;

/** FCSQLQueryProcessor is accountable for the serialization of FCSQL 
 *  to KoralQuery. The KoralQuery is structured as a map containing 
 *  parts of JSON-LD serializations of KoralObjects.  
 * 
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

    private final QueryParser fcsParser = new QueryParser();
    
    /** Constructs FCSQLQueryProcessor for the given query and version.
     * @param query an FCS query string
     */
    public FCSQLQueryProcessor (String query) {
        super();
        process(query);
    }

    @Override
    public Map<String, Object> getRequestMap() {
        return this.requestMap;
    }

    @Override
    public void process(String query) {
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

    /** Translates the given FCS query string into an FCSSSRUQuery object.
     * @param query an FCS query string
     * @return an FCSSRUQuery
     */
    private FCSSRUQuery parseQueryStringtoFCSQuery(String query) {
        if ((query == null) || query.isEmpty())
            addError(StatusCodes.NO_QUERY,
                    "No query has been passed.");
        FCSSRUQuery fcsQuery = null;
        try {
            QueryNode parsedQuery = fcsParser.parse(query);
            fcsQuery = new FCSSRUQuery(query, parsedQuery);
            if (fcsQuery == null) {
                addError(StatusCodes.UNKNOWN_QUERY_ERROR,
                        "Unexpected error while parsing query.");
            }
        }
        catch (QueryParserException e) {
            addError(
                    StatusCodes.UNKNOWN_QUERY_ERROR,
                    "Query cannot be parsed, "
                            + e.getMessage());
        }
        catch (Exception e) {
            addError(StatusCodes.UNKNOWN_QUERY_ERROR, "Unexpected error while parsing query.");
        }
        return fcsQuery;
    }

    /** Generates a query map structure for the given FCS query node.
     * 
     * @param queryNode an FCS query node
     * @throws KoralException
     */
    private void parseFCSQueryToKoralQuery(QueryNode queryNode) throws KoralException {
        FCSSRUQueryParser parser = new FCSSRUQueryParser();
        KoralObject o = parser.parseQueryNode(queryNode);
        Map<String, Object> queryMap = MapBuilder.buildQueryMap(o);
        if (queryMap != null) requestMap.put("query", queryMap);
    }
}
