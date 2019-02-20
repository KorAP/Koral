package de.ids_mannheim.korap.test.cosmas2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ids_mannheim.korap.query.object.ClassRefCheck;
import de.ids_mannheim.korap.query.object.ClassRefOp;
import de.ids_mannheim.korap.query.object.KoralOperation;
import de.ids_mannheim.korap.query.serialize.QuerySerializer;

public class OPOVTest {

    String query;
    ArrayList<JsonNode> operands;

    QuerySerializer qs = new QuerySerializer();
    ObjectMapper mapper = new ObjectMapper();
    JsonNode res;

    private void checkClassOp (JsonNode node, ClassRefOp operation) {
        assertEquals("koral:group", node.at("/@type").asText());
        assertEquals("operation:class", node.at("/operation").asText());
        assertEquals(operation.toString(), node.at("/classRefOp").asText());
        assertEquals(2, node.at("/classIn").size());
        assertEquals(129, node.at("/classIn/0").asInt());
        assertEquals(130, node.at("/classIn/1").asInt());
        assertEquals(131, node.at("/classOut").asInt());
    }

    private void checkClassRef (JsonNode node, ClassRefCheck ref) {
        assertEquals("koral:group", node.at("/@type").asText());
        assertEquals("operation:class", node.at("/operation").asText());
        assertEquals(ref.toString(), node.at("/classRefCheck/0").asText());
        assertTrue(node.at("/classOut").isMissingNode());
        assertEquals(1, node.at("/classRefCheck").size());
        assertEquals(2, node.at("/classIn").size());
        assertEquals(129, node.at("/classIn/0").asInt());
        assertEquals(130, node.at("/classIn/1").asInt());
    }

    @Test
    public void testOPOV () throws JsonProcessingException, IOException {
        query = "wegen #OV <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());

        checkClassOp(res.at("/query"), ClassRefOp.UNION);
        res = res.at("/query/operands/0");

        checkClassRef(res, ClassRefCheck.INTERSECTS);
        // frames
        assertEquals("koral:group", res.at("/operands/0/@type").asText());
        assertEquals("operation:position",
                res.at("/operands/0/operation").asText());
        assertEquals(4, res.at("/operands/0/frames").size());

        // operands
        assertEquals("koral:group", res.at("/operands/0/@type").asText());
        assertEquals("operation:class",
                res.at("/operands/0/operands/0/operation").asText());
        assertEquals(129, res.at("/operands/0/operands/0/classOut").asInt());
        assertEquals("koral:token",
                res.at("/operands/0/operands/0/operands/0/@type").asText());
        assertEquals("wegen",
                res.at("/operands/0/operands/0/operands/0/wrap/key").asText());
        assertEquals("koral:group",
                res.at("/operands/0/operands/1/@type").asText());
        assertEquals("operation:class",
                res.at("/operands/0/operands/1/operation").asText());

        assertEquals(130, res.at("/operands/0/operands/1/classOut").asInt());
        assertEquals("koral:span",
                res.at("/operands/0/operands/1/operands/0/@type").asText());
        assertEquals("s",
                res.at("/operands/0/operands/1/operands/0/wrap/key").asText());
    }

    @Test
    public void testOPOV_X () throws JsonProcessingException, IOException {
        query = "wegen #OV(X) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());

        checkClassOp(res.at("/query"), ClassRefOp.UNION);
        res = res.at("/query/operands/0");
        checkClassRef(res, ClassRefCheck.INTERSECTS);

        assertEquals(1, res.at("/operands/0/frames").size());
        assertEquals("frames:isAround",
                res.at("/operands/0/frames/0").asText());
    }

    @Test
    public void testOPOV_L () throws JsonProcessingException, IOException {
        query = "wegen #OV(L) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());

        checkClassOp(res.at("/query"), ClassRefOp.UNION);
        res = res.at("/query/operands/0");
        checkClassRef(res, ClassRefCheck.INTERSECTS);

        assertEquals(2, res.at("/operands/0/frames").size());
        assertEquals("frames:startsWith",
                res.at("/operands/0/frames/0").asText());
        assertEquals("frames:overlapsLeft",
                res.at("/operands/0/frames/1").asText());
        // assertEquals("frames:matches",
        // res.at("/operands/0/frames/0").asText());
    }

