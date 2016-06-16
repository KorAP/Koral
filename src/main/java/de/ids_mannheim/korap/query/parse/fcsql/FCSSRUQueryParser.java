package de.ids_mannheim.korap.query.parse.fcsql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.ids_mannheim.korap.query.object.KoralContext;
import de.ids_mannheim.korap.query.serialize.util.KoralException;
import de.ids_mannheim.korap.query.serialize.util.StatusCodes;
import de.ids_mannheim.korap.query.object.KoralBoundary;
import de.ids_mannheim.korap.query.object.KoralGroup;
import de.ids_mannheim.korap.query.object.KoralDistance;
import de.ids_mannheim.korap.query.object.KoralObject;
import de.ids_mannheim.korap.query.object.KoralOperation;
import de.ids_mannheim.korap.query.object.KoralSpan;
import de.ids_mannheim.korap.query.object.KoralTerm;
import de.ids_mannheim.korap.query.object.KoralGroup.Frame;
import eu.clarin.sru.server.fcs.parser.ExpressionWildcard;
import eu.clarin.sru.server.fcs.parser.QueryDisjunction;
import eu.clarin.sru.server.fcs.parser.QueryGroup;
import eu.clarin.sru.server.fcs.parser.QueryNode;
import eu.clarin.sru.server.fcs.parser.QuerySegment;
import eu.clarin.sru.server.fcs.parser.QuerySequence;
import eu.clarin.sru.server.fcs.parser.QueryWithWithin;
import eu.clarin.sru.server.fcs.parser.SimpleWithin;
import eu.clarin.sru.server.fcs.parser.SimpleWithin.Scope;

/**
 * @author margaretha
 * 
 */
public class FCSSRUQueryParser {

    private ExpressionParser expressionParser;

    public FCSSRUQueryParser () {
        this.expressionParser = new ExpressionParser();
    }

    public KoralObject parseQueryNode(QueryNode queryNode)
            throws KoralException {

        if (queryNode instanceof QuerySegment) {
            return parseQuerySegment((QuerySegment) queryNode);
        }
        else if (queryNode instanceof QueryGroup) {
            return parseQueryNode(queryNode.getChild(0));
        }
        else if (queryNode instanceof QuerySequence) {
            return parseSequenceQuery(queryNode.getChildren());
        }
        else if (queryNode instanceof QueryDisjunction) {
            return parseGroupQuery(queryNode.getChildren(),
                    KoralOperation.DISJUNCTION);
        }
        else if (queryNode instanceof QueryWithWithin) {
            return parseWithinQuery((QueryWithWithin) queryNode);
        }
        else if (queryNode instanceof SimpleWithin) {
            SimpleWithin withinNode = (SimpleWithin) queryNode;
            return parseWithinScope(withinNode.getScope());
        }
        else {
            throw new KoralException(StatusCodes.QUERY_TOO_COMPLEX,
                    "FCS diagnostic 11:" + queryNode.getNodeType().name()
                            + " is currently unsupported.");
        }
    }

    private KoralObject parseWithinQuery(QueryWithWithin queryNode)
            throws KoralException {
        KoralGroup koralGroup = new KoralGroup(KoralOperation.POSITION);
        koralGroup.setFrames(Arrays.asList(Frame.IS_AROUND));

        List<KoralObject> operands = new ArrayList<KoralObject>();
        operands.add(parseQueryNode(queryNode.getWithin()));
        operands.add(parseQueryNode(queryNode.getQuery()));
        koralGroup.setOperands(operands);
        return koralGroup;
    }

    private KoralSpan parseWithinScope(Scope scope) throws KoralException {
        if (scope == null) {
            throw new KoralException(StatusCodes.MALFORMED_QUERY,
                    "FCS diagnostic 11: Within context is missing.");
        }

        KoralContext contextSpan;
        if (scope == Scope.SENTENCE) {
            contextSpan = KoralContext.SENTENCE;
        }
        else if (scope == Scope.PARAGRAPH) {
            contextSpan = KoralContext.PARAGRAPH;
        }
        else if (scope == Scope.TEXT) {
            contextSpan = KoralContext.TEXT;
        }
        else {
            throw new KoralException(StatusCodes.QUERY_TOO_COMPLEX,
                    "FCS diagnostic 11: Within scope " + scope.toString()
                            + " is currently unsupported.");
        }

        return new KoralSpan(new KoralTerm(contextSpan));
    }

    private KoralGroup parseGroupQuery(List<QueryNode> children,
            KoralOperation operation) throws KoralException {
        KoralGroup koralGroup = new KoralGroup(operation);
        List<KoralObject> operands = new ArrayList<KoralObject>();
        for (QueryNode child : children) {
            operands.add(parseQueryNode(child));
        }
        koralGroup.setOperands(operands);
        return koralGroup;
    }

