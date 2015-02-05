package de.ids_mannheim.korap.query.parse.cosmas;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

/* COSMAS II Plain Syntax (c2ps).
 * lokale Grammatik für Optionen von #IN(Opts).
 * 12.12.12/FB
 *
 * check(input): Input Bsp.: "#IN", "#IN()", "#IN(L)", "#IN(L,min,%)", etc.
 *
 * Opts nimmt eine oder mehrere, durch Kommata getrennte Optionen auf:
 * - Bereichsoptionen: ALL, HIT, -.
 * - Positionsoptionen: L, R, F, FE, FI, N, -.
 * - Ausschließungsoptionen: %, -.
 * - Gruppenbildungsoptionen: min, max, -.
 * Für die Nutzung ohne Optionen steht Operator #IN zur Verfügung.
 */

public class c2ps_opIN

{

 public static Tree check(String input, int index)
 	{
	ANTLRStringStream
		ss = new ANTLRStringStream(input);
	c2ps_opINLexer
		lex = new c2ps_opINLexer(ss);
	CommonTokenStream tokens = 
  		new CommonTokenStream(lex);
	c2ps_opINParser 
		g = new c2ps_opINParser(tokens);
	c2ps_opINParser.opIN_return
		c2PQReturn = null;

  /*
  System.out.println("check opIN:" + index + ": " + input);
  System.out.flush();
  */

	try 
		{
		c2PQReturn = g.opIN();
		}
	catch (RecognitionException e) 
		{
		e.printStackTrace();
		}

	// AST Tree anzeigen:
	Tree tree = (Tree)c2PQReturn.getTree();
	// System.out.println("opIN: " + tree.toStringTree() );

	return tree;
	}

 /*
  * main: testprogram:
  */

 public static void main(String args[]) throws Exception 
  {
   String[]
		input = {"#IN", "#IN()", "#IN(L)", "#IN(FE,min)", "#IN(R,%,max)", "#IN(FI,ALL)", 
					"#IN(FE,ALL,%,MIN)"};  
	Tree
		tree;

	System.out.println("Tests von #IN-Optionen:\n");

	for(int i=0; i<input.length; i++)
		{
		tree = check(input[i], 0);
		System.out.println("#IN: input: " + input[i]);
		System.out.println("#IN: AST  : " + tree.toStringTree() + "\n");
		}

  } // main

} 
