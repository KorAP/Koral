package de.ids_mannheim.korap.query.serialize;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.tree.ParseTree;

import de.ids_mannheim.korap.query.annis.AqlLexer;
import de.ids_mannheim.korap.query.annis.AqlParser;
import de.ids_mannheim.korap.query.serialize.AbstractSyntaxTree;
import de.ids_mannheim.korap.util.QueryException;

/**
 * Map representation of Poliqarp syntax tree as returned by ANTLR
 * @author joachim
 *
 */
public class AqlTree extends AbstractSyntaxTree {
	
	/**
	 * Top-level map representing the whole request.
	 */
	LinkedHashMap<String,Object> requestMap = new LinkedHashMap<String,Object>();
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
	static Parser aqlParser;
	/**
	 * Keeps track of all visited nodes in a tree
	 */
	List<ParseTree> visited = new ArrayList<ParseTree>();

	/**
	 * Keeps track of active fields (like 'base=foo').
	 */
	LinkedList<ArrayList<Object>> fieldStack = new LinkedList<ArrayList<Object>>();
	/**
	 * Keeps track of active sequences.
	 */
	LinkedList<LinkedHashMap<String,Object>> sequenceStack = new LinkedList<LinkedHashMap<String,Object>>();
	/**
	 * Keeps track of active tokens.
	 */
	LinkedList<LinkedHashMap<String,Object>> tokenStack = new LinkedList<LinkedHashMap<String,Object>>();
	/**
	 * Keeps track of sequence/token/field groups.
	 */
	LinkedList<ArrayList<Object>> groupStack = new LinkedList<ArrayList<Object>>();
	/**
	 * Marks the currently active object (sequence/token/group...) in order to know where to add stuff like occurrence info etc.
	 */
	LinkedHashMap<String,Object> curObject = new LinkedHashMap<String,Object>();
	/**
	 * Marks the currently active token in order to know where to add flags (might already have been taken away from token stack).
	 */
	LinkedHashMap<String,Object> curToken = new LinkedHashMap<String,Object>();
	
	public static boolean verbose = false;
	
	/**
	 * 
	 * @param tree The syntax tree as returned by ANTLR
	 * @param parser The ANTLR parser instance that generated the parse tree
	 */
	public AqlTree(String query) {
//		prepareContext();
		requestMap.put("@context", "http://ids-mannheim.de/ns/KorAP/json-ld/v0.1/context.jsonld");
		try {
			process(query);
		} catch (QueryException e) {
			e.printStackTrace();
		}
		System.out.println(">>> "+requestMap.get("query")+" <<<");
	}

	private void prepareContext() {
		LinkedHashMap<String,Object> context = new LinkedHashMap<String,Object>();
		LinkedHashMap<String,Object> operands = new LinkedHashMap<String,Object>();
		LinkedHashMap<String,Object> relation = new LinkedHashMap<String,Object>();
		LinkedHashMap<String,Object> classMap = new LinkedHashMap<String,Object>();
		
		operands.put("@id", "korap:operands");
		operands.put("@container", "@list");
		
		relation.put("@id", "korap:relation");
		relation.put("@type", "korap:relation#types");
		
		classMap.put("@id", "korap:class");
		classMap.put("@type", "xsd:integer");
		
		context.put("korap", "http://korap.ids-mannheim.de/ns/query");
		context.put("@language", "de");
		context.put("operands", operands);
		context.put("relation", relation);
		context.put("class", classMap);
		context.put("query", "korap:query");
		context.put("filter", "korap:filter");
		context.put("meta", "korap:meta");
		
		requestMap.put("@context", context);		
	}

	@Override
	public Map<String, Object> getRequestMap() {
		return requestMap;
	}
	
	@Override
	public void process(String query) throws QueryException {
		ParseTree tree = parseAnnisQuery(query);
		System.out.println("Processing Annis QL");
		processNode(tree);
	}
	
