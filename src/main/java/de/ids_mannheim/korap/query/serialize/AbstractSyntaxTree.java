package de.ids_mannheim.korap.query.serialize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.runtime.tree.Tree;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import de.ids_mannheim.korap.util.QueryException;

public abstract class AbstractSyntaxTree {
	
	public abstract void process(String query) throws QueryException;
	
	public static final Integer MAXIMUM_DISTANCE = 100; 

	Logger log;
	/**
	 *  The query
	 */
	String query;
	/**
	 * Top-level map representing the whole request.
	 */
	LinkedHashMap<String, Object> requestMap = new LinkedHashMap<String, Object>();
	/**
	 * Keeps track of open node categories
	 */
	LinkedList<String> openNodeCats = new LinkedList<String>();
	/**
	 * Keeps track of all visited nodes in a tree
	 */
	List<ParseTree> visited = new ArrayList<ParseTree>();
	/**
	 * Keeps track of active object.
	 */
	LinkedList<LinkedHashMap<String, Object>> objectStack = new LinkedList<LinkedHashMap<String, Object>>();
	/**
	 * Keeps track of how many objects there are to pop after every recursion of {@link #processNode(ParseTree)}
	 */
	LinkedList<Integer> objectsToPop = new LinkedList<Integer>();
	/**
	 * If true, print debug statements
	 */
	public static boolean verbose = false;
	ParseTree currentNode = null;
	Integer stackedObjects = 0;
	private ArrayList<List<Object>> errors = new ArrayList<List<Object>>();
	private ArrayList<List<Object>> warnings = new ArrayList<List<Object>>();
	private ArrayList<List<Object>> messages = new ArrayList<List<Object>>();
	LinkedHashMap<String, Object> collection = new LinkedHashMap<String,Object>();
	
	AbstractSyntaxTree() {
		requestMap.put("@context", "http://ids-mannheim.de/ns/KorAP/json-ld/v0.2/context.jsonld");
		requestMap.put("errors", errors);
		requestMap.put("warnings", warnings);
		requestMap.put("messages", messages);
		requestMap.put("collection", collection);
		requestMap.put("meta", new LinkedHashMap<String, Object>());
	}
	
	public void addWarning(int code, String msg) {
		List<Object> warning = Arrays.asList(new Object[]{code, msg}); 
		warnings.add(warning);
	}
	
	public void addWarning(String msg) {
		List<Object> warning = Arrays.asList(new Object[]{msg}); 
		warnings.add(warning);
	}
	
	public void addMessage(int code, String msg) {
		List<Object> message = Arrays.asList(new Object[]{code, msg}); 
		messages.add(message);
	}
	
	public void addMessage(String msg) {
		List<Object> message = Arrays.asList(new Object[]{msg}); 
		messages.add(message);
	}
	
	public void addError(int code, String msg) {
		List<Object> error = Arrays.asList(new Object[]{code, msg}); 
		errors.add(error);
	}
	
	public Map<String, Object> getRequestMap() {
		return requestMap;
	}
	
	protected LinkedHashMap<String, Object> makeSpan() {
		LinkedHashMap<String, Object> span = new LinkedHashMap<String, Object>();
		span.put("@type", "korap:span");
		return span;
	}
	
	protected LinkedHashMap<String, Object> makeSpan(String key) {
		LinkedHashMap<String, Object> span = new LinkedHashMap<String, Object>();
		span.put("@type", "korap:span");
		span.put("key", key);
		return span;
	}
	
	protected LinkedHashMap<String, Object> makeTerm() {
		LinkedHashMap<String, Object> term = new LinkedHashMap<String, Object>();
		term.put("@type", "korap:term");
		return term;
	}
	
	protected LinkedHashMap<String, Object> makeTermGroup(String relation) {
		LinkedHashMap<String, Object> term = new LinkedHashMap<String, Object>();
		term.put("@type", "korap:termGroup");
		term.put("relation", "relation:"+relation);
		term.put("operands", new ArrayList<Object>());
		return term;
	}
	
