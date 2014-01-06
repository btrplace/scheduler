package btrplace.solver.api.cstrSpec.util;

import java.util.Iterator;

/**
 * @author Fabien Hermenier
 */
public interface Generator<T> extends Iterator<T>, Iterable<T> {

    int count();

    int done();

    void reset();
}
