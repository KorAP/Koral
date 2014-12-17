package de.ids_mannheim.korap.query.serialize.util;

import java.util.ArrayList;
import java.util.AbstractMap.SimpleEntry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ids_mannheim.korap.query.cosmas2.IErrorReporter;
import de.ids_mannheim.korap.query.serialize.QueryUtils;

/**
 * Custom descriptive error listener for Antlr3 grammars. Requires interface IErrorReporter to be present in 
 * grammar destination (generated source directory).
 * @author Joachim Bingel (bingel@ids-mannheim.de)
 *
 */
public class Antlr3DescriptiveErrorListener implements IErrorReporter {

	private String query;
	private String offendingSymbol;
	private String expected;
	private int charPosition;

	public Antlr3DescriptiveErrorListener(String query) {
		this.query = query;
	};

	@Override
	public void reportError(String error) {
		String charPositionStr = null;
		String offendingSymbol = null;
		String expected = null;
		Pattern p = Pattern.compile("line \\d+:(\\d+).* '(.+?)' expecting (.+)");
		Matcher m = p.matcher(error);
		if (m.find()) {
			charPositionStr = m.group(1);
			offendingSymbol = m.group(2);
			expected = m.group(3);
		}
		if (charPositionStr != null)
			this.charPosition = Integer.parseInt(charPositionStr);
		if (offendingSymbol != null)
			this.offendingSymbol = offendingSymbol;
		if (expected != null)
			this.expected = expected;
	}

	public ArrayList<Object> generateFullErrorMsg() {
		ArrayList<Object> errorSpecs = new ArrayList<Object>();
		String msg = getDetailedErrorMessage(); 
		errorSpecs.add(StatusCodes.MALFORMED_QUERY);
		errorSpecs.add(msg);
		errorSpecs.add(getCharPosition());
		return errorSpecs;
	}

	private String getDetailedErrorMessage() {
		// default message, in case no detailed info is available;
		String msg = "Malformed query. Could not parse."; 
		char offendingSymbol = query.charAt(0);
		if (query.length() > charPosition) offendingSymbol = query.charAt(charPosition);
		msg = "Failing to parse at symbol: '"+offendingSymbol+"'";
		if (expected != null) {
			if (expected.equals("EOF") || expected.equals("<EOF>")) {
				msg += " Expected end of query.";
			} else {
				msg += " Expected '"+expected+"'";
			}
		}
		// check for unbalanced parantheses
		SimpleEntry<String, Integer> unbalanced = QueryUtils.checkUnbalancedPars(query); 
		if (unbalanced != null) {
			msg = unbalanced.getKey();
			charPosition = unbalanced.getValue();
		}

		return msg;
	}

	public int getCharPosition() {
		return charPosition;
	}


}
