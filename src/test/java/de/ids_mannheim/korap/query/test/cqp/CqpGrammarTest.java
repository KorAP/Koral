package de.ids_mannheim.korap.query.test.cqp;

import static org.junit.Assert.*;
import org.junit.Test;

import de.ids_mannheim.korap.query.parse.cqp.CQPLexer;
import de.ids_mannheim.korap.query.parse.cqp.CQPParser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.lang.reflect.*;
import java.lang.*;

/**
 * Tests for CQP grammar parsing.
 */
public class CqpGrammarTest {

    String query;
    Lexer lexer = new CQPLexer((CharStream) null);
    ParserRuleContext tree = null;
    
    @Test
    public void testRegex () {
        assertEquals(
            "(request (query (segment (token (regex \"copil\")))) ;)",
            treeString("\"copil\";")
            );
    };

    @Test
    public void testRegex2 () {
        assertEquals(
            "(request (query (segment (token (regex \"copil.*\")))) ;)",
            treeString("\"copil.*\";")
            );
    };
    
    private String treeString (String query) {
        try {
            Method startRule = CQPParser.class.getMethod("request");
            ANTLRInputStream input = new ANTLRInputStream(query);
            lexer.setInputStream(input);
            CQPParser parser = new CQPParser(new CommonTokenStream(lexer));

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
