/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.solver.choco.runner.staticPartitioning;

import btrplace.model.*;
import gnu.trove.map.hash.TIntIntHashMap;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Unit tests for {@link SplittableIndex}.
 *
 * @author Fabien Hermenier
 */
public class SplittableIndexTest {

    @Test
    public void test() {
        Model mo = new DefaultModel();
        List<VM> l = new ArrayList<>();
        final TIntIntHashMap index = new TIntIntHashMap();
        Random rnd = new Random();
        for (int i = 0; i < 10; i++) {
            l.add(mo.newVM());
            index.put(i, rnd.nextInt(3));
        }
        SplittableIndex.newVMIndex(l, index).
                forEachIndexEntry(new IndexEntryProcedure<VM>() {
                    @Override
                    public boolean extract(SplittableIndex<VM> index, int key, int from, int to) {
                        TIntIntHashMap idx = index.getRespectiveIndex();
                        VM[] values = index.getValues();
                        for (int i = 0; i < values.length; i++) {
                            VM v = values[i];
                            if (i < from) {
                                Assert.assertTrue(idx.get(v.id()) < key);
                            } else if (i >= to) {
                                Assert.assertTrue(idx.get(v.id()) > key);
                            } else {
                                Assert.assertEquals(idx.get(v.id()), key);
                            }
                        }
                        return true;
                    }
                });
    }

    public static TIntIntHashMap makeVMIndex(Collection<Instance> instances) {
        TIntIntHashMap index = new TIntIntHashMap();
        int p = 0;
        for (Instance i : instances) {
            Mapping m = i.getModel().getMapping();
            for (Node n : m.getOnlineNodes()) {
                for (VM v : m.getRunningVMs(n)) {
                    index.put(v.id(), p);
                }
                for (VM v : m.getSleepingVMs(n)) {
                    index.put(v.id(), p);
                }
            }
            for (VM v : m.getReadyVMs()) {
                index.put(v.id(), p);
            }
            p++;
        }
        return index;
    }

    public static TIntIntHashMap makeNodeIndex(Collection<Instance> instances) {
        TIntIntHashMap index = new TIntIntHashMap();
        int p = 0;
        for (Instance i : instances) {
            Mapping m = i.getModel().getMapping();
            for (Node n : m.getOfflineNodes()) {
                index.put(n.id(), p);
            }
            for (Node n : m.getOnlineNodes()) {
                index.put(n.id(), p);
            }
            p++;
        }
        return index;
    }

}
