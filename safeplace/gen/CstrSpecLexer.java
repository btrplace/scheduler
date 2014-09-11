/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

// Generated from /Users/fhermeni/Research/Code/Btrplace/solver/safeplace/src/main/antlr/btrplace/solver/api/cstrSpec/CstrSpec.g4 by ANTLR 4.x
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class CstrSpecLexer extends Lexer {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		BLOCK_COMMENT=1, LINE_COMMENT=2, WS=3, SUCH_AS=4, LACC=5, RACC=6, COMMA=7, 
		IN=8, NOT_IN=9, INCL=10, NOT_INCL=11, PLUS=12, MINUS=13, MULT=14, DIV=15, 
		ALL=16, EXISTS=17, INT=18, INTER=19, UNION=20, AND=21, OR=22, EQ=23, NOT_EQ=24, 
		LPARA=25, RPARA=26, DEF_CONTENT=27, IMPLIES=28, IFF=29, LT=30, LEQ=31, 
		GT=32, GEQ=33, TRUE=34, FALSE=35, NOT=36, LBRACK=37, RBRACK=38, STRING=39, 
		BEGIN=40, DISCRETE=41, CORE=42, CONSTRAINT=43, ID=44;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"'\\u0000'", "'\\u0001'", "'\\u0002'", "'\\u0003'", "'\\u0004'", "'\\u0005'", 
		"'\\u0006'", "'\\u0007'", "'\b'", "'\t'", "'\n'", "'\\u000B'", "'\f'", 
		"'\r'", "'\\u000E'", "'\\u000F'", "'\\u0010'", "'\\u0011'", "'\\u0012'", 
		"'\\u0013'", "'\\u0014'", "'\\u0015'", "'\\u0016'", "'\\u0017'", "'\\u0018'", 
		"'\\u0019'", "'\\u001A'", "'\\u001B'", "'\\u001C'", "'\\u001D'", "'\\u001E'", 
		"'\\u001F'", "' '", "'!'", "'\"'", "'#'", "'$'", "'%'", "'&'", "'''", 
		"'('", "')'", "'*'", "'+'", "','"
	};
	public static final String[] ruleNames = {
		"BLOCK_COMMENT", "LINE_COMMENT", "WS", "SUCH_AS", "LACC", "RACC", "COMMA", 
		"IN", "NOT_IN", "INCL", "NOT_INCL", "PLUS", "MINUS", "MULT", "DIV", "ALL", 
		"EXISTS", "INT", "INTER", "UNION", "AND", "OR", "EQ", "NOT_EQ", "LPARA", 
		"RPARA", "DEF_CONTENT", "IMPLIES", "IFF", "LT", "LEQ", "GT", "GEQ", "TRUE", 
		"FALSE", "NOT", "LBRACK", "RBRACK", "STRING", "BEGIN", "DISCRETE", "CORE", 
		"CONSTRAINT", "ID"
	};


	public CstrSpecLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "CstrSpec.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2.\u010e\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\3\2\3\2\3\2\3\2\7\2`\n\2\f\2\16\2c\13\2\3\2\3\2\3\2\3\2\3\2"+
		"\3\3\3\3\3\3\3\3\7\3n\n\3\f\3\16\3q\13\3\3\3\3\3\3\4\6\4v\n\4\r\4\16\4"+
		"w\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\n\3\13\3\13"+
		"\3\13\3\f\3\f\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21"+
		"\3\22\3\22\3\23\3\23\5\23\u009e\n\23\3\23\3\23\7\23\u00a2\n\23\f\23\16"+
		"\23\u00a5\13\23\5\23\u00a7\n\23\3\24\3\24\3\24\3\25\3\25\3\25\3\26\3\26"+
		"\3\27\3\27\3\30\3\30\3\31\3\31\3\31\3\32\3\32\3\33\3\33\3\34\3\34\3\34"+
		"\3\34\3\35\3\35\3\35\3\35\3\36\3\36\3\36\3\36\3\36\3\37\3\37\3 \3 \3 "+
		"\3!\3!\3\"\3\"\3\"\3#\3#\3#\3#\3#\3$\3$\3$\3$\3$\3$\3%\3%\3&\3&\3\'\3"+
		"\'\3(\3(\7(\u00e6\n(\f(\16(\u00e9\13(\3(\3(\3)\3)\3*\3*\3*\3*\3*\3*\3"+
		"*\3*\3*\3+\3+\3+\3+\3+\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3-\3-\7-\u010a"+
		"\n-\f-\16-\u010d\13-\3a\2.\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25"+
		"\f\27\r\31\16\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32"+
		"\63\33\65\34\67\359\36;\37= ?!A\"C#E$G%I&K\'M(O)Q*S+U,W-Y.\3\2\t\4\2\f"+
		"\f\17\17\5\2\13\f\16\17\"\"\5\2\60\60\63\63;;\5\2\60\60\62\62;;\4\2$$"+
		"^^\5\2C\\aac|\6\2\62;C\\aac|\u0115\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2"+
		"\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3"+
		"\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2"+
		"\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2"+
		"\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2"+
		"\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2"+
		"\2\2C\3\2\2\2\2E\3\2\2\2\2G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2\2"+
		"O\3\2\2\2\2Q\3\2\2\2\2S\3\2\2\2\2U\3\2\2\2\2W\3\2\2\2\2Y\3\2\2\2\3[\3"+
		"\2\2\2\5i\3\2\2\2\7u\3\2\2\2\t{\3\2\2\2\13}\3\2\2\2\r\177\3\2\2\2\17\u0081"+
		"\3\2\2\2\21\u0083\3\2\2\2\23\u0085\3\2\2\2\25\u0088\3\2\2\2\27\u008b\3"+
		"\2\2\2\31\u008f\3\2\2\2\33\u0091\3\2\2\2\35\u0093\3\2\2\2\37\u0095\3\2"+
		"\2\2!\u0097\3\2\2\2#\u0099\3\2\2\2%\u00a6\3\2\2\2\'\u00a8\3\2\2\2)\u00ab"+
		"\3\2\2\2+\u00ae\3\2\2\2-\u00b0\3\2\2\2/\u00b2\3\2\2\2\61\u00b4\3\2\2\2"+
		"\63\u00b7\3\2\2\2\65\u00b9\3\2\2\2\67\u00bb\3\2\2\29\u00bf\3\2\2\2;\u00c3"+
		"\3\2\2\2=\u00c8\3\2\2\2?\u00ca\3\2\2\2A\u00cd\3\2\2\2C\u00cf\3\2\2\2E"+
		"\u00d2\3\2\2\2G\u00d7\3\2\2\2I\u00dd\3\2\2\2K\u00df\3\2\2\2M\u00e1\3\2"+
		"\2\2O\u00e3\3\2\2\2Q\u00ec\3\2\2\2S\u00ee\3\2\2\2U\u00f7\3\2\2\2W\u00fc"+
		"\3\2\2\2Y\u0107\3\2\2\2[\\\7\61\2\2\\]\7,\2\2]a\3\2\2\2^`\13\2\2\2_^\3"+
		"\2\2\2`c\3\2\2\2ab\3\2\2\2a_\3\2\2\2bd\3\2\2\2ca\3\2\2\2de\7,\2\2ef\7"+
		"\61\2\2fg\3\2\2\2gh\b\2\2\2h\4\3\2\2\2ij\7\61\2\2jk\7\61\2\2ko\3\2\2\2"+
		"ln\n\2\2\2ml\3\2\2\2nq\3\2\2\2om\3\2\2\2op\3\2\2\2pr\3\2\2\2qo\3\2\2\2"+
		"rs\b\3\2\2s\6\3\2\2\2tv\t\3\2\2ut\3\2\2\2vw\3\2\2\2wu\3\2\2\2wx\3\2\2"+
		"\2xy\3\2\2\2yz\b\4\2\2z\b\3\2\2\2{|\7\60\2\2|\n\3\2\2\2}~\7}\2\2~\f\3"+
		"\2\2\2\177\u0080\7\177\2\2\u0080\16\3\2\2\2\u0081\u0082\7.\2\2\u0082\20"+
		"\3\2\2\2\u0083\u0084\7<\2\2\u0084\22\3\2\2\2\u0085\u0086\7\61\2\2\u0086"+
		"\u0087\7<\2\2\u0087\24\3\2\2\2\u0088\u0089\7>\2\2\u0089\u008a\7<\2\2\u008a"+
		"\26\3\2\2\2\u008b\u008c\7\61\2\2\u008c\u008d\7>\2\2\u008d\u008e\7<\2\2"+
		"\u008e\30\3\2\2\2\u008f\u0090\7-\2\2\u0090\32\3\2\2\2\u0091\u0092\7/\2"+
		"\2\u0092\34\3\2\2\2\u0093\u0094\7,\2\2\u0094\36\3\2\2\2\u0095\u0096\7"+
		"\61\2\2\u0096 \3\2\2\2\u0097\u0098\7#\2\2\u0098\"\3\2\2\2\u0099\u009a"+
		"\7A\2\2\u009a$\3\2\2\2\u009b\u00a7\7\62\2\2\u009c\u009e\7/\2\2\u009d\u009c"+
		"\3\2\2\2\u009d\u009e\3\2\2\2\u009e\u009f\3\2\2\2\u009f\u00a3\t\4\2\2\u00a0"+
		"\u00a2\t\5\2\2\u00a1\u00a0\3\2\2\2\u00a2\u00a5\3\2\2\2\u00a3\u00a1\3\2"+
		"\2\2\u00a3\u00a4\3\2\2\2\u00a4\u00a7\3\2\2\2\u00a5\u00a3\3\2\2\2\u00a6"+
		"\u009b\3\2\2\2\u00a6\u009d\3\2\2\2\u00a7&\3\2\2\2\u00a8\u00a9\7^\2\2\u00a9"+
		"\u00aa\7\61\2\2\u00aa(\3\2\2\2\u00ab\u00ac\7\61\2\2\u00ac\u00ad\7^\2\2"+
		"\u00ad*\3\2\2\2\u00ae\u00af\7(\2\2\u00af,\3\2\2\2\u00b0\u00b1\7~\2\2\u00b1"+
		".\3\2\2\2\u00b2\u00b3\7?\2\2\u00b3\60\3\2\2\2\u00b4\u00b5\7\61\2\2\u00b5"+
		"\u00b6\7?\2\2\u00b6\62\3\2\2\2\u00b7\u00b8\7*\2\2\u00b8\64\3\2\2\2\u00b9"+
		"\u00ba\7+\2\2\u00ba\66\3\2\2\2\u00bb\u00bc\7<\2\2\u00bc\u00bd\7<\2\2\u00bd"+
		"\u00be\7?\2\2\u00be8\3\2\2\2\u00bf\u00c0\7/\2\2\u00c0\u00c1\7/\2\2\u00c1"+
		"\u00c2\7@\2\2\u00c2:\3\2\2\2\u00c3\u00c4\7>\2\2\u00c4\u00c5\7/\2\2\u00c5"+
		"\u00c6\7/\2\2\u00c6\u00c7\7@\2\2\u00c7<\3\2\2\2\u00c8\u00c9\7>\2\2\u00c9"+
		">\3\2\2\2\u00ca\u00cb\7>\2\2\u00cb\u00cc\7?\2\2\u00cc@\3\2\2\2\u00cd\u00ce"+
		"\7@\2\2\u00ceB\3\2\2\2\u00cf\u00d0\7@\2\2\u00d0\u00d1\7?\2\2\u00d1D\3"+
		"\2\2\2\u00d2\u00d3\7v\2\2\u00d3\u00d4\7t\2\2\u00d4\u00d5\7w\2\2\u00d5"+
		"\u00d6\7g\2\2\u00d6F\3\2\2\2\u00d7\u00d8\7h\2\2\u00d8\u00d9\7c\2\2\u00d9"+
		"\u00da\7n\2\2\u00da\u00db\7u\2\2\u00db\u00dc\7g\2\2\u00dcH\3\2\2\2\u00dd"+
		"\u00de\7\u0080\2\2\u00deJ\3\2\2\2\u00df\u00e0\7]\2\2\u00e0L\3\2\2\2\u00e1"+
		"\u00e2\7_\2\2\u00e2N\3\2\2\2\u00e3\u00e7\7$\2\2\u00e4\u00e6\n\6\2\2\u00e5"+
		"\u00e4\3\2\2\2\u00e6\u00e9\3\2\2\2\u00e7\u00e5\3\2\2\2\u00e7\u00e8\3\2"+
		"\2\2\u00e8\u00ea\3\2\2\2\u00e9\u00e7\3\2\2\2\u00ea\u00eb\7$\2\2\u00eb"+
		"P\3\2\2\2\u00ec\u00ed\7`\2\2\u00edR\3\2\2\2\u00ee\u00ef\7f\2\2\u00ef\u00f0"+
		"\7k\2\2\u00f0\u00f1\7u\2\2\u00f1\u00f2\7e\2\2\u00f2\u00f3\7t\2\2\u00f3"+
		"\u00f4\7g\2\2\u00f4\u00f5\7v\2\2\u00f5\u00f6\7g\2\2\u00f6T\3\2\2\2\u00f7"+
		"\u00f8\7e\2\2\u00f8\u00f9\7q\2\2\u00f9\u00fa\7t\2\2\u00fa\u00fb\7g\2\2"+
		"\u00fbV\3\2\2\2\u00fc\u00fd\7e\2\2\u00fd\u00fe\7q\2\2\u00fe\u00ff\7p\2"+
		"\2\u00ff\u0100\7u\2\2\u0100\u0101\7v\2\2\u0101\u0102\7t\2\2\u0102\u0103"+
		"\7c\2\2\u0103\u0104\7k\2\2\u0104\u0105\7p\2\2\u0105\u0106\7v\2\2\u0106"+
		"X\3\2\2\2\u0107\u010b\t\7\2\2\u0108\u010a\t\b\2\2\u0109\u0108\3\2\2\2"+
		"\u010a\u010d\3\2\2\2\u010b\u0109\3\2\2\2\u010b\u010c\3\2\2\2\u010cZ\3"+
		"\2\2\2\u010d\u010b\3\2\2\2\13\2aow\u009d\u00a3\u00a6\u00e7\u010b\3\2\3"+
		"\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}