/*
* Copyright 2013 SFB 632.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
lexer grammar AqlLexer;

@header {package de.ids_mannheim.korap.query.parse.annis;}

// additional tokens
tokens {
RANGE,
ANNO,
FROM_TO,
DOM
}
TOK:'tok';
NODE:'node';
META:'meta';
AND:'&';
OR:'|';
EQ_VAL:'==';
EQ: '=';
NEQ:'!=';
DOMINANCE:'>';
POINTING:'->';
PRECEDENCE:'.';
NEAR: '^';
TEST:'%';
IDENT_COV:'_=_';
INCLUSION:'_i_';
OVERLAP:'_o_';
LEFT_ALIGN:'_l_';
RIGHT_ALIGN:'_r_';
LEFT_OVERLAP:'_ol_';
RIGHT_OVERLAP:'_or_';
LEFT_CHILD:'@l';
RIGHT_CHILD:'@r';
COMMON_PARENT:'$';
IDENTITY:'_id_';
ROOT:':root';
ARITY:':arity';
TOKEN_ARITY:':tokenarity';
COMMA:',';
STAR:'*';
BRACE_OPEN:'(';
BRACE_CLOSE:')';
BRACKET_OPEN:'[';
BRACKET_CLOSE:']';
COLON:':';
DOUBLECOLON:'::';
SLASH:'/';
QMARK : '?';

/*
 * Regular expressions (delimited by slashes in Annis)
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


WS : ( ' ' | '\t' | '\r' | '\n' )+ -> skip ;

VAR_DEF
:	('a'..'z'|'A'..'Z') ( '0' .. '9'|'a'..'z'|'A'..'Z')* '#'
;

REF
:	'#' ( '0' .. '9'|'a'..'z'|'A'..'Z')+
;

ID :	('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_'|'-')*;
//ID :	('a'..'z'|'A'..'Z'|'_'|'['|']') ('a'..'z'|'A'..'Z'|'0'..'9'|'_'|'-'|'['|']')*;

DIGITS : ('0'..'9')+;



//START_TEXT_REGEX : '/' -> pushMode(IN_REGEX);
START_TEXT_PLAIN:'"' -> pushMode(IN_TEXT);



mode IN_REGEX;

//END_TEXT_REGEX : '/' -> popMode;
TEXT_REGEX : (~'/'|'\\"')+;

mode IN_TEXT;

END_TEXT_PLAIN : '"' -> popMode;
TEXT_PLAIN : (~'"'|'\\"')+;


