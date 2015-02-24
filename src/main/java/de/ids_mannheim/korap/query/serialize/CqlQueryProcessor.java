package de.ids_mannheim.korap.query.serialize;

import de.ids_mannheim.korap.query.serialize.util.StatusCodes;

import org.z3950.zing.cql.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author margaretha
 * @date 09.05.14
 */
public class CqlQueryProcessor extends AbstractQueryProcessor {

    private static final String VERSION_1_1 = "1.1";
    private static final String VERSION_1_2 = "1.2";
    private static final String INDEX_CQL_SERVERCHOICE = "cql.serverChoice";
    private static final String INDEX_WORDS = "words";
    private static final String TERM_RELATION_CQL_1_1 = "scr";
    private static final String TERM_RELATION_CQL_1_2 = "=";
    private static final String SUPPORTED_RELATION_EXACT = "exact"; // not
                                                                    // in
                                                                    // the
                                                                    // doc
    private static final String OPERATION_OR = "operation:or";
    private static final String OPERATION_SEQUENCE = "operation:sequence";
    private static final String OPERATION_POSITION = "operation:position";
    private static final String KORAP_CONTEXT = "http://ids-mannheim.de/ns/KorAP/json-ld/v0.1/context.jsonld";

    private LinkedHashMap<String, Object> requestMap;
    private String version;
    private boolean isCaseSensitive; // default true

    public CqlQueryProcessor (String query) {
        this(query, VERSION_1_2, true);
    }

    public CqlQueryProcessor (String query, String version) {
        this(query, version, true);
    }

    public CqlQueryProcessor (String query, String version,
            boolean isCaseSensitive) {
        this.version = version;
        this.isCaseSensitive = isCaseSensitive;
        this.requestMap = new LinkedHashMap<>();
        requestMap.put("@context", KORAP_CONTEXT);
        process(query);
    }

    @Override
    public Map<String, Object> getRequestMap() {
        return this.requestMap;
    }

    @Override
    public void process(String query) {
        if ((query == null) || query.isEmpty())
            addError(StatusCodes.MALFORMED_QUERY,
                    "SRU diagnostic 27: An empty query is unsupported.");

        CQLNode cqlNode = parseQuerytoCQLNode(query);
        Map<String, Object> queryMap = parseCQLNode(cqlNode);
        requestMap.put("query", queryMap);
        // requestMap.put("query", sentenceWrapper(queryMap));
    }

    private Map<String, Object> sentenceWrapper(Map<String, Object> m) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("@type", "koral:group");
        map.put("operation", OPERATION_POSITION);
        map.put("frame", "frame:contains");

        Map<String, Object> sentence = new LinkedHashMap<String, Object>();
        sentence.put("@type", "koral:span");
        sentence.put("key", "s");

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        list.add(sentence);
        list.add(m);
        map.put("operands", list);

