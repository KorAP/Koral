package de.ids_mannheim.korap.test.cosmas2;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ids_mannheim.korap.query.serialize.QuerySerializer;

// EM: exclusion always returns the first operand
@Ignore
public class OPINWithExclusionTest {
    private String query;

    private QuerySerializer qs = new QuerySerializer();
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode res;

    @Test
    public void testOPINWithExclusion ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(%) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
//        System.out.println(res);
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:exclusion",
                res.at("/query/operation").asText());
        assertEquals(4, res.at("/query/frames").size());
        assertEquals("frames:alignsLeft", res.at("/query/frames/0").asText());
        assertEquals("frames:alignsRight", res.at("/query/frames/1").asText());
        assertEquals("frames:matches", res.at("/query/frames/3").asText());
        assertEquals("frames:isWithin", res.at("/query/frames/2").asText());
        
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:span", res.at("/query/operands/1/@type").asText());
    }

    @Test
    public void testOPINWithExclusionN ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(%,N) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());

        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:exclusion",
                res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/frames").size());
        assertEquals("frames:isWithin", res.at("/query/frames/0").asText());
        
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:span", res.at("/query/operands/1/@type").asText());

		// ND: I think, this requires
		//     ["frames:alignsLeft","frames:alignsRight", "frames:matches"]
		//     and not ["frames:within"] ..
        
        // EM: I thought exclusion of ["frames:alignsLeft","frames:alignsRight", "frames:matches"]
        // is the same as frame:isWithin
    }


    @Test
    public void testOPINwithExclusionL ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(%, L) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
//        System.out.println(res);
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:exclusion",
                res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/frames").size());
        assertEquals("frames:alignsLeft", res.at("/query/frames/0").asText());
        
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:span", res.at("/query/operands/1/@type").asText());

		// ND: I think, frames:matches is wrong here ...
        // EM: fixed
    }


    @Test
    public void testOPINwithExclusionR ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(%, R) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());

        assertEquals("operation:exclusion",
                res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/frames").size());
        assertEquals("frames:alignsRight", res.at("/query/frames/0").asText());
		// ND: I think, frames:matches is wrong here ...
        // EM: fixed
	}


    @Test
    public void testOPINwithExclusionF ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(%, F) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());

        assertEquals("operation:exclusion",
                res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/frames").size());
        assertEquals("frames:matches", res.at("/query/frames/0").asText());
    }


    @Test
    public void testOPINwithExclusionFE ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(%, FE) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
