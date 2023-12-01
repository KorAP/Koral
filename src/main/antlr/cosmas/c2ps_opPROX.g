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
	  GRP; MIN; MAX; }
	  
@header {package de.ids_mannheim.korap.query.parse.cosmas;}
@lexer::header {package de.ids_mannheim.korap.query.parse.cosmas;}

// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//
// 						PROX-Lexer
//
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

DISTVALUE
	:	('0' .. '9')+ ;
	
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//
// 						PROX-Parser
//
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *


opPROX	:	proxTyp proxDist (',' proxDist)* (',' proxGroup)? 
		
		-> ^(PROX_OPTS {$proxTyp.tree} ^(DIST_LIST proxDist+) {$proxGroup.tree});
	
proxTyp	:  '/' -> ^(TYP PROX)	// klassischer Abstand.
		|  '%' -> ^(TYP EXCL);	// ausschließender Abstand.

// proxDist: e.g. +5w or -s0 or /w2:4 etc.
// kein proxDirection? hier, weil der Default erst innerhalb von Regel proxDirection erzeugt werden kann.
/* incomplete original version:
proxDist:	proxDirection (v1=proxDistValue m1=proxMeasure | m2=proxMeasure v2=proxDistValue)

		-> {$v1.tree != null}? ^(DIST {$proxDirection.tree} {$v1.tree} {$m1.tree})
		->		 		       ^(DIST {$proxDirection.tree} {$v2.tree} {$m2.tree});
*/

// new rule: accepts options in any order:
// but how to handle multiple values for those options?
// 28.11.23/FB

proxDist:	(m=proxMeasure|d=proxDirection|v=proxDistValue)+
		      
		->  {c2ps_opPROX.encodeDIST(DIST, DIR, $d.tree, $m.tree, $v.tree, $proxDist.text)};

// new rule accepts only '+' and '-'; default tree for direction is 
// set in c2ps_opPROX.encodeDIST() now.
// 28.11.23/FB

proxDirection
		: '+'	-> ^(DIR PLUS)
		| '-'	-> ^(DIR MINUS);

proxDistValue	:	(m1=proxDistMin ) (':' m2=proxDistMax)? 
	
		-> {$m2.text != null}? ^(RANGE $m1  $m2)
		->				       ^(RANGE VAL0 $m1);

proxMeasure
	:	(meas='w'|meas='s'|meas='p'|meas='t') -> {c2ps_opPROX.checkMeasure($meas)} ;  // {c2ps_opPROX.checkMeasure($meas)} ? ^(MEAS $meas);

proxDistMin
	:	DISTVALUE;
	
proxDistMax
	:	DISTVALUE;
	
proxGroup
	:	'min' -> ^(GRP MIN)
	|	'max' -> ^(GRP MAX);
	
