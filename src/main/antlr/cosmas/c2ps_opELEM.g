// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//												//
// 	COSMAS II zeilenorientierten Suchanfragesprache (C2 plain syntax)			//
// 	lokale Grammatik für #ELEM(Expr).							//
//	08.01.13/FB										//
//      v-0.2											//
// TODO: -											//
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

grammar c2ps_opELEM;

options {output=AST;}

tokens {OPELEM; EMPTY;
	ELNAME; 
	EQ; NOTEQ;
	}
@header {package de.ids_mannheim.korap.query.parse.cosmas;}
@lexer::header {package de.ids_mannheim.korap.query.parse.cosmas;}

// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//
// 						Lexer
//
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

WS  	:	( ' '|'\t'|'\r'|'\n')+ {$channel=HIDDEN;};

// remove '#' from ID to avoid #ELEM(C) being tokenized as an ID;
// stating '#' should not start an ID has no effect in ANTLR.
// ID may contain an escaped ', e.g. l\'été.
ID	:	(~('#'|'\''|' '|'='|'!'|'<'|'>'|')') | ('\\' '\''))+;

// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//
// 						Parser
//
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

opELEM	:	'#ELEM(' ')'              -> ^(OPELEM EMPTY)
	|	'#ELEM(' elem avExpr* ')' -> ^(OPELEM elem avExpr*)
	|	'#ELEM(' avExpr+ ')'      -> ^(OPELEM avExpr+);

elem	:	ID -> ^(ELNAME ID);

avExpr	:	id1=ID op id2=ID            -> ^(op $id1 $id2)
	|	id1=ID op '\'' id3+=ID+ '\'' -> ^(op $id1 $id3+);
	
op	:	'='             -> ^(EQ)
	|	('<>' | '!=')   -> ^(NOTEQ);
	
	