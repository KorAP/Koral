package de.ids_mannheim.korap.query.serialize;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.runtime.tree.Tree;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang.StringUtils;

import de.ids_mannheim.korap.util.QueryException;

/**
 * @author hanl
 * @date 10/12/2013
 */
public class QueryUtils {

	/**
	 * Returns the category (or 'label') of the root of a ParseTree (ANTLR 4).
	 * @param node
	 * @return
	 */
	public static String getNodeCat(ParseTree node) {
		String nodeCat = node.toStringTree(PoliqarpPlusTree.poliqarpParser);
		Pattern p = Pattern.compile("\\((.*?)\\s"); // from opening parenthesis to 1st whitespace
		Matcher m = p.matcher(node.toStringTree(PoliqarpPlusTree.poliqarpParser));
		if (m.find()) {
			nodeCat = m.group(1);
		} 
		return nodeCat;
	}
	
	/**
	 * Returns the category (or 'label') of the root of a ParseTree (ANTLR 3).
	 * @param node
	 * @return
	 */
	public static String getNodeCat(Tree node) {
		String nodeCat = node.toStringTree();
		Pattern p = Pattern.compile("\\((.*?)\\s"); // from opening parenthesis to 1st whitespace
		Matcher m = p.matcher(node.toStringTree());
		if (m.find()) {
			nodeCat = m.group(1);
		} 
		return nodeCat;
	}
	
	
	/**
	 * Tests whether a certain node has a child by a certain name
	 * @param node The parent node.
	 * @param childCat The category of the potential child.
	 * @return true iff one or more children belong to the specified category
	 */
	public static boolean hasChild(ParseTree node, String childCat) {
		for (int i=0; i<node.getChildCount(); i++) {
			if (getNodeCat(node.getChild(i)).equals(childCat)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean hasDescendant(ParseTree node, String childCat) {
		for (int i=0; i<node.getChildCount(); i++) {
			ParseTree child = node.getChild(i);
			if (getNodeCat(child).equals(childCat)) {
				return true;
			}
			if (hasDescendant(child, childCat)) {
				return true;
			}
		}
		return false;
	}
	
	public static List<Tree> getChildrenWithCat(Tree node, String nodeCat) {
		ArrayList<Tree> children = new ArrayList<Tree>();
		for (int i=0; i<node.getChildCount(); i++) {
			if (getNodeCat(node.getChild(i)).equals(nodeCat)) {
				children.add(node.getChild(i));
			}
		}
		return children;
	}
	
	public static List<ParseTree> getChildrenWithCat(ParseTree node, String nodeCat) {
		ArrayList<ParseTree> children = new ArrayList<ParseTree>();
		for (int i=0; i<node.getChildCount(); i++) {
			if (getNodeCat(node.getChild(i)).equals(nodeCat)) {
				children.add(node.getChild(i));
			}
		}
		return children;
	}
	
	public static Tree getFirstChildWithCat(Tree node, String nodeCat) {
		for (int i=0; i<node.getChildCount(); i++) {
			if (getNodeCat(node.getChild(i)).equals(nodeCat)) {
				return node.getChild(i);
			}
		}
		return null;
	}
	
	public static ParseTree getFirstChildWithCat(ParseTree node, String nodeCat) {
		for (int i=0; i<node.getChildCount(); i++) {
			if (getNodeCat(node.getChild(i)).equals(nodeCat)) {
				return node.getChild(i);
			}
		}
		return null;
	}
	
	public static void checkUnbalancedPars(String q) throws QueryException {
		int openingPars = StringUtils.countMatches(q, "(");
		int closingPars = StringUtils.countMatches(q, ")");
		int openingBrkts = StringUtils.countMatches(q, "[");
		int closingBrkts = StringUtils.countMatches(q, "]");
		int openingBrcs = StringUtils.countMatches(q, "{");
		int closingBrcs = StringUtils.countMatches(q, "}");
		if (openingPars != closingPars) throw new QueryException(
				"Your query string contains an unbalanced number of parantheses.");
		if (openingBrkts != closingBrkts) throw new QueryException(
				"Your query string contains an unbalanced number of brackets.");
		if (openingBrcs != closingBrcs) throw new QueryException(
				"Your query string contains an unbalanced number of braces.");
	}
	
	public static List<String> parseMorph(String stringTree) {
		
		ArrayList<String> morph = new ArrayList<String>();
		System.err.println(stringTree);
		return morph;
	}


    public static String buildCypherQuery(String cypher, String ctypel, String ctyper,
                                    int cl, int cr, int page, int limit) {
        //todo: implies that there is only one type allowed!
        String sctypel = "", sctyper = "";
        switch (ctypel) {
            case "C":
                sctypel = "chars";
                break;
            case "T":
                sctypel = "tokens";
                break;
        }
        switch (ctyper) {
            case "C":
                sctyper = "chars";
                break;
            case "T":
                sctyper = "tokens";
                break;
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append("<query><cypher><![CDATA[");
        buffer.append(cypher);
        buffer.append("]]></cypher>");
        buffer.append("<wordAliasPrefix>wtok_</wordAliasPrefix>");
        buffer.append("<contextColumn>sent</contextColumn>");
        buffer.append("<contextIdColumn>sid</contextIdColumn>");
        buffer.append("<textColumn>txt</textColumn>");
        buffer.append("<startIndex>");
        buffer.append(page);
        buffer.append("</startIndex>");
        buffer.append("<itemsPerPage>");
        buffer.append(limit);
        buffer.append("</itemsPerPage>");
        buffer.append("<context>");
        buffer.append("<left>");
        buffer.append("<" + sctypel + ">");
        buffer.append(cl);
        buffer.append("</" + sctypel + ">");
        buffer.append("</left>");
        buffer.append("<right>");
        buffer.append("<" + sctyper + ">");
        buffer.append(cr);
        buffer.append("</" + sctyper + ">");
        buffer.append("</right>");
        buffer.append("</context>");
        buffer.append("</query>");
        return buffer.toString();
    }

    public static String buildDotQuery(long sid, String graphdb_id) {
        StringBuffer b = new StringBuffer();
        b.append("<query>");
        b.append("<sentenceId>");
        b.append(sid);
        b.append("</sentenceId>");
        b.append("<gdbId>");
        b.append(graphdb_id);
        b.append("</gdbId>");
        b.append("<hls>");
        b.append("<hl>");
        b.append(40857);
        b.append("</hl>");
        b.append("<hl>");
        b.append(40856);
        b.append("</hl>");
        b.append("</hls>");
        b.append("</query>");

        return b.toString();
    }

    public String buildaggreQuery(String query) {
        StringBuffer b = new StringBuffer();
        b.append("<query><cypher><![CDATA[");
        b.append(query);
        b.append("]]></cypher>");
        b.append("<columns>");
        b.append("<column agg='true' sum='false'>");
        b.append("<cypherAlias>");
        b.append("aggBy");
        b.append("</cypherAlias>");
        b.append("<displayName>");
        b.append("Aggregate");
        b.append("</displayName>");
        b.append("</column>");

        b.append("<column agg='fals' sum='true'>");
        b.append("<cypherAlias>");
        b.append("cnt");
        b.append("</cypherAlias>");
        b.append("<displayName>");
        b.append("Count");
        b.append("</displayName>");
        b.append("</column>");
        b.append("</columns>");

        b.append("</query>");
        return b.toString();
    }

    public static Map addParameters(Map request, int page, int num, String cli, String cri,
                                    int cls, int crs, boolean cutoff) {
        Map ctx = new LinkedHashMap();
        List left = new ArrayList();
        left.add(cli);
        left.add(cls);
        List right = new ArrayList();
        right.add(cri);
        right.add(crs);
        ctx.put("left", left);
        ctx.put("right", right);

        request.put("startPage", page);
        request.put("count", num);
        request.put("context", ctx);
        request.put("cutOff", cutoff);

        return request;
    }

	

	



}
