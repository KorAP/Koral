package de.ids_mannheim.korap.query.serialize;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * This class is provides methods for navigation and search in
 * Abstract Syntax
 * Trees returned by ANTLR v4 parsers, using ANTLR v4 libraries. Any
 * class that
 * extends this abstract class will thus be equipped with such
 * methods,
 * which makes it easier to, e.g., retrieve children of a specific
 * category
 * for some node.
 * 
 * @author Joachim Bingel (bingel@ids-mannheim.de)
 * @version 0.3.0
 * @since 0.1.0
 */
public abstract class Antlr4AbstractQueryProcessor extends
        AbstractQueryProcessor {

    /**
     * The ANTLR parser. Subclasses need to instantiate this field
     * such that it
     * can be used in the methods of this class.
     */
    protected Parser parser;


    /**
     * Returns the category (or 'label') of the root of a (sub-)
     * ParseTree (ANTLR 4).
     * 
     * @param node
     *            The tree node.
     * @return The category of the node.
     */
    protected String getNodeCat (ParseTree node) {
        String nodeCat = node.toStringTree(parser);
        // pattern: from opening parenthesis to 1st whitespace
        Pattern p = Pattern.compile("\\((.*?)\\s");
        Matcher m = p.matcher(node.toStringTree(parser));
        if (m.find()) {
            nodeCat = m.group(1);
        }
        return nodeCat;
    }


    /**
     * Tests whether a certain node has a child of a certain category.
     * 
     * @param node
     *            The parent node.
     * @param childCat
     *            The category of the potential child.
     * @return true iff one or more children belong to the specified
     *         category.
     */
    protected boolean hasChild (ParseTree node, String childCat) {
        for (int i = 0; i < node.getChildCount(); i++) {
            if (getNodeCat(node.getChild(i)).equals(childCat)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Tests whether a certain node has a descendant (direct or
     * indirect child)
     * of a certain category.
     * 
     * @param node
     *            The parent node.
     * @param childCat
     *            The category of the potential descendant.
     * @return true iff one or more descendants belong to the
     *         specified
     *         category.
     */
    protected boolean hasDescendantWithCat (ParseTree node, String childCat) {
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


    /**
     * Returns all children of a node.
     * 
     * @param node
     *            The node.
     * @return A list containing all children.
     */
    protected List<ParseTree> getChildren (ParseTree node) {
        ArrayList<ParseTree> children = new ArrayList<ParseTree>();
        for (int i = 0; i < node.getChildCount(); i++) {
            children.add(node.getChild(i));
        }
        return children;
    }


    /**
     * Returns all children of a node which are of a given category.
     * 
     * @param node
     *            The node.
     * @param nodeCat
     *            The node category constraining the returned
     *            children.
     * @return A (possibly empty) list containing all children of the
     *         given
     *         category.
     */
    protected List<ParseTree> getChildrenWithCat (ParseTree node, String nodeCat) {
        ArrayList<ParseTree> children = new ArrayList<ParseTree>();
        for (int i = 0; i < node.getChildCount(); i++) {
            if (getNodeCat(node.getChild(i)).equals(nodeCat)) {
                children.add(node.getChild(i));
            }
        }
        return children;
    }


    /**
     * Returns all descendants (direct or indirect children) of a node
     * which
     * are of a given category.
     * 
     * @param node
     *            The node.
     * @param nodeCat
     *            The node category constraining the returned
     *            descendants.
     * @return A (possibly empty) list containing all descendants of
     *         the given
     *         category.
     */
    protected List<ParseTree> getDescendantsWithCat (ParseTree node,
            String nodeCat) {
        ArrayList<ParseTree> descendants = new ArrayList<ParseTree>();
        for (ParseTree child : getChildren(node)) {
            if (getNodeCat(child).equals(nodeCat)) {
                descendants.add(child);
            }
            descendants.addAll(getDescendantsWithCat(child, nodeCat));
        }
        return descendants;
    }


    /**
     * Returns the first child of a node which is of a given category.
     * 
     * @param node
     *            The node.
     * @param nodeCat
     *            The node category constraining the returned child.
     * @return The first child with the given category, <tt>null</tt>
     *         if no
     *         such child exists.
     */
    protected ParseTree getFirstChildWithCat (ParseTree node, String nodeCat) {
        return getNthChildWithCat(node, nodeCat, 1);
    }


    /**
     * Returns the nth child of a node which is of a given category.
     * 
     * @param node
     *            The node.
     * @param nodeCat
     *            The node category constraining the returned child.
     * @param n
     *            The index of the child to return, among all children
     *            with the
     *            given category.
     * @return The nth child with the given category, <tt>null</tt> if
     *         no
     *         such child exists (i.e., if n is larger than the number
     *         of children
     *         with the given category).
     */
    protected ParseTree getNthChildWithCat (ParseTree node, String nodeCat,
            int n) {
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