package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.func.Forall;
import btrplace.solver.api.cstrSpec.func.Function;
import btrplace.solver.api.cstrSpec.type.NatType;
import btrplace.solver.api.cstrSpec.type.Primitives;
import btrplace.solver.api.cstrSpec.type.Type;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class MyListener extends CstrSpecBaseListener {

    private Functions funcs;

    private Deque<Term> stack;

    private Deque<Proposition> propositions;

    private SymbolsTable syms;

    private Primitives primitives;

    private List<Variable> params;

    private String cstrName;

    private List<Forall> binders;

    public MyListener(StatesExtractor ex) {
        stack = new ArrayDeque<>();
        propositions = new ArrayDeque<>();
        funcs = ex.getFunctions();
        syms = new SymbolsTable();
        primitives = new Primitives();
    }

    @Override
    public void enterConstraint(@NotNull CstrSpecParser.ConstraintContext ctx) {
        params = new ArrayList<>();
        cstrName = ctx.ID().getText();
        binders = new ArrayList<>();
    }

    public List<Variable> getVariables() {
        return params;
    }

    /*public List<List<Value>> expandParameters() {
        Value [][] doms = new Value[params.size()][];
        int [] indexes = new int[params.size()];
        int i = 0;
        int nbStates = 1;
        List<List<Value>> all = new ArrayList<>();
        for (Variable v : params) {
            indexes[i] = 0;
            Set<Object> sDom = v.domain();
            doms[i] = sDom.toArray(new Value[sDom.size()]);
            nbStates *= doms[i].length;
            i++;
        }
        for (int k = 0; k < nbStates; k++) {
            List<Value> entries = new ArrayList<>();
            for (int x = 0; x < params.size(); x++) {
                entries.add(doms[x][indexes[x]]);
            }
            for (int x = 0; x < params.size(); x++) {
                indexes[x]++;
                if (indexes[x] < doms[x].length) {
                    break;
                }
                indexes[x] = 0;
            }
            all.add(entries);
        }
        return all;
    }       */

    public String getConstraintName() {
        return cstrName;
    }

/*    @Override
    public void exitConstraint(@NotNull CstrSpecParser.ConstraintContext ctx) {


        Variable [] vars = params.toArray(new Variable[params.size()]);
        Value [][] doms = new Value[vars.length][];
        int [] indexes = new int[vars.length];
        int i = 0;
        int nbStates = 1;
        for (Variable v : vars) {
            indexes[i] = 0;
            Set<Value> sDom = v.domain();
            doms[i] = sDom.toArray(new Value[sDom.size()]);
            nbStates *= doms[i].length;
            i++;
        }
        for (int k = 0; k < nbStates; k++) {
            List<Value> entries = new ArrayList<>();
            for (int x = 0; x < vars.length; x++) {
                entries.add(doms[x][indexes[x]]);
            }
            for (int x = 0; x < vars.length; x++) {
                indexes[x]++;
                if (indexes[x] < vars[x].domain().size()) {
                    break;
                }
                indexes[x] = 0;
            }
            System.err.println(name + "(" + entries + ")");
        }
    }      */

    @Override
    public void exitFunc(@NotNull CstrSpecParser.FuncContext ctx) {
        //get function name
        String id = ctx.ID().getText();
        Function f = funcs.get(id, stack);
        push(f);
    }

    @Override
    public void enterTerm(@NotNull CstrSpecParser.TermContext ctx) {

    }

    @Override
    public void enterSet(@NotNull CstrSpecParser.SetContext ctx) {

    }

    @Override
    public void exitSet(@NotNull CstrSpecParser.SetContext ctx) {

    }

    private boolean inBinder = false;

    @Override
    public void enterBinder(@NotNull CstrSpecParser.BinderContext ctx) {
        inBinder = true;
    }

    @Override
    public void exitTerm(@NotNull CstrSpecParser.TermContext ctx) {
        if (ctx.ID() != null) {
            String lbl = ctx.ID().getText();
            Variable var = syms.get(lbl);
            if (var != null) {
                push(var);
            } else {
                Type t = primitives.fromValue(lbl);
                if (t == null) {
                    throw new RuntimeException("Cannot resolve symbol '" + lbl + "'");
                }
                Value v = t.newValue(lbl);
                push(v);
            }
        } else if (ctx.NAT() != null) {
            push(NatType.getInstance().newValue(ctx.NAT().getText()));
        }
    }

    @Override
    public void exitTypedef(@NotNull CstrSpecParser.TypedefContext ctx) {
        Variable v = syms.get(ctx.ID(1).getText());
        Type t = v.type();
        String id = ctx.ID(0).getText();

        params.add(syms.newVariable(id, ctx.getChild(1).getText(), t));
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
        } else if (ctx.getChild(1) == ctx.NOT_IN()) {
            Term t2 = pop();
            NIn p =  new NIn(pop(), t2);
            propositions.add(p);
        } else if (ctx.getChild(1) == ctx.IN()) {
            Term t2 = pop();
            In p =  new In(pop(), t2);
            propositions.add(p);
        } else if (ctx.getChild(1) == ctx.INCL()) {
            Term t2 = pop();
            Inc p =  new Inc(pop(), t2);
            propositions.add(p);
        } else if (ctx.getChild(1) == ctx.NOT_INCL()) {
            Term t2 = pop();
            NInc p =  new NInc(pop(), t2);
            propositions.add(p);
        } else if (ctx.getChild(1) == ctx.IFF()) {
            Proposition b = propositions.pop();
            Proposition a = propositions.pop();
            And a1 = new And().add(a).add(b);
            And a2 = new And().add(a.not()).add(b.not());
            propositions.add(new Or().add(a1).add(a2));
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
            Type t;
            String n = tn.getText();
            if (syms.get(right) != null) { //It's a variable
                t = syms.get(right).type();
            } else {
                t = primitives.fromValue(right);
                if (t == null) {
                    throw new RuntimeException("Cannot resolve symbol '" + right + "'");
                }
                Value v = t.newValue(right);
                push(v);
            }

            //The new type depends on the operator:
            Variable v = syms.newVariable(n, ctx.getChild(ctx.getChildCount() - 4).getText(), t);
            if (ctx.ALL() != null) {
                Forall f = new Forall(v);
                binders.add(f);
            }
        }
        inBinder = false;
    }

    @Override
    public void exitSpec(@NotNull CstrSpecParser.SpecContext ctx) {

    }

    public Proposition getStates() throws Exception {
        return propositions.getFirst();
/*        System.err.println(propositions.getFirst());
        System.err.println(propositions.getFirst().expand());*/
    }

    public List<Forall> getBinders() {
        return binders;
    }
}
