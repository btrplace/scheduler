package btrplace.solver.api.cstrSpec.type;

import btrplace.solver.api.cstrSpec.Value;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class NodeStateType implements Type {

    private static enum Type {online, offline}

    private static NodeStateType instance = new NodeStateType();

    private NodeStateType() {}

    public static NodeStateType getInstance() {
        return instance;
    }

    @Override
    public Set getPossibleValues() {
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
        return "nodeState";
    }

    @Override
    public Value newValue(String n) {
        return new Value(Type.valueOf(n), this);
    }

}
