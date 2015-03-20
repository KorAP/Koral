package de.ids_mannheim.korap.query.serialize.util;

import java.util.ArrayList;
import java.util.AbstractMap.SimpleEntry;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import de.ids_mannheim.korap.query.serialize.QueryUtils;

public class Antlr4DescriptiveErrorListener extends BaseErrorListener {

    String query;
    String message;
    int line;
    int charPosition;


    public Antlr4DescriptiveErrorListener (String query) {
        this.query = query;
    };


    @Override
    public void syntaxError (Recognizer<?, ?> recognizer,
            Object offendingSymbol, int line, int charPositionInLine,
            String msg, RecognitionException e) {
        this.message = msg;
        this.line = line;
        this.charPosition = charPositionInLine;
    }


    public String getMessage () {
        return message;
    }


    public int getLine () {
        return line;
    }


    public int getCharPosition () {
        return charPosition;
    }


    public ArrayList<Object> generateFullErrorMsg () {
        ArrayList<Object> errorSpecs = new ArrayList<Object>();
        String msg = getDetailedErrorMessage();
        errorSpecs.add(StatusCodes.MALFORMED_QUERY);
        errorSpecs.add(msg);
        errorSpecs.add(getCharPosition());
        return errorSpecs;
    }


    private String getDetailedErrorMessage () {
        // default message, in case no detailed info is available;
        String msg = "Malformed query. Could not parse.";
        char offendingSymbol = query.charAt(0);
        if (query.length() > charPosition)
            offendingSymbol = query.charAt(charPosition);
        msg = "Failing to parse at symbol: '" + offendingSymbol + "'";
        // check for unbalanced parantheses
        SimpleEntry<String, Integer> unbalanced = QueryUtils
                .checkUnbalancedPars(query);
        if (unbalanced != null) {
            msg = unbalanced.getKey();
            charPosition = unbalanced.getValue();
        }
        // check if more more arguments expected before closing operator
        if (String.valueOf(offendingSymbol).equals(")")) {
            msg = "Early closing parenthesis. Possibly lacking arguments for operator.";
        }
        return msg;
    }

}