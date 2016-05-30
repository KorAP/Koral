package de.ids_mannheim.korap.query.parse.fcsql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import de.ids_mannheim.korap.query.parse.fcsql.KoralSequence.Distance;
import de.ids_mannheim.korap.query.serialize.FCSQLQueryProcessor;
import de.ids_mannheim.korap.query.serialize.util.StatusCodes;
import eu.clarin.sru.server.fcs.parser.Expression;
import eu.clarin.sru.server.fcs.parser.ExpressionAnd;
import eu.clarin.sru.server.fcs.parser.Operator;
import eu.clarin.sru.server.fcs.parser.QueryNode;
import eu.clarin.sru.server.fcs.parser.QuerySegment;
import eu.clarin.sru.server.fcs.parser.RegexFlag;

public class FCSSRUQueryParser {

    private static final String FOUNDRY_CNX = "cnx";
    private static final String FOUNDRY_OPENNLP = "opennlp";
    private static final String FOUNDRY_TT = "tt";
    private static final String FOUNDRY_MATE = "mate";
    private static final String FOUNDRY_XIP = "xip";

    private List<String> supportedFoundries = Arrays
            .asList(new String[] { FOUNDRY_CNX, FOUNDRY_OPENNLP, FOUNDRY_TT,
                    FOUNDRY_MATE, FOUNDRY_XIP });

    private FCSQLQueryProcessor processor;

    public FCSSRUQueryParser (FCSQLQueryProcessor processor) {
        this.processor = processor;
    }

    public Object parseQueryNode(QueryNode queryNode) {

        if (queryNode instanceof QuerySegment) {
            return parseQuerySegment((QuerySegment) queryNode);
            // } else if (queryNode instanceof QueryGroup) {
            //
            // } else if (queryNode instanceof QuerySequence) {
            //
            // } else if (queryNode instanceof QueryDisjunction) {
            //
            // } else if (queryNode instanceof QueryWithWithin) {

        }
        else {
            processor.addError(StatusCodes.QUERY_TOO_COMPLEX,
                    "FCS diagnostic 11:" + queryNode.getNodeType().name()
                            + " is currently unsupported.");
            return null;
        }
    }

    private Object parseQuerySegment(QuerySegment segment) {
        if ((segment.getMinOccurs() == 1) && (segment.getMaxOccurs() == 1)) {
            return parseExpression(segment.getExpression());
        }
        else {
            processor.addError(StatusCodes.QUERY_TOO_COMPLEX,
                    "FCS diagnostic 11: Query is too complex.");
            return null;
        }
    }

    private Object parseExpression(QueryNode queryNode) {
        if (queryNode instanceof Expression) {
            Expression expression = (Expression) queryNode;
            return parseSimpleExpression(expression);
        }
        else if (queryNode instanceof ExpressionAnd) {
            ExpressionAnd expressionAnd = (ExpressionAnd) queryNode;
            return parseExpressionAnd(expressionAnd);
        }
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
            processor.addError(StatusCodes.QUERY_TOO_COMPLEX,
                    "FCS diagnostic 11: Query is too complex.");
            return null;
        }
    }

    private Object parseExpressionAnd(ExpressionAnd expressionAnd) {
        KoralSequence koralSequence = new KoralSequence();
        List<Object> operands = new ArrayList<Object>();
        for (QueryNode child : expressionAnd.getChildren()) {
            operands.add(parseExpression(child));
        }

        List<Distance> distances = new ArrayList<Distance>();
        Distance d = koralSequence.new Distance("s", 0, 0);
        distances.add(d);

        koralSequence.setOperands(operands);
        koralSequence.setDistances(distances);
        return koralSequence;
    }

    private Object parseSimpleExpression(Expression expression) {
        KoralTerm koralTerm = new KoralTerm();
        koralTerm.setQueryTerm(expression.getRegexValue());
        parseLayerIdentifier(koralTerm, expression.getLayerIdentifier());
        parseQualifier(koralTerm, expression.getLayerQualifier());
        parseOperator(koralTerm, expression.getOperator());
        parseRegexFlags(koralTerm, expression.getRegexFlags());
        return koralTerm;
    }

    private void parseLayerIdentifier(KoralTerm koralTerm, String identifier) {
        String layer = null;
        if (identifier == null) {
            processor.addError(StatusCodes.MALFORMED_QUERY,
                    "FCS diagnostic 10: Layer identifier is missing.");
            koralTerm.setInvalid(true);
        }
        else if (identifier.equals("text")) {
            layer = "orth";
        }
        else if (identifier.equals("pos")) {
            layer = "p";
        }
        else if (identifier.equals("lemma")) {
            layer = "l";
        }
        else {
            processor.addError(StatusCodes.UNKNOWN_QUERY_ELEMENT,
                    "SRU diagnostic 48: Layer " + identifier
                            + " is unsupported.");
            koralTerm.setInvalid(true);
        }

        koralTerm.setLayer(layer);
    }

    private void parseQualifier(KoralTerm koralTerm, String qualifier) {
        String layer = koralTerm.getLayer();
        if (layer == null) {
            koralTerm.setInvalid(true);
            return;
        }
        // Set default foundry
        if (qualifier == null) {
            if (layer.equals("orth")) {
                qualifier = FOUNDRY_OPENNLP;
            }
            else {
                qualifier = FOUNDRY_TT;
            }
        }
        else if (qualifier.equals(FOUNDRY_OPENNLP) && layer.equals("l")) {
            processor
                    .addError(StatusCodes.UNKNOWN_QUERY_ELEMENT,
                            "SRU diagnostic 48: Layer lemma with qualifier opennlp is unsupported.");
            koralTerm.setInvalid(true);
        }
        else if (!supportedFoundries.contains(qualifier)) {
            processor.addError(StatusCodes.UNKNOWN_QUERY_ELEMENT,
                    "SRU diagnostic 48: Qualifier " + qualifier
                            + " is unsupported.");
            koralTerm.setInvalid(true);
        }

        koralTerm.setFoundry(qualifier);
    }

    private void parseOperator(KoralTerm koralTerm, Operator operator) {
        String matchOperator = null;
        if (operator == null || operator == Operator.EQUALS) {
            matchOperator = "match:eq";
        }
        else if (operator == Operator.NOT_EQUALS) {
            matchOperator = "match:ne";
        }
        else {
            processor
                    .addError(StatusCodes.UNKNOWN_QUERY_ELEMENT,
                            "SRU diagnostic 37:" + operator.name()
                                    + " is unsupported.");
            koralTerm.setInvalid(true);
        }
        koralTerm.setOperator(matchOperator);
    }

    private void parseRegexFlags(KoralTerm koralTerm, Set<RegexFlag> set) {
        // default case sensitive
        if (set != null) {
            for (RegexFlag f : set) {
                if (f == RegexFlag.CASE_SENSITVE) {
                    koralTerm.setCaseSensitive(true);
                }
                else if (f == RegexFlag.CASE_INSENSITVE) {
                    koralTerm.setCaseSensitive(false);
                }
                else {
                    processor.addError(StatusCodes.UNKNOWN_QUERY_ELEMENT,
                            "SRU diagnostic 48:" + f.name()
                                    + " is unsupported.");
                    koralTerm.setInvalid(true);
                }
            }
        }
    }

}
