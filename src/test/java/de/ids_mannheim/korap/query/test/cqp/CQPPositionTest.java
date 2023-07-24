package de.ids_mannheim.korap.query.test.cqp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import de.ids_mannheim.korap.query.test.BaseQueryTest;

public class CQPPositionTest extends BaseQueryTest{

    private JsonNode result;

    public CQPPositionTest () {
        super("CQP");
    }
    
    @Test
    public void testSequenceStartsWithStartSentence () throws JsonProcessingException {
       
         // throws error because of the FailedPredicateException; 
        query =" <s> <np> \"copil\" </np> ;"; 
        result = runQuery(query);
        assertEquals("unmatched span tags!", result.at("/errors/0/1").asText());
        assertEquals(302, result.at("/errors/1/0").asInt());

        query = "<s> []+ (\"der\")* []+ </z>";
        result = runQuery(query);
        assertEquals("unmatched span tags!", result.at("/errors/0/1").asText());

        query =" <base/s=s> \"copil\" ;"; //KoralFrame.STARTS_WITH
        result = runQuery(query);
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:position", result.at("/query/operation").asText());
        assertEquals("frames:startsWith", result.at("/query/frames/0").asText());
        assertEquals("koral:span", result.at("/query/operands/0/@type").asText());
        assertEquals("s", result.at("/query/operands/0/wrap/key").asText());
        assertEquals("base", result.at("/query/operands/0/wrap/foundry").asText());
        assertEquals("s", result.at("/query/operands/0/wrap/layer").asText());
        assertEquals("koral:token", result.at("/query/operands/1/@type").asText());
        assertEquals("copil", result.at("/query/operands/1/wrap/key").asText());

        // it is interpreted as a sentence starting/ended with a np, followed/preceded by "copil"; 
        // np is a segment in the sequence, not a position operator, like s
        query =" <s> <np> \"copil\" ;"; //KoralFrame.STARTS_WITH
        result = runQuery(query);
        assertEquals("koral:group", result.at("/query/@type").asText());
        query ="\"copil\" </np> </s>;"; //KoralFrame.ENDS_WITH
        result = runQuery(query);
        assertEquals("koral:group", result.at("/query/@type").asText());
        
       
    //    assertEquals("koral:group", result.at("/query/@type").asText());
       // matches np imbricated in sentence; imbrication works for correct qstructures
        query =" <s> <np> \"copil\" </np> \"mic\"</s>;"; 
        result = runQuery(query);
        assertEquals("koral:group", result.at("/query/@type").asText());

       
    }
    
  
    @Test
    public void testSequenceStartsWithEndSentence () throws JsonProcessingException {
        
        // parsed as a sequence of span s and token copil; focus on the token;
        query =" </base/s=s> \"copil\" ;"; 
        result = runQuery(query);
        assertEquals("koral:reference", result.at("/query/@type").asText());
        assertEquals("operation:focus", result.at("/query/operation").asText());
        assertEquals("koral:group", result.at("/query/operands/0/@type").asText());
        assertEquals("operation:sequence", result.at("/query/operands/0/operation").asText());
        assertEquals("koral:span", result.at("/query/operands/0/operands/0/@type").asText());
        assertEquals("koral:group", result.at("/query/operands/0/operands/1/@type").asText());
        assertEquals("operation:class", result.at("/query/operands/0/operands/1/operation").asText());
        assertEquals("koral:token", result.at("/query/operands/0/operands/1/operands/0/@type").asText());
        assertEquals("type:regex", result.at("/query/operands/0/operands/1/operands/0/wrap/type").asText());
        assertEquals("copil", result.at("/query/operands/0/operands/1/operands/0/wrap/key").asText());
        
        
        // (span token token) sequence; focus on both tokens
        query =" </base/s=s> \"copil\" \"cuminte\";";
        result = runQuery(query);
        assertEquals("koral:reference", result.at("/query/@type").asText());
        assertEquals("operation:focus", result.at("/query/operation").asText());
        assertEquals("koral:group", result.at("/query/operands/0/@type").asText());
        assertEquals("operation:sequence", result.at("/query/operands/0/operation").asText());
        assertEquals("koral:span", result.at("/query/operands/0/operands/0/@type").asText());
        assertEquals("koral:group", result.at("/query/operands/0/operands/1/@type").asText());
        assertEquals("operation:class", result.at("/query/operands/0/operands/1/operation").asText());
        assertEquals("koral:token", result.at("/query/operands/0/operands/1/operands/0/@type").asText());
        assertEquals("type:regex", result.at("/query/operands/0/operands/1/operands/0/wrap/type").asText());
        assertEquals("copil", result.at("/query/operands/0/operands/1/operands/0/wrap/key").asText());
        assertEquals("koral:group", result.at("/query/operands/0/operands/2/@type").asText());
        assertEquals("operation:class", result.at("/query/operands/0/operands/2/operation").asText());
        assertEquals("koral:token", result.at("/query/operands/0/operands/2/operands/0/@type").asText());
        assertEquals("type:regex", result.at("/query/operands/0/operands/2/operands/0/wrap/type").asText());
        assertEquals("cuminte", result.at("/query/operands/0/operands/2/operands/0/wrap/key").asText());
    }


