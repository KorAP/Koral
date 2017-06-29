package de.ids_mannheim.korap.query.serialize;

import de.ids_mannheim.korap.query.object.KoralTermGroupRelation;
import de.ids_mannheim.korap.query.parse.collection.CollectionQueryLexer;
import de.ids_mannheim.korap.query.parse.collection.CollectionQueryParser;
import de.ids_mannheim.korap.query.serialize.util.Antlr4DescriptiveErrorListener;
import de.ids_mannheim.korap.query.serialize.util.KoralObjectGenerator;
import de.ids_mannheim.korap.query.serialize.util.StatusCodes;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class processes queries that define virtual collections and
 * create
 * an KoralQuery representation of these in the <tt>collection</tt>
 * attribute
 * of the KoralQuery tree. See the official documentation for VC query
 * syntax
 * and functionality.
 * 
 * @author Michael Hanl (hanl@ids-mannheim.de)
 * @author Joachim Bingel (bingel@ids-mannheim.de)
 * @version 0.3.0
 * @since 0.1.0
 */
public class CollectionQueryProcessor extends Antlr4AbstractQueryProcessor {

    private static Logger log = LoggerFactory
            .getLogger(CollectionQueryProcessor.class);


    public CollectionQueryProcessor () {
        KoralObjectGenerator.setQueryProcessor(this);
    }


    public CollectionQueryProcessor (boolean verbose) {
        KoralObjectGenerator.setQueryProcessor(this);
        CollectionQueryProcessor.verbose = verbose;
    }


    public CollectionQueryProcessor (String query) {
        KoralObjectGenerator.setQueryProcessor(this);
        process(query);
    }


    @Override
    public void process (String query) {
        ParseTree tree = parseCollectionQuery(query);
        if (this.parser != null) {
            super.parser = this.parser;
        }
        else {
            throw new NullPointerException("Parser has not been instantiated!");
        }
        log.info("Processing virtual collection query: " + query);
        log.debug("ANTLR parse tree: " + tree.toStringTree(parser));
        if (tree != null) {
            processNode(tree);
        }
        else {
            addError(StatusCodes.MALFORMED_QUERY, new String[]{"Could not parse query >>> "
                    + query + " <<<.", query});
        }
    }


