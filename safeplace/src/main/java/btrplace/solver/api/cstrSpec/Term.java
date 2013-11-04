package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;

/**
 * @author Fabien Hermenier
 */
public interface Term {


/*    Set<Value> domain();

    Type type();*/

    Object getValue(Model mo);
}
