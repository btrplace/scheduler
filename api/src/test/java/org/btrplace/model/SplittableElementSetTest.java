/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.model;

import gnu.trove.map.hash.TIntIntHashMap;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link SplittableElementSet}.
 *
 * @author Fabien Hermenier
 */
public class SplittableElementSetTest {

    @Test
    public void testNewVMSet() {
        List<VM> l = new ArrayList<>();
        TIntIntHashMap m = new TIntIntHashMap();
        for (int i = 0; i < 10; i++) {
            l.add(new VM(i));
            m.put(i, i % 2);
        }
        SplittableElementSet<VM> s = SplittableElementSet.newVMIndex(l, m);
        for (VM v : s.getValues()) {
            Assert.assertTrue(v.id() >= 0 && v.id() < 10);
        }
        Assert.assertEquals(s.size(), l.size());
        Assert.assertEquals(s.getRespectiveIndex(), m);
    }

    @Test
    public void testNewNodeSet() {
        List<Node> l = new ArrayList<>();
        TIntIntHashMap m = new TIntIntHashMap();
        for (int i = 0; i < 10; i++) {
            l.add(new Node(i));
            m.put(i, i % 2);
        }
        SplittableElementSet<Node> s = SplittableElementSet.newNodeIndex(l, m);
        for (Node v : s.getValues()) {
            Assert.assertTrue(v.id() >= 0 && v.id() < 10);
        }
        Assert.assertEquals(s.size(), l.size());
        Assert.assertEquals(s.getRespectiveIndex(), m);
    }

    @Test(dependsOnMethods = "testNewVMSet")
    public void testOrdering() {
        List<VM> l = new ArrayList<>();
        final TIntIntHashMap index = new TIntIntHashMap();
        Random rnd = new Random();
        for (int i = 0; i < 10; i++) {
            l.add(new VM(i));
            index.put(i, rnd.nextInt(3));
        }
        SplittableElementSet<VM> s = SplittableElementSet.newVMIndex(l, index);
        System.err.println(s);
        VM[] values = s.getValues();
        for (int i = 0; i < values.length - 1; i++) {
            Assert.assertTrue(index.get(values[i].id()) <= index.get(values[i + 1].id()));
        }
    }

    @Test(dependsOnMethods = "testNewVMSet")
    public void testGetPartitions() {
        List<VM> l = new ArrayList<>();
        final TIntIntHashMap index = new TIntIntHashMap();
        for (int i = 0; i < 10; i++) {
            l.add(new VM(i));
            index.put(i, i % 2);
        }
        SplittableElementSet<VM> s = SplittableElementSet.newVMIndex(l, index);
        //check each partition contains element having the same partition key.
        List<ElementSubSet<VM>> ss = s.getPartitions();
        for (ElementSubSet<VM> sub : ss) {
            Iterator<VM> ite = sub.iterator();
            int partKey = index.get(ite.next().id());
            while (ite.hasNext()) {
                Assert.assertEquals(index.get(ite.next().id()), partKey);
            }
        }
        Assert.assertEquals(s.size(), 10);
        Assert.assertEquals(ss.size(), 2);
    }

    @Test(dependsOnMethods = "testNewVMSet")
    public void testForEachPartition() {
        List<VM> l = new ArrayList<>();
        final TIntIntHashMap index = new TIntIntHashMap();
        for (int i = 0; i < 10; i++) {
            l.add(new VM(i));
            index.put(i, i % 2);
        }
        SplittableElementSet<VM> s = SplittableElementSet.newVMIndex(l, index);
        s.forEachPartition(new IterateProcedure<VM>() {
            @Override
            public boolean extract(SplittableElementSet<VM> index, int key, int from, int to) {
                Assert.assertEquals(to - from, 5);
                for (int i = from; i < to; i++) {
                    Assert.assertEquals(key, index.getValues()[i].id() % 2);
                }
                return true;
            }
        });

        //We stop after the first partition
        s.forEachPartition(new IterateProcedure<VM>() {
            boolean first = true;

            @Override
            public boolean extract(SplittableElementSet<VM> index, int key, int from, int to) {
                Assert.assertTrue(first);
                first = false;
                return first;
            }
        });
    }

    @Test
    public void testGetSubSet() {
        List<VM> l = new ArrayList<>();
        final TIntIntHashMap index = new TIntIntHashMap();
        for (int i = 0; i < 12; i++) {
            l.add(new VM(i));
            index.put(i, i % 3);
        }
        SplittableElementSet<VM> s = SplittableElementSet.newVMIndex(l, index);

        for (int i : new int[]{0, 1, 2}) {
            Set<VM> ss = s.getSubSet(i);
            Assert.assertEquals(ss.size(), 4);
            for (VM v : ss) {
                Assert.assertEquals(index.get(v.id()), i);
            }
        }

        //Unknown set
        Assert.assertTrue(s.getSubSet(-1).isEmpty());
    }
}
