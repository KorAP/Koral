package de.ids_mannheim.korap.query.serialize;

import static org.junit.Assert.assertEquals;


import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FcsqlQueryProcessorTest {
	ObjectMapper mapper = new ObjectMapper();

	private void runAndValidate(String query, String jsonLD)
			throws JsonProcessingException {
		FCSQLQueryProcessor tree = new FCSQLQueryProcessor(query, "2.0");
		String serializedQuery = mapper.writeValueAsString(tree.getRequestMap()
				.get("query"));
		assertEquals(jsonLD.replace(" ", ""), serializedQuery.replace("\"", ""));
	}

	@Test
	public void testTermQuery() throws JsonProcessingException {
		String query = "\"Sonne\"";
		String jsonLd = "{@type:koral:token, wrap:{@type:koral:term, key:Sonne, "
				+ "foundry:opennlp, layer:orth, match:match:eq}}";
		runAndValidate(query, jsonLd);
	}

	@Test
	public void testTermQueryWithRegexFlag() throws JsonProcessingException {
		String query = "\"Fliegen\" /c";
		String jsonLd = "{@type:koral:token, wrap:{@type:koral:term, caseInsensitive:true, "
				+ "key:Fliegen, foundry:opennlp, layer:orth, match:match:eq}}";
		runAndValidate(query, jsonLd);
	}

	@Test
	public void testTermQueryWithSpecificLayer() throws JsonProcessingException {
		String query = "[text = \"Sonne\"]";
		String jsonLd = "{@type:koral:token, wrap:{@type:koral:term, key:Sonne, "
				+ "foundry:opennlp, layer:orth, match:match:eq}}";
		runAndValidate(query, jsonLd);

		query = "[lemma = \"sein\"]";
		jsonLd = "{@type:koral:token, wrap:{@type:koral:term, key:sein, "
				+ "foundry:tt, layer:l, match:match:eq}}";
		runAndValidate(query, jsonLd);

		query = "[pos = \"NN\"]";
		jsonLd = "{@type:koral:token, wrap:{@type:koral:term, key:NN, "
				+ "foundry:tt, layer:p, match:match:eq}}";
		runAndValidate(query, jsonLd);
	}

	@Test
	public void testTermQueryWithQualifier() throws JsonProcessingException {
		String query = "[mate:lemma = \"sein\"]";
		String jsonLd = "{@type:koral:token, wrap:{@type:koral:term, key:sein, "
				+ "foundry:mate, layer:l, match:match:eq}}";
		runAndValidate(query, jsonLd);

		query = "[cnx:pos = \"N\"]";
		jsonLd = "{@type:koral:token, wrap:{@type:koral:term, key:N, "
				+ "foundry:cnx, layer:p, match:match:eq}}";
		runAndValidate(query, jsonLd);
	}

	@Test
	public void testMatchOperation() throws JsonProcessingException {
		String query = "[cnx:pos != \"N\"]";
		String jsonLd = "{@type:koral:token, wrap:{@type:koral:term, key:N, "
				+ "foundry:cnx, layer:p, match:match:ne}}";
		runAndValidate(query, jsonLd);
	}

	// @Test
	// public void testSequenceQuery() throws JsonProcessingException {
	// String query = "\"blaue\" [pos = \"NN\"]";
	// String jsonLd =
	// "{@type:koral:group, operation:operation:sequence, operands:["
	// +
	// "{@type:koral:token, wrap:{@type:koral:term, key:blaue, foundry:opennlp, layer:orth, match:match:eq}},"
	// +
	// "{@type:koral:token, wrap:{@type:koral:term, key:NN, foundry:tt, layer:p, match:match:eq}}"
	// + "]}";
	// runAndValidate(query, jsonLd);
	// }

}
