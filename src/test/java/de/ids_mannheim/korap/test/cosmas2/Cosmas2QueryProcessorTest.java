package de.ids_mannheim.korap.test.cosmas2;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ids_mannheim.korap.query.serialize.QuerySerializer;
import de.ids_mannheim.korap.query.serialize.util.StatusCodes;

import static org.junit.Assert.*;

import static de.ids_mannheim.korap.query.parse.cosmas.c2ps_opREG.*;
import de.ids_mannheim.korap.util.StringUtils;

/**
 * Tests for JSON-LD serialization of Cosmas II queries.
 * 
 * @author Joachim Bingel (bingel@ids-mannheim.de)
 * @author Nils Diewald
 * @author Franck Bodmer
 * @version 1.2 - 21.09.23
 */
public class Cosmas2QueryProcessorTest {


	// default layer for part of speech:
    private static final String LAYER_POS = "p";
    private static final boolean showParserErrorQuery = true;	// print a line around those queries which get "no viable alternative" Parser warnings: 
    															// the parser warnings are intentional to test syntax errors.
    															// the parser error for "Der:sa" is corrected in another issue.
	String query;
    ArrayList<JsonNode> operands;

    QuerySerializer qs = new QuerySerializer(1.1);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode res;

    @Test
    public void testContext () throws JsonProcessingException, IOException {
        String contextString = "http://korap.ids-mannheim.de/ns/koral/0.3/context.jsonld";

        query = "foo";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals(contextString, res.get("@context").asText());        
    }


    @Test
    public void testSingleToken () throws JsonProcessingException, IOException {
        query = "der";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("der", res.at("/query/wrap/key").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
        
        query = "&Mann";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("Mann", res.at("/query/wrap/key").asText());
        assertEquals("lemma", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());
        
        /* check Lemma with extended opts and with wildcard '+' inside options.
         * 09.12.24/FB
         */

        query = "&COSFes-&Prüfung";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());

        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("COSFes-&Prüfung", res.at("/query/wrap/key").asText());
        assertEquals("lemma", res.at("/query/wrap/layer").asText());

        query = "&COSFes+&Prüfung";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());

        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("COSFes+&Prüfung", res.at("/query/wrap/key").asText());
        assertEquals("lemma", res.at("/query/wrap/layer").asText());
        
