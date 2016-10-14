/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

package org.btrplace.safeplace.spec;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.btrplace.safeplace.spec.antlr.CstrSpecParser;
import org.btrplace.safeplace.spec.prop.*;
import org.btrplace.safeplace.spec.term.*;
import org.btrplace.safeplace.spec.term.func.Function;
import org.btrplace.safeplace.spec.term.func.FunctionCall;
import org.btrplace.safeplace.spec.term.func.ValueAt;
import org.btrplace.safeplace.spec.type.*;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Fabien Hermenier
 */
public class MyCstrSpecVisitor extends org.btrplace.safeplace.spec.antlr.CstrSpecBaseVisitor {

    private SymbolsTable symbols;

    private String filename;

    private SpecException err = null;

    public MyCstrSpecVisitor() {
        symbols = new SymbolsTable();
    }

    public MyCstrSpecVisitor args(List<UserVar> args) {
        args.forEach(symbols::put);
        return this;
    }

    public MyCstrSpecVisitor library(List<Function> funcs) {
        funcs.forEach(symbols::put);
        return this;
    }

    public MyCstrSpecVisitor constraints(List<Constraint> cstrs) {
        cstrs.forEach(symbols::put);
        return this;
    }


    public Proposition getProposition(String name, ParseTree t) throws SpecException {
        symbols = symbols.enterSpec();
        filename = name;

        Proposition p = (Proposition) visit(t);
        if (err != null) {
            throw err;
        }
        symbols = symbols.leaveScope();
        return p;
    }

    public UserVar getUserVar(String name, ParseTree t) throws SpecException {
        symbols = symbols.enterSpec();
        filename = name;
        List<UserVar> l = (List) visit(t);
        if (err != null) {
            throw err;
        }
        UserVar v = l.get(0);
        symbols = symbols.leaveScope();
        return v;
    }

    @Override
    public Proposition visitAll(@NotNull CstrSpecParser.AllContext ctx) {
        List<UserVar> vars = visitTypedef(ctx.typedef());
        if (vars == null) {
            return null;
        }

        Proposition p = (Proposition) visit(ctx.formula());
        if (p == null) {
            return null;
        }
        return new ForAll(vars, p);
    }

    @Override
    public Object visitExists(@NotNull CstrSpecParser.ExistsContext ctx) {
        List<UserVar> vars = visitTypedef(ctx.typedef());
        if (vars == null) {
            return null;
        }
        Proposition p = (Proposition) visit(ctx.formula());
        if (p == null) {
            return null;
        }
        return new Exists(vars, p);
    }

    public FunctionCall visitCall(@NotNull CstrSpecParser.CallContext ctx) {
        String id = ctx.ID().getText();
        List<Term> ps = new ArrayList<>();
        for (CstrSpecParser.TermContext t : ctx.term()) {
            Term tm = (Term) visit(t);
            if (tm == null) {
                return null;
            }
            ps.add(tm);
        }

        Function f = symbols.getFunction(id);
        if (f == null) {
            report(ctx.ID().getSymbol(), SpecException.ErrType.SYMBOL_NOT_FOUND, id, "Cannot resolve symbol '" + id + "'");
            return null;
        }

        try {
            FunctionCall.Moment m = FunctionCall.Moment.any;
            if (ctx.BEGIN() != null) {
                m = FunctionCall.Moment.begin;
            }
            return new FunctionCall(f, ps, m);
        } catch (IllegalArgumentException ex) {
            report(ctx.ID().getSymbol(), ex.getMessage());
            return null;
        }
    }

    @Override
    public ConstraintCall visitCstrCall(@NotNull CstrSpecParser.CstrCallContext ctx) {
        String lbl = ctx.call().ID().getText();
        List<Term> ps = new ArrayList<>();
        for (CstrSpecParser.TermContext t : ctx.call().term()) {
            Term tm = (Term) visit(t);
            if (tm == null) {
                return null;
            }
            ps.add(tm);
        }

        Function<Boolean> ref = symbols.getFunction(lbl);
        if (ref == null) {
            report(ctx.call().ID().getSymbol(), SpecException.ErrType.SYMBOL_NOT_FOUND, lbl, "Cannot resolve symbol '" + lbl + "'");
            return null;
        }

        try {
            return new ConstraintCall(ref, ps);
        } catch (IllegalArgumentException ex) {
            report(ctx.call().ID().getSymbol(), ex.getMessage());
            return null;
        }
    }

