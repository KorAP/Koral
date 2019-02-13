package de.ids_mannheim.korap.test.cosmas2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ids_mannheim.korap.query.object.ClassRefCheck;
import de.ids_mannheim.korap.query.serialize.QuerySerializer;

// EM: OPIN only returns the first operand.
// MAX groups all first operand spans that are in a same second operand span

public class OPINTest {
    private String query;

    private QuerySerializer qs = new QuerySerializer();
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode res;

    private void checkKoralSpan (JsonNode node) {
        assertEquals("koral:span", node.at("/operands/0/@type").asText());
        assertEquals("koral:term", node.at("/operands/0/wrap/@type").asText());
        assertEquals("s", node.at("/operands/0/wrap/key").asText());
        assertEquals("s", node.at("/operands/0/wrap/layer").asText());

    }
    
    private void checkFocus (JsonNode node) {
        assertEquals("koral:reference", node.at("/query/@type").asText()); 
        assertEquals("operation:focus", node.at("/query/operation").asText());
        assertEquals(129, node.at("/query/classRef/0").asInt());

    }
    
    private void checkClassRef (JsonNode node, ClassRefCheck ref) {
        assertEquals(ref.toString(), node.at("/classRefCheck/0").asText());
        assertTrue(node.at("/classOut").isMissingNode());
        assertEquals(1, res.at("/classRefCheck").size());
        assertEquals(2, res.at("/classIn").size());
        assertEquals(129, res.at("/classIn/0").asInt());
        assertEquals(130, res.at("/classIn/1").asInt());
    }
    
    private void checkALL (JsonNode res) {
        // ALL class
        assertEquals("operation:class",
                res.at("/operands/0/operation").asText());
        assertEquals(130, res.at("/operands/0/classOut").asInt());
        
        res = res.at("/operands/0/operands/0");
        // sequence
        assertEquals("operation:sequence", res.at("/operation").asText());
        // sequence class
        assertEquals("operation:class",
                res.at("/operands/0/operation").asText());

        assertEquals("gelten",
                res.at("/operands/0/operands/0/wrap/key").asText());
        assertEquals("zurecht",
                res.at("/operands/1/operands/0/wrap/key").asText());
    }
    
    @Test
    public void testOPIN () throws JsonProcessingException, IOException {
        query = "wegen #IN <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
//        System.out.println(res); 
        checkFocus(res);
         
        res = res.at("/query/operands/0");
        checkKoralSpan(res);
        
        assertEquals("koral:group", res.at("/@type").asText());
        assertEquals("operation:position", res.at("/operation").asText());
        assertEquals(4, res.at("/frames").size());
        assertEquals("frames:matches", res.at("/frames/0").asText());
        assertEquals("frames:alignsLeft", res.at("/frames/1").asText());
        assertEquals("frames:alignsRight", res.at("/frames/2").asText());
        assertEquals("frames:isWithin", res.at("/frames/3").asText());
        
        assertEquals("operation:class", res.at("/operands/1/operation").asText());
        assertEquals("koral:token", res.at("/operands/1/operands/0/@type").asText());
    }

    @Test
    public void testOPINWithOptionN ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(N) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        checkFocus(res);
         
        res = res.at("/query/operands/0");
        checkKoralSpan(res);
        
        assertEquals("koral:group", res.at("/@type").asText());
        assertEquals("operation:position", res.at("/operation").asText());
        assertEquals("frames:isWithin", res.at("/frames/0").asText());
        
        assertEquals("operation:class", res.at("/operands/1/operation").asText());
        assertEquals("wegen", res.at("/operands/1/operands/0/wrap/key").asText());
    }


    @Test
    public void testOPINWithOptionL ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(L) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        checkFocus(res);
        
        res = res.at("/query/operands/0");
        checkKoralSpan(res);
        
        assertEquals("operation:position", res.at("/operation").asText());
        assertEquals("frames:alignsLeft", res.at("/frames/0").asText());
        assertEquals(1, res.at("/frames").size());

        assertEquals("operation:class", res.at("/operands/1/operation").asText());
        assertEquals("wegen", res.at("/operands/1/operands/0/wrap/key").asText());
    }


    @Test
    public void testOPINWithOptionR ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(R) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        checkFocus(res);
        res = res.at("/query/operands/0");
        checkKoralSpan(res);
        
        assertEquals("operation:position", res.at("/operation").asText());
        assertEquals("frames:alignsRight", res.at("/frames/0").asText());
        assertEquals(1, res.at("/frames").size());
    }


    @Test
    public void testOPINwithOptionF ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(F) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        checkFocus(res);
        res = res.at("/query/operands/0");
        
        assertEquals(true,
                res.at("/query/operands/0/classRefCheck").isMissingNode());
        assertEquals("frames:matches", res.at("/frames/0").asText());
        assertEquals(true, res.at("/frames/1").isMissingNode());
    }

    @Test
    public void testOPINwithOptionFI ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(FI) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
