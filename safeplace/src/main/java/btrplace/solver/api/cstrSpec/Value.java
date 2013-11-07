package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.type.Type;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

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
        return o;
    }

    @Override
    public Term plus(Term t2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Term minus(Term t2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Term mult(Term t2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Term div(Term t2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Term inter(Term t2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Term union(Term t2) {
        throw new UnsupportedOperationException();
    }

    public Value lt(Value b) {
        throw new UnsupportedOperationException();
    }

    public Value eq(Value b) {
        throw new UnsupportedOperationException();
    }

    public Value leq(Value b) {
        throw new UnsupportedOperationException();
    }

}
