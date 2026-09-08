package de.ids_mannheim.korap.query.parse.cosmas;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import de.ids_mannheim.korap.util.*;

/* parses Search Expression inside MORPH(...).
 * 01.06.26/FB
 */


public class c2ps_opMORPH

{
	final static boolean bShow  	 = false;
	final static boolean bShowTokens = false;
	
	/* Method check():
     * input: 
     *  - MORPH(), 
     * 	- MORPH(NOU SG EIGN),
     *  - MORPH(foundry/layer=NOU), MORPH(foundry/layer=temp:past) etc.
     *  - MORPH(foundry1/layer1=NOU (&)? foundry2/layer2/temp:pres etc. ).
     * 
     */
    public static Tree check (String input, int index) 
    {
        ANTLRStringStream ss 		= new ANTLRStringStream(input);
        c2ps_opMORPHLexer lex 		= new c2ps_opMORPHLexer(ss);
        CommonTokenStream tokens 	= new CommonTokenStream(lex);
        c2ps_opMORPHParser g 		= new c2ps_opMORPHParser(tokens);
        c2ps_opMORPHParser.opMORPH_return 
        	c2PQReturn 				= null;

        if( bShow )
        	{
        	System.out.printf("check opMORPH index=%d, input='%s'.\n", index, input); 
        	if( bShowTokens )
        		TokenUtils.printLexerTokens(tokens, "opMORPH");
        	}

        try {
            c2PQReturn = g.opMORPH();
        	}
        catch (RecognitionException e) {
            e.printStackTrace();
        	}
       
        Tree tree = (Tree) c2PQReturn.getTree();
        
        // show AST Tree:
        if( bShow )
        	System.out.printf("check opMORPH: AST='%s'.\n", tree.toStringTree());
        	
        return tree;
    }


    /*
     * main - Testprogramm for #ELEM(...)
     */

    public static void main (String args[]) throws Exception 
    {
    /*
         String[] input = { "#ELEM()", "#ELEM(   )", "#ELEM(S)",
     
                "#ELEM(W ANA='DET ADV')",
                "#ELEM( TITLE TYPE!=Unterüberschrift )",
                "#ELEM(v='a b c' w!='d e f' x=y )",
                "#ELEM(flexion='l\\'été' lemma='été')" };
        Tree tree;

        for (int i = 0; i < input.length; i++) {
            System.out.println("#ELEM input: " + input[i]);
            tree = check(input[i], 0);
            System.out.println("#ELEM AST  : " + tree.toStringTree());
        }
	*/
    } // main

}
