package de.ids_mannheim.korap.query.serialize;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.ids_mannheim.korap.query.parse.fcsql.FCSSRUQueryParser;
import de.ids_mannheim.korap.query.parse.fcsql.KoralSequence;
import de.ids_mannheim.korap.query.parse.fcsql.KoralSequence.Distance;
import de.ids_mannheim.korap.query.parse.fcsql.KoralTerm;
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
    private static final String OPERATION_OR = "operation:or";
    private static final String OPERATION_SEQUENCE = "operation:sequence";
    private static final String OPERATION_POSITION = "operation:position";

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
            QueryNode fcsQueryNode = fcsSruQuery.getParsedQuery();
            parseFCSQueryToKoralQuery(fcsQueryNode);
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
        }
        catch (QueryParserException e) {
            addError(StatusCodes.UNKNOWN_QUERY_ERROR, "FCS diagnostic 10: +"
                    + e.getMessage());
        }
        catch (Exception e) {
            addError(StatusCodes.UNKNOWN_QUERY_ERROR, "FCS diagnostic 10: +"
                    + "Unexpected error while parsing query.");
        }
        return fcsQuery;
    }

    private void parseFCSQueryToKoralQuery(QueryNode queryNode) {
        FCSSRUQueryParser parser = new FCSSRUQueryParser(this);
        Object o = parser.parseQueryNode(queryNode);
        Map<String, Object> queryMap = buildQueryMap(o);
        if (queryMap != null) requestMap.put("query", queryMap);
    }

    private Map<String, Object> buildQueryMap(Object o) {
        if (o != null) {
            if (o instanceof KoralTerm) {
                KoralTerm koralTerm = (KoralTerm) o;
                if (!koralTerm.isInvalid()) {
                    return createTermMap(koralTerm);
                }
            }
            else if (o instanceof KoralSequence) {
                KoralSequence koralSequence = (KoralSequence) o;
                return createSequenceMap(koralSequence);
            }
        }
        return null;
    }

    private Map<String, Object> createSequenceMap(KoralSequence koralSequence) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("@type", "koral:group");
        map.put("operation", OPERATION_SEQUENCE);
        map.put("inOrder", koralSequence.isInOrder());

        if (koralSequence.getDistances() != null) {
            List<Map<String, Object>> distanceList = new ArrayList<Map<String, Object>>();
            for (Distance d : koralSequence.getDistances()) {
                distanceList.add(createDistanceMap(d));
            }
            map.put("distances", distanceList);
        }

        List<Map<String, Object>> operandList = new ArrayList<Map<String, Object>>();
        for (Object o : koralSequence.getOperands()) {
            operandList.add(buildQueryMap(o));
        }
        map.put("operands", operandList);
        return map;
    }

    private Map<String, Object> createDistanceMap(Distance distance) {
        Map<String, Object> distanceMap = new LinkedHashMap<String, Object>();
        distanceMap.put("@type", "koral:distance");
        distanceMap.put("key", distance.getKey());
        distanceMap.put("min", distance.getMin());
        distanceMap.put("max", distance.getMax());
        return distanceMap;

    }

    private Map<String, Object> createTermMap(KoralTerm fcsQuery) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("@type", "koral:term");
        if (!fcsQuery.isCaseSensitive()) {
            map.put("caseInsensitive", "true");
        }
        map.put("key", fcsQuery.getQueryTerm());
        map.put("foundry", fcsQuery.getFoundry());
        map.put("layer", fcsQuery.getLayer());
        map.put("match", fcsQuery.getOperator());

        Map<String, Object> tokenMap = new LinkedHashMap<String, Object>();
        tokenMap.put("@type", "koral:token");
        tokenMap.put("wrap", map);
        return tokenMap;
    }

}
