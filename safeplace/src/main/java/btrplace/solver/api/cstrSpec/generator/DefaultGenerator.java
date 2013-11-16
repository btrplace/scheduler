package btrplace.solver.api.cstrSpec.generator;

import java.util.Iterator;

/**
 * @author Fabien Hermenier
 */
public abstract class DefaultGenerator<T> implements Generator<T> {

    protected Generator tg;

    @Override
    public boolean hasNext() {
        return tg.hasNext();
    }

    public void reset() {
        tg.reset();
    }

    @Override
    public Iterator<T> iterator() {
        return this;
    }

    @Override
    public int count() {
        return tg.count();
    }

    @Override
    public int done() {
        return tg.done();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}

