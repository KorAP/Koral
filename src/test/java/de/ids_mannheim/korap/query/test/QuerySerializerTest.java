package de.ids_mannheim.korap.query.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

import de.ids_mannheim.korap.query.serialize.QuerySerializer;

public class QuerySerializerTest {

	@Test
	public void serializePoliqarp () {
		// Backup the original System.out
		PrintStream originalOut = System.out;

		// Create a stream to hold the output
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outputStream));

		String query = "Sonne";
		String ql = "poliqarp";
		String[] args = { query, ql };
		QuerySerializer.main(args);

		System.setOut(originalOut);
		String expectedOutput = "{\"query\":{\"@type\":\"koral:token\","
				+ "\"wrap\":{\"@type\":\"koral:term\",\"match\":"
				+ "\"match:eq\",\"layer\":\"orth\",\"key\":\"Sonne\"}},"
				+ "\"@context\":\"http://korap.ids-mannheim.de/ns/koral"
				+ "/0.3/context.jsonld\"}\n" + System.lineSeparator();
		assertEquals(expectedOutput, outputStream.toString());
	}
	
	@Test
	public void serializeFCSQL () {
		// Backup the original System.out
		PrintStream originalOut = System.out;

		// Create a stream to hold the output
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outputStream));

		String query = "\"Sonne\"";
		String ql = "fcsql";
		String[] args = { query, ql };
		QuerySerializer.main(args);

		System.setOut(originalOut);
		String expectedOutput = "{\"query\":{\"@type\":\"koral:token\","
				+ "\"wrap\":{\"@type\":\"koral:term\",\"key\":\"Sonne\","
				+ "\"foundry\":\"opennlp\",\"layer\":\"orth\",\"type\":"
				+ "\"type:regex\",\"match\":\"match:eq\"}},\"@context\":"
				+ "\"http://korap.ids-mannheim.de/ns/koral/0.3/"
				+ "context.jsonld\"}\n" + System.lineSeparator();
		assertEquals(expectedOutput, outputStream.toString());
	}
	
	@Test
	public void serializeCorpusQuery () {
		// Backup the original System.out
		PrintStream originalOut = System.out;

		// Create a stream to hold the output
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outputStream));

		String query = "corpusSigle=WPD17";
		String ql = "cq";
		String[] args = { query, ql };
		QuerySerializer.main(args);

		System.setOut(originalOut);
		String expectedOutput = "{\"collection\":{\"@type\":\"koral:doc\","
				+ "\"match\":\"match:eq\",\"value\":\"WPD17\",\"key\":"
				+ "\"corpusSigle\"},\"@context\":\"http://korap.ids-mannheim.de"
				+ "/ns/koral/0.3/context.jsonld\"}";
		assertEquals(expectedOutput, outputStream.toString().trim());
	}
}
