/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model;

import gnu.trove.map.hash.TIntIntHashMap;
import org.btrplace.scheduler.runner.disjoint.model.ElementSubSet;
import org.btrplace.scheduler.runner.disjoint.model.SplittableElementSet;
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
        List<VM> values = si.getValues();

        ElementSubSet<VM> p1 = new ElementSubSet<>(si, 0, 0, 5);
        //test contains()
        Assert.assertTrue(p1.contains(values.get(0)));
        Assert.assertFalse(p1.contains(values.get(5)));

        //test containsAll()
        Assert.assertFalse(p1.containsAll(l));

        //test size()
        Assert.assertEquals(p1.size(), 5);
        Assert.assertFalse(p1.isEmpty());

        System.out.println(p1);
        //test iterator
        for (VM v : p1) {
            Assert.assertEquals(v.id() % 2, 0);
        }
    }
}
