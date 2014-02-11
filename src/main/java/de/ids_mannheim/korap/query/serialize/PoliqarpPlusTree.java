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

import de.ids_mannheim.korap.query.PoliqarpPlusLexer;
import de.ids_mannheim.korap.query.PoliqarpPlusParser;
import de.ids_mannheim.korap.query.serialize.AbstractSyntaxTree;
import de.ids_mannheim.korap.util.QueryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Map representation of Poliqarp syntax tree as returned by ANTLR
 * @author joachim
 *
 */
public class PoliqarpPlusTree extends AbstractSyntaxTree {

    Logger log = LoggerFactory.getLogger(PoliqarpPlusTree.class);
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
	 * Flag that indicates whether a cq_segment is to be ignored (e.g. when it is empty, is followed directly by only a spanclass and has no other children etc...). 
	 */
	boolean ignoreCq_segment = false;
	/**
	 *  Flag that indicates whether a cq_segments element is quantified by an occ element.
	 */
	boolean cqHasOccSibling = false;
	/**
	 *  Flag that indicates whether a cq_segments' children are quantified by an occ element.
	 */
	boolean cqHasOccChild = false;
	/**
	 * Flag for negation of complete field
	 */
	boolean negField = false;
	/**
	 * Flag that indicates whether subsequent element is to be aligned.
	 */
	boolean alignNext = false;
	/**
	 * Flag that indicates whether current element has been aligned.
	 */
	boolean isAligned = false;
	/**
	 * Indicates a sequence which has an align operator as its child. Needed for deciding
	 * when to close the align group object.
	 */
//	ParseTree alignedSequence = null;
	/**
	 * Parser object deriving the ANTLR parse tree.
	 */
	static Parser poliqarpParser;
	/**
	 * Keeps track of all visited nodes in a tree
	 */
	List<ParseTree> visited = new ArrayList<ParseTree>();

	/**
	 * Keeps track of active fields (like 'base=foo').
	 */
	LinkedList<ArrayList<Object>> fieldStack = new LinkedList<ArrayList<Object>>();
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
	 * Marks the object to which following occurrence information is to be added.
	 */
	LinkedHashMap<String,Object> curOccGroup = new LinkedHashMap<String,Object>();
	/**
	 * Keeps track of how many objects there are to pop after every recursion of {@link #processNode(ParseTree)}
	 */
	LinkedList<Integer> objectsToPop = new LinkedList<Integer>();
	/**
	 * Keeps track of how many objects there are to pop after every recursion of {@link #processNode(ParseTree)}
	 */
	LinkedList<Integer> tokensToPop = new LinkedList<Integer>();
	/**
	 * Keeps track of how many objects there are to pop after every recursion of {@link #processNode(ParseTree)}
	 */
	LinkedList<Integer> fieldsToPop = new LinkedList<Integer>();
	/**
	 * If true, print debug statements
	 */
	public static boolean debug = false;
	/**
	 * Index of the current child of its parent (needed for relating occ elements to their operands).
	 */
	int curChildIndex = 0;
	/**
	 * 
	 */
	Integer stackedObjects = 0;
	Integer stackedTokens= 0;
	Integer stackedFields = 0;
	
	
	/**
	 * Most centrally, this class maintains a set of nested maps and lists which represent the JSON tree, which is built by the JSON serialiser
	 * on basis of the {@link #requestMap} at the root of the tree. 
	 * <br/>
	 * The class further maintains a set of stacks which effectively keep track of which objects to embed in which containing objects. 
	 * 
	 * @param query The syntax tree as returned by ANTLR
	 * @throws QueryException 
	 */
	public PoliqarpPlusTree(String query) throws QueryException {
		try {
			process(query); 
		} catch (NullPointerException e) {
			if (query.contains(" ")) {
				System.err.println("Warning: It seems like your query contains illegal whitespace characters. Trying again with whitespaces removed...");
				query = query.replaceAll(" ", "");
				process(query);
			} else {
				try {
					throw new QueryException("Error handling query.");
				} catch (QueryException e1) {
					e1.printStackTrace();
					System.exit(1);
				}
			}
		}
		System.out.println(">>> "+requestMap.get("query")+" <<<");
        log.info(">>> " + requestMap.get("query") + " <<<");
	}

	@Override
	public Map<String, Object> getRequestMap() {
		return requestMap;
	}
	
