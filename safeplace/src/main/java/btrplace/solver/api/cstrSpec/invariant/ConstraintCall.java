package btrplace.solver.api.cstrSpec.invariant;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.Constraint;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
    public Boolean evaluate(Model m) {
        List<Var> ps = c.getParameters();
        Map<String, Object> ins = new HashMap<>();
        for (int i = 0; i < args.size(); i++) {
            String lbl = ps.get(i).label();
            Object val = args.get(i).eval(m);
            ins.put(lbl, val);
        }
        return c.instantiate(ins, m);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(c.getConstraintName()).append('(');
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
