package de.ids_mannheim.korap.query.parse.fcsql;

import java.util.ArrayList;
import java.util.List;

import de.ids_mannheim.korap.query.elements.KoralGroup;
import de.ids_mannheim.korap.query.elements.KoralOperation;
import de.ids_mannheim.korap.query.serialize.FCSQLQueryProcessor;
import de.ids_mannheim.korap.query.serialize.util.StatusCodes;
import eu.clarin.sru.server.fcs.parser.QueryDisjunction;
import eu.clarin.sru.server.fcs.parser.QueryNode;
import eu.clarin.sru.server.fcs.parser.QuerySegment;
import eu.clarin.sru.server.fcs.parser.QuerySequence;

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
            // } else if (queryNode instanceof QueryGroup) {
            //
        }
        else if (queryNode instanceof QuerySequence) {
            return parseGroupQuery(queryNode.getChildren(),
                    KoralOperation.SEQUENCE);
        }
        else if (queryNode instanceof QueryDisjunction) {
            return parseGroupQuery(queryNode.getChildren(),
                    KoralOperation.DISJUNCTION);
            // } else if (queryNode instanceof QueryWithWithin) {

        }
        else {
            processor.addError(StatusCodes.QUERY_TOO_COMPLEX,
                    "FCS diagnostic 11:" + queryNode.getNodeType().name()
                            + " is currently unsupported.");
            return null;
        }
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
