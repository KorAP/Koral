package de.ids_mannheim.korap.query.parse.cosmas;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

/*
 * parses Opts of PROX: /w3:4,s0,min or %w3:4,s0,min.
 */

public class c2ps_opPROX

{

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
