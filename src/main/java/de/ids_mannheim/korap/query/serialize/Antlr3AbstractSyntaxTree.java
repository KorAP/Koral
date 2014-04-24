package de.ids_mannheim.korap.query.serialize;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.runtime.Parser;
import org.antlr.runtime.tree.Tree;

public abstract class Antlr3AbstractSyntaxTree extends AbstractSyntaxTree {
	
	public Parser parser;

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
    
    public static Tree getFirstChildWithCat(Tree node, String nodeCat) {
        for (int i = 0; i < node.getChildCount(); i++) {
            if (getNodeCat(node.getChild(i)).equals(nodeCat)) {
                return node.getChild(i);
            }
        }
        return null;
    }
}
