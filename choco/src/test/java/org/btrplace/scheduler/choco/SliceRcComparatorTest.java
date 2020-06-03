/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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

  private static final Random rnd = new Random();

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
