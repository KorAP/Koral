grammar CQP;

@header {package de.ids_mannheim.korap.query.parse.cqp;}



options
{
language=Java;
}

/*

CQP grammar 
--author Elena IRimia, based on PQ Plus grammar (author: Joachim BIngel)
Based on CQP
 - http://cwb.sourceforge.net/files/CQP_Tutorial/
&& - elemente de scos din gramatica!
 */

POSITION_OP : 'lbound' | 'rbound'; //&& 'contains' | 
REGION_OP: 'region';
RELATION_OP	: ('dominates' | 'dependency' | 'relatesTo'); //not in PQ tutorial &&
MATCH_OPM : 'meet' ;
//MATCH_OPF: 'focus'; // | 'split');  split is not in PQ tutorial &&
SUBMATCH_OP	: 'submatch';  //not in PQ tutorial &&
WITHIN		: 'within';
MU: 'MU';
META		: 'meta';
SPANCLASS_ID: ('@'| '@1');


/*
 Regular expression
 %c means case insensitivity
 %d ignore diacritics */
 /* l flag: alternative for escape character "\"; word="?"; not implemented in KoralQuery %*/

FLAG_lcd: '%' (('l'|'c'|'d'|'L'|'C'|'D') ('l'|'c'|'d'|'L'|'C'|'D')? ('l'|'c'|'d'|'L'|'C'|'D')?);




/** Simple strings and Simple queries */
WS                  : [ \t]  -> channel(HIDDEN);
fragment FOCC       : '{' WS* ( [0-9]* WS* ',' WS* [0-9]+ | [0-9]+ WS* ','? ) WS* '}'; 
fragment ALPHABET   : ~('\t' | ' ' | '/' | '*' | '+' | '{' | '}' | '[' | ']'
                    | '(' | ')' | '|' | '"' | ',' | ':' | '\'' | '\\' | '!' | '=' | '~' | '&' | '^' | '<' | '>' | ';' | '?' | '@' |'%');
NUMBER              : ('-')? [0-9]+;

NL                  : [\r\n] -> skip;

WORD: ALPHABET+ ;

/*Complex queries*/
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
EMPTYREL	: '@'; //take care: two tokens, EMPTYREL and SPANCLAS_ID, matching the same input; &&
BACKSLASH	: '\\';
SQUOTE      : '\'';
//in PQ+, DQUOTE was not defined
DQUOTE: ('"'|'„'|'“'|'“'|'”');


/* Regular expressions and Regex queries */
fragment RE_symbol     : ~('*' | '?' | '+' | '{' | '}' | '[' | ']'
                     | '(' | ')' | '|' | '\\' | '"' | ':' | '\'');
fragment RE_esc      : ('\\' ('.' | '*' | '?' | '+' | '{' | '}' | '[' | ']'
                     | '(' | ')' | '|' | '\\' | ':' | '"' | '\''))| '\'' '\'' |  '"' '"';
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
/* you can search for DQUOTE inside SQUOUTE, and viceversa:  '"' or "'"; */
fragment RE_dquote   : DQUOTE  (RE_expr | '\'' | ':' )* DQUOTE; // DQOUTE is not good, modify like verbatim in PQ+!
fragment RE_squote   : SQUOTE  (RE_expr | '"' | ':')* SQUOTE; 



REGEX: RE_dquote|RE_squote;





/*parser*/


flag
: FLAG_lcd
;


boolOp
: CONJ | DISJ
;

regex
: REGEX
;



key
: regex; // | verbatim


// key for spans
skey
: WORD
; 

// think of the whole "foundry\layer" string as a key (p-attribute in cqp), like in PQPlus grammar
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
 ///////
/* Fields */
term
: NEG* (foundry SLASH)? layer termOp key (COLON value)? flag?
| NEG* foundry flag layer? termOp key (COLON value)? flag?
| NEG* LRPAREN (foundry SLASH)? layer termOp key (COLON value)? flag? RRPAREN  (CONJ position)+ // for position ops
| NEG* LRPAREN foundry flag layer? termOp key (COLON value)? flag? RRPAREN (CONJ position)+ // for position ops
| NEG* LRPAREN term RRPAREN 
| key flag? (CONJ position)+ // for position ops
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


startpos //&&
: NUMBER
;

length //&&
: NUMBER
;


range
: LBRACE 
  (
    min COMMA max 
    | COMMA max 
    | min COMMA 
    | max
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
    | key flag?
    )
;


