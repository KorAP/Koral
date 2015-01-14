package de.ids_mannheim.korap.query.serialize;

import java.util.ArrayList;
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
	protected String getNodeCat(ParseTree node) {
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
    protected boolean hasChild(ParseTree node, String childCat) {
        for (int i = 0; i < node.getChildCount(); i++) {
            if (getNodeCat(node.getChild(i)).equals(childCat)) {
                return true;
            }
        }
        return false;
    }

    protected boolean hasDescendantWithCat(ParseTree node, String childCat) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ParseTree child = node.getChild(i);
            if (getNodeCat(child).equals(childCat)) {
                return true;
            }
            if (hasDescendantWithCat(child, childCat)) {
                return true;
            }
        }
        return false;
    }
    

    protected List<ParseTree> getChildren(ParseTree node) {
        ArrayList<ParseTree> children = new ArrayList<ParseTree>();
        for (int i = 0; i < node.getChildCount(); i++) {
                children.add(node.getChild(i));
        }
        return children;
    }
    
    protected List<ParseTree> getChildrenWithCat(ParseTree node, String nodeCat) {
        ArrayList<ParseTree> children = new ArrayList<ParseTree>();
        for (int i = 0; i < node.getChildCount(); i++) {
            if (getNodeCat(node.getChild(i)).equals(nodeCat)) {
                children.add(node.getChild(i));
            }
        }
        return children;
    }

    protected ParseTree getFirstChildWithCat(ParseTree node, String nodeCat) {
        return getNthChildWithCat(node, nodeCat, 1);
    }
    
    protected ParseTree getNthChildWithCat(ParseTree node, String nodeCat, int n) {
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
}