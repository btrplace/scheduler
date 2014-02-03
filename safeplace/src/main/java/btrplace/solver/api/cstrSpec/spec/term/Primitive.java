package btrplace.solver.api.cstrSpec.spec.term;

import btrplace.solver.api.cstrSpec.spec.type.SetType;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public abstract class Primitive extends Var<Set> {

    private Type type;

    protected Set cnt;

    public Primitive(String name, Type enclosingType) {
        super(name);
        type = new SetType(enclosingType);
    }

    @Override
    public abstract Set eval(SpecModel m);

    @Override
    public Type type() {
        return type;
    }
}
