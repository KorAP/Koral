package de.ids_mannheim.korap.query.serialize;

import de.ids_mannheim.korap.query.serialize.util.CollectionQueryLexer;
import de.ids_mannheim.korap.query.serialize.util.CollectionQueryParser;
import de.ids_mannheim.korap.util.QueryException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author hanl, bingel
 * @date 06/12/2013
 */
public class CollectionQueryTree extends Antlr4AbstractSyntaxTree {

    private Parser parser;
    private boolean verbose = false;
    private List<ParseTree> visited = new ArrayList<ParseTree>();
    /**
     * Top-level map representing the whole request.
     */
    LinkedHashMap<String, Object> requestMap = new LinkedHashMap<String, Object>();
    /**
     * Keeps track of active object.
     */
    LinkedList<LinkedHashMap<String, Object>> objectStack = new LinkedList<LinkedHashMap<String, Object>>();
    /**
     * Keeps track of open node categories
     */
    LinkedList<String> openNodeCats = new LinkedList<String>();
    /**
     * Keeps track of how many objects there are to pop after every recursion of {@link #processNode(ParseTree)}
     */
    LinkedList<Integer> objectsToPop = new LinkedList<Integer>();
    Integer stackedObjects = 0;

    @Override
    public void process(String query) throws QueryException {
        ParseTree tree = parseCollectionQuery(query);
        if (this.parser != null) {
            super.parser = this.parser;
        } else {
            throw new NullPointerException("Parser has not been instantiated!");
        }
        System.out.println("Processing collection query");
        if (verbose) System.out.println(tree.toStringTree(parser));
        processNode(tree);
    }

    private void processNode(ParseTree node) {
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
        	String operator = node.getChild(1).getChild(0).toStringTree(parser).equals("&") ? "and" : "or"; 
            LinkedHashMap<String, Object> relationGroup = makeDocGroup(operator);
            putIntoSuperObject(relationGroup);
            objectStack.push(relationGroup);
            stackedObjects++;
        }

//        if (nodeCat.equals("orGroup")) {
//            LinkedHashMap<String, Object> exprGroup = makeDocGroup("or");
//            putIntoSuperObject(exprGroup);
//            objectStack.push(exprGroup);
//            stackedObjects++;
//        }

        if (nodeCat.equals("meta")) {
            ParseTree fieldNode = getFirstChildWithCat(node, "field");
            String field = fieldNode.getChild(0).toStringTree(parser);
            List<ParseTree> operatorNodes = getChildrenWithCat(node, "operator");
            List<ParseTree> valueNodes = getChildrenWithCat(node, "value");

            if (valueNodes.size() == 1) {
                LinkedHashMap<String, Object> term = makeDoc();
                term.put("key", field);
                term.putAll(parseValue(valueNodes.get(0)));
                String match = operatorNodes.get(0).getText();
                term.put("match", "match:" + interpretMatch(match));
                putIntoSuperObject(term);
            } else { // (valueNodes.size()==2)
                LinkedHashMap<String, Object> termGroup = makeDocGroup("and");
                ArrayList<Object> termGroupOperands = (ArrayList<Object>) termGroup.get("operands");

                LinkedHashMap<String, Object> term1 = makeDoc();
                term1.put("key", field);
                term1.putAll(parseValue(valueNodes.get(0)));
                String match1 = operatorNodes.get(0).getText();
                term1.put("match", "match:" + invertInequation(interpretMatch(match1)));
                termGroupOperands.add(term1);

                LinkedHashMap<String, Object> term2 = makeDoc();
                term2.put("key", field);
                term2.putAll(parseValue(valueNodes.get(1)));
                String match2 = operatorNodes.get(1).getText();
                term2.put("match", "match:" + interpretMatch(match2));
                termGroupOperands.add(term2);

                putIntoSuperObject(termGroup);
            }

        }
        
