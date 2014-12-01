import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;


import de.ids_mannheim.korap.query.serialize.QuerySerializer;
import de.ids_mannheim.korap.util.QueryException;

public class PoliqarpPlusTreeJSONTest {
	
	String map;
	String expected;
	String metaExpected;
	String metaMap;
	String query;
	ArrayList<JsonNode> operands;
	
	QuerySerializer qs = new QuerySerializer();
	ObjectMapper mapper = new ObjectMapper();
	JsonNode res;
	
	@Test
	public void testContext() throws QueryException, JsonProcessingException, IOException {
		query = "foo";
		String contextString = "http://ids-mannheim.de/ns/KorAP/json-ld/v0.2/context.jsonld";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals(contextString, res.get("@context").asText());
	}
	
	
	
	@Test
	public void testSingleTokens() throws QueryException, JsonProcessingException, IOException {
		query = "[base=Mann]";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:token", 		res.at("/query/@type").asText());
		assertEquals("Mann", 				res.at("/query/wrap/key").asText());
		assertEquals("lemma", 				res.at("/query/wrap/layer").asText());
		assertEquals("match:eq",			res.at("/query/wrap/match").asText());
		
		query = "[orth!=Frau]";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:token", 		res.at("/query/@type").asText());
		assertEquals("Frau", 				res.at("/query/wrap/key").asText());
		assertEquals("orth", 				res.at("/query/wrap/layer").asText());
		assertEquals("match:ne",			res.at("/query/wrap/match").asText());
		
		query = "[p!=NN]";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:token", 		res.at("/query/@type").asText());
		assertEquals("NN", 					res.at("/query/wrap/key").asText());
		assertEquals("p", 					res.at("/query/wrap/layer").asText());
		assertEquals("match:ne",			res.at("/query/wrap/match").asText());
		
		query = "[!p!=NN]";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:token", 		res.at("/query/@type").asText());
		assertEquals("NN", 					res.at("/query/wrap/key").asText());
		assertEquals("p", 					res.at("/query/wrap/layer").asText());
		assertEquals("match:eq",			res.at("/query/wrap/match").asText());
		
		query = "[base=schland/x]";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:token", 		res.at("/query/@type").asText());
		assertEquals(".*?schland.*?",		res.at("/query/wrap/key").asText());
		assertEquals("lemma", 				res.at("/query/wrap/layer").asText());
		assertEquals("type:regex",			res.at("/query/wrap/type").asText());
		assertEquals("match:eq",			res.at("/query/wrap/match").asText());
	}
	
	@Test
	public void testValue() throws QueryException, JsonProcessingException, IOException {
		query = "[mate/m=temp:pres]";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:token", 		res.at("/query/@type").asText());
		assertEquals("korap:term",			res.at("/query/wrap/@type").asText());
		assertEquals("temp",				res.at("/query/wrap/key").asText());
		assertEquals("pres",				res.at("/query/wrap/value").asText());
		assertEquals("m",	 				res.at("/query/wrap/layer").asText());
		assertEquals("mate", 				res.at("/query/wrap/foundry").asText());
		assertEquals("match:eq",			res.at("/query/wrap/match").asText());
	}
	
	@Test
	public void testRegex() throws QueryException, JsonProcessingException, IOException {
		query = "[orth=\"M(a|ä)nn(er)?\"]";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:token", 		res.at("/query/@type").asText());
		assertEquals("korap:term",			res.at("/query/wrap/@type").asText());
		assertEquals("M(a|ä)nn(er)?",		res.at("/query/wrap/key").asText());
		assertEquals("type:regex",			res.at("/query/wrap/type").asText());
		assertEquals("orth", 				res.at("/query/wrap/layer").asText());
		assertEquals("match:eq",			res.at("/query/wrap/match").asText());

		query = "[orth=\"M(a|ä)nn(er)?\"/x]";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:token", 		res.at("/query/@type").asText());
		assertEquals("korap:term",			res.at("/query/wrap/@type").asText());
		assertEquals(".*?M(a|ä)nn(er)?.*?",	res.at("/query/wrap/key").asText());
		assertEquals("type:regex",			res.at("/query/wrap/type").asText());
		assertEquals("orth", 				res.at("/query/wrap/layer").asText());
		assertEquals("match:eq",			res.at("/query/wrap/match").asText());
		
		query = "\".*?Mann.*?\"";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:token", 		res.at("/query/@type").asText());
		assertEquals("korap:term",			res.at("/query/wrap/@type").asText());
		assertEquals(".*?Mann.*?",			res.at("/query/wrap/key").asText());
		assertEquals("type:regex",			res.at("/query/wrap/type").asText());
		assertEquals("orth", 				res.at("/query/wrap/layer").asText());
		assertEquals("match:eq",			res.at("/query/wrap/match").asText());
		
		query = "z.B./x";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:token", 		res.at("/query/@type").asText());
		assertEquals("korap:term",			res.at("/query/wrap/@type").asText());
		assertEquals(".*?z\\.B\\..*?",		res.at("/query/wrap/key").asText());
		assertEquals("type:regex",			res.at("/query/wrap/type").asText());
		assertEquals("orth", 				res.at("/query/wrap/layer").asText());
		assertEquals("match:eq",			res.at("/query/wrap/match").asText());
	
	}
	
	@Test
	public void testCaseSensitivityFlag() throws QueryException, JsonProcessingException, IOException {
		query = "[orth=deutscher/i]";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:token", 		res.at("/query/@type").asText());
		assertEquals("korap:term",			res.at("/query/wrap/@type").asText());
		assertEquals("deutscher",			res.at("/query/wrap/key").asText());
		assertEquals("true",				res.at("/query/wrap/caseInsensitive").asText());
		assertEquals("orth", 				res.at("/query/wrap/layer").asText());
		assertEquals("match:eq",			res.at("/query/wrap/match").asText());

		query = "deutscher/i";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:token", 		res.at("/query/@type").asText());
		assertEquals("korap:term",			res.at("/query/wrap/@type").asText());
		assertEquals("deutscher",			res.at("/query/wrap/key").asText());
		assertEquals("true",				res.at("/query/wrap/caseInsensitive").asText());
		assertEquals("orth", 				res.at("/query/wrap/layer").asText());
		assertEquals("match:eq",			res.at("/query/wrap/match").asText());
		
		query = "deutscher/I";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:token", 		res.at("/query/@type").asText());
		assertEquals("korap:term",			res.at("/query/wrap/@type").asText());
		assertEquals("deutscher",			res.at("/query/wrap/key").asText());
		assertEquals("false",				res.at("/query/wrap/caseInsensitive").asText());
		assertEquals("orth", 				res.at("/query/wrap/layer").asText());
		assertEquals("match:eq",			res.at("/query/wrap/match").asText());
		
		query = "[orth=deutscher/i][orth=Bundestag]";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group", 		res.at("/query/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operation").asText());
		operands = Lists.newArrayList(res.at("/query/operands").elements());
		assertEquals("korap:token",			operands.get(0).at("/@type").asText());
		assertEquals("deutscher",			operands.get(0).at("/wrap/key").asText());
		assertEquals("orth",				operands.get(0).at("/wrap/layer").asText());
		assertEquals("match:eq",			operands.get(0).at("/wrap/match").asText());
		assertEquals(true,					operands.get(0).at("/wrap/caseInsensitive").asBoolean());
		assertEquals("korap:token",			operands.get(1).at("/@type").asText());
		assertEquals("Bundestag",			operands.get(1).at("/wrap/key").asText());
		assertEquals("orth",				operands.get(1).at("/wrap/layer").asText());
		assertEquals("match:eq",			operands.get(1).at("/wrap/match").asText());
		assertEquals(true,					operands.get(1).at("/wrap/caseInsensitive").isMissingNode());
		
		query = "deutscher/i Bundestag";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group", 		res.at("/query/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operation").asText());
		operands = Lists.newArrayList(res.at("/query/operands").elements());
		assertEquals("korap:token",			operands.get(0).at("/@type").asText());
		assertEquals("deutscher",			operands.get(0).at("/wrap/key").asText());
		assertEquals("orth",				operands.get(0).at("/wrap/layer").asText());
		assertEquals("match:eq",			operands.get(0).at("/wrap/match").asText());
		assertEquals(true,					operands.get(0).at("/wrap/caseInsensitive").asBoolean());
		assertEquals("korap:token",			operands.get(1).at("/@type").asText());
		assertEquals("Bundestag",			operands.get(1).at("/wrap/key").asText());
		assertEquals("orth",				operands.get(1).at("/wrap/layer").asText());
		assertEquals("match:eq",			operands.get(1).at("/wrap/match").asText());
		assertEquals(true,					operands.get(1).at("/wrap/caseInsensitive").isMissingNode());
	}
	
