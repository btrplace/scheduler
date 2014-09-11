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
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class CstrSpecParser extends Parser {
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
	public static final String[] tokenNames = {
		"<INVALID>", "BLOCK_COMMENT", "LINE_COMMENT", "WS", "'.'", "'{'", "'}'", 
		"','", "':'", "'/:'", "'<:'", "'/<:'", "'+'", "'-'", "'*'", "'/'", "'!'", 
		"'?'", "INT", "'\\/'", "'/\\'", "'&'", "'|'", "'='", "'/='", "'('", "')'", 
		"'::='", "'-->'", "'<-->'", "'<'", "'<='", "'>'", "'>='", "'true'", "'false'", 
		"'~'", "'['", "']'", "STRING", "'^'", "'discrete'", "'core'", "'constraint'", 
		"ID"
	};
	public static final int
		RULE_term = 0, RULE_set = 1, RULE_list = 2, RULE_comparison = 3, RULE_typedef = 4, 
		RULE_formula = 5, RULE_call = 6, RULE_constraint = 7, RULE_spec = 8;
	public static final String[] ruleNames = {
		"term", "set", "list", "comparison", "typedef", "formula", "call", "constraint", 
		"spec"
	};

	@Override
	public String getGrammarFileName() { return "CstrSpec.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public CstrSpecParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class TermContext extends ParserRuleContext {
		public TermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_term; }
	 
		public TermContext() { }
		public void copyFrom(TermContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class IntTermContext extends TermContext {
		public TerminalNode INT() { return getToken(CstrSpecParser.INT, 0); }
		public IntTermContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).enterIntTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).exitIntTerm(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CstrSpecVisitor ) return ((CstrSpecVisitor<? extends T>)visitor).visitIntTerm(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TermFuncContext extends TermContext {
		public CallContext call() {
			return getRuleContext(CallContext.class,0);
		}
		public TermFuncContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).enterTermFunc(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).exitTermFunc(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CstrSpecVisitor ) return ((CstrSpecVisitor<? extends T>)visitor).visitTermFunc(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class IdTermContext extends TermContext {
		public TerminalNode ID() { return getToken(CstrSpecParser.ID, 0); }
		public IdTermContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).enterIdTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).exitIdTerm(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CstrSpecVisitor ) return ((CstrSpecVisitor<? extends T>)visitor).visitIdTerm(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ArrayTermContext extends TermContext {
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public TerminalNode RBRACK() { return getToken(CstrSpecParser.RBRACK, 0); }
		public TerminalNode ID() { return getToken(CstrSpecParser.ID, 0); }
		public TerminalNode LBRACK() { return getToken(CstrSpecParser.LBRACK, 0); }
		public ArrayTermContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).enterArrayTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).exitArrayTerm(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CstrSpecVisitor ) return ((CstrSpecVisitor<? extends T>)visitor).visitArrayTerm(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetTermContext extends TermContext {
		public SetContext set() {
			return getRuleContext(SetContext.class,0);
		}
		public SetTermContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).enterSetTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).exitSetTerm(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CstrSpecVisitor ) return ((CstrSpecVisitor<? extends T>)visitor).visitSetTerm(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class StringTermContext extends TermContext {
		public TerminalNode STRING() { return getToken(CstrSpecParser.STRING, 0); }
		public StringTermContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).enterStringTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).exitStringTerm(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CstrSpecVisitor ) return ((CstrSpecVisitor<? extends T>)visitor).visitStringTerm(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ProtectedTermContext extends TermContext {
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public TerminalNode LPARA() { return getToken(CstrSpecParser.LPARA, 0); }
		public TerminalNode RPARA() { return getToken(CstrSpecParser.RPARA, 0); }
		public ProtectedTermContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).enterProtectedTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).exitProtectedTerm(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CstrSpecVisitor ) return ((CstrSpecVisitor<? extends T>)visitor).visitProtectedTerm(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ListTermContext extends TermContext {
		public ListContext list() {
			return getRuleContext(ListContext.class,0);
		}
		public ListTermContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).enterListTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).exitListTerm(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CstrSpecVisitor ) return ((CstrSpecVisitor<? extends T>)visitor).visitListTerm(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TermOpContext extends TermContext {
		public TermContext t1;
		public Token op;
		public TermContext t2;
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public TerminalNode UNION() { return getToken(CstrSpecParser.UNION, 0); }
		public TerminalNode DIV() { return getToken(CstrSpecParser.DIV, 0); }
		public TerminalNode MULT() { return getToken(CstrSpecParser.MULT, 0); }
		public TerminalNode MINUS() { return getToken(CstrSpecParser.MINUS, 0); }
		public TerminalNode INTER() { return getToken(CstrSpecParser.INTER, 0); }
		public TerminalNode PLUS() { return getToken(CstrSpecParser.PLUS, 0); }
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public TermOpContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).enterTermOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).exitTermOp(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CstrSpecVisitor ) return ((CstrSpecVisitor<? extends T>)visitor).visitTermOp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		return term(0);
	}

	private TermContext term(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		TermContext _localctx = new TermContext(_ctx, _parentState);
		TermContext _prevctx = _localctx;
		int _startState = 0;
		enterRecursionRule(_localctx, 0, RULE_term, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(34);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				{
				_localctx = new ProtectedTermContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(19); match(LPARA);
				setState(20); term(0);
				setState(21); match(RPARA);
				}
				break;
			case 2:
				{
				_localctx = new TermFuncContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(23); call();
				}
				break;
			case 3:
				{
				_localctx = new IdTermContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(24); match(ID);
				}
				break;
			case 4:
				{
				_localctx = new ArrayTermContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(25); match(ID);
				setState(26); match(LBRACK);
				setState(27); term(0);
				setState(28); match(RBRACK);
				}
				break;
			case 5:
				{
				_localctx = new SetTermContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(30); set();
				}
				break;
			case 6:
				{
				_localctx = new ListTermContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(31); list();
				}
				break;
			case 7:
				{
				_localctx = new IntTermContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(32); match(INT);
				}
				break;
			case 8:
				{
				_localctx = new StringTermContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(33); match(STRING);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(41);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
			while ( _alt!=2 && _alt!=ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new TermOpContext(new TermContext(_parentctx, _parentState));
					((TermOpContext)_localctx).t1 = _prevctx;
					pushNewRecursionContext(_localctx, _startState, RULE_term);
					setState(36);
					if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
					setState(37);
					((TermOpContext)_localctx).op = _input.LT(1);
					_la = _input.LA(1);
					if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << PLUS) | (1L << MINUS) | (1L << MULT) | (1L << DIV) | (1L << INTER) | (1L << UNION))) != 0)) ) {
						((TermOpContext)_localctx).op = (Token)_errHandler.recoverInline(this);
					}
					consume();
					setState(38); ((TermOpContext)_localctx).t2 = term(10);
					}
					} 
				}
				setState(43);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class SetContext extends ParserRuleContext {
		public SetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_set; }
	 
		public SetContext() { }
		public void copyFrom(SetContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class SetInExtensionContext extends SetContext {
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public List<TerminalNode> COMMA() { return getTokens(CstrSpecParser.COMMA); }
		public TerminalNode LACC() { return getToken(CstrSpecParser.LACC, 0); }
		public TerminalNode RACC() { return getToken(CstrSpecParser.RACC, 0); }
		public TerminalNode COMMA(int i) {
			return getToken(CstrSpecParser.COMMA, i);
		}
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public SetInExtensionContext(SetContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).enterSetInExtension(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).exitSetInExtension(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CstrSpecVisitor ) return ((CstrSpecVisitor<? extends T>)visitor).visitSetInExtension(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SetInComprehensionContext extends SetContext {
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public FormulaContext formula() {
			return getRuleContext(FormulaContext.class,0);
		}
		public TerminalNode SUCH_AS() { return getToken(CstrSpecParser.SUCH_AS, 0); }
		public TerminalNode COMMA() { return getToken(CstrSpecParser.COMMA, 0); }
		public TypedefContext typedef() {
			return getRuleContext(TypedefContext.class,0);
		}
		public TerminalNode LACC() { return getToken(CstrSpecParser.LACC, 0); }
		public TerminalNode RACC() { return getToken(CstrSpecParser.RACC, 0); }
		public SetInComprehensionContext(SetContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).enterSetInComprehension(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).exitSetInComprehension(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CstrSpecVisitor ) return ((CstrSpecVisitor<? extends T>)visitor).visitSetInComprehension(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SetContext set() throws RecognitionException {
		SetContext _localctx = new SetContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_set);
		int _la;
		try {
			setState(65);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				_localctx = new SetInComprehensionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(44); match(LACC);
				setState(45); term(0);
				setState(46); match(SUCH_AS);
				setState(47); typedef();
				setState(50);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(48); match(COMMA);
					setState(49); formula(0);
					}
				}

				setState(52); match(RACC);
				}
				break;
			case 2:
				_localctx = new SetInExtensionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(54); match(LACC);
				setState(55); term(0);
				setState(60);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(56); match(COMMA);
					setState(57); term(0);
					}
					}
					setState(62);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(63); match(RACC);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ListContext extends ParserRuleContext {
		public ListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_list; }
	 
		public ListContext() { }
		public void copyFrom(ListContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ListInComprehensionContext extends ListContext {
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public TerminalNode RBRACK() { return getToken(CstrSpecParser.RBRACK, 0); }
		public FormulaContext formula() {
			return getRuleContext(FormulaContext.class,0);
		}
		public TerminalNode SUCH_AS() { return getToken(CstrSpecParser.SUCH_AS, 0); }
		public TerminalNode COMMA() { return getToken(CstrSpecParser.COMMA, 0); }
		public TypedefContext typedef() {
			return getRuleContext(TypedefContext.class,0);
		}
		public TerminalNode LBRACK() { return getToken(CstrSpecParser.LBRACK, 0); }
		public ListInComprehensionContext(ListContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).enterListInComprehension(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).exitListInComprehension(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CstrSpecVisitor ) return ((CstrSpecVisitor<? extends T>)visitor).visitListInComprehension(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ListInExtensionContext extends ListContext {
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public TerminalNode RBRACK() { return getToken(CstrSpecParser.RBRACK, 0); }
		public List<TerminalNode> COMMA() { return getTokens(CstrSpecParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(CstrSpecParser.COMMA, i);
		}
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public TerminalNode LBRACK() { return getToken(CstrSpecParser.LBRACK, 0); }
		public ListInExtensionContext(ListContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).enterListInExtension(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).exitListInExtension(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CstrSpecVisitor ) return ((CstrSpecVisitor<? extends T>)visitor).visitListInExtension(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ListContext list() throws RecognitionException {
		ListContext _localctx = new ListContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_list);
		int _la;
		try {
			setState(88);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				_localctx = new ListInComprehensionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(67); match(LBRACK);
				setState(68); term(0);
				setState(69); match(SUCH_AS);
				setState(70); typedef();
				setState(73);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(71); match(COMMA);
					setState(72); formula(0);
					}
				}

				setState(75); match(RBRACK);
				}
				break;
			case 2:
				_localctx = new ListInExtensionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(77); match(LBRACK);
				setState(78); term(0);
				setState(83);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(79); match(COMMA);
					setState(80); term(0);
					}
					}
					setState(85);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(86); match(RBRACK);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ComparisonContext extends ParserRuleContext {
		public TermContext t1;
		public Token op;
		public TermContext t2;
		public TerminalNode INCL() { return getToken(CstrSpecParser.INCL, 0); }
		public TerminalNode IN() { return getToken(CstrSpecParser.IN, 0); }
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public TerminalNode GEQ() { return getToken(CstrSpecParser.GEQ, 0); }
		public TerminalNode LT() { return getToken(CstrSpecParser.LT, 0); }
		public TerminalNode LEQ() { return getToken(CstrSpecParser.LEQ, 0); }
		public TerminalNode NOT_INCL() { return getToken(CstrSpecParser.NOT_INCL, 0); }
		public TerminalNode GT() { return getToken(CstrSpecParser.GT, 0); }
		public TerminalNode NOT_IN() { return getToken(CstrSpecParser.NOT_IN, 0); }
		public TerminalNode NOT_EQ() { return getToken(CstrSpecParser.NOT_EQ, 0); }
		public TerminalNode EQ() { return getToken(CstrSpecParser.EQ, 0); }
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public ComparisonContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparison; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).enterComparison(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).exitComparison(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CstrSpecVisitor ) return ((CstrSpecVisitor<? extends T>)visitor).visitComparison(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComparisonContext comparison() throws RecognitionException {
		ComparisonContext _localctx = new ComparisonContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_comparison);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(90); ((ComparisonContext)_localctx).t1 = term(0);
			setState(91);
			((ComparisonContext)_localctx).op = _input.LT(1);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << IN) | (1L << NOT_IN) | (1L << INCL) | (1L << NOT_INCL) | (1L << EQ) | (1L << NOT_EQ) | (1L << LT) | (1L << LEQ) | (1L << GT) | (1L << GEQ))) != 0)) ) {
				((ComparisonContext)_localctx).op = (Token)_errHandler.recoverInline(this);
			}
			consume();
			setState(92); ((ComparisonContext)_localctx).t2 = term(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypedefContext extends ParserRuleContext {
		public Token op;
		public TermContext i2;
		public TerminalNode INCL() { return getToken(CstrSpecParser.INCL, 0); }
		public TerminalNode IN() { return getToken(CstrSpecParser.IN, 0); }
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(CstrSpecParser.COMMA); }
		public List<TerminalNode> ID() { return getTokens(CstrSpecParser.ID); }
		public TerminalNode NOT_INCL() { return getToken(CstrSpecParser.NOT_INCL, 0); }
		public TerminalNode NOT_IN() { return getToken(CstrSpecParser.NOT_IN, 0); }
		public TerminalNode COMMA(int i) {
			return getToken(CstrSpecParser.COMMA, i);
		}
		public TerminalNode ID(int i) {
			return getToken(CstrSpecParser.ID, i);
		}
		public TypedefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typedef; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).enterTypedef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).exitTypedef(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CstrSpecVisitor ) return ((CstrSpecVisitor<? extends T>)visitor).visitTypedef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypedefContext typedef() throws RecognitionException {
		TypedefContext _localctx = new TypedefContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_typedef);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(94); match(ID);
			setState(99);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(95); match(COMMA);
				setState(96); match(ID);
				}
				}
				setState(101);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(102);
			((TypedefContext)_localctx).op = _input.LT(1);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << IN) | (1L << NOT_IN) | (1L << INCL) | (1L << NOT_INCL))) != 0)) ) {
				((TypedefContext)_localctx).op = (Token)_errHandler.recoverInline(this);
			}
			consume();
			setState(103); ((TypedefContext)_localctx).i2 = term(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FormulaContext extends ParserRuleContext {
		public FormulaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_formula; }
	 
		public FormulaContext() { }
		public void copyFrom(FormulaContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class FormulaOpContext extends FormulaContext {
		public FormulaContext f1;
		public Token op;
		public FormulaContext f2;
		public List<FormulaContext> formula() {
			return getRuleContexts(FormulaContext.class);
		}
		public TerminalNode AND() { return getToken(CstrSpecParser.AND, 0); }
		public TerminalNode OR() { return getToken(CstrSpecParser.OR, 0); }
		public FormulaContext formula(int i) {
			return getRuleContext(FormulaContext.class,i);
		}
		public TerminalNode IFF() { return getToken(CstrSpecParser.IFF, 0); }
		public TerminalNode IMPLIES() { return getToken(CstrSpecParser.IMPLIES, 0); }
		public FormulaOpContext(FormulaContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).enterFormulaOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).exitFormulaOp(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CstrSpecVisitor ) return ((CstrSpecVisitor<? extends T>)visitor).visitFormulaOp(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NotContext extends FormulaContext {
		public FormulaContext formula() {
			return getRuleContext(FormulaContext.class,0);
		}
		public TerminalNode NOT() { return getToken(CstrSpecParser.NOT, 0); }
		public NotContext(FormulaContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).enterNot(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).exitNot(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CstrSpecVisitor ) return ((CstrSpecVisitor<? extends T>)visitor).visitNot(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TermComparisonContext extends FormulaContext {
		public ComparisonContext comparison() {
			return getRuleContext(ComparisonContext.class,0);
		}
		public TermComparisonContext(FormulaContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).enterTermComparison(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).exitTermComparison(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CstrSpecVisitor ) return ((CstrSpecVisitor<? extends T>)visitor).visitTermComparison(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class FalseFormulaContext extends FormulaContext {
		public TerminalNode FALSE() { return getToken(CstrSpecParser.FALSE, 0); }
		public FalseFormulaContext(FormulaContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).enterFalseFormula(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).exitFalseFormula(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CstrSpecVisitor ) return ((CstrSpecVisitor<? extends T>)visitor).visitFalseFormula(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ExistsContext extends FormulaContext {
		public FormulaContext formula() {
			return getRuleContext(FormulaContext.class,0);
		}
		public TerminalNode EXISTS() { return getToken(CstrSpecParser.EXISTS, 0); }
		public TypedefContext typedef() {
			return getRuleContext(TypedefContext.class,0);
		}
		public TerminalNode LPARA() { return getToken(CstrSpecParser.LPARA, 0); }
		public TerminalNode RPARA() { return getToken(CstrSpecParser.RPARA, 0); }
		public ExistsContext(FormulaContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).enterExists(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).exitExists(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CstrSpecVisitor ) return ((CstrSpecVisitor<? extends T>)visitor).visitExists(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TrueFormulaContext extends FormulaContext {
		public TerminalNode TRUE() { return getToken(CstrSpecParser.TRUE, 0); }
		public TrueFormulaContext(FormulaContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).enterTrueFormula(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).exitTrueFormula(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CstrSpecVisitor ) return ((CstrSpecVisitor<? extends T>)visitor).visitTrueFormula(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ProtectedFormulaContext extends FormulaContext {
		public FormulaContext formula() {
			return getRuleContext(FormulaContext.class,0);
		}
		public TerminalNode LPARA() { return getToken(CstrSpecParser.LPARA, 0); }
		public TerminalNode RPARA() { return getToken(CstrSpecParser.RPARA, 0); }
		public ProtectedFormulaContext(FormulaContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).enterProtectedFormula(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).exitProtectedFormula(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CstrSpecVisitor ) return ((CstrSpecVisitor<? extends T>)visitor).visitProtectedFormula(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CstrCallContext extends FormulaContext {
		public CallContext call() {
			return getRuleContext(CallContext.class,0);
		}
		public CstrCallContext(FormulaContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).enterCstrCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).exitCstrCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CstrSpecVisitor ) return ((CstrSpecVisitor<? extends T>)visitor).visitCstrCall(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AllContext extends FormulaContext {
		public FormulaContext formula() {
			return getRuleContext(FormulaContext.class,0);
		}
		public TerminalNode ALL() { return getToken(CstrSpecParser.ALL, 0); }
		public TypedefContext typedef() {
			return getRuleContext(TypedefContext.class,0);
		}
		public TerminalNode LPARA() { return getToken(CstrSpecParser.LPARA, 0); }
		public TerminalNode RPARA() { return getToken(CstrSpecParser.RPARA, 0); }
		public AllContext(FormulaContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).enterAll(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).exitAll(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CstrSpecVisitor ) return ((CstrSpecVisitor<? extends T>)visitor).visitAll(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FormulaContext formula() throws RecognitionException {
		return formula(0);
	}

	private FormulaContext formula(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		FormulaContext _localctx = new FormulaContext(_ctx, _parentState);
		FormulaContext _prevctx = _localctx;
		int _startState = 10;
		enterRecursionRule(_localctx, 10, RULE_formula, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(128);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				{
				_localctx = new NotContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(106); match(NOT);
				setState(107); formula(6);
				}
				break;
			case 2:
				{
				_localctx = new AllContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(108); match(ALL);
				setState(109); match(LPARA);
				setState(110); typedef();
				setState(111); match(RPARA);
				setState(112); formula(5);
				}
				break;
			case 3:
				{
				_localctx = new ExistsContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(114); match(EXISTS);
				setState(115); match(LPARA);
				setState(116); typedef();
				setState(117); match(RPARA);
				setState(118); formula(4);
				}
				break;
			case 4:
				{
				_localctx = new ProtectedFormulaContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(120); match(LPARA);
				setState(121); formula(0);
				setState(122); match(RPARA);
				}
				break;
			case 5:
				{
				_localctx = new TermComparisonContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(124); comparison();
				}
				break;
			case 6:
				{
				_localctx = new TrueFormulaContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(125); match(TRUE);
				}
				break;
			case 7:
				{
				_localctx = new FalseFormulaContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(126); match(FALSE);
				}
				break;
			case 8:
				{
				_localctx = new CstrCallContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(127); call();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(135);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			while ( _alt!=2 && _alt!=ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new FormulaOpContext(new FormulaContext(_parentctx, _parentState));
					((FormulaOpContext)_localctx).f1 = _prevctx;
					pushNewRecursionContext(_localctx, _startState, RULE_formula);
					setState(130);
					if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
					setState(131);
					((FormulaOpContext)_localctx).op = _input.LT(1);
					_la = _input.LA(1);
					if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << AND) | (1L << OR) | (1L << IMPLIES) | (1L << IFF))) != 0)) ) {
						((FormulaOpContext)_localctx).op = (Token)_errHandler.recoverInline(this);
					}
					consume();
					setState(132); ((FormulaOpContext)_localctx).f2 = formula(9);
					}
					} 
				}
				setState(137);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class CallContext extends ParserRuleContext {
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public List<TerminalNode> COMMA() { return getTokens(CstrSpecParser.COMMA); }
		public TerminalNode ID() { return getToken(CstrSpecParser.ID, 0); }
		public TerminalNode LPARA() { return getToken(CstrSpecParser.LPARA, 0); }
		public TerminalNode BEGIN() { return getToken(CstrSpecParser.BEGIN, 0); }
		public TerminalNode COMMA(int i) {
			return getToken(CstrSpecParser.COMMA, i);
		}
		public TerminalNode RPARA() { return getToken(CstrSpecParser.RPARA, 0); }
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public CallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_call; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).enterCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).exitCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CstrSpecVisitor ) return ((CstrSpecVisitor<? extends T>)visitor).visitCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CallContext call() throws RecognitionException {
		CallContext _localctx = new CallContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_call);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(139);
			_la = _input.LA(1);
			if (_la==BEGIN) {
				{
				setState(138); match(BEGIN);
				}
			}

			setState(141); match(ID);
			setState(142); match(LPARA);
			setState(143); term(0);
			setState(148);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(144); match(COMMA);
				setState(145); term(0);
				}
				}
				setState(150);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(151); match(RPARA);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConstraintContext extends ParserRuleContext {
		public TypedefContext typedef(int i) {
			return getRuleContext(TypedefContext.class,i);
		}
		public FormulaContext formula() {
			return getRuleContext(FormulaContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(CstrSpecParser.COMMA); }
		public TerminalNode ID() { return getToken(CstrSpecParser.ID, 0); }
		public List<TypedefContext> typedef() {
			return getRuleContexts(TypedefContext.class);
		}
		public TerminalNode LPARA() { return getToken(CstrSpecParser.LPARA, 0); }
		public TerminalNode CONSTRAINT() { return getToken(CstrSpecParser.CONSTRAINT, 0); }
		public TerminalNode CORE() { return getToken(CstrSpecParser.CORE, 0); }
		public TerminalNode DEF_CONTENT() { return getToken(CstrSpecParser.DEF_CONTENT, 0); }
		public TerminalNode COMMA(int i) {
			return getToken(CstrSpecParser.COMMA, i);
		}
		public TerminalNode RPARA() { return getToken(CstrSpecParser.RPARA, 0); }
		public TerminalNode DISCRETE() { return getToken(CstrSpecParser.DISCRETE, 0); }
		public ConstraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).enterConstraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).exitConstraint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CstrSpecVisitor ) return ((CstrSpecVisitor<? extends T>)visitor).visitConstraint(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstraintContext constraint() throws RecognitionException {
		ConstraintContext _localctx = new ConstraintContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_constraint);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(154);
			_la = _input.LA(1);
			if (_la==CORE) {
				{
				setState(153); match(CORE);
				}
			}

			setState(157);
			_la = _input.LA(1);
			if (_la==DISCRETE) {
				{
				setState(156); match(DISCRETE);
				}
			}

			setState(159); match(CONSTRAINT);
			setState(160); match(ID);
			setState(161); match(LPARA);
			setState(170);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(162); typedef();
				setState(167);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(163); match(COMMA);
					setState(164); typedef();
					}
					}
					setState(169);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(172); match(RPARA);
			setState(173); match(DEF_CONTENT);
			setState(174); formula(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SpecContext extends ParserRuleContext {
		public ConstraintContext constraint(int i) {
			return getRuleContext(ConstraintContext.class,i);
		}
		public List<ConstraintContext> constraint() {
			return getRuleContexts(ConstraintContext.class);
		}
		public SpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_spec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).enterSpec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CstrSpecListener ) ((CstrSpecListener)listener).exitSpec(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CstrSpecVisitor ) return ((CstrSpecVisitor<? extends T>)visitor).visitSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SpecContext spec() throws RecognitionException {
		SpecContext _localctx = new SpecContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_spec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(177); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(176); constraint();
				}
				}
				setState(179); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << DISCRETE) | (1L << CORE) | (1L << CONSTRAINT))) != 0) );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 0: return term_sempred((TermContext)_localctx, predIndex);
		case 5: return formula_sempred((FormulaContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean term_sempred(TermContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0: return precpred(_ctx, 9);
		}
		return true;
	}
	private boolean formula_sempred(FormulaContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1: return precpred(_ctx, 8);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3.\u00b8\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\3\2\3\2"+
		"\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\5\2%\n\2\3\2"+
		"\3\2\3\2\7\2*\n\2\f\2\16\2-\13\2\3\3\3\3\3\3\3\3\3\3\3\3\5\3\65\n\3\3"+
		"\3\3\3\3\3\3\3\3\3\3\3\7\3=\n\3\f\3\16\3@\13\3\3\3\3\3\5\3D\n\3\3\4\3"+
		"\4\3\4\3\4\3\4\3\4\5\4L\n\4\3\4\3\4\3\4\3\4\3\4\3\4\7\4T\n\4\f\4\16\4"+
		"W\13\4\3\4\3\4\5\4[\n\4\3\5\3\5\3\5\3\5\3\6\3\6\3\6\7\6d\n\6\f\6\16\6"+
		"g\13\6\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\5\7\u0083\n\7\3\7\3\7\3\7\7\7"+
		"\u0088\n\7\f\7\16\7\u008b\13\7\3\b\5\b\u008e\n\b\3\b\3\b\3\b\3\b\3\b\7"+
		"\b\u0095\n\b\f\b\16\b\u0098\13\b\3\b\3\b\3\t\5\t\u009d\n\t\3\t\5\t\u00a0"+
		"\n\t\3\t\3\t\3\t\3\t\3\t\3\t\7\t\u00a8\n\t\f\t\16\t\u00ab\13\t\5\t\u00ad"+
		"\n\t\3\t\3\t\3\t\3\t\3\n\6\n\u00b4\n\n\r\n\16\n\u00b5\3\n\2\4\2\f\13\2"+
		"\4\6\b\n\f\16\20\22\2\6\4\2\16\21\25\26\5\2\n\r\31\32 #\3\2\n\r\4\2\27"+
		"\30\36\37\u00cc\2$\3\2\2\2\4C\3\2\2\2\6Z\3\2\2\2\b\\\3\2\2\2\n`\3\2\2"+
		"\2\f\u0082\3\2\2\2\16\u008d\3\2\2\2\20\u009c\3\2\2\2\22\u00b3\3\2\2\2"+
		"\24\25\b\2\1\2\25\26\7\33\2\2\26\27\5\2\2\2\27\30\7\34\2\2\30%\3\2\2\2"+
		"\31%\5\16\b\2\32%\7.\2\2\33\34\7.\2\2\34\35\7\'\2\2\35\36\5\2\2\2\36\37"+
		"\7(\2\2\37%\3\2\2\2 %\5\4\3\2!%\5\6\4\2\"%\7\24\2\2#%\7)\2\2$\24\3\2\2"+
		"\2$\31\3\2\2\2$\32\3\2\2\2$\33\3\2\2\2$ \3\2\2\2$!\3\2\2\2$\"\3\2\2\2"+
		"$#\3\2\2\2%+\3\2\2\2&\'\f\13\2\2\'(\t\2\2\2(*\5\2\2\f)&\3\2\2\2*-\3\2"+
		"\2\2+)\3\2\2\2+,\3\2\2\2,\3\3\2\2\2-+\3\2\2\2./\7\7\2\2/\60\5\2\2\2\60"+
		"\61\7\6\2\2\61\64\5\n\6\2\62\63\7\t\2\2\63\65\5\f\7\2\64\62\3\2\2\2\64"+
		"\65\3\2\2\2\65\66\3\2\2\2\66\67\7\b\2\2\67D\3\2\2\289\7\7\2\29>\5\2\2"+
		"\2:;\7\t\2\2;=\5\2\2\2<:\3\2\2\2=@\3\2\2\2><\3\2\2\2>?\3\2\2\2?A\3\2\2"+
		"\2@>\3\2\2\2AB\7\b\2\2BD\3\2\2\2C.\3\2\2\2C8\3\2\2\2D\5\3\2\2\2EF\7\'"+
		"\2\2FG\5\2\2\2GH\7\6\2\2HK\5\n\6\2IJ\7\t\2\2JL\5\f\7\2KI\3\2\2\2KL\3\2"+
		"\2\2LM\3\2\2\2MN\7(\2\2N[\3\2\2\2OP\7\'\2\2PU\5\2\2\2QR\7\t\2\2RT\5\2"+
		"\2\2SQ\3\2\2\2TW\3\2\2\2US\3\2\2\2UV\3\2\2\2VX\3\2\2\2WU\3\2\2\2XY\7("+
		"\2\2Y[\3\2\2\2ZE\3\2\2\2ZO\3\2\2\2[\7\3\2\2\2\\]\5\2\2\2]^\t\3\2\2^_\5"+
		"\2\2\2_\t\3\2\2\2`e\7.\2\2ab\7\t\2\2bd\7.\2\2ca\3\2\2\2dg\3\2\2\2ec\3"+
		"\2\2\2ef\3\2\2\2fh\3\2\2\2ge\3\2\2\2hi\t\4\2\2ij\5\2\2\2j\13\3\2\2\2k"+
		"l\b\7\1\2lm\7&\2\2m\u0083\5\f\7\bno\7\22\2\2op\7\33\2\2pq\5\n\6\2qr\7"+
		"\34\2\2rs\5\f\7\7s\u0083\3\2\2\2tu\7\23\2\2uv\7\33\2\2vw\5\n\6\2wx\7\34"+
		"\2\2xy\5\f\7\6y\u0083\3\2\2\2z{\7\33\2\2{|\5\f\7\2|}\7\34\2\2}\u0083\3"+
		"\2\2\2~\u0083\5\b\5\2\177\u0083\7$\2\2\u0080\u0083\7%\2\2\u0081\u0083"+
		"\5\16\b\2\u0082k\3\2\2\2\u0082n\3\2\2\2\u0082t\3\2\2\2\u0082z\3\2\2\2"+
		"\u0082~\3\2\2\2\u0082\177\3\2\2\2\u0082\u0080\3\2\2\2\u0082\u0081\3\2"+
		"\2\2\u0083\u0089\3\2\2\2\u0084\u0085\f\n\2\2\u0085\u0086\t\5\2\2\u0086"+
		"\u0088\5\f\7\13\u0087\u0084\3\2\2\2\u0088\u008b\3\2\2\2\u0089\u0087\3"+
		"\2\2\2\u0089\u008a\3\2\2\2\u008a\r\3\2\2\2\u008b\u0089\3\2\2\2\u008c\u008e"+
		"\7*\2\2\u008d\u008c\3\2\2\2\u008d\u008e\3\2\2\2\u008e\u008f\3\2\2\2\u008f"+
		"\u0090\7.\2\2\u0090\u0091\7\33\2\2\u0091\u0096\5\2\2\2\u0092\u0093\7\t"+
		"\2\2\u0093\u0095\5\2\2\2\u0094\u0092\3\2\2\2\u0095\u0098\3\2\2\2\u0096"+
		"\u0094\3\2\2\2\u0096\u0097\3\2\2\2\u0097\u0099\3\2\2\2\u0098\u0096\3\2"+
		"\2\2\u0099\u009a\7\34\2\2\u009a\17\3\2\2\2\u009b\u009d\7,\2\2\u009c\u009b"+
		"\3\2\2\2\u009c\u009d\3\2\2\2\u009d\u009f\3\2\2\2\u009e\u00a0\7+\2\2\u009f"+
		"\u009e\3\2\2\2\u009f\u00a0\3\2\2\2\u00a0\u00a1\3\2\2\2\u00a1\u00a2\7-"+
		"\2\2\u00a2\u00a3\7.\2\2\u00a3\u00ac\7\33\2\2\u00a4\u00a9\5\n\6\2\u00a5"+
		"\u00a6\7\t\2\2\u00a6\u00a8\5\n\6\2\u00a7\u00a5\3\2\2\2\u00a8\u00ab\3\2"+
		"\2\2\u00a9\u00a7\3\2\2\2\u00a9\u00aa\3\2\2\2\u00aa\u00ad\3\2\2\2\u00ab"+
		"\u00a9\3\2\2\2\u00ac\u00a4\3\2\2\2\u00ac\u00ad\3\2\2\2\u00ad\u00ae\3\2"+
		"\2\2\u00ae\u00af\7\34\2\2\u00af\u00b0\7\35\2\2\u00b0\u00b1\5\f\7\2\u00b1"+
		"\21\3\2\2\2\u00b2\u00b4\5\20\t\2\u00b3\u00b2\3\2\2\2\u00b4\u00b5\3\2\2"+
		"\2\u00b5\u00b3\3\2\2\2\u00b5\u00b6\3\2\2\2\u00b6\23\3\2\2\2\24$+\64>C"+
		"KUZe\u0082\u0089\u008d\u0096\u009c\u009f\u00a9\u00ac\u00b5";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}