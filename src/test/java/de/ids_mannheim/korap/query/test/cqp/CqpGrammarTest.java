package de.ids_mannheim.korap.query.test.cqp;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Ignore;

import de.ids_mannheim.korap.query.parse.cqp.CQPLexer;
import de.ids_mannheim.korap.query.parse.cqp.CQPParser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.lang.reflect.*;
//import java.lang.*;

/**
 * Tests for CQP grammar parsing.
 */
public class CqpGrammarTest {

    String query;
    Lexer lexer = new CQPLexer((CharStream) null);
    ParserRuleContext tree = null;
    @Test
    public void testRegex$1 () {

    	 	 assertEquals(
    	 	 "(request (query (segment (token (key (regex \"copil\"))))) ;)", 
    	 	 treeString("\"copil\";")
    	 	 );
    };

    @Test
    public void testRegex$2 () {

    	 	 assertEquals( 
    	 	 "(request (query (segment (token (key (regex \"copil.*\"))))) ;)",
    	 	 treeString("\"copil.*\";")
    	 	 );
    };

    @Test
    public void testRegex$3 () {

    	 	 assertEquals( 
    	 	 "(request (query (segment (token (key (regex \"copil(ul|a)\"))))) ;)", 
    	 	 treeString("\"copil(ul|a)\";")
    	 	 );
    };

    @Test
    public void testRegexQuoute () {

    	 	assertEquals( 
    		"(request (query (segment (token (key (regex 'copil(ul|a)'))))) ;)", 
    		treeString("'copil(ul|a)';")
    		);
    };
    @Test
    public void testRegexARond () {

    	 	assertEquals( 
    		"(request (query (segment (token (key (regex 'elena@racai.ro'))))) ;)", 
    		treeString("'elena@racai.ro';")
    		);
    };
    @Test
    public void testRegexFlags () {

    	 	 assertEquals( 
    		"(request (query (segment (token (key (regex \"copil\")) (flag %ldc)))) ;)", 
    		treeString("\"copil\" %ldc;")
    		);
    		
    };
	@Test
    public void testRegexVerbatim () {

     
    	assertEquals( 
    		"(request (query (segment (token (key (regex \"D'Man\\n\")) (flag %l)))) ;)", 
    		treeString("\"D'Man\n\"%l;")
    		);
			assertEquals( 
    		"(request (query (segment (token [ (term (foundry mate) / (layer b) (termOp =) (key (regex \"D'Man\\n\")) (flag %l)) ]))) ;)", 
    		treeString("[mate/b=\"D'Man\n\"%l];")
    		);
   
			assertEquals( 
    		"(request (query (segment (token [ (term (foundry mate) / (layer b) (termOp =) (key (regex 'D\\'Ma \\\\nn')) (flag %l)) ]))))", 
    		treeString("[mate/b='D\\'Ma \\\\nn'%l]")
    		);
			
    };
    @Test
    public void testRegexEscapedQuoutes () {

        // escape by doubling the double quote 	
    	assertEquals( 
    		"(request (query (segment (token (key (regex \"22\"\"-inch\"))))) ;)", 
    		treeString("\"22\"\"-inch\";")
    		);
    	// escape by using single quotes to encapsulate expressions that contain double quotes
	 	assertEquals( 
	    		"(request (query (segment (token (key (regex '22\"-inch'))))) ;)", 
	    		treeString("'22\"-inch\';")
	    		);
    	assertEquals( // this one is working in https://cqpweb.lancs.ac.uk/familyx/
        		"(request (query (segment (token (key (regex \"22\\\"-inch\"))))) ;)", 
        		treeString("\"22\\\"-inch\";")
        		);
    
    };

    @Test
    public void testRegexEscapedSquoutes () {
    		// escape by doubling the single quote
    	 	assertEquals( 
    		"(request (query (segment (token (key (regex 'anna''s house'))))) ;)", 
    		treeString("'anna''s house';")
    		);
    	 	//escape by using double quotes to encapsulate expressions that contain single quotes
    	 	assertEquals( 
    	    		"(request (query (segment (token (key (regex \"anna's house\"))))) ;)", 
    	    		treeString("\"anna's house\";")
    	    		);
    	 	assertEquals( 
    	    		"(request (query (segment (token (key (regex 'anna\\'s house'))))) ;)", 
    	    		treeString("'anna\\'s house';")
    	    		);
    };
    
