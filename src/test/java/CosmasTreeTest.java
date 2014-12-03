import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import de.ids_mannheim.korap.query.serialize.CosmasTree;
import de.ids_mannheim.korap.query.serialize.QuerySerializer;
import de.ids_mannheim.korap.util.QueryException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests for JSON-LD serialization of Cosmas II queries. 
 * @author Joachim Bingel (bingel@ids-mannheim.de)
 * @version 1.0
 */

public class CosmasTreeTest {
	

	String query;
	ArrayList<JsonNode> operands;

	QuerySerializer qs = new QuerySerializer();
	ObjectMapper mapper = new ObjectMapper();
	JsonNode res;
	
	@Test
	public void testContext() throws QueryException, JsonProcessingException, IOException {
		String contextString = "http://ids-mannheim.de/ns/KorAP/json-ld/v0.2/context.jsonld";
		query = "foo";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals(contextString, res.get("@context").asText());
	}
	
	
	@Test
	public void testSingleToken() throws QueryException, JsonProcessingException, IOException {
		query = "der";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:token", 		res.at("/query/@type").asText());
		assertEquals("korap:term", 			res.at("/query/wrap/@type").asText());
		assertEquals("der", 				res.at("/query/wrap/key").asText());
		assertEquals("orth", 				res.at("/query/wrap/layer").asText());
		assertEquals("match:eq",			res.at("/query/wrap/match").asText());
		
		query = "&Mann";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:token", 		res.at("/query/@type").asText());
		assertEquals("korap:term", 			res.at("/query/wrap/@type").asText());
		assertEquals("Mann", 				res.at("/query/wrap/key").asText());
		assertEquals("lemma", 				res.at("/query/wrap/layer").asText());
		assertEquals("match:eq",			res.at("/query/wrap/match").asText());
	}
		

	
	@Test
	public void testWildcardToken() throws QueryException, JsonProcessingException, IOException {
		query = "*der";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:term", 			res.at("/query/wrap/@type").asText());
		assertEquals("type:wildcard",		res.at("/query/wrap/type").asText());
		assertEquals("*der", 				res.at("/query/wrap/key").asText());
		assertEquals("orth", 				res.at("/query/wrap/layer").asText());
		assertEquals("match:eq",			res.at("/query/wrap/match").asText());
		
		query = "*de*?r";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("*de*?r", 				res.at("/query/wrap/key").asText());
	}
//	
	@Test
	public void testCaseSensitivityFlag() throws QueryException, JsonProcessingException, IOException {
		query = "$deutscher";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:term", 			res.at("/query/wrap/@type").asText());
		assertEquals("deutscher",			res.at("/query/wrap/key").asText());
		assertEquals(true,					res.at("/query/wrap/caseInsensitive").asBoolean());
		assertEquals("orth", 				res.at("/query/wrap/layer").asText());
		assertEquals("match:eq",			res.at("/query/wrap/match").asText());
		
		query = "$deutscher Bundestag";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group",			res.at("/query/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operation").asText());
		assertEquals("korap:term", 			res.at("/query/operands/0/wrap/@type").asText());
		assertEquals("deutscher",			res.at("/query/operands/0/wrap/key").asText());
		assertEquals(true,					res.at("/query/operands/0/wrap/caseInsensitive").asBoolean());
		assertEquals("orth", 				res.at("/query/operands/0/wrap/layer").asText());
		assertEquals("match:eq",			res.at("/query/operands/0/wrap/match").asText());
		assertEquals("Bundestag",			res.at("/query/operands/1/wrap/key").asText());
		assertEquals(true,					res.at("/query/operands/1/wrap/caseInsensitive").isMissingNode());
	}
	
	@Test
	public void testMORPH() throws QueryException, JsonProcessingException, IOException {
		query = "MORPH(p=V)";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:token",			res.at("/query/@type").asText());
		assertEquals("korap:term", 			res.at("/query/wrap/@type").asText());
		assertEquals("V",					res.at("/query/wrap/key").asText());
		assertEquals("p",					res.at("/query/wrap/layer").asText());
		assertEquals("match:eq",			res.at("/query/wrap/match").asText());
		
		query = "MORPH(V)";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:token",			res.at("/query/@type").asText());
		assertEquals("korap:term", 			res.at("/query/wrap/@type").asText());
		assertEquals("V",					res.at("/query/wrap/key").asText());
		assertEquals("match:eq",			res.at("/query/wrap/match").asText());
		
		query = "MORPH(tt/p=V)";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:token",			res.at("/query/@type").asText());
		assertEquals("korap:term", 			res.at("/query/wrap/@type").asText());
		assertEquals("V",					res.at("/query/wrap/key").asText());
		assertEquals("p",					res.at("/query/wrap/layer").asText());
		assertEquals("tt",					res.at("/query/wrap/foundry").asText());
		assertEquals("match:eq",			res.at("/query/wrap/match").asText());
		
		query = "MORPH(mate/m=temp:pres)";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:token",			res.at("/query/@type").asText());
		assertEquals("korap:term", 			res.at("/query/wrap/@type").asText());
		assertEquals("temp",				res.at("/query/wrap/key").asText());
		assertEquals("pres",				res.at("/query/wrap/value").asText());
		assertEquals("m",					res.at("/query/wrap/layer").asText());
		assertEquals("mate",				res.at("/query/wrap/foundry").asText());
		assertEquals("match:eq",			res.at("/query/wrap/match").asText());
		
		query = "MORPH(tt/p=V & mate/m!=temp:pres)";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:token",			res.at("/query/@type").asText());
		assertEquals("korap:termGroup",		res.at("/query/wrap/@type").asText());
		assertEquals("V",					res.at("/query/wrap/operands/0/key").asText());
		assertEquals("p",					res.at("/query/wrap/operands/0/layer").asText());
		assertEquals("tt",					res.at("/query/wrap/operands/0/foundry").asText());
		assertEquals("match:eq",			res.at("/query/wrap/operands/0/match").asText());
		assertEquals("temp",				res.at("/query/wrap/operands/1/key").asText());
		assertEquals("pres",				res.at("/query/wrap/operands/1/value").asText());
		assertEquals("m",					res.at("/query/wrap/operands/1/layer").asText());
		assertEquals("mate",				res.at("/query/wrap/operands/1/foundry").asText());
		assertEquals("match:ne",			res.at("/query/wrap/operands/1/match").asText());
	}
	
	@Test
	public void testSequence() throws QueryException, JsonProcessingException, IOException {
		query = "der Mann";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group",			res.at("/query/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operation").asText());
		assertEquals("der",					res.at("/query/operands/0/wrap/key").asText());
		assertEquals("Mann",				res.at("/query/operands/1/wrap/key").asText());
		assertEquals(true,					res.at("/query/operands/2").isMissingNode());
		
		query = "der Mann schläft";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group",			res.at("/query/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operation").asText());
		assertEquals("der",					res.at("/query/operands/0/wrap/key").asText());
		assertEquals("Mann",				res.at("/query/operands/1/wrap/key").asText());
		assertEquals("schläft",				res.at("/query/operands/2/wrap/key").asText());
		assertEquals(true,					res.at("/query/operands/3").isMissingNode());
		
		query = "der Mann schläft lang";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group",			res.at("/query/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operation").asText());
		assertEquals("der",					res.at("/query/operands/0/wrap/key").asText());
		assertEquals("Mann",				res.at("/query/operands/1/wrap/key").asText());
		assertEquals("schläft",				res.at("/query/operands/2/wrap/key").asText());
		assertEquals("lang",				res.at("/query/operands/3/wrap/key").asText());
		assertEquals(true,					res.at("/query/operands/4").isMissingNode());
		
		query = "der #ELEM(W)";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group",			res.at("/query/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operation").asText());
		assertEquals("der",					res.at("/query/operands/0/wrap/key").asText());
		assertEquals("w",					res.at("/query/operands/1/key").asText());
		assertEquals("korap:span",			res.at("/query/operands/1/@type").asText());
		assertEquals(true,					res.at("/query/operands/2").isMissingNode());
		
		query = "der #ELEM(W) Mann";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group",			res.at("/query/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operation").asText());
		assertEquals("der",					res.at("/query/operands/0/wrap/key").asText());
		assertEquals("w",					res.at("/query/operands/1/key").asText());
		assertEquals("korap:span",			res.at("/query/operands/1/@type").asText());
		assertEquals("Mann",				res.at("/query/operands/2/wrap/key").asText());
		assertEquals(true,					res.at("/query/operands/3").isMissingNode());
		
		query = "der MORPH(p=ADJA) Mann";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group",			res.at("/query/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operation").asText());
		assertEquals("der",					res.at("/query/operands/0/wrap/key").asText());
		assertEquals("ADJA",				res.at("/query/operands/1/wrap/key").asText());
		assertEquals("p",					res.at("/query/operands/1/wrap/layer").asText());
		assertEquals("Mann",				res.at("/query/operands/2/wrap/key").asText());
		assertEquals(true,					res.at("/query/operands/3").isMissingNode());
	}

