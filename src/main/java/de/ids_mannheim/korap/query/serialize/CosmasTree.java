package de.ids_mannheim.korap.query.serialize;

import java.util.ArrayList;
import java.util.Arrays;
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
	 * A list of node categories that can be sequenced (i.e. which can be in a sequence with any number of other nodes in this list)
	 */
	private final List<String> sequentiableCats = Arrays.asList(new String[] {"OPWF", "OPLEM", "OPMORPH", "OPBEG", "OPEND", "OPIN"});
	/**
	 * Keeps track of sequenced nodes, i.e. nodes that implicitly govern  a sequence, as in (C2PQ (OPWF der) (OPWF Mann)).
	 * This is necessary in order to know when to take the sequence off the object stack, as the sequence is introduced by the
	 * first child but cannot be closed after this first child in order not to lose its siblings
	 */
	private LinkedList<Tree> sequencedNodes = new LinkedList<Tree>();
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

		
		// Check for potential implicit sequences as in (C2PQ (OPWF der) (OPWF Mann)). The sequence is introduced
		// by the first child if it (and its siblings) is sequentiable.
		if (sequentiableCats.contains(nodeCat)) {
			// for each node, check if parent has more than one child (-> could be implicit sequence)
			if (node.getParent().getChildCount()>1) {
				// if node is first child of parent...
				if (node == node.getParent().getChild(0)) {
					// Step I: create sequence
					LinkedHashMap<String, Object> sequence = new LinkedHashMap<String, Object>();
					sequence.put("@type", "korap:sequence");
					sequence.put("operands", new ArrayList<Object>());
					// push sequence on object stack but don't increment stackedObjects counter since
					// we've got to wait until the parent node is processed - therefore, add the parent
					// to the sequencedNodes list and remove the sequence from the stack when the parent
					// has been processed
					objectStack.push(sequence);
					sequencedNodes.push(node.getParent());
					// Step II: decide where to put sequence
					if (objectStack.size()>1) {
						ArrayList<Object> topObjectOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
						topObjectOperands.add(sequence);
					} else {
						requestMap.put("query", sequence);
					}
					
				}
			}
		}
		
		
		// C2QP is tree root
		if (nodeCat.equals("C2PQ")) {
//			if (node.getChildCount()>1) {
//				// Step I: create sequence
//				LinkedHashMap<String, Object> sequence = new LinkedHashMap<String, Object>();
//				sequence.put("@type", "korap:sequence");
//				sequence.put("operands", new ArrayList<Object>());
//				objectStack.push(sequence);
//				stackedObjects++;
//				// Step II: decide where to put sequence
//				requestMap.put("query", sequence);
//			}
		}
		
		// Nodes introducing tokens. Process all in the same manner, except for the fieldMap entry
		if (nodeCat.equals("OPWF") || nodeCat.equals("OPLEM") || nodeCat.equals("OPMORPH")) {
			
			//Step I: get info
			LinkedHashMap<String, Object> token = new LinkedHashMap<String, Object>();
			token.put("@type", "korap:token");
			objectStack.push(token);
			stackedObjects++;
			// check if this token comes after a distant operator (like "/+w3:4") and if yes,
			// insert the empty tokenGroups before the current token
//			if (openNodeCats.get(1).equals("ARG2")) {
//				if (openNodeCats.get(2).equals("OPPROX") && !distantTokensStack.isEmpty()) {
//					for (List<Object> distantTokenGroup : distantTokensStack.pop()) {
////						if (tokenGroupsStack.isEmpty()) {
////							queryMap.put("token"+tokenGroupCount+"_1", distantTokenGroup);
////						} else {
//						tokenStack.getFirst().put("token", distantTokenGroup);
////						}
////						tokenGroupCount++;
//					}
//				}  
				// check negation of token by preceding OPNOT
//				else if (openNodeCats.get(2).equals("OPNOT")) {
//					negate = true;
//				}
//			}
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
		
//		if (nodeCat.equals("ARG1") || nodeCat.equals("ARG2")) {
//			if (node.getChildCount()>1) {
//				// Step I: create sequence
//				LinkedHashMap<String, Object> sequence = new LinkedHashMap<String, Object>();
//				sequence.put("@type", "korap:sequence");
//				sequence.put("operands", new ArrayList<Object>());
//				objectStack.push(sequence);
//				stackedObjects++;
//				// Step II: decide where to put sequence
//				if (objectStack.size()>1) {
//					ArrayList<Object> topObjectOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
//					topObjectOperands.add(sequence);
//				} else {
//					requestMap.put("query", sequence);
//				}
//			}
//		}
		
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
			
			// Step II: decide where to put
			if (objectStack.size()>1) {
				ArrayList<Object> topObjectOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
				topObjectOperands.add(disjunction);
			} else {
				requestMap.put("query", disjunction);
			}
		}
		
		if (nodeCat.equals("OPPROX")) {
			// collect info
			Tree prox_opts = node.getChild(0);
			Tree typ = prox_opts.getChild(0);
			Tree dist_list = prox_opts.getChild(1);
			// Step I: create group
			LinkedHashMap<String, Object> proxGroup = new LinkedHashMap<String, Object>();
			proxGroup.put("@type", "korap:group");
			proxGroup.put("relation", "distance");
			objectStack.push(proxGroup);
			stackedObjects++;
			ArrayList<Object> constraints = new ArrayList<Object>();
			String subtype = typ.getChild(0).toStringTree().equals("PROX") ? "incl" : "excl"; 
			proxGroup.put("@subtype", subtype);
			proxGroup.put("constraint", constraints);
			proxGroup.put("operands", new ArrayList<Object>());
			
			// if only one dist_info, put directly into constraints
			if (dist_list.getChildCount()==1) {
				String direction = dist_list.getChild(0).getChild(0).getChild(0).toStringTree().toLowerCase();
				String min = dist_list.getChild(0).getChild(1).getChild(0).toStringTree();
				String max = dist_list.getChild(0).getChild(1).getChild(1).toStringTree();
				String meas = dist_list.getChild(0).getChild(2).getChild(0).toStringTree();
				if (min.equals("VAL0")) {
					min=max;
				}
				LinkedHashMap<String, Object> distance = new LinkedHashMap<String, Object>();
				distance.put("@type", "korap:distance");
				distance.put("measure", meas);
				distance.put("direction", direction);
				distance.put("min", min);
				distance.put("max", max);
				constraints.add(distance);
				
			}
			// otherwise, create group and add info there
			else {
				LinkedHashMap<String, Object> distanceGroup = new LinkedHashMap<String, Object>();
				ArrayList<Object> groupOperands = new ArrayList<Object>();
				distanceGroup.put("@type", "korap:group");
				distanceGroup.put("relation", "and");
				distanceGroup.put("operands", groupOperands);
				constraints.add(distanceGroup);
				for (int i=0; i<dist_list.getChildCount(); i++) {
					String direction = dist_list.getChild(i).getChild(0).getChild(0).toStringTree().toLowerCase();
					String min = dist_list.getChild(i).getChild(1).getChild(0).toStringTree();
					String max = dist_list.getChild(i).getChild(1).getChild(1).toStringTree();
					String meas = dist_list.getChild(i).getChild(2).getChild(0).toStringTree();
					if (min.equals("VAL0")) {
						min=max;
					}
					LinkedHashMap<String, Object> distance = new LinkedHashMap<String, Object>();
					distance.put("@type", "korap:distance");
					distance.put("measure", meas);
					distance.put("direction", direction);
					distance.put("min", min);
					distance.put("max", max);
					groupOperands.add(distance);
				}
			}
			
			
			// Step II: decide where to put
			if (objectStack.size()>1) {
				ArrayList<Object> topObjectOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
				topObjectOperands.add(proxGroup);
			} else {
				requestMap.put("query", proxGroup);
			}
		}
		
		// inlcusion or overlap
		if (nodeCat.equals("OPIN") || nodeCat.equals("OPOV")) {
			// Step I: create group
			LinkedHashMap<String, Object> ingroup = new LinkedHashMap<String, Object>();
			ingroup.put("@type", "korap:group");
			String combination = nodeCat.equals("OPIN") ? "include" : "overlap";
			ingroup.put("relation", combination);
			// add optional position info, if present
			if (QueryUtils.getNodeCat(node.getChild(0)).equals("POS")) {
				ingroup.put("position", node.getChild(0).getChild(0).toStringTree());
			}
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
		
		if (nodeCat.equals("OPALL") || nodeCat.equals("OPNHIT")) {
			// Step I: create group
			LinkedHashMap<String, Object> allgroup = new LinkedHashMap<String, Object>();
			allgroup.put("@type", "korap:group");
			String scope = nodeCat.equals("OPALL") ? "all" : "nhit";
			allgroup.put("relation", scope);
			// add optional position info, if present
			if (QueryUtils.getNodeCat(node.getChild(0)).equals("POS")) {
				allgroup.put("position", node.getChild(0).getChild(0).toStringTree());
			}
			allgroup.put("operands", new ArrayList<Object>());
			objectStack.push(allgroup);
			stackedObjects++;
			
			// Step II: decide where to put
			if (objectStack.size()>1) {
				ArrayList<Object> topObjectOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
				topObjectOperands.add(allgroup);
			} else {
				requestMap.put("query", allgroup);
			}
		}
		
		if (nodeCat.equals("OPEND") || nodeCat.equals("OPBEG")) {
			// Step I: create group
			LinkedHashMap<String, Object> ingroup = new LinkedHashMap<String, Object>();
			ingroup.put("@type", "korap:group");
			ingroup.put("relation", "reduction");
			String reduction = nodeCat.equals("OPEND") ? "end" : "begin";
			ingroup.put("reduction", reduction);
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
		
		// remove sequence from object stack if node is implicitly sequenced
		if (sequencedNodes.size()>0) {
			if (node == sequencedNodes.getFirst()) {
				objectStack.pop();
				sequencedNodes.pop();
			}
		}
		
		if (nodeCat.equals("ARG2") && openNodeCats.get(1).equals("OPNOT")) {
			negate = false;
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
//				"der",
//				"der Mann",
//				"Sonne nicht (Mond Stern)",
//				"Sonne /+w1:4 Mond",
////				"wegen #IN(L) <s>"
				"#BEG(<s>) /5w,s0 #END(<s>)",
				"#RECHTS(ELEM(S))",
				"#END(ELEM(S))",
//				"der Mann",
//				"Mond oder Sterne",
//				"(Sonne scheint) oder Mond"
//				"Sonne oder Mond oder Sterne",
//				"Mann #OV (der Mann)",
//				"Mann #OV(L) der Mann"
//				"*tür",
//				"#BED(der, sa)",
//				"das %w3 Haus",
//				"das /w3 Haus"
				"#ALL(gehen /w1:10 voran)",
				"#NHIT(gehen /w1:10 voran)",
				"das /w1:2,s0 Haus",
				"das /w1:2 Haus und Hof",
				"nicht Frau"
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