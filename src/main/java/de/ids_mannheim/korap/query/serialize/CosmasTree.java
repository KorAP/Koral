package de.ids_mannheim.korap.query.serialize;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ids_mannheim.korap.query.cosmas2.c2psLexer;
import de.ids_mannheim.korap.query.cosmas2.c2psParser;
import de.ids_mannheim.korap.query.serialize.AbstractSyntaxTree;

/**
 * Map representation of CosmasII syntax tree as returned by ANTLR
 * @author joachim
 *
 */
public class CosmasTree extends AbstractSyntaxTree {
	
	Logger log = LoggerFactory.getLogger(CosmasTree.class);
	
	private static c2psParser cosmasParser;
	/*
	 * Following collections have the following functions:
	 * - the request is a map with two keys (meta/query):			{meta=[], query=[]}
	 * - the query is a list of token group maps: 					{meta=[], query=[tg1=[], tg2=[]]}
	 * - each token group is a list of tokens: 						{meta=[], query=[tg1=[t1_1, t1_2], tg2=[t2_1, t2_2, t2_3]]}
	 * - each token corresponds to a single 'fields' linked list	{meta=[], query=[tg1=[t1_1=[], t1_2=[]], ... ]}
	 * - each fields list contains a logical operator and 'field maps' defining attributes and values
	 * 																{meta=[], query=[tg1=[t1_1=[[disj, {base=foo}, {base=bar}]], t1_2=[]], ... ]}
	 */
	String query;
	LinkedHashMap<String,Object> requestMap = new LinkedHashMap<String,Object>();
	LinkedHashMap<String,Object> queryMap = new LinkedHashMap<String,Object>();
	LinkedHashMap<String,Object> tokenGroup = new LinkedHashMap<String,Object>();
	ArrayList<Object> fieldGroup = new ArrayList<Object>(); 
	LinkedHashMap<String,Object> fieldMap;
	ArrayList<List<Object>> distantTokens;
	/**
	 * Keeps track of active tokens.
	 */
	LinkedList<LinkedHashMap<String,Object>> tokenStack = new LinkedList<LinkedHashMap<String,Object>>();
	/**
	 * Marks the currently active token in order to know where to add flags (might already have been taken away from token stack).
	 */
	LinkedHashMap<String,Object> curToken = new LinkedHashMap<String,Object>();
	/**
	 * Keeps track of active object.
	 */
	LinkedList<LinkedHashMap<String,Object>> objectStack = new LinkedList<LinkedHashMap<String,Object>>();
	/**
	 * Makes it possible to store several distantTokenGroups
	 */
	LinkedList<ArrayList<List<Object>>> distantTokensStack = new LinkedList<ArrayList<List<Object>>>();
	/**
	 * Field for repetition query (Kleene + or * operations, or min/max queries: {2,4}
	 */
	String repetition = "";
	/**
	 * Keeps track of open node categories
	 */
	LinkedList<String> openNodeCats = new LinkedList<String>();
	/**
	 * Global control structure for fieldGroups, keeps track of open fieldGroups.
	 */
	LinkedList<ArrayList<Object>> openFieldGroups = new LinkedList<ArrayList<Object>>();
	/**
	 * Keeps track of how many objects there are to pop after every recursion of {@link #processNode(ParseTree)}
	 */
	LinkedList<Integer> objectsToPop = new LinkedList<Integer>();
	/**
	 * Flag that indicates whether token fields or meta fields are currently being processed
	 */
	boolean inMeta = false;
	boolean negate = false;
	
	Tree cosmasTree;
	
	LinkedHashMap<String,Object> treeMap = new LinkedHashMap<String,Object>();
	/**
	 * Keeps track of all visited nodes in a tree
	 */
	List<Tree> visited = new ArrayList<Tree>();

	Integer stackedObjects = 0;
	
