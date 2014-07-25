package btrplace.solver.api.cstrSpec.spec.term;

import btrplace.solver.api.cstrSpec.spec.type.SetType;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Primitive extends Var<Set> {

    private Type type;


    private Primitive(String name, Type enclosingType, Set c) {
        super(name);
        type = new SetType(enclosingType);
    }

    public Primitive(String name, Type enclosingType) {
        this(name, enclosingType, null);
    }

    @Override
    public Set eval(SpecModel mo) {
        Set dom = mo.getVerifDomain(label());
        if (dom == null) {
            throw new UnsupportedOperationException("No domain has been set for primitive '" + label() + "'");
        }
        return dom;
    }

    @Override
    public Type type() {
        return type;
    }
}
