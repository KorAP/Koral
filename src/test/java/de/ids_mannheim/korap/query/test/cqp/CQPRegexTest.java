package de.ids_mannheim.korap.query.test.cqp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import de.ids_mannheim.korap.query.test.BaseQueryTest;

public class CQPRegexTest extends BaseQueryTest {

    private JsonNode result;

    public CQPRegexTest () {
        super("CQP");
    }


    @Test
    public void testRegex () throws JsonProcessingException {
        query = "[orth=\"M(a|ä)nn(er)?\"]";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("M(a|ä)nn(er)?", result.at("/query/wrap/key").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());

        query = "\".*?Mann.*?\"";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals(".*?Mann.*?", result.at("/query/wrap/key").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());

        // issue #56
        query = "„.*?Mann.*?“";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals(".*?Mann.*?", result.at("/query/wrap/key").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());

        query = "“.*?Mann.*?”";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals(".*?Mann.*?", result.at("/query/wrap/key").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());

        query = "\"z.B.\""; // /x is not implemented
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("z.B.", result.at("/query/wrap/key").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
    }


    @Test
    public void testRegexEscape () throws JsonProcessingException {
        // Escape regex symbols
        // test doubling squoutes and dquoutes; not working now!
        query = "\"a\\.\"";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("a\\.", result.at("/query/wrap/key").asText());

        // escape doublequoutes and singlequoutes
        query = "'\"';";
        result = runQuery(query);

        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("\"", result.at("/query/wrap/key").asText());

        query = "'copil\"';";
        result = runQuery(query);

        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("copil\"", result.at("/query/wrap/key").asText());

        query = "\"copil'\";";
        result = runQuery(query);

        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("copil'", result.at("/query/wrap/key").asText());

        query = "\"copil\"\"\";";
        result = runQuery(query);

        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("copil\"", result.at("/query/wrap/key").asText());

        query = "'copil''';";
        result = runQuery(query);

        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("copil'", result.at("/query/wrap/key").asText());

        query = "\"22\\\"-inch\";";
        result = runQuery(query);

        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("22\\\"-inch", result.at("/query/wrap/key").asText());


        // pe aici am ramas! lamureste te cu ghilimelele! 

        query = "'a''.+?';";
        result = runQuery(query);

        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("a'.+?", result.at("/query/wrap/key").asText());



        query = "\"a\"\".+?\";";
        result = runQuery(query);

        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("a\".+?", result.at("/query/wrap/key").asText());



        query = "\"a.+?\"";
        result = runQuery(query);

        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("a.+?", result.at("/query/wrap/key").asText());

        query = "\"a\\.\"";
        result = runQuery(query);

        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("a\\.", result.at("/query/wrap/key").asText());

        query = "\"a\\.\\+\\?\\\\\"";
        result = runQuery(query);

        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("a\\.\\+\\?\\\\", result.at("/query/wrap/key").asText());
    }


    @Test
    public void testRegexWhiteSpace () throws JsonProcessingException {
        // Escape regex symbols
        query = "\"a b\"";
        result = runQuery(query);

        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("a b", result.at("/query/wrap/key").asText());
    }

}
