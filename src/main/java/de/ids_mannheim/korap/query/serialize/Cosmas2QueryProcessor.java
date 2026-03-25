package de.ids_mannheim.korap.query.serialize;

//import de.ids_mannheim.korap.query.parse.cosmas.c2ps_opPROX; // error codes.
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
import de.ids_mannheim.korap.query.serialize.util.MORPH_Mapper;
import de.ids_mannheim.korap.query.serialize.util.ResourceMapper;
import de.ids_mannheim.korap.query.serialize.util.StatusCodes;
import de.ids_mannheim.korap.util.*;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.FailedPredicateException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
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
 * @author Franck Bodmer (bodmer@ids-mannheim.de)
 * @version 0.4 -- 09.06.26/FB
 */
public class Cosmas2QueryProcessor extends Antlr3AbstractQueryProcessor {

	private static final boolean DEBUG 		 	= false;
	private static final boolean bShowTokens 	= false;
	private static final boolean showRegGroups	= false;
	
    private static Logger log =
            LoggerFactory.getLogger(Cosmas2QueryProcessor.class);

    private static final int messLang = StatusCodes.MLANG_GERMAN;
    
    private LinkedList<Map<String, Object>[]> toWrapStack =
            new LinkedList<Map<String, Object>[]>();
    
    // default layer for part of speech: 'p'; 'pos' is for Poliqarp.
    private static final String LAYER_POS   = "p"; 
    private static final String LAYER_S	 	= "s"; // structure layer for span: e.g. <s>, <p>, <head>, etc. 

    // types for handling of #ELEM() and MORPH()
    private static final int opELEM			= 1;
    private static final int opMORPH		= 2;
    
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

	static void printLexerTokens(CommonTokenStream tokens )
		
	{
		if( tokens == null )
			{
			System.out.printf("Debug: Tokens: empty!\n");
			return;
			}
		
		tokens.fill();
		
		if( tokens.size() == 0 )
			{
			System.out.printf("Debug: Tokens: empty!\n");
			return;
			}
		
		for( Token token : tokens.getTokens())
			{
			//System.out.printf("Debug: Token: '%s'.\n",  token.toString());
			System.out.printf("Debug: Token: '%s'.\n",  token.toString());
			}
	}

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
    			errMess = node.getChild(2) != null ? node.getChild(2).getText() : StatusCodes.getErrMess(StatusCodes.UNKNOWN_QUERY_ERROR, messLang, "");
    			
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

    	if( DEBUG )
    		System.out.printf("Debug: processOPBED: '%s'.\n",  node.toStringTree());
    	
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


    private void printOPELEM(Tree node)
    {
    	int nChilds = node.getChildCount();
    	
		System.out.printf("Debug: processOPELEM: Tree node'%s'.\n", node.toStringTree());
		System.out.printf("Debug: processOPELEM: #childs: %d.\n", nChilds);
		
		for(int i=0; i<nChilds; i++)
		{
			Tree child = node.getChild(i);
			System.out.printf("Debug: processOPELEM: child[%d]='%s' #=%d.\n", i, child.toStringTree(),
					child.getChildCount());
		}
    }
   
    /*
     * isLayerForPOS()
     * 
     * returns true if layer applies to a POS query, i.e. a token Query.
     *         false in all other cases, i.e. they apply to a span query.
     * - foundries are not handled in Koral, so they cannot be used to determine
     *   if a query relates to token or span.
     * - "ne" = named entities: span or token?
     * 13.05.26/FB
     */
    
    private boolean isLayerForPOS(String layer)
    
    {
    	if( layer.equals("p") || layer.equals("l") || layer.equals("m") || layer.equals("ne") )
    		return true;
    	
    	return false; // not for POS
    }
    	
    /* addSubVal()
     * 
     * sets fields "key" and "value" from attVal.
     * if attVal == "v1:v2" -> key : "v1", value : "v2"
     * else -> value : "attVal".
     * return: 
     * 		true if a negative value is stored in field 'value'. E.g. Attval = "number:-PL",
     * 		false if not. 
     * 05.05.26/FB
     */
    private boolean addSubVal(Map<String, Object> term, String attVal)
    
    {
    	boolean
    		isNeg;
    	String[]
    		splitted = attVal.split(":");
    	/*
    	System.out.printf("Debug: addSubVal: attVal='%s' splitted.size=%d.\n", attVal, splitted.length);
    	if( splitted.length > 1)
    		System.out.printf("Debug: addSubVal: splitted[0]='%s' %d, splitted[1]='%s' %d.\n", splitted[0], splitted[0].length(), splitted[1], splitted[1].length());
		*/
    	
    	// avoid special case: attVal=":abc" -> splitted.length = 2 & splitted[0] = '', or splitted.length > 2, e.g. attVal="abc:def:ghi" with multiple ':'.
    	if( splitted.length == 2 && splitted[0].length() > 0 && splitted[1].length() > 0 )
    		{ // attVal = "key:value", e.g. "gender:fem".
    		term.put("key", splitted[0]);
    		isNeg = addNegVal(term, "value", splitted[1]);
    		}
    	else
    		{ // not splitted: store key : attVal.
    		isNeg = addNegVal(term, "key", attVal);
    		}
    	
    	return isNeg;
    }
    
    /* addNegVal:
     * - add AttVal to field 'field' of 'term'.
     * - if attVal ='-SG' : add 'SG' to the field and return isNeg=true.
     *      else: add AttVal as is and return isNeg=false.
     * 20.04.26/FB
     */
    
