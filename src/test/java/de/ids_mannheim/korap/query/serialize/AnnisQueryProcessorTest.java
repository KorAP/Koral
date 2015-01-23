package de.ids_mannheim.korap.query.serialize;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ids_mannheim.korap.query.serialize.QuerySerializer;
import de.ids_mannheim.korap.query.serialize.util.StatusCodes;

/**
 * Tests for JSON-LD serialization of ANNIS QL queries. 
 * @author Joachim Bingel (bingel@ids-mannheim.de)
 * @version 1.0
 */
public class AnnisQueryProcessorTest {
	
	String query;
	ArrayList<JsonNode> operands;

	QuerySerializer qs = new QuerySerializer();
	ObjectMapper mapper = new ObjectMapper();
	JsonNode res;

	@Test
	public void testContext() throws JsonProcessingException, IOException {
		String contextUrl = "http://ids-mannheim.de/ns/KorAP/json-ld/v0.2/context.jsonld";
		query = "foo";
		qs.setQuery(query, "annis");
		res = mapper.readTree(qs.toJSON());
		assertEquals(contextUrl, res.get("@context").asText());
	}
	
	@Test
	public void testSingleTokens() throws JsonProcessingException, IOException {
		query = "\"Mann\"";
		qs.setQuery(query, "annis");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:token",			 res.at("/query/@type").asText());
		assertEquals("korap:term",			 res.at("/query/wrap/@type").asText());
		assertEquals("orth",				 res.at("/query/wrap/layer").asText());
		assertEquals("Mann",				 res.at("/query/wrap/key").asText());
		assertEquals("match:eq",			 res.at("/query/wrap/match").asText());
		
		query = "tok!=\"Frau\"";
		qs.setQuery(query, "annis");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:token",			 res.at("/query/@type").asText());
		assertEquals("korap:term",			 res.at("/query/wrap/@type").asText());
		assertEquals("orth",				 res.at("/query/wrap/layer").asText());
		assertEquals("Frau",				 res.at("/query/wrap/key").asText());
		assertEquals("match:ne",			 res.at("/query/wrap/match").asText());
		
		query = "tok";  // special keyword for token
		qs.setQuery(query, "annis");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:token",			 res.at("/query/@type").asText());
		
		query = "Mann"; // no special keyword -> defaults to layer name
		qs.setQuery(query, "annis");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:span",			 res.at("/query/@type").asText());
		assertEquals("Mann",				 res.at("/query/layer").asText());
	}
	
	@Test 
	public void testSpans() throws JsonProcessingException, IOException {
		query = "node"; // special keyword for general span
		qs.setQuery(query, "annis");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:span",			 res.at("/query/@type").asText());
		
		query = "cat=\"np\"";  // cat is special keyword for spans
		qs.setQuery(query, "annis");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:span",			 res.at("/query/@type").asText());
		assertEquals("np",					 res.at("/query/key").asText());
		assertEquals("c",					 res.at("/query/layer").asText());
		
		query = "cat=\"NP\"";  
		qs.setQuery(query, "annis");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:span",			 res.at("/query/@type").asText());
		assertEquals("NP",					 res.at("/query/key").asText());
		assertEquals("c",					 res.at("/query/layer").asText());
	}
	
	@Test
	public void testRegex() throws JsonProcessingException, IOException {
		query = "/Mann/";  
		qs.setQuery(query, "annis");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:token",			 res.at("/query/@type").asText());
		assertEquals("korap:term",			 res.at("/query/wrap/@type").asText());
		assertEquals("type:regex",			 res.at("/query/wrap/type").asText());
		assertEquals("orth",				 res.at("/query/wrap/layer").asText());
		assertEquals("Mann",				 res.at("/query/wrap/key").asText());
		assertEquals("match:eq",			 res.at("/query/wrap/match").asText());
		
		query = "/.*?Mann.*?/";  
		qs.setQuery(query, "annis");
		res = mapper.readTree(qs.toJSON());
		assertEquals("type:regex",			res.at("/query/wrap/type").asText());
		assertEquals(".*?Mann.*?",			res.at("/query/wrap/key").asText());
	}

	@Test
	public void testFoundriesLayers() throws JsonProcessingException, IOException {
		query = "c=\"np\"";  
		qs.setQuery(query, "annis");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:span",			 res.at("/query/@type").asText());
		assertEquals("np",					 res.at("/query/key").asText());
		assertEquals("c",					 res.at("/query/layer").asText());
		
		query = "cnx/c=\"np\"";  
		qs.setQuery(query, "annis");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:span",			 res.at("/query/@type").asText());
		assertEquals("np",					 res.at("/query/key").asText());
		assertEquals("c",					 res.at("/query/layer").asText());
		assertEquals("cnx",					 res.at("/query/foundry").asText());
		
		query = "tt/pos=\"np\"";  
		qs.setQuery(query, "annis");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:token",			 res.at("/query/@type").asText());
		assertEquals("korap:term",			 res.at("/query/wrap/@type").asText());
		assertEquals("np",					 res.at("/query/wrap/key").asText());
		assertEquals("p",					 res.at("/query/wrap/layer").asText());
		assertEquals("tt",					 res.at("/query/wrap/foundry").asText());
	}
	
