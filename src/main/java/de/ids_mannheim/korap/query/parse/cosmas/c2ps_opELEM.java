package de.ids_mannheim.korap.query.parse.cosmas;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

// parses Search Expression inside #ELEM(...):

public class c2ps_opELEM

{

    /* Method check():
     * input: e.g. #ELEM(S), #ELEM(W ANA='DET ADJ'),
     *             #ELEM(ANA <> 'V sg' TYP !=VP), etc.
     */
    public static Tree check (String input, int index) {
        ANTLRStringStream ss = new ANTLRStringStream(input);
        c2ps_opELEMLexer lex = new c2ps_opELEMLexer(ss);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        c2ps_opELEMParser g = new c2ps_opELEMParser(tokens);
        c2ps_opELEMParser.opELEM_return c2PQReturn = null;

        /*
        System.out.println("check opELEM: " + index + ": " + "'" + input + "'");
        System.out.flush();
         */

        try {
            c2PQReturn = g.opELEM();
        }
        catch (RecognitionException e) {
            e.printStackTrace();
        }

        // AST Tree anzeigen:
        Tree tree = (Tree) c2PQReturn.getTree();
        //System.out.println("#ELEM Opts: " + tree.toStringTree() );

        return tree;
    }


    /*
     * main - Testprogramm for #ELEM(...)
     */

    public static void main (String args[]) throws Exception {
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

    } // main

}
