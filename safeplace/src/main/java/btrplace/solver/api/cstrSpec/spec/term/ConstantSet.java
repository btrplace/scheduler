package btrplace.solver.api.cstrSpec.spec.term;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.spec.type.Type;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class ConstantSet extends Primitive {

    private Set cnt;

    public ConstantSet(String lbl, Type t, Set cnt) {
        super(lbl, t);
        this.cnt = cnt;
    }

    @Override
    public Set eval(Model m) {
        return cnt;
    }
}
