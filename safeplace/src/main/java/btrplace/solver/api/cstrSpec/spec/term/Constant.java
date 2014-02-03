package btrplace.solver.api.cstrSpec.spec.term;

import btrplace.solver.api.cstrSpec.LazySet;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.verification.specChecker.SpecModel;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class Constant extends Term {

    private Type t;

    private Object o;

    public Constant(Object o, Type t) {
        this.t = t;
        this.o = o;
    }

    public Type type() {
        return t;
    }

    //@Override
    public Set<Constant> domain() {
        return Collections.singleton(this);
    }

    @Override
    public String toString() {
        if (o instanceof Collection) {
            StringBuilder b = new StringBuilder("{");
            Iterator ite = ((Collection) o).iterator();
            if (ite.hasNext()) {
                b.append(ite.next().toString());
            }
            while (ite.hasNext()) {
                b.append(", ").append(ite.next());
            }
            b.append('}');
            return b.toString();
        }
        return o.toString();
    }

    @Override
    public boolean equals(Object o1) {
        if (this == o1) return true;
        if (!(o1 instanceof Constant)) return false;

        Constant value = (Constant) o1;

        return (o.equals(value.o) && t.equals(value.t));
    }

    @Override
    public int hashCode() {
        return Objects.hash(t, o);
    }

    @Override
    public Object eval(SpecModel mo) {
        if (o instanceof LazySet) {
            return ((LazySet) o).expand(mo);
        }
        return o;
    }
}
