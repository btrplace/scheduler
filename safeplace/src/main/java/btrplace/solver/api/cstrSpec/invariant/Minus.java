package btrplace.solver.api.cstrSpec.invariant;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.invariant.type.Type;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Minus implements Term {

    private Term a, b;

    public Minus(Term t1, Term t2) {
        if (!t1.type().equals(t2.type())) {
            throw new RuntimeException("Unconsistent substraction " + t1.type() + " + " + t2.type());
        }
        this.a = t1;
        this.b = t2;
    }

    @Override
    public Object eval(Model mo) {
        Object o1 = a.eval(mo);
        Object o2 = b.eval(mo);
        if (o1 == null || o2 == null) {
            return null;
        }
        if (o1 instanceof Integer) {
            return (Integer) o1 - (Integer) o2;
        } else if (o1 instanceof Collection) {
            Collection c1 = (Collection) o1;
            Collection c2 = (Collection) o2;
            Set l = new HashSet();
            for (Object o : c1) {
                if (!c2.contains(o)) {
                    l.add(o);
                }
            }
            return l;
        }
        throw new RuntimeException("Unsupported operation on '" + a.type() + "'");
    }

    @Override
    public String toString() {
        return new StringBuilder(a.toString()).append(" - ").append(b.toString()).toString();
    }

    @Override
    public Type type() {
        return a.type();
    }
}
