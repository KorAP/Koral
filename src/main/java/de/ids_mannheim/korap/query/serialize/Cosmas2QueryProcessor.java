package de.ids_mannheim.korap.query.serialize;

import de.ids_mannheim.korap.query.parse.cosmas.c2ps_opPROX; // error codes.
import de.ids_mannheim.korap.query.object.ClassRefCheck;
import de.ids_mannheim.korap.query.object.ClassRefOp;
import de.ids_mannheim.korap.query.object.CosmasPosition;
import de.ids_mannheim.korap.query.object.KoralFrame;
import de.ids_mannheim.korap.query.object.KoralMatchOperator;
import de.ids_mannheim.korap.query.object.KoralOperation;
import de.ids_mannheim.korap.query.object.KoralTermGroupRelation;
import de.ids_mannheim.korap.query.object.KoralType;
import de.ids_mannheim.korap.query.parse.cosmas.c2ps_opPROX; 
import de.ids_mannheim.korap.query.parse.cosmas.c2psLexer;
import de.ids_mannheim.korap.query.parse.cosmas.c2psParser;
import de.ids_mannheim.korap.query.serialize.util.Antlr3DescriptiveErrorListener;
import de.ids_mannheim.korap.query.serialize.util.Converter;
import de.ids_mannheim.korap.query.serialize.util.KoralObjectGenerator;
import de.ids_mannheim.korap.query.serialize.util.ResourceMapper;
import de.ids_mannheim.korap.query.serialize.util.StatusCodes;
import de.ids_mannheim.korap.util.StringUtils;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.FailedPredicateException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.Tree;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Map representation of CosmasII syntax tree as returned by ANTLR
 * 
 * @author Joachim Bingel (bingel@ids-mannheim.de)
 * @author Nils Diewald (diewald@ids-mannheim.de)
 * @author Eliza Margaretha (margaretha@ids-mannheim.de)
 * @version 0.3
 */
public class Cosmas2QueryProcessor extends Antlr3AbstractQueryProcessor {

    private static final boolean DEBUG = true;

    private static Logger log =
            LoggerFactory.getLogger(Cosmas2QueryProcessor.class);

    private static final int messLang = c2ps_opPROX.MLANG_GERMAN;
    
    private LinkedList<Map<String, Object>[]> toWrapStack =
            new LinkedList<Map<String, Object>[]>();
    /**
     * Field for repetition query (Kleene + or * operations, or
     * min/max queries: {2,4}
     */
    private String repetition = "";
    /**
     * Global control structure for fieldGroups, keeps track of open
     * fieldGroups.
     */
    private LinkedList<ArrayList<Object>> openFieldGroups =
            new LinkedList<ArrayList<Object>>();
    /**
     * Keeps track of how many toWrap objects there are to pop after
     * every recursion of {@link #processNode(ParseTree)}
     */
    private LinkedList<Integer> toWrapsToPop = new LinkedList<Integer>();
    /**
     * Flag that indicates whether token fields or meta fields are
     * currently being processed
     */
    private boolean inMeta = false;
    /**
     * If true, a sequence (OPPROX node) is governed by an OPALL node
     * (ALL()-operator), which requires to match all tokens of the
     * sequence.
     */
    private boolean inOPALL = false;
    private boolean inOPNHIT = false;
    /**
             *
             */
    private int classCounter = 1;
    private boolean negate = false;

    /**
     * Allows for the definition of objects to be wrapped around the
     * arguments of an operation. Each record in the table holds the
     * parent node of the argument, the number of the argument and an
     * object in whose operands list the argument shall be wrapped.
     */
    private Table<Tree, Integer, Map<String, Object>> operandWrap =
            HashBasedTable.create();

    /**
     * Keeps track of all visited nodes in a tree
     */
    private List<Tree> visited = new ArrayList<Tree>();

    Integer stackedToWrap = 0;
    /**
     * A list of node categories that can be sequenced (i.e. which can
     * be in a sequence with any number of other nodes in this list)
     */
    private final List<String> sequentiableNodeTypes =
            Arrays.asList(new String[] { "OPWF", "OPLEM", "OPMORPH", "OPBEG",
                    "OPEND", "OPIN", "OPBED", "OPELEM", "OPOR", "OPAND" });
    /**
     * Keeps track of sequenced nodes, i.e. nodes that implicitly
     * govern a sequence, as in (C2PQ (OPWF der) (OPWF Mann)). This is
     * necessary in order to know when to take the sequence off the
     * object stack, as the sequence is introduced by the first child
     * but cannot be closed after this first child in order not to
     * lose its siblings
     */
    private LinkedList<Tree> sequencedNodes = new LinkedList<Tree>();

    private boolean nodeHasSequentiableSiblings;

    /**
     * Keeps track of operands lists that are to be serialised in an
     * inverted order (e.g. the IN() operator) compared to their AST
     * representation.
     */
    private LinkedList<ArrayList<Object>> invertedOperandsLists =
            new LinkedList<ArrayList<Object>>();

    public static Pattern wildcardStarPattern = Pattern.compile("([*])");
    public static Pattern wildcardPlusPattern = Pattern.compile("([+])");
    public static Pattern wildcardQuestionPattern = Pattern.compile("([?])");

	/**
	 * reportErrorsinTree:
	 * - traverse the AST tree and search for nodes of type ERROR, they contain
	 *   the errCode, the error message and the error char position.
	 * - returns true if an error node is found in the tree referenced by 'node'.
	 * - adds error code, error position and error message to the error list.
	 * Arguments:
	 * node		: might be null if it has been reseted previously by another error handler.
	 * @param node
	 * @return: true: error node was found,
	 * 			false; no error node found.
	 * 19.12.23/FB
	 */
    
    private boolean reportErrorsinTree(Tree node)
    
    {
    	// not used when not debugging: 
    	final String func = "reportErrorsinTree";
    	
     	if( node == null )
    		{
    		// System.err.printf("Warning: %s: node == null: no action requested.\n", func);
    		return false;
    		}
 
    	if( node.getType() == 1 && node.getText().compareTo("ERROR") == 0 )
	    	{
	    	// error node found:
    		// child[0] : error pos.
    		// child[1] : error code. 
    		// child[2] : error message, containing offending string.
    		/*
    		System.err.printf("Debug: %s: child[0]='%s' child[1]='%s' child[2]='%s'.\n", func,
    					node.getChild(0) != null ? node.getChild(0).getText() : "???",
    	    			node.getChild(1) != null ? node.getChild(1).getText() : "???",
    	    			node.getChild(2) != null ? node.getChild(2).getText() : "???");
    		*/
    		
    		int
    			errPos  = node.getChild(0) != null ? Integer.parseInt(node.getChild(0).getText()) : 0;
    		int
    			errCode = node.getChild(1) != null ? Integer.parseInt(node.getChild(1).getText()) : StatusCodes.ERR_PROX_UNKNOWN; 
    		String
    			errMess = node.getChild(2) != null ? node.getChild(2).getText() : c2ps_opPROX.getErrMess(StatusCodes.UNKNOWN_QUERY_ERROR, messLang, "");
    			
			ArrayList<Object> 
				errorSpecs = new ArrayList<Object>();
			
	        errorSpecs.add(errCode);
	        errorSpecs.add(errMess);
	        errorSpecs.add(errPos);
    		addError(errorSpecs);
    		return true;
	    	}
    	
    	for(int i=0; i<node.getChildCount(); i++)
	    	{
    		Tree
    			son = node.getChild(i);
    		
    		/* System.err.printf(" node: text='%s' type=%d start=%d end=%d.\n",
    				son.getText(), 
    				son.getType(),
    				son.getTokenStartIndex(),
    				son.getTokenStopIndex());
    		*/
    		// return the first error found only:
    		if( reportErrorsinTree(son) )
    			return true; // error found, stop here.
	    	}
    	
    	// no error node:
    	return false;
    } // reportErrorsinTree

