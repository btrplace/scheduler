package btrplace.solver.api.cstrSpec.generator;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * @author Fabien Hermenier
 */
public class RandomTuplesGenerator<T> implements Generator<T[]> {

    private T[][] doms;

    private int[] indexes;

    private int nbStates;

    private int k;

    private Class<T> cl;

    public RandomTuplesGenerator(Class<T> cl, List<List<T>> domains) {
        doms = (T[][]) new Object[domains.size()][];
        indexes = new int[domains.size()];
        int i = 0;
        nbStates = 1;
        this.cl = cl;
        for (List<T> v : domains) {
            indexes[i] = 0;
            doms[i] = v.toArray((T[]) new Object[v.size()]);
            nbStates *= doms[i].length;
            i++;
        }
    }

    public void reset() {
        k = 0;
    }

    public int count() {
        return nbStates;
    }

    public int done() {
        return k;
    }

    @Override
    public boolean hasNext() {
        return k < nbStates; //TODO: ensure completeness
    }

    private static final Random rnd = new Random();

    private T randomIn(T[] vals) {
        return vals[rnd.nextInt(vals.length)];
    }

    @Override
    public T[] next() {
        T[] tuple = (T[]) Array.newInstance(cl, doms.length);
        for (int x = 0; x < doms.length; x++) {
            tuple[x] = randomIn(doms[x]);
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