	protected LinkedHashMap<String, Object> makeDoc() {
		LinkedHashMap<String, Object> term = new LinkedHashMap<String, Object>();
		term.put("@type", "korap:doc");
		return term;
	}
	
	protected LinkedHashMap<String, Object> makeDocGroup(String relation) {
		LinkedHashMap<String, Object> term = new LinkedHashMap<String, Object>();
		term.put("@type", "korap:docGroup");
		term.put("operation", "operation:"+relation);
		term.put("operands", new ArrayList<Object>());
		return term;
	}
	
	protected LinkedHashMap<String, Object> makeToken() {
		LinkedHashMap<String, Object> token = new LinkedHashMap<String, Object>();
		token.put("@type", "korap:token");
		return token;
	}
	
	protected LinkedHashMap<String, Object> makeGroup(String operation) {
		LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
		group.put("@type", "korap:group");
		group.put("operation", "operation:"+operation);
		group.put("operands", new ArrayList<Object>());
		return group;
	}
	
	protected LinkedHashMap<String, Object> makeRepetition(Integer min, Integer max) {
		LinkedHashMap<String, Object> group = makeGroup("repetition");
		group.put("boundary", makeBoundary(min, max));
		group.put("min", min);
		if (max != null) {
			group.put("max", max);
		}
		addMessage(303, "Deprecated 2014-07-24: 'min' and 'max' to be supported until 3 months from deprecation date.");
		return group;
	}
	
	@Deprecated
	protected LinkedHashMap<String, Object> makePosition(String frame) {
		LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
		group.put("@type", "korap:group");
		group.put("operation", "operation:position");
		group.put("frame", "frame:"+frame);
		group.put("operands", new ArrayList<Object>());
		return group;
	}
	
	protected LinkedHashMap<String, Object> makePosition(String[] allowedFrames, String[] classRefCheck) {
		LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
		group.put("@type", "korap:group");
		group.put("operation", "operation:position");
		group.put("frames", Arrays.asList(allowedFrames));
		group.put("operands", new ArrayList<Object>());
		// DEPRECATED 'frame'
		if (classRefCheck == null || classRefCheck.length==0) classRefCheck = new String[]{"classRefCheck:includes"};
		String frame = "";
		
		if (allowedFrames.length==0 && classRefCheck[0]=="classRefCheck:includes") {
			frame = "frame:contains";
		} else if (allowedFrames[0]=="frames:overlapsLeft" && allowedFrames[1]=="frames:overlapsRight" && classRefCheck[0]=="classRefCheck:intersects") {
			frame = "frame:overlaps";
		} else if (allowedFrames[0]=="frames:startswith" && classRefCheck[0]=="classRefCheck:includes") {
			frame = "frame:startswith";
		} else if (allowedFrames[0]=="frames:endswith" && classRefCheck[0]=="classRefCheck:includes") {
			frame = "frame:endswith";
		} else if (allowedFrames[0]=="frames:matches" && classRefCheck[0]=="classRefCheck:includes" && classRefCheck.length==1) {
			frame = "frame:matches";
		} else if (allowedFrames[0]=="frames:matches" && classRefCheck[0]=="classRefCheck:includes" && classRefCheck[1]=="classRefCheck:unequals") {
			frame = "frame:matches";
		} else if (allowedFrames[0]=="frames:matches" && classRefCheck[0]=="classRefCheck:equals") {
			frame = "frame:matches";			
		} else if (allowedFrames[0]=="frames:contains" && classRefCheck[0]=="classRefCheck:includes") {
			frame = "frame:contains";
		} else if (allowedFrames[0]=="frames:startswith" && classRefCheck[0]=="classRefCheck:intersects") {
			frame = "frame:overlapsLeft";
		} else if (allowedFrames[0]=="frames:endswith" && classRefCheck[0]=="classRefCheck:intersects") {
			frame = "frame:overlapsRight";
		} else if (allowedFrames[0]=="frames:matches" && classRefCheck[0]=="classRefCheck:intersects") {
			frame = "frame:matches";
		} else if (allowedFrames[0]=="frames:matches" && classRefCheck[0]=="classRefCheck:unequals") {
			frame = "frame:matches";
		} else if (allowedFrames[0]=="frames:matches" && classRefCheck[0]=="classRefCheck:equals") {
			frame = "frame:matches";
		} else if (allowedFrames[0]=="frames:contains" && classRefCheck[0]=="classRefCheck:intersects") {
			frame = "frame:contains";
		}
		group.put("frame", frame);
		addMessage(303, "Deprecated 2014-09-22: 'frame' only to be supported until 3 months from deprecation date. " +
				"Position frames are now expressed through 'frames'.");
		return group;
	}
	
