package de.ids_mannheim.korap.query.serialize;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import de.ids_mannheim.korap.query.annis.AqlLexer;
import de.ids_mannheim.korap.query.annis.AqlParser;
import de.ids_mannheim.korap.util.QueryException;

/**
 * Map representation of syntax tree as returned by ANTLR
 * @author bingel
 *
 */
public class TreeTemplate extends Antlr4AbstractSyntaxTree {
	/**
	 * Keeps track of open node categories
	 */
	LinkedList<String> openNodeCats = new LinkedList<String>();
	/**
	 * Flag that indicates whether token fields or meta fields are currently being processed
	 */
	boolean inMeta = false;
	/**
	 * Parser object deriving the ANTLR parse tree.
	 */
	static Parser qlParser;
	/**
	 * Keeps track of all visited nodes in a tree
	 */
	List<ParseTree> visited = new ArrayList<ParseTree>();
	/**
	 * Keeps track of active object.
	 */
	LinkedList<LinkedHashMap<String,Object>> objectStack = new LinkedList<LinkedHashMap<String,Object>>();
	/**
	 * Marks the currently active token in order to know where to add flags (might already have been taken away from token stack).
	 */
	LinkedHashMap<String,Object> curToken = new LinkedHashMap<String,Object>();

	private LinkedList<ArrayList<ArrayList<Object>>> distributedOperandsLists = new LinkedList<ArrayList<ArrayList<Object>>>();
	
	/**
	 * Keeps track of how many objects there are to pop after every recursion of {@link #processNode(ParseTree)}
	 */
	LinkedList<Integer> objectsToPop = new LinkedList<Integer>();
	Integer stackedObjects = 0;
	public static boolean verbose = false;
	
	/**
	 * 
	 * @param tree The syntax tree as returned by ANTLR
	 * @param parser The ANTLR parser instance that generated the parse tree
	 */
	public TreeTemplate(String query) {
		try {
			process(query);
		} catch (QueryException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void process(String query) throws QueryException {
		ParseTree tree = parseQuery(query);
		System.out.println("Processing Annis QL");
		processNode(tree);
	}
	
	private void processNode(ParseTree node) {
		// Top-down processing
		if (visited.contains(node)) return;
		else visited.add(node);
		
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
		
		
		for (int i=0; i<objectsToPop.pop(); i++) {
			objectStack.pop();
		}
		

		openNodeCats.pop();
		
	}

	@SuppressWarnings("unused")
	private void putIntoSuperObject(LinkedHashMap<String, Object> object) {
		putIntoSuperObject(object, 0);
	}
	
	@SuppressWarnings({ "unchecked" })
	private void putIntoSuperObject(LinkedHashMap<String, Object> object, int objStackPosition) {
		if (distributedOperandsLists.size()>0) {
			ArrayList<ArrayList<Object>> distributedOperands = distributedOperandsLists.pop();
			for (ArrayList<Object> operands : distributedOperands) {
				operands.add(object);
			}
		} else if (objectStack.size()>objStackPosition) {
			ArrayList<Object> topObjectOperands = (ArrayList<Object>) objectStack.get(objStackPosition).get("operands");
			topObjectOperands.add(0, object);
			
		} else {
			requestMap.put("query", object);
		}
	}
	
	private static ParserRuleContext parseQuery (String q) throws QueryException {
		Lexer qlLexer = new AqlLexer((CharStream)null);
	    ParserRuleContext tree = null;
	    // Like p. 111
	    try {

	      // Tokenize input data
	      ANTLRInputStream input = new ANTLRInputStream(q);
	      qlLexer.setInputStream(input);
	      CommonTokenStream tokens = new CommonTokenStream(qlLexer);
	      qlParser = new AqlParser(tokens);

	      // Don't throw out erroneous stuff
	      qlParser.setErrorHandler(new BailErrorStrategy());
	      qlParser.removeErrorListeners();

	      // Get starting rule from parser
	      Method startRule = AqlParser.class.getMethod("start"); 
	      tree = (ParserRuleContext) startRule.invoke(qlParser, (Object[])null);
	    }

	    // Some things went wrong ...
	    catch (Exception e) {
	      System.err.println( e.getMessage() );
	    }
	    
	    if (tree == null) {
	    	throw new QueryException("Could not parse query. Make sure it is correct QL syntax.");
	    }

	    // Return the generated tree
	    return tree;
	  }
}