        if (nodeCat.equals("token")) {
			LinkedHashMap<String,Object> token = makeToken();
			// handle negation
			List<ParseTree> negations = getChildrenWithCat(node, "!");
			boolean negated = false;
			boolean isRegex = false;
			if (negations.size() % 2 == 1) negated = true;
			if (getNodeCat(node.getChild(0)).equals("key")) {
				// no 'term' child, but direct key specification: process here
				LinkedHashMap<String,Object> term = makeTerm();
				
				String key = node.getChild(0).getText();
				if (getNodeCat(node.getChild(0).getChild(0)).equals("regex")) {
					isRegex = true;
					term.put("type", "type:regex");
					key = key.substring(1,key.length()-1);
				}
				term.put("layer", "orth");
				term.put("key", key);
				String matches = negated ? "ne" : "eq";
				term.put("match", "match:"+matches);
				ParseTree flagNode = getFirstChildWithCat(node, "flag");
				if (flagNode != null) {
					String flag = getNodeCat(flagNode.getChild(0)).substring(1); //substring removes leading slash '/'
					if (flag.contains("i")) term.put("caseInsensitive", true);
					else if (flag.contains("I")) term.put("caseInsensitive", false);
					if (flag.contains("x")) {
						term.put("type", "type:regex");
						if (!isRegex) {
							key = QueryUtils.escapeRegexSpecialChars(key); 
						}
						term.put("key", ".*?"+key+".*?"); // overwrite key
					}
				}
				token.put("wrap", term);
			} else {
				// child is 'term' or 'termGroup' -> process in extra method 
				LinkedHashMap<String,Object> termOrTermGroup = parseTermOrTermGroup(node.getChild(1), negated);
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


    private LinkedHashMap<String, Object> parseValue(ParseTree valueNode) {
    	LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
    	if (getNodeCat(valueNode.getChild(0)).equals("regex")) {
    		String regex = valueNode.getChild(0).getChild(0).toStringTree(parser);
    		map.put("value", regex.substring(1, regex.length()-1));
    		map.put("type", "type:regex");
    	} else {
    		map.put("value", valueNode.getChild(0).toStringTree(parser));
    	}
		return map;
	}

	private String interpretMatch(String match) {
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
        }
        return out;
    }

    private String invertInequation(String op) {
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

    private void putIntoSuperObject(LinkedHashMap<String, Object> object) {
        putIntoSuperObject(object, 0);
    }

    @SuppressWarnings({"unchecked"})
    private void putIntoSuperObject(LinkedHashMap<String, Object> object, int objStackPosition) {
        if (objectStack.size() > objStackPosition) {
            ArrayList<Object> topObjectOperands = (ArrayList<Object>) objectStack.get(objStackPosition).get("operands");
            topObjectOperands.add(object);
        } else {
            // I want the raw object, not a wrapped
//            requestMap.put("filter", object);
        	requestMap = object;
        }
    }

    private LinkedHashMap<String, Object> parseTermOrTermGroup(
			ParseTree node, boolean negated) {
		return parseTermOrTermGroup(node, negated, "token");
	}
    
    /**
	 * Parses a (term) or (termGroup) node
	 * @param node
	 * @param negatedGlobal Indicates whether the term/termGroup is globally negated, e.g. through a negation 
	 * operator preceding the related token like "![base=foo]". Global negation affects the term's "match" parameter.
	 * @return A term or termGroup object, depending on input
	 */
	@SuppressWarnings("unchecked")
	private LinkedHashMap<String, Object> parseTermOrTermGroup(ParseTree node, boolean negatedGlobal, String mode) {
		if (getNodeCat(node).equals("term")) {
			String key = null;
			LinkedHashMap<String,Object> term = makeTerm();
			// handle negation
			boolean negated = negatedGlobal;
			boolean isRegex = false;
			List<ParseTree> negations = getChildrenWithCat(node, "!");
			if (negations.size() % 2 == 1) negated = !negated;
			// retrieve possible nodes
			ParseTree keyNode = getFirstChildWithCat(node, "key");
			ParseTree valueNode = getFirstChildWithCat(node, "value");
			ParseTree layerNode = getFirstChildWithCat(node, "layer");
			ParseTree foundryNode = getFirstChildWithCat(node, "foundry");
			ParseTree termOpNode = getFirstChildWithCat(node, "termOp");
			ParseTree flagNode = getFirstChildWithCat(node, "flag");
			// process foundry
			if (foundryNode != null) term.put("foundry", foundryNode.getText());
			// process layer: map "base" -> "lemma"
			if (layerNode != null) {
				String layer = layerNode.getText();
				if (layer.equals("base")) layer="lemma";
				if (mode.equals("span")) term.put("key", layer);
				else term.put("layer", layer);
			}
			// process key: 'normal' or regex?
			key = keyNode.getText();
			if (getNodeCat(keyNode.getChild(0)).equals("regex")) {
				isRegex = true;
				term.put("type", "type:regex");
				key = key.substring(1, key.length()-1); // remove leading and trailing quotes
			}
			if (mode.equals("span")) term.put("value", key);
			else term.put("key", key);
			// process value
			if (valueNode != null) term.put("value", valueNode.getText());
			// process operator ("match" property)
			if (termOpNode != null) {
				String termOp = termOpNode.getText();
				negated = termOp.contains("!") ? !negated : negated; 
				if (!negated) term.put("match", "match:eq");
				else term.put("match", "match:ne");
			}
			// process possible flags
			if (flagNode != null) {
				String flag = getNodeCat(flagNode.getChild(0)).substring(1); //substring removes leading slash '/'
				if (flag.contains("i")) term.put("caseInsensitive", true);
				else if (flag.contains("I")) term.put("caseInsensitive", false);
				if (flag.contains("x")) {
					if (!isRegex) {
						key = QueryUtils.escapeRegexSpecialChars(key); 
					}
					term.put("key", ".*?"+key+".*?");  // flag 'x' allows submatches: overwrite key with appended .*? 
					term.put("type", "type:regex");
				}
			}
			return term;
		} else {
			// For termGroups, establish a boolean relation between operands and recursively call this function with
			// the term or termGroup operands
			LinkedHashMap<String,Object> termGroup = null;
			ParseTree leftOp = null;
			ParseTree rightOp = null;
			// check for leading/trailing parantheses
			if (!getNodeCat(node.getChild(0)).equals("(")) leftOp = node.getChild(0);
			else leftOp = node.getChild(1);
			if (!getNodeCat(node.getChild(node.getChildCount()-1)).equals(")")) rightOp = node.getChild(node.getChildCount()-1);
			else rightOp = node.getChild(node.getChildCount()-2);
			// establish boolean relation
			ParseTree boolOp = getFirstChildWithCat(node, "booleanOp"); 
			String operator = boolOp.getText().equals("&") ? "and" : "or";
			termGroup = makeTermGroup(operator);
			ArrayList<Object> operands = (ArrayList<Object>) termGroup.get("operands");
			// recursion with left/right operands
			operands.add(parseTermOrTermGroup(leftOp, negatedGlobal, mode));
			operands.add(parseTermOrTermGroup(rightOp, negatedGlobal, mode));
			return termGroup;
		}
	}
    
    private ParserRuleContext parseCollectionQuery(String p) throws QueryException {
        Lexer collectionQueryLexer = new CollectionQueryLexer((CharStream) null);
        ParserRuleContext tree = null;
        // Like p. 111
        try {

            // Tokenize input data
            ANTLRInputStream input = new ANTLRInputStream(p);
            collectionQueryLexer.setInputStream(input);
            CommonTokenStream tokens = new CommonTokenStream(collectionQueryLexer);
            parser = new CollectionQueryParser(tokens);

            // Don't throw out erroneous stuff
            parser.setErrorHandler(new BailErrorStrategy());
            parser.removeErrorListeners();
            // Get starting rule from parser
            Method startRule = CollectionQueryParser.class.getMethod("start");
            tree = (ParserRuleContext) startRule.invoke(parser, (Object[]) null);
            System.out.println(tree.toStringTree(parser));

        }
        // Some things went wrong ...
        catch (Exception e) {
            System.err.println("Parsing exception message: " + e.getMessage());
        }
        if (tree == null) {
            throw new QueryException("Could not parse query. Make sure it is correct syntax.");
        }
        // Return the generated tree
        return tree;
    }

    public static void main(String[] args) {
        String query = "foo=bar&c=d";
        query = "(1990<year<2010&genre=Sport)|textClass=politk";
        query = "(textClass=wissenschaft & textClass=politik) | textClass=ausland";
        query = "1990<year<2010 & genre=Sport";
        query = "1990<year<2010";
        query = "year=2010   ";
        CollectionQueryTree filter = new CollectionQueryTree();
//    	filter.verbose = true;
        try {
            filter.process(query);
        } catch (QueryException e) {
            e.printStackTrace();
        }
        System.out.println(filter.getRequestMap());

    }

    @Override
    public Map<String, Object> getRequestMap() {
        return requestMap;
    }


}
