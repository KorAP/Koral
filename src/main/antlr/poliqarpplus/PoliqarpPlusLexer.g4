lexer grammar PoliqarpPlusLexer;

@header {package de.ids_mannheim.korap.query.parse.poliqarpplus;}

import Regex;

options
{
language=Java;
}

/*
 -- author: Joachim Bingel
 -- date: 14-06-27

 -- updated: 18-09-28 (diewald) 


 Poliqarp Query Language lexer

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

POSITION_OP : ('contains' | 'startswith' | 'startsWith' | 'endswith' | 'endsWith' | 'matches' | 'overlaps') ;
RELATION_OP	: ('dominates' | 'dependency' | 'relatesTo');
MATCH_OP 	: ('focus' | 'split'); 
SUBMATCH_OP	: 'submatch';
WITHIN		: 'within';
META		: 'meta';

/*
 Regular expression
 /x allows submatches like /^.*?RE.*?$/
 /X forces full matches
 /i means case insensitivity
 /I forces case sensitivity
*/
FLAG_xi      : '/' ( ('x'|'X') ('i'|'I')? );
FLAG_ix      : '/' ( ('i'|'I') ('x'|'X')? );



/** Simple strings and Simple queries */
WS                  : [ \t]  -> channel(HIDDEN);
fragment NO_RE      : ~[ \t/];
fragment ALPHABET   : ~('\t' | ' ' | '/' | '*' | '?' | '+' | '{' | '}' | '[' | ']'
                    | '(' | ')' | '|' | '"' | ',' | ':' | '\'' | '\\' | '!' | '=' | '~' | '&' | '^' | '<' | '>' | '#' );
NUMBER              : [0-9]+;

NL                  : [\r\n] -> skip;

WORD                : ALPHABET+;


/* Complex queries */
LPAREN      : '[';
RPAREN      : ']';
LRPAREN     : '(';
RRPAREN     : ')';
NEG         : '!';
QMARK		: '?';
CONJ        : '&';
DISJ        : '|';
COMMA		: ',';
LT			: '<';
GT			: '>';
LBRACE		: '{';
RBRACE		: '}';
SLASH		: '/';
COLON		: ':';
TILDE		: '~';
EQ			: '=';
CARET 		: '^';
STAR		: '*';
PLUS		: '+';
EMPTYREL	: '@';
BACKSLASH	: '\\';
SQUOTE      : '\'';
HASH        : '#';

REGEX             : RE_dquote;

ESC_SQUOTE        : BACKSLASH SQUOTE;
