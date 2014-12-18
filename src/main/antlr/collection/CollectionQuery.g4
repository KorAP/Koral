grammar CollectionQuery;

@header {package de.ids_mannheim.korap.query.parse.collection;}

/*
 -- author: jbingel
 -- date: 2014-05-11
*/

/*
 * LEXER SECTION
 */
/*
 Regular expression
 /x allows submatches like /^.*?RE.*?$/
 /X forces full matches
 /i means case insensitivity
 /I forces case sensitivity
*/
FLAG_xi      : '/' ( ('x'|'X') ('i'|'I')? );
FLAG_ix      : '/' ( ('i'|'I') ('x'|'X')? );
 
 
LRB					: '(';
RRB					: ')';
LB					: '[';
RB					: ']';
LT					: '<';
GT					: '>';
LEQ					: '<=';
GEQ					: '>=';
EQ					: '=';
AND					: '&' | 'AND' | 'and' | 'UND' | 'und' ;
OR					: '|' | 'OR' | 'or' | 'ODER' | 'oder' ;
NEG					: '!';
QMARK				: '?';
SLASH				: '/';
COLON				: ':';
DASH				: '-';
TILDE				: '~';
NEGTILDE			: '!~';
SINCE				: 'since';
UNTIL				: 'until';
IN					: 'in';
ON					: 'on';
WS 					: ( ' ' | '\t' | '\r' | '\n' )+ -> skip ;
fragment NO_RE      : ~[ \t\/];
fragment ALPHABET   : ~('\t' | ' ' | '/' | '*' | '?' | '+' | '{' | '}' | '[' | ']'
                    | '(' | ')' | '|' | '"' | ',' | ':' | '\'' | '\\' | '!' | '=' | '~' | '&' | '^' | '<' | '>' );
fragment ALPHA		: [a-zA-Z];


DIGIT		: [0-9];
DATE
: DIGIT DIGIT DIGIT DIGIT (DASH DIGIT DIGIT (DASH DIGIT DIGIT)?)?
;

NL                  : [\r\n] -> skip;
ws                  : WS+;

WORD				: ALPHABET+;
//WORD                : ALPHABET* ALPHA ALPHABET*;  // needs to have at least one alphabetical letter (non-numeric)


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
: DATE
;

dateOp
: SINCE
| UNTIL
| IN
| ON
;

operator
:	(NEG? EQ) | LT | GT | LEQ | GEQ | TILDE | NEGTILDE;

expr
: constraint
| dateconstraint
| token
;

dateconstraint
: field dateOp date
//| date dateOp field dateOp date
;

constraint
: field operator value
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
: '"' WORD+ '"'
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