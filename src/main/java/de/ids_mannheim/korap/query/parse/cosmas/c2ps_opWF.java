package de.ids_mannheim.korap.query.parse.cosmas;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import de.ids_mannheim.korap.query.parse.cosmas.c2ps_opPROXLexer;
import de.ids_mannheim.korap.query.parse.cosmas.c2ps_opPROX;
import de.ids_mannheim.korap.query.serialize.util.StatusCodes;

/*
 * parses prefixed and suffixed options of a search wordform.
 * E.g. :fi:Hendrix:sa/-pe.
 */

public class c2ps_opWF

{
	//static final int OPWF  = 5; // must be same value than OPWF in c2ps_opWF.g
	//static final int OPLEM = 7; // must be same value than OPLEM in c2ps_opWF.g
    /* Arguments:
     * bStrip: true: 'input' contains "wort" -> strip " away -> wort.
     *        false: 'input' contains no " -> nothing to strip.
     * bLem: true: input contains a Lemma; generates tree ^(OPLEM...).
     *       false: input contains a Wordform; generates tree ^(OPWF...).
     * input: may be a single Lemma or Wform or a list of Wforms.
     */

    public static Tree check (String input, boolean bStrip, boolean bLem, int pos) 
    {
    	if( bLem )
    		{
    		System.out.printf("c2ps_opWF.check: input='%s' bStrip=%b bLem=%b pos=%d.\n", 
    				 input, bStrip, bLem, pos);
    		System.out.flush();
    		}
    	
    	if (bStrip)
            input = input.substring(1, input.length() - 1);

        if (bLem && input.charAt(0) == '&') {
            input = input.substring(1, input.length());
            //System.out.println("Lemma: strip '&' -> " + input);
        }

        ANTLRStringStream ss = new ANTLRStringStream(input);
        c2ps_opWFLexer lex = new c2ps_opWFLexer(ss);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        c2ps_opWFParser g = new c2ps_opWFParser(tokens);
        c2ps_opWFParser.searchWFs_return c2PQWFReturn = null;
        c2ps_opWFParser.searchLEM_return c2PQLEMReturn = null;

        /*
        System.out.println("check opWF:" + index + ": " + input);
        System.out.flush();
        */

        try {
            if (bLem)
                c2PQLEMReturn = g.searchLEM(pos);
            else
                c2PQWFReturn = g.searchWFs(pos);
        }
        catch (RecognitionException e) {
            e.printStackTrace();
        }

        // AST Tree anzeigen:
        Tree tree = bLem ? (Tree)c2PQLEMReturn.getTree() : (Tree)c2PQWFReturn.getTree();
        
        if( bLem )
        	 {
        	 System.out.printf("c2ps_opWF.check: %s: '%s'.\n", bLem ? "opLEM" : "opWF", 
        			 tree.toStringTree() );
        	 System.out.flush();
        	 }

        return tree;
    }


    /* Wordform Encoding, e.g. to insert a Wordform into an AST.
     * a) wf ->  "wf".
     * b) remove escape char before ':': abc\: -> abc:.
     * Args:
     * wf			: wordform or lemma (expected lemma : "lemma" or "opts&lemma",
     * 				  the starting '&' has been removed before entering this function).
     * tokenType	: either OPWF or OPLEM.
     * pos			: start position of wf.
     * Notes:
     *  - &opts&lemma : may contain wildcards as options in the &opts& section only.
     *    reject if wildcards appear in the &lemma section.
     * Returns a Tree or an ErrorTree.
     */
    public static Tree encode (String wf, int tokenType, int pos)

