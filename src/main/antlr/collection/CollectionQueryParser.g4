parser grammar CollectionQueryParser;

@header {package de.ids_mannheim.korap.query.parse.collection;}

options
{
language=Java;
tokenVocab=CollectionQueryLexer;
}

/*
 * PARSER SECTION
 */

regex
: REGEX
;

date
: DATE
;

dateOp
: SINCE
| UNTIL
| IN
| ON
;

vcOp
: REF
;

operator
:	(NEG? EQ) | LT | GT | LEQ | GEQ | TILDE | NEGTILDE;

expr
: constraint
| dateConstraint
| vcConstraint
| token
;

vcConstraint
: vcOp vcName 
;

dateConstraint
: field dateOp date
//| date dateOp field dateOp date
;

constraint
: field operator value flag?
;

vcName
: WORD
| WORD SLASH WORD
;

token
: LB (term|termGroup) RB
;

term       
: NEG* (foundry SLASH)? layer termOp key (COLON value)? flag? 
;

termOp
: (NEG? EQ? EQ | NEG? TILDE? TILDE)
;

termGroup
: (term | LRB termGroup RRB) booleanOp (term | LRB termGroup RRB | termGroup)
;

key
: WORD
| regex
;

foundry
: WORD
;

layer
: WORD
;

booleanOp
: AND 
| OR 
;

flag
: FLAG_xi 
| FLAG_ix
;
	
field
: WORD
;
	
value
: WORD
| DIGIT+
| DATE
| multiword
| regex
;

multiword
//: '"' ~'"'* '"'
: QUOTE (~QUOTE  | ESC_QUOTE)* QUOTE
;

relation
:	(expr|exprGroup) booleanOp (expr|exprGroup|relation)
; 

exprGroup
:	LRB (expr | exprGroup | relation) RRB
;

start
: expr EOF
| exprGroup EOF 
| relation EOF
;
