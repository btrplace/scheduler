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
import java.util.stream.Collectors;


/**
 * @author Fabien Hermenier
 */
public class MyCstrSpecVisitor extends org.btrplace.safeplace.spec.antlr.CstrSpecBaseVisitor {

    private SymbolsTable symbols;

    private String filename;

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


    /**
     * @param name
     * @param t
     * @return
     * @throws SpecException
     */
    public Proposition getProposition(String name, ParseTree t) {
        symbols = symbols.enterSpec();
        filename = name;
        try {
            return (Proposition) visit(t);
        } finally {
            symbols = symbols.leaveScope();
        }
    }

    /**
     *
     * @param name
     * @param t
     * @throws SpecException
     * @return
     */
    public UserVar getUserVar(String name, ParseTree t) {
        symbols = symbols.enterSpec();
        filename = name;
        try {
            return ((List<UserVar>) visit(t)).get(0);
        } finally {
            symbols = symbols.leaveScope();
        }
    }

    @Override
    public Proposition visitAll(@NotNull CstrSpecParser.AllContext ctx) {
        List<UserVar> vars = visitTypedef(ctx.typedef());
        Proposition p = (Proposition) visit(ctx.formula());
        return new ForAll(vars, p);
    }

    @Override
    public Exists visitExists(@NotNull CstrSpecParser.ExistsContext ctx) {
        List<UserVar> vars = visitTypedef(ctx.typedef());
        Proposition p = (Proposition) visit(ctx.formula());
        return new Exists(vars, p);
    }

    private Function resolveFunction(Token t, List<Term> args) {
        Function f = symbols.getFunction(t.getText());
        if (f == null) {
            throw SpecException.unknownSymbol(filename, t);
        }

        Type[] expected = f.signature();
        if (expected.length != args.size()) {
            throw SpecException.badFunctionCall(filename, t, f, args);
        }
        for (int i = 0; i < expected.length; i++) {
            if (!expected[i].equals(args.get(i).type())) {
                throw SpecException.badFunctionCall(filename, t, f, args);
            }
        }

        return f;
    }

    @Override
    public FunctionCall visitCall(@NotNull CstrSpecParser.CallContext ctx) {

        List<Term> ps = ctx.term().stream().map(t -> (Term) visit(t)).collect(Collectors.toList());
        Function f = resolveFunction(ctx.ID().getSymbol(), ps);

        FunctionCall.Moment m = FunctionCall.Moment.any;
        if (ctx.BEGIN() != null) {
            m = FunctionCall.Moment.begin;
        }
        return new FunctionCall(f, ps, m);
    }

    @Override
    public ConstraintCall visitCstrCall(@NotNull CstrSpecParser.CstrCallContext ctx) {
        List<Term> ps = ctx.call().term().stream().map(t -> (Term) visit(t)).collect(Collectors.toList());
        Function f = resolveFunction(ctx.call().ID().getSymbol(), ps);
        return new ConstraintCall(f, ps);
    }