    @Test
    public void testTerm1 () {
    		
    		assertEquals(
    		"(request (query (segment (token [ (term (layer pos) (termOp =) (key (regex \"JJ\"))) ]))) ;)", 
    		treeString("[pos = \"JJ\"];")
    		);
    };

    @Test
    public void testTermFlag () {

    	 	assertEquals( 
    		"(request (query (segment (token [ (term (layer lemma) (termOp =) (key (regex \"pole\")) (flag %c)) ]))) ;)", 
    		treeString("[lemma = \"pole\" %c];")
    		);
    };

    @Test
    public void testBoolOp1 () {
    		assertEquals( 
    		"(request (query (segment (token [ (termGroup (term (layer lemma) (termOp =) (key (regex \"under.+\"))) (boolOp &) (term (layer pos) (termOp =) (key (regex \"V.*\")))) ]))) ;)", 
    		treeString(" [lemma=\"under.+\" & pos=\"V.*\"];")
    		);
    };

    @Test
    public void testNegTerm () {

    	 	// do more tests for negation!! 
    		assertEquals( 
    	 	 "(request (query (segment (token [ (termGroup (term ( (term (layer lemma) (termOp =) (key (regex \"under.+\"))) )) (boolOp &) (term ! ( (term (layer pos) (termOp =) (key (regex \"V.*\"))) ))) ]))) ;)", 
    	 	 treeString("[(lemma=\"under.+\") & !(pos=\"V.*\")];")
    	 	 );
    };

    @Test
    public void testNegTermGroup () {

    	 	//do more tests! 
    	    assertEquals(
    	 	 "(request (query (segment (token [ (termGroup (term (layer lemma) (termOp =) (key (regex \"go\"))) (boolOp &) (termGroup ! ( (termGroup (term (layer word) (termOp =) (key (regex \"went\")) (flag %c)) (boolOp |) (term (layer word) (termOp =) (key (regex \"gone\")) (flag %c))) ))) ]))) ;)", 
    	 	 treeString(" [lemma=\"go\" & ! (word=\"went\"%c | word = \"gone\" %c)];")
    	 	 );
    };
    @Test
    public void testValue () {

    	     assertEquals(
    	     "(request (query (segment (token [ (term (foundry mate) / (layer m) (termOp =) (key (regex 'temp')) : (value (regex 'pres'))) ]))))", 
    	     treeString("[mate/m='temp':'pres']")
    	     );
    	    };
    @Test
    public void testRegexBoolOp1 () {

    	 	assertEquals(
    		"(request (query (sequence (segment (token (key (regex \"on\")))) (segment (token (key (regex \"and\")))) (segment (token (key (regex \"on|off\")))))) ;)", 
    		treeString("\"on\" \"and\" \"on|off\";")
    		);
    };
    
    
    @Test
    public void testVerbatim () {

    	 	assertEquals(
    		"(request (query (segment (token [ (term (foundry mate) / (layer b) (termOp =) (key (regex \"D\\\\Ma \\\\nn\"))) ]))))", 
    		treeString("[mate/b=\"D\\\\Ma \\\\nn\"]")
    		);
    };
    
    @Test
    public void testRegexBoolOp2 () {

    	 	assertEquals( 
    	 	"(request (query (sequence (segment (token (key (regex \"el\")))) (segment (group ( (disjunction (segment (token (key (regex \"bueno\")))) | (segment (token (key (regex \"malo\"))))) ))) (segment (token [ (term (layer pos) (termOp ! =) (key (regex \"N.*\"))) ])))) ;)", 
    	 	treeString("\"el\" (\"bueno\"|\"malo\") [pos!=\"N.*\"];")
    	 	);
    };

    @Test
    public void testRegexDisjunction1 () {

    	 	 assertEquals( 
    	 	 "(request (query (sequence (segment (token (key (regex \"es\")))) (segment (group ( (disjunction (sequence (segment (token (key (regex \"el\")))) (segment (token (key (regex \"bueno\"))))) | (sequence (segment (token (key (regex \"el\")))) (segment (token (key (regex \"malo\")))))) ))) (segment (token [ (term (layer pos) (termOp ! =) (key (regex \"N.*\"))) ])))) ;)", 
    	 	 treeString("\"es\" (\"el\" \"bueno\"|\"el\" \"malo\") [pos!=\"N.*\"];")
    	 	 );
    };

