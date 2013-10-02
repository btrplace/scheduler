package btrplace.solver.api.cstrSpec.type;

import btrplace.solver.api.cstrSpec.Value;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class VM implements Type {

    private static VM instance = new VM();

    private Set<Value> values;

    private VM() {
        values = new HashSet<>();
    }

    public static VM getInstance() {
        return instance;
    }

    @Override
    public Set getPossibleValues() {
        return Collections.emptySet();
    }

    @Override
    public String toString() {
        return label();
    }

    @Override
    public boolean isIn(String n) {
        return false;
    }

    @Override
    public String label() {
        return "VM";
    }

    @Override
    public Value newValue(String n) {
        Value v = new Value(n, this);
        if (values.add(v)) {
            Node.getInstance().newValue("node4" + n);
            return v;
        }
        return null;
    }

}
