// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//												//
// 	COSMAS II zeilenorientierten Suchanfragesprache (C2 plain syntax)			//
// 	lokale Grammatik für #OV() und #OV(Options).						//
//	17.12.12/FB										//
//      v-0.1											//
//												//
// Opts nimmt eine oder mehrere, durch Kommata getrennte Optionen auf:				//
// - Positionsoptionen (POS): L, R, F, FE, FI, X, -.						//
// - Ausschließungsoptionen (EXCL): %, -.							//
// - Gruppenbildungsoptionen (GROUP): min, max, -.						//
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

grammar c2ps_opOV;

options {output=AST;}
tokens  {OPOV;
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

POSTYP	:	'L'|'l'|'R'|'r'|'F'|'f'|'FE'|'fe'|'FI'|'fi'|'N'|'n'|'X'|'x' ;

// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//
// 						Parser
//
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

opOV	:	'#OV'            -> ^(OPOV)
	|	'#OV(' opts? ')' -> ^(OPOV opts?);

opts	:	opt (',' opt)*   -> opt*;

opt	:	(optPos | optExcl | optGrp);

// Position:
optPos	:	POSTYP 

		-> ^(POS POSTYP);

optExcl	:	'%' 

		-> ^(EXCL YES);

optGrp	:	('MIN' | 'min') -> ^(GROUP MIN)
	| 	('MAX' | 'max') -> ^(GROUP MAX) ;