    @Test
    public void testOPOV_R () throws JsonProcessingException, IOException {
        query = "wegen #OV(R) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());

        checkClassOp(res.at("/query"), ClassRefOp.UNION);
        res = res.at("/query/operands/0");
        checkClassRef(res, ClassRefCheck.INTERSECTS);

        assertEquals(2, res.at("/operands/0/frames").size());
        assertEquals("frames:endsWith",
                res.at("/operands/0/frames/0").asText());
        assertEquals("frames:overlapsRight",
                res.at("/operands/0/frames/1").asText());
        // assertEquals("frames:matches",
        // res.at("/operands/0/frames/0").asText());
    }

    @Test
    public void testOPOV_F () throws JsonProcessingException, IOException {
        query = "wegen #OV(F) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());

        checkClassOp(res.at("/query"), ClassRefOp.UNION);
        res = res.at("/query/operands/0");
        checkClassRef(res, ClassRefCheck.INTERSECTS);

        assertEquals(1, res.at("/operands/0/frames").size());
        assertEquals("frames:matches", res.at("/operands/0/frames/0").asText());
    }

    @Test
    public void testOPOV_FI () throws JsonProcessingException, IOException {
        query = "wegen #OV(FI) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        // System.out.println(res);
        checkClassOp(res.at("/query"), ClassRefOp.UNION);
        JsonNode node = res.at("/query/operands/0");

        assertEquals("koral:group", node.at("/@type").asText());
        assertEquals("operation:class", node.at("/operation").asText());
        assertEquals(ClassRefCheck.INTERSECTS.toString(),
                node.at("/classRefCheck/0").asText());
        assertEquals(ClassRefCheck.DIFFERS.toString(),
                node.at("/classRefCheck/1").asText());

        assertTrue(node.at("/classOut").isMissingNode());
        assertEquals(2, node.at("/classRefCheck").size());
        assertEquals(2, node.at("/classIn").size());
        assertEquals(129, node.at("/classIn/0").asInt());
        assertEquals(130, node.at("/classIn/1").asInt());

        assertEquals(1, node.at("/operands/0/frames").size());
        assertEquals("frames:matches",
                node.at("/operands/0/frames/0").asText());
    }

    @Test
    public void testOPOV_FE () throws JsonProcessingException, IOException {
        query = "wegen #OV(FE) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());

        checkClassOp(res.at("/query"), ClassRefOp.UNION);
        res = res.at("/query/operands/0");
        checkClassRef(res, ClassRefCheck.EQUALS);

        assertEquals(1, res.at("/operands/0/frames").size());
        assertEquals("frames:matches", res.at("/operands/0/frames/0").asText());
    }

    
    @Test
    public void testOPOV_L_ALL () throws JsonProcessingException, IOException {
        query = "der #OV(L,ALL) (der /+w2:2 ist)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());

        checkClassOp(res.at("/query"), ClassRefOp.UNION);
        res = res.at("/query/operands/0");
        checkClassRef(res, ClassRefCheck.INTERSECTS);

        assertEquals(2, res.at("/operands/0/frames").size());
        assertEquals("frames:startsWith",
                res.at("/operands/0/frames/0").asText());
        assertEquals("frames:overlapsLeft",
                res.at("/operands/0/frames/1").asText());

        // class
        res = res.at("/operands/0/operands/1");
        assertEquals(KoralOperation.CLASS.toString(),
                res.at("/operation").asText());
        assertEquals(130, res.at("/classOut").asInt());
    }
    
    @Test
    public void testOPOV_L_HIT () throws JsonProcessingException, IOException {
        query = "der #OV(L) (der /+w2:2 ist)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        checkClassOp(res.at("/query"), ClassRefOp.UNION);
        res = res.at("/query/operands/0");
        checkClassRef(res, ClassRefCheck.INTERSECTS);

        assertEquals(2, res.at("/operands/0/frames").size());
        assertEquals("frames:startsWith",
                res.at("/operands/0/frames/0").asText());
        assertEquals("frames:overlapsLeft",
                res.at("/operands/0/frames/1").asText());

        // sequence
        res = res.at("/operands/0/operands/1");
        assertEquals(KoralOperation.SEQUENCE.toString(),
                res.at("/operation").asText());
        // class
        assertEquals(130, res.at("/operands/0/classOut").asInt());
        assertEquals(130, res.at("/operands/1/classOut").asInt());
    }
}
