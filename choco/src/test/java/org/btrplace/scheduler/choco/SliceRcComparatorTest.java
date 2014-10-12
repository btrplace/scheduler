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

package org.btrplace.scheduler.choco;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.btrplace.model.view.ShareableResource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Unit tests for {@link SliceRcComparator}.
 *
 * @author Fabien Hermenier
 */
public class SliceRcComparatorTest {

    private static Random rnd = new Random();

    private static List<Slice> makeSlices() {
        Model mo = new DefaultModel();
        List<Slice> l = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            l.add(new Slice(mo.newVM(), null, null, null, null));
        }
        return l;
    }

    @Test
    public void testAscending() {
        List<Slice> l = makeSlices();
        ShareableResource rc = new ShareableResource("cpu");
        for (Slice s : l) {
            rc.setConsumption(s.getSubject(), rnd.nextInt(10));
        }
        SliceRcComparator cmp = new SliceRcComparator(rc, true);
        Collections.sort(l, cmp);
        for (int i = 0; i < l.size() - 1; i++) {
            VM u1 = l.get(i).getSubject();
            VM u2 = l.get(i + 1).getSubject();
            Assert.assertTrue(rc.getConsumption(u1) <= rc.getConsumption(u2));
        }
    }

    @Test
    public void testDescending() {
        List<Slice> l = makeSlices();
        ShareableResource rc = new ShareableResource("cpu");
        for (Slice s : l) {
            rc.setConsumption(s.getSubject(), rnd.nextInt(10));
        }
        SliceRcComparator cmp = new SliceRcComparator(rc, false);
        Collections.sort(l, cmp);
        for (int i = 0; i < l.size() - 1; i++) {
            VM u1 = l.get(i).getSubject();
            VM u2 = l.get(i + 1).getSubject();
            Assert.assertTrue(rc.getConsumption(u1) >= rc.getConsumption(u2));
        }
    }
}
