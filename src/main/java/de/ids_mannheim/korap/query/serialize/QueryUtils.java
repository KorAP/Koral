package de.ids_mannheim.korap.query.serialize;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hanl
 * @date 10/12/2013
 */
public class QueryUtils {




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
