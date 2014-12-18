package de.ids_mannheim.korap.query.serialize;

import de.ids_mannheim.korap.query.poliqarp.PoliqarpPlusLexer;
import de.ids_mannheim.korap.query.poliqarp.PoliqarpPlusParser;
import de.ids_mannheim.korap.query.serialize.util.Antlr4DescriptiveErrorListener;
import de.ids_mannheim.korap.query.serialize.util.StatusCodes;
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
 * @author Joachim Bingel (bingel@ids-mannheim.de)
 */
public class PoliqarpPlusTree extends Antlr4AbstractSyntaxTree {

	private static Logger log = LoggerFactory.getLogger(PoliqarpPlusTree.class);
	private int classCounter = 128;

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
		log.info(">>> " + requestMap.get("query") + " <<<");
	}

	@Override
	public void process(String query) throws QueryException {
		ParseTree tree;
		tree = parsePoliqarpQuery(query);
		super.parser = this.parser;
		log.info("Processing PoliqarpPlus");
		if (tree != null) {
			log.debug("ANTLR parse tree: "+tree.toStringTree(parser));
			processNode(tree);
		}
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
			processSegment(node);
		}
		
		if (nodeCat.equals("sequence")) {
			processSequence(node);
		}

		if (nodeCat.equals("emptyTokenSequence")) {
			processEmptyTokenSequence(node);
		}

		if (nodeCat.equals("emptyTokenSequenceClass")) {
			processEmptyTokenSequenceClass(node);
		}

		if (nodeCat.equals("token")) {
			processToken(node);
		}

		if (nodeCat.equals("alignment")) {
			processAlignment(node);
		}

		if (nodeCat.equals("span")) {
			processSpan(node);
		}

		if (nodeCat.equals("disjunction")) {
			processDisjunction(node);
		}

		if (nodeCat.equals("position")) {
			processPosition(node);
		}

		if (nodeCat.equals("relation")) {
			processRelation(node);
		}

		if (nodeCat.equals("spanclass")) {
			processSpanclass(node);
		}

		if (nodeCat.equals("matching")) {
			processMatching(node);
		}

		if (nodeCat.equals("submatch")) {
			processSubmatch(node);
		}

		if (nodeCat.equals("meta")) {
			processMeta(node);
		}

		if (nodeCat.equals("within") && !getNodeCat(node.getParent()).equals("position")) {
			processWithin(node);
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

	private void processSegment(ParseTree node) {
		// Cover possible quantification (i.e. repetition) of segment
		ParseTree quantification = getFirstChildWithCat(node, "repetition");
		if (quantification != null) {
			LinkedHashMap<String,Object> quantGroup = makeGroup("repetition");
			Integer[] minmax = parseRepetition(quantification);
			quantGroup.put("boundary", makeBoundary(minmax[0], minmax[1]));
			if (minmax[0] != null) quantGroup.put("min", minmax[0]);
			if (minmax[1] != null) quantGroup.put("max", minmax[1]);
			addMessage(StatusCodes.DEPRECATED_QUERY_ELEMENT, "Deprecated 2014-07-24: 'min' and 'max' to be " +
					"supported until 3 months from deprecation date.");
			putIntoSuperObject(quantGroup);
			objectStack.push(quantGroup);
			stackedObjects++;
		}
	}

	private void processSequence(ParseTree node) {
		LinkedHashMap<String,Object> sequence = makeGroup("sequence");
		ParseTree distanceNode = getFirstChildWithCat(node, "distance");

		if (distanceNode!=null) {
			Integer[] minmax = parseDistance(distanceNode);
			LinkedHashMap<String,Object> distance = makeDistance("w", minmax[0], minmax[1]);
			sequence.put("inOrder", true);
			ArrayList<Object> distances = new ArrayList<Object>();
			distances.add(distance);
			sequence.put("distances", distances);
			visited.add(distanceNode.getChild(0)); // don't re-visit the emptyTokenSequence node
		}
		putIntoSuperObject(sequence);
		objectStack.push(sequence);
		stackedObjects++;
	}

	@SuppressWarnings("unchecked")
	/**
	 * empty tokens at beginning/end of sequence
	 * @param node
	 */
	private void processEmptyTokenSequence(ParseTree node) {
		Integer[] minmax = parseEmptySegments(node);
		// object will be either a repetition group or a single empty token
		LinkedHashMap<String,Object> object; 
		LinkedHashMap<String,Object> emptyToken = makeToken();
		if (minmax[0] != 1 || minmax[1] == null || minmax[1] != 1) {
			object = makeRepetition(minmax[0], minmax[1]);
			((ArrayList<Object>) object.get("operands")).add(emptyToken);
		} else {
			object = emptyToken;
		}
		putIntoSuperObject(object);
		objectStack.push(object);
		stackedObjects++;
	}

	private void processEmptyTokenSequenceClass(ParseTree node) {
		int classId = 1;
		if (hasChild(node, "spanclass_id")) {
			classId = Integer.parseInt(node.getChild(1).getChild(0).toStringTree(parser));
		}
		LinkedHashMap<String,Object> classGroup = makeSpanClass(classId, false);
		putIntoSuperObject(classGroup);
		objectStack.push(classGroup);
		stackedObjects++;
	}

	private void processToken(ParseTree node) {
		LinkedHashMap<String,Object> token = makeToken();
		// handle negation
		List<ParseTree> negations = getChildrenWithCat(node, "!");
		boolean negated = false;
		boolean isRegex = false;
		if (negations.size() % 2 == 1) negated = true;
		if (getNodeCat(node.getChild(0)).equals("key")) {
			// no 'term' child, but direct key specification: process here
			LinkedHashMap<String,Object> term = makeTerm();

			String key = node.getChild(0).getText();
			if (getNodeCat(node.getChild(0).getChild(0)).equals("regex")) {
				isRegex = true;
				term.put("type", "type:regex");
				key = key.substring(1,key.length()-1);
			}
			term.put("layer", "orth");
			term.put("key", key);
			String matches = negated ? "ne" : "eq";
			term.put("match", "match:"+matches);
			ParseTree flagNode = getFirstChildWithCat(node, "flag");
			if (flagNode != null) {
				// substring removes leading slash '/'
				String flag = getNodeCat(flagNode.getChild(0)).substring(1);
				if (flag.contains("i")) term.put("caseInsensitive", true);
				else if (flag.contains("I")) term.put("caseInsensitive", false);
				if (flag.contains("x")) {
					term.put("type", "type:regex");
					if (!isRegex) {
						key = QueryUtils.escapeRegexSpecialChars(key); 
					}
					term.put("key", ".*?"+key+".*?"); // overwrite key
				}
			}
			token.put("wrap", term);
		} else {
			// child is 'term' or 'termGroup' -> process in extra method 
			LinkedHashMap<String,Object> termOrTermGroup = 
					parseTermOrTermGroup(node.getChild(1), negated);
			token.put("wrap", termOrTermGroup);
		}
		putIntoSuperObject(token);
		visited.add(node.getChild(0));
		visited.add(node.getChild(2));
	}
	

	@SuppressWarnings("unchecked")
	private void processAlignment(ParseTree node) {
		LinkedHashMap<String,Object> alignClass = makeSpanClass(++classCounter,false);
		LinkedHashMap<String,Object> metaMap = (LinkedHashMap<String, Object>) requestMap.get("meta");
		if (metaMap.containsKey("alignment")) {
			ArrayList<Integer> alignedClasses = new ArrayList<Integer>();
			try {
				alignedClasses = (ArrayList<Integer>) metaMap.get("alignment"); 
			} catch (ClassCastException cce) {
				alignedClasses.add((Integer) metaMap.get("alignment"));
			}
			alignedClasses.add(classCounter);
			metaMap.put("alignment", alignedClasses);
		} else {
			metaMap.put("alignment", classCounter);
		}

		putIntoSuperObject(alignClass);
		objectStack.push(alignClass);
		stackedObjects++;
	}

	private void processSpan(ParseTree node) {
		List<ParseTree> negations = getChildrenWithCat(node, "!");
		boolean negated = false;
		if (negations.size() % 2 == 1) negated = true;
		LinkedHashMap<String,Object> span = makeSpan();
		ParseTree keyNode = getFirstChildWithCat(node, "key");
		ParseTree layerNode = getFirstChildWithCat(node, "layer");
		ParseTree foundryNode = getFirstChildWithCat(node, "foundry");
		ParseTree termOpNode = getFirstChildWithCat(node, "termOp");
		ParseTree termNode = getFirstChildWithCat(node, "term");
		ParseTree termGroupNode = getFirstChildWithCat(node, "termGroup");
		if (foundryNode != null) span.put("foundry", foundryNode.getText());
		if (layerNode != null) {
			String layer = layerNode.getText();
			if (layer.equals("base")) layer="lemma";
			span.put("layer", layer);
		}
		span.put("key", keyNode.getText());
		if (termOpNode != null) {
			String termOp = termOpNode.getText();
			if (termOp.equals("==")) span.put("match", "match:eq");
			else if (termOp.equals("!=")) span.put("match", "match:ne");
		}
		if (termNode != null) {
			LinkedHashMap<String,Object> termOrTermGroup = 
					parseTermOrTermGroup(termNode, negated, "span");
			span.put("attr", termOrTermGroup);
		}
		if (termGroupNode != null) {
			LinkedHashMap<String,Object> termOrTermGroup = 
					parseTermOrTermGroup(termGroupNode, negated, "span");
			span.put("attr", termOrTermGroup);
		}
		putIntoSuperObject(span);
		objectStack.push(span);
		stackedObjects++;
	}

	private void processDisjunction(ParseTree node) {
		LinkedHashMap<String,Object> disjunction = makeGroup("or");
		putIntoSuperObject(disjunction);
		objectStack.push(disjunction);
		stackedObjects++;
	}

	private void processPosition(ParseTree node) {
		LinkedHashMap<String,Object> position = parseFrame(node.getChild(0));
		putIntoSuperObject(position);
		objectStack.push(position);
		stackedObjects++;
	}

	private void processRelation(ParseTree node) {
		LinkedHashMap<String, Object> relationGroup = makeGroup("relation");
		LinkedHashMap<String, Object> relation = makeRelation();
		relationGroup.put("relation", relation);
		if (node.getChild(0).getText().equals("dominates")) {
			relation.put("layer", "c");
		}
		ParseTree relSpec = getFirstChildWithCat(node, "relSpec");
		ParseTree repetition = getFirstChildWithCat(node, "repetition");
		if (relSpec != null) {
			ParseTree foundry = getFirstChildWithCat(relSpec, "foundry");
			ParseTree layer = getFirstChildWithCat(relSpec, "layer");
			ParseTree key = getFirstChildWithCat(relSpec, "key");
			if (foundry != null) relation.put("foundry", foundry.getText());
			if (layer != null) relation.put("layer", layer.getText());
			if (key != null) relation.put("key", key.getText());
		}
		if (repetition != null) {
			Integer[] minmax =  parseRepetition(repetition);
			relation.put("boundary", makeBoundary(minmax[0], minmax[1]));
		}
		putIntoSuperObject(relationGroup);
		objectStack.push(relationGroup);
		stackedObjects++;
	}

	private void processSpanclass(ParseTree node) {
		// Step I: get info
		int classId = 1;
		if (getNodeCat(node.getChild(1)).equals("spanclass_id")) {
			String ref = node.getChild(1).getChild(0).toStringTree(parser);
			try {
				classId = Integer.parseInt(ref);
			} catch (NumberFormatException e) {
				String msg = "The specified class reference in the " +
						"focus/split-Operator is not a number: " + ref;
				log.error(msg);
				addError(StatusCodes.UNDEFINED_CLASS_REFERENCE, msg);
			}
			// only allow class id up to 127
			if (classId > 127) {
				addWarning("Only class IDs up to 127 are allowed. Your class "+classId+" has been set back to 127. "
						+ "Check for possible conflict with other classes.");
				classId = 127;
			}
		}
		LinkedHashMap<String, Object> classGroup = makeSpanClass(classId, false);
		putIntoSuperObject(classGroup);
		objectStack.push(classGroup);
		stackedObjects++;
		
	}

	private void processMatching(ParseTree node) {
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
						// only allow class id up to 127
						if (classRef > 127) {
							addWarning("Only class references up to 127 are allowed. Your reference to class "+classRef+" has been set back to 127. "
									+ "Check for possible conflict with other classes.");
							classRef = 127;
						}
						classRefs.add(classRef);
					} catch (NumberFormatException e) {
						String err = "The specified class reference in the " +
								"shrink/split-Operator is not a number.";
						addError(StatusCodes.UNDEFINED_CLASS_REFERENCE, err);
					}
				}
			}
		} else {
			classRefs.add(1);
		}
		LinkedHashMap<String, Object> referenceGroup = makeReference(classRefs);

		String type = node.getChild(0).toStringTree(parser);
		// Default is focus(), if deviating catch here
		if (type.equals("split")) referenceGroup.put("operation", "operation:split");
		if (type.equals("submatch") || type.equals("shrink")) {
			String warning = "Deprecated 2014-07-24: "+type + "() as a match reducer " +
					"to a specific class is deprecated in favor of focus() and will " +
					"only be supported for 3 months after deprecation date.";
			addMessage(StatusCodes.DEPRECATED_QUERY_ELEMENT, warning);
		}
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

	private void processSubmatch(ParseTree node) {
		LinkedHashMap<String,Object> submatch = makeReference(null);
		submatch.put("operands", new ArrayList<Object>());
		ParseTree startpos = getFirstChildWithCat(node,"startpos");
		ParseTree length = getFirstChildWithCat(node,"length");
		ArrayList<Integer> spanRef = new ArrayList<Integer>();
		spanRef.add(Integer.parseInt(startpos.getText()));
		if (length != null) {
			spanRef.add(Integer.parseInt(length.getText()));
		}
		submatch.put("spanRef", spanRef);
		putIntoSuperObject(submatch);
		objectStack.push(submatch);
		stackedObjects++;
		visited.add(node.getChild(0));
	}

	/**
	 * Creates meta field in requestMap, later filled by terms
	 * @param node
	 */
	private void processMeta(ParseTree node) {
		LinkedHashMap<String, Object> metaFilter = new LinkedHashMap<String, Object>();
		requestMap.put("meta", metaFilter);
		metaFilter.put("@type", "korap:meta");
	}

	/**
	 * NB: requires that parent is not 'position'!
	 * @param node
	 */
	private void processWithin(ParseTree node) {
		ParseTree domainNode = node.getChild(2);
		String domain = getNodeCat(domainNode);
		LinkedHashMap<String, Object> curObject = 
				(LinkedHashMap<String, Object>) objectStack.getFirst();
		curObject.put("within", domain);
		visited.add(node.getChild(0));
		visited.add(node.getChild(1));
		visited.add(domainNode);
	}

	/**
	 * Parses a repetition node
	 * @param node
	 * @return A two-element array, of which the first element is an int representing 
	 * the minimal number of repetitions of the quantified element, and the second 
	 * element representing the maximal number of repetitions 
	 */
	private Integer[] parseRepetition(ParseTree node) {
		Integer min = 0, max = 0;
		boolean maxInfinite = false;
		// (repetition) node can be of two types: 'kleene' or 'range'
		ParseTree repetitionTypeNode = node.getChild(0);
		String repetitionType = getNodeCat(repetitionTypeNode);
		if (repetitionType.equals("kleene")) {
			// kleene operators (+ and *) as well as optionality (?)
			String kleeneOp = repetitionTypeNode.getText();
			if (kleeneOp.equals("*")) {
				maxInfinite = true;
			} else if (kleeneOp.equals("+")) {
				min = 1;
				maxInfinite = true;
			} if (kleeneOp.equals("?")) {
				max = 1;
			}
		} else {
			// Range node of form "{ min , max }" or "{ max }" or "{ , max }"  or "{ min , }"
			ParseTree minNode = getFirstChildWithCat(repetitionTypeNode, "min");
			ParseTree maxNode = getFirstChildWithCat(repetitionTypeNode, "max");
			if (maxNode!=null) max = Integer.parseInt(maxNode.getText());
			else maxInfinite = true;
			// min is optional: if not specified, min = max
			if (minNode!=null) min = Integer.parseInt(minNode.getText());
			else if (hasChild(repetitionTypeNode, ",")) min = 0;
			else {
				min = max;
//				warnings.add("Your query contains a segment of the form {n}, where n is some number. This expression is ambiguous. "
//						+ "It could mean a repetition (\"Repeat the previous element n times!\") or a word form that equals the number, "
//						+ "enclosed by a \"class\" (which is denoted by braces like '{x}', see the documentation on classes)."
//						+ "KorAP has by default interpreted the segment as a repetition statement. If you want to express the"
//						+ "number as a word form inside a class, use the non-shorthand form {[orth=n]}.");
			}
		}
		if (maxInfinite) {
			max = null;
		}
		return new Integer[]{min,max};
	}

	private LinkedHashMap<String,Object> parseFrame(ParseTree node) {
		String operator = node.toStringTree(parser).toLowerCase();
		String[] frames = new String[]{""};
		String[] classRefCheck = new String[]{"classRefCheck:includes"};
		switch (operator) {
		case "contains":
			frames = new String[]{"frames:contains"};
			break;
		case "matches":
			frames = new String[]{"frames:matches"};
			break;
		case "startswith":
			frames = new String[]{"frames:startswith"};
			break;
		case "endswith":
			frames = new String[]{"frames:endswith"};
			break;	
		case "overlaps":
			frames = new String[]{"frames:overlapsLeft","frames:overlapsRight"};
			classRefCheck = new String[]{"classRefCheck:intersects"};
			break;
		}
		return makePosition(frames,classRefCheck);
	}


	private LinkedHashMap<String, Object> parseTermOrTermGroup(
			ParseTree node, boolean negated) {
		return parseTermOrTermGroup(node, negated, "token");
	}

	/**
	 * Parses a (term) or (termGroup) node
	 * @param node
	 * @param negatedGlobal Indicates whether the term/termGroup is globally negated, e.g. through a negation 
	 * operator preceding the related token like "![base=foo]". Global negation affects the term's "match" parameter.
	 * @return A term or termGroup object, depending on input
	 */
	@SuppressWarnings("unchecked")
	private LinkedHashMap<String, Object> parseTermOrTermGroup(ParseTree node, boolean negatedGlobal, String mode) {
		String nodeCat = getNodeCat(node);
		if (nodeCat.equals("term")) {
			String key = null;
			LinkedHashMap<String,Object> term = makeTerm();
			// handle negation
			boolean negated = negatedGlobal;
			boolean isRegex = false;
			List<ParseTree> negations = getChildrenWithCat(node, "!");
			if (negations.size() % 2 == 1) negated = !negated;
			// retrieve possible nodes
			ParseTree keyNode = getFirstChildWithCat(node, "key");
			ParseTree valueNode = getFirstChildWithCat(node, "value");
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
				if (mode.equals("span")) term.put("key", layer);
				else term.put("layer", layer);
			}
			// process key: 'normal' or regex?
			key = keyNode.getText();
			if (getNodeCat(keyNode.getChild(0)).equals("regex")) {
				isRegex = true;
				term.put("type", "type:regex");
				key = key.substring(1, key.length()-1); // remove leading and trailing quotes
			}
			if (mode.equals("span")) term.put("value", key);
			else term.put("key", key);
			// process value
			if (valueNode != null) term.put("value", valueNode.getText());
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
				if (flag.contains("i")) term.put("caseInsensitive", true);
				else if (flag.contains("I")) term.put("caseInsensitive", false);
				if (flag.contains("x")) {
					if (!isRegex) {
						key = QueryUtils.escapeRegexSpecialChars(key); 
					}
					term.put("key", ".*?"+key+".*?");  // flag 'x' allows submatches: overwrite key with appended .*? 
					term.put("type", "type:regex");
				}
			}
			return term;
		} else if (nodeCat.equals("termGroup")) {
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
			ParseTree boolOp = getFirstChildWithCat(node, "boolOp"); 
			String operator = boolOp.getText().equals("&") ? "and" : "or";
			termGroup = makeTermGroup(operator);
			ArrayList<Object> operands = (ArrayList<Object>) termGroup.get("operands");
			// recursion with left/right operands
			operands.add(parseTermOrTermGroup(leftOp, negatedGlobal, mode));
			operands.add(parseTermOrTermGroup(rightOp, negatedGlobal, mode));
			return termGroup;
		}
		return null;
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

	/**
	 * Basically only increases the min and max counters as required by Poliqarp
	 * @param distanceNode
	 * @return
	 */
	private Integer[] parseDistance(ParseTree distanceNode) {
		int emptyTokenSeqIndex = getNodeCat(distanceNode).equals("distance") ? 0 : 2; 
		Integer[] minmax = parseEmptySegments(distanceNode.getChild(emptyTokenSeqIndex));
		Integer min = minmax[0];
		Integer max = minmax[1];
		min++;
		if (max != null) max++;
		//		min = cropToMaxValue(min);
		//		max = cropToMaxValue(max);
		return new Integer[]{min, max};
	}

	private Integer[] parseEmptySegments(ParseTree emptySegments) {
		Integer min = 0;
		Integer max = 0;
		ParseTree child;
		for (int i = 0; i < emptySegments.getChildCount(); i++) {
			child = emptySegments.getChild(i);
			ParseTree nextSibling = emptySegments.getChild(i + 1);
			if (child.toStringTree(parser).equals("(emptyToken [ ])")) {
				if (nextSibling != null && getNodeCat(nextSibling).equals("repetition")) {
					Integer[] minmax = parseRepetition(nextSibling);
					min += minmax[0];
					if (minmax[1] != null) {
						max += minmax[1];
					} else {
						max = null;
					}
				} else {
					min++;
					max++;
				}
			}
		}
		//		min = cropToMaxValue(min);
		//		max = cropToMaxValue(max);
		return new Integer[]{min, max};
	}


	private ParserRuleContext parsePoliqarpQuery(String query) throws QueryException {
		Lexer lexer = new PoliqarpPlusLexer((CharStream) null);
		ParserRuleContext tree = null;
		Antlr4DescriptiveErrorListener errorListener = new Antlr4DescriptiveErrorListener(query);
		// Like p. 111
		try {
			// Tokenize input data
			ANTLRInputStream input = new ANTLRInputStream(query);
			lexer.setInputStream(input);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			parser = new PoliqarpPlusParser(tokens);

			// Don't throw out erroneous stuff
			parser.setErrorHandler(new BailErrorStrategy());
			lexer.removeErrorListeners();
            lexer.addErrorListener(errorListener);
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);

			// Get starting rule from parser
			Method startRule = PoliqarpPlusParser.class.getMethod("request");
			tree = (ParserRuleContext) startRule.invoke(parser, (Object[]) null);
		}
		// Some things went wrong ...
		catch (Exception e) {
			log.error("Could not parse query. Please make sure it is well-formed.");
			log.error(errorListener.generateFullErrorMsg().toString());
			addError(errorListener.generateFullErrorMsg());
		}
		return tree;
	}
}
