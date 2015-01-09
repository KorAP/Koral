package de.ids_mannheim.korap.query.serialize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;

public abstract class Antlr4AbstractQueryProcessor extends AbstractQueryProcessor {

	/**
	 * Parser object deriving the ANTLR parse tree.
	 */
	protected Parser parser;

	 /**
     * Returns the category (or 'label') of the root of a (sub-) ParseTree (ANTLR 4).
     *
     * @param node
     * @return
     */
    public String getNodeCat(ParseTree node) {
        String nodeCat = node.toStringTree(parser);
        Pattern p = Pattern.compile("\\((.*?)\\s"); // from opening parenthesis to 1st whitespace
        Matcher m = p.matcher(node.toStringTree(parser));
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
    public boolean hasChild(ParseTree node, String childCat) {
        for (int i = 0; i < node.getChildCount(); i++) {
            if (getNodeCat(node.getChild(i)).equals(childCat)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasDescendant(ParseTree node, String childCat) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ParseTree child = node.getChild(i);
            if (getNodeCat(child).equals(childCat)) {
                return true;
            }
            if (hasDescendant(child, childCat)) {
                return true;
            }
        }
        return false;
    }
    

    public static List<ParseTree> getChildren(ParseTree node) {
        ArrayList<ParseTree> children = new ArrayList<ParseTree>();
        for (int i = 0; i < node.getChildCount(); i++) {
                children.add(node.getChild(i));
        }
        return children;
    }
    
    public List<ParseTree> getChildrenWithCat(ParseTree node, String nodeCat) {
        ArrayList<ParseTree> children = new ArrayList<ParseTree>();
        for (int i = 0; i < node.getChildCount(); i++) {
            if (getNodeCat(node.getChild(i)).equals(nodeCat)) {
                children.add(node.getChild(i));
            }
        }
        return children;
    }

    public ParseTree getFirstChildWithCat(ParseTree node, String nodeCat) {
        return getNthChildWithCat(node, nodeCat, 1);
    }
    
    public ParseTree getNthChildWithCat(ParseTree node, String nodeCat, int n) {
    	int counter = 0;
    	for (int i = 0; i < node.getChildCount(); i++) {
    		if (getNodeCat(node.getChild(i)).equals(nodeCat)) {
    			counter++;
    			if (counter == n) {
    				return node.getChild(i);
    			}
    		}
    	}
        return null;
    }
    
    /**
     * Checks whether a node only serves as a container for another node (e.g. in (cq_segment ( cg_seg_occ ...)), the cq_segment node does not contain
     * any information and only contains the cq_seg_occ node.  
     * @param node The node to check
     * @return true iff the node is a container only.
     */
    public boolean isContainerOnly(ParseTree node) {
    	String[] validNodeNamesArray = "cq_segment sq_segment element empty_segments spanclass".split(" ");
    	List<String> validNodeNames = Arrays.asList(validNodeNamesArray);
    	List<ParseTree> children = getChildren(node);
    	for (ParseTree child : children) {
    		if (validNodeNames.contains(getNodeCat(child))) {
    			return false;
    		}
    	}
    	return true;
    }
	
}
