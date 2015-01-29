package de.ids_mannheim.korap.query.serialize;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;

/**
 * @author hanl
 * @date 04/06/2014
 * <p/>
 * create idn for korap:token
 */
public class IdWriter {

    private JsonNode node;
    private int counter;
    private ObjectMapper mapper = new ObjectMapper();

    public IdWriter(String json) {
        node = mapper.valueToTree(json);
        counter = 0;
    }

    public IdWriter process() {
        process(node.path("query"));
        return this;
    }

    private void process(JsonNode node) {
        if (node.path("@type").asText().equals("korap:group")) {
            Iterator<JsonNode> operands = node.path("operands").elements();
            while (operands.hasNext())
                process(operands.next());
        }
        else if (node.path("@type").asText().equals("korap:token"))
            addId(node);
    }

    private JsonNode addId(JsonNode node) {
        if (node.isObject()) {
            ObjectNode o = (ObjectNode) node;
            String s = extractToken(node);
            if (s != null && !s.isEmpty())
                o.put("idn", s + "_" + counter++);
        }
        return node;
    }

    private String extractToken(JsonNode token) {
        if (!token.path("@type").equals("korap:term")) {
            JsonNode wrap = token.path("wrap");
            JsonNode op = token.path("operands");
            if (!wrap.isMissingNode())
                return extractToken(wrap);
            if (!op.isMissingNode()) {
                Iterator<JsonNode> operands = op.elements();
                while (operands.hasNext())
                    return extractToken(operands.next());
            }
        }
        return token.path("key").asText();

    }

    @Deprecated public JsonNode getFinalNode() {
        return this.node;
    }

    public String toJSON() {
        try {
            return mapper.writeValueAsString(node);
        }
        catch (JsonProcessingException e) {
            return "";
        }
    }

}
