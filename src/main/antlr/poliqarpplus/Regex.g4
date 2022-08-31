lexer grammar Regex;

fragment RE_ws       : [ \t];
fragment RE_focc     : '{' RE_ws* ( [0-9]* RE_ws* ',' RE_ws* [0-9]+ | [0-9]+ RE_ws* ','? ) RE_ws* '}';

/* Regular expressions and Regex queries */
fragment RE_symbol     : ~('*' | '?' | '+' | '{' | '}' | '[' | ']'
                     | '(' | ')' | '|' | '\\' | '"' | ':' | '\'' | '^' );
fragment RE_esc      : '\\' ('.' | '*' | '?' | '+' | '{' | '}' | '[' | ']'
                     | '(' | ')' | '|' | '\\' | '"' | ':' | '\'' | '^' );
fragment RE_char     : (RE_symbol | RE_esc );
fragment RE_alter    : ((RE_char | ('(' RE_expr ')') | RE_chgroup) '|' RE_expr )+;

fragment RE_chgroup  : '[' '^'? RE_char+ ']';
fragment RE_quant    : ('.' | RE_char | RE_chgroup | ( '(' RE_expr ')')) ('?' | '*' | '+' | RE_focc) '?'?;
fragment RE_group    : '(' RE_expr ')';
fragment RE_expr     : ('.' | RE_char | RE_alter | RE_chgroup | RE_quant | RE_group | '^' )+;
fragment RE_dquote   : ('"'|'„'|'“')  (RE_expr | '\'' | ':' )* ('"'|'“'|'”');