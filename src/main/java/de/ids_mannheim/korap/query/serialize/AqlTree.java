package de.ids_mannheim.korap.query.serialize;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import de.ids_mannheim.korap.query.annis.AqlLexer;
import de.ids_mannheim.korap.query.annis.AqlParser;
import de.ids_mannheim.korap.util.QueryException;

/**
 * Map representation of ANNIS QL syntax tree as returned by ANTLR
 * @author joachim
 *
 */
public class AqlTree extends Antlr4AbstractSyntaxTree {
	private static Logger log = LoggerFactory.getLogger(AqlTree.class);
	/**
	 * Flag that indicates whether token fields or meta fields are currently being processed
	 */
	boolean inMeta = false;
	/**
	 * Keeps track of operands that are to be integrated into yet uncreated objects.
	 */
	LinkedList<LinkedHashMap<String,Object>> operandStack = new LinkedList<LinkedHashMap<String,Object>>();
	/**
	 * Keeps track of explicitly (by #-var definition) or implicitly (number as reference) introduced entities (for later reference by #-operator)
	 */
	Map<String, LinkedHashMap<String,Object>> variableReferences = new LinkedHashMap<String, LinkedHashMap<String,Object>>(); 
	/**
	 * Counter for variable definitions.
	 */
	Integer variableCounter = 1;
	/**
	 * Marks the currently active token in order to know where to add flags (might already have been taken away from token stack).
	 */
	LinkedHashMap<String,Object> curToken = new LinkedHashMap<String,Object>();
	/**
	 * Keeps track of operands lists that are to be serialised in an inverted
	 * order (e.g. the IN() operator) compared to their AST representation. 
	 */
	private LinkedList<ArrayList<Object>> invertedOperandsLists = new LinkedList<ArrayList<Object>>();
	/**
	 * Keeps track of operation:class numbers.
	 */
	int classCounter = 0;
	/**
	 * Keeps track of numers of relations processed (important when dealing with multiple predications).
	 */
	int relationCounter = 0;
	/**
	 * Keeps track of references to nodes that are operands of groups (e.g. tree relations). Those nodes appear on the top level of the parse tree
	 * but are to be integrated into the AqlTree at a later point (namely as operands of the respective group). Therefore, store references to these
	 * nodes here and exclude the operands from being written into the query map individually.   
	 */
	private LinkedList<String> operandOnlyNodeRefs = new LinkedList<String>();
	private List<ParseTree> globalLingTermNodes = new ArrayList<ParseTree>();
	private int totalRelationCount;
	/**
	 * Keeps a record of reference-class-mapping, i.e. which 'class' has been assigned to which #n reference. This is important when introducing korap:reference 
	 * spans to refer back to previously established classes for entities.
	 */
	private LinkedHashMap<String, Integer> refClassMapping = new LinkedHashMap<String, Integer>();
	private LinkedHashMap<String, Integer> nodeReferencesTotal = new LinkedHashMap<String, Integer>();
	private LinkedHashMap<String, Integer> nodeReferencesProcessed = new LinkedHashMap<String, Integer>();

	/**
	 * 
	 * @param tree The syntax tree as returned by ANTLR
	 * @param parser The ANTLR parser instance that generated the parse tree
	 */
	public AqlTree(String query) {
		try {
			process(query);
		} catch (QueryException e) {
			e.printStackTrace();
		}
		System.out.println(">>> "+requestMap.get("query")+" <<<");
	}

	@Override
	public void process(String query) throws QueryException {
		ParseTree tree = parseAnnisQuery(query);
		if (this.parser != null) {
			super.parser = this.parser;
		} else {
			throw new NullPointerException("Parser has not been instantiated!"); 
		}
		log.info("Processing Annis query.");
		log.info("AST is: "+tree.toStringTree(parser));
		System.out.println("Processing Annis QL");
		if (verbose) System.out.println(tree.toStringTree(parser));
		processNode(tree);
		log.info(requestMap.toString());
	}

