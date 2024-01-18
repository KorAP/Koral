package de.ids_mannheim.korap.util;

import org.antlr.runtime.*;

/* general String manipulation functions moved
 * from de.ids_mannheim.de.korap.query.parse.cosmas.c2ps_opREG.java and Cosmas2QueryProcessor.java.
 * 24.10.23/FB
 */

public final class C2RecognitionException extends RecognitionException {

	public String mismatchedToken;
	
	public C2RecognitionException(String mismatchedToken)
	
	{
		this.mismatchedToken = mismatchedToken;
		
	} // constructor C2RecognitionException
	
	public String getMismatchedToken()
	{
		return this.mismatchedToken;
	}
	
}
