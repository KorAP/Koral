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
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.LoggerFactory;

import de.ids_mannheim.korap.query.annis.AqlLexer;
import de.ids_mannheim.korap.query.annis.AqlParser;
import de.ids_mannheim.korap.util.QueryException;

/**
 * Map representation of ANNIS QL syntax tree as returned by ANTLR
 * @author joachim
 *
 */
public class AqlTree extends Antlr4AbstractSyntaxTree {
    private org.slf4j.Logger log = LoggerFactory
            .getLogger(AqlTree.class);
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
	 * Parser object deriving the ANTLR parse tree.
	 */
	Parser parser;
	/**
	 * Keeps track of all visited nodes in a tree
	 */
	List<ParseTree> visited = new ArrayList<ParseTree>();
	/**
	 * Keeps track of active object.
	 */
	LinkedList<LinkedHashMap<String,Object>> objectStack = new LinkedList<LinkedHashMap<String,Object>>();
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
	 * Keeps track of how many objects there are to pop after every recursion of {@link #processNode(ParseTree)}
	 */
	LinkedList<Integer> objectsToPop = new LinkedList<Integer>();
	Integer stackedObjects = 0;
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
	
	private LinkedList<Integer> establishedFoci = new LinkedList<Integer>();
	private LinkedList<Integer> usedReferences = new LinkedList<Integer>();
//	private List<String> mirroredPositionFrames = Arrays.asList(new String[]{"startswith", "endswith", "overlaps", "contains"});
	private List<String> mirroredPositionFrames = Arrays.asList(new String[]{});
	List<ParseTree> globalLingTermNodes = new ArrayList<ParseTree>();
	private int totalRelationCount;
	private LinkedHashMap<String, Integer> refClassMapping = new LinkedHashMap<String, Integer>();
	private LinkedHashMap<String, Integer> nodeReferencesTotal = new LinkedHashMap<String, Integer>();
	private LinkedHashMap<String, Integer> nodeReferencesProcessed = new LinkedHashMap<String, Integer>();
	public static boolean verbose = false;
	
	/**
	 * 
	 * @param tree The syntax tree as returned by ANTLR
	 * @param parser The ANTLR parser instance that generated the parse tree
	 */
	public AqlTree(String query) {
//		prepareContext();
//		parseAnnisQuery(query);
//		super.parser = this.parser;
		requestMap.put("@context", "http://ids-mannheim.de/ns/KorAP/json-ld/v0.1/context.jsonld");
		try {
			process(query);
		} catch (QueryException e) {
			e.printStackTrace();
		}
		System.out.println(">>> "+requestMap.get("query")+" <<<");
	}

	@SuppressWarnings("unused")
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
		if (nodeCat.equals("start")) {
		}
		
		if (nodeCat.equals("exprTop")) {
			// has several andTopExpr as children delimited by OR (Disj normal form)
			if (node.getChildCount() > 1) {
				// TODO or-groups for every and
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
		
		// establish new variables or relations between vars
		if (nodeCat.equals("expr")) {
		}
		
		if (nodeCat.equals("unary_linguistic_term")) {
			LinkedHashMap<String, Object> unaryOperator = parseUnaryOperator(node);
			String reference = node.getChild(0).toStringTree(parser).substring(1);
			LinkedHashMap<String, Object> object = variableReferences.get(reference);
			object.putAll(unaryOperator);
		}
		
		if (nodeCat.equals("n_ary_linguistic_term")) {
			System.err.println(operandStack);
			relationCounter++;
			// get operator and determine type of group (sequence/treeRelation/relation/...)
			// It's possible in Annis QL to concatenate operators, so there may be several operators under one n_ary_linguistic_term node. 
			// Counter 'i' will iteratively point to all operator nodes (odd-numbered) under this node.
			for (int i=1; i<node.getChildCount(); i = i+2) {
				ParseTree operandTree1 = node.getChild(i-1);
				ParseTree operandTree2 = node.getChild(i+1);
				
				LinkedHashMap<String, Object> operatorGroup = parseOperatorNode(node.getChild(i).getChild(0));
				String groupType;
				try {
					groupType = (String) operatorGroup.get("groupType");
				} catch (ClassCastException | NullPointerException n) {
					groupType = "relation";
				}
				LinkedHashMap<String, Object> group = makeGroup(groupType);
				if (groupType.equals("relation") || groupType.equals("treeRelation")) {
					LinkedHashMap<String, Object> relationGroup = new LinkedHashMap<String, Object>();
					putAllButGroupType(relationGroup, operatorGroup);
					group.put("relation", relationGroup);
				} else if (groupType.equals("sequence") || groupType.equals("position")) {
					putAllButGroupType(group, operatorGroup);
				}
				// Get operands list before possible re-assignment of 'group' (see following 'if')
				ArrayList<Object> operands  = (ArrayList<Object>) group.get("operands");
				// Wrap in reference object in case other relations are following
				if (i < node.getChildCount()-2) {
					group = wrapInReference(group, classCounter);
				}
				// Retrieve operands.
				String ref1 = null;
				String ref2 = null;
				LinkedHashMap<String, Object> operand1 = null;
				LinkedHashMap<String, Object> operand2 = null;
				// Operand 1
				if (!getNodeCat(operandTree1.getChild(0)).equals("variableExpr")) {
					ref1 = operandTree1.getChild(0).toStringTree(parser).substring(1);
					operand1 = variableReferences.get(ref1);
					if (nodeReferencesTotal.get(ref1) > 1) {
						if (nodeReferencesProcessed.get(ref1) == 0) {
							refClassMapping.put(ref1, classCounter);
							operand1 = wrapInClass(operand1);
							nodeReferencesProcessed.put(ref1, nodeReferencesProcessed.get(ref1)+1);
						} else if (nodeReferencesProcessed.get(ref1)>0 && nodeReferencesTotal.get(ref1)>1) {
							try {
								operand1 = wrapInReference(operandStack.pop(), refClassMapping.get(ref1));
							} catch (NoSuchElementException e) {
								operand1 = makeReference(refClassMapping.get(ref1));
							}
						}
					}
				}
				// Operand 2
				if (!getNodeCat(operandTree2.getChild(0)).equals("variableExpr")) {
					ref2 = operandTree2.getChild(0).toStringTree(parser).substring(1);
					operand2 = variableReferences.get(ref2);
					if (nodeReferencesTotal.get(ref2) > 1) {
						if (nodeReferencesProcessed.get(ref2)==0) {
							refClassMapping.put(ref2, classCounter);
							operand2 = wrapInClass(operand2);
							nodeReferencesProcessed.put(ref2, nodeReferencesProcessed.get(ref2)+1);
						} else if (nodeReferencesProcessed.get(ref2)>0 && nodeReferencesTotal.get(ref2)>1) {
							try {
								operand2 = wrapInReference(operandStack.pop(), refClassMapping.get(ref2));
							} catch (NoSuchElementException e) {
								operand2 = makeReference(refClassMapping.get(ref2));
							}
						}
					}
				}
				// Inject operands.
				// -> Case distinction:
				if (node.getChildCount()==3) {
					// Things are easy when there's just one operator (thus 3 children incl. operands)...
					if (operand1 != null) operands.add(operand1);
					if (operand2 != null) operands.add(operand2);
				} else {
					// ... but things get a little more complicated here. The AST is of this form: (operand1 operator 1 operand2 operator2 operand3 operator3 ...)
					// but we'll have to serialize it in a nested, binary way: (((operand1 operator1 operand2) operator2 operand3) operator3 ...)
					// the following code will do just that:
					if (i == 1) {
						// for the first operator, include both operands
						if (operand1 != null) operands.add(operand1);
						if (operand2 != null) operands.add(wrapInClass(operand2));
						// Don't put this into the super object directly but store on operandStack 
						// (because this group will have to be an operand of a subsequent operator)
						operandStack.push(group);
					// for all subsequent operators, only take the 2nd operand (first was already added by previous operator)
					} else if (i < node.getChildCount()-2){
						// for all intermediate operators, include other previous groups and 2nd operand. Store this on the operandStack, too.
						if (operand2 != null) operands.add(wrapInClass(operand2));
						operands.add(0, operandStack.pop());
						operandStack.push(group);
					} else if (i == node.getChildCount()-2) {
						// This is the last operator. Include 2nd operand only
						if (operand2 != null) operands.add(operand2);
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
		
		if (nodeCat.equals("variableExpr")) {
			// simplex word or complex assignment (like qname = textSpec)?
			String firstChildNodeCat = getNodeCat(node.getChild(0));
			LinkedHashMap<String, Object> object = null;
			if (firstChildNodeCat.equals("node")) {
				object = makeSpan();
			} else if (firstChildNodeCat.equals("tok")) {
				object = makeToken();
				LinkedHashMap<String, Object> term = makeTerm();
				term.put("layer", "orth");
				object.put("wrap", term);
			} else if (firstChildNodeCat.equals("qName")) {	// only (foundry/)?layer specified
				// may be token or span, depending on indicated layer! (e.g. cnx/cat=NP or mate/pos=NN)
				HashMap<String, Object> qNameParse = parseQNameNode(node.getChild(0));
				if (Arrays.asList(new String[]{"pos", "lemma", "morph", "tok"}).contains(qNameParse.get("layer"))) {
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
				term.putAll(parseTextSpec(node.getChild(0)));
			}
				
			if (node.getChildCount() == 3) {  			// (foundry/)?layer=key specification
				if (object.get("@type").equals("korap:token")) {
					HashMap<String, Object> term = (HashMap<String, Object>) object.get("wrap");
					term.putAll(parseTextSpec(node.getChild(2)));
					term.put("match", parseMatchOperator(node.getChild(1)));
				} else {
					object.putAll(parseTextSpec(node.getChild(2)));
					object.put("match", parseMatchOperator(node.getChild(1)));
				}
			}
			
			if (object != null) {
				if (! operandOnlyNodeRefs.contains(variableCounter.toString())) {
					putIntoSuperObject(object);
				}
				variableReferences.put(variableCounter.toString(), object);
				variableCounter++;
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

	private LinkedHashMap<String, Object> wrapInReference(LinkedHashMap<String, Object> group, Integer classId) {
		LinkedHashMap<String, Object> refGroup = makeReference(classId);
		ArrayList<Object> operands = new ArrayList<Object>();
		operands.add(group);
		refGroup.put("operands", operands);
		return refGroup;
	}

	@SuppressWarnings("unchecked")
	private LinkedHashMap<String, Object> wrapInClass(LinkedHashMap<String, Object> group) {
		LinkedHashMap<String, Object> classGroup = makeSpanClass(classCounter);
		((ArrayList<Object>) classGroup.get("operands")).add(group);
		classCounter++;
		return classGroup;
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

	private LinkedHashMap<String, Object> parseOperatorNode(ParseTree operatorNode) {
		LinkedHashMap<String, Object> relation = null;
		String operator = getNodeCat(operatorNode);
		// DOMINANCE
		if (operator.equals("dominance")) {
			relation = makeTreeRelation("dominance");
			relation.put("groupType", "relation");
			ParseTree leftChildSpec = getFirstChildWithCat(operatorNode, "@l");
			ParseTree rightChildSpec = getFirstChildWithCat(operatorNode, "@r");
			ParseTree qName = getFirstChildWithCat(operatorNode, "qName");
			ParseTree edgeSpec = getFirstChildWithCat(operatorNode, "edgeSpec");
			ParseTree star = getFirstChildWithCat(operatorNode, "*");
			ParseTree rangeSpec = getFirstChildWithCat(operatorNode, "rangeSpec");
			if (leftChildSpec != null) relation.put("index", 0);
			if (rightChildSpec != null) relation.put("index", -1);
			if (qName != null) relation.putAll(parseQNameNode(qName));
			if (edgeSpec != null) relation.put("wrap", parseEdgeSpec(edgeSpec)) ;
			if (star != null) relation.put("distance", makeDistance("r", 0, 100));
			if (rangeSpec != null) relation.put("distance", distanceFromRangeSpec("r", rangeSpec));
			
		}
		else if (operator.equals("pointing")) {
//			String reltype = operatorNode.getChild(1).toStringTree(parser);
			relation = makeRelation(null);
			relation.put("groupType", "relation");
			ParseTree qName = getFirstChildWithCat(operatorNode, "qName");
			ParseTree edgeSpec = getFirstChildWithCat(operatorNode, "edgeSpec");
			ParseTree star = getFirstChildWithCat(operatorNode, "*");
			ParseTree rangeSpec = getFirstChildWithCat(operatorNode, "rangeSpec");
//			if (qName != null) relation.putAll(parseQNameNode(qName));
			if (qName != null) relation.put("reltype", qName.getText());
			if (edgeSpec != null) relation.put("wrap", parseEdgeSpec(edgeSpec)) ;
			if (star != null) relation.put("distance", makeDistance("r", 0, 100));
			if (rangeSpec != null) relation.put("distance", distanceFromRangeSpec("r", rangeSpec));
			
		}
		else if (operator.equals("precedence")) {
			relation = new LinkedHashMap<String, Object>();
			relation.put("groupType", "sequence");
			ParseTree rangeSpec = getFirstChildWithCat(operatorNode, "rangeSpec");
			ParseTree star = getFirstChildWithCat(operatorNode, "*");
			ArrayList<Object> distances = new ArrayList<Object>();
			if (star != null) {
				distances.add(makeDistance("w", 0, 100));
				relation.put("distances", distances);
			}
			if (rangeSpec != null) {
				distances.add(parseDistance(rangeSpec));
				relation.put("distances", distances);
			}
			relation.put("inOrder", true);
		}
		else if (operator.equals("spanrelation")) {
			relation = makeGroup(null);
			relation.put("groupType", "position");
			String reltype = operatorNode.getChild(0).toStringTree(parser);
			String frame = null;
			boolean inOrder = true;
			switch (reltype) {
				case "_=_":
					frame = "matches"; break;
				case "_l_":
					frame = "startswith"; 
					inOrder = false;
					break;
				case "_r_":
					frame = "endswith";
					inOrder = false;
					break;
				case "_i_":
					frame = "contains"; break;
				case "_o_":
					frame = "overlaps"; 
					inOrder = false;
					break;
				case "_ol_":
					frame = "overlapsLeft"; 
					inOrder = false;
					break;
				case "_or_":
					frame = "overlapsRight"; 
					inOrder = false;
					break;
			}
			relation.put("operation", "operation:position");
			if (!inOrder) relation.put("inOrder", false);
			relation.put("frame", "frame:"+frame);
		}
		else if (operator.equals("commonparent")) {
			//TODO
		}
		else if (operator.equals("commonancestor")) {
			//TODO
		}
		else if (operator.equals("identity")) {
			//TODO
		}
		else if (operator.equals("equalvalue")) {
			//TODO
		}
		else if (operator.equals("notequalvalue")) {
			//TODO
		}
		return relation;
	}

	private Object parseEdgeSpec(ParseTree edgeSpec) {
		ArrayList<Object> edgeAnnos = new ArrayList<Object>();
		for (ParseTree edgeAnno : getChildrenWithCat(edgeSpec, "edgeAnno")) {
			edgeAnnos.add(parseEdgeAnno(edgeAnno));
		}
		return edgeAnnos;
	}

	private LinkedHashMap<String, Object> parseEdgeAnno(
			ParseTree edgeAnnoSpec) {
		LinkedHashMap<String, Object> edgeAnno = new LinkedHashMap<String, Object>();
		edgeAnno.put("@type", "korap:term");
		ParseTree qNameNode = edgeAnnoSpec.getChild(0);
		ParseTree matchOperatorNode = edgeAnnoSpec.getChild(1);
		ParseTree textSpecNode = edgeAnnoSpec.getChild(2);
		ParseTree layerNode = getFirstChildWithCat(qNameNode, "layer");
		ParseTree foundryNode = getFirstChildWithCat(qNameNode, "foundry");
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
		if (expandToMax) max = MAXIMUM_DISTANCE;
		if (rangeSpec.getChildCount()==3) 
			max = Integer.parseInt(rangeSpec.getChild(2).toStringTree(parser));
		return makeBoundary(min, max);
	}

	private LinkedHashMap<String, Object> distanceFromRangeSpec(String key, ParseTree rangeSpec) {
		Integer min = Integer.parseInt(rangeSpec.getChild(0).toStringTree(parser));
		Integer max = MAXIMUM_DISTANCE;
		if (rangeSpec.getChildCount()==3) 
			max = Integer.parseInt(rangeSpec.getChild(2).toStringTree(parser));
		return makeDistance(key, min, max);
	}
	
	private LinkedHashMap<String, Object> parseDistance(ParseTree rangeSpec) {
		Integer min = Integer.parseInt(rangeSpec.getChild(0).toStringTree(parser));
		Integer max = MAXIMUM_DISTANCE;
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
		return node.toStringTree(parser).equals("=") ? "match:eq" : "match:ne";
	}
	
	
	/**
	 * Parses a textSpec node (which holds the 'key' field)
	 * @param node
	 * @return
	 */
	private LinkedHashMap<String, Object> parseVarKey(ParseTree node) {
		LinkedHashMap<String, Object> fields = new LinkedHashMap<String, Object>();
		if (node.getChildCount() == 2) {	// no content, empty quotes
			
		} else if (node.getChildCount() == 3) {
			fields.put("key", node.getChild(1).toStringTree(parser));
			if (node.getChild(0).toStringTree(parser).equals("/") &&		// slashes -> regex
					node.getChild(2).toStringTree(parser).equals("/")) {
				fields.put("type", "type:regex");
			}
		}
		return fields;
	}


	private LinkedHashMap<String, Object> parseQNameNode(ParseTree node) {
		LinkedHashMap<String, Object> fields = new LinkedHashMap<String, Object>();
		ParseTree layerNode = getFirstChildWithCat(node, "layer");
		ParseTree foundryNode = getFirstChildWithCat(node, "foundry");
		if (foundryNode != null) fields.put("foundry", foundryNode.getChild(0).toStringTree(parser));
		fields.put("layer", layerNode.getChild(0).toStringTree(parser));
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

	@SuppressWarnings("unchecked")
	private void putGroupAndWrapFocus(LinkedHashMap<String, Object> thisObject, LinkedHashMap<String, Object> formerObject, int focusClass) {
		LinkedHashMap<String, Object> focusWrap = makeGroup("focus");
		focusWrap.put("classRef", focusClass);
		((ArrayList<Object>) focusWrap.get("operands")).add(formerObject);
		((ArrayList<Object>) thisObject.get("operands")).add(focusWrap);
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
//			 "node & #1:root",
//			 "pos=\"N\" & pos=\"V\" & pos=\"N\" & #1 . #2 & #2 . #3",
//			 "cat=\"NP\" & #1:tokenarity=2",
//			 "node & node & node & #1 . #2 . #3",
//			 "cat=\"CP\" & cat=\"VP\" & cat=\"NP\" & #1 > #2 > #3",
//			 "cat=\"CP\" & cat=\"VP\" & cat=\"NP\" & cat=\"DP\" & #1 > #2 > #3 > #4",
//			 "pos=\"N\" & pos=\"V\" & pos=\"P\" & #1 . #2 & #2 . #3"
//			 "cnx/cat=\"NP\" > node",
//			 "node > node",
//			 "cat=/NP/ > node",
//			 "/Mann/",
//			 "node > tok=\"foo\"",
//				"tok=\"Sonne\" & tok=\"Mond\" & #1 > #2 .0,4  tok=\"Sterne\"",
//				 "pos=\"N\" & pos=\"V\" & pos=\"P\" & #1 . #2 & #2 . #3",
//				 "cat=\"NP\" & pos=\"V\" & pos=\"P\" & #1 > #2 & #1 > #3 & #2 . #3",
				 "cat=\"CP\" & cat=\"VP\" & cat=\"NP\" & #1 > #2 > #3"
			};
		AqlTree.verbose=true;
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