    private void processNode (ParseTree node) {
        // Top-down processing
        String nodeCat = getNodeCat(node);
        openNodeCats.push(nodeCat);

        stackedObjects = 0;
        if (verbose) {
            System.err.println(" " + objectStack);
            System.out.println(openNodeCats);
        }

        /*
         ****************************************************************
         **************************************************************** 
         * 			Processing individual node categories  				*
         ****************************************************************
         ****************************************************************
         */

        if (nodeCat.equals("relation")) {
            String operator = getNodeCat(node.getChild(1).getChild(0)).equals(
                    "&") ? "and" : "or";
            Map<String, Object> relationGroup = KoralObjectGenerator
                    .makeDocGroup(operator);
            putIntoSuperObject(relationGroup);
            objectStack.push(relationGroup);
            stackedObjects++;
        }

        if (nodeCat.equals("constraint")) {
            ParseTree fieldNode = getFirstChildWithCat(node, "field");
            String field = fieldNode.getChild(0).toStringTree(parser);
            ParseTree operatorNode = getFirstChildWithCat(node, "operator");
            ParseTree valueNode = getFirstChildWithCat(node, "value");
            Map<String, Object> term = KoralObjectGenerator.makeDoc();
            term.put("key", field);
            term.putAll(parseValue(valueNode));
            String match = operatorNode.getText();
            term.put("match", "match:" + interpretMatchOperator(match));

            if (!checkOperatorValueConformance(term)) {
                addError(StatusCodes.INCOMPATIBLE_OPERATOR_AND_OPERAND, 
                        "Operator "+match+" is not valid.");
                requestMap = new HashMap<String, Object>();
                return;
            }
            //            if (QueryUtils.checkDateValidity(valueNode.getText())) {
            //                addWarning("The collection query contains a value that looks"
            //                        + " like a date ('" + valueNode.getText() + "') and an"
            //                        + " operator that is only defined for strings" + " ('"
            //                        + match + "'). The value is interpreted as"
            //                        + " a string. Use a date operator to ensure the value"
            //                        + " is treated as a date");
            //            }

            putIntoSuperObject(term);
        }

        if (nodeCat.equals("dateconstraint")) {
            ParseTree fieldNode = getFirstChildWithCat(node, "field");
            String field = fieldNode.getChild(0).toStringTree(parser);
            ParseTree dateOpNode = getFirstChildWithCat(node, "dateOp");
            ParseTree dateNode = getFirstChildWithCat(node, "date");

            Map<String, Object> term = KoralObjectGenerator.makeDoc();
            term.put("key", field);
            term.putAll(parseValue(dateNode));
            String match = dateOpNode.getText();
            term.put("match", "match:" + interpretMatchOperator(match));
            if (!checkOperatorValueConformance(term)) {
                requestMap = new HashMap<String, Object>();
                return;
            }
            if (!QueryUtils.checkDateValidity(dateNode.getText())) {
                addError(StatusCodes.MALFORMED_QUERY,
                        "The collection query contains elements "
                                + "that could not be parsed as a date ('"
                                + dateNode.getText() + "')");
                return;
            }
            putIntoSuperObject(term);

        }

        if (nodeCat.equals("token")) {
            Map<String, Object> token = KoralObjectGenerator
                    .makeToken();
            // handle negation
            List<ParseTree> negations = getChildrenWithCat(node, "!");
            boolean negated = false;
            boolean isRegex = false;
            if (negations.size() % 2 == 1)
                negated = true;
            if (getNodeCat(node.getChild(0)).equals("key")) {
                // no 'term' child, but direct key specification: process here
                Map<String, Object> term = KoralObjectGenerator
                        .makeTerm();
                String key = node.getChild(0).getText();
                if (getNodeCat(node.getChild(0).getChild(0)).equals("regex")) {
                    isRegex = true;
                    term.put("type", "type:regex");
                    key = key.substring(1, key.length() - 1);
                }
                term.put("layer", "orth");
                term.put("key", key);
                String matches = negated ? "ne" : "eq";
                term.put("match", "match:" + matches);
                ParseTree flagNode = getFirstChildWithCat(node, "flag");
                if (flagNode != null) {
                    // substring removes leading slash '/'
                    String flag = getNodeCat(flagNode.getChild(0)).substring(1);
                    if (flag.contains("i"))
                        term.put("caseInsensitive", true);
                    else if (flag.contains("I"))
                        term.put("caseInsensitive", false);
                    if (flag.contains("x")) {
                        term.put("type", "type:regex");
                        if (!isRegex) {
                            key = QueryUtils.escapeRegexSpecialChars(key);
                        }
                        term.put("key", ".*?" + key + ".*?"); // overwrite key
                    }
                }
                token.put("wrap", term);
            }
            else {
                // child is 'term' or 'termGroup' -> process in extra method 
                Map<String, Object> termOrTermGroup = parseTermOrTermGroup(
                        node.getChild(1), negated);
                token.put("wrap", termOrTermGroup);
            }
            putIntoSuperObject(token);
            visited.add(node.getChild(0));
            visited.add(node.getChild(2));
        }
        objectsToPop.push(stackedObjects);

        /*
         ****************************************************************
         **************************************************************** 
         *  recursion until 'request' node (root of tree) is processed  *
         ****************************************************************
         ****************************************************************
         */
        for (int i = 0; i < node.getChildCount(); i++) {
            ParseTree child = node.getChild(i);
            processNode(child);
        }

        /*
         **************************************************************
         * Stuff that happens after processing the children of a node *
         **************************************************************
         */
        if (!objectsToPop.isEmpty()) {
            int toPop = objectsToPop.pop();
            for (int i = 0; i < toPop; i++) {
                objectStack.pop();
            }
        }
        openNodeCats.pop();

    }


    /**
     * Checks whether the combination of operator and value is legal
     * (inequation operators <,>,<=,>= may only be used with dates).
     */
    private boolean checkOperatorValueConformance (
            Map<String, Object> term) {
        String match = (String) term.get("match");
        String type = (String) term.get("type");
        if (type == null || type.equals("type:regex")) {
            if (!(match.equals("match:eq") || match.equals("match:ne")
                    || match.equals("match:contains") || match
                        .equals("match:containsnot"))) {
                return false;
            }
        }
        return true;
    }


    private Map<String, Object> parseValue (ParseTree valueNode) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (getNodeCat(valueNode).equals("date")) {
            map.put("type", "type:date");
            checkDateValidity(valueNode);
        }
        String node_cat = getNodeCat(valueNode.getChild(0));

