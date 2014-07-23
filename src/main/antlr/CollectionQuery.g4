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
DASH				: '-';
WS 					: ( ' ' | '\t' | '\r' | '\n' )+ -> skip ;
fragment NO_RE      : ~[ \t\/];
fragment ALPHABET   : ~('\t' | ' ' | '/' | '*' | '?' | '+' | '{' | '}' | '[' | ']'
                    | '(' | ')' | '|' | '"' | ',' | ':' | '\'' | '\\' | '!' | '=' | '~' | '&' | '^' | '<' | '>' );
DIGIT				: [0-9];
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
: REGEX
;

date
: DIGIT DIGIT DIGIT DIGIT  (DASH DIGIT DIGIT (DASH DIGIT DIGIT)?)?
;

conj
:	AND | OR;

operator
:	EQ | NE | LT | GT | LEQ | GEQ;

expr
:	(value operator)? field operator value
//|	LRB expr RRB
;
	
field
: WORD
;
	
value
: WORD 
| NUMBER 
| date
| '"' (WORD ws*)+'"'
| regex
;



relation
:	(expr|exprGroup) conj (expr|exprGroup|relation)
//|	LRB relation RRB
; 

exprGroup
:	LRB (expr | exprGroup | relation) RRB
;

start
:	( expr 
	| exprGroup 
	| relation  ) 
;