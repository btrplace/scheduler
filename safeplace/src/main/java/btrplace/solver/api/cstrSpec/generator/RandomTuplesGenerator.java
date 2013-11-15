package btrplace.solver.api.cstrSpec.generator;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * @author Fabien Hermenier
 */
public class RandomTuplesGenerator<T> implements Generator<T[]> {

    private List<List<T>> doms;

    private Class<T> cl;

    public RandomTuplesGenerator(Class<T> cl, List<List<T>> domains) {
        this.doms = domains;
        this.cl = cl;
    }

    public void reset() {

    }

    public int count() {
        return Integer.MAX_VALUE;
    }

    public int done() {
        return 0;
    }

    @Override
    public boolean hasNext() {
        return true; //Beware to infinite loop
    }

    private static final Random rnd = new Random();

    private T randomIn(List<T> vals) {
        return vals.get(rnd.nextInt(vals.size()));
    }

    @Override
    public T[] next() {
        int s = doms.size();
        T[] tuple = (T[]) Array.newInstance(cl, s);
        for (int x = 0; x < s; x++) {
            tuple[x] = randomIn(doms.get(x));
        }
        return tuple;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<T[]> iterator() {
        return this;
    }
}
