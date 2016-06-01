package de.ids_mannheim.korap.query.parse.fcsql;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.ids_mannheim.korap.query.serialize.FCSQLQueryProcessor;
import de.ids_mannheim.korap.query.serialize.util.StatusCodes;
import eu.clarin.sru.server.fcs.parser.Operator;
import eu.clarin.sru.server.fcs.parser.RegexFlag;

public class KoralTerm {

	private String layer;
	private String foundry;
	private String operator;
	private String queryTerm;
	private boolean caseSensitive = true;
	private boolean invalid = false;
	
	
	public String getLayer() {
		return layer;
	}
	public void setLayer(String layer) {
		this.layer = layer;
	}
	public String getFoundry() {
		return foundry;
	}
	public void setFoundry(String foundry) {
		this.foundry = foundry;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public String getQueryTerm() {
		return queryTerm;
	}
	public void setQueryTerm(String queryTerm) {
		this.queryTerm = queryTerm;
	}
	public boolean isCaseSensitive() {
		return caseSensitive;
	}
	public void setCaseSensitive(boolean isCaseSensitive) {
		this.caseSensitive = isCaseSensitive;
	}
	public boolean isInvalid() {
		return invalid;
	}
	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}
	
}
