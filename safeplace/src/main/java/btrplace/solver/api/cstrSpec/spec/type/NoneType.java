package btrplace.solver.api.cstrSpec.spec.type;

import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.spec.term.None;

/**
 * @author Fabien Hermenier
 */
public class NoneType implements Type {

    private static NoneType i = new NoneType();

    public static NoneType getInstance() {
        return i;
    }

    private NoneType() {

    }

    @Override
    public String label() {
        return "none";
    }

    @Override
    public boolean match(String n) {
        return n.equals(None.instance().toString());
    }

    @Override
    public Constant newValue(String n) {
        return null;
    }

    @Override
    public Type inside() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean comparable(Type t) {
        return true;
    }

}
