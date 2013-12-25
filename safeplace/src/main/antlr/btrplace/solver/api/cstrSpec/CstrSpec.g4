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
AND:'&&';
OR:'||';
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
SUCH_AS: '.';
LBRACK: '[';
RBRACK: ']';
MARSHAL : '"""' ( '\\"\\"\\"' | . )*? '"""' ;
CURRENT: '$';

filter: LBRACK comparison (AND|OR comparison)* RBRACK;
term: t1=term op=(INTER|UNION|PLUS|MINUS|MULT|DIV) t2=term         #termOp
    | LPARA term RPARA                              #protectedTerm
    | func                                          #termFunc
    | ID filter?                                     #idTerm
    | set filter?                                   #setTerm
    | NAT                                           #natTerm
    ;

set: LACC (term (COMMA term)*)? RACC #extensionSet
   | LACC term SUCH_AS formula RACC #comprehensionSet
   ;

comparison: t1=term op=(EQ | NOT_EQ| LT | LEQ | GT | GEQ | IN | NOT_IN | INCL | NOT_INCL) t2=term;
forall: ALL LPARA ID (COMMA ID)* op=(IN|NOT_IN|INCL|NOT_INCL) t=term RPARA formula;
itExists: EXISTS LPARA ID (COMMA ID)* op=(IN|NOT_IN|INCL|NOT_INCL) t=term RPARA SUCH_AS formula;
typedef: i1=ID op=(IN|NOT_IN|INCL|NOT_INCL) i2=term;
formula: LPARA formula RPARA   #protectedFormula
       |f1=formula op=(IMPLIES|OR|AND|IFF) f2=formula              #formulaOp
       |comparison #termComparison
       |NOT formula     #not
       |forall #all       
       |itExists #exists
       |TRUE        #trueFormula
       |FALSE       #falseFormula
       ;
       
func: cur=CURRENT? ID LPARA term (COMMA term)* RPARA;

constraint: 'cstr' ID LPARA (typedef (COMMA typedef)*)? RPARA DEF_CONTENT MARSHAL formula;
spec: constraint+;        