package de.ids_mannheim.korap.query.serialize;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.antlr.v4.parse.ANTLRParser.throwsSpec_return;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ids_mannheim.korap.query.object.KoralFrame;
import de.ids_mannheim.korap.query.object.KoralMatchOperator;
import de.ids_mannheim.korap.query.object.KoralOperation;
import de.ids_mannheim.korap.query.object.KoralTermGroupRelation;
import de.ids_mannheim.korap.query.object.KoralType;
import de.ids_mannheim.korap.query.parse.annis.AqlLexer;
import de.ids_mannheim.korap.query.parse.annis.AqlParser;
import de.ids_mannheim.korap.query.serialize.util.Antlr4DescriptiveErrorListener;
import de.ids_mannheim.korap.query.serialize.util.KoralException;
import de.ids_mannheim.korap.query.serialize.util.KoralObjectGenerator;
import de.ids_mannheim.korap.query.serialize.util.StatusCodes;

/**
 * Processor class for ANNIS QL queries. This class uses an ANTLR v4
 * grammar
 * for query parsing, it therefore extends
 * {@link Antlr4AbstractQueryProcessor}.
 * The parser object is inherited from the parent class and
 * instantiated in {@link #parseAnnisQuery(String)} as an
 * {@link AqlParser}.
 * 
 * @see http://annis-tools.org/aql.html
 * 
 * @author Joachim Bingel (bingel@ids-mannheim.de)
 * @author Eliza Margaretha (margaretha@ids-mannheim.de)
 * @version 0.3.0
 * @since 0.1.0
 */
public class AnnisQueryProcessor extends Antlr4AbstractQueryProcessor {
    private static Logger log = LoggerFactory
            .getLogger(AnnisQueryProcessor.class);
    /**
     * Flag that indicates whether token fields or meta fields are
     * currently
     * being processed
     */
    boolean inMeta = false;
    /**
     * Keeps track of operands that are to be integrated into yet
     * uncreated
     * objects.
     */
    LinkedList<Map<String, Object>> operandStack = new LinkedList<Map<String, Object>>();
    /**
     * Keeps track of explicitly (by #-var definition) or implicitly
     * (number
     * as reference) introduced entities (for later reference by
     * #-operator)
     */
    Map<String, Map<String, Object>> nodeVariables = new HashMap<String, Map<String, Object>>();
    /**
     * Keeps track of explicitly (by #-var definition) or implicitly
     * (number
     * as reference) introduced entities (for later reference by
     * #-operator)s
     */
    Map<ParseTree, String> nodes2refs = new HashMap<ParseTree, String>();
    /**
     * Counter for variable definitions.
     */
    Integer variableCount = 1;
    /**
     * Marks the currently active token in order to know where to add
     * flags
     * (might already have been taken away from token stack).
     */
    Map<String, Object> curToken = new HashMap<String, Object>();
    /**
     * Keeps track of operands lists that are to be serialised in an
     * inverted
     * order (e.g. the IN() operator) compared to their AST
     * representation.
     */
    private LinkedList<ArrayList<Object>> invertedOperandsLists = new LinkedList<ArrayList<Object>>();
    /**
     * Keeps track of operation:class numbers.
     */
    int classCounter = 1;
    /**
     * Keeps track of numers of relations processed (important when
     * dealing
     * with multiple predications).
     */
    int relationCounter = 0;
    /**
     * Keeps track of references to nodes that are operands of groups
     * (e.g.
     * tree relations). Those nodes appear on the top level of the
     * parse tree
     * but are to be integrated into the AqlTree at a later point
     * (namely as
     * operands of the respective group). Therefore, store references
     * to these
     * nodes here and exclude the operands from being written into the
     * query
     * map individually.
     */
    private int totalRelationCount = 0;
    /**
     * Keeps a record of reference-class-mapping, i.e. which 'class'
     * has been
     * assigned to which #n reference. This is important when
     * introducing
     * koral:reference spans to refer back to previously established
     * classes for
     * entities.
     */
    private Map<String, Integer> refClassMapping = new HashMap<String, Integer>();
    /**
     * Keeps a record of unary relations on spans/tokens.
     */
    private Map<String, ArrayList<ParseTree>> unaryRelations = new HashMap<String, ArrayList<ParseTree>>();
    /**
     * Keeps track of the number of references to a node/token by
     * means of #n.
     * E.g. in the query <tt>tok="x" & tok="y" & tok="z" & #1 . #2 &
     * #2 . #3</tt>,
     * the 2nd token ("y") is referenced twice, the others once.
     */
    private Map<String, Integer> nodeReferencesTotal = new HashMap<String, Integer>();
    /**
     * Keeps track of the number of references to a node/token that
     * have
     * already been processed.
     */
    private Map<String, Integer> nodeReferencesProcessed = new HashMap<String, Integer>();
    /**
     * Keeps track of queued relations. Relations sometimes cannot be
     * processed
     * directly, namely in case it does not share any operands with
     * the
     * previous relation. Then wait until a relation with a shared
     * operand has
     * been processed.
     */
    private LinkedList<ParseTree> queuedRelations = new LinkedList<ParseTree>();
    /**
     * For some objects, it may be decided in the initial scan
     * ({@link #processAndTopExpr(ParseTree)} that they need to be
     * wrapped in a
     * class operation when retrieved later. This map stores this
     * information.
     * More precisely, it stores for every node in the tree which
     * class ID its
     * derived KoralQuery object will receive.
     */
    private Map<ParseTree, Integer> objectsToWrapInClass = new HashMap<ParseTree, Integer>();


    public AnnisQueryProcessor (String query) {
        KoralObjectGenerator.setQueryProcessor(this);
        process(query);
    }


