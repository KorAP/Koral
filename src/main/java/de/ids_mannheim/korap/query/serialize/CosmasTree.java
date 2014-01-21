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
import de.ids_mannheim.korap.query.serialize.util.CosmasCondition;
import de.ids_mannheim.korap.util.QueryException;

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

	private boolean hasSequentiableSiblings;

	/**
	 * Keeps track of operands lists that are to be serialised in an inverted
	 * order (e.g. the IN() operator) compared to their AST representation. 
	 */
	private LinkedList<ArrayList<Object>> invertedOperandsLists = new LinkedList<ArrayList<Object>>();
	
	private LinkedList<ArrayList<ArrayList<Object>>> distributedOperandsLists = new LinkedList<ArrayList<ArrayList<Object>>>();
	/**
	 * 
	 * @param tree The syntax tree as returned by ANTLR
	 * @param parser The ANTLR parser instance that generated the parse tree
	 * @throws QueryException 
	 */
	public CosmasTree(String query) throws QueryException {
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
	public void process(String query) throws QueryException {
		Tree tree = null;
		try {
			tree = parseCosmasQuery(query);
		} catch (RecognitionException e) {
			throw new QueryException("Your query could not be processed. Please make sure it is well-formed.");
		} catch (NullPointerException e) {
			throw new QueryException("Your query could not be processed. Please make sure it is well-formed.");
		}
		
		System.out.println("Processing Cosmas");
		prepareContext();
		processNode(tree);
	}
	
	private void processNode(Tree node) {
		
		// Top-down processing
		if (visited.contains(node)) return;
		else visited.add(node);
		
		
		String nodeCat = QueryUtils.getNodeCat(node);
		openNodeCats.push(nodeCat);
		
		stackedObjects = 0;
		
		if (debug) {
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
			Tree parent = node.getParent();
			if (parent.getChildCount()>1) {
				// if node is first child of parent...
				if (node == parent.getChild(0)) {
					hasSequentiableSiblings = false;
					for (int i=1; i<parent.getChildCount() ;i++) {
						if (sequentiableCats.contains(QueryUtils.getNodeCat(parent.getChild(i)))) {
							hasSequentiableSiblings = true;
						}
					}
					if (hasSequentiableSiblings) {
						// Step I: create sequence
						LinkedHashMap<String, Object> sequence = new LinkedHashMap<String, Object>();
						sequence.put("@type", "korap:sequence");
						sequence.put("operands", new ArrayList<Object>());
						// push sequence on object stack but don't increment stackedObjects counter since
						// we've got to wait until the parent node is processed - therefore, add the parent
						// to the sequencedNodes list and remove the sequence from the stack when the parent
						// has been processed
						objectStack.push(sequence);
						sequencedNodes.push(parent);
						// Step II: decide where to put sequence
						putIntoSuperObject(sequence, 1);
					}
				}
			}
		}
		
		// Nodes introducing tokens. Process all in the same manner, except for the fieldMap entry
		if (nodeCat.equals("OPWF") || nodeCat.equals("OPLEM") || nodeCat.equals("OPMORPH")) {
			
			//Step I: get info
			LinkedHashMap<String, Object> token = new LinkedHashMap<String, Object>();
			token.put("@type", "korap:token");
			objectStack.push(token);
			stackedObjects++;
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
			putIntoSuperObject(token, 1);
		}
		
		if (nodeCat.equals("OPELEM")) {
			// Step I: create element
			LinkedHashMap<String, Object> elem = new LinkedHashMap<String, Object>();
			elem.put("@type", "korap:elem");
			elem.put("@value", node.getChild(0).getChild(0).toStringTree().toLowerCase());
			//Step II: decide where to put
			putIntoSuperObject(elem);
		}		
		
		if (nodeCat.equals("OPLABEL")) {
			// Step I: create element
			LinkedHashMap<String, Object> elem = new LinkedHashMap<String, Object>();
			elem.put("@type", "korap:elem");
			elem.put("@value", node.getChild(0).toStringTree().replaceAll("<|>", ""));
			//Step II: decide where to put
			putIntoSuperObject(elem);
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
			// Step II: decide where to put
			putIntoSuperObject(disjunction, 1);
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
			if (openNodeCats.get(1).equals("OPALL")) proxGroup.put("match", "all");
			else if (openNodeCats.get(1).equals("OPNHIT")) proxGroup.put("match", "between");
			else proxGroup.put("match", "operands");
			ArrayList<Object> constraints = new ArrayList<Object>();
			String subtype = typ.getChild(0).toStringTree().equals("PROX") ? "incl" : "excl"; 
			proxGroup.put("@subtype", subtype);
			proxGroup.put("constraint", constraints);
			ArrayList<Object> operands = new ArrayList<Object>(); 
			proxGroup.put("operands", operands);
			
			// if only one dist_info, put directly into constraints
			if (dist_list.getChildCount()==1) {
				String direction = dist_list.getChild(0).getChild(0).getChild(0).toStringTree().toLowerCase();
				String min = dist_list.getChild(0).getChild(1).getChild(0).toStringTree();
				String max = dist_list.getChild(0).getChild(1).getChild(1).toStringTree();
				String meas = dist_list.getChild(0).getChild(2).getChild(0).toStringTree();
				if (min.equals("VAL0")) {
					min="0";
				}
				if (direction.equals("minus")) {
					direction = "plus";
					invertedOperandsLists.add(operands);
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
			putIntoSuperObject(proxGroup, 1);
		}
		
		// inlcusion or overlap
		if (nodeCat.equals("OPIN") || nodeCat.equals("OPOV")) {
			// Step I: create group
			LinkedHashMap<String, Object> shrinkgroup = new LinkedHashMap<String, Object>();
			shrinkgroup.put("@type", "korap:group");
			shrinkgroup.put("relation", "shrink");
			shrinkgroup.put("shrink", "1");
			
			ArrayList<Object> shrinkoperands = new ArrayList<Object>(); 
			LinkedHashMap<String, Object> posgroup = new LinkedHashMap<String, Object>();
			shrinkgroup.put("operands", shrinkoperands);
			shrinkoperands.add(posgroup);
			posgroup.put("@type", "korap:group");
			String relation = nodeCat.equals("OPIN") ? "position" : "overlap";
			posgroup.put("relation", relation);
			
			if (nodeCat.equals("OPIN")) {
				parseOPINOptions(node, posgroup);
			} else {
				parseOPOVOptions(node, posgroup);
			}
			
			
			ArrayList<Object> posoperands = new ArrayList<Object>();
			posgroup.put("operands", posoperands);
			objectStack.push(posgroup);
			// mark this an inverted list
			invertedOperandsLists.push(posoperands);
			stackedObjects++;
			
			// Step II: decide where to put
			putIntoSuperObject(shrinkgroup, 1);
		}
		
		
		// Wrap the first argument of an #IN operator in a class group
		if (nodeCat.equals("ARG1") && (openNodeCats.get(1).equals("OPIN") || openNodeCats.get(1).equals("OPOV"))) {
			// Step I: create group
			LinkedHashMap<String, Object> classGroup = new LinkedHashMap<String, Object>();
			classGroup.put("@type", "korap:group");
			classGroup.put("class", "1");
			classGroup.put("operands", new ArrayList<Object>());
			objectStack.push(classGroup);
			stackedObjects++;
			// Step II: decide where to put
			putIntoSuperObject(classGroup, 1);
		}
		
		
		if (nodeCat.equals("OPALL") || nodeCat.equals("OPNHIT")) {
//			proxGroupMatching = nodeCat.equals("OPALL") ? "all" : "exlcude";
		}
		
		if (nodeCat.equals("OPEND") || nodeCat.equals("OPBEG")) {
			// Step I: create group
			LinkedHashMap<String, Object> beggroup = new LinkedHashMap<String, Object>();
			beggroup.put("@type", "korap:group");
			beggroup.put("relation", "shrink");
			String reduction = nodeCat.equals("OPBEG") ? "first" : "last";
			beggroup.put("shrink", reduction);
			beggroup.put("operands", new ArrayList<Object>());
			objectStack.push(beggroup);
			stackedObjects++;
			
			// Step II: decide where to put
			putIntoSuperObject(beggroup, 1);
		}
		
		if (nodeCat.equals("OPBED")) {
			// Step I: create group
			int optsChild = node.getChildCount()-1;
			Tree conditions = node.getChild(optsChild).getChild(0);
			// Distinguish two cases. Normal case: query has just one condition, like #BED(XY, sa) ...
			if (conditions.getChildCount()==1) {
				LinkedHashMap<String, Object> posgroup = new LinkedHashMap<String, Object>();
				posgroup.put("@type", "korap:group");
				posgroup.put("relation", "position");
				CosmasCondition c = new CosmasCondition(conditions.getChild(0));
				posgroup.put("position", c.position);
				if (c.negated) posgroup.put("@subtype", "excl");
				ArrayList<Object> operands = new ArrayList<Object>();
				posgroup.put("operands", operands);
				LinkedHashMap<String, Object> bedElem = new LinkedHashMap<String, Object>();
				operands.add(bedElem);
				bedElem.put("@type", "korap:elem");
				bedElem.put("@value", c.elem);
				objectStack.push(posgroup);
				stackedObjects++;
				// Step II: decide where to put
				putIntoSuperObject(posgroup, 1);
			// ... or the query has several conditions specified, like #BED(XY, sa,-pa). In that case,
			//     create an 'and' group and embed the position groups in its operands
			} else {
				// node has several conditions (like 'sa, -pa')
				// -> create 'and' group and embed all position groups there
				LinkedHashMap<String, Object> conjunct = new LinkedHashMap<String, Object>();
				conjunct.put("@type", "korap:group");
				conjunct.put("relation", "and");
				ArrayList<Object> operands = new ArrayList<Object>();
				conjunct.put("operands", operands);
				ArrayList<ArrayList<Object>> distributedOperands = new ArrayList<ArrayList<Object>>();
				
				for (int i=0; i<conditions.getChildCount(); i++) {
					// for each condition, create a position group. problem: how to get argument into every operands list?
					// -> use distributedOperandsLists
					LinkedHashMap<String, Object> posGroup = new LinkedHashMap<String, Object>();
					operands.add(posGroup);
					
					CosmasCondition c = new CosmasCondition(conditions.getChild(i));
					posGroup.put("@type", "korap:group");
					posGroup.put("relation", "position");
					posGroup.put("position", c.position);
					if (c.negated) posGroup.put("@subtype", "excl");
					ArrayList<Object> posOperands = new ArrayList<Object>();
					distributedOperands.add(posOperands);
					posGroup.put("operands", posOperands);
					LinkedHashMap<String, Object> bedElem = new LinkedHashMap<String, Object>();
					posOperands.add(bedElem);
					bedElem.put("@type", "korap:elem");
					bedElem.put("@value", c.elem);
					
					
				}
				putIntoSuperObject(conjunct, 0);
				distributedOperandsLists.push(distributedOperands);
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
			Tree child = node.getChild(i);
			processNode(child);
		}
		
		/*
		 **************************************************************
		 * Stuff that happens after processing the children of a node *
		 **************************************************************
		 */
		
		// remove sequence from object stack if node is implicitly sequenced
		if (sequencedNodes.size()>0) {
			if (node == sequencedNodes.getFirst()) {
				objectStack.pop();
				sequencedNodes.pop();
			}
		}
		
		for (int i=0; i<objectsToPop.get(0); i++) {
			objectStack.pop();
		}
		objectsToPop.pop();
		
		if (nodeCat.equals("ARG2") && openNodeCats.get(1).equals("OPNOT")) {
			negate = false;
		}

		openNodeCats.pop();
		
	}

	
	private void parseOPINOptions(Tree node, LinkedHashMap<String, Object> posgroup) {
		Tree posnode = QueryUtils.getFirstChildWithCat(node, "POS");
		Tree rangenode = QueryUtils.getFirstChildWithCat(node, "RANGE");
		Tree exclnode = QueryUtils.getFirstChildWithCat(node, "EXCL");
		Tree groupnode = QueryUtils.getFirstChildWithCat(node, "GROUP");
		boolean negatePosition = false;
		
		String position = "";
		if (posnode != null) {
			String value = posnode.getChild(0).toStringTree();
			position = translateTextAreaArgument(value);
			if (value.equals("N")) {
				negatePosition = !negatePosition;
			}
		} else {
			position = "contains";
		}
		posgroup.put("position", position);
		position = openNodeCats.get(1).equals("OPIN") ? "contains" : "full";
		
		if (rangenode != null) {
			String range = rangenode.getChild(0).toStringTree();
			posgroup.put("range", range.toLowerCase());
		}
		
		if (exclnode != null) {
			if (exclnode.getChild(0).toStringTree().equals("YES")) {
				negatePosition = !negatePosition;
			}
		}
		
		if (negatePosition) {
			posgroup.put("@subtype", "excl");	
		}
		
		if (groupnode != null) {
			String grouping = groupnode.getChild(0).toStringTree().equals("MAX") ? "true" : "false";
			posgroup.put("grouping", grouping);
		}
	}
	
	private void parseOPOVOptions(Tree node, LinkedHashMap<String, Object> posgroup) {
		Tree posnode = QueryUtils.getFirstChildWithCat(node, "POS");
		Tree exclnode = QueryUtils.getFirstChildWithCat(node, "EXCL");
		Tree groupnode = QueryUtils.getFirstChildWithCat(node, "GROUP");
		
		String position = "";
		if (posnode != null) {
			String value = posnode.getChild(0).toStringTree();
			position = translateTextAreaArgument(value);
		} else {
			position = "any";
		}
		posgroup.put("position", position);
		position = openNodeCats.get(1).equals("OPIN") ? "contains" : "full";
		
		if (exclnode != null) {
			if (exclnode.getChild(0).toStringTree().equals("YES")) {
				posgroup.put("@subtype", "excl");
			}
		}
		if (groupnode != null) {
			String grouping = groupnode.getChild(0).toStringTree().equals("MAX") ? "true" : "false";
			posgroup.put("grouping", grouping);
		}
		
	}

	/**
	 * Translates the text area specifications (position option arguments) to terms used in serealisation.
	 * For the allowed argument types and their values for OPIN and OPOV, see
	 * http://www.ids-mannheim.de/cosmas2/win-app/hilfe/suchanfrage/eingabe-grafisch/syntax/ARGUMENT_I.html or
	 * http://www.ids-mannheim.de/cosmas2/win-app/hilfe/suchanfrage/eingabe-grafisch/syntax/ARGUMENT_O.html, respectively.
	 * @param argument
	 * @return
	 */
	private String translateTextAreaArgument(String argument) {
		String position = "";
		switch (argument) {
		case "L":
			position = "startswith";
			break;
		case "R":
			position = "endswith";
			break;
		case "F":
			position = "leftrightmatch";
			break;
		case "FE":
			position = "ident";
			break;
		case "FI":
			position = "leftrightmatch-noident";
			break;
		case "N": // for OPIN only - exclusion constraint formulated in parseOPINOptions 
			position = "leftrightmatch";
			break;
		case "X": // for OPOV only
			position = "residual";
			break;
	}
		return position;
	}
	
	@SuppressWarnings("unchecked")
	private void putIntoSuperObject(LinkedHashMap<String, Object> object, int objStackPosition) {
		if (distributedOperandsLists.size()>0) {
			ArrayList<ArrayList<Object>> distributedOperands = distributedOperandsLists.pop();
			for (ArrayList<Object> operands : distributedOperands) {
				operands.add(object);
			}
		} else if (objectStack.size()>objStackPosition) {
			ArrayList<Object> topObjectOperands = (ArrayList<Object>) objectStack.get(objStackPosition).get("operands");
			if (!invertedOperandsLists.contains(topObjectOperands)) {
				topObjectOperands.add(object);
			} else {
				topObjectOperands.add(0, object);
			}
			
		} else {
			requestMap.put("query", object);
		}
	}
	
	private void putIntoSuperObject(LinkedHashMap<String, Object> object) {
		putIntoSuperObject(object, 0);
	}
	

	private static Tree parseCosmasQuery(String p) throws RecognitionException {
		  Tree tree = null;
		  ANTLRStringStream	ss = new ANTLRStringStream(p);
		  c2psLexer	lex = new c2psLexer(ss);
		  org.antlr.runtime.CommonTokenStream tokens = new org.antlr.runtime.CommonTokenStream(lex);  //v3
		  cosmasParser = new c2psParser(tokens);
		  c2psParser.c2ps_query_return c2Return = cosmasParser.c2ps_query();  // statt t().
		  // AST Tree anzeigen:
		  tree = (Tree)c2Return.getTree();
		  
		  String treestring = tree.toStringTree();
		  if (treestring.contains("<mismatched token") || treestring.contains("<error") || treestring.contains("<unexpected")) {
			  throw new RecognitionException();
		  } 
		  return tree;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		 * For debugging
		 */
		String[] queries = new String[] {
				/* COSMAS 2 */
				};
		CosmasTree.debug=true;
		for (String q : queries) {
			try {
				System.out.println(q);
				try {
					System.out.println(parseCosmasQuery(q).toStringTree());
					@SuppressWarnings("unused")
					CosmasTree act = new CosmasTree(q);
				} catch (RecognitionException e) {
					e.printStackTrace();
				} catch (QueryException e) {
					e.printStackTrace();
				}
				System.out.println();
				
			} catch (NullPointerException npe) {
				npe.printStackTrace();
				System.out.println("null\n");
			}
		}
	}
}