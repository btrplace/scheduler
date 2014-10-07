grammar ANTLRBtrplaceSL2;

options {
	language = Java;
	output = AST;
}


@parser::header {
package org.btrplace.btrpsl;
	
	import java.util.LinkedList;
	import java.util.List;
}

@lexer::header {
package org.btrplace.btrpsl;

import java.util.LinkedList;
}

@lexer::members {

  java.util.Queue<Token> tokens = new java.util.LinkedList<Token>();

  public void offer(int ttype, String ttext) {
    Token t = new CommonToken(ttype, ttext);
    t.setLine(getLine());
    t.setCharPositionInLine(getCharPositionInLine());
    emit(t);
  }
  
  @Override
  public void emit(Token t) {
    state.token = t;
    tokens.offer(t);
  }
  
  @Override
  public Token nextToken() {
    super.nextToken();
    Token t = tokens.isEmpty() ? getEOFToken() : tokens.poll();
    return t;
  }

  private ErrorReporter errReporter;

  public void setErrorReporter(ErrorReporter errReporter) {
    this.errReporter = errReporter;
  }

  public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
        if (errReporter != null) {
            int lineNo = e.line;
            int colNo = e.charPositionInLine;
            if (lineNo == 0) { //EOF ?
                for (int i = e.token.getTokenIndex(); i >= 0; i--) {
                    Token t = e.token;
                    if (t.getLine() != 0) {
                        lineNo = t.getLine();
                        colNo = t.getCharPositionInLine();
                        break;
                    }
                }
            }
            errReporter.append(lineNo, colNo, getErrorMessage(e, tokenNames));
        }  }


}

@parser::members {
  private ErrorReporter errReporter;

  public void setErrorReporter(ErrorReporter errReporter) {
    this.errReporter = errReporter;
  }

  public void displayRecognitionError(String[] tokenNames,
                                        RecognitionException e) {
        if (errReporter != null) {
            int lineNo = e.line;
            int colNo = e.charPositionInLine;
            if (lineNo == 0) { //EOF ?
                for (int i = e.token.getTokenIndex(); i >= 0; i--) {
                    Token t = input.get(i);
                    if (t.getLine() != 0) {
                        lineNo = t.getLine();
                        colNo = t.getCharPositionInLine();
                        break;
                    }
                }
            }
            errReporter.append(lineNo, colNo, getErrorMessage(e, tokenNames));
        }
  }
}
fragment Letter	:'a'..'z'|'A'..'Z';

fragment Hostname: (Letter|Digit) (('-'|'_')? (Letter|Digit))*;

NODE_NAME: '@'Hostname ('.' Hostname)*;
fragment LeftFQDN: '@' (Letter|Digit)((Letter|Digit|'_'|'-'|'.'))*;
LEFTFQDN: LeftFQDN '[';

STRING
    :  '"' ( ESC_SEQ | ~('\\'|'"') )* '"'
    ;

fragment ESC_SEQ:   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\');


IDENTIFIER: Letter 
	(Letter|Digit)*
	(('_' (Letter|Digit) LblTail*)
	|( '.' ('*' | (Letter PkgTail* '.*'?)))
	)?;

fragment LblTail: Letter
		 |Digit
		 |('_' (Letter|Digit));
fragment PkgTail: Letter
		  |Digit
		  |('.' Letter);

fragment LeftIdentifier:	Letter(Letter|Digit|'_')*;
LEFTIDENTIFIER: LeftIdentifier '[' ;

fragment Digit :'0'..'9';
fragment Digits :'0'..'9'*;
fragment Hex_Content: HEX_DIGIT+;		
fragment HEX_DIGIT: Digit|'a'..'f'|'A'..'F';	
	
fragment DECIMAL : ;
fragment OCTAL : ;
fragment HEXA: ;
fragment FLOAT: ;
fragment ONETONIME: '1'..'9';

INT_OR_FLOAT_OR_RANGE_OR_HEXA:
'0'
	(
		('x'|'X') Hex_Content {$type = HEXA;}
		|
		('0'..'7')+ {$type = OCTAL;}
		|
		'.'(
			'.' {$type = DECIMAL; offer(DECIMAL, "0"); offer(RANGE, "..");}
			|
			Digit+ {$type =FLOAT;}
		)	
		| {$type = DECIMAL;}		
	)
|	
ONETONIME d2=Digits 
	(
		'.'(
			d4=Digit+ {$type = FLOAT;}
			|	
			'.' {$type = DECIMAL;	
			StringBuilder buf = new StringBuilder($ONETONIME.text);						
			if ($d2 != null) {				
				buf.append($d2.text);
			}	
			offer(DECIMAL,buf.toString());
			 offer(RANGE,"..");}
		)
		|{$type = DECIMAL;}
	)	
;


//THE OPERATORS
EQUALS	:	'=';
PLUS	:	'+';
MINUS	:	'-';
DIV	:	'/';
REMAINDER	:	'%';
TIMES	:	'*';
AND	: 	'&&';
OR	: 	'||';
TYPE_DEFINITION: ':';
POWER: '^';
GT : '>';
GEQ: '>=';
LT: '<';
LEQ: '<=';
NOT: '!';
EQ: '==';
NEQ:'!=';
RANGE: '..';
PLUS_EQUALS: '+=';
MINUS_EQUALS: '-=';
TIMES_EQUALS: '*=';
DIV_EQUALS: '/=';
REMAINDER_EQUALS: '%=';
DISCRETE: '>>';

