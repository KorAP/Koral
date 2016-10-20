package de.ids_mannheim.korap.query.parse.cosmas;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

// parses Opts in #BED(x,Opts):

public class c2ps_opBED

{

    public static Tree check (String input, int index) {
        ANTLRStringStream ss = new ANTLRStringStream(input);
        c2ps_opBEDLexer lex = new c2ps_opBEDLexer(ss);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        c2ps_opBEDParser g = new c2ps_opBEDParser(tokens);
        c2ps_opBEDParser.opBEDOpts_return c2PQReturn = null;

        /*
        System.out.println("check opBED: " + index + ": " + input);
        System.out.flush();
        */

        try {
            c2PQReturn = g.opBEDOpts();
        }
        catch (RecognitionException e) {
            e.printStackTrace();
        }

        // AST Tree anzeigen:
        Tree tree = (Tree) c2PQReturn.getTree();
        //System.out.println("#BED Opts: " + tree.toStringTree() );

        return tree;
    }


    /*
     * check Text Position starting at rule textpos.
     */

    public static Tree checkTPos (String input, int index) {
        ANTLRStringStream ss = new ANTLRStringStream(input);
        c2ps_opBEDLexer lex = new c2ps_opBEDLexer(ss);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        c2ps_opBEDParser g = new c2ps_opBEDParser(tokens);
        c2ps_opBEDParser.textpos_return c2PQReturn = null;

        /*
        System.out.println("check opBED: " + index + ": " + input);
        System.out.flush();
        */

        try {
            c2PQReturn = g.textpos();
        }
        catch (RecognitionException e) {
            e.printStackTrace();
        }

        // AST Tree anzeigen:
        Tree tree = (Tree) c2PQReturn.getTree();
        // System.out.println("#BED Opts: " + tree.toStringTree() );

        return tree;
    }


    public static void main (String args[]) throws Exception {
        String[] input = { ",sa,se,-ta,-te/pa,-pe)", ",sa)", ",/pa,-pe)" };
        Tree tree;

        for (int i = 0; i < input.length; i++) {
            tree = check(input[i], 0);
            System.out.println(
                    "Parsing input: " + input[i] + ": " + tree.toStringTree());
        }

    } // main

}
