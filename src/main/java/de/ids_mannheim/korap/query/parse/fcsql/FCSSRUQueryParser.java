package de.ids_mannheim.korap.query.parse.fcsql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.ids_mannheim.korap.query.object.KoralContext;
import de.ids_mannheim.korap.query.serialize.util.KoralException;
import de.ids_mannheim.korap.query.serialize.util.StatusCodes;
import de.ids_mannheim.korap.query.object.KoralBoundary;
import de.ids_mannheim.korap.query.object.KoralGroup;
import de.ids_mannheim.korap.query.object.KoralDistance;
import de.ids_mannheim.korap.query.object.KoralFrame;
import de.ids_mannheim.korap.query.object.KoralObject;
import de.ids_mannheim.korap.query.object.KoralOperation;
import de.ids_mannheim.korap.query.object.KoralSpan;
import de.ids_mannheim.korap.query.object.KoralTerm;
import eu.clarin.sru.server.fcs.parser.ExpressionWildcard;
import eu.clarin.sru.server.fcs.parser.QueryDisjunction;
import eu.clarin.sru.server.fcs.parser.QueryGroup;
import eu.clarin.sru.server.fcs.parser.QueryNode;
import eu.clarin.sru.server.fcs.parser.QuerySegment;
import eu.clarin.sru.server.fcs.parser.QuerySequence;
import eu.clarin.sru.server.fcs.parser.QueryWithWithin;
import eu.clarin.sru.server.fcs.parser.SimpleWithin;
import eu.clarin.sru.server.fcs.parser.SimpleWithin.Scope;

/** This class handles FCS query nodes from the FCSParser converting FCSQL to Java objects. 
 * 
 * @author margaretha
 * 
 */
public class FCSSRUQueryParser {

    private ExpressionParser expressionParser;

    /** Constructs the FCSSRUQueryParser.
     * 
     */
    public FCSSRUQueryParser () {
        this.expressionParser = new ExpressionParser();
    }

