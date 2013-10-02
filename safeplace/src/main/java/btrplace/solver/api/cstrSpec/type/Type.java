package btrplace.solver.api.cstrSpec.type;

import btrplace.solver.api.cstrSpec.Value;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public interface Type {

    Set getPossibleValues();

    String label();

    boolean isIn(String n);

    Value newValue(String n);
}
