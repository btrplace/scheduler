package btrplace.solver.api.cstrSpec.verification.spec;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public interface VerifDomain<T> {
    Set<T> domain();

    String type();
}