    @Test
    public void testSequenceEndsWithEndSentence () throws JsonProcessingException{
        query ="\"copil\" </base/s=s>;"; //KoralFrame.ENDS_WITH
        result = runQuery(query);
        
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:position", result.at("/query/operation").asText());
        assertEquals("frames:endsWith", result.at("/query/frames/0").asText());
        assertEquals("frames:matches", result.at("/query/frames/1").asText());
        assertEquals(2, result.at("/query/frames").size());
        assertEquals(2, result.at("/query/operands").size());
        
        assertEquals("koral:span", result.at("/query/operands/0/@type").asText());
        assertEquals("s", result.at("/query/operands/0/wrap/key").asText());
        assertEquals("base", result.at("/query/operands/0/wrap/foundry").asText());
        assertEquals("s", result.at("/query/operands/0/wrap/layer").asText());
        
        assertEquals("koral:token", result.at("/query/operands/1/@type").asText());
        assertEquals("type:regex", result.at("/query/operands/1/wrap/type").asText());
        assertEquals("copil", result.at("/query/operands/1/wrap/key").asText());

    }
    
    @Test
    public void testLongerSequenceEndsWithEndSentence () throws JsonProcessingException{
        query ="\"copil\" \"cuminte\" </base/s=s>;"; //KoralFrame.ENDS_WITH
        result = runQuery(query);
        
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:position", result.at("/query/operation").asText());
        assertEquals(2, result.at("/query/operands").size());
        assertEquals(2, result.at("/query/frames").size());
        assertEquals("frames:endsWith", result.at("/query/frames/0").asText());
        assertEquals("frames:matches", result.at("/query/frames/1").asText());
 
        
        assertEquals("koral:span", result.at("/query/operands/0/@type").asText());
        assertEquals("s", result.at("/query/operands/0/wrap/key").asText());
        assertEquals("base", result.at("/query/operands/0/wrap/foundry").asText());
        assertEquals("s", result.at("/query/operands/0/wrap/layer").asText());
        
        assertEquals("koral:reference", result.at("/query/operands/1/@type").asText());
        assertEquals("operation:focus", result.at("/query/operands/1/operation").asText());
  
        assertEquals("koral:group", result.at("/query/operands/1/operands/0/@type").asText());
        assertEquals("operation:sequence", result.at("/query/operands/1/operands/0/operation").asText());
  
        
        assertEquals("koral:group", result.at("/query/operands/1/operands/0/operands/0/@type").asText());
        assertEquals("operation:class", result.at("/query/operands/1/operands/0/operands/0/operation").asText());
        assertEquals("koral:token", result.at("/query/operands/1/operands/0/operands/0/operands/0/@type").asText());
        assertEquals("copil", result.at("/query/operands/1/operands/0/operands/0/operands/0/wrap/key").asText());
   
        
        assertEquals("koral:group", result.at("/query/operands/1/operands/0/operands/1/@type").asText());
        assertEquals("operation:class", result.at("/query/operands/1/operands/0/operands/1/operation").asText());
        assertEquals("koral:token", result.at("/query/operands/1/operands/0/operands/1/operands/0/@type").asText());
        assertEquals("cuminte", result.at("/query/operands/1/operands/0/operands/1/operands/0/wrap/key").asText());
    }
    
 
    @Test
    public void testSequenceEndsWithStartSentence () throws JsonProcessingException{
        query ="\"copil\" <base/s=s>;"; 
        result = runQuery(query);     
        assertEquals("koral:reference", result.at("/query/@type").asText());
        assertEquals("operation:focus", result.at("/query/operation").asText());
        assertEquals("koral:group", result.at("/query/operands/0/@type").asText());
        assertEquals("operation:sequence", result.at("/query/operands/0/operation").asText());
        assertEquals("koral:span", result.at("/query/operands/0/operands/1/@type").asText());
        assertEquals("koral:group", result.at("/query/operands/0/operands/0/@type").asText());
        assertEquals("operation:class", result.at("/query/operands/0/operands/0/operation").asText());
        assertEquals("koral:token", result.at("/query/operands/0/operands/0/operands/0/@type").asText());
        assertEquals("type:regex", result.at("/query/operands/0/operands/0/operands/0/wrap/type").asText());
        assertEquals("copil", result.at("/query/operands/0/operands/0/operands/0/wrap/key").asText());
    }
    
   
    @Test
    public void testLongerSequenceEndsWithStartSentence () throws JsonProcessingException{
        query ="\"copil\" \"cuminte\" <base/s=s>;"; 
        result = runQuery(query);
        assertEquals("koral:reference", result.at("/query/@type").asText());
        assertEquals("operation:focus", result.at("/query/operation").asText());
        assertEquals("koral:group", result.at("/query/operands/0/@type").asText());
        assertEquals("operation:sequence", result.at("/query/operands/0/operation").asText());  
        assertEquals("koral:group", result.at("/query/operands/0/operands/0/@type").asText());
        assertEquals("operation:class", result.at("/query/operands/0/operands/0/operation").asText());
        assertEquals("koral:token", result.at("/query/operands/0/operands/0/operands/0/@type").asText());
        assertEquals("type:regex", result.at("/query/operands/0/operands/0/operands/0/wrap/type").asText());
        assertEquals("copil", result.at("/query/operands/0/operands/0/operands/0/wrap/key").asText());
        assertEquals("koral:group", result.at("/query/operands/0/operands/1/@type").asText());
        assertEquals("operation:class", result.at("/query/operands/0/operands/1/operation").asText());
        assertEquals("koral:token", result.at("/query/operands/0/operands/1/operands/0/@type").asText());
        assertEquals("type:regex", result.at("/query/operands/0/operands/1/operands/0/wrap/type").asText());
        assertEquals("cuminte", result.at("/query/operands/0/operands/1/operands/0/wrap/key").asText());
        
        assertEquals("koral:span", result.at("/query/operands/0/operands/2/@type").asText());
   }

