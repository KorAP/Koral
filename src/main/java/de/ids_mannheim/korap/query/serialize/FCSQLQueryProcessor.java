package de.ids_mannheim.korap.query.serialize;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.ids_mannheim.korap.query.serialize.util.StatusCodes;
import eu.clarin.sru.server.SRUQueryBase;
import eu.clarin.sru.server.SRUVersion;
import eu.clarin.sru.server.fcs.Constants;
import eu.clarin.sru.server.fcs.parser.Expression;
import eu.clarin.sru.server.fcs.parser.Operator;
import eu.clarin.sru.server.fcs.parser.QueryNode;
import eu.clarin.sru.server.fcs.parser.QueryParser;
import eu.clarin.sru.server.fcs.parser.QueryParserException;
import eu.clarin.sru.server.fcs.parser.QuerySegment;
import eu.clarin.sru.server.fcs.parser.RegexFlag;

public class FCSQLQueryProcessor extends AbstractQueryProcessor {

	public static final class FCSQuery extends SRUQueryBase<QueryNode> {

		private FCSQuery(String rawQuery, QueryNode parsedQuery) {
			super(rawQuery, parsedQuery);
		}

		@Override
		public String getQueryType() {
			return Constants.FCS_QUERY_TYPE_FCS;
		}
	}

	public enum Foundry {
		CNX, OPENNLP, TT, MATE, XIP;
	}

	private static final String KORAP_CONTEXT = "http://ids-mannheim.de/ns/KorAP/json-ld/v0.1/context.jsonld";
	private String version;
	private List<Foundry> supportedFoundries;
	private final QueryParser parser = new QueryParser();

	public FCSQLQueryProcessor(String query, String version) {
		if (version == null) {
			addError(StatusCodes.MISSING_VERSION,
					"SRU Diagnostic 7: Version number is missing.");
		} else if (!version.equals(SRUVersion.VERSION_2_0)) {
			addError(StatusCodes.MISSING_VERSION,
					"SRU Diagnostic 5: Only supports SRU version 2.0.");
		}
		this.version = version;

		this.requestMap = new LinkedHashMap<>();
		requestMap.put("@context", KORAP_CONTEXT);

		this.supportedFoundries = new ArrayList<Foundry>(5);
		supportedFoundries.add(Foundry.CNX);
		supportedFoundries.add(Foundry.OPENNLP);
		supportedFoundries.add(Foundry.TT);
		supportedFoundries.add(Foundry.MATE);
		supportedFoundries.add(Foundry.XIP);

		process(query);
	}

	@Override
	public Map<String, Object> getRequestMap() {
		return this.requestMap;
	}

	@Override
	public void process(String query) {
		FCSQuery fcsQuery = parseQueryStringtoFCSQuery(query);
		QueryNode fcsQueryNode = fcsQuery.getParsedQuery();
		Map<String, Object> queryMap = parseFCSQuery(fcsQueryNode);
		requestMap.put("query", queryMap);
	}

	private FCSQuery parseQueryStringtoFCSQuery(String query) {
		if ((query == null) || query.isEmpty())
			addError(StatusCodes.MALFORMED_QUERY,
					"SRU diagnostic 1: No query has been passed.");
		FCSQuery fcsQuery = null;
		try {
			QueryNode parsedQuery = parser.parse(query);
			fcsQuery = new FCSQuery(query, parsedQuery);
		} catch (QueryParserException e) {
			addError(StatusCodes.UNKNOWN_QUERY_ERROR, "FCS Diagnostic 10: +"
					+ e.getMessage());
		}
		catch (Exception e) {
			addError(StatusCodes.UNKNOWN_QUERY_ERROR, "FCS Diagnostic 10: +"
					+ "Unexpected error while parsing query.");
		}
		return fcsQuery;
	}

	private Map<String, Object> parseFCSQuery(QueryNode queryNode) {
		Map<String, Object> queryMap = parseQueryNode(queryNode);
		if (queryMap == null) {
			addError(StatusCodes.UNKNOWN_QUERY_ERROR, "SRU diagnostic 47:"
					+ " Failed parsing query for unknown reasons.");
		}
		return queryMap;

	}

	private Map<String, Object> parseQueryNode(QueryNode queryNode) {
		Map<String, Object> queryMap = null;

		if (queryNode instanceof QuerySegment) {
			queryMap = parseQuerySegment((QuerySegment) queryNode);
//		} else if (queryNode instanceof QueryGroup) {
//
//		} else if (queryNode instanceof QuerySequence) {
//
//		} else if (queryNode instanceof QueryDisjunction) {
//
//		} else if (queryNode instanceof QueryWithWithin) {

		}else {
			addError(StatusCodes.QUERY_TOO_COMPLEX, "FCS diagnostic 11:"
					+ queryNode.getNodeType().name()
					+ " is currently unsupported.");
		}

		return queryMap;
	}

