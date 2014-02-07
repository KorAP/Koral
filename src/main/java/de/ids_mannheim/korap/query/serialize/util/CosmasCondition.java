package de.ids_mannheim.korap.query.serialize.util;

import org.antlr.runtime.tree.Tree;

public class CosmasCondition {

	public boolean negated = false;
	public String elem = "";
	public String position = "";
	
	public CosmasCondition(Tree cond) {
		String nodeString = cond.toStringTree();
		if (nodeString.startsWith("-")) {
			negated = true;
			nodeString = nodeString.substring(1);
		} else if (nodeString.startsWith("+")) {
			nodeString = nodeString.substring(1);
		}
		
		elem = nodeString.substring(0, 1);
		nodeString = nodeString.substring(1);
		
		position = nodeString.equals("a") ? "startswith" : "endswith";
	}
}