    @Test
    public void testRegexDisjunction2 () {

    	 	assertEquals( 
    		"(request (query (sequence (segment (token (key (regex \"in\")))) (segment (token (key (regex \"any|every\")))) (segment (token [ (term (layer pos) (termOp =) (key (regex \"NN\"))) ])))) ;)",
    		treeString("\"in\" \"any|every\" [pos=\"NN\"];")
    		);
    };

    @Test
    public void testTermOpNegation1 () {

    	 	assertEquals( 
    		"(request (query (segment (token [ (term ! (layer pos) (termOp =) (key (regex \"ADJA\"))) ]))) ;)",
    		treeString("[!pos=\"ADJA\"];")
    		);
    };

    @Test
    public void testRegexDisjunction3 () {

    	 	assertEquals( 
    		"(request (query (disjunction (segment (token (key (regex \"on\")))) | (segment (token (key (regex \"off\")))))) ;)",
    		treeString("\"on\"|\"off\";")
    		);
    };

    @Test
    public void testTermDisjunction () {

    	 	assertEquals( 
    		"(request (query (segment (token [ (termGroup (term (layer word) (termOp =) (key (regex \"on\"))) (boolOp |) (term (layer word) (termOp =) (key (regex \"off\")))) ]))) ;)",
    		treeString("[word= \"on\"|word = \"off\"];")
    		);
    };

    @Test
    public void testTermSequence () {

    	 	assertEquals( 
    		"(request (query (sequence (segment (token [ (term (layer pos) (termOp =) (key (regex \"IN\"))) ])) (segment (token [ (term (layer pos) (termOp =) (key (regex \"DT\"))) ]) (repetition (kleene ?))) (segment (group ( (sequence (segment (token [ (term (layer pos) (termOp =) (key (regex \"RB\"))) ]) (repetition (kleene ?))) (segment (token [ (term (layer pos) (termOp =) (key (regex \"JJ.*\"))) ]))) )) (repetition (kleene *))) (segment (token [ (term (layer pos) (termOp =) (key (regex \"N.*\"))) ]) (repetition (kleene +))))) ;)",
    		treeString("[pos = \"IN\"] [pos = \"DT\"]? ([pos = \"RB\"]? [pos = \"JJ.*\"])* [pos = \"N.*\"]+;")
    		);
    };

    @Test
    public void testTermOpNegation2 () {

    	 	assertEquals( 
    		"(request (query (sequence (segment (token [ (term (layer pos) (termOp ! =) (key (regex \"N.*\"))) ])) (segment (token [ (term ! (layer pos) (termOp =) (key (regex \"V.*\"))) ])))) ;)",
    		treeString(" [pos!=\"N.*\"] [!pos=\"V.*\"];")
    		);
    };

    @Test
    public void testRepetitionRange () {

    	 	assertEquals( 
    		"(request (query (sequence (segment (group ( (disjunction (sequence (segment (token [ (term (layer pos) (termOp =) (key (regex \"APPR\"))) ])) (segment (token [ (term (layer pos) (termOp =) (key (regex \"ART\"))) ]))) | (segment (token [ (term (layer pos) (termOp =) (key (regex \"ARPRART\"))) ]))) ))) (segment (group ( (sequence (segment (token [ (term (layer pos) (termOp =) (key (regex \"ADJD|ADV\"))) ]) (repetition (kleene ?))) (segment (token [ (term (layer pos) (termOp =) (key (regex \"ADJA\"))) ]))) )) (repetition (kleene *))) (segment (token [ (term (layer pos) (termOp =) (key (regex \"NN\"))) ]) (repetition (range { , (max 1) }))))) ;)",
    		treeString(" ([pos = \"APPR\"] [pos=\"ART\"] | [pos = \"ARPRART\"]) ([pos=\"ADJD|ADV\"]? [pos=\"ADJA\"])* [pos=\"NN\"]{,1};")
    		);
    };

    @Test
    public void testEmptyToken () {

    	 	assertEquals( 
    		"(request (query (sequence (segment (token (key (regex \"right\")))) (segment (emptyTokenSequence (emptyToken [ ]) (repetition (kleene ?)))) (segment (token (key (regex \"left\")))))) ;)",
    		treeString("\"right\" []? \"left\";")
    		);
    };

    @Test
    public void testEmptyTokenWithin () {

    	 	assertEquals( 
    		"(request (query (sequence (segment (token (key (regex \"no\")))) (segment (token (key (regex \"sooner\")))) (segment (emptyTokenSequence (emptyToken [ ]) (repetition (kleene *)))) (segment (token (key (regex \"than\")))))) (within within s) ;)",
    		treeString("\"no\" \"sooner\" []* \"than\" within s;")
    		);
    };

