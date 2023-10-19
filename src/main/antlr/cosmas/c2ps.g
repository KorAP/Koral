// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//												
// 	COSMAS II zeilenorientierten Suchanfragesprache (C2 plain syntax)	
// 	globale Grammatik (ruft lokale c2ps_x.g Grammatiken auf).			
//	17.12.12/FB										
//      v-0.6										
// TODO:											
// - se1: Einsetzen des Default-Operators in den kumulierten AST.		
//
//  v0.7 - 25.07.23/FB
//    - added: #REG(x)
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

grammar c2ps;

options { output=AST; backtrack=true; k=5;}
// tokens that will appear as node names in the resulting AST:
tokens  {C2PQ; OPBED; OPTS; OPBEG; OPEND; OPNHIT; OPALL; OPLEM; OPPROX;
	 ARG1; ARG2; 
	 OPWF; OPLEM; OPANNOT;
	 OPLABEL;
	 OPIN; OPOV;
	 OPAND;
	 OPOR;
	 OPNOT;
	 OPEXPR1;
	 OPMORPH; OPELEM;
	 OPREG;
	}

@header {package de.ids_mannheim.korap.query.parse.cosmas;}
@lexer::header {package de.ids_mannheim.korap.query.parse.cosmas;}

@lexer::members {
    private IErrorReporter errorReporter = null;
    
    public void setErrorReporter(IErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
    }
    
    public void emitErrorMessage(String msg) {
        errorReporter.reportError(msg);
    }
}

@parser::members {
    private IErrorReporter errorReporter = null;
    
    public void setErrorReporter(IErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
    }
    
    public void emitErrorMessage(String msg) {
        errorReporter.reportError(msg);
    }
}

// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//
// 						Lexer
//
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

WS	:	(' '|'\r'|'\n')+ {skip();};

// Suchoperator PROX:
// ------------------

fragment DISTVALUE
	:	 ('0' .. '9')+ (':' ('0'..'9')+)? ;
		
fragment DIST
	:	('+'|'-')? (DISTVALUE ('w'|'s'|'p'|'t') | ('w'|'s'|'p'|'t') DISTVALUE);
	
fragment GROUP
	:	('min' | 'max');

OP_PROX	:	('/' | '%') DIST (',' DIST)* (',' GROUP)? ;

OP_IN	:	'#IN' | '#IN(' OP_IN_OPTS? ')' ; 

OP_OV	:	'#OV' | '#OV(' OP_OV_OPTS? ')' ;

// #REG(abc['"]) or #REG('abc\'s') or #REG("abc\"s"):

OP_REG	: '#REG(' ' '* '\'' ('\\\''|~'\'')+  '\'' (' ')* ')'	
			| 
		  '#REG(' ' '* '"' ('\\"'|~'"')+ '"' (' ')* ')'
		  	|
		  '#REG(' ' '* ~('\''|'"'|' ') (~(')'))* ')';

// EAVEXP wird hier eingesetzt fÃ¼r eine beliebige Sequenz von Zeichen bis zu ')'.
fragment OP_IN_OPTS
	:	EAVEXPR ;

// EAVEXP wird hier eingesetzt fÃ¼r eine beliebige Sequenz von Zeichen bis zu ')'.	
fragment OP_OV_OPTS
	:	EAVEXPR ;

// OP_BED: #BED( searchExp , Bedingung )
// OP_BED_END = ", Bedingung )" 
// ungelÃ¶st: #BED(Jimi Hendrix, sa) -> Komma wird "Hendrix," zugeschlagen!
// Umgehung: Blank vor dem Komma: #BED(Jimi Hendrix , sa) -> OK.

OP_BED_END
	:	',' ~(')')+ ')' ; 
	
// OP1: Operator with single argument:
// (funktioniert nicht: fragment OP1 : OP1BEG | OP1END ...;)

//OP1	:	'#BEG(' | '#END(' | '#ALL(' | '#NHIT(' ;	

// Labels als Keywords fÃ¼r Suchbegriffe mit besonderer Bedeutung (Ãberschriften, etc.),
// muss VOR SEARCHWORD1/2 deklariert werden.

SEARCHLABEL
	:	('<s>' | '<p>' | '<Ã¼>' | '<Ã¼d>' | '<Ã¼h>' | '<Ã¼u>' | '<Ã¼z>' | '<Ã¼r>');