    private KoralGroup parseSequenceQuery(List<QueryNode> children)
            throws KoralException {
        KoralGroup koralGroup = new KoralGroup(KoralOperation.SEQUENCE);
        List<KoralObject> operands = new ArrayList<KoralObject>();
        KoralObject operand;

        boolean isEmptyTokenFound = false;
        boolean isLastTokenEmpty = false;
        int size = children.size();

        for (int i = 0; i < size; i++) {
            QueryNode child = children.get(i);
            if (i > 0 && i < size - 1 && findEmptyToken(child)) {
                QuerySegment qs = (QuerySegment) child;
                if (isLastTokenEmpty) {
                    updateBoundary(operands.get(operands.size() - 1), qs);
                }
                else {
                    operands.add(new KoralBoundary(qs.getMinOccurs(), qs
                            .getMaxOccurs()));
                    isLastTokenEmpty = true;
                }
                isEmptyTokenFound = true;
                continue;
            }
            operand = parseQueryNode(child);
            operands.add(operand);
            isLastTokenEmpty = false;
        }

        if (isEmptyTokenFound) {
            //operands = updateOperands(operands);
            operands = createDistance(koralGroup,operands);
        }

        koralGroup.setOperands(operands);
        return koralGroup;
    }

    private boolean findEmptyToken(QueryNode child) {
        if (child instanceof QuerySegment
                && ((QuerySegment) child).getExpression() instanceof ExpressionWildcard) {
            return true;
        }
        return false;
    }

    private void updateBoundary(KoralObject koralObject, QuerySegment qs) {
        KoralBoundary boundary = (KoralBoundary) koralObject;
        boundary.setMin(boundary.getMin() + qs.getMinOccurs());
        boundary.setMax(boundary.getMax() + qs.getMaxOccurs());
    }

    private List<KoralObject> createDistance(KoralGroup koralGroup, List<KoralObject> operands){
        boolean isLastOperandUpdated = false;
        boolean isDistanceSet = false;
        List<KoralObject> newOperands = new ArrayList<KoralObject>(
                operands.size());        
        newOperands.add(operands.get(0));        
        int operandSize = operands.size();
        for (int i = 1; i < operandSize - 1; i++) {
            KoralObject operand = operands.get(i);
            if (operand instanceof KoralBoundary) {
                if (isDistanceSet){
                    
                }
                else{
                    List<KoralDistance> distances = new ArrayList<KoralDistance>(1);
                    distances.add(new KoralDistance((KoralBoundary) operand));
                    koralGroup.setDistances(distances);
                }
                isLastOperandUpdated = true;
            }
            isLastOperandUpdated = false;
        }
        if (!isLastOperandUpdated){
            newOperands.add(operands.get(operandSize-1));
        }
        return newOperands;
    }
    
    private List<KoralObject> updateOperands(List<KoralObject> operands) {
        boolean isLastOperandUpdated = false;
        List<KoralObject> newOperands = new ArrayList<KoralObject>(
                operands.size());        
        newOperands.add(operands.get(0));        
        int operandSize = operands.size();
        for (int i = 1; i < operandSize - 1; i++) {
            KoralObject operand = operands.get(i);
            if (operand instanceof KoralBoundary) {
                KoralGroup koralGroup = new KoralGroup(KoralOperation.SEQUENCE);
                List<KoralDistance> distances = new ArrayList<KoralDistance>(1);
                distances.add(new KoralDistance((KoralBoundary) operand));
                koralGroup.setDistances(distances);
                koralGroup.setOperands(Arrays.asList(newOperands.get(i - 1),
                        operands.get(i + 1)));
                newOperands.set(i-1,koralGroup);
                isLastOperandUpdated = true;
            }
            isLastOperandUpdated = false;
        }
        if (!isLastOperandUpdated){
            newOperands.add(operands.get(operandSize-1));
        }
        return newOperands;
    }

    private KoralObject parseQuerySegment(QuerySegment segment)
            throws KoralException {
        int minOccurs = segment.getMinOccurs();
        int maxOccurs = segment.getMaxOccurs();

        if ((minOccurs == 1) && (maxOccurs == 1)) {
            return expressionParser.parseExpression(segment.getExpression());
        }
        else {
            KoralBoundary boundary = new KoralBoundary(minOccurs, maxOccurs);
            List<KoralObject> operand = new ArrayList<KoralObject>(1);
            operand.add(expressionParser.parseExpression(segment
                    .getExpression()));

            KoralGroup koralGroup = new KoralGroup(KoralOperation.REPETITION);
            koralGroup.setBoundary(boundary);
            koralGroup.setOperands(operand);
            return koralGroup;
        }
    }
}
