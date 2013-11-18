package btrplace.solver.api.cstrSpec.invariant.type;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.invariant.Value;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class VMStateType implements Type {

    public static enum Type {running, ready, sleeping, waiting}

    private static VMStateType instance = new VMStateType();

    private Set<Object> vals;

    private VMStateType() {
        Set<Object> s = new HashSet<>();
        for (Type t : Type.values()) {
            s.add(t);
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
    public Set<Object> domain(Model mo) {
        return vals;
    }

    @Override
    public Value newValue(String n) {
        return new Value(Type.valueOf(n), this);
    }

}
