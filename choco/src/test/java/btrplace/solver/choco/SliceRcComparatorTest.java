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

package btrplace.solver.choco;

import btrplace.model.view.ShareableResource;
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

        List<Slice> l = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            int u = i;
            l.add(new Slice(u, null, null, null, null));
        }
        return l;
    }

    @Test
    public void testAscending() {
        List<Slice> l = makeSlices();
        ShareableResource rc = new ShareableResource("cpu");
        for (Slice s : l) {
            rc.set(s.getSubject(), rnd.nextInt(10));
        }
        SliceRcComparator cmp = new SliceRcComparator(rc, true);
        Collections.sort(l, cmp);
        for (int i = 0; i < l.size() - 1; i++) {
            int u1 = l.get(i).getSubject();
            int u2 = l.get(i + 1).getSubject();
            Assert.assertTrue(rc.get(u1) <= rc.get(u2));
        }
    }

    @Test
    public void testDescending() {
        List<Slice> l = makeSlices();
        ShareableResource rc = new ShareableResource("cpu");
        for (Slice s : l) {
            rc.set(s.getSubject(), rnd.nextInt(10));
        }
        SliceRcComparator cmp = new SliceRcComparator(rc, false);
        Collections.sort(l, cmp);
        for (int i = 0; i < l.size() - 1; i++) {
            int u1 = l.get(i).getSubject();
            int u2 = l.get(i + 1).getSubject();
            Assert.assertTrue(rc.get(u1) >= rc.get(u2));
        }
    }
}