	@Override
	public void process(String query) throws QueryException {
		ParseTree tree = null;
		try {
			tree = parsePoliqarpQuery(query);
		} catch (QueryException e) {
			try {
				tree = parsePoliqarpQuery(query.replaceAll(" ", ""));
			} catch (QueryException e1) {
				System.exit(1);
			}
		}
		System.out.println("Processing PoliqarpPlus");
		QueryUtils.prepareContext(requestMap);
		processNode(tree);
	}
	
	/**
	 * Recursively calls itself with the children of the currently active node, traversing the tree nodes in a top-down, depth-first fashion. 
	 * A list is maintained that contains all visited nodes 
	 * in case they have been directly addressed by its (grand-/grand-grand-/...) parent node, such that some processing time is saved, as these node will
	 * not be processed. This method is effectively a list of if-statements that are responsible for treating the different node types correctly and filling the
	 * respective maps/lists.
	 *
	 * @param node The currently processed node. The process(String query) method calls this method with the root.
	 * @throws QueryException 
	 */
	@SuppressWarnings("unchecked")
	private void processNode(ParseTree node) throws QueryException {
		// Top-down processing
		if (visited.contains(node)) return;
		else visited.add(node);
		
		if (alignNext) {
			alignNext=false;
			isAligned=true;
		}
		
		String nodeCat = QueryUtils.getNodeCat(node);
		openNodeCats.push(nodeCat);
		
		stackedObjects = 0;
		stackedTokens= 0;
		stackedFields = 0;
		
		if (debug) {
			System.err.println(" "+objectStack);
			System.err.println(" "+tokenStack);
			System.out.println(openNodeCats);
		}
		

		/*
		 ****************************************************************
		 **************************************************************** 
		 * 			Processing individual node categories  				*
		 ****************************************************************
		 ****************************************************************
		 */

		// cq_segments/sq_segments: token group
		if (nodeCat.equals("cq_segments") || nodeCat.equals("sq_segments")) {
			cqHasOccSibling = false;
			cqHasOccChild = false;
			// disregard empty segments in simple queries (parsed by ANTLR as empty cq_segments) 
			ignoreCq_segment = (node.getChildCount() == 1 && (node.getChild(0).toStringTree(poliqarpParser).equals(" ") || QueryUtils.getNodeCat(node.getChild(0)).equals("spanclass") || QueryUtils.getNodeCat(node.getChild(0)).equals("position")));
			// ignore this node if it only serves as an aligned sequence container
			if (node.getChildCount()>1) {
				if (QueryUtils.getNodeCat(node.getChild(1)).equals("cq_segments") && QueryUtils.hasChild(node.getChild(1), "alignment")) {
//				if (QueryUtils.getNodeCat(node.getChild(0)).equals("align")) {	
					ignoreCq_segment = true;
				}
			}
			if (!ignoreCq_segment) {
				LinkedHashMap<String,Object> sequence = new LinkedHashMap<String,Object>();
				// Step 0:  cq_segments has 'occ' child -> introduce group as super group to the sequence/token/group
				// this requires creating a group and inserting it at a suitable place
				if (node.getParent().getChildCount()>curChildIndex+2 && QueryUtils.getNodeCat(node.getParent().getChild(curChildIndex+2)).equals("occ")) {
					cqHasOccSibling = true;
					createOccGroup(node);
				}
				if (QueryUtils.getNodeCat(node.getChild(node.getChildCount()-1)).equals("occ")) {
					cqHasOccChild = true;
				}
				// Step I: decide type of element (one or more elements? -> token or sequence)
				// take into account a possible 'occ' child with accompanying parantheses, therefore 3 extra children
				int occExtraChildren = cqHasOccChild ? 3:0;
				if (node.getChildCount()>1 + occExtraChildren) {
					ParseTree emptySegments = QueryUtils.getFirstChildWithCat(node, "empty_segments");
					if (emptySegments != null && emptySegments != node.getChild(0)) {
						String[] minmax = parseEmptySegments(emptySegments);
						Integer min = Integer.parseInt(minmax[0]);
						Integer max = Integer.parseInt(minmax[1]);
						sequence.put("type", "group");
						sequence.put("operation", "sequence");
//						sequence.put("operation", "distance");
						sequence.put("inOrder", "true");
						ArrayList<Object> constraint = new ArrayList<Object>(); 
						sequence.put("distances", constraint);
						ArrayList<Object> sequenceOperands = new ArrayList<Object>();
						sequence.put("operands", sequenceOperands);
						objectStack.push(sequence);
						stackedObjects++;
						LinkedHashMap<String, Object> distMap = new LinkedHashMap<String, Object>();
						constraint.add(distMap);
						distMap.put("type", "distance");
						distMap.put("measure", "w");
						distMap.put("min", min);
						distMap.put("max", max);
					} else {
						sequence.put("type", "group");
						sequence.put("operation", "sequence");
						ArrayList<Object> sequenceOperands = new ArrayList<Object>();
						if (emptySegments != null) {
							String[] minmax = parseEmptySegments(emptySegments);
							Integer min = Integer.parseInt(minmax[0]);
							Integer max = Integer.parseInt(minmax[1]);
							sequence.put("offset-min", min);
							sequence.put("offset-max", max);
						}
						sequence.put("operands", sequenceOperands);
						objectStack.push(sequence);
						stackedObjects++;
					}
				} else {
					// if only child, make the sequence a mere token...
					// ... but only if it has a real token/element beneath it
					if (QueryUtils.getNodeCat(node.getChild(0)).equals("cq_segment")
						|| QueryUtils.getNodeCat(node.getChild(0)).equals("sq_segment")
						|| QueryUtils.getNodeCat(node.getChild(0)).equals("element")	) {
						sequence.put("type", "token");
						tokenStack.push(sequence);
						stackedTokens++;
						objectStack.push(sequence);
						stackedObjects++;
					// else, it's a group (with shrink()/spanclass/align... as child)
					} else {
						sequence.put("type", "group");
					}
				}
				// Step II: decide where to put this element 
				// check if this is an argument for a containing occurrence group (see step 0)
				if (cqHasOccSibling) {
					ArrayList<Object> topGroupOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
					topGroupOperands.add(sequence);
				// ...if not modified by occurrence, put into suitable super structure
				} else {
					if (openNodeCats.get(1).equals("query")) {
						// cq_segment is top query node
						if (node.getParent().getChildCount()==1) {
							// only child
							requestMap.put("query", sequence);
						} else {
							// not an only child, need to create containing sequence
							if (node.getParent().getChild(0).equals(node)) {
								// if first child, create containing sequence and embed there
								LinkedHashMap<String,Object> superSequence = new LinkedHashMap<String,Object>();
								superSequence.put("type", "group");
								superSequence.put("operation", "sequence");
								ArrayList<Object> operands = new ArrayList<Object>();
								superSequence.put("operands", operands);
								operands.add(sequence);
								requestMap.put("query", superSequence);
								objectStack.push(superSequence); // add at 2nd position to keep current cq_segment accessible
								stackedObjects++;
							} else {
								// if not first child, add to previously created parent sequence
								ArrayList<Object> topSequenceOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
								topSequenceOperands.add(sequence);
							}
						}
					} else if (!objectStack.isEmpty()){
						// embed in super sequence
						ArrayList<Object> topSequenceOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
						topSequenceOperands.add(sequence);
					}
				}
			}
		}
		
		// cq_segment
		if (nodeCat.equals("cq_segment")) {
			int onTopOfObjectStack = 0;
			// Step I: determine whether to create new token or get token from the stack (if added by cq_segments)
			LinkedHashMap<String, Object> token;
			if (tokenStack.isEmpty()) {
				token = new LinkedHashMap<String, Object>();
				tokenStack.push(token);
				stackedTokens++;
				// do this only if token is newly created, otherwise it'll be in objectStack twice
				objectStack.push(token);
				onTopOfObjectStack = 1;
				stackedObjects++;
			} else {
				// in case cq_segments has already added the token
				token = tokenStack.getFirst();
			}
			curToken = token;
			// Step II: start filling object and add to containing sequence
			token.put("type", "token");
			// add token to sequence only if it is not an only child (in that case, cq_segments has already added the info and is just waiting for the values from "field")
			// take into account a possible 'occ' child
			if (node.getParent().getChildCount()>1) {				
				if (node.getText().equals("[]")) {
//					LinkedHashMap<String, Object> sequence  = objectStack.get(onTopOfObjectStack);
//					String offsetStr = (String) sequence.get("offset");
//					if (offsetStr == null) {
//						sequence.put("offset", "1");
//					} else {
//						Integer offset = Integer.parseInt(offsetStr);
//						sequence.put("offset", offset+1);
//					}
//					
				} else {
					ArrayList<Object> topSequenceOperands = (ArrayList<Object>) objectStack.get(onTopOfObjectStack).get("operands");
					topSequenceOperands.add(token);
				}
			}
		}
		
		// cq_segment modified by occurrence
		if (nodeCat.equals("cq_seg_occ")) {
			LinkedHashMap<String,Object> group = new LinkedHashMap<String,Object>();
			curOccGroup = group;
			group.put("type", "group");
			group.put("operands", new ArrayList<Object>());
			objectStack.push(group);
			stackedObjects++;
			// add group to sequence only if it is not an only child (in that case, cq_segments has already added the info and is just waiting for the values from "field")
			// take into account a possible 'occ' child
			if (node.getParent().getChildCount()>1) {
				ArrayList<Object> topSequenceOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
				topSequenceOperands.add(group);
			} else {
				requestMap.put("query", group);
			}
		}

		// disjoint cq_segments, like ([base=foo][base=bar])|[base=foobar]
		if (nodeCat.equals("cq_disj_segments")) {
			LinkedHashMap<String,Object> disjunction = new LinkedHashMap<String,Object>();
			objectStack.push(disjunction);
			stackedObjects++;
			ArrayList<Object> disjOperands = new ArrayList<Object>();
			disjunction.put("type", "group");
			disjunction.put("operation", "or");
			disjunction.put("operands", disjOperands);
			// decide where to put the disjunction
			if (openNodeCats.get(1).equals("query")) {
				requestMap.put("query", disjunction);	
			} else if (openNodeCats.get(1).equals("cq_segments")) {
				ArrayList<Object> topSequenceOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
				topSequenceOperands.add(disjunction);
			}
		}
		
		// field element (outside meta)
		if (nodeCat.equals("field")) {
			LinkedHashMap<String,Object> fieldMap = new LinkedHashMap<String,Object>();
			// Step I: extract info
			String fieldName = "";
			ParseTree fieldNameNode = node.getChild(0);
			if (fieldNameNode.getChildCount() == 1) {
				fieldName = fieldNameNode.getChild(0).toStringTree(poliqarpParser);   //e.g. (field_name base) (field_op !=) (re_query "bar*")
			} else if (fieldNameNode.getChildCount() == 3) {
				// layer is indicated, merge layer and field name (0th and 2nd children, 1st is "/")
				String layer = fieldNameNode.getChild(0).toStringTree(poliqarpParser);
				if (layer.equals("base")) layer="lemma";
				String layeredFieldName = fieldNameNode.getChild(2).toStringTree(poliqarpParser);
				fieldName = layer+"/"+layeredFieldName;
			}
			
			String relation = node.getChild(1).getChild(0).toStringTree(poliqarpParser);
			if (negField) {
				if (relation.startsWith("!")) {
					relation = relation.substring(1);
				} else {
					relation = "!"+relation;
				}
			}
			if (relation.equals("=")) {
				relation="eq";
			}
			if (relation.equals("!=")) {
				relation="ne";
			}
			String value = "";
			ParseTree valNode = node.getChild(2);
			String valType = QueryUtils.getNodeCat(valNode);
			fieldMap.put("type", "term");
			if (valType.equals("simple_query")) {
				value = valNode.getChild(0).getChild(0).toStringTree(poliqarpParser);   //e.g. (simple_query (sq_segment foo))
			} else if (valType.equals("re_query")) {
				value = valNode.getChild(0).toStringTree(poliqarpParser); 				//e.g. (re_query "bar*")
				fieldMap.put("@subtype", "value#regex");
			}
			fieldMap.put("key", value);
			if (fieldName.contains("/")) {
				String[] splitted = fieldName.split("/");
				fieldMap.put("layer", splitted[1]);
				fieldMap.put("foundry", splitted[0]);
			} else {
				if (fieldName.equals("base")) fieldName = "lemma";
				fieldMap.put("layer", fieldName);
			}
			fieldMap.put("match", relation);
			// Step II: decide where to put the field map (as the only value of a token or the meta filter or as a part of a group in case of coordinated fields)
			if (fieldStack.isEmpty()) {
				if (!inMeta) {
					tokenStack.getFirst().put("key", fieldMap);
				} else {
					((HashMap<String, Object>) requestMap.get("meta")).put("key", fieldMap);
				}
			} else {
				fieldStack.getFirst().add(fieldMap);
			}
			visited.add(node.getChild(0));
			visited.add(node.getChild(1));
			visited.add(node.getChild(2));
		}
		
		if (nodeCat.equals("neg_field") || nodeCat.equals("neg_field_group")) {
			negField=!negField;
		}
		
		// conj_field serves for both conjunctions and disjunctions
		if (nodeCat.equals("conj_field")) {
			LinkedHashMap<String,Object> group = new LinkedHashMap<String,Object>(); 
			ArrayList<Object> groupOperands = new ArrayList<Object>();
			group.put("type", "group");
			group.put("operands", groupOperands);
			fieldStack.push(groupOperands);
			stackedFields++;
			// Step I: get operator (& or |)
			ParseTree operatorNode = node.getChild(1).getChild(0);
			String operator = QueryUtils.getNodeCat(operatorNode);
			String relation = operator.equals("&") ? "and" : "or";
			if (negField) {
				relation = relation.equals("or") ? "and": "or";
			}
			group.put("operation", relation);
			// Step II: decide where to put the group (directly under token or in top meta filter section or embed in super group)
			if (openNodeCats.get(1).equals("cq_segment")) {
				tokenStack.getFirst().put("key", group);
			} else if (openNodeCats.get(1).equals("meta_field_group")) {
				((HashMap<String, Object>) requestMap.get("meta")).put("key", group);
			} else if (openNodeCats.get(2).equals("conj_field")) {
				fieldStack.get(1).add(group);
			} else {
				tokenStack.getFirst().put("key", group);
			}
			// skip the operator
			visited.add(node.getChild(1));
		}
		
		
		if (nodeCat.equals("sq_segment")) {
			// Step I: determine whether to create new token or get token from the stack (if added by cq_segments)
			LinkedHashMap<String, Object> token;
			if (tokenStack.isEmpty()) {
				token = new LinkedHashMap<String, Object>();
				tokenStack.push(token);
				stackedTokens++;
			} else {
				// in case sq_segments has already added the token
				token = tokenStack.getFirst();
			}
			curToken = token;
			objectStack.push(token);
			stackedObjects++;
			// Step II: fill object (token values) and put into containing sequence
			if (node.getText().equals("[]")) {
				
			} else {
				token.put("type", "token");
				String word = node.getChild(0).toStringTree(poliqarpParser);
				LinkedHashMap<String,Object> tokenValues = new LinkedHashMap<String,Object>();
				token.put("key", tokenValues);
				tokenValues.put("type", "term");
				tokenValues.put("key", word);
				tokenValues.put("layer", "orth");
				tokenValues.put("match", "eq");
				// add token to sequence only if it is not an only child (in that case, sq_segments has already added the info and is just waiting for the values from "field")
				if (node.getParent().getChildCount()>1) {
					ArrayList<Object> topSequenceOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
					topSequenceOperands.add(token);
				}
			}
			visited.add(node.getChild(0));
		}
		
		if (nodeCat.equals("re_query")) {
			LinkedHashMap<String,Object> reQuery = new LinkedHashMap<String,Object>();
			reQuery.put("@subtype", "regex");
			String regex = node.getChild(0).toStringTree(poliqarpParser);
			reQuery.put("key", regex);
			reQuery.put("match", "eq");
			
			// if in field, regex was already added there
			if (!openNodeCats.get(1).equals("field")) {
				LinkedHashMap<String,Object> token = new LinkedHashMap<String,Object>();
				token.put("type", "token");
				token.put("key", reQuery);
				reQuery.put("type", "term");
				
				if (openNodeCats.get(1).equals("query")) {
					requestMap.put("query", token);
				} else {
					ArrayList<Object> topSequenceOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
					topSequenceOperands.add(token);
				}
			} 
		}
		
		if (nodeCat.equals("alignment")) {
			alignNext = true;
			LinkedHashMap<String,Object> alignGroup = new LinkedHashMap<String,Object>();
			// push but don't increase the stackedObjects counter in order to keep this
			// group open until the mother cq_segments node will be closed, since the
			// operands are siblings of this align node rather than children, i.e. the group
			// would be removed from the stack before seeing its operands.
			objectStack.push(alignGroup);
			stackedObjects++;
			// Step I: get info
			// fill group
			alignGroup.put("type", "group");
			alignGroup.put("alignment", "left");
			alignGroup.put("operands", new ArrayList<Object>());
			// Step II: decide where to put the group
			// add group to sequence only if it is not an only child (in that case, sq_segments has already added the info and is just waiting for the relevant info)
			if (node.getParent().getChildCount()>1) {
				ArrayList<Object> topSequenceOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
				topSequenceOperands.add(alignGroup);
			} else if (openNodeCats.get(2).equals("query")) {
				requestMap.put("query", alignGroup);	
			} else {
				ArrayList<Object> topSequenceOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
				topSequenceOperands.add(alignGroup); 
			}
			visited.add(node.getChild(0));
		}
		
		if (nodeCat.equals("element")) {
			// Step I: determine whether to create new token or get token from the stack (if added by cq_segments)
			LinkedHashMap<String, Object> elem;
			if (tokenStack.isEmpty()) {
				elem = new LinkedHashMap<String, Object>();
			} else {
				// in case sq_segments has already added the token
				elem = tokenStack.getFirst();
			}
			curToken = elem;
			objectStack.push(elem);
			stackedObjects++;
			// Step II: fill object (token values) and put into containing sequence
			elem.put("type", "span");
			String value = node.getChild(1).toStringTree(poliqarpParser);
			elem.put("key", value);
			// add token to sequence only if it is not an only child (in that case, cq_segments has already added the info and is just waiting for the values from "field")
			if (node.getParent().getChildCount()>1) {
				ArrayList<Object> topSequenceOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
				topSequenceOperands.add(elem);
			}
			visited.add(node.getChild(0));
			visited.add(node.getChild(1));
			visited.add(node.getChild(2));
		}
		
		if (nodeCat.equals("spanclass")) {
			LinkedHashMap<String,Object> span = new LinkedHashMap<String,Object>();
			span.put("type", "group");
			span.put("operation", "class");
			objectStack.push(span);
			stackedObjects++;
			ArrayList<Object> spanOperands = new ArrayList<Object>();
			// Step I: get info
			int classId = 0;
			if (QueryUtils.getNodeCat(node.getChild(1)).equals("spanclass_id")) {
				String ref = node.getChild(1).getChild(0).toStringTree(poliqarpParser);
				try {	
					classId = Integer.parseInt(ref);
				} catch (NumberFormatException e) {
					throw new QueryException("The specified class reference in the shrink/split-Operator is not a number: "+ref);
				}
				// only allow class id up to 255
				if (classId>255) {
					classId = 0;
				}
			}
			span.put("class", classId);
			span.put("operands", spanOperands);
			// Step II: decide where to put the span
			// add span to sequence only if it is not an only child (in that case, cq_segments has already added the info and is just waiting for the relevant info)
			if (openNodeCats.get(2).equals("query") && node.getParent().getChildCount() == 1) {
				requestMap.put("query", span);	
			} else if (objectStack.size()>1) {
				ArrayList<Object> topSequenceOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
				topSequenceOperands.add(span); 
			} 
			// ignore leading and trailing braces
			visited.add(node.getChild(0));
			visited.add(node.getChild(node.getChildCount()-1)); 
			if (QueryUtils.getNodeCat(node.getChild(1)).equals("spanclass_id")) {
				visited.add(node.getChild(1));
			}
		}
		
		if (nodeCat.equals("position")) {
			LinkedHashMap<String,Object> positionGroup = new LinkedHashMap<String,Object>();
			objectStack.push(positionGroup);
			stackedObjects++;
			ArrayList<Object> posOperands = new ArrayList<Object>();
			// Step I: get info
			String relation = QueryUtils.getNodeCat(node.getChild(0));
			positionGroup.put("type", "group");
			positionGroup.put("operation", "position");
			positionGroup.put("frame", relation.toLowerCase());
//			positionGroup.put("@subtype", "incl");
			positionGroup.put("operands", posOperands);
			// Step II: decide where to put the group
			// add group to sequence only if it is not an only child (in that case, sq_segments has already added the info and is just waiting for the relevant info)
			if (node.getParent().getChildCount()>1) {
				ArrayList<Object> topSequenceOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
				topSequenceOperands.add(positionGroup); 
			} else if (openNodeCats.get(2).equals("query")) {
				requestMap.put("query", positionGroup);	
			} else {
				ArrayList<Object> topSequenceOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
				topSequenceOperands.add(positionGroup); 
			}
		}
		
		if (nodeCat.equals("shrink")) {
			LinkedHashMap<String,Object> shrinkGroup = new LinkedHashMap<String,Object>();
			objectStack.push(shrinkGroup);
			stackedObjects++;
			ArrayList<Object> shrinkOperands = new ArrayList<Object>();
			// Step I: get info
			System.out.println("WAAAAAAAHHHHHHHHHHHHHHHHHHHHHH "+node.getChild(2).toStringTree(poliqarpParser));
			ArrayList<Integer> classRefs = new ArrayList<Integer>();
			String classRefOp = null;
			if (QueryUtils.getNodeCat(node.getChild(2)).equals("spanclass_id")) {
				ParseTree spanNode = node.getChild(2);
				for (int i=0; i<spanNode.getChildCount()-1; i++) {
					String ref = spanNode.getChild(i).getText();
					System.err.println("       "+ref);
					if (ref.equals("|") || ref.equals("&")) {
						classRefOp = ref.equals("|") ? "intersection" : "union";
					} else {
						try {
							int classRef = Integer.parseInt(ref);
							// only allow class id up to 255
							if (classRef>255) {
								classRef = 0;
							}
							classRefs.add(classRef);
						} catch (NumberFormatException e) {
							throw new QueryException("The specified class reference in the shrink/split-Operator is not a number.");
						}
					}
				}
			} else {
				classRefs.add(0);
			}
			shrinkGroup.put("type", "group");
			String type = node.getChild(0).toStringTree(poliqarpParser);
			String operation = type.equals("shrink") ? "submatch" : "split";
			shrinkGroup.put("operation", operation);
			shrinkGroup.put("classRef", classRefs);
			if (classRefOp != null) {
				shrinkGroup.put("classRefOp", classRefOp);
			}
			shrinkGroup.put("operands", shrinkOperands);
			int i=1;
			// Step II: decide where to put the group
			// add group to sequence only if it is not an only child (in that case, sq_segments has already added the info and is just waiting for the relevant info)
			if (node.getParent().getChildCount()>1) {
				ArrayList<Object> topSequenceOperands = (ArrayList<Object>) objectStack.get(i).get("operands"); // this shrinkGroup is on top
				topSequenceOperands.add(shrinkGroup);
			} else if (openNodeCats.get(2).equals("query")) {
				requestMap.put("query", shrinkGroup);	
			} else if (objectStack.size()>1) {
				ArrayList<Object> topSequenceOperands = (ArrayList<Object>) objectStack.get(i).get("operands");
				topSequenceOperands.add(shrinkGroup); 
			} 
			visited.add(node.getChild(0));
		}
		
		// repetition of token group
		if (nodeCat.equals("occ")) {
			ParseTree occChild = node.getChild(0);
			String repetition = occChild.toStringTree(poliqarpParser);
			String[] minmax = parseRepetition(repetition);
			curOccGroup.put("operation", "repetition");
			curOccGroup.put("@min", minmax[0]);
			curOccGroup.put("@max", minmax[1]);
			visited.add(occChild);
		}
				
		// flags for case sensitivity and whole-word-matching
		if (nodeCat.equals("flag")) {
			String flag = QueryUtils.getNodeCat(node.getChild(0)).substring(1); //substring removes leading slash '/'
			// add to current token's value
			((HashMap<String, Object>) curToken.get("key")).put("flag", flag);
		}
		
		if (nodeCat.equals("meta")) {
			inMeta=true;
			LinkedHashMap<String,Object> metaFilter = new LinkedHashMap<String,Object>();
			requestMap.put("meta", metaFilter);
			metaFilter.put("type", "meta");
		}
		
		if (nodeCat.equals("within") && !QueryUtils.getNodeCat(node.getParent()).equals("position")) {
			ParseTree domainNode = node.getChild(2);
			String domain = QueryUtils.getNodeCat(domainNode);
			LinkedHashMap<String,Object> curObject = (LinkedHashMap<String, Object>) objectStack.getFirst();
			curObject.put("within", domain);
			visited.add(node.getChild(0));
			visited.add(node.getChild(1));
			visited.add(domainNode);
		}
		
		objectsToPop.push(stackedObjects);
		tokensToPop.push(stackedTokens);
		fieldsToPop.push(stackedFields);
		
		/*
		 ****************************************************************
		 **************************************************************** 
		 *  recursion until 'request' node (root of tree) is processed  *
		 ****************************************************************
		 ****************************************************************
		 */
		for (int i=0; i<node.getChildCount(); i++) {
			ParseTree child = node.getChild(i);
			curChildIndex = i;
			processNode(child);
		}
				
		// set negField back 
		if (nodeCat.equals("neg_field") || nodeCat.equals("neg_field_group")) {
			negField = !negField;
		}
		
		// pop the align group that was introduced by previous 'align' but never closed
//		if (isAligned) {
//			isAligned=false;
//			objectStack.pop();
//		}
		
		// Stuff that happens when leaving a node (taking items off the stacks)
		for (int i=0; i<objectsToPop.get(0); i++) {
			objectStack.pop();
		}
		objectsToPop.pop();
		for (int i=0; i<tokensToPop.get(0); i++) {
			tokenStack.pop();
		}
		tokensToPop.pop();
		for (int i=0; i<fieldsToPop.get(0); i++) {
			fieldStack.pop();
		}
		fieldsToPop.pop();
		openNodeCats.pop();
	}

