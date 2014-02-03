package btrplace.solver.api.cstrSpec.spec.type;

import btrplace.solver.api.cstrSpec.spec.term.Constant;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class NodeStateType extends Atomic {

    public static enum Type {online, booting, halting, offline}

    private static NodeStateType instance = new NodeStateType();

    private NodeStateType() {
        Set<Constant> s = new HashSet<>();
        for (Type t : Type.values()) {
            s.add(new Constant(t, this));
        }
    }

    public static NodeStateType getInstance() {
        return instance;
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
    public Constant newValue(String n) {
        return new Constant(Type.valueOf(n), this);
    }

    @Override
    public boolean comparable(btrplace.solver.api.cstrSpec.spec.type.Type t) {
        return t.equals(NoneType.getInstance()) || equals(t);
    }

}
