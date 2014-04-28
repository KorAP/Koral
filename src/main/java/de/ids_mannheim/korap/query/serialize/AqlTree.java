package de.ids_mannheim.korap.query.serialize;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
 * Map representation of ANNIS QL syntax tree as returned by ANTLR
 * @author joachim
 *
 */
public class AqlTree extends Antlr4AbstractSyntaxTree {
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
	Parser parser;
	/**
	 * Keeps track of all visited nodes in a tree
	 */
	List<ParseTree> visited = new ArrayList<ParseTree>();
	/**
	 * Keeps track of active object.
	 */
	LinkedList<LinkedHashMap<String,Object>> objectStack = new LinkedList<LinkedHashMap<String,Object>>();
	/**
	 * Keeps track of explicitly (by #-var definition) or implicitly (number as reference) introduced entities (for later reference by #-operator)
	 */
	Map<String, Object> variableReferences = new LinkedHashMap<String, Object>(); 
	/**
	 * Counter for variable definitions.
	 */
	Integer variableCounter = 1;
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
	/**
	 * Keeps track of references to nodes that are operands of groups (e.g. tree relations). Those nodes appear on the top level of the parse tree
	 * but are to be integrated into the AqlTree at a later point (namely as operands of the respective group). Therefore, store references to these
	 * nodes here and exclude the operands from being written into the query map individually.   
	 */
	private LinkedList<String> operandOnlyNodeRefs = new LinkedList<String>();
	public static boolean verbose = false;
	
	/**
	 * 
	 * @param tree The syntax tree as returned by ANTLR
	 * @param parser The ANTLR parser instance that generated the parse tree
	 */
	public AqlTree(String query) {
//		prepareContext();
//		parseAnnisQuery(query);
//		super.parser = this.parser;
		requestMap.put("@context", "http://ids-mannheim.de/ns/KorAP/json-ld/v0.1/context.jsonld");
		try {
			process(query);
		} catch (QueryException e) {
			e.printStackTrace();
		}
		System.out.println(">>> "+requestMap.get("query")+" <<<");
	}

	@SuppressWarnings("unused")
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
		if (this.parser != null) {
			super.parser = this.parser;
		} else {
			throw new NullPointerException("Parser has not been instantiated!"); 
		}
		