	private String[] parseRepetition(String repetition) {
		if (repetition.equals("*")) {
			return new String[] {"0", "100"};
		} else if (repetition.equals("+")) {
			return new String[] {"1", "100"};
		} else if (repetition.equals("?")) {
			return new String[] {"0", "1"};
		} else {
			repetition = repetition.substring(1, repetition.length()-1); // remove braces
			return repetition.split(",");
		}
	}

	private String[] parseEmptySegments(ParseTree emptySegments) {
		String[] minmax = new String[2];
		Integer min = 0;
		Integer max = 0;
		ParseTree child;
		for (int i=0; i<emptySegments.getChildCount()-1; i++) {
			child = emptySegments.getChild(i);
			ParseTree nextSibling = emptySegments.getChild(i+1);
			String nextSiblingString = nextSibling.toStringTree(); 
			if (child.toStringTree().equals("[]")) {
				if (nextSiblingString.equals("?")) {
					max++;
				} else if (nextSiblingString.startsWith("{")) {
					String occ = nextSiblingString.substring(1,nextSiblingString.length()-1);
					System.out.println(occ);
					if (occ.contains(",")) {
						String[] minmaxOcc = occ.split(",");
						min += Integer.parseInt(minmaxOcc[0]);
						max += Integer.parseInt(minmaxOcc[1]);
					} else {
						min += Integer.parseInt(occ);
						max += Integer.parseInt(occ);
					}
				} else {
					min++;
					max++;
				}
			}
		}
		child = emptySegments.getChild(emptySegments.getChildCount()-1);
		if (child.toStringTree().equals("[]")) {
			min++;
			max++;
		}
		minmax[0] = min.toString();
		minmax[1] = max.toString();
		return minmax;
	}

