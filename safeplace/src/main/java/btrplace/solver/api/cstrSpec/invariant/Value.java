package btrplace.solver.api.cstrSpec.invariant;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.LazySet;
import btrplace.solver.api.cstrSpec.invariant.type.Type;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class Value implements Term {

    private Type t;

    private Object o;

    public Value(Object o, Type t) {
        this.t = t;
        this.o = o;
    }

    public Type type() {
        return t;
    }

    //@Override
    public Set<Value> domain() {
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
        if (!(o1 instanceof Value)) return false;

        Value value = (Value) o1;

        if (!o.equals(value.o)) return false;
        if (!t.equals(value.t)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(t, o);
    }

    @Override
    public Object getValue(Model mo) {
        if (o instanceof LazySet) {
            return ((LazySet) o).expand(mo);
        }
        return o;
    }
}