	@Test
	public void testDirectDeclarationRelations() throws JsonProcessingException, IOException {
		query = "node > node";  
		qs.setQuery(query, "annis");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group",			res.at("/query/@type").asText());
		assertEquals("operation:relation",	res.at("/query/operation").asText());
		assertEquals("korap:span",			res.at("/query/operands/0/@type").asText());
		assertEquals("korap:span",			res.at("/query/operands/1/@type").asText());
		assertEquals("korap:relation",		res.at("/query/relation/@type").asText());
		assertEquals("korap:term",			res.at("/query/relation/wrap/@type").asText());
		assertEquals("c",					res.at("/query/relation/wrap/layer").asText());
		
		query = "node > cnx/c=\"np\"";  
		qs.setQuery(query, "annis");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group",			res.at("/query/@type").asText());
		assertEquals("operation:relation",	res.at("/query/operation").asText());
		assertEquals("korap:span",			res.at("/query/operands/0/@type").asText());
		assertEquals("korap:span",			res.at("/query/operands/1/@type").asText());
		assertEquals("np",					res.at("/query/operands/1/key").asText());
		assertEquals("c",					res.at("/query/operands/1/layer").asText());
		assertEquals("cnx",					res.at("/query/operands/1/foundry").asText());
		assertEquals("korap:relation",		res.at("/query/relation/@type").asText());
		assertEquals("korap:term",			res.at("/query/relation/wrap/@type").asText());
		assertEquals("c",					res.at("/query/relation/wrap/layer").asText());
		
		query = "cnx/c=\"np\" > node";  
		qs.setQuery(query, "annis");
		res = mapper.readTree(qs.toJSON());
		assertEquals(true,					res.at("/query/operands/1/key").isMissingNode());
		assertEquals("np",					res.at("/query/operands/0/key").asText());
		
		query = "cat=/NP/ & cat=/PP/ > #1";
		qs.setQuery(query, "annis");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group",			res.at("/query/@type").asText());
		assertEquals("operation:relation",	res.at("/query/operation").asText());
		assertEquals("korap:span",			res.at("/query/operands/0/@type").asText());
		assertEquals("PP",					res.at("/query/operands/0/key").asText());
		assertEquals("korap:span",			res.at("/query/operands/1/@type").asText());
		assertEquals("NP",					res.at("/query/operands/1/key").asText());
		assertEquals(true,					res.at("/query/operands/2").isMissingNode());
		assertEquals("korap:relation",		res.at("/query/relation/@type").asText());
		assertEquals("korap:term",			res.at("/query/relation/wrap/@type").asText());
		assertEquals("c",					res.at("/query/relation/wrap/layer").asText());
	}
	
	@Test
	public void testDefPredicationInversion() throws JsonProcessingException, IOException {
		query = "#1 > #2 & cnx/cat=\"vp\" & cnx/cat=\"np\"";  
		qs.setQuery(query, "annis");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group",			res.at("/query/@type").asText());
		assertEquals("operation:relation",	res.at("/query/operation").asText());
		assertEquals("korap:span",			res.at("/query/operands/0/@type").asText());
		assertEquals("korap:span",			res.at("/query/operands/0/@type").asText());
		assertEquals("vp",					res.at("/query/operands/0/key").asText());
		assertEquals("c",					res.at("/query/operands/0/layer").asText());
		assertEquals("cnx",					res.at("/query/operands/0/foundry").asText());
		assertEquals("korap:span",			res.at("/query/operands/1/@type").asText());
		assertEquals("np",					res.at("/query/operands/1/key").asText());
		assertEquals("c",					res.at("/query/operands/1/layer").asText());
		assertEquals("cnx",					res.at("/query/operands/1/foundry").asText());
		assertEquals("korap:relation",		res.at("/query/relation/@type").asText());
		assertEquals("korap:term",			res.at("/query/relation/wrap/@type").asText());
		assertEquals("c",					res.at("/query/relation/wrap/layer").asText());
	}
	
