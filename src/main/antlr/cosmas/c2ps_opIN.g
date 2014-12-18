// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//												//
// 	COSMAS II zeilenorientierten Suchanfragesprache (C2 plain syntax)			//
// 	lokale Grammatik für #IN() und #IN(Options).						//
//	17.12.12/FB										//
//      v-0.1											//
//												//
// Opts nimmt eine oder mehrere, durch Kommata getrennte Optionen auf:				//
// - Bereichsoptionen (RANGE): ALL, HIT, -.							//
// - Positionsoptionen (POS): L, R, F, FE, FI, N, -.						//
// - Ausschließungsoptionen: %, -.								//
// - Gruppenbildungsoptionen (GROUP): min, max, -.						//
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

grammar c2ps_opIN;

options {output=AST;}
tokens  {OPIN;
	 RANGE; ALL; HIT; 
	 POS; 
	 EXCL; YES;
	 GROUP; MIN; MAX; }

@header {package de.ids_mannheim.korap.query.parse.cosmas;}
@lexer::header {package de.ids_mannheim.korap.query.parse.cosmas;}
	 
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//
// 						Lexer
//
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

WS	:	(' ')+ {skip();};

POSTYP	:	('L'|'l'|'R'|'r'|'F'|'f'|'FE'|'fe'|'FI'|'fi'|'N'|'n' );

// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//
// 						Parser
//
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

opIN	:	'#IN' -> ^(OPIN)
	|	'#IN(' opts? ')' -> ^(OPIN opts?);

opts	:	opt (',' opt)*

		-> opt*;

opt	:	(optRange |optPos | optExcl | optGrp);

// Bereich:
optRange:	('ALL' | 'all') -> ^(RANGE ALL) 
	| 	('HIT' | 'hit')	-> ^(RANGE HIT); 

// Position:
optPos	:	POSTYP

		-> ^(POS POSTYP);

optExcl	:	'%' 

		-> ^(EXCL YES);

optGrp	:	('MIN' | 'min') -> ^(GROUP MIN)
	| 	('MAX' | 'max') -> ^(GROUP MAX) ;

