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
    public Set domain() {
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

    int nb = 1;
    @Override
    public Value newValue(String n) {
        Value v = new Value("VM" + (nb++), this);
        if (values.add(v)) {
            //System.err.println("New VM " + v);
            Node.getInstance().newValue(n);
            return v;
        }
        return null;
    }

}
