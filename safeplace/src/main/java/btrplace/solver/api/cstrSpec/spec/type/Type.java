package btrplace.solver.api.cstrSpec.spec.type;

import btrplace.solver.api.cstrSpec.spec.term.Constant;

/**
 * @author Fabien Hermenier
 */
public interface Type {

    String label();

    boolean match(String n);

    Constant newValue(String n);

    Type inside();
}
