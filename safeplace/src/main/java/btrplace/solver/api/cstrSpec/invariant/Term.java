package btrplace.solver.api.cstrSpec.invariant;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.invariant.type.Type;

/**
 * @author Fabien Hermenier
 */
public interface Term<T> {

    T eval(Model mo);

    Type type();
}