	private static boolean debug = false;
	  
	
	/**
	 * 
	 * @param tree The syntax tree as returned by ANTLR
	 * @param parser The ANTLR parser instance that generated the parse tree
	 */
	public CosmasTree(String query) {
		this.query = query;
		process(query);
		System.out.println(requestMap.get("query"));
	}
	
	@Override
	public Map<String, Object> getRequestMap() {
		return this.requestMap;
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
	public void process(String query) {
		Tree tree = parseCosmasQuery(query);
		System.out.println("Processing Cosmas");
		prepareContext();
		processNode(tree);
	}
	
	@SuppressWarnings("unchecked")
	private void processNode(Tree node) {
		
		// Top-down processing
		if (visited.contains(node)) return;
		else visited.add(node);
		
		
		String nodeCat = QueryUtils.getNodeCat(node);
		openNodeCats.push(nodeCat);
		
		stackedObjects = 0;
		
		if (debug) {
//			System.out.println(distantTokensStack);
			System.err.println(" "+objectStack);
			System.out.println(openNodeCats);
		}
		
		
		/* ***************************************
		 * Processing individual node categories *
		 *****************************************/
		// C2QP is tree root
		if (nodeCat.equals("C2PQ")) {
			if (node.getChildCount()>1) {
				// Step I: create sequence
				LinkedHashMap<String, Object> sequence = new LinkedHashMap<String, Object>();
				sequence.put("@type", "korap:sequence");
				sequence.put("operands", new ArrayList<Object>());
				objectStack.push(sequence);
				stackedObjects++;
				// Step II: decide where to put sequence
				requestMap.put("query", sequence);
				
			}
		}
		
		// Nodes introducing tokens. Process all in the same manner, except for the fieldMap entry
		if (nodeCat.equals("OPWF") || nodeCat.equals("OPLEM") || nodeCat.equals("OPMORPH")) {
			
			//Step I: get info
			LinkedHashMap<String, Object> token = new LinkedHashMap<String, Object>();
			token.put("@type", "korap:token");
			tokenStack.push(token);
			objectStack.push(token);
			stackedObjects++;
			// check if this token comes after a distant operator (like "/+w3:4") and if yes,
			// insert the empty tokenGroups before the current token
			if (openNodeCats.get(1).equals("ARG2")) {
				if (openNodeCats.get(2).equals("OPPROX") && !distantTokensStack.isEmpty()) {
					for (List<Object> distantTokenGroup : distantTokensStack.pop()) {
//						if (tokenGroupsStack.isEmpty()) {
//							queryMap.put("token"+tokenGroupCount+"_1", distantTokenGroup);
//						} else {
						tokenStack.getFirst().put("token", distantTokenGroup);
//						}
//						tokenGroupCount++;
					}
				}  
				// check negation of token by preceding OPNOT
//				else if (openNodeCats.get(2).equals("OPNOT")) {
//					negate = true;
//				}
			}
			LinkedHashMap<String, Object> fieldMap = new LinkedHashMap<String, Object>();
			token.put("@value", fieldMap);
			
			fieldMap.put("@type", "korap:term");			
			// make category-specific fieldMap entry
			String value = "";
			if (nodeCat.equals("OPWF")) {
				value = "orth:"+node.getChild(0).toStringTree().replaceAll("\"", "");
			}
			if (nodeCat.equals("OPLEM")) {
				value = "base:"+node.getChild(0).toStringTree().replaceAll("\"", "");
			}
			if (nodeCat.equals("OPMORPH")) {
				value = "morph:"+node.toStringTree();
				//TODO decompose morphology query
			}
			fieldMap.put("@value", value);
			// negate field (see above)
			if (negate) {
				fieldMap.put("relation", "!=");
			} else {
				fieldMap.put("relation", "=");
			}
			
			//Step II: decide where to put
			if (objectStack.size()>1) {
				ArrayList<Object> topObjectOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
				topObjectOperands.add(token);
			} else {
				requestMap.put("query", token);
			}
		}
		
		if (nodeCat.equals("OPLABEL")) {
			// Step I: create element
			LinkedHashMap<String, Object> elem = new LinkedHashMap<String, Object>();
			elem.put("@type", "korap:element");
			elem.put("@value", node.getChild(0).toStringTree().replaceAll("<|>", ""));
			//Step II: decide where to put
			if (objectStack.size()>0) {
				ArrayList<Object> topObjectOperands = (ArrayList<Object>) objectStack.get(0).get("operands");
				topObjectOperands.add(elem);
			} else {
				requestMap.put("query", elem);
			}
		}
		
//		// negate every token that's under OPNOT > ARG2
//		if (nodeCat.equals("ARG2") && openNodeCats.get(1).equals("OPNOT")) {
//			negate = true;
//		}
		
		if (nodeCat.equals("ARG1") || nodeCat.equals("ARG2")) {
			if (node.getChildCount()>1) {
				// Step I: create sequence
				LinkedHashMap<String, Object> sequence = new LinkedHashMap<String, Object>();
				sequence.put("@type", "korap:sequence");
				sequence.put("operands", new ArrayList<Object>());
				objectStack.push(sequence);
				stackedObjects++;
				// Step II: decide where to put sequence
				if (objectStack.size()>1) {
					ArrayList<Object> topObjectOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
					topObjectOperands.add(sequence);
				} else {
					requestMap.put("query", sequence);
				}
			}
		}
		
		if (nodeCat.equals("OPOR") || nodeCat.equals("OPAND") || nodeCat.equals("OPNOT")) {
			// Step I: create group
			LinkedHashMap<String, Object> disjunction = new LinkedHashMap<String, Object>();
			disjunction.put("@type", "korap:group");
			String relation = "or";
			if (nodeCat.equals("OPAND")) relation = "and";
			if (nodeCat.equals("OPNOT")) relation = "not";
			disjunction.put("relation", relation);
			disjunction.put("operands", new ArrayList<Object>());
			objectStack.push(disjunction);
			stackedObjects++;
			if (tokenStack.isEmpty()) {
				queryMap.put("tokenGroup", tokenGroup);
			} else {
				tokenStack.getFirst().put("tokenGroup", tokenGroup);
			}
			tokenGroup.put("type", "disj");
			tokenStack.push(tokenGroup);
			
			// Step II: decide where to put
			if (objectStack.size()>1) {
				ArrayList<Object> topObjectOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
				topObjectOperands.add(disjunction);
			} else {
				requestMap.put("query", disjunction);
			}
		}
		
		if (nodeCat.equals("OPPROX")) {
			// Step I: create group
			LinkedHashMap<String, Object> proxGroup = new LinkedHashMap<String, Object>();
			proxGroup.put("@type", "korap:group");
			proxGroup.put("relation", "distance");

			// collect info
			Tree prox_opts = node.getChild(0);
			Tree typ = prox_opts.getChild(0);
			Tree dist_list = prox_opts.getChild(1);
			String direction = dist_list.getChild(0).getChild(0).getChild(0).toStringTree();
			String min = dist_list.getChild(0).getChild(1).getChild(0).toStringTree();
			String max = dist_list.getChild(0).getChild(1).getChild(1).toStringTree();
			String meas = dist_list.getChild(0).getChild(2).getChild(0).toStringTree();
			
			if (min.equals("VAL0")) {
				min=max;
			}
			
			proxGroup.put("@subtype", meas);
			proxGroup.put("min", min);
			proxGroup.put("max", max);
			proxGroup.put("operands", new ArrayList<Object>());
			objectStack.push(proxGroup);
			stackedObjects++;
			
			// Step II: decide where to put
			if (objectStack.size()>1) {
				ArrayList<Object> topObjectOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
				topObjectOperands.add(proxGroup);
			} else {
				requestMap.put("query", proxGroup);
			}
		}
		
		if (nodeCat.equals("OPIN")) {
			// Step I: create group
			LinkedHashMap<String, Object> ingroup = new LinkedHashMap<String, Object>();
			ingroup.put("@type", "korap:group");
			ingroup.put("relation", "in");
			ingroup.put("position", node.getChild(0).getChild(0).toStringTree());
			ingroup.put("operands", new ArrayList<Object>());
			objectStack.push(ingroup);
			stackedObjects++;
			
			// Step II: decide where to put
			if (objectStack.size()>1) {
				ArrayList<Object> topObjectOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
				topObjectOperands.add(ingroup);
			} else {
				requestMap.put("query", ingroup);
			}
		}
		
		objectsToPop.push(stackedObjects);
		
		// recursion until 'query' node (root of tree) is processed
		for (int i=0; i<node.getChildCount(); i++) {
			Tree child = node.getChild(i);
			processNode(child);
		}
		
		for (int i=0; i<objectsToPop.get(0); i++) {
			objectStack.pop();
		}
		objectsToPop.pop();
		
		if (nodeCat.equals("ARG2") && openNodeCats.get(1).equals("OPNOT")) {
			negate = false;
		}

		if (nodeCat.equals("OPAND") || nodeCat.equals("OPOR")) {
			tokenStack.pop();
//			tokenGroupCount--;
//			tokenCount=0;
		}
		
		openNodeCats.pop();
		
	}

	

	private static Tree parseCosmasQuery(String p) {
		  Tree tree = null;
		  ANTLRStringStream
			ss = new ANTLRStringStream(p);
		  c2psLexer
			lex = new c2psLexer(ss);
		  org.antlr.runtime.CommonTokenStream tokens =   //v3
	  		new org.antlr.runtime.CommonTokenStream(lex);
		  cosmasParser = new c2psParser(tokens);
		  c2psParser.c2ps_query_return
			c2Return = null;
		  try 
			{
			c2Return = cosmasParser.c2ps_query();  // statt t().
			}
		  catch (RecognitionException e) 
			{
			e.printStackTrace();
			}
		  // AST Tree anzeigen:
		  tree = (Tree)c2Return.getTree();
		  return tree;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		 * For testing
		 */
		String[] queries = new String[] {
				/* COSMAS 2 */
//				"&Mond",
//				"Mond Sterne",
//				"Mond*",
//				"Mond oder Sterne",
//				"(des oder eines) /+w2 (Bauern oder Bauers oder Bauerns)",
//				"(Sonne /+w2 Mond) /+w2:3 Sterne",
//				"Mond oder Sonne /w2 Sterne",
//				"MORPH(V PCP)",
//				"MORPH(V PCP) Baum" ,
//				"Sonne %w2 Mond",
//				"Sonne /w2 Mond",
//				"Sonne nicht (Mond Stern)",
//				"Sonne nicht (Mond oder Stern)",
//				"Sonne /+w1:4 Mond",
//				"(sonne und mond) oder sterne",
//				"(stern oder (sonne und mond)) und MORPH(V PCP)",
//				"(sonne und (stern oder mond)) /+w2 luna???",
//				"(Tag /+w2 $offenen) /+w1 Tür",
//				"heißt /+w2 \"und\" ,"
				"der",
				"der Mann",
				"Sonne nicht (Mond Stern)",
				"Sonne /+w1:4 Mond",
//				"wegen #IN(L) <s>"
				"#BEG(<s>) /5w,s0 #END(<s>)",
				
				};
		CosmasTree.debug=true;
		for (String q : queries) {
			try {
				System.out.println(q);
				System.out.println(parseCosmasQuery(q).toStringTree());
				CosmasTree act = new CosmasTree(q);
				System.out.println();
				
			} catch (NullPointerException npe) {
				npe.printStackTrace();
				System.out.println("null\n");
			}
		}
	}
}