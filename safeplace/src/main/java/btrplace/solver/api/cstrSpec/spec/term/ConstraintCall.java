package btrplace.solver.api.cstrSpec.spec.term;

import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.prop.Not;
import btrplace.solver.api.cstrSpec.spec.prop.Proposition;
import btrplace.solver.api.cstrSpec.spec.term.func.Function;
import btrplace.solver.api.cstrSpec.spec.term.func.FunctionCall;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class ConstraintCall implements Proposition {

    private Constraint c;

    private List<Term> args;

    public ConstraintCall(Constraint c, List<Term> args) {
        this.c = c;
        this.args = args;
        //TODO: check(c, args);
    }

    @Override
    public Proposition not() {
        return new Not(this);
    }

    @Override
    public Boolean eval(SpecModel m) {
        List<UserVar> ps = c.getParameters();
        List<Object> ins = new ArrayList<>(ps.size());
        for (Term t : args) {
            ins.add(t.eval(m));
        }
        return c.eval(m, ins);
    }

    private static void check(Function f, List<Term> args) {
        Type[] expected = f.signature(args);
        if (expected.length != args.size()) {
            throw new IllegalArgumentException(FunctionCall.toString(f.id(), args) + " cannot match " + f);
        }
        for (int i = 0; i < expected.length; i++) {
            if (!expected[i].equals(args.get(i).type())) {
                throw new IllegalArgumentException(FunctionCall.toString(f.id(), args) + " cannot match " + f);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(c.id()).append('(');
        Iterator<Term> ite = args.iterator();
        while (ite.hasNext()) {
            b.append(ite.next().toString());
            if (ite.hasNext()) {
                b.append(", ");
            }
        }
        return b.append(')').toString();
    }
}
