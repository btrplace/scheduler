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
INT: '0' | '-'?[1..9][0..9]*;
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
LBRACK: '[';
RBRACK: ']';
MARSHAL : '"""' ( '\\"\\"\\"' | . )*? '"""' ;
STRING: '"' (~('\\'|'"'))* '"';
CURRENT: '$';

filter: LBRACK comparison (AND|OR comparison)* RBRACK;
term: t1=term op=(INTER|UNION|PLUS|MINUS|MULT|DIV) t2=term         #termOp
    | LPARA term RPARA                              #protectedTerm
    | call filter?                                         #termFunc
    | ID filter?                                   #idTerm
    | set filter?                                    #setTerm
    | INT                                           #intTerm
    | STRING #stringTerm
    ;

set: LACC term '.' typedef RACC #setInComprehension
   | LACC term (COMMA term)* RACC #setInExtension;

comparison: t1=term op=(EQ | NOT_EQ| LT | LEQ | GT | GEQ | IN | NOT_IN | INCL | NOT_INCL) t2=term;
typedef: ID (COMMA ID)* op=(IN|INCL|NOT_IN|NOT_INCL) i2=term filter?;
formula: LPARA formula RPARA   #protectedFormula
       |f1=formula op=(IMPLIES|OR|AND|IFF) f2=formula              #formulaOp
       |comparison #termComparison
       |NOT formula     #not
       |ALL LPARA typedef RPARA formula #all
       |EXISTS LPARA typedef RPARA formula #exists
       |TRUE        #trueFormula
       |FALSE       #falseFormula
       |call        #cstrCall
       ;
       
call: cur=CURRENT? ID LPARA term (COMMA term)* RPARA;

constraint: 'cstr' ID LPARA (typedef (COMMA typedef)*)? RPARA DEF_CONTENT MARSHAL formula;
spec: constraint+;        