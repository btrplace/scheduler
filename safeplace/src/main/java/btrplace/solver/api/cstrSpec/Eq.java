package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.func.Function;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Eq extends AtomicProp {

    public Eq(Term a, Term b) {
        super(a, b);
    }

    @Override
    public String toString() {
        return new StringBuilder(a.toString()).append(" = ").append(b).toString();
    }

    @Override
    public AtomicProp not() {
        return new NEq(a, b);
    }

    @Override
    public Or expand() {
        Or or = new Or();
        if (a.domain().size() > 1 && b.domain().size() > 1) {
            /* {1,2} ={1,2} == {1} = {1} | {2} = {2} */
            for (Value i : a.domain()) {
                for (Value j : b.domain()) {
                    if (i.equals(j)) {
                        or.add(new Eq(a, j)).add(new Eq(b, i));
                    }
                }
            }
        } else {
            or.add(this);
        }
        System.err.println(this + " => " + or);
        return or;
        /*Function f = a instanceof Function ? (Function) a : (Function) b;
        Set<Value> avoid = a instanceof Function ? b.domain() : a.domain();
        Or o = new Or();
        if (avoid.size() == 1) {
            Value v = avoid.iterator().next();
            for (Value allowed : f.domain()) {
                if (!allowed.equals(v)) {
                    o.add(new Eq(f, allowed));
                }
            }
        } else {
            o.add(this);
        }
        return o;   */
    }
}