    @Test
    public void testEmptyTokenRange () {

    	 	assertEquals( 
    		"(request (query (sequence (segment (token (key (regex \"as\")))) (segment (emptyTokenSequence (emptyToken [ ]) (repetition (range { (min 1) , (max 3) })))) (segment (token (key (regex \"as\")))))) ;)",
    		treeString(" \"as\" []{1,3} \"as\";")
    		);
    };

    @Test
    public void testEmptyTokenMax () {

    	 	assertEquals( 
    		"(request (query (sequence (segment (token (key (regex \"as\")))) (segment (emptyTokenSequence (emptyToken [ ]) (repetition (range { (max 3) })))) (segment (token (key (regex \"as\")))))) ;)",
    		treeString("\"as\" []{3} \"as\";")
    		);
    };

    @Test
    public void testSequenceDisj () {

    	 	 assertEquals( 
    		"(request (query (disjunction (sequence (segment (token (key (regex \"left\")))) (segment (token (key (regex \"to\")))) (segment (token (key (regex \"right\"))))) | (sequence (segment (token (key (regex \"right\")))) (segment (token (key (regex \"to\")))) (segment (token (key (regex \"left\"))))))) ;)",
    		treeString("\"left\" \"to\" \"right\" | \"right\" \"to\" \"left\";")
    		);
    };



    @Test
    public void testSpanClassIDTarget () {

    	 	assertEquals( 
    		"(request (query (sequence (segment (token (key (regex \"in\")))) (segment (spanclass @ (token [ (term (layer pos) (termOp =) (key (regex \"DT\"))) ]))) (segment (token [ (term (layer lemma) (termOp =) (key (regex \"case\"))) ])))) ;)",
    		treeString("\"in\" @[pos=\"DT\"] [lemma=\"case\"];")
    		);
    	 	assertEquals( 
    	    		"(request (query (sequence (segment (spanclass @ (token [ (term (layer base) (termOp =) (key (regex 'Mann'))) ]))) (segment (spanclass @1 (token [ (term (layer orth) (termOp =) (key (regex 'Frau'))) ]))))))",
    	    		treeString("@[base='Mann']@1[orth='Frau']")
    	    		);
    	 	
    };


    @Test
    public void testSpanClassRange () {

    	 	// the target (@) should be the rightmost JJ from the at least 2 repetitions!!
    		assertEquals( 
    		"(request (query (sequence (segment (token [ (term (layer pos) (termOp =) (key (regex \"DT\"))) ])) (segment (group ( (sequence (segment (spanclass @ (token [ (term (layer pos) (termOp =) (key (regex \"JJ.*\"))) ]))) (segment (token (key (regex \",\"))) (repetition (kleene ?)))) )) (repetition (range { (min 2) , }))) (segment (token [ (term (layer pos) (termOp =) (key (regex \"NNS\"))) ])))) ;)",
    		treeString("[pos=\"DT\"] (@[pos=\"JJ.*\"] \",\"?){2,} [pos=\"NNS\"];")
    		);
    };


    @Test
    public void testSpanClassIDKeyword () {

    	 	assertEquals( 
    	 	"(request (query (sequence (segment (token (key (regex \"in\")))) (segment (spanclass @ (token [ (term (layer pos) (termOp =) (key (regex \"DT\"))) ]))) (segment (spanclass @1 (token [ (term (layer pos) (termOp =) (key (regex \"J.*\"))) ])) (repetition (kleene ?))) (segment (token [ (term (layer lemma) (termOp =) (key (regex \"case\"))) ])))) ;)",
    		treeString("\"in\" @[pos=\"DT\"] @1[pos=\"J.*\"]? [lemma=\"case\"];")
    		);
    };
    @Ignore
    @Test
    public void testSpanClassNested () {
//this will fail because spanclass can only be used with tokens!!
	 	assertEquals( 
	 	"(request (query (segment (spanclass @ (segment (group ( (sequence (segment (token (key (regex \"el\")))) (segment (spanclass @1 (segment (token [ (term (layer pos) (termOp =) (key (regex \"A.*\"))) ]))))) )))))) ;)",
		treeString("@(\"el\" @1[pos=\"A.*\"]);")
		);
};
   

