package btrplace.solver.api.cstrSpec.spec.type;

import btrplace.solver.api.cstrSpec.spec.term.Constant;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class BoolType extends Atomic {

    public static final Set<Boolean> DOMAIN = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(Boolean.TRUE, Boolean.FALSE)));

    private static BoolType instance = new BoolType();

    private BoolType() {
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
