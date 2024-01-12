// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//												
// 	lokale Grammatik der COSMAS II zeilenorientierten Suchanfragesprache (= c2ps)		
//	für den Abstandsoperator /w... und %w...	
//	v-1.0 - 07.12.12/FB							
//  v-1.1 - 30.11.23/FB opPROX accepts any order of direction, measure and value.
//												
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

grammar c2ps_opPROX;

options {output=AST;}

tokens  { PROX_OPTS; 
	  TYP; PROX; EXCL; 
	  DIST_LIST; DIST; RANGE; VAL0; 
	  MEAS; // measure
	  DIR; PLUS; MINUS; BOTH;
	  GRP; MIN; MAX;
	  }
	  
@header {package de.ids_mannheim.korap.query.parse.cosmas;
		 import  de.ids_mannheim.korap.util.C2RecognitionException;}
		 
@lexer::header {package de.ids_mannheim.korap.query.parse.cosmas;}


// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//
// 						PROX-Lexer
//
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

DISTVALUE
	:	('0' .. '9')+ ;
	
// trying to catch everything (at the end of the option sequence) that should not appear inside the prox. options:
// e.g. /w5umin -> remain = 'umin'.

PROX_REMAIN
	: (',')? ('b'..'h'|'j'..'l'|'n'|'o'|'q'|'r'|'u'|'v'|'y'|'z'|'B'..'H'|'J'..'L'|'N'|'O'|'Q'|'R'|'U'|'V'|'Y'|'Z') (~ ' ')* ;

// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//
// 						PROX-Parser
//
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *


opPROX[int pos]	:	proxTyp proxDist[$pos] (',' proxDist[$pos])* (',' proxGroup)?  (proxRemain[$pos])?
		
		-> ^(PROX_OPTS {$proxTyp.tree} ^(DIST_LIST proxDist+) {$proxGroup.tree} {$proxRemain.tree});
	
proxRemain[int pos] : PROX_REMAIN

		-> { c2ps_opPROX.checkRemain(DIST, $PROX_REMAIN.text, $pos) };

proxTyp	:  '/' -> ^(TYP PROX)	// klassischer Abstand.
		|  '%' -> ^(TYP EXCL);	// ausschließender Abstand.

// proxDist: e.g. +5w or -s0 or /w2:4 etc.
// kein proxDirection? hier, weil der Default erst innerhalb von Regel proxDirection erzeugt werden kann.

// new rule: accepts options in any order:
// count each option type and find out if any one is missing or occures multiple times.
// 28.11.23/FB

proxDist[int pos]
@init{ int countM=0; int countD=0; int countV=0;}
	:
		((m=proxMeasure {countM++;})|(d=proxDirection {countD++;})|(v=proxDistValue {countV++;}) )+
		 
	->  {c2ps_opPROX.encodeDIST(DIST, DIR, $d.tree, $m.tree, $v.tree, $proxDist.text, countD, countM, countV, $pos)};
	

// new rule accepts only '+' and '-'; default tree for direction is 
// set in c2ps_opPROX.encodeDIST() now.
// 28.11.23/FB

proxDirection
		: '+'	-> ^(DIR PLUS)
		| '-'	-> ^(DIR MINUS);

proxDistValue	:	(m1=proxDistMin ) (':' m2=proxDistMax)? 
	
		-> {$m2.text != null}? ^(RANGE $m1  $m2)
		->				       ^(RANGE VAL0 $m1);

// mentioning >1 measures will be checked/rejected in c2ps_opPROX.encodeDIST(). 

proxMeasure
	:	(meas='w'|meas='s'|meas='p'|meas='t') -> ^(MEAS $meas) ;  

proxDistMin
	:	DISTVALUE;
	
proxDistMax
	:	DISTVALUE;
	
proxGroup
	:	('min'|'MIN') -> ^(GRP MIN)
	|	('max'|'MAX') -> ^(GRP MAX);
	

	