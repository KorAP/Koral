package de.ids_mannheim.korap.query.serialize;

import java.util.Map;

import de.ids_mannheim.korap.util.QueryException;

public abstract class AbstractSyntaxTree {

	public abstract Map<String, Object> getRequestMap();

	public abstract void process(String query) throws QueryException;

}