    @Test
    public void testEmptyTokenSequenceClass () {

    	 	assertEquals( 
    	 	"(request (query (sequence (segment (token (key (regex \"in\")))) (segment (spanclass @ (segment (emptyTokenSequence (emptyToken [ ]))))) (segment (spanclass @1 (token [ (term (layer pos) (termOp =) (key (regex \"J.*\"))) ])) (repetition (kleene ?))) (segment (token [ (term (layer lemma) (termOp =) (key (regex \"case\"))) ])))) ;)",
    		treeString("\"in\" @[] @1[pos=\"J.*\"]? [lemma=\"case\"];")
    		);
    };


    @Test
    public void testSpanClassTermLabel () {

    	 	assertEquals( 
    	 	"(request (query (segment (spanclass (label adj) : (segment (token [ (term (layer pos) (termOp =) (key (regex \"JJ.*\"))) ]))))) ;)",
    		treeString("adj: [pos=\"JJ.*\"];")
    		);
    };

    @Test
    public void testEmptySequenceLabel1 () {

    	 	assertEquals( 
    		"(request (query (sequence (segment (token [ (term (layer pos) (termOp =) (key (regex \"DT\"))) ])) (segment (spanclass (label a) : (segment (emptyTokenSequence (emptyToken [ ]) (repetition (kleene ?)))))) (segment (token [ (term (layer pos) (termOp =) (key (regex \"NNS?\"))) ])))) ;)",
    		treeString("[pos=\"DT\"] a:[]? [pos=\"NNS?\"];")
    		);
    };

    @Test
    public void testEmptySequenceLabel2 () {

    	 	assertEquals( 
    	 	"(request (query (segment (spanclass (label a) : (sequence (segment (emptyTokenSequence (emptyToken [ ]))) (segment (token (key (regex \"and\")))) (segment (spanclass (label b) : (segment (emptyTokenSequence (emptyToken [ ]))))))))) ;)",
    		treeString("a:[] \"and\" b:[];")
    		);
    };


    @Test
    public void testEmptySequenceLabel3 () {

    	 	assertEquals( 
    		"(request (query (segment (spanclass (label a) : (sequence (segment (token [ (term (layer pos) (termOp =) (key (regex \"PP\"))) ])) (segment (spanclass (label c) : (segment (emptyTokenSequence (emptyToken [ ]) (repetition (range { (min 0) , (max 5) })))))) (segment (spanclass (label b) : (segment (token [ (term (layer pos) (termOp =) (key (regex \"VB.*\"))) ])))))))) ;)",
    		treeString("a:[pos=\"PP\"] c:[]{0,5} b:[pos = \"VB.*\"];")
    		);
    };

    @Test
    public void testRegex$37 () {

    	 	assertEquals( 
    	 	"(request (query (segment (token [ (term (layer _.pos) (termOp =) (key (regex \"NPS\"))) ]))) ;)",
    		treeString(" [_.pos = \"NPS\"];")
    		);
    };

    

/////////////////// **** STRUCT TESTS****	
    		
    @Test
    public void testRegion () {

    	assertEquals( 
        		"(request (query (sequence (segment (token [ (term (layer base) (termOp =) (key (regex \"Mann\"))) ])) (segment (region / region [ (span < (foundry cnx) / (layer c) (termOp =) (skey vp) >) ])))))",
        		treeString("[base=\"Mann\"] /region[<cnx/c=vp>]")); 	
    	assertEquals( 
    		"(request (query (sequence (segment (token [ (term (layer base) (termOp =) (key (regex \"Mann\"))) ])) (segment (region / region [ (span (skey vp)) ])))))",
    		treeString("[base=\"Mann\"] /region[vp]"));
			assertEquals( 
        		"(request (query (segment (region / region [ (span < (foundry cnx) / (layer c) (termOp ! =) (skey vp) ( (termGroup (term (layer class) (termOp ! =) (key (regex \"header\"))) (boolOp &) (term (layer id) (termOp =) (key (regex \"7\")))) ) >) ]))))",
        		treeString("/region[<cnx/c!=vp (class!=\"header\" & id=\"7\")>]")); 

    	
    	
    };


    @Ignore
	@Test
    public void testStructDisj () {

    	 	// structural attributes as spans!!! not working
    		assertEquals( 
    	 	 "(request (query (sequence (segment (group ( (disjunction (segment (span < np >)) | (segment (span < np1 >)) | (segment (span < np2 >))) ))) (segment (emptyTokenSequence (emptyToken [ ]) (repetition (kleene *)))) (segment (group ( (disjunction (segment (span < / np2 >)) | (segment (span < / np1 >)) | (segment (span < / np >))) ))))) ;)",
    		treeString(" (<np>|<np1>|<np2>) []* (</np2>|</np1>|</np>);")
    		);
    };