		System.out.println("Processing Annis QL");
		if (verbose) System.out.println(tree.toStringTree(parser));
		processNode(tree);
	}
	
	@SuppressWarnings("unchecked")
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
		if (nodeCat.equals("start")) {
		}
		
		if (nodeCat.equals("exprTop")) {
			// has several andTopExpr as children delimited by OR (Disj normal form)
			if (node.getChildCount() > 1) {
				// TODO or-groups for every and
			}
		}
		
		if (nodeCat.equals("andTopExpr")) {
			// Before processing any child expr node, check if it has one or more "n_ary_linguistic_term" nodes.
			// Those nodes may use references to earlier established operand nodes.
			// Those operand nodes are not to be included into the query map individually but
			// naturally as operands of the relations/groups introduced by the 
			// n_ary_linguistic_term node. For that purpose, this section mines all used references
			// and stores them in a list for later reference.
			for (ParseTree exprNode : getChildrenWithCat(node,"expr")) {
				List<ParseTree> lingTermNodes = new ArrayList<ParseTree>();
				lingTermNodes.addAll(getChildrenWithCat(exprNode, "unary_linguistic_term"));
				lingTermNodes.addAll(getChildrenWithCat(exprNode, "n_ary_linguistic_term"));
				// Traverse refOrNode nodes under *ary_linguistic_term nodes and extract references
				for (ParseTree lingTermNode : lingTermNodes) {
					for (ParseTree refOrNode : getChildrenWithCat(lingTermNode, "refOrNode")) {
						operandOnlyNodeRefs.add(refOrNode.getChild(0).toStringTree(parser).substring(1));
					}
				}
			}
//			// TODO first check all 'expr' children for n_ary_linguistic_terms (see below)
//			if (node.getChildCount() > 1) {
//				LinkedHashMap<String, Object> andGroup = makeGroup("and");
//				objectStack.push(andGroup);
//				stackedObjects++;
//				putIntoSuperObject(andGroup,1);
//			}
		}
		
		// establish new variables or relations between vars
		if (nodeCat.equals("expr")) {
			// Check if expr node has one or more "n_ary_linguistic_term" nodes.
			// Those nodes may use references to earlier established operand nodes.
			// Those operand nodes are not to be included into the query map individually but
			// naturally as operands of the relations/groups introduced by the 
			// n_ary_linguistic_term node. For that purpose, this section mines all used references
			// and stores them in a list for later reference.
//			List<ParseTree> lingTermNodes = new ArrayList<ParseTree>();
//			lingTermNodes.addAll(getChildrenWithCat(node, "unary_linguistic_term"));
//			lingTermNodes.addAll(getChildrenWithCat(node, "n_ary_linguistic_term"));
//			// Traverse refOrNode nodes under *ary_linguistic_term nodes and extract references
//			for (ParseTree lingTermNode : lingTermNodes) {
//				for (ParseTree refOrNode : getChildrenWithCat(lingTermNode, "refOrNode")) {
//					operandOnlyNodeRefs.add(refOrNode.getChild(0).toStringTree(parser).substring(1));
//				}
//			}
		}
		
		if (nodeCat.equals("n_ary_linguistic_term") || nodeCat.equals("unary_linguistic_term")) {
			// get referenced operands
			// TODO generalize operator
			// TODO capture variableExprs
			LinkedHashMap<String, Object> group = makeGroup("treeRelation");
			group.put("treeRelation", getNodeCat(node.getChild(1).getChild(0)));
			List<Object> operands = (List<Object>) group.get("operands");
			for (ParseTree refOrNode : getChildrenWithCat(node, "refOrNode")) {
				String ref = refOrNode.getChild(0).toStringTree(parser).substring(1);
				operands.add(variableReferences.get(ref));
			}
			putIntoSuperObject(group);
		}
		
		if (nodeCat.equals("variableExpr")) {
			// simplex word or complex assignment (like qname = textSpec)?
			String firstChildNodeCat = getNodeCat(node.getChild(0));
			LinkedHashMap<String, Object> object = null;
			if (firstChildNodeCat.equals("node")) {
				object = makeSpan();
			} else if (firstChildNodeCat.equals("tok")) {
				object = makeToken();
				LinkedHashMap<String, Object> term = makeTerm();
				object.put("wrap", term);
			} else if (firstChildNodeCat.equals("qName")) {	// only (foundry/)?layer specified
				// TODO may also be token!
				object = makeSpan();
				object.putAll(parseQNameNode(node.getChild(0)));
			} else if (firstChildNodeCat.equals("textSpec")) {
				object = makeToken();
				LinkedHashMap<String, Object> term = makeTerm();
				object.put("wrap", term);
				term.putAll(parseTextSpec(node.getChild(0)));
			}
				
			if (node.getChildCount() == 3) {  			// (foundry/)?layer=key specification
				if (firstChildNodeCat.equals("tok")) {
					HashMap<String, Object> term = (HashMap<String, Object>) object.get("wrap");
					term.putAll(parseTextSpec(node.getChild(2)));
					term.put("match", parseMatchOperator(node.getChild(1)));
				} else {
					object.putAll(parseTextSpec(node.getChild(2)));
					object.put("match", parseMatchOperator(node.getChild(1)));
				}
			}
			
			if (object != null) {
				if (! operandOnlyNodeRefs.contains(variableCounter.toString())) putIntoSuperObject(object);
				variableReferences.put(variableCounter.toString(), object);
				variableCounter++;
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
			for (int i=0; i<objectsToPop.pop(); i++) {
				objectStack.pop();
			}
		}
		openNodeCats.pop();
	}





	private Map<? extends String, ? extends Object> parseTextSpec(ParseTree node) {
		HashMap<String, Object> term = new HashMap<String, Object>();
		if (hasChild(node, "regex")) {
			term.put("type", "type:regex");
			term.put("key", node.getChild(0).getChild(0).toStringTree(parser).replaceAll("/", ""));
		} else {
			term.put("key", node.getChild(1).toStringTree(parser));
		}
		term.put("match", "match:eq");
		return term;
	}

	/**
	 * Parses the match operator (= or !=)
	 * @param node
	 * @return
	 */
	private String parseMatchOperator(ParseTree node) {
		return node.toStringTree(parser).equals("=") ? "match:eq" : "match:ne";
	}
	
	
	/**
	 * Parses a textSpec node (which holds the 'key' field)
	 * @param node
	 * @return
	 */
	private LinkedHashMap<String, Object> parseVarKey(ParseTree node) {
		LinkedHashMap<String, Object> fields = new LinkedHashMap<String, Object>();
		if (node.getChildCount() == 2) {	// no content, empty quotes
			
		} else if (node.getChildCount() == 3) {
			fields.put("key", node.getChild(1).toStringTree(parser));
			if (node.getChild(0).toStringTree(parser).equals("/") &&		// slashes -> regex
					node.getChild(2).toStringTree(parser).equals("/")) {
				fields.put("type", "type:regex");
			}
		}
		return fields;
	}


	private LinkedHashMap<String, Object> parseQNameNode(ParseTree node) {
		System.err.println(getNodeCat(node));
		LinkedHashMap<String, Object> fields = new LinkedHashMap<String, Object>();
		if (node.getChildCount() == 1) { 									// only layer specification
			fields.put("layer", node.getChild(0).toStringTree(parser));
		} else if (node.getChildCount() == 3) {								// foundry / layer specification
			fields.put("foundry", node.getChild(0).toStringTree(parser));
			fields.put("layer", node.getChild(2).toStringTree(parser));
		}
		return fields;
	}

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
	
	private ParserRuleContext parseAnnisQuery (String p) throws QueryException {
		Lexer poliqarpLexer = new AqlLexer((CharStream)null);
	    ParserRuleContext tree = null;
	    // Like p. 111
	    try {

	      // Tokenize input data
	      ANTLRInputStream input = new ANTLRInputStream(p);
	      poliqarpLexer.setInputStream(input);
	      CommonTokenStream tokens = new CommonTokenStream(poliqarpLexer);
	      parser = new AqlParser(tokens);

	      // Don't throw out erroneous stuff
	      parser.setErrorHandler(new BailErrorStrategy());
	      parser.removeErrorListeners();

	      // Get starting rule from parser
	      Method startRule = AqlParser.class.getMethod("start"); 
	      tree = (ParserRuleContext) startRule.invoke(parser, (Object[])null);
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
			"node & pos=\"VVFIN\" & #2 > #1",
			"node & pos=\"VVFIN\" & #2 > #1",
			"pos=\"VVFIN\" > cas=\"Nom\" ",
			"pos=\"VVFIN\" >* cas=\"Nom\" ",
			"tiger/pos=\"NN\" >  node",
			"ref#node & pos=\"NN\" > #ref",
			"node & tree/pos=\"NN\"",
			"/node/",
			"\"Mann\"",
			"tok!=/Frau/",
			"node",
			"treetagger/pos=\"NN\"",
			"node & node & #2 > #1"
			};
//		AqlTree.verbose=true;
		for (String q : queries) {
			try {
				System.out.println(q);
//				System.out.println(AqlTree.parseAnnisQuery(q).toStringTree(AqlTree.parser));
				AqlTree at = new AqlTree(q);
				System.out.println(at.parseAnnisQuery(q).toStringTree(at.parser));
				System.out.println();
				
			} catch (NullPointerException | QueryException npe) {
				npe.printStackTrace();
			}
		}
	}

}