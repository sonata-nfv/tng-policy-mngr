grammar GPolicy;
/*
 * Parser Rules
 */
 
policy              : policyinfo policyrule+ EOF ;

policyinfo          : title ('parentpolicyengine:' title)*;

policyrule          : policytitle priority inertia? when whenpart+  NEWLINE then suggestion+;

priority            : 'priority' WHITESPACE? NUMBER NEWLINE ;

inertia            : 'inertia' WHITESPACE? NUMBER TIMEUNIT NEWLINE ;


when                : 'when:' NEWLINE ;

then                : 'then:' NEWLINE;

policytitle         : title ;

title               : WORD NEWLINE ;

message             : (color | mention | WORD | WHITESPACE)+ ;
 
whenpart           : expression ( ANDOR expression )* ;

expression          : (basicexpression | parexpression) ;

basicexpression     : event condition?;

condition           : OPERATOR threshold ('over window:time('NUMBER TIMEUNIT') aggr:'AGGREGATIONFUNTION)?;

parexpression       : OPEN_PAREN whenpart CLOSE_PAREN;




suggestion          : (event | pep ) NEWLINE?;

/*pep                 : action command message;*/
pep                 : action target;

event               : 'event' '{' field+ propagate? expires? '}' WHITESPACE* ;

/*Action Definition*/

action             : 'action' '{' 'id:' WORD ',type:' WORD (',value:' (WORD | NUMBER))? '}' WHITESPACE ;

/*Target Definition*/
target              : 'target' '{' 'gnsid:' WORD (',cid:' (WORD | NUMBER))? '}' WHITESPACE? ;



/*subject            : '"subject":' '"'WORD'"';
predicate           : ',"predicate":' '"'WORD'"' ;*/
field               : COMMA?'"'WORD'":' ('"'WORD'"' | NUMBER );

propagate           : ',"propagate":' BOOLEAN;
expires             : ',"expires":'   '"'NUMBER TIMEUNIT'"';

name                : WORD WHITESPACE;

threshold           : ( WORD | NUMBER ) WHITESPACE?;
 
command             : (SAYS | SHOUTS) ':' WHITESPACE ;
                                        
 
color               : '/' WORD '/' message '/';
 
mention             : '@' WORD ;


/*
 * Lexer Rules
 */
 
fragment A          : ('A'|'a') ;
fragment B          : ('B'|'b') ;
fragment E          : ('E'|'e') ;
fragment F          : ('F'|'f') ;
fragment Y          : ('Y'|'y') ;
fragment H          : ('H'|'h') ;
fragment O          : ('O'|'o') ;
fragment U          : ('U'|'u') ;
fragment L          : ('L'|'l') ;
fragment M          : ('M'|'m') ;
fragment D          : ('D'|'d') ;
fragment X          : ('X'|'x') ;
fragment V          : ('V'|'v') ;
fragment G          : ('G'|'g') ;
fragment I          : ('I'|'i') ;
fragment N          : ('N'|'n') ;
fragment S          : ('S'|'s') ;
fragment T          : ('T'|'t') ;
fragment R          : ('R'|'r') ;
 
fragment LOWERCASE  : [a-z] ;
fragment UPPERCASE  : [A-Z] ;

fragment DIGIT : [0-9] ;

NUMBER         : ('+'|'-')? DIGIT+ ([.,] DIGIT+)? ;
 
SAYS                : S A Y S ;

TIMEUNIT            : (S | M | H |D) ;

BOOLEAN             : (T R U E | F A L S E) ;

AGGREGATIONFUNTION  : (A V G | M I N | M A X) ;

 
SHOUTS              : S H O U T S;
 
WORD                : (LOWERCASE | UPPERCASE |'_')+ ;
 
WHITESPACE          : (' ' | '\t') ;
 
NEWLINE             : ('\r'? '\n' | '\r')+ ;


OPERATOR            : ('>' | '>=' | '<'| '<='| '=')  WHITESPACE;

ANDOR               : WHITESPACE? ('AND' | 'OR') WHITESPACE?;

OPEN_PAREN          : '(';
CLOSE_PAREN         : ')';

COMMA               : ',' ;