//        System.out.println(res);
        checkFocus(res);
        res = res.at("/query/operands/0");
        
        assertEquals("classRefCheck:differs",
                res.at("/classRefCheck/0").asText());
        assertEquals(1, res.at("/classRefCheck").size());
        assertEquals(2, res.at("/classIn").size());
        assertEquals(129, res.at("/classIn/0").asInt());
        assertEquals(130, res.at("/classIn/1").asInt());
        
        assertEquals("frames:matches",
                res.at("/operands/0/frames/0").asText());
        assertEquals(true,
                res.at("/operands/0/frames/1").isMissingNode());
        assertEquals("operation:position",
                res.at("/operands/0/operation").asText());
        
        res = res.at("/operands/0/operands/0");
        assertEquals("operation:class", res.at("/operation").asText());
        checkKoralSpan(res);

		// ND: This should fail with a focus requirement on the first operand!
		// ND: The serialization is correct, though the query optimizer
		//     should be able to see that no match can satisfy this query when
		//     X and Y are non-complex operands
        // EM: You mean query optimizer in Krill?
    }


    @Test
    public void testOPINwithOptionFE ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(FE) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        checkFocus(res);
        res = res.at("/query/operands/0");
        assertEquals("classRefCheck:equals",
                res.at("/classRefCheck/0").asText());
        assertEquals(1, res.at("/classRefCheck").size());
        assertEquals(2, res.at("/classIn").size());
        assertEquals(129, res.at("/classIn/0").asInt());
        assertEquals(130, res.at("/classIn/1").asInt());
        assertEquals("frames:matches",
                res.at("/operands/0/frames/0").asText());
        assertEquals(true,
                res.at("/operands/0/frames/1").isMissingNode());
        
        res = res.at("/operands/0/operands/0");
        assertEquals("operation:class", res.at("/operation").asText());
        checkKoralSpan(res);
	}

    
    @Test
    public void testOPINWithOptionN_ALL ()
            throws JsonProcessingException, IOException {
        query = "sich #IN(N,ALL) (&gelten /w5:10 zurecht)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        checkFocus(res);

        res = res.at("/query/operands/0");
        checkClassRef(res, ClassRefCheck.INCLUDES);

        res = res.at("/operands/0");
        assertEquals("koral:group", res.at("/@type").asText());
        assertEquals("operation:position", res.at("/operation").asText());
        assertEquals("frames:isWithin", res.at("/frames/0").asText());

        assertEquals("operation:class",
                res.at("/operands/1/operation").asText());
        assertEquals("sich",
                res.at("/operands/1/operands/0/wrap/key").asText());
        
        checkALL(res);
    }

    @Test
    public void testOPINWithOptionN_HIT ()
            throws JsonProcessingException, IOException {
        query = "sich #IN(N) (&gelten /w5:10 zurecht)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        checkFocus(res);
//        System.out.println(res);
        res = res.at("/query/operands/0");
        checkClassRef(res, ClassRefCheck.INCLUDES);

        res = res.at("/operands/0");
        assertEquals("koral:group", res.at("/@type").asText());
        assertEquals("operation:position", res.at("/operation").asText());
        assertEquals("frames:isWithin", res.at("/frames/0").asText());

        assertEquals("operation:class",
                res.at("/operands/1/operation").asText());
        assertEquals("sich",
                res.at("/operands/1/operands/0/wrap/key").asText());
        
        res = res.at("/operands/0");
        // sequence
        assertEquals("operation:sequence", res.at("/operation").asText());
        // sequence class
        assertEquals("operation:class",
                res.at("/operands/0/operation").asText());
        assertEquals(130, res.at("/operands/0/classOut").asInt());
        
        assertEquals("gelten",
                res.at("/operands/0/operands/0/wrap/key").asText());
        assertEquals("zurecht",
                res.at("/operands/1/operands/0/wrap/key").asText());
    }
    
    @Test
    public void testOPINWithOptionFE_HIT ()
            throws JsonProcessingException, IOException {
        query = "gilt #IN(FE,HIT) (&gelten /w5:10 zurecht)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
//        System.out.println(res);
        checkFocus(res);
        res = res.at("/query/operands/0");
        assertEquals("koral:group", res.at("/@type").asText());
        assertEquals("operation:class", res.at("/operation").asText());

        assertEquals("classRefCheck:equals",
                res.at("/classRefCheck/0").asText());
                assertEquals("classRefCheck:includes",
                        res.at("/classRefCheck/1").asText());
        assertEquals(2, res.at("/classRefCheck").size());
        assertEquals(2, res.at("/classIn").size());

        assertEquals("operation:position",
                res.at("/operands/0/operation").asText());
        assertEquals("frames:matches",
                res.at("/operands/0/frames/0").asText());
        // positions operands
        res = res.at("/operands/0/operands");
        assertEquals("gilt",
                res.at("/1/operands/0/wrap/key").asText());
        assertEquals(129, res.at("/1/classOut").asInt());
        
        // sequence operands
        res = res.at("/0/operands");
        assertEquals("gelten", res.at("/0/operands/0/wrap/key").asText());
        assertEquals(130, res.at("/0/classOut").asInt());
        assertEquals("zurecht", res.at("/1/operands/0/wrap/key").asText());
        assertEquals(130, res.at("/1/classOut").asInt());
    }
    
    @Test
    public void testOPINWithOptionFI_HIT ()
            throws JsonProcessingException, IOException {
        query = "gilt #IN(FI,HIT) (&gelten /w5:10 zurecht)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        checkFocus(res);
        res = res.at("/query/operands/0");
        assertEquals("koral:group", res.at("/@type").asText());
        assertEquals("operation:class", res.at("/operation").asText());
        
        assertEquals("classRefCheck:differs",
                res.at("/classRefCheck/0").asText());
        assertEquals(2, res.at("/classRefCheck").size());
        assertEquals(2, res.at("/classIn").size());

        assertEquals("operation:position",
                res.at("/operands/0/operation").asText());
        assertEquals("frames:matches",
                res.at("/operands/0/frames/0").asText());
    }


    @Test
    @Ignore
    public void testOPINWithOptionN_MAX ()
            throws JsonProcessingException, IOException {
        // EM: Fix operation:merge
        query = "wegen #IN(N, MAX) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());

        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(130, res.at("/query/classRef/0").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:position",
                res.at("/query/operands/0/operation").asText());
        assertEquals("frames:isAround",
                res.at("/query/operands/0/frames/0").asText());
    }
    
}
