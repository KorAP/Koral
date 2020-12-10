grammar CQP;

/*@header {package de.ids_mannheim.korap.query.parse.poliqarpplus;}



options
{
language=Java;
}*/

/*
 -- author: Joachim Bingel
 -- date: 14-06-27

 -- updated: 28-09-2018 (diewald) 


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

request: query within? meta?  ';' ;
POSITION_OP : ('matches' | 'overlaps') ; // not in PQ tutorial
RELATION_OP	: ('dominates' | 'dependency' | 'relatesTo'); //not in PQ tutorial
MATCH_OP 	: 'meet'; // | 'split');  split is not in PQ tutorial
SUBMATCH_OP	: 'submatch';  //not in PQ tutorial
WITHIN		: 'within';
META		: 'meta';
SPANCLASS_ID: ('@'| '@1');




/*
 Regular expression
 /x allows submatches like /^.*?RE.*?$/
 /X forces full matches
 /i means case insensitivity
 /I forces case sensitivity
 %c means case insensitivity
 %d ignore diacritics */
/*FLAG_xi      : '/' ( ('x'|'X') ('i'|'I')? );
FLAG_ix      : '/' ( ('i'|'I') ('x'|'X')? );*/

FLAG_cd: '%' ('c' ('d')?);
FLAG_dc: '%' ('d' ('c')?);

/* l flag: alternative for escape character "\"; word="?" %*/
FLAG_l: '%' 'l';


/** Simple strings and Simple queries */
WS                  : [ \t]  -> channel(HIDDEN);
fragment FOCC       : '{' WS* ( [0-9]* WS* ',' WS* [0-9]+ | [0-9]+ WS* ','? ) WS* '}'; // i don;t know what is this;
fragment NO_RE      : ~[ \t/];
fragment ALPHABET   : ~('\t' | ' ' | '/' | '*' | '+' | '{' | '}' | '[' | ']'
                    | '(' | ')' | '|' | '"' | ',' | ':' | '\'' | '\\' | '!' | '=' | '~' | '&' | '^' | '<' | '>' | ';' | '?' | '@');
NUMBER              : ('-')? [0-9]+;

NL                  : [\r\n] -> skip;

WORD: ALPHABET+ ;


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
//MINUS: '-';

//in PQ+, DQUOTE was not defined
DQUOTE: ('"'|'„'|'“');


/* Regular expressions and Regex queries */
fragment RE_symbol     : ~('*' | '?' | '+' | '{' | '}' | '[' | ']'
                     | '(' | ')' | '|' | '\\' | '"' | ':' | '\'');
fragment RE_esc      : (('\\' ('.' | '*' | '?' | '+' | '{' | '}' | '[' | ']'
                     | '(' | ')' | '|' | '\\' | ':'))| '\'' '\'' |  '"' '"');
fragment RE_char     : (RE_symbol | RE_esc );
fragment RE_alter    : ((RE_char | ('(' RE_expr ')') | RE_chgroup) '|' RE_expr )+;

fragment RE_chgroup  : '[' RE_char+ ']';
fragment RE_quant	 : (RE_star | RE_plus | RE_occ) QMARK?;
fragment RE_opt      : (RE_char | RE_chgroup | ( '(' RE_expr ')')) '?';
fragment RE_star     : (RE_char | RE_chgroup | ( '(' RE_expr ')')) '*';
fragment RE_plus     : (RE_char | RE_chgroup | ( '(' RE_expr ')')) '+';
fragment RE_occ      : (RE_char | RE_chgroup | ( '(' RE_expr ')')) FOCC;
fragment RE_group    : '(' RE_expr ')';
fragment RE_expr     : ('.' | RE_char | RE_alter | RE_chgroup | RE_opt | RE_quant | RE_group)+;
/* se pot face cautari dupa ghilimele sau apostrof, daca folosim pt marcarea regex apostrof respectiv gilimele: '"' sau "'"*/
fragment RE_dquote   : DQUOTE  (RE_expr | '\'' | ':' )* DQUOTE;
fragment RE_squote   : SQUOTE  (RE_expr | '"' | ':')* SQUOTE;