	@Test
	public void testOPOR() throws QueryException, JsonProcessingException, IOException {
		query = "Sonne oder Mond";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group",			res.at("/query/@type").asText());
		assertEquals("operation:or",		res.at("/query/operation").asText());
		assertEquals("Sonne",				res.at("/query/operands/0/wrap/key").asText());
		assertEquals("Mond",				res.at("/query/operands/1/wrap/key").asText());
		assertEquals(true,					res.at("/query/operands/2").isMissingNode());
		
		query = "(Sonne scheint) oder Mond";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group",			res.at("/query/@type").asText());
		assertEquals("operation:or",		res.at("/query/operation").asText());
		assertEquals("korap:group",			res.at("/query/operands/0/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operands/0/operation").asText());
		assertEquals("Sonne",				res.at("/query/operands/0/operands/0/wrap/key").asText());
		assertEquals("scheint",				res.at("/query/operands/0/operands/1/wrap/key").asText());
		assertEquals("Mond",				res.at("/query/operands/1/wrap/key").asText());
		assertEquals(true,					res.at("/query/operands/2").isMissingNode());
		
		query = "(Sonne scheint) oder (Mond scheint)";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group",			res.at("/query/@type").asText());
		assertEquals("operation:or",		res.at("/query/operation").asText());
		assertEquals("korap:group",			res.at("/query/operands/0/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operands/0/operation").asText());
		assertEquals("korap:group",			res.at("/query/operands/1/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operands/1/operation").asText());
		assertEquals("Sonne",				res.at("/query/operands/0/operands/0/wrap/key").asText());
		assertEquals("scheint",				res.at("/query/operands/0/operands/1/wrap/key").asText());
		assertEquals("Mond",				res.at("/query/operands/1/operands/0/wrap/key").asText());
		assertEquals("scheint",				res.at("/query/operands/1/operands/1/wrap/key").asText());
		assertEquals(true,					res.at("/query/operands/2").isMissingNode());
	}

	@Test
	public void testOPORAND() throws QueryException, JsonProcessingException, IOException {
		query = "(Sonne oder Mond) und scheint";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group",			res.at("/query/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operation").asText());
		assertEquals("cosmas:distance",		res.at("/query/distances/0/@type").asText());
		assertEquals("t",					res.at("/query/distances/0/key").asText());
		assertEquals(0,						res.at("/query/distances/0/min").asInt());
		assertEquals(0,						res.at("/query/distances/0/max").asInt());
		assertEquals("korap:group",			res.at("/query/operands/0/@type").asText());
		assertEquals("operation:or",		res.at("/query/operands/0/operation").asText());
		assertEquals("Sonne",				res.at("/query/operands/0/operands/0/wrap/key").asText());
		assertEquals("Mond",				res.at("/query/operands/0/operands/1/wrap/key").asText());
		assertEquals("korap:token",			res.at("/query/operands/1/@type").asText());
		assertEquals("scheint",				res.at("/query/operands/1/wrap/key").asText());
		
		query = "scheint und (Sonne oder Mond)";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group",			res.at("/query/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operation").asText());
		assertEquals("cosmas:distance",		res.at("/query/distances/0/@type").asText());
		assertEquals("t",					res.at("/query/distances/0/key").asText());
		assertEquals(0,						res.at("/query/distances/0/min").asInt());
		assertEquals(0,						res.at("/query/distances/0/max").asInt());
		assertEquals("korap:token",			res.at("/query/operands/0/@type").asText());
		assertEquals("scheint",				res.at("/query/operands/0/wrap/key").asText());
		assertEquals("korap:group",			res.at("/query/operands/1/@type").asText());
		assertEquals("operation:or",		res.at("/query/operands/1/operation").asText());
		assertEquals("Sonne",				res.at("/query/operands/1/operands/0/wrap/key").asText());
		assertEquals("Mond",				res.at("/query/operands/1/operands/1/wrap/key").asText());
		
		query = "Regen und scheint und (Sonne oder Mond)";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group",			res.at("/query/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operation").asText());
		assertEquals("cosmas:distance",		res.at("/query/distances/0/@type").asText());
		assertEquals("t",					res.at("/query/distances/0/key").asText());
		assertEquals(0,						res.at("/query/distances/0/min").asInt());
		assertEquals(0,						res.at("/query/distances/0/max").asInt());
		assertEquals("korap:token",			res.at("/query/operands/0/@type").asText());
		assertEquals("Regen",				res.at("/query/operands/0/wrap/key").asText());
		assertEquals("korap:group",			res.at("/query/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operation").asText());
		assertEquals("cosmas:distance",		res.at("/query/operands/1/distances/0/@type").asText());
		assertEquals("t",					res.at("/query/operands/1/distances/0/key").asText());
		assertEquals(0,						res.at("/query/operands/1/distances/0/min").asInt());
		assertEquals(0,						res.at("/query/operands/1/distances/0/max").asInt());
		assertEquals("scheint",				res.at("/query/operands/1/operands/0/wrap/key").asText());
		assertEquals("korap:group",			res.at("/query/operands/1/operands/1/@type").asText());
		assertEquals("operation:or",		res.at("/query/operands/1/operands/1/operation").asText());
		assertEquals("Sonne",				res.at("/query/operands/1/operands/1/operands/0/wrap/key").asText());
		assertEquals("Mond",				res.at("/query/operands/1/operands/1/operands/1/wrap/key").asText());
	}
	
