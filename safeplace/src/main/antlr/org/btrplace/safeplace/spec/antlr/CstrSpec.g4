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

SUCH_AS :'.';
LACC:'{';
RACC:'}';
COMMA:',';
IN:':';
NOT_IN:'/:';
INCL:'<:';
NOT_INCL:'/<:';
PART: '<<:';
NOT_PART: '/<<:';
PLUS:'+';
MINUS:'-';
MULT:'*';
DIV:'/';
ALL:'!';
EXISTS:'?';
INT: '0' | '-'?[1..9][0..9]*;
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
//NOT:'~';
LBRACK: '[';
RBRACK: ']';
STRING: '"' (~('\\'|'"'))* '"';
BEGIN: '^';
DISCRETE: 'discrete';
CORE: 'core';
CONSTRAINT: 'constraint';
ID: [a-zA-Z_][a-zA-Z0-9_]*;

term: t1=term op=(INTER|UNION|PLUS|MINUS|MULT|DIV) t2=term         #termOp
    | LPARA term RPARA                              #protectedTerm
    | call                                         #termFunc
    | ID                               #idTerm    
    | ID  LBRACK term RBRACK                              #arrayTerm    
    | set                                    #setTerm
    | list      #listTerm
    | INT                                           #intTerm
    | STRING #stringTerm
    ;

set: LACC term SUCH_AS typedef (COMMA formula)? RACC #setInComprehension
   | LACC term (COMMA term)* RACC #setInExtension;

list: LBRACK term SUCH_AS typedef (COMMA formula)? RBRACK #listInComprehension
      | LBRACK term (COMMA term)* RBRACK #listInExtension;

comparison: t1=term op=(EQ | NOT_EQ| LT | LEQ | GT | GEQ | IN | NOT_IN | INCL | NOT_INCL | PART|NOT_PART) t2=term;

typedef: ID (COMMA ID)* op=(IN|INCL|NOT_IN|NOT_INCL|PART|NOT_PART) i2=term;
arg: ID op=(IN|INCL|PART) i2=term;
formula: LPARA formula RPARA   #protectedFormula
       |f1=formula op=(IMPLIES|OR|AND|IFF) f2=formula              #formulaOp
       |comparison #termComparison
       |ALL LPARA typedef RPARA formula #all
       |EXISTS LPARA typedef RPARA formula #exists
       |TRUE        #trueFormula
       |FALSE       #falseFormula
       |call        #cstrCall
       ;
       
call: BEGIN? ID LPARA term (COMMA term)* RPARA;

constraint: CORE? DISCRETE? CONSTRAINT ID LPARA (arg (COMMA arg)*)? RPARA DEF_CONTENT formula;

spec: constraint+;