package de.ids_mannheim.korap.query.test.poliqarpplus;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Ignore;

import de.ids_mannheim.korap.query.parse.poliqarpplus.PoliqarpPlusLexer;
import de.ids_mannheim.korap.query.parse.poliqarpplus.PoliqarpPlusParser;


import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.lang.reflect.*;
//import java.lang.*;

/**
 * Tests for PQ+ grammar parsing.
 */

public class PQGrammarTest {
    String query;
    Lexer lexer = new PoliqarpPlusLexer((CharStream) null);
    ParserRuleContext tree = null;
    @Test
    public void squoutes_verbatim () {

    	 	 assertEquals(
    	 	 "(request (query (segment (token (key (verbatim ' copil '))))) <EOF>)", 
    	 	 treeString("'copil'")
    	 	 );

              
            assertEquals(
    	 	 "(request (query ' ' ') <EOF>)",   // not parsing the query
    	 	 treeString("'''")
    	 	 );
                assertEquals(
    	 	 "(request (query ' ' -key ') <EOF>)",  // not parsing the query
    	 	 treeString("''-key'")
    	 	 );


            assertEquals(
                "(request (query (segment (token (key (verbatim ' \\' '))))) <EOF>)",  
                treeString("'\\''")
                );
    }
    
    @Test
    public void layerWithFlag () {
        assertEquals(
                "(request (query (segment (token [ (term (foundry mate) (flag /x) (termOp =) (key Baum) (flag /i)) ]))) <EOF>)",  
                treeString("[mate/x=Baum/i]")
                );
    };
    
    @Test
    public void dquoutes () {


        // how it behaves
      assertEquals(
        "(request (query (segment (token (key (regex \"\"))))) <EOF>)",
        treeString("\"\"\"")
        );
       // how it should behave
        assertNotEquals(
    	 	 "(request (query \"\"\") <EOF>)",   // not parsing the query
    	 	 treeString("\"\"\"")
    	 	 );
        assertEquals(
        "(request (query (sequence (segment (token (key (regex \"\")))) (segment (token (key -key))))) <EOF>)",  // see different behaviour of " and '; for ", the query is parsed and an empty regex is generated
        treeString("\"\"-key\"")
        );
        assertEquals(
            "(request query <EOF>)",  // see different behaviour of " and '; for ", the query is parsed and an empty regex is generated
            treeString("\"?\"")
            );
      assertEquals(
          "(request (query (segment (token (key (verbatim ' \\' '))))) <EOF>)",  // not parsing the query
          treeString("'\\''")
          );
}
@Test
    public void spantest () {
        assertEquals(
            "(request (query < cnx / c ! = vp ! ! >) <EOF>)",  // not parsing the query
            treeString("<cnx/c!=vp!!>")
            );
    
    }
    private String treeString (String query) {
        try {
            Method startRule = PoliqarpPlusParser.class.getMethod("request");
            ANTLRInputStream input = new ANTLRInputStream(query);
            lexer.setInputStream(input);
            PoliqarpPlusParser parser = new PoliqarpPlusParser(new CommonTokenStream(lexer));

            // Get starting rule from parser
            tree = (ParserRuleContext) startRule.invoke(parser, (Object[]) null);
            return Trees.toStringTree(tree, parser);
        }
        catch (Exception e) {
            System.err.println(e);
        };
        return "";
    }
}
