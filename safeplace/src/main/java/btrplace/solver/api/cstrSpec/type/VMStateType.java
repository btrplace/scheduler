package btrplace.solver.api.cstrSpec.type;

import btrplace.solver.api.cstrSpec.Value;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class VMStateType implements Type {

    private static enum Type { running, ready, sleeping, waiting}

    private static VMStateType instance = new VMStateType();

    private VMStateType() {}

    public static VMStateType getInstance() {
        return instance;
    }

    @Override
    public Set<Value> domain() {
        Set s = new HashSet();
        for (Type t : Type.values()) {
            s.add(new Value(t, this));
        }
        return s;
    }

    @Override
    public String toString() {
        return label();
    }

    @Override
    public boolean isIn(String n) {
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
    public Value newValue(String n) {
        return new Value(Type.valueOf(n), this);
    }



}
