package btrplace.solver.api.cstrSpec.spec.prop;

import btrplace.solver.api.cstrSpec.spec.term.Term;
import btrplace.solver.api.cstrSpec.verification.specChecker.SpecModel;

import java.util.Collection;

/**
 * @author Fabien Hermenier
 */
public class NIn extends AtomicProp {

    public NIn(Term a, Term b) {
        super(a, b);
    }

    @Override
    public String toString() {
        return new StringBuilder(a.toString()).append(" /: ").append(b).toString();
    }

    @Override
    public AtomicProp not() {
        return new In(a, b);
    }

    @Override
    public Boolean eval(SpecModel m) {
        Object o = a.eval(m);
        Collection c = (Collection) b.eval(m);
        if (c == null) {
            return null;
        }
        return !c.contains(o);
    }

    /*@Override
    public Or expand() {
        Or or = new Or();
        if (!a.type().equals(((SetType)b.type()).subType())) {
            throw new RuntimeException();
        }
        //System.err.println("Expand " + a + " "+ a.domain() + " " + " :/ " + b + " " + b.domain());
        if (a.domain().size() > 1 && b.domain().size() > 1) {
            //func(i) : {1, 2} /= func(j) : {1,2} == (func(i) = 1 & func(j) = 2) || (func(i) = 2 & func(j) = 1)
            Constant[] domA = a.domain().toArray(new Constant[a.domain().size()]);
            Constant[] domB = b.domain().toArray(new Constant[b.domain().size()]);
            for (Constant i : domA) {
                Constant oI =  i;
                for (Constant j : domB) {
                    Set oJ = (Set) j.value();
                    if (!oJ.contains(oI)) {
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
    }         */

}
