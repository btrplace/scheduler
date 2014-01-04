package btrplace.solver.api.cstrSpec.invariant;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.Constraint;

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
