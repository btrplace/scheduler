package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.type.Type;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public interface Term {


    Set<Value> domain();

    Type type();
}