        /* syntax error: reject wildcards in lemma :
         */
        query = "&COS&Prüfung+";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());

        assertTrue(res.get("errors") != null);
        assertEquals(res.get("errors").get(0).get(0).asInt(), StatusCodes.ERR_LEM_WILDCARDS);
        
        query = "&Pr?fung*";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());

        assertTrue(res.get("errors") != null);
        assertEquals(res.get("errors").get(0).get(0).asInt(), StatusCodes.ERR_LEM_WILDCARDS);

    }



    @Test
    public void testWildcardToken () throws JsonProcessingException,
            IOException {
        query = "*der";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("type:regex", res.at("/query/wrap/type").asText());
        assertEquals(".*der", res.at("/query/wrap/key").asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "*de?r";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals(".*de.r", res.at("/query/wrap/key").asText());
        
        query = "*de+r";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals(".*de.?r", res.at("/query/wrap/key").asText());

        query = "*de+?r";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals(".*de.?.r", res.at("/query/wrap/key").asText());        
    }


    //	
    @Test
    public void testCaseSensitivityFlag () throws JsonProcessingException,
            IOException {
        query = "$deutscher";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:term", res.at("/query/wrap/@type").asText());
        assertEquals("deutscher", res.at("/query/wrap/key").asText());
        assertEquals("flags:caseInsensitive", res.at("/query/wrap/flags/0")
                .asText());
        assertEquals("orth", res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/wrap/match").asText());

        query = "$deutscher Bundestag";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("koral:term", res.at("/query/operands/0/wrap/@type")
                .asText());
        assertEquals("deutscher", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("flags:caseInsensitive",
                res.at("/query/operands/0/wrap/flags/0").asText());
        assertEquals("orth", res.at("/query/operands/0/wrap/layer").asText());
        assertEquals("match:eq", res.at("/query/operands/0/wrap/match")
                .asText());
        assertEquals("Bundestag", res.at("/query/operands/1/wrap/key").asText());
    }


    @Test
    public void testMORPH () throws JsonProcessingException, IOException {

    	/*
    	 *  MORPH( expr ) are translated from c2-style STTS to original STTS,
    	 *    e.g. MORPH(VRB fin a) -> VAFIN.
    	 */
    	
        query = "MORPH(VRB)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", 	res.at("/query/wrap/@type").asText());
        assertEquals("match:eq", 	res.at("/query/wrap/match").asText());
        assertEquals("p", 			res.at("/query/wrap/layer").asText());
        assertEquals("V.*", 		res.at("/query/wrap/key").asText());

        query = "MORPH(VRB fin a)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", 		res.at("/query/@type").asText());
        assertEquals("koral:termGroup", 	res.at("/query/wrap/@type").asText());
        assertEquals("relation:and", 		res.at("/query/wrap/relation").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/0/@type").asText());
        assertEquals("match:eq", 			res.at("/query/wrap/operands/0/match").asText());
        assertEquals("p", 					res.at("/query/wrap/operands/0/layer").asText());
        assertEquals("VAFIN", 				res.at("/query/wrap/operands/0/key").asText());
        
        // STTS: although imperative verbs are finit verb, STTS has another tag for it:
        
        query = "MORPH(VRB imp)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", 		res.at("/query/@type").asText());
        assertEquals("koral:termGroup", 	res.at("/query/wrap/@type").asText());
        assertEquals("relation:and", 		res.at("/query/wrap/relation").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/0/@type").asText());
        assertEquals("match:eq", 			res.at("/query/wrap/operands/0/match").asText());
        assertEquals("p", 					res.at("/query/wrap/operands/0/layer").asText());
        assertEquals("V.IMP", 				res.at("/query/wrap/operands/0/key").asText());

        query = "MORPH(VRB imp a)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", 		res.at("/query/@type").asText());
        assertEquals("koral:termGroup", 	res.at("/query/wrap/@type").asText());
        assertEquals("relation:and", 		res.at("/query/wrap/relation").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/0/@type").asText());
        assertEquals("match:eq", 			res.at("/query/wrap/operands/0/match").asText());
        assertEquals("p", 					res.at("/query/wrap/operands/0/layer").asText());
        assertEquals("VAIMP", 				res.at("/query/wrap/operands/0/key").asText());

        // imperative forms are negated, "a" is skipped.
        
        query = "MORPH(VRB -imp a)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", 		res.at("/query/@type").asText());
        assertEquals("koral:termGroup", 	res.at("/query/wrap/@type").asText());
        assertEquals("relation:and", 		res.at("/query/wrap/relation").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/0/@type").asText());
        assertEquals("type:regex", 			res.at("/query/wrap/operands/0/type").asText());
        assertEquals("match:eq", 			res.at("/query/wrap/operands/0/match").asText());
        assertEquals("p", 					res.at("/query/wrap/operands/0/layer").asText());
        assertEquals("V.*", 				res.at("/query/wrap/operands/0/key").asText());

        assertEquals("koral:term", 			res.at("/query/wrap/operands/1/@type").asText());
        assertEquals("type:regex", 			res.at("/query/wrap/operands/1/type").asText());
        assertEquals("match:ne", 			res.at("/query/wrap/operands/1/match").asText());
        assertEquals("p", 					res.at("/query/wrap/operands/1/layer").asText());
        assertEquals("V.IMP", 				res.at("/query/wrap/operands/1/key").asText());

        // STTS: non finit verb, "-a" is skipped:
        
        query = "MORPH(VRB -fin -a)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", 		res.at("/query/@type").asText());
        assertEquals("koral:termGroup", 	res.at("/query/wrap/@type").asText());
        assertEquals("relation:and", 		res.at("/query/wrap/relation").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/0/@type").asText());
        assertEquals("type:regex", 			res.at("/query/wrap/operands/0/type").asText());
        assertEquals("match:eq", 			res.at("/query/wrap/operands/0/match").asText());
        assertEquals("p", 					res.at("/query/wrap/operands/0/layer").asText());
        assertEquals("V.*",	 				res.at("/query/wrap/operands/0/key").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/1/@type").asText());
        assertEquals("type:regex", 			res.at("/query/wrap/operands/1/type").asText());
        assertEquals("match:ne", 			res.at("/query/wrap/operands/1/match").asText());
        assertEquals("p",					res.at("/query/wrap/operands/1/layer").asText());
        assertEquals("V.FIN",				res.at("/query/wrap/operands/1/key").asText());

        query = "MORPH(PRON)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", 		res.at("/query/@type").asText());
        assertEquals("type:regex", 			res.at("/query/wrap/type").asText());
        assertEquals("match:eq", 			res.at("/query/wrap/match").asText());
        assertEquals("p", 					res.at("/query/wrap/layer").asText());
        assertEquals("P.*",	 				res.at("/query/wrap/key").asText());

        // some C2-style annotations may translate to a reg. expression formulating alternatives:
        
        query = "MORPH(PRON per)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", 		res.at("/query/@type").asText());
        assertEquals("koral:termGroup", 	res.at("/query/wrap/@type").asText());
        assertEquals("relation:and", 		res.at("/query/wrap/relation").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/0/@type").asText());
        assertEquals("type:regex", 			res.at("/query/wrap/operands/0/type").asText());
        assertEquals("match:eq", 			res.at("/query/wrap/operands/0/match").asText());
        assertEquals("p", 					res.at("/query/wrap/operands/0/layer").asText());
        assertEquals("(PRF|PPER)",			res.at("/query/wrap/operands/0/key").asText());
        
        query = "MORPH(PRON -per)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", 		res.at("/query/@type").asText());
        assertEquals("koral:termGroup", 	res.at("/query/wrap/@type").asText());
        assertEquals("relation:and", 		res.at("/query/wrap/relation").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/0/@type").asText());
        assertEquals("type:regex", 			res.at("/query/wrap/operands/0/type").asText());
        assertEquals("match:eq", 			res.at("/query/wrap/operands/0/match").asText());
        assertEquals("p", 					res.at("/query/wrap/operands/0/layer").asText());
        assertEquals("P.*",					res.at("/query/wrap/operands/0/key").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/1/@type").asText());
        assertEquals("type:regex", 			res.at("/query/wrap/operands/1/type").asText());
        assertEquals("match:ne", 			res.at("/query/wrap/operands/1/match").asText());
        assertEquals("p", 					res.at("/query/wrap/operands/1/layer").asText());
        assertEquals("(PRF|PPER)",			res.at("/query/wrap/operands/1/key").asText());

        query = "MORPH(PRON w -at)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", 		res.at("/query/@type").asText());
        assertEquals("koral:termGroup", 	res.at("/query/wrap/@type").asText());
        assertEquals("relation:and", 		res.at("/query/wrap/relation").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/0/@type").asText());
        assertEquals("type:regex", 			res.at("/query/wrap/operands/0/type").asText());
        assertEquals("match:eq", 			res.at("/query/wrap/operands/0/match").asText());
        assertEquals("p", 					res.at("/query/wrap/operands/0/layer").asText());
        assertEquals("PW.*",				res.at("/query/wrap/operands/0/key").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/1/@type").asText());
        assertEquals("match:ne", 			res.at("/query/wrap/operands/1/match").asText());
        assertEquals("p", 					res.at("/query/wrap/operands/1/layer").asText());
        assertEquals("PWAT",				res.at("/query/wrap/operands/1/key").asText());

        /*
         * CONNEXOR: translating from C2-style annotations to marmot POS + morph:
         */

        // CONNEXOR: special encoding of C2/CONNEXOR for finite verbs:

        query = "MORPH(V -inf -pcp)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", 		res.at("/query/@type").asText());
        assertEquals("koral:termGroup", 	res.at("/query/wrap/@type").asText());
        assertEquals("relation:and", 		res.at("/query/wrap/relation").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/0/@type").asText());
        assertEquals("type:regex", 			res.at("/query/wrap/operands/0/type").asText());
        assertEquals("match:eq", 			res.at("/query/wrap/operands/0/match").asText());
        assertEquals("p", 					res.at("/query/wrap/operands/0/layer").asText());
        assertEquals("V.FIN", 				res.at("/query/wrap/operands/0/key").asText());

        // C2-MORPH-Assistant generates tags like 'imp' in between sequences like "V -inf -pcp",
        // Serialization must detect this to ensure "V -inf -pcp" is correctly translated.
        // Also see translation onto 2 different layers:
        
        query = "MORPH(V imp -inf -pcp)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", 		res.at("/query/@type").asText());
        assertEquals("koral:termGroup", 	res.at("/query/wrap/@type").asText());
        assertEquals("relation:and", 		res.at("/query/wrap/relation").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/0/@type").asText());
        assertEquals("type:regex", 			res.at("/query/wrap/operands/0/type").asText());
        assertEquals("match:eq", 			res.at("/query/wrap/operands/0/match").asText());
        assertEquals("p", 					res.at("/query/wrap/operands/0/layer").asText());
        assertEquals("V.FIN", 				res.at("/query/wrap/operands/0/key").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/1/@type").asText());
        assertEquals("match:eq", 			res.at("/query/wrap/operands/1/match").asText());
        assertEquals("m", 					res.at("/query/wrap/operands/1/layer").asText());
        assertEquals("mood", 				res.at("/query/wrap/operands/1/key").asText());
        assertEquals("imper", 				res.at("/query/wrap/operands/1/value").asText());

        query = "MORPH(V -imp -inf -past -pcp)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", 		res.at("/query/@type").asText());
        assertEquals("koral:termGroup", 	res.at("/query/wrap/@type").asText());
        assertEquals("relation:and", 		res.at("/query/wrap/relation").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/0/@type").asText());
        assertEquals("type:regex", 			res.at("/query/wrap/operands/0/type").asText());
        assertEquals("match:eq", 			res.at("/query/wrap/operands/0/match").asText());
        assertEquals("p", 					res.at("/query/wrap/operands/0/layer").asText());
        assertEquals("V.FIN", 				res.at("/query/wrap/operands/0/key").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/1/@type").asText());
        assertEquals("match:ne", 			res.at("/query/wrap/operands/1/match").asText());
        assertEquals("m", 					res.at("/query/wrap/operands/1/layer").asText());
        assertEquals("mood", 				res.at("/query/wrap/operands/1/key").asText());
        assertEquals("imper", 				res.at("/query/wrap/operands/1/value").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/2/@type").asText());
        assertEquals("match:ne", 			res.at("/query/wrap/operands/2/match").asText());
        assertEquals("m", 					res.at("/query/wrap/operands/2/layer").asText());
        assertEquals("tense", 				res.at("/query/wrap/operands/2/key").asText());
        assertEquals("past", 				res.at("/query/wrap/operands/2/value").asText());

        // special case for cardinals:
        
        query = "MORPH(NUM -ord)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", 		res.at("/query/@type").asText());
        assertEquals("koral:termGroup", 	res.at("/query/wrap/@type").asText());
        assertEquals("relation:and", 		res.at("/query/wrap/relation").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/0/@type").asText());
        assertEquals("match:eq", 			res.at("/query/wrap/operands/0/match").asText());
        assertEquals("p", 					res.at("/query/wrap/operands/0/layer").asText());
        assertEquals("CARD", 				res.at("/query/wrap/operands/0/key").asText());
        
        // MORPH(N -prop) -> p=NE (= normal entity):
        // order of values must be rearranged:
        
        query = "MORPH(N -pl -prop)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", 		res.at("/query/@type").asText());
        assertEquals("koral:termGroup", 	res.at("/query/wrap/@type").asText());
        assertEquals("relation:and", 		res.at("/query/wrap/relation").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/0/@type").asText());
        assertEquals("match:eq", 			res.at("/query/wrap/operands/0/match").asText());
        assertEquals("p", 					res.at("/query/wrap/operands/0/layer").asText());
        assertEquals("NN",	 				res.at("/query/wrap/operands/0/key").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/1/@type").asText());
        assertEquals("match:ne", 			res.at("/query/wrap/operands/1/match").asText());
        assertEquals("m", 					res.at("/query/wrap/operands/1/layer").asText());
        assertEquals("number", 				res.at("/query/wrap/operands/1/key").asText());
        assertEquals("pl",	 				res.at("/query/wrap/operands/1/value").asText());
        
        // special tagset case: translate from V to ADJ.
        
        query = "MORPH(V pcp prog)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", 		res.at("/query/@type").asText());
        assertEquals("koral:termGroup", 	res.at("/query/wrap/@type").asText());
        assertEquals("relation:and", 		res.at("/query/wrap/relation").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/0/@type").asText());
        assertEquals("type:regex", 			res.at("/query/wrap/operands/0/type").asText());
        assertEquals("match:eq", 			res.at("/query/wrap/operands/0/match").asText());
        assertEquals("p", 					res.at("/query/wrap/operands/0/layer").asText());
        assertEquals("ADJ.", 				res.at("/query/wrap/operands/0/key").asText());
      
        query = "MORPH(V pcp prog)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", 		res.at("/query/@type").asText());
        assertEquals("koral:termGroup", 	res.at("/query/wrap/@type").asText());
        assertEquals("relation:and", 		res.at("/query/wrap/relation").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/0/@type").asText());
        assertEquals("type:regex", 			res.at("/query/wrap/operands/0/type").asText());
        assertEquals("match:eq", 			res.at("/query/wrap/operands/0/match").asText());
        assertEquals("p", 					res.at("/query/wrap/operands/0/layer").asText());
        assertEquals("ADJ.", 				res.at("/query/wrap/operands/0/key").asText());
      
        // Extension of C2-Connexor tagset for morph. annotations available in marmot/m:
        
        query = "MORPH(N dat sg 1 -masc)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", 		res.at("/query/@type").asText());
        assertEquals("koral:termGroup", 	res.at("/query/wrap/@type").asText());
        assertEquals("relation:and", 		res.at("/query/wrap/relation").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/0/@type").asText());
        assertEquals("type:regex", 			res.at("/query/wrap/operands/0/type").asText());
        assertEquals("match:eq", 			res.at("/query/wrap/operands/0/match").asText());
        assertEquals("p", 					res.at("/query/wrap/operands/0/layer").asText());
        assertEquals("N.", 					res.at("/query/wrap/operands/0/key").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/1/@type").asText());
        assertEquals("match:eq", 			res.at("/query/wrap/operands/1/match").asText());
        assertEquals("m", 					res.at("/query/wrap/operands/1/layer").asText());
        assertEquals("case", 				res.at("/query/wrap/operands/1/key").asText());
        assertEquals("dat", 				res.at("/query/wrap/operands/1/value").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/2/@type").asText());
        assertEquals("match:eq", 			res.at("/query/wrap/operands/2/match").asText());
        assertEquals("m", 					res.at("/query/wrap/operands/2/layer").asText());
        assertEquals("number", 				res.at("/query/wrap/operands/2/key").asText());
        assertEquals("sg", 					res.at("/query/wrap/operands/2/value").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/3/@type").asText());
        assertEquals("match:eq", 			res.at("/query/wrap/operands/3/match").asText());
        assertEquals("m", 					res.at("/query/wrap/operands/3/layer").asText());
        assertEquals("person", 				res.at("/query/wrap/operands/3/key").asText());
        assertEquals("1", 					res.at("/query/wrap/operands/3/value").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/4/@type").asText());
        assertEquals("match:ne", 			res.at("/query/wrap/operands/4/match").asText());
        assertEquals("m", 					res.at("/query/wrap/operands/4/layer").asText());
        assertEquals("gender",				res.at("/query/wrap/operands/4/key").asText());
        assertEquals("masc", 				res.at("/query/wrap/operands/4/value").asText());
        
        /*
         *  C2 Extensions (foundry and layer may be used in MORPH for KorAP):
         */
        
        query = "MORPH(p=VRB)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", 	res.at("/query/wrap/@type").asText());
        assertEquals("match:eq", 	res.at("/query/wrap/match").asText());
        assertEquals("p", 			res.at("/query/wrap/layer").asText());
        assertEquals("VRB", 		res.at("/query/wrap/key").asText());
        
        query = "MORPH(p=-VRB)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", 		res.at("/query/@type").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/@type").asText());
        assertEquals("match:ne", 			res.at("/query/wrap/match").asText());
        assertEquals("p", 					res.at("/query/wrap/layer").asText());
        assertEquals("VRB", 				res.at("/query/wrap/key").asText());
        
        query = "MORPH(p!=VRB)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", 		res.at("/query/@type").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/@type").asText());
        assertEquals("match:ne", 			res.at("/query/wrap/match").asText());
        assertEquals("p", 					res.at("/query/wrap/layer").asText());
        assertEquals("VRB", 				res.at("/query/wrap/key").asText());

        query = "MORPH(p<>VRB)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", 		res.at("/query/@type").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/@type").asText());
        assertEquals("match:ne", 			res.at("/query/wrap/match").asText());
        assertEquals("p", 					res.at("/query/wrap/layer").asText());
        assertEquals("VRB", 				res.at("/query/wrap/key").asText());

        query = "MORPH(tt/p=VRB)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", 	res.at("/query/wrap/@type").asText());
        assertEquals("match:eq", 	res.at("/query/wrap/match").asText());
        assertEquals("p", 			res.at("/query/wrap/layer").asText());
        assertEquals("VRB", 		res.at("/query/wrap/key").asText());

        query = "MORPH(tt/p=-VRB)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", 	res.at("/query/wrap/@type").asText());
        assertEquals("match:ne", 	res.at("/query/wrap/match").asText());
        assertEquals("p", 			res.at("/query/wrap/layer").asText());
        assertEquals("VRB", 		res.at("/query/wrap/key").asText());

        query = "MORPH(tt/p=\"V.*\")";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", 	res.at("/query/wrap/@type").asText());
        assertEquals("type:regex", 	res.at("/query/wrap/type").asText());
        assertEquals("V.*", 		res.at("/query/wrap/key").asText());
        assertEquals("p", 			res.at("/query/wrap/layer").asText());
        assertEquals("tt", 			res.at("/query/wrap/foundry").asText());
        assertEquals("match:eq", 	res.at("/query/wrap/match").asText());

        // regex also allowed between '...':
        query = "MORPH(tt/p='V.*')";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", 	res.at("/query/wrap/@type").asText());
        assertEquals("type:regex", 	res.at("/query/wrap/type").asText());
        assertEquals("V.*", 		res.at("/query/wrap/key").asText());
        assertEquals("p", 			res.at("/query/wrap/layer").asText());
        assertEquals("tt", 			res.at("/query/wrap/foundry").asText());
        assertEquals("match:eq", 	res.at("/query/wrap/match").asText());

        // regex also without any quotes:
        query = "MORPH(tt/p=V.*)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", 	res.at("/query/wrap/@type").asText());
        assertEquals("type:regex", 	res.at("/query/wrap/type").asText());
        assertEquals("V.*", 		res.at("/query/wrap/key").asText());
        assertEquals("p", 			res.at("/query/wrap/layer").asText());
        assertEquals("tt", 			res.at("/query/wrap/foundry").asText());
        assertEquals("match:eq", 	res.at("/query/wrap/match").asText());

        // escaping regex :
        query = "MORPH(tt/p='V\\.\\*')";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", 	res.at("/query/wrap/@type").asText());
        assertTrue  (			 	res.at("/query/wrap/type").isMissingNode()); // no type:regex
        assertEquals("V\\.\\*",		res.at("/query/wrap/key").asText());
        assertEquals("p", 			res.at("/query/wrap/layer").asText());
        assertEquals("tt", 			res.at("/query/wrap/foundry").asText());
        assertEquals("match:eq", 	res.at("/query/wrap/match").asText());

        // in this case, the annotation values are translate as is, not via CONNEXOR nor STTS.
        query = "MORPH(tt/p='V.* -fin -a')";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", 		res.at("/query/@type").asText());
        assertEquals("koral:termGroup", 	res.at("/query/wrap/@type").asText());
        assertEquals("relation:and", 		res.at("/query/wrap/relation").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/0/@type").asText());
        assertEquals("tt",	 				res.at("/query/wrap/operands/0/foundry").asText());
        assertEquals("type:regex", 			res.at("/query/wrap/operands/0/type").asText());
        assertEquals("match:eq", 			res.at("/query/wrap/operands/0/match").asText());
        assertEquals("p", 					res.at("/query/wrap/operands/0/layer").asText());
        assertEquals("V.*",	 				res.at("/query/wrap/operands/0/key").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/1/@type").asText());
        assertEquals("tt",	 				res.at("/query/wrap/operands/1/foundry").asText());
        assertEquals("match:ne", 			res.at("/query/wrap/operands/1/match").asText());
        assertEquals("p", 					res.at("/query/wrap/operands/1/layer").asText());
        assertEquals("fin", 				res.at("/query/wrap/operands/1/key").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/2/@type").asText());
        assertEquals("tt",	 				res.at("/query/wrap/operands/2/foundry").asText());
        assertEquals("match:ne", 			res.at("/query/wrap/operands/2/match").asText());
        assertEquals("p", 					res.at("/query/wrap/operands/2/layer").asText());
        assertEquals("a",	 				res.at("/query/wrap/operands/2/key").asText());

        // works also with double quotes, double quotes do not enforce reg. expressions:
        query = "MORPH(tt/p=\"V.* -fin -a\")";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", 		res.at("/query/@type").asText());
        assertEquals("koral:termGroup", 	res.at("/query/wrap/@type").asText());
        assertEquals("relation:and", 		res.at("/query/wrap/relation").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/0/@type").asText());
        assertEquals("tt",	 				res.at("/query/wrap/operands/0/foundry").asText());
        assertEquals("type:regex", 			res.at("/query/wrap/operands/0/type").asText());
        assertEquals("match:eq", 			res.at("/query/wrap/operands/0/match").asText());
        assertEquals("p", 					res.at("/query/wrap/operands/0/layer").asText());
        assertEquals("V.*",	 				res.at("/query/wrap/operands/0/key").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/1/@type").asText());
        assertEquals("tt",	 				res.at("/query/wrap/operands/1/foundry").asText());
        assertEquals("match:ne", 			res.at("/query/wrap/operands/1/match").asText());
        assertEquals("p", 					res.at("/query/wrap/operands/1/layer").asText());
        assertEquals("fin", 				res.at("/query/wrap/operands/1/key").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/operands/2/@type").asText());
        assertEquals("tt",	 				res.at("/query/wrap/operands/2/foundry").asText());
        assertEquals("match:ne", 			res.at("/query/wrap/operands/2/match").asText());
        assertEquals("p", 					res.at("/query/wrap/operands/2/layer").asText());
        assertEquals("a",	 				res.at("/query/wrap/operands/2/key").asText());

        query = "MORPH(mate/m=temp:pres)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term", 	res.at("/query/wrap/@type").asText());
        assertEquals("temp", 		res.at("/query/wrap/key").asText());
        assertEquals("pres", 		res.at("/query/wrap/value").asText());
        assertEquals("m", 			res.at("/query/wrap/layer").asText());
        assertEquals("mate", 		res.at("/query/wrap/foundry").asText());
        assertEquals("match:eq", 	res.at("/query/wrap/match").asText());

        query = "MORPH(tt/p<>V & mate/m!=temp:pres)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:termGroup", res.at("/query/wrap/@type").asText());       
        assertEquals("relation:and", res.at("/query/wrap/relation").asText());
        assertEquals("koral:termGroup", res.at("/query/wrap/@type").asText());
        assertEquals("V", res.at("/query/wrap/operands/0/key").asText());
        assertEquals("p", res.at("/query/wrap/operands/0/layer").asText());
        assertEquals("tt", res.at("/query/wrap/operands/0/foundry").asText());
        assertEquals("match:ne", res.at("/query/wrap/operands/0/match").asText());
        assertEquals("temp", res.at("/query/wrap/operands/1/key").asText());
        assertEquals("pres", res.at("/query/wrap/operands/1/value").asText());
        assertEquals("m", res.at("/query/wrap/operands/1/layer").asText());
        assertEquals("mate", res.at("/query/wrap/operands/1/foundry").asText());
        assertEquals("match:ne", res.at("/query/wrap/operands/1/match").asText());
        
        // '&' in MORPH is optional: same query without '&' should return same serialization.
        query = "MORPH(tt/p<>V mate/m!=temp:pres)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:termGroup", res.at("/query/wrap/@type").asText());       
        assertEquals("relation:and", res.at("/query/wrap/relation").asText());
        assertEquals("koral:termGroup", res.at("/query/wrap/@type").asText());
        assertEquals("V", res.at("/query/wrap/operands/0/key").asText());
        assertEquals("p", res.at("/query/wrap/operands/0/layer").asText());
        assertEquals("tt", res.at("/query/wrap/operands/0/foundry").asText());
        assertEquals("match:ne", res.at("/query/wrap/operands/0/match").asText());
        assertEquals("temp", res.at("/query/wrap/operands/1/key").asText());
        assertEquals("pres", res.at("/query/wrap/operands/1/value").asText());
        assertEquals("m", res.at("/query/wrap/operands/1/layer").asText());
        assertEquals("mate", res.at("/query/wrap/operands/1/foundry").asText());
        assertEquals("match:ne", res.at("/query/wrap/operands/1/match").asText());
        
    }


    @Test
    public void testSequence () throws JsonProcessingException, IOException {
        query = "der Mann";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("der", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("Mann", res.at("/query/operands/1/wrap/key").asText());
        assertTrue(res.at("/query/operands/2").isMissingNode());

        query = "der Mann schläft";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("der", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("Mann", res.at("/query/operands/1/wrap/key").asText());
        assertEquals("schläft", res.at("/query/operands/2/wrap/key").asText());
        assertTrue(res.at("/query/operands/3").isMissingNode());

        query = "der Mann schläft lang";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("der", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("Mann", res.at("/query/operands/1/wrap/key").asText());
        assertEquals("schläft", res.at("/query/operands/2/wrap/key").asText());
        assertEquals("lang", res.at("/query/operands/3/wrap/key").asText());
        assertTrue(res.at("/query/operands/4").isMissingNode());

        query = "#ELEM(s)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:span", res.at("/query/@type").asText());
        assertEquals("s", res.at("/query/wrap/key").asText());
        assertTrue(res.at("/query/key").isMissingNode());
		
        // #ELEM(W) should not generate key=W  - 16.04.26/FB
        query = "der #ELEM(W)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", 		res.at("/query/@type").asText());
        assertEquals("operation:sequence", 	res.at("/query/operation").asText());
        assertEquals("koral:token",			res.at("/query/operands/0/@type").asText());
        assertEquals("koral:term",			res.at("/query/operands/0/wrap/@type").asText());
        assertEquals("der", 				res.at("/query/operands/0/wrap/key").asText());
        assertEquals("orth", 				res.at("/query/operands/0/wrap/layer").asText());

        assertEquals("koral:token", 		res.at("/query/operands/1/@type").asText());
        assertTrue(							res.at("/query/operands/1/layer").isMissingNode());

        // #ELEM(W) should not generate key=W  - 16.04.26/FB
        query = "der #ELEM(W) Mann";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", 		res.at("/query/@type").asText());
        assertEquals("operation:sequence", 	res.at("/query/operation").asText());
        assertEquals("koral:token",			res.at("/query/operands/0/@type").asText());
        assertEquals("der", 				res.at("/query/operands/0/wrap/key").asText());
        assertEquals("orth", 				res.at("/query/operands/0/wrap/layer").asText());
        assertEquals("koral:token", 		res.at("/query/operands/1/@type").asText());
        assertEquals("koral:token",			res.at("/query/operands/2/@type").asText());
        assertEquals("Mann", 				res.at("/query/operands/2/wrap/key").asText());
        assertEquals("orth",	 			res.at("/query/operands/2/wrap/layer").asText());

        query = "der MORPH(p=ADJA) Mann";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:group", 		res.at("/query/@type").asText());
        assertEquals("operation:sequence", 	res.at("/query/operation").asText());
        assertEquals("der", 				res.at("/query/operands/0/wrap/key").asText());
        assertEquals("orth", 				res.at("/query/operands/0/wrap/layer").asText());
        assertEquals("ADJA", 				res.at("/query/operands/1/wrap/key").asText());
        assertEquals("p", 					res.at("/query/operands/1/wrap/layer").asText());
        assertEquals("Mann", 				res.at("/query/operands/2/wrap/key").asText());
        assertEquals("orth", 				res.at("/query/operands/2/wrap/layer").asText());
        assertTrue(							res.at("/query/operands/3").isMissingNode());
    }


    @Test
    public void testOPOR () throws JsonProcessingException, IOException {
        query = "Sonne oder Mond";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:disjunction", res.at("/query/operation")
                .asText());
        assertEquals("Sonne", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("Mond", res.at("/query/operands/1/wrap/key").asText());
        assertTrue(res.at("/query/operands/2").isMissingNode());

        query = "(Sonne scheint) oder Mond";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:disjunction", res.at("/query/operation")
                .asText());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/0/operation").asText());
        assertEquals("Sonne", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("scheint", res.at("/query/operands/0/operands/1/wrap/key")
                .asText());
        assertEquals("Mond", res.at("/query/operands/1/wrap/key").asText());
        assertTrue(res.at("/query/operands/2").isMissingNode());

        query = "(Sonne scheint) oder (Mond scheint)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:disjunction", res.at("/query/operation")
                .asText());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/0/operation").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/1/operation").asText());
        assertEquals("Sonne", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("scheint", res.at("/query/operands/0/operands/1/wrap/key")
                .asText());
        assertEquals("Mond", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("scheint", res.at("/query/operands/1/operands/1/wrap/key")
                .asText());
        assertTrue(res.at("/query/operands/2").isMissingNode());
    }


    @Test
    public void testOPORAND () throws JsonProcessingException, IOException {

        // Query
        query = "(Sonne oder Mond) und scheint";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());

        assertFalse(res.at("/query/inOrder").isMissingNode());
        assertFalse(res.at("/query/inOrder").asBoolean());

        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("t", res.at("/query/distances/0/key").asText());
        assertEquals(0, res.at("/query/distances/0/min").asInt());
        assertEquals(0, res.at("/query/distances/0/max").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:disjunction",
                res.at("/query/operands/0/operation").asText());

        assertFalse(res.at("/query/operands/0/inOrder").isMissingNode());
        assertFalse(res.at("/query/operands/0/inOrder").asBoolean());

        assertEquals("Sonne", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("Mond", res.at("/query/operands/0/operands/1/wrap/key")
                .asText());
        assertEquals("koral:token", res.at("/query/operands/1/@type").asText());
        assertEquals("scheint", res.at("/query/operands/1/wrap/key").asText());

        // Query
        query = "scheint und (Sonne oder Mond)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());

        assertFalse(res.at("/query/inOrder").isMissingNode());
        assertFalse(res.at("/query/inOrder").asBoolean());

        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("t", res.at("/query/distances/0/key").asText());
        assertEquals(0, res.at("/query/distances/0/min").asInt());
        assertEquals(0, res.at("/query/distances/0/max").asInt());
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("scheint", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:disjunction",
                res.at("/query/operands/1/operation").asText());

        assertFalse(res.at("/query/operands/1/inOrder").isMissingNode());
        assertFalse(res.at("/query/operands/1/inOrder").asBoolean());

        assertEquals("Sonne", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("Mond", res.at("/query/operands/1/operands/1/wrap/key")
                .asText());

        // Query
        query = "Regen und scheint und (Sonne oder Mond)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());

        assertFalse(res.at("/query/inOrder").isMissingNode());
        assertFalse(res.at("/query/inOrder").asBoolean());

        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("t", res.at("/query/distances/0/key").asText());
        assertEquals(0, res.at("/query/distances/0/min").asInt());
        assertEquals(0, res.at("/query/distances/0/max").asInt());
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("Regen", res.at("/query/operands/0/wrap/key").asText());

        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/1/operation").asText());

        assertFalse(res.at("/query/operands/1/inOrder").isMissingNode());
        assertFalse(res.at("/query/operands/1/inOrder").asBoolean());

        assertEquals("cosmas:distance",
                res.at("/query/operands/1/distances/0/@type").asText());
        assertEquals("t", res.at("/query/operands/1/distances/0/key").asText());
        assertEquals(0, res.at("/query/operands/1/distances/0/min").asInt());
        assertEquals(0, res.at("/query/operands/1/distances/0/max").asInt());
        assertEquals("scheint", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("koral:group", res
                .at("/query/operands/1/operands/1/@type").asText());

        assertFalse(res.at("/query/operands/1/operands/1/inOrder")
                .isMissingNode());
        assertFalse(res.at("/query/operands/1/operands/1/inOrder")
                .asBoolean());

        assertEquals("operation:disjunction",
                res.at("/query/operands/1/operands/1/operation").asText());
        assertEquals("Sonne",
                res.at("/query/operands/1/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals("Mond",
                res.at("/query/operands/1/operands/1/operands/1/wrap/key")
                        .asText());
    }


    @Test
    public void testOPNOT () throws JsonProcessingException, IOException {
        query = "Sonne nicht Mond";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("t", res.at("/query/distances/0/key").asText());
        assertEquals(0, res.at("/query/distances/0/min").asInt());
        assertEquals(0, res.at("/query/distances/0/max").asInt());
        assertTrue(res.at("/query/distances/0/exclude").asBoolean());
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("Sonne", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("Mond", res.at("/query/operands/1/wrap/key").asText());

        query = "Sonne nicht Mond nicht Sterne";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("t", res.at("/query/distances/0/key").asText());
        assertEquals(0, res.at("/query/distances/0/min").asInt());
        assertEquals(0, res.at("/query/distances/0/max").asInt());
        assertTrue(res.at("/query/distances/0/exclude").asBoolean());
        assertEquals("koral:token", res.at("/query/operands/0/@type").asText());
        assertEquals("Sonne", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/1/operation").asText());
        assertEquals("cosmas:distance",
                res.at("/query/operands/1/distances/0/@type").asText());
        assertEquals("t", res.at("/query/operands/1/distances/0/key").asText());
        assertEquals(0, res.at("/query/operands/1/distances/0/min").asInt());
        assertEquals(0, res.at("/query/operands/1/distances/0/max").asInt());
        assertTrue(res.at("/query/operands/1/distances/0/exclude")
                .asBoolean());
        assertEquals("Mond", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("Sterne", res.at("/query/operands/1/operands/1/wrap/key")
                .asText());

        query = "(Sonne nicht Mond) nicht Sterne";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("t", res.at("/query/distances/0/key").asText());
        assertEquals(0, res.at("/query/distances/0/min").asInt());
        assertEquals(0, res.at("/query/distances/0/max").asInt());
        assertTrue(res.at("/query/distances/0/exclude").asBoolean());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/0/operation").asText());
        assertEquals("cosmas:distance",
                res.at("/query/operands/0/distances/0/@type").asText());
        assertEquals("t", res.at("/query/operands/0/distances/0/key").asText());
        assertEquals(0, res.at("/query/operands/0/distances/0/min").asInt());
        assertEquals(0, res.at("/query/operands/0/distances/0/max").asInt());
        assertTrue(res.at("/query/operands/0/distances/0/exclude")
                .asBoolean());
        assertEquals("Sonne", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("Mond", res.at("/query/operands/0/operands/1/wrap/key")
                .asText());
        assertEquals("koral:token", res.at("/query/operands/1/@type").asText());
        assertEquals("Sterne", res.at("/query/operands/1/wrap/key").asText());
    }


    @Test
    public void testOPPROX () throws JsonProcessingException, IOException {
    	
        query = "Sonne /+w1:4 Mond";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("w", res.at("/query/distances/0/key").asText());
        assertEquals(1, res.at("/query/distances/0/boundary/min").asInt());
        assertEquals(4, res.at("/query/distances/0/boundary/max").asInt());
        assertTrue(res.at("/query/inOrder").asBoolean());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/0/operation")
                .asText());
        assertEquals(129, res.at("/query/operands/0/classOut").asInt());
        assertEquals(129, res.at("/query/operands/1/classOut").asInt());
        assertEquals("koral:token", res
                .at("/query/operands/0/operands/0/@type").asText());
        assertEquals("Sonne", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("Mond", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());

        query = "Sonne /+w1:4,s0,p1:3 Mond";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("w", res.at("/query/distances/0/key").asText());
        assertEquals(1, res.at("/query/distances/0/boundary/min").asInt());
        assertEquals(4, res.at("/query/distances/0/boundary/max").asInt());
        assertEquals("s", res.at("/query/distances/1/key").asText());
        assertEquals(0, res.at("/query/distances/1/boundary/min").asInt());
        assertEquals("p", res.at("/query/distances/2/key").asText());
        assertEquals(1, res.at("/query/distances/2/boundary/min").asInt());
        assertEquals(3, res.at("/query/distances/2/boundary/max").asInt());
        assertTrue(res.at("/query/inOrder").asBoolean());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/0/operation")
                .asText());
        assertEquals(129, res.at("/query/operands/0/classOut").asInt());
        assertEquals(129, res.at("/query/operands/1/classOut").asInt());
        assertEquals("koral:token", res
                .at("/query/operands/0/operands/0/@type").asText());
        assertEquals("Sonne", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("Mond", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());

        // Order of min and max is irrelevant in C2
        query = "Sonne /+w4:1,s0,p3:1 Mond";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("w", res.at("/query/distances/0/key").asText());
        assertEquals(1, res.at("/query/distances/0/boundary/min").asInt());
        assertEquals(4, res.at("/query/distances/0/boundary/max").asInt());
        assertEquals("s", res.at("/query/distances/1/key").asText());
        assertEquals(0, res.at("/query/distances/1/boundary/min").asInt());
        assertEquals("p", res.at("/query/distances/2/key").asText());
        assertEquals(1, res.at("/query/distances/2/boundary/min").asInt());
        assertEquals(3, res.at("/query/distances/2/boundary/max").asInt());
        assertTrue(res.at("/query/inOrder").asBoolean());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/0/operation")
                .asText());
        assertEquals(129, res.at("/query/operands/0/classOut").asInt());
        assertEquals(129, res.at("/query/operands/1/classOut").asInt());
        assertEquals("koral:token", res
                .at("/query/operands/0/operands/0/@type").asText());
        assertEquals("Sonne", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("Mond", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());

        
        query = "Sonne /+w4 Mond";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("w", res.at("/query/distances/0/key").asText());
        assertEquals(0, res.at("/query/distances/0/boundary/min").asInt());
        assertEquals(4, res.at("/query/distances/0/boundary/max").asInt());

        query = "Sonne /-w4 Mond";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("w", res.at("/query/distances/0/key").asText());
        assertEquals(0, res.at("/query/distances/0/boundary/min").asInt());
        assertEquals(4, res.at("/query/distances/0/boundary/max").asInt());
        assertEquals("Mond", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("Sonne", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());

        query = "Sonne /w4 Mond";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("w", res.at("/query/distances/0/key").asText());
        assertEquals(0, res.at("/query/distances/0/boundary/min").asInt());
        assertEquals(4, res.at("/query/distances/0/boundary/max").asInt());
        assertEquals("Sonne", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("Mond", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertFalse(res.at("/query/inOrder").asBoolean());
        
        // -- check exclude operator -- // 
        
        query = "Sonne %-w1:2 Sterne";
        qs.setQuery(query,  "cosmas2");
    	res = mapper.readTree(qs.toJSON());
      	
    	assertEquals("cosmas:distance",  res.at("/query/distances").get(0).get("@type").asText());
    	assertTrue( res.at("/query/distances").get(0).get("exclude").asBoolean());
    	assertEquals("w", res.at("/query/distances").get(0).get("key").asText());
    	
        // 15.01.24/FB: checking syntax error:
        
        query = "Sonne /+w Mond"; // distance value missing.
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertTrue(res.get("errors") != null);
        //System.out.printf("Query '%s': errors : '%s'.\n", query, res.get("errors").toPrettyString()) ;
        assertEquals(StatusCodes.ERR_PROX_VAL_NULL, res.get("errors").get(0).get(0).asInt());
        
        query = "Sonne /+2sw Mond"; // 2 distance types instead of 1.
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
          
        assertTrue(res.get("errors") != null);
        //System.out.printf("Query '%s': errors : '%s'.\n", query, res.get("errors").toPrettyString()) ;
    	assertEquals(StatusCodes.ERR_PROX_MEAS_TOOGREAT, res.get("errors").get(0).get(0).asInt());
        
        query = "Sonne /+2s- Mond"; // 2 distance directions instead of 1.
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertTrue(res.get("errors") != null);
        //System.out.printf("Query '%s': errors : '%s'.\n", query, res.get("errors").toPrettyString()) ;
    	assertEquals(StatusCodes.ERR_PROX_DIR_TOOGREAT, res.get("errors").get(0).get(0).asInt());
        
        query = "Sonne /+2s7 Mond"; // 2 distance values instead of 1.
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertTrue(res.get("errors") != null);
        //System.out.printf("Query '%s': errors : '%s'.\n", query, res.get("errors").toPrettyString()) ;
    	assertEquals(StatusCodes.ERR_PROX_VAL_TOOGREAT, res.get("errors").get(0).get(0).asInt());
        
    	// tests for error messages for unknown proximity options:
    	// 29.05.24/FB
    	
    	query = "ab /+w1:2u,p cd";
    	qs.setQuery(query,  "cosmas2");
    	res = mapper.readTree(qs.toJSON());
    	
    	assertTrue("Error code expected!",!res.get("errors").isNull());
    	assertEquals(StatusCodes.ERR_PROX_WRONG_CHARS, res.get("errors").get(0).get(0).asInt());
    	
    	query = "ab %-w1:2,2su cd";
    	qs.setQuery(query,  "cosmas2");
    	res = mapper.readTree(qs.toJSON());

    	assertTrue("Error code expected!",!res.get("errors").isNull());
    	assertEquals(StatusCodes.ERR_PROX_WRONG_CHARS, res.get("errors").get(0).get(0).asInt());
    	
    	query = "ab /w1:2s cd";
    	qs.setQuery(query,  "cosmas2");
    	res = mapper.readTree(qs.toJSON());

      	//System.out.printf("Query '%s': context: '%s'.\n", query, res.get("@context").toPrettyString()) ;
    	//System.out.printf("Query '%s': errors : '%s'.\n", query, res.get("errors").toPrettyString()) ;
    	//System.out.printf("Query '%s': errorCode: '%s'.\n", query, res.get("errors").get(0).get(0).toPrettyString()) ;
    	//System.out.printf("Query '%s': errorText : '%s'.\n", query, res.get("errors").get(0).get(1).toPrettyString()) ;
    	//System.out.printf("Query '%s': errorPos : '%s'.\n", query, res.get("errors").get(0).get(2).toPrettyString()) ;
    
      	assertTrue("Error code expected!", res.get("errors") != null);
    	assertEquals(StatusCodes.ERR_PROX_MEAS_TOOGREAT, res.get("errors").get(0).get(0).asInt());
    	
    	query = "Sonne %-w1:2,+2su Galaxien";
        qs.setQuery(query,  "cosmas2");
    	res = mapper.readTree(qs.toJSON());

      	assertTrue("Error code expected!", res.get("errors") != null);
    	assertEquals(StatusCodes.ERR_PROX_WRONG_CHARS, res.get("errors").get(0).get(0).asInt());

    }


    @Test
    public void testOPPROXNested () throws JsonProcessingException, IOException {
        query = "Sonne /+w1:4 Mond /+w1:7 Sterne";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("w", res.at("/query/distances/0/key").asText());
        assertEquals(1, res.at("/query/distances/0/boundary/min").asInt());
        assertEquals(4, res.at("/query/distances/0/boundary/max").asInt());
        assertTrue(res.at("/query/inOrder").asBoolean());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/0/operation")
                .asText());
        assertEquals(129, res.at("/query/operands/0/classOut").asInt());
        assertEquals("Sonne", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals(129, res.at("/query/operands/1/classOut").asInt());
        assertEquals("operation:sequence",
                res.at("/query/operands/1/operands/0/operation").asText());
        assertEquals("w", res
                .at("/query/operands/1/operands/0/distances/0/key").asText());
        assertEquals(1,
                res.at("/query/operands/1/operands/0/distances/0/boundary/min")
                        .asInt());
        assertEquals(7,
                res.at("/query/operands/1/operands/0/distances/0/boundary/max")
                        .asInt());
        assertEquals(130,
                res.at("/query/operands/1/operands/0/operands/0/classOut")
                        .asInt());
        assertEquals(
                "Mond",
                res.at("/query/operands/1/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(130,
                res.at("/query/operands/1/operands/0/operands/1/classOut")
                        .asInt());
        assertEquals(
                "Sterne",
                res.at("/query/operands/1/operands/0/operands/1/operands/0/wrap/key")
                        .asText());

        query = "Sonne /+w1:4 Mond /-w1:7 Sterne";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("Sonne", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals(
                "Sterne",
                res.at("/query/operands/1/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(
                "Mond",
                res.at("/query/operands/1/operands/0/operands/1/operands/0/wrap/key")
                        .asText());

        query = "Sonne /-w4 Mond /+w2 Sterne";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("w", res.at("/query/distances/0/key").asText());
        assertEquals(0, res.at("/query/distances/0/boundary/min").asInt());
        assertEquals(4, res.at("/query/distances/0/boundary/max").asInt());
        assertTrue(res.at("/query/inOrder").asBoolean());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/1/operation")
                .asText());
        assertEquals(129, res.at("/query/operands/1/classOut").asInt());
        assertEquals("Sonne", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals(129, res.at("/query/operands/0/classOut").asInt());
        assertEquals("operation:sequence",
                res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals("w", res
                .at("/query/operands/0/operands/0/distances/0/key").asText());
        assertEquals(0,
                res.at("/query/operands/0/operands/0/distances/0/boundary/min")
                        .asInt());
        assertEquals(2,
                res.at("/query/operands/0/operands/0/distances/0/boundary/max")
                        .asInt());
        assertEquals(130,
                res.at("/query/operands/0/operands/0/operands/0/classOut")
                        .asInt());
        assertEquals(
                "Mond",
                res.at("/query/operands/0/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(130,
                res.at("/query/operands/0/operands/0/operands/1/classOut")
                        .asInt());
        assertEquals(
                "Sterne",
                res.at("/query/operands/0/operands/0/operands/1/operands/0/wrap/key")
                        .asText());

    }


    @Test
    public void testBEG_END () throws JsonProcessingException, IOException {
        query = "#BEG(der /w3:5 Mann)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(0, res.at("/query/spanRef/0").asInt());
        assertEquals(1, res.at("/query/spanRef/1").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/0/operation").asText());
        assertEquals("cosmas:distance",
                res.at("/query/operands/0/distances/0/@type").asText());
        assertEquals("w", res.at("/query/operands/0/distances/0/key").asText());
        assertEquals(3, res.at("/query/operands/0/distances/0/boundary/min")
                .asInt());
        assertEquals(5, res.at("/query/operands/0/distances/0/boundary/max")
                .asInt());
        assertFalse(res.at("/query/operands/0/inOrder").asBoolean());
        assertEquals("koral:token", res
                .at("/query/operands/0/operands/0/@type").asText());
        assertEquals("der", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("Mann", res.at("/query/operands/0/operands/1/wrap/key")
                .asText());

        query = "#BEG(der /w3:5 Mann) /+w10 kommt";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("w", res.at("/query/distances/0/key").asText());
        assertEquals(0, res.at("/query/distances/0/boundary/min").asInt());
        assertEquals(10, res.at("/query/distances/0/boundary/max").asInt());
        assertTrue(res.at("/query/inOrder").asBoolean());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/0/operation")
                .asText());
        assertEquals(129, res.at("/query/operands/0/classOut").asInt());
        assertEquals("koral:reference",
                res.at("/query/operands/0/operands/0/@type").asText());
        assertEquals("operation:focus",
                res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals(0, res.at("/query/operands/0/operands/0/spanRef/0")
                .asInt());
        assertEquals(1, res.at("/query/operands/0/operands/0/spanRef/1")
                .asInt());
        assertEquals("koral:group",
                res.at("/query/operands/0/operands/0/operands/0/@type")
                        .asText());
        assertEquals("operation:sequence",
                res.at("/query/operands/0/operands/0/operands/0/operation")
                        .asText());
        assertEquals(
                "cosmas:distance",
                res.at("/query/operands/0/operands/0/operands/0/distances/0/@type")
                        .asText());
        assertEquals(
                "w",
                res.at("/query/operands/0/operands/0/operands/0/distances/0/key")
                        .asText());
        assertEquals(
                3,
                res.at("/query/operands/0/operands/0/operands/0/distances/0/boundary/min")
                        .asInt());
        assertEquals(
                5,
                res.at("/query/operands/0/operands/0/operands/0/distances/0/boundary/max")
                        .asInt());
        assertFalse(res.at("/query/operands/0/operands/0/operands/0/inOrder")
                .asBoolean());
        assertEquals(
                "koral:token",
                res.at("/query/operands/0/operands/0/operands/0/operands/0/@type")
                        .asText());
        assertEquals(
                "der",
                res.at("/query/operands/0/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(
                "Mann",
                res.at("/query/operands/0/operands/0/operands/0/operands/1/wrap/key")
                        .asText());
        assertEquals("operation:class", res.at("/query/operands/1/operation")
                .asText());
        assertEquals(129, res.at("/query/operands/1/classOut").asInt());
        assertEquals("koral:token", res
                .at("/query/operands/1/operands/0/@type").asText());
        assertEquals("kommt", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());

        query = "kommt /+w10 #BEG(der /w3:5 Mann)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("w", res.at("/query/distances/0/key").asText());
        assertEquals(0, res.at("/query/distances/0/boundary/min").asInt());
        assertEquals(10, res.at("/query/distances/0/boundary/max").asInt());
        assertTrue(res.at("/query/inOrder").asBoolean());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/1/operation")
                .asText());
        assertEquals(129, res.at("/query/operands/1/classOut").asInt());
        assertEquals("koral:reference",
                res.at("/query/operands/1/operands/0/@type").asText());
        assertEquals("operation:focus",
                res.at("/query/operands/1/operands/0/operation").asText());
        assertEquals(0, res.at("/query/operands/1/operands/0/spanRef/0")
                .asInt());
        assertEquals(1, res.at("/query/operands/1/operands/0/spanRef/1")
                .asInt());
        assertEquals("koral:group",
                res.at("/query/operands/1/operands/0/operands/0/@type")
                        .asText());
        assertEquals("operation:sequence",
                res.at("/query/operands/1/operands/0/operands/0/operation")
                        .asText());
        assertEquals(
                "cosmas:distance",
                res.at("/query/operands/1/operands/0/operands/0/distances/0/@type")
                        .asText());
        assertEquals(
                "w",
                res.at("/query/operands/1/operands/0/operands/0/distances/0/key")
                        .asText());
        assertEquals(
                3,
                res.at("/query/operands/1/operands/0/operands/0/distances/0/boundary/min")
                        .asInt());
        assertEquals(
                5,
                res.at("/query/operands/1/operands/0/operands/0/distances/0/boundary/max")
                        .asInt());
        assertFalse(res.at("/query/operands/1/operands/0/operands/0/inOrder")
                .asBoolean());
        assertEquals(
                "koral:token",
                res.at("/query/operands/1/operands/0/operands/0/operands/0/@type")
                        .asText());
        assertEquals(
                "der",
                res.at("/query/operands/1/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(
                "Mann",
                res.at("/query/operands/1/operands/0/operands/0/operands/1/wrap/key")
                        .asText());
        assertEquals("operation:class", res.at("/query/operands/0/operation")
                .asText());
        assertEquals(129, res.at("/query/operands/0/classOut").asInt());
        assertEquals("koral:token", res
                .at("/query/operands/0/operands/0/@type").asText());
        assertEquals("kommt", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());

        query = "#END(der /w3:5 Mann)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(-1, res.at("/query/spanRef/0").asInt());
        assertEquals(1, res.at("/query/spanRef/1").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/0/operation").asText());
        assertEquals("cosmas:distance",
                res.at("/query/operands/0/distances/0/@type").asText());
        assertEquals("w", res.at("/query/operands/0/distances/0/key").asText());
        assertEquals(3, res.at("/query/operands/0/distances/0/boundary/min")
                .asInt());
        assertEquals(5, res.at("/query/operands/0/distances/0/boundary/max")
                .asInt());
        assertFalse(res.at("/query/operands/0/inOrder").asBoolean());
        assertEquals("koral:token", res
                .at("/query/operands/0/operands/0/@type").asText());
        assertEquals("der", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        assertEquals("Mann", res.at("/query/operands/0/operands/1/wrap/key")
                .asText());
    }


    @Test
    public void testELEM () throws JsonProcessingException, IOException {
    	/* empty query */
    	query = "#ELEM()";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
       	assertFalse(res.at("/errors").isMissingNode());
       	assertEquals("302", res.at("/errors/0/0").asText()); // error code for empty #ELEM.;
         
    	/** queries which work in C2 **/
        
       	query = "#ELEM(S)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:span", 	res.at("/query/@type").asText());
        assertEquals("koral:term",	res.at("/query/wrap/@type").asText());	
        assertEquals("s",	 		res.at("/query/wrap/key").asText());	// "s" lower cased.
        assertEquals("s", 			res.at("/query/wrap/layer").asText()); 	// "s" = layer, added - 25.03.26/FB 
        
        // corrected: 26.03.26/FB
        
        query = "#ELEM(HEAD type=TOP)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:span", 	res.at("/query/@type").asText());
        assertEquals("head", 		res.at("/query/wrap/key").asText());	// "head" lower cased.
        assertEquals("s",    		res.at("/query/wrap/layer").asText());
		
        assertEquals("koral:term",	res.at("/query/wrap/attr/@type").asText());
        assertEquals("s", 			res.at("/query/wrap/attr/layer").asText()); // default layer for #ELEM(), same as for element name.
        assertEquals("type", 		res.at("/query/wrap/attr/key").asText());
        assertEquals("TOP", 		res.at("/query/wrap/attr/value").asText());
        assertEquals("match:eq", 	res.at("/query/wrap/attr/match").asText());
        
        // check several attribute/value pairs and value in single quotes: 27.03.26/FB
        
        query = "#ELEM(HEAD type=TOP link=url5 style='bold')";
        
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:span", 			res.at("/query/@type").asText());
        assertEquals("koral:term", 			res.at("/query/wrap/@type").asText());
        assertEquals("head", 				res.at("/query/wrap/key").asText());
        assertEquals("s",    				res.at("/query/wrap/layer").asText());
	
        assertEquals("koral:termGroup",		res.at("/query/wrap/attr/@type").asText());
        assertEquals("relation:and",		res.at("/query/wrap/attr/relation").asText());
        
        assertEquals("type", 				res.at("/query/wrap/attr/operands/0/key").asText());
        assertEquals("TOP", 				res.at("/query/wrap/attr/operands/0/value").asText());
        assertEquals("match:eq",			res.at("/query/wrap/attr/operands/0/match").asText());
        assertEquals("koral:term",			res.at("/query/wrap/attr/operands/0/@type").asText());
        assertEquals("s",					res.at("/query/wrap/attr/operands/0/layer").asText());
        
        assertEquals("link", 				res.at("/query/wrap/attr/operands/1/key").asText());
        assertEquals("url5", 				res.at("/query/wrap/attr/operands/1/value").asText());
        assertEquals("match:eq",			res.at("/query/wrap/attr/operands/1/match").asText());
        assertEquals("koral:term",			res.at("/query/wrap/attr/operands/1/@type").asText());
        assertEquals("s",					res.at("/query/wrap/attr/operands/1/layer").asText());

        assertEquals("style", 				res.at("/query/wrap/attr/operands/2/key").asText());
        assertEquals("bold", 				res.at("/query/wrap/attr/operands/2/value").asText());
        assertEquals("match:eq",			res.at("/query/wrap/attr/operands/2/match").asText());
        assertEquals("koral:term",			res.at("/query/wrap/attr/operands/2/@type").asText());
        assertEquals("s",					res.at("/query/wrap/attr/operands/2/layer").asText());

        // check attribute with several values - 27.03.26/FB
        query = "#ELEM(HEAD type!='url5 top bold')";
        
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:span", 			res.at("/query/@type").asText());
        assertEquals("head", 				res.at("/query/wrap/key").asText());
        assertEquals("s",    				res.at("/query/wrap/layer").asText());
		
        assertEquals("koral:termGroup",		res.at("/query/wrap/attr/@type").asText());
        assertEquals("relation:and",		res.at("/query/wrap/attr/relation").asText());
        
        assertEquals("type", 				res.at("/query/wrap/attr/operands/0/key").asText());
        assertEquals("url5", 				res.at("/query/wrap/attr/operands/0/value").asText());
        assertEquals("match:ne",			res.at("/query/wrap/attr/operands/0/match").asText());
        assertEquals("s",	 				res.at("/query/wrap/attr/operands/0/layer").asText());
        assertEquals("koral:term",			res.at("/query/wrap/attr/operands/0/@type").asText());
        
        assertEquals("type", 				res.at("/query/wrap/attr/operands/1/key").asText());
        assertEquals("top", 				res.at("/query/wrap/attr/operands/1/value").asText());
        assertEquals("match:ne",			res.at("/query/wrap/attr/operands/1/match").asText());
        assertEquals("s",	 				res.at("/query/wrap/attr/operands/1/layer").asText());
        assertEquals("koral:term",			res.at("/query/wrap/attr/operands/1/@type").asText());

        assertEquals("type", 				res.at("/query/wrap/attr/operands/2/key").asText());
        assertEquals("bold", 				res.at("/query/wrap/attr/operands/2/value").asText());
        assertEquals("match:ne",			res.at("/query/wrap/attr/operands/2/match").asText());
        assertEquals("s",	 				res.at("/query/wrap/attr/operands/2/layer").asText());
        assertEquals("koral:term",			res.at("/query/wrap/attr/operands/2/@type").asText());
        
        // check attr <> value - 27.03.26/FB
        
        query = "#ELEM(HEAD style<>bold)";
        
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:span", 	res.at("/query/@type").asText());
        assertEquals("head", 		res.at("/query/wrap/key").asText());
        assertEquals("s",    		res.at("/query/wrap/layer").asText());
		
        assertEquals("koral:term",	res.at("/query/wrap/attr/@type").asText());
        assertEquals("s", 			res.at("/query/wrap/attr/layer").asText()); // default layer for #ELEM(), same as for element name.
        assertEquals("style", 		res.at("/query/wrap/attr/key").asText());
        assertEquals("bold", 		res.at("/query/wrap/attr/value").asText());
        assertEquals("match:ne", 	res.at("/query/wrap/attr/match").asText());
        
        // no element specified, attribute only:
        query = "#ELEM(type=TOP)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:span", 	res.at("/query/@type").asText());
	
        assertEquals("koral:term",	res.at("/query/wrap/attr/@type").asText());
        assertEquals("s", 			res.at("/query/wrap/attr/layer").asText()); // default layer for #ELEM(), same as for element name.
        assertEquals("type", 		res.at("/query/wrap/attr/key").asText());
        assertEquals("TOP", 		res.at("/query/wrap/attr/value").asText());
        assertEquals("match:eq", 	res.at("/query/wrap/attr/match").asText());
    }
    
    /* a special case of #ELEM is the use of element name 'W' and/or attribute 'ana',
     * which must be mapped in KorAP onto the layer for part of speech.
     * 17.04.26/FB
     */
    
    @Test
    public void testELEM_W() throws JsonProcessingException, IOException 
    {
        query = "#ELEM(W ANA=NOU)";
        
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        // System.out.printf("Query='%s' res='%s'.\n", query, res.toString());
        
        assertEquals("koral:token",	res.at("/query/@type").asText());
        assertFalse("Element 'w' not expected as a key!", 
        							res.at("/query/wrap/key").asText().equals("w"));
        assertFalse("Attribute 'ana' not expected as a key!",
        							res.at("/query/attr/key").asText().equals("ana"));
        assertEquals("koral:term", 	res.at("/query/wrap/attr/@type").asText());
        assertEquals("NOU", 		res.at("/query/wrap/attr/key").asText());
        assertEquals(LAYER_POS, 	res.at("/query/wrap/attr/layer").asText()); // same as element name.
        assertEquals("match:eq", 	res.at("/query/wrap/attr/match").asText());

        // Element 'W' is optional when using attribute 'ana':
        
        query = "#ELEM(ana=NOU)";
        
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        // System.out.printf("Query='%s' res='%s'.\n", query, res.toString());
        
        assertEquals("koral:token",	res.at("/query/@type").asText());
        assertFalse("Element 'w' not expected as a key!", 
        							res.at("/query/wrap/key").asText().equals("w"));
        assertFalse("Attribute 'ana' not expected as a key!",
        							res.at("/query/wrap/key").asText().equals("ana"));
        assertEquals("koral:term", 	res.at("/query/wrap/@type").asText());
        assertEquals("NOU", 		res.at("/query/wrap/attr/key").asText());
        assertEquals(LAYER_POS, 	res.at("/query/wrap/attr/layer").asText()); // same as element name.
        assertEquals("match:eq", 	res.at("/query/wrap/attr/match").asText());

        query = "#ELEM(W ANA='NOU SG PROPN')";

        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token",		res.at("/query/@type").asText());
        
        assertEquals("koral:term", 		res.at("/query/wrap/@type").asText());
        assertEquals(LAYER_POS, 		res.at("/query/wrap/layer").asText());
        
        assertEquals("koral:termGroup",	res.at("/query/wrap/attr/@type").asText());
        assertEquals("relation:and", 	res.at("/query/wrap/attr/relation").asText());
        assertEquals("koral:term", 		res.at("/query/wrap/attr/operands/0/@type").asText());
        assertEquals("NOU",		 		res.at("/query/wrap/attr/operands/0/key").asText());
        assertEquals("match:eq",		res.at("/query/wrap/attr/operands/0/match").asText());
        assertEquals("koral:term", 		res.at("/query/wrap/attr/operands/1/@type").asText());
        assertEquals("SG",		 		res.at("/query/wrap/attr/operands/1/key").asText());
        assertEquals("match:eq",		res.at("/query/wrap/attr/operands/1/match").asText());
        assertEquals("koral:term", 		res.at("/query/wrap/attr/operands/2/@type").asText());
        assertEquals("PROPN",	 		res.at("/query/wrap/attr/operands/2/key").asText());
        assertEquals("match:eq",		res.at("/query/wrap/attr/operands/2/match").asText());

        // for part of speech, 2 kinds of negation are possible:
        query = "#ELEM(W ana='NOU -PL' ana!='PROPN')";

        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", 	res.at("/query/@type").asText());
        
        assertEquals("koral:term", 		res.at("/query/wrap/@type").asText());
        assertEquals(LAYER_POS, 		res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", 		res.at("/query/wrap/match").asText());
        
        assertEquals("koral:termGroup",	res.at("/query/wrap/attr/@type").asText());
        assertEquals("relation:and", 	res.at("/query/wrap/attr/relation").asText());
        assertEquals("koral:term", 		res.at("/query/wrap/attr/operands/0/operands/0/@type").asText());
        assertEquals("NOU",		 		res.at("/query/wrap/attr/operands/0/operands/0/key").asText());
        assertEquals("match:eq",		res.at("/query/wrap/attr/operands/0/operands/0/match").asText());
        assertEquals("koral:term", 		res.at("/query/wrap/attr/operands/0/operands/1/@type").asText());
        assertEquals("PL",		 		res.at("/query/wrap/attr/operands/0/operands/1/key").asText());
        assertEquals("match:ne",		res.at("/query/wrap/attr/operands/0/operands/1/match").asText());
        assertEquals("koral:termGroup",	res.at("/query/wrap/attr/operands/0/@type").asText());
        assertEquals("relation:and",	res.at("/query/wrap/attr/operands/0/relation").asText());
        assertEquals("koral:term", 		res.at("/query/wrap/attr/operands/1/@type").asText());
        assertEquals("PROPN",	 		res.at("/query/wrap/attr/operands/1/key").asText());
        assertEquals("match:ne",		res.at("/query/wrap/attr/operands/1/match").asText());
        
        // the other operator for negation:
        query = "#ELEM(W ANA!='NOU')";

        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token",		res.at("/query/@type").asText());
        assertEquals("koral:term", 		res.at("/query/wrap/@type").asText());
        assertEquals(LAYER_POS, 		res.at("/query/wrap/layer").asText());
        assertEquals("match:eq", 		res.at("/query/wrap/match").asText());
        
        assertEquals("koral:term",		res.at("/query/wrap/attr/@type").asText());
        assertEquals("match:ne",		res.at("/query/wrap/attr/match").asText());
        assertEquals("NOU",			 	res.at("/query/wrap/attr/key").asText());
        assertEquals(LAYER_POS,			res.at("/query/wrap/attr/layer").asText());
        
        // different attributes: 'ana' and non 'ana':
        query = "#ELEM(W ANA = 'NOU' type!='COMP')";
        
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token",		res.at("/query/@type").asText());
        
        assertEquals("koral:term", 		res.at("/query/wrap/@type").asText());
        assertEquals("match:eq", 		res.at("/query/wrap/match").asText());
        assertEquals(LAYER_POS,		 	res.at("/query/wrap/layer").asText());
        
        assertEquals("koral:termGroup", res.at("/query/wrap/attr/@type").asText());
        assertEquals("relation:and", 	res.at("/query/wrap/attr/relation").asText());
        
        assertEquals("koral:term",		res.at("/query/wrap/attr/operands/0/@type").asText());
        assertEquals("match:eq",	 	res.at("/query/wrap/attr/operands/0/match").asText());
        assertEquals("NOU",				res.at("/query/wrap/attr/operands/0/key").asText());
        
        assertEquals("koral:term",		res.at("/query/wrap/attr/operands/1/@type").asText());
        assertEquals("match:ne",	 	res.at("/query/wrap/attr/operands/1/match").asText());
        assertEquals("type",			res.at("/query/wrap/attr/operands/1/key").asText()); 	// normal case 
        assertEquals("COMP",			res.at("/query/wrap/attr/operands/1/value").asText());	// normal case

    }

    /*
     * Extensions of C2-Queries for KorAP : 
     * - specify foundry and layer.
     * - e.g. #ELEM(base/p=NOU).
     * 20.04.26/FB
     */
    
    @Test
    public void testELEM_Foundry() throws JsonProcessingException, IOException {
    	
    // special case: for "dereko/p" the @type is a term.
	query = "#ELEM(dereko/s=head)";
	
    qs.setQuery(query, "cosmas2");
    res = mapper.readTree(qs.toJSON());
    
    assertEquals("koral:span",	 		res.at("/query/@type").asText());
    assertEquals("koral:term", 			res.at("/query/wrap/@type").asText());
    assertEquals("match:eq",		 	res.at("/query/wrap/attr/match").asText());
    assertEquals("dereko", 				res.at("/query/wrap/attr/foundry").asText());
    assertEquals("s", 					res.at("/query/wrap/attr/layer").asText());
    assertEquals("head", 				res.at("/query/wrap/attr/key").asText());
    
    /* base or dereko ? */
    query = "#ELEM(base/c=NP)";
    
    qs.setQuery(query, "cosmas2");
    res = mapper.readTree(qs.toJSON());
    
    assertEquals("koral:span", 		res.at("/query/@type").asText());
    assertEquals("koral:term",		res.at("/query/wrap/@type").asText());
    assertEquals("base", 			res.at("/query/wrap/attr/foundry").asText());
    assertEquals("c", 				res.at("/query/wrap/attr/layer").asText());
    assertEquals("NP", 				res.at("/query/wrap/attr/key").asText());
    
    query = "#ELEM(marmot/m=number:pl)";
    
    qs.setQuery(query, "cosmas2");
    res = mapper.readTree(qs.toJSON());
    
    assertEquals("koral:token",		res.at("/query/@type").asText());
    assertEquals("koral:term",		res.at("/query/wrap/@type").asText());
    assertEquals("marmot", 			res.at("/query/wrap/attr/foundry").asText());
    assertEquals("m",	 			res.at("/query/wrap/attr/layer").asText());
    assertEquals("number", 			res.at("/query/wrap/attr/key").asText());
    assertEquals("pl",	 			res.at("/query/wrap/attr/value").asText());
    
    query = "#ELEM(marmot/m='number:pl gender:-fem')";
    
    qs.setQuery(query, "cosmas2");
    res = mapper.readTree(qs.toJSON());
    
    assertEquals("koral:token",		res.at("/query/@type").asText());
    assertEquals("koral:term",		res.at("/query/wrap/@type").asText());
    assertEquals("marmot", 			res.at("/query/wrap/attr/operands/0/foundry").asText());
    assertEquals("m",	 			res.at("/query/wrap/attr/operands/0/layer").asText());
    assertEquals("number", 			res.at("/query/wrap/attr/operands/0/key").asText());
    assertEquals("pl",	 			res.at("/query/wrap/attr/operands/0/value").asText());
    
    assertEquals("marmot", 			res.at("/query/wrap/attr/operands/1/foundry").asText());
    assertEquals("m",	 			res.at("/query/wrap/attr/operands/1/layer").asText());
    assertEquals("gender", 			res.at("/query/wrap/attr/operands/1/key").asText());
    assertEquals("fem",	 			res.at("/query/wrap/attr/operands/1/value").asText());
    assertEquals("match:ne",		res.at("/query/wrap/attr/operands/1/match").asText());
    
    query = "#ELEM(marmot/m!=number:pl)";
    
    qs.setQuery(query, "cosmas2");
    res = mapper.readTree(qs.toJSON());
    
    assertEquals("koral:token",		res.at("/query/@type").asText());
    assertEquals("koral:term",		res.at("/query/wrap/@type").asText());
    assertEquals("marmot", 			res.at("/query/wrap/attr/foundry").asText());
    assertEquals("m",	 			res.at("/query/wrap/attr/layer").asText());
    assertEquals("number", 			res.at("/query/wrap/attr/key").asText());
    assertEquals("pl",	 			res.at("/query/wrap/attr/value").asText());
    assertEquals("match:ne",		res.at("/query/wrap/attr/match").asText());
    
    query = "#ELEM(HEAD dereko/s<>type:top)";
    
    qs.setQuery(query, "cosmas2");
    res = mapper.readTree(qs.toJSON());
    
    assertEquals("koral:span", 		res.at("/query/@type").asText());
    assertEquals("koral:term",		res.at("/query/wrap/@type").asText());
    assertEquals("dereko", 			res.at("/query/wrap/attr/foundry").asText());
    assertEquals("s", 				res.at("/query/wrap/attr/layer").asText());
    assertEquals("type", 			res.at("/query/wrap/attr/key").asText());
    assertEquals("top",	 			res.at("/query/wrap/attr/value").asText());
    assertEquals("match:ne",		res.at("/query/wrap/attr/match").asText());

    query = "#ELEM(HEAD dereko/s='top bottom')";
    
    qs.setQuery(query, "cosmas2");
    res = mapper.readTree(qs.toJSON());
    
    assertEquals("koral:span", 		res.at("/query/@type").asText());
    assertEquals("koral:term",		res.at("/query/wrap/@type").asText());
    assertEquals("s",				res.at("/query/wrap/layer").asText());
    assertEquals("head",			res.at("/query/wrap/key").asText());
    assertEquals("koral:termGroup",	res.at("/query/wrap/attr/@type").asText());
    assertEquals("relation:and",	res.at("/query/wrap/attr/relation").asText());
    assertEquals("dereko", 			res.at("/query/wrap/attr/operands/0/foundry").asText());
    assertEquals("s",				res.at("/query/wrap/attr/operands/0/layer").asText());
    assertEquals("top", 			res.at("/query/wrap/attr/operands/0/key").asText());
    assertEquals("dereko", 			res.at("/query/wrap/attr/operands/1/foundry").asText());
    assertEquals("s",				res.at("/query/wrap/attr/operands/1/layer").asText());
    assertEquals("bottom", 			res.at("/query/wrap/attr/operands/1/key").asText());
    
    /*
     * test the other foundries/layers for either token or span.
     */
    
    query = "#ELEM(tt/l=Haus)";
    
    qs.setQuery(query, "cosmas2");
    res = mapper.readTree(qs.toJSON());
    
    assertEquals("koral:token",		res.at("/query/@type").asText());
    assertEquals("koral:term",		res.at("/query/wrap/@type").asText());
    assertEquals("koral:term",		res.at("/query/wrap/attr/@type").asText());
    assertEquals("tt",	 			res.at("/query/wrap/attr/foundry").asText());
    assertEquals("l",				res.at("/query/wrap/attr/layer").asText());
    assertEquals("Haus", 			res.at("/query/wrap/attr/key").asText());
    
    query = "#ELEM(base/s=s)"; 		// sentences
    
    qs.setQuery(query, "cosmas2");
    res = mapper.readTree(qs.toJSON());
    
    assertEquals("koral:span", 		res.at("/query/@type").asText());
    assertEquals("koral:term",		res.at("/query/wrap/@type").asText());
    assertEquals("base", 			res.at("/query/wrap/attr/foundry").asText());
    assertEquals("s", 				res.at("/query/wrap/attr/layer").asText());
    assertEquals("s", 				res.at("/query/wrap/attr/key").asText());

    query = "#ELEM(base/s=p)"; 		// paragraphes
    
    qs.setQuery(query, "cosmas2");
    res = mapper.readTree(qs.toJSON());
    
    assertEquals("koral:span", 		res.at("/query/@type").asText());
    assertEquals("koral:term",		res.at("/query/wrap/@type").asText());
    assertEquals("base", 			res.at("/query/wrap/attr/foundry").asText());
    assertEquals("s", 				res.at("/query/wrap/attr/layer").asText());
    assertEquals("p", 				res.at("/query/wrap/attr/key").asText());

    query = "#ELEM(base/s=t)"; 		// texts (?)
    
    qs.setQuery(query, "cosmas2");
    res = mapper.readTree(qs.toJSON());
    
    assertEquals("koral:span", 		res.at("/query/@type").asText());
    assertEquals("koral:term",		res.at("/query/wrap/@type").asText());
    assertEquals("base", 			res.at("/query/wrap/attr/foundry").asText());
    assertEquals("s", 				res.at("/query/wrap/attr/layer").asText());
    assertEquals("t", 				res.at("/query/wrap/attr/key").asText());

    query = "#ELEM(marmot/p=APPO)";
        
    qs.setQuery(query, "cosmas2");
    res = mapper.readTree(qs.toJSON());
    
    assertEquals("koral:token",		res.at("/query/@type").asText());
    assertEquals("koral:term",		res.at("/query/wrap/@type").asText());
    assertEquals("koral:term",		res.at("/query/wrap/attr/@type").asText());
    assertEquals("marmot", 			res.at("/query/wrap/attr/foundry").asText());
    assertEquals("p",				res.at("/query/wrap/attr/layer").asText());
    assertEquals("APPO", 			res.at("/query/wrap/attr/key").asText());
    
    query = "#ELEM(corenlp/p=ADJD)";
    
    qs.setQuery(query, "cosmas2");
    res = mapper.readTree(qs.toJSON());
    
    assertEquals("koral:token",		res.at("/query/@type").asText());
    assertEquals("koral:term",		res.at("/query/wrap/@type").asText());
    assertEquals("corenlp",			res.at("/query/wrap/attr/foundry").asText());
    assertEquals("p",				res.at("/query/wrap/attr/layer").asText());
    assertEquals("ADJD", 			res.at("/query/wrap/attr/key").asText());
    
    query = "#ELEM(corenlp/c=AP)";
    
    qs.setQuery(query, "cosmas2");
    res = mapper.readTree(qs.toJSON());
    
    assertEquals("koral:span",		res.at("/query/@type").asText());
    assertEquals("koral:term",		res.at("/query/wrap/@type").asText());
    assertEquals("koral:term",		res.at("/query/wrap/attr/@type").asText());
    assertEquals("corenlp",			res.at("/query/wrap/attr/foundry").asText());
    assertEquals("c",				res.at("/query/wrap/attr/layer").asText());
    assertEquals("AP", 				res.at("/query/wrap/attr/key").asText());
    
    query = "#ELEM(corenlp/ne=I-ORG)";
    
    qs.setQuery(query, "cosmas2");
    res = mapper.readTree(qs.toJSON());
    
    assertEquals("koral:token",		res.at("/query/@type").asText());
    assertEquals("koral:term",		res.at("/query/wrap/@type").asText());
    assertEquals("koral:term",		res.at("/query/wrap/attr/@type").asText());
    assertEquals("corenlp",			res.at("/query/wrap/attr/foundry").asText());
    assertEquals("ne",				res.at("/query/wrap/attr/layer").asText());
    assertEquals("I-ORG", 			res.at("/query/wrap/attr/key").asText());

    }

    @Test
    public void testOPALL () throws JsonProcessingException, IOException {
        query = "#ALL(gehen /w1:10 voran)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("gehen", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("voran", res.at("/query/operands/1/wrap/key").asText());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("w", res.at("/query/distances/0/key").asText());
        assertEquals(1, res.at("/query/distances/0/boundary/min").asInt());
        assertEquals(10, res.at("/query/distances/0/boundary/max").asInt());

        query = "#ALL(gehen /w1:10 (voran /w1:4 schnell))";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:group", res.at("/query/@type").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type")
                .asText());
        assertEquals("w", res.at("/query/distances/0/key").asText());
        assertEquals(1, res.at("/query/distances/0/boundary/min").asInt());
        assertEquals(10, res.at("/query/distances/0/boundary/max").asInt());
        assertEquals("gehen", res.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:group", res.at("/query/operands/1/@type").asText());
        assertEquals("operation:sequence", res
                .at("/query/operands/1/operation").asText());
        assertEquals("cosmas:distance",
                res.at("/query/operands/1/distances/0/@type").asText());
        assertEquals("w", res.at("/query/operands/1/distances/0/key").asText());
        assertEquals(1, res.at("/query/operands/1/distances/0/boundary/min")
                .asInt());
        assertEquals(4, res.at("/query/operands/1/distances/0/boundary/max")
                .asInt());
        assertEquals("voran", res.at("/query/operands/1/operands/0/wrap/key")
                .asText());
        assertEquals("schnell", res.at("/query/operands/1/operands/1/wrap/key")
                .asText());


    }


    @Test
    public void testOPNHIT () throws JsonProcessingException, IOException {
        query = "#NHIT(gehen /w1:10 voran)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(129, res.at("/query/classRef/0").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/0/operation")
                .asText());
        assertEquals("classRefOp:inversion",
                res.at("/query/operands/0/classRefOp").asText());
        assertEquals(130, res.at("/query/operands/0/classIn/0").asInt());
        assertEquals(131, res.at("/query/operands/0/classIn/1").asInt());
        assertEquals(129, res.at("/query/operands/0/classOut").asInt());
        assertEquals("koral:group", res
                .at("/query/operands/0/operands/0/@type").asText());
        assertEquals("operation:sequence",
                res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals("cosmas:distance",
                res.at("/query/operands/0/operands/0/distances/0/@type")
                        .asText());
        assertEquals("w", res
                .at("/query/operands/0/operands/0/distances/0/key").asText());
        assertEquals(1,
                res.at("/query/operands/0/operands/0/distances/0/boundary/min")
                        .asInt());
        assertEquals(10,
                res.at("/query/operands/0/operands/0/distances/0/boundary/max")
                        .asInt());
        assertEquals("koral:group",
                res.at("/query/operands/0/operands/0/operands/0/@type")
                        .asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/0/operands/0/operation")
                        .asText());
        assertEquals(130,
                res.at("/query/operands/0/operands/0/operands/0/classOut")
                        .asInt());
        assertEquals(131,
                res.at("/query/operands/0/operands/0/operands/1/classOut")
                        .asInt());
        assertEquals(
                "koral:token",
                res.at("/query/operands/0/operands/0/operands/0/operands/0/@type")
                        .asText());
        assertEquals(
                "gehen",
                res.at("/query/operands/0/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(
                "voran",
                res.at("/query/operands/0/operands/0/operands/1/operands/0/wrap/key")
                        .asText());

        query = "#NHIT(gehen /w1:10 voran /w1:10 Beispiel)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(129, res.at("/query/classRef/0").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:class", res.at("/query/operands/0/operation")
                .asText());
        assertEquals("classRefOp:inversion",
                res.at("/query/operands/0/classRefOp").asText());
        assertEquals(130, res.at("/query/operands/0/classIn/0").asInt());
        assertEquals(131, res.at("/query/operands/0/classIn/1").asInt());
        assertEquals(129, res.at("/query/operands/0/classOut").asInt());
        assertEquals("koral:group", res
                .at("/query/operands/0/operands/0/@type").asText());
        assertEquals("operation:sequence",
                res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals("cosmas:distance",
                res.at("/query/operands/0/operands/0/distances/0/@type")
                        .asText());
        assertEquals("w", res
                .at("/query/operands/0/operands/0/distances/0/key").asText());
        assertEquals(1,
                res.at("/query/operands/0/operands/0/distances/0/boundary/min")
                        .asInt());
        assertEquals(10,
                res.at("/query/operands/0/operands/0/distances/0/boundary/max")
                        .asInt());
        assertEquals("koral:group",
                res.at("/query/operands/0/operands/0/operands/0/@type")
                        .asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/0/operands/0/operation")
                        .asText());
        assertEquals(130,
                res.at("/query/operands/0/operands/0/operands/0/classOut")
                        .asInt());
        assertEquals(
                "gehen",
                res.at("/query/operands/0/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(131,
                res.at("/query/operands/0/operands/0/operands/1/classOut")
                        .asInt());
        //        assertEquals("classRefOp:merge",    res.at("/query/operands/0/operands/0/operands/1/classRefOp").asText());
        assertEquals(
                "operation:sequence",
                res.at("/query/operands/0/operands/0/operands/1/operands/0/operation")
                        .asText());
        assertEquals(
                132,
                res.at("/query/operands/0/operands/0/operands/1/operands/0/operands/0/classOut")
                        .asInt());
        assertEquals(
                "voran",
                res.at("/query/operands/0/operands/0/operands/1/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(
                132,
                res.at("/query/operands/0/operands/0/operands/1/operands/0/operands/1/classOut")
                        .asInt());
        assertEquals(
                "Beispiel",
                res.at("/query/operands/0/operands/0/operands/1/operands/0/operands/1/operands/0/wrap/key")
                        .asText());

    }

    /* some tests added - 08.11.23/FB
     */
    
    @Test
    public void testOPBED () throws JsonProcessingException, IOException {
        query = "#BED(der , sa)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(129, res.at("/query/classRef/0").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:position", res
                .at("/query/operands/0/operation").asText());
        assertEquals("frames:startsWith", res.at("/query/operands/0/frames/0")
                .asText());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals(129, res.at("/query/operands/0/operands/1/classOut")
                .asInt());
        assertEquals("koral:token",
                res.at("/query/operands/0/operands/1/operands/0/@type")
                        .asText());
        assertEquals("der",
                res.at("/query/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals("koral:span", res.at("/query/operands/0/operands/0/@type")
                .asText());
        assertEquals("s", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());

        // 08.11.23/FB
        // treats now "der," as "der" + ",":
        query = "#BED(der, sa)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(129, res.at("/query/classRef/0").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:position", res
                .at("/query/operands/0/operation").asText());
        assertEquals("frames:startsWith", res.at("/query/operands/0/frames/0")
                .asText());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals(129, res.at("/query/operands/0/operands/1/classOut")
                .asInt());
        assertEquals("koral:token",
                res.at("/query/operands/0/operands/1/operands/0/@type")
                        .asText());
        assertEquals("der",
                res.at("/query/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals("koral:span", res.at("/query/operands/0/operands/0/@type")
                .asText());
        assertEquals("s", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        
        
        // 08.11.23/FB
        // treats now "der,sa" as "der" + "," + "sa":
        query = "#BED(der,sa)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(129, res.at("/query/classRef/0").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:position", res
                .at("/query/operands/0/operation").asText());
        assertEquals("frames:startsWith", res.at("/query/operands/0/frames/0")
                .asText());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals(129, res.at("/query/operands/0/operands/1/classOut")
                .asInt());
        assertEquals("koral:token",
                res.at("/query/operands/0/operands/1/operands/0/@type")
                        .asText());
        assertEquals("der",
                res.at("/query/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals("koral:span", res.at("/query/operands/0/operands/0/@type")
                .asText());
        assertEquals("s", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        
        // 08.11.23/FB
        // treats now "der,s0," as "der,s0" unchanged while written inside "...":
        query = "#BED(\"der,so\", sa)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(129, res.at("/query/classRef/0").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:position", res
                .at("/query/operands/0/operation").asText());
        assertEquals("frames:startsWith", res.at("/query/operands/0/frames/0")
                .asText());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals(129, res.at("/query/operands/0/operands/1/classOut")
                .asInt());
        assertEquals("koral:token",
                res.at("/query/operands/0/operands/1/operands/0/@type")
                        .asText());
        assertEquals("der,so",
                res.at("/query/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals("koral:span", res.at("/query/operands/0/operands/0/@type")
                .asText());
        assertEquals("s", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
        
        query = "#COND(der , sa)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(129, res.at("/query/classRef/0").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:position", res
                .at("/query/operands/0/operation").asText());
        assertEquals("frames:startsWith", res.at("/query/operands/0/frames/0")
                .asText());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals(129, res.at("/query/operands/0/operands/1/classOut")
                .asInt());
        assertEquals("koral:token",
                res.at("/query/operands/0/operands/1/operands/0/@type")
                        .asText());
        assertEquals("der",
                res.at("/query/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals("koral:span", res.at("/query/operands/0/operands/0/@type")
                .asText());
        assertEquals("s", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());

        
        query = "#BED(der Mann , +pe)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(129, res.at("/query/classRef/0").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:position", res
                .at("/query/operands/0/operation").asText());
        assertEquals("frames:matches", res.at("/query/operands/0/frames/0")
                .asText());
        assertEquals("koral:reference",
                res.at("/query/operands/0/operands/0/@type").asText());
        assertEquals("operation:focus",
                res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals(-1, res.at("/query/operands/0/operands/0/spanRef/0")
                .asInt());
        assertEquals(1, res.at("/query/operands/0/operands/0/spanRef/1")
                .asInt());
        assertEquals("koral:span",
                res.at("/query/operands/0/operands/0/operands/0/@type")
                        .asText());
        assertEquals("p",
                res.at("/query/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals("koral:reference",
                res.at("/query/operands/0/operands/1/@type").asText());
        assertEquals("operation:focus",
                res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals(0, res.at("/query/operands/0/operands/1/spanRef/0")
                .asInt());
        assertEquals(1, res.at("/query/operands/0/operands/1/spanRef/1")
                .asInt());
        assertEquals("koral:group",
                res.at("/query/operands/0/operands/1/operands/0/@type")
                        .asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/1/operands/0/operation")
                        .asText());
        assertEquals(129,
                res.at("/query/operands/0/operands/1/operands/0/classOut")
                        .asInt());
        assertEquals(
                "operation:sequence",
                res.at("/query/operands/0/operands/1/operands/0/operands/0/operation")
                        .asText());
        assertEquals(
                "der",
                res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(
                "Mann",
                res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/1/wrap/key")
                        .asText());

        query = "#BED(der Mann , sa,-pa)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(129, res.at("/query/classRef/0").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:position", res
                .at("/query/operands/0/operation").asText());
        assertEquals("frames:matches", res.at("/query/operands/0/frames/0")
                .asText());

        assertEquals("koral:group", res
                .at("/query/operands/0/operands/0/@type").asText());
        assertEquals("operation:position",
                res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals("frames:startsWith",
                res.at("/query/operands/0/operands/0/frames/0").asText());
        assertEquals("koral:span",
                res.at("/query/operands/0/operands/0/operands/0/@type")
                        .asText());
        assertEquals("s",
                res.at("/query/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals("koral:group",
                res.at("/query/operands/0/operands/0/operands/1/@type")
                        .asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/0/operands/1/operation")
                        .asText());
        assertEquals(129,
                res.at("/query/operands/0/operands/0/operands/1/classOut")
                        .asInt());
        assertEquals(
                "operation:sequence",
                res.at("/query/operands/0/operands/0/operands/1/operands/0/operation")
                        .asText());
        assertEquals(
                "der",
                res.at("/query/operands/0/operands/0/operands/1/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(
                "Mann",
                res.at("/query/operands/0/operands/0/operands/1/operands/0/operands/1/wrap/key")
                        .asText());

        assertEquals("koral:group", res
                .at("/query/operands/0/operands/1/@type").asText());
        assertEquals("operation:position",
                res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals("frames:startsWith",
                res.at("/query/operands/0/operands/1/frames/0").asText());
        assertTrue(res.at("/query/operands/0/operands/1/exclude")
                .asBoolean());
        assertEquals("koral:span",
                res.at("/query/operands/0/operands/1/operands/0/@type")
                        .asText());
        assertEquals("p",
                res.at("/query/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals("koral:group",
                res.at("/query/operands/0/operands/1/operands/1/@type")
                        .asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/1/operands/1/operation")
                        .asText());
        assertEquals(130,
                res.at("/query/operands/0/operands/1/operands/1/classOut")
                        .asInt());
        assertEquals(
                "operation:sequence",
                res.at("/query/operands/0/operands/1/operands/1/operands/0/operation")
                        .asText());
        assertEquals(
                "der",
                res.at("/query/operands/0/operands/1/operands/1/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(
                "Mann",
                res.at("/query/operands/0/operands/1/operands/1/operands/0/operands/1/wrap/key")
                        .asText());

    }


    @Test
    public void testColonSeparatedConditions () throws JsonProcessingException,
            IOException {
    	
        query = "der:sa";
        if( showParserErrorQuery ) System.err.printf("Debug: intentional warning: query='%s'.\n", query);
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        if( showParserErrorQuery ) System.err.printf("Debug: intentional warning: query='%s': done.\n", query);
        
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(129, res.at("/query/classRef/0").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:position", res
                .at("/query/operands/0/operation").asText());
        assertEquals("frames:startsWith", res.at("/query/operands/0/frames/0")
                .asText());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals(129, res.at("/query/operands/0/operands/1/classOut")
                .asInt());
        assertEquals("koral:token",
                res.at("/query/operands/0/operands/1/operands/0/@type")
                        .asText());
        assertEquals("der",
                res.at("/query/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals("koral:span", res.at("/query/operands/0/operands/0/@type")
                .asText());
        assertEquals("s", res.at("/query/operands/0/operands/0/wrap/key")
                .asText());
/* fails after modifications to MORPH -- 12.06.26/FB
        query = "der:sa,-pa";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(129, res.at("/query/classRef/0").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:position", res
                .at("/query/operands/0/operation").asText());
        assertEquals("frames:matches", res.at("/query/operands/0/frames/0")
                .asText());
        assertEquals("koral:group", res
                .at("/query/operands/0/operands/0/@type").asText());
        assertEquals("operation:position",
                res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals("frames:startsWith",
                res.at("/query/operands/0/operands/0/frames/0").asText());
        assertEquals("koral:span",
                res.at("/query/operands/0/operands/0/operands/0/@type")
                        .asText());
        assertEquals("s",
                res.at("/query/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals("koral:group",
                res.at("/query/operands/0/operands/0/operands/1/@type")
                        .asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/0/operands/1/operation")
                        .asText());
        assertEquals(129,
                res.at("/query/operands/0/operands/0/operands/1/classOut")
                        .asInt());
        assertEquals(
                "der",
                res.at("/query/operands/0/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals("koral:group", res
                .at("/query/operands/0/operands/1/@type").asText());
        assertEquals("operation:position",
                res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals("frames:startsWith",
                res.at("/query/operands/0/operands/1/frames/0").asText());
        assertTrue(res.at("/query/operands/0/operands/1/exclude")
                .asBoolean());
        assertEquals("koral:span",
                res.at("/query/operands/0/operands/1/operands/0/@type")
                        .asText());
        assertEquals("p",
                res.at("/query/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals("koral:group",
                res.at("/query/operands/0/operands/1/operands/1/@type")
                        .asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/1/operands/1/operation")
                        .asText());
        assertEquals(130,
                res.at("/query/operands/0/operands/1/operands/1/classOut")
                        .asInt());
        assertEquals(
                "der",
                res.at("/query/operands/0/operands/1/operands/1/operands/0/wrap/key")
                        .asText());
*/
        /*
        query = "der:sa,-pa,+te";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("koral:reference", res.at("/query/@type").asText());
        assertEquals("operation:focus", res.at("/query/operation").asText());
        assertEquals(129, res.at("/query/classRef/0").asInt());
        assertEquals("koral:group", res.at("/query/operands/0/@type").asText());
        assertEquals("operation:position", res
                .at("/query/operands/0/operation").asText());
        assertEquals("frames:matches", res.at("/query/operands/0/frames/0")
                .asText());
        assertEquals("koral:group", res
                .at("/query/operands/0/operands/0/@type").asText());
        assertEquals("operation:position",
                res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals("frames:startsWith",
                res.at("/query/operands/0/operands/0/frames/0").asText());
        assertEquals("koral:span",
                res.at("/query/operands/0/operands/0/operands/0/@type")
                        .asText());
        assertEquals("s",
                res.at("/query/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals("koral:group",
                res.at("/query/operands/0/operands/0/operands/1/@type")
                        .asText());
        assertEquals("operation:class",
                res.at("/query/operands/0/operands/0/operands/1/operation")
                        .asText());
        assertEquals(129,
                res.at("/query/operands/0/operands/0/operands/1/classOut")
                        .asInt());
        assertEquals(
                "der",
                res.at("/query/operands/0/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals("koral:reference",
                res.at("/query/operands/0/operands/1/@type").asText());
        assertEquals("operation:focus",
                res.at("/query/operands/0/operands/1/operation").asText());
        assertEquals(130, res.at("/query/operands/0/operands/1/classRef/0")
                .asInt());
        assertEquals("koral:group",
                res.at("/query/operands/0/operands/1/operands/0/@type")
                        .asText());
        assertEquals("operation:position",
                res.at("/query/operands/0/operands/1/operands/0/operation")
                        .asText());
        assertEquals("frames:matches",
                res.at("/query/operands/0/operands/1/operands/0/frames/0")
                        .asText());
        assertEquals(
                "koral:group",
                res.at("/query/operands/0/operands/1/operands/0/operands/0/@type")
                        .asText());
        assertEquals(
                "operation:position",
                res.at("/query/operands/0/operands/1/operands/0/operands/0/operation")
                        .asText());
        assertEquals(
                "frames:startsWith",
                res.at("/query/operands/0/operands/1/operands/0/operands/0/frames/0")
                        .asText());
        assertTrue(res.at("/query/operands/0/operands/1/operands/0/operands/0/exclude")
                .asBoolean());
        assertEquals(
                "koral:span",
                res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/0/@type")
                        .asText());
        assertEquals(
                "p",
                res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(
                "koral:group",
                res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/1/@type")
                        .asText());
        assertEquals(
                "operation:class",
                res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/1/operation")
                        .asText());
        assertEquals(
                130,
                res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/1/classOut")
                        .asInt());
        assertEquals(
                "der",
                res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/1/operands/0/wrap/key")
                        .asText());
        assertEquals(
                "koral:group",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/@type")
                        .asText());
        assertEquals(
                "operation:position",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operation")
                        .asText());
        assertEquals(
                "frames:matches",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/frames/0")
                        .asText());
        assertEquals(
                "koral:reference",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/0/@type")
                        .asText());
        assertEquals(
                "operation:focus",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/0/operation")
                        .asText());
        assertEquals(
                -1,
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/0/spanRef/0")
                        .asInt());
        assertEquals(
                1,
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/0/spanRef/1")
                        .asInt());
        assertEquals(
                "koral:span",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/0/operands/0/@type")
                        .asText());
        assertEquals(
                "t",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/0/operands/0/wrap/key")
                        .asText());
        assertEquals(
                "koral:reference",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/1/@type")
                        .asText());
        assertEquals(
                "operation:focus",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/1/operation")
                        .asText());
        assertEquals(
                0,
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/1/spanRef/0")
                        .asInt());
        assertEquals(
                1,
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/1/spanRef/1")
                        .asInt());
        assertEquals(
                "koral:group",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/1/operands/0/@type")
                        .asText());
        assertEquals(
                "operation:class",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/1/operands/0/operation")
                        .asText());
        assertEquals(
                131,
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/1/operands/0/classOut")
                        .asInt());
        assertEquals(
                "der",
                res.at("/query/operands/0/operands/1/operands/0/operands/1/operands/1/operands/0/operands/0/wrap/key")
                        .asText());
*/
    }

    @Test
    public void testWildcard () throws JsonProcessingException, IOException {
        query = "meine* /+w1:2,s0 &Erfahrung";
        qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
        assertEquals("type:regex",
					 res.at("/query/operands/0/operands/0/wrap/type").asText());
        assertEquals("meine.*",
					 res.at("/query/operands/0/operands/0/wrap/key").asText());
	};	

    @Test
    public void testErrors () throws JsonProcessingException, IOException {
        query = "MORPH(tt/p=\"\")";
        if( showParserErrorQuery ) System.err.printf("Debug: intentional warning: query='%s'.\n", query);
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        if( showParserErrorQuery ) System.err.printf("Debug: intentional warning: query='%s': done.\n", query);
        assertTrue(res.at("/query/@type").isMissingNode());
        assertEquals(StatusCodes.MALFORMED_QUERY,
                res.at("/errors/0/0").asInt());
        assertTrue(res
                .at("/errors/0/1")
                .asText()
                .startsWith(
                        "Empty MORPH() operator"));
        
        query = "MORPH(tt/p=\"foo)";
        if( showParserErrorQuery ) System.err.printf("Debug: intentional warning: query='%s'.\n", query);
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        if( showParserErrorQuery ) System.err.printf("Debug: intentional warning: query='%s': done.\n", query);
        assertTrue(res.at("/query/@type").isMissingNode());
        assertEquals(StatusCodes.MALFORMED_QUERY, res.at("/errors/0/0").asInt());
        assertTrue(res.at("/errors/0/1").asText()
                .startsWith("unexpected input at position"));
		
        query = "MORPH(tt/p=)";
        if( showParserErrorQuery ) System.err.printf("Debug: intentional warning: query='%s'.\n", query);
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        if( showParserErrorQuery ) System.err.printf("Debug: intentional warning: query='%s': done.\n", query);
        assertTrue(res.at("/query/@type").isMissingNode());
        assertEquals(StatusCodes.MALFORMED_QUERY, res.at("/errors/0/0").asInt());
        assertTrue(res.at("/errors/0/1").asText()
                .startsWith("unexpected input at position"));
    	
    }

    @Test
    public void testMultipleParenthesis () throws JsonProcessingException, IOException {
        query = "(Pop-up OR Pop-ups) %s0 (Internet OR  Programm)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        assertEquals("Pop-up", res.at("/query/operands/0/operands/0/operands/0/wrap/key").asText());
        assertEquals("Pop-ups", res.at("/query/operands/0/operands/0/operands/1/wrap/key").asText());
        assertEquals("operation:disjunction", res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals(129, res.at("/query/operands/0/classOut").asInt());
        assertEquals("operation:disjunction", res.at("/query/operands/1/operands/0/operation").asText());
        assertEquals("cosmas:distance", res.at("/query/distances/0/@type").asText());
        assertTrue(res.at("/query/distances/0/exclude").asBoolean());
        assertEquals("s", res.at("/query/distances/0/key").asText());
        assertEquals("operation:sequence", res.at("/query/operation").asText());
    }
    
    /* Testing #REG(expr), #REG('expr') and #REG("expr").
     * 21.09.23/FB
     */
     
    @Test
    public void testREG () throws JsonProcessingException, IOException {
    	
    	boolean debug = false;
    	
        query = "#REG(^aber$)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("koral:token", res.at("/query/@type").asText());
        assertEquals("koral:term",  res.at("/query/wrap/@type").asText());
        assertEquals("^aber$",      res.at("/query/wrap/key").asText());
        assertEquals("orth",        res.at("/query/wrap/layer").asText());
        assertEquals("type:regex",  res.at("/query/wrap/type").asText());
        assertEquals("match:eq",    res.at("/query/wrap/match").asText());

        query = "#REG('été\\'')";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("été'"	,       res.at("/query/wrap/key").asText());
        assertEquals("type:regex",  res.at("/query/wrap/type").asText());
        assertEquals("orth",        res.at("/query/wrap/layer").asText());

        query = "#REG('été\' )";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("été"	,       res.at("/query/wrap/key").asText());
        assertEquals("type:regex",  res.at("/query/wrap/type").asText());
        assertEquals("orth",        res.at("/query/wrap/layer").asText());

        query = "#REG('été\\')";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("été\\",       res.at("/query/wrap/key").asText());
        assertEquals("type:regex",  res.at("/query/wrap/type").asText());
        assertEquals("orth",        res.at("/query/wrap/layer").asText());

        query = "#REG(l'été)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        if( debug ) System.out.printf("testREG: query: >>%s<< -> key: >>%s<<.\n",  query, res.at("/query/wrap/key").asText());
        assertEquals("l'été",       res.at("/query/wrap/key").asText());
        assertEquals("type:regex",  res.at("/query/wrap/type").asText());
        assertEquals("orth",        res.at("/query/wrap/layer").asText());

        query = "#REG(l\\'été)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        if( debug ) System.out.printf("testREG: query: >>%s<< -> key: >>%s<<.\n",  query, res.at("/query/wrap/key").asText());
        assertEquals("l'été",       res.at("/query/wrap/key").asText());
        assertEquals("type:regex",  res.at("/query/wrap/type").asText());
        assertEquals("orth",        res.at("/query/wrap/layer").asText());

        query = "#REG(\"l'été\")";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        if( debug ) System.out.printf("testREG: query: >>%s<< -> key: >>%s<<.\n",  query, res.at("/query/wrap/key").asText());
        assertEquals("l'été",       res.at("/query/wrap/key").asText());
        assertEquals("type:regex",  res.at("/query/wrap/type").asText());
        assertEquals("orth",        res.at("/query/wrap/layer").asText());

        query = "#REG(\"l\\'été\")";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        if( debug ) System.out.printf("testREG: query: >>%s<< -> key: >>%s<<.\n",  query, res.at("/query/wrap/key").asText());
        assertEquals("l'été",       res.at("/query/wrap/key").asText());
        assertEquals("type:regex",  res.at("/query/wrap/type").asText());
        assertEquals("orth",        res.at("/query/wrap/layer").asText());

        query = "#REG('l\\'été.*')";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        if( debug ) System.out.printf("testREG: query: >>%s<< -> key: >>%s<<.\n",  query, res.at("/query/wrap/key").asText());
        assertEquals("l'été.*",     res.at("/query/wrap/key").asText());
        assertEquals("type:regex",  res.at("/query/wrap/type").asText());
        assertEquals("orth",        res.at("/query/wrap/layer").asText());

        query = "#REG('\\\"été\\\"$')"; // means user input is #REG('\"été\"').
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        if( debug ) System.out.printf("testREG: query: >>%s<< -> key: >>%s<<.\n",  query, res.at("/query/wrap/key").asText());
        assertEquals("\"été\"$",    res.at("/query/wrap/key").asText());
        assertEquals("type:regex",  res.at("/query/wrap/type").asText());
        assertEquals("orth",        res.at("/query/wrap/layer").asText());

        // checks the >>"<<:
        query = "#REG(\\\"Abend\\\"-Ticket)"; // means user input = #REG(\"Abend\"-Ticket).
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());

        if( debug ) System.out.printf("testREG: query: >>%s<< -> key: >>%s<<.\n",  query, res.at("/query/wrap/key").asText());
        assertEquals("\"Abend\"-Ticket",res.at("/query/wrap/key").asText());
        assertEquals("type:regex",  res.at("/query/wrap/type").asText());
        assertEquals("orth",        res.at("/query/wrap/layer").asText());

        query = "#REG('\\\"Abend\\\"-Ticket')"; // means user input = #REG(\"Abend\"-Ticket).
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());

        if( debug ) System.out.printf("testREG: query: >>%s<< -> key: >>%s<<.\n",  query, res.at("/query/wrap/key").asText());
        assertEquals("\"Abend\"-Ticket",res.at("/query/wrap/key").asText());
        assertEquals("type:regex",  res.at("/query/wrap/type").asText());
        assertEquals("orth",        res.at("/query/wrap/layer").asText());

        query = "#REG('\"Abend\"-Ticket')"; // means user input = #REG('"Abend"-Ticket').
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        if( debug ) System.out.printf("testREG: query: >>%s<< -> key: >>%s<<.\n",  query, res.at("/query/wrap/key").asText());
        assertEquals("\"Abend\"-Ticket",res.at("/query/wrap/key").asText()); // key must be escaped, because converted to in "...".
        assertEquals("type:regex",  res.at("/query/wrap/type").asText());
        assertEquals("orth",        res.at("/query/wrap/layer").asText());

        query = "#REG(\"\\\"Abend\\\"-Ticket\")"; // means user input = #REG("\"Abend\"-Ticket") -> key: >>"Abend"-Ticket<<.
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        if( debug ) System.out.printf("testREG: query: >>%s<< -> key: >>%s<<.\n",  query, res.at("/query/wrap/key").asText());
        assertEquals("\"Abend\"-Ticket",res.at("/query/wrap/key").asText());
        assertEquals("type:regex",  res.at("/query/wrap/type").asText());
        assertEquals("orth",        res.at("/query/wrap/layer").asText());
        //

        query = "#REG('^(a|b)?+*$')";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("^(a|b)?+*$",     res.at("/query/wrap/key").asText());
        assertEquals("type:regex",  res.at("/query/wrap/type").asText());
        assertEquals("orth",        res.at("/query/wrap/layer").asText());

        query = "#REG(\"[A-Z()]\")";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        assertEquals("[A-Z()]",     res.at("/query/wrap/key").asText());
        assertEquals("orth",        res.at("/query/wrap/layer").asText());
        assertEquals("type:regex",  res.at("/query/wrap/type").asText());

        query = "#REG(^klein.*) /s0 #REG(A.*ung)";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
        
        //System.out.printf("Debug: res: pretty: %s.\n",  res.toPrettyString());
        
        assertEquals("^klein.*",    res.at("/query/operands/0/operands/0/wrap/key").asText());
        assertEquals("orth",        res.at("/query/operands/0/operands/0/wrap/layer").asText());
        assertEquals("type:regex",  res.at("/query/operands/0/operands/0/wrap/type").asText());
        
        assertEquals("A.*ung",      res.at("/query/operands/1/operands/0/wrap/key").asText());
        assertEquals("orth",        res.at("/query/operands/1/operands/0/wrap/layer").asText());
        assertEquals("type:regex",  res.at("/query/operands/1/operands/0/wrap/type").asText());
        /*
        query = "#REG( ) ";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
 		
        assertTrue(res.toString().contains("Failing to parse"));
        */
        query = "#REG('' ) ";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
 
        assertTrue(res.toString().contains("Failing to parse"));

        query = "#REG(\"\") ";
        qs.setQuery(query, "cosmas2");
        res = mapper.readTree(qs.toJSON());
 
        assertTrue(res.toString().contains("Failing to parse"));

    }

    @Test
    public void testREGencode2DoubleQuoted () {
        StringBuffer sb = new StringBuffer("..\"..");
        StringUtils.encode2DoubleQuoted(sb);
        assertEquals("\"..\\\"..\"",sb.toString());

        sb = new StringBuffer("..\\..");
        StringUtils.encode2DoubleQuoted(sb);
        assertEquals("\"..\\\\..\"", sb.toString());

        sb = new StringBuffer("..\"..");
        StringUtils.encode2DoubleQuoted(sb);
        assertEquals("\"..\\\"..\"", sb.toString());
    }

    @Test
    public void testREGremoveBlanksAtBothSides () {
        StringBuffer sb = new StringBuffer("    aabc cjs   ss   ");
        StringUtils.removeBlanksAtBothSides(sb);
        assertEquals("aabc cjs   ss",sb.toString());

        sb = new StringBuffer("abc   ");
        StringUtils.removeBlanksAtBothSides(sb);
        assertEquals("abc",sb.toString());

        sb = new StringBuffer("   abc");
        StringUtils.removeBlanksAtBothSides(sb);
        assertEquals("abc",sb.toString());

        }
        
}
