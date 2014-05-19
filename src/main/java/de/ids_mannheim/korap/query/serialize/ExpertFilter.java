package de.ids_mannheim.korap.query.serialize;

import de.ids_mannheim.korap.query.serialize.util.CollectionQueryParser;
import de.ids_mannheim.korap.query.serialize.util.CollectionQueryLexer;
import de.ids_mannheim.korap.util.QueryException;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.LoggerFactory;

/**
 * @author bingel
 * @date 12/05/2014
 */
public class ExpertFilter extends Antlr4AbstractSyntaxTree {
	
    private org.slf4j.Logger log = LoggerFactory
            .getLogger(ExpertFilter.class);

	private Parser parser;
	private boolean verbose = false;
	private List<ParseTree> visited = new ArrayList<ParseTree>();
	/**
	 * Top-level map representing the whole request.
	 */
	LinkedHashMap<String,Object> requestMap = new LinkedHashMap<String,Object>();
	/**
	 * Keeps track of active object.
	 */
	LinkedList<LinkedHashMap<String,Object>> objectStack = new LinkedList<LinkedHashMap<String,Object>>();
	/**
	 * Keeps track of open node categories
	 */
	LinkedList<String> openNodeCats = new LinkedList<String>();
	/**
	 * Keeps track of how many objects there are to pop after every recursion of {@link #processNode(ParseTree)}
	 */
	LinkedList<Integer> objectsToPop = new LinkedList<Integer>();
	Integer stackedObjects = 0;


    public ExpertFilter() {
    }
    
	@Override
	public void process(String query) throws QueryException {
		ParseTree tree = parseCollectionQuery(query);
		if (this.parser != null) {
			super.parser = this.parser;
		} else {
			throw new NullPointerException("Parser has not been instantiated!"); 
		}
		
		log.info("Processing collection query: "+query);
		if (verbose) System.out.println(tree.toStringTree(parser));
		requestMap.put("@type", "korap:filter");
		processNode(tree);
		log.info(requestMap.toString());
	}

	private void processNode(ParseTree node) {
		// Top-down processing
		String nodeCat = getNodeCat(node);
		openNodeCats.push(nodeCat);
		
		stackedObjects = 0;
		
		if (verbose) {
			System.err.println(" "+objectStack);
			System.out.println(openNodeCats);
		}

		/*
		 ****************************************************************
		 **************************************************************** 
		 * 			Processing individual node categories  				*
		 ****************************************************************
		 ****************************************************************
		 */
		
		if (nodeCat.equals("andGroup")) {
			LinkedHashMap<String, Object> exprGroup = makeTermGroup("and");
			objectStack.push(exprGroup);
			stackedObjects++;
			putIntoSuperObject(exprGroup,1);
		}
		
		if (nodeCat.equals("orGroup")) {
			LinkedHashMap<String, Object> exprGroup = makeTermGroup("or");
			objectStack.push(exprGroup);
			stackedObjects++;
			putIntoSuperObject(exprGroup,1);
		}
		
		if (nodeCat.equals("expr")) {
			ParseTree fieldNode = getFirstChildWithCat(node, "field");
			String field = fieldNode.getChild(0).toStringTree(parser);
			List<ParseTree> operatorNodes = getChildrenWithCat(node, "operator");
			List<ParseTree> valueNodes = getChildrenWithCat(node, "value");
			
			if (valueNodes.size()==1) {
				LinkedHashMap<String, Object> term = makeTerm();
				term.put("attribute", field);
				term.putAll(parseValue(valueNodes.get(0)));
				String match = operatorNodes.get(0).getChild(0).toStringTree(parser);
				term.put("match", "match:"+interpretMatch(match));
				putIntoSuperObject(term);
			} else { // (valueNodes.size()==2)
				LinkedHashMap<String, Object> termGroup = makeTermGroup("and");
				@SuppressWarnings("unchecked")
				ArrayList<Object> termGroupOperands = (ArrayList<Object>) termGroup.get("operands");
				
				LinkedHashMap<String, Object> term1 = makeTerm();
				term1.put("attribute", field);
				term1.putAll(parseValue(valueNodes.get(0)));
				String match1 = operatorNodes.get(0).getChild(0).toStringTree(parser);
				term1.put("match", "match:"+invertInequation(interpretMatch(match1)));
				termGroupOperands.add(term1);
				
				LinkedHashMap<String, Object> term2 = makeTerm();
				term2.put("attribute", field);
				term2.putAll(parseValue(valueNodes.get(1)));
				String match2 = operatorNodes.get(1).getChild(0).toStringTree(parser);
				term2.put("match", "match:"+interpretMatch(match2));
				termGroupOperands.add(term2);
				
				putIntoSuperObject(termGroup);
			}
			
		}
		
		objectsToPop.push(stackedObjects);

		/*
		 ****************************************************************
		 **************************************************************** 
		 *  recursion until 'request' node (root of tree) is processed  *
		 ****************************************************************
		 ****************************************************************
		 */
		for (int i=0; i<node.getChildCount(); i++) {
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
			for (int i=0; i<toPop; i++) {
				objectStack.pop();
			}
		}
		openNodeCats.pop();
		
		
	}

	
	
