package btrplace.solver.api.cstrSpec.spec.term;

import btrplace.solver.api.cstrSpec.spec.type.NoneType;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.verification.specChecker.SpecModel;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class None extends Primitive {

    private static None instance = new None();

    public None() {
        super(null, NoneType.getInstance());
    }

    @Override
    public Set eval(SpecModel m) {
        return null;
    }

    public static None instance() {
        return instance;
    }

    @Override
    public UserVar newInclusive(String n, boolean not) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UserVar<Set> newPart(String n, boolean not) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Type type() {
        return NoneType.getInstance();
    }

    @Override
    public String label() {
        return type().label();
    }
}
