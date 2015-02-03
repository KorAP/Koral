parser grammar PoliqarpPlusParser;

@header {package de.ids_mannheim.korap.query.parse.poliqarpplus;}

options
{
language=Java;
tokenVocab=PoliqarpPlusLexer;
}
/*
 -- author: Joachim Bingel
 -- date: 14-06-27

 Poliqarp Query Language parser

 Language documentations:
 - Adam Przepiórkowski (2004):
   "The IPI PAN Corpus -- preliminary version", pp. 44

 Further information:
 - http://korpus.pl/index.php?page=poliqarp
 Statistical extension:
 - http://nlp.ipipan.waw.pl/Poliqarp/
 Based on CQP
 - http://cwb.sourceforge.net/files/CQP_Tutorial/

Todo: Some special characters aren't supported in REGEX and strings.
Todo: tags can be splittet at ':' in case the fieldname is 'tag'

*/


flag: 
FLAG_xi | FLAG_ix
;

boolOp
: CONJ | DISJ
;

regex
: REGEX
;

key
: WORD
| regex
| NUMBER
;

foundry
: WORD
;

layer
: WORD
;

value
: WORD 
| NUMBER
| regex
;
 
/* Fields */
term       
: NEG* (foundry SLASH)? layer termOp key (COLON value)? flag? 
;

termOp
: (NEG? EQ? EQ | NEG? TILDE? TILDE)
;

min
: NUMBER
;

max
: NUMBER
;

startpos
: NUMBER
;

length
: NUMBER
;

range
: LBRACE 
  ( min COMMA max
  | max 
  | COMMA max
  | min COMMA  
  )
  RBRACE
; 

emptyToken
: LPAREN RPAREN
;

termGroup
: (term | LRPAREN termGroup RRPAREN) boolOp (term | LRPAREN termGroup RRPAREN | termGroup)
;

repetition
: kleene
| range
;

kleene
: QMARK
| STAR
| PLUS
;

token
: NEG* 
    ( LPAREN term RPAREN
    | LPAREN termGroup RPAREN
    | key flag?
    )
;

span
: LT ((foundry SLASH)? layer termOp)? key NEG* (LRPAREN term RRPAREN|LRPAREN termGroup RRPAREN)? GT
| LT ((foundry SLASH)? layer termOp)? key NEG* (term|termGroup)? GT
;

position
: POSITION_OP LRPAREN (segment|sequence) COMMA (segment|sequence) RRPAREN
;

relation
: RELATION_OP LRPAREN ((EMPTYREL|relSpec)? repetition? COLON)? (segment|sequence) COMMA (segment|sequence) RRPAREN
;

relSpec
: (foundry SLASH)? layer (termOp key)?
;

submatch
: SUBMATCH_OP LRPAREN startpos (COMMA length)? COLON (segment|sequence) RRPAREN
;

matching
: MATCH_OP LRPAREN spanclass_id? (segment|sequence)? RRPAREN
;

alignment
: CARET (segment|sequence)
;

disjunction
: (segment|sequence|group) (DISJ (segment|sequence|group))+
;

group
: LRPAREN ( disjunction | sequence ) RRPAREN 
; 

spanclass_id
: NUMBER (boolOp NUMBER)* COLON
;

emptyTokenSequence
: (emptyToken repetition?)+
;

emptyTokenSequenceClass
: LBRACE spanclass_id? emptyTokenSequence RBRACE     // class defined around empty tokens 
;


distance
: emptyTokenSequence
;

spanclass
: LBRACE spanclass_id? (segment|sequence) RBRACE
;

segment
: ( position 
  | token 
  | span 
  | group
  | spanclass 
  | matching
  | submatch
  | relation
  | LRPAREN segment RRPAREN
  ) 
  repetition?
; 

sequence
: segment* (emptyTokenSequence|emptyTokenSequenceClass)		// ordering important! this subrule must precede any 'distance'-subrules to give precedence to repetition-interpretation of numbers in braces (could be mistaken for number tokens in spanclass), e.g. {2}.
| (emptyTokenSequence|emptyTokenSequenceClass) (segment+ | sequence) (emptyTokenSequence|emptyTokenSequenceClass)?
| segment segment+ 
| segment (distance|emptyTokenSequenceClass) segment 
| segment (distance|emptyTokenSequenceClass)? sequence
| segment+ alignment
;


/** Entry point for linguistic queries */

query
: segment | sequence | disjunction
;

within
: WITHIN WORD
;

/**
 === META section ===
 defines metadata filters on request
*/

meta               : META metaTermGroup;
metaTermGroup	   : ( term | termGroup )+;

/**
    Entry point for all requests. Linguistic query is obligatory, metadata filtering
    is optional.
*/
request            : query within? meta? EOF;