	@Test
	public void testSpans() throws QueryException, JsonProcessingException, IOException {
		query = "<s>";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:span", 			res.at("/query/@type").asText());
		assertEquals("s",					res.at("/query/key").asText());
		
		query = "<vp>";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:span", 			res.at("/query/@type").asText());
		assertEquals("vp",					res.at("/query/key").asText());
		
		query = "<cnx/c=vp>";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:span", 			res.at("/query/@type").asText());
		assertEquals("vp",					res.at("/query/key").asText());
		assertEquals("cnx",					res.at("/query/foundry").asText());
		assertEquals("c",					res.at("/query/layer").asText());
		
		query = "<cnx/c!=vp>";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:span", 			res.at("/query/@type").asText());
		assertEquals("vp",					res.at("/query/key").asText());
		assertEquals("cnx",					res.at("/query/foundry").asText());
		assertEquals("c",					res.at("/query/layer").asText());
		assertEquals("match:ne",			res.at("/query/match").asText());
		
		query = "<cnx/c!=vp class!=header>";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:span", 			res.at("/query/@type").asText());
		assertEquals("vp",					res.at("/query/key").asText());
		assertEquals("cnx",					res.at("/query/foundry").asText());
		assertEquals("c",					res.at("/query/layer").asText());
		assertEquals("match:ne",			res.at("/query/match").asText());
		assertEquals("class",				res.at("/query/attr/key").asText());
		assertEquals("header",				res.at("/query/attr/value").asText());
		assertEquals("match:ne",			res.at("/query/attr/match").asText());
		
		query = "<cnx/c!=vp !(class!=header)>";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:span", 			res.at("/query/@type").asText());
		assertEquals("vp",					res.at("/query/key").asText());
		assertEquals("cnx",					res.at("/query/foundry").asText());
		assertEquals("c",					res.at("/query/layer").asText());
		assertEquals("match:ne",			res.at("/query/match").asText());
		assertEquals("class",				res.at("/query/attr/key").asText());
		assertEquals("header",				res.at("/query/attr/value").asText());
		assertEquals("match:eq",			res.at("/query/attr/match").asText());
		
		query = "<cnx/c!=vp !(class=header & id=7)>";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:span", 			res.at("/query/@type").asText());
		assertEquals("vp",					res.at("/query/key").asText());
		assertEquals("cnx",					res.at("/query/foundry").asText());
		assertEquals("c",					res.at("/query/layer").asText());
		assertEquals("match:ne",			res.at("/query/match").asText());
		assertEquals("korap:termGroup",		res.at("/query/attr/@type").asText());
		assertEquals("relation:and",		res.at("/query/attr/relation").asText());
		operands = Lists.newArrayList( res.at("/query/attr/operands").elements());
		assertEquals("korap:term",			operands.get(0).at("/@type").asText());
		assertEquals("class",				operands.get(0).at("/key").asText());
		assertEquals("header",				operands.get(0).at("/value").asText());
		assertEquals("match:ne",			operands.get(0).at("/match").asText());
		assertEquals("korap:term",			operands.get(1).at("/@type").asText());
		assertEquals("id",					operands.get(1).at("/key").asText());
		assertEquals(7,						operands.get(1).at("/value").asInt());
		assertEquals("match:ne",			operands.get(1).at("/match").asText());
	}
	
	@Test
	public void testDistances() throws QueryException, JsonProcessingException, IOException {
		query = "[base=der][][base=Mann]";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group", 		res.at("/query/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operation").asText());
		assertEquals("true",				res.at("/query/inOrder").asText());
		assertEquals("korap:distance",		res.at("/query/distances").elements().next().at("/@type").asText());
		assertEquals("w",					res.at("/query/distances").elements().next().at("/key").asText());
		assertEquals("korap:boundary",		res.at("/query/distances").elements().next().at("/boundary/@type").asText());
		assertEquals(2,						res.at("/query/distances").elements().next().at("/boundary/min").asInt());
		assertEquals(2,						res.at("/query/distances").elements().next().at("/boundary/max").asInt());
		operands = Lists.newArrayList(res.at("/query/operands").elements());
		assertEquals("korap:token",			operands.get(0).at("/@type").asText());
		assertEquals("der",					operands.get(0).at("/wrap/key").asText());
		assertEquals("lemma",				operands.get(0).at("/wrap/layer").asText());
		assertEquals("match:eq",			operands.get(0).at("/wrap/match").asText());
		assertEquals("korap:token",			operands.get(1).at("/@type").asText());
		assertEquals("Mann",				operands.get(1).at("/wrap/key").asText());
		assertEquals("lemma",				operands.get(1).at("/wrap/layer").asText());
		assertEquals("match:eq",			operands.get(1).at("/wrap/match").asText());
		
		query = "[base=der][][][base=Mann]";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group", 		res.at("/query/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operation").asText());
		assertEquals("true",				res.at("/query/inOrder").asText());
		assertEquals("korap:distance",		res.at("/query/distances").elements().next().at("/@type").asText());
		assertEquals("w",					res.at("/query/distances").elements().next().at("/key").asText());
		assertEquals("korap:boundary",		res.at("/query/distances").elements().next().at("/boundary/@type").asText());
		assertEquals(3,						res.at("/query/distances").elements().next().at("/boundary/min").asInt());
		assertEquals(3,						res.at("/query/distances").elements().next().at("/boundary/max").asInt());
		operands = Lists.newArrayList(res.at("/query/operands").elements());
		assertEquals("korap:token",			operands.get(0).at("/@type").asText());
		assertEquals("der",					operands.get(0).at("/wrap/key").asText());
		assertEquals("lemma",				operands.get(0).at("/wrap/layer").asText());
		assertEquals("match:eq",			operands.get(0).at("/wrap/match").asText());
		assertEquals("korap:token",			operands.get(1).at("/@type").asText());
		assertEquals("Mann",				operands.get(1).at("/wrap/key").asText());
		assertEquals("lemma",				operands.get(1).at("/wrap/layer").asText());
		assertEquals("match:eq",			operands.get(1).at("/wrap/match").asText());
		
		query = "[base=der][][]?[base=Mann]";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group", 		res.at("/query/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operation").asText());
		assertEquals("true",				res.at("/query/inOrder").asText());
		assertEquals("korap:distance",		res.at("/query/distances").elements().next().at("/@type").asText());
		assertEquals("w",					res.at("/query/distances").elements().next().at("/key").asText());
		assertEquals("korap:boundary",		res.at("/query/distances").elements().next().at("/boundary/@type").asText());
		assertEquals(2,						res.at("/query/distances").elements().next().at("/boundary/min").asInt());
		assertEquals(3,						res.at("/query/distances").elements().next().at("/boundary/max").asInt());
		operands = Lists.newArrayList(res.at("/query/operands").elements());
		assertEquals("korap:token",			operands.get(0).at("/@type").asText());
		assertEquals("der",					operands.get(0).at("/wrap/key").asText());
		assertEquals("lemma",				operands.get(0).at("/wrap/layer").asText());
		assertEquals("match:eq",			operands.get(0).at("/wrap/match").asText());
		assertEquals("korap:token",			operands.get(1).at("/@type").asText());
		assertEquals("Mann",				operands.get(1).at("/wrap/key").asText());
		assertEquals("lemma",				operands.get(1).at("/wrap/layer").asText());
		assertEquals("match:eq",			operands.get(1).at("/wrap/match").asText());
		
		query = "[base=der][]+[base=Mann]";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group", 		res.at("/query/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operation").asText());
		assertEquals("true",				res.at("/query/inOrder").asText());
		assertEquals("korap:distance",		res.at("/query/distances").elements().next().at("/@type").asText());
		assertEquals("w",					res.at("/query/distances").elements().next().at("/key").asText());
		assertEquals("korap:boundary",		res.at("/query/distances").elements().next().at("/boundary/@type").asText());
		assertEquals(2,						res.at("/query/distances").elements().next().at("/boundary/min").asInt());
		assertEquals(true,					res.at("/query/distances").elements().next().at("/boundary/max").isMissingNode());
		operands = Lists.newArrayList(res.at("/query/operands").elements());
		assertEquals("korap:token",			operands.get(0).at("/@type").asText());
		assertEquals("der",					operands.get(0).at("/wrap/key").asText());
		assertEquals("lemma",				operands.get(0).at("/wrap/layer").asText());
		assertEquals("match:eq",			operands.get(0).at("/wrap/match").asText());
		assertEquals("korap:token",			operands.get(1).at("/@type").asText());
		assertEquals("Mann",				operands.get(1).at("/wrap/key").asText());
		assertEquals("lemma",				operands.get(1).at("/wrap/layer").asText());
		assertEquals("match:eq",			operands.get(1).at("/wrap/match").asText());
		
		query = "[base=der][]*[base=Mann]";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group", 		res.at("/query/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operation").asText());
		assertEquals("true",				res.at("/query/inOrder").asText());
		assertEquals("korap:distance",		res.at("/query/distances").elements().next().at("/@type").asText());
		assertEquals("w",					res.at("/query/distances").elements().next().at("/key").asText());
		assertEquals("korap:boundary",		res.at("/query/distances").elements().next().at("/boundary/@type").asText());
		assertEquals(1,						res.at("/query/distances").elements().next().at("/boundary/min").asInt());
		assertEquals(true,					res.at("/query/distances").elements().next().at("/boundary/max").isMissingNode());
		
		query = "[base=der][]{2,5}[base=Mann][]?[][base=Frau]";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group", 		res.at("/query/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operation").asText());
		assertEquals("true",				res.at("/query/inOrder").asText());
		assertEquals("korap:distance",		res.at("/query/distances").elements().next().at("/@type").asText());
		assertEquals("w",					res.at("/query/distances").elements().next().at("/key").asText());
		assertEquals("korap:boundary",		res.at("/query/distances").elements().next().at("/boundary/@type").asText());
		assertEquals(3,						res.at("/query/distances").elements().next().at("/boundary/min").asInt());
		assertEquals(6,						res.at("/query/distances").elements().next().at("/boundary/max").asInt());
		operands = Lists.newArrayList(res.at("/query/operands").elements());
		assertEquals("korap:token",			operands.get(0).at("/@type").asText());
		assertEquals("der",					operands.get(0).at("/wrap/key").asText());
		assertEquals("lemma",				operands.get(0).at("/wrap/layer").asText());
		assertEquals("match:eq",			operands.get(0).at("/wrap/match").asText());
		assertEquals("korap:group",			operands.get(1).at("/@type").asText());
		assertEquals("operation:sequence",	operands.get(1).at("/operation").asText());
		assertEquals("korap:distance",		operands.get(1).get("distances").elements().next().at("/@type").asText());
		assertEquals("w",					operands.get(1).get("distances").elements().next().at("/key").asText());
		assertEquals("korap:boundary",		operands.get(1).get("distances").elements().next().at("/boundary/@type").asText());
		assertEquals(2,						operands.get(1).get("distances").elements().next().at("/boundary/min").asInt());
		assertEquals(3,						operands.get(1).get("distances").elements().next().at("/boundary/max").asInt());
		operands = Lists.newArrayList(operands.get(1).get("operands").elements());
		assertEquals("Mann",				operands.get(0).at("/wrap/key").asText());
		assertEquals("lemma",				operands.get(0).at("/wrap/layer").asText());
		assertEquals("match:eq",			operands.get(0).at("/wrap/match").asText());
		assertEquals("Frau",				operands.get(1).at("/wrap/key").asText());
		assertEquals("lemma",				operands.get(1).at("/wrap/layer").asText());
		assertEquals("match:eq",			operands.get(1).at("/wrap/match").asText());
		
		query = "[base=geht][base=der][]*contains(<s>,<np>)";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group", 		res.at("/query/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operation").asText());
		assertEquals(true,					res.at("/query/inOrder").isMissingNode());
		assertEquals(true,					res.at("/query/distances").isMissingNode());
		operands = Lists.newArrayList(res.at("/query/operands").elements());
		assertEquals("korap:token",			operands.get(0).at("/@type").asText());
		assertEquals("geht",					operands.get(0).at("/wrap/key").asText());
		assertEquals("lemma",				operands.get(0).at("/wrap/layer").asText());
		assertEquals("match:eq",			operands.get(0).at("/wrap/match").asText());
		assertEquals("korap:group",			operands.get(1).at("/@type").asText());
		assertEquals("operation:sequence",	operands.get(1).at("/operation").asText());
		assertEquals("korap:distance",		operands.get(1).get("distances").elements().next().at("/@type").asText());
		assertEquals("w",					operands.get(1).get("distances").elements().next().at("/key").asText());
		assertEquals("korap:boundary",		operands.get(1).get("distances").elements().next().at("/boundary/@type").asText());
		assertEquals(1,						operands.get(1).get("distances").elements().next().at("/boundary/min").asInt());
		assertEquals(true,					operands.get(1).get("distances").elements().next().at("/boundary/max").isMissingNode());
		operands = Lists.newArrayList(operands.get(1).get("operands").elements());
		assertEquals("der",				operands.get(0).at("/wrap/key").asText());
		assertEquals("lemma",				operands.get(0).at("/wrap/layer").asText());
		assertEquals("match:eq",			operands.get(0).at("/wrap/match").asText());
		assertEquals("korap:group",			operands.get(1).at("/@type").asText());
		assertEquals("operation:position",	operands.get(1).at("/operation").asText());
	}

