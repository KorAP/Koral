parser grammar PoliqarpPlusParser;

@header {package de.ids_mannheim.korap.query.parse.poliqarpplus;}

options
{
language=Java;
tokenVocab=PoliqarpPlusLexer;
}
/*
 -- author: Joachim Bingel
 -- date: 06-27-06-2014

 -- updated: 26-10-2016 (margaretha) 

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

verbatim
: SQUOTE (~SQUOTE | ESC_SQUOTE)* SQUOTE;


key
: (WORD
| regex
| verbatim
| NUMBER)
;

foundry
: WORD
;

layer
: WORD
;

value
: (WORD | NUMBER) | regex
;

/* Fields */
term       
: NEG* (foundry SLASH)? layer termOp key (COLON value)? flag?
| NEG* foundry flag layer? termOp key (COLON value)? flag?
| LRPAREN term RRPAREN
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
| LRPAREN termGroup RRPAREN
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
: LT ((foundry SLASH)? layer termOp)? key NEG* ((LRPAREN term RRPAREN|LRPAREN termGroup RRPAREN)? | (term|termGroup)?) GT
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
: segment? ( (CARET segment)+ | CARET)
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

user
: WORD
;

ref
: WORD
;

queryref
: LBRACE HASH (user SLASH)? ref RBRACE 
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
  | emptyTokenSequence
  | emptyTokenSequenceClass
  | queryref
  ) 
  repetition?
 ; 

sequence
: alignment segment* 	// give precedence to this subrule over the next to make sure preceding segments come into 'alignment'
| segment+ alignment segment*
| segment segment+

//| alignment (segment|sequence) alignment?
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
