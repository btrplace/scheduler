package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;

import java.util.List;

/**
 * A logical proposition.
 *
 * @author Fabien Hermenier
 */
public interface Proposition {

    Proposition not();

    int size();

    boolean inject(Model mo);

    Proposition expand();

}