// Search Word: 
// spezialzeichen werden in "..." versteckt.
// SEARCHWORD1: single or multiple words not enclosed in "...".
// SEARCHWORD2: single or multiple words enclosed in "...".
SEARCHLEMMA
	:	'&' SEARCHWORD1 ; // rewrite rules funktionieren im lexer nicht: -> ^(OPLEM $SEARCHWORD1.text); 

// SEARCHWORD2: schluckt Blanks. Diese mÃ¼ssen nachtrÃ¤glich als Wortdelimiter erkannt werden.
SEARCHWORD1
	:	~('"' | ' ' | '#' | ')' | '(' )+ ;

SEARCHWORD2
	:	'"' (~('"') | '\\"')+ '"' ;

// Annotationsoperator #ELEM( EAVEXPR ).
// EAVEXPR = Element Attribut Value Expression.
// alle Spezialzeichen vor dem Blank ausgeschlossen.
// e.g. #ELEM(ANA='N pl'); #ELEM(HEAD, TYPE='DACHUEBERSCHRIFT');
// e.g. #ELEM( ANA='N()' LEM='L\'Ã©tÃ©');

fragment EAVEXPR
	:	( ~( '(' | ')' | '\'' | ('\u0000'..'\u001F')) | ('\'' (~('\'' | '\\') | '\\' '\'')* '\'') )+ ;
	
fragment WORD
	:	~('\t' | ' ' | '/' | '*' | '?' | '+' | '{' | '}' | '[' | ']'
                    | '(' | ')' | '|' | '"' | ',' | ':' | '\'' | '\\' | '!' | '=' | '~' | '&' | '^' | '<' | '>' )+;

fragment FOCC       : '{'( ('0'..'9')*  ',' ('0'..'9')+ | ('0'..'9')+  ','? )  '}';

/* Regular expressions and Regex queries */
fragment RE_char     : ~('*' | '?' | '+' | '{' | '}' | '[' | ']'
                     | '(' | ')' | '|' | '"' | ':' | '\'' | '\\');
fragment RE_alter    : ( ( RE_char | RE_chgroup ) '|' RE_expr )+;
fragment RE_chgroup  : '[' RE_char+ ']';
fragment RE_chars    : (RE_char | RE_chgroup | ( '(' RE_expr ')')) (('+'|'*'|FOCC)'?'? |'?')? ;
fragment RE_expr   : (RE_alter | RE_chars)+;
fragment REGEX       : '"'  (RE_expr | '\'' | ':' )* '"';

// "#ELEM()" nur fuer Fehlerbehandlung, ansonsten sinnlose Anfrage.
OP_ELEM	:	'#ELEM(' EAVEXPR ')' | '#ELEM(' ')';

fragment MORPHEXPR
	: (WORD|REGEX)
	| WORD ':' (WORD|REGEX)
	| WORD '!'? '=' (WORD|REGEX) 
	| WORD '!'? '=' WORD ':' (WORD|REGEX)
	| WORD '/' WORD '!'? '=' (WORD|REGEX)
	| WORD '/' WORD '!'? '=' WORD ':' (WORD|REGEX)
	;

OP_MORPH:	'MORPH(' 
				MORPHEXPR (' '* '&' ' '* MORPHEXPR)* ' '* 
			')' ;

// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//
// 						Parser
//
// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

c2ps_query 
	:	searchExpr EOF -> ^(C2PQ searchExpr);

/* this rule is OK.
searchExpr
	:	(op1 | searchWord | searchLemma | searchAnnot | searchLabel | '('! searchExpr ')'!)+ (op2^ searchExpr)? ;
*/
// trying to modify the rule above for generating arg1 and arg2 in the resulting AST more easily.
// notes: se1+=searchExpr1 is of type List. -> $se1+ (not {$se1+} !) is the AST of the list.
searchExpr
	:	(se1+=searchExpr1)+ (op2 se2=searchExpr)? 
	
		-> {$op2.tree != null}? ^({$op2.tree} ^(ARG1 $se1+) ^(ARG2 {$se2.tree}))
		-> $se1+ ;

