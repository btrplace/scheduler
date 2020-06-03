/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.util;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author Fabien Hermenier
 */
public class AllTuplesGenerator<T> implements Iterator<T[]> {

    private final T[][] doms;

    private final int[] indexes;

    private int nbStates;

    private int k;

    private final Class<T> cl;

    public AllTuplesGenerator(Class<T> cl, List<List<T>> domains) {
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

    @Override
    public boolean hasNext() {
        return k < nbStates;
    }

    @Override
    public T[] next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
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
}
