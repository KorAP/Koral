package de.ids_mannheim.korap.query.serialize.util;

/**
 * Define a custom exception for errors in parsing into and
 * serializing KoralQuery.
 * 
 * @author margaretha
 *
 */
public class KoralException extends Exception {

    private static final long serialVersionUID = 5463242042200109417L;
    private int statusCode;


    public KoralException (int code, String message) {
        super(message);
        this.statusCode = code;
    }


    public KoralException (int code, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = code;
    }


    public int getStatusCode () {
        return statusCode;
    }


    public void setStatusCode (int statusCode) {
        this.statusCode = statusCode;
    }


}
