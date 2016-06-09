package de.ids_mannheim.korap.query.parse.fcsql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.ids_mannheim.korap.query.object.KoralContext;
import de.ids_mannheim.korap.query.serialize.util.KoralException;
import de.ids_mannheim.korap.query.serialize.util.StatusCodes;
import de.ids_mannheim.korap.query.object.KoralGroup;
import de.ids_mannheim.korap.query.object.KoralObject;
import de.ids_mannheim.korap.query.object.KoralOperation;
import de.ids_mannheim.korap.query.object.KoralSpan;
import de.ids_mannheim.korap.query.object.KoralTerm;
import de.ids_mannheim.korap.query.object.KoralGroup.Frame;
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

    public KoralObject parseQueryNode(QueryNode queryNode) throws KoralException {

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
        } else if (queryNode instanceof QueryWithWithin) {
        	return parseWithinQuery((QueryWithWithin)queryNode);
	    } else if (queryNode instanceof SimpleWithin) {
	    	SimpleWithin withinNode = (SimpleWithin) queryNode;
	    	return parseWithinScope(withinNode.getScope());
	    }
        else {
            throw new KoralException(StatusCodes.QUERY_TOO_COMPLEX,
                    "FCS diagnostic 11:" + queryNode.getNodeType().name()
                            + " is currently unsupported.");
        }
    }
    private KoralObject parseWithinQuery(QueryWithWithin queryNode) throws KoralException {
    	KoralGroup koralGroup = new KoralGroup(KoralOperation.POSITION);
    	koralGroup.setFrames(Arrays.asList(Frame.IS_AROUND));
    	
    	List<KoralObject> operands = new ArrayList<KoralObject>();
    	operands.add(parseQueryNode(queryNode.getWithin()));
    	operands.add(parseQueryNode(queryNode.getQuery()));
    	koralGroup.setOperands(operands);
    	return koralGroup;
	}

    private KoralSpan parseWithinScope(Scope scope) throws KoralException{
    	if (scope == null){
    		throw new KoralException(StatusCodes.MALFORMED_QUERY,
                    "FCS diagnostic 11: Within context is missing.");
    	}

    	KoralContext contextSpan;
    	if (scope == Scope.SENTENCE) {
			contextSpan = KoralContext.SENTENCE;
    	}
    	else if (scope == Scope.PARAGRAPH){
			contextSpan = KoralContext.PARAGRAPH;
    	}
    	else if (scope == Scope.TEXT){
            contextSpan = KoralContext.TEXT;
    	}
    	else{
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

    private KoralObject parseQuerySegment(QuerySegment segment) throws KoralException {
        if ((segment.getMinOccurs() == 1) && (segment.getMaxOccurs() == 1)) {
            return expressionParser.parseExpression(segment.getExpression());
        }
        else {
            throw new KoralException(StatusCodes.QUERY_TOO_COMPLEX,
                    "FCS diagnostic 11: Query is too complex.");
        }
    }
}
