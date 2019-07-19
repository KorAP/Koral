package de.ids_mannheim.korap.query.parse.fcsql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.ids_mannheim.korap.query.object.KoralMatchOperator;
import de.ids_mannheim.korap.query.object.KoralObject;
import de.ids_mannheim.korap.query.object.KoralTerm;
import de.ids_mannheim.korap.query.object.KoralTerm.KoralTermType;
import de.ids_mannheim.korap.query.object.KoralTermGroup;
import de.ids_mannheim.korap.query.object.KoralTermGroupRelation;
import de.ids_mannheim.korap.query.object.KoralToken;
import de.ids_mannheim.korap.query.serialize.util.KoralException;
import de.ids_mannheim.korap.query.serialize.util.StatusCodes;
import eu.clarin.sru.server.fcs.parser.Expression;
import eu.clarin.sru.server.fcs.parser.ExpressionAnd;
import eu.clarin.sru.server.fcs.parser.ExpressionGroup;
import eu.clarin.sru.server.fcs.parser.ExpressionNot;
import eu.clarin.sru.server.fcs.parser.ExpressionOr;
import eu.clarin.sru.server.fcs.parser.ExpressionWildcard;
import eu.clarin.sru.server.fcs.parser.Operator;
import eu.clarin.sru.server.fcs.parser.QueryNode;
import eu.clarin.sru.server.fcs.parser.RegexFlag;

/**
 * This class handles and parses various FCSQL expressions (e.g.
 * simple and boolean expressions) into a KoralObject.
 * 
 * @author margaretha
 * 
 */
public class ExpressionParser {

    private static final String FOUNDRY_CORENLP = "corenlp";
    private static final String FOUNDRY_OPENNLP = "opennlp";
    private static final String FOUNDRY_TT = "tt";
    private static final String FOUNDRY_MARMOT = "marmot";

    private List<String> supportedFoundries =
            Arrays.asList(new String[] { FOUNDRY_CORENLP, FOUNDRY_OPENNLP,
                    FOUNDRY_TT, FOUNDRY_MARMOT });

    /**
     * Parses the given query node and constructs a koral object
     * representation of it.
     * 
     * @param queryNode
     *            an FCSQL query node
     * @return a koral object representation of the given node
     * @throws KoralException
     */
    public KoralObject parseExpression(QueryNode queryNode)
            throws KoralException {
        return parseExpression(queryNode, false, true);
    }

    /**
     * Parses the given query node using the specified parameters.
     * 
     * @param queryNode
     *            an FCSQL query node
     * @param isNot
     *            a boolean value indicating if the query node was
     *            negated or not.
     * @param isToken
     *            a boolean value indicating if the query node is a
     *            token or not.
     * @return a koral object representation of the given node
     * @throws KoralException
     */
    public KoralObject parseExpression(QueryNode queryNode, boolean isNot,
            boolean isToken) throws KoralException {

        if (queryNode instanceof Expression) {
            return parseSimpleExpression((Expression) queryNode, isNot, isToken);
        }
        else if (queryNode instanceof ExpressionAnd) {
            List<QueryNode> operands = queryNode.getChildren();
            if (isNot) {
                return parseBooleanExpression(operands,
                        KoralTermGroupRelation.OR, isToken);
            }
            else {
                return parseBooleanExpression(operands,
                        KoralTermGroupRelation.AND, isToken);
            }
        }
        else if (queryNode instanceof ExpressionGroup) {
            // Ignore the group
            return parseExpression(queryNode.getFirstChild(), false, isToken);
        }
        else if (queryNode instanceof ExpressionNot) {
            boolean negation = isNot ? false : true;
            return parseExpression(queryNode.getChild(0), negation, isToken);
        }
        else if (queryNode instanceof ExpressionOr) {
            List<QueryNode> operands = queryNode.getChildren();
            if (isNot) {
                return parseBooleanExpression(operands,
                        KoralTermGroupRelation.AND, isToken);
            }
            else {
                return parseBooleanExpression(operands,
                        KoralTermGroupRelation.OR, isToken);
            }
        }
        else if (queryNode instanceof ExpressionWildcard) {
            return new KoralToken();
        }
        else {
            throw new KoralException(StatusCodes.QUERY_TOO_COMPLEX,
                    "Query is too complex.");
        }
    }

