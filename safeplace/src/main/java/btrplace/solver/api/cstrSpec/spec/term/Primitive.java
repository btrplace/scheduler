package btrplace.solver.api.cstrSpec.spec.term;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.spec.type.SetType;
import btrplace.solver.api.cstrSpec.spec.type.Type;

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
    public abstract Set eval(Model m);

    @Override
    public Type type() {
        return type;
    }
}
