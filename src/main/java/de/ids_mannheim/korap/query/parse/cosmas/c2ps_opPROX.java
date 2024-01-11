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
	// type of an Error CommonToken:
	final static int typeERROR = 1; 
	// error codes returned to client:
	final public static int ERR_PROX_UNKNOWN 	= 300;
	final static int ERR_MEAS_NULL 		= 301;
	final static int ERR_MEAS_TOOGREAT 	= 302;
	final static int ERR_VAL_NULL 		= 303;
	final static int ERR_VAL_TOOGREAT 	= 304;
	final static int ERR_DIR_TOOGREAT 	= 305;
	
	private static CommonTree buildErrorTree(String text, int errCode, int typeDIST, int pos) throws RecognitionException
	
	{
	CommonTree
		errorTree = new CommonTree(new CommonToken(typeDIST, "DIST")); 
	CommonTree
		errorNode = new CommonTree(new CommonToken(typeERROR, "ERROR"));
	CommonTree
		errorPos  = new CommonTree(new CommonToken(typeERROR, String.valueOf(pos)));
	CommonTree
		errorCode = new CommonTree(new CommonToken(typeERROR, String.valueOf(errCode)));
	CommonTree
		errorMes;
	String
		mess;
	
	switch( errCode )
		{
	case ERR_MEAS_NULL:
		mess      = String.format("Abstandsoperator an der Stelle '%s' es fehlt eine der folgenden Angaben: w,s,p!", text);
		errorMes  = new CommonTree(new CommonToken(typeERROR, mess));
		break;
	case ERR_MEAS_TOOGREAT:
		mess      = String.format("Abstandsoperator an der Stelle '%s': Bitte nur 1 der folgenden Angaben einsetzen: w,s,p! " +
							 "Falls Mehrfachangabe erwünscht, müssen diese durch Kommata getrennt werden (z.B.: /+w2,s0).", text);
		errorMes  = new CommonTree(new CommonToken(typeERROR, mess));
		break;
	case ERR_VAL_NULL:
		mess      = String.format("Abstandsoperator an der Stelle '%s': Bitte einen numerischen Wert einsetzen (z.B. /+w5)! ", text);
		errorMes  = new CommonTree(new CommonToken(typeERROR, mess));
		break;
	case ERR_VAL_TOOGREAT:
		mess      = String.format("Abstandsoperator an der Stelle '%s': Bitte nur 1 numerischen Wert einsetzen (z.B. /+w5)! ", text);
		errorMes  = new CommonTree(new CommonToken(typeERROR, mess));
		break;
	case ERR_DIR_TOOGREAT:
		mess      = String.format("Abstandsoperator an der Stelle '%s': Bitte nur 1 Angabe '+' oder '-' oder keine! ", text);
		errorMes  = new CommonTree(new CommonToken(typeERROR, mess));
		break;
	default:
		mess = String.format("Abstandsoperator an der Stelle '%s': unbekannter Fehler. Korrekte Syntax z.B.: /+w2 oder /w10,s0.", text);

		errorMes  = new CommonTree(new CommonToken(typeERROR, mess));
		}
	
	errorTree.addChild(errorNode);
	errorNode.addChild(errorPos);
	errorNode.addChild(errorCode);
	errorNode.addChild(errorMes);

	return errorTree;
	}

	/* encodeDIST():
	 * - returns a CommonTree built from the Direction/Measure/Distance value.
	 * - accepts options in any order.
	 * - creates CommonTree in that order: Direction .. Distance value .. Measure.
	 * - sets default direction to BOTH if not set yet.
	 * - unfortunatly, in ANTLR3 it seems that there is no way inside the Parser Grammar to get 
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
									throws RecognitionException
			
	{
		CommonTree tree1 = (CommonTree)ctDir;
		CommonTree tree2 = (CommonTree)ctMeas;
		CommonTree tree3 = (CommonTree)ctVal;
		
		System.err.printf("Debug: encodeDIST: scanned input='%s' countM=%d countD=%d countV=%d pos=%d.\n", 
					text, countM, countD, countV, pos);

		if( countM == 0 )
			return buildErrorTree(text, ERR_MEAS_NULL, typeDIST, pos);
		if( countM > 1 )
			return buildErrorTree(text, ERR_MEAS_TOOGREAT, typeDIST, pos);
		if( countV == 0 )
			return buildErrorTree(text, ERR_VAL_NULL, typeDIST, pos);
		if( countV > 1 )
			return buildErrorTree(text, ERR_VAL_TOOGREAT, typeDIST, pos);
		
		if( countD == 0 )
			{
			// if direction is not specified (ctDir == null or countD==0), return default = BOTH:
			CommonTree treeDIR  = new CommonTree(new CommonToken(typeDIR, (String)"DIR"));
			CommonTree treeBOTH = new CommonTree(new CommonToken(typeDIR, "BOTH"));
			treeDIR.addChild(treeBOTH);
			
			System.err.printf("Debug: encodeDIST: tree for DIR: '%s'.\n", 
					treeDIR.toStringTree());
			tree1 = treeDIR;
			}
		else if( countD > 1 )
			return buildErrorTree(text, ERR_DIR_TOOGREAT, typeDIST, pos);
	
		// create DIST tree:
		CommonTree 
			tree = new CommonTree(new CommonToken(typeDIST, "DIST"));
		
		tree.addChild(tree1);
		tree.addChild(tree3); // tree3 before tree2 expected by serialization.
		tree.addChild(tree2);
		
		System.err.printf("Debug: encodeDIST: returning '%s'.\n", tree.toStringTree());
		
		return tree;
	} // encodeDIST
	
	public static boolean checkDIST(String input)
	
	{
		return true;
	}
	
	public static Tree check (String input, int pos) throws RecognitionException
	{
        ANTLRStringStream ss = new ANTLRStringStream(input);
        c2ps_opPROXLexer lex = new c2ps_opPROXLexer(ss);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        c2ps_opPROXParser g = new c2ps_opPROXParser(tokens);
        c2ps_opPROXParser.opPROX_return c2PQReturn = null;

        /**/
        System.out.printf("check opPROX: pos=%d input='%s'.\n", pos, input);
        System.out.flush();
        /**/

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
