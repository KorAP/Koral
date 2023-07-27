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
    public void testRegex () throws JsonProcessingException {
        query = "\"\"\"";
        result = runQuery(query);
        assertEquals("", result.at("/query/wrap/key").asText());
        assertNotEquals(302, result.at("/errors/0/0").asInt());

        query = "\"\"copil\"";
        result = runQuery(query);
        assertEquals("", result.at("/query/wrap/key").asText());
        assertEquals(302, result.at("/errors/0/0").asInt());

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
        
        
        query = "\"?\"";
        result = runQuery(query);
        assertEquals(302, result.at("/errors/0/0").asInt());
        
      
    }


    @Test
    public void testRegexEscape () throws JsonProcessingException {
        // Escape regex symbols; tests for https://korap.ids-mannheim.de/gerrit/c/KorAP/Koral/+/7375
        
        query = "'22\\'-inch'";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("22'-inch", result.at("/query/wrap/key").asText()); // (no regex escape)

        query = "\"22\\\"-inch\";"; // query = "22\"-inch"; (without Java string escaping)
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("orth", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("22\"-inch", result.at("/query/wrap/key").asText()); // (no regex escape)
        
        query = "[mate/b='22\\'-inch']";
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("mate", result.at("/query/wrap/foundry").asText());
        assertEquals("b", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("22'-inch", result.at("/query/wrap/key").asText()); // (no regex escape)

        query = "[mate/b=\"22\\\"-inch\"];"; // query = "22\"-inch"; (without Java string escaping)
        result = runQuery(query);
        assertEquals("koral:token", result.at("/query/@type").asText());
        assertEquals("koral:term", result.at("/query/wrap/@type").asText());
        assertEquals("type:regex", result.at("/query/wrap/type").asText());
        assertEquals("mate", result.at("/query/wrap/foundry").asText());
        assertEquals("b", result.at("/query/wrap/layer").asText());
        assertEquals("match:eq", result.at("/query/wrap/match").asText());
        assertEquals("22\"-inch", result.at("/query/wrap/key").asText()); // (no regex escape)
        
       

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

        query = "\"a\\.\\+\\?\\\\\""; //query = "a\.\+\?\\"
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
