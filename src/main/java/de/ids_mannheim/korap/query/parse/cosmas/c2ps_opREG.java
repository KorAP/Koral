package de.ids_mannheim.korap.query.parse.cosmas;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

import de.ids_mannheim.korap.query.serialize.util.Antlr3DescriptiveErrorListener;

/*
 * 1. transforms and encodes a regular COSMAS II like expression #REG(regexpr)
 *    into a AST tree -> encode().
 * 2. transforms tree into the corresponding Koral:token/Koral:term, like:
 *    e.g. #REG(abc[']?s) ->
 *     {
 *      "@type": "koral:term",
 *      "match": "match:eq",
 *      "type" : "type:regex",
 *      "key"  : "abc[']?s",
 *      "layer": "orth"
 *     }...
 *
 * - see doc: http://korap.github.io/Koral/
 * - generation of koral:term -> processOPREG().
 * 06.09.23/FB
 */

public class c2ps_opREG

{

	private static boolean DEBUG = false;

	/* encode2DoubleQuoted:
	 * transforms an unquoted string into an double quoted string
	 * and escapes >>"<< and >>/<<.
	 * E.g. >>.."..<<  -> >>"..\".."<<.
	 * E.g. >>..\..<<  -> >>"..\\.."<<.
	 * E.g. >>..\"..<< -> >>"..\\\".."<<, etc.
	 * 
	 * escaping >>"<< and >>\<<, because they will be
	 * enclosed in >>"..."<<.
	 * >>"<<   -> >>\"<<
	 * >>\<<   -> >>\\<<
	 * 
	 * 28.09.23/FB
	 * 
	 * E.g. from previous, olddated version:
	 * \\" -> \\\"
	 * \\\" -> \\\"
	 */
	
	static void encode2DoubleQuoted(StringBuffer sb)
	{
	if( DEBUG ) System.out.printf("encode2DoubleQuoted:  input = >>%s<<.\n", sb.toString());
	
	for(int i=0; i<sb.length()-1; i++)
    	{	
		if( sb.charAt(i) == '\\' )
    		{
			sb.insert(i,  '\\');
			i++;
			}	
    	else if( sb.charAt(i) == '"')
        	{ 
        	sb.insert(i, '\\');	
        	i++; 
        	}
    	}

	// enclose reg. expr. with "..." before returning:
	sb.insert(0, '"');
	sb.append('"');
	
	if( DEBUG ) System.out.printf("encode2DoubleQuoted: output = >>%s<<.\n", sb.toString());
	}  // encode2DoubleQuoted
	
	/*
	 * removeBlanksAtBothSides
	 * 28.09.23/FB
	 */
	
	static void removeBlanksAtBothSides(StringBuffer sb)
	
	{
	int len;
		
    // remove leading blanks: >>  abc  << -> >>abc  <<:
	while( sb.length() > 0 && sb.charAt(0) == ' ')
		sb.deleteCharAt(0);
	
	// remove trailing blanks: >>abc  << -> >>abc<<:
	while( (len=sb.length()) > 0 && sb.charAt(len-1) == ' ' )
		sb.deleteCharAt(len-1);
	
	} // removeBlanksAtBothSides
	
	/* 
	 * encode():
	 * 
	 * input = e.g. "#REG('abc(d|e)*')" -> return AST = (OPREG abc(d|e)*):
	 * 
	 * Returned String: no enclosing "..." needed, so no escaping of " nor \ needed.
	 * 06.09.23/FB
	 */
	public static Tree encode (String input, int tokenType) 
	    
