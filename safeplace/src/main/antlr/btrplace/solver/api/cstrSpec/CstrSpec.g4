grammar CstrSpec;

BLOCK_COMMENT
: '/*' .*? '*/' -> channel(HIDDEN)
;

LINE_COMMENT
: '//' ~[\r\n]* -> channel(HIDDEN)
;
WS
: [ \t\r\n\f]+
-> channel(HIDDEN)
;

LACC:'{';
RACC:'}';
COMMA:',';
DOT:'.';
IN:':';
NOT_IN:'/:';
INCL:'<:';  
NOT_INCL:'/<:';
PLUS:'+';
MINUS:'-';
ALL:'!';
EXISTS:'?';
NAT: '0' | '-'?[1..9][0..9]*;
ID: [a-zA-Z_][a-zA-Z0-9_]*;
INTER: '\\/';
UNION: '/\\';
AND:'&';
OR:'|';
EQ:'=';
NOT_EQ:'/=';
LPARA :'(';
RPARA:')';
DEF_CONTENT: '::=';
IMPLIES:'-->';
IFF: '<-->';
LT:'<';
LEQ:'<=';
GT:'>';
GEQ:'>=';
TRUE:'true';
FALSE:'false';
NOT:'~';
NEXT: 'next';

binder: (ALL|EXISTS) LPARA ID (COMMA ID)* (IN|NOT_IN|INCL|NOT_INCL) term RPARA DOT;

term: term (INTER|UNION|PLUS|MINUS) term
    | LPARA term RPARA
    | func
    | NEXT term
    | (ID|NAT)    
    | set
    ;

set: LACC (term (COMMA term)*)? RACC /* definition in extension */
   | LACC binder* formula RACC /* definition in comprehension */
   ;

typedef: ID (IN|NOT_IN|INCL|NOT_INCL) ID;
formula: 
       |formula (IMPLIES|OR|AND|IFF) formula
       |term (EQ | NOT_EQ| LT | LEQ | GT | GEQ | IN | NOT_IN | INCL | NOT_INCL) term        
       |NOT formula              
       |binder formula
       |(TRUE|FALSE)
       ;
       
func: ID LPARA term (COMMA term)* RPARA;

constraint: 'native'? 'cstr' ID LPARA (typedef (COMMA typedef)*)? RPARA DEF_CONTENT formula;
func_def: 'func' ID LPARA ID (COMMA ID)* RPARA ID; 
require: 'require' '"' ID '"';
spec: require * (func_def|constraint)+;        