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

import btrplace.model.view.ShareableResource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Unit tests for {@link btrplace.model.ElementComparator}.
 *
 * @author Fabien Hermenier
 */
public class ElementComparatorTest {

    private static Random rnd = new Random();

    @Test
    public void testSimpleResource() {
        ShareableResource rc = new ShareableResource("foo");
        List<Integer> uuids = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            uuids.add(rnd.nextInt());
        }
        Random rnd = new Random();
        for (int id : uuids) {
            rc.setNodeCapacity(id, rnd.nextInt(5));
        }

        ElementComparator c1 = new ElementComparator(false, rc);
        Collections.sort(uuids, c1);
        for (int i = 0; i < uuids.size() - 1; i++) {
            Assert.assertTrue(rc.getNodeCapacity(uuids.get(i)) - rc.getNodeCapacity(uuids.get(i + 1)) <= 0);
        }

        //Descending order
        c1 = new ElementComparator(false, rc);
        Collections.sort(uuids, c1);
        for (int i = 0; i < uuids.size() - 1; i++) {
            Assert.assertTrue(rc.getNodeCapacity(uuids.get(i)) - rc.getNodeCapacity(uuids.get(i + 1)) >= 0);
        }
    }

    /**
     * Test when there is multiple resource to compare to
     */
    @Test(dependsOnMethods = {"testSimpleResource"})
    public void testCombination() {
        List<Integer> uuids = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            uuids.add(rnd.nextInt());
        }
        ShareableResource rc = new ShareableResource("foo");

        for (int i = 0; i < 4; i++) {
            rc.setVMConsumption(uuids.get(i), i);
        }
        rc.setVMConsumption(uuids.get(4), 2);
        rc.setVMConsumption(uuids.get(5), 2);
        rc.setVMConsumption(uuids.get(6), 3);

        ShareableResource rc2 = new ShareableResource("bar");
        for (int i = 0; i < 4; i++) {
            rc2.setVMConsumption(uuids.get(i), i / 2);
        }
        rc2.setVMConsumption(uuids.get(4), -1);
        rc2.setVMConsumption(uuids.get(5), 3);
        rc2.setVMConsumption(uuids.get(6), 3);

        ElementComparator c1 = new ElementComparator(true, rc);
        c1.append(rc2, false);
        Collections.sort(uuids, c1);

        for (int i = 0; i < uuids.size() - 1; i++) {
            int id = uuids.get(i);
            Assert.assertTrue(rc.getVMConsumption(id) <= rc.getVMConsumption(uuids.get(i + 1)));
            if (rc.getVMConsumption(id) == rc.getVMConsumption(uuids.get(i + 1))) {
                Assert.assertTrue(rc2.getVMConsumption(id) >= rc2.getVMConsumption(uuids.get(i + 1)));
            }
        }
        //The 2 criteria are ascending
        c1 = new ElementComparator(true, rc);
        c1.append(rc2, true);
        Collections.sort(uuids, c1);

        for (int i = 0; i < uuids.size() - 1; i++) {
            int id = uuids.get(i);
            Assert.assertTrue(rc.getVMConsumption(id) <= rc.getVMConsumption(uuids.get(i + 1)));
            if (rc.getVMConsumption(id) == rc.getVMConsumption(uuids.get(i + 1))) {
                Assert.assertTrue(rc2.getVMConsumption(id) <= rc2.getVMConsumption(uuids.get(i + 1)));
            }
        }
    }
}
