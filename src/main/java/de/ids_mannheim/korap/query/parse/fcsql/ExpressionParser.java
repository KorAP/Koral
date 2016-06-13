package de.ids_mannheim.korap.query.parse.fcsql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.ids_mannheim.korap.query.object.KoralMatchOperator;
import de.ids_mannheim.korap.query.object.KoralObject;
import de.ids_mannheim.korap.query.object.KoralRelation;
import de.ids_mannheim.korap.query.object.KoralTerm;
import de.ids_mannheim.korap.query.object.KoralTerm.KoralTermType;
import de.ids_mannheim.korap.query.object.KoralTermGroup;
import de.ids_mannheim.korap.query.object.KoralToken;
import de.ids_mannheim.korap.query.serialize.util.KoralException;
import de.ids_mannheim.korap.query.serialize.util.StatusCodes;
import eu.clarin.sru.server.fcs.parser.Expression;
import eu.clarin.sru.server.fcs.parser.ExpressionAnd;
import eu.clarin.sru.server.fcs.parser.ExpressionGroup;
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

    public KoralObject parseExpression(QueryNode queryNode) throws KoralException {
        return parseExpression(queryNode, false, true);
    }

    public KoralObject parseExpression(QueryNode queryNode, boolean isNot,
            boolean isToken) throws KoralException {

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
        else if (queryNode instanceof ExpressionGroup) {
            // Ignore the group
            return parseExpression(queryNode.getFirstChild());
        }
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
            throw new KoralException(StatusCodes.QUERY_TOO_COMPLEX,
                    "FCS diagnostic 11: Query is too complex.");
        }
    }

    private KoralToken parseBooleanExpression(List<QueryNode> operands,
            KoralRelation relation) throws KoralException {
        List<KoralObject> terms = new ArrayList<>();
        for (QueryNode node : operands) {
            terms.add(parseExpression(node, false, false));
        }
        KoralTermGroup termGroup = new KoralTermGroup(relation, terms);
        return new KoralToken(termGroup);
    }

    private KoralObject parseSimpleExpression(Expression expression, boolean isNot,
            boolean isToken) throws KoralException {
        KoralTerm koralTerm = parseTerm(expression, isNot);
        if (isToken) {
            return new KoralToken(koralTerm);
        }
        else {
            return koralTerm;
        }
    }

    public KoralTerm parseTerm(Expression expression, boolean isNot) throws KoralException {
    	KoralTerm koralTerm = null;
    	koralTerm = new KoralTerm(expression.getRegexValue());
        koralTerm.setType(KoralTermType.REGEX);
        parseLayerIdentifier(koralTerm, expression.getLayerIdentifier());
        parseQualifier(koralTerm, expression.getLayerQualifier());
        parseOperator(koralTerm, expression.getOperator(), isNot);
        parseRegexFlags(koralTerm, expression.getRegexFlags());
        return koralTerm;
    }

    private void parseLayerIdentifier(KoralTerm koralTerm, String identifier) throws KoralException {
        String layer = null;
        if (identifier == null) {
            throw new KoralException(StatusCodes.MALFORMED_QUERY,
                    "FCS diagnostic 10: Layer identifier is missing.");
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
            throw new KoralException(StatusCodes.UNKNOWN_QUERY_ELEMENT,
                    "SRU diagnostic 48: Layer " + identifier
                            + " is unsupported.");
        }

        koralTerm.setLayer(layer);
    }

    private void parseQualifier(KoralTerm koralTerm, String qualifier) throws KoralException {
        String layer = koralTerm.getLayer();
        if (layer == null) {
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
            throw new KoralException(StatusCodes.UNKNOWN_QUERY_ELEMENT,
                            "SRU diagnostic 48: Layer lemma with qualifier opennlp is unsupported.");
        }
        else if (!supportedFoundries.contains(qualifier)) {
            throw new KoralException(StatusCodes.UNKNOWN_QUERY_ELEMENT,
                    "SRU diagnostic 48: Qualifier " + qualifier
                            + " is unsupported.");
        }

        koralTerm.setFoundry(qualifier);
    }

    private void parseOperator(KoralTerm koralTerm, Operator operator,
            boolean isNot) throws KoralException {
    	KoralMatchOperator matchOperator = null;
        if (operator == null || operator == Operator.EQUALS) {
            matchOperator = isNot ? KoralMatchOperator.NOT_EQUALS : KoralMatchOperator.EQUALS;
        }
        else if (operator == Operator.NOT_EQUALS) {
            matchOperator = isNot ? KoralMatchOperator.EQUALS : KoralMatchOperator.NOT_EQUALS;
        }
        else {
        	throw new KoralException(StatusCodes.UNKNOWN_QUERY_ELEMENT,
                            "SRU diagnostic 37:" + operator.name()
                                    + " is unsupported.");
        }
        koralTerm.setOperator(matchOperator);
    }

    private void parseRegexFlags(KoralTerm koralTerm, Set<RegexFlag> set) throws KoralException {
        // default case sensitive
        if (set == null) return;
        
        ArrayList<String> names = new ArrayList<String>();
        Iterator<RegexFlag> i = set.iterator();
        while (i.hasNext()) {
            RegexFlag f = i.next();
            if (f == RegexFlag.CASE_SENSITVE) {
                koralTerm.setCaseSensitive(true);
            }
            else if (f == RegexFlag.CASE_INSENSITVE) {
                koralTerm.setCaseSensitive(false);
            }
            else {
                names.add(f.name());
            }
        }

        if (names.size() == 1) {
            throw new KoralException(StatusCodes.UNKNOWN_QUERY_ELEMENT,
                    "SRU diagnostic 48: Regexflag: " + names.get(0)
                            + " is unsupported.");
        }
        else if (names.size() > 1) {
            throw new KoralException(StatusCodes.UNKNOWN_QUERY_ELEMENT,
                    "SRU diagnostic 48: Regexflags: " + names.toString()
                            + " are unsupported.");
        }
    }

}
