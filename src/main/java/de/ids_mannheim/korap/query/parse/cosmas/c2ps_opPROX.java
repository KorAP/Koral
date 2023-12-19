package de.ids_mannheim.korap.query.parse.cosmas;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import de.ids_mannheim.korap.query.serialize.util.Antlr3DescriptiveErrorListener;
import de.ids_mannheim.korap.query.serialize.util.StatusCodes;
import de.ids_mannheim.korap.util.*;

/*
 * parses Opts of PROX: /w3:4,s0,min or %w3:4,s0,min.
 */

public class c2ps_opPROX

{

	/* encodeDIST():
	 * - returns a CommonTree built of out Direction/Measure/Distance value.
	 * - accepts options in any order.
	 * - creates CommonTree in that order: Direction .. Distance value .. Measure.
	 * - sets default direction to BOTH if not set yet.
	 * 28.11.23/FB
	 */
	
	public static Object encodeDIST(int typeDIST, int typeDIR, Object ctDir, Object ctMeas, Object ctVal, String text)  
			throws C2RecognitionException 
	{
		boolean multiple = true;
		CommonTree tree1 = (CommonTree)ctDir;
		CommonTree tree2 = (CommonTree)ctMeas;
		CommonTree tree3 = (CommonTree)ctVal;
		
		//addError(StatusCodes.MALFORMED_QUERY,
        //        "Could not parse query. Please make sure it is well-formed.");
		
		System.err.printf("Debug: encodeDIST: scanned input='%s'.\n", text);
		
		if( multiple == true )
			{
			CommonTree 
				errorTree = new CommonTree(new CommonToken(typeDIST, "DIST")),
				errorNode = new CommonTree(new CommonToken(1, "ERROR")),
				errorPos  = new CommonTree(new CommonToken(1, "15")),
				errorArg  = new CommonTree(new CommonToken(1, text));
			
			errorTree.addChild(errorNode);
			errorNode.addChild(errorPos);
			errorNode.addChild(errorArg);
			
			System.err.printf("Debug: encodeDIST: parse error found, returning error tree: %s.\n", 
					errorTree.toStringTree());
			
			return errorTree;
			
			/* throwing an Exception is only part of the solution,
			 * but how to stop parsing and return the error code properly - 07.12.23/DB
			 * 
			String mess = String.format("line 0:%d %s expecting only 1 of 'wsp'!\n",
									2345, text);
			//de.ids_mannheim.korap.query.serialize.Antlr3AbstractQueryProcessor.reportError(mess);
			//reportError(mess);
			C2RecognitionException re = new C2RecognitionException(text);
			re.c = '/';
			re.charPositionInLine = 15; //tokenPos;
			re.index = 1;
			re.line = 0;
			
			throw re;
			*/
			}
			
		
		System.err.printf("Debug: encodeDIST: ctDir='%s': %d ctMeas='%s': %d ctVal='%s': %d.\n",
				tree1 != null ? tree1.toStringTree() : "null",
				tree1 != null ? tree1.getChildCount() : 0,
				tree2 != null ? tree2.toStringTree() : "null",
				tree2 != null ? tree2.getChildCount() : 0,
				tree3 != null ? tree3.toStringTree() : "null",
				tree3 != null ? tree3.getChildCount() : 0);

		// if direction is not specified, return default = BOTH:
		if( ctDir == null )
			{
			CommonTree treeDIR = new CommonTree(new CommonToken(typeDIR, (String)"DIR"));
			//CommonToken tok = new CommonToken(typeDIR, "BOTH");
			CommonTree treeBOTH = new CommonTree(new CommonToken(typeDIR, "BOTH"));
			treeDIR.addChild(treeBOTH);
			
			System.err.printf("Debug: encodeDIST: tree for DIR: '%s'.\n", 
					treeDIR.toStringTree());
			tree1 = treeDIR;
			}
		
		CommonTree 
			tree = new CommonTree(new CommonToken(typeDIST, "DIST"));
		
		tree.addChild(tree1);
		tree.addChild(tree3); // tree3 before tree2 expected by serialization.
		tree.addChild(tree2);
		
		System.err.printf("Debug: encodeDIST: returning '%s'.\n", 
				tree.toStringTree());
		
		return tree;
	} // encodeDIST
	
	public static boolean checkDIST(String input)
	
	{
		return true;
	}
	
	public static Tree check (String input, int index) {
        ANTLRStringStream ss = new ANTLRStringStream(input);
        c2ps_opPROXLexer lex = new c2ps_opPROXLexer(ss);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        c2ps_opPROXParser g = new c2ps_opPROXParser(tokens);
        c2ps_opPROXParser.opPROX_return c2PQReturn = null;

        /*
        System.out.println("check opPROX:" + index + ": " + input);
        System.out.flush();
         */

        try {
            c2PQReturn = g.opPROX();
        }
        catch (RecognitionException e) {
            e.printStackTrace();
        }

        // AST Tree anzeigen:
        Tree tree = (Tree) c2PQReturn.getTree();
        //System.out.println("PROX: " + tree.toStringTree() );

        return tree;
    }

	public static boolean checkFalse()
	{
	
	return false; // testwise	
	}
	
	public static boolean checkMeasure( Object measure)
	{
		System.err.printf("Debug: checkMeasure: measure = %s.\n",
				measure == null ? "null" : "not null");
		return true;
	}
	
    /*
     * main testprogram:
     */

    public static void main (String args[]) throws Exception {
        String[] input = { "/w1:3", "%w5", "/+w3,s0,max" };
        Tree tree;

        System.out.println("Tests von PROX-Optionen:\n");

        for (int i = 0; i < input.length; i++) {
            tree = check(input[i], 0);
            System.out.println("PROX: input: " + input[i]);
            System.out.println("PROX: AST  : " + tree.toStringTree() + "\n");

            // Visualize AST Tree:
            /*
            DOTTreeGenerator gen = new DOTTreeGenerator();
            StringTemplate st = gen.toDOT(tree);
            System.out.println("DOTTREE: " + st);
            */
        }

    } // main

}
