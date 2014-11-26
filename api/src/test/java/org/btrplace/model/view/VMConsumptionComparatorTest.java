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

package org.btrplace.model.view;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.Util;
import org.btrplace.model.VM;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
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
        Collections.sort(vms, c1);
        for (int i = 0; i < vms.size() - 1; i++) {
            Assert.assertTrue(rc.getConsumption(vms.get(i)) - rc.getConsumption(vms.get(i + 1)) <= 0);
        }

        //Descending order
        c1 = new VMConsumptionComparator(rc, false);
        Collections.sort(vms, c1);
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
        rc2.setConsumption(vms.get(4), -1);
        rc2.setConsumption(vms.get(5), 3);
        rc2.setConsumption(vms.get(6), 3);

        VMConsumptionComparator c1 = new VMConsumptionComparator(rc, true);
        c1.append(rc2, false);
        Collections.sort(vms, c1);

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
        Collections.sort(vms, c1);

        for (int i = 0; i < vms.size() - 1; i++) {
            VM id = vms.get(i);
            Assert.assertTrue(rc.getConsumption(id) <= rc.getConsumption(vms.get(i + 1)));
            if (rc.getConsumption(id) == rc.getConsumption(vms.get(i + 1))) {
                Assert.assertTrue(rc2.getConsumption(id) <= rc2.getConsumption(vms.get(i + 1)));
            }
        }
    }
}