	@SuppressWarnings("unchecked")
	private void processNode(ParseTree node) {
		// Top-down processing
		if (visited.contains(node)) return;
		else visited.add(node);

		String nodeCat = getNodeCat(node);
		openNodeCats.push(nodeCat);

		stackedObjects = 0;

		if (verbose) {
			System.err.println(" "+objectStack);
			System.out.println(openNodeCats);
		}

		/*
		 ****************************************************************
		 **************************************************************** 
		 * 			Processing individual node categories  				*
		 ****************************************************************
		 ****************************************************************
		 */
		if (nodeCat.equals("exprTop")) {
			List<ParseTree> andTopExprs = getChildrenWithCat(node, "andTopExpr");
			if (andTopExprs.size() > 1) {
				LinkedHashMap<String, Object> topOr = makeGroup("or");
				requestMap.put("query", topOr);
				objectStack.push(topOr);
			}
		}

		if (nodeCat.equals("andTopExpr")) {
			// Before processing any child expr node, check if it has one or more "*ary_linguistic_term" nodes.
			// Those nodes may use references to earlier established operand nodes.
			// Those operand nodes are not to be included into the query map individually but
			// naturally as operands of the relations/groups introduced by the 
			// *node. For that purpose, this section mines all used references
			// and stores them in a list for later reference.
			for (ParseTree exprNode : getChildrenWithCat(node,"expr")) {
				// Pre-process any 'variableExpr' such that the variableReferences map can be filled
				List<ParseTree> definitionNodes = new ArrayList<ParseTree>();
				definitionNodes.addAll(getChildrenWithCat(exprNode, "variableExpr"));
				for (ParseTree definitionNode : definitionNodes) {
					processNode(definitionNode);
				}
				// Then, mine all relations between nodes
				List<ParseTree> lingTermNodes = new ArrayList<ParseTree>();
				lingTermNodes.addAll(getChildrenWithCat(exprNode, "n_ary_linguistic_term"));
				globalLingTermNodes.addAll(lingTermNodes);
				totalRelationCount  = globalLingTermNodes.size();
				// Traverse refOrNode nodes under *ary_linguistic_term nodes and extract references
				for (ParseTree lingTermNode : lingTermNodes) {
					for (ParseTree refOrNode : getChildrenWithCat(lingTermNode, "refOrNode")) {
						String refOrNodeString = refOrNode.getChild(0).toStringTree(parser);
						if (refOrNodeString.startsWith("#")) {
							String ref = refOrNode.getChild(0).toStringTree(parser).substring(1);
							if (nodeReferencesTotal.containsKey(ref)) {
								nodeReferencesTotal.put(ref, nodeReferencesTotal.get(ref)+1);
							} else {
								nodeReferencesTotal.put(ref, 1);
								nodeReferencesProcessed.put(ref, 0);
							}
						}
					}
				}
			}
		}

		if (nodeCat.equals("unary_linguistic_term")) {
			LinkedHashMap<String, Object> unaryOperator = parseUnaryOperator(node);
			String reference = node.getChild(0).toStringTree(parser).substring(1);
			LinkedHashMap<String, Object> object = variableReferences.get(reference);
			object.putAll(unaryOperator);
		}

		if (nodeCat.equals("n_ary_linguistic_term")) {
			processN_ary_linguistic_term(node);
		}

		if (nodeCat.equals("variableExpr")) {
			// simplex word or complex assignment (like qname = textSpec)?
			String firstChildNodeCat = getNodeCat(node.getChild(0));
			LinkedHashMap<String, Object> object = null;
			if (firstChildNodeCat.equals("node")) {
				object = makeSpan();
			} else if (firstChildNodeCat.equals("tok")) {
				object = makeToken();
				if (node.getChildCount() > 1) { // empty tokens do not wrap a term
					LinkedHashMap<String, Object> term = makeTerm();
					term.put("layer", "orth");
					object.put("wrap", term);
				}
			} else if (firstChildNodeCat.equals("qName")) {	// only (foundry/)?layer specified
				// may be token or span, depending on indicated layer! (e.g. cnx/cat=NP vs mate/pos=NN)
				// TODO generalize the list below -> look up layers associated with tokens rather than spans somewhere
				HashMap<String, Object> qNameParse = parseQNameNode(node.getChild(0));
				if (Arrays.asList(new String[]{"p", "lemma", "m", "orth"}).contains(qNameParse.get("layer"))) { 
					object = makeToken();
					LinkedHashMap<String, Object> term = makeTerm();
					object.put("wrap", term);
					term.putAll(qNameParse);
				} else {
					object = makeSpan();
					object.putAll(qNameParse);
				}
			} else if (firstChildNodeCat.equals("textSpec")) {
				object = makeToken();
				LinkedHashMap<String, Object> term = makeTerm();
				object.put("wrap", term);
				term.put("layer", "orth");
				term.putAll(parseTextSpec(node.getChild(0)));
			}

			if (node.getChildCount() == 3) {  			// (foundry/)?layer=key specification
				if (object.get("@type").equals("korap:token")) {
					HashMap<String, Object> term = (HashMap<String, Object>) object.get("wrap");
					term.putAll(parseTextSpec(node.getChild(2)));
					term.put("match", parseMatchOperator(getFirstChildWithCat(node, "eqOperator")));
				} else {
					object.putAll(parseTextSpec(node.getChild(2)));
					object.put("match", parseMatchOperator(getFirstChildWithCat(node, "eqOperator")));
				}
			}

			if (object != null) {
				if (! operandOnlyNodeRefs.contains(variableCounter.toString())) {
					putIntoSuperObject(object);
				}
				ParseTree parentsFirstChild = node.getParent().getChild(0);
				if (getNodeCat(parentsFirstChild).endsWith("#")) {
					variableReferences.put(getNodeCat(parentsFirstChild).replaceAll("#", ""), object);
				}
				variableReferences.put(variableCounter.toString(), object);
				variableCounter++;
				System.out.println(variableReferences);
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
			ParseTree child = node.getChild(i);
			processNode(child);
		}

		/*
		 **************************************************************
		 * Stuff that happens after processing the children of a node *
		 **************************************************************
		 */
		if (!objectsToPop.isEmpty()) {
			for (int i=0; i<objectsToPop.pop(); i++) {
				objectStack.pop();
			}
		}
		openNodeCats.pop();
	}



	/**
	 * Processes an operand node, creating a map for the operand containing all its information
	 * given in the node definition (referenced via '#'). If this node has been referred to  and used earlier,
	 * a korap:reference is created in its place. 
	 * The operand will be wrapped in a class group if necessary.
	 * @param operandTree
	 * @return A map object with the appropriate CQLF representation of the operand 
	 */
	private LinkedHashMap<String, Object> retrieveOperand(ParseTree operandTree) {
		LinkedHashMap<String, Object> operand = null;
		if (!getNodeCat(operandTree.getChild(0)).equals("variableExpr")) {
			String ref = operandTree.getChild(0).toStringTree(parser).substring(1);
			operand = variableReferences.get(ref);
			if (nodeReferencesTotal.get(ref) > 1) {
				if (nodeReferencesProcessed.get(ref)==0) {
					refClassMapping.put(ref, classCounter);
					operand = wrapInClass(operand, classCounter++);
					nodeReferencesProcessed.put(ref, nodeReferencesProcessed.get(ref)+1);
				} else if (nodeReferencesProcessed.get(ref)>0 && nodeReferencesTotal.get(ref)>1) {
					try {
						operand = wrapInReference(operandStack.pop(), refClassMapping.get(ref));
					} catch (NoSuchElementException e) {
						operand = makeReference(refClassMapping.get(ref));
					}
				}
			}
		}
		return operand;
	}

	@SuppressWarnings("unchecked")
	private void processN_ary_linguistic_term(ParseTree node) {
		relationCounter++;
		// get operator and determine type of group (sequence/treeRelation/relation/...)
		// It's possible in Annis QL to concatenate operators, so there may be several operators under one n_ary_linguistic_term node. 
		// Counter 'i' will iteratively point to all operator nodes (odd-numbered) under this node.
		for (int i=1; i<node.getChildCount(); i = i+2) {
			ParseTree operandTree1 = node.getChild(i-1);
			ParseTree operandTree2 = node.getChild(i+1);
			String reltype = getNodeCat(node.getChild(i).getChild(0));

			LinkedHashMap<String,Object> group = null;
			ArrayList<Object> operands = null;
			// Retrieve operands.
			LinkedHashMap<String, Object> operand1 = retrieveOperand(operandTree1);
			LinkedHashMap<String, Object> operand2 = retrieveOperand(operandTree2);

			// 'Proper' n_ary_linguistic_operators receive a considerably different serialisation than 'commonparent' and 'commonancestor'.
			// For the latter cases, a dummy span is introduced and declared as a span class that has a dominance relation towards
			// the two operands, one after the other, thus resulting in two nested relations! A Poliqarp+ equivalent for A $ B would be
			// contains(focus(1:contains({1:<>},A)), B).
			// This is modeled here...
			if (reltype.equals("commonparent") || reltype.equals("commonancestor")) {
				// make an (outer) group and an inner group containing the dummy node or previous relations
				group = makeGroup("relation");
				LinkedHashMap<String,Object> innerGroup = makeGroup("relation");
				LinkedHashMap<String,Object> relation = makeRelation();
				LinkedHashMap<String,Object> term = makeTerm();
				term.put("layer", "c");
				relation.put("wrap", term);
				// commonancestor is an indirect commonparent relation
				if (reltype.equals("commonancestor")) relation.put("boundary", makeBoundary(1, null));
				group.put("relation", relation);
				innerGroup.put("relation", relation);
				// Get operands list before possible re-assignment of 'group' (see following 'if')
				ArrayList<Object> outerOperands  = (ArrayList<Object>) group.get("operands");
				ArrayList<Object> innerOperands  = (ArrayList<Object>) innerGroup.get("operands");
				// for lowest level, add the underspecified node as first operand and wrap it in a class group
				if (i == 1) {
					innerOperands.add(wrapInClass(makeSpan(), classCounter));
					// add the first operand and wrap the whole group in a focusing reference 
					innerOperands.add(operand1);
					innerGroup = wrapInReference(innerGroup, classCounter);
					outerOperands.add(innerGroup);
				} else {
					outerOperands.add(operandStack.pop());
				}
				// Lookahead: if next operator is not commonparent or commonancestor, wrap in class for accessibility
				if (i < node.getChildCount()-2 && !getNodeCat(node.getChild(i+2).getChild(0)).startsWith("common")) {
					operand2 = wrapInClass(operand2, ++classCounter);
				}
				outerOperands.add(operand2);

				// Wrap in another reference object in case other relations are following
				if (i < node.getChildCount()-2) {
					group = wrapInReference(group, classCounter);
				}
				// All other n-ary linguistic relations have special 'relation' attributes defined in CQLF and can be
				// handled more easily...
			} else {
				LinkedHashMap<String, Object> operatorGroup = parseOperatorNode(node.getChild(i).getChild(0));
				String groupType;
				try {
					groupType = (String) operatorGroup.get("groupType");
				} catch (ClassCastException | NullPointerException n) {
					groupType = "relation";
				}
				if (groupType.equals("relation") || groupType.equals("treeRelation")) {
					group = makeGroup(groupType);
					LinkedHashMap<String, Object> relation = new LinkedHashMap<String, Object>();
					putAllButGroupType(relation, operatorGroup);
					group.put("relation", relation);
				} else if (groupType.equals("sequence")) {
					group = makeGroup(groupType);
					putAllButGroupType(group, operatorGroup);
				} else if (groupType.equals("position")) {
					group = new LinkedHashMap<String,Object>();
					putAllButGroupType(group, operatorGroup);
				}
					
				// Get operands list before possible re-assignment of 'group' (see following 'if')
				operands  = (ArrayList<Object>) group.get("operands");
				
				ParseTree leftChildSpec = getFirstChildWithCat(node.getChild(i).getChild(0), "@l");
				ParseTree rightChildSpec = getFirstChildWithCat(node.getChild(i).getChild(0), "@r");
				if (leftChildSpec != null || rightChildSpec != null) { //XXX
					String frame = (leftChildSpec!=null) ? "frames:startswith" : "frames:endswith";
					LinkedHashMap<String,Object> positionGroup = makePosition(new String[]{frame}, null);
					operand2 = wrapInClass(operand2, ++classCounter);
					((ArrayList<Object>) positionGroup.get("operands")).add(group);
					((ArrayList<Object>) positionGroup.get("operands")).add(makeReference(classCounter,true));
					group = positionGroup;
				}
				
				// Wrap in reference object in case other relations are following
				if (i < node.getChildCount()-2) {
					group = wrapInReference(group, classCounter);
				}

				// Inject operands.
				// -> Case distinction:
				if (node.getChildCount()==3) {
					// Things are easy when there's just one operator (thus 3 children incl. operands)...
					if (operand1 != null) operands.add(operand1);
					if (operand2 != null) operands.add(operand2);
				} else {
					// ... but things get a little more complicated here. The AST is of this form: (operand1 operator1 operand2 operator2 operand3 operator3 ...)
					// but we'll have to serialize it in a nested, binary way: (((operand1 operator1 operand2) operator2 operand3) operator3 ...)
					// the following code will do just that:
					if (i == 1) {
						// for the first operator, include both operands
						if (operand1 != null) operands.add(operand1);
						if (operand2 != null) operands.add(wrapInClass(operand2, classCounter++));
						// Don't put this into the super object directly but store on operandStack 
						// (because this group will have to be an operand of a subsequent operator)
						operandStack.push(group);
						// for all subsequent operators, only take the 2nd operand (first was already added by previous operator)
					} else if (i < node.getChildCount()-2) {
						// for all intermediate operators, include other previous groups and 2nd operand. Store this on the operandStack, too.
						if (operand2 != null) operands.add(wrapInClass(operand2, classCounter++));
						operands.add(0, operandStack.pop());
						operandStack.push(group);
					} else if (i == node.getChildCount()-2) {
						// This is the last operator. Include 2nd operand only
						if (operand2 != null) operands.add(operand2);
					}
				}
			}
			// Final step: decide what to do with the 'group' object, depending on whether all relations have been processed
			if (i == node.getChildCount()-2 && relationCounter == totalRelationCount) {
				putIntoSuperObject(group);
				if (!operandStack.isEmpty()) {
					operands.add(0, operandStack.pop());
				}
				objectStack.push(group);
				stackedObjects++;
			} else {
				operandStack.push(group);
			}
		}
	}



	/**
	 * Parses a unary_linguistic_operator node. Possible operators are: root, arity, tokenarity.
	 * Operators are embedded into a korap:term, in turn wrapped by an 'attr' property in a korap:span.
	 * @param node The unary_linguistic_operator node
	 * @return A map containing the attr key, to be inserted into korap:span 
	 */
	private LinkedHashMap<String, Object> parseUnaryOperator(ParseTree node) {
		LinkedHashMap<String, Object> attr = new LinkedHashMap<String, Object>();
		LinkedHashMap<String, Object> term = makeTerm();
		String op = node.getChild(1).toStringTree(parser).substring(1);
		if (op.equals("arity") || op.equals("tokenarity")) {
			LinkedHashMap<String, Object> boundary = boundaryFromRangeSpec(node.getChild(3), false);
			term.put(op, boundary);
		} else {
			term.put(op, true);
		}
		attr.put("attr", term);
		return attr;
	}

	@SuppressWarnings("unchecked")
	private LinkedHashMap<String, Object> parseOperatorNode(ParseTree operatorNode) {
		LinkedHashMap<String, Object> relation = null;
		String operator = getNodeCat(operatorNode);
		// DOMINANCE
		if (operator.equals("dominance")) {
			relation = makeRelation();
			relation.put("groupType", "relation");
			ParseTree leftChildSpec = getFirstChildWithCat(operatorNode, "@l");
			ParseTree rightChildSpec = getFirstChildWithCat(operatorNode, "@r");
			ParseTree qName = getFirstChildWithCat(operatorNode, "qName");
			ParseTree edgeSpecNode = getFirstChildWithCat(operatorNode, "edgeSpec");
			ParseTree star = getFirstChildWithCat(operatorNode, "*");
			ParseTree rangeSpec = getFirstChildWithCat(operatorNode, "rangeSpec");
			LinkedHashMap<String,Object> term = makeTerm();
			term.put("layer", "c");
			if (qName != null) term = parseQNameNode(qName);
			if (edgeSpecNode != null) {
				LinkedHashMap<String,Object> edgeSpec = parseEdgeSpec(edgeSpecNode);
				String edgeSpecType = (String) edgeSpec.get("@type");
				if (edgeSpecType.equals("korap:termGroup")) {
					((ArrayList<Object>) edgeSpec.get("operands")).add(term);
					term = edgeSpec;
				} else {
					term = makeTermGroup("and");
					ArrayList<Object> termGroupOperands = (ArrayList<Object>) term.get("operands");
					termGroupOperands.add(edgeSpec);
					LinkedHashMap<String,Object> constTerm = makeTerm();
					constTerm.put("layer", "c");
					termGroupOperands.add(constTerm);
				}
			}
			if (star != null) relation.put("boundary", makeBoundary(0, null));
			if (rangeSpec != null) relation.put("boundary", boundaryFromRangeSpec(rangeSpec));
			relation.put("wrap", term);
		}
		else if (operator.equals("pointing")) {
			//			String reltype = operatorNode.getChild(1).toStringTree(parser);
			relation = makeRelation();
			relation.put("groupType", "relation");
			ParseTree qName = getFirstChildWithCat(operatorNode, "qName");
			ParseTree edgeSpec = getFirstChildWithCat(operatorNode, "edgeSpec");
			ParseTree star = getFirstChildWithCat(operatorNode, "*");
			ParseTree rangeSpec = getFirstChildWithCat(operatorNode, "rangeSpec");
			//			if (qName != null) relation.putAll(parseQNameNode(qName));
			LinkedHashMap<String,Object> term = makeTerm();
			if (qName != null) term.putAll(parseQNameNode(qName));
			if (edgeSpec != null) term.putAll(parseEdgeSpec(edgeSpec));
			if (star != null) relation.put("boundary", makeBoundary(0, null));
			if (rangeSpec != null) relation.put("boundary", boundaryFromRangeSpec(rangeSpec));
			relation.put("wrap", term);
		}
		else if (operator.equals("precedence")) {
			relation = new LinkedHashMap<String, Object>();
			relation.put("groupType", "sequence");
			ParseTree rangeSpec = getFirstChildWithCat(operatorNode, "rangeSpec");
			ParseTree star = getFirstChildWithCat(operatorNode, "*");
			ArrayList<Object> distances = new ArrayList<Object>();
			if (star != null) {
				distances.add(makeDistance("w", 0, null));
				relation.put("distances", distances);
			}
			if (rangeSpec != null) {
				distances.add(parseDistance(rangeSpec));
				relation.put("distances", distances);
			}
			relation.put("inOrder", true);
		}
		else if (operator.equals("spanrelation")) {
//			relation = makeGroup("position");
//			relation.put("groupType", "position");
			String reltype = operatorNode.getChild(0).toStringTree(parser);
			String[] frames = new String[]{};
			switch (reltype) {
			case "_=_":
				frames = new String[]{"frames:matches"}; 
				break;
			case "_l_":
				frames = new String[]{"frames:startswith"};
				break;
			case "_r_":
				frames = new String[]{"frames:endswith"};
				break;
			case "_i_":
				frames = new String[]{"frames:contains"};break;
			case "_o_":
				frames = new String[]{"frames:overlapsLeft", "frames:overlapsRight"};
				break;
			case "_ol_":
				frames = new String[]{"frames:overlapsLeft"};
				break;
			case "_or_":
				frames = new String[]{"frames:overlapsRight"};
				break;
			}
//			relation.put("frames", frames);
//			relation.put("sharedClasses", sharedClasses);
			relation = makePosition(frames, new String[]{});
			relation.put("groupType", "position");
		}
		else if (operator.equals("identity")) {
			//TODO since ANNIS v. 3.1.6
		}
		else if (operator.equals("equalvalue")) {
			//TODO since ANNIS v. 3.1.6
		}
		else if (operator.equals("notequalvalue")) {
			//TODO since ANNIS v. 3.1.6
		}
		return relation;
	}

	@SuppressWarnings("unchecked")
	private LinkedHashMap<String,Object> parseEdgeSpec(ParseTree edgeSpec) {
		List<ParseTree> annos = getChildrenWithCat(edgeSpec, "edgeAnno");
		if (annos.size() == 1) return parseEdgeAnno(annos.get(0));
		else {
			LinkedHashMap<String,Object> termGroup = makeTermGroup("and");
			ArrayList<Object> operands = (ArrayList<Object>) termGroup.get("operands");
			for (ParseTree anno : annos) {
				operands.add(parseEdgeAnno(anno));
			}
			return termGroup;
		}
	}

	private LinkedHashMap<String, Object> parseEdgeAnno(ParseTree edgeAnnoSpec) {
		LinkedHashMap<String, Object> edgeAnno = new LinkedHashMap<String, Object>();
		edgeAnno.put("@type", "korap:term");
		ParseTree textSpecNode = getFirstChildWithCat(edgeAnnoSpec, "textSpec");
		ParseTree layerNode = getFirstChildWithCat(edgeAnnoSpec, "layer");
		ParseTree foundryNode = getFirstChildWithCat(edgeAnnoSpec, "foundry");
		ParseTree matchOperatorNode = getFirstChildWithCat(edgeAnnoSpec, "eqOperator");
		if (foundryNode!=null) edgeAnno.put("foundry", foundryNode.getChild(0).toStringTree(parser));
		if (layerNode!=null) edgeAnno.put("layer", layerNode.getChild(0).toStringTree(parser));
		edgeAnno.putAll(parseTextSpec(textSpecNode));
		edgeAnno.put("match", parseMatchOperator(matchOperatorNode));
		return edgeAnno;
	}

	private LinkedHashMap<String, Object> boundaryFromRangeSpec(ParseTree rangeSpec) {
		return boundaryFromRangeSpec(rangeSpec, true); 
	}

	private LinkedHashMap<String, Object> boundaryFromRangeSpec(ParseTree rangeSpec, boolean expandToMax) {
		Integer min = Integer.parseInt(rangeSpec.getChild(0).toStringTree(parser));
		Integer max = min;
		if (expandToMax) max = null;
		if (rangeSpec.getChildCount()==3) 
			max = Integer.parseInt(rangeSpec.getChild(2).toStringTree(parser));
		return makeBoundary(min, max);
	}

	private LinkedHashMap<String, Object> parseDistance(ParseTree rangeSpec) {
		Integer min = Integer.parseInt(rangeSpec.getChild(0).toStringTree(parser));
		Integer max = null;
		if (rangeSpec.getChildCount()==3) 
			max = Integer.parseInt(rangeSpec.getChild(2).toStringTree(parser));
		return makeDistance("w", min, max);
	}

	private LinkedHashMap<String, Object> parseTextSpec(ParseTree node) {
		LinkedHashMap<String, Object> term = new LinkedHashMap<String, Object>();
		if (hasChild(node, "regex")) {
			term.put("type", "type:regex");
			term.put("key", node.getChild(0).getChild(0).toStringTree(parser).replaceAll("/", ""));
		} else {
			term.put("key", node.getChild(1).toStringTree(parser));
		}
		term.put("match", "match:eq");
		return term;
	}

	/**
	 * Parses the match operator (= or !=)
	 * @param node
	 * @return
	 */
	private String parseMatchOperator(ParseTree node) {
		if (node.getChildCount()>0) {
			return node.getChild(0).toStringTree(parser).equals("=") ? "match:eq" : "match:ne";
		}
		return null;
	}

	private LinkedHashMap<String, Object> parseQNameNode(ParseTree node) {
		LinkedHashMap<String, Object> fields = new LinkedHashMap<String, Object>();
		ParseTree layerNode = getFirstChildWithCat(node, "layer");
		ParseTree foundryNode = getFirstChildWithCat(node, "foundry");
		if (foundryNode != null) fields.put("foundry", foundryNode.getChild(0).toStringTree(parser));
		String layer = layerNode.getChild(0).toStringTree(parser);
		if (layer.equals("pos")) layer = "p";
		if (layer.equals("cat")) layer = "c";
		fields.put("layer", layer);
		return fields;
	}

	private void putIntoSuperObject(LinkedHashMap<String, Object> object) {
		putIntoSuperObject(object, 0);
	}

	@SuppressWarnings({ "unchecked" })
	private void putIntoSuperObject(LinkedHashMap<String, Object> object, int objStackPosition) {
		if (objectStack.size()>objStackPosition) {
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

	private void putAllButGroupType(Map<String, Object> container, Map<String, Object> input) {
		for (String key : input.keySet()) {
			if (!key.equals("groupType")) {
				container.put(key, input.get(key));
			}
		}
	}

	private ParserRuleContext parseAnnisQuery (String p) throws QueryException {
		Lexer poliqarpLexer = new AqlLexer((CharStream)null);
		ParserRuleContext tree = null;
		// Like p. 111
		try {

			// Tokenize input data
			ANTLRInputStream input = new ANTLRInputStream(p);
			poliqarpLexer.setInputStream(input);
			CommonTokenStream tokens = new CommonTokenStream(poliqarpLexer);
			parser = new AqlParser(tokens);

			// Don't throw out erroneous stuff
			parser.setErrorHandler(new BailErrorStrategy());
			parser.removeErrorListeners();

			// Get starting rule from parser
			Method startRule = AqlParser.class.getMethod("start"); 
			tree = (ParserRuleContext) startRule.invoke(parser, (Object[])null);
		}

		// Some things went wrong ...
		catch (Exception e) {
			log.error(e.getMessage());
			System.err.println( e.getMessage() );
		}

		if (tree == null) {
			log.error("Could not parse query. Make sure it is correct ANNIS QL syntax.");
			throw new QueryException("Could not parse query. Make sure it is correct ANNIS QL syntax.");
		}

		// Return the generated tree
		return tree;
	}

	public static void main(String[] args) {
		/*
		 * For testing
		 */
		String[] queries = new String[] {
				"cat=\"S\" & node & #1 >@l #2"
		};
		//		AqlTree.verbose=true;
		for (String q : queries) {
			try {
				System.out.println(q);
				AqlTree at = new AqlTree(q);
				System.out.println(at.parseAnnisQuery(q).toStringTree(at.parser));
				System.out.println();

			} catch (NullPointerException | QueryException npe) {
				npe.printStackTrace();
			}
		}
	}

}
