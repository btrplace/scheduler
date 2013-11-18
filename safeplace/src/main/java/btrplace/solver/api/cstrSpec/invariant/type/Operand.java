package btrplace.solver.api.cstrSpec.invariant.type;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public interface Operand {

    Type type();

    Set domain();
}
