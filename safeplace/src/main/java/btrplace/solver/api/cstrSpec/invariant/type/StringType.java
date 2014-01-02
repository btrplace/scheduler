package btrplace.solver.api.cstrSpec.invariant.type;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.invariant.Constant;

import java.util.Collections;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class StringType extends Atomic {

    private static StringType instance = new StringType();

    @Override
    public Set domain(Model mo) {
        return Collections.emptySet();
    }

    @Override
    public boolean match(String n) {
        return n.startsWith("\"") && n.endsWith("\"");
    }

    @Override
    public Constant newValue(String n) {
        if (match(n)) {
            return new Constant(n, this);
        }
        return null;
    }

    @Override
    public String label() {
        return "string";
    }

    @Override
    public String toString() {
        return label();
    }

    public static StringType getInstance() {
        return instance;
    }
}
