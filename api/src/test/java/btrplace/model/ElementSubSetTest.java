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

package btrplace.model;

import gnu.trove.map.hash.TIntIntHashMap;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link btrplace.model.ElementSubSet}.
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
        System.err.println(si);
        ElementSubSet<VM> p1 = new ElementSubSet<>(si, 0, 0, 5);
        //test contains()
        Assert.assertTrue(p1.contains(values[0]));
        Assert.assertFalse(p1.contains(values[5]));

        //test size()
        Assert.assertEquals(p1.size(), 5);
        System.out.println(p1);
        //test iterator
        for (VM v : p1) {
            Assert.assertEquals(v.id() % 2, 0);
        }
    }
}
