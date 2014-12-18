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

parser grammar RawAqlPreParser;

@header {package de.ids_mannheim.korap.query.parse.annis;}

options
{
language=Java;
tokenVocab=AqlLexer;
}

start
: expr EOF
;

expr
: expr AND expr # AndExpr
| expr OR expr # OrExpr
| BRACE_OPEN expr BRACE_CLOSE # BraceExpr
| ~(AND | OR | BRACE_OPEN | BRACE_CLOSE)+ # LeafExpr
;