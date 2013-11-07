package btrplace.solver.api.cstrSpec.type;

import btrplace.model.VM;
import btrplace.solver.api.cstrSpec.Value;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class VMType implements Type {

    private static VMType instance = new VMType();

    private Set<VM> values;

    private VMType() {
        Set<VM> s = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            s.add(new VM(i));
        }

        values = Collections.unmodifiableSet(s);
    }

    public static VMType getInstance() {
        return instance;
    }

    @Override
    public Set<VM> domain() {
        return values;
    }

    @Override
    public String toString() {
        return label();
    }

    @Override
    public boolean match(String n) {
        return false;
    }

    @Override
    public String label() {
        return "VM";
    }


    @Override
    public Value newValue(String n) {
        throw new RuntimeException();
    }

}