    @Override
    public List<UserVar> visitTypedef(@NotNull CstrSpecParser.TypedefContext ctx) {

        Term parent = (Term) visit(ctx.term());
        if (parent == null) {
            return null;
        }
        if (parent.type() instanceof Atomic) {
            report(ctx.op, "Unsupported operation: '" + parent + "' is an atomic type");
            return null;
        }
        List<UserVar> vars = new ArrayList<>();
        for (TerminalNode n : ctx.ID()) {
            String lbl = n.getText();
            UserVar v = new UserVar(lbl, ctx.op.getText(), parent);
            symbols.put(v);
            vars.add(v);
        }
        return vars;
    }

    @Override
    public UserVar visitArg(CstrSpecParser.ArgContext ctx) {
        Term parent = (Term) visit(ctx.term());
        if (parent == null) {
            return null;
        }
        if (parent.type() instanceof Atomic) {
            report(ctx.op, "Unsupported operation: '" + parent + "' is an atomic type");
            return null;
        }
        String lbl = ctx.ID().getText();
        UserVar v = new UserVar(lbl, ctx.op.getText(), parent);
        symbols.put(v);
        return v;
    }

    @Override
    public Proposition visitFormulaOp(@NotNull CstrSpecParser.FormulaOpContext ctx) {
        Proposition p1 = (Proposition) visit(ctx.f1);
        if (p1 == null) {
            return null;
        }
        Proposition p2 = (Proposition) visit(ctx.f2);
        if (p2 == null) {
            return null;
        }
        switch (ctx.op.getType()) {
            case CstrSpecParser.AND:
                return new And(p1, p2);
            case CstrSpecParser.OR:
                return new Or(p1, p2);
            case CstrSpecParser.IMPLIES:
                return new Implies(p1, p2);
            case CstrSpecParser.IFF:
                return new Iff(p1, p2);
        }
        report(ctx.op, "Unsupported operation: " + ctx.op.getText());
        return null;
    }

    private void report(Token t, String msg) {
        report(t, SpecException.ErrType.UNKNOWN, "", msg);
    }

    private void report(Token t, SpecException.ErrType type, String val, String msg) {
        int l = t.getLine();
        int c = t.getCharPositionInLine();
        err = new SpecException(type, "[" + filename + " " + l + ":" + c + "] " + msg);
        err.value = val;
    }

    @Override
    public ExplodedSet visitSetInExtension(@NotNull CstrSpecParser.SetInExtensionContext ctx) {
        List<Term> s = new ArrayList<>();
        Type ty = null;
        for (CstrSpecParser.TermContext t : ctx.term()) {
            Term tr = (Term) visit(t);
            if (tr == null) {
                return null;
            } else if (ty == null) {
                ty = tr.type();
            } else {
                if (!ty.equals(tr.type())) {
                    report(t.start, "A Set of '" + ty + "' cannot embed a " + tr.type());
                    return null;
                }
            }
            s.add(tr);
        }
        return new ExplodedSet(s, ty);
    }

    @Override
    public Term visitProtectedTerm(@NotNull CstrSpecParser.ProtectedTermContext ctx) {
        Term t = (Term) visit(ctx.term());
        return t == null ? null : new ProtectedTerm(t);
    }


    @Override
    public SetBuilder visitSetInComprehension(@NotNull CstrSpecParser.SetInComprehensionContext ctx) {
        //Get the binder
        List<UserVar> v = visitTypedef(ctx.typedef());
        if (v == null) {
            return null;
        }

        Proposition p = Proposition.True;
        if (ctx.COMMA() != null) {
            p = (Proposition) visit(ctx.formula());
            if (p == null) {
                return null;
            }
        }
        Term t = (Term) visit(ctx.term());
        if (t == null) {
            return null;
        }
        return new SetBuilder(t, v.get(0), p);
    }

