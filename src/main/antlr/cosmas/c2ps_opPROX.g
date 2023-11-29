// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//												//
// 	lokale Grammatik der COSMAS II zeilenorientierten Suchanfragesprache (= c2ps)		//
//	für den Abstandsoperator /w... und %w...						//
//	v-1.0 - 07.12.12/FB									//
//												//
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
	
DISTOPTS
	:	('s'|'p'|'w'|'t'|'+'|'-')+;
	
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
		-> 		       ^(DIST {$proxDirection.tree} {$v2.tree} {$m2.tree});
*/

// +++ new version 29.11.23 by including all possible option ordering +++ //
/*
proxDist:	
		proxDirection v1=proxDistValue m1=proxMeasure 
			-> ^(DIST {$proxDirection.tree} {$v1.tree} {$m1.tree})
	|	proxDirection m1=proxMeasure v1=proxDistValue
			-> ^(DIST {$proxDirection.tree} {$v1.tree} {$m1.tree})
	|	m1=proxMeasure d1=proxDirection v1=proxDistValue
			-> ^(DIST {$d1.tree} {$v1.tree} {$m1.tree})
	|	m1=proxMeasure v1=proxDistValue d1=proxDirection 
			-> ^(DIST {$d1.tree} {$v1.tree} {$m1.tree})
	|	v1=proxDistValue m1=proxMeasure d1=proxDirection 
			-> ^(DIST {$d1.tree} {$v1.tree} {$m1.tree})
	|	v1=proxDistValue d1=proxDirection m1=proxMeasure 
			-> ^(DIST {$d1.tree} {$v1.tree} {$m1.tree});
*/			

// new version: accepts any order (28.11.23/FB):

proxDist:	(DISTOPTS|DISTVALUE)+ 

		-> {c2ps_opPROX.encode($proxDist.text, DIST)};


//+++++++++ new version: accepts options in any order (28.11.23/FB): +++++++ //
	// -> {$v.tree != null && $m.tree != null} ? ^(DIST DIST); //{c2ps_opPROX.encodeDIST(DIR, $d.tree, $m.tree, $v.tree)} );
/*
proxDist:	(m=proxMeasure|d=proxDirection|v=proxDistValue)+
		      
		->  {c2ps_opPROX.encodeDIST(DIST, $m.tree, $m.tree, $m.tree)};
*/
/* old rule for optional direction with default setting:
proxDirection:
		(p='+'|m='-')?	-> {$p != null}? ^(DIR PLUS)
						-> {$m != null}? ^(DIR MINUS)
						->               ^(DIR BOTH) ;
*/

// new rule accepts + and -. Default tree for direction is set in c2ps_opPROX.encode():
// 28.11.23/FB
/*
proxDirection
		: '+'	-> ^(DIR PLUS)
		| '-'	-> ^(DIR MINUS);
*/
/*
proxDistValue	// proxDistMin ( ':' proxDistMax)? ;
	:	(m1=proxDistMin -> ^(DIST_RANGE VAL0 $m1)) (':' m2=proxDistMax -> ^(DIST_RANGE $m1 $m2))? ;
*/

// proxDistMin ( ':' proxDistMax)? ;
/*
proxDistValue	
	:	(m1=proxDistMin ) (':' m2=proxDistMax)? 
	
		-> {$m2.text != null}? ^(RANGE $m1  $m2)
		->		       ^(RANGE VAL0 $m1);
*/
/*
proxMeasure
	:	(m='w'|m='s'|m='p'|m='t') -> ^(MEAS $m);

proxDistMin
	:	DISTVALUE;
	
proxDistMax
	:	DISTVALUE;
*/	
proxGroup
	:	'min' -> ^(GRP MIN)
	|	'max' -> ^(GRP MAX);
	
