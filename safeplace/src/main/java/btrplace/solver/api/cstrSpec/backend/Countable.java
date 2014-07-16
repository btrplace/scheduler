package btrplace.solver.api.cstrSpec.backend;

/**
 * @author Fabien Hermenier
 */
public interface Countable extends VerificationBackend {

    int getNbCompliant();

    int getNbDefiant();

}