    /**
     * Handles a boolean expression by parsing the given operands and
     * creates a koral token using the parsed operands and the given
     * relation.
     * 
     * @param operands
     *            a list of query node
     * @param relation
     *            the boolean operator
     * @return a koral token
     * @throws KoralException
     */
    private KoralObject parseBooleanExpression(List<QueryNode> operands,
            KoralTermGroupRelation relation, boolean isToken) throws KoralException {
        List<KoralObject> terms = new ArrayList<>();
        for (QueryNode node : operands) {
            terms.add(parseExpression(node, false, false));
        }
        
        KoralTermGroup termGroup = new KoralTermGroup(relation, terms);
        if (isToken){        
            return new KoralToken(termGroup);
        }
        else {
            return termGroup;
        }
    }

    /**
     * Parses the given simple expression considering the other
     * specified parameters.
     * 
     * @param expression
     *            a simple expression
     * @param isNot
     *            a boolean value indicating if the expression was
     *            negated or not.
     * @param isToken
     *            a boolean value indicating if the expression is a
     *            token or not.
     * @return
     * @throws KoralException
     */
    private KoralObject parseSimpleExpression(Expression expression,
            boolean isNot, boolean isToken) throws KoralException {
        KoralTerm koralTerm = parseTerm(expression, isNot);
        if (isToken) {
            return new KoralToken(koralTerm);
        }
        else {
            return koralTerm;
        }
    }

    /**
     * Parses the given expression and constructs a KoralTerm.
     * 
     * @param expression
     *            an expression
     * @param isNot
     *            a boolean value indicating if the expression was
     *            negated or not.
     * @return a koral term
     * @throws KoralException
     */
    public KoralTerm parseTerm(Expression expression, boolean isNot)
            throws KoralException {
        KoralTerm koralTerm = null;
        koralTerm = new KoralTerm(expression.getRegexValue());
        koralTerm.setType(KoralTermType.REGEX);
        parseLayerIdentifier(koralTerm, expression.getLayerIdentifier());
        parseQualifier(koralTerm, expression.getLayerQualifier());
        parseOperator(koralTerm, expression.getOperator(), isNot);
        parseRegexFlags(koralTerm, expression.getRegexFlags());
        return koralTerm;
    }

    /**
     * Parses the given layer identifier and adds it to the koral
     * term.
     * 
     * @param koralTerm
     *            a koral term
     * @param identifier
     *            a layer identifier
     * @throws KoralException
     */
    private void parseLayerIdentifier(KoralTerm koralTerm, String identifier)
            throws KoralException {
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
                    "Layer " + identifier
                            + " is unsupported.");
        }

        koralTerm.setLayer(layer);
    }

    /**
     * Parses the given layer qualifier and adds it to the koral term.
     * 
     * @param koralTerm
     *            a koral term
     * @param qualifier
     *            a layer qualifier
     * @throws KoralException
     */
    private void parseQualifier(KoralTerm koralTerm, String qualifier)
            throws KoralException {
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
                    "Layer lemma with qualifier opennlp is unsupported.");
        }
        else if (!supportedFoundries.contains(qualifier)) {
            throw new KoralException(StatusCodes.UNKNOWN_QUERY_ELEMENT,
                    "Qualifier " + qualifier
                            + " is unsupported.");
        }

        koralTerm.setFoundry(qualifier);
    }

    /**
     * Parses the given match operator and adds it to the koral term.
     * 
     * @param koralTerm
     *            a koral term
     * @param operator
     *            a match operator
     * @param isNot
     *            a boolean value indicating if there was a negation
     *            or not.
     * @throws KoralException
     */
    private void parseOperator(KoralTerm koralTerm, Operator operator,
            boolean isNot) throws KoralException {
        KoralMatchOperator matchOperator = null;
        if (operator == null || operator == Operator.EQUALS) {
            matchOperator = isNot ? KoralMatchOperator.NOT_EQUALS
                    : KoralMatchOperator.EQUALS;
        }
        else if (operator == Operator.NOT_EQUALS) {
            matchOperator = isNot ? KoralMatchOperator.EQUALS
                    : KoralMatchOperator.NOT_EQUALS;
        }
        else {
            throw new KoralException(StatusCodes.UNKNOWN_QUERY_ELEMENT,
                    operator.name() + " is unsupported.");
        }
        koralTerm.setOperator(matchOperator);
    }

    /**
     * Parses the given set of regex flags and adds them to the koral
     * term.
     * 
     * @param koralTerm
     *            a koral term
     * @param set
     *            a set of regex flags
     * @throws KoralException
     */
    private void parseRegexFlags(KoralTerm koralTerm, Set<RegexFlag> set)
            throws KoralException {
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
                    "Regexflag: " + names.get(0)
                            + " is unsupported.");
        }
        else if (names.size() > 1) {
            throw new KoralException(StatusCodes.UNKNOWN_QUERY_ELEMENT,
                    "Regexflags: " + names.toString()
                            + " are unsupported.");
        }
    }

}
