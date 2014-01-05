package btrplace.solver.api.cstrSpec.spec.type;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.spec.term.Constant;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public interface Type {

    Set domain(Model mo);

    String label();

    boolean match(String n);

    Constant newValue(String n);

    Type inside();
}