	@Test
	public void testDistancesWithClass() throws QueryException, JsonProcessingException, IOException {
		query = "[base=der]{[]}[base=Mann]";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group", 		res.at("/query/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operation").asText());
		assertEquals(true,					res.at("/query/inOrder").isMissingNode());
		assertEquals(true,					res.at("/query/distances").isMissingNode());
		operands = Lists.newArrayList(res.at("/query/operands").elements());
		assertEquals("der",					operands.get(0).at("/wrap/key").asText());
		assertEquals("Mann",				operands.get(2).at("/wrap/key").asText());
		assertEquals("korap:group",			operands.get(1).at("/@type").asText());
		assertEquals("operation:class",		operands.get(1).at("/operation").asText());
		assertEquals(1,						operands.get(1).at("/classOut").asInt());
		operands = Lists.newArrayList(operands.get(1).at("/operands").elements());
		assertEquals("korap:token",			operands.get(0).at("/@type").asText());
		assertEquals(true,					operands.get(0).at("/wrap").isMissingNode());
		
		query = "[base=der]{2:[]}[base=Mann]";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		operands = Lists.newArrayList(res.at("/query/operands").elements());
		assertEquals("operation:class",		operands.get(1).at("/operation").asText());
		assertEquals(2,						operands.get(1).at("/classOut").asInt());
		
		query = "{1:[]}[base=der][base=Mann]";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		operands = Lists.newArrayList(res.at("/query/operands").elements());
		assertEquals("operation:class",		operands.get(0).at("/operation").asText());
		assertEquals(1,						operands.get(0).at("/classOut").asInt());
		
		query = "{1:{2:der} {3:[]} Mann}";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		operands = Lists.newArrayList(res.at("/query/operands").elements());
		assertEquals(1,	operands.size());  // class operation may only have one operand (the sequence)
		operands = Lists.newArrayList(operands.get(0).at("/operands").elements());
		assertEquals(3,	operands.size());  // the sequence has three operands ("der", "[]" and "Mann")
		
	}
	
	@Test
	public void testLeadingTrailingEmptyTokens() throws QueryException, JsonProcessingException, IOException {
		query = "[][base=Mann]";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		operands = Lists.newArrayList(res.at("/query/operands").elements());
		assertEquals("korap:token",			operands.get(0).at("/@type").asText());
		assertEquals(true,					operands.get(0).at("/key").isMissingNode());
		
		query = "[][][base=Mann]";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		operands = Lists.newArrayList(res.at("/query/operands").elements());
		assertEquals("korap:group",			operands.get(0).at("/@type").asText());
		assertEquals("operation:repetition",operands.get(0).at("/operation").asText());
		assertEquals(2,						operands.get(0).at("/boundary/min").asInt());
		assertEquals(2,						operands.get(0).at("/boundary/max").asInt());
		operands = Lists.newArrayList(operands.get(0).at("/operands").elements());
		assertEquals("korap:token",			operands.get(0).at("/@type").asText());
		assertEquals(true,					operands.get(0).at("/key").isMissingNode());
		
		query = "startswith(<s>, [][base=Mann])";
		qs.setQuery(query, "poliqarpplus");
		res = mapper.readTree(qs.toJSON());
		operands = Lists.newArrayList(res.at("/query/operands"));
		operands = Lists.newArrayList(operands.get(1).at("/operands"));
		assertEquals("korap:token",			operands.get(0).at("/@type").asText());
		assertEquals(true,					operands.get(0).at("/key").isMissingNode());
	}
	

    @Test
    public void testGroupRepetition() throws QueryException, JsonProcessingException, IOException {
      query = "contains(<s>, (der){3})";
      qs.setQuery(query, "poliqarpplus");
      res = mapper.readTree(qs.toJSON());
      assertEquals("korap:group", res.at("/query/@type").asText());
      assertEquals("operation:position", res.at("/query/operation").asText());
      assertEquals("korap:span", res.at("/query/operands/0/@type").asText());
      assertEquals("s", res.at("/query/operands/0/key").asText());
      assertEquals("korap:group", res.at("/query/operands/1/@type").asText());
      assertEquals("operation:repetition", res.at("/query/operands/1/operation").asText());
    };


