package de.ids_mannheim.korap.query.serialize;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.runtime.Parser;
import org.antlr.runtime.tree.Tree;

public abstract class Antlr3AbstractQueryProcessor extends
        AbstractQueryProcessor {

    protected Parser parser;

    /**
     * Returns the category (or 'label') of the root of a (sub-)
     * ParseTree (ANTLR 3).
     *
     * @param node
     * @return
     */
    protected static String getNodeCat(Tree node) {
        String nodeCat = node.toStringTree();
        // from opening parenthesis to 1st whitespace
        Pattern p = Pattern.compile("\\((.*?)\\s"); 
        Matcher m = p.matcher(node.toStringTree());
        if (m.find()) {
            nodeCat = m.group(1);
        }
        return nodeCat;
    }

    /**
     * Tests whether a certain node has a child by a certain name
     *
     * @param node
     *            The parent node.
     * @param childCat
     *            The category of the potential child.
     * @return true iff one or more children belong to the specified
     *         category
     */
    protected static boolean hasChild(Tree node, String childCat) {
        for (int i = 0; i < node.getChildCount(); i++) {
            if (getNodeCat(node.getChild(i)).equals(childCat)) {
                return true;
            }
        }
        return false;
    }

    protected static boolean hasDescendantWithCat(Tree node, String childCat) {
        for (int i = 0; i < node.getChildCount(); i++) {
            Tree child = node.getChild(i);
            if (getNodeCat(child).equals(childCat)) {
                return true;
            }
            if (hasDescendantWithCat(child, childCat)) {
                return true;
            }
        }
        return false;
    }

    protected static List<Tree> getChildren(Tree node) {
        ArrayList<Tree> children = new ArrayList<Tree>();
        for (int i = 0; i < node.getChildCount(); i++) {
            children.add(node.getChild(i));
        }
        return children;
    }

    protected static List<Tree> getChildrenWithCat(Tree node, String nodeCat) {
        ArrayList<Tree> children = new ArrayList<Tree>();
        for (int i = 0; i < node.getChildCount(); i++) {
            if (getNodeCat(node.getChild(i)).equals(nodeCat)) {
                children.add(node.getChild(i));
            }
        }
        return children;
    }

    protected static Tree getFirstChildWithCat(Tree node, String nodeCat) {
        return getNthChildWithCat(node, nodeCat, 1);
    }

    protected static Tree getNthChildWithCat(Tree node, String nodeCat, int n) {
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
