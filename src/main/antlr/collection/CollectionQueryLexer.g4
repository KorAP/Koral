lexer grammar CollectionQueryLexer;

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
FLAG_xi      : '/' (('x'|'X') ('i'|'I')? );
FLAG_ix      : '/' (('i'|'I') ('x'|'X')? );
 
QUOTE				: '"'; 
BACKSLASH           : '\\'; 
LRB					: '(';
RRB					: ')';
LB					: '[';
RB					: ']';
LT					: '<';
GT					: '>';
LEQ					: '<=';
GEQ					: '>=';
EQ					: '=';
AND					: '&' | 'AND' | 'and';
OR					: '|' | 'OR' | 'or';
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
REF                 : 'referTo';
WS 					: ( ' ' | '\t' | '\r' | '\n' )+ -> channel(HIDDEN);
fragment NO_RE      : ~[ \t\/];
fragment ALPHABET   : ~('\t' | ' ' | '/' | '*' | '?' | '+' | '{' | '}' | '[' | ']'
                    | '(' | ')' | '|' | '"' | ',' | '\'' | '\\' | '!' | '=' | '~' | '&' | '^' | '<' | '>' );
// EM: allow ':' in ALPHABET 
fragment ALPHA		: [a-zA-Z];

ESC_QUOTE          : BACKSLASH QUOTE;


DIGIT		: [0-9];
DATE
: DIGIT DIGIT DIGIT DIGIT (DASH DIGIT DIGIT (DASH DIGIT DIGIT)?)?
;

NL                  : [\r\n] -> skip;
//ws                  : WS+;

WORD				: ALPHABET+;
//WORD                : ALPHABET* ALPHA ALPHABET*;  // needs to have at least one alphabetical letter (non-numeric)

REGEX     		    : SLASH .*? SLASH; 