    @Test
    public void testStructStartsWith () {

    	 	assertEquals( 
    		"(request (query (struct (startswith (span < (skey s) >) (segment (token [ (term (layer pos) (termOp =) (key (regex \"VBG\"))) ]))))) ;)",
    		treeString(" <s> [pos=\"VBG\"];")
    		);
    };
    @Test
    public void testStructEndsWith () {

    	 	assertEquals( 
    		"(request (query (struct (endswith (sequence (segment (token [ (term (layer pos) (termOp =) (key (regex \"VBG\"))) ])) (segment (token [ (term (layer pos) (termOp =) (key (regex \"SENT\"))) ]) (repetition (kleene ?)))) (span < / (foundry base) / (layer s) (termOp =) (skey s) >)))) ;)",
    		treeString(" [pos = \"VBG\"] [pos = \"SENT\"]? </base/s=s>;"));

    };
    @Test
    public void testStructContains1 () {

    	 	assertEquals( 
    		"(request (query (struct (matches (span < (skey np) >) (sequence (segment (emptyTokenSequence (emptyToken [ ]) (repetition (kleene *)))) (segment (group ( (sequence (segment (token [ (term (layer pos) (termOp =) (key (regex \"JJ.*\"))) ])) (segment (emptyTokenSequence (emptyToken [ ]) (repetition (kleene *))))) )) (repetition (range { (min 3) , })))) (span < / (skey np) >)))) ;)",
    		treeString(" <np> []* ([pos=\"JJ.*\"] []*){3,} </np>;")
    		);
    };
    
    @Test
    public void testStructContains1bis () {

    	 	assertEquals( 
    		"(request (query (struct (isaround (span < (skey np) >) (emptyTokenSequenceAround (emptyTokenSequence (emptyToken [ ]) (repetition (kleene *)))) (segment ( (segment (token [ (term (layer pos) (termOp =) (key (regex \"JJ.*\"))) ])) ) (repetition (range { (min 3) , }))) (emptyTokenSequenceAround (emptyTokenSequence (emptyToken [ ]) (repetition (kleene *)))) (span < / (skey np) >)))) ;)",
    		treeString(" <np> []* ([pos=\"JJ.*\"]){3,} []* </np>; #contains (NP, sequence)")
    		);
    };
    
    @Test
    public void testStructContains1tris () {

	 	assertEquals( 
			 "(request (query (struct (isaround (span < (skey np) >) (emptyTokenSequenceAround (emptyTokenSequence (emptyToken [ ]) (repetition (kleene *)))) (segment (token (key (regex \"copil\")))) (emptyTokenSequenceAround (emptyTokenSequence (emptyToken [ ]) (repetition (kleene *)))) (span < / (skey np) >)))) ;)",
		treeString(" <np> []* \"copil\" []* </np>; #contains (NP, copil)"));


		assertEquals( 
			"(request (query (struct (isaround (span < (foundry base) / (layer s) (termOp =) (skey s) >) (emptyTokenSequenceAround (emptyTokenSequence (emptyToken [ ]) (repetition (kleene *)))) (sequence (segment (token (key (regex \"copil\")))) (segment (emptyTokenSequence (emptyToken [ ]))) (segment (token (key (regex \"cuminte\"))))) (emptyTokenSequenceAround (emptyTokenSequence (emptyToken [ ]) (repetition (kleene *)))) (span < / (foundry base) / (layer s) (termOp =) (skey s) >)))))",
		treeString("<base/s=s> []* \"copil\" [] \"cuminte\" []* </base/s=s>")); 
};

    @Test
    public void testStructContains2 () {

    	 	assertEquals( 
    		"(request (query (struct (matches (span < (foundry base) / (layer s) (termOp =) (skey s) >) (segment (token (key (regex \"copil\")))) (span < / (foundry base) / (layer s) (termOp =) (skey s) >)))) ;)",
    		treeString(" <base/s=s> \"copil\" </base/s=s>;")
    		);
    };
    @Test
    public void testStructContains2bis () {

	 	assertEquals( 
		"(request (query (struct (startswith (span < (foundry base) / (layer s) (termOp =) (skey s) >) (segment (token (key (regex \"copil\"))))))) ;)",
		treeString(" <base/s=s> \"copil\" ;")
		);
    };
    
