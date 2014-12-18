package de.ids_mannheim.korap.query.parse.cosmas;

import java.io.*;
import org.antlr.runtime.*;
import org.antlr.runtime.debug.DebugEventSocketProxy;
import org.antlr.runtime.tree.*;

/* COSMAS II Plain Syntax (c2ps).
 * lokale Grammatik für Optionen von #OV(Opts).
 * 12.12.12/FB
 *
 * Input Bsp.: "#OV", "#OV()", "#OV(L)", "#OV(F,%,max)", etc.
 *
 * Opts nimmt eine oder mehrere, durch Kommata getrennte Optionen auf:
 * - Positionsoptionen: L, R, F, FE, FI, X, -.
 * - Ausschließungsoptionen: %, -.
 * - Gruppenbildungsoptionen: min, max, -.
 * Falls keine Optionen eingesetzt werden, kann der Operator #OV eingesetzt werden.
 */

public class c2ps_opOV

{

 public static Tree check(String input, int index)
 	{
	ANTLRStringStream
		ss = new ANTLRStringStream(input);
	c2ps_opOVLexer
		lex = new c2ps_opOVLexer(ss);
	CommonTokenStream tokens = 
  		new CommonTokenStream(lex);
	c2ps_opOVParser 
		g = new c2ps_opOVParser(tokens);
	c2ps_opOVParser.opOV_return
		c2PQReturn = null;

	try 
		{
		c2PQReturn = g.opOV();
		}
	catch (RecognitionException e) 
		{
		e.printStackTrace();
		}

	// AST Tree anzeigen:
	Tree tree = (Tree)c2PQReturn.getTree();
	// System.out.println("opOV: " + tree.toStringTree() );

	return tree;
	}

 /*
  * main: testprogram:
  */

 // TODOO: input "OV()" führt zu unendlichem loop... 19.12.12/FB
 // TODOO: input "#OV(FI,ALL)" -> loop, weil ALL nicht bekannter Token...

 public static void main(String args[]) throws Exception 
  {
   String[]
		input = {"#OV", "#OV()", "#OV(L)", "#OV(FE,min)", "#OV(R,% , max)"};  
	Tree
		tree;

	System.out.println("Tests von #OV-Optionen:\n");

	for(int i=0; i<input.length; i++)
		{
		System.out.println("#OV: input: " + input[i]);
		tree = check(input[i], 0);
		System.out.println("#OV: AST  : " + tree.toStringTree() + "\n");
		}

	System.out.println("Tests von #OV-Optionen: quit.\n");
	System.out.flush();
  } // main

} 