    @Test
    public void testPositions() throws QueryException, JsonProcessingException, IOException {
      query = "contains(<s>, der)";
      qs.setQuery(query, "poliqarpplus");
      res = mapper.readTree(qs.toJSON());
      assertEquals("korap:group", res.at("/query/@type").asText());
      assertEquals("operation:position", res.at("/query/operation").asText());
      assertEquals("frame:contains", res.at("/query/frame").asText());
      assertEquals("korap:span", res.at("/query/operands/0/@type").asText());
      assertEquals("s", res.at("/query/operands/0/key").asText());
      assertEquals("korap:token", res.at("/query/operands/1/@type").asText());
      
      query = "overlaps(<s>, der)";
      qs.setQuery(query, "poliqarpplus");
      res = mapper.readTree(qs.toJSON());
      assertEquals("korap:group", res.at("/query/@type").asText());
      assertEquals("operation:position", res.at("/query/operation").asText());
      assertEquals("frame:overlaps", res.at("/query/frame").asText());
      assertEquals("korap:span", res.at("/query/operands/0/@type").asText());
      assertEquals("s", res.at("/query/operands/0/key").asText());
      assertEquals("korap:token", res.at("/query/operands/1/@type").asText());
    };

	
//	}
//	
//	@Test
//	public void testCoordinatedFields() throws QueryException {
//		// [base=Mann&(cas=N|cas=A)]
//		String cof1 = 
//			"{@type=korap:token, wrap=" +
//				"{@type=korap:termGroup, relation=relation:and, operands=[" +
//					"{@type=korap:term, layer=lemma, key=Mann, match=match:eq}," +
//					"{@type=korap:termGroup, relation=relation:or, operands=[" +
//						"{@type=korap:term, layer=cas, key=N, match=match:eq}," +
//						"{@type=korap:term, layer=cas, key=A, match=match:eq}" +
//					"]}" +
//				"]}" +
//			"}";
//		ppt = new PoliqarpPlusTree("[base=Mann&(cas=N|cas=A)]");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(cof1.replaceAll(" ", ""), map.replaceAll(" ", ""));
//
//
//		assertEquals(
//		    new PoliqarpPlusTree(" [ base=Mann & ( cas=N | cas=A)] ").getRequestMap().get("query").toString(),
//		    new PoliqarpPlusTree("[base=Mann &(cas=N|cas=A)]").getRequestMap().get("query").toString()
//	        );
//		
//		// [base=Mann&cas=N&gen=m]
//		String cof2 = 
//			"{@type=korap:token, wrap=" +
//				"{@type=korap:termGroup, relation=relation:and, operands=[" +
//					"{@type=korap:term, layer=lemma, key=Mann, match=match:eq}," +
//					"{@type=korap:termGroup, relation=relation:and, operands=[" +
//						"{@type=korap:term, layer=cas, key=N, match=match:eq}," +
//						"{@type=korap:term, layer=gen, key=m, match=match:eq}" +
//					"]}" +
//				"]}" +
//			"}";
//		ppt = new PoliqarpPlusTree("[base=Mann&cas=N&gen=m]");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(cof2.replaceAll(" ", ""), map.replaceAll(" ", ""));
//	}
//	
//	@Test
//	public void testOccurrence() throws QueryException {
//		// [base=foo]*
//		String occ1 = "{@type=korap:group, operation=operation:repetition, operands=[" +
//					     "{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=foo, match=match:eq}}" +
//					  "], boundary={@type=korap:boundary, min=0}, min=0}"; 
//		ppt = new PoliqarpPlusTree("[base=foo]*");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(occ1.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// [base=foo]*[base=bar]
//		String occ2 = 
//				"{@type=korap:group, operation=operation:sequence, operands=[" +
//					"{@type=korap:group, operation=operation:repetition, operands=[" +
//					     "{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=foo, match=match:eq}}" +
//					"], boundary={@type=korap:boundary, min=0}, min=0 }," +
//					"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=bar, match=match:eq}}" +
//				"]}"; 
//		ppt = new PoliqarpPlusTree("[base=foo]*[base=bar]");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(occ2.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// [base=bar][base=foo]*
//		String occ3 = 
//				"{@type=korap:group, operation=operation:sequence, operands=[" +
//					"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=bar, match=match:eq}}," +
//					"{@type=korap:group, operation=operation:repetition, operands=[" +
//					     "{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=foo, match=match:eq}}" +
//					"], boundary={@type=korap:boundary, min=0}, min=0 }" +
//				"]}"; 
//		ppt = new PoliqarpPlusTree("[base=bar][base=foo]*");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(occ3.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// ([base=bar][base=foo])*
//		String occ4 = 
//				"{@type=korap:group, operation=operation:repetition, operands=[" +	
//					"{@type=korap:group, operation=operation:sequence, operands=[" +
//						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=bar, match=match:eq}}," +
//						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=foo, match=match:eq}}" +
//					"]}" +
//				"], boundary={@type=korap:boundary, min=0}, min=0}" ;
//		ppt = new PoliqarpPlusTree("([base=bar][base=foo])*");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(occ4.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// <s>([base=bar][base=foo])*
//		String occ5 = 
//				"{@type=korap:group, operation=operation:sequence, operands=[" +
//					"{@type=korap:span, key=s}," +
//					"{@type=korap:group, operation=operation:repetition, operands=[" +	
//						"{@type=korap:group, operation=operation:sequence, operands=[" +
//							"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=bar, match=match:eq}}," +
//							"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=foo, match=match:eq}}" +
//						"]}" +
//					"], boundary={@type=korap:boundary, min=0}, min=0 }" +
//				"]}" ;
//		ppt = new PoliqarpPlusTree("<s>([base=bar][base=foo])*");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(occ5.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// <s><np>([base=bar][base=foo])*
//		String occ6 = 
//				"{@type=korap:group, operation=operation:sequence, operands=[" +
//					"{@type=korap:span, key=s}," +
//					"{@type=korap:span, key=np}," +
//					"{@type=korap:group, operation=operation:repetition, operands=[" +	
//						"{@type=korap:group, operation=operation:sequence, operands=[" +
//							"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=bar, match=match:eq}}," +
//							"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=foo, match=match:eq}}" +
//						"]}" +
//					"], boundary={@type=korap:boundary, min=0}, min=0 }" +
//				"]}" ;
//		ppt = new PoliqarpPlusTree("<s><np>([base=bar][base=foo])*");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(occ6.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// <s><np>([base=bar][base=foo])*[p=NN]
//		// comment: embedded sequence shouldn't really be here, but does not really hurt, either. (?)
//		// really hard to get this behaviour out of the PQPlus grammar...
//		String occ7 = 
//				"{@type=korap:group, operation=operation:sequence, operands=[" +
//					"{@type=korap:span, key=s}," +
//					"{@type=korap:span, key=np}," +
//					"{@type=korap:group, operation=operation:repetition, operands=[" +	
//						"{@type=korap:group, operation=operation:sequence, operands=[" +
//							"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=bar, match=match:eq}}," +
//							"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=foo, match=match:eq}}" +
//						"]}" +
//					"], boundary={@type=korap:boundary, min=0}, min=0 }," +
//					"{@type=korap:token, wrap={@type=korap:term, layer=p, key=NN, match=match:eq}}" +
//				"]}" ;
//		ppt = new PoliqarpPlusTree("<s><np>([base=bar][base=foo])*[p=NN]");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(occ7.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// ([base=bar][base=foo])*[p=NN]
//		String occ8 = 
//				"{@type=korap:group, operation=operation:sequence, operands=[" +
//					"{@type=korap:group, operation=operation:repetition, operands=[" +	
//						"{@type=korap:group, operation=operation:sequence, operands=[" +
//							"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=bar, match=match:eq}}," +
//							"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=foo, match=match:eq}}" +
//						"]}" +
//					"], boundary={@type=korap:boundary, min=0}, min=0 }," +
//					"{@type=korap:token, wrap={@type=korap:term, layer=p, key=NN, match=match:eq}}" +
//				"]}" ;
//		ppt = new PoliqarpPlusTree("([base=bar][base=foo])*[p=NN]");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(occ8.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// [base=foo]+
//		String occ9 = "{@type=korap:group, operation=operation:repetition, operands=[" +
//					     "{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=foo, match=match:eq}}" +
//					  "], boundary={@type=korap:boundary, min=1}, min=1}"; 
//		ppt = new PoliqarpPlusTree("[base=foo]+");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(occ9.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// [base=foo]?
//		String occ10 = "{@type=korap:group, operation=operation:repetition, operands=[" +
//					     "{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=foo, match=match:eq}}" +
//					  "], boundary={@type=korap:boundary, min=0, max=1}, min=0, max=1}"; 
//		ppt = new PoliqarpPlusTree("[base=foo]?");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(occ10.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// [base=foo]{2,5}
//		String occ11 = "{@type=korap:group, operation=operation:repetition, operands=[" +
//					     "{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=foo, match=match:eq}}" +
//					  "], boundary={@type=korap:boundary, min=2, max=5}, min=2, max=5}"; 
//		ppt = new PoliqarpPlusTree("[base=foo]{2,5}");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(occ11.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// [base=foo]{2}
//		String occ12 = "{@type=korap:group, operation=operation:repetition, operands=[" +
//					     "{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=foo, match=match:eq}}" +
//					  "], boundary={@type=korap:boundary, min=2, max=2}, min=2, max=2}"; 
//		ppt = new PoliqarpPlusTree("[base=foo]{2}");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(occ12.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// [base=foo]{2}
//		String occ13 = "{@type=korap:group, operation=operation:repetition, operands=[" +
//					     "{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=foo, match=match:eq}}" +
//					  "], boundary={@type=korap:boundary, min=2}, min=2}"; 
//		ppt = new PoliqarpPlusTree("[base=foo]{2,}");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(occ13.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// [base=foo]{2}
//		String occ14 = "{@type=korap:group, operation=operation:repetition, operands=[" +
//					     "{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=foo, match=match:eq}}" +
//					  "], boundary={@type=korap:boundary, min=0, max=2}, min=0, max=2}"; 
//		ppt = new PoliqarpPlusTree("[base=foo]{,2}");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(occ14.replaceAll(" ", ""), map.replaceAll(" ", ""));
//	}
//	
//	@Test
//	public void testTokenSequence() throws QueryException {
//		// [base=Mann][orth=Frau]
//		String seq1 = "{@type=korap:group, operation=operation:sequence, operands=[" +
//				"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}, " +
//				"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Frau, match=match:eq}}" +
//				"]}";
//		assertTrue(equalsQueryContent(seq1, "[base=Mann][orth=Frau]"));
//		
//		// [base=Mann][orth=Frau][p=NN]
//		String seq2 = "{@type=korap:group, operation=operation:sequence, operands=[" +
//				"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}, " +
//				"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Frau, match=match:eq}}, " +
//				"{@type=korap:token, wrap={@type=korap:term, layer=p, key=NN, match=match:eq}}" +
//				"]}";
//		assertTrue(equalsQueryContent(seq2, "[base=Mann][orth=Frau][p=NN]"));
//	}
//	
//	@Test
//	public void testDisjSegments() throws QueryException {
//		// ([base=der]|[base=das])[base=Schild]
//		String disj1 = 
//				"{@type=korap:group, operation=operation:sequence, operands=[" +
//					"{@type=korap:group, operation=operation:or, operands=[" +
//						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=der, match=match:eq}}," +
//						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=das, match=match:eq}}" +
//					"]}," +
//					"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Schild, match=match:eq}}" +
//				"]}";
//		ppt = new PoliqarpPlusTree("([base=der]|[base=das])[base=Schild]");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(disj1.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// [base=Schild]([base=der]|[base=das])
//		String disj2 = 
//				"{@type=korap:group, operation=operation:sequence, operands=[" +
//					"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Schild, match=match:eq}}," +
//					"{@type=korap:group, operation=operation:or, operands=[" +
//						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=der, match=match:eq}}," +
//						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=das, match=match:eq}}" +
//					"]}" +
//				"]}";
//		ppt = new PoliqarpPlusTree("[base=Schild]([base=der]|[base=das])");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(disj2.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// "([orth=der][base=katze])|([orth=eine][base=baum])"
//		String disj3 = 
//				"{@type=korap:group, operation=operation:or, operands=[" +
//					"{@type=korap:group, operation=operation:sequence, operands=[" +
//						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=der, match=match:eq}}," +
//						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=katze, match=match:eq}}" +
//					"]}," +
//					"{@type=korap:group, operation=operation:sequence, operands=[" +
//						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=eine, match=match:eq}}," +
//						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=baum, match=match:eq}}" +
//					"]}" +
//				"]}";
//		ppt = new PoliqarpPlusTree("([orth=der][base=katze])|([orth=eine][base=baum])");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(disj3.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// "[orth=der][base=katze]|[orth=eine][base=baum]"
//		String disj4 = 
//				"{@type=korap:group, operation=operation:or, operands=[" +
//					"{@type=korap:group, operation=operation:sequence, operands=[" +
//						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=der, match=match:eq}}," +
//						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=katze, match=match:eq}}" +
//					"]}," +
//					"{@type=korap:group, operation=operation:sequence, operands=[" +
//						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=eine, match=match:eq}}," +
//						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=baum, match=match:eq}}" +
//					"]}" +
//				"]}";
//		ppt = new PoliqarpPlusTree("[orth=der][base=katze]|[orth=eine][base=baum]");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(disj4.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		PoliqarpPlusTree ppt1 = new PoliqarpPlusTree("[orth=der][base=katze]|[orth=eine][base=baum]");
//		PoliqarpPlusTree ppt2 = new PoliqarpPlusTree("([orth=der][base=katze])|([orth=eine][base=baum])");
//		assertEquals(ppt1.getRequestMap().toString(), ppt2.getRequestMap().toString());
//		
//		// "[orth=der][base=katze]|[orth=der][base=hund]|[orth=der][base=baum]"
//		String disj5 = 
//				"{@type=korap:group, operation=operation:or, operands=[" +
//					"{@type=korap:group, operation=operation:sequence, operands=[" +
//						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=der, match=match:eq}}," +
//						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=katze, match=match:eq}}" +
//					"]}," +
//					"{@type=korap:group, operation=operation:sequence, operands=[" +
//						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=der, match=match:eq}}," +
//						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=hund, match=match:eq}}" +
//					"]}," +
//					"{@type=korap:group, operation=operation:sequence, operands=[" +
//						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=der, match=match:eq}}," +
//						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=baum, match=match:eq}}" +
//					"]}" +
//				"]}";
//		ppt = new PoliqarpPlusTree("[orth=der][base=katze]|[orth=der][base=hund]|[orth=der][base=baum]");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(disj5.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// [orth=der]([base=katze]|[base=hund]|[base=baum])
//		String disj6 = 
//				"{@type=korap:group, operation=operation:sequence, operands=[" +
//					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=der, match=match:eq}}," +
//					"{@type=korap:group, operation=operation:or, operands=[" +
//						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=katze, match=match:eq}}," +
//						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=hund, match=match:eq}}," +
//						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=baum, match=match:eq}}" +
//					"]}" +
//				"]}";
//		ppt = new PoliqarpPlusTree("[orth=der]([base=katze]|[base=hund]|[base=baum])");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(disj6.replaceAll(" ", ""), map.replaceAll(" ", ""));
//	}
//	
//	@Test
//	public void testTokenElemSequence() throws QueryException {
//		// [base=Mann]<vp>
//		String seq1 = "{@type=korap:group, operation=operation:sequence, operands=[" +
//				"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}, " +
//				"{@type=korap:span, key=vp}" +
//				"]}";
//		assertTrue(equalsQueryContent(seq1, "[base=Mann]<vp>"));
//		
//		// <vp>[base=Mann]
//		String seq2 = "{@type=korap:group, operation=operation:sequence, operands=[" +
//				"{@type=korap:span, key=vp}, "+
//				"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}} " +
//				"]}";
//		assertTrue(equalsQueryContent(seq2, "<vp>[base=Mann]"));
//		
//		// <vp>[base=Mann]<pp>
//		String seq3 = "{@type=korap:group, operation=operation:sequence, operands=[" +
//				"{@type=korap:span, key=vp}, "+
//				"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}, " +
//				"{@type=korap:span, key=pp} "+
//				"]}";
//		assertTrue(equalsQueryContent(seq3, "<vp>[base=Mann]<pp>"));
//	}
//	
//	@Test
//	public void testElemSequence() throws QueryException {
//		// <np><vp>
//		String seq1 = "{@type=korap:group, operation=operation:sequence, operands=[" +
//				"{@type=korap:span, key=np}," +
//				"{@type=korap:span, key=vp}" +
//				"]}";
//		assertTrue(equalsQueryContent(seq1, "<np><vp>"));
//		
//		// <np><vp><pp>
//		String seq2 = "{@type=korap:group, operation=operation:sequence, operands=[" +
//				"{@type=korap:span, key=np}," +
//				"{@type=korap:span, key=vp}," +
//				"{@type=korap:span, key=pp}" +
//				"]}";
//		assertTrue(equalsQueryContent(seq2, "<np><vp><pp>"));
//	}
//	
//	@Test 
//	public void testClasses() throws QueryException {
//		String query;
//		// {[base=Mann]}
//		String cls1 = "{@type=korap:group, operation=operation:class, class=0, classOut=0, operands=[" +
//				"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}" +
//				"]}";
//		ppt = new PoliqarpPlusTree("{[base=Mann]}");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(cls1.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// {[base=Mann][orth=Frau]}
//		query = "{[base=Mann][orth=Frau]}";
//		String cls2 = "{@type=korap:group, operation=operation:class, class=0, classOut=0, operands=[" +
//				 "{@type=korap:group, operation=operation:sequence, operands=[" +
//				  "{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}," +
//				  "{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Frau, match=match:eq}}" +
//				 "]}" +
//				"]}";
//		ppt = new PoliqarpPlusTree(query);
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(cls2.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// [p=NN]{[base=Mann][orth=Frau]}
//		String cls3 = "{@type=korap:group, operation=operation:sequence, operands=[" +
//						"{@type=korap:token, wrap={@type=korap:term, layer=p, key=NN, match=match:eq}}," +
//						"{@type=korap:group, operation=operation:class, class=0, classOut=0, operands=[" +
//							"{@type=korap:group, operation=operation:sequence, operands=[" +
//								"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}," +
//								"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Frau, match=match:eq}}" +
//							"]}" +
//						"]}" +
//					  "]}";
//		ppt = new PoliqarpPlusTree("[p=NN]{[base=Mann][orth=Frau]}");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(cls3.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// {[base=Mann][orth=Frau]}[p=NN]
//		String cls4 = "{@type=korap:group, operation=operation:sequence, operands=[" +
//						"{@type=korap:group, operation=operation:class, class=0, classOut=0, operands=[" +
//						   "{@type=korap:group, operation=operation:sequence, operands=[" +
//						     "{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}," +
//						     "{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Frau, match=match:eq}}" +
//						   "]}" +
//						"]}," +
//						"{@type=korap:token, wrap={@type=korap:term, layer=p, key=NN, match=match:eq}}" +
//					  "]}";
//		ppt = new PoliqarpPlusTree("{[base=Mann][orth=Frau]}[p=NN]");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(cls4.replaceAll(" ", ""), map.replaceAll(" ", ""));
//
//		// {2:{1:[tt/p=ADJA]}[mate/p=NN]}"
//		String cls5 = "{@type=korap:group, operation=operation:class, class=2, classOut=2, operands=[" +
//						"{@type=korap:group, operation=operation:sequence, operands=[" +
//						   "{@type=korap:group, operation=operation:class, class=1, classOut=1, operands=[" +
//						     "{@type=korap:token, wrap={@type=korap:term, foundry=tt, layer=p, key=ADJA, match=match:eq}}" +
//						   "]}," +
//						   "{@type=korap:token, wrap={@type=korap:term, foundry=mate, layer=p, key=NN, match=match:eq}}" + 
//						"]}" +
//					  "]}";
//		ppt = new PoliqarpPlusTree("{2: {1:[tt/p=ADJA]}[mate/p=NN]}");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(cls5.replaceAll(" ", ""), map.replaceAll(" ", ""));
//	}
//	
//	@Test
//	public void testPositions() throws QueryException {
//		// contains(<s>,<np>)
//		String pos1 = "{@type=korap:group, operation=operation:position, frames=[frames:contains], operands=[" +
//				  "{@type=korap:span, key=s}," +
//				  "{@type=korap:span, key=np}" +
//				"], frame=frame:contains}";
//		assertTrue(equalsQueryContent(pos1, "contains(<s>,<np>)"));
//		
//		// contains(<s>,[base=Mann])
//		String pos2 = "{@type=korap:group, operation=operation:position, frames=[frames:contains], operands=[" +
//				  "{@type=korap:span, key=s}," +
//				  "{@type=korap:token, wrap= {@type=korap:term, layer=lemma, key=Mann, match=match:eq}}" +
//				"], frame=frame:contains}";
//		assertTrue(equalsQueryContent(pos2, "contains(<s>,[base=Mann])"));
//		
//		// contains(<s>,[orth=der][orth=Mann])
//		String pos3 = "{@type=korap:group, operation=operation:position, frames=[frames:contains], operands=[" +
//				  	"{@type=korap:span, key=s}," +
//				  	"{@type=korap:group, operation=operation:sequence, operands=[" +
//				  		"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=der, match=match:eq}}," +
//				  		"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mann, match=match:eq}}" +
//				  	"]}" +
//				  "], frame=frame:contains}";
//		ppt = new PoliqarpPlusTree("contains(<s>,[orth=der][orth=Mann])");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(pos3.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// [base=Auto]contains(<s>,[base=Mann])
//		String pos4 = 
//				"{@type=korap:group, operation=operation:sequence, operands=[" +
//					"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Auto, match=match:eq}}," +
//					"{@type=korap:group, operation=operation:position, frames=[frames:contains], operands=[" +
//				  		"{@type=korap:span, key=s}," +
//				  		"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Mann, match=match:eq}}" +
//				  	"], frame=frame:contains}" +
//				"]}";
//		ppt = new PoliqarpPlusTree("[base=Auto]contains(<s>,[base=Mann])");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(pos4.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// contains(<s>,[pos=N]*)
//		String pos5 = 
//					"{@type=korap:group, operation=operation:position, frames=[frames:contains], operands=[" +
//				  		"{@type=korap:span, key=s}," +
//				  		"{@type=korap:group, operation=operation:repetition, " +
//				  			"operands=[{@type=korap:token, wrap={@type=korap:term, layer=pos, key=N, match=match:eq}}" +
//				  			"], boundary={@type=korap:boundary, min=0}, min=0" +
//				  		"}" +
//				  	"], frame=frame:contains}";
//		ppt = new PoliqarpPlusTree("contains(<s>,[pos=N]*)");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(pos5.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// [base=Auto]contains(<s>,[pos=N]*)
//		String pos6 = 
//				"{@type=korap:group, operation=operation:sequence, operands=[" +
//					"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Auto, match=match:eq}}," +
//					"{@type=korap:group, operation=operation:position, frames=[frames:contains], operands=[" +
//				  		"{@type=korap:span, key=s}," +
//				  		"{@type=korap:group, operation=operation:repetition, " +
//				  			"operands=[{@type=korap:token, wrap={@type=korap:term, layer=pos, key=N, match=match:eq}}" +
//				  			"], boundary={@type=korap:boundary, min=0}, min=0" +
//				  		"}" +
//				  	"], frame=frame:contains}" +
//				"]}";
//		ppt = new PoliqarpPlusTree("[base=Auto]contains(<s>,[pos=N]*)");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(pos6.replaceAll(" ", ""), map.replaceAll(" ", ""));
//	}
//	
//	@Test
//	public void testNestedPositions() throws QueryException {
//		// contains(<s>,startswith(<np>,[orth=Der]))
//		String npos1 = 
//			"{@type=korap:group, operation=operation:position, frames=[frames:contains], operands=[" +
//				"{@type=korap:span, key=s}," +
//				"{@type=korap:group, operation=operation:position, frames=[frames:startswith], operands=[" +
//					"{@type=korap:span, key=np}," +
//					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Der, match=match:eq}}" +
//				"], frame=frame:startswith}" +
//			"], frame=frame:contains}";
//		ppt = new PoliqarpPlusTree("contains(<s>, startswith(<np>,[orth=Der]))");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(npos1.replaceAll(" ", ""), map.replaceAll(" ", ""));
//	}
//	
//	@Test
//	public void testFocusSplit() throws QueryException {
//		// focus([orth=Der]{[orth=Mann]})
//		String shr1 = 
//			"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
//				"{@type=korap:group, operation=operation:sequence, operands=[" +
//					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Der, match=match:eq}}," +
//					"{@type=korap:group, operation=operation:class, class=0, classOut=0, operands=[" +
//						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mann, match=match:eq}}" +
//					"]}" +
//				"]}" +
//			"]}";
//		ppt = new PoliqarpPlusTree("focus([orth=Der]{[orth=Mann]})");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(shr1.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// focus([orth=Der]{[orth=Mann][orth=geht]})
//		String shr2 = 
//			"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
//				"{@type=korap:group, operation=operation:sequence, operands=[" +
//					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Der, match=match:eq}}," +
//					"{@type=korap:group, operation=operation:class, class=0, classOut=0, operands=[" +
//						"{@type=korap:group, operation=operation:sequence, operands=[" +
//							"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mann, match=match:eq}}," +
//							"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=geht, match=match:eq}}" +
//						"]}" +
//					"]}" +
//				"]}" +
//			"]}";
//		ppt = new PoliqarpPlusTree("focus([orth=Der]{[orth=Mann][orth=geht]})");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(shr2.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// focus(1:[orth=Der]{1:[orth=Mann][orth=geht]})
//		String shr3 = 
//			"{@type=korap:reference, operation=operation:focus, classRef=[1], operands=[" +
//				"{@type=korap:group, operation=operation:sequence, operands=[" +
//					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Der, match=match:eq}}," +
//					"{@type=korap:group, operation=operation:class, class=1, classOut=1, operands=[" +
//						"{@type=korap:group, operation=operation:sequence, operands=[" +
//							"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mann, match=match:eq}}," +
//							"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=geht, match=match:eq}}" +
//						"]}" +
//					"]}" +
//				"]}" +
//			"]}";
//		ppt = new PoliqarpPlusTree("focus(1:[orth=Der]{1:[orth=Mann][orth=geht]})");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(shr3.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// focus(1:startswith(<s>,{1:<np>}))
//		String shr4 = 
//			"{@type=korap:reference, operation=operation:focus, classRef=[1], operands=[" +
//				"{@type=korap:group, operation=operation:position, frames=[frames:startswith], operands=[" +
//					"{@type=korap:span, key=s}," +
//					"{@type=korap:group, operation=operation:class, class=1, classOut=1, operands=[" +
//						"{@type=korap:span, key=np}" +
//					"]}" +
//				"], frame=frame:startswith}" +
//			"]}";
//		ppt = new PoliqarpPlusTree("focus(1:startswith(<s>,{1:<np>}))");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(shr4.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// focus(3: startswith(<s>, {3:[base=der]{1:[mate/p=ADJA]{2:[tt/p=NN]}}})) 
//		String shr5 = 
//			"{@type=korap:reference, operation=operation:focus, classRef=[3], operands=[" +
//				"{@type=korap:group, operation=operation:position, frames=[frames:startswith], operands=[" +
//					"{@type=korap:span, key=s}," +
//					"{@type=korap:group, operation=operation:class, class=3, classOut=3, operands=[" +
//						"{@type=korap:group, operation=operation:sequence, operands=[" +
//							"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=der, match=match:eq}}," +
//							"{@type=korap:group, operation=operation:class, class=1, classOut=1, operands=[" +
//								"{@type=korap:group, operation=operation:sequence, operands=[" +
//									"{@type=korap:token, wrap={@type=korap:term, foundry=mate, layer=p, key=ADJA, match=match:eq}}," +
//									"{@type=korap:group, operation=operation:class, class=2, classOut=2, operands=[" +
//										"{@type=korap:token, wrap={@type=korap:term, foundry=tt, layer=p, key=NN, match=match:eq}}" +
//									"]}" + 
//								"]}" +
//							"]}" +
//						"]}" +
//					"]}" +
//				"], frame=frame:startswith}" +
//			"]}";
//		ppt = new PoliqarpPlusTree("focus(3:startswith(<s>,{3:[base=der]{1:[mate/p=ADJA]{2:[tt/p=NN]}}})) ");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(shr5.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// split(3: startswith(<s>, {3:[base=der]{1:[mate/p=ADJA]{2:[tt/p=NN]}}})) 
//		String shr6 = 
//			"{@type=korap:reference, operation=operation:split, classRef=[3], operands=[" +
//				"{@type=korap:group, operation=operation:position, frames=[frames:startswith], operands=[" +
//					"{@type=korap:span, key=s}," +
//					"{@type=korap:group, operation=operation:class, class=3, classOut=3, operands=[" +
//						"{@type=korap:group, operation=operation:sequence, operands=[" +
//							"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=der, match=match:eq}}," +
//							"{@type=korap:group, operation=operation:class, class=1, classOut=1, operands=[" +
//								"{@type=korap:group, operation=operation:sequence, operands=[" +
//									"{@type=korap:token, wrap={@type=korap:term, foundry=mate, layer=p, key=ADJA, match=match:eq}}," +
//									"{@type=korap:group, operation=operation:class, class=2, classOut=2, operands=[" +
//										"{@type=korap:token, wrap={@type=korap:term, foundry=tt, layer=p, key=NN, match=match:eq}}" +
//									"]}" + 
//								"]}" +
//							"]}" +
//						"]}" +
//					"]}" +
//				"], frame=frame:startswith}" +
//			"]}";
//		ppt = new PoliqarpPlusTree("split(3:startswith(<s>,{3:[base=der]{1:[mate/p=ADJA]{2:[tt/p=NN]}}})) ");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(shr6.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// split(2|3: startswith(<s>, {3:[base=der]{1:[mate/p=ADJA]{2:[tt/p=NN]}}})) 
//		String shr7 = 
//			"{@type=korap:reference, operation=operation:split, classRef=[2, 3], classRefOp=classRefOp:intersection, operands=[" +
//				"{@type=korap:group, operation=operation:position, frames=[frames:startswith], operands=[" +
//					"{@type=korap:span, key=s}," +
//					"{@type=korap:group, operation=operation:class, class=3, classOut=3, operands=[" +
//						"{@type=korap:group, operation=operation:sequence, operands=[" +
//							"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=der, match=match:eq}}," +
//							"{@type=korap:group, operation=operation:class, class=1, classOut=1, operands=[" +
//								"{@type=korap:group, operation=operation:sequence, operands=[" +
//									"{@type=korap:token, wrap={@type=korap:term, foundry=mate, layer=p, key=ADJA, match=match:eq}}," +
//									"{@type=korap:group, operation=operation:class, class=2, classOut=2, operands=[" +
//										"{@type=korap:token, wrap={@type=korap:term, foundry=tt, layer=p, key=NN, match=match:eq}}" +
//									"]}" + 
//								"]}" +
//							"]}" +
//						"]}" +
//					"]}" +
//				"], frame=frame:startswith}" +
//			"]}";
//		ppt = new PoliqarpPlusTree("split(2|3:startswith(<s>,{3:[base=der]{1:[mate/p=ADJA]{2:[tt/p=NN]}}})) ");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(shr7.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		
//		String shr8 = 
//			"{@type=korap:reference, operation=operation:focus, classRef=[1], operands=[" +
//				"{@type=korap:group, operation=operation:sequence, operands=[" +
//					"{@type=korap:group, operation=operation:class, class=0, classOut=0, operands=[" +
//						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=der, match=match:eq}}" +
//					"]}," +
//					"{@type=korap:group, operation=operation:class, class=1, classOut=1, operands=[" +
//						"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=ADJA, match=match:eq}}" +
//					"]}" +
//				"]}" +
//			"]}";
//		ppt = new PoliqarpPlusTree("focus(1:{[base=der]}{1:[pos=ADJA]})");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(shr8.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//	}
//	
//	@Test
//	public void testSubspan() throws QueryException {
//		query = "submatch(1,:<s>)";
//		expected = 
//			"{@type=korap:reference, operation=operation:focus, operands=[" +
//					"{@type=korap:span, key=s}" +
//				"], spanRef=[1]" +
//			"}";
//		ppt = new PoliqarpPlusTree(query);
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
//	
//		query = "submatch(1,4:<s>)";
//		expected = 
//			"{@type=korap:reference, operation=operation:focus, operands=[" +
//					"{@type=korap:span, key=s}" +
//				"], spanRef=[1,4]" +
//			"}";
//		ppt = new PoliqarpPlusTree(query);
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		query = "submatch(1,4:contains(<s>,[base=Haus]))";
//		expected = 
//			"{@type=korap:reference, operation=operation:focus, operands=[" +
//				"{@type=korap:group, operation=operation:position, frames=[frames:contains], operands=[" +
//					"{@type=korap:span, key=s}," +
//					"{@type=korap:token, wrap= {@type=korap:term, layer=lemma, key=Haus, match=match:eq}}" +
//				"], frame=frame:contains}" +
//				"], spanRef=[1,4]" +
//			"}";
//		ppt = new PoliqarpPlusTree(query);
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
//	}
//	
//	@Test
//	public void testRelations() throws QueryException {
//		query = "relatesTo(<s>,<np>)";
//		expected = 
//			"{@type=korap:group, operation=operation:relation, operands=[" +
//					"{@type=korap:span, key=s}," +
//					"{@type=korap:span, key=np}" +
//				"], relation={@type=korap:relation}" +
//			"}";
//		ppt = new PoliqarpPlusTree(query);
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		query = "relatesTo([base=Baum],<np>)";
//		expected = 
//				"{@type=korap:group, operation=operation:relation, operands=[" +
//						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Baum, match=match:eq}}," +
//						"{@type=korap:span, key=np}" +
//					"], relation={@type=korap:relation}" +
//				"}";
//		ppt = new PoliqarpPlusTree(query);
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		query = "dominates(<np>,[base=Baum])";
//		expected = 
//				"{@type=korap:group, operation=operation:relation, operands=[" +
//						"{@type=korap:span, key=np}," +
//						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Baum, match=match:eq}}" +
//					"], relation={@type=korap:relation, layer=c}" +
//				"}";
//		ppt = new PoliqarpPlusTree(query);
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		query = "dominates(cnx/c:<np>,[base=Baum])";
//		expected = 
//				"{@type=korap:group, operation=operation:relation, operands=[" +
//						"{@type=korap:span, key=np}," +
//						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Baum, match=match:eq}}" +
//					"], relation={@type=korap:relation, layer=c, foundry=cnx}" +
//				"}";
//		ppt = new PoliqarpPlusTree(query);
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		query = "dominates(cnx/c*:<np>,[base=Baum])";
//		expected = 
//				"{@type=korap:group, operation=operation:relation, operands=[" +
//						"{@type=korap:span, key=np}," +
//						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Baum, match=match:eq}}" +
//					"], relation={@type=korap:relation, layer=c, foundry=cnx, boundary={@type=korap:boundary, min=0}}" +
//				"}";
//		ppt = new PoliqarpPlusTree(query);
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		query = "dominates(cnx/c{1,5}:<np>,[base=Baum])";
//		expected = 
//				"{@type=korap:group, operation=operation:relation, operands=[" +
//						"{@type=korap:span, key=np}," +
//						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Baum, match=match:eq}}" +
//					"], relation={@type=korap:relation, layer=c, foundry=cnx, boundary={@type=korap:boundary, min=1, max=5}}" +
//				"}";
//		ppt = new PoliqarpPlusTree(query);
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		query = "relatesTo(mate/d=HEAD:<np>,[base=Baum])";
//		expected = 
//				"{@type=korap:group, operation=operation:relation, operands=[" +
//						"{@type=korap:span, key=np}," +
//						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=Baum, match=match:eq}}" +
//					"], relation={@type=korap:relation, foundry=mate, layer=d, key=HEAD}" +
//				"}";
//		ppt = new PoliqarpPlusTree(query);
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//	}
//	
//	
//	
//	@Test
//	public void testFoundries() throws QueryException {
//		// [tt/base=Mann]
//		String layer1 = "{@type=korap:token, wrap={@type=korap:term, foundry=tt, layer=lemma, key=Mann, match=match:eq}}";
//		ppt = new PoliqarpPlusTree("[tt/base=Mann]");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(layer1.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//	}
//	
//	@Test
//	public void testAlign() throws QueryException {
//		// [orth=der]^[orth=Mann]
//		query = "[orth=der]^[orth=Mann]";
//		expected = 
//				"{@type=korap:group, operation=operation:sequence, operands=[" +
//					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=der, match=match:eq}}," +
//					"{@type=korap:group, operation=operation:class, class=1025, classOut=1025, operands=[" +
//						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mann, match=match:eq}}" +
//					"]}" +
//				"]}";
//		metaExpected = 
//				"{alignment=1025}";
//		ppt = new PoliqarpPlusTree(query);
//		map = ppt.getRequestMap().get("query").toString();
//		metaMap = ppt.getRequestMap().get("meta").toString();
//		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		assertEquals(metaExpected.replaceAll(" ", ""), metaMap.replaceAll(" ", ""));
//		
//		// [orth=der]^[orth=große][orth=Mann]
//		query = "[orth=der]^[orth=große][orth=Mann]";
//		String expected = 
//				"{@type=korap:group, operation=operation:sequence, operands=[" +
//					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=der, match=match:eq}}," +
//					"{@type=korap:group, operation=operation:class, class=1025, classOut=1025, operands=[" +
//						"{@type=korap:group, operation=operation:sequence, operands=[" +
//							"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=große, match=match:eq}}," +
//							"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mann, match=match:eq}}" +
//						"]}" +
//					"]}" +
//				"]}";
//		metaExpected = 
//				"{alignment=1025}";
//		ppt = new PoliqarpPlusTree(query);
//		map = ppt.getRequestMap().get("query").toString();
//		metaMap = ppt.getRequestMap().get("meta").toString();
//		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		assertEquals(metaExpected.replaceAll(" ", ""), metaMap.replaceAll(" ", ""));
//		
//		query = "([base=a]^[base=b])|[base=c]";
//		expected = 
//				"{@type=korap:group, operation=operation:or, operands=[" +
//						"{@type=korap:group, operation=operation:sequence, operands=[" +
//							"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=a, match=match:eq}}," +
//							"{@type=korap:group, operation=operation:class, class=1025, classOut=1025, operands=[" +
//								"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=b, match=match:eq}}" +
//							"]}" +
//						"]}," +
//						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=c, match=match:eq}}" +
//				"]}";
//		metaExpected = 
//				"{alignment=1025}";
//		ppt = new PoliqarpPlusTree(query);
//		map = ppt.getRequestMap().get("query").toString();
//		metaMap = ppt.getRequestMap().get("meta").toString();
//		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		assertEquals(metaExpected.replaceAll(" ", ""), metaMap.replaceAll(" ", ""));
//		
//		query = "([base=a]^[base=b][base=c])|[base=d]";
//		expected = 
//				"{@type=korap:group, operation=operation:or, operands=[" +
//						"{@type=korap:group, operation=operation:sequence, operands=[" +
//							"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=a, match=match:eq}}," +
//							"{@type=korap:group, operation=operation:class, class=1025, classOut=1025, operands=[" +
//								"{@type=korap:group, operation=operation:sequence, operands=[" +
//									"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=b, match=match:eq}}," +
//									"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=c, match=match:eq}}" +
//								"]}" +
//							"]}" +
//						"]}," +
//						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=d, match=match:eq}}" +
//				"]}";
//		metaExpected = 
//				"{alignment=1025}";
//		ppt = new PoliqarpPlusTree(query);
//		map = ppt.getRequestMap().get("query").toString();
//		metaMap = ppt.getRequestMap().get("meta").toString();
//		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		assertEquals(metaExpected.replaceAll(" ", ""), metaMap.replaceAll(" ", ""));
//		
//		query = "([base=a]^[base=b]^[base=c])|[base=d]";
//		expected = 
//				"{@type=korap:group, operation=operation:or, operands=[" +
//						"{@type=korap:group, operation=operation:sequence, operands=[" +
//							"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=a, match=match:eq}}," +
//							"{@type=korap:group, operation=operation:class, class=1025, classOut=1025, operands=[" +
//								"{@type=korap:group, operation=operation:sequence, operands=[" +
//									"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=b, match=match:eq}}," +
//									"{@type=korap:group, operation=operation:class, class=1026, classOut=1026, operands=[" +
//										"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=c, match=match:eq}}" +
//									"]}" +
//								"]}" +
//							"]}" +
//						"]}," +
//						"{@type=korap:token, wrap={@type=korap:term, layer=lemma, key=d, match=match:eq}}" +
//				"]}";
//		metaExpected = 
//				"{alignment=[1025,1026]}";
//		ppt = new PoliqarpPlusTree(query);
//		map = ppt.getRequestMap().get("query").toString();
//		metaMap = ppt.getRequestMap().get("meta").toString();
//		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		assertEquals(metaExpected.replaceAll(" ", ""), metaMap.replaceAll(" ", ""));
//		
//		
//	}
//	
//	@Test
//	public void testSimpleQueries() throws QueryException {
//		// Baum
//		String simple1 = 
//				"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Baum, match=match:eq}}";
//		ppt = new PoliqarpPlusTree("Baum");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(simple1.replaceAll(" ", ""), map.replaceAll(" ", ""));
//
//		// Baum/i
//		String simple1b = 
//				"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Baum, match=match:eq, caseInsensitive=true}}";
//		ppt = new PoliqarpPlusTree("Baum/i");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(simple1b.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// Der Baum
//		String simple2 = 
//				"{@type=korap:group, operation=operation:sequence, operands=[" +
//					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Der, match=match:eq}}, " +
//					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Baum, match=match:eq}}" +
//				"]}";
//		ppt = new PoliqarpPlusTree("Der Baum");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(simple2.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// Der Baum/i
//		String simple2b = 
//				"{@type=korap:group, operation=operation:sequence, operands=[" +
//					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Der, match=match:eq}}, " +
//					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Baum, match=match:eq, caseInsensitive=true}}" +
//				"]}";
//		ppt = new PoliqarpPlusTree("Der Baum/i");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(simple2b.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// Der große Baum
//		String simple3 = 
//				"{@type=korap:group, operation=operation:sequence, operands=[" +
//					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Der, match=match:eq}}, " +
//					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=große, match=match:eq}}, " +						
//					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Baum, match=match:eq}}" +
//				"]}";
//		ppt = new PoliqarpPlusTree("Der große Baum");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(simple3.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// Baum | Stein
//		String simple4 = 
//				"{@type=korap:group, operation=operation:or, operands=[" +
//					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Baum, match=match:eq}}, " +						
//					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Stein, match=match:eq}}" +
//				"]}";
//		ppt = new PoliqarpPlusTree("Baum | Stein");
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(simple4.replaceAll(" ", ""), map.replaceAll(" ", ""));		
//		
//		// Baum | Stein Haus
//		String query = "(Baum | Stein) Haus";
//		String simple5 = 
//				"{@type=korap:group, operation=operation:sequence, operands=[" +
//					"{@type=korap:group, operation=operation:or, operands=[" +
//						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Baum, match=match:eq}}, " +						
//						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Stein, match=match:eq}}" +
//					"]}," +
//					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Haus, match=match:eq}} " +			
//				"]}";
//		ppt = new PoliqarpPlusTree(query);
//		map = ppt.getRequestMap().get("query").toString();
//		assertEquals(simple5.replaceAll(" ", ""), map.replaceAll(" ", ""));		
//	}
}