	@Test
	public void testSimpleDominance() throws JsonProcessingException, IOException {
		query = "node & node & #2 > #1";  
		qs.setQuery(query, "annis");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group",			res.at("/query/@type").asText());
		assertEquals("operation:relation",	res.at("/query/operation").asText());
		assertEquals("korap:span",			res.at("/query/operands/0/@type").asText());
		assertEquals("korap:span",			res.at("/query/operands/1/@type").asText());
		assertEquals("korap:relation",		res.at("/query/relation/@type").asText());
		assertEquals("korap:term",			res.at("/query/relation/wrap/@type").asText());
		assertEquals("c",					res.at("/query/relation/wrap/layer").asText());
		
		query = "\"Mann\" & node & #2 > #1";  
		qs.setQuery(query, "annis");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group",			res.at("/query/@type").asText());
		assertEquals("operation:relation",	res.at("/query/operation").asText());
		assertEquals("korap:span",			res.at("/query/operands/0/@type").asText());
		assertEquals("korap:token",			res.at("/query/operands/1/@type").asText());
		assertEquals("Mann",				res.at("/query/operands/1/wrap/key").asText());
		assertEquals("korap:relation",		res.at("/query/relation/@type").asText());
		assertEquals("korap:term",			res.at("/query/relation/wrap/@type").asText());
		assertEquals("c",					res.at("/query/relation/wrap/layer").asText());
		
		query = "\"Mann\" & node & #2 >[func=\"SB\"] #1";  //coordinates the func=SB term and requires a "c"-layer term (consituency relation/dominance)
		qs.setQuery(query, "annis");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:relation",		res.at("/query/relation/@type").asText());
		assertEquals("korap:termGroup",		res.at("/query/relation/wrap/@type").asText());
		assertEquals("relation:and",		res.at("/query/relation/wrap/relation").asText());
		assertEquals("c",					res.at("/query/relation/wrap/operands/1/layer").asText());
		assertEquals("func",				res.at("/query/relation/wrap/operands/0/layer").asText());
		assertEquals("SB",					res.at("/query/relation/wrap/operands/0/key").asText());
		
		query = "cat=\"S\" & node & #1 >[func=\"SB\" func=\"MO\"] #2";  // quite meaningless (function is subject and modifier), but this is allowed by Annis, however its backend only regards the 1st option
		qs.setQuery(query, "annis");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:relation",		res.at("/query/relation/@type").asText());
		assertEquals("korap:termGroup",		res.at("/query/relation/wrap/@type").asText());
		assertEquals("relation:and",		res.at("/query/relation/wrap/relation").asText());
		assertEquals("func",				res.at("/query/relation/wrap/operands/0/layer").asText());
		assertEquals("SB",					res.at("/query/relation/wrap/operands/0/key").asText());
		assertEquals("func",				res.at("/query/relation/wrap/operands/1/layer").asText());
		assertEquals("MO"	,				res.at("/query/relation/wrap/operands/1/key").asText());
		assertEquals("c",					res.at("/query/relation/wrap/operands/2/layer").asText());
		
		query = "cat=\"S\" & cat=\"NP\" & #1 >@l #2";  // all sentences starting with NP  -> wrap relation in startswith and retrieve 2nd operand with focus
		qs.setQuery(query, "annis");
		res = mapper.readTree(qs.toJSON());
		assertEquals("operation:position",	res.at("/query/operation").asText());
		assertEquals("operation:relation",	res.at("/query/operands/0/operation").asText());
		assertEquals("frames:startswith",	res.at("/query/frames/0").asText());
		assertEquals("korap:span",			res.at("/query/operands/0/operands/0/@type").asText());
		assertEquals("S",					res.at("/query/operands/0/operands/0/key").asText());
		assertEquals("korap:group",			res.at("/query/operands/0/operands/1/@type").asText());
		assertEquals("operation:class",		res.at("/query/operands/0/operands/1/operation").asText());
		assertEquals(129,					res.at("/query/operands/0/operands/1/classOut").asInt());
		assertEquals("korap:span",			res.at("/query/operands/0/operands/1/operands/0/@type").asText());
		assertEquals("NP",					res.at("/query/operands/0/operands/1/operands/0/key").asText());
		assertEquals("korap:reference",		res.at("/query/operands/1/@type").asText());
		assertEquals("operation:focus",		res.at("/query/operands/1/operation").asText());
		assertEquals(129,					res.at("/query/operands/1/classRef/0").asInt());
		
		query = "cat=\"S\" & cat=\"NP\" & #1 >@r #2";  
		qs.setQuery(query, "annis");
		res = mapper.readTree(qs.toJSON());
		assertEquals("operation:position",	res.at("/query/operation").asText());
		assertEquals("operation:relation",	res.at("/query/operands/0/operation").asText());
		assertEquals("frames:endswith",		res.at("/query/frames/0").asText());
		assertEquals("korap:span",			res.at("/query/operands/0/operands/0/@type").asText());
		assertEquals("S",					res.at("/query/operands/0/operands/0/key").asText());
		assertEquals("korap:group",			res.at("/query/operands/0/operands/1/@type").asText());
		assertEquals("operation:class",		res.at("/query/operands/0/operands/1/operation").asText());
		assertEquals(129,					res.at("/query/operands/0/operands/1/classOut").asInt());
		assertEquals("korap:span",			res.at("/query/operands/0/operands/1/operands/0/@type").asText());
		assertEquals("NP",					res.at("/query/operands/0/operands/1/operands/0/key").asText());
		assertEquals("korap:reference",		res.at("/query/operands/1/@type").asText());
		assertEquals("operation:focus",		res.at("/query/operands/1/operation").asText());
		assertEquals(129,					res.at("/query/operands/1/classRef/0").asInt());
	}
	
