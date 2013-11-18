package btrplace.solver.api.cstrSpec.invariant;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.invariant.type.Type;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Plus implements Term {

    private Term a, b;

    public Plus(Term t1, Term t2) {
        this.a = t1;
        this.b = t2;
    }

    @Override
    public Object getValue(Model mo) {
        Object o1 = a.getValue(mo);
        Object o2 = b.getValue(mo);
        if (o1 == null || o2 == null) {
            return null;
        }
        if (o1 instanceof Integer) {
            return (Integer) o1 + (Integer) o2;
        } else if (o1 instanceof Collection) {
            Collection c1 = (Collection) o1;
            Collection c2 = (Collection) o2;
            Set l = new HashSet(c1);
            l.addAll(c2);
            return new Value(l, a.type());
        }
        throw new RuntimeException("Unsupported operation on '" + o1.getClass().getSimpleName() + "'");
    }

    @Override
    public String toString() {
        return new StringBuilder(a.toString()).append(" + ").append(b.toString()).toString();
    }

    @Override
    public Type type() {
        return a.type();
    }
}