span
: (skey // for lbound/sbound; check how it works for meet!
  | LT ((foundry SLASH)? layer termOp)? '/'? skey NEG* ((LRPAREN term RRPAREN|LRPAREN termGroup RRPAREN)? | (term|termGroup)?) GT
  )
;

closingspan
:
LT '/' ((foundry SLASH)? layer termOp)? '/'? skey NEG* ((LRPAREN term RRPAREN|LRPAREN termGroup RRPAREN)? | (term|termGroup)?) GT
;

position //&&
//: POSITION_OP LRPAREN (segment|sequence) COMMA (segment|sequence) RRPAREN
: POSITION_OP LRPAREN span RRPAREN 
;


disjunction
: (segment|sequence|group) (DISJ (segment|sequence|group))+ // do i need group here? test!
;

group
: LRPAREN ( disjunction | sequence ) RRPAREN 
; 

spanclass
: (SPANCLASS_ID (token|segment|group| LRBRACE sequence RRBRACE) // ai adaugat sequence! vezi de ce nu intra pe ea, si daca intra, vezi cum ruleaza procesorul; am nevoie de token si group aici?
  | label COLON (segment|sequence) 
  | LBRACE SPANCLASS_ID? (segment|sequence) RBRACE /* do i need this???*/
  | MATCH_OPM LRPAREN meetunion RRPAREN // for recursive meet
  | MATCH_OPM segment // for simple meet
  )
;

label: WORD; //&&

emptyTokenSequence
: (emptyToken repetition?)+
;

emptyTokenSequenceAround
: (emptyToken PLUS?)+
;



emptyTokenSequenceClass
: (SPANCLASS_ID emptyTokenSequence | label COLON emptyTokenSequence)    // class defined around empty tokens 
;


/* special CQP section*/


//structural atributes instead of positional operators : cotains, startwith, endswith
/* LT and GT are optional to match the struct in meet;
examples:
----------------------------------------------------------------------
positional operators in PQ+          ||     structural attributes in CQP
-------------------------------------------------------------------------
contains(<base/s=s>, "copil")        ||     <s> []+ "copil" []+ </s>;
startsWith(<base/s=s>, "copil")      ||     <s>  "copil";
endsWith(<base/s=s>, "copil")        ||    "copil" </s>;
matches(<base/s=s>, "copil")         ||     <s> copil </s>
overlaps()                           ||       ??? 
---------------------------------------------------------------------------*/



qstruct: isaround | matches; // the query itself is a struct
sstruct: startswith  |  endswith ; // struct is a segment in a query;
isaround :  span emptyTokenSequenceAround (segment|sequence) emptyTokenSequenceAround closingspan; 
matches:  span  (segment|sequence)  closingspan; 
startswith: span segment;
endswith: segment closingspan; // (segment|sequence)
region: SLASH REGION_OP LPAREN span RPAREN;



sequence
: segment+ sstruct
| sstruct segment+
| alignment segment*  // give precedence to this subrule over the next to make sure preceding segments come into 'alignment'
| segment+ alignment segment*
| segment segment+
| alignment (segment|sequence) alignment?
;

segment
: (  token 
  | group
  | region
  | spanclass 
  | matching
  | submatch
  | relation
  | LRPAREN segment RRPAREN
  | emptyTokenSequence
  | emptyTokenSequenceClass
  | span
  | closingspan
  ) 
  repetition?
 ; 

/** Entry point for linguistic queries */

query
: qstruct | sstruct | segment | sequence | disjunction 
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



// PQ Plus rules that are not described in https://korap.ids-mannheim.de/doc/ql/fcsql#page-top; i don't know how they work;
relation
: RELATION_OP LRPAREN ((EMPTYREL|relSpec)? repetition? COLON)? (segment|sequence) COMMA (segment|sequence) RRPAREN
;

relSpec
: (foundry SLASH)? layer (termOp key)?
;

submatch //&&
: SUBMATCH_OP LRPAREN startpos (COMMA length)? COLON (segment|sequence) RRPAREN
;



alignment
: segment? ( (CARET segment)+ | CARET)
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
: (MU LRPAREN meetunion RRPAREN);
// //| (MATCH_OPF LRPAREN SPANCLASS_ID? (segment|sequence)? RRPAREN)
meetunion
: 
(((LRPAREN meetunion RRPAREN) | segment) ((LRPAREN meetunion RRPAREN) | segment) ((NUMBER  NUMBER) | span))
|
(segment segment  ((NUMBER NUMBER) | span))
;


/**
    Entry point for all requests. Linguistic query is obligatory, metadata filtering
    is optional.
*/

request: query within? meta?  ';'? ;