REGEX: RE_dquote|RE_squote;
ESC_SQUOTE        : BACKSLASH SQUOTE;

/*parser*/


flag
: FLAG_l | FLAG_cd | FLAG_dc
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


// i don't know how to represent it in cqp: maybe think of the whole "foundry\layer" string as a key (p=-attribute in cqp)?
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
:  NEG* (foundry SLASH)? layer termOp regex (COLON value)? flag? | NEG* foundry flag layer? termOp regex (COLON value)? flag? | NEG* LRPAREN term RRPAREN 
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

exactly
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
  (
    min COMMA max 
    | COMMA max 
    | min COMMA 
    | exactly
  ) 
  RBRACE
;

emptyToken
: LPAREN RPAREN
;

termGroup
: (term | LRPAREN termGroup RRPAREN) boolOp (term | LRPAREN termGroup RRPAREN | termGroup)
| NEG* LRPAREN termGroup RRPAREN
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
    | regex flag?
    )
;


//span is not implemented like this in CQP-- see struct
/*span
: LT ((foundry SLASH)? layer termOp)? key NEG* ((LRPAREN term RRPAREN|LRPAREN termGroup RRPAREN)? | (term|termGroup)?) GT
;*/

position
: POSITION_OP LRPAREN (segment|sequence) COMMA (segment|sequence) RRPAREN
;

/*Elena */



relation
: RELATION_OP LRPAREN ((EMPTYREL|relSpec)? repetition? COLON)? (segment|sequence) COMMA (segment|sequence) RRPAREN
;

relSpec
: (foundry SLASH)? layer (termOp key)?
;

submatch
: SUBMATCH_OP LRPAREN startpos (COMMA length)? COLON (segment|sequence) RRPAREN
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

spanclass
: (SPANCLASS_ID (segment|sequence) | label ':' (segment|sequence))
;

label: WORD;

emptyTokenSequence
: (emptyToken repetition?)+
;

emptyTokenSequenceClass
: SPANCLASS_ID emptyTokenSequence     // class defined around empty tokens 
;


distance
: emptyTokenSequence
;



segment
: ( position 
  | token 
  //| span 
  | struct
  | group
  | spanclass 
  | matching
  | submatch
  | relation
  | LRPAREN segment RRPAREN
  | emptyTokenSequence
  | emptyTokenSequenceClass
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

/* special CQP section*/


//structural atributes instead of positional operators : cotains, startwith, endswith
// LT and GT are optional to match the struct in meet
struct:
LT? '/'? WORD GT?
;

/* MU queries instead of focus operator; CQP offers search-engine like Boolean queries in a special meet-union (MU) notation. This
feature goes back to the original developer of CWB and is not supported officially. In particular,
there is no precise specifcation of the semantics of MU queries and the original implementation
does not produce consistent results. A meet clause matches two token patterns within a specified distance of each other. More
precisely, instances of the firrst pattern are altered, keeping only those where the second pattern
occurs within the specified window. For example, the following query founds nouns that co-occur
with the adjective lovely:
> MU(meet [pos = "NN.*"] [lemma = "lovely"] -2 2);
This query returns all nouns for which lovely occurs within two tokens to the left (window starting
at offset -2) or right (window ending at offset +2)). The adjective lovely is not included in the
match, nor marked in any other way.
focus(der {Baum}) = MU(meet(Baum, der))
*/
matching
: 'MU' LRPAREN meet RRPAREN
;
meet
:(MATCH_OP (segment|sequence)  NUMBER?  NUMBER? WORD?) | (MATCH_OP ((LRPAREN meet RRPAREN) | (segment|sequence))+ NUMBER? NUMBER? WORD?)
;