    @Override
    public ListBuilder visitListInComprehension(@NotNull CstrSpecParser.ListInComprehensionContext ctx) {
        //Get the binder
        List<UserVar> v = visitTypedef(ctx.typedef());
        if (v == null) {
            return null;
        }

        Proposition p = Proposition.True;
        if (ctx.COMMA() != null) {
            p = (Proposition) visit(ctx.formula());
            if (p == null) {
                return null;
            }
        }
        Term t = (Term) visit(ctx.term());
        if (t == null) {
            return null;
        }
        return new ListBuilder(t, v.get(0), p);
    }

    @Override
    public Proposition visitProtectedFormula(@NotNull CstrSpecParser.ProtectedFormulaContext ctx) {
        Proposition p = (Proposition) visit(ctx.formula());
        return p == null ? null : new ProtectedProposition(p);
    }

    @Override
    public Term visitArrayTerm(@NotNull CstrSpecParser.ArrayTermContext ctx) {
        String lbl = ctx.ID().getText();
        Var v = symbols.getVar(lbl);
        if (v == null) {
            report(ctx.ID().getSymbol(), SpecException.ErrType.SYMBOL_NOT_FOUND, lbl, "Unknown symbol '" + lbl + "'");
            return null;
        }
        //Type check
        if (!(v.type() instanceof ListType)) {
            report(ctx.ID().getSymbol(), "'" + lbl + "' must be a list (currently: '" + v.type() + "')");
            return null;
        }

        Term idx = (Term) visit(ctx.term());
        if (!(idx.type() instanceof IntType)) {
            report(ctx.ID().getSymbol(), "The index must be an integer (currently: '" + v.type() + "')");
            return null;
        }

        try {
            return new ValueAt(v, idx);
        } catch (IllegalArgumentException ex) {
            report(ctx.ID().getSymbol(), ex.getMessage());
            return null;
        }
    }

    @Override
    public Term visitIdTerm(@NotNull CstrSpecParser.IdTermContext ctx) {
        String ref = ctx.ID().getText();
        Term v = symbols.getVar(ref);
        if (v != null) {
            return v;
        }

        Term c;
        c = VMStateType.getInstance().parse(ref);
        if (c == null) {
            c = NodeStateType.getInstance().parse(ref);
        }
        if (c == null) {
            c = TimeType.getInstance().parse(ref);
        }
        if (c == null) {
            report(ctx.ID().getSymbol(), SpecException.ErrType.SYMBOL_NOT_FOUND, ctx.ID().getText(), "Unknown symbol '" + ctx.ID().getText() + "'");
        }
        return c;
    }

    @Override
    public Constant visitIntTerm(@NotNull CstrSpecParser.IntTermContext ctx) {
        return IntType.getInstance().parse(ctx.INT().getText());
    }

    @Override
    public Object visitStringTerm(@NotNull CstrSpecParser.StringTermContext ctx) {
        String txt = ctx.STRING().getText();
        return StringType.getInstance().parse(txt.substring(1, txt.length()));
    }


    private boolean sameType(Token to, Term t1, Term t2) {
        if (t1.type().equals(NoneType.getInstance()) || t2.type().equals(NoneType.getInstance())) {
            return true;
        }
        if (!t1.type().equals(t2.type())) {
            report(to, "Incompatible types: expecting '" + t2.type() + " " + to.getText() + " " + t2.type() +
                    "' but was '" + t1.type() + " " + to.getText() + " " + t2.type() + "'");
            return false;
        }
        return true;
    }

    private boolean sameType(Token to, Term t1, Term t2, Type t) {
        if (t1.type().equals(NoneType.getInstance()) || t2.type().equals(NoneType.getInstance())) {
            return true;
        }
        if (!t1.type().equals(t2.type())) {
            report(to, "Incompatible types: expecting '" + t + " " + to.getText() + " " + t +
                    "' but was '" + t1.type() + " " + to.getText() + " " + t2.type() + "'");
            return false;
        }
        return true;
    }

