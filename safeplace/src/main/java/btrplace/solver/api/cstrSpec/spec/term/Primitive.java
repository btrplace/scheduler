package btrplace.solver.api.cstrSpec.spec.term;

import btrplace.solver.api.cstrSpec.spec.type.SetType;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;
import btrplace.solver.api.cstrSpec.verification.spec.VerifDomain;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Primitive extends Var<Set> {

    private Type type;

    private VerifDomain vd;

    private Primitive(String name, Type enclosingType, Set c) {
        super(name);
        type = new SetType(enclosingType);
    }

    public void setVerifDomain(VerifDomain v) {
        vd = v;
    }

    public VerifDomain getVerifDomain() {
        return vd;
    }

    public Primitive(String name, Type enclosingType) {
        this(name, enclosingType, null);
    }

    @Override
    public Set eval(SpecModel mo) {
        if (vd == null) {
            throw new UnsupportedOperationException("No domain has been set for primitive '" + label() + "'");
        }
        Set s = vd.domain();
        if (s == null) {
            throw new UnsupportedOperationException("No domain has been set for primitive '" + label() + "'");
        }
        return s;
    }

    @Override
    public Type type() {
        return type;
    }
}
