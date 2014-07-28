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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Map representation of CosmasII syntax tree as returned by ANTLR
 *
 * @author joachim
 */
public class CosmasTree extends Antlr3AbstractSyntaxTree {

    private static Logger log = LoggerFactory.getLogger(CosmasTree.class);

    String query;
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
     *
     */
    int classCounter = 1;
    boolean negate = false;
    int wrapFirstOpInClass = -1;
    int wrapSecondOpInClass = -1;

    Tree cosmasTree;

    LinkedHashMap<String, Object> treeMap = new LinkedHashMap<String, Object>();
    /**
     * Keeps track of all visited nodes in a tree
     */
    List<Tree> visited = new ArrayList<Tree>();

    Integer stackedToWrap = 0;
    /**
     * A list of node categories that can be sequenced (i.e. which can be in a sequence with any number of other nodes in this list)
     */
    private final List<String> sequentiableCats = Arrays.asList(new String[]{"OPWF", "OPLEM", "OPMORPH", "OPBEG", "OPEND", "OPIN", "OPBED"});
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
    /**
     * @param tree   The syntax tree as returned by ANTLR
     * @param parser The ANTLR parser instance that generated the parse tree
     * @throws QueryException
     */
    public CosmasTree(String query) throws QueryException {
        this.query = query;
        process(query);
        System.out.println("\n" + requestMap.get("query"));
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
        log.info("Processing CosmasII query");
        System.out.println("Processing Cosmas");
        requestMap.put("@context", "http://ids-mannheim.de/ns/KorAP/json-ld/v0.1/context.jsonld");
//		prepareContext(requestMap);
        processNode(tree);
        log.info(requestMap.toString());
    }

