package de.ids_mannheim.korap.query.serialize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ids_mannheim.korap.query.cosmas2.c2psLexer;
import de.ids_mannheim.korap.query.cosmas2.c2psParser;
import de.ids_mannheim.korap.query.serialize.util.CosmasCondition;
import de.ids_mannheim.korap.util.QueryException;

/**
 * Map representation of CosmasII syntax tree as returned by ANTLR
 * @author joachim
 *
 */
public class CosmasTree extends Antlr3AbstractSyntaxTree {
	
	private static Logger log = LoggerFactory.getLogger(CosmasTree.class);
	
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
	private final List<String> sequentiableCats = Arrays.asList(new String[] {"OPWF", "OPLEM", "OPMORPH", "OPBEG", "OPEND", "OPIN", "OPBED"});
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
		System.out.println("\n"+requestMap.get("query"));
		log.info(">>> " + requestMap.get("query") + " <<<");
	}
	
	@Override
	public Map<String, Object> getRequestMap() {
		return this.requestMap;
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
		requestMap.put("@context", "http://ids-mannheim.de/ns/KorAP/json-ld/v0.1/context.jsonld");
//		prepareContext(requestMap);
		processNode(tree);
	}
	
	private void processNode(Tree node) {
		
		// Top-down processing
		if (visited.contains(node)) return;
		else visited.add(node);
		
		
		String nodeCat = getNodeCat(node);
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
						if (sequentiableCats.contains(getNodeCat(parent.getChild(i)))) {
							hasSequentiableSiblings = true;
							continue;
						}
					}
					if (hasSequentiableSiblings) {
						// Step I: create sequence
						LinkedHashMap<String, Object> sequence = new LinkedHashMap<String, Object>();
						sequence.put("@type", "korap:group");
						sequence.put("operation", "operation:sequence");
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
		if (nodeCat.equals("OPWF") || nodeCat.equals("OPLEM")) {
			
			//Step I: get info
			LinkedHashMap<String, Object> token = new LinkedHashMap<String, Object>();
			token.put("@type", "korap:token");
			objectStack.push(token);
			stackedObjects++;
			LinkedHashMap<String, Object> fieldMap = new LinkedHashMap<String, Object>();
			token.put("wrap", fieldMap);
			
			fieldMap.put("@type", "korap:term");			
			// make category-specific fieldMap entry
			String attr = nodeCat.equals("OPWF") ? "orth" : "lemma";
			String value = node.getChild(0).toStringTree().replaceAll("\"", "");
			if (value.startsWith("$")) {
				value = value.substring(1);
				fieldMap.put("caseInsensitive", true);
			}
			fieldMap.put("key", value);
			fieldMap.put("layer", attr);
			
			// negate field (see above)
			if (negate) {
				fieldMap.put("match", "match:ne");
			} else {
				fieldMap.put("match", "match:eq");
			}
			//Step II: decide where to put
			if (! hasChild(node, "TPOS")) {
				putIntoSuperObject(token, 1);
			} else {
				
			}
			
		}
		
		if (nodeCat.equals("OPMORPH")) {
			//Step I: get info
			LinkedHashMap<String, Object> token = new LinkedHashMap<String, Object>();
			token.put("@type", "korap:token");
			LinkedHashMap<String, Object> fieldMap = new LinkedHashMap<String, Object>();
			token.put("wrap", fieldMap);
			
			fieldMap.put("@type", "korap:term");
//			fieldMap.put("key", "morph:"+node.getChild(0).toString().replace(" ", "_"));
			String[] morphValues = node.getChild(0).toString().split(" ");
			String pos = morphValues[0];
			
			fieldMap.put("key", pos);
			fieldMap.put("layer", "pos");
			// make category-specific fieldMap entry
			// negate field (see above)
			if (negate) {
				fieldMap.put("match", "match:ne");
			} else {
				fieldMap.put("match", "match:eq");
			}
//			List<String> morphValues = parseMorph(node.getChild(0).toStringTree());
//			System.err.println(morphValues);
//			if (morphValues.size() == 1) {
//				LinkedHashMap<String, Object> fieldMap = new LinkedHashMap<String, Object>();
//				token.put("key", fieldMap);
//				
//				fieldMap.put("@type", "korap:term");
//				fieldMap.put("key", morphValues.get(0));
//				// make category-specific fieldMap entry
//				// negate field (see above)
//				if (negate) {
//					fieldMap.put("operation", "operation:"+ "!=");
//				} else {
//					fieldMap.put("operation", "operation:"+ "=");
//				}
//			} else {
//				LinkedHashMap<String, Object> conjGroup = new LinkedHashMap<String, Object>();
//				token.put("key", conjGroup);
//				ArrayList<Object> conjOperands = new ArrayList<Object>();
//				conjGroup.put("@type", "korap:group");
//				conjGroup.put("operation", "operation:"+ "and");
//				conjGroup.put("operands", conjOperands);
//				for (String value : morphValues) {
//					LinkedHashMap<String, Object> fieldMap = new LinkedHashMap<String, Object>();
//					token.put("key", fieldMap);
//					
//					fieldMap.put("@type", "korap:term");
//					fieldMap.put("key", value);
//					// make category-specific fieldMap entry
//					// negate field (see above)
//					if (negate) {
//						fieldMap.put("operation", "operation:"+ "!=");
//					} else {
//						fieldMap.put("operation", "operation:"+ "=");
//					}
//				}
//			}
			
			
			//Step II: decide where to put
			putIntoSuperObject(token, 0);
		}
		
		if (nodeCat.equals("OPELEM")) {
			// Step I: create element
			LinkedHashMap<String, Object> elem = new LinkedHashMap<String, Object>();
			elem.put("@type", "korap:span");
			elem.put("key", node.getChild(0).getChild(0).toStringTree().toLowerCase());
			//Step II: decide where to put
			putIntoSuperObject(elem);
		}		
		
		if (nodeCat.equals("OPLABEL")) {
			// Step I: create element
			LinkedHashMap<String, Object> elem = new LinkedHashMap<String, Object>();
			elem.put("@type", "korap:span");
			elem.put("key", node.getChild(0).toStringTree().replaceAll("<|>", ""));
			//Step II: decide where to put
			putIntoSuperObject(elem);
		}
		
		if (nodeCat.equals("OPAND") || nodeCat.equals("OPNOT")) {
			// Step I: create group
			LinkedHashMap<String, Object> distgroup = new LinkedHashMap<String, Object>();
			distgroup.put("@type", "korap:group");
			distgroup.put("operation", "operation:sequence");
			ArrayList<Object> distances = new ArrayList<Object>(); 
			LinkedHashMap<String, Object> zerodistance = new LinkedHashMap<String, Object>();
			zerodistance.put("@type", "korap:distance");
			zerodistance.put("key", "t");
			zerodistance.put("min", 0);
			zerodistance.put("max", 0);
			if (nodeCat.equals("OPNOT")) zerodistance.put("exclude", true);
			distances.add(zerodistance);
			distgroup.put("distances", distances);
			distgroup.put("operands", new ArrayList<Object>());
			objectStack.push(distgroup);
			stackedObjects++;
			// Step II: decide where to put
			putIntoSuperObject(distgroup, 1);
		}
		
		if (nodeCat.equals("OPOR")) {
			// Step I: create group
			LinkedHashMap<String, Object> disjunction = new LinkedHashMap<String, Object>();
			disjunction.put("@type", "korap:group");
			disjunction.put("operation", "operation:or");
			disjunction.put("operands", new ArrayList<Object>());
			objectStack.push(disjunction);
			stackedObjects++;
			// Step II: decide where to put
			putIntoSuperObject(disjunction, 1);
		}
		
		if (nodeCat.equals("OPPROX")) {
			//TODO direction "both": wrap in "or" group with operands once flipped, once not
			// collect info
			Tree prox_opts = node.getChild(0);
			Tree typ = prox_opts.getChild(0);
			Tree dist_list = prox_opts.getChild(1);
			// Step I: create group
			LinkedHashMap<String, Object> proxSequence = new LinkedHashMap<String, Object>();
			proxSequence.put("@type", "korap:group");
			proxSequence.put("operation", "operation:"+ "sequence");
			objectStack.push(proxSequence);
			stackedObjects++;
			ArrayList<Object> constraints = new ArrayList<Object>();
			boolean exclusion = ! typ.getChild(0).toStringTree().equals("PROX"); 
			
			boolean inOrder = false;
			proxSequence.put("inOrder", inOrder);
			proxSequence.put("distances", constraints);
			
			ArrayList<Object> operands = new ArrayList<Object>(); 
			proxSequence.put("operands", operands);
		
			// possibly several distance constraints
			for (int i=0; i<dist_list.getChildCount(); i++) {
				String direction = dist_list.getChild(i).getChild(0).getChild(0).toStringTree().toLowerCase();
				String min = dist_list.getChild(i).getChild(1).getChild(0).toStringTree();
				String max = dist_list.getChild(i).getChild(1).getChild(1).toStringTree();
				String meas = dist_list.getChild(i).getChild(2).getChild(0).toStringTree();
				if (min.equals("VAL0")) {
					min="0";
				}
				LinkedHashMap<String, Object> distance = new LinkedHashMap<String, Object>();
				distance.put("@type", "korap:distance");
				distance.put("key", meas);
				distance.put("min", Integer.parseInt(min));
				distance.put("max", Integer.parseInt(max));
				if (exclusion) {
					distance.put("exclude", exclusion);
				}
				constraints.add(distance);
				if (direction.equals("plus")) {
					inOrder=true;
				} else if (direction.equals("minus")) {
					inOrder=true;
					invertedOperandsLists.add(operands);
				}
			}
			proxSequence.put("inOrder", inOrder);
			// Step II: decide where to put
			putIntoSuperObject(proxSequence, 1);
		}
		
		// inlcusion or overlap
		if (nodeCat.equals("OPIN") || nodeCat.equals("OPOV")) {
			// Step I: create group
			LinkedHashMap<String, Object> submatchgroup = new LinkedHashMap<String, Object>();
			submatchgroup.put("@type", "korap:group");
			submatchgroup.put("operation", "operation:"+ "submatch");
			ArrayList<Integer> classRef = new ArrayList<Integer>();
			classRef.add(1);
			submatchgroup.put("classRef", classRef);
			
			ArrayList<Object> submatchoperands = new ArrayList<Object>(); 
			LinkedHashMap<String, Object> posgroup = new LinkedHashMap<String, Object>();
			submatchgroup.put("operands", submatchoperands);
			submatchoperands.add(posgroup);
			posgroup.put("@type", "korap:group");
//			String relation = nodeCat.equals("OPIN") ? "position" : "overlaps";
			posgroup.put("operation", "operation:"+ "position");
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
			putIntoSuperObject(submatchgroup, 1);
		}
		
		
		// Wrap the first argument of an #IN operator in a class group
		if (nodeCat.equals("ARG1") && (openNodeCats.get(1).equals("OPIN") || openNodeCats.get(1).equals("OPOV") || openNodeCats.get(2).equals("OPNHIT"))) {
			// Step I: create group
			LinkedHashMap<String, Object> classGroup = new LinkedHashMap<String, Object>();
			classGroup.put("@type", "korap:group");
			classGroup.put("operation", "operation:"+ "class");
			classGroup.put("class", 1);
			classGroup.put("operands", new ArrayList<Object>());
			objectStack.push(classGroup);
			stackedObjects++;
			// Step II: decide where to put
			putIntoSuperObject(classGroup, 1);
		}
		
		// Wrap the 2nd argument of an #IN operator embedded in NHIT in a class group
		if (nodeCat.equals("ARG2") && openNodeCats.get(2).equals("OPNHIT")) {
			// Step I: create group
			LinkedHashMap<String, Object> classGroup = new LinkedHashMap<String, Object>();
			classGroup.put("@type", "korap:group");
			classGroup.put("operation", "operation:"+ "class");
			classGroup.put("class", 2);
			classGroup.put("operands", new ArrayList<Object>());
			objectStack.push(classGroup);
			stackedObjects++;
			// Step II: decide where to put
			putIntoSuperObject(classGroup, 1);
		}
		
		
		if (nodeCat.equals("OPNHIT")) {
			LinkedHashMap<String, Object> exclGroup = new LinkedHashMap<String, Object>();
			exclGroup.put("@type", "korap:group");
			exclGroup.put("operation", "operation:"+ "submatch");
			ArrayList<Integer> classRef = new ArrayList<Integer>();
			classRef.add(1);
			classRef.add(2);
			exclGroup.put("classRef", classRef);
			exclGroup.put("classRefOp", "classRefOp:"+"intersection");
			ArrayList<Object> operands = new ArrayList<Object>();
			exclGroup.put("operands", operands);
			objectStack.push(exclGroup);
			stackedObjects++;
			putIntoSuperObject(exclGroup, 1);
		}
		
		if (nodeCat.equals("OPEND") || nodeCat.equals("OPBEG")) {
			// Step I: create group
			LinkedHashMap<String, Object> beggroup = new LinkedHashMap<String, Object>();
			beggroup.put("@type", "korap:group");
			beggroup.put("operation", "operation:"+ "submatch");
			ArrayList<Integer> spanRef = new ArrayList<Integer>();
			if (nodeCat.equals("OPBEG")) {
				spanRef.add(0); spanRef.add(1);
			} else {
				spanRef.add(-1); spanRef.add(1);
			}
			beggroup.put("spanRef", spanRef);
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
			
			// create a containing group expressing the submatch constraint on the first argument
			LinkedHashMap<String, Object> submatchgroup = new LinkedHashMap<String, Object>();
			submatchgroup.put("@type", "korap:group");
			submatchgroup.put("operation", "operation:"+ "submatch");
			ArrayList<Integer> spanRef = new ArrayList<Integer>();
			spanRef.add(1);
			submatchgroup.put("classRef", spanRef);
			ArrayList<Object> submatchoperands = new ArrayList<Object>();
			submatchgroup.put("operands", submatchoperands);
			putIntoSuperObject(submatchgroup, 0);
			
			// Distinguish two cases. Normal case: query has just one condition, like #BED(X, sa) ...
			if (conditions.getChildCount()==1) {
				CosmasCondition c = new CosmasCondition(conditions.getChild(0));
				
				// create the group expressing the position constraint
				LinkedHashMap<String, Object> posgroup = new LinkedHashMap<String, Object>();
				posgroup.put("@type", "korap:group");
				posgroup.put("operation", "operation:"+ "position");
				
				posgroup.put("frame", "frame:"+c.position);
				if (c.negated) posgroup.put("exclude", true);
				ArrayList<Object> operands = new ArrayList<Object>();
				posgroup.put("operands", operands);

				// create span representing the element expressed in the condition
				LinkedHashMap<String, Object> bedElem = new LinkedHashMap<String, Object>();
				bedElem.put("@type", "korap:span");
				bedElem.put("key", c.elem);
				
				// create a class group containing the argument, in order to submatch the arg.
				LinkedHashMap<String, Object> classGroup = new LinkedHashMap<String, Object>();
				classGroup.put("@type", "korap:group");
				classGroup.put("operation", "operation:class");
				classGroup.put("class", 1);
				classGroup.put("operands", new ArrayList<Object>());
				objectStack.push(classGroup);
				stackedObjects++;
				operands.add(bedElem);
				operands.add(classGroup);
				// Step II: decide where to put
				submatchoperands.add(posgroup);
				
			// ... or the query has several conditions specified, like #BED(XY, sa,-pa). In that case,
			//     create an 'and' group and embed the position groups in its operands
			} else {
				// node has several conditions (like 'sa, -pa')
				// -> create zero-distance sequence group and embed all position groups there
				LinkedHashMap<String, Object> conjunct = new LinkedHashMap<String, Object>();
				conjunct.put("@type", "korap:group");
				conjunct.put("operation", "operation:"+ "sequence");
				ArrayList<Object> distances = new ArrayList<Object>();
				conjunct.put("distances", distances);
				LinkedHashMap<String, Object> zerodistance = new LinkedHashMap<String, Object>();
				zerodistance.put("@type", "korap:distance");
				zerodistance.put("key", "w");
				zerodistance.put("min", 0);
				zerodistance.put("max", 0);
				distances.add(zerodistance);
				ArrayList<Object> operands = new ArrayList<Object>();
				conjunct.put("operands", operands);
				ArrayList<ArrayList<Object>> distributedOperands = new ArrayList<ArrayList<Object>>();
				
				for (int i=0; i<conditions.getChildCount(); i++) {
					// for each condition, create a position group containing a class group. problem: how to get argument into every operands list?
					// -> use distributedOperandsLists
					LinkedHashMap<String, Object> posGroup = new LinkedHashMap<String, Object>();
					operands.add(posGroup);
					
					// make position group
					CosmasCondition c = new CosmasCondition(conditions.getChild(i));
					posGroup.put("@type", "korap:group");
					posGroup.put("operation", "operation:"+ "position");
					posGroup.put("frame", "frame:"+c.position);
					if (c.negated) posGroup.put("exclude", "true");
					ArrayList<Object> posOperands = new ArrayList<Object>();
					
					// make class group 
					LinkedHashMap<String, Object> classGroup = new LinkedHashMap<String, Object>();
					classGroup.put("@type", "korap:group");
					classGroup.put("operation", "operation:class");
					classGroup.put("class", 1);
					ArrayList<Object> classOperands = new ArrayList<Object>(); 
					classGroup.put("operands", classOperands);
					distributedOperands.add(classOperands);  // subtree to be put into every class group -> distribute
					
					// put the span and the class group into the position group
					posGroup.put("operands", posOperands);
					LinkedHashMap<String, Object> span = new LinkedHashMap<String, Object>();
					posOperands.add(span);
					posOperands.add(classGroup);
					span.put("@type", "korap:span");
					span.put("key", c.elem);
				}
				submatchoperands.add(conjunct);
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
		Tree posnode = getFirstChildWithCat(node, "POS");
		Tree rangenode = getFirstChildWithCat(node, "RANGE");
		Tree exclnode = getFirstChildWithCat(node, "EXCL");
		Tree groupnode = getFirstChildWithCat(node, "GROUP");
		boolean negatePosition = false;
		
		String position = "";
		if (posnode != null) {
			String value = posnode.getChild(0).toStringTree();
			position = translateTextAreaArgument(value, "in");
			if (value.equals("N")) {
				negatePosition = !negatePosition;
			}
		} else {
			position = "contains";
		}
		posgroup.put("frame", "frame:"+position);
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
		System.err.println(negatePosition);
		if (negatePosition) {
			posgroup.put("exclude", "true");	
//			negate = !negate;
		}
		
		if (groupnode != null) {
			String grouping = groupnode.getChild(0).toStringTree().equals("max") ? "true" : "false";
			posgroup.put("grouping", grouping);
		}
	}
	
	private void parseOPOVOptions(Tree node, LinkedHashMap<String, Object> posgroup) {
		Tree posnode = getFirstChildWithCat(node, "POS");
		Tree exclnode = getFirstChildWithCat(node, "EXCL");
		Tree groupnode = getFirstChildWithCat(node, "GROUP");
		
		String position = "";
		if (posnode != null) {
			String value = posnode.getChild(0).toStringTree();
			position = "-"+translateTextAreaArgument(value, "ov");
		}
		posgroup.put("frame", "frame:"+"overlaps"+position);
		
		if (exclnode != null) {
			if (exclnode.getChild(0).toStringTree().equals("YES")) {
				posgroup.put("match", "match:"+"ne");
			}
		}
		if (groupnode != null) {
			String grouping = groupnode.getChild(0).toStringTree().equals("@max") ? "true" : "false";
			posgroup.put("grouping", grouping);
		}
		
	}

	/**
	 * Translates the text area specifications (position option arguments) to terms used in serialisation.
	 * For the allowed argument types and their values for OPIN and OPOV, see
	 * http://www.ids-mannheim.de/cosmas2/win-app/hilfe/suchanfrage/eingabe-grafisch/syntax/ARGUMENT_I.html or
	 * http://www.ids-mannheim.de/cosmas2/win-app/hilfe/suchanfrage/eingabe-grafisch/syntax/ARGUMENT_O.html, respectively.
	 * @param argument
	 * @param mode 
	 * @return
	 */
	private String translateTextAreaArgument(String argument, String mode) {
		String position = "";
		switch (argument) {
		case "L":
			position = mode.equals("in") ? "startswith" : "left";
			break;
		case "R":
			position = mode.equals("in") ? "endswith" : "right";
			break;
		case "F":
			position = "leftrightmatch";
			break;
		case "FE":
			position = "matches";
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
	

	private Tree parseCosmasQuery(String q) throws RecognitionException {
		  Pattern p = Pattern.compile("(\\w+):((\\+|-)?(sa|se|pa|pe|ta|te),?)+");
		  Matcher m = p.matcher(q);
		  
		  String rewrittenQuery = q;
		  while (m.find()) {
			  String match = m.group();
			  String conditionsString = match.split(":")[1];
			  Pattern conditionPattern = Pattern.compile("(\\+|-)?(sa|se|pa|pe|ta|te)");
			  Matcher conditionMatcher = conditionPattern.matcher(conditionsString);
			  String replacement = "#BED("+m.group(1)+" , ";
			  while (conditionMatcher.find()) {
				  replacement = replacement+conditionMatcher.group()+",";
			  }
			  replacement = replacement.substring(0, replacement.length()-1)+")"; //remove trailing comma and close parenthesis
			  System.out.println(replacement);
			  rewrittenQuery = rewrittenQuery.replace(match, replacement);
		  }
		  q = rewrittenQuery;
		  Tree tree = null;
		  ANTLRStringStream	ss = new ANTLRStringStream(q);
		  c2psLexer	lex = new c2psLexer(ss);
		  org.antlr.runtime.CommonTokenStream tokens = new org.antlr.runtime.CommonTokenStream(lex);  //v3
		  parser = new c2psParser(tokens);
		  c2psParser.c2ps_query_return c2Return = ((c2psParser) parser).c2ps_query();  // statt t().
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
//				"MORPH(V)",
//				"MORPH(V PRES)",
//				"wegen #IN(%, L) <s>",
//				"wegen #IN(%) <s>",
//				"(Mann oder Frau) #IN <s>",
//				"#BEG(der /w3:5 Mann) /+w10 kommt",
//				"&würde /w0 MORPH(V)",
//				"#NHIT(gehen /w1:10 voran)",
//				"#BED(der Mann , sa,-pa)",
//				"Mann /t0 Frau",
				"sagt der:sa Bundeskanzler",
//				"Der:sa,-pe,+te ",
				};
//		CosmasTree.debug=true;
		for (String q : queries) {
			try {
				System.out.println(q);
				try {
					@SuppressWarnings("unused")
					CosmasTree act = new CosmasTree(q);
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