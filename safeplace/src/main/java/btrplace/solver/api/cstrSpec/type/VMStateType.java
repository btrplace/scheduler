package btrplace.solver.api.cstrSpec.type;

import btrplace.solver.api.cstrSpec.Value;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class VMStateType implements Type {

    private static enum Type {running, ready, sleeping, waiting}

    private static VMStateType instance = new VMStateType();

    private Set<Value> vals;

    private VMStateType() {
        Set<Value> s = new HashSet<>();
        for (Type t : Type.values()) {
            s.add(new Value(t, this));
        }
        vals = Collections.unmodifiableSet(s);
    }

    public static VMStateType getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return label();
    }

    @Override
    public boolean match(String n) {
        try {
            Type t = Type.valueOf(n);
            return true;
        } catch (Exception e) {

        }
        return false;
    }

    @Override
    public String label() {
        return "vmState";
    }


    @Override
    public Set<Value> domain() {
        return vals;
    }

    @Override
    public Value newValue(String n) {
        return new Value(Type.valueOf(n), this);
    }

}
