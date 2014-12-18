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
	
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//
// 						PROX-Parser
//
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *


opPROX	:	proxTyp proxDist (',' proxDist)* (',' proxGroup)? 
		
		-> ^(PROX_OPTS {$proxTyp.tree} ^(DIST_LIST proxDist+) {$proxGroup.tree});
	
proxTyp	:	  '/' -> ^(TYP PROX)	// klassischer Abstand.
		| '%' -> ^(TYP EXCL);	// ausschließender Abstand.

// proxDist: e.g. +5w or -s0 or /w2:4 etc.
// kein proxDirection? hier, weil der Default erst innerhalb von Regel proxDirection erzeugt werden kann.
proxDist:	proxDirection (v1=proxDistValue m1=proxMeasure | m2=proxMeasure v2=proxDistValue)

		-> {$v1.tree != null}? ^(DIST {$proxDirection.tree} {$v1.tree} {$m1.tree})
		-> 		       ^(DIST {$proxDirection.tree} {$v2.tree} {$m2.tree});

proxDirection
	:	(p='+'|m='-')?	-> {$p != null}? ^(DIR PLUS)
				-> {$m != null}? ^(DIR MINUS)
				->               ^(DIR BOTH) ;
/*
proxDistValue	// proxDistMin ( ':' proxDistMax)? ;
	:	(m1=proxDistMin -> ^(DIST_RANGE VAL0 $m1)) (':' m2=proxDistMax -> ^(DIST_RANGE $m1 $m2))? ;
*/
proxDistValue	// proxDistMin ( ':' proxDistMax)? ;
	:	(m1=proxDistMin ) (':' m2=proxDistMax)? 
	
		-> {$m2.text != null}? ^(RANGE $m1  $m2)
		->		       ^(RANGE VAL0 $m1);
		
proxMeasure
	:	(m='w'|m='s'|m='p'|m='t') -> ^(MEAS $m);

proxDistMin
	:	DISTVALUE;
	
proxDistMax
	:	DISTVALUE;
	
proxGroup
	:	'min' -> ^(GRP MIN)
	|	'max' -> ^(GRP MAX);
	