	@SuppressWarnings("unchecked")
	private void createOccGroup(ParseTree node) {
		LinkedHashMap<String,Object> occGroup = new LinkedHashMap<String,Object>();
		occGroup.put("type", "group");
		ArrayList<Object> groupOperands = new ArrayList<Object>();
		occGroup.put("operands", groupOperands);
		curOccGroup = occGroup;
		objectStack.push(occGroup);
		stackedObjects++;
		// if only this group is on the object stack, add as top query element
		if (objectStack.size()==1) {
				requestMap.put("query", occGroup);
		// embed in super sequence
		} else {
			ArrayList<Object> topSequenceOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
			topSequenceOperands.add(occGroup);
		}		
	}

	

	
	private static ParserRuleContext parsePoliqarpQuery (String p) throws QueryException {
		QueryUtils.checkUnbalancedPars(p);
		
		Lexer poliqarpLexer = new PoliqarpPlusLexer((CharStream)null);
	    ParserRuleContext tree = null;
	    // Like p. 111
	    try {

	      // Tokenize input data
	      ANTLRInputStream input = new ANTLRInputStream(p);
	      poliqarpLexer.setInputStream(input);
	      CommonTokenStream tokens = new CommonTokenStream(poliqarpLexer);
	      poliqarpParser = new PoliqarpPlusParser(tokens);

	      // Don't throw out erroneous stuff
	      poliqarpParser.setErrorHandler(new BailErrorStrategy());
	      poliqarpParser.removeErrorListeners();

	      // Get starting rule from parser
	      Method startRule = PoliqarpPlusParser.class.getMethod("request");
	      tree = (ParserRuleContext) startRule.invoke(poliqarpParser, (Object[])null);
	    }

	    // Some things went wrong ...
	    catch (Exception e) {
	      System.err.println( e.getMessage() );
	    }
	    
	    if (tree==null) throw new QueryException(
	    		"The query you specified could not be processed. Please make sure it is well-formed.");
	    
	    // Return the generated tree
	    return tree;
	  }
	
	public static void main(String[] args) {
		/*
		 * For testing
		 */
		
		
		String[] queries = new String[] {
				"shrink(1|2:{1:[base=der]}{2:[base=Mann]})",
//				"[base=foo] meta (author=name&year=2000)",
//				"[base=foo] meta year=2000",
				"{[base=Mann]}",
				"shrink(1:[orth=Der]{1:[orth=Mann][orth=geht]})"
		};
		PoliqarpPlusTree.debug=true;
		for (String q : queries) {
			try {
				System.out.println(q);
				System.out.println(PoliqarpPlusTree.parsePoliqarpQuery(q).toStringTree(PoliqarpPlusTree.poliqarpParser));
				@SuppressWarnings("unused")
				PoliqarpPlusTree pt = new PoliqarpPlusTree(q);
				System.out.println(q);
				System.out.println();
				
			} catch (Exception npe) {
				npe.printStackTrace();
				System.out.println("null\n");
			}
		}
	}

}