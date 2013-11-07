package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.type.Type;

/**
 * @author Fabien Hermenier
 */
public interface Term {


/*    Set<Value> domain();

    Type type();*/

    Object getValue(Model mo);

    Type type();
}
