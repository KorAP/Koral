// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//												//
// 	COSMAS II zeilenorientierten Suchanfragesprache (C2 plain syntax)			//
// 	lokale Grammatik für #ELEM(Expr).							//
//	08.01.13/FB										//
//      v-0.2											//
// 05.05.26/FB : #ELEM(foundry/layer=name) is treated like #ELEM(avExpr), although it might 
//   be the element name: OK.
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
@lexer::members { private boolean inSQuotes = false; private boolean inDQuotes = false; }

// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//
// 						Lexer
//
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

WS  	:	( ' '|'\t'|'\r'|'\n')+ {skip();}; // old: $channel=HIDDEN;};

// remove '#' from ID to avoid #ELEM(C) being tokenized as an ID;
// stating '#' should not start an ID has no effect in ANTLR.
// ID may contain an escaped ', e.g. l\'été.
// ID	   :	(~('#'|'\''|'/'|'\\'| '|'='|'!'|'<'|'>'|')') | ('\\' '\''))+;

// declare ATTID before ID.
APOS	: '\'' { inSQuotes  = !inSQuotes;};
APOS2	: '\"' { inDQuotes  = !inDQuotes;};
ID		: ('A'..'Z'|'a'..'z')('A'..'Z'|'a'..'z'|'0'..'9')*;
ATTID	: ID '/' ID;

VID		: ( ~( '\''| '\"' | '='|'!'|'<'|'>'|')'|'('|'/'|'\\' | ' ' ) )+ ;

VIDinQ	: {inSQuotes}? => ( (~( '\'' | ' ' ) | ('\\' '\'') )+ )
		| {inDQuotes}? => ( (~( '\"' | ' ' ) | ('\\' '\"') )+ );

OPEQ	: {!inSQuotes && !inDQuotes}? '=';

// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//
// 						Parser
//
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

opELEM	:	'#ELEM(' ')'              EOF -> ^(OPELEM EMPTY)
		|	'#ELEM(' elem avExpr+ ')' EOF -> ^(OPELEM elem avExpr+)
		|	'#ELEM(' elem ')' 		  EOF -> ^(OPELEM elem)
		|	'#ELEM(' avExpr+ ')'      EOF -> ^(OPELEM avExpr+);

// elem = element name:
elem	:	(ID) => ID -> ^(ELNAME ID);

att		: 	ATTID 		 
		|	ID;
		
val0	: 	ID  
		|	VID ;

vals	:   (VIDinQ | ID)+;  

avExpr	:	att op APOS  vals APOS  -> ^(op att vals)
		|	att op APOS2 vals APOS2 -> ^(op att vals)
		|	att op val0			    -> ^(op att val0) ;
	
op		:	OPEQ			-> ^(EQ)
		|	('<>' | '!=')   -> ^(NOTEQ);
