/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.view;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.Util;
import org.btrplace.model.VM;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Random;

/**
 * Unit tests for {@link VMConsumptionComparator}.
 *
 * @author Fabien Hermenier
 */
public class VMConsumptionComparatorTest {

    @Test
    public void testSimpleResource() {
        ShareableResource rc = new ShareableResource("foo");
        Model mo = new DefaultModel();
        List<VM> vms = Util.newVMs(mo, 10);
        Random rnd = new Random();
        for (VM id : vms) {
            rc.setConsumption(id, rnd.nextInt(5));
        }

        VMConsumptionComparator c1 = new VMConsumptionComparator(rc, true);
        vms.sort(c1);
        for (int i = 0; i < vms.size() - 1; i++) {
            Assert.assertTrue(rc.getConsumption(vms.get(i)) - rc.getConsumption(vms.get(i + 1)) <= 0);
        }

        //Descending order
        c1 = new VMConsumptionComparator(rc, false);
        vms.sort(c1);
        for (int i = 0; i < vms.size() - 1; i++) {
            Assert.assertTrue(rc.getConsumption(vms.get(i)) - rc.getConsumption(vms.get(i + 1)) >= 0);
        }
    }

    /**
     * Test when there is multiple resource to compare to
     */
    @Test(dependsOnMethods = {"testSimpleResource"})
    public void testCombination() {
        Model mo = new DefaultModel();
        List<VM> vms = Util.newVMs(mo, 7);
        ShareableResource rc = new ShareableResource("foo");

        for (int i = 0; i < 4; i++) {
            rc.setConsumption(vms.get(i), i);
        }
        rc.setConsumption(vms.get(4), 2);
        rc.setConsumption(vms.get(5), 2);
        rc.setConsumption(vms.get(6), 3);

        ShareableResource rc2 = new ShareableResource("bar");
        for (int i = 0; i < 4; i++) {
            rc2.setConsumption(vms.get(i), i / 2);
        }
        rc2.setConsumption(vms.get(4), 1);
        rc2.setConsumption(vms.get(5), 3);
        rc2.setConsumption(vms.get(6), 3);

        VMConsumptionComparator c1 = new VMConsumptionComparator(rc, true);
        c1.append(rc2, false);
        vms.sort(c1);

        for (int i = 0; i < vms.size() - 1; i++) {
            VM id = vms.get(i);
            Assert.assertTrue(rc.getConsumption(id) <= rc.getConsumption(vms.get(i + 1)));
            if (rc.getConsumption(id) == rc.getConsumption(vms.get(i + 1))) {
                Assert.assertTrue(rc2.getConsumption(id) >= rc2.getConsumption(vms.get(i + 1)));
            }
        }
        //The 2 criteria are ascending
        c1 = new VMConsumptionComparator(rc, true);
        c1.append(rc2, true);
        vms.sort(c1);

        for (int i = 0; i < vms.size() - 1; i++) {
            VM id = vms.get(i);
            Assert.assertTrue(rc.getConsumption(id) <= rc.getConsumption(vms.get(i + 1)));
            if (rc.getConsumption(id) == rc.getConsumption(vms.get(i + 1))) {
                Assert.assertTrue(rc2.getConsumption(id) <= rc2.getConsumption(vms.get(i + 1)));
            }
        }
    }
}
