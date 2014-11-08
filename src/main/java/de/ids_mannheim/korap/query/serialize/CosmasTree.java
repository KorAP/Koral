package de.ids_mannheim.korap.query.serialize;

import de.ids_mannheim.korap.query.cosmas2.c2psLexer;
import de.ids_mannheim.korap.query.cosmas2.c2psParser;
import de.ids_mannheim.korap.query.serialize.util.CosmasCondition;
import de.ids_mannheim.korap.query.serialize.util.ResourceMapper;
import de.ids_mannheim.korap.util.QueryException;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Map representation of CosmasII syntax tree as returned by ANTLR
 *
 * @author bingel
 * @version 0.2
 */
public class CosmasTree extends Antlr3AbstractSyntaxTree {

	private static Logger log = LoggerFactory.getLogger(CosmasTree.class);

	LinkedList<LinkedHashMap[]> toWrapStack = new LinkedList<LinkedHashMap[]>();
	/**
	 * Field for repetition query (Kleene + or * operations, or min/max queries: {2,4}
	 */
	String repetition = "";
	/**
	 * Global control structure for fieldGroups, keeps track of open fieldGroups.
	 */
	LinkedList<ArrayList<Object>> openFieldGroups = new LinkedList<ArrayList<Object>>();
	/**
	 * Keeps track of how many toWrap objects there are to pop after every recursion of {@link #processNode(ParseTree)}
	 */
	LinkedList<Integer> toWrapsToPop = new LinkedList<Integer>();
	/**
	 * Flag that indicates whether token fields or meta fields are currently being processed
	 */
	boolean inMeta = false;
	/**
	 * If true, a sequence (OPPROX node) is governed by an OPALL node (ALL()-operator), which requires to match
	 * all tokens of the sequence.
	 */
	boolean inOPALL = false;
	boolean inOPNHIT = false;
	/**
	 *
	 */
	int classCounter = 1;
	boolean negate = false;

	/**
	 * Allows for the definition of objects to be wrapped around the arguments of an operation.
	 * Each record in the table holds the parent node of the argument, the number of the argument 
	 * and an object in whose operands list the argument shall be wrapped.
	 */
	Table<Tree,Integer,LinkedHashMap<String,Object>> operandWrap = HashBasedTable.create();

	/**
	 * Keeps track of all visited nodes in a tree
	 */
	List<Tree> visited = new ArrayList<Tree>();

	Integer stackedToWrap = 0;
	/**
	 * A list of node categories that can be sequenced (i.e. which can be in a sequence with any number of other nodes in this list)
	 */
	private final List<String> sequentiableCats = Arrays.asList(new String[]{"OPWF", "OPLEM", "OPMORPH", "OPBEG", "OPEND", "OPIN", "OPBED", "OPELEM"});
	/**
	 * Keeps track of sequenced nodes, i.e. nodes that implicitly govern  a sequence, as in (C2PQ (OPWF der) (OPWF Mann)).
	 * This is necessary in order to know when to take the sequence off the object stack, as the sequence is introduced by the
	 * first child but cannot be closed after this first child in order not to lose its siblings
	 */
	private LinkedList<Tree> sequencedNodes = new LinkedList<Tree>();

	private boolean nodeHasSequentiableSiblings;

	/**
	 * Keeps track of operands lists that are to be serialised in an inverted
	 * order (e.g. the IN() operator) compared to their AST representation.
	 */
	private LinkedList<ArrayList<Object>> invertedOperandsLists = new LinkedList<ArrayList<Object>>();
	/**
	 * @param tree   The syntax tree as returned by ANTLR
	 * @param parser The ANTLR parser instance that generated the parse tree
	 * @throws QueryException
	 */
	public CosmasTree(String query) throws QueryException {
		this.query = query;
		process(query);
		log.info(">>> " + requestMap.get("query") + " <<<");
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
		log.info("Processing CosmasII query");
		System.out.println("Processing Cosmas");
		processNode(tree);
		log.info(requestMap.toString());
	}