        return map;
    }

    private CQLNode parseQuerytoCQLNode(String query) {
        try {
            int compat = -1;
            switch (version) {
                case VERSION_1_1:
                    compat = CQLParser.V1POINT1;
                    break;
                case VERSION_1_2:
                    compat = CQLParser.V1POINT2;
            }
            return new CQLParser(compat).parse(query);

        }
        catch (CQLParseException | IOException e) {
            addError(StatusCodes.MALFORMED_QUERY, "Error parsing CQL");
            return null;
        }
    }

    private Map<String, Object> parseCQLNode(CQLNode node) {

        if (node instanceof CQLTermNode) {
            return parseTermNode((CQLTermNode) node);
        }
        else if (node instanceof CQLAndNode) {
            return parseAndNode((CQLAndNode) node);
        }
        else if (node instanceof CQLOrNode) {
            return parseOrNode((CQLOrNode) node);
        }
        else {
            addError(
                    StatusCodes.UNKNOWN_QUERY_ELEMENT,
                    "SRU diagnostic 48: Only basic search including term-only "
                            + "and boolean (AND,OR) operator queries are currently supported.");
            return new LinkedHashMap<String, Object>();
        }
    }

    private Map<String, Object> parseTermNode(CQLTermNode node) {
        checkTermNode(node);
        final String term = node.getTerm();
        if ((term == null) || term.isEmpty()) {
            addError(StatusCodes.NO_QUERY,
                    "SRU diagnostic 27: An empty term is unsupported.");
            return new LinkedHashMap<String, Object>();
        }
        else if (term.contains(" ")) {
            return writeSequence(term);
        }
        else {
            return writeTerm(term);
        }
    }

    private Map<String, Object> parseAndNode(CQLAndNode node) {
        checkBooleanModifier(node);

        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("@type", "koral:group");
        map.put("operation", OPERATION_SEQUENCE);
        map.put("inOrder", false);

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> distanceMap = new LinkedHashMap<String, Object>();
        distanceMap.put("@type", "koral:distance");
        distanceMap.put("key", "s");
        distanceMap.put("min", "0");
        distanceMap.put("max", "0");
        list.add(distanceMap);
        map.put("distances", list);

        List<Map<String, Object>> operandList = new ArrayList<Map<String, Object>>();
        operandList.add(parseCQLNode(node.getLeftOperand()));
        operandList.add(parseCQLNode(node.getRightOperand()));
        map.put("operands", operandList);

        return map;
    }

    private Map<String, Object> parseOrNode(CQLOrNode node) {
        checkBooleanModifier(node);

        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("@type", "koral:group");
        map.put("operation", OPERATION_OR);

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        list.add(parseCQLNode(node.getLeftOperand()));
        list.add(parseCQLNode(node.getRightOperand()));
        map.put("operands", list);

        return map;
    }

    private Map<String, Object> writeSequence(String str) {
        Map<String, Object> sequenceMap = new LinkedHashMap<String, Object>();
        sequenceMap.put("@type", "koral:group");
        sequenceMap.put("operation", OPERATION_SEQUENCE);

        List<Map<String, Object>> termList = new ArrayList<Map<String, Object>>();
        String[] terms = str.split(" ");
        for (String term : terms) {
            termList.add(writeTerm(term));
        }
        sequenceMap.put("operands", termList);

        return sequenceMap;
    }

    private Map<String, Object> writeTerm(String term) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("@type", "koral:term");
        if (!isCaseSensitive) {
            map.put("caseInsensitive", "true");
        }
        map.put("key", term);
        map.put("layer", "orth");
        map.put("match", "match:eq");

        Map<String, Object> tokenMap = new LinkedHashMap<String, Object>();
        tokenMap.put("@type", "koral:token");
        tokenMap.put("wrap", map);
        return tokenMap;
    }

    private void checkBooleanModifier(CQLBooleanNode node) {
        List<Modifier> modifiers = node.getModifiers();
        if ((modifiers != null) && !modifiers.isEmpty()) {
            Modifier modifier = modifiers.get(0);
            addError(105,
                    "SRU diagnostic 20: Relation modifier " + modifier.toCQL()
                            + " is not supported.");
        }
    }

    private void checkTermNode(CQLTermNode node) {
        // only allow "cql.serverChoice" and "words" index
        if (!(INDEX_CQL_SERVERCHOICE.equals(node.getIndex()) || INDEX_WORDS
                .equals(node.getIndex()))) {
            addError(105, "SRU diagnostic 16: Index " + node.getIndex()
                    + " is not supported.");
        }
        // only allow "=" relation without any modifiers
        CQLRelation relation = node.getRelation();
        String baseRel = relation.getBase();
        if (!(TERM_RELATION_CQL_1_1.equals(baseRel)
                || TERM_RELATION_CQL_1_2.equals(baseRel) || SUPPORTED_RELATION_EXACT
                    .equals(baseRel))) {
            addError(105, "SRU diagnostic 19: Relation " + relation.getBase()
                    + " is not supported.");
        }
        List<Modifier> modifiers = relation.getModifiers();
        if ((modifiers != null) && !modifiers.isEmpty()) {
            Modifier modifier = modifiers.get(0);
            addError(
                    105,
                    "SRU diagnostic 20: Relation modifier "
                            + modifier.getValue() + " is not supported.");
        }
    }

}
