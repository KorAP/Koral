package de.ids_mannheim.korap.query.serialize;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.runtime.tree.Tree;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang.StringUtils;

import de.ids_mannheim.korap.util.QueryException;

public abstract class AbstractSyntaxTree {
	
	public abstract Map<String, Object> getRequestMap();

	public abstract void process(String query) throws QueryException;
	
	public static final Integer MAXIMUM_DISTANCE = 100; 

	protected LinkedHashMap<String, Object> makeSpan() {
		LinkedHashMap<String, Object> span = new LinkedHashMap<String, Object>();
		span.put("@type", "korap:span");
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
	
	protected LinkedHashMap<String, Object> makeClass(int classCount) {
		LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
		group.put("@type", "korap:group");
		group.put("operation", "operation:class");
		group.put("class", classCount);
		group.put("operands", new ArrayList<Object>());
		return group;
	}
	
	protected LinkedHashMap<String, Object> makeTreeRelation(String reltype) {
		LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
		group.put("@type", "korap:treeRelation");
		if (reltype != null) group.put("reltype", reltype);
		return group;
	}
	
	protected LinkedHashMap<String, Object> makeRelation(String reltype) {
		LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
		group.put("@type", "korap:relation");
		if (reltype != null) group.put("reltype", reltype);
		return group;
	}
	
	protected LinkedHashMap<String, Object> makeBoundary(int min, int max) {
		LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
		group.put("@type", "korap:boundary");
		group.put("min", min);
		group.put("max", max);
		return group;
	}

	protected LinkedHashMap<String, Object> makeDistance(String key, int min, int max) {
		LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
		group.put("@type", "korap:distance");
		group.put("key", key);
		group.put("min", min);
		group.put("max", max);
		return group;
	}
	
	protected LinkedHashMap<String, Object> makeReference(ArrayList<Integer> classRefs) {
		LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
		group.put("@type", "korap:reference");
		group.put("classRef", classRefs);
		return group;
	}
	
	protected LinkedHashMap<String, Object> makeReference(int classRef) {
		ArrayList<Integer> classRefs = new ArrayList<Integer>();
		classRefs.add(classRef);
		return makeReference(classRefs);
	}
	
	protected void addOperandsToGroup(LinkedHashMap<String, Object> group) {
		ArrayList<Object> operands = new ArrayList<Object>();
		group.put("operands", operands);
	}
	
    /**
     * Returns the category (or 'label') of the root of a (sub-) ParseTree (ANTLR 3).
     *
     * @param node
     * @return
     */
    public static String getNodeCat(Tree node) {
        String nodeCat = node.toStringTree();
        Pattern p = Pattern.compile("\\((.*?)\\s"); // from opening parenthesis to 1st whitespace
        Matcher m = p.matcher(node.toStringTree());
        if (m.find()) {
            nodeCat = m.group(1);
        }
        return nodeCat;
    }


    /**
     * Tests whether a certain node has a child by a certain name
     *
     * @param node     The parent node.
     * @param childCat The category of the potential child.
     * @return true iff one or more children belong to the specified category
     */
    public static boolean hasChild(Tree node, String childCat) {
        for (int i = 0; i < node.getChildCount(); i++) {
            if (getNodeCat(node.getChild(i)).equals(childCat)) {
                return true;
            }
        }
        return false;
    }
    


    public static List<Tree> getChildrenWithCat(Tree node, String nodeCat) {
        ArrayList<Tree> children = new ArrayList<Tree>();
        for (int i = 0; i < node.getChildCount(); i++) {
            if (getNodeCat(node.getChild(i)).equals(nodeCat)) {
                children.add(node.getChild(i));
            }
        }
        return children;
    }


    public static List<ParseTree> getChildren(ParseTree node) {
        ArrayList<ParseTree> children = new ArrayList<ParseTree>();
        for (int i = 0; i < node.getChildCount(); i++) {
                children.add(node.getChild(i));
        }
        return children;
    }
    
    public static Tree getFirstChildWithCat(Tree node, String nodeCat) {
        for (int i = 0; i < node.getChildCount(); i++) {
            if (getNodeCat(node.getChild(i)).equals(nodeCat)) {
                return node.getChild(i);
            }
        }
        return null;
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
