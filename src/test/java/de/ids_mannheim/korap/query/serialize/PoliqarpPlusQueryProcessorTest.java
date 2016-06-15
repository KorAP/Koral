package de.ids_mannheim.korap.query.serialize;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import de.ids_mannheim.korap.query.serialize.QuerySerializer;

/**
 * Tests for JSON-LD serialization of PoliqarpPlus queries.
 * 
 * @author Joachim Bingel (bingel@ids-mannheim.de)
 * @version 1.0
 */
public class PoliqarpPlusQueryProcessorTest {

    String query;
    ArrayList<JsonNode> operands;

    QuerySerializer qs = new QuerySerializer();
    ObjectMapper mapper = new ObjectMapper();
    JsonNode res;


    @Test
    public void testContext () throws JsonProcessingException, IOException {
        query = "foo";
        String contextString = "http://korap.ids-mannheim.de/ns/koral/0.3/context.jsonld";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals(contextString, res.get("@context").asText());
    }


    @Test
    public void testSingleTokens () throws JsonProcessingException, IOException {
        query = "[base=Mann]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("Mann", res.at("/query/wrap/key").asText());
        assertEquals("lemma", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "[orth!=Frau]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("Frau", res.at("/query/wrap/key").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:ne", res.at("/query/wrap/match").asText());

        query = "[p!=NN]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("NN", res.at("/query/wrap/key").asText());
        assertEquals("p", res.at("/query/wrap/layer").asText());
        assertEquals("match:ne", res.at("/query/wrap/match").asText());

        query = "[!p!=NN]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("NN", res.at("/query/wrap/key").asText());
        assertEquals("p", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "[base=schland/x]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals(".*?schland.*?", res.at("/query/wrap/key").asText());
        assertEquals("lemma", res.at("/query/wrap/layer").asText());
        assertEquals("type:regex", res.at("/query/wrap/type").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
    }


    @Test
    public void testNegatedTokens () throws JsonProcessingException,
            IOException {
        query = "![p!=NN]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("NN", res.at("/query/wrap/key").asText());
        assertEquals("p", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
    }


    @Test
    public void testValue () throws JsonProcessingException, IOException {
        query = "[mate/m=temp:pres]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("temp", res.at("/query/wrap/key").asText());
        assertEquals("pres", res.at("/query/wrap/value").asText());
        assertEquals("m", res.at("/query/wrap/layer").asText());
        assertEquals("mate", res.at("/query/wrap/foundry").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
    }


    @Test
    public void testRegex () throws JsonProcessingException, IOException {
        query = "[orth=\"M(a|ä)nn(er)?\"]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("M(a|ä)nn(er)?", res.at("/query/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/wrap/type").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "[orth=\"M(a|ä)nn(er)?\"/x]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals(".*?M(a|ä)nn(er)?.*?", res.at("/query/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/wrap/type").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "\".*?Mann.*?\"";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals(".*?Mann.*?", res.at("/query/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/wrap/type").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "z.B./x";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        System.out.println("QUERY IS  " + res);
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals(".*?z\\.B\\..*?", res.at("/query/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/wrap/type").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        // Escape regex symbols
        query = "\"a\\.\"";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        System.out.println("QUERY IS  " + res);
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("type:regex", res.at("/query/wrap/type").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
        assertEquals("a\\.", res.at("/query/wrap/key").asText());
    }


    @Test
    public void testPunct () throws JsonProcessingException, IOException {
        query = "[punct=.]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals(".", res.at("/query/wrap/key").asText());
        assertEquals("type:punct", res.at("/query/wrap/type").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "[punct=\".\"]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals(".", res.at("/query/wrap/key").asText());
        assertEquals("type:punct", res.at("/query/wrap/type").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
    }


    @Test
    public void testCaseSensitivityFlag () throws JsonProcessingException,
            IOException {
        query = "[orth=deutscher/i]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("deutscher", res.at("/query/wrap/key").asText());
        assertEquals(true, res.at("/query/wrap/flags:caseInsensitive")
                .isMissingNode());
        assertEquals("flags:caseInsensitive", res.at("/query/wrap/flags/0")
                .asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "deutscher/i";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("deutscher", res.at("/query/wrap/key").asText());
        assertEquals("flags:caseInsensitive", res.at("/query/wrap/flags/0")
                .asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "deutscher/I";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("deutscher", res.at("/query/wrap/key").asText());
        assertEquals(true, res.at("/query/wrap/flags/0").isMissingNode());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "[orth=deutscher/i][orth=Bundestag]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals("koral:token", operands.get(0).at("/@type").asText());
        assertEquals("deutscher", operands.get(0).at("/wrap/key").asText());
        assertEquals("orth", operands.get(0).at("/wrap/layer").asText());
        assertEquals("match:eq", operands.get(0).at("/wrap/match").asText());
        assertEquals("flags:caseInsensitive",
                operands.get(0).at("/wrap/flags/0").asText());
        assertEquals("koral:token", operands.get(1).at("/@type").asText());
        assertEquals("Bundestag", operands.get(1).at("/wrap/key").asText());
        assertEquals("orth", operands.get(1).at("/wrap/layer").asText());
        assertEquals("match:eq", operands.get(1).at("/wrap/match").asText());
        assertEquals(true, operands.get(1).at("/wrap/flags/0").isMissingNode());

        query = "deutscher/i Bundestag";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals("koral:token", operands.get(0).at("/@type").asText());
        assertEquals("deutscher", operands.get(0).at("/wrap/key").asText());
        assertEquals("orth", operands.get(0).at("/wrap/layer").asText());
        assertEquals("match:eq", operands.get(0).at("/wrap/match").asText());
        assertEquals("flags:caseInsensitive",
                operands.get(0).at("/wrap/flags/0").asText());
        assertEquals("koral:token", operands.get(1).at("/@type").asText());
        assertEquals("Bundestag", operands.get(1).at("/wrap/key").asText());
        assertEquals("orth", operands.get(1).at("/wrap/layer").asText());
        assertEquals("match:eq", operands.get(1).at("/wrap/match").asText());
        assertEquals(true, operands.get(1).at("/wrap/flags:caseInsensitive")
                .isMissingNode());
    }


    @Test
    public void testSpans () throws JsonProcessingException, IOException {
        query = "<s>";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("s", res.at("/query/wrap/key").asText());

        query = "<\".*\">";
        qs.setQuery(query, "poliqarpplus");
       res = mapper.readTree(qs.toJSON());
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals(".*", res.at("/query/wrap/key").asText());
        assertEquals("type:regex", res.at("/query/wrap/type").asText());

        query = "<vp>";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("vp", res.at("/query/wrap/key").asText());

        query = "<cnx/c=vp>";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("vp", res.at("/query/wrap/key").asText());
        assertEquals("cnx", res.at("/query/wrap/foundry").asText());
        assertEquals("c", res.at("/query/wrap/layer").asText());

        query = "<cnx/c!=vp>";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("vp", res.at("/query/wrap/key").asText());
        assertEquals("cnx", res.at("/query/wrap/foundry").asText());
        assertEquals("c", res.at("/query/wrap/layer").asText());
        assertEquals("match:ne", res.at("/query/wrap/match").asText());

        query = "<cnx/c!=vp class!=header>";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("vp", res.at("/query/wrap/key").asText());
        assertEquals("cnx", res.at("/query/wrap/foundry").asText());
        assertEquals("c", res.at("/query/wrap/layer").asText());
        assertEquals("match:ne", res.at("/query/wrap/match").asText());
        assertEquals("class", res.at("/query/attr/key").asText());
        assertEquals("header", res.at("/query/attr/value").asText());
        assertEquals("match:ne", res.at("/query/attr/match").asText());

        query = "<cnx/c!=vp !(class!=header)>";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("vp", res.at("/query/wrap/key").asText());
        assertEquals("cnx", res.at("/query/wrap/foundry").asText());
        assertEquals("c", res.at("/query/wrap/layer").asText());
        assertEquals("match:ne", res.at("/query/wrap/match").asText());
        assertEquals("class", res.at("/query/attr/key").asText());
        assertEquals("header", res.at("/query/attr/value").asText());
        assertEquals("match:eq", res.at("/query/attr/match").asText());

        query = "<cnx/c!=vp !(class=header & id=7)>";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
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
        assertEquals("match:ne", operands.get(1).at("/match").asText());
    }


    @Test
    public void testDistances () throws JsonProcessingException, IOException {
        query = "[base=der][][base=Mann]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("true", res.at("/query/inOrder").asText());
        assertEquals("koral:distance", res.at("/query/distances").elements()
                .next().at("/@type").asText());
        assertEquals("w",
                res.at("/query/distances").elements().next().at("/key")
                        .asText());
        assertEquals("koral:boundary", res.at("/query/distances").elements()
                .next().at("/boundary/@type").asText());
        assertEquals(1,
                res.at("/query/distances").elements().next()
                        .at("/boundary/min").asInt());
        assertEquals(1,
                res.at("/query/distances").elements().next()
                        .at("/boundary/max").asInt());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals("koral:token", operands.get(0).at("/@type").asText());
        assertEquals("der", operands.get(0).at("/wrap/key").asText());
        assertEquals("lemma", operands.get(0).at("/wrap/layer").asText());
        assertEquals("match:eq", operands.get(0).at("/wrap/match").asText());
        assertEquals("koral:token", operands.get(1).at("/@type").asText());
        assertEquals("Mann", operands.get(1).at("/wrap/key").asText());
        assertEquals("lemma", operands.get(1).at("/wrap/layer").asText());
        assertEquals("match:eq", operands.get(1).at("/wrap/match").asText());

        query = "[base=der][][][base=Mann]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("true", res.at("/query/inOrder").asText());
        assertEquals("koral:distance", res.at("/query/distances").elements()
                .next().at("/@type").asText());
        assertEquals("w",
                res.at("/query/distances").elements().next().at("/key")
                        .asText());
        assertEquals("koral:boundary", res.at("/query/distances").elements()
                .next().at("/boundary/@type").asText());
        assertEquals(2,
                res.at("/query/distances").elements().next()
                        .at("/boundary/min").asInt());
        assertEquals(2,
                res.at("/query/distances").elements().next()
                        .at("/boundary/max").asInt());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals("koral:token", operands.get(0).at("/@type").asText());
        assertEquals("der", operands.get(0).at("/wrap/key").asText());
        assertEquals("lemma", operands.get(0).at("/wrap/layer").asText());
        assertEquals("match:eq", operands.get(0).at("/wrap/match").asText());
        assertEquals("koral:token", operands.get(1).at("/@type").asText());
        assertEquals("Mann", operands.get(1).at("/wrap/key").asText());
        assertEquals("lemma", operands.get(1).at("/wrap/layer").asText());
        assertEquals("match:eq", operands.get(1).at("/wrap/match").asText());

        query = "[base=der][][]?[base=Mann]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("true", res.at("/query/inOrder").asText());
        assertEquals("koral:distance", res.at("/query/distances").elements()
                .next().at("/@type").asText());
        assertEquals("w",
                res.at("/query/distances").elements().next().at("/key")
                        .asText());
        assertEquals("koral:boundary", res.at("/query/distances").elements()
                .next().at("/boundary/@type").asText());
        assertEquals(1,
                res.at("/query/distances").elements().next()
                        .at("/boundary/min").asInt());
        assertEquals(2,
                res.at("/query/distances").elements().next()
                        .at("/boundary/max").asInt());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals("koral:token", operands.get(0).at("/@type").asText());
        assertEquals("der", operands.get(0).at("/wrap/key").asText());
        assertEquals("lemma", operands.get(0).at("/wrap/layer").asText());
        assertEquals("match:eq", operands.get(0).at("/wrap/match").asText());
        assertEquals("koral:token", operands.get(1).at("/@type").asText());
        assertEquals("Mann", operands.get(1).at("/wrap/key").asText());
        assertEquals("lemma", operands.get(1).at("/wrap/layer").asText());
        assertEquals("match:eq", operands.get(1).at("/wrap/match").asText());

        query = "[base=der][]+[base=Mann]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("true", res.at("/query/inOrder").asText());
        assertEquals("koral:distance", res.at("/query/distances").elements()
                .next().at("/@type").asText());
        assertEquals("w",
                res.at("/query/distances").elements().next().at("/key")
                        .asText());
        assertEquals("koral:boundary", res.at("/query/distances").elements()
                .next().at("/boundary/@type").asText());
        assertEquals(1,
                res.at("/query/distances").elements().next()
                        .at("/boundary/min").asInt());
        assertEquals(true,
                res.at("/query/distances").elements().next()
                        .at("/boundary/max").isMissingNode());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals("koral:token", operands.get(0).at("/@type").asText());
        assertEquals("der", operands.get(0).at("/wrap/key").asText());
        assertEquals("lemma", operands.get(0).at("/wrap/layer").asText());
        assertEquals("match:eq", operands.get(0).at("/wrap/match").asText());
        assertEquals("koral:token", operands.get(1).at("/@type").asText());
        assertEquals("Mann", operands.get(1).at("/wrap/key").asText());
        assertEquals("lemma", operands.get(1).at("/wrap/layer").asText());
        assertEquals("match:eq", operands.get(1).at("/wrap/match").asText());

        query = "[base=der][]*[base=Mann]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("true", res.at("/query/inOrder").asText());
        assertEquals("koral:distance", res.at("/query/distances").elements()
                .next().at("/@type").asText());
        assertEquals("w",
                res.at("/query/distances").elements().next().at("/key")
                        .asText());
        assertEquals("koral:boundary", res.at("/query/distances").elements()
                .next().at("/boundary/@type").asText());
        assertEquals(0,
                res.at("/query/distances").elements().next()
                        .at("/boundary/min").asInt());
        assertEquals(true,
                res.at("/query/distances").elements().next()
                        .at("/boundary/max").isMissingNode());

        query = "[base=der][]{2,5}[base=Mann][]?[][base=Frau]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("true", res.at("/query/inOrder").asText());
        assertEquals("koral:distance", res.at("/query/distances").elements()
                .next().at("/@type").asText());
        assertEquals("w",
                res.at("/query/distances").elements().next().at("/key")
                        .asText());
        assertEquals("koral:boundary", res.at("/query/distances").elements()
                .next().at("/boundary/@type").asText());
        assertEquals(2,
                res.at("/query/distances").elements().next()
                        .at("/boundary/min").asInt());
        assertEquals(5,
                res.at("/query/distances").elements().next()
                        .at("/boundary/max").asInt());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals("koral:token", operands.get(0).at("/@type").asText());
        assertEquals("der", operands.get(0).at("/wrap/key").asText());
        assertEquals("lemma", operands.get(0).at("/wrap/layer").asText());
        assertEquals("match:eq", operands.get(0).at("/wrap/match").asText());
        assertEquals("koral:group", operands.get(1).at("/@type").asText());
        assertEquals("operation:sequence", operands.get(1).at("/operation")
                .asText());
        assertEquals("koral:distance", operands.get(1).get("distances")
                .elements().next().at("/@type").asText());
        assertEquals("w", operands.get(1).get("distances").elements().next()
                .at("/key").asText());
        assertEquals("koral:boundary", operands.get(1).get("distances")
                .elements().next().at("/boundary/@type").asText());
        assertEquals(
                1,
                operands.get(1).get("distances").elements().next()
                        .at("/boundary/min").asInt());
        assertEquals(
                2,
                operands.get(1).get("distances").elements().next()
                        .at("/boundary/max").asInt());
        operands = Lists.newArrayList(operands.get(1).get("operands")
                .elements());
        assertEquals("Mann", operands.get(0).at("/wrap/key").asText());
        assertEquals("lemma", operands.get(0).at("/wrap/layer").asText());
        assertEquals("match:eq", operands.get(0).at("/wrap/match").asText());
        assertEquals("Frau", operands.get(1).at("/wrap/key").asText());
        assertEquals("lemma", operands.get(1).at("/wrap/layer").asText());
        assertEquals("match:eq", operands.get(1).at("/wrap/match").asText());

        query = "[base=geht][base=der][]*contains(<s>,<np>)";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals(true, res.at("/query/inOrder").isMissingNode());
        assertEquals(true, res.at("/query/distances").isMissingNode());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals("koral:token", operands.get(0).at("/@type").asText());
        assertEquals("geht", operands.get(0).at("/wrap/key").asText());
        assertEquals("lemma", operands.get(0).at("/wrap/layer").asText());
        assertEquals("match:eq", operands.get(0).at("/wrap/match").asText());
        assertEquals("koral:group", operands.get(1).at("/@type").asText());
        assertEquals("operation:sequence", operands.get(1).at("/operation")
                .asText());
        assertEquals("koral:distance", operands.get(1).get("distances")
                .elements().next().at("/@type").asText());
        assertEquals("w", operands.get(1).get("distances").elements().next()
                .at("/key").asText());
        assertEquals("koral:boundary", operands.get(1).get("distances")
                .elements().next().at("/boundary/@type").asText());
        assertEquals(
                0,
                operands.get(1).get("distances").elements().next()
                        .at("/boundary/min").asInt());
        assertEquals(true, operands.get(1).get("distances").elements().next()
                .at("/boundary/max").isMissingNode());
        operands = Lists.newArrayList(operands.get(1).get("operands")
                .elements());
        assertEquals("der", operands.get(0).at("/wrap/key").asText());
        assertEquals("lemma", operands.get(0).at("/wrap/layer").asText());
        assertEquals("match:eq", operands.get(0).at("/wrap/match").asText());
        assertEquals("koral:group", operands.get(1).at("/@type").asText());
        assertEquals("operation:position", operands.get(1).at("/operation")
                .asText());
    }


    @Test
    public void testDistancesWithClass () throws JsonProcessingException,
            IOException {
        query = "[base=der]{[]}[base=Mann]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals(true, res.at("/query/inOrder").isMissingNode());
        assertEquals(true, res.at("/query/distances").isMissingNode());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals("der", operands.get(0).at("/wrap/key").asText());
        assertEquals("Mann", operands.get(2).at("/wrap/key").asText());
        assertEquals("koral:group", operands.get(1).at("/@type").asText());
        assertEquals("operation:class", operands.get(1).at("/operation")
                .asText());
        assertEquals(1, operands.get(1).at("/classOut").asInt());
        operands = Lists.newArrayList(operands.get(1).at("/operands")
                .elements());
        assertEquals("koral:token", operands.get(0).at("/@type").asText());
        assertEquals(true, operands.get(0).at("/wrap").isMissingNode());
        assertEquals(1, res.at("/meta/highlight/0").asInt());

        query = "[base=der]{2:[]}[base=Mann]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals("operation:class", operands.get(1).at("/operation")
                .asText());
        assertEquals(2, operands.get(1).at("/classOut").asInt());
        assertEquals(2, res.at("/meta/highlight/0").asInt());

        query = "[base=der]{3:{2:[]}}";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals("operation:class", operands.get(1).at("/operation")
                .asText());
        assertEquals(3, operands.get(1).at("/classOut").asInt());
        assertEquals(3, res.at("/meta/highlight/0").asInt());
        assertEquals("operation:class",
                operands.get(1).at("/operands/0/operation").asText());
        assertEquals(2, operands.get(1).at("/operands/0/classOut").asInt());
        assertEquals(3, res.at("/meta/highlight/0").asInt());
        assertEquals(2, res.at("/meta/highlight/1").asInt());


        query = "{1:[]}[base=der][base=Mann]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals("operation:class", operands.get(0).at("/operation")
                .asText());
        assertEquals(1, operands.get(0).at("/classOut").asInt());
        assertEquals(1, res.at("/meta/highlight/0").asInt());

        query = "{1:{2:der} {3:[]} Mann}";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals(1, operands.size());  // class operation may only have one operand (the sequence)
        operands = Lists.newArrayList(operands.get(0).at("/operands")
                .elements());
        assertEquals(3, operands.size());  // the sequence has three operands ("der", "[]" and "Mann")
        assertEquals(1, res.at("/meta/highlight/0").asInt());
        assertEquals(2, res.at("/meta/highlight/1").asInt());
        assertEquals(3, res.at("/meta/highlight/2").asInt());
    }


    @Test
    public void testEmptyTokens () throws JsonProcessingException, IOException {
        query = "[]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals(true, res.at("/query/key").isMissingNode());

        query = "contains(<s>, [])";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("frames:isAround", res.at("/query/frames/0").asText());
        assertEquals(true, res.at("/query/frames/1").isMissingNode());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:token", res.at("/query/operands/1/@type").asText());
        assertEquals(true, res.at("/query/operands/1/key").isMissingNode());

        query = "contains(<s>, []{3})";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:repetition",
                res.at("/query/operands/1/operation").asText());
        assertEquals("koral:token", res
                .at("/query/operands/1/operands/0/@type").asText());
        assertEquals(true, res.at("/query/operands/1/operands/0/key")
                .isMissingNode());

        query = "contains(<s>, {1:[]{3}})";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/1/operation")
                .asText());
        assertEquals("operation:repetition",
                res.at("/query/operands/1/operands/0/operation").asText());
        assertEquals("koral:token",
                res.at("/query/operands/1/operands/0/operands/0/@type")
                        .asText());
        assertEquals(true, res
                .at("/query/operands/1/operands/0/operands/0/key")
                .isMissingNode());

    }


    @Test
    public void testLeadingTrailingEmptyTokens ()
            throws JsonProcessingException, IOException {
        query = "[][base=Mann]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals("koral:token", operands.get(0).at("/@type").asText());
        assertEquals(true, operands.get(0).at("/key").isMissingNode());

        query = "[][][base=Mann]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        operands = Lists.newArrayList(res.at("/query/operands").elements());
        assertEquals("koral:group", operands.get(0).at("/@type").asText());
        assertEquals("operation:repetition", operands.get(0).at("/operation")
                .asText());
        assertEquals(2, operands.get(0).at("/boundary/min").asInt());
        assertEquals(2, operands.get(0).at("/boundary/max").asInt());
        operands = Lists.newArrayList(operands.get(0).at("/operands")
                .elements());
        assertEquals("koral:token", operands.get(0).at("/@type").asText());
        assertEquals(true, operands.get(0).at("/key").isMissingNode());

        query = "startswith(<s>, [][base=Mann])";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        operands = Lists.newArrayList(res.at("/query/operands"));
        operands = Lists.newArrayList(operands.get(1).at("/operands"));
        assertEquals("koral:token", operands.get(0).at("/@type").asText());
        assertEquals(true, operands.get(0).at("/key").isMissingNode());
    }


    @Test
    public void testRepetition () throws JsonProcessingException, IOException {
        query = "der{3}";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:repetition", res.at("/query/operation")
                .asText());
        assertEquals("der", res.at("/query/operands/0/wrap/key").asText());
        assertEquals(3, res.at("/query/boundary/min").asInt());
        assertEquals(3, res.at("/query/boundary/max").asInt());

        query = "der{,3}";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals(0, res.at("/query/boundary/min").asInt());
        assertEquals(3, res.at("/query/boundary/max").asInt());

        query = "der{3,}";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals(3, res.at("/query/boundary/min").asInt());
        assertEquals(true, res.at("/query/boundary/max").isMissingNode());

        query = "der{3,7}";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals(3, res.at("/query/boundary/min").asInt());
        assertEquals(7, res.at("/query/boundary/max").asInt());

        query = "der*";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals(0, res.at("/query/boundary/min").asInt());
        assertEquals(true, res.at("/query/boundary/max").isMissingNode());

        query = "der+";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals(1, res.at("/query/boundary/min").asInt());
        assertEquals(true, res.at("/query/boundary/max").isMissingNode());
    };


    @Test
    public void testGroupRepetition () throws JsonProcessingException,
            IOException {
        query = "contains(<s>, (der){3})";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:repetition",
                res.at("/query/operands/1/operation").asText());

        query = "contains(<s>, (der){3,})";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals(3, res.at("/query/operands/1/boundary/min").asInt());
        assertEquals(true, res.at("/query/operands/1/boundary/max")
                .isMissingNode());

        query = "contains(<s>, (der){,3})";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals(0, res.at("/query/operands/1/boundary/min").asInt());
        assertEquals(3, res.at("/query/operands/1/boundary/max").asInt());

        query = "contains(<s>, (der){3,7})";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals(3, res.at("/query/operands/1/boundary/min").asInt());
        assertEquals(7, res.at("/query/operands/1/boundary/max").asInt());

        query = "contains(<s>, (der)*)";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals(0, res.at("/query/operands/1/boundary/min").asInt());
        assertEquals(true, res.at("/query/operands/1/boundary/max")
                .isMissingNode());
    };


    @Test
    public void testPositions () throws JsonProcessingException, IOException {
        query = "contains(<s>, der)";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("frames:isAround", res.at("/query/frames/0").asText());
        assertEquals(true, res.at("/query/frames/1").isMissingNode());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:token", res.at("/query/operands/1/@type").asText());

        query = "contains(<s>,<np>)";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("np", res.at("/query/operands/1/wrap/key").asText());

        query = "contains(<s>,[orth=der][orth=Mann])";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("frames:isAround", res.at("/query/frames/0").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/1/operation").asText());
        assertEquals("der", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("Mann", res.at("/query/operands/1/operands/1/wrap/key")
                .asText());

        query = "contains(<s>,[orth=der][orth=Mann]*)";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("frames:isAround", res.at("/query/frames/0").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/1/operation").asText());
        assertEquals("der", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("operation:repetition",
                res.at("/query/operands/1/operands/1/operation").asText());
        assertEquals(0, res.at("/query/operands/1/operands/1/boundary/min")
                .asInt());
        assertEquals(true, res.at("/query/operands/1/operands/1/boundary/max")
                .isMissingNode());
        assertEquals("Mann",
                res.at("/query/operands/1/operands/1/operands/0/wrap/key")
                        .asText());

        query = "contains(<s>,startswith(<np>,<pp>))";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("frames:startsWith", res.at("/query/operands/1/frames/0")
                .asText());
        assertEquals("operation:position", res
                .at("/query/operands/1/operation").asText());
        assertEquals("np", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("pp", res.at("/query/operands/1/operands/1/wrap/key")
                .asText());

        query = "[base=Auto]overlaps(<s>, der)";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:position", res
                .at("/query/operands/1/operation").asText());
        assertEquals("frames:overlapsLeft", res
                .at("/query/operands/1/frames/0").asText());
        assertEquals("frames:overlapsRight",
                res.at("/query/operands/1/frames/1").asText());
        assertEquals("koral:span", res.at("/query/operands/1/operands/0/@type")
                .asText());
        assertEquals("s", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("koral:token", res
                .at("/query/operands/1/operands/1/@type").asText());

        query = "[base=Auto]            overlaps(<s>, der)";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:position", res
                .at("/query/operands/1/operation").asText());
        assertEquals("frames:overlapsLeft", res
                .at("/query/operands/1/frames/0").asText());
        assertEquals("frames:overlapsRight",
                res.at("/query/operands/1/frames/1").asText());
        assertEquals("koral:span", res.at("/query/operands/1/operands/0/@type")
                .asText());
        assertEquals("s", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("koral:token", res
                .at("/query/operands/1/operands/1/@type").asText());
    };


    @Test
    public void testCoordinatedFields () throws JsonProcessingException,
            IOException {
        query = "[base=Mann&(cas=N|cas=A)]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("relation:and", res.at("/query/wrap/relation").asText());
        assertEquals("Mann", res.at("/query/wrap/operands/0/key").asText());
        assertEquals("lemma", res.at("/query/wrap/operands/0/layer").asText());
        assertEquals("koral:termGroup", res.at("/query/wrap/operands/1/@type")
                .asText());
        assertEquals("relation:or", res.at("/query/wrap/operands/1/relation")
                .asText());
        assertEquals("N", res.at("/query/wrap/operands/1/operands/0/key")
                .asText());
        assertEquals("cas", res.at("/query/wrap/operands/1/operands/0/layer")
                .asText());
        assertEquals("A", res.at("/query/wrap/operands/1/operands/1/key")
                .asText());
        assertEquals("cas", res.at("/query/wrap/operands/1/operands/1/layer")
                .asText());

        query = "[base=Mann&cas=N&gen=m]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("relation:and", res.at("/query/wrap/relation").asText());
        assertEquals("Mann", res.at("/query/wrap/operands/0/key").asText());
        assertEquals("lemma", res.at("/query/wrap/operands/0/layer").asText());
        assertEquals("koral:termGroup", res.at("/query/wrap/operands/1/@type")
                .asText());
        assertEquals("relation:and", res.at("/query/wrap/operands/1/relation")
                .asText());
        assertEquals("N", res.at("/query/wrap/operands/1/operands/0/key")
                .asText());
        assertEquals("cas", res.at("/query/wrap/operands/1/operands/0/layer")
                .asText());
        assertEquals("m", res.at("/query/wrap/operands/1/operands/1/key")
                .asText());
        assertEquals("gen", res.at("/query/wrap/operands/1/operands/1/layer")
                .asText());
    }

    @Test
    public void testUnnecessaryParentheses () throws JsonProcessingException,
            IOException {
        query = "[(base=Mann)]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("Mann", res.at("/query/wrap/key").asText());
        assertEquals("lemma", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "[(((base=Mann)))]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("Mann", res.at("/query/wrap/key").asText());
        assertEquals("lemma", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "[(base=Mann&cas=N)]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:termGroup", res.at("/query/wrap/@type")
                .asText());
        assertEquals("relation:and", res.at("/query/wrap/relation")
                     .asText());
        assertEquals("Mann", res.at("/query/wrap/operands/0/key")
                     .asText());
        assertEquals("lemma", res.at("/query/wrap/operands/0/layer")
                     .asText());
        assertEquals("N", res.at("/query/wrap/operands/1/key")
                     .asText());
        assertEquals("cas", res.at("/query/wrap/operands/1/layer")
                     .asText());


        query = "[(((base=Mann&cas=N)))]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:termGroup", res.at("/query/wrap/@type")
                .asText());
        assertEquals("relation:and", res.at("/query/wrap/relation")
                     .asText());
        assertEquals("Mann", res.at("/query/wrap/operands/0/key")
                     .asText());
        assertEquals("lemma", res.at("/query/wrap/operands/0/layer")
                     .asText());
        assertEquals("N", res.at("/query/wrap/operands/1/key")
                     .asText());
        assertEquals("cas", res.at("/query/wrap/operands/1/layer")
                     .asText());


        query = "[(((base=Mann&((cas=N)))))]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:termGroup", res.at("/query/wrap/@type")
                .asText());
        assertEquals("relation:and", res.at("/query/wrap/relation")
                     .asText());
        assertEquals("Mann", res.at("/query/wrap/operands/0/key")
                     .asText());
        assertEquals("lemma", res.at("/query/wrap/operands/0/layer")
                     .asText());
        assertEquals("N", res.at("/query/wrap/operands/1/key")
                     .asText());
        assertEquals("cas", res.at("/query/wrap/operands/1/layer")
                     .asText());

    };

    @Test
    public void testTokenSequence () throws JsonProcessingException,
            IOException {
        query = "[base=Mann][orth=Frau]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("Mann", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("lemma", res.at("/query/operands/0/wrap/layer").asText());
        assertEquals("Frau", res.at("/query/operands/1/wrap/key").asText());
        assertEquals("orth", res.at("/query/operands/1/wrap/layer").asText());

        query = "[base=Mann][orth=Frau][p=NN]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("NN", res.at("/query/operands/2/wrap/key").asText());
        assertEquals("p", res.at("/query/operands/2/wrap/layer").asText());

        query = "[base=Mann][orth=Frau][p=NN][foo=bar]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("bar", res.at("/query/operands/3/wrap/key").asText());
        assertEquals("foo", res.at("/query/operands/3/wrap/layer").asText());
    }


    @Test
    public void testDisjSegments () throws JsonProcessingException, IOException {
        query = "[base=der]|[base=das]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:disjunction", res.at("/query/operation")
                .asText());
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:token", res.at("/query/operands/1/@type").asText());
        assertEquals("der", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("lemma", res.at("/query/operands/0/wrap/layer").asText());
        assertEquals("das", res.at("/query/operands/1/wrap/key").asText());
        assertEquals("lemma", res.at("/query/operands/1/wrap/layer").asText());

        query = "([base=der]|[base=das])[base=Schild]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("Schild", res.at("/query/operands/1/wrap/key").asText());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:disjunction",
                res.at("/query/operands/0/operation").asText());

        query = "[base=Schild]([base=der]|[base=das])";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("Schild", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:disjunction",
                res.at("/query/operands/1/operation").asText());

        query = "([orth=der][base=katze])|([orth=eine][base=baum])";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:disjunction", res.at("/query/operation")
                .asText());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/0/operation").asText());
        assertEquals("koral:token", res
                .at("/query/operands/0/operands/0/@type").asText());
        assertEquals("der", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("katze", res.at("/query/operands/0/operands/1/wrap/key")
                .asText());
        assertEquals("eine", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("baum", res.at("/query/operands/1/operands/1/wrap/key")
                .asText());

        query = "[orth=der][base=katze]|[orth=eine][base=baum]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:disjunction", res.at("/query/operation")
                .asText());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/0/operation").asText());
        assertEquals("koral:token", res
                .at("/query/operands/0/operands/0/@type").asText());
        assertEquals("der", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("katze", res.at("/query/operands/0/operands/1/wrap/key")
                .asText());
        assertEquals("eine", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("baum", res.at("/query/operands/1/operands/1/wrap/key")
                .asText());

        query = "[orth=der]([base=katze]|[orth=eine])[base=baum]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:disjunction",
                res.at("/query/operands/1/operation").asText());
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:token", res.at("/query/operands/2/@type").asText());
        assertEquals("der", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("katze", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("eine", res.at("/query/operands/1/operands/1/wrap/key")
                .asText());
        assertEquals("baum", res.at("/query/operands/2/wrap/key").asText());

        query = "[orth=der][base=katze]|[orth=der][base=hund]|[orth=der][base=baum]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("der", res.at("/query/operands/2/operands/0/wrap/key")
                .asText());
        assertEquals("baum", res.at("/query/operands/2/operands/1/wrap/key")
                .asText());

        query = "[orth=der]([base=katze]|[base=hund]|[base=baum])";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:disjunction",
                res.at("/query/operands/1/operation").asText());
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:token", res
                .at("/query/operands/1/operands/0/@type").asText());
        assertEquals("koral:token", res
                .at("/query/operands/1/operands/1/@type").asText());
        assertEquals("koral:token", res
                .at("/query/operands/1/operands/2/@type").asText());
        assertEquals("katze", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("hund", res.at("/query/operands/1/operands/1/wrap/key")
                .asText());
        assertEquals("baum", res.at("/query/operands/1/operands/2/wrap/key")
                .asText());
    }


    @Test
    public void testTokenSpanSequence () throws JsonProcessingException,
            IOException {
        query = "[base=Mann]<vp>";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("Mann", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:span", res.at("/query/operands/1/@type").asText());
        assertEquals("vp", res.at("/query/operands/1/wrap/key").asText());

        query = "<vp>[base=Mann]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("vp", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:token", res.at("/query/operands/1/@type").asText());
        assertEquals("Mann", res.at("/query/operands/1/wrap/key").asText());

        query = "<vp>[base=Mann]<pp>";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:span", res.at("/query/operands/2/@type").asText());
        assertEquals("pp", res.at("/query/operands/2/wrap/key").asText());

        query = "<vp>[base=Mann]<pp><np>";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("pp", res.at("/query/operands/2/wrap/key").asText());
        assertEquals("np", res.at("/query/operands/3/wrap/key").asText());
    }


    @Test
    public void testClasses () throws JsonProcessingException, IOException {
        query = "{[base=Mann]}";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:class", res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/classOut").asInt());
        assertEquals(true, res.at("/query/classIn").isMissingNode());
        assertEquals("Mann", res.at("/query/operands/0/wrap/key").asText());

        query = "{[base=Mann][orth=Frau]}";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:class", res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/classOut").asInt());
        assertEquals(true, res.at("/query/classIn").isMissingNode());
        assertEquals("Mann", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("Frau", res.at("/query/operands/0/operands/1/wrap/key")
                .asText());

        query = "{[base=Mann]}{[orth=Frau]}";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("operation:class", res.at("/query/operands/0/operation")
                .asText());
        assertEquals(1, res.at("/query/operands/0/classOut").asInt());
        assertEquals("operation:class", res.at("/query/operands/1/operation")
                .asText());
        assertEquals(1, res.at("/query/operands/1/classOut").asInt());
        assertEquals(true, res.at("/query/classIn").isMissingNode());
        assertEquals("Mann", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("Frau", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());

        query = "[p=NN]{[base=Mann][orth=Frau]}";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/1/operation")
                .asText());
        assertEquals(1, res.at("/query/operands/1/classOut").asInt());
        assertEquals(true, res.at("/query/operands/1/classIn").isMissingNode());
        assertEquals("Mann",
                res.at("/query/operands/1/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals("Frau",
                res.at("/query/operands/1/operands/0/operands/1/wrap/key")
                        .asText());

        query = "{[base=Mann][orth=Frau]}[p=NN]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/0/operation")
                .asText());
        assertEquals(1, res.at("/query/operands/0/classOut").asInt());
        assertEquals(true, res.at("/query/operands/0/classIn").isMissingNode());
        assertEquals("Mann",
                res.at("/query/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals("Frau",
                res.at("/query/operands/0/operands/0/operands/1/wrap/key")
                        .asText());
        assertEquals(1, res.at("/meta/highlight/0").asInt());

        query = "{2:{1:[tt/p=ADJA]}[mate/p=NN]}";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:class", res.at("/query/operation").asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/0/operation").asText());
        assertEquals(2, res.at("/query/classOut").asInt());
        assertEquals(1, res.at("/query/operands/0/operands/0/classOut").asInt());
        assertEquals(2, res.at("/meta/highlight/0").asInt());
        assertEquals(1, res.at("/meta/highlight/1").asInt());
    }


    @Test
    public void testFocusSplit () throws JsonProcessingException, IOException {
        query = "focus([orth=Der]{[orth=Mann]})";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/classRef/0").asInt());
        assertEquals("operation:sequence", res
                .at("/query/operands/0/operation").asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals(1, res.at("/query/operands/0/operands/1/classOut").asInt());
        assertEquals("Mann",
                res.at("/query/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals(1, res.at("/meta/highlight/0").asInt());

        query = "focus([orth=Der]{[orth=Mann][orth=geht]})";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("operation:sequence",
                res.at("/query/operands/0/operands/1/operands/0/operation")
                        .asText());
        assertEquals(
                "Mann",
                res.at("/query/operands/0/operands/1/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(
                "geht",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/wrap/key")
                        .asText());

        query = "focus(2:[orth=Der]{2:[orth=Mann][orth=geht]})";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals(2, res.at("/query/classRef/0").asInt());
        assertEquals(2, res.at("/query/operands/0/operands/1/classOut").asInt());
        assertEquals(2, res.at("/meta/highlight/0").asInt());

        query = "focus(3:startswith(<s>,{3:<np>}))";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals(3, res.at("/query/classRef/0").asInt());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals("operation:position", res
                .at("/query/operands/0/operation").asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals(3, res.at("/query/operands/0/operands/1/classOut").asInt());
        assertEquals("frames:startsWith", res.at("/query/operands/0/frames/0")
                .asText());
        assertEquals(3, res.at("/meta/highlight/0").asInt());

        query = "focus(1000:startswith(<s>,{1000:<np>}))";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals(1000, res.at("/query/classRef/0").asInt());
        assertEquals(1000, res.at("/query/operands/0/operands/1/classOut")
                .asInt());
        assertEquals(1000, res.at("/meta/highlight/0").asInt());

        query = "focus(3: startswith(<s>, {3:[base=der]{1:[mate/p=ADJA]{2:[tt/p=NN]}}}))";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals(3, res.at("/query/classRef/0").asInt());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals("operation:position", res
                .at("/query/operands/0/operation").asText());
        assertEquals("frames:startsWith", res.at("/query/operands/0/frames/0")
                .asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals(3, res.at("/query/operands/0/operands/1/classOut").asInt());
        assertEquals("operation:sequence",
                res.at("/query/operands/0/operands/1/operands/0/operation")
                        .asText());
        assertEquals(
                "operation:class",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operation")
                        .asText());
        assertEquals(
                1,
                res.at("/query/operands/0/operands/1/operands/0/operands/1/classOut")
                        .asInt());
        assertEquals(
                "operation:sequence",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/0/operation")
                        .asText());
        assertEquals(
                "operation:class",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/0/operands/1/operation")
                        .asText());
        assertEquals(
                2,
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/0/operands/1/classOut")
                        .asInt());

        query = "split(3: startswith(<s>, {3:[base=der]{1:[mate/p=ADJA]{2:[tt/p=NN]}}}))";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals(3, res.at("/query/classRef/0").asInt());
        assertEquals(true, res.at("/query/classRef/1").isMissingNode());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:split", res.at("/query/operation").asText());

        query = "split(2|3: startswith(<s>, {3:[base=der]{1:[mate/p=ADJA]{2:[tt/p=NN]}}}))";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals(2, res.at("/query/classRef/0").asInt());
        assertEquals(3, res.at("/query/classRef/1").asInt());
        assertEquals("classRefOp:intersection", res.at("/query/classRefOp")
                .asText());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:split", res.at("/query/operation").asText());
        assertEquals(3, res.at("/meta/highlight/0").asInt());
        assertEquals(1, res.at("/meta/highlight/1").asInt());
        assertEquals(2, res.at("/meta/highlight/2").asInt());

        query = "focus(1:{[base=der]}{1:[pos=ADJA]})";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals(1, res.at("/query/classRef/0").asInt());
        assertEquals(1, res.at("/query/operands/0/operands/0/classOut").asInt());
        assertEquals(1, res.at("/query/operands/0/operands/1/classOut").asInt());
    }


    @Test
    public void testSubmatch () throws JsonProcessingException, IOException {
        query = "submatch(1:<s>)";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/spanRef/0").asInt());
        assertEquals(true, res.at("/query/spanRef/1").isMissingNode());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());

        query = "submatch(1,4:<s>)";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/spanRef/0").asInt());
        assertEquals(4, res.at("/query/spanRef/1").asInt());

        query = "submatch(1,4:contains(<s>,[base=Haus]))";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/spanRef/0").asInt());
        assertEquals(4, res.at("/query/spanRef/1").asInt());
        assertEquals("frames:isAround", res.at("/query/operands/0/frames/0")
                .asText());
        assertEquals("s", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("Haus", res.at("/query/operands/0/operands/1/wrap/key")
                .asText());
    }


    @Test
    public void testRelations () throws JsonProcessingException, IOException {
        query = "dominates(<s>,<np>)";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:relation", res.at("/query/operation").asText());
        assertEquals("koral:relation", res.at("/query/relation/@type").asText());
        assertEquals("c", res.at("/query/relation/wrap/layer").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("np", res.at("/query/operands/1/wrap/key").asText());

        query = "relatesTo([base=Baum],<np>)";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:relation", res.at("/query/operation").asText());
        assertEquals("koral:relation", res.at("/query/relation/@type").asText());
        assertEquals("Baum", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("np", res.at("/query/operands/1/wrap/key").asText());

        query = "relatesTo(Baum,<np>)";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("orth", res.at("/query/operands/0/wrap/layer").asText());
        assertEquals("Baum", res.at("/query/operands/0/wrap/key").asText());

        query = "relatesTo(mate/d=HEAD:<np>,[base=Baum])";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("lemma", res.at("/query/operands/1/wrap/layer").asText());
        assertEquals("Baum", res.at("/query/operands/1/wrap/key").asText());
        assertEquals("koral:relation", res.at("/query/relation/@type").asText());
        assertEquals("mate", res.at("/query/relation/wrap/foundry").asText());
        assertEquals("d", res.at("/query/relation/wrap/layer").asText());
        assertEquals("HEAD", res.at("/query/relation/wrap/key").asText());

        query = "dependency([base=fällen],[base=Baum])";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("lemma", res.at("/query/operands/0/wrap/layer").asText());
        assertEquals("fällen", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("lemma", res.at("/query/operands/1/wrap/layer").asText());
        assertEquals("Baum", res.at("/query/operands/1/wrap/key").asText());
        assertEquals("koral:relation", res.at("/query/relation/@type").asText());
        assertEquals("d", res.at("/query/relation/wrap/layer").asText());

        query = "dominates(Baum,<np>)";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("orth", res.at("/query/operands/0/wrap/layer").asText());
        assertEquals("Baum", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:relation", res.at("/query/relation/@type").asText());
        assertEquals("c", res.at("/query/relation/wrap/layer").asText());

        query = "dominates(cnx/c:<vp>,<np>)";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("cnx", res.at("/query/relation/wrap/foundry").asText());
        assertEquals("c", res.at("/query/relation/wrap/layer").asText());

        query = "dominates(cnx/c*:<vp>,<np>)";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("cnx", res.at("/query/relation/wrap/foundry").asText());
        assertEquals("c", res.at("/query/relation/wrap/layer").asText());
        assertEquals(0, res.at("/query/relation/boundary/min").asInt());
        assertEquals(true, res.at("/query/relation/boundary/max")
                .isMissingNode());

        query = "dominates(cnx/c{1,5}:<vp>,<np>)";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals(1, res.at("/query/relation/boundary/min").asInt());
        assertEquals(5, res.at("/query/relation/boundary/max").asInt());

        query = "dominates(cnx/c{,5}:<vp>,<np>)";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals(0, res.at("/query/relation/boundary/min").asInt());
        assertEquals(5, res.at("/query/relation/boundary/max").asInt());

        query = "dominates(cnx/c{5}:<vp>,<np>)";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals(5, res.at("/query/relation/boundary/min").asInt());
        assertEquals(5, res.at("/query/relation/boundary/max").asInt());
    }


    @Test
    public void testAlign () throws JsonProcessingException, IOException {
        query = "[orth=der]^[orth=Mann]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("der", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals(1, res.at("/query/operands/0/classOut").asInt());
        assertEquals("Mann", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("operation:class", res.at("/query/operands/1/operation")
                .asText());
        assertEquals(2, res.at("/query/operands/1/classOut").asInt());
        assertEquals(1, res.at("/meta/alignment/0/0").asInt());
        assertEquals(2, res.at("/meta/alignment/0/1").asInt());

        query = "[orth=der]^[orth=große][orth=Mann]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("der", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals(1, res.at("/query/operands/0/classOut").asInt());
        assertEquals("große", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("operation:class", res.at("/query/operands/1/operation")
                .asText());
        assertEquals(2, res.at("/query/operands/1/classOut").asInt());
        assertEquals("Mann", res.at("/query/operands/2/wrap/key").asText());
        assertEquals(1, res.at("/meta/alignment/0/0").asInt());
        assertEquals(2, res.at("/meta/alignment/0/1").asInt());


        query = "([base=a]^[base=b])|[base=c]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("operation:disjunction", res.at("/query/operation")
                .asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/0/operation").asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals("a",
                res.at("/query/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals("b",
                res.at("/query/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals("c", res.at("/query/operands/1/wrap/key").asText());
        assertEquals(1, res.at("/query/operands/0/operands/0/classOut").asInt());
        assertEquals(2, res.at("/query/operands/0/operands/1/classOut").asInt());
        assertEquals(1, res.at("/meta/alignment/0/0").asInt());
        assertEquals(2, res.at("/meta/alignment/0/1").asInt());

        query = "([base=a]^[base=b][base=c])|[base=d]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("a",
                res.at("/query/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals("b",
                res.at("/query/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals("c", res.at("/query/operands/0/operands/2/wrap/key")
                .asText());
        assertEquals("d", res.at("/query/operands/1/wrap/key").asText());

        query = "([base=a]^[base=b]^[base=c])|[base=d]";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("a",
                res.at("/query/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(1, res.at("/query/operands/0/operands/0/classOut").asInt());
        assertEquals("b",
                res.at("/query/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals(2, res.at("/query/operands/0/operands/1/classOut").asInt());
        assertEquals("c",
                res.at("/query/operands/0/operands/2/operands/0/wrap/key")
                        .asText());
        assertEquals(3, res.at("/query/operands/0/operands/2/classOut").asInt());
        assertEquals("d", res.at("/query/operands/1/wrap/key").asText());
        assertEquals(1, res.at("/meta/alignment/0/0").asInt());
        assertEquals(2, res.at("/meta/alignment/0/1").asInt());
        assertEquals(2, res.at("/meta/alignment/1/0").asInt());
        assertEquals(3, res.at("/meta/alignment/1/1").asInt());

        query = "^ Mann";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("Mann", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("operation:class", res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/classOut").asInt());
        assertEquals(-1, res.at("/meta/alignment/0/0").asInt());
        assertEquals(1, res.at("/meta/alignment/0/1").asInt());

        query = "Mann ^";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("Mann", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("operation:class", res.at("/query/operation").asText());
        assertEquals(1, res.at("/query/classOut").asInt());
        assertEquals(1, res.at("/meta/alignment/0/0").asInt());
        assertEquals(-1, res.at("/meta/alignment/0/1").asInt());
    }


    @Test
    public void testSimpleQueries () throws JsonProcessingException,
            IOException {
        query = "Baum";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("Baum", res.at("/query/wrap/key").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "Der Baum";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("koral:term", res.at("/query/operands/0/wrap/@type")
                .asText());
        assertEquals("Der", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("Baum", res.at("/query/operands/1/wrap/key").asText());
        assertEquals("orth", res.at("/query/operands/0/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/operands/0/wrap/match")
                .asText());
        assertEquals("orth", res.at("/query/operands/1/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/operands/1/wrap/match")
                .asText());

        query = "Der große Baum";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("Der", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("große", res.at("/query/operands/1/wrap/key").asText());
        assertEquals("Baum", res.at("/query/operands/2/wrap/key").asText());

        query = "Der (große|kleine) Baum";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("Der", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("operation:disjunction",
                res.at("/query/operands/1/operation").asText());
        assertEquals("große", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("kleine", res.at("/query/operands/1/operands/1/wrap/key")
                .asText());
        assertEquals("Baum", res.at("/query/operands/2/wrap/key").asText());

        query = "der große Baum | der kleine Baum";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("operation:disjunction", res.at("/query/operation")
                .asText());
        assertEquals("der", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("große", res.at("/query/operands/0/operands/1/wrap/key")
                .asText());
        assertEquals("Baum", res.at("/query/operands/0/operands/2/wrap/key")
                .asText());
        assertEquals("der", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("kleine", res.at("/query/operands/1/operands/1/wrap/key")
                .asText());
        assertEquals("Baum", res.at("/query/operands/1/operands/2/wrap/key")
                .asText());

        query = "Der [p=ADJA] Baum";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("Der", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("ADJA", res.at("/query/operands/1/wrap/key").asText());
        assertEquals("p", res.at("/query/operands/1/wrap/layer").asText());
        assertEquals("Baum", res.at("/query/operands/2/wrap/key").asText());
    }


    @Test
    public void testWithin () throws JsonProcessingException, IOException {
        query = "[p=VVFIN] within s";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("frames:isAround", res.at("/query/frames/0").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("VVFIN", res.at("/query/operands/1/wrap/key").asText());
    }


    @Test
    public void testSpanSerialization () throws JsonProcessingException,
            IOException {

        // Both constructs should be serialized identically
        query = "contains(<s>, der)";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("frames:isAround", res.at("/query/frames/0").asText());
        assertEquals(true, res.at("/query/frames/1").isMissingNode());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:token", res.at("/query/operands/1/@type").asText());

        query = "der within s";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:position", res.at("/query/operation").asText());
        assertEquals("frames:isAround", res.at("/query/frames/0").asText());
        assertEquals(true, res.at("/query/frames/1").isMissingNode());
        assertEquals("koral:span", res.at("/query/operands/0/@type").asText());
        assertEquals("s", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:token", res.at("/query/operands/1/@type").asText());
    }


    @Test
    public void testMeta () throws JsonProcessingException, IOException {
        query = "x meta textClass=Sport";
        qs.setQuery(query, "poliqarpplus");
        res = mapper.readTree(qs.toJSON());
        assertEquals("x", res.at("/query/wrap/key").asText());
        assertEquals("koral:doc", res.at("/collection/@type").asText());
        assertEquals("textClass", res.at("/collection/key").asText());
        assertEquals("Sport", res.at("/collection/value").asText());

        query = "x meta textClass=Sport";
        qs.setQuery(query, "poliqarpplus");
        qs.setCollection("author=Smith");
        res = mapper.readTree(qs.toJSON());
        assertEquals("x", res.at("/query/wrap/key").asText());
        assertEquals("koral:docGroup", res.at("/collection/@type").asText());
        assertEquals("operation:and", res.at("/collection/operation").asText());
        assertEquals("textClass", res.at("/collection/operands/0/key").asText());
        assertEquals("Sport", res.at("/collection/operands/0/value").asText());
        assertEquals("author", res.at("/collection/operands/1/key").asText());
        assertEquals("Smith", res.at("/collection/operands/1/value").asText());

        // TODO more tests
    }
}