    private boolean addNegVal(Map<String, Object> term, String field, String attVal)
    
    {
    if( DEBUG )
    	System.out.printf("Debug: addNegVal: attVal = '%s'.\n", attVal);
   
   	if( attVal.charAt(0) == '-' )
		{
		term.put(field, attVal.substring(1)); // skip '-'.
		return true;
		}
	else
		{
		term.put(field, attVal);
		return false;
		}
	} // addNegValue

    /* addAttVal:
     * 
     * - normal case: adds key=attName and value=AttVal to 'term'.
     * - POS (part of speech) case: iPOS=true or attName='ana':
     *   adds key=attVal.
     *   attName is not added, but it switches to layer=LAYER_POS, so 'value' is unused.
     *   attVal: '-Val' is converted to key=Val (without '-') and match=NOT_EQUALS.
     * - returns true if layer is specified and it applies to a part of speech query (isLayerPOS = true). 
     *           e.g. layer=p
     * 17.04.26/FB
     * 11.05.26/FB all layers added which apply to a POS query -> this is dereko specific!
     */
    
    private boolean addAttVal(Map<String, Object> term, boolean isPOS, String nodeCat, String attName, String attVal)
    
    {
    boolean 
    	isNeg 		= false,
    	isLayerPOS 	= false;
    String[] 
        	splitted = attName.split("/");
        
    if( DEBUG )
    	System.out.printf("Debug: addAttVal: isPOS=%b attName='%s' attVal='%s'.\n",
    			isPOS, attName, attVal);
    
    if (splitted.length > 1) 
    	{ // foundry+layer specified: #ELEM(dereko/s=val).
    	if(DEBUG)
    		System.out.printf("Debug: processOPELEM: splitted[0]='%s' splitted[1]='%s'.\n",
    				splitted[0], splitted[1]);
    	
        term.put("foundry", splitted[0]);
        term.put("layer",   splitted[1]);
        isNeg = addSubVal(term, attVal);
       
        if( isLayerForPOS(splitted[1]) )
        	isLayerPOS = true;
        }
    else if( isPOS || attName.equals("ana") )
    	{ // case for part of speech POS: isPOS: #ELEM(W ana='N'), or att='ana': #ELEM(ana='N').
    	  // if attVal == -value : set NOT_EQUAL and remove negation mark.
    	if( attName.equals("ana") == true )
    		{ // 'ana' is not stored. a. attVal="NOU" -> store "key" = attVal (no "value");
    		  // b. attVal="gender:fem" -> store "key" = "gender" and "value" = "fem":
    		isNeg = addSubVal(term, attVal);
            }
    	else
    		{
    		// still layer=p (isPOS==true), but key == value should be searched as this is not a POS att-value-pair.
    		// no attVal = "x:y" expected.
    		term.put("key", attName);
            isNeg = addNegVal(term, "value", attVal);
    		}
    		    		
    	term.put("layer", LAYER_POS);
    	}
    else
    	{ // normal case: layer = s, not a POS:
    	  // no attVal = "x:y" expected.
		term.put("key", attName);
        term.put("layer", "s"); // "s" is default for #ELEM().
        isNeg = addNegVal(term, "value", attVal);
    	}
    
    // consider the negation mark in front of the key, too:
    KoralMatchOperator match =
            isNeg == true || nodeCat.equals("NOTEQ")
                    ? KoralMatchOperator.NOT_EQUALS
                    : KoralMatchOperator.EQUALS;
    
    term.put("match", match.toString());
    return isLayerPOS;
	} // addAttVal

    /* hasPOS()
     * - returns true if tree 'node' has a 'W' element name or, if there is not element name specified,
     *   any of its attribute is 'ana'.
     * - returns false else.
     * - E.g. #ELEM(W ana=NOU), #ELEM(ana=NOU), #ELEM(W): return true.
     *        #ELEM(Head), #ELEM(head ana=top): return false.
     * 24.04.26/FB
     * 
     */
    private boolean hasPOS(Tree node)
    
    {
    	Tree
    		elNode = getFirstChildWithCat(node, "ELNAME");
    	
        if( elNode != null )
        	{ // if element has a name, it must be "W".
            if( elNode.getChild(0).toString().toLowerCase().equals("w") )
	        	return true;
	        else
            	return false;
	    	}
        	
        // if element has no name, see if it has an attribute name = "ana":
    	for (int i = 0; i < node.getChildCount(); i++) 
        	{
    		Tree 
    			child = node.getChild(i);
    		if( child.getChildCount() > 0)
    			{
	    		String
	    			attName = child.getChild(0).toStringTree().toLowerCase(); // attr. name lowered.
	    		if( attName.equals("ana") )
	            	return true;
	        	}
        	}
        return false;
    }
    
    /* TODO: The handling of attributes vs. element names is somehow disputable ...
     * E.g. Tree node'(OPELEM (ELNAME BoBo) (EQ type TOP) (EQ ana N SG))'
     * -> 3 children: (ELNAME Bobo), (EQ type TOP), (EQ ana N SG).
     *    child[0]='(ELNAME BoBo)' #subChildren=1.
		  child[1]='(EQ type TOP)' #subChildren=2.
	 	  child[2]='(EQ ana N SG)' #subChildren=3.
	 *
     * - Element name expected by the grammar should be a single name without foundry/layer:
     *   e.g. "HEAD"          : (ELNAME head) : OK; 
     *        "dereko/s=HEAD" : (EQ dereko/s head) : node ELNAME is lost!
     * Corrections: 
     * 24.03.26/FB missing default layer 's' added.
     * 26.03.26/FB correct field names to attributes.
     * 27.03.26/FB attribute name to lowercase, attribute value not: corrected.
     *             element with several attributes and attributes with several values: corrected.
     * 23.04.26/FB spec. case: #ELEM(w ana=..) is a Koral:term, not a Koral:span.
     */
    
