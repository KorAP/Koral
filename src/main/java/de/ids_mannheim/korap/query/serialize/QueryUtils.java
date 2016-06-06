package de.ids_mannheim.korap.query.serialize;

import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Joachim Bingel (bingel@ids-mannheim.de),
 *         Michael Hanl (hanl@ids-mannheim.de)
 * @version 0.3.0
 * @date 10/12/2013
 * @since 0.1.0
 */
public class QueryUtils {

    public static SimpleEntry<String, Integer> checkUnbalancedPars (String q) {
        Map<Character, Character> brackets = new HashMap<Character, Character>();
        brackets.put('[', ']');
        brackets.put('{', '}');
        brackets.put('(', ')');
        Set<Character> allChars = new HashSet<Character>();
        allChars.addAll(brackets.keySet());
        allChars.addAll(brackets.values());
        int lastOpenBracket = 0;

        final Stack<Character> stack = new Stack<Character>();
        for (int i = 0; i < q.length(); i++) {
            if (!allChars.contains(q.charAt(i)))
                continue;
            if (brackets.containsKey(q.charAt(i))) {
                stack.push(q.charAt(i));
                lastOpenBracket = i;
            }
            else if (stack.empty()
                    || (q.charAt(i) != brackets.get(stack.pop()))) {
                return new SimpleEntry<String, Integer>(
                        "Parantheses/brackets unbalanced.", i);
            }
        }
        if (!stack.empty())
            return new SimpleEntry<String, Integer>(
                    "Parantheses/brackets unbalanced.", lastOpenBracket);
        return null;
    }


    public static List<String> parseMorph (String stringTree) {

        ArrayList<String> morph = new ArrayList<String>();
        return morph;
    }


    public static String buildCypherQuery (String cypher, String ctypel,
            String ctyper, int cl, int cr, int page, int limit) {
        // todo: implies that there is only one type allowed!
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


    public static String buildDotQuery (long sid, String graphdb_id) {
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


    public String buildaggreQuery (String query) {
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


    @Deprecated
    public static Map addParameters (Map request, int page, int num,
            String cli, String cri, int cls, int crs, boolean cutoff) {
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


    /**
     * Checks if value is a date
     * 
     * @param value
     * @return
     */

    public static boolean checkDateValidity (String value) {
        Pattern p = Pattern.compile("^[0-9]{4}(-([0-9]{2})(-([0-9]{2}))?)?$");
        Matcher m = p.matcher(value);

        if (!m.find())
            return false;
        String month = m.group(2);
        String day = m.group(4);
        if (month != null) {
            if (Integer.parseInt(month) > 12) {
                return false;
            }
            else if (day != null) {
                if (Integer.parseInt(day) > 31) {
                    return false;
                }
            }
        }
        return true;
    }


    public static String escapeRegexSpecialChars (String key) {
        key.replace("\\", "\\\\");
        Pattern p = Pattern
                .compile("\\.|\\^|\\$|\\||\\?|\\*|\\+|\\(|\\)|\\[|\\]|\\{|\\}");
        Matcher m = p.matcher(key);
        while (m.find()) {
            String match = m.group();
            key = m.replaceAll("\\\\" + match);
        }
        return key;
    }

}
