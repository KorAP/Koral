package de.ids_mannheim.korap.query.parse.fcsql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.ids_mannheim.korap.query.elements.KoralGroup;
import de.ids_mannheim.korap.query.elements.KoralOperation;
import de.ids_mannheim.korap.query.elements.KoralGroup.Frame;
import de.ids_mannheim.korap.query.elements.KoralSpan;
import de.ids_mannheim.korap.query.elements.MatchOperator;
import de.ids_mannheim.korap.query.elements.Scope;
import de.ids_mannheim.korap.query.serialize.FCSQLQueryProcessor;
import de.ids_mannheim.korap.query.serialize.util.StatusCodes;
import eu.clarin.sru.server.fcs.parser.QueryDisjunction;
import eu.clarin.sru.server.fcs.parser.QueryGroup;
import eu.clarin.sru.server.fcs.parser.QueryNode;
import eu.clarin.sru.server.fcs.parser.QuerySegment;
import eu.clarin.sru.server.fcs.parser.QuerySequence;
import eu.clarin.sru.server.fcs.parser.QueryWithWithin;
import eu.clarin.sru.server.fcs.parser.SimpleWithin;

/**
 * @author margaretha
 * 
 */
public class FCSSRUQueryParser {

    private FCSQLQueryProcessor processor;
    private ExpressionParser expressionParser;

    public FCSSRUQueryParser (FCSQLQueryProcessor processor) {
        this.processor = processor;
        this.expressionParser = new ExpressionParser(processor);
    }

    public Object parseQueryNode(QueryNode queryNode) {

        if (queryNode instanceof QuerySegment) {
            return parseQuerySegment((QuerySegment) queryNode);
        }
        else if (queryNode instanceof QueryGroup) {
            return parseQueryNode(queryNode.getChild(0));
        }
        else if (queryNode instanceof QuerySequence) {
            return parseGroupQuery(queryNode.getChildren(),
                    KoralOperation.SEQUENCE);
        }
        else if (queryNode instanceof QueryDisjunction) {
            return parseGroupQuery(queryNode.getChildren(),
                    KoralOperation.DISJUNCTION);
        }
        else if (queryNode instanceof QueryWithWithin) {
            return parseWithinQuery((QueryWithWithin) queryNode);
        }
        else if (queryNode instanceof SimpleWithin) {
            return parseFrame((SimpleWithin) queryNode);
        }
        else {
            processor.addError(StatusCodes.QUERY_TOO_COMPLEX,
                    "FCS diagnostic 11:" + queryNode.getNodeType().name()
                            + " is currently unsupported.");
            return null;
        }
    }

    private KoralSpan parseFrame(SimpleWithin frame) {
        String foundry = "base";
        String layer = "s"; // structure

        if (frame.getScope() == null) {
            processor.addError(StatusCodes.MALFORMED_QUERY,
                    "FCS diagnostic 10: Within context is missing.");
        }
        switch (frame.getScope()) {
            case SENTENCE:
                return new KoralSpan(Scope.SENTENCE.toString(), foundry, layer,
                        MatchOperator.EQUALS);
            default:
                processor.addError(StatusCodes.QUERY_TOO_COMPLEX,
                        "FCS diagnostic 11:" + frame.toString()
                                + " is currently unsupported.");
                return null;
        }
    }

    private KoralGroup parseWithinQuery(QueryWithWithin queryNode) {
        KoralGroup koralGroup = new KoralGroup(KoralOperation.POSITION);
        koralGroup.setFrames(Arrays.asList(Frame.IS_AROUND));

        List<Object> operands = new ArrayList<Object>();
        operands.add(parseQueryNode(queryNode.getWithin()));
        operands.add(parseQueryNode(queryNode.getChild(0)));
        koralGroup.setOperands(operands);

        return koralGroup;
    }

    private KoralGroup parseGroupQuery(List<QueryNode> children,
            KoralOperation operation) {
        KoralGroup koralGroup = new KoralGroup(operation);
        List<Object> operands = new ArrayList<Object>();
        for (QueryNode child : children) {
            operands.add(parseQueryNode(child));
        }
        koralGroup.setOperands(operands);
        return koralGroup;
    }

    private Object parseQuerySegment(QuerySegment segment) {
        if ((segment.getMinOccurs() == 1) && (segment.getMaxOccurs() == 1)) {
            return expressionParser.parseExpression(segment.getExpression());
        }
        else {
            processor.addError(StatusCodes.QUERY_TOO_COMPLEX,
                    "FCS diagnostic 11: Query is too complex.");
            return null;
        }
    }
}
