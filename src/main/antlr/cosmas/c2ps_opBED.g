// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//												//
// 	Lokale Grammatik der COSMAS II zeilenorientierten Suchanfragesprache			//
//	Dez. 2012/FB										//
//      v1.0											//
//	lokale Grammatik für #BED(x, Opts).							//
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

grammar c2ps_opBED;

options {output=AST;}
tokens  {TPBEG; TPEND; }
@header {package de.ids_mannheim.korap.query.parse.cosmas;}
@lexer::header {package de.ids_mannheim.korap.query.parse.cosmas;}

// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//
// 						Lexer
//
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

TP_POS	:	('+'|'-')? ('sa'|'SA'|'se'|'SE'|'pa'|'PA'|'pe'|'PE'|'ta'|'TA'|'te'|'TE') ;

WS	:	(' ')+ {skip();};

// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//
// 						Parser
//
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *


opBEDOpts
	:	',' textpos ')' -> textpos ;
	
textpos	:	( tpBeg ('/' tpEnd)? | '/' tpEnd ) -> tpBeg? tpEnd?;

tpBeg	:	tpExpr -> ^(TPBEG tpExpr);

tpEnd	:	tpExpr -> ^(TPEND tpExpr);

tpExpr	:	tpPos (',' tpPos)* -> tpPos*;

tpPos	:	TP_POS; 