	private LinkedHashMap<String, Object> parseValue(ParseTree node) {
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		String key = "";
		if (getNodeCat(node.getChild(0)).equals("regex")) {
			key = node.getChild(0).getChild(0).toStringTree(parser);
			key = key.substring(1,key.length()-1); //remove leading and trailing slashes
			map.put("key", key);
			map.put("type", "type:regex");
		}
		else {
			if (node.getChildCount() == 1) {
				key = node.getChild(0).toStringTree(parser);
			} else {
				Pattern p = Pattern.compile("\" (.*) \"");
				Matcher m = p.matcher(node.toStringTree(parser));
				if (m.find()) {
					key = m.group(1);
				}
			}
			map.put("key", key);
		}
		return map;
	}

	private String interpretMatch(String match) {
		String out = null;
		       if (match.equals("<")) {
		    	   out = "lt";
		} else if (match.equals(">")) {
			out = "gt";
		} else if (match.equals("<=")) {
			out = "leq";
		} else if (match.equals(">=")) {
			out = "geq";
		} else if (match.equals("=")) {
			out = "eq";
		} else if (match.equals("!=")) {
			out = "ne";
		}
		return out;
	}

	private String invertInequation(String op) {
		String inv = null;
			   if (op.equals("lt")) {
			inv = "gt";
		} else if (op.equals("leq")) {
			inv = "geq";
		} else if (op.equals("gt")) {
			inv = "lt";
		} else if (op.equals("geq")) {
			inv = "leq";
		}
		return inv;
	}

	private void putIntoSuperObject(LinkedHashMap<String, Object> object) {
		putIntoSuperObject(object, 0);
	}
	
	@SuppressWarnings({ "unchecked" })
	private void putIntoSuperObject(LinkedHashMap<String, Object> object, int objStackPosition) {
		if (objectStack.size()>objStackPosition) {
			ArrayList<Object> topObjectOperands = (ArrayList<Object>) objectStack.get(objStackPosition).get("operands");
			topObjectOperands.add(object);
			
		} else {
			requestMap.put("filter", object);
		}
	}
    
	private ParserRuleContext parseCollectionQuery (String p) throws QueryException {
		Lexer collectionQueryLexer = new CollectionQueryLexer((CharStream)null);
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
	      tree = (ParserRuleContext) startRule.invoke(parser, (Object[])null);
	      
	    }
	    // Some things went wrong ...
	    catch (Exception e) {
	      System.err.println( e.getMessage() );
	      log.error(e.getMessage());
	    }
	    if (tree == null) {
	    	log.error("Could not parse expert filter query. Make sure it is correct syntax.");
	    	throw new QueryException("Could not parse expert filter query. Make sure it is correct syntax.");
	    }
	    // Return the generated tree
	    return tree;
	  }
	

	@Override
	public Map<String, Object> getRequestMap() {
		return requestMap;
	}

    
    public static void main(String[] args) {
    	String query = "foo=bar&c=d";
    	query = "(1990<year<2010&genre=Sport)|textClass=politk";
    	query = "(textClass=wissenschaft & textClass=politik) | textClass=ausland";
    	query = "1990<year<2010 oder genre=Sport";
    	query = "title=\"Der Titel\"";
    	query = "(corpusID=A00 & corpusID=WPD) | textClass=wissenschaft ";
    	query = "(corpusID=A00 | corpusID=WPD) & (textClass=wissenschaft & textClass=politik)";
//    	query = "corpusID=A00 & corpusID=WPD & textClass=wissenschaft";
//    	query = "corpusID=A00 | corpusID=WPD";
    	query = "(textClass=wissenschaft & textClass=politik) & (corpusID=A00 | corpusID=WPD)";
    	query = "textClass=wissenschaft | (textClass=politik | corpusID=A00)";
    	ExpertFilter filter = new ExpertFilter();
    	filter.verbose = true;
    	try {
			filter.process(query);
		} catch (QueryException e) {
			e.printStackTrace();
		}
    	System.out.println(filter.getRequestMap());
    	
    }


}
