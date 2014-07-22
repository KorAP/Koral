grammar CollectionQuery;

@header {package de.ids_mannheim.korap.query.serialize.util;}

/*
 -- author: jbingel
 -- date: 14-05-11
*/

/*
 * LEXER SECTION
 */
LRB					: '(';
RRB					: ')';
LT					: '<';
GT					: '>';
LEQ					: '<=';
GEQ					: '>=';
EQ					: '=';
NE					: '!=';
AND					: '&' | 'AND' | 'and' | 'UND' | 'und' ;
OR					: '|' | 'OR' | 'or' | 'ODER' | 'oder' ;
QMARK				: '?';
SLASH				: '/';
WS 					: ( ' ' | '\t' | '\r' | '\n' )+ -> skip ;
fragment NO_RE      : ~[ \t\/];
fragment ALPHABET   : ~('\t' | ' ' | '/' | '*' | '?' | '+' | '{' | '}' | '[' | ']'
                    | '(' | ')' | '|' | '"' | ',' | ':' | '\'' | '\\' | '!' | '=' | '~' | '&' | '^' | '<' | '>' );
NUMBER              : [0-9]+;

NL                  : [\r\n] -> skip;
ws                  : WS+;

WORD                : ALPHABET+;

/*
 * Regular expressions
 */
fragment FOCC	     : '{' WS* ( [0-9]* WS* ',' WS* [0-9]+ | [0-9]+ WS* ','? ) WS* '}';
fragment RE_char     : ~('*' | '?' | '+' | '{' | '}' | '[' | ']' | '/'
         	            | '(' | ')' | '|' | '"' | ':' | '\'' | '\\');
fragment RE_alter    : ((RE_char | ('(' REGEX ')') | RE_chgroup) '|' REGEX )+;
fragment RE_chgroup  : '[' RE_char+ ']';
fragment RE_quant	 : (RE_star | RE_plus | RE_occ) QMARK?;
fragment RE_opt      : (RE_char | RE_chgroup | ( '(' REGEX ')')) '?';
fragment RE_star     : (RE_char | RE_chgroup | ( '(' REGEX ')')) '*';
fragment RE_plus     : (RE_char | RE_chgroup | ( '(' REGEX ')')) '+';
fragment RE_occ      : (RE_char | RE_chgroup | ( '(' REGEX ')')) FOCC;
fragment RE_group    : '(' REGEX ')';
REGEX     		     : SLASH ('.' | RE_char | RE_alter | RE_chgroup | RE_opt | RE_quant | RE_group)* SLASH;

/*
 * PARSER SECTION
 */

regex
:	REGEX
;

conj
:	AND | OR;

operator
:	EQ | NE | LT | GT | LEQ | GEQ;

expr
:	(value operator)? field operator value
;
	
field
:	WORD
;
	
value
: WORD 
| NUMBER 
| '"' (WORD ws*)+'"'
| regex
;

/*	
andGroup
: 	(((LRB exprGroup RRB)|expr) AND)+ ((LRB exprGroup RRB)|expr)
;

orGroup
: 	(((LRB exprGroup RRB)|expr) OR)+ ((LRB exprGroup RRB)|expr)
;
*/

relation
:	(expr|exprGroup) conj (expr|exprGroup|relation)
; 

exprGroup
:	LRB (expr | exprGroup | relation) RRB
;

start
:	( expr 
	| exprGroup EOF 
	| relation EOF ) 
;