// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//                                  								//
//		COSMAS II zeilenorientierten Suchanfragesprache (C2 plain syntax)    		//
//    		lokale Grammatik für Wortformen							// 
// 		(Bsp: Hendrix, Hendrix:sa/-pe, :fi:Hendrix, etc. )				//
// 		v-0.3 - 10.01.13/FB								//
//                                  								//
// Strategie: 											//
// - Input string: :cccc:wwww:ppp								//
// - diese Grammatik trennt ccc, www und ppp voneinander, ccc und ppp werden in weiteren 	//
//   lokalen Grammatiken zerlegt.								//
// - Begründung: weil die Tokens in ccc, www und ppp sich überschneiden, ist eine große 	//
//   Grammatik unnötig umständlich.								//
// - Bsp.: :FiOlDs:Würde:sa/-pe,-te -> c=FiOlDs + w=Würde + p=sa/-pe,-te.			//
// Mögliche Werte für die Case-Optionen:							//
// www.ids-mannheim.de/cosmas2/win-app/hilfe/suchanfrage/eingabe-grafisch/syntax/GROSS_KLEIN.html //
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

grammar c2ps_opWF;

options {output=AST;}

tokens  { OPWF; OPLEM; OPTCASE; TPOS; }
@header {package de.ids_mannheim.korap.query.parse.cosmas;}
@lexer::header {package de.ids_mannheim.korap.query.parse.cosmas;}

// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//
//                   PROX-Lexer
//
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

fragment STRING
	:	( ~(':' | ' ') | '\\:' )+ ; 

Case	:	':' STRING ':';

TPos	:	':' STRING;

WF	:	STRING;

WS	:	(' ')+ {skip();};

// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//
//                   PROX-Parser
//
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 
searchWFs
	:	searchWF+;
	
searchWF:	optCase? wordform tpos?

		-> ^(OPWF wordform optCase? tpos? ) ;

wordform:	WF -> {c2ps_opWF.encode($WF.text, OPWF)};

// Case Options:
optCase	:	Case 

		-> {c2ps_optCase.check($Case.text, $Case.index)} ;

// textposition Options:
tpos	:	TPos 

		-> ^(TPOS {c2ps_opBED.checkTPos($TPos.text, $TPos.index)});

// analog für Lemmata, kein optCase:
searchLEM
	:	wordform tpos?
	
		-> ^(OPLEM wordform tpos?);
		
