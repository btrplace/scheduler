package btrplace.solver.api.cstrSpec.spec.term;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.prop.Not;
import btrplace.solver.api.cstrSpec.spec.prop.Proposition;
import btrplace.solver.api.cstrSpec.spec.term.func.Function;
import btrplace.solver.api.cstrSpec.spec.type.Type;

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
    public Boolean eval(Model m) {
        List<Var> ps = c.getParameters();
        List<Object> ins = new ArrayList<>(ps.size());
        for (int i = 0; i < args.size(); i++) {
            ins.add(args.get(i).eval(m));
        }
        return c.eval(m, ins);
    }

    private static void check(Function f, List<Term> args) {
        Type[] expected = f.signature();
        if (expected.length != args.size()) {
            throw new IllegalArgumentException(toString(f.id(), args) + " cannot match " + f);
        }
        for (int i = 0; i < expected.length; i++) {
            if (!expected[i].equals(args.get(i).type())) {
                throw new IllegalArgumentException(toString(f.id(), args) + " cannot match " + f);
            }
        }
    }

    private static String toString(String id, List<Term> args) {
        StringBuilder b = new StringBuilder(id);
        b.append('(');
        Iterator<Term> ite = args.iterator();
        while (ite.hasNext()) {
            b.append(ite.next().type());
            if (ite.hasNext()) {
                b.append(", ");
            }
        }
        return b.append(')').toString();
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
