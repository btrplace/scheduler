package btrplace.solver.api.cstrSpec.spec.term;

import btrplace.solver.api.cstrSpec.verification.specChecker.SpecModel;

/**
 * @author Fabien Hermenier
 */
public class IntPlus extends Plus<Integer> {


    public IntPlus(Term<Integer> t1, Term<Integer> t2) {
        super(t1, t2);
    }

    @Override
    public Integer eval(SpecModel mo) {
        Integer o1 = a.eval(mo);
        Integer o2 = b.eval(mo);
        return o1 + o2;
    }
}
