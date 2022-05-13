package de.ids_mannheim.korap.query.test.cqp;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

import de.ids_mannheim.korap.query.serialize.util.StatusCodes;
import de.ids_mannheim.korap.query.test.BaseQueryTest;

/**
 * @author Elena Irimia, margaretha
 *
 */
public class CQPRegionTest extends BaseQueryTest {

    public CQPRegionTest () {
        super("CQP");
    }

    @Test
    public void testMatchingAttributeForAllRegion ()
            throws JsonMappingException, JsonProcessingException {

        // /region needs a span argument
         JsonNode n = runQuery("/region[(class=\"header\")]");

        // EM: is this the expected result? Elena: i guess so...
        assertEquals(StatusCodes.MALFORMED_QUERY, n.at("/errors/0/0").asInt());
    }


    @Test
    public void testMatchingAttributeInSentence ()
            throws JsonMappingException, JsonProcessingException {
        JsonNode res = runQuery("/region[<s (class=\"header\")>]");
        assertEquals("s", res.at("/query/wrap/key").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("koral:term", res.at("/query/attr/@type").asText());
        assertEquals("class", res.at("/query/attr/key").asText());
        assertEquals("header", res.at("/query/attr/value").asText());
        assertEquals("match:eq", res.at("/query/attr/match").asText());
        
    }


    @Test
    public void testSpans () throws JsonProcessingException, IOException {

        JsonNode res = runQuery(
                "/region[<cnx/c!=vp (class!=\"header\" & id=\"7\")>]");
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("vp", res.at("/query/wrap/key").asText());
        assertEquals("cnx", res.at("/query/wrap/foundry").asText());
        assertEquals("c", res.at("/query/wrap/layer").asText());
        assertEquals("match:ne", res.at("/query/wrap/match").asText());
        assertEquals("koral:termGroup", res.at("/query/attr/@type").asText());
        assertEquals("relation:and", res.at("/query/attr/relation").asText());
        operands = Lists
                .newArrayList(res.at("/query/attr/operands").elements());
        assertEquals("koral:term", operands.get(0).at("/@type").asText());
        assertEquals("class", operands.get(0).at("/key").asText());
        assertEquals("header", operands.get(0).at("/value").asText());
        assertEquals("match:ne", operands.get(0).at("/match").asText());
        assertEquals("koral:term", operands.get(1).at("/@type").asText());
        assertEquals("id", operands.get(1).at("/key").asText());
        assertEquals(7, operands.get(1).at("/value").asInt());
        assertEquals("match:eq", operands.get(1).at("/match").asText());

        res = runQuery("/region[<cnx/c=vp (class!=\"header\" & id=\"7\")>]");
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("vp", res.at("/query/wrap/key").asText());
        assertEquals("cnx", res.at("/query/wrap/foundry").asText());
        assertEquals("c", res.at("/query/wrap/layer").asText());
        assertEquals("koral:termGroup", res.at("/query/attr/@type").asText());
        assertEquals("relation:and", res.at("/query/attr/relation").asText());
        operands = Lists
                .newArrayList(res.at("/query/attr/operands").elements());
        assertEquals("koral:term", operands.get(0).at("/@type").asText());
        assertEquals("class", operands.get(0).at("/key").asText());
        assertEquals("header", operands.get(0).at("/value").asText());
        assertEquals("match:ne", operands.get(0).at("/match").asText());
        assertEquals("koral:term", operands.get(1).at("/@type").asText());
        assertEquals("id", operands.get(1).at("/key").asText());
        assertEquals(7, operands.get(1).at("/value").asInt());
        assertEquals("match:eq", operands.get(1).at("/match").asText());

        res = runQuery("/region[<cnx/c=vp (class=\"header\")>]");

        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("vp", res.at("/query/wrap/key").asText());
        assertEquals("cnx", res.at("/query/wrap/foundry").asText());
        assertEquals("c", res.at("/query/wrap/layer").asText());
        assertEquals("class", res.at("/query/attr/key").asText());
        assertEquals("header", res.at("/query/attr/value").asText());
        assertEquals("match:eq", res.at("/query/attr/match").asText());

        // matches all sentences
        query = "/region[s]";
        res = runQuery(query);
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("s", res.at("/query/wrap/key").asText());

        // matches all vps;
        query = "/region[<vp>]";
        res = runQuery(query);
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("vp", res.at("/query/wrap/key").asText());


        query = "/region[<cnx/c=vp>]";
        res = runQuery(query);
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("vp", res.at("/query/wrap/key").asText());
        assertEquals("cnx", res.at("/query/wrap/foundry").asText());
        assertEquals("c", res.at("/query/wrap/layer").asText());


        query = "/region[<cnx/c!=vp>]";
        res = runQuery(query);
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("vp", res.at("/query/wrap/key").asText());
        assertEquals("cnx", res.at("/query/wrap/foundry").asText());
        assertEquals("c", res.at("/query/wrap/layer").asText());
        assertEquals("match:ne", res.at("/query/wrap/match").asText());


        query = "/region[<cnx/c!=vp class!=\"header\">]";
        res = runQuery(query);
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("vp", res.at("/query/wrap/key").asText());
        assertEquals("cnx", res.at("/query/wrap/foundry").asText());
        assertEquals("c", res.at("/query/wrap/layer").asText());
        assertEquals("match:ne", res.at("/query/wrap/match").asText());
        assertEquals("class", res.at("/query/attr/key").asText());
        assertEquals("header", res.at("/query/attr/value").asText());
        assertEquals("match:ne", res.at("/query/attr/match").asText());



        query = "/region[<cnx/c!=vp !(class!=\"header\")>]";
        res = runQuery(query);
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("vp", res.at("/query/wrap/key").asText());
        assertEquals("cnx", res.at("/query/wrap/foundry").asText());
        assertEquals("c", res.at("/query/wrap/layer").asText());
        assertEquals("match:ne", res.at("/query/wrap/match").asText());
        assertEquals("class", res.at("/query/attr/key").asText());
        assertEquals("header", res.at("/query/attr/value").asText());
        assertEquals("match:eq", res.at("/query/attr/match").asText());
    }
    

    @Test
    public void testRegionAttributeGroupNegation ()
            throws JsonMappingException, JsonProcessingException {
        query = "/region[<cnx/c!=vp !(class=\"header\" & id=\"7\")>]";
        JsonNode res = runQuery(query);
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("vp", res.at("/query/wrap/key").asText());
        assertEquals("cnx", res.at("/query/wrap/foundry").asText());
        assertEquals("c", res.at("/query/wrap/layer").asText());
        assertEquals("match:ne", res.at("/query/wrap/match").asText());
        assertEquals("koral:termGroup", res.at("/query/attr/@type").asText());
        assertEquals("relation:or", res.at("/query/attr/relation").asText());
        operands = Lists
                .newArrayList(res.at("/query/attr/operands").elements());
        assertEquals("koral:term", operands.get(0).at("/@type").asText());
        assertEquals("class", operands.get(0).at("/key").asText());
        assertEquals("header", operands.get(0).at("/value").asText());
        assertEquals("match:ne", operands.get(0).at("/match").asText());
        assertEquals("koral:term", operands.get(1).at("/@type").asText());
        assertEquals("id", operands.get(1).at("/key").asText());
        assertEquals(7, operands.get(1).at("/value").asInt());
        assertEquals("match:ne", operands.get(1).at("/match").asText());
    }

    @Test
    public void testRegionAndTokenSequence () throws JsonProcessingException,
            IOException {
        // vezi ca asta e de la modificarea cu span-ul de ieri, cand ai schimbat ordinea in operators list!
        query = "[base='Mann'] /region[vp]"; // in PQ+ "[base=Mann]<vp>"
        JsonNode result = runQuery(query);
        
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:sequence", result.at("/query/operation").asText());
        assertEquals("koral:token", result.at("/query/operands/0/@type").asText());
        assertEquals("Mann", result.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:span", result.at("/query/operands/1/@type").asText());
        assertEquals("vp", result.at("/query/operands/1/wrap/key").asText());

        query = "/region[<coreNLP/c=NP>] [base='Mann']"; // region with foundry and layer
        result = runQuery(query);
        
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:sequence", result.at("/query/operation").asText());
        assertEquals("koral:span", result.at("/query/operands/0/@type").asText());
        assertEquals("NP", result.at("/query/operands/0/wrap/key").asText());
        assertEquals("coreNLP", result.at("/query/operands/0/wrap/foundry").asText());
        assertEquals("c", result.at("/query/operands/0/wrap/layer").asText());
        assertEquals("koral:token", result.at("/query/operands/1/@type").asText());
        assertEquals("Mann", result.at("/query/operands/1/wrap/key").asText());
        

        query = "/region[vp] [base=\"Mann\"] /region[pp] /region[np]";
        result = runQuery(query);
        
        assertEquals("pp", result.at("/query/operands/2/wrap/key").asText());
        assertEquals("np", result.at("/query/operands/3/wrap/key").asText());
       
    }
}