    /**
     * @param tree
     *            The syntax tree as returned by ANTLR
     * @param parser
     *            The ANTLR parser instance that generated the parse
     *            tree
     * @throws QueryException
     */
    public Cosmas2QueryProcessor (String query) {
        KoralObjectGenerator.setQueryProcessor(this);
        this.query = query;
        process(query);
        if (verbose) 
        	{ 
            //log.debug(">>> " + requestMap.get("query") + " <<<");
            try {
	        	// query from requestMap is unformatted JSON. Make it pretty before displaying:
	        	ObjectMapper mapper = new ObjectMapper();
	        	String jsonQuery = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestMap.get("query"));
				System.out.printf("Cosmas2QueryProcessor: JSON output:\n%s\n\n", jsonQuery);
				} 
	        catch (JsonProcessingException e) 
	        	{
	        	System.out.printf("Cosmas2QueryProcessor: >>%s<<.\n",  requestMap.get("query"));
	            //e.printStackTraObjectMapper mapper = new ObjectMapper();ce();
				}
        	}
    	}


    @Override
    public void process (String query) {
        Tree tree = null;
        
        if (DEBUG) 
	    	{ 
	    	System.out.printf("\nProcessing COSMAS II query: %s.\n\n", query);
	        log.debug("Processing CosmasII query: " + query);
	    	}
	        
        tree = parseCosmasQuery(query);
        
        if (tree != null) 
        	{
            if (verbose) {
	        	log.debug("ANTLR parse tree: " + tree.toStringTree());
	            System.out.printf("\nANTLR parse tree: %s.\n\n",  tree.toStringTree());
	            }

            processNode(tree);
        	}
    }


    private void processNode (Tree node) {
        // Top-down processing
        if (visited.contains(node))
            return;
        else
            visited.add(node);

        String nodeCat = getNodeCat(node);
        openNodeCats.push(nodeCat);

        stackedObjects = 0;
        stackedToWrap = 0;

        /*
         if (verbose) {
            System.err.println(" " + objectStack);
            System.out.println(openNodeCats);
        }
        */
        
        /* ***************************************
         * Processing individual node categories *
         * ***************************************
         */

        // Check for potential implicit sequences as in (C2PQ (OPWF
        // der) (OPWF Mann)). The sequence is introduced
        // by the first child if it (and its siblings) is
        // sequentiable.
        if (sequentiableNodeTypes.contains(nodeCat)) {
            // for each node, check if parent has more than one child
            // (-> could be implicit sequence)
            Tree parent = node.getParent();
            if (parent.getChildCount() > 1) {
                // if node is first child of parent...
                if (node == parent.getChild(0)) {
                    nodeHasSequentiableSiblings = false;
                    for (int i = 1; i < parent.getChildCount(); i++) {
                        if (sequentiableNodeTypes
                                .contains(getNodeCat(parent.getChild(i)))) {
                            nodeHasSequentiableSiblings = true;
                            continue;
                        }
                    }
                    if (nodeHasSequentiableSiblings) {
                        // Step I: create sequence
                        Map<String, Object> sequence = KoralObjectGenerator
                                .makeGroup(KoralOperation.SEQUENCE);
                        // push sequence on object stack but don't
                        // increment stackedObjects counter since
                        // we've got to wait until the parent node is
                        // processed - therefore, add the parent
                        // to the sequencedNodes list and remove the
                        // sequence from the stack when the parent
                        // has been processed
                        objectStack.push(sequence);
                        sequencedNodes.push(parent);
                        // Step II: decide where to put sequence
                        putIntoSuperObject(sequence, 1);
                    }
                }
            }
        }

        if (nodeCat.equals("OPWF") || nodeCat.equals("OPLEM")) {
            processOPWF_OPLEM(node);
        }

        if (nodeCat.equals("OPMORPH")) {
            processOPMORPH(node);
        }

        if (nodeCat.equals("OPELEM")) {
            processOPELEM(node);
        }

        if (nodeCat.equals("OPLABEL")) {
            processOPLABEL(node);
        }

        if (nodeCat.equals("OPAND") || nodeCat.equals("OPNOT")) {
            processOPAND_OPNOT(node);
        }

        if (nodeCat.equals("OPOR")) {
            processOPOR(node);
        }

        if (nodeCat.equals("OPPROX")) {
            processOPPROX(node);
        }

        // inlcusion or overlap
        if (nodeCat.equals("OPIN") || nodeCat.equals("OPOV")) {
            processOPIN_OPOV(node);
        }

        // Wrap the argument of an #IN operator in a previously
        // defined container
        if (nodeCat.equals("ARG1") || nodeCat.equals("ARG2")) {
            processARG1_ARG2(node);
        }

        if (nodeCat.equals("OPALL")) {
            inOPALL = true;
        }

        if (nodeCat.equals("OPNHIT")) {
            processOPNHIT(node);
        }

        if (nodeCat.equals("OPEND") || nodeCat.equals("OPBEG")) {
            processOPEND_OPBEG(node);
        }

        if (nodeCat.equals("OPBED")) {
            processOPBED(node);
        }
        
        if (nodeCat.equals("OPREG")) {
            processOPREG(node);
        }
        
        objectsToPop.push(stackedObjects);
        toWrapsToPop.push(stackedToWrap);

        /*
         * ***************************************************************
         * ***************************************************************
         * recursion until 'request' node (root of tree) is processed
         * *
         * ***********************************************************
         * ****
         * ********************************************************
         * *******
         */
        for (int i = 0; i < node.getChildCount(); i++) {
            Tree child = node.getChild(i);
            processNode(child);
        }

        /*
         * *************************************************************
         * Stuff that happens after processing the children of a node
         * *
         * ***********************************************************
         * **
         */

        // remove sequence from object stack if node is implicitly
        // sequenced
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

        // if (!toWrapStack.isEmpty())
        // System.err.println(toWrapStack.get(0)[0]);
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


    private void processOPEND_OPBEG (Tree node) {
        // Step I: create group
        String nodeCat = getNodeCat(node);
        Map<String, Object> beggroup = new HashMap<String, Object>();
        beggroup.put("@type", "koral:reference");
        beggroup.put("operation", "operation:focus");
        ArrayList<Integer> spanRef = new ArrayList<Integer>();
        if (nodeCat.equals("OPBEG")) {
            spanRef.add(0);
            spanRef.add(1);
        }
        else if (nodeCat.equals("OPEND")) {
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


    private void processOPBED (Tree node) {
        // Node structure is (OPBED X+ (OPTS (TPBEG tpos*) (TPEND
        // tpos*)))
        // X is some segment, TPBEG or TPEND must be present
        // (inclusive OR)
        // tpos is a three-char string of the form "[+-]?[spt][ae]".
        // s/p/t indicates span, a/e beginning/end, - means negation
        // See C-II QL documentation for more detail:
        // http://www.ids-mannheim.de/cosmas2/win-app/hilfe/suchanfrage/eingabe-grafisch/syntax/textpositionen.html

        // Step I: create group
        int optsChild = node.getChildCount() - 1;
        Tree begConditions =
                getFirstChildWithCat(node.getChild(optsChild), "TPBEG");
        Tree endConditions =
                getFirstChildWithCat(node.getChild(optsChild), "TPEND");

        Map<String, Object> submatchgroup =
                KoralObjectGenerator.makeReference(classCounter + 128);
        ArrayList<Object> submatchOperands = new ArrayList<Object>();
        submatchgroup.put("operands", submatchOperands);
        putIntoSuperObject(submatchgroup);

        // Step II: collect all conditions, create groups for them in
        // processPositionCondition()
        ArrayList<Object> distributedOperands = new ArrayList<Object>();
        ArrayList<Map<String, Object>> conditionGroups =
                new ArrayList<Map<String, Object>>();
        if (begConditions != null) {
            for (Tree condition : getChildren(begConditions)) {
                conditionGroups.add(processPositionCondition(condition,
                        distributedOperands, "beg"));
            }
        }
        if (endConditions != null) {
            for (Tree condition : getChildren(endConditions)) {
                conditionGroups.add(processPositionCondition(condition,
                        distributedOperands, "end"));
            }
        }
        // Step III: insert conditions. need to stack matches-groups
        // because position groups may only have two operands
        // indicates where to insert next condition group
        ArrayList<Object> currentLowestOperands = submatchOperands;
        int conditionCount = 0;
        for (Map<String, Object> conditionGroup : conditionGroups) {
            conditionCount++;
            if (conditionGroups.size() == 1) {
                submatchOperands.add(conditionGroup);
            }
            else if (conditionCount < conditionGroups.size()) {
                ArrayList<KoralFrame> frames = new ArrayList<KoralFrame>();
                frames.add(KoralFrame.MATCHES);
                Map<String, Object> matchesGroup =
                        KoralObjectGenerator.makePosition(frames);
                @SuppressWarnings("unchecked")
                ArrayList<Object> matchesOperands =
                        (ArrayList<Object>) matchesGroup.get("operands");
                matchesOperands.add(conditionGroup);
                // matches groups that are embedded at the second or
                // lower level receive an additional
                // focus to grep out only the query term to which the
                // constraint applies
                if (conditionCount > 1) {
                    Map<String, Object> focus = KoralObjectGenerator
                            .makeReference(classCounter + 128 - 2);
                    ArrayList<Object> focusOperands = new ArrayList<Object>();
                    focus.put("operands", focusOperands);
                    focusOperands.add(matchesGroup);
                    currentLowestOperands.add(focus);
                }
                else {
                    currentLowestOperands.add(matchesGroup);
                }
                currentLowestOperands = matchesOperands;
            }
            else {
                currentLowestOperands.add(conditionGroup);
            }
        }
    }

        /* processOPREG:
         * 
         * - input Node structure is: (OPREG "regexpr").
		 * - transforms tree into the corresponding Koral:token/Koral:term, like:
		 *    e.g. #REG(abc[']?s) ->
		 *     {
		 *      "@type": "koral:term",
		 *      "match": "match:eq",   // optional
		 *      "type" : "type:regex",
		 *      "key"  : "abc[']?s",
		 *      "layer": "orth"
		 *     }.
		 *
		 * - see doc: http://korap.github.io/Koral/
		 * 
		 * 06.09.23/FB
		 */
    	
    private void processOPREG (Tree node) 
    
    {
        int 
        	nChild = node.getChildCount() - 1;
        Tree
        	nodeChild = node.getChild(0);
        boolean
        	bDebug = false;
        
        if( bDebug )
        	{
        	//System.out.printf("Debug: processOPREG: node='%s' nChilds=%d.\n", node.toStringTree(), nChild+1);
            System.out.printf("Debug: processOPREG: child: >>%s<< cat=%s type=%d.\n",
            		nodeChild.getText(), getNodeCat(node), nodeChild.getType());
            }
        
        // empty case (is that possible?):
        if( nChild < 0 )
        	return;
        
        // see processOPWF_OPWF_OPLEM
        // for how to insert regexpr into Koral JSON-LD
        
        Map<String, Object> 
        	token = KoralObjectGenerator.makeToken();
        
        objectStack.push(token);
        stackedObjects++;
        
        Map<String, Object> 
        	fieldMap = KoralObjectGenerator.makeTerm();
        
        token.put("wrap", fieldMap);
        
        // make category-specific fieldMap entry:
        /*
        System.out.printf("Debug: processOPREG: before replaceALL: >>%s<<.\n", nodeChild.toStringTree());
        String 
        	value = nodeChild.toStringTree().replaceAll("\"", "");
        System.out.printf("Debug: processOPREG: after  replaceALL: >>%s<<.\n", value);
        */
        
        /* replace replaceALL() by replaceIfNotEscaped() to delete every occurence of >>"<<
         * which is not escaped by >>\<<, as it is important to keep the escaped sequence for
         * the argument of #REG().
         * This is not possible with replaceALL().
         */
        String
        	value = nodeChild.toStringTree(); // old version: replaceDoubleQuotes(nodeChild.toStringTree());
        
        if( bDebug )
        	System.out.printf("Debug: processOPREG: key: >>%s<<.\n", value);
        
        fieldMap.put("key",   value);
        fieldMap.put("layer", "orth");
        fieldMap.put("type",  "type:regex");
        fieldMap.put("match", "match:eq");
        
        // decide where to put (objPos=1, not clear why, but it works only like that - 20.09.23/FB):
        putIntoSuperObject(token,1); 
        
    } // processOPREG


    private void processOPNHIT (Tree node) {
        Integer[] classRef = new Integer[] { classCounter + 128 + 1,
                classCounter + 128 + 2 };
        // classRef.add(classCounter + 1); // yes, do this twice (two
        // classes)!
        Map<String, Object> group =
                KoralObjectGenerator.makeReference(classCounter + 128);
        Map<String, Object> classRefCheck = KoralObjectGenerator.makeClassRefOp(
                ClassRefOp.INVERSION, classRef, classCounter + 128);
        ArrayList<Object> operands = new ArrayList<Object>();
        operands.add(classRefCheck);
        group.put("operands", operands);
        classCounter++;
        // direct child is OPPROX
        wrapOperandInClass(node.getChild(0), 1, 128 + classCounter++);
        wrapOperandInClass(node.getChild(0), 2, 128 + classCounter++);
        objectStack.push(classRefCheck);
        stackedObjects++;
        putIntoSuperObject(group, 1);
    }


    private void processARG1_ARG2 (Tree node) {
        String nodeCat = getNodeCat(node);
        Tree parent = node.getParent();
        if (operandWrap.containsRow(parent)) {
            
            // Step I: create group
            int argNr = nodeCat.equals("ARG1") ? 1 : 2;
            Map<String, Object> container = operandWrap.row(parent).get(argNr);
            // Step II: ingest
            if (container != null) {
                objectStack.push(container);
                stackedObjects++;
                putIntoSuperObject(container, 1);
            }
        }
    }


    private boolean isExclusion (Tree node) {
        Tree exclnode = getFirstChildWithCat(node, "EXCL");
        if (exclnode != null
                && exclnode.getChild(0).toStringTree().equals("YES")) {
            return true;
        }
        return false;
    }


    @SuppressWarnings("unchecked")
    private Map<String, Object> addClassRefCheck (
            ArrayList<ClassRefCheck> check, Map<String, Object> group,
            int classCounter) {

        Set<Integer> classIn = new HashSet<Integer>();
        classIn.add(classCounter + 128 - 1);
        classIn.add(classCounter + 128);

        ArrayList<Integer> classInList = new ArrayList<Integer>(classIn);
        // wrap position in a classRefCheck
        Map<String, Object> topGroup = KoralObjectGenerator
                .makeClassRefCheck(check, classInList);
        ((ArrayList<Object>) topGroup.get("operands")).add(group);
        return topGroup;
    }
    
    private Map<String, Object> addClassRefOp (
            ClassRefOp operation, Map<String, Object> group,
            int classCounter) {

        Integer[] classIn = new Integer[2];
        classIn[0] = (classCounter + 128 - 1);
        classIn[1] = (classCounter + 128);

        int classOut = classCounter + 128 + 1;
        // wrap position in a classRefCheck
        Map<String, Object> topGroup = KoralObjectGenerator
                .makeClassRefOp(operation, classIn, classOut);
        ((ArrayList<Object>) topGroup.get("operands")).add(group);
        return topGroup;
    }


    private Map<String, Object> addClassFocus (boolean isMatchAll,
            Map<String, Object> posGroup, int startClassCounter) {
        Map<String, Object> focusGroup = null;
        if (isMatchAll) {
            focusGroup = KoralObjectGenerator.makeClassRefOp(ClassRefOp.DELETE,
                    new Integer[] { 128 + classCounter++ }, 128 + classCounter);
            ((ArrayList<Object>) focusGroup.get("operands")).add(posGroup);
        }
        else { // match only first argument
            focusGroup = KoralObjectGenerator.wrapInReference(posGroup,
                    startClassCounter + 128 - 1);
//            classCounter++;
        }
        return focusGroup;
    }


    @SuppressWarnings("unchecked")
    private void processOPIN_OPOV (Tree node) {
        String nodeCat = getNodeCat(node);
        
        // Map<String, Object> posgroup =
        // makePosition(null);
        boolean isExclusion = isExclusion(node);
        boolean bDebug = false;
        
        int focusClassCounter = classCounter;
        Map<String, Object> posGroup;
        
        if (!isComplexQuery(node, "ARG1")){
            wrapOperandInClass(node, 1, 128 + classCounter++);
            focusClassCounter = classCounter;
        }
        
        if (isExclusion) {
            posGroup = KoralObjectGenerator.makeGroup(KoralOperation.EXCLUSION);
        }
        else {
            posGroup = KoralObjectGenerator.makeGroup(KoralOperation.POSITION);
            if (bDebug) log.debug(posGroup.toString());
        }

        Map<String, Object> positionOptions;
        if (nodeCat.equals("OPIN")) {
            positionOptions = parseOPINOptions(node, isExclusion);
            if (nodeCat.equals("OPIN")) {
                invertedOperandsLists.add((ArrayList<Object>) posGroup.get("operands"));
            }
        }
        else {
            positionOptions = parseOPOVOptions(node,isExclusion);
        }

        posGroup.put("frames", positionOptions.get("frames"));
        // EM: is frame needed?
        // posGroup.put("frame", positionOptions.get("frame"));
        objectStack.push(posGroup);
        stackedObjects++;
        
        ArrayList<ClassRefCheck> checkList =
                (ArrayList<ClassRefCheck>) positionOptions.get("classRefCheck");
        
        // Step II: wrap in classRefCheck and/or focus and decide where to put
        if (!checkList.isEmpty()) {
            posGroup =
                    addClassRefCheck((ArrayList<ClassRefCheck>) positionOptions
                            .get("classRefCheck"), posGroup, 2);
        }
        
        if (nodeCat.equals("OPIN")) {
            // add focus
            posGroup = addClassFocus((boolean) positionOptions.get("matchall"),
                    posGroup, focusClassCounter);
        }
        else{
            posGroup = addClassRefOp(ClassRefOp.UNION, posGroup, 2);
        }
        
        // wrap in 'merge' operation if grouping option is set
        if (!isExclusion && positionOptions.containsKey("grouping")
                && (boolean) positionOptions.get("grouping")) {
            Map<String, Object> mergeOperation =
                    KoralObjectGenerator.makeGroup(KoralOperation.MERGE);
            ArrayList<Object> mergeOperands =
                    (ArrayList<Object>) mergeOperation.get("operands");
            mergeOperands.add(posGroup);
            posGroup = mergeOperation;
        }
        putIntoSuperObject(posGroup, 1);
    }


    @SuppressWarnings("unchecked")
    private void processOPPROX (Tree node) {
    	
        // collect info
        Tree prox_opts = node.getChild(0);
        Tree typ = prox_opts.getChild(0);
        Tree dist_list = prox_opts.getChild(1);
    	
    	// Step I: create group
        Map<String, Object> group =
                KoralObjectGenerator.makeGroup(KoralOperation.SEQUENCE);

        ArrayList<Object> constraints = new ArrayList<Object>();
        boolean exclusion = typ.getChild(0).toStringTree().equals("EXCL");

        boolean inOrder = false;
        boolean invertedOperands = false;

        group.put("inOrder", inOrder);
        group.put("distances", constraints);

        boolean putIntoOverlapDisjunction = false;

        int tmin, min = 0, max = 0;
        // possibly several distance constraints
        for (int i = 0; i < dist_list.getChildCount(); i++) {
            String direction = dist_list.getChild(i).getChild(0).getChild(0)
                    .toStringTree().toLowerCase();
            String minStr = dist_list.getChild(i).getChild(1).getChild(0)
                    .toStringTree();
            String maxStr = dist_list.getChild(i).getChild(1).getChild(1)
                    .toStringTree();
            String meas = dist_list.getChild(i).getChild(2).getChild(0)
                    .toStringTree();
            if (minStr.equals("VAL0")) {
                minStr = "0";
            }
            min = Integer.parseInt(minStr);
            max = Integer.parseInt(maxStr);
            // If zero word-distance, wrap this sequence in a
            // disjunction along with an overlap position
            // between the two operands
            /*
             * XXX: This is currently deactivated. Uncomment to
             * activate treatment of zero-word distances as
             * overlap-alternatives (see google doc on special
             * distances serialization)
             * 
             * if (meas.equals("w") && min == 0) { min = 1;
             * putIntoOverlapDisjunction = true; }
             */
            if (!meas.equals("w") && min == 0) {
                processSpanDistance(meas, min, max);
            }

            if (max < min) {
                tmin = min;
                min = max;
                max = tmin;
            }
            
            Map<String, Object> distance =
                    KoralObjectGenerator.makeDistance(meas, min, max);
            // override @type, min/max to be treated according to 
            // Cosmas particularities
            distance.put("@type", KoralType.COSMAS_DISTANCE.toString());
            if (exclusion) {
                distance.put("exclude", true);
            }
            // if (! openNodeCats.get(1).equals("OPNHIT")) {
            constraints.add(distance);
            // }
            if (i == 0) {
                if (direction.equals("plus")) {
                    inOrder = true;
                }
                else if (direction.equals("minus")) {
                    inOrder = true;
                    invertedOperands = true;
                }
                else if (direction.equals("both")) {
                    inOrder = false;
                }
            }
        }
        group.put("inOrder", inOrder);
        Map<String, Object> embeddedSequence = group;

        if (!(openNodeCats.get(1).equals("OPBEG")
                || openNodeCats.get(1).equals("OPEND") || inOPALL
                || openNodeCats.get(1).equals("OPNHIT"))) {
            wrapOperandInClass(node, 1, 128 + classCounter);
            wrapOperandInClass(node, 2, 128 + classCounter);
            // Deactivated, uncomment to wrap sequence in reference.
            //            group = KoralObjectGenerator.wrapInReference(group,
            //                    classCounter++);
            classCounter++;
        }
        else if (openNodeCats.get(1).equals("OPNHIT")) {
            Map<String, Object> repetition =
                    KoralObjectGenerator.makeRepetition(min, max);
            ((ArrayList<Object>) repetition.get("operands"))
                    .add(KoralObjectGenerator.makeToken());
            // TODO go on with this: put the repetition into a class
            // and put it in between the operands
            // -> what if there's several distance constraints. with
            // different keys, like /w4,s0?
        }

        //        Map<String, Object> sequence = null;
        //        if (putIntoOverlapDisjunction) {
        //            sequence = embeddedSequence;
        //            group = KoralObjectGenerator.makeGroup("or");
        //            ArrayList<Object> disjOperands = 
        //                    (ArrayList<Object>) group.get("operands");
        //            Map<String, Object> overlapsGroup = KoralObjectGenerator
        //                    .makePosition(new String[0]);
        //
        //            ArrayList<Object> overlapsOperands = 
        //                    (ArrayList<Object>) overlapsGroup.get("operands");
        //            // this ensures identity of the operands lists and thereby
        //            // a distribution of the operands for both created objects
        //            sequence.put("operands", overlapsOperands);
        //            if (invertedOperands) {
        //                invertedOperandsLists.push(overlapsOperands);
        //            }
        //            disjOperands.add(overlapsGroup);
        //            disjOperands.add(KoralObjectGenerator.wrapInReference(sequence, 0));
        //            // Step II: decide where to put
        //            putIntoSuperObject(group, 0);
        //            objectStack.push(sequence);
        //        }
        //        else {
        if (invertedOperands) {
            ArrayList<Object> operands =
                    (ArrayList<Object>) embeddedSequence.get("operands");
            invertedOperandsLists.push(operands);
        }
        // Step II: decide where to put
        putIntoSuperObject(group, 0);
        objectStack.push(embeddedSequence);
        //        }
        stackedObjects++;
        visited.add(node.getChild(0));
    }


    private void processOPOR (Tree node) {
        // Step I: create group
        Map<String, Object> disjunction =
                KoralObjectGenerator.makeGroup(KoralOperation.DISJUNCTION);
        disjunction.put("inOrder", false); // Order is not important 
        objectStack.push(disjunction);
        stackedObjects++;
        // Step II: decide where to put
        putIntoSuperObject(disjunction, 1);
    }


    private void processOPAND_OPNOT (Tree node) {
        // Step I: create group
        String nodeCat = getNodeCat(node);
        Map<String, Object> distgroup =
                KoralObjectGenerator.makeGroup(KoralOperation.SEQUENCE);
        distgroup.put("inOrder", false); // Order is not important 
        ArrayList<Object> distances = new ArrayList<Object>();
        Map<String, Object> zerodistance =
                KoralObjectGenerator.makeDistance("t", 0, 0);
        zerodistance.put("@type", "cosmas:distance"); // overwrite @type: cosmas:distance! 
        if (nodeCat.equals("OPNOT")) zerodistance.put("exclude", true);
        distances.add(zerodistance);
        distgroup.put("distances", distances);
        distgroup.put("operands", new ArrayList<Object>());
        objectStack.push(distgroup);
        stackedObjects++;
        // Step II: decide where to put
        putIntoSuperObject(distgroup, 1);
    }


    private void processOPLABEL (Tree node) {
        // Step I: create element
        String key = node.getChild(0).toStringTree().replaceAll("[<>]", "");
        Map<String, Object> elem = KoralObjectGenerator.makeSpan(key);
        // Step II: decide where to put
        putIntoSuperObject(elem);
    }


    // TODO: The handling of attributes vs. element names is somehow disputable ...
    @SuppressWarnings("unchecked")
    private void processOPELEM (Tree node) {
        // Step I: create element
        Map<String, Object> span = KoralObjectGenerator.makeSpan();
        if (node.getChild(0).toStringTree().equals("EMPTY")) {
            addError(StatusCodes.MALFORMED_QUERY, "Empty #ELEM() operator."
                    + " Please specify a valid element key (like 's' for sentence).");
            return;
        }
        else {
            int elname = 0;
            Tree elnameNode = getFirstChildWithCat(node, "ELNAME");
            /*
            // TODO: This is identical to processOPMORPH
            String wordOrRegex = "\\w+|\".+?\"";
            Pattern p = Pattern.compile("((\\w+)/)?((\\w*)(!?=))?(" + wordOrRegex
            							+ ")(:(" + wordOrRegex + "))?");
            */

            if (elnameNode != null) {
                /*
                span.put("key", elnameNode.getChild(0).toStringTree()
                        .toLowerCase());
                */
                Map<String, Object> fm =
                        termToFieldMap(elnameNode.getChild(0).toStringTree());

                if (fm == null) return;

                // Workaround for things like #ELEM(S) to become #ELEM(s)
                if (fm.get("foundry") == null && fm.get("layer") == null
                        && fm.get("key") != null) {
                    fm.put("key", fm.get("key").toString().toLowerCase());
                };
                span.put("wrap", fm);
                elname = 1;

            }

            if (node.getChildCount() > elname) {
                /*
                 * Attributes can carry several values, like #ELEM(W
                 * ANA != 'N V'), denoting a word whose POS is neither
                 * N nor V. When seeing this, create a sub-termGroup
                 * and put it into the top-level term group, but only
                 * if there are other attributes in that group. If
                 * not, put the several values as distinct
                 * attr-val-pairs into the top-level group (in order
                 * to avoid a top-level group that only contains a
                 * sub-group).
                 */
                Map<String, Object> termGroup = KoralObjectGenerator
                        .makeTermGroup(KoralTermGroupRelation.AND);
                ArrayList<Object> termGroupOperands =
                        (ArrayList<Object>) termGroup.get("operands");
                for (int i = elname; i < node.getChildCount(); i++) {
                    Tree attrNode = node.getChild(i);
                    if (attrNode.getChildCount() == 2) {
                        Map<String, Object> term =
                                KoralObjectGenerator.makeTerm();
                        termGroupOperands.add(term);
                        String layer = attrNode.getChild(0).toStringTree();
                        String[] splitted = layer.split("/");
                        if (splitted.length > 1) {
                            term.put("foundry", splitted[0]);
                            layer = splitted[1];
                        }
                        term.put("layer", translateMorph(layer));
                        term.put("key", attrNode.getChild(1).toStringTree());
                        KoralMatchOperator match =
                                getNodeCat(attrNode).equals("EQ")
                                        ? KoralMatchOperator.EQUALS
                                        : KoralMatchOperator.NOT_EQUALS;
                        term.put("match", match.toString());
                    }
                    else {
                        Map<String, Object> subTermGroup = KoralObjectGenerator
                                .makeTermGroup(KoralTermGroupRelation.AND);
                        ArrayList<Object> subTermGroupOperands =
                                (ArrayList<Object>) subTermGroup
                                        .get("operands");
                        int j;
                        for (j = 1; j < attrNode.getChildCount(); j++) {
                            Map<String, Object> term =
                                    KoralObjectGenerator.makeTerm();
                            String layer = attrNode.getChild(0).toStringTree();
                            String[] splitted = layer.split("/");
                            if (splitted.length > 1) {
                                term.put("foundry", splitted[0]);
                                layer = splitted[1];
                            }
                            term.put("layer", translateMorph(layer));
                            term.put("key",
                                    attrNode.getChild(j).toStringTree());
                            KoralMatchOperator match =
                                    getNodeCat(attrNode).equals("EQ")
                                            ? KoralMatchOperator.EQUALS
                                            : KoralMatchOperator.NOT_EQUALS;
                            term.put("match", match.toString());
                            if (node.getChildCount() == elname + 1) {
                                termGroupOperands.add(term);
                            }
                            else {
                                subTermGroupOperands.add(term);
                            }
                        }
                        if (node.getChildCount() > elname + 1) {
                            termGroupOperands.add(subTermGroup);
                        }
                    }
                    if (getNodeCat(attrNode).equals("NOTEQ")) negate = true;
                }
                // possibly only one term was present throughout all
                // nodes: extract it from the group
                if (termGroupOperands.size() == 1) {
                    termGroup = (Map<String, Object>) termGroupOperands.get(0);
                }

                // TODO: This should be improved ...
                if (elname == 0) {
                    span.put("wrap", termGroup);
                }
                else {
                    span.put("attr", termGroup);
                }
            }
        }
        // Step II: decide where to put
        putIntoSuperObject(span);
    }


    private void processOPMORPH (Tree node) {
        // Step I: get info
        String[] morphterms =
                node.getChild(0).toStringTree().replace(" ", "").split("&");
        Map<String, Object> token = KoralObjectGenerator.makeToken();
        ArrayList<Object> terms = new ArrayList<Object>();
        Map<String, Object> fieldMap = null;

        for (String morphterm : morphterms) {

            fieldMap = termToFieldMap(morphterm);
            if (fieldMap == null) {
                return;
            };

            terms.add(fieldMap);
        }

        if (morphterms.length == 1) {
            token.put("wrap", fieldMap);
        }

        else {
            Map<String, Object> termGroup = KoralObjectGenerator
                    .makeTermGroup(KoralTermGroupRelation.AND);
            termGroup.put("operands", terms);
            token.put("wrap", termGroup);
        }
        // Step II: decide where to put
        putIntoSuperObject(token, 0);
        visited.add(node.getChild(0));
    }


    /**
     * Nodes introducing tokens. Process all in the same manner,
     * except for the fieldMap entry
     * 09.12.24/FB
     *  - do not search for wildcards [+*?] in &opts&lemma expressions, as they are not allowed there.
     *  - but lemma options may contain e.g. '+', e.g. '&Fes+&Prüfung', so do not replace this one.
     * @param node
     */
    
    private void processOPWF_OPLEM (Tree node) 
    {
        String nodeCat = getNodeCat(node);
        // Step I: get info
        Map<String, Object> token = KoralObjectGenerator.makeToken();
        objectStack.push(token);
        stackedObjects++;
        Map<String, Object> fieldMap = KoralObjectGenerator.makeTerm();
        token.put("wrap", fieldMap);
        // make category-specific fieldMap entry
        String attr = nodeCat.equals("OPWF") ? "orth" : "lemma";
        String value = node.getChild(0).toStringTree().replaceAll("\"", "");
        // check for wildcard string

        // check for wildcards in OPWF only.
        if( nodeCat.equals("OPWF") )
        {
        	// http://www.ids-mannheim.de/cosmas2/web-app/hilfe/suchanfrage/eingabe-zeile/syntax/platzhalter.html
	        boolean isFound = false;
	        Matcher m = wildcardStarPattern.matcher(value);
	        if (m.find()) {
	            isFound = true;
	            value = m.replaceAll(".$1");
	        }
	        m.reset();
	        m = wildcardQuestionPattern.matcher(value);
	        if (m.find()) {
	            isFound = true;
	            value = m.replaceAll(".");
	        }
	        m.reset();
	        m = wildcardPlusPattern.matcher(value);
	        if (m.find()) {
	            isFound = true;
	            value = m.replaceAll(".?");
	        }
	
	        if (isFound) {
	            fieldMap.put("type", "type:regex");
	        }
	
	        if (value.startsWith("$")) {
	            value = value.substring(1);
	            ArrayList<String> flags = new ArrayList<String>();
	            flags.add("flags:caseInsensitive");
	            fieldMap.put("flags", flags);
	        }
        }
	    
        // OPWF and OPLEM:
        fieldMap.put("key", value);
        fieldMap.put("layer", attr);

        // negate field (see above)
        if (negate) {
            fieldMap.put("match", KoralMatchOperator.NOT_EQUALS.toString());
        }
        else {
            fieldMap.put("match", KoralMatchOperator.EQUALS.toString());
        }
        // Step II: decide where to put
        if (!hasChild(node, "TPOS")) {
            putIntoSuperObject(token, 1);
            visited.add(node.getChild(0));
        }
        else {
            // TODO
        }
    }


    private void processSpanDistance (String meas, int min, int max) {
        // Do stuff here in case we'll decide one day to treat span distances
        // in a special way (see GDoc Special Distances Serialization).
    }


    /**
     * Registers an entry in the {@link #operandWrap} table in order
     * to allow an operator's arguments (or only one of them) to be
     * wrapped in a class group.
     * 
     * @param node
     *            The operator node (parent node of the ARG1/ARG2
     *            node)
     * @param arg
     *            The argument number (1 or 2)
     * @param cls
     *            The class id.
     */
    private void wrapOperandInClass (Tree node, int arg, int cls) {
        Map<String, Object> clsGroup = KoralObjectGenerator.makeSpanClass(cls);
        wrapOperand(node, arg, clsGroup);
    }


    /**
     * Registers an entry in the {@link #operandWrap} table in order
     * to allow an operator's arguments (or only one of them) to be
     * wrapped in an arbitrary object, e.g. a reference group.
     * 
     * @param node
     *            The operator node (parent node of the ARG1/ARG2
     *            node)
     * @param arg
     *            The argument number (1 or 2)
     * @param container
     *            The object in whose operand list the argument shall
     *            be wrapped.
     */
    private void wrapOperand (Tree node, int arg,
            Map<String, Object> container) {
        operandWrap.put(node, arg, container);
    }


    private Object translateMorph (String layer) {
        // might be extended...
        if (layer.equals("ANA"))
            return ResourceMapper.descriptor2policy("ANA");
        else
            return layer;

    }


    @SuppressWarnings("unchecked")
    /**
     * Processes individual position conditions as provided in the
     * OPTS node under the OPBEG node.
     * #BEG allows to specify position constrains that apply to the
     * beginning or the end of the subquery X.
     * E.g., in #BEG(X, tpos/tpos), the 'tpos' constraints before the
     * slash indicate conditions that apply
     * to the beginning of X, those after the slash are conditions
     * that apply to the end of X.
     * See the official C-II documentation for more details.
     * <br/><br/>
     * What's important here is what follows: <br/>
     * Assume the query #BED(der Mann, sa/pa). This means that <b>the
     * beginning<b/> of "der Mann" stands at
     * the beginning of a sentence and that <b>the end</b> (because
     * this constraint comes after the slash) stands at the
     * beginning of a paragraph. The "end" means the last item, here
     * "Mann", so this token comes at the beginning
     * of a paragraph. To capture this, we choose spanRefs: The last
     * item of X matches the first item of the span (here: P).
     * 
     * @param cond
     * @param distributedOperands
     * @param mode
     * @return
     */
    private Map<String, Object> processPositionCondition (Tree cond,
            ArrayList<Object> distributedOperands, String mode) {
        boolean negated = false;
        String elem; // the 'span' (s/p/t)
        KoralFrame position = KoralFrame.MATCHES; // default
        // spanRef to be used for the element ('span')
        Integer[] elemSpanRef = null;
        // spanRef to be used for the subquery X
        Integer[] hitSpanRef = null;

        String nodeString = cond.toStringTree();
        if (nodeString.startsWith("-")) {
            negated = true;
            nodeString = nodeString.substring(1);
        }
        else if (nodeString.startsWith("+")) {
            nodeString = nodeString.substring(1);
        }
        elem = nodeString.substring(0, 1);
        nodeString = nodeString.substring(1);
        // in cases where the end of X shall match the beginning of
        // the span, or vice versa,
        // we need to define spanRefs
        if (mode.equals("beg")) {
            if (nodeString.equals("a")) {
                position = KoralFrame.STARTS_WITH;
            }
            else if (nodeString.equals("e")) {
                hitSpanRef = new Integer[] { 0, 1 };
                elemSpanRef = new Integer[] { -1, 1 };
            }
        }
        else if (mode.equals("end")) {
            if (nodeString.equals("e")) {
                position = KoralFrame.ENDS_WITH;
            }
            else if (nodeString.equals("a")) {
                hitSpanRef = new Integer[] { 0, 1 };
                elemSpanRef = new Integer[] { -1, 1 };
            }
        }
        // Create the position group and add the span and the subquery
        // as operands, possibly wrapped in spanRefs
        ArrayList<KoralFrame> frames = new ArrayList<KoralFrame>();
        frames.add(position);
        Map<String, Object> positionGroup =
                KoralObjectGenerator.makePosition(frames);
        if (negated) positionGroup.put("exclude", true);
        ArrayList<Object> posOperands = new ArrayList<Object>();
        Map<String, Object> classGroup =
                KoralObjectGenerator.makeSpanClass(128 + classCounter++);
        classGroup.put("operands", distributedOperands);
        positionGroup.put("operands", posOperands);
        Map<String, Object> span = KoralObjectGenerator.makeSpan(elem);
        objectStack.push(classGroup);
        if (hitSpanRef != null) {
            Map<String, Object> spanRefAroundHit = KoralObjectGenerator
                    .makeSpanReference(hitSpanRef, KoralOperation.FOCUS);
            ((ArrayList<Object>) spanRefAroundHit.get("operands"))
                    .add(classGroup);
            // re-assign after wrapping classGroup in spanRef
            classGroup = spanRefAroundHit;
        }
        if (elemSpanRef != null) {
            Map<String, Object> spanRefAroundSpan = KoralObjectGenerator
                    .makeSpanReference(elemSpanRef, KoralOperation.FOCUS);
            ((ArrayList<Object>) spanRefAroundSpan.get("operands")).add(span);
            // re-assign after wrapping span in spanRef
            span = spanRefAroundSpan;
        }
        posOperands.add(span);
        posOperands.add(classGroup);
        return positionGroup;
    }


    private Map<String, Object> parseOPINOptions (Tree node,
            boolean isExclusion) {
        Tree posnode = getFirstChildWithCat(node, "POS");
        Tree groupnode = getFirstChildWithCat(node, "GROUP");

        Map<String, Object> posOptions = new HashMap<String, Object>();
        ArrayList<KoralFrame> positions = new ArrayList<KoralFrame>();
        ArrayList<ClassRefCheck> classRefCheck = new ArrayList<ClassRefCheck>();
        posOptions.put("matchall", false);

        String posOption = "";
        if (posnode != null) {
            posOption = posnode.getChild(0).toStringTree().toUpperCase();
        }

        if (isExclusion) {
            checkINWithExclusionOptions(posOption, positions, classRefCheck);
        }
        else {
            checkINOptions(node,posOption, positions, classRefCheck);
        }

        posOptions.put("frames", Converter.enumListToStringList(positions));

        if (isComplexQuery(node, "ARG2")) {
            if (!posOption.equals("FI") && !posOption.equals("FE")) {
                classRefCheck.add(ClassRefCheck.INCLUDES);
            }
            checkRange(node);
        }
        else if (!isExclusion) {
            if (!classRefCheck.isEmpty()) {
                wrapOperandInClass(node, 2, 128 + classCounter++);
            }
            else{
                checkRange(node);
            }
        }
        
//        if (classRefCheck.contains(ClassRefCheck.INCLUDES)) {
////            wrapOperandInClass(node, 1, 128 + classCounter++);
//            
////            if (classRefCheck.contains(ClassRefCheck.EQUALS)){
////                classRefCheck.remove(ClassRefCheck.EQUALS);
////            }
//            if (classRefCheck.contains(ClassRefCheck.DIFFERS)){
//                wrapOperandInClass(node, 2, 128 + classCounter++);
//            }
//        }
//        else if (classRefCheck.contains(ClassRefCheck.EQUALS)
//                || classRefCheck.contains(ClassRefCheck.DIFFERS)) {
////            wrapOperandInClass(node, 1, 128 + classCounter++);
//            wrapOperandInClass(node, 2, 128 + classCounter++);
//        }
        
        posOptions.put("classRefCheck", classRefCheck);
        
        Boolean grouping = false;
        if (groupnode != null && groupnode.getChild(0).toStringTree()
                .equalsIgnoreCase("max")) {
            grouping = true;
        }
        posOptions.put("grouping", grouping);

        return posOptions;
    }

    private void checkRange (Tree node) {
        // if (range.equals("all")) {

        // posOptions.put("matchall", true);
        // Map<String,Object> ref =
//         makeResetReference(); // reset all defined classes
        // wrapOperand(node,2,ref);
        //}
        
        Tree rangenode = getFirstChildWithCat(node, "RANGE");
        if (rangenode != null){
            String range = rangenode.getChild(0).toStringTree().toLowerCase();
            // HIT is default in C2
            if (range.equals("all")){
                wrapOperandInClass(node, 2, 128 + classCounter++);
            }
        }
    }
    
    private boolean isComplexQuery (Tree node, String arg) {
        Tree argNode =
                getFirstChildWithCat(node, arg).getChild(0);
        if (getFirstChildWithCat(argNode, "ARG2") != null) {
            return true;
        }
        return false;

    }

    private void checkINOptions (Tree node, String posOption,
            ArrayList<KoralFrame> positions,
            ArrayList<ClassRefCheck> classRefCheck) {
        
        switch (posOption) {
            case "L":
                positions.add(KoralFrame.STARTS_WITH);
                break;
            case "R":
                positions.add(KoralFrame.ENDS_WITH);
                break;
            case "F":
                positions.add(KoralFrame.MATCHES);
                break;
            case "FE":
                positions.add(KoralFrame.MATCHES);
                classRefCheck.add(ClassRefCheck.EQUALS);
                break;
            case "FI":
                positions.add(KoralFrame.MATCHES);
                classRefCheck.add(ClassRefCheck.DIFFERS);
                break;
            case "N":
                positions.add(KoralFrame.IS_AROUND);
                break;
            default:
                positions.add(KoralFrame.MATCHES);
                positions.add(KoralFrame.STARTS_WITH);
                positions.add(KoralFrame.ENDS_WITH);
                positions.add(KoralFrame.IS_AROUND);
                
        }
        
    }


    private void checkINWithExclusionOptions (String posOption,
            ArrayList<KoralFrame> positions,
            ArrayList<ClassRefCheck> classRefCheck) {
        if (CosmasPosition.N.name().equals(posOption)) {
            positions.add(KoralFrame.IS_WITHIN);
            return;
        }
        else if (CosmasPosition.L.name().equals(posOption)) {
            positions.add(KoralFrame.ALIGNS_LEFT);
        }
        else if (CosmasPosition.R.name().equals(posOption)) {
            positions.add(KoralFrame.ALIGNS_RIGHT);
        }
        else if (CosmasPosition.FE.name().equals(posOption)) {
            positions.add(KoralFrame.MATCHES);
            classRefCheck.add(ClassRefCheck.DIFFERS);
        }
        else if (CosmasPosition.FI.name().equals(posOption)) {
            positions.add(KoralFrame.MATCHES);
            classRefCheck.add(ClassRefCheck.EQUALS);
        }
        else if (CosmasPosition.F.name().equals(posOption)) {
            positions.add(KoralFrame.MATCHES);
        }
        else {
            positions.add(KoralFrame.ALIGNS_LEFT);
            positions.add(KoralFrame.ALIGNS_RIGHT);
            positions.add(KoralFrame.IS_WITHIN);
            positions.add(KoralFrame.MATCHES);
        }

    }


    private Map<String, Object> parseOPOVOptions (Tree node, boolean isExclusion) {
        Tree posnode = getFirstChildWithCat(node, "POS");
        Tree exclnode = getFirstChildWithCat(node, "EXCL");
        Tree groupnode = getFirstChildWithCat(node, "GROUP");
        Map<String, Object> posOptions = new HashMap<String, Object>();
        ArrayList<KoralFrame> positions = new ArrayList<KoralFrame>();
        ArrayList<ClassRefCheck> classRefCheck = new ArrayList<ClassRefCheck>();
        posOptions.put("matchall", false);
        String posOption = "";
        if (posnode != null) {
            posOption = posnode.getChild(0).toStringTree();
        }
        
        if (isExclusion){
            classRefCheck.add(ClassRefCheck.DISJOINT);
            checkOVExclusionOptions(posOption, positions, classRefCheck);
        }
        else{
            checkOVOptions(posOption, positions, classRefCheck);
        }
        
        posOptions.put("frames", Converter.enumListToStringList(positions));
        posOptions.put("classRefCheck", classRefCheck);
        if (isComplexQuery(node, "ARG2")) {
            checkRange(node);
        }
        else if (!classRefCheck.isEmpty()){
            wrapOperandInClass(node, 2, 128 + classCounter++);
        }
        else{
            checkRange(node);
        }
//        if (exclnode != null) {
//            if (exclnode.getChild(0).toStringTree().equals("YES")) {
//                negatePosition = !negatePosition;
//            }
//        }

//        if (negatePosition) {
//            posOptions.put("exclude", "true");
//        }

        boolean grouping = false;
        if (groupnode != null) {
            if (groupnode.getChild(0).toStringTree().equalsIgnoreCase("max")) {
                grouping = true;
            }
        }
        posOptions.put("grouping", grouping);

        return posOptions;
    }


    private void checkOVExclusionOptions (String posOption,
            ArrayList<KoralFrame> positions,
            ArrayList<ClassRefCheck> classRefCheck) {
        
        switch (posOption) {
            case "L":
                positions.add(KoralFrame.ALIGNS_LEFT);
                positions.add(KoralFrame.OVERLAPS_LEFT);
                classRefCheck.add(ClassRefCheck.INTERSECTS);
                classRefCheck.add(ClassRefCheck.DISJOINT);
                break;
            case "R":
                positions.add(KoralFrame.ALIGNS_RIGHT);
                positions.add(KoralFrame.OVERLAPS_RIGHT);
                classRefCheck.add(ClassRefCheck.INTERSECTS);
                classRefCheck.add(ClassRefCheck.DISJOINT);
                break;
            case "F":
                positions.add(KoralFrame.MATCHES);
                break;
            case "FE":
                classRefCheck.add(ClassRefCheck.DIFFERS);
                positions.add(KoralFrame.MATCHES);
                break;
            case "FI":
                classRefCheck.add(ClassRefCheck.EQUALS);
                positions.add(KoralFrame.MATCHES);
                break;
            case "X":
                positions.add(KoralFrame.IS_WITHIN);
                break;
        }
        
    }


    private void checkOVOptions (String posOption, 
            ArrayList<KoralFrame> positions, 
            ArrayList<ClassRefCheck> classRefCheck) {
        classRefCheck.add(ClassRefCheck.INTERSECTS);
        switch (posOption) {
            case "L":
                positions.add(KoralFrame.STARTS_WITH);
                positions.add(KoralFrame.OVERLAPS_LEFT);
                break;
            case "R":
                positions.add(KoralFrame.ENDS_WITH);
                positions.add(KoralFrame.OVERLAPS_RIGHT);
                break;
            case "F":
                positions.add(KoralFrame.MATCHES);
                break;
            case "FE":
                positions.add(KoralFrame.MATCHES);
                classRefCheck.add(ClassRefCheck.EQUALS);
                classRefCheck.remove(ClassRefCheck.INTERSECTS);
                break;
            case "FI":
                positions.add(KoralFrame.MATCHES);
                classRefCheck.add(ClassRefCheck.DIFFERS);
                break;
            case "X":
                positions.add(KoralFrame.IS_AROUND);
                break;
            default:
                positions.add(KoralFrame.MATCHES);
                positions.add(KoralFrame.STARTS_WITH);
                positions.add(KoralFrame.ENDS_WITH);
                positions.add(KoralFrame.IS_AROUND);
                break;
        }
        
    }


    @SuppressWarnings({ "unchecked" })
    private Map<String, Object> wrap (Map<String, Object>[] wrapCascade) {
        int i;
        for (i = 0; i < wrapCascade.length - 1; i++) {
            ArrayList<Object> containerOperands =
                    (ArrayList<Object>) wrapCascade[i + 1].get("operands");
            containerOperands.add(0, wrapCascade[i]);
        }
        return wrapCascade[i];
    }


    @SuppressWarnings("unchecked")
    private void putIntoSuperObject (Map<String, Object> object, int objStackPosition) 
    
    	{
    	boolean bDebug = false;
    	
    	if( bDebug )
	    	{
	    	System.out.printf("Debug: putIntosuperObject(<>,int): objectStack.size=%d objStackPos=%d object=%s.\n", 
	    				objectStack.size(), objStackPosition, object == null ? "null" : "not null");
	    
	    	if( objectStack != null && objectStack.size() > 0 )
	    		System.out.printf("Debug: putIntosuperObject: objectStack = %s.\n",  objectStack.toString());
	    	
	    	if( invertedOperandsLists != null )
	    		System.out.printf("Debug: putIntosuperObject: invertedOperandsLists: [%s].\n", invertedOperandsLists.toString());
	    	}


    	if (objectStack.size() > objStackPosition) 
        	{
            ArrayList<Object> topObjectOperands =
                    (ArrayList<Object>) objectStack.get(objStackPosition).get("operands");
            
            if( bDebug )
            	System.out.printf("Debug: putIntosuperObject: topObjectOperands = [%s].\n", topObjectOperands == null ? "null" : "not null");
            
            objectStack.get(objStackPosition);
            
            if (!invertedOperandsLists.contains(topObjectOperands)) 
            	{
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


    private void putIntoSuperObject (Map<String, Object> object) {
        putIntoSuperObject(object, 0);
    }


    /**
     * Normalises position operators to equivalents using #BED
     */
    private String rewritePositionQuery (String q) {
        Pattern p = Pattern.compile("(\\w+):(([+\\-])?(sa|se|pa|pe|ta|te),?)+");
        Matcher m = p.matcher(q);

        String rewrittenQuery = q;
        while (m.find()) {
            String match = m.group();
            String conditionsString = match.split(":")[1];
            Pattern conditionPattern =
                    Pattern.compile("([+\\-])?(sa|se|pa|pe|ta|te)");
            Matcher conditionMatcher =
                    conditionPattern.matcher(conditionsString);
            StringBuilder replacement = new StringBuilder("#BED(" + m.group(1) + " , ");
            while (conditionMatcher.find()) {
                replacement.append(conditionMatcher.group()).append(",");
            }
            // remove trailing comma and close parenthesis
            replacement = new StringBuilder(replacement.substring(0, replacement.length() - 1) + ")");
            rewrittenQuery = rewrittenQuery.replace(match, replacement.toString());
        }
        return rewrittenQuery;
    }


    private Map<String, Object> termToFieldMap (String term) {

        // regex group #2 is foundry, #4 layer, #5 operator,
        // #6 key, #8 value
        String wordOrRegex = "\\w+|\".+?\"";
        // TODO: Should be initialized globally
        Pattern p = Pattern.compile("((\\w+)/)?((\\w*)(!?=))?(" + wordOrRegex
                + ")(:(" + wordOrRegex + "))?");
        Matcher m;

        m = p.matcher(term);
        if (!m.matches()) {
            addError(StatusCodes.INCOMPATIBLE_OPERATOR_AND_OPERAND,
                    "Something went wrong parsing the argument in MORPH() or #ELEM().");
            requestMap.put("query", new HashMap<String, Object>());
            return null;
        };

        Map<String, Object> fieldMap = null;
        fieldMap = KoralObjectGenerator.makeTerm();

        if (m.group(2) != null) fieldMap.put("foundry", m.group(2));
        if (m.group(4) != null) fieldMap.put("layer", m.group(4));
        if (m.group(5) != null) {
            if ("!=".equals(m.group(5))) negate = !negate;
        }
        if (m.group(6) != null) {
            String key = m.group(6);
            if (key.startsWith("\"") && key.endsWith("\"")) {
                key = key.substring(1, key.length() - 1);
                fieldMap.put("type", "type:regex");
            }
            fieldMap.put("key", key);
        }

        if (m.group(8) != null) {
            String value = m.group(8);
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
                fieldMap.put("type", "type:regex");
            }
            fieldMap.put("value", value);
        }

        // negate field (see above)
        if (negate) {
            fieldMap.put("match", KoralMatchOperator.NOT_EQUALS.toString());
        }
        else {
            fieldMap.put("match", KoralMatchOperator.EQUALS.toString());
        };
        return fieldMap;
    };


    private Tree parseCosmasQuery (String query) {
        
    	query = rewritePositionQuery(query);
        Tree tree = null;
        Antlr3DescriptiveErrorListener errorListener =
                new Antlr3DescriptiveErrorListener(query);
        try {
            ANTLRStringStream ss = new ANTLRStringStream(query);
            c2psLexer lex = new c2psLexer(ss);
            org.antlr.runtime.CommonTokenStream tokens =
                    new org.antlr.runtime.CommonTokenStream(lex); // v3
            
            parser = new c2psParser(tokens);
           
            // Use custom error reporters
            lex.setErrorReporter(errorListener);
            ((c2psParser) parser).setErrorReporter(errorListener);
            c2psParser.c2ps_query_return c2Return =
                    ((c2psParser) parser).c2ps_query(); // statt t().

            // AST Tree anzeigen:
            tree = (Tree) c2Return.getTree();

            if (DEBUG) 
            	{
            	System.out.printf("Debug: parseCosmasQuery: tree = '%s'.\n", tree.toStringTree());
            	log.debug(tree.toStringTree());
            	}
            }
        catch (FailedPredicateException fe)
	        { // unused so far - 11.01.24/FB
        	System.out.printf("parseCosmasQuery: FailedPredicateException!\n");
            addError(StatusCodes.MALFORMED_QUERY,
                    "failed predicate on prox something.");
	        }
        catch (RecognitionException e) {
        	// unused so far - 11.01.24/FB
        	System.out.printf("Debug: out: parseCosmasQuery: RecognitionException!\n");
            log.error(
                    "Could not parse query. Please make sure it is well-formed.");
            addError(StatusCodes.MALFORMED_QUERY,
                    "Could not parse query. Please make sure it is well-formed.");
        }

        String treestring = tree.toStringTree();
        
        boolean erroneous = false;
        if (parser.failed() || parser.getNumberOfSyntaxErrors() > 0) {
            erroneous = true;
            tree = null;
        }

        if (erroneous || treestring.contains("<mismatched token")
                || treestring.contains("<error")
                || treestring.contains("<unexpected")) 
        {
        	//System.err.printf("Debug: parseCosmasQuery: tree: '%s'.\n", treestring);
        	//System.err.printf("Debug: parseCosmasQuery: FullErrorMsg:  '%s'.\n", errorListener.generateFullErrorMsg().toString());
        	log.error(errorListener.generateFullErrorMsg().toString());
            addError(errorListener.generateFullErrorMsg());
        }

        // collect and report errors found by other functions than the lexer/parser:
        // tree might already be null if another error was reported above.
        if( reportErrorsinTree(tree) == true )
        {
        	if( DEBUG )
        		System.out.printf("Debug: parseCosmasQuery: reportErrorsinTree at least 1 error message found. Setting tree = null.\n");
            return null;
        }
        else
        	{
        	if(DEBUG)
        		System.out.printf("Debug: parseCosmasQuery: reportErrorsinTree has found no error messages.\n");
        	}
    	
        return tree;
    } // parseCosmasQuery
}
