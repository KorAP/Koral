package de.ids_mannheim.korap.query.serialize;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO replace AqlLexer with lexer for your Antlr4 grammar!
import de.ids_mannheim.korap.query.parse.annis.AqlLexer;
//TODO replace AqlParser with parser for your Antlr4 grammar!
import de.ids_mannheim.korap.query.parse.annis.AqlParser;
import de.ids_mannheim.korap.query.serialize.util.KoralObjectGenerator;
import de.ids_mannheim.korap.query.serialize.util.StatusCodes;

/**
 * Map representation of syntax tree as returned by ANTLR
 * 
 * @author Joachim Bingel (bingel@ids-mannheim.de)
 * @version 0.3.0
 * @since 0.1.0
 */
public class TreeTemplate extends Antlr4AbstractQueryProcessor {
    private static Logger log = LoggerFactory.getLogger(TreeTemplate.class);


    /**
     * 
     * @param tree
     *            The syntax tree as returned by ANTLR
     * @param parser
     *            The ANTLR parser instance that generated the parse
     *            tree
     */
    public TreeTemplate (String query) {
        KoralObjectGenerator.setQueryProcessor(this);
        process(query);
    }


    @Override
    public void process (String query) {
        ParseTree tree = parseQuery(query);
        super.parser = this.parser;
        if (tree != null) {
            log.debug("ANTLR parse tree: " + tree.toStringTree(parser));
            processNode(tree);
        }
        else {
            addError(StatusCodes.MALFORMED_QUERY, "Could not parse query >>> "
                    + query + " <<<.");
        }
    }


    private void processNode (ParseTree node) {
        // Top-down processing
        if (visited.contains(node))
            return;
        else
            visited.add(node);

        String nodeCat = getNodeCat(node);
        openNodeCats.push(nodeCat);

        stackedObjects = 0;

        if (verbose) {
            System.err.println(" " + objectStack);
            System.out.println(openNodeCats);
        }

        /*
         ****************************************************************
         **************************************************************** 
         *          Processing individual node categories               *
         ****************************************************************
         ****************************************************************
         */
        objectsToPop.push(stackedObjects);

        /*
         ****************************************************************
         **************************************************************** 
         *  Recursion until 'request' node (root of tree) is processed  *
         ****************************************************************
         ****************************************************************
         */
        for (int i = 0; i < node.getChildCount(); i++) {
            ParseTree child = node.getChild(i);
            processNode(child);
        }

        /*
         ***************************************************************
         ***************************************************************
         * Stuff that happens after processing the children of a node  *
         ***************************************************************
         ***************************************************************
         */
        for (int i = 0; i < objectsToPop.pop(); i++) {
            objectStack.pop();
        }

        openNodeCats.pop();

    }


    @SuppressWarnings("unused")
    private void putIntoSuperObject (LinkedHashMap<String, Object> object) {
        putIntoSuperObject(object, 0);
    }


    @SuppressWarnings({ "unchecked" })
    private void putIntoSuperObject (LinkedHashMap<String, Object> object,
            int objStackPosition) {
        if (objectStack.size() > objStackPosition) {
            ArrayList<Object> topObjectOperands = (ArrayList<Object>) objectStack
                    .get(objStackPosition).get("operands");
            topObjectOperands.add(0, object);

        }
        else {
            requestMap.put("query", object);
        }
    }


    private ParserRuleContext parseQuery (String q) {
        // TODO replace AqlLexer with lexer for your Antlr4 grammar!
        Lexer qlLexer = new AqlLexer((CharStream) null);
        ParserRuleContext tree = null;
        // Like p. 111
        try {
            // Tokenize input data
            ANTLRInputStream input = new ANTLRInputStream(q);
            qlLexer.setInputStream(input);
            CommonTokenStream tokens = new CommonTokenStream(qlLexer);
            // TODO replace AqlParser with parser for your Antlr4
            // grammar!
            parser = new AqlParser(tokens);

            // Don't throw out erroneous stuff
            parser.setErrorHandler(new BailErrorStrategy());
            parser.removeErrorListeners();

            // Get starting rule from parser
            // TODO replace AqlParser with parser for your Antlr4
            // grammar!
            Method startRule = AqlParser.class.getMethod("start");
            tree = (ParserRuleContext) startRule
                    .invoke(parser, (Object[]) null);
        }

        // Some things went wrong ...
        catch (Exception e) {
            System.err.println(e.getMessage());
        }

        // Return the generated tree
        return tree;
    }
}