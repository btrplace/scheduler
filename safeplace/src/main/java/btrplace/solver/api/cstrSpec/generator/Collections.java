package btrplace.solver.api.cstrSpec.generator;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class Collections {

    public static <T> List<List<T>> allTuples(List<List<T>> l) {
        T [][] doms = (T[][]) new Object[l.size()][];
        int [] indexes = new int[l.size()];
        int i = 0;
        int nbStates = 1;
        for (List<T> v : l) {
            indexes[i] = 0;
            //Set<Object> sDom = v.domain();
            doms[i] = v.toArray((T[])new Object[v.size()]);
            nbStates *= doms[i].length;
            i++;
        }
        List<List<T>> all = new ArrayList<>(nbStates);
        for (int k = 0; k < nbStates; k++) {
            List<T> tuple = new ArrayList<>(l.size());
            for (int x = 0; x < l.size(); x++) {
                tuple.add(doms[x][indexes[x]]);
            }
            for (int x = 0; x < l.size(); x++) {
                indexes[x]++;
                if (indexes[x] < doms[x].length) {
                    break;
                }
                indexes[x] = 0;
            }
            all.add(tuple);
        }
        return all;
    }
}