	@Test
	public void testIndirectDominance() throws JsonProcessingException, IOException {
		query = "node & node & #1 >2,4 #2";  
		qs.setQuery(query, "annis");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group",			res.at("/query/@type").asText());
		assertEquals("operation:relation",	res.at("/query/operation").asText());
		assertEquals("korap:span",			res.at("/query/operands/0/@type").asText());
		assertEquals("korap:span",			res.at("/query/operands/1/@type").asText());
		assertEquals("korap:relation",		res.at("/query/relation/@type").asText());
		assertEquals(2,						res.at("/query/relation/boundary/min").asInt());
		assertEquals(4,						res.at("/query/relation/boundary/max").asInt());
		assertEquals("korap:term",			res.at("/query/relation/wrap/@type").asText());
		assertEquals("c",					res.at("/query/relation/wrap/layer").asText());
		
		query = "node & node & #1 >* #2";  
		qs.setQuery(query, "annis");
		res = mapper.readTree(qs.toJSON());
		assertEquals(0,						res.at("/query/relation/boundary/min").asInt());
		assertEquals(true,					res.at("/query/relation/boundary/max").isMissingNode());
	}

		
	@Test
	public void testMultipleDominance() throws JsonProcessingException, IOException {
		query = "cat=\"CP\" & cat=\"VP\" & cat=\"NP\" & #1 > #2 > #3";  
		qs.setQuery(query, "annis");
		res = mapper.readTree(qs.toJSON());
		assertEquals("korap:group",			res.at("/query/@type").asText());
		assertEquals("operation:relation",	res.at("/query/operation").asText());
		assertEquals("korap:reference",		res.at("/query/operands/0/@type").asText());
		assertEquals("operation:focus",		res.at("/query/operands/0/operation").asText());
		assertEquals(128,					res.at("/query/operands/0/classRef/0").asInt());
		assertEquals("korap:group",			res.at("/query/operands/0/operands/0/@type").asText());
		assertEquals("operation:relation",	res.at("/query/operands/0/operands/0/operation").asText());
		assertEquals("korap:relation",		res.at("/query/operands/0/operands/0/relation/@type").asText());
		assertEquals("c",					res.at("/query/operands/0/operands/0/relation/wrap/layer").asText());
		assertEquals("korap:span",			res.at("/query/operands/0/operands/0/operands/0/@type").asText());
		assertEquals("c",					res.at("/query/operands/0/operands/0/operands/0/layer").asText());
		assertEquals("CP",					res.at("/query/operands/0/operands/0/operands/0/key").asText());
		assertEquals("korap:group",			res.at("/query/operands/0/operands/0/operands/1/@type").asText());
		assertEquals("operation:class",		res.at("/query/operands/0/operands/0/operands/1/operation").asText());
		assertEquals(128,					res.at("/query/operands/0/operands/0/operands/1/classOut").asInt());
		assertEquals("VP",					res.at("/query/operands/0/operands/0/operands/1/operands/0/key").asText());
	}
//		query = "cat=\"CP\" & cat=\"VP\" & cat=\"NP\" & #1 > #2 > #3";
//		String dom1 = 
//				"{@type=korap:group, operation=operation:relation, operands=[" +
//						"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
//							"{@type=korap:group, operation=operation:relation, operands=[" +
//								"{@type=korap:span, layer=cat, key=CP, match=match:eq}," +
//								"{@type=korap:group, operation=operation:class, class=128, classOut=128, operands=[" +
//									"{@type=korap:span, layer=cat, key=VP, match=match:eq}" +
//								"]}" +
//							"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
//						"]}," +
//						"{@type=korap:span, layer=cat, key=NP, match=match:eq}" +
//				"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}" +
//				"}";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(dom1.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		query = "cat=\"CP\" & cat=\"VP\" & cat=\"NP\" & cat=\"DP\" & #1 > #2 > #3 > #4";
//		String dom2 = 
//				"{@type=korap:group, operation=operation:relation, operands=[" +
//						"{@type=korap:reference, operation=operation:focus, classRef=[1], operands=[" +
//							"{@type=korap:group, operation=operation:relation, operands=[" +
//								"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
//									"{@type=korap:group, operation=operation:relation, operands=[" +
//										"{@type=korap:span, layer=cat, key=CP, match=match:eq}," +
//										"{@type=korap:group, operation=operation:class, class=128, classOut=128, operands=[" +
//											"{@type=korap:span, layer=cat, key=VP, match=match:eq}" +
//										"]}" +
//									"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
//								"]}," +
//								"{@type=korap:group, operation=operation:class, class=129, classOut=129, operands=[" +
//									"{@type=korap:span, layer=cat, key=NP, match=match:eq}" +
//								"]}" +
//							"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
//						"]}," +
//						"{@type=korap:span, layer=cat, key=DP, match=match:eq}" +
//					"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}" +
//				"}";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(dom2.replaceAll(" ", ""), map.replaceAll(" ", ""));
//	}
//	
//	@Test
//	public void testPointingRelations() throws Exception {
//		query = "node & node & #2 ->coref[val=\"true\"] #1";
//		String dom1 = 
//				"{@type=korap:group, operation=operation:relation, operands=[" +
//						"{@type=korap:span}," +
//						"{@type=korap:span}" +
//				"], relation={@type=korap:relation, wrap={@type=korap:term, layer=coref, key=true, match=match:eq}}" +
//				"}";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(dom1.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		query = "node & node & #2 ->mate/coref[val=\"true\"] #1";
//		String dom2 = 
//				"{@type=korap:group, operation=operation:relation, operands=[" +
//						"{@type=korap:span}," +
//						"{@type=korap:span}" +
//				"], relation={@type=korap:relation, wrap={@type=korap:term, foundry=mate, layer=coref, key=true, match=match:eq}}" +
//				"}";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(dom2.replaceAll(" ", ""), map.replaceAll(" ", ""));
//	}
//	
//	@Test
//	public void testSequence() throws Exception {
//		query = "node & node & #1 . #2";
//		String seq1 = 
//				"{@type=korap:group, operation=operation:sequence, " +
//					"operands=[" +
//						"{@type=korap:span}," +
//						"{@type=korap:span}" +
//					"], inOrder=true" +
//				"}";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(seq1.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		query = "node & node & #1 .* #2";
//		String seq2 = 
//				"{@type=korap:group, operation=operation:sequence, operands=[" +
//						"{@type=korap:span}," +
//						"{@type=korap:span}" +
//					"], distances=[" +
//						"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=0}, min=0}" +
//					"], inOrder=true" +
//				"}";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(seq2.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		query = "node & node & #1 .2,3 #2";
//		String seq3 = 
//				"{@type=korap:group, operation=operation:sequence, operands=[" +
//						"{@type=korap:span}," +
//						"{@type=korap:span}" +
//					"], distances=[" +
//						"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=2, max=3}, min=2, max=3}" +
//					"], inOrder=true" +
//				"}";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(seq3.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//	}
//	
	@Test
	public void testMultipleSequence() throws Exception {
		query = "tok=\"a\" & tok=\"b\" & tok=\"c\" & #1 . #2 & #2 . #3";
		String seq4 = 
				"{@type=korap:group, operation=operation:sequence," +
					"operands=[" +
						"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
							"{@type=korap:group, operation=operation:sequence, operands=[" +
								"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Sonne, match=match:eq}}," +
								"{@type=korap:group, operation=operation:class, class=128, classOut=128, operands=[" +
									"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mond, match=match:eq}}" +
								"]}" +
							"], distances=[" +
								"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=0, max=2}, min=0, max=2}" +
							"], inOrder=true}" +
						"]}," +	
						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Sterne, match=match:eq}}" +
					"],distances=[" +
						"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=0, max=4}, min=0, max=4}" +
					"], inOrder=true" +
				"}";
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
        assertEquals("korap:group",         res.at("/query/@type").asText());
        assertEquals("operation:sequence",  res.at("/query/operation").asText());
        assertEquals("korap:reference",     res.at("/query/operands/0/@type").asText());
        assertEquals(128,                   res.at("/query/operands/0/classRef/0").asInt());
        assertEquals(res.at("/query/operands/0/classRef/0").asInt(), 
                     res.at("/query/operands/0/operands/0/operands/1/classOut").asInt());
	}

        
