package de.ids_mannheim.korap.query.parse.cosmas;

import java.io.*;
import org.antlr.runtime.*;
import org.antlr.runtime.debug.DebugEventSocketProxy;
import org.antlr.runtime.tree.*;

/*
 * parses prefixed and suffixed options of a search wordform.
 * E.g. :fi:Hendrix:sa/-pe.
 */

public class c2ps_opWF

{
 /* Arguments:
  * bStrip: true: 'input' contains "wort" -> strip " away -> wort.
  *        false: 'input' contains no " -> nothing to strip.
  * bLem: true: input contains a Lemma; generates tree ^(OPLEM...).
  *       false: input contains a Wordform; generates tree ^(OPWF...).
  * input: may be a single Lemma or Wform or a list of Wforms.
  */

 public static Tree check(String input, boolean bStrip, boolean bLem, int index)
 	{
	if( bStrip )
		input = input.substring(1, input.length()-1);

	if( bLem && input.charAt(0) == '&' )
		{
		input = input.substring(1, input.length());
		//System.out.println("Lemma: strip '&' -> " + input);
		}

	ANTLRStringStream
		ss = new ANTLRStringStream(input);
	c2ps_opWFLexer
		lex = new c2ps_opWFLexer(ss);
	CommonTokenStream tokens = 
  		new CommonTokenStream(lex);
	c2ps_opWFParser 
		g = new c2ps_opWFParser(tokens);
	c2ps_opWFParser.searchWFs_return
		c2PQWFReturn = null;
	c2ps_opWFParser.searchLEM_return
		c2PQLEMReturn = null;

  /*
  System.out.println("check opWF:" + index + ": " + input);
  System.out.flush();
  */
  
	try 
		{
		if( bLem )
			c2PQLEMReturn = g.searchLEM();
		else
			c2PQWFReturn = g.searchWFs();
		}
	catch (RecognitionException e) 
		{
		e.printStackTrace();
		}

	// AST Tree anzeigen:
	Tree tree = bLem ? (Tree)c2PQLEMReturn.getTree() : (Tree)c2PQWFReturn.getTree();
	// System.out.println(bLem? "opLEM: " : "opWF: " + tree.toStringTree() );

	return tree;
	}

	/* Wordform Encoding, e.g. to insert a Wordform into an AST.
	 * a) wf ->  "wf".
	 * b) remove escape char before ':': abc\: -> abc:.
	 * Returns a Tree.
	 */
	public static Tree encode(String wf, int tokenType)

	{
	// b)
   StringBuffer
		sbWF = new StringBuffer(wf);
	
	for(int i=0; i<sbWF.length()-1; i++)
		{
		if( sbWF.charAt(i) == '\\' && sbWF.charAt(i+1) == ':' )
			sbWF.deleteCharAt(i);
		}

	return new CommonTree(new CommonToken(tokenType, "\"" + sbWF.toString() + "\""));
	}

 /*
  * main testprogram:
  */

 public static void main(String args[]) throws Exception 
  {
   String[]
		input = {":fi:Hendrix:sa", ":FiOlDs:été:sa", "&Gitarre", "&Gitarre:sa/-pe",
					" \"Institut für \\:Deutsche\\: Sprache\" ",
					":Fi:der:-sa Wilde:-se Western:/se" };
	Tree
		tree;
	boolean
		bLem;

	System.out.println("Tests von WF und Lemma-Optionen:\n");

	for(int i=0; i<input.length; i++)
		{
		bLem = input[i].charAt(0) == '&' ? true : false; 

		System.out.println(bLem? "LEM: " : "WF: " + "input: " + input[i]);

		if( bLem )
			tree = check(input[i], false, true, 0); // bStrip=false, bLem=true;
		else
			tree = check(input[i], false, false, 0); // bStrip=false, bLem=false.
			
		System.out.println(bLem? "LEM: " : "WF: " + "AST  : " + tree.toStringTree() + "\n");
		}

  } // main

} 
