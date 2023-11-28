package de.ids_mannheim.korap.query.parse.cosmas;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

/*
 * parses Opts of PROX: /w3:4,s0,min or %w3:4,s0,min.
 */

public class c2ps_opPROX

{

	/* encode():
	 * - encodes Distance type, Direction and Distance value
	 *   which are written in any order.
	 * 28.11.23/FB
	 */
	
	public static Tree encode(String input, int type)
	{
		StringBuffer sb = new StringBuffer("(DIST (DIR MINUS) (RANGE VAL0 0) (MEAS w))");
		System.err.printf("Debug: encode: input = '%s' output = >>%s<<.\n", input, sb.toString());
		CommonTree ctree = new CommonTree(new CommonToken(type, sb.toString()));
		//CommonTree treeType = new CommonTree(new CommonToken(1, ""))
		//CommonToken ct = ct.
		System.err.printf("Debug: encode: CommonTree : '%s'.\n", ctree.toStringTree());
		//return new CommonTree(new CommonToken(type, sb.toString()));
		return ctree;
	} // encode
	
	/* encodeDefaultDir():
	 * - return a tree containing the default Prox Direction when there is no
	 *   direction indication in the input query.
	 * 28.11.23/FB
	 */
	
	public static Tree encodeDefautDir(String input, int type)
	{
		StringBuffer sb = new StringBuffer("BOTH");
		CommonTree tree = new CommonTree(new CommonToken(type, sb.toString()));
		
		System.err.printf("Debug: encodeDefaultDir: CommonTree : '%s'.\n", tree.toStringTree());

		return tree;
	} // encode
		
	/* encodeDefaultDir():
	 * - return a tree containing the default Prox Direction when there is no
	 *   direction indication in the input query.
	 * 28.11.23/FB
	 */
	
	public static Object encodeDIST(int type, Object ctDir, Object ctMeas, Object ctVal)
	{
		StringBuffer sb = new StringBuffer("BOTH");
		CommonTree tree1 = (CommonTree)ctDir;
		CommonTree tree2 = (CommonTree)ctMeas;
		CommonTree tree3 = (CommonTree)ctVal;
		
		System.err.printf("Debug: encodeDIST: ctDir='%s' ctMeas='%s' ctVal='%s'.\n",
				tree1 != null ? tree1.toStringTree() : "null",
				tree2 != null ? tree2.toStringTree() : "null",
				tree3 != null ? tree3.toStringTree() : "null");

		if( ctDir == null )
			{
				
			}
		CommonTree 
			tree = new CommonTree(new CommonToken(type, "DIST"));
		
		tree.addChild(tree1);
		tree.addChild(tree3); // tree3 before tree2.
		tree.addChild(tree2);
		
		return tree;
	} // encodeDIST
	
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
