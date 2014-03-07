package btrplace.solver.api.cstrSpec.spec;

import btrplace.solver.api.cstrSpec.*;
import btrplace.solver.api.cstrSpec.spec.prop.*;
import btrplace.solver.api.cstrSpec.spec.term.*;
import btrplace.solver.api.cstrSpec.spec.term.func.Function;
import btrplace.solver.api.cstrSpec.spec.term.func.FunctionCall;
import btrplace.solver.api.cstrSpec.spec.term.func.ValueAt;
import btrplace.solver.api.cstrSpec.spec.type.*;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class MyCstrSpecVisitor extends CstrSpecBaseVisitor {

    private SymbolsTable symbols;

    private String filename;

    public MyCstrSpecVisitor(String fn) {
        filename = fn;
    }

    public Specification getSpecification(ParseTree t) {
        return (Specification) visit(t);
    }

    @Override
    public Specification visitSpec(@NotNull CstrSpecParser.SpecContext ctx) {
        symbols = SymbolsTable.newBundle();
        List<Constraint> cstrs = new ArrayList<>();
        for (CstrSpecParser.ConstraintContext c : ctx.constraint()) {
            symbols = symbols.enterSpec();
            Constraint x = visitConstraint(c);
            symbols = symbols.leaveScope();
            if (x != null) {
                cstrs.add(x);
                symbols.put(x);
            }
        }
        return new Specification(cstrs);
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

    @Override
    public Constraint visitConstraint(@NotNull CstrSpecParser.ConstraintContext ctx) {
        List<UserVar> params = new ArrayList<>();
        String cstrName = ctx.ID().getText();
        if (symbols.getConstraint(cstrName) != null) {
            report(ctx.ID().getSymbol(), "Constraint '" + cstrName + "' is already defined in the scope");
            return null;
        }

        boolean discrete = ctx.DISCRETE() != null;

        if (ctx.CORE() != null) {
            if (!ctx.typedef().isEmpty()) {
                report(ctx.typedef().get(0).start, "A core constraint cannot have parameters");
                return null;
            }
            Proposition p = (Proposition) visit(ctx.formula());
            return p == null ? null : Constraint.newCoreConstraint(cstrName, p, discrete);
        } else {
            for (CstrSpecParser.TypedefContext c : ctx.typedef()) {
                List<UserVar> vars = visitTypedef(c);
                if (vars == null) {
                    return null;
                }
                params.addAll(vars);
            }
            Proposition p = (Proposition) visit(ctx.formula());
            return p == null ? null : Constraint.newPluggableConstraint(cstrName, p, params, discrete);
        }
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
            report(ctx.ID().getSymbol(), "Cannot resolve symbol '" + id + "'");
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

    private List<Term> extractTerms(List<CstrSpecParser.TermContext> ctxs) {
        List<Term> terms = new ArrayList<>(ctxs.size());
        for (CstrSpecParser.TermContext ctx : ctxs) {
            Term t = (Term) visit(ctx);
            if (t == null) {
                return null;
            }
            terms.add(t);
        }
        return terms;
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

        Constraint ref = symbols.getConstraint(lbl);
        if (ref == null) {
            report(ctx.call().ID().getSymbol(), "Cannot resolve symbol '" + lbl + "'");
            return null;
        }
        if (ref.isCore()) {
            report(ctx.call().ID().getSymbol(), "Cannot call core constraint '" + lbl + "'");
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
            UserVar v = null;
            switch (ctx.op.getType()) {
                case CstrSpecParser.IN:
                    v = parent.newInclusive(lbl, false);
                    break;
                case CstrSpecParser.NOT_IN:
                    v = parent.newInclusive(lbl, true);
                    break;
                case CstrSpecParser.INCL:
                    v = parent.newPart(lbl, false);
                    break;
                case CstrSpecParser.NOT_INCL:
                    v = parent.newPart(lbl, true);
                    break;
            }
            symbols.put(v);
            vars.add(v);
            //System.out.println("declared: " + v.label() + " as " + v.type());
        }
        return vars;
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
        int l = t.getLine();
        int c = t.getCharPositionInLine();
        System.out.println("[" + filename + " " + l + ":" + c + "] " + msg);
    }

    @Override
    public Constant visitSetInExtension(@NotNull CstrSpecParser.SetInExtensionContext ctx) {
        Set<Object> s = new HashSet<>();
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
        return new Constant(s, new SetType(ty));
    }

    @Override
    public Term visitProtectedTerm(@NotNull CstrSpecParser.ProtectedTermContext ctx) {
        Term t = (Term) visit(ctx.term());
        return t == null ? null : new ProtectedTerm(t);
    }


    @Override
    public Object visitSetInComprehension(@NotNull CstrSpecParser.SetInComprehensionContext ctx) {
        //Get the binder
        List<UserVar> v = visitTypedef(ctx.typedef());
        if (v == null) {
            return null;
        }

        Proposition p = Proposition.True;
        if (ctx.COMMA() != null) {
            p = (Proposition) visit(ctx.formula());
        }
        if (p == null) {
            return null;
        }
        Term t = (Term) visit(ctx.term());
        if (t == null) {
            return null;
        }
        return new Constant(new LazySet(t, v.get(0), p), new SetType(t.type()));
    }

    @Override
    public Proposition visitProtectedFormula(@NotNull CstrSpecParser.ProtectedFormulaContext ctx) {
        Proposition p = (Proposition) visit(ctx.formula());
        return p == null ? null : new ProtectedProposition(p);
    }

    @Override
    public Proposition visitNot(@NotNull CstrSpecParser.NotContext ctx) {
        Proposition p = (Proposition) visit(ctx.formula());
        if (p == null) {
            return null;
        }
        return p.not();
    }

    @Override
    public Term visitArrayTerm(@NotNull CstrSpecParser.ArrayTermContext ctx) {
        String lbl = ctx.ID().getText();
        Var v = symbols.getVar(lbl);
        if (v == null) {
            report(ctx.ID().getSymbol(), "Unknown symbol '" + lbl + "'");
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
        if (VMStateType.getInstance().match(ref)) {
            return VMStateType.getInstance().newValue(ref);
        }

        if (NodeStateType.getInstance().match(ref)) {
            return NodeStateType.getInstance().newValue(ref);
        }

        if (NoneType.getInstance().match(ref)) {
            return None.instance();
        }
        report(ctx.ID().getSymbol(), "Unknown symbol '" + ctx.ID().getText() + "'");
        return null;
    }

    @Override
    public Constant visitIntTerm(@NotNull CstrSpecParser.IntTermContext ctx) {
        return IntType.getInstance().newValue(ctx.INT().getText());
    }

    @Override
    public Object visitStringTerm(@NotNull CstrSpecParser.StringTermContext ctx) {
        String txt = ctx.STRING().getText();
        return StringType.getInstance().newValue(txt.substring(1, txt.length()));
    }


    private boolean sameType(Token to, Term t1, Term t2) {
        if (!t1.type().comparable(t2.type())) {
//        if (!t1.type().equals(t2.type())) {
            report(to, "Incompatible types: expecting '" + t2.type() + " " + to.getText() + " " + t2.type() +
                    "' but was '" + t1.type() + " " + to.getText() + " " + t2.type() + "'");
            return false;
        }
        return true;
    }

    private boolean sameType(Token to, Term t1, Term t2, Type t) {
        //if (!t1.type().equals(t2.type()) || !t1.type().equals(t)) {
        if (!t1.type().comparable(t2.type())) {
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
