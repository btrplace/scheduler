package btrplace.solver.api.cstrSpec;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class TupleGenerator<T> implements Iterator<List<T>> {

    private T [][] doms;

    private int [] indexes;

    private int nbStates;

    private int k;

    public TupleGenerator(List<List<T>> domains) {
        doms = (T[][]) new Object[domains.size()][];
        indexes = new int[domains.size()];
        int i = 0;
        nbStates = 1;
        for (List<T> v : domains) {
            indexes[i] = 0;
            doms[i] = v.toArray((T[])new Object[v.size()]);
            nbStates *= doms[i].length;
            i++;
        }
    }

    @Override
    public boolean hasNext() {
        return k < nbStates;
    }

    @Override
    public List<T> next() {
        List<T> tuple = new ArrayList<>(doms.length);
        for (int x = 0; x < doms.length; x++) {
            tuple.add(doms[x][indexes[x]]);
        }
        for (int x = 0; x < doms.length; x++) {
            indexes[x]++;
            if (indexes[x] < doms[x].length) {
                break;
            }
            indexes[x] = 0;
        }
        k++;
        return tuple;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