    @Override
    public List<UserVar> visitTypedef(@NotNull CstrSpecParser.TypedefContext ctx) {

        Term parent = (Term) visit(ctx.term());
        if (parent.type() instanceof Atomic) {
            throw new SpecException(filename, ctx.op.getCharPositionInLine(), "The right-hand side must be a collection");
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
    public Proposition visitFormulaOp(@NotNull CstrSpecParser.FormulaOpContext ctx) {
        Proposition p1 = (Proposition) visit(ctx.f1);
        Proposition p2 = (Proposition) visit(ctx.f2);
        switch (ctx.op.getType()) {
            case CstrSpecParser.AND:
                return new And(p1, p2);
            case CstrSpecParser.OR:
                return new Or(p1, p2);
            case CstrSpecParser.IMPLIES:
                return new Implies(p1, p2);
            case CstrSpecParser.IFF:
                return new Iff(p1, p2);
            default:
                throw SpecException.unsupportedOperation(filename, BoolType.getInstance(), ctx.op, BoolType.getInstance());
        }
    }

    @Override
    public ExplodedSet visitSetInExtension(@NotNull CstrSpecParser.SetInExtensionContext ctx) {
        List<Term> s = new ArrayList<>();
        Type ty = null;
        for (CstrSpecParser.TermContext t : ctx.term()) {
            Term tr = (Term) visit(t);
            if (ty == null) {
                ty = tr.type();
            }
            assertEqualsTypes(t.getStart(), ty, tr.type());
            s.add(tr);
        }
        return new ExplodedSet(s, ty);
    }

    @Override
    public Term visitProtectedTerm(@NotNull CstrSpecParser.ProtectedTermContext ctx) {
        Term t = (Term) visit(ctx.term());
        return new ProtectedTerm(t);
    }

    @Override
    public SetBuilder visitSetInComprehension(@NotNull CstrSpecParser.SetInComprehensionContext ctx) {
        //Get the binder
        List<UserVar> v = visitTypedef(ctx.typedef());

        Proposition p = Proposition.True;
        if (ctx.COMMA() != null) {
            p = (Proposition) visit(ctx.formula());
        }
        Term t = (Term) visit(ctx.term());
        return new SetBuilder(t, v.get(0), p);
    }

    @Override
    public ListBuilder visitListInComprehension(@NotNull CstrSpecParser.ListInComprehensionContext ctx) {
        //Get the binder
        List<UserVar> v = visitTypedef(ctx.typedef());

        Proposition p = Proposition.True;
        if (ctx.COMMA() != null) {
            p = (Proposition) visit(ctx.formula());
        }
        Term t = (Term) visit(ctx.term());
        return new ListBuilder(t, v.get(0), p);
    }

    @Override
    public Proposition visitProtectedFormula(@NotNull CstrSpecParser.ProtectedFormulaContext ctx) {
        Proposition p = (Proposition) visit(ctx.formula());
        return new ProtectedProposition(p);
    }

    @Override
    public Term visitArrayTerm(@NotNull CstrSpecParser.ArrayTermContext ctx) {
        String lbl = ctx.ID().getText();
        Var v = symbols.getVar(lbl);
        if (v == null) {
            throw SpecException.unknownSymbol(filename, ctx.ID().getSymbol());
        }
        //Type check
        if (!(v.type() instanceof ListType)) {
            throw new SpecException(filename, ctx.ID().getSymbol().getCharPositionInLine(), "List expected. Got '" + v.type() + "')");
        }

        Term idx = (Term) visit(ctx.term());
        assertEqualsTypes(ctx.term().getStart(), IntType.getInstance(), idx.type());
        return new ValueAt(v, idx);
    }

    @Override
    public Term visitIdTerm(@NotNull CstrSpecParser.IdTermContext ctx) {
        String ref = ctx.ID().getText();
        Term v = symbols.getVar(ref);
        if (v != null) {
            return v;
        }
        Term c = VMStateType.getInstance().parse(ref);
        if (c == null) {
            c = NodeStateType.getInstance().parse(ref);
        }
        if (c == null) {
            throw SpecException.unknownSymbol(filename, ctx.ID().getSymbol());
        }
        return c;
    }

    @Override
    public Constant visitIntTerm(@NotNull CstrSpecParser.IntTermContext ctx) {
        return IntType.getInstance().parse(ctx.INT().getText());
    }

    @Override
    public Constant visitStringTerm(@NotNull CstrSpecParser.StringTermContext ctx) {
        String txt = ctx.STRING().getText();
        return StringType.getInstance().parse(txt.substring(1, txt.length()));
    }

    private void assertEqualsTypes(Token to, Type expected, Type... got) {
        for (Type t : got) {
            if (!expected.equals(t)) {
                throw SpecException.typeMismatch(filename, to.getCharPositionInLine(), expected, t);
            }
        }
    }

    private void assertIn(Token to, Term t1, Term t2) {
        Type t = t2.type();
        if (!(t instanceof SetType)) {
            throw new SpecException(filename, to.getCharPositionInLine(), "The right-hand side must be a collection. Got '" + t2.type() + "'");
        }
        SetType st = (SetType) t;
        if (!st.enclosingType().equals(t1.type())) {
            throw new SpecException(filename, to.getCharPositionInLine(),
                    "Type mismatch. Expected '" + st.enclosingType() + "' for left-hand side. Got '" + t1.type() + "'");

        }
    }

    @Override
    public Proposition visitTermComparison(@NotNull CstrSpecParser.TermComparisonContext c) {
        CstrSpecParser.ComparisonContext ctx = c.comparison();
        Term t1 = (Term) visit(ctx.t1);
        Term t2 = (Term) visit(ctx.t2);

        switch (ctx.op.getType()) {

            case CstrSpecParser.INCL:
                assertEqualsTypes(ctx.op, t1.type(), t2.type());
                return new Inc(t1, t2);
            case CstrSpecParser.NOT_INCL:
                assertEqualsTypes(ctx.op, t1.type(), t2.type());
                return new NInc(t1, t2);
            case CstrSpecParser.EQ:
                assertEqualsTypes(ctx.op, t1.type(), t2.type());
                return new Eq(t1, t2);
            case CstrSpecParser.NOT_EQ:
                assertEqualsTypes(ctx.op, t1.type(), t2.type());
                return new NEq(t1, t2);
            case CstrSpecParser.LT:
                assertEqualsTypes(ctx.op, IntType.getInstance(), t1.type(), t2.type());
                return new Lt(t1, t2);
            case CstrSpecParser.LEQ:
                assertEqualsTypes(ctx.op, IntType.getInstance(), t1.type(), t2.type());
                return new Leq(t1, t2);
            case CstrSpecParser.GT:
                assertEqualsTypes(ctx.op, IntType.getInstance(), t1.type(), t2.type());
                return new Lt(t2, t1);
            case CstrSpecParser.GEQ:
                assertEqualsTypes(ctx.op, IntType.getInstance(), t1.type(), t2.type());
                return new Leq(t2, t1);

            case CstrSpecParser.IN:
                assertIn(ctx.op, t1, t2);
                return new In(t1, t2);
            case CstrSpecParser.NOT_IN:
                assertIn(ctx.op, t1, t2);
                return new NIn(t1, t2);
            case CstrSpecParser.PART:
                assertIn(ctx.op, t2, t1);
                return new Packings(t1, t2);
            case CstrSpecParser.NOT_PART:
                assertIn(ctx.op, t2, t1);
                return new NoPackings(t1, t2);
        }
        throw SpecException.unsupportedOperation(filename, t1.type(), ctx.op, t2.type());
    }

    @Override
    public Term visitTermOp(@NotNull CstrSpecParser.TermOpContext ctx) {
        Term t1 = (Term) visit(ctx.t1);
        Term t2 = (Term) visit(ctx.t2);

        assertEqualsTypes(ctx.op, t1.type(), t2.type());
        switch (ctx.op.getType()) {
            case CstrSpecParser.PLUS:
                if (t1.type() == IntType.getInstance()) {
                    return new IntPlus(t1, t2);
                } else if (t1.type() instanceof SetType) {
                    return new SetPlus(t1, t2);
                }
                break;
            case CstrSpecParser.MINUS:
                if (t1.type() == IntType.getInstance()) {
                    return new IntMinus(t1, t2);
                } else if (t1.type() instanceof SetType) {
                    return new SetMinus(t1, t2);
                }
                break;
            case CstrSpecParser.MULT:
                return new Mult(t1, t2);
        }
        throw SpecException.unsupportedOperation(filename, t1.type(), ctx.op, t2.type());
    }
}