	protected LinkedHashMap<String, Object> makeSpanClass(int classCount) {
		return makeSpanClass(classCount, true);
	}
	
	protected LinkedHashMap<String, Object> makeSpanClass(int classCount, boolean setBySystem) {
		LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
		group.put("@type", "korap:group");
		group.put("operation", "operation:class");
		if (setBySystem) {
			group.put("class", 128+classCount);
			group.put("classOut", 128+classCount);
			addMessage("A class has been introduced into the backend representation of " +
					"your query for later reference to a part of the query. The class id is "+(128+classCount));
		} else {
			group.put("class", classCount);
			group.put("classOut", classCount);
		}
		addMessage(303, "Deprecated 2014-10-07: 'class' only to be supported until 3 months from deprecation date. " +
				"Classes are now defined using the 'classOut' attribute.");
		group.put("operands", new ArrayList<Object>());
		return group;
	}
	
	protected LinkedHashMap<String, Object> makeClassRefCheck(ArrayList<String> check, Integer[] classIn, int classOut) {
		LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
		group.put("@type", "korap:group");
		group.put("operation", "operation:class");
		group.put("classRefCheck", check);
		group.put("classIn", Arrays.asList(classIn));
		group.put("classOut", classOut);
		group.put("class", classOut);
		addMessage(303, "Deprecated 2014-10-07: 'class' only to be supported until 3 months from deprecation date. " +
				"Classes are now defined using the 'classOut' attribute.");
		group.put("operands", new ArrayList<Object>());
		return group;
	}
	
	protected LinkedHashMap<String, Object> makeClassRefOp(String operation, Integer[] classIn, int classOut) {
		LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
		group.put("@type", "korap:group");
		group.put("operation", "operation:class");
		group.put("classRefOp", operation);
		group.put("classIn", Arrays.asList(classIn));
		group.put("classOut", classOut);
		group.put("operands", new ArrayList<Object>());
		return group;
	}
	
	@Deprecated
	protected LinkedHashMap<String, Object> makeTreeRelation(String reltype) {
		LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
		group.put("@type", "korap:treeRelation");
		if (reltype != null) group.put("reltype", reltype);
		return group;
	}
	
	protected LinkedHashMap<String, Object> makeRelation() {
		LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
		group.put("@type", "korap:relation");
		return group;
	}
	
	protected LinkedHashMap<String, Object> makeBoundary(Integer min, Integer max) {
		LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
		group.put("@type", "korap:boundary");
		group.put("min", min);
		if (max != null) {
			group.put("max", max);
		}
		return group;
	}

	protected LinkedHashMap<String, Object> makeDistance(String key, Integer min, Integer max) {
		LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
		if (key.equals("w")) {
			group.put("@type", "korap:distance");
		} else {
			group.put("@type", "cosmas:distance");
		}
		group.put("key", key);
		group.put("boundary", makeBoundary(min, max));
		group.put("min", min);
		if (max != null) {
			group.put("max", max);
		}
		addMessage(303, "Deprecated 2014-07-24: 'min' and 'max' to be supported until 3 months from deprecation date.");
		return group;
	}
	
	protected LinkedHashMap<String, Object> makeReference(ArrayList<Integer> classRefs, String operation) {
		LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
		group.put("@type", "korap:reference");
		group.put("operation", "operation:"+operation);
		if (classRefs!= null && !classRefs.isEmpty()) {
			group.put("classRef", classRefs);
		}
		return group;
	}
	