	@SuppressWarnings("unchecked")
	private void processNode(ParseTree node) {
		// Top-down processing
		if (visited.contains(node)) return;
		else visited.add(node);
		
		String nodeCat = getNodeCat(node);
		openNodeCats.push(nodeCat);
		
		if (verbose) System.out.println(openNodeCats);

		/*
		 ****************************************************************
		 **************************************************************** 
		 * 			Processing individual node categories  				*
		 ****************************************************************
		 ****************************************************************
		 */
		if (nodeCat.equals("query")) {
			
		}

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
				
		// Stuff that happens when leaving a node (taking it off the stack)
		if (nodeCat.equals("cq_segments") || nodeCat.equals("sq_segments")) {
			// exclude whitespaces analysed as empty cq_segments
			if (node.getChildCount() > 0 && !getNodeCat(node.getChild(0)).equals(" ")) {
				sequenceStack.pop();
			}
		}
		
		if (nodeCat.equals("cq_disj_segments")) {
			groupStack.pop();
		}
		
		if (nodeCat.equals("cq_segment") || nodeCat.equals("sq_segment")){
			tokenStack.pop();
		}
		
		if (nodeCat.equals("conj_field")) {
			fieldStack.pop();
		}
		
		openNodeCats.pop();
		
	}

	/**
	 * Returns the category (or 'label') of the root of a ParseTree.
	 * @param node
	 * @return
	 */
	public String getNodeCat(ParseTree node) {
		String nodeCat = node.toStringTree(aqlParser);
		Pattern p = Pattern.compile("\\((.*?)\\s"); // from opening parenthesis to 1st whitespace
		Matcher m = p.matcher(node.toStringTree(aqlParser));
		if (m.find()) {
			nodeCat = m.group(1);
		} 
		return nodeCat;
	}
	
	private static ParserRuleContext parseAnnisQuery (String p) throws QueryException {
		Lexer poliqarpLexer = new AqlLexer((CharStream)null);
	    ParserRuleContext tree = null;
	    // Like p. 111
	    try {

	      // Tokenize input data
	      ANTLRInputStream input = new ANTLRInputStream(p);
	      poliqarpLexer.setInputStream(input);
	      CommonTokenStream tokens = new CommonTokenStream(poliqarpLexer);
	      aqlParser = new AqlParser(tokens);

	      // Don't throw out erroneous stuff
	      aqlParser.setErrorHandler(new BailErrorStrategy());
	      aqlParser.removeErrorListeners();

	      // Get starting rule from parser
	      Method startRule = AqlParser.class.getMethod("start"); 
	      tree = (ParserRuleContext) startRule.invoke(aqlParser, (Object[])null);
	    }

	    // Some things went wrong ...
	    catch (Exception e) {
	      System.err.println( e.getMessage() );
	    }
	    
	    if (tree == null) {
	    	throw new QueryException("Could not parse query. Make sure it is correct ANNIS QL syntax.");
	    }

	    // Return the generated tree
	    return tree;
	  }
	
	public static void main(String[] args) {
		/*
		 * For testing
		 */
		String[] queries = new String[] {
			"node & node & #2 > #1",
			"#1 . #2 ",
			"#1 . #2 & meta::Genre=\"Sport\"",
			"A _i_ B",
			"A .* B",
			"A >* B",
			"#1 > [label=\"foo\"] #2",
			"pos=\"VVFIN\" & cas=\"Nom\" & #1 . #2",
			"A .* B ",
			"A .* B .* C",
			
			"#1 ->LABEL[lbl=\"foo\"] #2",
			"#1 ->LABEL[lbl=/foo/] #2",
			"#1 ->LABEL[foundry/layer=\"foo\"] #2",
			"#1 ->LABEL[foundry/layer=\"foo\"] #2",
			"/[Ss]tatisch/",
			"/boo/",
			"//",
			"lalala"
			};
//		AqlTree.verbose=true;
		for (String q : queries) {
			try {
				System.out.println(q);
				System.out.println(AqlTree.parseAnnisQuery(q).toStringTree(AqlTree.aqlParser));
				@SuppressWarnings("unused")
				AqlTree at = new AqlTree(q);
//				System.out.println(AqlTree.parseAnnisQuery(q).toStringTree(AqlTree.aqlParser));
				System.out.println();
				
			} catch (NullPointerException | QueryException npe) {
				npe.printStackTrace();
			}
		}
	}

}