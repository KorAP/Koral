package de.ids_mannheim.korap.query.serialize.util;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.tree.CommonTree;

import de.ids_mannheim.korap.query.parse.cosmas.c2ps_opPROX;

public class StatusCodes {
	
	// type of an Error CommonToken:
	public final static int typeERROR = 1; 
	
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
	
	// error codes for WF and LEM syntax/semantic errors:
	public final static int ERR_LEM_WILDCARDS		= 350;
	
	
	   /**
		 * buildErrorTree(): 
		 * @param text = part of the query that contains an error.
		 * @param errCode
		 * @param typeDIST
		 * @param pos
		 * @return
		 */
		
		//private static CommonTree buildErrorTree(String text, int errCode, int typeDIST, int pos)
	    
	    public static CommonTree buildErrorTree(String text, int errCode, int pos)
		{
		/*
		 CommonTree
		//errorTree = new CommonTree(new CommonToken(typeDIST, "DIST"));
		errorTree = new CommonTree(new CommonToken(c2ps_opPROX.typeERROR, "Fehlercherchen"));
		*/
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
		
		mess 	 = c2ps_opPROX.getErrMess(errCode, c2ps_opPROX.messLang, text);
		errorMes = new CommonTree(new CommonToken(typeERROR, mess));
		
		// new:
		errorNode.addChild(errorPos);
		errorNode.addChild(errorCode);
		errorNode.addChild(errorMes);
		
		return errorNode;
		
		/* old, no need for errorTree(typeXY).
		errorTree.addChild(errorNode);
		errorNode.addChild(errorPos);
		errorNode.addChild(errorCode);
		errorNode.addChild(errorMes);
		
		return errorTree;
		*/
		}
}