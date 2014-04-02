package de.ids_mannheim.korap.query.serialize;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ids_mannheim.korap.util.QueryException;
import org.z3950.zing.cql.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author margaretha
 */
public class CQLTree extends AbstractSyntaxTree {

    private static final String VERSION_1_1 = "1.1";
    private static final String VERSION_1_2 = "1.2";
    private static final String INDEX_CQL_SERVERCHOICE = "cql.serverChoice";
    private static final String INDEX_WORDS = "words";
    private static final String TERM_RELATION_CQL_1_1 = "scr";
    private static final String TERM_RELATION_CQL_1_2 = "=";
    private static final String SUPPORTED_RELATION_EXACT = "exact"; // not in the doc    
    private static final String OPERATION_OR = "\"operation:or\"";
    private static final String OPERATION_SEQUENCE = "\"operation:sequence\"";
    private static final String KORAP_CONTEXT = "http://ids-mannheim.de/ns/KorAP/json-ld/v0.1/context.jsonld";

    private LinkedHashMap<String, Object> requestMap;
    private String version;
    private boolean isCaseSensitive; // default true
    private StringBuilder sb;

    public CQLTree(String query) throws QueryException {
        this(query, VERSION_1_2, true);
    }

    public CQLTree(String query, String version) throws QueryException {
        this(query, version, true);
    }

    public CQLTree(String query, String version, boolean isCaseSensitive) throws QueryException {
        this.sb = new StringBuilder();
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
    public void process(String query) throws QueryException {
        CQLNode cqlNode = parseQuerytoCQLNode(query);
        parseCQLNode(cqlNode);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node;
        try {
            node = mapper.readTree(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
            throw new QueryException("Something went wrong!");
        }
        requestMap.put("query", node);

    }

    private CQLNode parseQuerytoCQLNode(String query) throws QueryException {
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

        } catch (CQLParseException | IOException e) {
            throw new QueryException("Error parsing CQL");
        }
    }

    private void parseCQLNode(CQLNode node) throws QueryException {
        if (node instanceof CQLTermNode) {
            parseTermNode((CQLTermNode) node);
        } else if (node instanceof CQLAndNode) {
            parseAndNode((CQLAndNode) node);
        } else if (node instanceof CQLOrNode) {
            parseOrNode((CQLOrNode) node);
        } else {
            throw new QueryException(48, "Only basic search including term-only " +
                    "and boolean operator queries (AND and OR) are currently supported.");
        }
    }

    private void parseTermNode(CQLTermNode node) throws QueryException {
        checkTermNode(node);
        final String term = node.getTerm();
        if ((term == null) || term.isEmpty()) {
            throw new QueryException(27, "An empty term is unsupported.");
        } else if (term.contains(" ")) {
            writeSequence(term);
        } else {
            writeTerm(term);
        }
    }

    private void parseAndNode(CQLAndNode node) throws QueryException {
        checkBooleanModifier(node);
        sb.append("{\"@type\":\"korap:group\", \"operation\":");
        sb.append(OPERATION_SEQUENCE);
        sb.append(", \"distances\":[{\"@type\":\"korap:distance\", \"key\":\"t\"," +
                " \"min\":0, \"max\":0}]");
        sb.append(", \"operands\":[");
        parseCQLNode(node.getLeftOperand());
        sb.append(", ");
        parseCQLNode(node.getRightOperand());
        sb.append("]}");
    }

    private void parseOrNode(CQLOrNode node) throws QueryException {
        checkBooleanModifier(node);
        sb.append("{\"@type\":\"korap:group\", \"operation\":");
        sb.append(OPERATION_OR);
        sb.append(", \"operands\":[");
        parseCQLNode(node.getLeftOperand());
        sb.append(", ");
        parseCQLNode(node.getRightOperand());
        sb.append("]}");
    }

    private void writeSequence(String str) {
        String[] terms = str.split(" ");
        sb.append("{\"@type\":\"korap:group\", \"operation\":");
        sb.append(OPERATION_SEQUENCE);
        sb.append(", \"operands\":[");

        int size = terms.length;
        for (int i = 0; i < size; i++) {
            writeTerm(terms[i]);
            if (i < size - 1)
                sb.append(", ");
        }

        sb.append("]}");
    }

    private void writeTerm(String term) {
        sb.append("{\"@type\":\"korap:token\", \"wrap\":{\"@type\":\"korap:term\"");
        if (!isCaseSensitive) {
            sb.append(", \"caseInsensitive\":true");
        }
        sb.append(", \"key\":");
        sb.append("\"" + term + "\"");
        sb.append(", \"layer\":\"orth\", \"match\":\"match:eq\"}}");
    }

    private void checkBooleanModifier(CQLBooleanNode node) throws QueryException {
        List<Modifier> modifiers = node.getModifiers();
        if ((modifiers != null) && !modifiers.isEmpty()) {
            Modifier modifier = modifiers.get(0);
            throw new QueryException(20, "Relation modifier " +
                    modifier.toCQL() + " is not supported.");
        }
    }

    private void checkTermNode(CQLTermNode node) throws QueryException {
        // only allow "cql.serverChoice" and "words" index
        if (!(INDEX_CQL_SERVERCHOICE.equals(node.getIndex()) ||
                INDEX_WORDS.equals(node.getIndex()))) {
            throw new QueryException(16, "Index " + node.getIndex() + " is not supported.");
        }
        // only allow "=" relation without any modifiers
        CQLRelation relation = node.getRelation();
        String baseRel = relation.getBase();
        if (!(TERM_RELATION_CQL_1_1.equals(baseRel) ||
                TERM_RELATION_CQL_1_2.equals(baseRel) ||
                SUPPORTED_RELATION_EXACT.equals(baseRel))) {
            throw new QueryException(19, "Relation " +
                    relation.getBase() + " is not supported.");
        }
        List<Modifier> modifiers = relation.getModifiers();
        if ((modifiers != null) && !modifiers.isEmpty()) {
            Modifier modifier = modifiers.get(0);
            throw new QueryException(20, "Relation modifier " +
                    modifier.getValue() + " is not supported.");
        }
    }

}
