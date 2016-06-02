package de.ids_mannheim.korap.query.parse.fcsql;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import de.ids_mannheim.korap.query.elements.KoralRelation;
import de.ids_mannheim.korap.query.elements.KoralTerm;
import de.ids_mannheim.korap.query.elements.KoralTerm.KoralTermType;
import de.ids_mannheim.korap.query.elements.KoralTermGroup;
import de.ids_mannheim.korap.query.elements.KoralToken;
import de.ids_mannheim.korap.query.elements.MatchOperator;
import de.ids_mannheim.korap.query.serialize.FCSQLQueryProcessor;
import de.ids_mannheim.korap.query.serialize.util.StatusCodes;
import eu.clarin.sru.server.fcs.parser.Expression;
import eu.clarin.sru.server.fcs.parser.ExpressionAnd;
import eu.clarin.sru.server.fcs.parser.ExpressionNot;
import eu.clarin.sru.server.fcs.parser.ExpressionOr;
import eu.clarin.sru.server.fcs.parser.Operator;
import eu.clarin.sru.server.fcs.parser.QueryNode;
import eu.clarin.sru.server.fcs.parser.RegexFlag;

/**
 * @author margaretha
 * 
 */
public class ExpressionParser {

    private static final String FOUNDRY_CNX = "cnx";
    private static final String FOUNDRY_OPENNLP = "opennlp";
    private static final String FOUNDRY_TT = "tt";
    private static final String FOUNDRY_MATE = "mate";
    private static final String FOUNDRY_XIP = "xip";

    private List<String> supportedFoundries = Arrays
            .asList(new String[] { FOUNDRY_CNX, FOUNDRY_OPENNLP, FOUNDRY_TT,
                    FOUNDRY_MATE, FOUNDRY_XIP });

    private FCSQLQueryProcessor processor;

    public ExpressionParser (FCSQLQueryProcessor processor) {
        this.processor = processor;
    }

    public Object parseExpression(QueryNode queryNode) {
        return parseExpression(queryNode, false, true);
    }

    public Object parseExpression(QueryNode queryNode, boolean isNot,
            boolean isToken) {

        if (queryNode instanceof Expression) {
            return parseSimpleExpression((Expression) queryNode, isNot, isToken);
        }
        else if (queryNode instanceof ExpressionAnd) {
            List<QueryNode> operands = queryNode.getChildren();
            if (isNot) {
                return parseBooleanExpression(operands, KoralRelation.OR);
            }
            else {
                return parseBooleanExpression(operands, KoralRelation.AND);
            }
        }
        // else if (queryNode instanceof ExpressionGroup) {
        //
        // }
        else if (queryNode instanceof ExpressionNot) {
            boolean negation = isNot ? false : true;
            return parseExpression(queryNode.getChild(0), negation, isToken);
        }
        else if (queryNode instanceof ExpressionOr) {
            List<QueryNode> operands = queryNode.getChildren();
            if (isNot) {
                return parseBooleanExpression(operands, KoralRelation.AND);
            }
            else {
                return parseBooleanExpression(operands, KoralRelation.OR);
            }
        }
        // else if (queryNode instanceof ExpressionWildcard) {
        // for distance query, using empty token
        // }
        else {
            processor.addError(StatusCodes.QUERY_TOO_COMPLEX,
                    "FCS diagnostic 11: Query is too complex.");
            return null;
        }
    }

    private Object parseBooleanExpression(List<QueryNode> operands,
            KoralRelation relation) {
        KoralTermGroup termGroup = new KoralTermGroup(this, relation, operands);
        return new KoralToken(termGroup);
    }

    private Object parseSimpleExpression(Expression expression, boolean isNot,
            boolean isToken) {
        KoralTerm koralTerm = parseTerm(expression, isNot);
        if (isToken) {
            return new KoralToken(koralTerm);
        }
        else {
            return koralTerm;
        }
    }

    public KoralTerm parseTerm(Expression expression, boolean isNot) {
        KoralTerm koralTerm = new KoralTerm();
        koralTerm.setType(KoralTermType.REGEX);
        koralTerm.setKey(expression.getRegexValue());
        parseLayerIdentifier(koralTerm, expression.getLayerIdentifier());
        parseQualifier(koralTerm, expression.getLayerQualifier());
        parseOperator(koralTerm, expression.getOperator(), isNot);
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

    private void parseOperator(KoralTerm koralTerm, Operator operator,
            boolean isNot) {
        MatchOperator matchOperator = null;
        if (operator == null || operator == Operator.EQUALS) {
            matchOperator = isNot ? MatchOperator.NOT_EQUALS
                    : MatchOperator.EQUALS;
        }
        else if (operator == Operator.NOT_EQUALS) {
            matchOperator = isNot ? MatchOperator.EQUALS
                    : MatchOperator.NOT_EQUALS;
        }
        else {
            processor
                    .addError(StatusCodes.UNKNOWN_QUERY_ELEMENT,
                            "SRU diagnostic 37:" + operator.name()
                                    + " is unsupported.");
            koralTerm.setInvalid(true);
        }
        koralTerm.setOperator(matchOperator.toString());
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
