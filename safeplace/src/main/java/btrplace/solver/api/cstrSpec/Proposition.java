package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;

/**
 * A logical proposition.
 *
 * @author Fabien Hermenier
 */
public interface Proposition {

    Proposition not();

    int size();

    //Proposition expand();

    Boolean evaluate(Model m);
}
