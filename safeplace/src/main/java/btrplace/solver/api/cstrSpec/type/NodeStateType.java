package btrplace.solver.api.cstrSpec.type;

import btrplace.solver.api.cstrSpec.Value;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class NodeStateType implements Type {

    public static enum Type {online, offline}

    private static NodeStateType instance = new NodeStateType();

    private Set<Value> vals;

    private NodeStateType() {
        Set<Value> s = new HashSet<>();
        for (Type t : Type.values()) {
            s.add(new Value(t, this));
        }
        vals = Collections.unmodifiableSet(s);
    }

    public static NodeStateType getInstance() {
        return instance;
    }

    @Override
    public Set<Value> domain() {
        return vals;
    }

    @Override
    public String toString() {
        return label();
    }

    @Override
    public boolean match(String n) {
        try {
            Type.valueOf(n);
            return true;
        } catch (Exception e) {

        }
        return false;
    }

    @Override
    public String label() {
        return "nodeState";
    }

    @Override
    public Value newValue(String n) {
        return new Value(Type.valueOf(n), this);
    }

}