	{
    if( DEBUG )
    	{
    	System.out.printf("opREG.encode: input = >>%s<<, token type=%d.\n", input, tokenType); 
    	System.out.flush();
    	}
    
    if( input.substring(0, 5).compareToIgnoreCase("#REG(") != 0 || input.charAt(input.length()-1) != ')' )
    	{
    	// error: '#REG(' and ')' not found: return input unchanged.
        if( DEBUG ) System.out.printf("opREG.encode: unexpected input = >>%s<<: nothing encoded!\n", input);
    	return new CommonTree(new CommonToken(tokenType, input));
    	}
        

    StringBuffer sb = new StringBuffer(input.substring(5));
    sb.deleteCharAt(sb.length()-1);
        
	// #REG("a"), #REG(a), #REG('a') -> >>a<<.
    // enclosing ".." are appended at the end of this function.
    // a. remove blanks around ".." and '..',
    //    e.g. a. #REG( ' abc ' ) -> #REG(' abc ').
    
    removeBlanksAtBothSides(sb);
    
	if( sb.charAt(0) == '\'' || sb.charAt(0) == '"')
		{
		// remove pairwise at both ends.
		sb.deleteCharAt(0);
		if( sb.charAt(sb.length()-1) == '\'' || sb.charAt(sb.length()-1) == '"' )
			sb.deleteCharAt(sb.length()-1);
		}
    
	// b. remove blanks inside '..' or "..",
    //    E.g. #REG(' abc ') -> #REG('abc'):
	
	removeBlanksAtBothSides(sb);
    
	/* unescape >>'<<, >>"<< and >>\<<.
	 * e.g. #REG('that\'s') -> "that\'s" -> >>that's<<.
	 */

	for(int i=0; i<sb.length()-1; i++)
		{
		if( sb.charAt(i) == '\\' && 
			(sb.charAt(i+1) == '\'' || sb.charAt(i+1) == '"' || sb.charAt(i+1) == '\\' ))
			sb.deleteCharAt(i);
		}
	
	/* old version:
	for(int i=0; i<sb.length()-1; i++)
		{
		if( sb.charAt(i) == '\\' && sb.charAt(i+1) == '\'' )
			sb.deleteCharAt(i);
		}
	*/
	
	/* old version:
	 * encode2DoubleQuoted(sb);
	 */
	
	if( DEBUG ) 
    	System.out.printf("opREG.encode: encoded = >>%s<<.\n", sb.toString());
	        
    return new CommonTree(new CommonToken(tokenType, sb.toString()));
     
	} // encode

	/*
	 * printTokens:
	 * Notes:
	 * - must build a separate CommonTokenStream here, because
	 *   tokens.fill() will consume all tokens.
	 * - prints to stdout list of tokens from lexer.
	 * - mainly for debugging.
	 * 14.09.23/FB
	 * 
	 */
	
	private static void printTokens(String query, Antlr3DescriptiveErrorListener errorListener)
	
		{
	    ANTLRStringStream 
	    	ss = new ANTLRStringStream(query);
	    c2psLexer 
	    	lex = new c2psLexer(ss);
	    org.antlr.runtime.CommonTokenStream 
	    	tokens = new org.antlr.runtime.CommonTokenStream(lex); // v3
	    
        lex.setErrorReporter(errorListener);

	    // get all tokens from lexer:
		tokens.fill();
	    
		System.out.printf("opREG.check: no. of tokens = %d.\n",  tokens.size()); 
	    for(int i=0; i<tokens.size(); i++)
	        	System.out.printf("opREG.check: token[%2d] = %s.\n",  i, tokens.get(i).getText()); 
	    
		} // printTokens
	
		/* check:
		 * Notes:
		 * - must build a separate CommonTokenStream here, because
		 *   tokens.fill() will consume all tokens.
		 */
	
	   public static Tree check (String query, int index) 
	   
