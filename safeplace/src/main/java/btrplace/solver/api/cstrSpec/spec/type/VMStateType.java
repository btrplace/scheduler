package btrplace.solver.api.cstrSpec.spec.type;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.spec.term.Constant;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class VMStateType extends Atomic {

    public static enum Type {running, ready, sleeping, waiting, terminated}

    private static VMStateType instance = new VMStateType();

    private Set<Object> vals;

    private VMStateType() {
        Set<Object> s = new HashSet<>();
        Collections.addAll(s, Type.values());
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
            Type.valueOf(n);
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
    public Constant newValue(String n) {
        return new Constant(Type.valueOf(n), this);
    }

}
