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
MULT:'*';
DIV:'/';
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
STRING : '"""' ( '\\"\\"\\"' | . )*? '"""' ;

binder: type=(ALL|EXISTS) LPARA ID (COMMA ID)* op=(IN|NOT_IN|INCL|NOT_INCL) t=term RPARA DOT formula;

term: t1=term op=(INTER|UNION|PLUS|MINUS|MULT|DIV) t2=term         #termOp
    | LPARA term RPARA                              #protectedTerm
    | func                                          #termFunc
    | NEXT term                                     #nextTerm
    | ID                                            #idTerm
    | NAT                                           #natTerm
    | set                                           #setTerm
    ;

set: LACC (term (COMMA term)*)? RACC /* definition in extension */ #extensionSet
   | LACC binder* formula RACC /* definition in comprehension */ #comprehensionSet
   ;

typedef: i1=ID op=(IN|NOT_IN|INCL|NOT_INCL) i2=ID;
formula: LPARA formula RPARA   #protectedFormula
       |f1=formula op=(IMPLIES|OR|AND|IFF) f2=formula              #formulaOp
       |t1=term op=(EQ | NOT_EQ| LT | LEQ | GT | GEQ | IN | NOT_IN | INCL | NOT_INCL) t2=term  #termComparison
       |NOT formula     #not
       |binder  #binderFormula
       |TRUE        #trueFormula
       |FALSE       #falseFormula
       ;
       
func: ID LPARA term (COMMA term)* RPARA;

constraint: 'native'? 'cstr' ID LPARA (typedef (COMMA typedef)*)? RPARA DEF_CONTENT STRING? formula;
func_def: 'func' ID LPARA ID (COMMA ID)* RPARA ID; 
require: 'require' '"' ID '"';
spec: require * (func_def|constraint)+;        