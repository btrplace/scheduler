package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Minus implements Term {

    private Term a, b;

    public Minus(Term a, Term b) {
        this.a = a;
        this.b = b;
    }
    @Override
    public Object getValue(Model mo) {
        Collection<?> o1 = (Collection) a.getValue(mo);
        Collection<?> o2 = (Collection) b.getValue(mo);
        List<Object> l = new ArrayList<>();
        for (Object o : o1) {
            if (!o2.contains(o)) {
                l.add(o);
            }
        }
        return l;
    }
}
