package btrplace.solver.api.cstrSpec.spec.term.func;

import btrplace.solver.api.cstrSpec.spec.term.Term;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.List;

/**
 * Get the value at a given index of a list.
 *
 * @author Fabien Hermenier
 */
public class ValueAt extends Term {

    private Term<List> arr;
    private Term<Integer> idx;

    public ValueAt(Term<List> arr, Term<Integer> idx) {
        this.arr = arr;
        this.idx = idx;
    }

    @Override
    public Type type() {
        return arr.type().inside();
    }

    @Override
    public Object eval(SpecModel mo) {
        List l = arr.eval(mo);
        if (l == null) {
            return null;
        }
        Integer i = idx.eval(mo);
        if (i == null) {
            return null;
        }
        return l.get(i);
    }

    @Override
    public String toString() {
        return arr.toString() + "[" + idx.toString() + "]";
    }
}