    @SuppressWarnings("unchecked")
    private void processOPELEM (Tree node) 
    {
    	if( DEBUG )
    		printOPELEM(node);
    
        // Step I: create element: 'span' = a span or a term (spec. case: #ELEM(w).
        Map<String, Object> 
        	span; // = KoralObjectGenerator.makeSpan();
        boolean
        	isLayerPOS = false;
        
        if( hasPOS(node))
        	span = KoralObjectGenerator.makeToken(); // not makeTerm().
        else
        	span = KoralObjectGenerator.makeSpan();
        
        if (node.getChild(0).toStringTree().equals("EMPTY")) {
            addError(StatusCodes.MALFORMED_QUERY, "Empty #ELEM() operator."
                    + " Please specify a valid element name (like 's' for sentence).");
            return;
        }
       
        int elname = 0;
        boolean 
        	isPOS = false,	// wordpos = part of speech, if #ELEM(w ana=...).
        	isW   = false;	// #ELEM(W)
        
        Tree elnameNode = getFirstChildWithCat(node, "ELNAME");
        /*
        // TODO: This is identical to processOPMORPH
        String wordOrRegex = "\\w+|\".+?\"";
        Pattern p = Pattern.compile("((\\w+)/)?((\\w*)(!?=))?(" + wordOrRegex
        							+ ")(:(" + wordOrRegex + "))?");
        */

        if (elnameNode != null) 
        {
        	if( DEBUG )
        		System.out.printf("Debug: processOPELEM: elnameNode='%s'.\n", elnameNode.toStringTree());

        	String
        		elName = elnameNode.getChild(0).toString().toLowerCase();

        	Map<String, Object> 
            	fm = termToFieldMap(elName); // key=elName.

            if (fm == null) 
            	return;

            // special keys -> special layers:
            if( elName.equals("w"))
            	{
            	// default layer for word pos. = LAYER_POS.
            	fm.put("layer", LAYER_POS); 
            	fm.remove("key"); // remove 'w' as a key as it is not stored in KorAP.
            	isPOS = true;
            	isW   = true;
            	}
            else
            	fm.put("layer", LAYER_S); // default layer for span.

        	// add 'wrap' only if not #ELEM(W), i.e. element 'W' withou children:
            if( !isW || node.getChildCount() > 1 ) 
            	span.put("wrap", fm);
	            
            elname = 1;                
        }
        else
        	{
        	// for #ELEM( att=val ) without an element name, also add a "wrap" with an empty key;
        	// otherwise the att/val pair(s) will be inserted outside a "wrap".
        	Map<String, Object> 
        		wrap = KoralObjectGenerator.makeTerm();
            
            span.put("wrap", wrap); // adds an empty "wrap".
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
            
            // 1 child = attribute; foundry/layer not expected for attributes,
            // they are the same for all attributes:
            for (int i = elname; i < node.getChildCount(); i++) 
            {
                Tree attrNode = node.getChild(i);
                if( DEBUG ) 
                	System.out.printf("Debug: processOPELEM: attrNode='%s #=%d'.\n", attrNode.toStringTree(), attrNode.getChildCount());
                if (attrNode.getChildCount() == 2) 
                	{
                    Map<String, Object> term =
                            KoralObjectGenerator.makeTerm();
                    termGroupOperands.add(term);
                    
                    // String layer = attrNode.getChild(0).toStringTree();
                    if( DEBUG ) System.out.printf("Debug: processOPELEM: child[0]='%s' child[1]='%s'.\n", 
                    		attrNode.getChild(0).toStringTree(),
                    		attrNode.getChild(1).toStringTree());
                    
                    String 
                    	nodeCat = getNodeCat(attrNode),
                    	attName = attrNode.getChild(0).toStringTree().toLowerCase(), // attr. name lowered.
                    	attVal  = attrNode.getChild(1).toStringTree();
                    
                    if( addAttVal(term, isPOS, nodeCat, attName, attVal) )
                        isLayerPOS = true;
                	}
                else 
                	{ // childCount != 2:
                    Map<String, Object> subTermGroup = KoralObjectGenerator
                            .makeTermGroup(KoralTermGroupRelation.AND);
                    ArrayList<Object> subTermGroupOperands =
                            (ArrayList<Object>) subTermGroup.get("operands");
                    if( DEBUG )
                    	System.out.printf("Debug: att='%s', #values = %d.\n", 
                    			attrNode.getChild(0).toStringTree(),
                    			attrNode.getChildCount());
                    
                    if( attrNode.getChild(0).toStringTree().toLowerCase().equals("ana") )
                    	isPOS = true; // for the case 'W' is not specified.
                    
                    int j;
                    for (j = 1; j < attrNode.getChildCount(); j++) {
                        Map<String, Object> term =
                                KoralObjectGenerator.makeTerm();
                        if( DEBUG )
                        	System.out.printf("Debug: att='%s' val[%d]='%s' isPOS=%b.\n", 
                        			attrNode.getChild(0).toStringTree(), j,
                        			attrNode.getChild(j).toStringTree(), isPOS);
                        String 
                        	nodeCat = getNodeCat(attrNode),
                        	attName = attrNode.getChild(0).toStringTree().toLowerCase(), // attr. name lowered.
                        	attVal  = attrNode.getChild(j).toStringTree();
                        
                        if( addAttVal(term, isPOS, nodeCat, attName, attVal) )
                            isLayerPOS = true;
                        
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
            if (termGroupOperands.size() == 1) 
                termGroup = (Map<String, Object>) termGroupOperands.get(0);

            // if 'wrap' exists, add termGroup as 'attr' to 'wrap',
            // else add termGroup as 'attr'.
        	Object
        		wrap = span.get("wrap");
            
            if ( wrap != null ) 
            	{
            	Map<String, Object> 
            		fmWrap = (Map <String, Object>)wrap;
            	fmWrap.put("attr", termGroup);
            	if( DEBUG )
            		System.out.printf("Debug: processELEM: adding termGroup to 'attr' to 'wrap'.\n");
            	}
            else 
            	{
            	if( DEBUG )
            		System.out.printf("Debug: processElem: no 'wrap': adding termGroup into 'attr'.\n");
                span.put("attr", termGroup);
            	}
        }
        
        // replace 'span' by 'token' if layer has specified a part of speech:
        if( isLayerPOS )
        	span.put("@type",  "koral:token");
        
        // Step II: decide where to put
        putIntoSuperObject(span);
    }

    /* combineAnnot
     * - return the first 2 children of node as 1 combination,
     *   if at least 2 children exist.
     * 20.06.26/DB
     */
    
    private String combineAnnot(Tree node, int n)
    
    {
    if( node.getChildCount() < n )
    	return null; // no combination possible.
    
    String
    	combi = "";

    for(int i=0; i<n; i++)
	    {
    	if( i == 0 )
    		combi = combi.concat(node.getChild(i).toString().toUpperCase());
    	else
    		combi = combi.concat(" " + node.getChild(i).toString().toLowerCase());
	    }
    
    if( DEBUG )
    	System.out.printf("Debug: combineAnnot: combi='%s'.\n", combi);
    
    return combi;
    }
    
    /* combineAnnots:
     * tries combinations of the first 3 or 2 values and looks up for a match in MORPH_Mapper for CONNEXOR tags.
     * 19.06.26/FB
     */
    
    private int combineAnnots(Tree node, List<String>newAnnots)
    
    {
    final String 
    	func = "Debug: combineAnnots";
    String
    	annotCombi = null,
    	newAnnot;
    
    for(int n=3; n>1; n--)
    	{
	    if( node.getChildCount() >= n && (annotCombi = combineAnnot(node, n)) != null )
		    {
	    	newAnnot = MORPH_Mapper.translate_CONNEXOR(annotCombi); 
	        if( newAnnot != null )
		    	{
		    	if( DEBUG )
		    		System.out.printf("%s: combi='%s' -> '%s'-> returns %d.\n",  func, annotCombi, newAnnot, n);
		    	newAnnots.add(newAnnot);
		    	return n; // the first n values have been consumed.
		    	}
	        else
		        {
		        if( DEBUG ) System.out.printf("%s: combi='%s' -> null.\n", func, annotCombi);
			    }
		    }
    	}

    if( DEBUG ) 
    	System.out.printf("%s: returns 0.\n", func);
    
    return 0; // no combination matches, so no. values comsumed.
    }

    /* rearrangeChildren:
     * - rearrange order of children of node: POS children first, morph children last,
     * - keep sort order inside POS and morph values.
     * - E.g. MORPH(V imp -inf past -pcp) -> MORPH(V -inf -pcp imp past).
     * - Reason: "V -inf -pcp" must be kept together, because they translate together to a STTS POS;
     *         "imp", "past" are single morph annotations that translate one by one.
     * 22.06.26/FB 
     */
    
    private void rearrangeChildren(Tree node, int nChildren)
    
    {	final String 
    		func = "Debug: rearrangeChildren";
    	ArrayList<String>
    		children,				// POS and morph children.
    		morphChildren = null;	// morph children only.
    	
    	if( nChildren == 0 )
    		return; 
    	
    	if( DEBUG )
    		System.out.printf("%s: input: '%s'.\n", func, node.toStringTree());
    	
    	children 	  = new ArrayList<String>();
    	morphChildren = new ArrayList<String>();
    	
    	for(int i=0; i<nChildren; i++)
    		{
    		String child = node.getChild(i).toString();
    		
    		if( MORPH_Mapper.isMorphAnnot(child) == false )
    			children.add(child);
    		else
    			morphChildren.add(child);
    		}
    	
    	// add morph children after POS children:
    	for(int i=0; i<morphChildren.size(); i++)
    		children.add( morphChildren.get(i) );
    	
    	// overwrite children values in Tree node using order in children list.
    	for(int i=0; i<nChildren; i++)
    		{
    		Tree child 				= node.getChild(i);
    		CommonTree commonChild  = (CommonTree) child;
    		commonChild.getToken().setText(children.get(i));
    		}
 
       	if( DEBUG )
    		System.out.printf("%s: return node as '%s'.\n", func, node.toStringTree());
    }
    
    /* translate_MORPH_STTS:
     * 
     * - translates a MORPH/STTS expression to a STTS annotation,
     *   i.e. translates from C2-style to STTS-style.
     * - e.g. "VRB fin a"  -> VAFIN.
     * - e.g. "VRB fin -a" -> V.FIN -VAFIN.
     * - e.g. "VRB -fin a" -> V.* - F.FIN.
     * - e.g. "-VRB fin a" -> -V.*.
     * - 1st annot expected to be in upper case, the following annots to be in lower case.
     * Returns: STTS annotation if found, null else.
     * 16.05.26/FB
     */
    
    private List<String> translate_MORPH_STTS(Tree node)
    
    {
    final String 
    	func = "Debug: translate_MORPH_STTS";
    int
    	nChildren = node.getChildCount();
    List<String>
    	annots = new ArrayList<String>();
    List<String>
    	annots_STTS = null;
    int 
    	iFirst = -1;
   	String
		annotSTTS = null; 
    
   	for(int i=0; i<nChildren; i++)
   		{
   		String child = node.getChild(i).toString();
   		if( child.startsWith("-") )
   			{
   			child = child.substring(1);
   			if( iFirst == -1 )
   				iFirst = i;
   			}

   		if( i==0 )
   			annots.add(child.toUpperCase());
   		else
   			annots.add(child.toLowerCase());
   		}
   	
   	if( iFirst == -1 )
   		{ // simple translation when no annot. is negative:
   		annotSTTS = MORPH_Mapper.translate_STTS(annots);
   		if( annotSTTS != null )
   			{
   			annots_STTS = new ArrayList<String>(1);
   			annots_STTS.add(annotSTTS);
   	   	   	//System.out.printf("%s: %s translates to: '%s'.\n", func, annots.toString(), annotSTTS == null ? "null" : annotSTTS);
   	 		}
   		}
   	else if( iFirst == 0 )
	   	{ // "-VRB..." -> "-V.*" 
   		annotSTTS = MORPH_Mapper.translate_STTS(annots.subList(0, 1));
   		if( annotSTTS != null )
   			{
   			annots_STTS = new ArrayList<String>(1);
   			annots_STTS.add("-" + annotSTTS);
   	   	   	//System.out.printf("%s: %s translates to: '%s'.\n", func, annots.toString(), annotSTTS == null ? "null" : annotSTTS);
   	 		}	
	   	}
   	else
   		{ // this returns a list of 2 STTS annotations.
   		annots_STTS = MORPH_Mapper.translate_STTS_withNeg(annots, iFirst);
   		// System.out.printf("%s: %s translates to: '%s'.\n", func, annots.toString(), annots_STTS == null ? "null" : annots_STTS);
   	   	}

   	if( DEBUG )
   		System.out.printf("%s: %s translates to: '%s'.\n", func, annots.toString(), annots_STTS == null ? "null" : annots_STTS);
	   	
   	return annots_STTS;
    }
    
    /* translate_MORPH_CONNEXOR:
     * 
     * - translates a MORPH expression to a STTS annotation,
     *   i.e. translates vom C2-style to STTS-style.
     * - e.g. "VRB fin a"  -> VAFIN.
     * - e.g. "VRB fin -a" -> V.FIN -VAFIN.
     * - e.g. "VRB -fin a" -> V.* - F.FIN.
     * - e.g. "-VRB fin a" -> -V.*.
     * - tag combinations:
     *   e.g. "N prop" -> "NE"
     *   e.g. "N -prop" -> "NN"
     *   
     * Returns: STTS annotation if all values could be translated, null else.
     * 16.05.26/FB
     */
    
    private List<String> translate_MORPH_CONNEXOR(Tree node)
    
    {
    	final String 
    		func = "translate_MORPH_CONNEXOR";
    	int
     		nChildren = node.getChildCount();
	    List<String>
	     	newAnnots = new ArrayList<String>();
	    String 
	     	newAnnot;
	    int
	    	iSkip = 0; // default: skip no children.
	    boolean
	    	hasUnknownValues = false;
	    
	    // 0. rearrange order of children inside node: 
	    rearrangeChildren(node, nChildren);
	    
	    // 1. try combinations of the first 2 or 3 values including negative prefix.
	    //    iSkip returns the no. of consumed values, may be 0 if none matches.
	    //    the translated combination is added to newAnnots:
	    iSkip = combineAnnots(node, newAnnots);
	    
	    // 2. try to translate single values and/or the remaining values after combination has been found (iSkip>0):
	    //    break at the first value that cannot be translated.
    	for(int i=iSkip; i<nChildren; i++)
    		{
    		String 
    			child = node.getChild(i).toString();
    		boolean
    			isNeg = false;
    		
    		if( child.startsWith("-") )
				{
    			isNeg = true;
    			child = child.substring(1);
				}
    		
    		if( i==0 )
    			newAnnot = MORPH_Mapper.translate_CONNEXOR(child.toUpperCase());
    		else
    			newAnnot = MORPH_Mapper.translate_CONNEXOR(child.toLowerCase());
    		
    		// add translated annotation if found, else break.
    		if( newAnnot == null )
    			{
    			hasUnknownValues = true;
    			break; // cannot convert CONNEXOR annotation(s) fully.
    			}
    		
    		// convert to negative value
    		if( isNeg )
    			newAnnot = newAnnot.replace("=", "!=");
    		
    		newAnnots.add(newAnnot);	
    		}
    	
    	if( DEBUG )
    		System.out.printf("%s: node='%s' translates to '%s' unknown=%b.\n", func, node.toStringTree(), 
    							newAnnots != null ? newAnnots.toString() : "null", hasUnknownValues);
    	
    	return newAnnots == null || newAnnots.size() == 0 || hasUnknownValues ? null : newAnnots;
    }
    
    /* processOPMORPH: 
     * - STTS annotation in C2 style are translated to STTS style.
     * - e.g. "N ne" -> "NE". 
     * 27.03.26/FB
     */

    private void processOPMORPH (Tree node) 
    {
        // Step I: get info
    	final String
    		func = "Debug: processOPMORPH";
        Map<String, Object> 
        	token = KoralObjectGenerator.makeToken();
        ArrayList<Object> 
        	terms = new ArrayList<Object>(); 
        Map<String, Object> 
        	fieldMap = null;
        int	
        	nChildren = node.getChildCount();
	    
        // MORPH empty:
	    if( nChildren == 1 && node.getChild(0).toString().equals("EMPTY") )
		    {
		    addError(StatusCodes.MALFORMED_QUERY, "Empty MORPH() operator."
                    + " Please specify a valid part of speech (like 'MORPH(NOU)' or 'MORPH(tt/p=NOU)' for searching nouns).");
            return;
            }

	    // MORPH with STTS annotations in C2-style:
	    List <String>
	    	annots_STTS = null;  
	    List<String>
	    	annots_CONNEX = null;
	    
	    if( (annots_STTS = translate_MORPH_STTS(node)) != null )
		    { // translates MORPH expression to STTS expression, considering negative values.
	    	if( DEBUG )
	    		System.out.printf("Debug: processOPMORPH: annots_STTS='%s'.\n", annots_STTS == null ? "null" : annots_STTS);
		    for( String annot : annots_STTS)
	    		{
			    fieldMap = termToFieldMap(annot, opMORPH);	
			    terms.add(fieldMap);
	    		}
		    }
	    else if( (annots_CONNEX=translate_MORPH_CONNEXOR(node)) != null )
	    	{ // translates MORPH expression of CONNEXOR tagset to marmot pos and morphological expressions, considering negative values.
	    	if( DEBUG )
	    		System.out.printf("Debug: processOPMORPH: annots_CONNEX='%s'.\n", annots_CONNEX == null ? "null" : annots_CONNEX);
		    for( String annot : annots_CONNEX )
	    		{
			    fieldMap = termToFieldMap(annot, opMORPH);	
			    terms.add(fieldMap);
	    		}
	    	}
		else
		    { // neither STTS nor CONNEXOR:
			if( DEBUG ) System.out.printf("%s: processing other tags.\n", func);
		    
			for(int i=0; i<nChildren; i++)
		    	{
		    	String morphterm = node.getChild(i).toString();
		    	if( DEBUG )
		    		System.out.printf("Debug: processOPMORPH: morphterm = '%s'.\n", morphterm);
		    	
		        fieldMap = termToFieldMap(morphterm, opMORPH);
		        if (fieldMap == null)
		        	return;
			        
		        terms.add(fieldMap);
		    	}
		    }
	    
        if (nChildren == 1) 
        	{
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
        	if( DEBUG )
        		System.out.printf("Error: processOPWF_OPLEM: TPOS not implemented: '%s'!\n", node.toStringTree());
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
        
        //System.out.printf("Debug: processPositionCondition: mode='%s' node=='%s' elem='%s'.\n", mode, nodeString, elem);

        // in cases where the end of X shall match the beginning of
        // the span, or vice versa,
        // we need to define spanRefs
        if (mode.equals("beg")) {
        	//System.out.printf("Debug: processPositionCondition: 'beg' nodeString='%s'.\n", nodeString);
            if (nodeString.equals("a")) {
                position = KoralFrame.STARTS_WITH;
            }
            else if (nodeString.equals("e")) {
                hitSpanRef = new Integer[] { 0, 1 };
                elemSpanRef = new Integer[] { -1, 1 };
            }
        }
        else if (mode.equals("end")) {
        	//System.out.printf("Debug: processPositionCondition: 'end' nodeString='%s'.\n", nodeString);
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
     * 02.06.26/FB
     * - deactivated as it interacts with other operators.
     * - e.g. MORPH(temp:past) -> MORPH(#BEG(temp , pa)st) !
     * 
     */
    private String rewritePositionQuery (String q) {
        Pattern p = Pattern.compile("(\\w+):(([+\\-])?(sa|se|pa|pe|ta|te),?)+");
        Matcher m = p.matcher(q);

        System.out.printf("rewritePositionQuery: enter query='%s'.\n", q);
        
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
        
        System.out.printf("rewritePositionQuery: leave query='%s'.\n", rewrittenQuery);
        return rewrittenQuery;
    }

    // may be replaced by termToFieldMap(term, type).
    
    private Map<String, Object> termToFieldMap (String term) {

        // regex group #2 is foundry, #4 layer, #5 operator,
        // #6 key, #8 value
        String wordOrRegex = "\\w+|\".+?\"|'.+?'";
        // TODO: Should be initialized globally
        Pattern p = Pattern.compile("((\\w+)/)?((\\w*)(!?=|<>))?(" + wordOrRegex
                + ")(:(" + wordOrRegex + "))?");
        Matcher m;
        boolean negate = false;
        
        m = p.matcher(term);
        if (!m.matches()) {
            addError(StatusCodes.INCOMPATIBLE_OPERATOR_AND_OPERAND,
                    "Something went wrong parsing the argument in MORPH() or #ELEM().");
            requestMap.put("query", new HashMap<String, Object>());
            System.out.printf("Debug: termToFieldMap: term='%s' m.matches() = null!\n", term);
            return null;
        };

        Map<String, Object> fieldMap = null;
        fieldMap = KoralObjectGenerator.makeTerm();

        if( showRegGroups )	
        	{
            for(int j=0; j<= m.groupCount(); j++)
	        	{
	        	System.out.printf("Debug: group(%d) = '%s'.\n",  j, 
	        			m.group(j) != null ? m.group(j) : "null");
	        	}
        	}
        
        if (m.group(2) != null) fieldMap.put("foundry", m.group(2));
        if (m.group(4) != null) fieldMap.put("layer", m.group(4));
        if (m.group(5) != null) {
            if ("!=".equals(m.group(5)) || "<>".equals(m.group(5))) 
            	negate = !negate;
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

    /* isRegExpr():
     * returns true if str is a regular expression containing
     * 				'*', *?*, '+' or '.' not being excaped.
     * 09.06.26/FB
     */
    
    public boolean isRegExpr(String str)
    
    {
    	char c;
    	
    	for(int i=0; i<str.length(); i++)
	    	{
	    	c = str.charAt(i);
	    	if( c == '*' || c == '+' || c == '?' || c == '.' || c == '(' || c == ')' || c == '|' )
	    		{
	    		if( i == 0 || str.charAt(i-1) != '\\' )
	    			return true; // wildcard not being escaped.
	    		}
    		}
    	
    	return false; // not found.
    }
   
   	private void printRegGroups(Matcher m)
   	
   	{
        for(int j=0; j<= m.groupCount(); j++)
        	{
        	System.out.printf("Debug: group(%d) = '%s'.\n",  j, 
        			m.group(j) != null ? m.group(j) : "null");
        	}
    }
   	
    /* groupToFieldMap:
     * Args:
     *   negate: is set in cases like pos!="val val..." or pos<>='val val ..'.
     * - builds a list of operands out of values[].
     * - accepts >= 1 value(s) in values[].
     * Returns 1 termGroup if > 1 values, or 1 term if there is 1 single value in values[].
     * 09.06.26/FB
     */
    
    private Map<String, Object> groupToFieldMap(Map<String, Object> termMap, String[] values, boolean negate)
    
    {
    	final String func = "Debug: groupToFieldMap";
    	
    	if( values == null ) // || values.length == 1 )
    		return termMap;
    	
    	String[]
    		subValues = null;
    	Map<String, Object> 
			termGroup = KoralObjectGenerator.makeTermGroup(KoralTermGroupRelation.AND);
    	ArrayList<Object> 
    		terms = new ArrayList<Object>();
    	boolean
    		isNeg, isNeg1, isNeg2;
    	
    	// clear 'type:regex' and 'value' (if set):
    	termMap.remove("type");	 // must be set individually.
    	termMap.remove("value"); // must be set individually.
    	
    	for(String term : values)
    		{
    		if( DEBUG )
    			System.out.printf("%s: term='%s'.\n", func, term);
    		
    		Map<String, Object>
    			termMap2 = new HashMap<>(termMap);
    		
    		isNeg1 = isNeg2 = false;
    		
    		if( isRegExpr(term) )
    			termMap2.put("type", "type:regex");
    			
    		subValues = term.trim().split(":");
    		if( subValues.length > 1 )
    			{
    			isNeg1 = addNegVal(termMap2, "key",   subValues[0]);    			
    			isNeg2 = addNegVal(termMap2, "value", subValues[1]);
    			}
    		else
    			isNeg1 = addNegVal(termMap2, "key",   term);    			
			
    		isNeg = negate;
    		if( isNeg1 || isNeg2 )
    			isNeg = !negate;
    		
    		if( isNeg )
    			termMap2.put("match", KoralMatchOperator.NOT_EQUALS.toString());
    		else
    			termMap2.put("match", KoralMatchOperator.EQUALS.toString());
    		 
    		if( DEBUG )
    			System.out.printf("%s: termMap2='%s'.\n", func, termMap2.toString());
    		
    		terms.add(termMap2);
        	}

    	if( values.length == 1 )
    		{
    		// return a single term.
    		return (Map<String, Object>) terms.get(0);
    		}
    	else
    		{
    		// return a group:
    		termGroup.put("operands", terms);
        	
        	return termGroup;	
    		}
    }
    
    /* termToFieldMap(term, type)
     * type: opMORPH or opELEM
     * term: may also contain a list of values:
     *       e.g. foundry/layer="a b c...", foundry/layer='a b c...'.
     * shall replace termToFieldMap(term).
     * - wordOrRegex: also accept regexpr when no '...' nor "..." are used.
     * - in an older version, layer="a b c" the values inside "..." were automatically
     *   interpreted as reg. expr.
     * notes:
     * - pos=numb:sg -> group6 = numb & group8 = sg
     * - pos='numb:sg' or pos="numb:sg" -> group6 = numb:sg & group8 = null.
     * - this is due to the reg. expr of pattern p.
     * 10.06.26/FB
     */
 
    private Map<String, Object> termToFieldMap (String term, int type)
    {
        // regex group #2 is foundry, #4 layer, #5 operator,
        // #6 key, #8 value
    	final String func = "Debug: termToFieldMap";
    	
        String wordOrRegex = "-?[\\w][\\w|\\*|\\+|\\.|\\?|\\\\|(|)|\\|]*|\".+?\"|'.+?'";
        // TODO: Should be initialized globally
        Pattern p = Pattern.compile("((\\w+)/)?((\\w*)(!?=|<>))?(" + wordOrRegex
                + ")(:(" + wordOrRegex + "))?");
        Matcher m;
        boolean negate = false;
        
        m = p.matcher(term);
        
        if (!m.matches()) {
            addError(StatusCodes.INCOMPATIBLE_OPERATOR_AND_OPERAND,
                    "Something went wrong parsing the argument in MORPH() or #ELEM().");
            requestMap.put("query", new HashMap<String, Object>());
            System.out.printf("%s: term='%s' m.matches() = null!\n", func, term);
            return null;
        };

        Map<String, Object> fieldMap = KoralObjectGenerator.makeTerm();

        if( showRegGroups )
        	printRegGroups(m);
        	
        if (m.group(2) != null) fieldMap.put("foundry", m.group(2));
        if (m.group(4) != null) 
        	fieldMap.put("layer", m.group(4));
        else
	        { // MORPH(NOU) -> add default layer = LAYER_POS.
	        if( type == opMORPH )
	        	fieldMap.put("layer", LAYER_POS);
	        }
        
        if (m.group(5) != null) {
            if ("!=".equals(m.group(5)) || "<>".equals(m.group(5))) 
            	negate = !negate;
        }
        
        // group 6: key or -key or 'key' or 'key key...' or "key" or "key key...":
       
        String[] 
        	values = null;
        
        if (m.group(6) != null) 
	     	{
	        String key = m.group(6);
	        boolean isNeg1 = false, isNeg2 = false;
	        
	        if ( (key.startsWith("\"") && key.endsWith("\"")) || (key.startsWith("'") && key.endsWith("'")) ) 
	        	{
	        	// a list of 1 or more expressions/values is handled by groupToFieldMap().
	        	key 	 = key.substring(1, key.length() - 1);
	        	// is this a list with one or more values?
		        values   = key.trim().split("\\s+"); // split by sequences of white spaces.
		
		        if( DEBUG )
		        	System.out.printf("%s: group6 key '%s' : # of values = %d.\n", func, key, values.length);
		        
		        return groupToFieldMap(fieldMap, values, negate);  
		        }
	        
        	// treat here an unquoted single expression/value: Either a single key (e.g. pos=key), or
        	// a key:value pair (e.g. pos=key:value). In the later case 'value' will be handled with group 8.
        
	        if( key.startsWith("-") )
	        	{
	        	key    = key.substring(1); // remove '-'.
	        	negate = true;
	        	}
	        if( isRegExpr(key) ) 
	        	fieldMap.put("type", "type:regex");
		        
	        isNeg1 = addNegVal(fieldMap, "key", key);
	        
	    	if( isNeg1 )
    			negate = !negate;
    		}
	        
        // m.group(8) is checked if it is a single value, not written between '..' nor "..":
        if (m.group(8) != null) 
        	{
        	String value = m.group(8);
        	boolean isNeg = false;
        	
            if( DEBUG )
            	System.out.printf("%s: group 8 = '%s'.\n", func, value);
            
	        if ( (value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'")) ) 
	        	value = value.substring(1, value.length()-1); 

	        isNeg = addNegVal(fieldMap, "value", value);
            
	        if( isNeg )
	        	negate = !negate;
	        
	        if ( isRegExpr(value)) 
	        	fieldMap.put("type", "type:regex");
            }

        // negate field (see above)
        if (negate) 
            fieldMap.put("match", KoralMatchOperator.NOT_EQUALS.toString());
        else
            fieldMap.put("match", KoralMatchOperator.EQUALS.toString());

        return fieldMap;        			
    };

    private Tree parseCosmasQuery (String query) {
        
    	// deactivate rewrite...() as it blindly interacts with every part of a query
    	// which contains e.g. temp:past like in MORPH(temp:past) - 02.06.26/FB
    	// query = rewritePositionQuery(query);
    	
        Tree tree = null;
        Antlr3DescriptiveErrorListener errorListener =
                new Antlr3DescriptiveErrorListener(query);
        try {
            ANTLRStringStream ss = new ANTLRStringStream(query);
            c2psLexer lex = new c2psLexer(ss);
            org.antlr.runtime.CommonTokenStream tokens =
                    new org.antlr.runtime.CommonTokenStream(lex); // v3
            
            // Use custom error reporters
            lex.setErrorReporter(errorListener);
            
            if( DEBUG && bShowTokens )
	        	TokenUtils.printLexerTokens(tokens, "parseCosmasQuery");
	        	
            parser = new c2psParser(tokens);
           
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
        	if( treestring.contains("<unexpected: [") )
				{
	         	Pattern p = Pattern.compile("\\[@\\d+,(\\d+):(\\d+)=");
	         	Matcher m = p.matcher(treestring);
	
	         	if (m.find()) 
	         		{
	         	    int start = Integer.parseInt(m.group(1));
	         	    int end   = Integer.parseInt(m.group(2));
	
	         	    if( DEBUG )
	         	    	System.out.printf("parseCosmasQuery: unexpected input [%d-%d]!\n", start, end);
	         	    addError(StatusCodes.MALFORMED_QUERY,
	                         "unexpected input at position " + start + "-" + end + "!");
	 				}
	         	else
	         		addError(errorListener.generateFullErrorMsg());
				}
        	else
        		addError(errorListener.generateFullErrorMsg());
            return null;
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