    @Override
    public void process (String query) {
        ParseTree tree = parseAnnisQuery(query);
        if (this.parser != null) {
            super.parser = this.parser;
        }
        else {
            throw new NullPointerException("Parser has not been instantiated!");
        }
        log.info("Processing Annis query: " + query);
        if (tree != null) {
            log.debug("ANTLR parse tree: " + tree.toStringTree(parser));
            processNode(tree);
            // Last check to see if all relations have left the queue
            if (!queuedRelations.isEmpty()) {
                ParseTree queued = queuedRelations.pop();
                if (verbose)
                    System.out.println(
                            "Taking off queue (last rel): " + queued.getText());
                if (checkOperandsProcessedPreviously(queued)) {
                    processNode(queued);
                }
                else {
                    addError(StatusCodes.UNBOUND_ANNIS_RELATION,
                            "The relation " + queued.getText()
                                    + " is not bound to any other relations.");
                    requestMap.put("query", new HashMap<String, Object>());
                }
            }
        }
    }


    /**
     * Traverses the parse tree by recursively calling itself,
     * starting with
     * the root node of the tree and calling itself with the children
     * of its
     * current node in a depth-first, left-to-right fashion. In each
     * call,
     * depending on the category of the current node, special
     * processor
     * methods for the respective node category are called to process
     * the node.
     * 
     * @param node
     *            The node currently visited in the parse tree
     *            traversal.
     */
    private void processNode (ParseTree node) {
        String nodeCat = getNodeCat(node);
        // Top-down processing
        if (visited.contains(node))
            return;
        openNodeCats.push(nodeCat);
        stackedObjects = 0;
        // Before doing anything else, check if any relations are queued
        // and need to be processed first
        if (nodeCat.equals("n_ary_linguistic_term")) {
            if (!queuedRelations.isEmpty()) {
                ParseTree queued = queuedRelations.getFirst();
                if (checkOperandsProcessedPreviously(queued)) {
                    if (verbose)
                        System.out.println(
                                "Taking off queue: " + queued.getText());
                    queuedRelations.removeFirst();
                    processNode(queued);
                }
            }
        }
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
        if (nodeCat.equals("exprTop")) {
            processExprTop(node);
        }

        if (nodeCat.equals("andTopExpr")) {
            processAndTopExpr(node);
        }

        if (nodeCat.equals("n_ary_linguistic_term")) {
            processN_ary_linguistic_term(node);
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

        /*
         **************************************************************
         * Stuff that happens after processing the children of a node *
         **************************************************************
         */
        if (!objectsToPop.isEmpty()) {
            for (int i = 0; i < objectsToPop.pop(); i++) {
                objectStack.pop();
            }
        }
        openNodeCats.pop();
    }


    /**
     * Processes an <tt>andTopExpr</tt> node. This is a child of the
     * root
     * and contains a set of expressions connected by logical
     * conjunction.
     * Several of these nodes are possibly connected via disjunction.
     * 
     * @param node
     *            The current parse tree node (must be of category
     *            <tt>andTopExpr</tt>).
     */
    private void processAndTopExpr (ParseTree node) {
        // Before processing any child expr node, check if it has one or more 
        // "*ary_linguistic_term" nodes.
        // Those nodes may use references to earlier established operand nodes.
        // Those operand nodes are not to be included into the query map 
        // individually but naturally as operands of the relations/groups 
        // introduced by the node. For that purpose, this section mines all 
        // used references and stores them in a list for later reference.
        for (ParseTree unaryTermNode : getDescendantsWithCat(node,
                "unary_linguistic_term")) {
            String ref = getNodeCat(unaryTermNode.getChild(0)).substring(1);
            ArrayList<ParseTree> unaryTermsForRef = unaryRelations.get(ref);
            if (unaryTermsForRef == null)
                unaryTermsForRef = new ArrayList<ParseTree>();
            unaryTermsForRef.add(unaryTermNode);
            unaryRelations.put(ref, unaryTermsForRef);
        }
        for (ParseTree lingTermNode : getDescendantsWithCat(node,
                "n_ary_linguistic_term")) {
            for (ParseTree refOrNode : getChildrenWithCat(lingTermNode,
                    "refOrNode")) {
                String refOrNodeString = refOrNode.getChild(0)
                        .toStringTree(parser);
                if (refOrNodeString.startsWith("#")) {
                    String ref = refOrNode.getChild(0).toStringTree(parser)
                            .substring(1);
                    if (nodeReferencesTotal.containsKey(ref)) {
                        nodeReferencesTotal.put(ref,
                                nodeReferencesTotal.get(ref) + 1);
                    }
                    else {
                        nodeReferencesTotal.put(ref, 1);
                        nodeReferencesProcessed.put(ref, 0);
                    }
                }
            }
            totalRelationCount++;
        }
        // Then, mine all object definitions. 
        for (ParseTree variableExprNode : getDescendantsWithCat(node,
                "variableExpr")) {
            String ref;
            // might be a ref label rather than a counting number
            ParseTree varDef = getFirstChildWithCat(
                    variableExprNode.getParent(), "varDef");
            if (varDef != null) {
                // remove trailing #
                ref = varDef.getText().replaceFirst("#", "");
            }
            else {
                ref = variableCount.toString();
            }
            nodes2refs.put(variableExprNode, ref);
            Map<String, Object> object = processVariableExpr(variableExprNode);
            nodeVariables.put(ref, object);
            variableCount++;
            // Check if this object definition is part of a "direct declaration
            // relation", i.e. a relation which declares its operands directly
            // rather than using references to earlier declared objects. These 
            // objects must still be available for later reference, handle this 
            // here. Direct declaration relation is present when grandparent is
            // n_ary_linguistic_term node.
            if (getNodeCat(variableExprNode.getParent().getParent())
                    .equals("n_ary_linguistic_term")) {
                if (nodeReferencesTotal.containsKey(ref)) {
                    nodeReferencesTotal.put(ref,
                            nodeReferencesTotal.get(ref) + 1);
                }
                else {
                    nodeReferencesTotal.put(ref, 1);
                }
                // This is important for later relations wrapping the present 
                // relation. If the object isn't registered as processed, it 
                // won't be available  for referencing.
                nodeReferencesProcessed.put(ref, 1);
                // Register this node for latter wrapping in class.
                if (nodeReferencesTotal.get(ref) > 1) {
                    refClassMapping.put(ref, classCounter + 128);
                    objectsToWrapInClass.put(variableExprNode,
                            128 + classCounter++);
                }
            }
        }
    }


    private void processExprTop (ParseTree node) {
        List<ParseTree> andTopExprs = getChildrenWithCat(node, "andTopExpr");
        if (andTopExprs.size() > 1) {
            Map<String, Object> topOr = KoralObjectGenerator
                    .makeGroup(KoralOperation.DISJUNCTION);
            requestMap.put("query", topOr);
            objectStack.push(topOr);
        }
    }


    @SuppressWarnings("unchecked")
    private Map<String, Object> processVariableExpr (ParseTree node) {
        // simplex word or complex assignment (like qname = textSpec)?
        String firstChildNodeCat = getNodeCat(node.getChild(0));
        Map<String, Object> object = null;
        if (firstChildNodeCat.equals("node")) {
            object = KoralObjectGenerator.makeSpan();
        }
        else if (firstChildNodeCat.equals("tok")) {
            object = KoralObjectGenerator.makeToken();
            if (node.getChildCount() > 1) { // empty tokens do not wrap a term
                Map<String, Object> term = KoralObjectGenerator.makeTerm();
                term.put("layer", "orth");
                object.put("wrap", term);
            }
        }
        else if (firstChildNodeCat.equals("qName")) {
            // Only (foundry/)?layer specified.
            // May be token or span, depending on indicated layer! 
            // (e.g. cnx/cat=NP vs mate/pos=NN)
            // TODO generalize the list below -> look up layers associated with
            // tokens rather than spans somewhere
            Map<String, Object> qNameParse = parseQNameNode(node.getChild(0));
            if (Arrays.asList(new String[] { "p", "lemma", "m", "orth" })
                    .contains(qNameParse.get("layer"))) {
                object = KoralObjectGenerator.makeToken();
                Map<String, Object> term = KoralObjectGenerator.makeTerm();
                object.put("wrap", term);
                term.putAll(qNameParse);
            }
            else {

                object = KoralObjectGenerator.makeSpan();
                if (node.getChildCount() == 3) {
                    Map<String, Object> term = KoralObjectGenerator.makeTerm();
                    term.putAll(parseTextSpec(node.getChild(2)));
                    term.putAll(qNameParse);
                    object.put("wrap", term);
                }
                // EM: koral:span without key but layer and foundry should generally 
                // not be allowed except as relation type
                else{
                    addError(StatusCodes.MALFORMED_QUERY,
                            "Malformed query.");
                }
                
            }
        }
        else if (firstChildNodeCat.equals("textSpec")) {
            object = KoralObjectGenerator.makeToken();
            Map<String, Object> term = KoralObjectGenerator.makeTerm();
            object.put("wrap", term);
            term.put("layer", "orth");
            term.putAll(parseTextSpec(node.getChild(0)));
        }
        // (foundry/)?layer=key specification
        if (object.get("@type").equals("koral:token")
                && node.getChildCount() == 3) {
            HashMap<String, Object> term = (HashMap<String, Object>) object
                    .get("wrap");
            term.putAll(parseTextSpec(node.getChild(2)));
            term.put("match", parseMatchOperator(
                    getFirstChildWithCat(node, "eqOperator")));
        }

        // Check if there's a unary relation defined for this node
        // If yes, parse and retrieve it and put it in the object. 
        String ref = nodes2refs.get(node);
        if (unaryRelations.containsKey(ref)) {
            ArrayList<ParseTree> unaryTermsForRef = unaryRelations.get(ref);
            if (unaryTermsForRef.size() == 1) {
                object.put("attr", parseUnaryOperator(unaryTermsForRef.get(0)));
            }
            else {
                Map<String, Object> termGroup = KoralObjectGenerator
                        .makeTermGroup(KoralTermGroupRelation.AND);
                ArrayList<Object> operands = (ArrayList<Object>) termGroup
                        .get("operands");
                for (ParseTree unaryTerm : unaryTermsForRef) {
                    operands.add(parseUnaryOperator(unaryTerm));
                }
                object.put("attr", termGroup);
            }
        }
        if (object != null) {
            // query: object only, no relation
            if (totalRelationCount == 0) {
                putIntoSuperObject(object);
            }
            ParseTree parentsFirstChild = node.getParent().getChild(0);
            if (getNodeCat(parentsFirstChild).endsWith("#")) {
                nodeVariables.put(
                        getNodeCat(parentsFirstChild).replaceAll("#", ""),
                        object);
            }
            if (objectsToWrapInClass.containsKey(node)) {
                int classId = objectsToWrapInClass.get(node);
                object = KoralObjectGenerator.wrapInClass(object, classId);
            }
        }
        return object;
    }


    /**
     * Processes an operand node, creating a map for the operand
     * containing
     * all its information given in the node definition (referenced
     * via '#').
     * If this node has been referred to and used earlier, a reference
     * is
     * created in its place. The operand will be wrapped in a class
     * group if
     * necessary.
     * 
     * @param operandNode
     *            The operand node of a relation, e.g. '#1'
     * @return A map object with the appropriate KoralQuery
     *         representation
     *         of the operand
     */
    private Map<String, Object> retrieveOperand (ParseTree operandNode) {
        Map<String, Object> operand = null;
        if (!getNodeCat(operandNode.getChild(0)).equals("variableExpr")) {
            String ref = operandNode.getChild(0).toStringTree(parser)
                    .substring(1);
            operand = nodeVariables.get(ref);
            if (nodeReferencesTotal.get(ref) > 1) {
                if (nodeReferencesProcessed.get(ref) == 0) {
                    refClassMapping.put(ref, classCounter + 128);
                    operand = KoralObjectGenerator.wrapInClass(operand,
                            128 + classCounter++);
                }
                else if (nodeReferencesProcessed.get(ref) > 0
                        && nodeReferencesTotal.get(ref) > 1) {
                    try {
                        operand = KoralObjectGenerator.wrapInReference(
                                operandStack.pop(), refClassMapping.get(ref));
                    }
                    catch (NoSuchElementException e) {
                        operand = KoralObjectGenerator
                                .makeReference(refClassMapping.get(ref));
                    }
                }
                nodeReferencesProcessed.put(ref,
                        nodeReferencesProcessed.get(ref) + 1);
            }
        }
        else {
            operand = processVariableExpr(operandNode.getChild(0));
        }
        return operand;
    }


    /**
     * @param node
     * @return
     */
    private boolean checkOperandsProcessedPreviously (ParseTree node) {
        // We can assume two operands.
        ParseTree operand1 = node.getChild(0);
        ParseTree operand2 = node.getChild(2);
        if (checkOperandProcessedPreviously(operand1)
                || checkOperandProcessedPreviously(operand2)) {
            return true;
        }
        return false;
    }


    /**
     * @param operand
     * @return
     */
    private boolean checkOperandProcessedPreviously (ParseTree operand) {
        String operandRef = operand.getText();
        if (operandRef.startsWith("#")) {
            operandRef = operandRef.substring(1, operandRef.length());
            if (nodeReferencesProcessed.get(operandRef) > 0) {
                return true;
            }
        }
        return false;
    }


    @SuppressWarnings("unchecked")
    private void processN_ary_linguistic_term (ParseTree node) {
        relationCounter++;
        // Get operator and determine type of group (sequence/treeRelation/
        // relation/...). It's possible in Annis QL to concatenate operatiors,
        // so there may be several operators under one n_ary_linguistic_term 
        // node. Counter 'i' will iteratively point to all operator nodes 
        // (odd-numbered children) under this node.
        for (int i = 1; i < node.getChildCount(); i = i + 2) {
            ParseTree operandTree1 = node.getChild(i - 1);
            ParseTree operandTree2 = node.getChild(i + 1);
            String reltype = getNodeCat(node.getChild(i).getChild(0));

            Map<String, Object> group = null;
            ArrayList<Object> operands = null;
            // make sure one of the operands has already been put into a 
            // relation (if this is not the 1st relation). If none of the
            // operands has been ingested at a lower level (and is therefore
            // unavailable for refrencing), queue this relation for later 
            // processing.
            if (relationCounter != 1) {
                if (!checkOperandsProcessedPreviously(node)) {
                    queuedRelations.add(node);
                    relationCounter--;
                    if (verbose) {
                        System.out
                                .println("Adding to queue: " + node.getText());
                    }
                    objectsToPop.push(stackedObjects);
                    return;
                }
            }
            // Retrieve operands.
            Map<String, Object> operand1 = retrieveOperand(operandTree1);
            Map<String, Object> operand2 = retrieveOperand(operandTree2);
            // 'Proper' n_ary_linguistic_operators receive a considerably 
            // different serialisation than 'commonparent' and 'commonancestor'
            // For the latter cases, a dummy span is introduced and declared as
            // a span class that has a dominance relation towards the two 
            // operands, one after the other, thus resulting in two nested 
            // relations! A Poliqarp+ equivalent for A $ B would be
            // contains(focus(1:contains({1:<>},A)), B).
            // This is modeled here...
            if (reltype.equals("commonparent")
                    || reltype.equals("commonancestor")) {
                // make an (outer) group and an inner group containing the dummy 
                // node or previous relations
                group = KoralObjectGenerator.makeGroup(KoralOperation.RELATION);
                Map<String, Object> innerGroup = KoralObjectGenerator
                        .makeGroup(KoralOperation.RELATION);
                Map<String, Object> relation = KoralObjectGenerator
                        .makeRelation();
                Map<String, Object> term = KoralObjectGenerator.makeTerm();
                term.put("layer", "c");
                relation.put("wrap", term);
                // commonancestor is an indirect commonparent relation
                if (reltype.equals("commonancestor"))
                    relation.put("boundary",
                            KoralObjectGenerator.makeBoundary(1, null));
                group.put("relation", relation);
                innerGroup.put("relation", relation);
                // Get operands list before possible re-assignment of 'group' 
                // (see following 'if')
                ArrayList<Object> outerOperands = (ArrayList<Object>) group
                        .get("operands");
                ArrayList<Object> innerOperands = (ArrayList<Object>) innerGroup
                        .get("operands");
                // for lowest level, add the underspecified node as first 
                // operand and wrap it in a class group
                if (i == 1) {
                    innerOperands.add(KoralObjectGenerator.wrapInClass(
                            KoralObjectGenerator.makeSpan(),
                            classCounter + 128));
                    // add the first operand and wrap the whole group in a 
                    // focusing reference 
                    innerOperands.add(operand1);
                    innerGroup = KoralObjectGenerator
                            .wrapInReference(innerGroup, classCounter + 128);
                    outerOperands.add(innerGroup);
                }
                else {
                    outerOperands.add(operandStack.pop());
                }
                // Lookahead: if next operator is not commonparent or 
                // commonancestor, wrap in class for accessibility
                if (i < node.getChildCount() - 2
                        && !getNodeCat(node.getChild(i + 2).getChild(0))
                                .startsWith("common")) {
                    operand2 = KoralObjectGenerator.wrapInClass(operand2,
                            ++classCounter + 128);
                }
                outerOperands.add(operand2);
                // Wrap in another reference object in case other relations
                // are following
                if (i < node.getChildCount() - 2) {
                    group = KoralObjectGenerator.wrapInReference(group,
                            classCounter + 128);
                }
                // All other n-ary linguistic relations have special 'relation' 
                // attributes defined in KoralQ. and can be handled more easily
            }
            else {
                Map<String, Object> operatorGroup = parseOperatorNode(
                        node.getChild(i).getChild(0));
                // EM: change group type to enum KoralOperation
                String groupType;
                try {
                    groupType = (String) operatorGroup.get("groupType");
                }
                catch (ClassCastException | NullPointerException n) {
                    groupType = "relation";
                }

                if (groupType.equals("relation")
                //                        || groupType.equals("treeRelation")
                ) {
                    group = KoralObjectGenerator
                            .makeGroup(KoralOperation.RELATION);
                    Map<String, Object> relation = new HashMap<String, Object>();
                    putAllBut(relation, operatorGroup, "groupType");
                    group.put("relType", relation);
                }
                else if (groupType.equals("sequence")) {
                    group = KoralObjectGenerator
                            .makeGroup(KoralOperation.SEQUENCE);
                    putAllBut(group, operatorGroup, "groupType");
                }
                else if (groupType.equals("hierarchy")) {
                    Map<String, Object> attribute = null;
                    if (operatorGroup.containsKey("edgeValue")) {
                        String edgeValue = (String) operatorGroup
                                .get("edgeValue");
                        attribute = KoralObjectGenerator.makeTerm();
                        attribute.put("key", edgeValue);
                        attribute.put("match",
                                KoralMatchOperator.EQUALS.toString());
                        if (!inheritFoundryAndLayer(attribute, operand1)){
                            inheritFoundryAndLayer(attribute, operand2);
                        }
                    }

                    if (operatorGroup.containsKey("edgeType") && !operatorGroup
                            .get("edgeType").toString().equals("edgetype")) {
                        group = KoralObjectGenerator
                                .makeGroup(KoralOperation.RELATION);
                        Map<String, Object> relation = KoralObjectGenerator
                                .makeRelation();
                        // EM: for relations between arbitrary nodes e.g. secedge, rst without a label 
                        // need layer and foundry rewrite default in Kustvakt 
                        if (attribute != null) {
                            relation.put("wrap", attribute);
                        }
                        group.put("relType", relation);
                    }
                    else {
                        group = KoralObjectGenerator
                                .makeGroup(KoralOperation.HIERARCHY);
                        if (!operatorGroup.containsKey("edgeType")) {
                            group.put("edgeType", "edgetype");
                        }
                        if (attribute != null) {
                            operand1.put("attr", attribute);
                        }
                        if (operatorGroup.containsKey("boundary")) {
                            checkBoundary(
                                    (Map<String, Object>) operatorGroup
                                            .get("boundary"),
                                    operand1, operand2);
                        }
                    }

                    putAllBut(group, operatorGroup, "groupType", "edgeValue");
                }
                else if (groupType.equals("position")) {
                    group = new HashMap<String, Object>();
                    putAllBut(group, operatorGroup, "groupType");
                }

                // Get operands list before possible re-assignment of 'group'
                // (see following 'if')
                operands = (ArrayList<Object>) group.get("operands");

                ParseTree leftChildSpec = getFirstChildWithCat(
                        node.getChild(i).getChild(0), "@l");
                ParseTree rightChildSpec = getFirstChildWithCat(
                        node.getChild(i).getChild(0), "@r");
                if (leftChildSpec != null || rightChildSpec != null) {
                    KoralFrame frame = (leftChildSpec != null)
                            ? KoralFrame.STARTS_WITH : KoralFrame.ENDS_WITH;
                    ArrayList<KoralFrame> frames = new ArrayList<KoralFrame>();
                    frames.add(frame);
                    Map<String, Object> positionGroup = KoralObjectGenerator
                            .makePosition(frames);
                    operand2 = KoralObjectGenerator.wrapInClass(operand2,
                            ++classCounter + 128);
                    ((ArrayList<Object>) positionGroup.get("operands"))
                            .add(group);
                    ((ArrayList<Object>) positionGroup.get("operands"))
                            .add(KoralObjectGenerator
                                    .makeReference(classCounter + 128));
                    group = positionGroup;
                }

                // Wrap in reference object in case other relations follow
                if (i < node.getChildCount() - 2) {
                    group = KoralObjectGenerator.wrapInReference(group,
                            classCounter + 128);
                }

                // Inject operands.
                // -> Case distinction:
                if (node.getChildCount() == 3) {
                    // Things are easy when there's just one operator 
                    // (thus 3 children incl. operands)...
                    if (operand1 != null)
                        operands.add(operand1);
                    if (operand2 != null)
                        operands.add(operand2);
                }
                else {
                    // ... but things get a little more complicated here. The 
                    // AST is of this form: (operand1 operator1 operand2 
                    // operator2 operand3 operator3 ...), but we'll have
                    // to serialize it in a nested, binary way: (((operand1 
                    // operator1 operand2) operator2 operand3) operator3 ...).
                    // The following code will do just that:
                    if (i == 1) {
                        // for the first operator, include both operands
                        if (operand1 != null)
                            operands.add(operand1);
                        if (operand2 != null)
                            operands.add(KoralObjectGenerator.wrapInClass(
                                    operand2, 128 + classCounter++));
                        // Don't put this into the super object directly but 
                        // store on operandStack (because this group will have  
                        // to be an operand of a subsequent operator)
                        operandStack.push(group);
                        // for all subsequent operators, only take 2nd operand
                        // (1st was already added by previous operator)
                    }
                    else if (i < node.getChildCount() - 2) {
                        // for all intermediate operators, include other 
                        // previous groups and 2nd operand. Store this on the
                        // operandStack, too.
                        if (operand2 != null)
                            operands.add(KoralObjectGenerator.wrapInClass(
                                    operand2, 128 + classCounter++));
                        operands.add(0, operandStack.pop());
                        operandStack.push(group);
                    }
                    else if (i == node.getChildCount() - 2) {
                        // This is the last operator. Include 2nd operand only
                        if (operand2 != null)
                            operands.add(operand2);
                    }
                }
            }
            // Final step: decide what to do with the 'group' object, depending
            // on whether all relations have been processed
            if (i == node.getChildCount() - 2
                    && relationCounter == totalRelationCount) {
                putIntoSuperObject(group);
                if (!operandStack.isEmpty()) {
                    operands.add(0, operandStack.pop());
                }
                objectStack.push(group);
                stackedObjects++;
            }
            else {
                operandStack.push(group);
            }
        }
    }


    private void checkBoundary (Map<String, Object> boundary,
            Map<String, Object> operand1, Map<String, Object> operand2) {

        boolean isDirect = true;
        if (boundary.containsKey("min")) {
            if ((int) boundary.get("min") != 1) {
                isDirect = false;
            }
        }
        if (isDirect && boundary.containsKey("max")) {
            if ((int) boundary.get("max") != 1) {
                isDirect = false;
            }
        }

        if (!isDirect) {

            String layer1 = searchMap(operand1, "layer");
            String layer2 = searchMap(operand2, "layer");

            if (layer1 != null && layer2 != null && !layer1.equals(layer2)) {
                addError(StatusCodes.INCOMPATIBLE_OPERATOR_AND_OPERAND,
                        "Indirect dominance between operands of different layers is not possible.");
                return;
            }

            String foundry1 = searchMap(operand1, "foundry");
            String foundry2 = searchMap(operand2, "foundry");

            if (foundry1 != null && foundry2 != null
                    && !foundry1.equals(foundry2)) {
                addError(StatusCodes.INCOMPATIBLE_OPERATOR_AND_OPERAND,
                        "Indirect dominance between operands of different foundries is not possible.");
            }
        }
    }


    @SuppressWarnings("unchecked")
    private String searchMap (Map<String, Object> operand, String key) {

        String type = (String) operand.get("@type");
        if (type.equals(KoralType.TOKEN.toString())
                && operand.containsKey("wrap")) {
            return searchMap((Map<String, Object>) operand.get("wrap"), key);
        }
        else if (type.equals(KoralType.SPAN.toString())) {
            // EM: legacy, should be deprecated later
            if (operand.containsKey(key)) {
                return (String) operand.get(key);
            }
            else if (operand.containsKey("wrap")) {
                return searchMap((Map<String, Object>) operand.get("wrap"),
                        key);
            }
        }
        else if (type.equals(KoralType.TERM.toString())) {
            if (operand.containsKey(key)) {
                return (String) operand.get(key);
            }
        }
        else {
            // EM: (Annis) operand should not be of koral:group
            addError(StatusCodes.MALFORMED_QUERY,
                    "Cannot determine the " + key + ".");
        }

        return null;
    }


    private boolean inheritFoundryAndLayer (
            Map<String, Object> attribute, Map<String, Object> operand) {

        if (operand.containsKey("wrap")) {
            operand = (Map<String, Object>) operand.get("wrap");
            if (operand.containsKey("layer")) {
                attribute.put("layer", operand.get("layer"));
                if (operand.containsKey("foundry")) {
                    attribute.put("foundry", operand.get("foundry"));
                }
                return true;
            }
            else if (operand.containsKey("foundry")) {
                attribute.put("foundry", operand.get("foundry"));
                return true;
            }
            
        }
        return false;
    }


    /**
     * Parses a unary_linguistic_operator node. Possible operators
     * are:
     * root, arity, tokenarity. Operators are embedded into a
     * koral:term,
     * in turn wrapped by an 'attr' property in a koral:span.
     * 
     * @param node
     *            The unary_linguistic_operator node
     * @return A map containing the attr key, to be inserted into
     *         koral:span
     */
    private Map<String, Object> parseUnaryOperator (ParseTree node) {
        Map<String, Object> term = KoralObjectGenerator.makeTerm();
        String op = node.getChild(1).toStringTree(parser).substring(1);
        if (op.equals("arity") || op.equals("tokenarity")) {
            Map<String, Object> boundary = boundaryFromRangeSpec(
                    node.getChild(3), false);
            term.put(op, boundary);
        }
        else {
            term.put(op, true);
        }
        return term;
    }


    @SuppressWarnings("unchecked")
    private Map<String, Object> parseOperatorNode (ParseTree operatorNode) {
        Map<String, Object> relation = null;
        String operator = getNodeCat(operatorNode);
        // DOMINANCE
        if (operator.equals("dominance")) {
            //            relation = KoralObjectGenerator.makeRelation();
            relation = new HashMap<String, Object>();
            relation.put("groupType", "hierarchy");
            ParseTree edgeType = getFirstChildWithCat(operatorNode, "edgeType");
            ParseTree edgeSpecNode = getFirstChildWithCat(operatorNode,
                    "edgeSpec");
            ParseTree star = getFirstChildWithCat(operatorNode, "*");
            ParseTree rangeSpec = getFirstChildWithCat(operatorNode,
                    "rangeSpec");

            if (edgeType != null) {
                relation.put("edgeType",
                        edgeType.getChild(0).toStringTree(parser));
            }

            if (edgeSpecNode != null) {
                Map<String, Object> edgeSpec = parseEdgeSpec(edgeSpecNode);
                relation.put("edgeValue", edgeSpec.get("key"));
            }
            if (star != null)
                relation.put("boundary",
                        KoralObjectGenerator.makeBoundary(0, null));
            if (rangeSpec != null)
                relation.put("boundary", boundaryFromRangeSpec(rangeSpec));
        }
        else if (operator.equals("pointing")) {
            relation = KoralObjectGenerator.makeRelation();
            relation.put("groupType", "relation");
            ParseTree qName = getFirstChildWithCat(operatorNode, "qName");
            ParseTree edgeSpec = getFirstChildWithCat(operatorNode, "edgeSpec");
            ParseTree star = getFirstChildWithCat(operatorNode, "*");
            ParseTree rangeSpec = getFirstChildWithCat(operatorNode,
                    "rangeSpec");

            Map<String, Object> type = KoralObjectGenerator.makeTerm();
            if (qName != null) {
                type.putAll(parseQNameNode(qName));
            }
            if (edgeSpec != null) {
                type.putAll(parseEdgeSpec(edgeSpec));
            }
            relation.put("wrap", type);

            if (star != null) {
                relation.put("boundary",
                        KoralObjectGenerator.makeBoundary(0, null));
            }
            if (rangeSpec != null) {
                relation.put("boundary", boundaryFromRangeSpec(rangeSpec));
            }
        }
        else if (operator.equals("precedence")) {
            relation = new HashMap<String, Object>();
            relation.put("groupType", "sequence");
            ParseTree rangeSpec = getFirstChildWithCat(operatorNode,
                    "rangeSpec");
            ParseTree star = getFirstChildWithCat(operatorNode, "*");
            ArrayList<Object> distances = new ArrayList<Object>();
            if (star != null) {
                distances.add(KoralObjectGenerator.makeDistance("w", 0, null));
                relation.put("distances", distances);
            }
            if (rangeSpec != null) {
                distances.add(parseDistance(rangeSpec));
                relation.put("distances", distances);
            }
            relation.put("inOrder", true);
        }
        else if (operator.equals("spanrelation")) {
            String reltype = operatorNode.getChild(0).toStringTree(parser);
            ArrayList<KoralFrame> frames = new ArrayList<>();
            switch (reltype) {
                case "_=_":
                    frames.add(KoralFrame.MATCHES);
                    break;
                case "_l_":
                    frames.add(KoralFrame.STARTS_WITH);
                    frames.add(KoralFrame.MATCHES);
                    break;
                case "_r_":
                    frames.add(KoralFrame.ENDS_WITH);
                    frames.add(KoralFrame.MATCHES);
                    break;
                case "_i_":
                    frames.add(KoralFrame.IS_AROUND);
                    break;
                case "_o_":
                    frames.add(KoralFrame.OVERLAPS_LEFT);
                    frames.add(KoralFrame.OVERLAPS_RIGHT);
                    break;
                case "_ol_":
                    frames.add(KoralFrame.OVERLAPS_LEFT);
                    break;
                case "_or_":
                    frames.add(KoralFrame.OVERLAPS_RIGHT);
                    break;
            }
            relation = KoralObjectGenerator.makePosition(frames);
            relation.put("groupType", "position");
        }
        else if (operator.equals("near")) {
            relation = new HashMap<String, Object>();
            relation.put("groupType", "sequence");
            ParseTree rangeSpec = getFirstChildWithCat(operatorNode,
                    "rangeSpec");
            ParseTree star = getFirstChildWithCat(operatorNode, "*");
            ArrayList<Object> distances = new ArrayList<Object>();
            if (star != null) {
                distances.add(KoralObjectGenerator.makeDistance("w", 0, null));
                relation.put("distances", distances);
            }
            if (rangeSpec != null) {
                distances.add(parseDistance(rangeSpec));
                relation.put("distances", distances);
            }
            relation.put("inOrder", false);
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
    private Map<String, Object> parseEdgeSpec (ParseTree edgeSpec) {
        List<ParseTree> annos = getChildrenWithCat(edgeSpec, "edgeAnno");
        if (annos.size() != 1) {
            addWarning(StatusCodes.MALFORMED_QUERY,
                    "Multiple annotations are not allowed. "
                            + "Processed only the first annotation.");
        }
        return parseEdgeAnno(annos.get(0));

        //        else {
        //            Map<String, Object> termGroup = KoralObjectGenerator
        //                    .makeTermGroup(KoralTermGroupRelation.AND);
        //            ArrayList<Object> operands = (ArrayList<Object>) termGroup
        //                    .get("operands");
        //            for (ParseTree anno : annos) {
        //                operands.add(parseEdgeAnno(anno));
        //            }
        //            return termGroup;
        //        }
    }


    private Map<String, Object> parseEdgeAnno (ParseTree edgeAnnoSpec) {
        Map<String, Object> edgeAnno = KoralObjectGenerator.makeTerm();
        ParseTree textSpecNode = getFirstChildWithCat(edgeAnnoSpec, "textSpec");
        //        ignored
        //        ParseTree keyNode = getFirstChildWithCat(edgeAnnoSpec, "key");
        //        ParseTree layerNode = getFirstChildWithCat(edgeAnnoSpec, "layer");
        //        ParseTree foundryNode = getFirstChildWithCat(edgeAnnoSpec, "foundry");
        ParseTree matchOperatorNode = getFirstChildWithCat(edgeAnnoSpec,
                "eqOperator");
        //        if (foundryNode != null)
        //            edgeAnno.put("foundry",
        //                    foundryNode.getChild(0).toStringTree(parser));
        //        if (layerNode != null)
        //            edgeAnno.put("layer", layerNode.getChild(0).toStringTree(parser));
        //        if (keyNode != null)
        //            edgeAnno.put("key", keyNode.getChild(0).toStringTree(parser));
        edgeAnno.putAll(parseTextSpec(textSpecNode, "key"));
        edgeAnno.put("match", parseMatchOperator(matchOperatorNode));
        return edgeAnno;
    }


    private Map<String, Object> boundaryFromRangeSpec (ParseTree rangeSpec) {
        return boundaryFromRangeSpec(rangeSpec, true);
    }


    private Map<String, Object> boundaryFromRangeSpec (ParseTree rangeSpec,
            boolean expandToMax) {
        Integer min = Integer
                .parseInt(rangeSpec.getChild(0).toStringTree(parser));
        Integer max = min;
        if (expandToMax)
            max = null;
        if (rangeSpec.getChildCount() == 3)
            max = Integer.parseInt(rangeSpec.getChild(2).toStringTree(parser));
        return KoralObjectGenerator.makeBoundary(min, max);
    }


    private Map<String, Object> parseDistance (ParseTree rangeSpec) {
        String minString = rangeSpec.getChild(0).toStringTree(parser);
        String maxString = null; // not always given, prevent NPE
        if (minString.equals("0")) {
            addError(StatusCodes.MALFORMED_QUERY, "Distance may not be 0!");
            return KoralObjectGenerator.makeDistance("w", 0, 0);
        }
        // decrease by 1 to account for disparity between ANNIS distance and 
        // koral:distance (ANNIS "x .1,3 y" means distance range 0,2 in KoralQ)
        Integer min = Integer.parseInt(minString) - 1;
        Integer max = null;
        if (rangeSpec.getChildCount() == 3) {
            maxString = rangeSpec.getChild(2).toStringTree(parser);
            max = Integer.parseInt(maxString) - 1;
        }
        return KoralObjectGenerator.makeDistance("w", min, max);
    }


    private Map<String, Object> parseTextSpec (ParseTree node) {
        return parseTextSpec(node, "key");
    }


    private Map<String, Object> parseTextSpec (ParseTree node, String name) {
        Map<String, Object> term = new HashMap<String, Object>();
        if (hasChild(node, "regex")) {
            term.put("type", "type:regex");
            term.put(name, node.getChild(0).getChild(0).toStringTree(parser)
                    .replaceAll("/", ""));
        }
        else {
            term.put(name, node.getChild(1).toStringTree(parser));
        }
        term.put("match", "match:eq");
        return term;
    }


    /**
     * Parses the match operator (= or !=)
     * 
     * @param node
     * @return
     */
    private String parseMatchOperator (ParseTree node) {
        if (node.getChildCount() > 0) {
            return node.getChild(0).getText().equals("=") ? "match:eq"
                    : "match:ne";
        }
        return "match:eq";
    }


    private Map<String, Object> parseQNameNode (ParseTree node) {
        Map<String, Object> fields = new HashMap<String, Object>();
        ParseTree layerNode = getFirstChildWithCat(node, "layer");
        ParseTree foundryNode = getFirstChildWithCat(node, "foundry");
        if (foundryNode != null)
            fields.put("foundry", foundryNode.getChild(0).toStringTree(parser));
        String layer = layerNode.getChild(0).toStringTree(parser);
        if (layer.equals("pos"))
            layer = "p";
        if (layer.equals("cat"))
            layer = "c";
        fields.put("layer", layer);
        return fields;
    }


    private void putIntoSuperObject (Map<String, Object> object) {
        putIntoSuperObject(object, 0);
    }


    @SuppressWarnings({ "unchecked" })
    private void putIntoSuperObject (Map<String, Object> object,
            int objStackPosition) {
        if (objectStack.size() > objStackPosition) {
            ArrayList<Object> topObjectOperands = (ArrayList<Object>) objectStack
                    .get(objStackPosition).get("operands");
            if (!invertedOperandsLists.contains(topObjectOperands)) {
                topObjectOperands.add(object);
            }
            else {
                topObjectOperands.add(0, object);
            }
        }
        else {
            requestMap.put("query", object);
        }
    }


    private void putAllBut (Map<String, Object> container,
            Map<String, Object> input, String ... keys) {

        Set<String> names = input.keySet();
        for (String k : keys) {
            names.remove(k);
        }

        for (String n : names) {
            container.put(n, input.get(n));
        }

        //        for (String key : input.keySet()) {
        //            if (!key.equals("groupType")) {
        //                container.put(key, input.get(key));
        //            }
        //        }
    }


    private ParserRuleContext parseAnnisQuery (String query) {
        Lexer lexer = new AqlLexer((CharStream) null);
        ParserRuleContext tree = null;
        Antlr4DescriptiveErrorListener errorListener = new Antlr4DescriptiveErrorListener(
                query);
        // Like p. 111
        try {
            // Tokenize input data
            ANTLRInputStream input = new ANTLRInputStream(query);
            lexer.setInputStream(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            parser = new AqlParser(tokens);
            // Don't throw out erroneous stuff
            parser.setErrorHandler(new BailErrorStrategy());
            lexer.removeErrorListeners();
            lexer.addErrorListener(errorListener);
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);
            // Get starting rule from parser
            Method startRule = AqlParser.class.getMethod("start");
            tree = (ParserRuleContext) startRule.invoke(parser,
                    (Object[]) null);
        }
        // Some things went wrong ...
        catch (Exception e) {
            log.error("Could not parse query. "
                    + "Please make sure it is well-formed.");
            log.error(errorListener.generateFullErrorMsg().toString());
            addError(errorListener.generateFullErrorMsg());
        }
        return tree;
    }
}