	@Test
	public void testOPNOT() throws QueryException, JsonProcessingException, IOException {
		query = "Sonne nicht Mond";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group",			res.at("/query/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operation").asText());
		assertEquals("cosmas:distance",		res.at("/query/distances/0/@type").asText());
		assertEquals("t",					res.at("/query/distances/0/key").asText());
		assertEquals(0,						res.at("/query/distances/0/min").asInt());
		assertEquals(0,						res.at("/query/distances/0/max").asInt());
		assertEquals(true,					res.at("/query/distances/0/exclude").asBoolean());
		assertEquals("korap:token",			res.at("/query/operands/0/@type").asText());
		assertEquals("Sonne",				res.at("/query/operands/0/wrap/key").asText());
		assertEquals("Mond",				res.at("/query/operands/1/wrap/key").asText());
		
		query = "Sonne nicht Mond nicht Sterne";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group",			res.at("/query/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operation").asText());
		assertEquals("cosmas:distance",		res.at("/query/distances/0/@type").asText());
		assertEquals("t",					res.at("/query/distances/0/key").asText());
		assertEquals(0,						res.at("/query/distances/0/min").asInt());
		assertEquals(0,						res.at("/query/distances/0/max").asInt());
		assertEquals(true,					res.at("/query/distances/0/exclude").asBoolean());
		assertEquals("korap:token",			res.at("/query/operands/0/@type").asText());
		assertEquals("Sonne",				res.at("/query/operands/0/wrap/key").asText());
		assertEquals("korap:group",			res.at("/query/operands/1/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operands/1/operation").asText());
		assertEquals("cosmas:distance",		res.at("/query/operands/1/distances/0/@type").asText());
		assertEquals("t",					res.at("/query/operands/1/distances/0/key").asText());
		assertEquals(0,						res.at("/query/operands/1/distances/0/min").asInt());
		assertEquals(0,						res.at("/query/operands/1/distances/0/max").asInt());
		assertEquals(true,					res.at("/query/operands/1/distances/0/exclude").asBoolean());
		assertEquals("Mond",				res.at("/query/operands/1/operands/0/wrap/key").asText());
		assertEquals("Sterne",				res.at("/query/operands/1/operands/1/wrap/key").asText());
		
		query = "(Sonne nicht Mond) nicht Sterne";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group",			res.at("/query/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operation").asText());
		assertEquals("cosmas:distance",		res.at("/query/distances/0/@type").asText());
		assertEquals("t",					res.at("/query/distances/0/key").asText());
		assertEquals(0,						res.at("/query/distances/0/min").asInt());
		assertEquals(0,						res.at("/query/distances/0/max").asInt());
		assertEquals(true,					res.at("/query/distances/0/exclude").asBoolean());
		assertEquals("korap:group",			res.at("/query/operands/0/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operands/0/operation").asText());
		assertEquals("cosmas:distance",		res.at("/query/operands/0/distances/0/@type").asText());
		assertEquals("t",					res.at("/query/operands/0/distances/0/key").asText());
		assertEquals(0,						res.at("/query/operands/0/distances/0/min").asInt());
		assertEquals(0,						res.at("/query/operands/0/distances/0/max").asInt());
		assertEquals(true,					res.at("/query/operands/0/distances/0/exclude").asBoolean());
		assertEquals("Sonne",				res.at("/query/operands/0/operands/0/wrap/key").asText());
		assertEquals("Mond",				res.at("/query/operands/0/operands/1/wrap/key").asText());
		assertEquals("korap:token",			res.at("/query/operands/1/@type").asText());
		assertEquals("Sterne",				res.at("/query/operands/1/wrap/key").asText());
	}
	
	@Test
	public void testOPPROX() throws QueryException, JsonProcessingException, IOException {
		query = "Sonne /+w1:4 Mond";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:reference",		res.at("/query/@type").asText());
		assertEquals("operation:focus",		res.at("/query/operation").asText());
		assertEquals(129,					res.at("/query/classRef/0").asInt());
		assertEquals("korap:group",			res.at("/query/operands/0/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operands/0/operation").asText());
		assertEquals("korap:distance",		res.at("/query/operands/0/distances/0/@type").asText());
		assertEquals("w",					res.at("/query/operands/0/distances/0/key").asText());
		assertEquals(1,						res.at("/query/operands/0/distances/0/boundary/min").asInt());
		assertEquals(4,						res.at("/query/operands/0/distances/0/boundary/max").asInt());
		assertEquals(true,					res.at("/query/operands/0/inOrder").asBoolean());
		assertEquals("korap:group",			res.at("/query/operands/0/operands/0/@type").asText());
		assertEquals("operation:class",		res.at("/query/operands/0/operands/0/operation").asText());
		assertEquals(129,					res.at("/query/operands/0/operands/0/classOut").asInt());
		assertEquals(129,					res.at("/query/operands/0/operands/1/classOut").asInt());
		assertEquals("korap:token",			res.at("/query/operands/0/operands/0/operands/0/@type").asText());
		assertEquals("Sonne",				res.at("/query/operands/0/operands/0/operands/0/wrap/key").asText());
		assertEquals("Mond",				res.at("/query/operands/0/operands/1/operands/0/wrap/key").asText());
		
		query = "Sonne /+w1:4,s0,p1:3 Mond";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:reference",		res.at("/query/@type").asText());
		assertEquals("operation:focus",		res.at("/query/operation").asText());
		assertEquals(129,					res.at("/query/classRef/0").asInt());
		assertEquals("korap:group",			res.at("/query/operands/0/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operands/0/operation").asText());
		assertEquals("korap:distance",		res.at("/query/operands/0/distances/0/@type").asText());
		assertEquals("w",					res.at("/query/operands/0/distances/0/key").asText());
		assertEquals(1,						res.at("/query/operands/0/distances/0/boundary/min").asInt());
		assertEquals(4,						res.at("/query/operands/0/distances/0/boundary/max").asInt());
		assertEquals("s",					res.at("/query/operands/0/distances/1/key").asText());
		assertEquals(0,						res.at("/query/operands/0/distances/1/boundary/min").asInt());
		assertEquals("p",					res.at("/query/operands/0/distances/2/key").asText());
		assertEquals(1,						res.at("/query/operands/0/distances/2/boundary/min").asInt());
		assertEquals(3,						res.at("/query/operands/0/distances/2/boundary/max").asInt());
		assertEquals(true,					res.at("/query/operands/0/inOrder").asBoolean());
		assertEquals("korap:group",			res.at("/query/operands/0/operands/0/@type").asText());
		assertEquals("operation:class",		res.at("/query/operands/0/operands/0/operation").asText());
		assertEquals(129,					res.at("/query/operands/0/operands/0/classOut").asInt());
		assertEquals(129,					res.at("/query/operands/0/operands/1/classOut").asInt());
		assertEquals("korap:token",			res.at("/query/operands/0/operands/0/operands/0/@type").asText());
		assertEquals("Sonne",				res.at("/query/operands/0/operands/0/operands/0/wrap/key").asText());
		assertEquals("Mond",				res.at("/query/operands/0/operands/1/operands/0/wrap/key").asText());
		
		query = "Sonne /+w4 Mond";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:distance",		res.at("/query/operands/0/distances/0/@type").asText());
		assertEquals("w",					res.at("/query/operands/0/distances/0/key").asText());
		assertEquals(0,						res.at("/query/operands/0/distances/0/boundary/min").asInt());
		assertEquals(4,						res.at("/query/operands/0/distances/0/boundary/max").asInt());
		
		query = "Sonne /-w4 Mond";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:distance",		res.at("/query/operands/0/distances/0/@type").asText());
		assertEquals("w",					res.at("/query/operands/0/distances/0/key").asText());
		assertEquals(0,						res.at("/query/operands/0/distances/0/boundary/min").asInt());
		assertEquals(4,						res.at("/query/operands/0/distances/0/boundary/max").asInt());
		assertEquals("Mond",				res.at("/query/operands/0/operands/0/operands/0/wrap/key").asText());
		assertEquals("Sonne",				res.at("/query/operands/0/operands/1/operands/0/wrap/key").asText());
		
		query = "Sonne /w4 Mond";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:distance",		res.at("/query/operands/0/distances/0/@type").asText());
		assertEquals("w",					res.at("/query/operands/0/distances/0/key").asText());
		assertEquals(0,						res.at("/query/operands/0/distances/0/boundary/min").asInt());
		assertEquals(4,						res.at("/query/operands/0/distances/0/boundary/max").asInt());
		assertEquals("Sonne",				res.at("/query/operands/0/operands/0/operands/0/wrap/key").asText());
		assertEquals("Mond",				res.at("/query/operands/0/operands/1/operands/0/wrap/key").asText());
		assertEquals(false,					res.at("/query/operands/0/inOrder").asBoolean());
	}
	