	protected LinkedHashMap<String, Object> makeReference(ArrayList<Integer> classRefs) {
		return makeReference(classRefs, "focus");
	}
	
	protected LinkedHashMap<String, Object> makeReference(int classRef, String operation, boolean setBySystem) {
		ArrayList<Integer> classRefs = new ArrayList<Integer>();
		if (setBySystem) classRef = classRef+128;
		classRefs.add(classRef);
		return makeReference(classRefs, operation);
	}
	
	protected LinkedHashMap<String, Object> makeReference(int classRef, boolean setBySystem) {
		ArrayList<Integer> classRefs = new ArrayList<Integer>();
		if (setBySystem) classRef = classRef+128;
		classRefs.add(classRef);
		return makeReference(classRefs, "focus");
	}
	
	protected LinkedHashMap<String, Object> makeReference(int classRef) {
		return makeReference(classRef, false);
	}
	
	protected LinkedHashMap<String, Object> makeResetReference() {
		LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
		group.put("@type", "korap:reference");
		group.put("operation", "operation:focus");
		group.put("reset", true);
		group.put("operands", new ArrayList<Object>());
		return group;
	}
	
	protected LinkedHashMap<String, Object> makeSpanReference(Integer[] spanRef, String operation) {
		LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
		group.put("@type", "korap:reference");
		group.put("operation", "operation:"+operation);
		group.put("spanRef", Arrays.asList(spanRef));
		group.put("operands", new ArrayList<Object>());
		return group;
	}
	
	protected void addOperandsToGroup(LinkedHashMap<String, Object> group) {
		ArrayList<Object> operands = new ArrayList<Object>();
		group.put("operands", operands);
	}
	
	protected LinkedHashMap<String, Object> wrapInReference(LinkedHashMap<String, Object> group, Integer classId) {
		LinkedHashMap<String, Object> refGroup = makeReference(classId);
		ArrayList<Object> operands = new ArrayList<Object>();
		operands.add(group);
		refGroup.put("operands", operands);
		return refGroup;
	}

	@SuppressWarnings("unchecked")
	protected LinkedHashMap<String, Object> wrapInClass(LinkedHashMap<String, Object> group, Integer classId) {
		LinkedHashMap<String, Object> classGroup = makeSpanClass(classId, true);
		((ArrayList<Object>) classGroup.get("operands")).add(group);
		return classGroup;
	}
	
	/**
	 * Ensures that a distance or quantification value does not exceed the allowed maximum value. 
	 * @param number
	 * @return The input number if it is below the allowed maximum value, else the maximum value. 
	 */
	protected int cropToMaxValue(int number) {
		if (number > MAXIMUM_DISTANCE) {
			number = MAXIMUM_DISTANCE; 
			String warning = String.format("You specified a distance between two segments that is greater than " +
					"the allowed max value of %d. Your query will be re-interpreted using a distance of %d.", MAXIMUM_DISTANCE, MAXIMUM_DISTANCE);
			addWarning(warning);
			log.warn("User warning: "+warning);
		}
		return number;
	}
	
    public static void checkUnbalancedPars(String q) throws QueryException {
        int openingPars = StringUtils.countMatches(q, "(");
        int closingPars = StringUtils.countMatches(q, ")");
        int openingBrkts = StringUtils.countMatches(q, "[");
        int closingBrkts = StringUtils.countMatches(q, "]");
        int openingBrcs = StringUtils.countMatches(q, "{");
        int closingBrcs = StringUtils.countMatches(q, "}");
        if (openingPars != closingPars) throw new QueryException(
                "Your query string contains an unbalanced number of parantheses.");
        if (openingBrkts != closingBrkts) throw new QueryException(
                "Your query string contains an unbalanced number of brackets.");
        if (openingBrcs != closingBrcs) throw new QueryException(
                "Your query string contains an unbalanced number of braces.");
    }

}
