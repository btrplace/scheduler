package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.func.Function;
import btrplace.solver.api.cstrSpec.type.Type;
import btrplace.solver.api.cstrSpec.type.Types;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class Satisfy extends CstrSpecBaseListener {

    private Functions funcs;

    private Deque<Term> stack;

    private Deque<Proposition> propositions;

    private SymbolsTable syms;

    private Types types;

    public Satisfy() {
        stack = new ArrayDeque<>();
        propositions = new ArrayDeque<>();
        funcs = new Functions(this);
        syms = new SymbolsTable();
        types = new Types();
    }
    @Override
    public void exitFunc(@NotNull CstrSpecParser.FuncContext ctx) {
        //get function name
        Function f = funcs.get(ctx.ID().getText());
        push(f);
    }

    @Override
    public void enterTerm(@NotNull CstrSpecParser.TermContext ctx) {
    }

    @Override
    public void exitTerm(@NotNull CstrSpecParser.TermContext ctx) {
        if (ctx.ID() != null) {
            //Variable or value
            if (!syms.isDeclared(ctx.ID().getText())) {
                Type t = types.getTypeFromValue(ctx.ID().getText());
                Value v = t.newValue(ctx.ID().getText());
                push(v);
            } else {
                push(syms.get(ctx.ID().getText()));
            }

        } else if (ctx.NAT() != null) {
            push(new Nat(Integer.parseInt(ctx.NAT().getText())));
        }
    }

    @Override
    public void exitTypedef(@NotNull CstrSpecParser.TypedefContext ctx) {
        String op = ctx.getChild(1).getText();
        if (!syms.def(ctx.ID(0).getText(), types.getTypeFromLabel(ctx.ID(1).getText()))) {
            throw new RuntimeException("Unable to declare " + ctx.ID());
        }
    }

    public void push(Term t) {
        //System.err.println("Push " + t);
        stack.push(t);
    }

    public Term pop() {
        Term t = stack.pop();
        //System.err.println("Pop " + t);
        return t;
    }
    @Override
    public void exitFormula(@NotNull CstrSpecParser.FormulaContext ctx) {
        if (ctx.getChild(1) == ctx.IMPLIES()) {
            Or o = new Or().add(propositions.pop().not()).add(propositions.pop());
            propositions.push(o);
        } else if (ctx.getChild(1) == ctx.AND()) {
            And p = new And().add(propositions.pop()).add(propositions.pop());
            propositions.push(p);
        } else if (ctx.getChild(1) == ctx.OR()) {
            Or p = new Or();
            p.add(propositions.pop());
            p.add(propositions.pop());
            propositions.push(p);
        } else if (ctx.getChild(1) == ctx.EQ()) {
            Term t2 = pop();
            Eq p =  new Eq(pop(), t2);
            propositions.add(p);
        } else if (ctx.getChild(1) == ctx.NOT_EQ()) {
            NEq p =  new NEq(pop(), pop());
            propositions.add(p);
        }
    }

    @Override
    public void exitBinder(@NotNull CstrSpecParser.BinderContext ctx) {
        String right = ctx.term().getText();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            TerminalNode tn = ctx.ID(i);
            if (tn == null) {
                break;
            }
            String n = tn.getText();
            Type t = null;
            if (syms.isDeclared(right)) { //It's a variable
                t = syms.get(right).getType();
            } else {     //It's a regular type
                t = types.getTypeFromLabel(right);
            }
            if (!syms.def(n, t)) {
                throw new RuntimeException(n + " is already defined");
            }

        }
    }

    @Override
    public void exitSpec(@NotNull CstrSpecParser.SpecContext ctx) {
    }

    public Proposition getInvariant() {
        //System.err.println(propositions);
        return propositions.getFirst();
    }
}
