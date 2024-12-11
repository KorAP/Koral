package de.ids_mannheim.korap.query.serialize.util;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.tree.CommonTree;

import de.ids_mannheim.korap.query.parse.cosmas.c2ps_opPROX;

public class StatusCodes {
	
	// type of an Error CommonToken:
	public final static int typeERROR = 1; 
	
	// error codes:
    public final static int NO_QUERY = 301;
    public final static int MALFORMED_QUERY = 302;
    public final static int DEPRECATED_QUERY_ELEMENT = 303;
    public final static int INVALID_CLASS_REFERENCE = 304;
    public final static int INCOMPATIBLE_OPERATOR_AND_OPERAND = 305;
    public final static int UNKNOWN_QUERY_ELEMENT = 306;
	public final static int UNKNOWN_QUERY_LANGUAGE = 307;
    public final static int UNBOUND_ANNIS_RELATION = 308;
	public final static int MISSING_VERSION = 309;
	public final static int UNSUPPORTED_VERSION = 310;
	public final static int QUERY_TOO_COMPLEX = 311;
	public final static int UNKNOWN_QUERY_ERROR = 399;
	public final static int SERIALIZATION_FAILED = 300;
	
	// error codes for PROX syntax errors:
	final public static int ERR_PROX_UNKNOWN 		= 320;
	public final static int ERR_PROX_MEAS_NULL 		= 321;
	public final static int ERR_PROX_MEAS_TOOGREAT 	= 322;
	public final static int ERR_PROX_VAL_NULL 		= 323;
	public final static int ERR_PROX_VAL_TOOGREAT 	= 324;
	public final static int ERR_PROX_DIR_TOOGREAT 	= 325;
	public final static int ERR_PROX_WRONG_CHARS	= 326;
	
	// error codes for WF and LEM syntax errors:
	public final static int ERR_LEM_WILDCARDS		= 350;
	
	// constants for message languages
	
	public static final int MLANG_ENGLISH = 0;
	public static final int MLANG_GERMAN  = 1;
	
	public static int messLang = MLANG_GERMAN; // default.
	
	   /**
		 * buildErrorTree(): 
		 * @param text = part of the query that contains an error.
		 * @param errCode
		 * @param pos = position of the expression where the error occurs.
		 * @return CommonTree (ERROR pos errCode errMess).
		 */
		
	public static CommonTree buildErrorTree(String text, int errCode, int pos)
		{
		CommonTree
			errorNode = new CommonTree(new CommonToken(typeERROR, "ERROR"));
		CommonTree
			errorPos  = new CommonTree(new CommonToken(typeERROR, String.valueOf(pos)));
		CommonTree
			errorCode = new CommonTree(new CommonToken(typeERROR, String.valueOf(errCode)));
		CommonTree
			errorMes;
		String
			mess;
		
		mess 	 = getErrMess(errCode, messLang, text);
		errorMes = new CommonTree(new CommonToken(typeERROR, mess));
		
		// new:
		errorNode.addChild(errorPos);
		errorNode.addChild(errorCode);
		errorNode.addChild(errorMes);
		
		return errorNode;
		}
		
	private static String getErrMessEN(int errCode, String text)
		
		{
		switch( errCode )
			{
		case ERR_PROX_MEAS_NULL:
			return String.format("Proximity operator at '%s': one of the following prox. types is missing: w,s,p!", text);

		case ERR_PROX_MEAS_TOOGREAT:
			return String.format("Proximity operator at '%s': Please, specify only 1 of the following prox. types: w,s,p! " +
								 "It is possible to specify several at once by separating them with a ','. E.g.: ' /+w2,s2,p0 '.", text);
			
		case ERR_PROX_VAL_NULL:
			return String.format("Proximity operator at '%s': please specify a numerical value for the distance. E.g. ' /+w5 '.", text);
			
		case ERR_PROX_VAL_TOOGREAT:
			return String.format("Proximity operator at '%s': please specify only 1 distance value. E.g. ' /+w5 '.", text);
			
		case ERR_PROX_DIR_TOOGREAT:
			return String.format("Proximity operator at '%s': please specify either '+' or '-' or none of them for the direction.", text);
			
		case ERR_PROX_WRONG_CHARS:
			return String.format("Proximity operator at '%s': unknown proximity options!", text);

		case ERR_LEM_WILDCARDS:
			return String.format("Lemma operator at '%s': wildcards (?*+) are not allowed inside a lemma.", text);
			
		default:
			return String.format("Proximity operator at '%s': unknown error. The correct syntax looks like this: E.g. ' /+w2 ' or ' /w10,s0 '.", text);
			}	
		}
		
	private static String getErrMessGE(int errCode, String text)

		{
		switch( errCode )
			{
		case ERR_PROX_MEAS_NULL:
			return String.format("Abstandsoperator an der Stelle '%s': es fehlt eine der folgenden Angaben: w,s,p!", text);
			
		case ERR_PROX_MEAS_TOOGREAT:
			return String.format("Abstandsoperator an der Stelle '%s': Bitte nur 1 der folgenden Angaben einsetzen: w,s,p! " +
								 "Falls Mehrfachangabe erwünscht, müssen diese durch Kommata getrennt werden (z.B.: ' /+w2,s2,p0 ').", text);
			
		case ERR_PROX_VAL_NULL:
			return String.format("Abstandsoperator an der Stelle '%s': Bitte einen numerischen Wert einsetzen (z.B. ' /+w5 ')! ", text);
			
		case ERR_PROX_VAL_TOOGREAT:
			return String.format("Abstandsoperator an der Stelle '%s': Bitte nur 1 numerischen Wert einsetzen (z.B. ' /+w5 ')! ", text);
			
		case ERR_PROX_DIR_TOOGREAT:
			return String.format("Abstandsoperator an der Stelle '%s': Bitte nur 1 Angabe '+' oder '-' oder keine! ", text);
			
		case ERR_PROX_WRONG_CHARS:
			return String.format("Abstandsoperator an der Stelle '%s': unbekannte Abstandsoption(en)!", text);
			
		case ERR_LEM_WILDCARDS:
			return String.format("Lemma-Suchbegriff an der Stelle '%s': Platzhalter (?*+) können im gesuchten Lemma nicht eingesetzt werden.", text);
			
		default:
			return String.format("Abstandsoperator an der Stelle '%s': unbekannter Fehler. Korrekte Syntax z.B.: ' /+w2 ' oder ' /w10,s0 '.", text);
			}
		}
	
	/* getErrMess:
	 * - returns error message depending of messLang.
	 * 12.12.24/FB
	 *  - moved to StatusCodes.java.
	 */
	
	public static String getErrMess(int errCode, int messLang, String text)
		
		{
		if( messLang == MLANG_GERMAN )
			return getErrMessGE(errCode, text);
		else
			return getErrMessEN(errCode, text);	
		}

}