    @SuppressWarnings("unchecked")
    private void processNode(Tree node) {

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
                    hasSequentiableSiblings = false;
                    for (int i = 1; i < parent.getChildCount(); i++) {
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
            if (!hasChild(node, "TPOS")) {
                putIntoSuperObject(token, 1);
                visited.add(node.getChild(0));
            } else {
            	// TODO
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
                                    System.err.println("taga");
                                } else {
                                    subTermGroupOperands.add(term);
                                    System.err.println(layer);
                                }
                            }
                            if (node.getChildCount() > elname + 1) {
                                System.err.println(termGroupOperands);
                                termGroupOperands.add(subTermGroup);
                                System.err.println(termGroupOperands);
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

            ArrayList<Object> sequenceOperands = (ArrayList<Object>) group.get("operands");

            // possibly several distance constraints
            for (int i = 0; i < dist_list.getChildCount(); i++) {
                String direction = dist_list.getChild(i).getChild(0).getChild(0).toStringTree().toLowerCase();
                String min = dist_list.getChild(i).getChild(1).getChild(0).toStringTree();
                String max = dist_list.getChild(i).getChild(1).getChild(1).toStringTree();
                String meas = dist_list.getChild(i).getChild(2).getChild(0).toStringTree();
                if (min.equals("VAL0")) {
                    min = "0";
                }
                // If zero word-distance, wrap this sequence in a disjunction along with an overlap position
                // between the two operands
                if (meas.equals("w") && min.equals("0")) {
                	min = "1";
                	putIntoOverlapDisjunction = true;
                }
                if (!meas.equals("w") && min.equals("0") ) {
                	processSpanDistance(meas, Integer.parseInt(min), Integer.parseInt(max));
                }
                LinkedHashMap<String, Object> distance = new LinkedHashMap<String, Object>();
                distance.put("@type", "korap:distance");
                distance.put("key", meas);
                distance.put("min", Integer.parseInt(min));
                distance.put("max", Integer.parseInt(max));
                if (exclusion) {
                    distance.put("exclude", true);
                }
                constraints.add(distance);
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
            
//            ArrayList<ArrayList<Object>> distributedOperands = new ArrayList<ArrayList<Object>>();
            LinkedHashMap<String,Object> sequence = null;
            if (putIntoOverlapDisjunction) {
            	sequence = group;
            	group = makeGroup("or");
            	ArrayList<Object> disjOperands = (ArrayList<Object>) group.get("operands");
            	LinkedHashMap<String,Object> overlapsGroup = makePosition("overlaps");
            	
            	ArrayList<Object> overlapsOperands = (ArrayList<Object>) overlapsGroup.get("operands");
            	// this ensures identity of the operands lists and thereby a distribution of the operands for both created objects 
            	sequence.put("operands", overlapsOperands);
            	if (invertedOperands) invertedOperandsLists.push(overlapsOperands);
            	disjOperands.add(overlapsGroup);
            	disjOperands.add(sequence);
            	// Step II: decide where to put
            	putIntoSuperObject(group, 0);
            	objectStack.push(sequence);
            }
            else {
            	// Step II: decide where to put
            	putIntoSuperObject(group, 0);
            	objectStack.push(group);
            }
            stackedObjects++;
            visited.add(node.getChild(0));
        }

        // inlcusion or overlap
        if (nodeCat.equals("OPIN") || nodeCat.equals("OPOV")) {
            // Step I: create group
        	wrapFirstOpInClass = classCounter;
            LinkedHashMap<String, Object> posgroup = makePosition(null);
            if (nodeCat.equals("OPIN")) {
                posgroup = parseOPINOptions(node, posgroup);
            } else {
            	posgroup = parseOPOVOptions(node, posgroup);
            }
            objectStack.push(posgroup);
            // mark this an inverted list
            invertedOperandsLists.push((ArrayList<Object>) posgroup.get("operands"));
            stackedObjects++;
            // Step II: wrap in reference and decide where to put
            LinkedHashMap<String, Object> submatchgroup = wrapInReference(posgroup, classCounter);
            putIntoSuperObject(submatchgroup, 1);
        }

        // Wrap the first argument of an #IN operator in a class group
        if (nodeCat.equals("ARG1"))  {
    		if (wrapFirstOpInClass > -1) {        		
        		// Step I: create group
    			System.err.println("1st op: "+wrapFirstOpInClass);
                LinkedHashMap<String, Object> classGroup = makeSpanClass(wrapFirstOpInClass);
                objectStack.push(classGroup);
                stackedObjects++;
                // Step II: decide where to put
                putIntoSuperObject(classGroup, 1);
                wrapFirstOpInClass = -1;
        	}
        }

        // Wrap the 2nd argument of an #IN operator embedded in NHIT in a class group
        if (nodeCat.equals("ARG2")) {
        	if (wrapSecondOpInClass > -1) {
        		System.err.println("2nd op: "+wrapSecondOpInClass);
        		// Step I: create group
                LinkedHashMap<String, Object> classGroup = makeSpanClass(wrapSecondOpInClass);
                objectStack.push(classGroup);
                stackedObjects++;
                // Step II: decide where to put
                putIntoSuperObject(classGroup, 1);
                wrapSecondOpInClass = -1;
        	}
        }


        if (nodeCat.equals("OPNHIT")) {
            ArrayList<Integer> classRef = new ArrayList<Integer>();
            classRef.add(classCounter);
            classRef.add(classCounter + 1);  // yes, do this twice (two classes)!
            LinkedHashMap<String, Object> exclGroup = makeReference(classRef);
            exclGroup.put("classRefOp", "classRefOp:intersection");
            ArrayList<Object> operands = new ArrayList<Object>();
            exclGroup.put("operands", operands);
            System.err.println(classCounter);
            wrapFirstOpInClass = classCounter++;
            wrapSecondOpInClass = classCounter++;
            objectStack.push(exclGroup);
            stackedObjects++;
            putIntoSuperObject(exclGroup, 1);
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
            // Step I: create group
            int optsChild = node.getChildCount() - 1;
            Tree conditions = node.getChild(optsChild).getChild(0);

            // create a containing group expressing the submatch constraint on the first argument
            ArrayList<Integer> spanRef = new ArrayList<Integer>();
            spanRef.add(1);
            LinkedHashMap<String, Object> submatchgroup = makeReference(spanRef);
            ArrayList<Object> submatchoperands = new ArrayList<Object>();
            submatchgroup.put("operands", submatchoperands);
            putIntoSuperObject(submatchgroup);

            // Distinguish two cases. Normal case: query has just one condition, like #BED(X, sa) ...
            if (conditions.getChildCount() == 1) {
                CosmasCondition c = new CosmasCondition(conditions.getChild(0));

                // create the group expressing the position constraint
                LinkedHashMap<String, Object> posgroup = makePosition(c.position);
                ArrayList<Object> operands = (ArrayList<Object>) posgroup.get("operands");
                if (c.negated) posgroup.put("exclude", true);

                // create span representing the element expressed in the condition
                LinkedHashMap<String, Object> bedElem = new LinkedHashMap<String, Object>();
                bedElem.put("@type", "korap:span");
                bedElem.put("key", c.elem);

                // create a class group containing the argument, in order to submatch the arg.
                LinkedHashMap<String, Object> classGroup = makeSpanClass(classCounter++);
                objectStack.push(classGroup);
                stackedObjects++;
                operands.add(bedElem);
                operands.add(classGroup);
                // Step II: decide where to put
                submatchoperands.add(posgroup);

                // ... or the query has several conditions specified, like #BED(XY, sa,-pa). In that case,
                //     use 'focus' operations to create nested conditions
            } else {
                // node has several conditions (like 'sa, -pa')
                // -> create zero-distance sequence group and embed all position groups there
                LinkedHashMap<String, Object> conjunct = makeGroup("sequence");
                ArrayList<Object> distances = new ArrayList<Object>();
                distances.add(makeDistance("w", 0,0));
                conjunct.put("distances", distances);
                ArrayList<Object> operands = new ArrayList<Object>();
                conjunct.put("operands", operands);
                ArrayList<Object> distributedOperands = new ArrayList<Object>();

                for (int i = 0; i < conditions.getChildCount(); i++) {
                    // for each condition, create a position group containing a class group.
                    // make position group
                    CosmasCondition c = new CosmasCondition(conditions.getChild(i));
                    LinkedHashMap<String, Object> posGroup = makePosition(c.position);
                    operands.add(posGroup);
                    if (c.negated) posGroup.put("exclude", "true");
                    ArrayList<Object> posOperands = new ArrayList<Object>();

                    // make class group
                    LinkedHashMap<String, Object> classGroup = makeSpanClass(classCounter++);
                    classGroup.put("operands", distributedOperands);

                    // put the span and the class group into the position group
                    posGroup.put("operands", posOperands);
                    LinkedHashMap<String, Object> span = new LinkedHashMap<String, Object>();
                    posOperands.add(span);
                    posOperands.add(classGroup);
                    span.put("@type", "korap:span");
                    span.put("key", c.elem);
                    objectStack.push(classGroup);
                }
                submatchoperands.add(conjunct);
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
        openNodeCats.pop();
    }

    private void processSpanDistance(String meas, int parseInt, int parseInt2) {
		// TODO Auto-generated method stub
		
	}

	private Object translateMorph(String layer) {
        // todo: not very nicely solved! Does this require extension somehow? if not, why not use simple string comparison?!
//        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
//        map.put("ANA", "pos");
//        if (map.containsKey(layer))
//            return map.get(layer);
//        else
//            return layer;
        if (layer.equals("ANA"))
            return ResourceMapper.descriptor2policy("ANA");
        else
            return layer;

    }

    @SuppressWarnings("unchecked")
	private LinkedHashMap<String, Object> parseOPINOptions(Tree node, LinkedHashMap<String, Object> posgroup) {
        Tree posnode = getFirstChildWithCat(node, "POS");
        Tree rangenode = getFirstChildWithCat(node, "RANGE");
        Tree exclnode = getFirstChildWithCat(node, "EXCL");
        Tree groupnode = getFirstChildWithCat(node, "GROUP");
        boolean negatePosition = false;

        String position = "";
        String posOption = null; 
        if (posnode != null) {
            posOption = posnode.getChild(0).toStringTree();
            position = translateTextAreaArgument(posOption, "in");
            if (posOption.equals("N")) {
                negatePosition = !negatePosition;
            }
        } else {
            position = "contains";
        }
        posgroup.put("frame", "frame:" + position);
//      position = openNodeCats.get(1).equals("OPIN") ? "contains" : "full";
        
        if (exclnode != null) {
            if (exclnode.getChild(0).toStringTree().equals("YES")) {
                negatePosition = !negatePosition;
            }
        }
        
        if (posOption != null && (posOption.equals("L") || posOption.equals("R")) && !negatePosition) {
        	LinkedHashMap<String, Object> noMatchPosition = makePosition("matches");
        	((ArrayList<Object>) noMatchPosition.get("operands")).add(makeReference(classCounter+1));
        	noMatchPosition.put("exclude", true);
        	LinkedHashMap<String,Object> innerFocus = makeReference(classCounter);
        	innerFocus.put("operands", new ArrayList<Object>());
        	LinkedHashMap<String,Object> outerFocus = makeReference(classCounter);
        	outerFocus.put("operands", new ArrayList<Object>());
        	toWrapStack.push(new LinkedHashMap[]{posgroup, innerFocus, noMatchPosition, outerFocus});
        	stackedToWrap++;
        	wrapSecondOpInClass = classCounter+1;
        }
        
        if (posOption != null && (posOption.equals("F") || posOption.equals("FI")) && !negatePosition) {
        	LinkedHashMap<String, Object> endsWithPosition = makePosition("endswith");
        	((ArrayList<Object>) endsWithPosition.get("operands")).add(makeReference(classCounter+1));
        	LinkedHashMap<String,Object> innerFocus = makeReference(classCounter);
        	innerFocus.put("operands", new ArrayList<Object>());
        	LinkedHashMap<String,Object> outerFocus = makeReference(classCounter);
        	outerFocus.put("operands", new ArrayList<Object>());
        	toWrapStack.push(new LinkedHashMap[]{posgroup, innerFocus, endsWithPosition, outerFocus});
        	stackedToWrap++;
        	wrapFirstOpInClass = classCounter;
        	wrapSecondOpInClass = ++classCounter;
        }
        
        
        if (rangenode != null) {
            String range = rangenode.getChild(0).toStringTree();
            posgroup.put("range", range.toLowerCase());
        }

        if (negatePosition) {
            posgroup.put("exclude", "true");
        }

        if (groupnode != null) {
            String grouping = groupnode.getChild(0).toStringTree().equals("max") ? "true" : "false";
            posgroup.put("grouping", grouping);
        }
        return posgroup;
    }

    private LinkedHashMap<String, Object> parseOPOVOptions(Tree node, LinkedHashMap<String, Object> posgroup) {
    	boolean negatePosition = false;
        Tree posnode = getFirstChildWithCat(node, "POS");
        Tree exclnode = getFirstChildWithCat(node, "EXCL");
        Tree groupnode = getFirstChildWithCat(node, "GROUP");

        String position = "";
        String posOption = null; 
        if (posnode != null) {
            posOption = posnode.getChild(0).toStringTree();
            position = translateTextAreaArgument(posOption, "ov");
            if (posOption.equals("N")) {
                negatePosition = !negatePosition;
            }
        } else {
            position = "overlaps";
        }
        
        if (posnode != null) {
            String value = posnode.getChild(0).toStringTree();
            position = translateTextAreaArgument(value, "ov");
        }
        posgroup.put("frame", "frame:" + position);

        if (exclnode != null) {
            if (exclnode.getChild(0).toStringTree().equals("YES")) {
                posgroup.put("match", "match:" + "ne");
            }
        }
        
        if (posOption != null && (posOption.equals("F") || posOption.equals("FI")) && !negatePosition) {
        	LinkedHashMap<String, Object> endsWithPosition = makePosition("endswith");
        	((ArrayList<Object>) endsWithPosition.get("operands")).add(makeReference(classCounter+1));
        	LinkedHashMap<String,Object> innerFocus = makeReference(classCounter);
        	innerFocus.put("operands", new ArrayList<Object>());
        	LinkedHashMap<String,Object> outerFocus = makeReference(classCounter);
        	outerFocus.put("operands", new ArrayList<Object>());
        	LinkedHashMap[] toWrap = new LinkedHashMap[]{posgroup, innerFocus, endsWithPosition, outerFocus};
        	if (posOption.equals("FI")) {
        		LinkedHashMap<String, Object> noMatchPosition = makePosition("matches");
            	((ArrayList<Object>) noMatchPosition.get("operands")).add(makeReference(classCounter+1));
            	noMatchPosition.put("exclude", true);
        		LinkedHashMap<String,Object> outermostFocus = makeReference(classCounter);
        		outermostFocus.put("operands", new ArrayList<Object>());
        		toWrap = new LinkedHashMap[]{posgroup, innerFocus, endsWithPosition, outerFocus, noMatchPosition, outermostFocus};
        	}
        	
        	toWrapStack.push(toWrap);
        	stackedToWrap++;
        	wrapFirstOpInClass = classCounter+1;
        	wrapSecondOpInClass = classCounter;
        }
        
        if (groupnode != null) {
            String grouping = groupnode.getChild(0).toStringTree().equals("@max") ? "true" : "false";
            posgroup.put("grouping", grouping);
        }
        return posgroup;
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
    
    private LinkedHashMap<String,Object> processPositionOption(String posOption) {
    	LinkedHashMap<String,Object> posgroup = null;

    	if (posOption.equals("F") || posOption.equals("FI")) {
    		posgroup = makePosition("startswith");
    		LinkedHashMap<String, Object> endsWithPosition = makePosition("endswith");
    		((ArrayList<Object>) endsWithPosition.get("operands")).add(makeReference(classCounter+1));
    		LinkedHashMap<String,Object> innerFocus = makeReference(classCounter);
    		innerFocus.put("operands", new ArrayList<Object>());
    		LinkedHashMap<String,Object> outerFocus = makeReference(classCounter);
    		outerFocus.put("operands", new ArrayList<Object>());
    		LinkedHashMap[] toWrap = new LinkedHashMap[]{posgroup, innerFocus, endsWithPosition, outerFocus};
    		if (posOption.equals("FI")) {
    			LinkedHashMap<String, Object> noMatchPosition = makePosition("matches");
    			((ArrayList<Object>) noMatchPosition.get("operands")).add(makeReference(classCounter+1));
    			noMatchPosition.put("exclude", true);
    			LinkedHashMap<String,Object> outermostFocus = makeReference(classCounter);
    			outermostFocus.put("operands", new ArrayList<Object>());
    			toWrap = new LinkedHashMap[]{posgroup, innerFocus, endsWithPosition, outerFocus, noMatchPosition, outermostFocus};
    		}

    		toWrapStack.push(toWrap);
    		stackedToWrap++;
    		wrapFirstOpInClass = classCounter+1;
    		wrapSecondOpInClass = classCounter;
    	}
    	
    	return posgroup;
    }
    
    /**
     * Translates the text area specifications (position option arguments) to terms used in serialisation.
     * For the allowed argument types and their values for OPIN and OPOV, see
     * http://www.ids-mannheim.de/cosmas2/win-app/hilfe/suchanfrage/eingabe-grafisch/syntax/ARGUMENT_I.html or
     * http://www.ids-mannheim.de/cosmas2/win-app/hilfe/suchanfrage/eingabe-grafisch/syntax/ARGUMENT_O.html, respectively.
     *
     * @param argument
     * @param mode
     * @return
     */
    private String translateTextAreaArgument(String argument, String mode) {
        String position = "overlaps";
        // POSTYP	:	'L'|'l'|'R'|'r'|'F'|'f'|'FE'|'fe'|'FI'|'fi'|'N'|'n'|'X'|'x' ;
        argument = argument.toUpperCase();
        switch (argument) {
            case "L":
                position = mode.equals("in") ? "startswith" : "overlapsLeft";
                break;
            case "R":
                position = mode.equals("in") ? "endswith" : "overlapsRight";
                break;
            case "F":
                position = "startswith";
                break;
            case "FE":
                position = "matches";
                break;
            case "FI":
                position = "startswith";
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
//                "wegen #OV(x) <s>",
//                "wegen #IN(L) <s>",
//                "#NHIT(gehen /w1:10 voran)",
//                "wegen #OV(FI) <s>",
//                "Sonne /+w4 Mond",
//                "#BEG(der /w3:5 Mann) /+w10 kommt",
//                "Sonne /s0 Mond"
//                "Sonne /+w4 Mond",
                "#BED(der Mann , sa,-pa)"
                
        };
		CosmasTree.verbose=true;
        for (String q : queries) {
            try {
                System.out.println(q);
                try {
                    CosmasTree act = new CosmasTree(q);
                    System.out.println(act.parseCosmasQuery(q).toStringTree());
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