    @Test
    public void testSingleTokenInSentence () throws JsonProcessingException {
        query =" <base/s=s> \"copil\" </base/s=s>;";  // KoralFrame.MATCHES
        result = runQuery(query);
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:position", result.at("/query/operation").asText());
        assertEquals("frames:matches", result.at("/query/frames/0").asText());
        assertTrue(result.at("/query/frames/1").isMissingNode());
        assertEquals("koral:span", result.at("/query/operands/0/@type").asText());
        assertEquals("s", result.at("/query/operands/0/wrap/key").asText());
        assertEquals("base", result.at("/query/operands/0/wrap/foundry").asText());
        assertEquals("s", result.at("/query/operands/0/wrap/layer").asText());
        assertEquals("koral:token", result.at("/query/operands/1/@type").asText());
        assertEquals("copil", result.at("/query/operands/1/wrap/key").asText());
    }
    
    
    @Test
    public void testSequenceInSentence () throws JsonProcessingException {
        query = "<base/s=s> []* \"copil\" []* </base/s=s>;";
        result = runQuery(query);
        
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:position", result.at("/query/operation").asText());
        assertEquals("frames:matches", result.at("/query/frames/0").asText());
        assertEquals(1, result.at("/query/frames").size());
        assertEquals(2, result.at("/query/operands").size());
        
        assertEquals("koral:span", result.at("/query/operands/0/@type").asText());
        assertEquals("s", result.at("/query/operands/0/wrap/key").asText());
        
        assertEquals("koral:group", result.at("/query/operands/1/@type").asText());
        assertEquals("operation:sequence", result.at("/query/operands/1/operation").asText());
        assertEquals(3, result.at("/query/operands/1/operands").size());
        
        assertEquals("koral:group", result.at("/query/operands/1/operands/0/@type").asText());
        assertEquals("operation:repetition", result.at("/query/operands/1/operands/0/operation").asText());
        assertEquals("koral:token", result.at("/query/operands/1/operands/0/operands/0/@type").asText());
        
        assertEquals("copil", result.at("/query/operands/1/operands/1/wrap/key").asText());
      
        assertEquals("koral:group", result.at("/query/operands/1/operands/2/@type").asText());
        assertEquals("operation:repetition", result.at("/query/operands/1/operands/2/operation").asText());
        assertEquals("koral:token", result.at("/query/operands/1/operands/2/operands/0/@type").asText());
        
    }
    