	@SuppressWarnings("unchecked")
	private void processNode(Tree node) throws QueryException {

		// Top-down processing
		if (visited.contains(node)) return;
		else visited.add(node);


		String nodeCat = getNodeCat(node);
		openNodeCats.push(nodeCat);

		stackedObjects = 0;
		stackedToWrap = 0;

		if (verbose) {
			System.err.println(" " + objectStack);
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
			if (parent.getChildCount() > 1) {
				// if node is first child of parent...
				if (node == parent.getChild(0)) {
					nodeHasSequentiableSiblings = false;
					for (int i = 1; i < parent.getChildCount(); i++) {
						if (sequentiableCats.contains(getNodeCat(parent.getChild(i)))) {
							nodeHasSequentiableSiblings = true;
							continue;
						}
					}
					if (nodeHasSequentiableSiblings) {
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
			// check for wildcard string
			Pattern p = Pattern.compile("[+*?]");
			Matcher m = p.matcher(value);
			if (m.find()) fieldMap.put("type", "type:wildcard");

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
			if (!hasChild(node, "TPOS")) {
				putIntoSuperObject(token, 1);
				visited.add(node.getChild(0));
			} else {
				// TODO
			}
		}

		if (nodeCat.equals("OPMORPH")) {
			//Step I: get info
			String[] morphterms = node.getChild(0).toStringTree().split("&");
			LinkedHashMap<String, Object> token = new LinkedHashMap<String, Object>();
			token.put("@type", "korap:token");
			ArrayList<Object> terms = new ArrayList<Object>();
			LinkedHashMap<String, Object> fieldMap = null;
			for (String morphterm : morphterms) {
				fieldMap = new LinkedHashMap<String, Object>();
				fieldMap.put("@type", "korap:term");
				String[] attrval = morphterm.split("=");
				if (attrval.length == 1) {
					fieldMap.put("key", morphterm);
				} else {
					if (attrval[0].endsWith("!")) {
						negate = !negate;
						attrval[0] = attrval[0].replace("!", "");
					}
					String[] foundrylayer = attrval[0].split("/");
					//     			fieldMap.put("key", "morph:"+node.getChild(0).toString().replace(" ", "_"));
					fieldMap.put("key", attrval[1]);
					if (foundrylayer.length==1) {
						fieldMap.put("layer", foundrylayer[0]);
					} else {
						fieldMap.put("foundry", foundrylayer[0]);
						fieldMap.put("layer", foundrylayer[1]);
					}
				}


				// make category-specific fieldMap entry
				// negate field (see above)
				if (negate) {
					fieldMap.put("match", "match:ne");
				} else {
					fieldMap.put("match", "match:eq");
				}
				terms.add(fieldMap);
			}
			if (morphterms.length == 1) {
				token.put("wrap", fieldMap);
			} else {
				LinkedHashMap<String, Object> termGroup = makeTermGroup("and");
				termGroup.put("operands", terms);
				token.put("wrap", termGroup);
			}
			//Step II: decide where to put
			putIntoSuperObject(token, 0);
			visited.add(node.getChild(0));
		}

		if (nodeCat.equals("OPELEM")) {
			// Step I: create element
			LinkedHashMap<String, Object> span = makeSpan();
			if (node.getChild(0).toStringTree().equals("EMPTY")) {

			} else {
				int elname = 0;
				Tree elnameNode = getFirstChildWithCat(node, "ELNAME");
				if (elnameNode != null) {
					span.put("key", elnameNode.getChild(0).toStringTree().toLowerCase());
					elname = 1;
				}
				if (node.getChildCount() > elname) {
					/*
					 * Attributes can carry several values, like #ELEM(W ANA != 'N V'), 
					 * denoting a word whose POS is neither N nor V.
					 * When seeing this, create a sub-termGroup and put it into the top-level
					 * term group, but only if there are other attributes in that group. If
					 * not, put the several values as distinct attr-val-pairs into the
					 * top-level group (in order to avoid a top-level group that only
					 * contains a sub-group).
					 */
					LinkedHashMap<String, Object> termGroup = makeTermGroup("and");
					ArrayList<Object> termGroupOperands = (ArrayList<Object>) termGroup.get("operands");
					for (int i = elname; i < node.getChildCount(); i++) {
						Tree attrNode = node.getChild(i);
						if (attrNode.getChildCount() == 2) {
							LinkedHashMap<String, Object> term = makeTerm();
							termGroupOperands.add(term);
							String layer = attrNode.getChild(0).toStringTree();
							String[] splitted = layer.split("/");
							if (splitted.length > 1) {
								term.put("foundry", splitted[0]);
								layer = splitted[1];
							}
							term.put("layer", translateMorph(layer));
							term.put("key", attrNode.getChild(1).toStringTree());
							String match = getNodeCat(attrNode).equals("EQ") ? "eq" : "ne";
							term.put("match", "match:" + match);
						} else {
							LinkedHashMap<String, Object> subTermGroup = makeTermGroup("and");
							ArrayList<Object> subTermGroupOperands = (ArrayList<Object>) subTermGroup.get("operands");
							int j;
							for (j = 1; j < attrNode.getChildCount(); j++) {
								LinkedHashMap<String, Object> term = makeTerm();
								String layer = attrNode.getChild(0).toStringTree();
								String[] splitted = layer.split("/");
								if (splitted.length > 1) {
									term.put("foundry", splitted[0]);
									layer = splitted[1];
								}
								term.put("layer", translateMorph(layer));
								term.put("key", attrNode.getChild(j).toStringTree());
								String match = getNodeCat(attrNode).equals("EQ") ? "eq" : "ne";
								term.put("match", "match:" + match);
								if (node.getChildCount() == elname + 1) {
									termGroupOperands.add(term);
								} else {
									subTermGroupOperands.add(term);
								}
							}
							if (node.getChildCount() > elname + 1) {
								termGroupOperands.add(subTermGroup);
							}
						}
						if (getNodeCat(attrNode).equals("NOTEQ")) negate = true;
					}
					span.put("attr", termGroup);
				}
			}

			//Step II: decide where to put
			putIntoSuperObject(span);
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
			zerodistance.put("@type", "cosmas:distance");
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
			// collect info
			Tree prox_opts = node.getChild(0);
			Tree typ = prox_opts.getChild(0);
			Tree dist_list = prox_opts.getChild(1);
			// Step I: create group
			LinkedHashMap<String, Object> group = makeGroup("sequence");

			ArrayList<Object> constraints = new ArrayList<Object>();
			boolean exclusion = typ.getChild(0).toStringTree().equals("EXCL");

			boolean inOrder = false;
			boolean invertedOperands = false;

			group.put("inOrder", inOrder);
			group.put("distances", constraints);

			boolean putIntoOverlapDisjunction = false;

			int min = 0, max = 0;
			// possibly several distance constraints
			for (int i = 0; i < dist_list.getChildCount(); i++) {
				String direction = dist_list.getChild(i).getChild(0).getChild(0).toStringTree().toLowerCase();
				String minStr = dist_list.getChild(i).getChild(1).getChild(0).toStringTree();
				String maxStr = dist_list.getChild(i).getChild(1).getChild(1).toStringTree();
				String meas = dist_list.getChild(i).getChild(2).getChild(0).toStringTree();
				if (minStr.equals("VAL0")) {
					minStr = "0";
				}
				min = Integer.parseInt(minStr);
				max = Integer.parseInt(maxStr);
				// If zero word-distance, wrap this sequence in a disjunction along with an overlap position
				// between the two operands
				/*   
     	XXX: This is currently deactivated. Uncomment to activate treatment of zero-word distances as overlap-alternatives
     			(see google doc on special distances serialization)

                if (meas.equals("w") && min == 0) {
                	min = 1;
                	putIntoOverlapDisjunction = true;
                }
				 */
				if (!meas.equals("w") && min == 0 ) {
					processSpanDistance(meas,min,max);
				}
				LinkedHashMap<String, Object> distance = makeDistance(meas,min,max);
				if (exclusion) {
					distance.put("exclude", true);
				}
				//                if (! openNodeCats.get(1).equals("OPNHIT")) {
				constraints.add(distance);
				//                }
				if (i==0) {
					if (direction.equals("plus")) {
						inOrder = true;
					} else if (direction.equals("minus")) {
						inOrder = true;
						invertedOperands = true;
					} else if (direction.equals("both")) {
						inOrder = false;
					}
				}
			}
			group.put("inOrder", inOrder);
			LinkedHashMap<String, Object> embeddedSequence = group;

			if (! (openNodeCats.get(1).equals("OPBEG") || openNodeCats.get(1).equals("OPEND") || inOPALL || openNodeCats.get(1).equals("OPNHIT"))) {
				wrapOperandInClass(node,1,classCounter);
				wrapOperandInClass(node,2,classCounter);
				group = wrapInReference(group, 128+classCounter++);
			} else if (openNodeCats.get(1).equals("OPNHIT")) {
				LinkedHashMap<String,Object> repetition = makeRepetition(min, max);
				((ArrayList<Object>) repetition.get("operands")).add(makeToken());
				// TODO go on with this: put the repetition into a class and put it in between the operands
				// -> what if there's several distance constraints. with different keys, like /w4,s0? 
			}

			LinkedHashMap<String,Object> sequence = null;
			if (putIntoOverlapDisjunction) {
				sequence = embeddedSequence;
				group = makeGroup("or");
				ArrayList<Object> disjOperands = (ArrayList<Object>) group.get("operands");
				String[] sharedClasses = new String[]{"intersects"};
				LinkedHashMap<String,Object> overlapsGroup = makePosition(new String[0], sharedClasses);

				ArrayList<Object> overlapsOperands = (ArrayList<Object>) overlapsGroup.get("operands");
				// this ensures identity of the operands lists and thereby a distribution of the operands for both created objects 
				sequence.put("operands", overlapsOperands);
				if (invertedOperands) {
					invertedOperandsLists.push(overlapsOperands);
				}
				disjOperands.add(overlapsGroup);
				disjOperands.add(wrapInReference(sequence, 0));
				// Step II: decide where to put
				putIntoSuperObject(group, 0);
				objectStack.push(sequence);
			}
			else {
				if (invertedOperands) {
					ArrayList<Object> operands = (ArrayList<Object>) embeddedSequence.get("operands");
					invertedOperandsLists.push(operands);
				}
				// Step II: decide where to put
				putIntoSuperObject(group, 0);
				objectStack.push(embeddedSequence);
			}
			stackedObjects++;
			visited.add(node.getChild(0));
		}

		// inlcusion or overlap
		if (nodeCat.equals("OPIN") || nodeCat.equals("OPOV")) {
			// Step I: create group
			wrapOperandInClass(node,2,classCounter++);
			wrapOperandInClass(node,1,classCounter++);
			//            LinkedHashMap<String, Object> posgroup = makePosition(null);
			LinkedHashMap<String, Object> posgroup = makeGroup("position");
			LinkedHashMap<String, Object> positionOptions;
			//            posgroup
			if (nodeCat.equals("OPIN")) {
				positionOptions = parseOPINOptions(node);
			} else {
				positionOptions = parseOPOVOptions(node);
			}
			posgroup.put("frames", positionOptions.get("frames"));
			posgroup.put("frame", positionOptions.get("frame"));
			if (positionOptions.containsKey("exclude")) {
				posgroup.put("exclude", positionOptions.get("exclude"));
			}
			if (positionOptions.containsKey("grouping")) {
				posgroup.put("grouping", positionOptions.get("grouping"));
			}
			objectStack.push(posgroup);
			// mark this an inverted operands object
			invertedOperandsLists.push((ArrayList<Object>) posgroup.get("operands"));
			stackedObjects++;
			// Step II: wrap in reference and decide where to put
			ArrayList<String> check = (ArrayList<String>) positionOptions.get("classRefCheck");
			Integer[] classIn = new Integer[]{128+classCounter-2,128+classCounter-1};
			LinkedHashMap<String, Object> classRefCheck = makeClassRefCheck(check, classIn, 128+classCounter);
			((ArrayList<Object>) classRefCheck.get("operands")).add(posgroup);
			LinkedHashMap<String, Object> focusGroup = null;
			if ((boolean) positionOptions.get("matchall") == true) {
				focusGroup = makeResetReference();
				((ArrayList<Object>) focusGroup.get("operands")).add(classRefCheck);
			} else { // match only first argument
				focusGroup = wrapInReference(classRefCheck, 128+classCounter-1);
			}
			putIntoSuperObject(focusGroup, 1);
		}

		// Wrap the argument of an #IN operator in a previously defined container
		if (nodeCat.equals("ARG1") || nodeCat.equals("ARG2"))  {
			Tree parent = node.getParent();
			//        	String child = getNodeCat(node.getChild(0));
			//        	if (child.equals("OPWF") | child.equals("OPLEM") | child.equals("OPELEM") | child.equals("OPMOPRH") | child.equals("OPLABEL")) {
			if (operandWrap.containsRow(parent)) {
				// Step I: create group
				int argNr = nodeCat.equals("ARG1") ? 1 : 2;
				LinkedHashMap<String,Object> container = operandWrap.row(parent).get(argNr);
				// Step II: ingest
				if (container!=null) {
					objectStack.push(container);
					stackedObjects++;
					putIntoSuperObject(container,1);
				}
			}
			//        	}
		}

		if (nodeCat.equals("OPALL")) {
			inOPALL = true;
		}

		if (nodeCat.equals("OPNHIT")) {
			Integer[] classRef = new Integer[]{128+classCounter+1, 128+classCounter+2}; 
			//            classRef.add(classCounter + 1);  // yes, do this twice (two classes)!
			LinkedHashMap<String, Object> group = makeReference(128+classCounter);
			LinkedHashMap<String, Object> classRefCheck = makeClassRefOp("classRefOp:inversion", classRef, classCounter+128);
			ArrayList<Object> operands = new ArrayList<Object>();
			operands.add(classRefCheck);
			group.put("operands", operands);
			classCounter++;
			wrapOperandInClass(node.getChild(0),1,classCounter++); // direct child is OPPROX
			wrapOperandInClass(node.getChild(0),2,classCounter++);
			objectStack.push(classRefCheck);
			stackedObjects++;
			putIntoSuperObject(group, 1);
		}

		if (nodeCat.equals("OPEND") || nodeCat.equals("OPBEG")) {
			// Step I: create group
			LinkedHashMap<String, Object> beggroup = new LinkedHashMap<String, Object>();
			beggroup.put("@type", "korap:reference");
			beggroup.put("operation", "operation:focus");
			ArrayList<Integer> spanRef = new ArrayList<Integer>();
			if (nodeCat.equals("OPBEG")) {
				spanRef.add(0);
				spanRef.add(1);
			} else if (nodeCat.equals("OPEND")) {
				spanRef.add(-1);
				spanRef.add(1);
			}
			beggroup.put("spanRef", spanRef);
			beggroup.put("operands", new ArrayList<Object>());
			objectStack.push(beggroup);
			stackedObjects++;

			// Step II: decide where to put
			putIntoSuperObject(beggroup, 1);
		}

		if (nodeCat.equals("OPBED")) { 
			// Node structure is (OPBED X+ (OPTS (TPBEG tpos*) (TPEND tpos*)))   
			// X is some segment, TPBEG or TPEND must be present (inclusive OR)
			// tpos is a three-char string of the form "[+-]?[spt][ae]". s/p/t indicates span, a/e beginning/end, - means negation
			// See C-II QL documentation for more detail: 
			// http://www.ids-mannheim.de/cosmas2/win-app/hilfe/suchanfrage/eingabe-grafisch/syntax/textpositionen.html
			
			// Step I: create group
			int optsChild = node.getChildCount() - 1;
			Tree begConditions = getFirstChildWithCat(node.getChild(optsChild), "TPBEG");
			Tree endConditions = getFirstChildWithCat(node.getChild(optsChild), "TPEND");

			LinkedHashMap<String, Object> submatchgroup = makeReference(128+classCounter);
			ArrayList<Object> submatchOperands = new ArrayList<Object>();
			submatchgroup.put("operands", submatchOperands);
			putIntoSuperObject(submatchgroup);

			// Step II: collect all conditions, create groups for them in processPositionCondition()
			ArrayList<Object> distributedOperands = new ArrayList<Object>();
			ArrayList<LinkedHashMap<String, Object>> conditionGroups = new ArrayList<LinkedHashMap<String, Object>>(); 
			if (begConditions != null) {
				for (Tree condition : getChildren(begConditions)) {
					conditionGroups.add(processPositionCondition(condition, distributedOperands, "beg"));
				}
			}
			if (endConditions != null) {
				for (Tree condition : getChildren(endConditions)) {
					conditionGroups.add(processPositionCondition(condition, distributedOperands, "end"));
				}
			}
			// Step III: insert conditions. need to stack matches-groups because position groups may only have two operands
			ArrayList<Object> currentLowestOperands = submatchOperands; // indicates where to insert next condition group
			int conditionCount = 0;
			for (LinkedHashMap<String,Object> conditionGroup : conditionGroups) {
				conditionCount++;
				if (conditionGroups.size()==1) {
					submatchOperands.add(conditionGroup);
				} else if (conditionCount < conditionGroups.size()) {
					LinkedHashMap<String,Object> matchesGroup = makePosition(new String[]{"frames:matches"}, new String[0]);
					ArrayList<Object> matchesOperands = (ArrayList<Object>) matchesGroup.get("operands");
					matchesOperands.add(conditionGroup);
					// matches groups that are embedded at the second or lower level receive an additional
					// focus to grep out only the query term to which the constraint applies
					if (conditionCount > 1) {
						LinkedHashMap<String,Object> focus = makeReference(128+classCounter-conditionGroups.size()+conditionCount-1);
						ArrayList<Object> focusOperands = new ArrayList<Object>();
						focus.put("operands", focusOperands);
						focusOperands.add(matchesGroup);
						currentLowestOperands.add(focus);
					} else {
						currentLowestOperands.add(matchesGroup);
					}
					currentLowestOperands = matchesOperands;
				} else {
					currentLowestOperands.add(conditionGroup);
				}
			}
		}
		objectsToPop.push(stackedObjects);
		toWrapsToPop.push(stackedToWrap);

		/*
		 ****************************************************************
		 **************************************************************** 
		 *  recursion until 'request' node (root of tree) is processed  *
		 ****************************************************************
		 ****************************************************************
		 */
		for (int i = 0; i < node.getChildCount(); i++) {
			Tree child = node.getChild(i);
			processNode(child);
		}

		/*
		 **************************************************************
		 * Stuff that happens after processing the children of a node *
		 **************************************************************
		 */

		// remove sequence from object stack if node is implicitly sequenced
		if (sequencedNodes.size() > 0) {
			if (node == sequencedNodes.getFirst()) {
				objectStack.pop();
				sequencedNodes.pop();
			}
		}

		for (int i = 0; i < objectsToPop.get(0); i++) {
			objectStack.pop();
		}
		objectsToPop.pop();


		//        if (!toWrapStack.isEmpty()) System.err.println(toWrapStack.get(0)[0]);
		for (int i = 0; i < toWrapsToPop.get(0); i++) {
			putIntoSuperObject(wrap(toWrapStack.pop()));
		}
		toWrapsToPop.pop();

		if (nodeCat.equals("ARG2") && openNodeCats.get(1).equals("OPNOT")) {
			negate = false;
		}

		if (nodeCat.equals("OPALL")) {
			inOPALL = false;
		}

		openNodeCats.pop();
	}

	private void processSpanDistance(String meas, int min, int max) {
		// Do stuff here in case we'll decide one day to treat span distances in a special way.
		// (see GDoc Special Distances Serialization)
	}

	/**
	 * Registers an entry in the {@link #operandWrap} table in order to allow an operator's arguments
	 * (or only one of them) to be wrapped in a class group.
	 * @param node The operator node (parent node of the ARG1/ARG2 node)
	 * @param arg The argument number (1 or 2)
	 * @param cls The class id.
	 */
	private void wrapOperandInClass(Tree node, int arg, int cls) {
		LinkedHashMap<String,Object> clsGroup = makeSpanClass(cls);
		wrapOperand(node,arg,clsGroup);
	}

	/**
	 * Registers an entry in the {@link #operandWrap} table in order to allow an operator's arguments
	 * (or only one of them) to be wrapped in an arbitrary object, e.g. a reference group.
	 * @param node The operator node (parent node of the ARG1/ARG2 node)
	 * @param arg The argument number (1 or 2)
	 * @param container The object in whose operand list the argument shall be wrapped.
	 */
	private void wrapOperand(Tree node, int arg, LinkedHashMap<String, Object> container) {
		operandWrap.put(node, arg, container);
	}

	private Object translateMorph(String layer) {
		// might be extended...
		if (layer.equals("ANA"))
			return ResourceMapper.descriptor2policy("ANA");
		else
			return layer;

	}

	@SuppressWarnings("unchecked")
	/**
	 * Processes individual position conditions as provided in the OPTS node under the OPBEG node.
	 * #BEG allows to specify position constrains that apply to the beginning or the end of the subquery X.
	 * E.g., in #BEG(X, tpos/tpos), the 'tpos' constraints before the slash indicate conditions that apply 
	 * to the beginning of X, those after the slash are conditions that apply to the end of X.
	 * See the official C-II documentation for more details. <br/><br/>
	 * What's important here is what follows: <br/>
	 * Assume the query #BED(der Mann, sa/pa). This means that <b>the beginning<b/> of "der Mann" stands at
	 * the beginning of a sentence and that <b>the end</b> (because this constraint comes after the slash) stands at the 
	 * beginning of a paragraph. The "end" means the last item, here "Mann", so this token comes at the beginning
	 * of a paragraph. To capture this, we choose spanRefs: The last item of X matches the first item of the span (here: P). 
	 * @param cond
	 * @param distributedOperands
	 * @param mode
	 * @return
	 */
	private LinkedHashMap<String, Object> processPositionCondition(Tree cond, ArrayList<Object> distributedOperands, String mode) {
		boolean negated = false;
		String elem; // the 'span' (s/p/t)
		String position = "frames:matches"; // default
		Integer[] elemSpanRef = null; // spanRef to be used for the element ('span')
		Integer[] hitSpanRef = null; // spanRef to be used for the subquery X 

		String nodeString = cond.toStringTree();
		if (nodeString.startsWith("-")) {
			negated = true;
			nodeString = nodeString.substring(1);
		} else if (nodeString.startsWith("+")) {
			nodeString = nodeString.substring(1);
		}

		elem = nodeString.substring(0, 1);
		nodeString = nodeString.substring(1);

		// in cases where the end of X shall match the beginning of the span, or vice versa, 
		// we need to define spanRefs
		if (mode.equals("beg")) {
			if (nodeString.equals("a")) {
				position = "frames:startswith";
			} else if (nodeString.equals("e")) {
				hitSpanRef = new Integer[]{0,1};
				elemSpanRef = new Integer[]{-1,1};
			}
		} else if (mode.equals("end")) {
			if (nodeString.equals("e")) {
				position = "frames:endswith";
			} else if (nodeString.equals("a")) {
				hitSpanRef = new Integer[]{0,1};
				elemSpanRef = new Integer[]{-1,1};
			}
		}
		// Create the position group and add the span and the subquery as operands, possibly wrapped in spanRefs
		LinkedHashMap<String, Object> positionGroup = makePosition(new String[]{position}, new String[0]);
		if (negated) positionGroup.put("exclude", true);
		ArrayList<Object> posOperands = new ArrayList<Object>();
		LinkedHashMap<String, Object> classGroup = makeSpanClass(classCounter++);
		classGroup.put("operands", distributedOperands);
		positionGroup.put("operands", posOperands);
		LinkedHashMap<String, Object> span = new LinkedHashMap<String, Object>();
		span.put("@type", "korap:span");
		span.put("key", elem);
		objectStack.push(classGroup);
		if (hitSpanRef != null) {
			LinkedHashMap<String, Object> spanRefAroundHit = makeSpanReference(hitSpanRef, "focus");
			((ArrayList<Object>) spanRefAroundHit.get("operands")).add(classGroup);
			classGroup = spanRefAroundHit; //re-assign after wrapping classGroup in spanRef
		}
		if (elemSpanRef != null) {
			LinkedHashMap<String, Object> spanRefAroundSpan = makeSpanReference(elemSpanRef, "focus");
			((ArrayList<Object>) spanRefAroundSpan.get("operands")).add(span);
			span = spanRefAroundSpan; //re-assign after wrapping span in spanRef
		}
		posOperands.add(span);
		posOperands.add(classGroup);
		return positionGroup;
	}

	private LinkedHashMap<String, Object> parseOPINOptions(Tree node) {
		Tree posnode = getFirstChildWithCat(node, "POS");
		Tree rangenode = getFirstChildWithCat(node, "RANGE");
		Tree exclnode = getFirstChildWithCat(node, "EXCL");
		Tree groupnode = getFirstChildWithCat(node, "GROUP");
		boolean negatePosition = false;
		LinkedHashMap<String, Object> posOptions = new LinkedHashMap<String, Object>();
		ArrayList<String> positions = new ArrayList<String>();
		ArrayList<String> classRefCheck = new ArrayList<String>();
		posOptions.put("matchall", false);
		String frame = "";
		String posOption = null; 
		if (posnode != null) {
			posOption = posnode.getChild(0).toStringTree();
			switch (posOption) {
			case "L":
				positions.add("frames:startswith");
				classRefCheck.add("classRefCheck:includes");
				frame = "startswith";
				break;
			case "R":
				positions.add("frames:endswith");
				classRefCheck.add("classRefCheck:includes");
				frame = "endswith";
				break;
			case "F":
				positions.add("frames:matches");
				classRefCheck.add("classRefCheck:includes");
				frame = "matches";
				break;
			case "FE":
				positions.add("frames:matches");
				classRefCheck.add("classRefCheck:equals");
				frame = "matches";
				break;
			case "FI":
				positions.add("frames:matches");
				classRefCheck.add("classRefCheck:unequals");
				classRefCheck.add("classRefCheck:includes");
				frame = "matches-noident";
				break;
			case "N": 
				positions.add("frames:contains");
				classRefCheck.add("classRefCheck:includes");
				frame = "contains";
				break;
			}
		} else {
			classRefCheck.add("classRefCheck:includes");
			frame = "contains";
		}
		posOptions.put("frames", positions);
		posOptions.put("classRefCheck", classRefCheck);
		posOptions.put("frame", "frame:"+frame);
		addMessage(303, "Deprecated 2014-09-22: 'frame' only to be supported until 3 months from deprecation date. " +
				"Position frames are now expressed through 'frames' and 'sharedClasses'");

		if (exclnode != null) {
			if (exclnode.getChild(0).toStringTree().equals("YES")) {
				negatePosition = !negatePosition;
			}
		}

		if (rangenode != null) {
			String range = rangenode.getChild(0).toStringTree().toLowerCase();
			if (range.equals("all")) {
				posOptions.put("matchall", true);
				//            	LinkedHashMap<String,Object> ref = makeResetReference(); // reset all defined classes
				//            	wrapOperand(node,2,ref);
			}
		}

		if (negatePosition) {
			posOptions.put("exclude", "true");
		}

		if (groupnode != null) {
			String grouping = groupnode.getChild(0).toStringTree().equals("max") ? "true" : "false";
			posOptions.put("grouping", grouping);
		}
		return posOptions;
	}



	private LinkedHashMap<String, Object> parseOPOVOptions(Tree node) {
		boolean negatePosition = false;
		Tree posnode = getFirstChildWithCat(node, "POS");
		Tree rangenode = getFirstChildWithCat(node, "RANGE");
		Tree exclnode = getFirstChildWithCat(node, "EXCL");
		Tree groupnode = getFirstChildWithCat(node, "GROUP");
		LinkedHashMap<String, Object> posOptions = new LinkedHashMap<String, Object>();
		ArrayList<String> positions = new ArrayList<String>();
		ArrayList<String> classRefCheck = new ArrayList<String>();
		posOptions.put("matchall", false);
		String frame = "";
		String posOption = null; 
		if (posnode != null) {
			posOption = posnode.getChild(0).toStringTree();
			switch (posOption) {
			case "L":
				positions.add("frames:startswith");
				positions.add("frames:overlapsLeft");
				classRefCheck.add("classRefCheck:intersects");
				frame = "overlapsLeft";
				break;
			case "R":
				positions.add("frames:endswith");
				positions.add("frames:overlapsRight");
				classRefCheck.add("classRefCheck:intersects");
				frame = "overlapsRight";
				break;
			case "F":
				positions.add("frames:matches");
				classRefCheck.add("classRefCheck:intersects");
				frame = "matches";
				break;
			case "FE":
				positions.add("frames:matches");
				classRefCheck.add("classRefCheck:equals");
				frame = "matches";
				break;
			case "FI":
				positions.add("frames:matches");
				classRefCheck.add("classRefCheck:unequals");
				frame = "matches-noident";
				break;
			case "X": 
				positions.add("frames:contains");
				classRefCheck.add("classRefCheck:intersects");
				frame = "overlaps";
				break;
			}
		} else {
			classRefCheck.add("classRefCheck:intersects");
			frame = "overlaps";
		}

		posOptions.put("frames", positions);
		posOptions.put("classRefCheck", classRefCheck);
		posOptions.put("frame", "frame:"+frame);
		addMessage(303, "Deprecated 2014-09-22: 'frame' only to be supported until 3 months from deprecation date. " +
				"Position frames are now expressed through 'frames' and 'sharedClasses'");

		if (exclnode != null) {
			if (exclnode.getChild(0).toStringTree().equals("YES")) {
				negatePosition = !negatePosition;
			}
		}

		if (rangenode != null) {
			String range = rangenode.getChild(0).toStringTree().toLowerCase();
			if (range.equals("all")) {
				posOptions.put("matchall", true);
				//            	LinkedHashMap<String,Object> ref = makeResetReference(); // reset all defined classes
				//            	wrapOperand(node,2,ref);
			}
		}

		if (negatePosition) {
			posOptions.put("exclude", "true");
		}

		if (groupnode != null) {
			String grouping = groupnode.getChild(0).toStringTree().equals("max") ? "true" : "false";
			posOptions.put("grouping", grouping);
		}
		return posOptions;
	}

	@SuppressWarnings({ "unchecked" })
	private LinkedHashMap<String,Object> wrap(LinkedHashMap[] wrapCascade) {
		int i;
		for (i=0; i<wrapCascade.length-1; i++) {
			ArrayList<Object> containerOperands = (ArrayList<Object>) wrapCascade[i+1].get("operands");
			containerOperands.add(0,wrapCascade[i]);
		}
		return wrapCascade[i];
	}

	LinkedList<ArrayList<Object>> nestedDistOperands = new LinkedList<ArrayList<Object>>();  

	@SuppressWarnings("unchecked")
	private void putIntoSuperObject(LinkedHashMap<String, Object> object, int objStackPosition) {
		if (objectStack.size() > objStackPosition) {
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
		q = rewritePositionQuery(q);

		Tree tree = null;
		ANTLRStringStream ss = new ANTLRStringStream(q);
		c2psLexer lex = new c2psLexer(ss);
		org.antlr.runtime.CommonTokenStream tokens = new org.antlr.runtime.CommonTokenStream(lex);  //v3
		parser = new c2psParser(tokens);
		c2psParser.c2ps_query_return c2Return = ((c2psParser) parser).c2ps_query();  // statt t().
		// AST Tree anzeigen:
		tree = (Tree) c2Return.getTree();

		String treestring = tree.toStringTree();
		if (treestring.contains("<mismatched token") || treestring.contains("<error") || treestring.contains("<unexpected")) {
			log.error("Invalid tree. Could not parse Cosmas query. Make sure it is well-formed.");
			throw new RecognitionException();
		}
		if (verbose) {
			System.out.println(tree.toStringTree());
		}
		return tree;
	}

	/**
	 * Normalises position operators to equivalents using #BED  
	 */
	private String rewritePositionQuery(String q) {
		Pattern p = Pattern.compile("(\\w+):((\\+|-)?(sa|se|pa|pe|ta|te),?)+");
		Matcher m = p.matcher(q);

		String rewrittenQuery = q;
		while (m.find()) {
			String match = m.group();
			String conditionsString = match.split(":")[1];
			Pattern conditionPattern = Pattern.compile("(\\+|-)?(sa|se|pa|pe|ta|te)");
			Matcher conditionMatcher = conditionPattern.matcher(conditionsString);
			String replacement = "#BED(" + m.group(1) + " , ";
			while (conditionMatcher.find()) {
				replacement = replacement + conditionMatcher.group() + ",";
			}
			replacement = replacement.substring(0, replacement.length() - 1) + ")"; //remove trailing comma and close parenthesis
			rewrittenQuery = rewrittenQuery.replace(match, replacement);
		}
		return rewrittenQuery;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		 * For debugging
		 */
		String[] queries = new String[]{
				/* COSMAS 2 */
				"Mann:sa,-pa,+te,se"
		};
//		CosmasTree.verbose=true;
		for (String q : queries) {
			try {
				System.out.println(q);
				try {
					CosmasTree act = new CosmasTree(q);
					System.out.println(act.parseCosmasQuery(q).toStringTree());
					System.out.println(act.getRequestMap().get("query"));
				} catch (QueryException e) {
					e.printStackTrace();
				} catch (RecognitionException e) {
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
