package de.ids_mannheim.korap.query.parse.cosmas;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

// parses Case Options.

public class c2ps_optCase

{

    public static Tree check (String input, int index) {
        ANTLRStringStream ss = new ANTLRStringStream(input);
        c2ps_optCaseLexer lex = new c2ps_optCaseLexer(ss);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        c2ps_optCaseParser g = new c2ps_optCaseParser(tokens);
        c2ps_optCaseParser.optCase_return c2PQReturn = null;

        /*
        System.out.println("check optCase: " + index + ": " + input);
        System.out.flush();
        */

        try {
            c2PQReturn = g.optCase();
        }
        catch (RecognitionException e) {
            e.printStackTrace();
        }

        // AST Tree anzeigen:
        Tree tree = (Tree) c2PQReturn.getTree();
        //System.out.println("Case Opts: " + tree.toStringTree() );

        return tree;
    }


    /*
     * Main Text programm.
     *
     */

    public static void main (String args[]) throws Exception {
        String[] input = { "Fi", "FiOsDi" };
        Tree tree;

        for (int i = 0; i < input.length; i++) {
            tree = check(input[i], 0);
            System.out.println("Parsing input: " + input[i] + ": "
                    + tree.toStringTree());
        }

    } // main

}
