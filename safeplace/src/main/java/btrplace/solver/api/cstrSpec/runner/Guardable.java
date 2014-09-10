package btrplace.solver.api.cstrSpec.runner;

import btrplace.solver.api.cstrSpec.guard.Guard;

/**
 * @author Fabien Hermenier
 */
public interface Guardable {

    Guardable limit(Guard l);
}