    @Test
    public void testStructContains2tris () {

	 	assertEquals( 
		"(request (query (struct (endswith (segment (token (key (regex \"copil\")))) (span < (foundry base) / (layer s) (termOp =) (skey s) >)))) ;)",
		treeString("\"copil\" <base/s=s>;")
		);
};
@Test
public void testPositionOpsLBoundTerm () {

 	// am nevoie de term in term here!!!! aici am ramas!
	assertEquals( 
	"(request (query (segment (token [ (term ! ( (layer lemma) (termOp =) (key (regex \"copil\")) ) & (position lbound ( (span < (foundry base) / (layer s) (termOp =) (skey s) >) ))) ]))) ;)",
	treeString("[!(lemma=\"copil\") & lbound(<base/s=s>) ];") //

	);
};

@Test
public void testPositionOpsRBoundSegment () {

 	assertEquals( 
	"(request (query (segment (token [ (term (key (regex \"copil\")) & (position rbound ( (span < (foundry base) / (layer s) (termOp =) (skey s) >) ))) ]))) ;)",
	treeString("[\"copil\" & rbound(<base/s=s>)];") 
	);
};

@Test
public void testPositionOpsRBoundSequence () {
		
 	assertEquals( 
	"(request (query (sequence (segment (token [ (term (layer word) (termOp =) (key (regex \"acest\"))) ])) (segment (token [ (term (key (regex \"copil\")) & (position rbound ( (span < (foundry base) / (layer s) (termOp =) (skey s) >) ))) ])))) ;)",
	treeString("[word = \"acest\"][\"copil\" & rbound(<base/s=s>)] ;")
	);
};


////////////////////////////
    @Ignore
	@Test
    public void testStructContains3 () {
// not working!!! not implemented
    	 	assertEquals( 
    		"(request (query (sequence (segment (struct < s >)) (segment (struct < np >)) (segment (emptyTokenSequence (emptyToken [ ]) (repetition (kleene *)))) (segment (struct < / np >)) (segment (emptyTokenSequence (emptyToken [ ]) (repetition (kleene *)))) (segment (struct < np >)) (segment (emptyTokenSequence (emptyToken [ ]) (repetition (kleene *)))) (segment (struct < / np >)) (segment (struct < / s >)))) ;)",
    		treeString(" <s><np>[]*</np> []* <np>[]*</np></s>; #sentence that starts and ends with a noun phrase (NP); startsWith, endsWith;")
    		);
    };


    @Test
    public void testWithinNp () {

    	 	assertEquals( 
    		"(request (query (sequence (segment (token [ (term (layer pos) (termOp =) (key (regex \"NN\"))) ])) (segment (emptyTokenSequence (emptyToken [ ]) (repetition (kleene *)))) (segment (token [ (term (layer pos) (termOp =) (key (regex \"NN\"))) ])))) (within within np) ;)",
    		treeString(" [pos=\"NN\"] []* [pos=\"NN\"] within np;")
    		);
    };



    @Test
    public void testMU0 () {

	 	assertEquals( 
		"(request (query (segment (matching MU ( (meetunion (segment (spanclass meet (segment (token (key (regex \"in\")))))) (segment (token (key (regex \"due\")))) 1 1) )))) ;)",
		treeString("MU(meet \"in\" \"due\" 1 1);")
		);
    };
    @Test
    public void testMU01 () {

	 	assertEquals( 
		"(request (query (segment (matching MU ( (meetunion (segment (spanclass meet (segment (token (key (regex \"in\")))))) (segment (token (key (regex \"due\")))) -1 1) )))) ;)",
		treeString("MU(meet \"in\" \"due\" -1 1);")
		);
    };
    
