// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//                                  								//
//		COSMAS II zeilenorientierten Suchanfragesprache (C2 plain syntax)    		//
//    		lokale Grammatik für Option 'Case'.						// 
// 		(Bsp: :fi: in :fi:Hendrix .							//
// 		v-0.1 - 14.12.12/FB								//
//                                  								//
// Externer Aufruf: 										//
// Mögliche Werte für die Case-Optionen:							//
// www.ids-mannheim.de/cosmas2/win-app/hilfe/suchanfrage/eingabe-grafisch/syntax/GROSS_KLEIN.html //
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

grammar c2ps_optCase;

options {output=AST;}

tokens  {CASE; }
@header {package de.ids_mannheim.korap.query.parse.cosmas;}
@lexer::header {package de.ids_mannheim.korap.query.parse.cosmas;}

// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//
//                   PROX-Lexer
//
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

// hier sind die 'englischen' Abkürzungen definiert. Es gibt noch die Entsprechungen in deut. Sprache.
//CA	:	('fi'|'Fi'|'fu'|'Fu'|'fs'|'Fs'|'fl'|'Fl'|'Os'|'os'|'Oi'|'oi'|'Ou'|'ou'|'Ol'|'ol'|'Ds'|'ds'|'Di'|'di');

fragment CA_FIRST
	:	('F'|'f');
fragment CA_OTHER
	:	('O'|'o');
fragment CA_HOW
	:	('s'|'i'|'u'|'l');
fragment CA_DIA
	:	('D'|'d');
		
CA	:	((CA_FIRST|CA_OTHER) CA_HOW) | ( CA_DIA ('s'|'i') );

// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//
//                   PROX-Parser
//
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 
optCase	:	ca+=CA+ 

		-> ^(CASE CA+ ) ;