        TokenStream stream = parser.getTokenStream();
        String stm = stream.getText(valueNode.getChild(0).getSourceInterval());
        // todo: is this correct?
        if (stm.startsWith("\"") && stm.endsWith("\""))
            stm = stm.replaceAll("\"", "");

        if ("regex".equals(node_cat)) {
			
            map.put("type", "type:regex");

			// remove leading and trailing slashes
			if (stm.startsWith("/") && stm.endsWith("/")) {
				stm = stm.substring(1, stm.length() - 1);
			};
            map.put("value", stm);
		}
		else if ("multiword".equals(node_cat))
            map.put("value", stm);
        else
            map.put("value", valueNode.getChild(0).toStringTree(parser));

        return map;
    }


    /**
     * Checks if value is a date
     * 
     * @param valueNode
     * @return
     */

    @Deprecated
    private boolean checkDateValidity (ParseTree valueNode) {
        Pattern p = Pattern.compile("[0-9]{4}(-([0-9]{2})(-([0-9]{2}))?)?");
        Matcher m = p.matcher(valueNode.getText());

        if (!m.find())
            return false;
        String month = m.group(2);
        String day = m.group(4);
        if (month != null) {
            if (Integer.parseInt(month) > 12) {
                return false;
            }
            else if (day != null) {
                if (Integer.parseInt(day) > 31) {
                    return false;
                }
            }
        }
        return true;
    }


    private String interpretMatchOperator (String match) {
        String out = null;
        switch (match) {
            case "<":
                out = "lt";
                break;
            case ">":
                out = "gt";
                break;
            case "<=":
                out = "leq";
                break;
            case ">=":
                out = "geq";
                break;
            case "=":
                out = "eq";
                break;
            case "!=":
                out = "ne";
                break;
            case "~":
                out = "contains";
                break;
            case "!~":
                out = "containsnot";
                break;
            case "in":
                out = "eq";
                break;
            case "on":
                out = "eq";
                break;
            case "until":
                out = "leq";
                break;
            case "since":
                out = "geq";
                break;
            default:
                out = match;
                addError(StatusCodes.UNKNOWN_QUERY_ELEMENT,
                        "Unknown operator '" + match + "'.");
                break;
        }
        return out;
    }


    @Deprecated
    private String invertInequation (String op) {
        String inv = null;
        switch (op) {
            case "lt":
                inv = "gt";
                break;
            case "leq":
                inv = "geq";
                break;
            case "gt":
                inv = "lt";
                break;
            case "geq":
                inv = "leq";
                break;
        }
        return inv;
    }


    private void putIntoSuperObject (Map<String, Object> object) {
        putIntoSuperObject(object, 0);
    }


    @SuppressWarnings({ "unchecked" })
    private void putIntoSuperObject (Map<String, Object> object,
            int objStackPosition) {
        if (objectStack.size() > objStackPosition) {
            ArrayList<Object> topObjectOperands = (ArrayList<Object>) objectStack
                    .get(objStackPosition).get("operands");
            topObjectOperands.add(object);
        }
        else {
            //        	requestMap = object;
            requestMap.put("collection", object);
        }
    }


    private Map<String, Object> parseTermOrTermGroup (ParseTree node,
            boolean negated) {
        return parseTermOrTermGroup(node, negated, "token");
    }


    /**
     * Parses a (term) or (termGroup) node
     * 
     * @param node
     * @param negatedGlobal
     *            Indicates whether the term/termGroup is globally
     *            negated, e.g. through a negation operator preceding
     *            the related token
     *            like "![base=foo]". Global negation affects the
     *            "match" parameter.
     * @return A term or termGroup object, depending on input
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseTermOrTermGroup (ParseTree node,
            boolean negatedGlobal, String mode) {
        if (getNodeCat(node).equals("term")) {
            String key = null;
            Map<String, Object> term = KoralObjectGenerator
                    .makeTerm();
            // handle negation
            boolean negated = negatedGlobal;
            boolean isRegex = false;
            List<ParseTree> negations = getChildrenWithCat(node, "!");
            if (negations.size() % 2 == 1)
                negated = !negated;
            // retrieve possible nodes
            ParseTree keyNode = getFirstChildWithCat(node, "key");
            ParseTree valueNode = getFirstChildWithCat(node, "value");
            ParseTree layerNode = getFirstChildWithCat(node, "layer");
            ParseTree foundryNode = getFirstChildWithCat(node, "foundry");
            ParseTree termOpNode = getFirstChildWithCat(node, "termOp");
            ParseTree flagNode = getFirstChildWithCat(node, "flag");
            // process foundry
            if (foundryNode != null)
                term.put("foundry", foundryNode.getText());
            // process layer: map "base" -> "lemma"
            if (layerNode != null) {
                String layer = layerNode.getText();
                if (layer.equals("base"))
                    layer = "lemma";
                if (mode.equals("span"))
                    term.put("key", layer);
                else
                    term.put("layer", layer);
            }
            // process key: 'normal' or regex?
            key = keyNode.getText();
            if (getNodeCat(keyNode.getChild(0)).equals("regex")) {
                isRegex = true;
                term.put("type", "type:regex");
                // remove leading and trailing slashes
                key = key.substring(1, key.length() - 1);
            }
            if (mode.equals("span"))
                term.put("value", key);
            else
                term.put("key", key);
            // process value
            if (valueNode != null)
                term.put("value", valueNode.getText());
            // process operator ("match" property)
            if (termOpNode != null) {
                String termOp = termOpNode.getText();
                negated = termOp.contains("!") ? !negated : negated;
                if (!negated)
                    term.put("match", "match:eq");
                else
                    term.put("match", "match:ne");
            }
            // process possible flags
            if (flagNode != null) {
                // substring removes leading slash '/'
                String flag = getNodeCat(flagNode.getChild(0)).substring(1);
                if (flag.contains("i"))
                    term.put("caseInsensitive", true);
                else if (flag.contains("I"))
                    term.put("caseInsensitive", false);
                if (flag.contains("x")) {
                    if (!isRegex) {
                        key = QueryUtils.escapeRegexSpecialChars(key);
                    }
                    // flag 'x' allows submatches: append .*? to key
                    term.put("key", ".*?" + key + ".*?");
                    term.put("type", "type:regex");
                }
            }
            return term;
        }
        else {
            // For termGroups, establish a boolean relation between operands 
            // and recursively call this function with the term or termGroup 
            // operands.
            Map<String, Object> termGroup = null;
            ParseTree leftOp = null;
            ParseTree rightOp = null;
            // check for leading/trailing parantheses
            if (!getNodeCat(node.getChild(0)).equals("("))
                leftOp = node.getChild(0);
            else
                leftOp = node.getChild(1);
            if (!getNodeCat(node.getChild(node.getChildCount() - 1))
                    .equals(")"))
                rightOp = node.getChild(node.getChildCount() - 1);
            else
                rightOp = node.getChild(node.getChildCount() - 2);
            // establish boolean relation
            ParseTree boolOp = getFirstChildWithCat(node, "booleanOp");
            if (boolOp.getText().equals("&")){
                termGroup = KoralObjectGenerator.makeTermGroup(KoralTermGroupRelation.AND);
            }
            else {
                termGroup = KoralObjectGenerator.makeTermGroup(KoralTermGroupRelation.OR);
            }
            ArrayList<Object> operands = (ArrayList<Object>) termGroup
                    .get("operands");
            // recursion with left/right operands
            operands.add(parseTermOrTermGroup(leftOp, negatedGlobal, mode));
            operands.add(parseTermOrTermGroup(rightOp, negatedGlobal, mode));
            return termGroup;
        }
    }


    private ParserRuleContext parseCollectionQuery (String query) {
        Lexer lexer = new CollectionQueryLexer((CharStream) null);
        ParserRuleContext tree = null;
        Antlr4DescriptiveErrorListener errorListener = new Antlr4DescriptiveErrorListener(
                query);
        // Like p. 111
        try {

            // Tokenize input data
            ANTLRInputStream input = new ANTLRInputStream(query);
            lexer.setInputStream(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            parser = new CollectionQueryParser(tokens);

            // Don't throw out erroneous stuff
            parser.setErrorHandler(new BailErrorStrategy());
            lexer.removeErrorListeners();
            lexer.addErrorListener(errorListener);
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);
            // Get starting rule from parser
            Method startRule = CollectionQueryParser.class.getMethod("start");
            tree = (ParserRuleContext) startRule
                    .invoke(parser, (Object[]) null);
        }
        // Some things went wrong ...
        catch (Exception e) {
            System.err
                    .println("ERROR: " + errorListener.generateFullErrorMsg());
            System.err.println("Parsing exception message: " + e);
        }
        // Return the generated tree
        return tree;
    }
}
