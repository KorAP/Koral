package de.ids_mannheim.korap.query.serialize;

import de.ids_mannheim.korap.query.poliqarp.PoliqarpPlusLexer;
import de.ids_mannheim.korap.query.poliqarp.PoliqarpPlusParser;
import de.ids_mannheim.korap.util.QueryException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Map representation of Poliqarp syntax tree as returned by ANTLR
 *
 * @author joachim
 */
public class PoliqarpPlusTree extends Antlr4AbstractSyntaxTree {

	Logger log = LoggerFactory.getLogger(PoliqarpPlusTree.class);
	/**
	 * Top-level map representing the whole request.
	 */
	LinkedHashMap<String, Object> requestMap = new LinkedHashMap<String, Object>();
	/**
	 * Keeps track of open node categories
	 */
	LinkedList<String> openNodeCats = new LinkedList<String>();
	/**
	 * Parser object deriving the ANTLR parse tree.
	 */
	private Parser parser;
	/**
	 * Keeps track of all visited nodes in a tree
	 */
	private List<ParseTree> visited = new ArrayList<ParseTree>();
	/**
	 * Keeps track of active object.
	 */
	private LinkedList<LinkedHashMap<String, Object>> objectStack = new LinkedList<LinkedHashMap<String, Object>>();
	/**
	 * Keeps track of how many objects there are to pop after every recursion of {@link #processNode(ParseTree)}
	 */
	private LinkedList<Integer> objectsToPop = new LinkedList<Integer>();
	/**
	 * If true, print debug statements
	 */
	public static boolean verbose = false;
	ParseTree currentNode = null;
	Integer stackedObjects = 0;

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
		process(query);
		System.out.println(">>> " + requestMap.get("query") + " <<<");
		log.info(">>> " + requestMap.get("query") + " <<<");
	}

	@Override
	public Map<String, Object> getRequestMap() {
		return requestMap;
	}

	@Override
	public void process(String query) throws QueryException {
		ParseTree tree;
		tree = parsePoliqarpQuery(query);
		super.parser = this.parser;
		log.info("Processing PoliqarpPlus");
		requestMap.put("@context", "http://ids-mannheim.de/ns/KorAP/json-ld/v0.1/context.jsonld");
		processNode(tree);
	}

	/**
	 * Recursively calls itself with the children of the currently active node, traversing the tree nodes in a top-down, depth-first fashion.
	 * A list is maintained that contains all visited nodes
	 * which have been directly addressed by their (grand-/grand-grand-/...) parent nodes, such that some processing time is saved, as these node will
	 * not be processed. This method is effectively a list of if-statements that are responsible for treating the different node types correctly and filling the
	 * respective maps/lists.
	 *
	 * @param node The currently processed node. The process(String query) method calls this method with the root.
	 * @throws QueryException
	 */
	private void processNode(ParseTree node) throws QueryException {
		// Top-down processing
		if (visited.contains(node)) return;
		else visited.add(node);

		currentNode = node;

		String nodeCat = getNodeCat(node);
		openNodeCats.push(nodeCat);

		stackedObjects = 0;

		if (verbose) {
			System.err.println(" " + objectStack);
			System.out.println(openNodeCats);
		}

		/*
		 ****************************************************************
		 **************************************************************** 
		 * 			Processing individual node categories  				*
		 ****************************************************************
		 ****************************************************************
		 */

		if (nodeCat.equals("segment")) {
			// Cover possible quantification (i.e. repetition) of segment
			ParseTree quantification = getFirstChildWithCat(node, "repetition");
			if (quantification != null) {
				LinkedHashMap<String,Object> quantGroup = makeGroup("repetition");
				int[] minmax = parseRepetition(quantification);
				quantGroup.put("min", minmax[0]);
				quantGroup.put("max", minmax[1]);
				putIntoSuperObject(quantGroup);
				objectStack.push(quantGroup);
				stackedObjects++;
			}
		}

		if (nodeCat.equals("sequence")) {
			LinkedHashMap<String,Object> sequence = makeGroup("sequence");
			ParseTree emptyTokens = getFirstChildWithCat(node, "emptyTokenSequence");
			if (emptyTokens!=null) {
				int[] minmax = parseEmptySegments(emptyTokens);
				LinkedHashMap<String,Object> distance = makeDistance("w", minmax[0], minmax[1]);
				sequence.put("inOrder", true);
				ArrayList<Object> distances = new ArrayList<Object>();
				distances.add(distance);
				sequence.put("distances", distances);
			}
			putIntoSuperObject(sequence);
			objectStack.push(sequence);
			stackedObjects++;
		}

		if (nodeCat.equals("token")) {
			LinkedHashMap<String,Object> token = makeToken();
			// handle negation
			List<ParseTree> negations = getChildrenWithCat(node, "!");
			boolean negated = false;
			if (negations.size() % 2 == 1) negated = true;
			if (getNodeCat(node.getChild(0)).equals("key")) {
				// no 'term' child, but direct key specification: process here
				LinkedHashMap<String,Object> term = makeTerm();
				
				String key = node.getChild(0).getText();
				if (getNodeCat(node.getChild(0).getChild(0)).equals("regex")) {
					term.put("type", "type:regex");
					key = key.substring(1,key.length()-1);
				}
				term.put("layer", "orth");
				term.put("key", key);
				String matches = negated ? "ne" : "eq";
				term.put("match", "match:"+matches);
				ParseTree flagNode = getFirstChildWithCat(node, "flag");
				if (flagNode != null) {
					String flag = getNodeCat(flagNode.getChild(0)).substring(1); //substring removes leading slash '/'
					if (flag.contains("i")) term.put("caseInsensitive", true);
					else if (flag.contains("I")) term.put("caseInsensitive", false);
					if (flag.contains("x")) {
						term.put("key", ".*?"+key+".*?");
					}
				}
				token.put("wrap", term);
			} else {
				// child is 'term' or 'termGroup' -> process in extra method 
				LinkedHashMap<String,Object> termOrTermGroup = parseTermOrTermGroup(node.getChild(1), negated);
				token.put("wrap", termOrTermGroup);
			}
			putIntoSuperObject(token);
			visited.add(node.getChild(0));
			visited.add(node.getChild(2));
		}

		if (nodeCat.equals("alignment")) {
			LinkedHashMap<String,Object> aligned = makeGroup("alignment");
			aligned.put("align", "align:left");
			putIntoSuperObject(aligned);
			objectStack.push(aligned);
			stackedObjects++;
		}

		if (nodeCat.equals("span")) {
			LinkedHashMap<String,Object> span = makeSpan();
			ParseTree keyNode = getFirstChildWithCat(node, "key");
			ParseTree layerNode = getFirstChildWithCat(node, "layer");
			ParseTree foundryNode = getFirstChildWithCat(node, "foundry");
			ParseTree termOpNode = getFirstChildWithCat(node, "termOp");
			if (foundryNode != null) span.put("foundry", foundryNode.getText());
			if (layerNode != null) {
				String layer = layerNode.getText();
				if (layer.equals("base")) layer="lemma";
				span.put("layer", layer);
			}
			span.put("key", keyNode.getText());
			if (termOpNode != null) {
				String termOp = termOpNode.getText();
				if (termOp.equals("==")) span.put("matches", "matches:eq");
				else if (termOp.equals("!=")) span.put("matches", "matches:ne");
			}
			putIntoSuperObject(span);
			objectStack.push(span);
			stackedObjects++;
		}

		if (nodeCat.equals("disjunction")) {
			LinkedHashMap<String,Object> disjunction = makeGroup("or");
			putIntoSuperObject(disjunction);
			objectStack.push(disjunction);
			stackedObjects++;
		}

		if (nodeCat.equals("position")) {
			LinkedHashMap<String,Object> position = makePosition(parseFrame(node.getChild(0)));
			putIntoSuperObject(position);
			objectStack.push(position);
			stackedObjects++;
			// offsets
			if (hasChild(node, "emptyTokenSequence")) {
				LinkedHashMap<String,Object> sequence = makeGroup("sequence");
				ParseTree leftOffset = getNthChildWithCat(node, "emptyTokenSequence", 1);
				if (leftOffset!=null) {
					int[] minmax = parseEmptySegments(leftOffset);
					sequence.put("leftoffset-min", minmax[0]-1);
					sequence.put("leftoffset-max", minmax[1]-1);
				}
				ParseTree rightOffset = getNthChildWithCat(node, "emptyTokenSequence", 2);
				if (rightOffset!=null) {
					int[] minmax = parseEmptySegments(rightOffset);
					sequence.put("rightoffset-min", minmax[0]-1);
					sequence.put("rightoffset-max", minmax[1]-1);
				}
				putIntoSuperObject(sequence);
				objectStack.push(sequence);
				stackedObjects++;
			}
		}

		if (nodeCat.equals("spanclass")) {
			// Step I: get info
			int classId = 0;
			if (getNodeCat(node.getChild(1)).equals("spanclass_id")) {
				String ref = node.getChild(1).getChild(0).toStringTree(parser);
				try {
					classId = Integer.parseInt(ref);
				} catch (NumberFormatException e) {
					log.error("The specified class reference in the focus/split-Operator is not a number: " + ref);
					throw new QueryException("The specified class reference in the focus/split-Operator is not a number: " + ref);
				}
				// only allow class id up to 255
				if (classId > 255) {
					classId = 0;
				}
			}
			LinkedHashMap<String, Object> classGroup = makeSpanClass(classId);
			putIntoSuperObject(classGroup);
			objectStack.push(classGroup);
			stackedObjects++;
		}


		if (nodeCat.equals("matching")) {
			// Step I: get info
			ArrayList<Integer> classRefs = new ArrayList<Integer>();
			String classRefOp = null;
			if (getNodeCat(node.getChild(2)).equals("spanclass_id")) {
				ParseTree spanNode = node.getChild(2);
				for (int i = 0; i < spanNode.getChildCount() - 1; i++) {
					String ref = spanNode.getChild(i).getText();
					if (ref.equals("|") || ref.equals("&")) {
						classRefOp = ref.equals("|") ? "intersection" : "union";
					} else {
						try {
							int classRef = Integer.parseInt(ref);
							classRefs.add(classRef);
						} catch (NumberFormatException e) {
							throw new QueryException("The specified class reference in the shrink/split-Operator is not a number.");
						}
					}
				}
			} else {
				classRefs.add(0);
			}
			LinkedHashMap<String, Object> referenceGroup = makeReference(classRefs);

			String type = node.getChild(0).toStringTree(parser);
			if (type.equals("split")) referenceGroup.put("operation", "operation:split");
			if (classRefOp != null) {
				referenceGroup.put("classRefOp", "classRefOp:" + classRefOp);
			}
			ArrayList<Object> referenceOperands = new ArrayList<Object>();
			referenceGroup.put("operands", referenceOperands);
			// Step II: decide where to put the group
			putIntoSuperObject(referenceGroup);
			objectStack.push(referenceGroup);
			stackedObjects++;
			visited.add(node.getChild(0));
		}

		if (nodeCat.equals("meta")) {
			LinkedHashMap<String, Object> metaFilter = new LinkedHashMap<String, Object>();
			requestMap.put("meta", metaFilter);
			metaFilter.put("@type", "korap:meta");
		}

		if (nodeCat.equals("within") && !getNodeCat(node.getParent()).equals("position")) {
			ParseTree domainNode = node.getChild(2);
			String domain = getNodeCat(domainNode);
			LinkedHashMap<String, Object> curObject = (LinkedHashMap<String, Object>) objectStack.getFirst();
			curObject.put("within", domain);
			visited.add(node.getChild(0));
			visited.add(node.getChild(1));
			visited.add(domainNode);
		}

		objectsToPop.push(stackedObjects);

		/*
		 ****************************************************************
		 **************************************************************** 
		 *  recursion until 'request' node (root of tree) is processed  *
		 ****************************************************************
		 ****************************************************************
		 */
		for (int i = 0; i < node.getChildCount(); i++) {
			ParseTree child = node.getChild(i);
			processNode(child);
		}

		// Stuff that happens when leaving a node (taking items off the stacks)
		for (int i = 0; i < objectsToPop.get(0); i++) {
			objectStack.pop();
		}
		objectsToPop.pop();
		openNodeCats.pop();
	}

	/**
	 * Parses a repetition node
	 * @param node
	 * @return A two-element array, of which the first element is an int representing 
	 * the minimal number of repetitions of the quantified element, and the second 
	 * element representing the maximal number of repetitions 
	 */
	private int[] parseRepetition(ParseTree node) {
		int min = 0, max = 0;
		// (repetition) node can be of two types: 'kleene' or 'range'
		ParseTree repetitionTypeNode = node.getChild(0);
		String repetitionType = getNodeCat(repetitionTypeNode);
		if (repetitionType.equals("kleene")) {
			// kleene operators (+ and *) as well as optionality (?)
			String kleeneOp = repetitionTypeNode.getText();
			if (kleeneOp.equals("*")) {
				max = MAXIMUM_DISTANCE;
			} else if (kleeneOp.equals("+")) {
				min = 1;
				max = MAXIMUM_DISTANCE;
			} if (kleeneOp.equals("?")) {
				max = 1;
			}
		} else {
			// Range node of form "{ min , max }" or "{ max }" or "{ , max }"  or "{ min , }"
			ParseTree minNode = getFirstChildWithCat(repetitionTypeNode, "min");
			ParseTree maxNode = getFirstChildWithCat(repetitionTypeNode, "max");
			if (maxNode!=null) max = Integer.parseInt(maxNode.getText());
			else max = MAXIMUM_DISTANCE;
			// min is optional: if not specified, min = max
			if (minNode!=null) min = Integer.parseInt(minNode.getText());
			else if (hasChild(repetitionTypeNode, ",")) min = 0;
			else min = max;
		}
		return new int[]{min,max};
	}

	private String parseFrame(ParseTree node) {
		return node.toStringTree(parser);
	}

	/**
	 * Parses a (term) or (termGroup) node
	 * @param node
	 * @param negatedGlobal Indicates whether the term/termGroup is globally negated, e.g. through a negation 
	 * operator preceding the related token like "![base=foo]". Global negation affects the term's "match" parameter.
	 * @return A term or termGroup object, depending on input
	 */
	@SuppressWarnings("unchecked")
	private LinkedHashMap<String, Object> parseTermOrTermGroup(ParseTree node, boolean negatedGlobal) {
		if (getNodeCat(node).equals("term")) {
			String key = null;
			LinkedHashMap<String,Object> term = makeTerm();
			// handle negation
			boolean negated = negatedGlobal;
			List<ParseTree> negations = getChildrenWithCat(node, "!");
			if (negations.size() % 2 == 1) negated = !negated;
			// retrieve possible nodes
			ParseTree keyNode = getFirstChildWithCat(node, "key");
			ParseTree layerNode = getFirstChildWithCat(node, "layer");
			ParseTree foundryNode = getFirstChildWithCat(node, "foundry");
			ParseTree termOpNode = getFirstChildWithCat(node, "termOp");
			ParseTree flagNode = getFirstChildWithCat(node, "flag");
			// process foundry
			if (foundryNode != null) term.put("foundry", foundryNode.getText());
			// process layer: map "base" -> "lemma"
			if (layerNode != null) {
				String layer = layerNode.getText();
				if (layer.equals("base")) layer="lemma";
				term.put("layer", layer);
			}
			// process key: 'normal' or regex?
			key = keyNode.getText();
			if (getNodeCat(keyNode.getChild(0)).equals("regex")) {
				term.put("type", "type:regex");
				key = key.substring(1, key.length()-1); // remove leading and trailing quotes
			}
			term.put("key", key);
			// process operator ("match" property)
			if (termOpNode != null) {
				String termOp = termOpNode.getText();
				negated = termOp.contains("!") ? !negated : negated; 
				if (!negated) term.put("match", "match:eq");
				else term.put("match", "match:ne");
			}
			// process possible flags
			if (flagNode != null) {
				String flag = getNodeCat(flagNode.getChild(0)).substring(1); //substring removes leading slash '/'
				System.err.println(flag);
				if (flag.contains("i")) term.put("caseInsensitive", true);
				else if (flag.contains("I")) term.put("caseInsensitive", false);
				if (flag.contains("x")) {
					term.put("key", ".*?"+key+".*?");  // flag 'x' allows submatches: overwrite key with appended .*? 
				}
			}
			return term;
		} else {
			// For termGroups, establish a boolean relation between operands and recursively call this function with
			// the term or termGroup operands
			LinkedHashMap<String,Object> termGroup = null;
			ParseTree leftOp = null;
			ParseTree rightOp = null;
			// check for leading/trailing parantheses
			if (!getNodeCat(node.getChild(0)).equals("(")) leftOp = node.getChild(0);
			else leftOp = node.getChild(1);
			if (!getNodeCat(node.getChild(node.getChildCount()-1)).equals(")")) rightOp = node.getChild(node.getChildCount()-1);
			else rightOp = node.getChild(node.getChildCount()-2);
			// establish boolean relation
			ParseTree boolOp = getFirstChildWithCat(node, "booleanOp"); 
			String operator = boolOp.getText().equals("&") ? "and" : "or";
			termGroup = makeTermGroup(operator);
			ArrayList<Object> operands = (ArrayList<Object>) termGroup.get("operands");
			// recursion with left/right operands
			operands.add(parseTermOrTermGroup(leftOp, negatedGlobal));
			operands.add(parseTermOrTermGroup(rightOp, negatedGlobal));
			return termGroup;
		}
	}

	/**
	 * Puts an object into the operands list of its governing (or "super") object which had been placed on the
	 * {@link #objectStack} before and is still on top of the stack. If this is the top object of the tree, it is put there
	 * instead of into some (non-existent) operand stack.
	 * @param object The object to be inserted
	 */
	private void putIntoSuperObject(LinkedHashMap<String, Object> object) {
		putIntoSuperObject(object, 0);
	}

	/**
	 * Puts an object into the operands list of its governing (or "super") object which had been placed on the
	 * {@link #objectStack} before. If this is the top object of the tree, it is put there
	 * instead of into some (non-existent) operand stack.
	 * @param object The object to be inserted
	 * @param objStackPosition Indicated the position of the super object on the {@link #objectStack} (in case not the top
	 * element of the stack is the super object.
	 */
	@SuppressWarnings({ "unchecked" })
	private void putIntoSuperObject(LinkedHashMap<String, Object> object, int objStackPosition) {
		if (objectStack.size()>objStackPosition) {
			ArrayList<Object> topObjectOperands = (ArrayList<Object>) objectStack.get(objStackPosition).get("operands");
			topObjectOperands.add(object);
		} else {
			requestMap.put("query", object);
		}
	}

	private int[] parseEmptySegments(ParseTree emptySegments) {
		Integer min = 1;
		Integer max = 1;
		ParseTree child;
		for (int i = 0; i < emptySegments.getChildCount(); i++) {
			child = emptySegments.getChild(i);
			ParseTree nextSibling = emptySegments.getChild(i + 1);
			if (child.toStringTree(parser).equals("(emptyToken [ ])")) {
				if (nextSibling != null && getNodeCat(nextSibling).equals("repetition")) {
					int[] minmax = parseRepetition(nextSibling);
					min += minmax[0];
					max += minmax[1];
				} else {
					min++;
					max++;
				}
			}
		}
		if (max > MAXIMUM_DISTANCE) max = MAXIMUM_DISTANCE;
		return new int[]{min, max};
	}

	private ParserRuleContext parsePoliqarpQuery(String p) throws QueryException {
		checkUnbalancedPars(p);
		Lexer poliqarpLexer = new PoliqarpPlusLexer((CharStream) null);
		ParserRuleContext tree = null;
		// Like p. 111
		try {
			// Tokenize input data
			ANTLRInputStream input = new ANTLRInputStream(p);
			poliqarpLexer.setInputStream(input);
			CommonTokenStream tokens = new CommonTokenStream(poliqarpLexer);
			parser = new PoliqarpPlusParser(tokens);

			// Don't throw out erroneous stuff
			parser.setErrorHandler(new BailErrorStrategy());
			parser.removeErrorListeners();

			// Get starting rule from parser
			Method startRule = PoliqarpPlusParser.class.getMethod("request");
			tree = (ParserRuleContext) startRule.invoke(parser, (Object[]) null);
			log.debug(tree.toStringTree(parser));
		}
		// Some things went wrong ...
		catch (Exception e) {
			log.error("Could not parse query. Please make sure it is well-formed.");;
			log.error("Underlying error is: "+e.getMessage());
			System.err.println(e.getMessage());
		}

		if (tree == null) {
			throw new QueryException("The query you specified could not be processed. Please make sure it is well-formed.");
		}
		// Return the generated tree
		return tree;
	}

	public static void main(String[] args) {
		/*
		 * For testing
		 */
		String[] queries = new String[]{
				"[base=foo][base=foo]",
				"Der \"Baum\"/x",
				"contains(<vp>,[][base=foo])",
				"[hallo=welt]*"
		};
//		PoliqarpPlusTree.verbose=true;
		for (String q : queries) {
			try {
				System.out.println(q);
				@SuppressWarnings("unused")
				PoliqarpPlusTree pt = new PoliqarpPlusTree(q);
				System.out.println();

			} catch (Exception npe) {
				npe.printStackTrace();
				System.out.println("null\n");
			}
		}
	}
}