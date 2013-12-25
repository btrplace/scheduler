package btrplace.solver.api.cstrSpec.invariant;

import btrplace.model.Model;

/**
 * @author Fabien Hermenier
 */
public class NEq extends AtomicProp {

    public NEq(Term a, Term b) {
        super(a, b);
    }

    @Override
    public String toString() {
        return new StringBuilder(a.toString()).append(" /= ").append(b).toString();
    }

    @Override
    public AtomicProp not() {
        return new Eq(a, b);
    }

    /*@Override
    public Or expand() {
        Or or = new Or();
        if (a.domain().size() > 1 && b.domain().size() > 1) {
           //func(i) : {1, 2} /= func(j) : {1,2} == (func(i) = 1 & func(j) = 2) || (func(i) = 2 & func(j) = 1)
           Constant[] domA = a.domain().toArray(new Constant[a.domain().size()]);
           Constant[] domB = b.domain().toArray(new Constant[b.domain().size()]);
           for (Constant i : domA) {
               for (Constant j : domB) {
                   if (!i.equals(j)) {
                        And and = new And().add(new Eq(a, i)).add(new Eq(b, j));
                        or.add(and);
                   }
               }
           }
        } else if (a.domain().size() == 1 || b.domain().size() == 1) {
            //func{1,2,3,4} /= {2} == (func() = 1 | func() = 3 | func() = 4)
            Term mult = a.domain().size() > 1 ? a : b;
            Term singleton = mult == a ? b : a;
            Constant v = singleton.domain().iterator().next();
            for (Constant i : mult.domain()) {
                if (!i.equals(v)) {
                    or.add(new Eq(mult, i));
                }
            }
        } else {
            //{1} /= {2}
            or.add(this);
        }
        return or;
    }      */

    @Override
    public Boolean evaluate(Model mo) {
        Object vA = a.eval(mo);
        Object vB = b.eval(mo);
        if ((vA == null && vB != null) || (vA != null && vB == null)) {
            return true;
        }
        if (vA == null && vB == null) {
            return true;
        }
        return !vA.equals(vB);
    }

}
