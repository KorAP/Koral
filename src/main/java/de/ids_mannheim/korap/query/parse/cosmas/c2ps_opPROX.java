package de.ids_mannheim.korap.query.parse.cosmas;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

import de.ids_mannheim.korap.query.serialize.Antlr3AbstractQueryProcessor;
import de.ids_mannheim.korap.query.serialize.util.Antlr3DescriptiveErrorListener;
import de.ids_mannheim.korap.query.serialize.util.StatusCodes;
import de.ids_mannheim.korap.util.*;

/*
 * parses Opts of PROX: /w3:4,s0,min or %w3:4,s0,min.
 */

public class c2ps_opPROX 

{
	final static boolean 
		bDebug = false;
	
	/* encodeDIST():
	 * - returns a CommonTree built from the Direction/Measure/Distance value.
	 * - accepts options in any order.
	 * - creates CommonTree in that order: Direction .. Distance value .. Measure.
	 * - sets default direction to BOTH if not set yet.
	 * - unfortunately, in ANTLR3 it seems that there is no way inside the Parser Grammar to get 
	 *   the absolute token position from the beginning of the query. Something like $ProxDist.pos or
	 *   $start.pos is not available, so we have no info in this function about the position at which
	 *   an error occurs. 
	 * - For multiple prox options, e.g. /w2,s2,p0, this function if called 3 times.
	 * Arguments:
	 * countD	: how many occurences of distance: + or - or nothing. If 0 insert the default BOTH.
	 * countM	: how many occurences of measure: w,s,p,t: should be 1.
	 * countV	: how many occurences of distance value: should be 1.
	 * 28.11.23/FB
	 */
	
	public static Object encodeDIST(int typeDIST, int typeDIR, Object ctDir, Object ctMeas, Object ctVal, String text,
									int countD, int countM, int countV, int pos)  
			
	{
		CommonTree tree1 = (CommonTree)ctDir;
		CommonTree tree2 = (CommonTree)ctMeas;
		CommonTree tree3 = (CommonTree)ctVal;
		
		if( bDebug )
			System.out.printf("Debug: encodeDIST: scanned input='%s' countM=%d countD=%d countV=%d pos=%d.\n", 
					text, countM, countD, countV, pos);

		if( countM == 0 )
			return StatusCodes.buildErrorTree(text, StatusCodes.ERR_PROX_MEAS_NULL, pos);
		if( countM > 1 )
			return StatusCodes.buildErrorTree(text, StatusCodes.ERR_PROX_MEAS_TOOGREAT, pos);
		if( countV == 0 )
			return StatusCodes.buildErrorTree(text, StatusCodes.ERR_PROX_VAL_NULL, pos);
		if( countV > 1 )
			return StatusCodes.buildErrorTree(text, StatusCodes.ERR_PROX_VAL_TOOGREAT, pos);
		
		if( countD == 0 )
			{
			// if direction is not specified (ctDir == null or countD==0), return default = BOTH:
			CommonTree treeDIR  = new CommonTree(new CommonToken(typeDIR, (String)"DIR"));
			CommonTree treeBOTH = new CommonTree(new CommonToken(typeDIR, "BOTH"));
			treeDIR.addChild(treeBOTH);
			
			if( bDebug )
				System.out.printf("Debug: encodeDIST: tree for DIR: '%s'.\n", treeDIR.toStringTree());
			tree1 = treeDIR;
			}
		else if( countD > 1 )
			return StatusCodes.buildErrorTree(text, StatusCodes.ERR_PROX_DIR_TOOGREAT, pos);
	
		// create DIST tree:
		CommonTree 
			tree = new CommonTree(new CommonToken(typeDIST, "DIST"));
		
		tree.addChild(tree1);
		tree.addChild(tree3); // tree3 before tree2 expected by serialization.
		tree.addChild(tree2);
		
		if( bDebug )
			System.out.printf("Debug: encodeDIST: returning '%s'.\n", tree.toStringTree());
		
		return tree;
	} // encodeDIST
	
	/* checkRemain:
	 * 
	 * - the chars in proxRemain are not allowed in prox. options.
	 * - return an error tree.
	 * 12.01.24/FB
	 */
	
	public static Object checkRemain(int typeDIST, String proxRemain, int pos)
	
	{
		if( bDebug )
			System.out.printf("Debug: checkRemain: '%s' at pos %d.\n", proxRemain, pos);
		
		return StatusCodes.buildErrorTree(proxRemain, StatusCodes.ERR_PROX_WRONG_CHARS, pos);
	}
	
	public static Tree check (String input, int pos) throws RecognitionException
	{
        ANTLRStringStream ss = new ANTLRStringStream(input);
        c2ps_opPROXLexer lex = new c2ps_opPROXLexer(ss);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        c2ps_opPROXParser g = new c2ps_opPROXParser(tokens);
        c2ps_opPROXParser.opPROX_return c2PQReturn = null;

        if( bDebug )
        	System.out.printf("check opPROX: pos=%d input='%s'.\n", pos, input);

        try {
            c2PQReturn = g.opPROX(pos);
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
