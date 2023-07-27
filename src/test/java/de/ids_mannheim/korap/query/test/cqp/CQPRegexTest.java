package de.ids_mannheim.korap.query.test.cqp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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
    public void testRegexError () throws JsonProcessingException {
       
        query = "\"?\"";
        result = runQuery(query);
        assertEquals(302, result.at("/errors/0/0").asInt());
        assertEquals("Failing to parse at symbol: '\"'", result.at("/errors/0/1").asText());
       
        query = "\"\\\"";
        result = runQuery(query);
        assertEquals(302, result.at("/errors/0/0").asInt());
        assertEquals("Failing to parse at symbol: '\"'", result.at("/errors/0/1").asText());
        
        query = "\"\"\"";
        result = runQuery(query);
        assertEquals(302, result.at("/errors/0/0").asInt());
        assertEquals("Failing to parse at symbol: '\"'", result.at("/errors/0/1").asText());

        query = "''';"; 
        result = runQuery(query);
        assertEquals(302, result.at("/errors/0/0").asInt());
        assertEquals("Failing to parse at symbol: '\''", result.at("/errors/0/1").asText());
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


        
        
        query = "\"a\\.\\+\\?\\\\\""; //query = "a\.\+\?\\"
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("a\\.\\+\\?\\", result.at("/query/wrap/key").asText());
        
        query = "\"a\\.\"";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("a\\.", result.at("/query/wrap/key").asText());

        // escape doublequoutes and singlequoutes
        query = "'\"';";  // query= '"';
        result = runQuery(query);

        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("\"", result.at("/query/wrap/key").asText());

        query = "'copil\"';";  // query= 'copil"';
        result = runQuery(query);

        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("copil\"", result.at("/query/wrap/key").asText());

        query = "\"copil'\";";  // query= "copil'";
        result = runQuery(query);

        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("copil'", result.at("/query/wrap/key").asText());

        query = "\"copil\"\"\";"; //  query= "copil"""; same as query= 'copil"';
        //escape doublequoutes by doubling them: we are looking for string: copil"
        result = runQuery(query);

        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("copil\"", result.at("/query/wrap/key").asText());

        query = "'copil''';"; // same as  query= "copil'";
        // escape quoutes by doubling them
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("copil'", result.at("/query/wrap/key").asText());

        query = "\"22\\\"-inch\";"; // query = "22"-inch"
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("22\"-inch", result.at("/query/wrap/key").asText());


        query = "'a''.+?';"; //query = 'a''.+?'
        result = runQuery(query);

        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("a'.+?", result.at("/query/wrap/key").asText());



        query = "\"a\"\".+?\";"; //query = "a"".?"
        result = runQuery(query);

        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("a\".+?", result.at("/query/wrap/key").asText());



        query = "\"a.+?\"";    // query = "a.+?"
        result = runQuery(query);

        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("a.+?", result.at("/query/wrap/key").asText());

        query = "\"a\\.\"";  //query = "a\."
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("a\\.", result.at("/query/wrap/key").asText());

     
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
