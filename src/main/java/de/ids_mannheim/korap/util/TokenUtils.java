package de.ids_mannheim.korap.util;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Token;

/* general String manipulation functions moved
 * from de.ids_mannheim.de.korap.query.parse.cosmas.c2ps_opREG.java and Cosmas2QueryProcessor.java.
 * 24.10.23/FB
 */

public class TokenUtils {

	//private static final boolean showDebug = false;
	
	/**
	 * printLexerTokens
	 * - prints the tokens recognized by a lexer. 
	 * 11.06.26/FB
	 */
	   
	public static void printLexerTokens(CommonTokenStream tokens, String prefix )
	
	{
	if( tokens == null )
		{
		System.out.printf("%s: Tokens: null (empty)!\n", prefix);
		return;
		}
	
	tokens.fill();
	
	if( tokens.size() == 0 )
		{
		System.out.printf("%s: Tokens: empty!\n", prefix);
		return;
		}
	
	for( Token token : tokens.getTokens())
		System.out.printf("%s: Token: '%s'.\n", prefix, token.toString());
    }
	
}