	private Map<String, Object> parseQuerySegment(QuerySegment segment) {
		Map<String, Object> queryMap = null;

		if ((segment.getMinOccurs() == 1) && (segment.getMaxOccurs() == 1)) {
			queryMap = parseExpression(segment.getExpression());
		} else {
			addError(StatusCodes.QUERY_TOO_COMPLEX, "FCS diagnostic 11:"
					+ "Query is too complex.");
		}
		return queryMap;
	}

	private Map<String, Object> parseExpression(QueryNode queryNode) {
		Map<String, Object> queryMap = null;

		if (queryNode instanceof Expression) {
			Expression expression = (Expression) queryNode;
			queryMap = parseLayer(expression);
		}
		// else if (queryNode instanceof ExpressionAnd) {
		//
		// }
		// else if (queryNode instanceof ExpressionGroup) {
		//
		// }
		// else if (queryNode instanceof ExpressionNot) {
		//
		// }
		// else if (queryNode instanceof ExpressionOr) {
		//
		// }
		// else if (queryNode instanceof ExpressionWildcard) {
		//
		// }
		else {
			addError(StatusCodes.QUERY_TOO_COMPLEX, "FCS diagnostic 11:"
					+ "Query is too complex.");
		}
		return queryMap;
	}

	private Map<String, Object> parseLayer(Expression expression) {
		String layer = parseLayerIdentifier(expression.getLayerIdentifier());
		String foundry = parseQualifier(expression.getLayerQualifier(), layer);
		String operator = parseOperator(expression.getOperator());
		boolean isCaseSensitive = parseRegexFlags(expression.getRegexFlags());		
		String term = expression.getRegexValue();
		
		return writeTerm(term, foundry, layer, operator, isCaseSensitive);
	}
	private String parseLayerIdentifier(String identifier) {
		String layer = null;
		if (identifier == null) {
			// throw exception
		} else if (identifier.equals("text")) {
			layer = "orth";
		} else if (identifier.equals("pos")) {
			layer = "p";
		} else if (identifier.equals("lemma")) {
			layer = "l";
		} else {
			addError(StatusCodes.UNKNOWN_QUERY_ELEMENT, "SRU diagnostic 48:"
					+ identifier + " is unsupported.");
		}

		return layer;
	}
	
	private String parseQualifier(String qualifier, String layer) {
		// Set default foundry
		if (qualifier == null) {
			if (layer.equals("orth")) {
				qualifier = Foundry.OPENNLP.name().toLowerCase();
			} else {
				qualifier = Foundry.TT.name().toLowerCase();
			}
		} else if (qualifier.equals(Foundry.OPENNLP.name().toLowerCase())
				&& layer.equals("lemma")) {
			addError(StatusCodes.UNKNOWN_QUERY_ELEMENT, "SRU diagnostic 48:"
					+ "Layer lemma with qualifier opennlp is unsupported.");
		} else if (!supportedFoundries.contains(qualifier)) {
			addError(StatusCodes.UNKNOWN_QUERY_ELEMENT, "SRU diagnostic 48:"
					+ "Layer " + layer + " with qualifier" + qualifier
					+ " is unsupported.");
		}
		return qualifier;
	}
	
	private String parseOperator(Operator operator) {
		String matchOperator = null;
		if (operator == null || operator == Operator.EQUALS) {
			matchOperator = "match:eq";
		} else if (operator == Operator.NOT_EQUALS) {
			matchOperator = "match:ne";
		} else {
			addError(StatusCodes.UNKNOWN_QUERY_ELEMENT, "SRU diagnostic 37:"
					+ operator.name() + " is unsupported.");
		}
		return matchOperator;
	}
	
	private boolean parseRegexFlags(Set<RegexFlag> set) {
		// default case sensitive
		boolean flag = true;
		if (set != null) {
			for (RegexFlag f : set) {
				if (f == RegexFlag.CASE_SENSITVE) {
					continue;
				} else if (f == RegexFlag.CASE_INSENSITVE) {
					flag = false;
				} else {
					addError(StatusCodes.UNKNOWN_QUERY_ELEMENT,
							"SRU diagnostic 48:" + f.name()
									+ " is unsupported.");
				}
			}
		}
		return flag;
	}

	private Map<String, Object> writeTerm(String term, String foundry,
			String layer, String operator, boolean isCaseSensitive) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("@type", "koral:term");
		if (!isCaseSensitive) {
			map.put("caseInsensitive", "true");
		}
		map.put("key", term);
		map.put("foundry", foundry);
		map.put("layer", layer);
		map.put("match", operator);

		Map<String, Object> tokenMap = new LinkedHashMap<String, Object>();
		tokenMap.put("@type", "koral:token");
		tokenMap.put("wrap", map);
		return tokenMap;
	}

}