searchExpr1
	:	op1 		   -> {$op1.tree}
	| 	searchWord 	   -> {$searchWord.tree}
	| 	searchLemma 	   -> {$searchLemma.tree}
	| 	searchAnnot 	   -> {$searchAnnot.tree}
	| 	searchLabel        -> {$searchLabel.tree}
	| 	'(' searchExpr ')' -> {$searchExpr.tree};

// Suchbegriff = Suchwort in Hochkommata (word2) oder ohne (word1):
// aufgegeben: word1+ | '"' word1+ '"' ; 

searchWord
	:	word1
	|	word2;

word1	:	SEARCHWORD1 -> {c2ps_opWF.check($SEARCHWORD1.text, false, false, $SEARCHWORD1.index)} ; 

word2	:	SEARCHWORD2 -> {c2ps_opWF.check($SEARCHWORD2.text, true, false, $SEARCHWORD2.index)} ;
	
// Suchbegriff = Lemma:
searchLemma
	:	SEARCHLEMMA -> {c2ps_opWF.check($SEARCHLEMMA.text, false, true, $SEARCHLEMMA.index)} ; 

// Suchbegriff = Annotationsoperator:
// (damit Lexer den richtige Token erzeugt, muss OP_ELEM den gesamten
// Annot-Ausdruck als 1 Token erkennen).
searchAnnot
	:	OP_ELEM  
		-> ^({c2ps_opELEM.check($OP_ELEM.text,$OP_ELEM.index)})
	| 	OP_MORPH 
		-> ^(OPMORPH ^({new CommonTree(new CommonToken(OPMORPH, c2ps_opAnnot.strip($OP_MORPH.text)))}));

// searchLabel: <s>, <p>, <Ã¼> etc.

searchLabel
	:	SEARCHLABEL -> ^(OPLABEL SEARCHLABEL); 
	
// Suchoperatoren:
// ---------------

// OP2: Suchoperatoren mit 2 Argumenten:
// -------------------------------------

// Der von op2 zurÃ¼ckgelieferte AST ist automatisch derjenige vom geparsten Operator.

op2	:	(opPROX | opIN | opOV | opAND | opOR | opNOT) ;
		
// AST with Options for opPROX is returned by c2ps_opPROX.check():
opPROX	:	OP_PROX -> ^(OPPROX {c2ps_opPROX.check($OP_PROX.text, $OP_PROX.index)} );

opIN	: 	OP_IN -> {c2ps_opIN.check($OP_IN.text, $OP_IN.index)};

opOV	:	OP_OV -> {c2ps_opOV.check($OP_OV.text, $OP_OV.index)};

opAND	:	('und' | 'UND' | 'and' | 'AND')     -> ^(OPAND);

opOR	:	('oder' | 'ODER' | 'or' | 'OR')     -> ^(OPOR);

opNOT	:	('nicht' | 'NICHT' | 'not' | 'NOT') -> ^(OPNOT);

// OP1: Suchoperatoren mit 1 Argument:
// -----------------------------------

op1	:	opBEG | opEND | opNHIT | opALL | opBED | opREG; 

// #BED(serchExpr, B).
// B muss nachtrÃ¤glich in einer lokalen Grammatik Ã¼berprÃ¼ft werden.

opBED	:	( '#COND(' | '#BED(' ) searchExpr opBEDEnd -> ^(OPBED searchExpr ^(OPTS {$opBEDEnd.tree})) ;

// c2ps_opBED.check() returns an AST that is returned by rule opBEDEnd.
// for this action inside a rewrite rule, no ';' behind the function call.
opBEDEnd:	OP_BED_END -> {c2ps_opBED.check($OP_BED_END.text, $OP_BED_END.index) };

opBEG	:	( '#BEG(' | '#LINKS(' ) searchExpr ')'  -> ^(OPBEG searchExpr) ;

opEND	:	( '#END(' | '#RECHTS(' ) searchExpr ')'  -> ^(OPEND searchExpr) ;

opNHIT	:	( '#NHIT(' | '#INKLUSIVE(' ) searchExpr ')' -> ^(OPNHIT searchExpr) ;

opALL	:	( '#ALL(' | '#EXKLUSIVE(' ) searchExpr ')'  -> ^(OPALL searchExpr) ;

opREG	:	OP_REG -> ^(OPREG {c2ps_opREG.encode($OP_REG.text, OPREG)}) ;