//		System.err.println(res.toString());

        assertEquals("operation:class", res.at("/query/operation").asText());
        assertEquals("classRefCheck:differs",
                res.at("/query/classRefCheck/0").asText());

        JsonNode classRefCheckOperand = res.at("/query/operands/0");
        assertEquals("operation:exclusion",
                classRefCheckOperand.at("/operation").asText());
        assertEquals(1, classRefCheckOperand.at("/frames").size());
        assertEquals("frames:matches",
                classRefCheckOperand.at("/frames/0").asText());

		// ND: Oh - that's a tough one ... and unfortunately
		//     I don't think it works that way.
		//     (I'm thinking here about the more complicated with a complex Y in HIT sense.)
		//     The problem is, this will rule out any matching Y at all before checking
		//     the classes.
		//     There are no classes left to check.
		//     So - I think you may even need an or-query here.
		//     Something like
		//     or(
		//       exclusion(['matches'],X,Y),
		//       focus(1:classRefCheck(['differs',1,2],position(['matches'],{1:X},{2:Y})))
		//     )
        
        // EM: hm, but the second OR operand means X matches Y which is not a correct answer 
        // (not an exclusion, it should not match).
    }


    @Test
    public void testOPINwithExclusionFI ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(%, FI) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());

        assertEquals("operation:class", res.at("/query/operation").asText());
        assertEquals("classRefCheck:equals",
                res.at("/query/classRefCheck/0").asText());

        JsonNode classRefCheckOperand = res.at("/query/operands/0");
        assertEquals("operation:exclusion",
                classRefCheckOperand.at("/operation").asText());
        assertEquals(1, classRefCheckOperand.at("/frames").size());
        assertEquals("frames:matches",
                classRefCheckOperand.at("/frames/0").asText());
    }


    // EM: MIN, MAX does not matter with %
    
    
    @Test
    public void testOPINwithExclusionFE_MIN ()
            throws JsonProcessingException, IOException {
        // MIN is the default value, thus the query below 
        // is the same as "wegen #IN(FE,%) <s>"
        query = "wegen #IN(FE,%,MIN) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("operation:class", res.at("/query/operation").asText());
        assertEquals("classRefCheck:differs",
                res.at("/query/classRefCheck/0").asText());
        assertEquals(1, res.at("/query/operands/0/frames").size());
        assertEquals("frames:matches",
                res.at("/query/operands/0/frames/0").asText());
    }

    @Test
    public void testOPINwithExclusionFE_MAX ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(FE,%,MAX) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("operation:class", res.at("/query/operation").asText());
        assertEquals("classRefCheck:differs",
                res.at("/query/classRefCheck/0").asText());

        JsonNode classRefCheckOperand = res.at("/query/operands/0");
        assertEquals("operation:exclusion",
                classRefCheckOperand.at("/operation").asText());
        assertEquals(1, classRefCheckOperand.at("/frames").size());
        assertEquals("frames:matches",
                classRefCheckOperand.at("/frames/0").asText());
    }

    @Test
    public void testOPINwithExclusionN_MAX ()
            throws JsonProcessingException, IOException {

        query = "wegen #IN(N,%,MAX) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:exclusion",
                res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/frames").size());
        assertEquals("frames:isWithin", res.at("/query/frames/0").asText());
        
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:span", res.at("/query/operands/1/@type").asText());
    }

    @Test
    public void testOPINwithExclusionL_MAX ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(L,%,MAX) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:exclusion",
                res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/frames").size());
        assertEquals("frames:alignsLeft", res.at("/query/frames/0").asText());
        
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:span", res.at("/query/operands/1/@type").asText());
    }

    // EM: KorAP always do ALL by default.
    
    @Test
    public void testOPINwithExclusionN_ALL ()
            throws JsonProcessingException, IOException {
        
        query = "wegen #IN(N,ALL,%) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
//        System.out.println(res);
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:exclusion",
                res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/frames").size());
        assertEquals("frames:isWithin", res.at("/query/frames/0").asText());
        
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());    }

    @Test
    public void testOPINwithExclusionN_ALL_MAX ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(N,ALL,%,MAX) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:exclusion",
                res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/frames").size());
        assertEquals("frames:isWithin", res.at("/query/frames/0").asText());
        
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
    }
    
    @Test
    public void testOPINwithExclusionFE_ALL ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(FE,ALL,%) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("operation:class", res.at("/query/operation").asText());
        assertEquals("classRefCheck:differs",
                res.at("/query/classRefCheck/0").asText());
        assertEquals(1, res.at("/query/operands/0/frames").size());
        assertEquals("frames:matches",
                res.at("/query/operands/0/frames/0").asText());
    }

    @Test
    public void testOPINwithExclusionFE_ALL_MAX ()
            throws JsonProcessingException, IOException {
        query = "wegen #IN(FE,ALL,%,MAX) <s>";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("operation:class", res.at("/query/operation").asText());
        assertEquals("classRefCheck:differs",
                res.at("/query/classRefCheck/0").asText());
        assertEquals(1, res.at("/query/operands/0/frames").size());
        assertEquals("frames:matches",
                res.at("/query/operands/0/frames/0").asText());
    }

    // EM: Does exclusion HIT not need classRefCheck?
    @Test
    public void testOPINwithExclusionN_HIT ()
            throws JsonProcessingException, IOException {
        
        query = "wegen #IN(N,HIT,%) (wegen /w5:10 des)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        // System.out.println(res);
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:exclusion",
                res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/frames").size());
        assertEquals("frames:isWithin", res.at("/query/frames/0").asText());
        
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        
        assertEquals("operation:sequence", res.at("/query/operands/1/operation").asText());
        
        JsonNode seqOperand = res.at("/query/operands/1/operands");
                
        assertEquals("operation:class", seqOperand.at("/0/operation").asText());
        assertEquals(129, seqOperand.at("/0/classOut").asInt());
        
        assertEquals("operation:class", seqOperand.at("/1/operation").asText());
        assertEquals(129, seqOperand.at("/1/classOut").asInt());
    }
    
}
