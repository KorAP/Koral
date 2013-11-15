package de.ids_mannheim.korap.query.serialize;

import de.ids_mannheim.korap.query.poliqarp.PoliqarpParser;
import de.ids_mannheim.korap.query.poliqarp.PoliqarpLexer;
import de.ids_mannheim.korap.query.cosmas2.*;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.v4.runtime.tree.Tree;
import org.antlr.v4.runtime.*;
import java.lang.reflect.Method;

	public class QueryParser {
	  // New lexer object
	  static Lexer poliqarpLexer     = new PoliqarpLexer((CharStream)null);
	  static c2psLexer cosmasLexer   = new c2psLexer();
	  
	  static PoliqarpParser poliqarpParser = null;
	  static c2psParser cosmasParser = null;

	  /**
	   * Parse a regex and return the generated tree string
	   */
	  public static ParserRuleContext parse (String ql, String p) {
		  if (ql == "poliqarp") {
			  return parsePoliqarpQuery(p);
		  } else if (ql == "cosmas") {
			  return (ParserRuleContext) parseCosmasQuery(p);
		  } else {
			  throw new IllegalArgumentException( "Please specify correct QL");
		  }
	  }
	  
	  private static Tree parseCosmasQuery(String p) {
		  Tree tree = null;
		  ANTLRStringStream
			ss = new ANTLRStringStream(p);
		  c2psLexer
			lex = new c2psLexer(ss);
		  org.antlr.runtime.CommonTokenStream tokens =   //v3
	  		new org.antlr.runtime.CommonTokenStream(lex);
		  cosmasParser = new c2psParser(tokens);
		  c2psParser.c2ps_query_return
			c2Return = null;
		  
		  
		  
		  try 
			{
			c2Return = cosmasParser.c2ps_query();  // statt t().
			}
		  catch (RecognitionException e) 
			{
			e.printStackTrace();
			}

		  // AST Tree anzeigen:
		  tree = (Tree)c2Return.getTree();
		  return tree;
	}

	private static ParserRuleContext parsePoliqarpQuery (String p) {
	    ParserRuleContext tree = null;
	    // Like p. 111
	    try {

	      // Tokenize input data
	      ANTLRInputStream input = new ANTLRInputStream(p);
	      poliqarpLexer.setInputStream(input);
	      CommonTokenStream tokens = new CommonTokenStream(poliqarpLexer);
	      poliqarpParser = new PoliqarpParser(tokens);

	      // Don't throw out erroneous stuff
	      poliqarpParser.setErrorHandler(new BailErrorStrategy());
	      poliqarpParser.removeErrorListeners();

	      // Get starting rule from parser
	      Method startRule = PoliqarpParser.class.getMethod("request");
	      tree = (ParserRuleContext) startRule.invoke(poliqarpParser, (Object[])null);
	    }

	    // Some things went wrong ...
	    catch (Exception e) {
	      System.err.println( e.getMessage() );
	    }

	    // Return the generated tree as a string
	    return tree;
	  }
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		 * for testing...
		 */
		String[] queries = new String[] {
//				"[orth=korpus][base=korpus]",
//				"korpus [] language",
//				"[orth=\"bez.?\"/i] ",
//				"[orth=Przyszedł/i]",
//				"[orth=się][][][base=bać]",
//				"[orth=Abc]{2,4}",
//				"[orth=się][pos!=interp]{,5}[base=bać]|[base=bać][base=\"on|ja|ty|my|wy\"]?[orth=się]",
//				"\"(la){3,}\"/x ",
//				"[orth=korpus]+[pos=n]",
//				"[orth=korpus]+[pos=n] within s",
//				"[base=on & orth=ja] ",
//				"[base=\"ja|on\"] ",
//				"[orth=ja]{2,4}",
//				"[orth=ja]{2,}",
//				"[orth=ja]{,4}",
//				"[orth=ja]+",
//				"ja",
//				"ja ne",
//				"[base=in]",
//				"([orth=foo][base=bar])*",
//				"[orth=foo][base!=\"bar*\"]",
//				"[cas==nom/xi]",
//				"[base=foo|base=bar]"
				"&Word"
		};
		
		for (String q : queries) {
			try {
				System.out.println(q);
				System.out.println(parseCosmasQuery(q));
//				System.out.println(parsePoliqarpQuery(q).toStringTree(poliqarpParser));
//				System.out.println(parsePoliqarpQuery(q).getChild(0).toStringTree(poliqarpParser));
				System.out.println();
			} catch (NullPointerException npe) {
				System.out.println("null\n");
			}
			
		}
	}

}
