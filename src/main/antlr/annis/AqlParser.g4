/*
* Copyright 2013 SFB 632.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

parser grammar AqlParser;

@header {package de.ids_mannheim.korap.query.parse.annis;}

options
{
language=Java;
tokenVocab=AqlLexer;
}


// the following start rule should end with an EOF: "(exprTop | regex) EOF". However, this causes 
// trouble with "qName operator textSpec" specifications at the end of the input (see variableExpr rule), while "TOK operator textSpec"
// works fine, for a  strange reason. Until this is further investigated, go without EOF
start           
: (exprTop | regex) EOF
;

regex : REGEX;

textSpec
:	START_TEXT_PLAIN END_TEXT_PLAIN # EmptyExactTextSpec
| START_TEXT_PLAIN content=TEXT_PLAIN END_TEXT_PLAIN # ExactTextSpec
| regex # RegexTextSpec
//| START_TEXT_REGEX END_TEXT_REGEX #EmptyRegexTextSpec
//| START_TEXT_REGEX content=TEXT_REGEX END_TEXT_REGEX # RegexTextSpec
//| SLASH content=TEXT_REGEX SLASH # RegexTextSpec
//| SLASH SLASH #EmptyRegexTextSpec
;

eqOperator
: EQ
| NEQ
;

rangeSpec
: min=DIGITS (COMMA max=DIGITS)?
;

qName
:	(foundry '/')? layer
;

edgeAnno
:	((foundry '/')? layer eqOperator)? textSpec
;

edgeSpec
: BRACKET_OPEN (edgeAnno WS*)+ BRACKET_CLOSE
;

refOrNode
: REF # ReferenceRef
| VAR_DEF? variableExpr # ReferenceNode
;

precedence
: PRECEDENCE (layer)? # DirectPrecedence
| PRECEDENCE (layer)? STAR # IndirectPrecedence
| PRECEDENCE (layer COMMA?)? rangeSpec #RangePrecedence
;

dominance
: DOMINANCE (qName)? (LEFT_CHILD | RIGHT_CHILD)? (anno=edgeSpec)? # DirectDominance
| DOMINANCE (qName)? STAR # IndirectDominance
| DOMINANCE (qName)? rangeSpec? # RangeDominance
;

pointing
: POINTING qName (anno=edgeSpec)? # DirectPointing
| POINTING qName (anno=edgeSpec)? STAR # IndirectPointing
| POINTING qName (anno=edgeSpec)? COMMA? rangeSpec # RangePointing
;

spanrelation
: IDENT_COV # IdenticalCoverage
|	LEFT_ALIGN # LeftAlign
|	RIGHT_ALIGN # RightAlign
|	INCLUSION # Inclusion
|	OVERLAP # Overlap
|	RIGHT_OVERLAP # RightOverlap
| LEFT_OVERLAP # LeftOverlap
;

commonparent_
: COMMON_PARENT (label)? # CommonParent
;

commonancestor_
: COMMON_PARENT (label)? STAR # CommonAncestor
;

identity
: IDENTITY
;

equalvalue
: EQ_VAL
;

notequalvalue
: NEQ
;

near
: NEAR (layer)? # DirectNear
| NEAR (layer)? STAR # IndirectNear
| NEAR (layer COMMA?)? rangeSpec #RangeNear
;

operator
: precedence
| spanrelation
| dominance
| pointing
| commonparent_
| commonancestor_
| identity
| equalvalue
| notequalvalue
| near
;

foundry
: ID;

layer
: ID;

label
: ID;

n_ary_linguistic_term
: refOrNode (operator refOrNode)+ # Relation
;

unary_linguistic_term
:	left=REF ROOT # RootTerm
|	left=REF ARITY EQ rangeSpec # ArityTerm
|	left=REF TOKEN_ARITY EQ rangeSpec # TokenArityTerm
;

variableExpr
: qName eqOperator txt=textSpec # AnnoEqTextExpr 
| TOK eqOperator txt=textSpec # TokTextExpr
| txt=textSpec # TextOnly // shortcut for tok="..."
| qName # AnnoOnlyExpr
| TOK # TokOnlyExpr
| NODE # NodeExpr
;

varDef
: VAR_DEF
;

expr
: varDef variableExpr # NamedVariableTermExpr
| variableExpr # VariableTermExpr
|	unary_linguistic_term # UnaryTermExpr
|	n_ary_linguistic_term # BinaryTermExpr
| META DOUBLECOLON id=qName op=EQ txt=textSpec # MetaTermExpr
;

andTopExpr
: ((expr (AND expr)*) | (BRACE_OPEN expr (AND expr)* BRACE_CLOSE)) # AndExpr
;


exprTop
: andTopExpr (OR andTopExpr)* # OrTop
;