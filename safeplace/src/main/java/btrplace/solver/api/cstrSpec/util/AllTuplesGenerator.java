package btrplace.solver.api.cstrSpec.util;

import java.lang.reflect.Array;
import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class AllTuplesGenerator<T> implements Generator<T[]> {

    private T[][] doms;

    private int[] indexes;

    private int nbStates;

    private int k;

    private Class<T> cl;

    public AllTuplesGenerator(Class<T> cl, List<List<T>> domains) {
        doms = (T[][]) new Object[domains.size()][];
        indexes = new int[domains.size()];
        int i = 0;
        nbStates = 1;
        this.cl = cl;
        //System.out.println(domains);
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
        return k < nbStates;
    }

    public static <T> Set<Set<T>> allSubsets(Class<T> cl, Collection<T> cnt) {
        List<List<T>> l = new ArrayList<>();
        for (T t : cnt) {
            l.add(new ArrayList<>(cnt));
        }
        AllTuplesGenerator<T> tg = new AllTuplesGenerator<>(cl, l);
        return tg.allSubsets();
    }

    public Set<Set<T>> allSubsets() {
        Set<Set<T>> res = new HashSet<>();
        for (T[] tuple : this) {
            Set<T> s = new HashSet<>(tuple.length);
            Collections.addAll(s, tuple);
            res.add(s);
        }
        return res;
    }

    @Override
    public T[] next() {
        T[] tuple = (T[]) Array.newInstance(cl, doms.length);
        for (int x = 0; x < doms.length; x++) {
            tuple[x] = doms[x][indexes[x]];
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

    @Override
    public Iterator<T[]> iterator() {
        return this;
    }
}
