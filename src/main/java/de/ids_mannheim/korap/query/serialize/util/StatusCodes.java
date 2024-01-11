package de.ids_mannheim.korap.query.serialize.util;

public class StatusCodes {
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
}