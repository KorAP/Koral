package de.ids_mannheim.korap.query.serialize;

import de.ids_mannheim.korap.query.object.ClassRefOp;
import de.ids_mannheim.korap.query.object.KoralFrame;
import de.ids_mannheim.korap.query.object.KoralMatchOperator;
import de.ids_mannheim.korap.query.object.KoralOperation;
import de.ids_mannheim.korap.query.object.KoralTermGroupRelation;
import de.ids_mannheim.korap.query.parse.poliqarpplus.PoliqarpPlusLexer;
import de.ids_mannheim.korap.query.parse.poliqarpplus.PoliqarpPlusParser;
import de.ids_mannheim.korap.query.serialize.util.Antlr4DescriptiveErrorListener;
import de.ids_mannheim.korap.query.serialize.util.KoralObjectGenerator;
import de.ids_mannheim.korap.query.serialize.util.StatusCodes;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Map representation of PoliqarpPlus syntax tree as returned by
 * ANTLR.
 * Most centrally, this class maintains a set of nested maps and
 * lists which represent the JSON tree, which is built by the JSON
 * serialiser on basis of the {@link #requestMap} at the root of
 * the tree. <br/>
 * The class further maintains a set of stacks which effectively
 * keep track of which objects to embed in which containing
 * objects.
 * 
 * This class expects the Poliqarp+ ANTLR grammar shipped with Koral
 * v0.3.0.
 * 
 * @author Joachim Bingel (bingel@ids-mannheim.de)
 * @author Eliza Margaretha (margaretha@ids-mannheim.de)
 * @author Nils Diewald (diewald@ids-mannheim.de)
 * @version 0.3.0
 * @since 0.1.0
 */
public class PoliqarpPlusQueryProcessor extends Antlr4AbstractQueryProcessor {

    private static final boolean DEBUG = false;
    private static Logger log = LoggerFactory
            .getLogger(PoliqarpPlusQueryProcessor.class);
    private int classCounter = 1;

    Map<ParseTree, Integer> classWrapRegistry = new HashMap<ParseTree, Integer>();


    /**
     * Constructor
     * 
     * @param query
     *            The syntax tree as returned by ANTLR
     */
    public PoliqarpPlusQueryProcessor (String query) {
        KoralObjectGenerator.setQueryProcessor(this);
        process(query);
        if (DEBUG) { 
            log.debug(">>> " + requestMap.get("query") + " <<<");
        }
    }


    @Override
    public void process (String query) {
        ParseTree tree;
        tree = parsePoliqarpQuery(query);
        // fixme: not required!?
        super.parser = this.parser;
        if (DEBUG) {
            log.debug("Processing PoliqarpPlus query: " + query);
        }
        if (tree != null) {
            if (DEBUG){
                log.debug("ANTLR parse tree: " + tree.toStringTree(parser));
            }
            processNode(tree);
        }
        else {
            addError(StatusCodes.MALFORMED_QUERY,
                    "Could not parse query >>> " + query + " <<<.");
        }
    }


    /**
     * Recursively calls itself with the children of the currently
     * active node, traversing the tree nodes in a top-down,
     * depth-first fashion. A list is maintained that contains all
     * visited nodes which have been directly addressed by their
     * (grand-/grand-grand-/...) parent nodes, such that some
     * processing time is saved, as these node will not be processed.
     * This method is effectively a list of if-statements that are
     * responsible for treating the different node types correctly and
     * filling the respective maps/lists.
     * 
     * @param node
     *            The currently processed node. The process(String
     *            query) method calls this method with the root.
     */
    private void processNode (ParseTree node) {
        // Top-down processing
        if (visited.contains(node))
            return;
        else
            visited.add(node);

        String nodeCat = getNodeCat(node);
        openNodeCats.push(nodeCat);

        stackedObjects = 0;

        if (verbose) {
            System.err.println(" " + objectStack);
            System.out.println(openNodeCats);
        }

        // Check if (the translation of) this node is registered to be wrapped
        // in a class, e.g. by an alignment operation
        if (classWrapRegistry.containsKey(node)) {
            Integer classId = classWrapRegistry.get(node);
            Map<String, Object> spanClass = KoralObjectGenerator
                    .makeSpanClass(classId);
            putIntoSuperObject(spanClass);
            objectStack.push(spanClass);
            stackedObjects++;
        }

        /*
         ****************************************************************
         **************************************************************** 
         *          Processing individual node categories               *
         ****************************************************************
         ****************************************************************
         */
        if (nodeCat.equals("segment")) {
            processSegment(node);
        }

        else if (nodeCat.equals("sequence")) {
            processSequence(node);
        }

        else if (nodeCat.equals("emptyTokenSequence")) {
            processEmptyTokenSequence(node);
        }

        else if (nodeCat.equals("emptyTokenSequenceClass")) {
            processEmptyTokenSequenceClass(node);
        }

        else if (nodeCat.equals("token")) {
            processToken(node);
        }

        else if (nodeCat.equals("alignment")) {
            processAlignment(node);
        }

        else if (nodeCat.equals("span")) {
            processSpan(node);
        }

        else if (nodeCat.equals("disjunction")) {
            processDisjunction(node);
        }

        else if (nodeCat.equals("position")) {
            processPosition(node);
        }

        else if (nodeCat.equals("relation")) {
            processRelation(node);
        }

        else if (nodeCat.equals("spanclass")) {
            processSpanclass(node);
        }

        else if (nodeCat.equals("matching")) {
            processMatching(node);
        }

        else if (nodeCat.equals("submatch")) {
            processSubmatch(node);
        }

        else if (nodeCat.equals("queryref")) {
            processQueryref(node);
        }

        else if (nodeCat.equals("meta")) {
            processMeta(node);
        }

        else if (nodeCat.equals("within")
                 && !getNodeCat(node.getParent()).equals("position")) {
            processWithin(node);
        }

        objectsToPop.push(stackedObjects);

        /*
         ****************************************************************
         **************************************************************** 
         *  Recursion until 'request' node (root of tree) is processed  *
         ****************************************************************
         ****************************************************************
         */
        for (int i = 0; i < node.getChildCount(); i++) {
            processNode(node.getChild(i));
        }

        // Stuff that happens when leaving a node (taking items off stacks)
        for (int i = 0; i < objectsToPop.get(0); i++) {
            objectStack.pop();
        }
        objectsToPop.pop();
        openNodeCats.pop();
    }


    /**
     * Processes a 'segment' node.
     * 
     * @param node
     */
    private void processSegment (ParseTree node) {
        // Cover possible quantification (i.e. repetition) of segment
        ParseTree quantification = getFirstChildWithCat(node, "repetition");
        if (quantification != null) {
            Map<String, Object> quantGroup = KoralObjectGenerator
                    .makeGroup(KoralOperation.REPETITION);
            Integer[] minmax = parseRepetition(quantification);
            quantGroup.put("boundary",
                    KoralObjectGenerator.makeBoundary(minmax[0], minmax[1]));
            putIntoSuperObject(quantGroup);
            objectStack.push(quantGroup);
            stackedObjects++;
        }
    }


    /**
     * Process a 'sequence' node.
     * 
     * @param node
     */
    private void processSequence (ParseTree node) {
        // skip in case of emptyTokenSequence or emptyTokenSequenceClass
        if (node.getChildCount() == 1 && getNodeCat(node.getChild(0))
                .startsWith("emptyTokenSequence")) {
            return;
        }
        // skip in case this sequence is just a container for an alignment 
        // node with just one child
        if (node.getChildCount() == 1
                && getNodeCat(node.getChild(0)).equals("alignment")) {
            ParseTree alignmentNode = node.getChild(0);
            if (alignmentNode.getChildCount() == 2) { // one child is the 
                // alignment operator (^), the other a segment
                return;
            }
        }
        Map<String, Object> sequence = KoralObjectGenerator
                .makeGroup(KoralOperation.SEQUENCE);

        putIntoSuperObject(sequence);
        objectStack.push(sequence);
        stackedObjects++;
    }


    @SuppressWarnings("unchecked")
    /**
     * empty tokens at beginning/end of sequence
     * 
     * @param node
     */
    private void processEmptyTokenSequence (ParseTree node) {
        Integer[] minmax = parseEmptySegments(node);
        // object will be either a repetition group or a single empty
        // token
        Map<String, Object> object;
        Map<String, Object> emptyToken = KoralObjectGenerator
                .makeToken();
        if (minmax[0] != 1 || minmax[1] == null || minmax[1] != 1) {
            object = KoralObjectGenerator.makeRepetition(minmax[0], minmax[1]);
            ((ArrayList<Object>) object.get("operands")).add(emptyToken);
        }
        else {
            object = emptyToken;
        }
        putIntoSuperObject(object);
        objectStack.push(object);
        stackedObjects++;
    }


    private void processQueryref (ParseTree node) {

        String queryNameStr = "";
        
        if (getNodeCat(node.getChild(2)).equals("user")) {
            queryNameStr = node.getChild(2).getText();
            queryNameStr += '/';
            queryNameStr += node.getChild(4).getText();
        }
        else {
            queryNameStr = node.getChild(2).getText();
        }

        Map<String, Object> object = KoralObjectGenerator.makeQueryRef(queryNameStr);
        putIntoSuperObject(object);
        objectStack.push(object);
        stackedObjects++;
    }
    

    private void processEmptyTokenSequenceClass (ParseTree node) {
        int classId = 1;
        if (hasChild(node, "spanclass_id")) {
            classId = Integer.parseInt(
                    node.getChild(1).getChild(0).toStringTree(parser));
        }
        Map<String, Object> classGroup = KoralObjectGenerator
                .makeSpanClass(classId);
        addHighlightClass(classId);
        putIntoSuperObject(classGroup);
        objectStack.push(classGroup);
        stackedObjects++;
    }


    private void processToken (ParseTree node) {
        Map<String, Object> token = KoralObjectGenerator.makeToken();
        // handle negation
        List<ParseTree> negations = getChildrenWithCat(node, "!");
        int termOrTermGroupChildId = 1;
        boolean negated = false;
        boolean isRegex = false;
        if (negations.size() % 2 == 1) {
            negated = true;
            termOrTermGroupChildId += negations.size();
        }

        if (getNodeCat(node.getChild(0)).equals("key")) {
            // no 'term' child, but direct key specification: process here
            Map<String, Object> term = KoralObjectGenerator
                    .makeTerm();

            String key = node.getChild(0).getText();

            if (getNodeCat(node.getChild(0).getChild(0)).equals("regex")) {
                isRegex = true;
                term.put("type", "type:regex");

                // fixme: use stream with offset to get text!
                // TokenStream stream = parser.getTokenStream();
                // key = stream.getText(node.getChild(0).getSourceInterval());
                key = key.substring(1, key.length() - 1);
            }
            term.put("layer", "orth");
            term.put("key", key);
            KoralMatchOperator matches = negated ? KoralMatchOperator.NOT_EQUALS
                    : KoralMatchOperator.EQUALS;
            term.put("match", matches.toString());
            ParseTree flagNode = getFirstChildWithCat(node, "flag");
            if (flagNode != null) {
                ArrayList<String> flags = new ArrayList<String>();
                // substring removes leading slash '/'
                String flag = getNodeCat(flagNode.getChild(0)).substring(1);
                if (flag.contains("i"))
                    flags.add("flags:caseInsensitive");
                if (flag.contains("x")) {
                    term.put("type", "type:regex");
                    if (!isRegex) {
                        key = QueryUtils.escapeRegexSpecialChars(key);
                    }
                    // overwrite key
                    term.put("key", ".*?" + key + ".*?");
                }
                if (!flags.isEmpty()) {
                    term.put("flags", flags);
                }
            }
            token.put("wrap", term);
        }
        else {
            // child is 'term' or 'termGroup' -> process in extra method
            Map<String, Object> termOrTermGroup = parseTermOrTermGroup(
                    node.getChild(termOrTermGroupChildId), negated);
            token.put("wrap", termOrTermGroup);
        }
        putIntoSuperObject(token);
        visited.addAll(getChildren(node));
    }


    /**
     * Processes an 'alignment' node. These nodes represent alignment
     * anchors
     * which introduce an alignment ruler in KWIC display. The
     * serialization
     * for this expects the two segments to the left and to the right
     * of each
     * anchor to be wrapped in classes, then these classes are
     * referenced in
     * the <tt>alignment</tt> array of the request tree.
     * 
     * @param node
     */
    private void processAlignment (ParseTree node) {
        int i = 1;
        if (node.getChild(0).getText().equals("^")) {
            i = 0; // if there is no first child (anchor is at extreme left or
                  // right of segment), start counting at 0 in the loop
        }
        // for every alignment anchor, get its left and right child and register
        // these to be wrapped in classes.
        for (; i < node.getChildCount(); i += 2) {
            int alignmentFirstArg = -1;
            int alignmentSecondArg = -1;
            ParseTree leftChild = node.getChild(i - 1);
            ParseTree rightChild = node.getChild(i + 1);
            if (leftChild != null) {
                if (!classWrapRegistry.containsKey(leftChild)) {
                    alignmentFirstArg = classCounter++;
                    classWrapRegistry.put(leftChild, alignmentFirstArg);
                }
                else {
                    alignmentFirstArg = classWrapRegistry.get(leftChild);
                }
            }
            if (rightChild != null) {
                if (!classWrapRegistry.containsKey(rightChild)) {
                    alignmentSecondArg = classCounter++;
                    classWrapRegistry.put(rightChild, alignmentSecondArg);
                }
                else {
                    alignmentSecondArg = classWrapRegistry.get(rightChild);
                }
            }
            addAlignment(alignmentFirstArg, alignmentSecondArg);
        }
    }


    private void processSpan (ParseTree node) {
        List<ParseTree> negations = getChildrenWithCat(node, "!");
        boolean negated = false;
        if (negations.size() % 2 == 1)
            negated = true;
        Map<String, Object> span = KoralObjectGenerator.makeSpan();
        Map<String, Object> wrappedTerm = KoralObjectGenerator
                .makeTerm();
        span.put("wrap", wrappedTerm);
        ParseTree keyNode = getFirstChildWithCat(node, "key");
        ParseTree layerNode = getFirstChildWithCat(node, "layer");
        ParseTree foundryNode = getFirstChildWithCat(node, "foundry");
        ParseTree termOpNode = getFirstChildWithCat(node, "termOp");
        ParseTree termNode = getFirstChildWithCat(node, "term");
        ParseTree termGroupNode = getFirstChildWithCat(node, "termGroup");
        if (foundryNode != null)
            wrappedTerm.put("foundry", foundryNode.getText());
        if (layerNode != null) {
            String layer = layerNode.getText();
            if (layer.equals("base"))
                layer = "lemma";
            wrappedTerm.put("layer", layer);
        }
        String key = keyNode.getText();
        // check if key is regular expression
        if (hasChild(keyNode, "regex")) {
            // remove leading/trailing double quotes
            key = key.substring(1, key.length() - 1);
            wrappedTerm.put("type", "type:regex");
        }
        wrappedTerm.put("key", key);
        if (termOpNode != null) {
            String termOp = termOpNode.getText();
            if (termOp.equals("=="))
                wrappedTerm.put("match", KoralMatchOperator.EQUALS.toString());
            else if (termOp.equals("!="))
                wrappedTerm.put("match", KoralMatchOperator.NOT_EQUALS.toString());
        }
        if (termNode != null) {
            Map<String, Object> termOrTermGroup = parseTermOrTermGroup(
                    termNode, negated, "span");
            span.put("attr", termOrTermGroup);
        }
        if (termGroupNode != null) {
            Map<String, Object> termOrTermGroup = parseTermOrTermGroup(
                    termGroupNode, negated, "span");
            span.put("attr", termOrTermGroup);
        }
        putIntoSuperObject(span);
        objectStack.push(span);
        stackedObjects++;
    }


    private void processDisjunction (ParseTree node) {
        Map<String, Object> disjunction = KoralObjectGenerator
                .makeGroup(KoralOperation.DISJUNCTION);
        putIntoSuperObject(disjunction);
        objectStack.push(disjunction);
        stackedObjects++;
    }


    private void processPosition (ParseTree node) {
        Map<String, Object> position = parseFrame(node.getChild(0));
        putIntoSuperObject(position);
        objectStack.push(position);
        stackedObjects++;
    }


    private void processRelation (ParseTree node) {
        Map<String, Object> relationGroup = KoralObjectGenerator
                .makeGroup(KoralOperation.RELATION);
        Map<String, Object> relation = KoralObjectGenerator
                .makeRelation();
        Map<String, Object> term = KoralObjectGenerator.makeTerm();
        relationGroup.put("relation", relation);
        relation.put("wrap", term);
        if (node.getChild(0).getText().equals("dominates")) {
            term.put("layer", "c");
        }
        else if (node.getChild(0).getText().equals("dependency")) {
            term.put("layer", "d");
        }
        ParseTree relSpec = getFirstChildWithCat(node, "relSpec");
        ParseTree repetition = getFirstChildWithCat(node, "repetition");
        if (relSpec != null) {
            ParseTree foundry = getFirstChildWithCat(relSpec, "foundry");
            ParseTree layer = getFirstChildWithCat(relSpec, "layer");
            ParseTree key = getFirstChildWithCat(relSpec, "key");
            if (foundry != null)
                term.put("foundry", foundry.getText());
            if (layer != null)
                term.put("layer", layer.getText());
            if (key != null)
                term.put("key", key.getText());
        }
        if (repetition != null) {
            Integer[] minmax = parseRepetition(repetition);
            relation.put("boundary",
                    KoralObjectGenerator.makeBoundary(minmax[0], minmax[1]));
        }
        putIntoSuperObject(relationGroup);
        objectStack.push(relationGroup);
        stackedObjects++;
    }


    private void processSpanclass (ParseTree node) {
        // Step I: get info
        int classId = 1;
        if (getNodeCat(node.getChild(1)).equals("spanclass_id")) {
            String ref = node.getChild(1).getChild(0).toStringTree(parser);
            try {
                classId = Integer.parseInt(ref);
            }
            catch (NumberFormatException e) {
                String msg = "The specified class reference in the "
                        + "focus/split-Operator is not a number: " + ref;
                classId = 0;
                log.error(msg);
                addError(StatusCodes.INVALID_CLASS_REFERENCE, msg);
            }
        }
        Map<String, Object> classGroup = KoralObjectGenerator
                .makeSpanClass(classId);
        addHighlightClass(classId);
        putIntoSuperObject(classGroup);
        objectStack.push(classGroup);
        stackedObjects++;

    }


    private void processMatching (ParseTree node) {
        // Step I: get info
        ArrayList<Integer> classRefs = new ArrayList<Integer>();
        ClassRefOp classRefOp = null;
        if (getNodeCat(node.getChild(2)).equals("spanclass_id")) {
            ParseTree spanNode = node.getChild(2);
            for (int i = 0; i < spanNode.getChildCount() - 1; i++) {
                String ref = spanNode.getChild(i).getText();
                if (ref.equals("|") || ref.equals("&")) {
                    classRefOp = ref.equals("|") ? ClassRefOp.INTERSECTION : ClassRefOp.UNION;
                }
                else {
                    try {
                        int classRef = Integer.parseInt(ref);
                        classRefs.add(classRef);
                    }
                    catch (NumberFormatException e) {
                        String err = "The specified class reference in the "
                                + "shrink/split-Operator is not a number.";
                        addError(StatusCodes.INVALID_CLASS_REFERENCE, err);
                    }
                }
            }
        }
        else {
            classRefs.add(1); // default
        }
        Map<String, Object> referenceGroup = KoralObjectGenerator
                .makeReference(classRefs);

        String type = node.getChild(0).toStringTree(parser);
        // Default is focus(), if deviating catch here
        if (type.equals("split"))
            referenceGroup.put("operation", "operation:split");
        if (classRefOp != null) {
            referenceGroup.put("classRefOp", classRefOp.toString());
        }
        ArrayList<Object> referenceOperands = new ArrayList<Object>();
        referenceGroup.put("operands", referenceOperands);
        // Step II: decide where to put the group
        putIntoSuperObject(referenceGroup);
        objectStack.push(referenceGroup);
        stackedObjects++;
        visited.add(node.getChild(0));
    }


    private void processSubmatch (ParseTree node) {
        Map<String, Object> submatch = KoralObjectGenerator
                .makeReference(null);
        submatch.put("operands", new ArrayList<Object>());
        ParseTree startpos = getFirstChildWithCat(node, "startpos");
        ParseTree length = getFirstChildWithCat(node, "length");
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
     * 
     * @param node
     */
    private void processMeta (ParseTree node) {
        addWarning("You used the 'meta' keyword in a PoliqarpPlus query. This"
                + " feature is currently not supported. Please use virtual "
                + "collections to restrict documents by metadata.");
        CollectionQueryProcessor cq = new CollectionQueryProcessor(
                node.getChild(1).getText());
        requestMap.put("collection", cq.getRequestMap().get("collection"));
        visited.addAll(getChildren(node));
    }


    @SuppressWarnings("unchecked")
    private void processWithin (ParseTree node) {
        ParseTree domainNode = node.getChild(1);
        String domain = getNodeCat(domainNode);
        Map<String, Object> span = KoralObjectGenerator
                .makeSpan(domain);
        Map<String, Object> queryObj = (Map<String, Object>) requestMap
                .get("query");
        ArrayList<KoralFrame> frames = new ArrayList<KoralFrame>();
        frames.add(KoralFrame.IS_AROUND);
        Map<String, Object> contains = KoralObjectGenerator
                .makePosition(frames);
        ArrayList<Object> operands = (ArrayList<Object>) contains
                .get("operands");
        operands.add(span);

        operands.add(queryObj);
        requestMap.put("query", contains);
        visited.add(node.getChild(0));
        visited.add(node.getChild(1));
    }


    /**
     * Parses a repetition node
     * 
     * @param node
     * @return A two-element array, of which the first element is an
     *         int representing the minimal number of repetitions of
     *         the quantified element, and the second element
     *         representing the maximal number of repetitions
     */
    private Integer[] parseRepetition (ParseTree node) {
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
            }
            else if (kleeneOp.equals("+")) {
                min = 1;
                maxInfinite = true;
            }
            if (kleeneOp.equals("?")) {
                max = 1;
            }
        }
        else {
            // Range node of form "{ min , max }" or "{ max }" or
            // "{ , max }" or "{ min , }"
            ParseTree minNode = getFirstChildWithCat(repetitionTypeNode, "min");
            ParseTree maxNode = getFirstChildWithCat(repetitionTypeNode, "max");
            if (maxNode != null)
                max = Integer.parseInt(maxNode.getText());
            else
                maxInfinite = true;
            // min is optional: if not specified, min = max
            if (minNode != null)
                min = Integer.parseInt(minNode.getText());
            else if (hasChild(repetitionTypeNode, ","))
                min = 0;
            else {
                min = max;
                // addWarning("Your query contains a segment of the form {n}, where n is some number. This expression is ambiguous. " +
                // "It could mean a repetition (\"Repeat the previous element n times!\") or a word form that equals the number, "+
                // "enclosed by a \"class\" (which is denoted by braces like '{x}', see the documentation on classes)."+
                // "KorAP has by default interpreted the segment as a repetition statement. If you want to express the"+
                // "number as a word form inside a class, use the non-shorthand form {[orth=n]}.");
            }
        }
        if (maxInfinite) {
            max = null;
        }
        return new Integer[] { min, max };
    }


    private Map<String, Object> parseFrame (ParseTree node) {
        String operator = node.toStringTree(parser).toLowerCase();
        ArrayList<KoralFrame> frames = new ArrayList<KoralFrame>();
        switch (operator) {
            case "contains":
                frames.add(KoralFrame.IS_AROUND);
                break;
            case "matches":
                frames.add(KoralFrame.MATCHES);
                break;
            case "startswith":
                frames.add(KoralFrame.STARTS_WITH);
                frames.add(KoralFrame.MATCHES);
                break;
            case "endswith":
                frames.add(KoralFrame.ENDS_WITH);
                frames.add(KoralFrame.MATCHES);
                break;
            case "overlaps":
                frames.add(KoralFrame.OVERLAPS_LEFT);
                frames.add(KoralFrame.OVERLAPS_RIGHT);
                break;
        }
        return KoralObjectGenerator.makePosition(frames);
    }


    private Map<String, Object> parseTermOrTermGroup (ParseTree node,
            boolean negated) {
        return parseTermOrTermGroup(node, negated, "token");
    }


    /**
     * Parses a (term) or (termGroup) node
     * 
     * @param node
     * @param negatedGlobal
     *            Indicates whether the term/termGroup is globally
     *            negated, e.g. through a negation operator preceding
     *            the related token like "![base=foo]". Global
     *            negation affects the term's "match" parameter.
     * @param mode
     *            'token' or 'span' (tokens and spans are treated
     *            differently).
     * @return A term or termGroup object, depending on input
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseTermOrTermGroup (ParseTree node,
            boolean negatedGlobal, String mode) {

        String nodeCat = getNodeCat(node);

        if (nodeCat.equals("term")) {

            // Term is defined recursive with non-necessary brackets
            if (getNodeCat(node.getChild(0)).equals("(")) {
                return parseTermOrTermGroup(node.getChild(1), negatedGlobal,
                        mode);
            };

            String key = null;
            Map<String, Object> term = KoralObjectGenerator
                    .makeTerm();
            // handle negation
            boolean negated = negatedGlobal;
            boolean isRegex = false;
            List<ParseTree> negations = getChildrenWithCat(node, "!");
            if (negations.size() % 2 == 1)
                negated = !negated;
            // retrieve possible nodes
            ParseTree keyNode = getFirstChildWithCat(node, "key");
            ParseTree valueNode = getFirstChildWithCat(node, "value");
            ParseTree layerNode = getFirstChildWithCat(node, "layer");
            ParseTree foundryNode = getFirstChildWithCat(node, "foundry");
            ParseTree termOpNode = getFirstChildWithCat(node, "termOp");
            ParseTree flagNode = getFirstChildWithCat(node, "flag");
            // process foundry
            if (foundryNode != null)
                term.put("foundry", foundryNode.getText());

            // process key: 'normal', 'verbatim' or regex?
			if (getNodeCat(keyNode.getChild(0)).equals("verbatim")) {

				// Get stream from hidden channel
				TokenStream stream = parser.getTokenStream();
				key = stream.getText(keyNode.getChild(0).getSourceInterval());

				if (key.startsWith("'") && key.endsWith("'"))
					key = key.substring(1, key.length()-1).replaceAll("\\\\\\\\","\\\\").replaceAll("\\\\'", "'");

			}
			else {
				key = keyNode.getText();
			};

            if (getNodeCat(keyNode.getChild(0)).equals("regex")) {
                isRegex = true;
                term.put("type", "type:regex");
                // remove leading and trailing quotes
                key = key.substring(1, key.length() - 1);
            }
            if (mode.equals("span"))
                term.put("value", key);
            else
                term.put("key", key);
            // process layer: map "base" -> "lemma"
            if (layerNode != null) {
                String layer = layerNode.getText();
                if (mode.equals("span")) {
                    term.put("key", layer);
                }
                else if (mode.equals("token")) {
                    if (layer.equals("base")) {
                        layer = "lemma";
                    }
                    else if (layer.equals("punct")) {
                        layer = "orth";
                        // will override "type":"type:regex"
                        term.put("type", "type:punct");
                    }
                    term.put("layer", layer);
                }
            }
            // process value
            if (valueNode != null) {
                term.put("value", valueNode.getText());
			};
				
            // process operator ("match" property)
            if (termOpNode != null) {
                String termOp = termOpNode.getText();
                negated = termOp.contains("!") ? !negated : negated;
                if (!negated)
                    term.put("match", "match:eq");
                else
                    term.put("match", "match:ne");
            }
            // process possible flags
            if (flagNode != null) {
                // substring removes leading slash
                String flag = getNodeCat(flagNode.getChild(0)).substring(1);
                
                // EM: handling flagnode as layer
                if (node.getChild(1).equals(flagNode)){
                    if (layerNode == null) {
                        term.put("layer", flag);
                    }
                    else{
                        String layer = (String) term.get("layer");
                        term.put("layer", flag+layer);
                    }
                    
                    // EM: check for other flags
                    List<ParseTree> list = getChildrenWithCat(node, "flag");
                    for (int i=1; i < list.size(); i++){
                        ParseTree n = list.get(i);
                        flag = getNodeCat(n.getChild(0)).substring(1);
                        parseFlag(flag, isRegex, key, term);
                    }
                }
                else {
                    term = parseFlag(flag, isRegex, key, term);
                }
            }
            return term;
        }
        else if (nodeCat.equals("termGroup")) {

            // TermGroup is defined recursive with non-necessary brackets
            if (getNodeCat(node.getChild(0)).equals("(") && node.getChildCount() == 3) {
                return parseTermOrTermGroup(node.getChild(1), negatedGlobal,
                        mode);
            };

            // For termGroups, establish a boolean relation between
            // operands and recursively call this function with
            // the term or termGroup operands
            Map<String, Object> termGroup = null;
            ParseTree leftOp = null;
            ParseTree rightOp = null;


            // check for leading/trailing parantheses
            if (!getNodeCat(node.getChild(0)).equals("("))
                leftOp = node.getChild(0);
            else
                leftOp = node.getChild(1);
            if (!getNodeCat(node.getChild(node.getChildCount() - 1))
                    .equals(")"))
                rightOp = node.getChild(node.getChildCount() - 1);
            else
                rightOp = node.getChild(node.getChildCount() - 2);
            // establish boolean relation
            ParseTree boolOp = getFirstChildWithCat(node, "boolOp");

			// Create group // de Morgan's Laws
            if (boolOp.getText().equals("&")) {
                if (negatedGlobal==false) {
                    termGroup = KoralObjectGenerator
                        .makeTermGroup(KoralTermGroupRelation.AND);
                } else {
                    termGroup = KoralObjectGenerator
                        .makeTermGroup(KoralTermGroupRelation.OR);
                }
            }
            else if (boolOp.getText().equals("|")) {
                if (negatedGlobal==false) {
                    termGroup = KoralObjectGenerator
                        .makeTermGroup(KoralTermGroupRelation.OR);
                } else {
                    termGroup = KoralObjectGenerator
                        .makeTermGroup(KoralTermGroupRelation.AND);
                }
            }

            ArrayList<Object> operands = (ArrayList<Object>) termGroup
                    .get("operands");
            // recursion with left/right operands
            operands.add(parseTermOrTermGroup(leftOp, negatedGlobal, mode));
            operands.add(parseTermOrTermGroup(rightOp, negatedGlobal, mode));
            return termGroup;
        }
        return null;
    }


    private Map<String, Object> parseFlag (String flag, boolean isRegex,
            String key, Map<String, Object> term) {
        ArrayList<String> flags = new ArrayList<String>();

        if (flag.contains("i")) flags.add("flags:caseInsensitive");
        if (flag.contains("x")) {
            if (!isRegex) {
                key = QueryUtils.escapeRegexSpecialChars(key);
            }
            // flag 'x' allows submatches:
            // overwrite key with appended .*?
            term.put("key", ".*?" + key + ".*?"); //
            term.put("type", "type:regex");
        }
        if (!flags.isEmpty()) {
            term.put("flags", flags);
        }
        return term;
    }
    
    /**
     * Puts an object into the operands list of its governing (or
     * "super") object which had been placed on the
     * {@link #objectStack} before and is still on top of the stack.
     * If this is the top object of the tree, it is put there instead
     * of into some (non-existent) operand stack.
     * 
     * @param object
     *            The object to be inserted
     */
    private void putIntoSuperObject (Map<String, Object> object) {
        putIntoSuperObject(object, 0);
    }


    /**
     * Puts an object into the operands list of its governing (or
     * "super") object which had been placed on the
     * {@link #objectStack} before. If this is the top object of the
     * tree, it is put there instead of into some (non-existent)
     * operand stack.
     * 
     * @param object
     *            The object to be inserted
     * @param objStackPosition
     *            Indicated the position of the super object on the
     *            {@link #objectStack} (in case not the top element of
     *            the stack is the super object.
     */
    @SuppressWarnings({ "unchecked" })
    private void putIntoSuperObject (Map<String, Object> object,
            int objStackPosition) {
        if (objectStack.size() > objStackPosition) {
            ArrayList<Object> topObjectOperands = (ArrayList<Object>) objectStack
                    .get(objStackPosition).get("operands");
            topObjectOperands.add(object);
        }
        else {
            requestMap.put("query", object);
        }
    }


    /**
     * Parses the min and max attributes for a boundary object as
     * defined in
     * a distance node.
     * 
     * @param distanceNode
     *            A node of category 'distance'
     * @return An array of two fields, where the first is the min
     *         value and the
     *         second is the max value and may be null.
     */
    private Integer[] parseDistance (ParseTree distanceNode) {
        int emptyTokenSeqIndex = getNodeCat(distanceNode).equals("distance") ? 0
                : 2;
        Integer[] minmax = parseEmptySegments(
                distanceNode.getChild(emptyTokenSeqIndex));
        Integer min = minmax[0];
        Integer max = minmax[1];
        //        min++;
        //        if (max != null)
        //            max++;
        return new Integer[] { min, max };
    }


    private Integer[] parseEmptySegments (ParseTree emptySegments) {
        Integer min = 0;
        Integer max = 0;
        ParseTree child;
        for (int i = 0; i < emptySegments.getChildCount(); i++) {
            child = emptySegments.getChild(i);
            ParseTree nextSibling = emptySegments.getChild(i + 1);
            if (child.toStringTree(parser).equals("(emptyToken [ ])")) {
                if (nextSibling != null
                        && getNodeCat(nextSibling).equals("repetition")) {
                    Integer[] minmax = parseRepetition(nextSibling);
                    min += minmax[0];
                    if (minmax[1] != null) {
                        max += minmax[1];
                    }
                    else {
                        max = null;
                    }
                }
                else {
                    min++;
                    max++;
                }
            }
        }
        // min = cropToMaxValue(min);
        // max = cropToMaxValue(max);
        return new Integer[] { min, max };
    }


    private ParserRuleContext parsePoliqarpQuery (String query) {
        Lexer lexer = new PoliqarpPlusLexer((CharStream) null);
        ParserRuleContext tree = null;
        Antlr4DescriptiveErrorListener errorListener = new Antlr4DescriptiveErrorListener(
                query);
        // Like p. 111
        CommonTokenStream tokens = null;
        try {
            // Tokenize input data
            ANTLRInputStream input = new ANTLRInputStream(query);
            lexer.setInputStream(input);
            tokens = new CommonTokenStream(lexer);
            
            parser = new PoliqarpPlusParser(tokens);

            // Don't throw out erroneous stuff
            parser.setErrorHandler(new BailErrorStrategy());
            lexer.removeErrorListeners();
            lexer.addErrorListener(errorListener);
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);

            // Get starting rule from parser
            Method startRule = PoliqarpPlusParser.class.getMethod("request");
            tree = (ParserRuleContext) startRule.invoke(parser,
                    (Object[]) null);
        }
        // Some things went wrong ...
        catch (Exception e) {
//            log.error("Could not parse query. "
//                    + "Please make sure it is well-formed.");
//            log.error(errorListener.generateFullErrorMsg().toString());
            addError(errorListener.generateFullErrorMsg());
        }
        return tree;
    }
}
