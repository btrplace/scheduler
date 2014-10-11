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

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link org.btrplace.model.ElementSubSet}.
 *
 * @author Fabien Hermenier
 */
public class ElementSubSetTest {

    @Test
    public void test() {
        Model mo = new DefaultModel();
        List<VM> l = new ArrayList<>();
        final TIntIntHashMap index = new TIntIntHashMap();

        for (int i = 0; i < 10; i++) {
            l.add(mo.newVM());
            index.put(i, i % 2);
        }

        SplittableElementSet<VM> si = SplittableElementSet.newVMIndex(l, index);
        VM[] values = si.getValues();

        ElementSubSet<VM> p1 = new ElementSubSet<>(si, 0, 0, 5);
        //test contains()
        Assert.assertTrue(p1.contains(values[0]));
        Assert.assertFalse(p1.contains(values[5]));

        //test containsAll()
        Assert.assertFalse(p1.containsAll(l));

        //test size()
        Assert.assertEquals(p1.size(), 5);
        Assert.assertFalse(p1.isEmpty());

        //toArray()
        Object[] objects = p1.toArray();
        Assert.assertEquals(objects.length, 5);
        for (int i = 0; i < 5; i++) {
            VM v = (VM) objects[i];
            Assert.assertEquals(v.id() % 2, 0);
        }

        VM[] vms = (VM[]) p1.toArray(new VM[5]);
        for (VM v : vms) {
            Assert.assertEquals(v.id() % 2, 0);
        }

        System.out.println(p1);
        //test iterator
        for (VM v : p1) {
            Assert.assertEquals(v.id() % 2, 0);
        }
    }
}
