package btrplace.solver.api.cstrSpec.invariant;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.invariant.type.SetType;
import btrplace.solver.api.cstrSpec.invariant.type.Type;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Primitive extends Var<Set> {

    private Type type;

    private Set cnt;

    public Primitive(String name, Type enclosingType) {
        this(name, enclosingType, null);
    }

    public Primitive(String name, Type enclosingType, Set values) {
        super(name);
        this.type = new SetType(enclosingType);
        this.cnt = values;
    }

    @Override
    public boolean set(Set o) {
        cnt = o;
        return true;
    }

    @Override
    public void unset() {
        cnt = null;
    }

    @Override
    public Set eval(Model m) {
        return cnt;
    }

    @Override
    public Type type() {
        return type;
    }
}
