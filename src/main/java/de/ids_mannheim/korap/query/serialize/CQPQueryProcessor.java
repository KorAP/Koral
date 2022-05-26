package de.ids_mannheim.korap.query.serialize;

import de.ids_mannheim.korap.query.object.ClassRefOp;
import de.ids_mannheim.korap.query.object.KoralFrame;
import de.ids_mannheim.korap.query.object.KoralMatchOperator;
import de.ids_mannheim.korap.query.object.KoralOperation;
import de.ids_mannheim.korap.query.object.KoralTermGroupRelation;
import de.ids_mannheim.korap.query.parse.cqp.CQPLexer;
import de.ids_mannheim.korap.query.parse.cqp.CQPParser;
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
 * Map representation of CQP syntax tree as returned by
 * ANTLR.
 * Most centrally, this class maintains a set of nested maps and
 * lists which represent the JSON tree, which is built by the JSON
 * serialiser on basis of the {@link #requestMap} at the root of
 * the tree. <br/>
 * The class further maintains a set of stacks which effectively
 * keep track of which objects to embed in which containing
 * objects.
 * 
 * This class expects the CQP ANTLR grammar shipped with Koral
 * v0.3.0.
 * 
 * @author Joachim Bingel (bingel@ids-mannheim.de)
 * @author Eliza Margaretha (margaretha@ids-mannheim.de)
 * @author Nils Diewald (diewald@ids-mannheim.de)
 * @author Elena Irimia (modified from PQProcessor; elena@racai.ro)
 * @version 0.3.0
 * @since 0.1.0
 */
public class CQPQueryProcessor extends Antlr4AbstractQueryProcessor {

    private static final boolean DEBUG = true;
    private static Logger log = LoggerFactory
            .getLogger(CQPQueryProcessor.class);
    private int classCounter = 1;

    Map<ParseTree, Integer> classWrapRegistry = new HashMap<ParseTree, Integer>();


    /**
     * Constructor
     * 
     * @param query
     *            The syntax tree as returned by ANTLR
     */
    public CQPQueryProcessor (String query) {
        KoralObjectGenerator.setQueryProcessor(this);
        process(query);
        if (DEBUG) { 
            log.debug(">>> " + requestMap.get("query") + " <<<");
        }
    }


    @Override
    public void process (String query) {

        ParseTree tree;
        tree = parseCQPQuery(query);  
        // fixme: not required!?
       
        super.parser = this.parser;
        if (DEBUG) {
            log.debug("Processing CQP query: " + query);
        }
        if (tree != null) {
            if (DEBUG){
                log.debug("ANTLR parse tree: " + tree.toStringTree(parser));
            }

            processNode(tree, true);
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
    private void processNode (ParseTree node, boolean putvisited) {
        // Top-down processing
        if (visited.contains(node))  /*** if the node was visited in previous steps ***/
            return;
        else
        	/** if skipvisited is false, we need the node to be visited again in future operations;  ***/
        	/** so we don't put it in visited! ***/ 
        	if (putvisited) visited.add(node); 

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

        if (nodeCat.equals("sequence")) {
            processSequence(node);
        }
        if (nodeCat.equals("meetunion")) {
            
        	/*** for the outer meet, of whatever type, putvisited is true and the node is put in visited****/
        	processMeetUnion(node, putvisited);
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
        if (nodeCat.equals("tokenstruct")) {
            processTokenStruct(node);
        }
        
        if (nodeCat.equals("alignment")) {
            processAlignment(node);
        }

        if ((nodeCat.equals("span") || nodeCat.equals("closingspan")) && getNodeCat(node.getChild(0))!="skey") {
        	 String nCat0 = getNodeCat(node.getChild(0));
        	 if (nCat0.equals("skey"))
        	        {
        	        	// for unlayered spans: \region[np], lbound(np), etc!
        		        processSpan(node);
        	        }
        	 else
        	 {
        		 // for struct like <s> ... </s>; check if the span is a closing one;
        		 spanNodeCats.push(nodeCat);
        		 String nCat = getNodeCat(node.getChild(1));
        		 int nspan = spanNodeCats.size();
        		 if (!nCat.equals("/"))
        		 { 
        			 //check if two operators situation (lbound and rbound); probably we don't need this!!!
        			/* if (!objectStack.isEmpty())
        			 {
        				 Map <String, Object> top = objectStack.getFirst();
        				 Object ops =  top.get("operands");
        				 if((ops!=null)&&(!ops.toString().contains("koral:span")))		
        				 processSpan(node);
        			 }
        			 else */processSpan(node);
        				 
        		 }
        		 else
        		 {
        			 if (nspan==1) // for situations like: sequence </s>, endsWith
        			 {
        				 processSpan(node);
        			 }
        		 }
        	 }
        }
      
        if (nodeCat.equals("disjunction")) {
            processDisjunction(node);
        }


        if (nodeCat.equals("relation")) {
            processRelation(node);
        }

        if (nodeCat.equals("spanclass")) {
            processSpanClass(node);
        }

        if (nodeCat.equals("matching")) {
            processMatching(node);
        }

        if (nodeCat.equals("submatch")) {
            processSubMatch(node);
        }

        if (nodeCat.equals("queryref")) {
            processQueryRef(node);
        }

        if (nodeCat.equals("meta")) {
            processMeta(node);
        }
       
        if (nodeCat.equals("sstruct")){
        	
        	processPosition(node);
        }
           
        if (nodeCat.equals("qstruct")){
        	
        	processPosition(node);
        }
        if (nodeCat.equals("position")){
        	
        	processPosition(node);
        }
        	
        	
        if (nodeCat.equals("within")
                && !getNodeCat(node.getParent()).equals("position")) { // why this condition??
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
        		ParseTree child = node.getChild(i);
                
        		if (!nodeCat.equals("meetunion") )
                {
        			  processNode(child, putvisited); // we propagate putvisited down in the tree;
                }
        	    else
        	    {
        	    	/*** if the node is meetunion, check if it has a span meet parent; ****/
        	    	/*** if the parent is span meet, we do not process child nodes; they were processed with putvisited false? ??? ***/
        	    	if(!checkIfParentIsSpanMeet(node))
        	    	{
        	    		processNode(child, putvisited);
        	    	}
        	    }
         
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
       // for segments following closingspan or preceding span
        if (getNodeCat(node.getParent().getChild(0).getChild(0)).equals("closingspan") && node.equals(node.getParent().getChild(1)))
        {
            Map<String, Object> classGroup = KoralObjectGenerator
        			                .makeSpanClass(1);
        			        
        			       addHighlightClass(1);
        			       putIntoSuperObject(classGroup);
        			       objectStack.push(classGroup);
        			       stackedObjects++;
        }
       int nchild = node.getParent().getChildCount();
        if (getNodeCat(node.getParent().getChild(nchild-1).getChild(0)).equals("span") && node.equals(node.getParent().getChild(nchild-2)))
        {
            Map<String, Object> classGroup = KoralObjectGenerator
        			                .makeSpanClass(1);
        			        
        			       addHighlightClass(1);
        			       putIntoSuperObject(classGroup);
        			       objectStack.push(classGroup);
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
        if (getNodeCat(node.getChild(0).getChild(0)).equals("closingspan") || getNodeCat(node.getChild(node.getChildCount()-1).getChild(0)).equals("span"))
        {
            
            ArrayList<Integer> classRefs = new ArrayList<Integer>();
           
            classRefs.add(1); 
            Map<String, Object> referenceGroup = KoralObjectGenerator.makeReference(classRefs);
            // Default is focus(), if deviating catch here
    
           referenceGroup.put("operation", "operation:focus");
           ArrayList<Object> referenceOperands = new ArrayList<Object>();
          referenceGroup.put("operands", referenceOperands);
          // Step II: decide where to put the group
          putIntoSuperObject(referenceGroup);
          objectStack.push(referenceGroup);
          stackedObjects++;
         // visited.add(node.getChild(0));
        
         }
        Map<String, Object> sequence = KoralObjectGenerator
                .makeGroup(KoralOperation.SEQUENCE);

        putIntoSuperObject(sequence);
        objectStack.push(sequence);
        stackedObjects++;
    }
    
    private boolean checkIfRecurrsiveMeet(ParseTree node) {
        boolean recurrence=false;
        // first segment and second segment are parsed differently in the antlr grammar
    	ParseTree firstsegment;
    	ParseTree secondsegment;	
    	List<ParseTree> segments = getChildrenWithChildren(node); // the others are terminal nodes in the MU syntax
        firstsegment = segments.get(0);
        secondsegment = segments.get(1);
        List<ParseTree> desc1=  getDescendantsWithCat(firstsegment, "meetunion");
        if (getNodeCat(firstsegment).equals("meetunion")) desc1.add(firstsegment);
        List<ParseTree> desc2=  getDescendantsWithCat(secondsegment, "meetunion");
        if (getNodeCat(secondsegment).equals("meetunion")) desc2.add(secondsegment);
        if((!desc1.isEmpty())||(!desc2.isEmpty()))
            {
                recurrence = true;
            }
        return recurrence;
    }


    private boolean checkIfParentIsSpanMeet (ParseTree node) {

    	boolean parentspanmeet=false;
    	// check if last child of the parent meet is integer, otherwise the parent meet is a span meet
        if (getNodeCat(node.getParent().getParent().getParent()).equals("meetunion") )
                {
                    
                    ParseTree testnode = node.getParent().getParent().getParent();
                    int lastchild = testnode.getChildCount()-1;
                    
                    try {
                    	Integer.parseInt(testnode.getChild(lastchild).getText());
                    
                        }
                    catch (NumberFormatException nfe)
                    	{ 
                        	parentspanmeet=true;
                    	}     
                }
                
                if (getNodeCat(node.getParent()).equals("meetunion") )
                {
                
                    ParseTree testnode = node.getParent();
                    int lastchild = testnode.getChildCount()-1;
                    
                    try {
                    Integer.parseInt(testnode.getChild(lastchild).getText());
                    
                            }
                    catch (NumberFormatException nfe)
                    { 
                        parentspanmeet=true;
                    }
                    
                }
            return parentspanmeet;    
    }
    private Integer computeSegmentDistance(ParseTree node) {
    	
    	int distance=0;
    	//outer offsets; they are equal
    	int ooffs1=0;
    	//first inner offsets; if there is no 1st inner meet, the offsets remain 0
    	int foffs1=0;
    	//second inner offsets;  if there is no 2nd inner meet, the offsets remain 0
    	int soffs1=0;
    	ooffs1= Integer.parseInt(node.getChild(node.getChildCount()-2).getText());
    
    	
       	ParseTree firstsegment;
    	ParseTree secondsegment;	
    	List<ParseTree> segments = getChildrenWithChildren(node); // the others are terminal nodes in the MU syntax
        firstsegment = segments.get(0);
        secondsegment = segments.get(1);
        List<ParseTree> desc1=  getDescendantsWithCat(firstsegment, "meetunion");
        if (getNodeCat(firstsegment).equals("meetunion")) desc1.add(firstsegment);
        List<ParseTree> desc2=  getDescendantsWithCat(secondsegment, "meetunion");
        if (getNodeCat(secondsegment).equals("meetunion")) desc2.add(secondsegment);
        if((!desc1.isEmpty()))
            {
        		// we don't need both offs because they are equal!
                foffs1= Integer.parseInt(desc1.get(0).getChild(desc1.get(0).getChildCount()-2).getText());
            }
        
        if((!desc2.isEmpty()))
        	{
        		// we don't need both offs because they are equal!
                soffs1= Integer.parseInt(desc2.get(0).getChild(desc2.get(0).getChildCount()-2).getText());
        	}
        if ((foffs1>0&&soffs1>0&&ooffs1>0) || (foffs1>0&&soffs1>0&&ooffs1<0) || (foffs1>0&&soffs1==0&&ooffs1>0) || (foffs1<0&&soffs1==0&&ooffs1<0))
        	{ 
        		if (Math.abs(ooffs1)-Math.abs(foffs1)-1>=0)
        			{
        				distance = Math.abs(ooffs1)-Math.abs(foffs1)-1;
        			}
        		else
        		{
        			System.out.println("Incompatible offset values! The differene between the absolute values of the outer offsets and the first inner offsets should not be smaller than 1!");
        			
        		}
        	}
        else
        {
        	if ((foffs1<0&&soffs1>0&&ooffs1>0) || (foffs1<0&&soffs1==0&&ooffs1>0) || (foffs1>0&&soffs1==0&&ooffs1<0) || (foffs1==0&&soffs1>0&&ooffs1>0))
         		{ 
        	 		if (Math.abs(ooffs1)-1>=0)
        	 			{
        	 				distance = Math.abs(ooffs1)-1;
        	 			}
        	 		else
        	 		{
        	 			System.out.println("Incompatible offset values! The absolute values of the outer offsets should not be smaller than 1!"); // this is also checked in the processMeetUnion function, when comparing offsets with zero
        	 		}
         		}
        	else
        	{
        		if (foffs1<0&&soffs1>0&&ooffs1<0)
        			{ 
        				if (Math.abs(ooffs1)-Math.abs(foffs1)-Math.abs(soffs1)-1>=0)
        				{
        					distance = Math.abs(ooffs1)-Math.abs(foffs1)-Math.abs(soffs1)-1;
        				}
        				else
        				{
        					System.out.println("Incompatible offset values! The differene between the absolute values of the outer offsets and the sum of the first inner and the second inner offsets should not be smaller than 1!");
        				}
        			}
        		else
        		{
        			if (foffs1==0&&soffs1>0&&ooffs1<0)
        				{ 
        					if (Math.abs(ooffs1)-Math.abs(soffs1)-1>=0)
        						{
        							distance = Math.abs(ooffs1)-Math.abs(soffs1)-1;
        						}
        					else
        					{
        						System.out.println("Incompatible offset values! The differene between the absolute values of the outer offsets and the second inner offsets should not be smaller than 1!");
        					}
        				}
        			else
        			{
        				System.out.println("Incompatible offset values! See the cqp tutorial!");
        			}
        		}
        	}
        }
    	return distance;
     }

   
    
    @SuppressWarnings("unchecked")
	private void processMeetUnion (ParseTree node, boolean putvisited) {
    	
	
	ParseTree firstsegment;
	ParseTree secondsegment;	
	List<ParseTree> segments = getChildrenWithChildren(node); // the others are terminal nodes in the MU syntax
    firstsegment = segments.get(0);
    secondsegment = segments.get(1);
	
    try
    { 
    	/************** this processes meet with offsets ************/
    	
    	Integer[] window = new Integer[] {0,0};
    	int offposition1= node.getChildCount()-2;
    	int offposition2= node.getChildCount()-1;
    	window = new Integer[] { Integer.parseInt(node.getChild(offposition1).getText()), Integer.parseInt(node.getChild(offposition2).getText()) }; // if fails, it is a meet span
    	if ((window[0]==0)||(window[1]==0))
    	{
    		//addWarning("The MeetUnion offsets cannot be 0!!");
    		addError(StatusCodes.MALFORMED_QUERY,"The MeetUnion offsets cannot be 0!!");
    		
    		return;
    	}
    	else
    	{
    		if (window[0]>window[1])
    		{
    			addError(StatusCodes.MALFORMED_QUERY, "Left meetunion offset is bigger than the right one!");
    			return;
    		}
    		else
    		{
    			/****  correct offsets ****/
    			/******* check if this is a recurrent meetunion *******/ 
    	   	    boolean recurrence = false;    
                recurrence = checkIfRecurrsiveMeet(node); // check if the actual meet is a recursive one
		
    			// the numbers in meet function behave like window offsets, not like the quantifiers in repetitions; 
    			//they are translated into quantifiers here below!
   
    			if(window[0].equals(window[1])) 
        		{
    				/***** equal offsets; the distance is exactly window[1]=window[2] ****/
                    Integer segmentDistance=0; /*** the distance between the first inner meet seq and the second inner meet seq; ****/
                    
                    /**** create the meet sequence ****/
            		Map<String, Object> sequence = KoralObjectGenerator.makeGroup(KoralOperation.SEQUENCE);
			    	putIntoSuperObject(sequence);
			    	objectStack.push(sequence);
			    	stackedObjects++;
					
                    if (window[0]>0)
        				{
        				  /** positive equal offsets; segmentDistance is exactly window[0]=window[1] and the order of the segments is kept ***/
        					
        			    	/*** first meet segment *****/
        			    	 // propagate the putvisited to the segments
        			    	processNode(firstsegment, putvisited);

        			    	if (recurrence==false) 
        			    	{
        			    		/*** non-recurrence ***/
        			    		/***** if window=[1, 1] no distance is necessary; ****/
        			    		if (window[0]>1) 
        			    		{
        			    			ArrayList<Object> distances = new ArrayList<Object>();
        			    	        sequence.put("distances", distances);
        			    	        ((ArrayList<Object>) sequence.get("distances")).add( KoralObjectGenerator.makeDistance("w", window[0]-1 , window[1]-1));
        			    			
        			    			
        			    		}
        			    	}
        			    	else
        			    	{
        			    		/*** recurrence ****/
        			    		segmentDistance = computeSegmentDistance(node);
            			    	//insert empty token repetition 
            					if (segmentDistance>0)
            					{
            						ArrayList<Object> distances = new ArrayList<Object>();
        			    	        sequence.put("distances", distances);
        			    	        ((ArrayList<Object>) sequence.get("distances")).add( KoralObjectGenerator.makeDistance("w", segmentDistance, segmentDistance));
            					}
            				}
        					
        			    	/*** second meet segment; put again into stack for recurrence situations ****/
        			    	if (recurrence == true) objectStack.push(sequence);
        			         //propagate the putvisited to the segments
        			    	processNode(secondsegment, putvisited);
        				}
        				else
        				{
        					/**** negative equal offsets; the distance is exactly  window[0]=window[1] and the order of the segments is changed ***/
        					/*** second meet segment ***/
        				    processNode(secondsegment, putvisited);
        					   
        					   if (recurrence==false) 
                               {
        							if (window[0]<-1)
        							{
        								ArrayList<Object> distances = new ArrayList<Object>();
            			    	        sequence.put("distances", distances);
            			    	        ((ArrayList<Object>) sequence.get("distances")).add( KoralObjectGenerator.makeDistance("w", (-window[1])-1 , (-window[0])-1));
        							}
        							//if window=[-1, -1], we just need to change the order of processing the segments
        						}
        						else
        						{	
        							segmentDistance = computeSegmentDistance(node);
        							if (segmentDistance>0)
        							{
        								ArrayList<Object> distances = new ArrayList<Object>();
            			    	        sequence.put("distances", distances);
            			    	        ((ArrayList<Object>) sequence.get("distances")).add( KoralObjectGenerator.makeDistance("w", segmentDistance, segmentDistance));
        							}
        						}
            					
        					   
        					   /*** first meet segment; put again into stack for recurrence situations ****/
        						if (recurrence==true) objectStack.push(sequence);
        						processNode(firstsegment, putvisited);
        					
        				}
        		}
        		else
        		{
        			/*** the offsets are not equal; we don't use putvisited here, because we did not implement recursive meet for this situation ***/
        			if (recurrence==true)
                    {
                        addError(StatusCodes.MALFORMED_QUERY,"We did not implement recursive meet with different offsets!!");
                        return;
                    }
                    else
                    {
                        if ((window[0]<0) && (window[1]>0))
                        {

                            /***  implementing disjunction of sequences ***/
        				    
                     //   	disj= true;
        				    Map<String, Object> disjunction = KoralObjectGenerator.makeGroup(KoralOperation.DISJUNCTION);
        				    putIntoSuperObject(disjunction);
        				    objectStack.push(disjunction);
        				    stackedObjects++;
        		
        				    /**** first disjunct, keep order of segments, distance is []{0,window[1]} ****/
        				    
        				    Map<String, Object> firstsequence = KoralObjectGenerator.makeGroup(KoralOperation.SEQUENCE);
        				    putIntoSuperObject(firstsequence);
        				    objectStack.push(firstsequence);
        				    stackedObjects++;
        				    processNode(firstsegment, false);
        				    if(window[1]>1)
        				    {
        				    	ArrayList<Object> distances = new ArrayList<Object>();
    			    	        firstsequence.put("distances", distances);
    			    	        ((ArrayList<Object>) firstsequence.get("distances")).add( KoralObjectGenerator.makeDistance("w", 0, window[1]-1));
        				    }
        				    processNode(secondsegment, false);
				
        				    /*** second disjunct, change order of segments, distance is []{0,-window[0]} ***/
        				    
        				    Map<String, Object> secondsequence = KoralObjectGenerator.makeGroup(KoralOperation.SEQUENCE);
        				    objectStack.push(secondsequence);
        				    stackedObjects++;
        				    ((ArrayList<Object>) disjunction.get("operands")).add(secondsequence);
        				    processNode(secondsegment, true);
        				    if(window[0]<-1)
        				    {
        				    	ArrayList<Object> distances = new ArrayList<Object>();
    			    	        secondsequence.put("distances", distances);
    			    	        ((ArrayList<Object>) secondsequence.get("distances")).add( KoralObjectGenerator.makeDistance("w", 0, (-window[0])-1));
        				    }
        				    processNode(firstsegment, true);
				        }
                        
        		        else
        		        {
        		        	/**** offsets are either both > 0 or both < 0 ****/
        		        	Map<String, Object> sequence = KoralObjectGenerator.makeGroup(KoralOperation.SEQUENCE);
                            putIntoSuperObject(sequence);
                            objectStack.push(sequence);
                            stackedObjects++;
                            
        		        	if (window[0]>0 && window[1]>0)
        				    {
        					   /*** both offsets are positive ***/
                               /**** first meet segment ****/
                               			
                               processNode(firstsegment, true);
                           	   ArrayList<Object> distances = new ArrayList<Object>();
			    	           sequence.put("distances", distances);
			    	           ((ArrayList<Object>) sequence.get("distances")).add( KoralObjectGenerator.makeDistance("w", window[0]-1, window[1]-1));
            					
            					
                               processNode(secondsegment, true);
        				    
        				    }
        		        	else
        		        	{
                            	/*** both offsets are negative; change order of segments ****/
    				         	/**** second meet segment ****/
    				         		
                            	processNode(secondsegment, true);
    				            
                            	ArrayList<Object> distances = new ArrayList<Object>();
    			    	        sequence.put("distances", distances);
    			    	        ((ArrayList<Object>) sequence.get("distances")).add( KoralObjectGenerator.makeDistance("w", (-window[1])-1,(-window[0])-1));

    				            /**** first meet segment ******/
    				            	
    				            processNode(firstsegment, true);
        				    }
                        }
                    }
                }
    	    }
    	}
    }
    
    catch (NumberFormatException nfe)
    {
    	
        /**** process meet  with span ***/
    	
    	ParseTree firstsequence = firstsegment;
    	ParseTree secondsequence = secondsegment;
    	Map<String, Object> position = parseFrame(node.getChild(node.getChildCount()-1));
        putIntoSuperObject(position);
        objectStack.push(position);
        stackedObjects++; 
        processNode(node.getChild(node.getChildCount()-1), true);		
        stackedObjects++;
  //      objectStack.push(position); 
        
   
		Map<String, Object> spannedsequence = KoralObjectGenerator.makeGroup(KoralOperation.SEQUENCE);
		putIntoSuperObject(spannedsequence);
		objectStack.push(spannedsequence);
		stackedObjects++;		
		processNode(firstsequence, true); 
		objectStack.push(spannedsequence); // for recurrence; the firstsequence was first in stack before;
		processNode(secondsequence, true);  
		/** distance between first sequence and second sequence can be anything in meet span**/
		ArrayList<Object> distances = new ArrayList<Object>();
        spannedsequence.put("distances", distances);
        ((ArrayList<Object>) spannedsequence.get("distances")).add( KoralObjectGenerator.makeDistance("w", 0, null));
		spannedsequence.put("inOrder", false);		
	
    }
  
}

	@SuppressWarnings("unchecked")
    /**
     * empty tokens at beginning/end of sequence
     * 
     * @param node
     */
    private void processEmptyTokenSequence (ParseTree node) {
	    
		Map<String, Object> lastobj= objectStack.getLast();
		
				Integer[] minmax = parseEmptySegments(node);
				// object will be either a repetition group or a single empty token
			
				Map<String, Object> object;
				Map<String, Object> emptyToken = KoralObjectGenerator.makeToken();
				if (minmax[0] != 1 || minmax[1] == null || minmax[1] != 1) 
				{
					object = KoralObjectGenerator.makeRepetition(minmax[0], minmax[1]);
					((ArrayList<Object>) object.get("operands")).add(emptyToken);
				}
				else 
				{
					object = emptyToken;
				}
				if (lastobj.containsKey("frames")) // check if frames:isAround and ignore the emptyTokens if the case, for the <s> []* token []* </s> situations;
				{
                   // String category= getNodeCat(node.getParent());
                    if (!getNodeCat(node.getParent()).equals("emptyTokenSequenceAround"))
                    {
                        putIntoSuperObject(object);
						objectStack.push(object);
						stackedObjects++;
                    }
                  
				}
				else
				{
					putIntoSuperObject(object);
					objectStack.push(object);
					stackedObjects++;
				}
				
			}
		


    private void processQueryRef (ParseTree node) {

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
       // boolean isRegex = false;
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
               //  isRegex = true;
                term.put("type", "type:regex");

                // fixme: use stream with offset to get text!
                // TokenStream stream = parser.getTokenStream();
                // key = stream.getText(node.getChild(0).getSourceInterval());
                String first = key.substring(0, 1);
                String last = key.substring(key.length()-1, key.length());
                key = key.substring(1, key.length() - 1);
                //treat the doubleqoutes and singlequoutes inside regex!
                if (first.equals("\"") && last.equals("\""))
                {
                       key =  key.replaceAll("\"\"", "\"");

                }
                if (first.equals("'") && last.equals("'"))
                {
                       key =  key.replaceAll("''", "'");

                }
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
                if (flag.contains("c") || flag.contains("C"))
                    flags.add("flags:caseInsensitive");
                if (flag.contains("d") || flag.contains("D"))
                    flags.add("flags:diacriticsInsensitive");
         
                if (flag.contains("l")|| flag.contains("L"))
                { 
                		ParseTree keyNode = node.getChild(0);

        				// Get stream from hidden channel
        				TokenStream stream = parser.getTokenStream();
        				key = stream.getText(keyNode.getChild(0).getSourceInterval());
        				key = key.substring(1, key.length()-1).replaceAll("\\\\\\\\","\\\\").replaceAll("\\\\'", "'");
        				//override key and type:string
        				term.put("key", key);
        				term.put("type", "type:string");
                }
                if (!flags.isEmpty()) {
                    term.put("flags", flags);
                }
            }
            token.put("wrap", term);
        }
        else {
            // child is 'term' or 'termGroup' -> process in extra method
            //check if the token has a position condition
        	
        	if  (hasChild(node.getChild(termOrTermGroupChildId), "position"))
        	{
        		ParseTree positionNode=  getFirstChildWithCat(node.getChild(termOrTermGroupChildId), "position"); 
        		processPosition(positionNode);
        	}
        	
        	Map<String, Object> termOrTermGroup = parseTermOrTermGroup(
                    node.getChild(termOrTermGroupChildId), negated);
            token.put("wrap", termOrTermGroup);
        }
        putIntoSuperObject(token);
        visited.addAll(getChildren(node));
    }
    
    
    private void processTokenStruct (ParseTree node) {
    	// differs from processToken because it doesn't require/have [] around the token
        Map<String, Object> token = KoralObjectGenerator.makeToken();
        // handle negation
        List<ParseTree> negations = getChildrenWithCat(node, "!");
        int termOrTermGroupChildId = 0;
        boolean negated = false;
      //  boolean isRegex = false;
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
            //    isRegex = true;
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
                if (flag.contains("c") || flag.contains("C"))
                    flags.add("flags:caseInsensitive");
                if (flag.contains("d") || flag.contains("D"))
                    flags.add("flags:diacriticsInsensitive");
                if (flag.contains("l")|| flag.contains("L"))
                { 
                		ParseTree keyNode = node.getChild(0);

        				// Get stream from hidden channel
        				TokenStream stream = parser.getTokenStream();
        				key = stream.getText(keyNode.getChild(0).getSourceInterval());
        				key = key.substring(1, key.length()-1).replaceAll("\\\\\\\\","\\\\").replaceAll("\\\\'", "'");
        				//override key and type:string
        				term.put("key", key);
        				term.put("type", "type:string");
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
        ParseTree keyNode = getFirstChildWithCat(node, "skey");
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
        
        // modified for the new span with WORD insted of key
        String key="";
        if (keyNode!=null)
        	{
        	 // check if key is regular expression
            if (hasChild(keyNode, "regex")) {
                // remove leading/trailing double quotes
                key = key.substring(1, key.length() - 1);
                wrappedTerm.put("type", "type:regex");
            }
            else key = keyNode.getText();
            }
        else key = node.getChild(node.getChildCount()-2).getText();
       
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
        if (hasChild(node, "span")) // for lboud and rbound, when span is child of position;
        {
        	ParseTree spanchildnode = getFirstChildWithCat (node, "span");
        	processSpan(spanchildnode);
        	
        	objectStack.pop();
        	stackedObjects=stackedObjects-2;
        }
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


    private void processSpanClass (ParseTree node) {
        // Step I: get info
        int classId = 1;

        if (getNodeCat(node.getChild(0)).equals("@1")) {
            
                classId = 2;
            }
        // check meetunion
        if (getNodeCat(node.getParent().getParent()).equals("meetunion"))
        		{
        		  // we want to maintain only the class of the focus of the leftmost  meet!		
        		if (!hasDescendantWithCat(node, "meetunion") && (!getNodeCat(node.getParent().getParent().getParent()).equals("meetunion")))
        				{
        				   Map<String, Object> classGroup = KoralObjectGenerator
        			                .makeSpanClass(classId);
        			        
        			       if (classId==1) addHighlightClass(classId);
        			       putIntoSuperObject(classGroup);
        			       objectStack.push(classGroup);
        			       stackedObjects++;
        				}
        		}
        else
        {
        Map<String, Object> classGroup = KoralObjectGenerator
                .makeSpanClass(classId);
        
        addHighlightClass(classId);
        putIntoSuperObject(classGroup);
        objectStack.push(classGroup);
        stackedObjects++;
        }

    }

// verifica si asta!!!
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
        if (type.equals("MU"))
            referenceGroup.put("operation", "operation:focus");
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


    private void processSubMatch (ParseTree node) {
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
        addWarning("You used the 'meta' keyword in a CQP query. This"
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
                // mai avem exprimare a claselor asa??
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
        if (getNodeCat((node).getParent()).equals("meetunion"))
        		{
        			frames.add(KoralFrame.IS_AROUND);
        		}
       if (operator.contains("startswith"))
       {
    	   frames.add(KoralFrame.STARTS_WITH);
           frames.add(KoralFrame.MATCHES);
           
       }
       if (operator.contains("endswith"))
       {
    	   frames.add(KoralFrame.ENDS_WITH);
           frames.add(KoralFrame.MATCHES);
           
       }
       if (operator.contains("isaround"))
       {
          
    		  frames.add(KoralFrame.IS_AROUND);
          
          }
    	
           
          if (operator.contains("matches"))
          {
            frames.add(KoralFrame.MATCHES);
        }
       
       if (operator.contains("rbound"))
              {
    	   		frames.add(KoralFrame.ENDS_WITH);
    	   		frames.add(KoralFrame.MATCHES);
              }
       if (operator.contains("lbound"))
       {
	   		frames.add(KoralFrame.STARTS_WITH);
	   		frames.add(KoralFrame.MATCHES);
       }
        
       
       // this is to eliminate doubled KoralFrame.MATCHES; to delete if cleared in the ifs above
       int matchescount=0;
       for (int f=0; f< frames.size();f++) 
       {
    	   KoralFrame frame = frames.get(f);
    	   String framevalue = frame.toString();
    	   if (framevalue.equals("frames:matches"))
    			   {
    		   			matchescount++;
    		   			if (matchescount>1)
    		   	    	   frames.remove(f);
    			   }
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

            // Term is defined recursively with non-necessary brackets; exception for lbound and rbound; // is it necessary to verify termGroup too??
            if (getNodeCat(node.getChild(0)).equals("(") && ((getNodeCat(node.getChild(1)).equals("term")) || getNodeCat(node.getChild(1)).equals("termGroup"))){
                return parseTermOrTermGroup(node.getChild(1), negatedGlobal,
                        mode);
            };
            //Term is negated outside brackets
            if (getNodeCat(node.getChild(0)).equals("!") && getNodeCat(node.getChild(1)).equals("(")) { 
            		// negate negatedGLobal
            	return parseTermOrTermGroup(node.getChild(2), !negatedGlobal,
                        mode);
            	
            		}

            String key = null;
            String value = null;
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

            //  process regex
			
            if (getNodeCat(keyNode.getChild(0)).equals("regex")) {
                isRegex = true;
                term.put("type", "type:regex");
                // remove leading and trailing quotes
                //process verbatim flag %l
                if (flagNode!=null) {
                if (getNodeCat(flagNode.getChild(0)).contains("l") || getNodeCat(flagNode.getChild(0)).contains("L"))  {

    				// Get stream from hidden channel
    				TokenStream stream = parser.getTokenStream();
    				key = stream.getText(keyNode.getChild(0).getSourceInterval());
    				key = key.substring(1, key.length()-1).replaceAll("\\\\\\\\","\\\\").replaceAll("\\\\'", "'");
    				//override with type:string
    				term.put("type", "type:string");
    				}
                	else
                	{
        				key = keyNode.getText();
        				key = key.substring(1, key.length() - 1);
                	}
                }
    			else {
    				key = keyNode.getText();
    				key = key.substring(1, key.length() - 1);
    			};
            
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
            	
            	  if (getNodeCat(valueNode.getChild(0)).equals("regex")) {
                      isRegex = true;
                      term.put("type", "type:regex");
                      // remove leading and trailing quotes
                      value = valueNode.getText();
          			  value = value.substring(1, value.length() - 1);
          			  term.put("value", value);
          			};
            	
                
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
                // substring removes leading %
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
            
           //TermGroup is negated outside brackets
            if (getNodeCat(node.getChild(0)).equals("!") && getNodeCat(node.getChild(1)).equals("(")) { 
            		// negate negatedGLobal
            	return parseTermOrTermGroup(node.getChild(2), !negatedGlobal,
                        mode);
            }

            // For termGroups, establish a boolean relation between
            // operands and recursively call this function with
            // the term or termGroup operands
            Map<String, Object> termGroup = null;
            ParseTree leftOp = null;
            ParseTree rightOp = null;


            // check for leading/trailing parentheses
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

			// Create group// De Morgan's laws
            if (boolOp.getText().equals("&") && negatedGlobal==false) {
                termGroup = KoralObjectGenerator
                        .makeTermGroup(KoralTermGroupRelation.AND);
            }
            if (boolOp.getText().equals("|") && negatedGlobal==false) {
                termGroup = KoralObjectGenerator
                        .makeTermGroup(KoralTermGroupRelation.OR);
            }
            if (boolOp.getText().equals("&") && negatedGlobal==true) {
                termGroup = KoralObjectGenerator
                        .makeTermGroup(KoralTermGroupRelation.OR);
            }
            if (boolOp.getText().equals("|") && negatedGlobal==true) {
                termGroup = KoralObjectGenerator
                        .makeTermGroup(KoralTermGroupRelation.AND);
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

        if (flag.contains("c")||flag.contains("C") ) flags.add("flags:caseInsensitive");
    
        if (flag.contains("d")|| flag.contains("D"))
        {
        	flags.add("flags:diacriticsInsensitive");
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
            if (object.get("@type").equals("koral:span")) 
            {
            	if (!topObjectOperands.isEmpty() && objectStack.get(objStackPosition) .containsKey("frames"))
            	{topObjectOperands.add(0, object);}
            	else
            	{topObjectOperands.add(object);}

            }
            else
            {
            	topObjectOperands.add(object);
            }
            
       
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
    */


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


    private ParserRuleContext parseCQPQuery (String query) {
        Lexer lexer = new CQPLexer((CharStream) null);
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
            
            parser = new CQPParser(tokens);

            // Don't throw out erroneous stuff
            parser.setErrorHandler(new BailErrorStrategy());
            lexer.removeErrorListeners();
            lexer.addErrorListener(errorListener);
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);

            // Get starting rule from parser
            Method startRule = CQPParser.class.getMethod("request");
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