	@Test
	public void testOPPROXNested() throws QueryException, JsonProcessingException, IOException {	
		query = "Sonne /+w1:4 Mond /+w1:7 Sterne";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:reference",		res.at("/query/@type").asText());
		assertEquals("operation:focus",		res.at("/query/operation").asText());
		assertEquals(129,					res.at("/query/classRef/0").asInt());
		assertEquals("korap:group",			res.at("/query/operands/0/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operands/0/operation").asText());
		assertEquals("korap:distance",		res.at("/query/operands/0/distances/0/@type").asText());
		assertEquals("w",					res.at("/query/operands/0/distances/0/key").asText());
		assertEquals(1,						res.at("/query/operands/0/distances/0/boundary/min").asInt());
		assertEquals(4,						res.at("/query/operands/0/distances/0/boundary/max").asInt());
		assertEquals(true,					res.at("/query/operands/0/inOrder").asBoolean());
		assertEquals("korap:group",			res.at("/query/operands/0/operands/0/@type").asText());
		assertEquals("operation:class",		res.at("/query/operands/0/operands/0/operation").asText());
		assertEquals(129,					res.at("/query/operands/0/operands/0/classOut").asInt());
		assertEquals("Sonne",				res.at("/query/operands/0/operands/0/operands/0/wrap/key").asText());
		assertEquals(129,					res.at("/query/operands/0/operands/1/classOut").asInt());
		assertEquals("korap:reference",		res.at("/query/operands/0/operands/1/operands/0/@type").asText());
		assertEquals(130,					res.at("/query/operands/0/operands/1/operands/0/classRef/0").asInt());
		assertEquals("operation:focus",		res.at("/query/operands/0/operands/1/operands/0/operation").asText());
		assertEquals("operation:sequence",	res.at("/query/operands/0/operands/1/operands/0/operands/0/operation").asText());
		assertEquals("w",					res.at("/query/operands/0/operands/1/operands/0/operands/0/distances/0/key").asText());
		assertEquals(1,						res.at("/query/operands/0/operands/1/operands/0/operands/0/distances/0/boundary/min").asInt());
		assertEquals(7,						res.at("/query/operands/0/operands/1/operands/0/operands/0/distances/0/boundary/max").asInt());
		assertEquals(130,					res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/0/classOut").asInt());
		assertEquals("Mond",				res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/0/operands/0/wrap/key").asText());
		assertEquals(130,					res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/1/classOut").asInt());
		assertEquals("Sterne",				res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/1/operands/0/wrap/key").asText());
		
		query = "Sonne /+w1:4 Mond /-w1:7 Sterne";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("Sonne",				res.at("/query/operands/0/operands/0/operands/0/wrap/key").asText());
		assertEquals("Sterne",				res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/0/operands/0/wrap/key").asText());
		assertEquals("Mond",				res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/1/operands/0/wrap/key").asText());
		
		query = "Sonne /-w4 Mond /+w2 Sterne";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:reference",		res.at("/query/@type").asText());
		assertEquals("operation:focus",		res.at("/query/operation").asText());
		assertEquals(129,					res.at("/query/classRef/0").asInt());
		assertEquals("korap:group",			res.at("/query/operands/0/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operands/0/operation").asText());
		assertEquals("korap:distance",		res.at("/query/operands/0/distances/0/@type").asText());
		assertEquals("w",					res.at("/query/operands/0/distances/0/key").asText());
		assertEquals(0,						res.at("/query/operands/0/distances/0/boundary/min").asInt());
		assertEquals(4,						res.at("/query/operands/0/distances/0/boundary/max").asInt());
		assertEquals(true,					res.at("/query/operands/0/inOrder").asBoolean());
		assertEquals("korap:group",			res.at("/query/operands/0/operands/1/@type").asText());
		assertEquals("operation:class",		res.at("/query/operands/0/operands/1/operation").asText());
		assertEquals(129,					res.at("/query/operands/0/operands/1/classOut").asInt());
		assertEquals("Sonne",				res.at("/query/operands/0/operands/1/operands/0/wrap/key").asText());
		assertEquals(129,					res.at("/query/operands/0/operands/0/classOut").asInt());
		assertEquals("korap:reference",		res.at("/query/operands/0/operands/0/operands/0/@type").asText());
		assertEquals(130,					res.at("/query/operands/0/operands/0/operands/0/classRef/0").asInt());
		assertEquals("operation:focus",		res.at("/query/operands/0/operands/0/operands/0/operation").asText());
		assertEquals("operation:sequence",	res.at("/query/operands/0/operands/0/operands/0/operands/0/operation").asText());
		assertEquals("w",					res.at("/query/operands/0/operands/0/operands/0/operands/0/distances/0/key").asText());
		assertEquals(0,						res.at("/query/operands/0/operands/0/operands/0/operands/0/distances/0/boundary/min").asInt());
		assertEquals(2,						res.at("/query/operands/0/operands/0/operands/0/operands/0/distances/0/boundary/max").asInt());
		assertEquals(130,					res.at("/query/operands/0/operands/0/operands/0/operands/0/operands/0/classOut").asInt());
		assertEquals("Mond",				res.at("/query/operands/0/operands/0/operands/0/operands/0/operands/0/operands/0/wrap/key").asText());
		assertEquals(130,					res.at("/query/operands/0/operands/0/operands/0/operands/0/operands/1/classOut").asInt());
		assertEquals("Sterne",				res.at("/query/operands/0/operands/0/operands/0/operands/0/operands/1/operands/0/wrap/key").asText());

	}
	
	@Test
	public void testOPIN() throws QueryException, JsonProcessingException, IOException {
		query = "wegen #IN <s>";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:reference",				res.at("/query/@type").asText());
		assertEquals("operation:focus",				res.at("/query/operation").asText());
		assertEquals(130,							res.at("/query/classRef/0").asInt());
		assertEquals("korap:group",					res.at("/query/operands/0/@type").asText());
		assertEquals("operation:class",				res.at("/query/operands/0/operation").asText());
		assertEquals("classRefCheck:includes",		res.at("/query/operands/0/classRefCheck").asText());
		assertEquals("korap:group",					res.at("/query/operands/0/operands/0/@type").asText());
		assertEquals("operation:position",			res.at("/query/operands/0/operands/0/operation").asText());
		assertEquals(true,							res.at("/query/operands/0/operands/0/frames/0").isMissingNode());
		assertEquals(129,							res.at("/query/operands/0/classIn/0").asInt());
		assertEquals(130,							res.at("/query/operands/0/classIn/1").asInt());
		assertEquals("korap:group",					res.at("/query/operands/0/operands/0/@type").asText());
		assertEquals("operation:class",				res.at("/query/operands/0/operands/0/operands/0/operation").asText());
		assertEquals(129,							res.at("/query/operands/0/operands/0/operands/0/classOut").asInt());
		assertEquals("korap:span",					res.at("/query/operands/0/operands/0/operands/0/operands/0/@type").asText());
		assertEquals("s",							res.at("/query/operands/0/operands/0/operands/0/operands/0/key").asText());
		assertEquals("korap:group",					res.at("/query/operands/0/operands/0/operands/1/@type").asText());
		assertEquals("operation:class",				res.at("/query/operands/0/operands/0/operands/1/operation").asText());
		assertEquals(130,							res.at("/query/operands/0/operands/0/operands/1/classOut").asInt());
		assertEquals("korap:token",					res.at("/query/operands/0/operands/0/operands/1/operands/0/@type").asText());
		assertEquals("wegen",						res.at("/query/operands/0/operands/0/operands/1/operands/0/wrap/key").asText());
		
		query = "wegen #IN(L) <s>";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("classRefCheck:includes",		res.at("/query/operands/0/classRefCheck").asText());
		assertEquals("frames:startswith",			res.at("/query/operands/0/operands/0/frames/0").asText());
		assertEquals(true,							res.at("/query/operands/0/operands/0/frames/1").isMissingNode());
		
		query = "wegen #IN(F) <s>";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("classRefCheck:includes",		res.at("/query/operands/0/classRefCheck").asText());
		assertEquals("frames:matches",				res.at("/query/operands/0/operands/0/frames/0").asText());
		assertEquals(true,							res.at("/query/operands/0/operands/0/frames/1").isMissingNode());
		
		query = "wegen #IN(FI) <s>";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("classRefCheck:unequals",		res.at("/query/operands/0/classRefCheck/0").asText());
		assertEquals("classRefCheck:includes",		res.at("/query/operands/0/classRefCheck/1").asText());
		assertEquals("frames:matches",				res.at("/query/operands/0/operands/0/frames/0").asText());
		assertEquals(true,							res.at("/query/operands/0/operands/0/frames/1").isMissingNode());
		
		query = "wegen #IN(FE) <s>";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("classRefCheck:equals",		res.at("/query/operands/0/classRefCheck").asText());
		assertEquals("frames:matches",				res.at("/query/operands/0/operands/0/frames/0").asText());
		assertEquals(true,							res.at("/query/operands/0/operands/0/frames/1").isMissingNode());
		
		query = "wegen #IN(%, L) <s>";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("classRefCheck:includes",		res.at("/query/operands/0/classRefCheck").asText());
		assertEquals("frames:startswith",			res.at("/query/operands/0/operands/0/frames/0").asText());
		assertEquals(true,							res.at("/query/operands/0/operands/0/frames/1").isMissingNode());
		assertEquals(true,							res.at("/query/operands/0/operands/0/exclude").asBoolean());
		
		query = "wegen #IN(FE,ALL,%,MIN) <s>";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals(true,							res.at("/query/reset").asBoolean());
		assertEquals("classRefCheck:equals",		res.at("/query/operands/0/classRefCheck").asText());
		assertEquals("frames:matches",				res.at("/query/operands/0/operands/0/frames/0").asText());
		assertEquals(true,							res.at("/query/operands/0/operands/0/exclude").asBoolean());
		
	}
	
	@Test
	public void testOPOV() throws QueryException, JsonProcessingException, IOException {
		query = "wegen #OV <s>";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:reference",				res.at("/query/@type").asText());
		assertEquals("operation:focus",				res.at("/query/operation").asText());
		assertEquals(130,							res.at("/query/classRef/0").asInt());
		assertEquals("korap:group",					res.at("/query/operands/0/@type").asText());
		assertEquals("operation:class",				res.at("/query/operands/0/operation").asText());
		assertEquals("classRefCheck:intersects",	res.at("/query/operands/0/classRefCheck").asText());
		assertEquals("korap:group",					res.at("/query/operands/0/operands/0/@type").asText());
		assertEquals("operation:position",			res.at("/query/operands/0/operands/0/operation").asText());
		assertEquals(true,							res.at("/query/operands/0/operands/0/frames/0").isMissingNode());
		assertEquals(129,							res.at("/query/operands/0/classIn/0").asInt());
		assertEquals(130,							res.at("/query/operands/0/classIn/1").asInt());
		assertEquals("korap:group",					res.at("/query/operands/0/operands/0/@type").asText());
		assertEquals("operation:class",				res.at("/query/operands/0/operands/0/operands/0/operation").asText());
		assertEquals(129,							res.at("/query/operands/0/operands/0/operands/0/classOut").asInt());
		assertEquals("korap:span",					res.at("/query/operands/0/operands/0/operands/0/operands/0/@type").asText());
		assertEquals("s",							res.at("/query/operands/0/operands/0/operands/0/operands/0/key").asText());
		assertEquals("korap:group",					res.at("/query/operands/0/operands/0/operands/1/@type").asText());
		assertEquals("operation:class",				res.at("/query/operands/0/operands/0/operands/1/operation").asText());
		assertEquals(130,							res.at("/query/operands/0/operands/0/operands/1/classOut").asInt());
		assertEquals("korap:token",					res.at("/query/operands/0/operands/0/operands/1/operands/0/@type").asText());
		assertEquals("wegen",						res.at("/query/operands/0/operands/0/operands/1/operands/0/wrap/key").asText());
		
		query = "wegen #OV(L) <s>";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("classRefCheck:intersects",	res.at("/query/operands/0/classRefCheck").asText());
		assertEquals("frames:startswith",			res.at("/query/operands/0/operands/0/frames/0").asText());
		assertEquals("frames:overlapsLeft",			res.at("/query/operands/0/operands/0/frames/1").asText());
		
		query = "wegen #OV(F) <s>";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("classRefCheck:intersects",	res.at("/query/operands/0/classRefCheck").asText());
		assertEquals("frames:matches",				res.at("/query/operands/0/operands/0/frames/0").asText());
		assertEquals(true,							res.at("/query/operands/0/operands/0/frames/1").isMissingNode());
		
		query = "wegen #OV(FI) <s>";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("classRefCheck:unequals",		res.at("/query/operands/0/classRefCheck").asText());
		assertEquals("frames:matches",				res.at("/query/operands/0/operands/0/frames/0").asText());
		
		query = "wegen #OV(FE) <s>";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("classRefCheck:equals",		res.at("/query/operands/0/classRefCheck").asText());
		assertEquals("frames:matches",				res.at("/query/operands/0/operands/0/frames/0").asText());
	}

	
	@Test
	public void testBEG_END() throws QueryException, JsonProcessingException, IOException {
		query = "#BEG(der /w3:5 Mann)";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:reference",		res.at("/query/@type").asText());
		assertEquals("operation:focus",		res.at("/query/operation").asText());
		assertEquals(0,						res.at("/query/spanRef/0").asInt());
		assertEquals(1,						res.at("/query/spanRef/1").asInt());
		assertEquals("korap:group",			res.at("/query/operands/0/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operands/0/operation").asText());
		assertEquals("korap:distance",		res.at("/query/operands/0/distances/0/@type").asText());
		assertEquals("w",					res.at("/query/operands/0/distances/0/key").asText());
		assertEquals(3,						res.at("/query/operands/0/distances/0/boundary/min").asInt());
		assertEquals(5,						res.at("/query/operands/0/distances/0/boundary/max").asInt());
		assertEquals(false,					res.at("/query/operands/0/inOrder").asBoolean());
		assertEquals("korap:token",			res.at("/query/operands/0/operands/0/@type").asText());
		assertEquals("der",					res.at("/query/operands/0/operands/0/wrap/key").asText());
		assertEquals("Mann",				res.at("/query/operands/0/operands/1/wrap/key").asText());
		
		query = "#BEG(der /w3:5 Mann) /+w10 kommt";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:reference",		res.at("/query/@type").asText());
		assertEquals("operation:focus",		res.at("/query/operation").asText());
		assertEquals(129,					res.at("/query/classRef/0").asInt());
		assertEquals("korap:group",			res.at("/query/operands/0/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operands/0/operation").asText());
		assertEquals("korap:distance",		res.at("/query/operands/0/distances/0/@type").asText());
		assertEquals("w",					res.at("/query/operands/0/distances/0/key").asText());
		assertEquals(0,						res.at("/query/operands/0/distances/0/boundary/min").asInt());
		assertEquals(10,					res.at("/query/operands/0/distances/0/boundary/max").asInt());
		assertEquals(true,					res.at("/query/operands/0/inOrder").asBoolean());
		assertEquals("korap:group",			res.at("/query/operands/0/operands/0/@type").asText());
		assertEquals("operation:class",		res.at("/query/operands/0/operands/0/operation").asText());
		assertEquals(129,					res.at("/query/operands/0/operands/0/classOut").asInt());
		assertEquals("korap:reference",		res.at("/query/operands/0/operands/0/operands/0/@type").asText());
		assertEquals("operation:focus",		res.at("/query/operands/0/operands/0/operands/0/operation").asText());
		assertEquals(0,						res.at("/query/operands/0/operands/0/operands/0/spanRef/0").asInt());
		assertEquals(1,						res.at("/query/operands/0/operands/0/operands/0/spanRef/1").asInt());
		assertEquals("korap:group",			res.at("/query/operands/0/operands/0/operands/0/operands/0/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operands/0/operands/0/operands/0/operands/0/operation").asText());
		assertEquals("korap:distance",		res.at("/query/operands/0/operands/0/operands/0/operands/0/distances/0/@type").asText());
		assertEquals("w",					res.at("/query/operands/0/operands/0/operands/0/operands/0/distances/0/key").asText());
		assertEquals(3,						res.at("/query/operands/0/operands/0/operands/0/operands/0/distances/0/boundary/min").asInt());
		assertEquals(5,						res.at("/query/operands/0/operands/0/operands/0/operands/0/distances/0/boundary/max").asInt());
		assertEquals(false,					res.at("/query/operands/0/operands/0/operands/0/operands/0/inOrder").asBoolean());
		assertEquals("korap:token",			res.at("/query/operands/0/operands/0/operands/0/operands/0/operands/0/@type").asText());
		assertEquals("der",					res.at("/query/operands/0/operands/0/operands/0/operands/0/operands/0/wrap/key").asText());
		assertEquals("Mann",				res.at("/query/operands/0/operands/0/operands/0/operands/0/operands/1/wrap/key").asText());
		assertEquals("operation:class",		res.at("/query/operands/0/operands/1/operation").asText());
		assertEquals(129,					res.at("/query/operands/0/operands/1/classOut").asInt());
		assertEquals("korap:token",			res.at("/query/operands/0/operands/1/operands/0/@type").asText());
		assertEquals("kommt",				res.at("/query/operands/0/operands/1/operands/0/wrap/key").asText());
		
		query = "kommt /+w10 #BEG(der /w3:5 Mann)";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:reference",		res.at("/query/@type").asText());
		assertEquals("operation:focus",		res.at("/query/operation").asText());
		assertEquals(129,					res.at("/query/classRef/0").asInt());
		assertEquals("korap:group",			res.at("/query/operands/0/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operands/0/operation").asText());
		assertEquals("korap:distance",		res.at("/query/operands/0/distances/0/@type").asText());
		assertEquals("w",					res.at("/query/operands/0/distances/0/key").asText());
		assertEquals(0,						res.at("/query/operands/0/distances/0/boundary/min").asInt());
		assertEquals(10,					res.at("/query/operands/0/distances/0/boundary/max").asInt());
		assertEquals(true,					res.at("/query/operands/0/inOrder").asBoolean());
		assertEquals("korap:group",			res.at("/query/operands/0/operands/1/@type").asText());
		assertEquals("operation:class",		res.at("/query/operands/0/operands/1/operation").asText());
		assertEquals(129,					res.at("/query/operands/0/operands/1/classOut").asInt());
		assertEquals("korap:reference",		res.at("/query/operands/0/operands/1/operands/0/@type").asText());
		assertEquals("operation:focus",		res.at("/query/operands/0/operands/1/operands/0/operation").asText());
		assertEquals(0,						res.at("/query/operands/0/operands/1/operands/0/spanRef/0").asInt());
		assertEquals(1,						res.at("/query/operands/0/operands/1/operands/0/spanRef/1").asInt());
		assertEquals("korap:group",			res.at("/query/operands/0/operands/1/operands/0/operands/0/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operands/0/operands/1/operands/0/operands/0/operation").asText());
		assertEquals("korap:distance",		res.at("/query/operands/0/operands/1/operands/0/operands/0/distances/0/@type").asText());
		assertEquals("w",					res.at("/query/operands/0/operands/1/operands/0/operands/0/distances/0/key").asText());
		assertEquals(3,						res.at("/query/operands/0/operands/1/operands/0/operands/0/distances/0/boundary/min").asInt());
		assertEquals(5,						res.at("/query/operands/0/operands/1/operands/0/operands/0/distances/0/boundary/max").asInt());
		assertEquals(false,					res.at("/query/operands/0/operands/1/operands/0/operands/0/inOrder").asBoolean());
		assertEquals("korap:token",			res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/0/@type").asText());
		assertEquals("der",					res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/0/wrap/key").asText());
		assertEquals("Mann",				res.at("/query/operands/0/operands/1/operands/0/operands/0/operands/1/wrap/key").asText());
		assertEquals("operation:class",		res.at("/query/operands/0/operands/0/operation").asText());
		assertEquals(129,					res.at("/query/operands/0/operands/0/classOut").asInt());
		assertEquals("korap:token",			res.at("/query/operands/0/operands/0/operands/0/@type").asText());
		assertEquals("kommt",				res.at("/query/operands/0/operands/0/operands/0/wrap/key").asText());
		
		query = "#END(der /w3:5 Mann)";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:reference",		res.at("/query/@type").asText());
		assertEquals("operation:focus",		res.at("/query/operation").asText());
		assertEquals(-1,					res.at("/query/spanRef/0").asInt());
		assertEquals(1,						res.at("/query/spanRef/1").asInt());
		assertEquals("korap:group",			res.at("/query/operands/0/@type").asText());
		assertEquals("operation:sequence",	res.at("/query/operands/0/operation").asText());
		assertEquals("korap:distance",		res.at("/query/operands/0/distances/0/@type").asText());
		assertEquals("w",					res.at("/query/operands/0/distances/0/key").asText());
		assertEquals(3,						res.at("/query/operands/0/distances/0/boundary/min").asInt());
		assertEquals(5,						res.at("/query/operands/0/distances/0/boundary/max").asInt());
		assertEquals(false,					res.at("/query/operands/0/inOrder").asBoolean());
		assertEquals("korap:token",			res.at("/query/operands/0/operands/0/@type").asText());
		assertEquals("der",					res.at("/query/operands/0/operands/0/wrap/key").asText());
		assertEquals("Mann",				res.at("/query/operands/0/operands/1/wrap/key").asText());
	}
	
	@Test
	public void testELEM() throws QueryException, JsonProcessingException, IOException {
		query = "#ELEM(S)";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:span",		res.at("/query/@type").asText());
		assertEquals("s",				res.at("/query/key").asText());
		
		query = "#ELEM(W ANA=N)";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:span",		res.at("/query/@type").asText());
		assertEquals("w",				res.at("/query/key").asText());
		assertEquals("korap:term",		res.at("/query/attr/@type").asText());
		assertEquals("N",				res.at("/query/attr/key").asText());
		assertEquals("p",				res.at("/query/attr/layer").asText());
		assertEquals("match:eq",		res.at("/query/attr/match").asText());
		
		query = "#ELEM(W ANA != 'N V')";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:span",		res.at("/query/@type").asText());
		assertEquals("w",				res.at("/query/key").asText());
		assertEquals("korap:termGroup",	res.at("/query/attr/@type").asText());
		assertEquals("relation:and",	res.at("/query/attr/relation").asText());
		assertEquals("korap:term",		res.at("/query/attr/operands/0/@type").asText());
		assertEquals("N",				res.at("/query/attr/operands/0/key").asText());
		assertEquals("p",				res.at("/query/attr/operands/0/layer").asText());
		assertEquals("match:ne",		res.at("/query/attr/operands/0/match").asText());
		assertEquals("korap:term",		res.at("/query/attr/operands/1/@type").asText());
		assertEquals("V",				res.at("/query/attr/operands/1/key").asText());
		assertEquals("p",				res.at("/query/attr/operands/1/layer").asText());
		assertEquals("match:ne",		res.at("/query/attr/operands/1/match").asText());
		
		query = "#ELEM(W ANA != 'N A V' Genre = Sport)";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:span",		res.at("/query/@type").asText());
		assertEquals("w",				res.at("/query/key").asText());
		assertEquals("korap:termGroup",	res.at("/query/attr/@type").asText());
		assertEquals("relation:and",	res.at("/query/attr/relation").asText());
		assertEquals("korap:termGroup",	res.at("/query/attr/operands/0/@type").asText());
		assertEquals("relation:and",	res.at("/query/attr/operands/0/relation").asText());
		assertEquals("N",				res.at("/query/attr/operands/0/operands/0/key").asText());
		assertEquals("A",				res.at("/query/attr/operands/0/operands/1/key").asText());
		assertEquals("V",				res.at("/query/attr/operands/0/operands/2/key").asText());
		assertEquals("Genre",			res.at("/query/attr/operands/1/layer").asText());
		assertEquals("Sport",			res.at("/query/attr/operands/1/key").asText());
		
		query = "#ELEM(W ANA != 'N A V' Genre != 'Sport Politik')";
		qs.setQuery(query, "cosmas2");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:span",		res.at("/query/@type").asText());
		assertEquals("w",				res.at("/query/key").asText());
		assertEquals("korap:termGroup",	res.at("/query/attr/@type").asText());
		assertEquals("relation:and",	res.at("/query/attr/relation").asText());
		assertEquals("korap:termGroup",	res.at("/query/attr/operands/0/@type").asText());
		assertEquals("relation:and",	res.at("/query/attr/operands/0/relation").asText());
		assertEquals("korap:termGroup",	res.at("/query/attr/operands/1/@type").asText());
		assertEquals("relation:and",	res.at("/query/attr/operands/1/relation").asText());
		assertEquals("N",				res.at("/query/attr/operands/0/operands/0/key").asText());
		assertEquals("A",				res.at("/query/attr/operands/0/operands/1/key").asText());
		assertEquals("V",				res.at("/query/attr/operands/0/operands/2/key").asText());
		assertEquals("match:ne",		res.at("/query/attr/operands/0/operands/2/match").asText());
		assertEquals("Genre",			res.at("/query/attr/operands/1/operands/0/layer").asText());
		assertEquals("Sport",			res.at("/query/attr/operands/1/operands/0/key").asText());
		assertEquals("Genre",			res.at("/query/attr/operands/1/operands/1/layer").asText());
		assertEquals("Politik",			res.at("/query/attr/operands/1/operands/1/key").asText());
		assertEquals("match:ne",		res.at("/query/attr/operands/1/operands/1/match").asText());
	}
//	@Test
//	public void testOPALL() throws QueryException {
//		query="#ALL(gehen /w1:10 voran)";
//		String all1 =
//				"{@type=korap:group, operation=operation:sequence, " +
//					"operands=[" +
//						"{@type=korap:token, wrap={@type=korap:term, key=gehen, layer=orth, match=match:eq}}," +
//						"{@type=korap:token, wrap={@type=korap:term, key=voran, layer=orth, match=match:eq}}" +
//					"], inOrder=false, " +
//					"distances=[" +
//						"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=1, max=10}, min=1, max=10}" +
//					"]" +
//				"}";
//		ct = new CosmasTree(query);
//		map = ct.getRequestMap().get("query").toString();
//		assertEquals(all1.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		query="#ALL(gehen /w1:10 (voran /w1:4 schnell))";
//		String all2 =
//				"{@type=korap:group, operation=operation:sequence, " +
//					"operands=[" +
//						"{@type=korap:token, wrap={@type=korap:term, key=gehen, layer=orth, match=match:eq}}," +
//							"{@type=korap:group, operation=operation:sequence, operands=[" +
//									"{@type=korap:token, wrap={@type=korap:term, key=voran, layer=orth, match=match:eq}}," +
//									"{@type=korap:token, wrap={@type=korap:term, key=schnell, layer=orth, match=match:eq}}" +
//								"], inOrder=false, " +
//								"distances=[" +
//									"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=1, max=4}, min=1, max=4}" +
//								"]" +
//							"}" +
//					"], inOrder=false, " +
//					"distances=[" +
//						"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=1, max=10}, min=1, max=10}" +
//					"]" +
//				"}";
//		ct = new CosmasTree(query);
//		map = ct.getRequestMap().get("query").toString();
//		assertEquals(all2.replaceAll(" ", ""), map.replaceAll(" ", ""));
//	}
//	
//	@Test
//	public void testOPNHIT() throws QueryException {
//		query="#NHIT(gehen /w1:10 voran)";
//		String nhit1 = 
//				"{@type=korap:reference, operation=operation:focus, classRef=[129], operands=[" +
//					"{@type=korap:group, operation=operation:class, classRefOp=classRefOp:inversion, classIn=[130,131], classOut=129, operands=[" +
//						"{@type=korap:group, operation=operation:sequence, " +
//							"operands=[" +
//								"{@type=korap:group, operation=operation:class, class=130 , classOut=130, operands=[" +
//									"{@type=korap:token, wrap={@type=korap:term, key=gehen, layer=orth, match=match:eq}}" +
//								"]}," +	
//								"{@type=korap:group, operation=operation:class, class=131 , classOut=131, operands=[" +
//									"{@type=korap:token, wrap={@type=korap:term, key=voran, layer=orth, match=match:eq}}" +
//								"]}" +	
//							"], inOrder=false, " +
//							"distances=[" +
//								"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=1, max=10}, min=1, max=10}" +
//							"]" +
//						"}" +
//					"]}" +
//				"]}";
//		ct = new CosmasTree(query);
//		map = ct.getRequestMap().get("query").toString();
//		assertEquals(nhit1.replaceAll(" ", ""), map.replaceAll(" ", ""));
//
////		query="#NHIT(gehen %w1:10 voran)";
////		String nhit2 = 
////				"{@type=korap:reference, operation=operation:focus, classRef=129, operands=[" +
////					"{@type=korap:group, operation=operation:sequence, " +
////						"operands=[" +
////							"{@type=korap:token, wrap={@type=korap:term, key=gehen, layer=orth, match=match:eq}}" +
////							"{@type=korap:group, operation=operation:class, class= , classOut=129, operands=[" +
////								"{@type=korap:group, operation=operation:repetition, operands=[" +
////									"{@type=korap:token}" +
////								"], boundary={@type=korap:boundary, min=1, max=10}, min=1, max=10}}" +
////							"]}," +	
////							"{@type=korap:token, wrap={@type=korap:term, key=voran, layer=orth, match=match:eq}}" +
////						"], inOrder=false, " +
////						"distances=[" +
////							"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=1, max=10}, min=1, max=10}" +
////						"]" +
////					"}" +
////				"]}";
////		ct = new CosmasTree(query);
////		map = ct.getRequestMap().get("query").toString();
////		assertEquals(nhit2.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		query="#NHIT(gehen /+w1:10 voran /w1:10 Beispiel)";
//		String nhit3 = 
//				"{@type=korap:reference, operation=operation:focus, classRef=[129], operands=[" +
//					"{@type=korap:group, operation=operation:class, classRefOp=classRefOp:inversion, classIn=[130,131], classOut=129, operands=[" +
//						"{@type=korap:group, operation=operation:sequence, " +
//							"operands=[" +
//								"{@type=korap:group, operation=operation:class, class=130, classOut=130, operands=[" +
//									"{@type=korap:token, wrap={@type=korap:term, key=gehen, layer=orth, match=match:eq}}" +
//								"]}," +
//								"{@type=korap:group, operation=operation:class, class=131, classOut=131, operands=[" +
//									"{@type=korap:reference, operation=operation:focus, classRef=[132], operands=[" +
//										"{@type=korap:group, operation=operation:sequence, " +
//											"operands=[" +
//												"{@type=korap:group, operation=operation:class, class=132, classOut=132, operands=[" +
//													"{@type=korap:token, wrap={@type=korap:term, key=voran, layer=orth, match=match:eq}}" +
//												"]}," +
//												"{@type=korap:group, operation=operation:class, class=132, classOut=132, operands=[" +	
//													"{@type=korap:token, wrap={@type=korap:term, key=Beispiel, layer=orth, match=match:eq}}" +
//												"]}" +
//											"], inOrder=false, " +
//											"distances=[" +
//												"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=1, max=10}, min=1, max=10}" +
//											"]" +
//										"}" +
//									"]}" +
//								"]}" +
//							"], inOrder=true, " +
//							"distances=[" +
//								"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=1, max=10}, min=1, max=10}" +
//							"]" +
//						"}" +
//					"]}" +
//				"]}";
//		ct = new CosmasTree(query);
//		map = ct.getRequestMap().get("query").toString();
//		assertEquals(nhit3.replaceAll(" ", ""), map.replaceAll(" ", ""));
//	}
//	
//	@Test
//	public void testOPBED() throws QueryException {
//		query = "#BED(der , sa)";
//		String bed1 = 
//				"{@type=korap:reference, operation=operation:focus, classRef=[129], operands= [" +
//					"{@type=korap:group, operation=operation:position, frames=[frames:startswith], operands=[" +
//						"{@type=korap:span, key=s}," +
//						"{@type=korap:group, operation=operation:class, class=129, classOut=129, operands=[" +
//							"{@type=korap:token, wrap={@type=korap:term, key=der, layer=orth, match=match:eq}}" +
//						"]}" +
//					"], frame=frame:startswith}" +
//				"]}";
//		ct = new CosmasTree(query);
//		map = ct.getRequestMap().get("query").toString();
//		assertEquals(bed1.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		query = "#BED(der Mann , +pe)";
//		String bed2 = 
//				"{@type=korap:reference, operation=operation:focus, classRef=[129], operands= [" +
//						"{@type=korap:group, operation=operation:position, frames=[frames:matches], operands=[" +
//							"{@type=korap:reference, operation=operation:focus, spanRef=[-1,1], operands=[" +
//								"{@type=korap:span, key=p}" +
//							"]}," +
//							"{@type=korap:reference, operation=operation:focus, spanRef=[0,1], operands=[" +
//								"{@type=korap:group, operation=operation:class, class=129, classOut=129, operands=[" +
//									"{@type=korap:group, operation=operation:sequence, operands=[" +
//										"{@type=korap:token, wrap={@type=korap:term, key=der, layer=orth, match=match:eq}}," +
//										"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}" +
//									"]}" +
//								"]}" +
//							"]}" +
//						"], frame=frame:matches}" +
//					"]}";
//		ct = new CosmasTree(query);
//		map = ct.getRequestMap().get("query").toString();
//		assertEquals(bed2.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		query = "#BED(der Mann , sa,-pa)";
//		String bed3 = 
//				"{@type=korap:reference, operation=operation:focus, classRef=[129], operands=[" +
//					"{@type=korap:group, operation=operation:position, frames=[frames:matches], operands=[" +
//						"{@type=korap:group, operation=operation:position, frames=[frames:startswith], operands=[" +
//							"{@type=korap:span, key=s}," +
//							"{@type=korap:group, operation=operation:class, class=129, classOut=129, operands=[" +
//								"{@type=korap:group, operation=operation:sequence, operands=[" +
//									"{@type=korap:token, wrap={@type=korap:term, key=der, layer=orth, match=match:eq}}," +
//									"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}" +
//								"]}" +
//							"]}" +
//						"], frame=frame:startswith}," +
//						"{@type=korap:group, operation=operation:position, frames=[frames:startswith], operands=[" +
//							"{@type=korap:span, key=p}," +
//							"{@type=korap:group, operation=operation:class, class=130, classOut=130, operands=[" +
//								"{@type=korap:group, operation=operation:sequence, operands=[" +
//									"{@type=korap:token, wrap={@type=korap:term, key=der, layer=orth, match=match:eq}}," +
//									"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}" +
//								"]}" +
//							"]}" +
//						"], frame=frame:startswith, exclude=true}" +
//					"], frame=frame:matches}" +
//				"]}";
//		ct = new CosmasTree(query);
//		map = ct.getRequestMap().get("query").toString();
//		assertEquals(bed3.replaceAll(" ", ""), map.replaceAll(" ", ""));
//	}
//	
//	@Test
//	public void testColonSeparatedConditions() throws QueryException {
//		
//		query = "Der:sa";
//		String col1 = 
//				"{@type=korap:reference, operation=operation:focus, classRef=[129], operands=[" +
//					"{@type=korap:group, operation=operation:position, frames=[frames:startswith], operands=[" +
//						"{@type=korap:span, key=s}," +
//						"{@type=korap:group, operation=operation:class, class=129, classOut=129, operands=[" +
//							"{@type=korap:token, wrap={@type=korap:term, key=Der, layer=orth, match=match:eq}}" +
//						"]}" +
//					"], frame=frame:startswith}" +
//				"]}";
//		ct = new CosmasTree(query);
//		map = ct.getRequestMap().get("query").toString();
//		assertEquals(col1.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		query = "Mann:sa,-pa,+te";
//		String col2 = 
//				"{@type=korap:reference, operation=operation:focus, classRef=[129], operands=[" +
//					"{@type=korap:group, operation=operation:position, frames=[frames:matches], operands=[" +
//						"{@type=korap:group, operation=operation:position, frames=[frames:startswith], operands=[" +
//							"{@type=korap:span, key=s}," +
//							"{@type=korap:group, operation=operation:class, class=129, classOut=129, operands=[" +
//									"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}" +
//							"]}" +
//						"], frame=frame:startswith}," +
//						"{@type=korap:reference, operation=operation:focus, classRef=[130], operands=[" +
//							"{@type=korap:group, operation=operation:position, frames=[frames:matches], operands=[" +	
//								"{@type=korap:group, operation=operation:position, frames=[frames:startswith], operands=[" +
//									"{@type=korap:span, key=p}," +
//									"{@type=korap:group, operation=operation:class, class=130, classOut=130, operands=[" +
//											"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}" +
//									"]}" +
//								"], frame=frame:startswith, exclude=true}," +
//								"{@type=korap:group, operation=operation:position, frames=[frames:matches], operands=[" +
//									"{@type=korap:reference, operation=operation:focus, spanRef=[-1,1], operands=[" +
//										"{@type=korap:span, key=t}" +
//									"]}," +
//									"{@type=korap:reference, operation=operation:focus, spanRef=[0,1], operands=[" +
//										"{@type=korap:group, operation=operation:class, class=131, classOut=131, operands=[" +
//												"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}" +
//										"]}" +
//									"]}" +
//								"], frame=frame:matches}" +
//							"], frame=frame:matches}" +
//						"]}" +
//					"], frame=frame:matches}" +
//				"]}";
//		ct = new CosmasTree(query);
//		map = ct.getRequestMap().get("query").toString();
//		assertEquals(col2.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		query = "Mann:sa,-pa,+te,se";
//		expected = 
//				"{@type=korap:reference, operation=operation:focus, classRef=[129], operands=[" +
//					"{@type=korap:group, operation=operation:position, frames=[frames:matches], operands=[" +
//						"{@type=korap:group, operation=operation:position, frames=[frames:startswith], operands=[" +
//							"{@type=korap:span, key=s}," +
//							"{@type=korap:group, operation=operation:class, class=129, classOut=129, operands=[" +
//									"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}" +
//							"]}" +
//						"], frame=frame:startswith}," +
//						"{@type=korap:reference, operation=operation:focus, classRef=[130], operands=[" +
//							"{@type=korap:group, operation=operation:position, frames=[frames:matches], operands=[" +	
//								"{@type=korap:group, operation=operation:position, frames=[frames:startswith], operands=[" +
//									"{@type=korap:span, key=p}," +
//									"{@type=korap:group, operation=operation:class, class=130, classOut=130, operands=[" +
//											"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}" +
//									"]}" +
//								"], frame=frame:startswith, exclude=true}," +
//								"{@type=korap:reference, operation=operation:focus, classRef=[131], operands=[" +
//									"{@type=korap:group, operation=operation:position, frames=[frames:matches], operands=[" +	
//										"{@type=korap:group, operation=operation:position, frames=[frames:matches], operands=[" +
//											"{@type=korap:reference, operation=operation:focus, spanRef=[-1,1], operands=[" +
//												"{@type=korap:span, key=t}" +
//											"]}," +
//											"{@type=korap:reference, operation=operation:focus, spanRef=[0,1], operands=[" +
//												"{@type=korap:group, operation=operation:class, class=131, classOut=131, operands=[" +
//														"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}" +
//												"]}" +
//											"]}" +
//										"], frame=frame:matches}," +
//										"{@type=korap:group, operation=operation:position, frames=[frames:matches], operands=[" +
//											"{@type=korap:reference, operation=operation:focus, spanRef=[-1,1], operands=[" +
//												"{@type=korap:span, key=s}" +
//											"]}," +
//											"{@type=korap:reference, operation=operation:focus, spanRef=[0,1], operands=[" +
//												"{@type=korap:group, operation=operation:class, class=132, classOut=132, operands=[" +
//														"{@type=korap:token, wrap={@type=korap:term, key=Mann, layer=orth, match=match:eq}}" +
//												"]}" +
//											"]}" +
//										"], frame=frame:matches}" +
//									"], frame=frame:matches}" +
//								"]}" +
//							"], frame=frame:matches}" +
//						"]}" +
//					"], frame=frame:matches}" +
//				"]}";
//		ct = new CosmasTree(query);
//		map = ct.getRequestMap().get("query").toString();
//		assertEquals(expected.replaceAll(" ", ""), map.replaceAll(" ", ""));
//	}
}
	






