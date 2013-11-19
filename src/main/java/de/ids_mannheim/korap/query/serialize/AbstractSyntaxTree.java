package de.ids_mannheim.korap.query.serialize;

import java.util.Map;

public abstract class AbstractSyntaxTree {

	public abstract Map<String, Object> getRequestMap();

	public abstract void process(String query);

}