    /** Parses the given query node according to its type.
     * @param queryNode an FCS a query node
     * @return a koral object
     * @throws KoralException
     */
    public KoralObject parseQueryNode(QueryNode queryNode)
            throws KoralException {

        if (queryNode instanceof QuerySegment) {
            return parseQuerySegment((QuerySegment) queryNode);
        }
        else if (queryNode instanceof QueryGroup) {
            return parseQueryGroup((QueryGroup) queryNode);
        }
        else if (queryNode instanceof QuerySequence) {
            return parseSequenceQuery(queryNode.getChildren());
        }
        else if (queryNode instanceof QueryDisjunction) {
            return parseQueryDisjunction(queryNode.getChildren());
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
                    queryNode.getNodeType().name()
                            + " is currently unsupported.");
        }
    }
    
    /** Parses the given query segment into a koral object.
     * @param segment a query segment
     * @return a koral object representation of the query segment
     * @throws KoralException
     */
    private KoralObject parseQuerySegment(QuerySegment segment)
            throws KoralException {
        KoralObject object = expressionParser.parseExpression(segment.getExpression());
        return handleQuantifier(object, segment.getMinOccurs(), segment.getMaxOccurs());
    }
    
    /** Parses the given query group into a koral object.
     * @param group a koral group
     * @return a koral object
     * @throws KoralException
     */
    private KoralObject parseQueryGroup(QueryGroup group) throws KoralException {
        KoralObject object = parseQueryNode(group.getFirstChild());
        return handleQuantifier(object, group.getMinOccurs(), group.getMaxOccurs());
    }
    
    /** Parses FCSQL quantifier into a koral boundary and adds it to a koral group
     * @param object a koral object
     * @param minOccurs minimum occurrences
     * @param maxOccurs maximum occurrences
     * @return a koral group or the given koral object when minimum and maximum occurrences are exactly 1.
     */
    private KoralObject handleQuantifier(KoralObject object, int minOccurs, int maxOccurs){
        if ((minOccurs == 1) && (maxOccurs == 1)) {
            return object;
        }
        
        KoralBoundary boundary = new KoralBoundary(minOccurs, maxOccurs);
        List<KoralObject> operand = new ArrayList<KoralObject>(1);
        operand.add(object);

        KoralGroup koralGroup = new KoralGroup(KoralOperation.REPETITION);
        koralGroup.setBoundary(boundary);
        koralGroup.setOperands(operand);
        return koralGroup;
    }
    
    /** Parses a QueryWithWithin into a koral group.
     * @param queryNode a query node of type QueryWithWithin
     * @return a koral group
     * @throws KoralException
     */
    private KoralGroup parseWithinQuery(QueryWithWithin queryNode)
            throws KoralException {
        KoralGroup koralGroup = new KoralGroup(KoralOperation.POSITION);
        koralGroup.setFrames(Collections.singletonList(KoralFrame.IS_AROUND));

        List<KoralObject> operands = new ArrayList<KoralObject>();
        operands.add(parseQueryNode(queryNode.getWithin()));
        operands.add(parseQueryNode(queryNode.getQuery()));
        koralGroup.setOperands(operands);
        return koralGroup;
    }

    /** Parses the scope of a QueryWithWithin into a koral span.
     * @param scope the scope of a QueryWithWithin
     * @return a koral span
     * @throws KoralException
     */
    private KoralSpan parseWithinScope(Scope scope) throws KoralException {
        if (scope == null) {
            throw new KoralException(StatusCodes.MALFORMED_QUERY,
                    "Within context is missing.");
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
                    "Within scope " + scope.toString()
                            + " is currently unsupported.");
        }

        return new KoralSpan(new KoralTerm(contextSpan));
    }

    /** Parses a query disjunction into a koral group.
     * @param children a list of query nodes.
     * @return a koral group with operation disjunction
     * @throws KoralException
     */
    private KoralGroup parseQueryDisjunction(List<QueryNode> children) throws KoralException {
        KoralGroup koralGroup = new KoralGroup(KoralOperation.DISJUNCTION);
        List<KoralObject> operands = new ArrayList<KoralObject>();
        for (QueryNode child : children) {
            operands.add(parseQueryNode(child));
        }
        koralGroup.setOperands(operands);
        return koralGroup;
    }

    /** Parses a sequence query into a koral group.
     * @param children a list query nodes.
     * @return a koral group
     * @throws KoralException
     */
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
                    KoralBoundary boundary = (KoralBoundary) operands.get(operands.size() - 1);
                    updateBoundary(boundary, qs);
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
            operands = createDistance(koralGroup,operands);
        }

        koralGroup.setOperands(operands);
        return koralGroup;
    }

    /** Determines if there is an empty token in the given query node.
     * @param node a query node
     * @return true if an empty token is found or false otherwise.
     */
    private boolean findEmptyToken(QueryNode node) {
        if (node instanceof QuerySegment
                && ((QuerySegment) node).getExpression() instanceof ExpressionWildcard) {
            return true;
        }
        return false;
    }

    /** Updates the boundary properties with the parameters in the query segment.
     * @param boundary a koral boundary
     * @param qs a query segment
     */
    private void updateBoundary(KoralBoundary boundary, QuerySegment qs) {
        boundary.setMin(boundary.getMin() + qs.getMinOccurs());
        boundary.setMax(boundary.getMax() + qs.getMaxOccurs());
    }

    /** Creates koral distances from KoralBoundary objects in the given operand list, 
     * and adds the distances the koral group. Removes the KoralBoundary objects from 
     * the operand list and returns the new operand list.
     * 
     * @param koralGroup a koral group
     * @param operands a list of koral objects
     * @return an updated operand lists without KoralBoundary objects 
     */
    private List<KoralObject> createDistance(KoralGroup koralGroup, List<KoralObject> operands){
        boolean isSubGroupAdded = false;
        List<KoralObject> newOperands = new ArrayList<KoralObject>(
                operands.size());        
        newOperands.add(operands.get(0));        
        int operandSize = operands.size();
        for (int i = 1; i < operandSize - 1; i++) {
            KoralObject operand = operands.get(i);
            if (operand instanceof KoralBoundary) {
                List<KoralDistance> distances = new ArrayList<KoralDistance>();
                distances.add(new KoralDistance ((KoralBoundary) operand));  
                
                if (koralGroup.getDistances() != null){
                    KoralObject lastOperand = newOperands.get(newOperands.size()-1);
                    KoralGroup subGroup = createSequenceGroupWithDistance(distances, lastOperand, operands.get(i+1));
                    newOperands.remove(lastOperand);
                    newOperands.add(subGroup);
                    isSubGroupAdded = true;
                    continue;
                }
                else{                    
                    koralGroup.setDistances(distances);
                    koralGroup.setInOrder(true);
                }
            }
            else{
                newOperands.add(operand);                
            }
            isSubGroupAdded = false;
        }
        
        if (!isSubGroupAdded){
            newOperands.add(operands.get(operandSize-1));
        }
        return newOperands;
    }
    
    /** Creates a distance query, namely a koral group of operation sequence 
     * with the given distances and operands.
     *  
     * @param distances a list of distances.
     * @param operand an operand
     * @param operand2 another operand
     * @return a koral group
     */
    private KoralGroup createSequenceGroupWithDistance(List<KoralDistance> distances, 
            KoralObject operand, KoralObject operand2) {
        KoralGroup subGroup = new KoralGroup(KoralOperation.SEQUENCE);
        subGroup.setDistances(distances);
        subGroup.setInOrder(true);
        List<KoralObject> operands = new ArrayList<KoralObject>();
        operands.add(operand);
        operands.add(operand2);
        subGroup.setOperands(operands);
        return subGroup;
    }
}
