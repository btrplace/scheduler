package btrplace.solver.api.cstrSpec.func;

import btrplace.solver.api.cstrSpec.AtomicProp;
import btrplace.solver.api.cstrSpec.Term;
import btrplace.solver.api.cstrSpec.type.Type;

/**
 * @author Fabien Hermenier
 */
public interface Function<E extends Type> extends Term {

    void eval();

}