	@Test
	public void testMU1 () {

    	 	assertEquals( 
    		"(request (query (segment (matching MU ( (meetunion (segment (spanclass meet (segment (token (key (regex \"in\")))))) ( (meetunion (segment (spanclass meet (segment (token (key (regex \"due\")))))) (segment (token (key (regex \"course\")))) 1 1) ) 1 1) )))) ;)",
    		treeString(" MU(meet \"in\" (meet \"due\" \"course\" 1 1) 1 1);")
    		);
    	 	
    	 
    	 	assertEquals( 
    	    		"(request (query (segment (matching MU ( (meetunion (segment (spanclass meet (segment (token (key (regex 'de')))))) ( (meetunion (segment (spanclass meet (segment (token (key (regex 'color')))))) (segment (token (key (regex 'piel')))) 2 2) ) -1 -1) )))))",
    	    		treeString("MU(meet 'de' (meet 'color' 'piel' 2 2) -1 -1)")
    	    		);
    	 	assertEquals( 
    	    		"(request (query (segment (matching MU ( (meetunion (segment (spanclass meet ( (meetunion (segment (spanclass meet (segment (token (key (regex 'color')))))) (segment (token (key (regex 'de')))) 1 1) ))) (segment (token (key (regex 'piel')))) 2 2) )))))",
    	    		treeString("MU(meet (meet 'color' 'de' 1 1) 'piel' 2 2)")
    	    		);
    	 	;
    };

    @Test
    public void testMU2 () {

    	 	assertEquals( 
    	 	"(request (query (segment (matching MU ( (meetunion (segment (spanclass meet (segment (token (key (regex \"in\")))))) ( (meetunion (segment (spanclass meet (segment (emptyTokenSequence (emptyToken [ ]))))) (segment (token (key (regex \"course\")))) 1 1) ) 1 1) )))) ;)",
    		treeString(" MU(meet \"in\" (meet [] \"course\" 1 1) 1 1);")
    		);
    };

    @Test
    public void testMU3 () {

    	 	assertEquals( 
    		"(request (query (segment (matching MU ( (meetunion (segment (spanclass meet ( (meetunion (segment (spanclass meet (segment (token (key (regex \"course\")))))) (segment (token (key (regex \"due\")))) -1 -1) ))) (segment (token (key (regex \"in\")))) -2 -2) )))) ;)",
    		treeString(" MU(meet (meet \"course\" \"due\" -1 -1) \"in\" -2 -2);")
    		);
    };

    @Test
    public void testMU4 () {

    	 	assertEquals( 
    		"(request (query (segment (matching MU ( (meetunion (segment (spanclass meet ( (meetunion (segment (spanclass meet (segment (token [ (term (layer pos) (termOp =) (key (regex \"NN.*\"))) ])))) (segment (token (key (regex \"virtue\")))) 2 2) ))) (segment (token (key (regex \"of\")))) 1 1) )))) ;)",
    		treeString(" MU(meet (meet [pos=\"NN.*\"] \"virtue\" 2 2) \"of\" 1 1);")
    		);
    };

    @Test
    public void testMU5 () {

    	 	assertEquals( 
    		"(request (query (segment (matching MU ( (meetunion (segment (spanclass meet ( (meetunion (segment (spanclass meet (segment (token (key (regex \"one\")))))) (segment (token (key (regex \"hand\")))) 1 1) ))) ( (meetunion (segment (spanclass meet (segment (token (key (regex \"other\")))))) (segment (token (key (regex \"hand\")))) 1 1) ) (span (skey s))) )))) ;)",
    		treeString(" MU(meet (meet \"one\" \"hand\" 1 1) (meet \"other\" \"hand\" 1 1) s);")
    		);
    };
   @Test
    public void testFocus1 () {

	 	assertEquals( 
		"(request (query (segment (matching focus ( (sequence (segment (spanclass { (segment (token (key (regex 'in')))) })) (segment (token (key (regex 'due'))))) )))))",
		treeString("focus({'in'} 'due')")
		);
};
/*@Test
// focus should not be implemented in CQP
public void focuscontains()
{
			assertEquals("", 
			treeString("focus(contains(<base/s=s>, {'de'} 'piel'))")
			);
}*/

    @Test
    public void testMUinS () {

    	 	assertEquals( 
    		"(request (query (segment (matching MU ( (meetunion (segment (spanclass meet (segment (token (key (regex \"tea\")) (flag %c))))) (segment (token (key (regex \"cakes\")) (flag %c))) (span (skey s))) )))) ;)",
    		treeString(" MU(meet \"tea\"%c \"cakes\"%c s);")
    		);
    };

 /* @Test

    // pt ca MU are ca argument sequence, pot sa pun oricate regex! vreau asta?
    public void testMUsequence () {

    	 	assertEquals( 
    		"(request (query (segment (matching MU ( (meet meet (sequence (segment (token (key (regex \"der\")))) (segment (emptyTokenSequence (emptyToken [ ]))) (segment (token (key (regex \"Mann\")))))) )))) ;)",
    		treeString(" MU(meet \"der\" [] \"Mann\");")
    		);
    }; */


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
