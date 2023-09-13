package de.ids_mannheim.korap.util;

/* general String manipulation functions moved
 * from de.ids_mannheim.de.korap.query.parse.cosmas.c2ps_opREG.java and Cosmas2QueryProcessor.java.
 * 24.10.23/FB
 */

public final class StringUtils {

	private static final boolean DEBUG = false;
	
	/**
	 * replaceIfNotEscaped:
	 * - kind of adhoc alternative to String.replaceAll().
	 * - replaces every occurence of >>"<< in buf IF it isn't escaped by >>\<<.
	 * Notes:
	 * - first intention: replace String.replaceALL() in processOPREG() because
	 *   replaceALL() cannot be used in that special case.
	 * Returns the replaced string.
	 * 25.09.23/FB
	 */
	   
	public static String replaceIfNotEscaped(String buf)
	
	{
	StringBuffer
		sb = new StringBuffer(buf);
	
	for(int i=0; i<sb.length(); i++)
		{
		//System.out.printf("ssb.length=%d ssb=%s.\n",  ssb.length(), ssb);
		if( sb.codePointAt(i) == '"' && (i==0 || sb.codePointBefore(i) != '\\') )
			{
			sb.deleteCharAt(i);
			i--;
			}
		}
	
	return sb.toString();
	
	} // replaceIfNotEscaped

	
    /**
	 * replaceDoubleQuotes:
	 * - kind of adhoc enhanced replacement function for >>"<< for #REG(expr)
	 *   instead of String.replaceAll().
	 * - replaces every occurence of >>"<< in buf that is not escaped by >>\<<.
	 * - If the >>"<< is escaped, the escape char is removed: >>\"<< -> >>"<<.
	 * Notes:
	 * - the converted string is intented to be greped.
	 * E.g.:  
	 * - >>"\"Abend\"-Ticket"<< -> >>"Abend"-Ticket<<.
	 * Returns the replaced string.
	 * 26.09.23/FB
	 */
	   
	public static String replaceDoubleQuotes(String buf)
	
	{
	StringBuffer
		sb = new StringBuffer(buf);
	
	if( DEBUG ) System.out.printf("replaceDoubleQuotes:  input: >>%s<<.\n", buf);
	
	for(int i=0; i<sb.length(); i++)
		{
		//System.out.printf("ssb.length=%d ssb=%s.\n",  ssb.length(), ssb);
		if( sb.codePointAt(i) == '\\' )
			{
			if( i+1 < sb.length() ) 
				{
				if( sb.codePointAt(i+1) == '"') // >>\"<< -> >>"<<.
					sb.deleteCharAt(i);
				else if( sb.codePointAt(i+1) == '\\' ) // >>\\<< unchanged.
					i++; // keep >>\\<< unchanged.
				}
			}
		else if( sb.codePointAt(i) == '"' )
			{
			sb.deleteCharAt(i); // unescaped >>"<< is removed.
			i--;
			}
		}
	
	if( DEBUG ) System.out.printf("replaceDoubleQuotes: output: >>%s<<.\n", sb.toString());
	
	return sb.toString();
	
	} // replaceDoubleQuotes
	
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
	
	public static void encode2DoubleQuoted(StringBuffer sb)
	
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
	
	public static void removeBlanksAtBothSides(StringBuffer sb)
	
	{
	int len;
		
    // remove leading blanks: >>  abc  << -> >>abc  <<:
	while( sb.length() > 0 && sb.charAt(0) == ' ')
		sb.deleteCharAt(0);
	
	// remove trailing blanks: >>abc  << -> >>abc<<:
	while( (len=sb.length()) > 0 && sb.charAt(len-1) == ' ' )
		sb.deleteCharAt(len-1);
	
	} // removeBlanksAtBothSides
	
}
