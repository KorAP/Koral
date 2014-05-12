grammar CollectionQuery;

@header {package de.ids_mannheim.korap.query.serialize.util;}

/*
 -- author: jbingel
 -- date: 14-05-11
*/

/*
 * LEXER SECTION
 */

WS 					: ( ' ' | '\t' | '\r' | '\n' )+ -> skip ;
fragment FOCC       : '{' WS* ( [0-9]* WS* ',' WS* [0-9]+ | [0-9]+ WS* ','? ) WS* '}';
fragment NO_RE      : ~[ \t\/];
fragment ALPHABET   : ~('\t' | ' ' | '/' | '*' | '?' | '+' | '{' | '}' | '[' | ']'
                    | '(' | ')' | '|' | '"' | ',' | ':' | '\'' | '\\' | '!' | '=' | '~' | '&' | '^' | '<' | '>' );
NUMBER              : [0-9]+;

NL                  : [\r\n] -> skip;
ws                  : WS+;

WORD                : ALPHABET+;
LRB					: '(';
RRB					: ')';
LT					: '<';
GT					: '>';
LEQ					: '<=';
GEQ					: '>=';
EQ					: '=';
NE					: '!=';
AND					: '&';
OR					: '|';

/*
 * PARSER SECTION
 */

conj
:	AND | OR;

operator
:	EQ | NE | LT | GT | LEQ | GEQ;

expr
:	field operator value
|	value operator field operator value
;
	
field
:	WORD;
	
value
:	WORD | NUMBER;
	
andGroup
: 	(expr AND)* (LRB orGroup RRB)? (AND expr)*
|	(expr AND)+ expr
;

orGroup
: 	(expr OR)* (LRB andGroup RRB)? (OR expr)* 
|	(expr OR)+ expr
;

exprGroup
:	andGroup
|	orGroup
;

start
:	expr
|	exprGroup
;