	   {
	        ANTLRStringStream 
	        	ss = new ANTLRStringStream(query);
	        c2psLexer 
	        	lex = new c2psLexer(ss);
	        org.antlr.runtime.CommonTokenStream 
	        	tokens = new org.antlr.runtime.CommonTokenStream(lex); // v3
	        c2psParser 
	        	g = new c2psParser(tokens);
	        Tree 
	        	tree = null;
           Antlr3DescriptiveErrorListener errorListener =
                   new Antlr3DescriptiveErrorListener(query);

           // Use custom error reporters for lex for use in printTokens(lex), or programm will break
           // by broken input, e.g. >>#REG(\" a"s\")<<.
           lex.setErrorReporter(errorListener);
           ((c2psParser) g).setErrorReporter(errorListener);

           if( DEBUG )
		       {
		        //System.out.format("opREG.check: input='%s', index=%d.\n", query, index); 
		        printTokens(query, errorListener);
		        System.out.flush();
		       }


           try {
               c2psParser.c2ps_query_return 
               		c2Return = ((c2psParser) g).c2ps_query(); // statt t().
               
               // AST Tree anzeigen:
               tree = (Tree) c2Return.getTree();
               //if (DEBUG) 
               // 	System.out.printf("opREG.check: tree = '%s'.\n", tree.toStringTree());
           	}
           catch (RecognitionException e) {
               System.err.printf("c2po_opREG.check: Recognition Exception!\n");
           	}

	     return tree;
	    } // check
	
	  /*
	   * replaceIfNotEscaped:
	   * - kind of adhoc alternative to String.replaceAll().
	   * - replaces every occurence of >>"<< in buf IF it is'nt escaped by >>\<<.
	   * Returns the replaced string.
	   * 25.09.23/FB
	   */
	   
	private static String replaceIfNotEscaped(String buf)
	
	{
	StringBuffer
		sb = new StringBuffer(buf);
	
	// Ersatz für replaceALL() für #REG:
	
	for(int i=1; i<sb.length(); i++)
		{
		//System.out.printf("ssb.length=%d ssb=%s.\n",  ssb.length(), ssb);
		if( sb.codePointAt(i) == '"' && sb.codePointBefore(i) != '\\')
			{
			sb.deleteCharAt(i);
			i--;
			}
		}
	
	return sb.toString();
	
	} // replaceIfNotEscaped
	
	/** 
	 * main
	 */
	   
    public static void main (String args[]) throws Exception 
    
    {
    	String input[] = {	"#REG(abc)", 
    						"#REG(def's)", 
    						"#REG(  def's  )", 		// all blanks should be removed.
    						"#REG( ' def\\'s ' )", 	// same
    						"#REG( \" def's \" )", // same
    						"#REG(abc[\"]ef)", 
    						"#REG('abc')", 			// ' fehlt: generates Syntax Error .
    						"#REG('abc\')",			// User input = #REG('abc\') : OK, nothing escaped.
    						"#REG('abc\\')",		// User input = #REG('abc\') : OK, same behavior: \\ == \.
    						"#REG((a|b))",			// broken input, should use ".." or '..'.
    						"#REG('(a|b)')",		// OK.
    						"#REG(\"(a|b)\")",		// OK.
    						"#REG(^[A-Z]+abc[\']*ung$)",
    						"#REG('ab(cd|ef)*')", 
    						"#REG('abc(def|g)*[)(]')",
    						"#REG(\"abc(def|g)*[)(]\")",
							"#REG('abc[\"]')",		// User input = #REG('abc["]') : OK, needs escape => #REG("...\"...")
							"#REG(\"abc[\\\"]\")",	// User input = #REG("abc["]") : broken because of 2nd " -> syntax error.
							"#REG(\"abc[\\\"]\")",	// User input = #REG("abc[\"]"): OK, already escaped by user => #REG("...\"...")
							"#REG(\"abc[\\\\\"]\")"	// User input = #REG("abc[\\"]") : broken. with escaped "    => #REG("...\"...")
							};
    	Tree tree;
   
    	for (int i = 0; i < input.length; i++) 
        	{
            System.out.printf("c2ps_opREG: Parsing input %02d: >>%s<<\n", i, input[i]);
            tree = check(input[i], 0);
            System.out.printf("c2ps_opREG: tree %02d: >>%s<<.\n\n", i, tree.toStringTree());
            }

    	
    } // main

}
