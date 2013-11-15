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

    private int k;

    private int max;

    private static <T> int nbCombinations(List<List<T>> d) {
        int x = 2;
        for (List l : d) {
            x *= l.size();
        }
        return x;
    }

    public RandomTuplesGenerator(Class<T> cl, List<List<T>> domains) {
        this(cl, domains, nbCombinations(domains));
    }

    public RandomTuplesGenerator(Class<T> cl, List<List<T>> domains, int max) {
        this.doms = domains;
        this.cl = cl;
        this.max = max;
    }

    public void reset() {
        k = 0;
    }

    public int count() {
        return max;
    }

    public int done() {
        return k;
    }

    @Override
    public boolean hasNext() {
        return k < max;
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
        k++;
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