IF	: '__if';
EXPLODED_SET: '__{}';
CARDINALITY: '__#';
BLOCK	: '__BK';
FOR: '__for';
USE: '__use';
EXPORT: '__export';
NAMESPACE: '__namespace';
TEMPLATE_OPTION: '__<>';
VARIABLE:	'$' Letter (('_'|'.')? (Letter|Digit))*; 
LEFTVAR: '$' (Letter (Letter|Digit|'_'|'.')*) '[' ;

COMMENT
    :   '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    |   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
    ;

WS	:	('\n'|'\r'|'\t'|' ') {$channel=HIDDEN;};

fragment CName: Letter(Letter|Digit|'_')*;
CONSTRAINTIDENTIFIER: CName '(' {setText($CName.text);};
BLANK: '_';


cardinality: x='#' powerable -> ^(CARDINALITY[$x] powerable);

number: DECIMAL
	|OCTAL
	|HEXA
	|FLOAT;
	
set : explodedSet
	|rangeVar
	|rangeFqdn
	|rangeIdentifier;

explodedSet: x='{' (setContent (',' setContent)*)? '}' -> ^(EXPLODED_SET[$x] setContent*);

RIGHT:']' (('.'|'_'|'-')? (Letter|Digit))*;

ENUM_VAR: '___enumVar___';

rangeVar: LEFTVAR r1=braceContent (',' r2=braceContent)* RIGHT -> ^(ENUM_VAR LEFTVAR braceContent+ RIGHT);

ENUM_FQDN: '___enumfqdn___';
rangeFqdn:  LEFTFQDN r1=braceContent (',' r2=braceContent)* RIGHT -> ^(ENUM_FQDN LEFTFQDN braceContent+ RIGHT);
	  
ENUM_ID: '____enumId____';	  
rangeIdentifier:   LEFTIDENTIFIER r1=braceContent (',' r2=braceContent)* RIGHT -> ^(ENUM_ID LEFTIDENTIFIER braceContent+ RIGHT);

braceContent : range -> ^(RANGE range);

range  : term (RANGE! term)?;
			 
setContent: expression
	    | BLANK;
	    
term	:	'(' expression ')' -> expression
		|IDENTIFIER
		|NODE_NAME
		|number
		|VARIABLE		
		|STRING
		|set
;

powerable: term (POWER^ term)?;	   
negation: NOT^ powerable
	| cardinality
	| powerable;
unary: MINUS negation -> ^(MINUS negation)
       |negation -> negation;
mult: unary ((TIMES|DIV|REMAINDER)^ unary)*;

add: mult ((PLUS|MINUS)^ mult)*;

relation: add ((EQ | NEQ | LT | LEQ | GT | GEQ)^ add)*;
expression: relation ((AND | OR)^ relation)*;


forEachStatement: x='for' VARIABLE 'in' expression '{' bloc '}' -> ^(FOR[x] VARIABLE expression bloc);

constraintCallStatement: d='>>'? CONSTRAINTIDENTIFIER expression (',' expression)* ')' ';' -> ^(CONSTRAINTIDENTIFIER $d? expression +);
instruction:	definitionStatement		
		|forEachStatement
		|ifStatement
		|constraintCallStatement
		|exportStatement		
		;
		
definitionStatement: (VARIABLE | set| IDENTIFIER | NODE_NAME)
(
(EQUALS|PLUS_EQUALS|MINUS_EQUALS|TIMES_EQUALS|DIV_EQUALS|REMAINDER_EQUALS)^ expression
|TYPE_DEFINITION^ typeSpec
) ';'!;

typeSpec: IDENTIFIER^ ('<'! templateOption (','! templateOption)* '>'!)?;

templateOption: i1=IDENTIFIER ('=' (i2=number|i3=STRING))? -> ^(TEMPLATE_OPTION $i1 $i2? $i3?);

bloc: instruction* -> ^(BLOCK instruction*);

script_decl:	nameSpaceStatement useStatement* instruction* EOF!;

ifStatement: 'if' expression '{' i1=bloc '}' 
		('else' ('{' i2=bloc '}'| if2=ifStatement))? ->^(IF expression $i1 $i2? $if2?);		

//exportStatement: x='export' (VARIABLE|set) (',' (VARIABLE|set))* 'to' ('*'|IDENTIFIER (',' IDENTIFIER)*) ';' ->^(EXPORT[$x] VARIABLE* set* '*'? IDENTIFIER*);
exportStatement: x='export' (VARIABLE|set) (',' (VARIABLE|set))* 'to' ('*'|IDENTIFIER (',' IDENTIFIER)*) ';' ->^(EXPORT[$x] VARIABLE* set* '*'? IDENTIFIER*);

nameSpaceStatement: x='namespace' IDENTIFIER ';' -> ^(NAMESPACE[$x] IDENTIFIER);

useStatement: x='import' IDENTIFIER ';' -> ^(USE[$x] IDENTIFIER); 

	   