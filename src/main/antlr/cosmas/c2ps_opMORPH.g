// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//																				//
// 	COSMAS II zeilenorientierten Suchanfragesprache (C2 plain syntax)			//
// 	lokale Grammatik für MORPH(Expr).											//
//	01.06.26/FB																	//
// TODO: -																		//
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

grammar c2ps_opMORPH;

options {output=AST;}

tokens {OPMORPH; EMPTY;
	   }
		
@header {package de.ids_mannheim.korap.query.parse.cosmas;}
@lexer::header {package de.ids_mannheim.korap.query.parse.cosmas;}

// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//
// 						Lexer
//
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

WS  	:	( ' '|'\t'|'\r'|'\n')+ {skip();}; // $channel=HIDDEN;};

// remove '#' from ID to avoid #ELEM(C) being tokenized as an ID;
// starjating '#' should not start an ID has no effect in ANTLR.
// ID may contain an escaped ', e.g. l\'été.
//ID	   :	(~('#'|'\''|' '|'='|'!'|'<'|'>'|')') | ('\\' '\''))+;

// accept 1..3 for person annotation in marmot:

EXPR		: ('a'..'z' | 'A'..'Z')('a'..'z' | 'A'..'Z'| '0'..'9'|'_')* | ( '1'|'2'|'3');
REG_EXPR	: ('a'..'z' | 'A'..'Z')('a'..'z' | 'A'..'Z'| '0'..'9'|'_' | '[' | ']' | '*' | '?' | '+' | '.' | '\\')* ;

OP		: ('=' | '!=' | '<>' ) ;

// Yes, ' ' must be specified as being part of the token that the lexer is identifying,
// i.e. when tokenizing the input, the WS rule is not activ.
// n.b. AVEXP1-6 must return the whole expression as 1 token with blanks in lists = 1 avExpr.

fragment B_EXPR	: EXPR ':' '-'? REG_EXPR | '-'? REG_EXPR ;

AVEXPR1	: EXPR '/' EXPR OP (B_EXPR | '\'' B_EXPR (' ' | B_EXPR)* '\'' | '"' B_EXPR (' ' | B_EXPR)* '"' ) ;
AVEXPR3	:          EXPR OP (B_EXPR | '\'' B_EXPR (' ' | B_EXPR)* '\'' | '"' B_EXPR (' ' | B_EXPR)* '"' ) ;
AVEXPR5	:                  EXPR ':' '-'? REG_EXPR ;
AVEXPR6 :							'-'? REG_EXPR ;

// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//
// 						Parser
//
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

// the '&' is optional and removed from the resulting AST.

opMORPH	:	'MORPH(' ')'		 				EOF	-> ^(OPMORPH EMPTY)
		|	'MORPH(' avExpr ('&'? avExpr)* ')' 	EOF	-> ^(OPMORPH avExpr+ );
		
avExpr	: AVEXPR1 
		| AVEXPR3
		| AVEXPR5
		| AVEXPR6
		| EXPR
		| REG_EXPR;