    @Test
    public void testSequenceInSentence2 () throws JsonProcessingException{
        query = "<base/s=s> []* \"copil\" [] \"cuminte\" []* </base/s=s>";
        result = runQuery(query);
        
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:position", result.at("/query/operation").asText());
      //  assertEquals("frames:isAround", res.at("/query/frames/0").asText());
      //  assertEquals("frames:startsWith", res.at("/query/frames/1").asText());
     //   assertEquals("frames:endsWith", res.at("/query/frames/2").asText());
        assertEquals(1, result.at("/query/frames").size());
        assertEquals("frames:matches", result.at("/query/frames/0").asText());
        assertEquals(2, result.at("/query/operands").size());
        
        assertEquals("koral:span", result.at("/query/operands/0/@type").asText());
        assertEquals("s", result.at("/query/operands/0/wrap/key").asText());
        
        assertEquals("koral:group", result.at("/query/operands/1/@type").asText());
        assertEquals("operation:sequence", result.at("/query/operands/1/operation").asText());
        
        result = result.at("/query/operands/1");
        assertEquals("koral:group", result.at("/@type").asText());
        assertEquals("operation:sequence", result.at("/operation").asText());
        assertEquals(5, result.at("/operands").size());

        // []*
        assertEquals("koral:group", result.at("/operands/0/@type").asText());
        assertEquals("operation:repetition", result.at("/operands/0/operation").asText());
        assertEquals("koral:token", result.at("/operands/0/operands/0/@type").asText());
        
        assertEquals(0, result.at("/operands/0/boundary/min").asInt());
        assertTrue(result.at("/operands/0/boundary/max").isMissingNode());
        
        // copil
        assertEquals("koral:token", result.at("/operands/1/@type").asText());
        assertEquals("koral:term", result.at("/operands/1/wrap/@type").asText());
        assertEquals("match:eq", result.at("/operands/1/wrap/match").asText());
        assertEquals("type:regex", result.at("/operands/1/wrap/type").asText());
        assertEquals("orth", result.at("/operands/1/wrap/layer").asText());
        assertEquals("copil", result.at("/operands/1/wrap/key").asText());
        
        // []
        assertEquals("koral:token", result.at("/operands/2/@type").asText());
        assertTrue(result.at("/operands/2/wrap/key").isMissingNode());
        
        // cuminte
        assertEquals("koral:token", result.at("/operands/3/@type").asText());
        assertEquals("koral:term", result.at("/operands/3/wrap/@type").asText());
        assertEquals("match:eq", result.at("/operands/3/wrap/match").asText());
        assertEquals("type:regex", result.at("/operands/3/wrap/type").asText());
        assertEquals("orth", result.at("/operands/3/wrap/layer").asText());
        assertEquals("cuminte", result.at("/operands/3/wrap/key").asText());
        
        //[]*
        assertEquals("koral:group", result.at("/operands/4/@type").asText());
        assertEquals("operation:repetition", result.at("/operands/4/operation").asText());
        assertEquals("koral:token", result.at("/operands/4/operands/0/@type").asText());
        assertEquals(0, result.at("/operands/4/boundary/min").asInt());
        assertTrue(result.at("/operands/4/boundary/max").isMissingNode());
    }
    
