package btrplace.solver.api.cstrSpec.invariant.type;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.invariant.Constant;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class BoolType extends Atomic {

    private Set<Boolean> dom;

    private static BoolType instance = new BoolType();

    private BoolType() {
        dom = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(Boolean.TRUE, Boolean.FALSE)));
    }

    @Override
    public Set<Boolean> domain(Model mo) {
        return dom;
    }

    public static BoolType getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return label();
    }

    @Override
    public boolean match(String n) {
        try {
            Boolean.parseBoolean(n);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public String label() {
        return "bool";
    }

    @Override
    public Constant newValue(String n) {
        return new Constant(Boolean.parseBoolean(n), BoolType.getInstance());
    }

    public Constant newValue(boolean i) {
        return new Constant(i, BoolType.getInstance());
    }
}