//		
//		query = "node & node & node & #1 . #2 .1,3 #3";
//		String seq5 = 
//				"{@type=korap:group, operation=operation:sequence, operands=[" +
//						"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
//							"{@type=korap:group, operation=operation:sequence, operands=[" +
//								"{@type=korap:span}," +
//								"{@type=korap:group, operation=operation:class, class=128, classOut=128, operands=[" +
//									"{@type=korap:span}" +
//								"]} "+
//							"], inOrder=true}" +
//						"]}," +
//						"{@type=korap:span}" +
//					"], distances=[" +
//							"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=1, max=3}, min=1, max=3}" +
//						"], inOrder=true" +
//				"}";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(seq5.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		query = "tok=\"Sonne\" & tok=\"Mond\" & tok=\"Sterne\" & tok=\"Himmel\" & #1 .0,2 #2 .0,4 #3 . #4";
//		String seq6 = 
//				"{@type=korap:group, operation=operation:sequence, operands=[" +
//					"{@type=korap:reference, operation=operation:focus, classRef=[1], operands=[" +
//						"{@type=korap:group, operation=operation:sequence, operands=[" +
//							"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
//								"{@type=korap:group, operation=operation:sequence, operands=[" +
//									"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Sonne, match=match:eq}}," +
//									"{@type=korap:group, operation=operation:class, class=128, classOut=128, operands=[" +
//										"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mond, match=match:eq}}" +
//									"]}" +
//								"], distances=[" +
//									"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=0, max=2}, min=0, max=2}" +
//								"], inOrder=true}" +
//							"]}," +	
//							"{@type=korap:group, operation=operation:class, class=129, classOut=129, operands=[" +
//								"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Sterne, match=match:eq}}" +
//							"]}" +
//						"],distances=[" +
//							"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=0, max=4}, min=0, max=4}" +
//						"], inOrder=true}" +
//					"]}," +
//					"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Himmel, match=match:eq}}" +
//				"], inOrder=true}" ;
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(seq6.replaceAll(" ", ""), map.replaceAll(" ", ""));
//	}
//	
	/**
	 * Tests the (rather difficult) serialization of queries where two subsequent relations
	 * do not share any common operand. Makes it impossible to wrap 2nd relation around 1st. 
	 * Must therefore re-order relations (or postpone processing of 2nd).
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	@Test
    public void testNoSharedOperand() throws JsonProcessingException, IOException {
	    query = "cat=\"A\" & cat=\"B\" & cat=\"C\" & cat=\"D\" & #1 . #2 & #3 . #4 & #1 > #3";  
	    // the resulting query should be equivalent to PQ+:  focus(2:dominates(focus(1:{1:<A>}<B>),{2:<C>}))<D> 
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
        assertEquals("korap:group",         res.at("/query/@type").asText());
        assertEquals("operation:sequence",  res.at("/query/operation").asText());
        assertEquals("korap:reference",     res.at("/query/operands/0/@type").asText());
        assertEquals("operation:focus",     res.at("/query/operands/0/operation").asText());
        assertEquals("korap:group",         res.at("/query/operands/0/operands/0/@type").asText());
        assertEquals("operation:relation",  res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals("korap:reference",     res.at("/query/operands/0/operands/0/operands/0/@type").asText());
        assertEquals("operation:focus",     res.at("/query/operands/0/operands/0/operands/0/operation").asText());
        assertEquals("korap:group",         res.at("/query/operands/0/operands/0/operands/0/operands/0/@type").asText());
        assertEquals("operation:sequence",  res.at("/query/operands/0/operands/0/operands/0/operands/0/operation").asText());
        assertEquals("operation:class",     res.at("/query/operands/0/operands/0/operands/0/operands/0/operands/0/operation").asText());
        assertEquals("A",                   res.at("/query/operands/0/operands/0/operands/0/operands/0/operands/0/operands/0/key").asText());
        assertEquals("B",                   res.at("/query/operands/0/operands/0/operands/0/operands/0/operands/1/key").asText());
        assertEquals("C",                   res.at("/query/operands/0/operands/0/operands/1/operands/0/key").asText());
        assertEquals("D",                   res.at("/query/operands/1/key").asText());
        
        query = "cat=\"A\" & cat=\"B\" & cat=\"C\" & cat=\"D\" & cat=\"E\" & cat=\"F\" & #1 . #2 & #3 . #4 & #5 . #6 & #1 > #3 & #3 > #5";  
        // the resulting query should be equivalent to PQ+:   focus(3:dominates(focus(2:dominates(focus(1:{1:<A>}<B>),{2:<C>}))<D>,{3:<E>}))<F> 
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
        assertEquals("korap:group",         res.at("/query/@type").asText());
        assertEquals("operation:sequence",  res.at("/query/operation").asText());
        assertEquals("korap:reference",     res.at("/query/operands/0/@type").asText());
        assertEquals("operation:focus",     res.at("/query/operands/0/operation").asText());
        assertEquals("korap:group",         res.at("/query/operands/0/operands/0/@type").asText());
        assertEquals("operation:relation",  res.at("/query/operands/0/operands/0/operation").asText());
        assertEquals("korap:reference",     res.at("/query/operands/0/operands/0/operands/0/@type").asText());
        assertEquals("operation:focus",     res.at("/query/operands/0/operands/0/operands/0/operation").asText());
        assertEquals("korap:group",         res.at("/query/operands/0/operands/0/operands/0/operands/0/@type").asText());
        assertEquals("operation:sequence",  res.at("/query/operands/0/operands/0/operands/0/operands/0/operation").asText());
        assertEquals("operation:class",     res.at("/query/operands/0/operands/0/operands/0/operands/0/operands/0/operands/0/operands/0/operands/0/operands/0/operation").asText());
        assertEquals("A",                   res.at("/query/operands/0/operands/0/operands/0/operands/0/operands/0/operands/0/operands/0/operands/0/operands/0/operands/0/key").asText());
        assertEquals("B",                   res.at("/query/operands/0/operands/0/operands/0/operands/0/operands/0/operands/0/operands/0/operands/0/operands/1/key").asText());
        assertEquals("C",                   res.at("/query/operands/0/operands/0/operands/0/operands/0/operands/0/operands/0/operands/1/operands/0/key").asText());
        assertEquals("D",                   res.at("/query/operands/0/operands/0/operands/0/operands/0/operands/1/key").asText());
        assertEquals("E",                   res.at("/query/operands/0/operands/0/operands/1/operands/0/key").asText());
        assertEquals("F",                   res.at("/query/operands/1/key").asText());
        
        query = "cat=\"A\" & cat=\"B\" & cat=\"C\" & cat=\"D\" & #1 . #2 & #3 . #4";  
        // the resulting query should be equivalent to PQ+:  focus(2:dominates(focus(1:{1:<A>}<B>),{2:<C>}))<D> 
        qs.setQuery(query, "annis");
        res = mapper.readTree(qs.toJSON());
        assertEquals(true,         res.at("/query/@type").isMissingNode());
        assertEquals(StatusCodes.UNBOUND_ANNIS_RELATION,   res.at("/errors/0/0").asInt());
    }
	
//	@Test
//	public void testMultipleMixedOperators() throws Exception {
//		query = "tok=\"Sonne\" & tok=\"Mond\" & tok=\"Sterne\" & #1 > #2 .0,4 #3";
//		String seq4 = 
//					"{@type=korap:group, operation=operation:sequence, operands=[" +
//							"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
//									"{@type=korap:group, operation=operation:relation, operands=[" +
//										"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Sonne, match=match:eq}}," +
//										"{@type=korap:group, operation=operation:class, class=128, classOut=128, operands=[" +
//											"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mond, match=match:eq}}" +
//										"]}" +
//									"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
//								"]}," +
//								"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Sterne, match=match:eq}}" +
//						"], distances=[" +
//							"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=0, max=4}, min=0, max=4}" +
//						"], inOrder=true" +
//					"}";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(seq4.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		query = "tok=\"Sonne\" & tok=\"Mond\" & #1 > #2 .0,4  tok=\"Sterne\"";
//		String seq5 = 
//					"{@type=korap:group, operation=operation:sequence, operands=[" +
//							"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
//									"{@type=korap:group, operation=operation:relation, operands=[" +
//										"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Sonne, match=match:eq}}," +
//										"{@type=korap:group, operation=operation:class, class=128, classOut=128, operands=[" +
//											"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mond, match=match:eq}}" +
//										"]}" +
//									"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
//								"]}," +
//								"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Sterne, match=match:eq}}" +
//						"], distances=[" +
//							"{@type=korap:distance, key=w, boundary={@type=korap:boundary, min=0, max=4}, min=0, max=4}" +
//						"], inOrder=true" +
//					"}";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(seq5.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		query = "cat=\"NP\" & cat=\"VP\" & cat=\"PP\" & #1 $ #2 > #3";
//		String cp2 =
//				"{@type=korap:group, operation=operation:relation, operands=[" +
//					"{@type=korap:reference, operation=operation:focus, classRef=[1], operands=[" +
//						"{@type=korap:group, operation=operation:relation, operands=[" +
//							"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
//								"{@type=korap:group, operation=operation:relation, operands=[" +
//									"{@type=korap:group, operation=operation:class, class=128, classOut=128, operands=[" +
//										"{@type=korap:span}" +
//									"]}," +
//									"{@type=korap:span, layer=cat, key=NP, match=match:eq}" +
//								"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
//							"]}," +
//							"{@type=korap:group, operation=operation:class, class=129, classOut=129, operands=[" +
//								"{@type=korap:span, layer=cat, key=VP, match=match:eq}" +
//							"]}" +
//						"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}" +
//						"}" +
//					"]}," +
//					"{@type=korap:span, layer=cat, key=PP, match=match:eq}" +
//				"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}" +
//				"}";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(cp2.replaceAll(" ", ""), map.replaceAll(" ", ""));		
//	}
//	/*
//	@Test
//	public void testMultipleOperatorsWithSameOperands() throws Exception {
//		
//		query = "cat=\"NP\" > cat=\"VP\" & #1 _l_ #2";
//		String eq2 =
//				"{@type=korap:group, operation=operation:position, frames=[frame:startswith], sharedClasses=[sharedClasses:includes], operands=[" +
//						"{@type=korap:group, operation=operation:relation, operands=[" +
//							"{@type=korap:group, operation=operation:class, class=, classOut=129, operands=[" +
//								"{@type=korap:span, layer=cat, key=NP, match=match:eq}" +
//							"]}," +
//							"{@type=korap:group, operation=operation:class, class=, classOut=129, operands=[" +
//								"{@type=korap:span, layer=cat, key=VP, match=match:eq}" +
//							"]}" +
//						"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}," +
//					"{@type=korap:reference, operation=operation:focus, classRef=[2]}" +
//				"]" +
//				"}"; // ???
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(eq2.replaceAll(" ", ""), map.replaceAll(" ", ""));		
//	}
//	*/
//	@Test
//	public void testPositions() throws Exception {
//		query = "node & node & #2 _=_ #1";
//		String pos1 = 
//				"{@type=korap:group, operation=operation:position, frames=[frames:matches], operands=[" +
//						"{@type=korap:span}," +
//						"{@type=korap:span}" +
//				"], frame=frame:matches}";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(pos1.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		query = "node & node & #2 _i_ #1";
//		String pos2 = 
//				"{@type=korap:group, operation=operation:position, frames=[frames:contains], operands=[" +
//						"{@type=korap:span}," +
//						"{@type=korap:span}" +
//				"], frame=frame:contains" +
//				"}";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(pos2.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		query = "node & node & #2 _l_ #1";
//		String pos3 = 
//				"{@type=korap:group, operation=operation:position, frames=[frames:startswith], operands=[" +
//						"{@type=korap:span}," +
//						"{@type=korap:span}" +
//				"], frame=frame:startswith" +
//				"}";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(pos3.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		query = "node & \"Mann\" & #1 _r_ #2";
//		String pos4 = 
//					"{@type=korap:group, operation=operation:position, frames=[frames:endswith], operands=[" +
//						"{@type=korap:span}," +
//						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mann, match=match:eq}}" +
//						"], frame=frame:endswith" +
//					"}";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(pos4.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		query = "node & \"Mann\" & #2 _r_ #1";
//		String pos5 = 
//					"{@type=korap:group, operation=operation:position, frames=[frames:endswith], operands=[" +
//						"{@type=korap:token, wrap={@type=korap:term, layer=orth, key=Mann, match=match:eq}}," +
//						"{@type=korap:span}" +
//						"], frame=frame:endswith" +
//					"}";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(pos5.replaceAll(" ", ""), map.replaceAll(" ", ""));
//	}
//	
//	@Test
//	public void testMultiplePredications() throws Exception {
//		// a noun before a verb before a preposition
//		query = "pos=\"N\" & pos=\"V\" & pos=\"P\" & #1 . #2 & #2 . #3"; 
//		String mult1 = 
//		"{@type=korap:group, operation=operation:sequence, operands=[" +
//			"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
//				"{@type=korap:group, operation=operation:sequence, operands=[" +
//					"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=N, match=match:eq}}," +
//					"{@type=korap:group, operation=operation:class, class=128, classOut=128, operands=[" +
//						"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=V, match=match:eq}}" +
//					"]}" +
//				"], inOrder=true}" +
//			"]}," +
//			"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=P, match=match:eq}}" +
//		"], inOrder=true}";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(mult1.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		// a noun before a verb before a preposition
//		query = "pos=\"N\" & pos=\"V\" & #1 . #2 & #2 . pos=\"P\""; 
//		String mult2 = 
//		"{@type=korap:group, operation=operation:sequence, operands=[" +
//			"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
//				"{@type=korap:group, operation=operation:sequence, operands=[" +
//					"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=N, match=match:eq}}," +
//					"{@type=korap:group, operation=operation:class, class=128, classOut=128, operands=[" +
//						"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=V, match=match:eq}}" +
//					"]}" +
//				"], inOrder=true}" +
//			"]}," +
//			"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=P, match=match:eq}}" +
//		"], inOrder=true}";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(mult2.replaceAll(" ", ""), map.replaceAll(" ", ""));
//
//		query = "pos=\"N\" & pos=\"V\" & pos=\"P\" & #1 > #2 & #1 > #3";
//		String mult3 = 
//			"{@type=korap:group, operation=operation:relation, operands=[" +
//				"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
//					"{@type=korap:group, operation=operation:relation, operands=[" +
//						"{@type=korap:group, operation=operation:class, class=128, classOut=128, operands=[" +
//							"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=N, match=match:eq}}" +
//						"]}," +
//						"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=V, match=match:eq}}" +
//					"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
//				"]}," +
//				"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=P, match=match:eq}}" +
//			"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(mult3.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		query = "cat=\"NP\" & pos=\"V\" & pos=\"P\" & #1 > #2 & #1 > #3 & #2 . #3";
//		String mult4 = 
//				"{@type=korap:group, operation=operation:sequence, operands=[" +
//					// reduce dominance relations "#1 > #2 & #1 > #3" to operand #2 in order to make it accessible for #2 . #3 (the last/outermost relation)  
//					"{@type=korap:reference, operation=operation:focus, classRef=[1], operands=[" +
//						"{@type=korap:group, operation=operation:relation, operands=[" +
//							// dominance relation #1 > #2 is reduced to #1, for expressing #1 > #3
//							"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
//								"{@type=korap:group, operation=operation:relation, operands=[" +
//									"{@type=korap:group, operation=operation:class, class=128, classOut=128, operands=[" +
//										"{@type=korap:span, layer=cat, key=NP, match=match:eq}" +
//									"]}," +
//									"{@type=korap:group, operation=operation:class, class=129, classOut=129, operands=[" +
//										"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=V, match=match:eq}}" +
//									"]}" +
//								"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
//							"]}," +
//							// establish class 2 around P for later reference
//							"{@type=korap:group, operation=operation:class, class=130, classOut=130, operands=[" +
//								"{@type=korap:token, wrap={@type=korap:term, layer=pos, key=P, match=match:eq}}" +
//							"]}" +
//						"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
//					"]}," +
//					// refer back to class 2 as second operand
//					"{@type=korap:reference, operation=operation:focus, classRef=[2]}" +
//				"], inOrder=true}";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(mult4.replaceAll(" ", ""), map.replaceAll(" ", ""));
//	}	
//	
//	@Test
//	public void testUnaryRelations() throws Exception {
//		query = "node & #1:tokenarity=2";
//		String unary1 = 
//				"{@type=korap:span, attr={@type=korap:term, tokenarity={@type=korap:boundary,min=2,max=2}}}";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(unary1.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		query = "cnx/cat=\"NP\" & #1:tokenarity=2";
//		String unary2 = 
//				"{@type=korap:span, foundry=cnx, layer=cat, key=NP, match=match:eq, attr={@type=korap:term, tokenarity={@type=korap:boundary,min=2,max=2}}}";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(unary2.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		query = "cnx/cat=\"NP\" & #1:root";
//		String unary3 = 
//				"{@type=korap:span, foundry=cnx, layer=cat, key=NP, match=match:eq, attr={@type=korap:term, root=true}}";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(unary3.replaceAll(" ", ""), map.replaceAll(" ", ""));
//		
//		query = "cnx/cat=\"NP\" & node & #1>#2 & #1:tokenarity=2";
//		String unary4 = 
//					"{@type=korap:group, operation=operation:relation, operands=[" +
//						"{@type=korap:span, foundry=cnx, layer=cat, key=NP, match=match:eq, attr={@type=korap:term, tokenarity={@type=korap:boundary,min=2,max=2}}}," +
//						"{@type=korap:span}" +
//					"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}" +
//					"}";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(unary4.replaceAll(" ", ""), map.replaceAll(" ", ""));
//	}	
//	
//	@Test
//	public void testCommonParent() throws Exception {
//		query = "cat=\"NP\" & cat=\"VP\" & #1 $ #2";
//		String cp1 =
//				"{@type=korap:group, operation=operation:relation, operands=[" +
//						"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
//							"{@type=korap:group, operation=operation:relation, operands=[" +
//								"{@type=korap:group, operation=operation:class, class=128, classOut=128, operands=[" +
//									"{@type=korap:span}" +
//								"]}," +
//								"{@type=korap:span, layer=cat, key=NP, match=match:eq}" +
//							"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
//						"]}," +
//						"{@type=korap:span, layer=cat, key=VP, match=match:eq}" +
//					"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
//					"";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(cp1.replaceAll(" ", ""), map.replaceAll(" ", ""));		
//		
//		query = "cat=\"NP\" & cat=\"VP\" & cat=\"PP\" & #1 $ #2 $ #3";
//		String cp2 =
//				"{@type=korap:group, operation=operation:relation, operands=[" +
//					"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
//						"{@type=korap:group, operation=operation:relation, operands=[" +
//							"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
//								"{@type=korap:group, operation=operation:relation, operands=[" +
//									"{@type=korap:group, operation=operation:class, class=128, classOut=128, operands=[" +
//										"{@type=korap:span}" +
//									"]}," +
//									"{@type=korap:span, layer=cat, key=NP, match=match:eq}" +
//								"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
//							"]}," +
//							"{@type=korap:span, layer=cat, key=VP, match=match:eq}" +
//						"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}" +
//						"}" +
//					"]}," +
//					"{@type=korap:span, layer=cat, key=PP, match=match:eq}" +
//				"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}" +
//				"}";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(cp2.replaceAll(" ", ""), map.replaceAll(" ", ""));		
//		
//		query = "cat=\"NP\" & cat=\"VP\" & cat=\"PP\" & cat=\"CP\" & #1 $ #2 $ #3 $ #4";
//		String cp3 =
//				"{@type=korap:group, operation=operation:relation, operands=[" +
//					"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
//						"{@type=korap:group, operation=operation:relation, operands=[" +
//							"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
//								"{@type=korap:group, operation=operation:relation, operands=[" +
//									"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
//										"{@type=korap:group, operation=operation:relation, operands=[" +
//											"{@type=korap:group, operation=operation:class, class=128, classOut=128, operands=[" +
//												"{@type=korap:span}" +
//											"]}," +
//											"{@type=korap:span, layer=cat, key=NP, match=match:eq}" +
//										"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
//									"]}," +
//									"{@type=korap:span, layer=cat, key=VP, match=match:eq}" +
//								"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
//							"]}," +
//							"{@type=korap:span, layer=cat, key=PP, match=match:eq}" +
//						"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}}" +
//					"]}," +
//					"{@type=korap:span, layer=cat, key=CP, match=match:eq}" +
//				"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c}}" +
//				"}" +
//				"";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(cp3.replaceAll(" ", ""), map.replaceAll(" ", ""));		
//		
//		query = "cat=\"NP\" & cat=\"VP\" & #1 $* #2";
//		String cp4 =
//				"{@type=korap:group, operation=operation:relation, operands=[" +
//						"{@type=korap:reference, operation=operation:focus, classRef=[0], operands=[" +
//							"{@type=korap:group, operation=operation:relation, operands=[" +
//								"{@type=korap:group, operation=operation:class, class=128, classOut=128, operands=[" +
//									"{@type=korap:span}" +
//								"]}," +
//								"{@type=korap:span, layer=cat, key=NP, match=match:eq}" +
//							"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c},boundary={@type=korap:boundary,min=1}}}" +
//						"]}," +
//						"{@type=korap:span, layer=cat, key=VP, match=match:eq}" +
//					"], relation={@type=korap:relation, wrap={@type=korap:term, layer=c},boundary={@type=korap:boundary,min=1}}}" +
//					"";
//		aqlt = new AqlTree(query);
//		map = aqlt.getRequestMap().get("query").toString();
//		assertEquals(cp4.replaceAll(" ", ""), map.replaceAll(" ", ""));		
//	}
	
	/*		
	@Test
	public void testEqualNotequalValue() throws Exception {
		query = "cat=\"NP\" & cat=\"VP\" & #1 == #2";
		String eq1 =
				"{}"; // ???
		aqlt = new AqlTree(query);
		map = aqlt.getRequestMap().get("query").toString();
		assertEquals(eq1.replaceAll(" ", ""), map.replaceAll(" ", ""));		
	}
	*/
	
}
