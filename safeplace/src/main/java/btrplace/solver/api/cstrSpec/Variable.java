package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.type.Type;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Variable implements Term {

    private Type t;

    private String n;

    public Variable (String n, Type t) {
        this.t = t;
        this.n = n;
    }

    @Override
    public Set domain() {
        return t.getPossibleValues();
    }

    @Override
    public String toString() {
        return n;
    }

    public Type getType() {
        return t;
    }
}