    @Test
    public void testSequenceWithinSentence () throws JsonProcessingException {
        // same as  "contains(<base/s=s>, \"copil\" \"cuminte\")"; in PQ+, checked!
        // only if []+ is paired and emmbeding the searched string/sequence: []+ ... []+
        
        query = "<base/s=s> []+ \"copil\" \"cuminte\" []+ </base/s=s>;";
        result = runQuery(query);
        
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:position", result.at("/query/operation").asText());
        assertEquals("frames:isAround", result.at("/query/frames/0").asText());
        assertEquals(1, result.at("/query/frames").size());
        
        assertEquals("koral:span", result.at("/query/operands/0/@type").asText());
        assertEquals("s", result.at("/query/operands/0/wrap/key").asText());
        
        assertEquals("koral:group", result.at("/query/operands/1/@type").asText());
        assertEquals("operation:sequence", result.at("/query/operands/1/operation").asText());
    }
    
    /**
     * @throws JsonProcessingException
     */
    @Test
    public void testSequenceAtConstituentEnd () throws JsonProcessingException {
        //if []+ is not embedding the rest of the expression, it is treated as token repetition;
        query = "<np> []+ ([pos=\"JJ.*\"] []+){3,} </np>;"; //KoralFrame.MATCHES
        result = runQuery(query);
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:position", result.at("/query/operation").asText());
        assertEquals("frames:matches", result.at("/query/frames/0").asText());
        assertTrue(result.at("/query/frames/1").isMissingNode());
        assertEquals(2, result.at("/query/operands").size());
        
        assertEquals("koral:span", result.at("/query/operands/0/@type").asText());
        assertEquals("np", result.at("/query/operands/0/wrap/key").asText());
        
        assertEquals("koral:group", result.at("/query/operands/1/@type").asText());
        assertEquals("operation:sequence", result.at("/query/operands/1/operation").asText());
        assertEquals(2, result.at("/query/operands/1/operands").size());
        
        JsonNode node = result.at("/query/operands/1");
        // []+
        assertEquals("koral:group", node.at("/operands/1/@type").asText());
        assertEquals("operation:repetition", node.at("/operands/0/operation").asText());
        assertEquals("koral:token", node.at("/operands/0/operands/0/@type").asText());
        assertEquals(1, node.at("/operands/0/boundary/min").asInt());
        
        // ([pos=\"JJ.*\"] []+){3,}
        assertEquals("operation:repetition", node.at("/operands/1/operation").asText());
        assertEquals(3, node.at("/operands/1/boundary/min").asInt());
        assertEquals(1, node.at("/operands/1/operands").size());        
        
        // ([pos=\"JJ.*\"] []+)
        node = node.at("/operands/1/operands/0");
        assertEquals("operation:sequence", node.at("/operation").asText());
        assertEquals(2, node.at("/operands").size());     
        
        // [pos=\"JJ.*\"]
        assertEquals("koral:token", node.at("/operands/0/@type").asText());
        assertEquals("pos", node.at("/operands/0/wrap/layer").asText());
        assertEquals("JJ.*", node.at("/operands/0/wrap/key").asText());
        
        // []+ 
        assertEquals("operation:repetition", node.at("/operands/1/operation").asText());
        assertEquals("1", node.at("/operands/1/boundary/min").asText());
        assertEquals("koral:token", node.at("/operands/1/operands/0/@type").asText());

    }
    @Test
    public void testEmbeddedStruct () throws JsonProcessingException {
        query = "<s><np>[]*</np> []* <np1>[]*</np1></s>"; 
        result = runQuery(query);
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:position", result.at("/query/operation").asText());
        assertEquals("frames:matches", result.at("/query/frames/0").asText());
        assertEquals("koral:span", result.at("/query/operands/0/@type").asText());
        assertEquals("s", result.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:group", result.at("/query/operands/1/@type").asText());
        assertEquals("operation:sequence", result.at("/query/operands/1/operation").asText());
        
        assertEquals("koral:group", result.at("/query/operands/1/operands/0/@type").asText());
        assertEquals("operation:position", result.at("/query/operands/1/operands/0/operation").asText());
        assertEquals("frames:matches", result.at("/query/operands/1/operands/0/frames/0").asText());
        assertEquals("koral:span", result.at("/query/operands/1/operands/0/operands/0/@type").asText());
        assertEquals("np", result.at("/query/operands/1/operands/0/operands/0/wrap/key").asText());
        assertEquals("koral:group", result.at("/query/operands/1/operands/0/operands/1/@type").asText());
        assertEquals("operation:repetition", result.at("/query/operands/1/operands/0/operands/1/operation").asText());
        assertEquals("koral:token", result.at("/query/operands/1/operands/0/operands/1/operands/0/@type").asText());
        assertEquals("0", result.at("/query/operands/1/operands/0/operands/1/boundary/min").asText());

        assertEquals("koral:group", result.at("/query/operands/1/operands/2/@type").asText());
        assertEquals("operation:position", result.at("/query/operands/1/operands/2/operation").asText());
        assertEquals("frames:matches", result.at("/query/operands/1/operands/2/frames/0").asText());
        assertEquals("np1", result.at("/query/operands/1/operands/2/operands/0/wrap/key").asText());
        assertEquals("koral:group", result.at("/query/operands/1/operands/2/operands/1/@type").asText());
        assertEquals("operation:repetition", result.at("/query/operands/1/operands/2/operands/1/operation").asText());
        assertEquals("koral:token", result.at("/query/operands/1/operands/2/operands/1/operands/0/@type").asText());
        assertEquals("0", result.at("/query/operands/1/operands/2/operands/1/boundary/min").asText());
        
  
        
    }  

    @Test
    public void testSequenceWithinConstituent () throws JsonProcessingException {
        // EM: comparable to PQ+ query: contains (NP, sequence)
        query =" <np> []+ ([pos=\"JJ.*\"]){3,} []+ </np>;"; //KoralFrame.IS_AROUND
        result = runQuery(query);
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:position", result.at("/query/operation").asText());
        assertEquals("frames:isAround", result.at("/query/frames/0").asText());
        assertEquals("koral:span", result.at("/query/operands/0/@type").asText());
        assertEquals("np", result.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:group", result.at("/query/operands/1/@type").asText());
        assertEquals("operation:repetition", result.at("/query/operands/1/operation").asText());
        assertEquals("JJ.*", result.at("/query/operands/1/operands/0/wrap/key").asText());
        assertEquals(3, result.at("/query/operands/1/boundary/min").asInt());
        // the []+ segments are not serialesd;
        assertTrue(result.at("/query/operands/2").isMissingNode());
        
    }
    
    // EM: rbound: last token in the region
    // match token at the end of a sentence
    @Test
    public void testRbound ()  throws JsonProcessingException {
        query ="[\"copil\"  & rbound(<s>)];"; 
        result = runQuery(query);
        assertEquals("koral:group", result.at("/query/@type").asText());
        
        query ="[\"copil\"  & rbound(s)];"; 
        result = runQuery(query);
        assertEquals("koral:group", result.at("/query/@type").asText());

        query ="[\"copil\"  & rbound(<base/s=s>)];"; 
        result = runQuery(query);
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:position", result.at("/query/operation").asText());
        assertEquals("frames:endsWith", result.at("/query/frames/0").asText());
        assertEquals("frames:matches", result.at("/query/frames/1").asText());
        //assertTrue(res.at("/query/frames/1").isMissingNode());
        assertEquals("koral:span", result.at("/query/operands/0/@type").asText());
        assertEquals("base", result.at("/query/operands/0/wrap/foundry").asText());
        assertEquals("s", result.at("/query/operands/0/wrap/key").asText());
        assertEquals("s", result.at("/query/operands/0/wrap/layer").asText());
        assertEquals("koral:token", result.at("/query/operands/1/@type").asText());
        assertEquals("copil", result.at("/query/operands/1/wrap/key").asText());
        
    }
    
    // EM: lbound: first token in the region
    // match token at the start of a sentence
    @Test
    public void testLBound () throws JsonProcessingException {
        query ="[(base=\"copil\") & lbound(<base/s=s>)];"; 
        result = runQuery(query);
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:position", result.at("/query/operation").asText());
        assertEquals("frames:startsWith", result.at("/query/frames/0").asText());
        assertEquals("frames:matches", result.at("/query/frames/1").asText());
        //assertTrue(res.at("/query/frames/1").isMissingNode());
        assertEquals("koral:span", result.at("/query/operands/0/@type").asText());
        assertEquals("base", result.at("/query/operands/0/wrap/foundry").asText());
        assertEquals("s", result.at("/query/operands/0/wrap/layer").asText());
        assertEquals("s", result.at("/query/operands/0/wrap/key").asText());
        assertEquals("koral:token", result.at("/query/operands/1/@type").asText());
        assertEquals("copil", result.at("/query/operands/1/wrap/key").asText());
        assertEquals("lemma", result.at("/query/operands/1/wrap/layer").asText());
    }
    
    @Test
    public void testRBoundSequence () throws JsonProcessingException {
        
        query ="[word = \"acest\"][\"copil\" & rbound(<base/s=s>)];"; 
        result = runQuery(query);
        assertEquals("koral:group", result.at("/query/@type").asText());
        assertEquals("operation:sequence", result.at("/query/operation").asText());
        assertEquals("koral:token", result.at("/query/operands/0/@type").asText());
        assertEquals("acest", result.at("/query/operands/0/wrap/key").asText());    
        assertEquals("koral:group", result.at("/query/operands/1/@type").asText());
        assertEquals("operation:position", result.at("/query/operands/1/operation").asText());
        assertEquals("frames:endsWith", result.at("/query/operands/1/frames/0").asText());
        assertEquals("frames:matches", result.at("/query/operands/1/frames/1").asText());
        //assertTrue(res.at("/query/frames/1").isMissingNode());
        assertEquals("koral:span", result.at("/query/operands/1/operands/0/@type").asText());
        assertEquals("base", result.at("/query/operands/1/operands/0/wrap/foundry").asText());
        assertEquals("s", result.at("/query/operands/1/operands/0/wrap/layer").asText());
        assertEquals("s", result.at("/query/operands/1/operands/0/wrap/key").asText());
        assertEquals("koral:token", result.at("/query/operands/1/operands/1/@type").asText());
        assertEquals("copil", result.at("/query/operands/1/operands/1/wrap/key").asText()); 
    }

}