    private boolean isIn(Token to, Term t1, Term t2) {
        Type t = t2.type();
        if (!(t instanceof SetType)) {
            report(to, t2 + " is supposed to be a set instead of a '" + t + "'");
            return false;
        }
        SetType st = (SetType) t;
        if (!st.enclosingType().equals(t1.type())) {
            report(to, "Incompatible types: expecting '" + st.enclosingType() + " " + to.getText() + " " + st +
                    "' but was '" + t1.type() + " " + to.getText() + " " + t2.type() + "'");
            return false;
        }
        return true;
    }

    @Override
    public Proposition visitTermComparison(@NotNull CstrSpecParser.TermComparisonContext c) {
        CstrSpecParser.ComparisonContext ctx = c.comparison();
        Term t1 = (Term) visit(ctx.t1);
        if (t1 == null) {
            return null;
        }
        Term t2 = (Term) visit(ctx.t2);
        if (t2 == null) {
            return null;
        }

        switch (ctx.op.getType()) {

            case CstrSpecParser.EQ:
                return sameType(ctx.op, t1, t2) ? new Eq(t1, t2) : null;
            case CstrSpecParser.NOT_EQ:
                return sameType(ctx.op, t1, t2) ? new NEq(t1, t2) : null;
            case CstrSpecParser.IN:
                return isIn(ctx.op, t1, t2) ? new In(t1, t2) : null;
            case CstrSpecParser.NOT_IN:
                return isIn(ctx.op, t1, t2) ? new NIn(t1, t2) : null;
            case CstrSpecParser.INCL:
                return sameType(ctx.op, t1, t2) ? new Inc(t1, t2) : null;
            case CstrSpecParser.NOT_INCL:
                return sameType(ctx.op, t1, t2) ? new NInc(t1, t2) : null;
            case CstrSpecParser.PART:
                return isIn(ctx.op, t2, t1) ? new Packings(t1, t2) : null;
            case CstrSpecParser.NOT_PART:
                return isIn(ctx.op, t2, t1) ? new NoPackings(t1, t2) : null;
            case CstrSpecParser.LT:
                return sameType(ctx.op, t1, t2, IntType.getInstance()) ? new Lt(t1, t2) : null;
            case CstrSpecParser.LEQ:
                return sameType(ctx.op, t1, t2, IntType.getInstance()) ? new Leq(t1, t2) : null;
            case CstrSpecParser.GT:
                return sameType(ctx.op, t1, t2, IntType.getInstance()) ? new Lt(t2, t1) : null;
            case CstrSpecParser.GEQ:
                return sameType(ctx.op, t1, t2, IntType.getInstance()) ? new Leq(t2, t1) : null;
        }
        report(ctx.op, "Unsupported operation: " + ctx.op.getText());
        return null;
    }

    @Override
    public Proposition visitFalseFormula(@NotNull CstrSpecParser.FalseFormulaContext ctx) {
        return Proposition.False;
    }

    @Override
    public Proposition visitTrueFormula(@NotNull CstrSpecParser.TrueFormulaContext ctx) {
        return Proposition.True;
    }

    @Override
    public Term visitTermOp(@NotNull CstrSpecParser.TermOpContext ctx) {
        Term t1 = (Term) visit(ctx.t1);
        if (t1 == null) {
            return null;
        }
        Term t2 = (Term) visit(ctx.t2);
        if (t2 == null) {
            return null;
        }

        switch (ctx.op.getType()) {
            case CstrSpecParser.PLUS:
                if (t1.type() == IntType.getInstance() && sameType(ctx.op, t1, t2)) {
                    return new IntPlus(t1, t2);
                } else if (t1.type() instanceof SetType && sameType(ctx.op, t1, t2)) {
                    return new SetPlus(t1, t2);
                }
                break;
            case CstrSpecParser.MINUS:
                if (t1.type() == IntType.getInstance() && sameType(ctx.op, t1, t2)) {
                    return new IntMinus(t1, t2);
                } else if (t1.type() instanceof SetType && sameType(ctx.op, t1, t2)) {
                    return new SetMinus(t1, t2);
                }
                break;
            case CstrSpecParser.MULT:
                return new Mult(t1, t2);
            default:
                report(ctx.op, "Unsupported operation: " + ctx.op.getText());
                return null;
        }
        report(ctx.op, "Unsupported operation: " + t1.type() + " " + ctx.op.getText() + " " + t2.type());
        return null;
    }
}
