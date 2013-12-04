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
import org.antlr.v4.runtime.tree.ParseTree;

import de.ids_mannheim.korap.query.PoliqarpPlusLexer;
import de.ids_mannheim.korap.query.PoliqarpPlusParser;
import de.ids_mannheim.korap.query.serialize.AbstractSyntaxTree;

/**
 * Map representation of Poliqarp syntax tree as returned by ANTLR
 * @author joachim
 *
 */
public class PoliqarpPlusTree extends AbstractSyntaxTree {
	
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
	boolean cqHasOcc = false;
	/**
	 * Flag for negation of complete field
	 */
	boolean negField = false;
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
	 * Most centrally, this class maintains a set of nested maps and lists which represent the JSON tree, which is built by the JSON serialiser
	 * on basis of the {@link #requestMap} at the root of the tree. 
	 * <br/>
	 * The class further maintains a set of stacks which effectively keep track of which objects to embed in which containing objects. 
	 * 
	 * @param tree The syntax tree as returned by ANTLR
	 * @param parser The ANTLR parser instance that generated the parse tree
	 */
	public PoliqarpPlusTree(String query) {
		prepareContext();
		process(query);
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
	public void process(String query) {
		ParseTree tree = parsePoliqarpQuery(query);
		System.out.println("Processing PoliqarpPlus");
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
	 */
	@SuppressWarnings("unchecked")
	private void processNode(ParseTree node) {
		// Top-down processing
		if (visited.contains(node)) return;
		else visited.add(node);
		
		String nodeCat = getNodeCat(node);
		openNodeCats.push(nodeCat);
		
		Integer stackedObjects = 0;
		Integer stackedTokens= 0;
		Integer stackedFields = 0;
//		
//		System.err.println(objectStack);
//		System.out.println(openNodeCats);
//		System.out.println(" "+requestMap.get("query")+"");
		

		/*
		 ****************************************************************
		 **************************************************************** 
		 * 			Processing individual node categories  				*
		 ****************************************************************
		 ****************************************************************
		 */
		if (nodeCat.equals("query")) {
		}

		// cq_segments/sq_segments: token group
		if (nodeCat.equals("cq_segments") || nodeCat.equals("sq_segments")) {
			cqHasOcc = false;
			// disregard empty segments in simple queries (parsed by ANTLR as empty cq_segments) 
//			ignoreCq_segment = (node.getChildCount() == 1 && (node.getChild(0).toStringTree(poliqarpParser).equals(" ") || getNodeCat(node.getChild(0)).equals("spanclass") || getNodeCat(node.getChild(0)).equals("position")));
			ignoreCq_segment = (node.getChildCount() == 1 && (node.getChild(0).toStringTree(poliqarpParser).equals(" ")) );
			if (!ignoreCq_segment) {
				LinkedHashMap<String,Object> sequence = new LinkedHashMap<String,Object>();
				objectStack.push(sequence);
				stackedObjects++;
				// Step 0:  cq_segments has 'occ' child -> introduce group as super group to the sequence/token/group
				// this requires creating a group and inserting it at a suitable place
				if (getNodeCat(node.getChild(node.getChildCount()-1)).equals("occ")) {
					cqHasOcc = true;
					LinkedHashMap<String,Object> occGroup = new LinkedHashMap<String,Object>();
					occGroup.put("@type", "korap:group");
					ArrayList<Object> groupOperands = new ArrayList<Object>();
					occGroup.put("operands", groupOperands);
					curOccGroup = occGroup;
					objectStack.push(occGroup);
					stackedObjects++;
					if (openNodeCats.get(1).equals("query")) {
						// top query node
						if (node.getParent().getChildCount() == 1) {
							// only child
							requestMap.put("query", occGroup);
						} else {
							// not an only child, need to create containing sequence
							if (node.getParent().getChild(0).equals(node)) {
								// if first child, create containing sequence and embed there
								LinkedHashMap<String,Object> superSequence = new LinkedHashMap<String,Object>();
								superSequence.put("@type", "korap:sequence");
								ArrayList<Object> operands = new ArrayList<Object>();
								operands.add(occGroup);
								superSequence.put("operands", operands);
								requestMap.put("query", superSequence);
								objectStack.add(1, superSequence); // add at 2nd position to keep current cq_segment accessible
								stackedObjects--;
							} else {
								// if not first child, add to previously created parent sequence
								ArrayList<Object> topSequenceOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
								topSequenceOperands.add(occGroup);
							}
						}
					} else if (!objectStack.isEmpty()){
						// embed in super sequence
						int occExtraChild = cqHasOcc ? 1:0;
						ArrayList<Object> topSequenceOperands = (ArrayList<Object>) objectStack.get(1+occExtraChild).get("operands");
						topSequenceOperands.add(occGroup);
					}
				}
				// Step I: decide type of element (one or more elements? -> token or sequence)
				// take into account a possible 'occ' child
				int occExtraChild = cqHasOcc ? 1:0;
				if (node.getChildCount()>1+occExtraChild) {
					sequence.put("@type", "korap:sequence");
					ArrayList<Object> sequenceOperands = new ArrayList<Object>();
					sequence.put("operands", sequenceOperands);
				} else {
					// if only child, make the sequence a mere korap:token
					sequence.put("@type", "korap:token");
					tokenStack.push(sequence);
					stackedTokens++;
				}
				// Step II: decide where to put this element 
				// check if this is an argument for a containing occurrence group (see step 0)
				if (cqHasOcc) {
					ArrayList<Object> topGroupOperands = (ArrayList<Object>) objectStack.get(0).get("operands");
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
								superSequence.put("@type", "korap:sequence");
								ArrayList<Object> operands = new ArrayList<Object>();
								superSequence.put("operands", operands);
								operands.add(sequence);
								requestMap.put("query", superSequence);
								objectStack.add(1, superSequence); // add at 2nd position to keep current cq_segment accessible
								stackedObjects--;
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
			// Step I: determine whether to create new token or get token from the stack (if added by cq_segments)
			LinkedHashMap<String, Object> token;
			if (tokenStack.isEmpty()) {
				token = new LinkedHashMap<String, Object>();
				tokenStack.push(token);
				stackedTokens++;
				// do this only if token is newly created, otherwise it'll be in objectStack twice
				objectStack.push(token);
				stackedObjects++;
			} else {
				// in case cq_segments has already added the token
				token = tokenStack.getFirst();
			}
			curToken = token;
			// Step II: start filling object and add to containing sequence
			token.put("@type", "korap:token");
			// add token to sequence only if it is not an only child (in that case, cq_segments has already added the info and is just waiting for the values from "field")
			// take into account a possible 'occ' child
			int occExtraChild = cqHasOcc ? 1:0;
			if (node.getParent().getChildCount()>1+occExtraChild) {
				ArrayList<Object> topSequenceOperands = (ArrayList<Object>) objectStack.get(1+occExtraChild).get("operands");
				topSequenceOperands.add(token);
			}
		}

		// disjoint cq_segments, like ([base=foo][base=bar])|[base=foobar]
		if (nodeCat.equals("cq_disj_segments")) {
			LinkedHashMap<String,Object> disjunction = new LinkedHashMap<String,Object>();
			objectStack.push(disjunction);
			stackedObjects++;
			ArrayList<Object> disjOperands = new ArrayList<Object>();
			disjunction.put("@type", "korap:group");
			disjunction.put("relation", "or");
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
			String featureName = node.getChild(0).getChild(0).toStringTree(poliqarpParser);   //e.g. (field_name base) (field_op !=) (re_query "bar*")
			String relation = node.getChild(1).getChild(0).toStringTree(poliqarpParser);
			if (negField) {
				if (relation.startsWith("!")) {
					relation = relation.substring(1);
				} else {
					relation = "!"+relation;
				}
			}
			String value = "";
			ParseTree valNode = node.getChild(2);
			String valType = getNodeCat(valNode);
			fieldMap.put("@type", "korap:term");
			if (valType.equals("simple_query")) {
				value = valNode.getChild(0).getChild(0).toStringTree(poliqarpParser);   //e.g. (simple_query (sq_segment foo))
			} else if (valType.equals("re_query")) {
				value = valNode.getChild(0).toStringTree(poliqarpParser); 				//e.g. (re_query "bar*")
				fieldMap.put("@subtype", "korap:value#regex");
			}
			fieldMap.put("@value", featureName+":"+value);
			fieldMap.put("relation", relation);
			// Step II: decide where to put the field map (as the only value of a token or the meta filter or as a part of a group in case of coordinated fields)
			if (fieldStack.isEmpty()) {
				if (!inMeta) {
					tokenStack.getFirst().put("@value", fieldMap);
				} else {
					((HashMap<String, Object>) requestMap.get("meta")).put("@value", fieldMap);
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
			group.put("@type", "korap:group");
			group.put("operands", groupOperands);
			fieldStack.push(groupOperands);
			stackedFields++;
			// Step I: get operator (& or |)
			ParseTree operatorNode = node.getChild(1).getChild(0);
			String operator = getNodeCat(operatorNode);
			String relation = operator.equals("&") ? "and" : "or";
			if (negField) {
				relation = relation.equals("or") ? "and": "or";
			}
			group.put("relation", relation);
			// Step II: decide where to put the group (directly under token or in top meta filter section or embed in super group)
			if (openNodeCats.get(1).equals("cq_segment")) {
				tokenStack.getFirst().put("@value", group);
			} else if (openNodeCats.get(1).equals("meta_field_group")) {
				((HashMap<String, Object>) requestMap.get("meta")).put("@value", group);
			} else if (openNodeCats.get(2).equals("conj_field")) {
				fieldStack.get(1).add(group);
			} else {
				tokenStack.getFirst().put("@value", group);
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
			token.put("@type", "korap:token");
			String word = node.getChild(0).toStringTree(poliqarpParser);
			LinkedHashMap<String,Object> tokenValues = new LinkedHashMap<String,Object>();
			token.put("@value", tokenValues);
			tokenValues.put("orth", word);
			tokenValues.put("relation", "=");
			// add token to sequence only if it is not an only child (in that case, sq_segments has already added the info and is just waiting for the values from "field")
			if (node.getParent().getChildCount()>1) {
				ArrayList<Object> topSequenceOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
				topSequenceOperands.add(token);
			}
		}
		
		if (nodeCat.equals("re_query")) {
			LinkedHashMap<String,Object> reQuery = new LinkedHashMap<String,Object>();
			reQuery.put("@subtype", "korap:regex");
			String regex = node.getChild(0).toStringTree(poliqarpParser);
			reQuery.put("@value", regex);
			reQuery.put("relation", "=");
			
			if (!openNodeCats.get(1).equals("field")) {
				LinkedHashMap<String,Object> token = new LinkedHashMap<String,Object>();
				token.put("@type", "korap:token");
				token.put("@value", reQuery);
				reQuery.put("type", "korap:term");
				
				if (openNodeCats.get(1).equals("query")) {
					requestMap.put("query", token);
				} else {
					ArrayList<Object> topSequenceOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
					topSequenceOperands.add(token);
				}
			} 
//			ArrayList<Object> topSequenceOperands = (ArrayList<Object>) objectStack.get(0).get("operands");
//			topSequenceOperands.add(reQuery);
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
			elem.put("@type", "korap:element");
			String value = node.getChild(1).toStringTree(poliqarpParser);
			elem.put("@value", value);
			// add token to sequence only if it is not an only child (in that case, cq_segments has already added the info and is just waiting for the values from "field")
			if (node.getParent().getChildCount()>1) {
				ArrayList<Object> topSequenceOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
				topSequenceOperands.add(elem);
			}
		}
		
		if (nodeCat.equals("spanclass")) {
			LinkedHashMap<String,Object> span = new LinkedHashMap<String,Object>();
			objectStack.push(span);
			stackedObjects++;
			ArrayList<Object> spanOperands = new ArrayList<Object>();
			String id = "0";
			// Step I: get info
			if (getNodeCat(node.getChild(1)).equals("spanclass_id")) {
				id = node.getChild(1).getChild(0).toStringTree(poliqarpParser);
				id = id.substring(0, id.length()-1); // remove trailing colon ':'
				// only allow class id up to 255
				if (Integer.parseInt(id)>255) {
					id = "0";
				}
			}
			span.put("@type", "korap:group");
			span.put("class", id);
			span.put("operands", spanOperands);
			// Step II: decide where to put the span
			// add span to sequence only if it is not an only child (in that case, cq_segments has already added the info and is just waiting for the relevant info)
//			if (node.getParent().getChildCount()>4) {
//				ArrayList<Object> topSequenceOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
//				topSequenceOperands.add(span);
//			} else
			if (openNodeCats.get(2).equals("query")) {
				requestMap.put("query", span);	
			} else if (objectStack.size()>1) {
				ArrayList<Object> topSequenceOperands = (ArrayList<Object>) objectStack.get(1).get("operands");
				topSequenceOperands.add(span); 
				
			} 
		}
		
		if (nodeCat.equals("position")) {
			LinkedHashMap<String,Object> positionGroup = new LinkedHashMap<String,Object>();
			objectStack.push(positionGroup);
			stackedObjects++;
			ArrayList<Object> posOperands = new ArrayList<Object>();
			// Step I: get info
			String relation = getNodeCat(node.getChild(0));
			positionGroup.put("@type", "korap:group");
			positionGroup.put("relation", "position");
			positionGroup.put("position", relation.toLowerCase());
			positionGroup.put("operands", posOperands);
			// Step II: decide where to put the group
			// add group to sequence only if it is not an only child (in that case, sq_segments has already added the info and is just waiting for the relevant info)
			if (node.getParent().getChildCount()>1) {
				
			} else if (openNodeCats.get(2).equals("query")) {
				requestMap.put("query", positionGroup);	
			} else if (objectStack.size()>1) {
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
			String operandClass = "0";
			String type = getNodeCat(node.getChild(0));
			if (getNodeCat(node.getChild(2)).equals("spanclass_id")) {
				operandClass = node.getChild(2).getChild(0).toStringTree(poliqarpParser);
				operandClass = operandClass.substring(0, operandClass.length()-1); // remove trailing colon ':'
				// only allow class id up to 255
				if (Integer.parseInt(operandClass)>255) {
					operandClass = "0";
				}
			}
			shrinkGroup.put("@type", "korap:group");
			shrinkGroup.put("relation", "shrink");
			shrinkGroup.put(type, operandClass);
			shrinkGroup.put("operands", shrinkOperands);
			int i=1;
			// check if sequence below this and, if so, insert
			if (hasChild(node,"cq_segments")) {
				i++;
				LinkedHashMap<String,Object> subSequence = new LinkedHashMap<String,Object>();
				subSequence.put("@type", "korap:sequence");
				ArrayList<Object> subSequenceOperands = new ArrayList<Object>();
				subSequence.put("operands", subSequenceOperands);
				shrinkOperands.add(subSequence);
				objectStack.push(subSequence);
				stackedObjects++;
			}
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
			curOccGroup.put("relation", "repetition");
			curOccGroup.put("quantifier", repetition);
			visited.add(occChild);
		}
				
		// flags for case sensitivity and whole-word-matching
		if (nodeCat.equals("flag")) {
			String flag = getNodeCat(node.getChild(0)).substring(1); //substring removes leading slash '/'
			// add to current token's value
			((HashMap<String, Object>) curToken.get("@value")).put("flag", flag);
		}
		
		if (nodeCat.equals("meta")) {
			inMeta=true;
			LinkedHashMap<String,Object> metaFilter = new LinkedHashMap<String,Object>();
			requestMap.put("meta", metaFilter);
			metaFilter.put("@type", "korap:meta");
		}
		
		if (nodeCat.equals("within") && !getNodeCat(node.getParent()).equals("position")) {
			ParseTree domainNode = node.getChild(2);
			String domain = getNodeCat(domainNode);
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
			processNode(child);
		}
				
		// set negField back 
		if (nodeCat.equals("neg_field") || nodeCat.equals("neg_field_group")) {
			negField = !negField;
		}
		
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

	/**
	 * Returns the category (or 'label') of the root of a (sub-)ParseTree.
	 * @param node
	 * @return
	 */
	public String getNodeCat(ParseTree node) {
		String nodeCat = node.toStringTree(poliqarpParser);
		Pattern p = Pattern.compile("\\((.*?)\\s"); // from opening parenthesis to 1st whitespace
		Matcher m = p.matcher(node.toStringTree(poliqarpParser));
		if (m.find()) {
			nodeCat = m.group(1);
		} 
		return nodeCat;
	}
	
	/**
	 * Tests whether a certain node has a child by a certain name
	 * @param node The parent node.
	 * @param childCat The category of the potential child.
	 * @return true iff one or more children belong to the specified category
	 */
	private boolean hasChild(ParseTree node, String childCat) {
		for (int i=0; i<node.getChildCount(); i++) {
			if (getNodeCat(node.getChild(i)).equals(childCat)) {
				return true;
			}
		}
		return false;
	}
	
	private static ParserRuleContext parsePoliqarpQuery (String p) {
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

	    // Return the generated tree
	    return tree;
	  }
	
	public static void main(String[] args) {
		/*
		 * For testing
		 */
		String[] queries = new String[] {
				"[base=foo]|([base=foo][base=bar])* meta author=Goethe&year=1815",
				"([base=foo]|[base=bar])[base=foobar]",
				"shrink({[base=Mann]})",
				"shrink({[base=foo]}[orth=bar])",
				"shrink(1:[base=Der]{1:[base=Mann]})",
				"[base=foo]|([base=foo][base=bar])*",
				"([base=foo]|[base=bar])[base=foobar]",
				"[base=foo]([base=bar]|[base=foobar/i])",
				"[base=bar|base=foo]",
				"[base=bar]",
				"[base=foo][base=bar]",
				"[(base=bar|base=foo)&orth=wee]",
				"[base=foo/i][base=bar]{2,4}",
				"foo bar/i",
				"{[base=foo]}[orth=bar]",
				"{[base=foo]}{[orth=bar]}",
				"{1:[base=foo]<np>}",
				"{[base=foo]}[orth=bar]",
				"shrink({[base=foo]})",
				"shrink({[base=foo]})[orth=bar]",
				"[orth=bar]shrink({[base=foo]})",
				"[base=foo]*[base=bar]",
				"([base=foo][base=bar])*",
				"shrink(1:[base=Der]{1:[base=Mann]})",
				"[base=foo]<np>",
				"[base!=foo]",
				"[base=foo&orth=bar]",
				"[!(base=foo&orth=bar)]",
				"'vers{2,3}uch'",
				"[orth='vers*ch']",
				"shrink(1:{[base=Der][base=Mann]})",
				"{[base=foo][orth=bar]}",
				"[base=der]shrink(1:[base=Der]{1:[base=Mann]})",
				"<np>",
//				"startsWith({<sentence>},<np>)",
//				"startsWith({<sentence>},[base=foo])",
				"[base=foo]|([base=foo][base=bar])* meta author=Goethe&year=1815",
				"([base=foo][base=bar])*",
				"[(base=bar|base=foo)&orth=foobar]",
				"contains(<np>,[base=foo])",
//				"[base=foo] within s",
				"within(<np>,[base=foo])"
				};
		for (String q : queries) {
			try {
				System.out.println(q);
				System.out.println(PoliqarpPlusTree.parsePoliqarpQuery(q).toStringTree(PoliqarpPlusTree.poliqarpParser));
				@SuppressWarnings("unused")
				PoliqarpPlusTree pt = new PoliqarpPlusTree(q);
//				System.out.println(PoliqarpPlusTree.parsePoliqarpQuery(q).toStringTree(PoliqarpPlusTree.poliqarpParser));
				System.out.println();
				
			} catch (NullPointerException npe) {
				npe.printStackTrace();
				System.out.println("null\n");
			}
		}
	}

}