    {
    	//System.out.printf("c2ps_opWF.encode: wf='%s' tokenType=%d pos=%d.\n",  wf, tokenType, pos);
    	
        // b)
        StringBuffer sbWF = new StringBuffer(wf);

        for (int i = 0; i < sbWF.length()-1; i++) 
        	{
            if (sbWF.charAt(i) == '\\' && sbWF.charAt(i + 1) == ':')
                sbWF.deleteCharAt(i);
        	}
        
        if( tokenType == c2ps_opWFLexer.OPLEM )
        	{
        	boolean hasOpts  = false; // true if a '&' occurs: e.g. "Fes+C&lemma"
        	boolean hasFound = false; // false for all wildcards found to the left of '&', true in all other cases.
        	
        	for(int i=0; i< sbWF.length(); i++)
	        	{
        		if( sbWF.charAt(i) == '&' )
        			{
        			hasOpts  = true;
        			hasFound = false;
        			}
        		else if (sbWF.charAt(i) == '?' || sbWF.charAt(i) == '*' || sbWF.charAt(i) == '+' )
        			{
        			hasFound = true;
        			}
        		}
        	
        	// error if hasFound==true:
        	if( hasFound )
	        	{
        		System.out.printf("c2ps_opWF.encode: Syntax error: '%s' contains wildcards inside lemma expression!\n", wf);
        		return buildErrorTree(wf, StatusCodes.ERR_LEM_WILDCARDS, pos);
        		}
        	}
        
        return new CommonTree(new CommonToken(tokenType, "\"" + sbWF.toString() + "\""));
    }

    /**
	 * buildErrorTree(): 
	 * @param text = part of the query that contains an error.
	 * @param errCode
	 * @param typeDIST
	 * @param pos
	 * @return
	 */
	
	//private static CommonTree buildErrorTree(String text, int errCode, int typeDIST, int pos)
    
    private static CommonTree buildErrorTree(String text, int errCode, int pos)
	{
	/*
	 CommonTree
	//errorTree = new CommonTree(new CommonToken(typeDIST, "DIST"));
	errorTree = new CommonTree(new CommonToken(c2ps_opPROX.typeERROR, "Fehlercherchen"));
	*/
	CommonTree
		errorNode = new CommonTree(new CommonToken(c2ps_opPROX.typeERROR, "ERROR"));
	CommonTree
		errorPos  = new CommonTree(new CommonToken(c2ps_opPROX.typeERROR, String.valueOf(pos)));
	CommonTree
		errorCode = new CommonTree(new CommonToken(c2ps_opPROX.typeERROR, String.valueOf(errCode)));
	CommonTree
		errorMes;
	String
		mess;
	
	mess 	 = c2ps_opPROX.getErrMess(errCode, c2ps_opPROX.messLang, text);
	errorMes = new CommonTree(new CommonToken(c2ps_opPROX.typeERROR, mess));
	
	// new:
	errorNode.addChild(errorPos);
	errorNode.addChild(errorCode);
	errorNode.addChild(errorMes);
	
	return errorNode;
	
	/* old, no need for errorTree(typeXY).
	errorTree.addChild(errorNode);
	errorNode.addChild(errorPos);
	errorNode.addChild(errorCode);
	errorNode.addChild(errorMes);
	
	return errorTree;
	*/
	}
    
    /*
     * main testprogram:
     */

    public static void main (String args[]) throws Exception {
        String[] input = { ":fi:Hendrix:sa", ":FiOlDs:été:sa", "&Gitarre",
                "&Gitarre:sa/-pe", " \"Institut für \\:Deutsche\\: Sprache\" ",
                ":Fi:der:-sa Wilde:-se Western:/se" };
        Tree tree;
        boolean bLem;

        System.out.println("Tests von WF und Lemma-Optionen:\n");

        for (int i = 0; i < input.length; i++) {
            bLem = input[i].charAt(0) == '&' ? true : false;

            System.out.println(bLem ? "LEM: " : "WF: " + "input: " + input[i]);

            if (bLem)
                tree = check(input[i], false, true, 0); // bStrip=false, bLem=true;
            else
                tree = check(input[i], false, false, 0); // bStrip=false, bLem=false.

            System.out.println(bLem ? "LEM: " : "WF: " + "AST  : "
                    + tree.toStringTree() + "\n");
        }

    } // main

}
