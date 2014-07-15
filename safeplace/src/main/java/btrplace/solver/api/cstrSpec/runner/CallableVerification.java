package btrplace.solver.api.cstrSpec.runner;

import java.util.concurrent.Callable;

/**
 * @author Fabien Hermenier
 */
public interface CallableVerification extends Callable<Boolean> {

    void stop();
}
