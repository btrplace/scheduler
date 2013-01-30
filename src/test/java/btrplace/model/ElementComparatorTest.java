/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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

import btrplace.model.view.DefaultShareableResource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link btrplace.model.ElementComparator}.
 *
 * @author Fabien Hermenier
 */
public class ElementComparatorTest {

    @Test
    public void testSimpleResource() {
        DefaultShareableResource rc = new DefaultShareableResource("foo");
        List<UUID> uuids = new ArrayList<UUID>(10);
        for (int i = 0; i < 10; i++) {
            uuids.add(UUID.randomUUID());
        }
        Random rnd = new Random();
        for (UUID id : uuids) {
            rc.set(id, rnd.nextInt(5));
        }

        ElementComparator c1 = new ElementComparator(rc);
        Collections.sort(uuids, c1);
        for (int i = 0; i < uuids.size() - 1; i++) {
            Assert.assertTrue(rc.compare(uuids.get(i), uuids.get(i + 1)) <= 0);
        }

        //Descending order
        c1 = new ElementComparator(rc, false);
        Collections.sort(uuids, c1);
        for (int i = 0; i < uuids.size() - 1; i++) {
            Assert.assertTrue(rc.compare(uuids.get(i), uuids.get(i + 1)) >= 0);
        }
    }

    /**
     * Test when there is multiple resource to compare to
     */
    @Test(dependsOnMethods = {"testSimpleResource"})
    public void testCombination() {
        List<UUID> uuids = new ArrayList<UUID>(7);
        for (int i = 0; i < 7; i++) {
            uuids.add(UUID.randomUUID());
        }
        DefaultShareableResource rc = new DefaultShareableResource("foo");

        for (int i = 0; i < 4; i++) {
            rc.set(uuids.get(i), i);
        }
        rc.set(uuids.get(4), 2);
        rc.set(uuids.get(5), 2);
        rc.set(uuids.get(6), 3);

        DefaultShareableResource rc2 = new DefaultShareableResource("bar");
        for (int i = 0; i < 4; i++) {
            rc2.set(uuids.get(i), i / 2);
        }
        rc2.set(uuids.get(4), -1);
        rc2.set(uuids.get(5), 3);
        rc2.set(uuids.get(6), 3);

        ElementComparator c1 = new ElementComparator(rc);
        c1.append(rc2, false);
        Collections.sort(uuids, c1);

        for (int i = 0; i < uuids.size() - 1; i++) {
            UUID id = uuids.get(i);
            Assert.assertTrue(rc.get(id) <= rc.get(uuids.get(i + 1)));
            if (rc.get(id) == rc.get(uuids.get(i + 1))) {
                Assert.assertTrue(rc2.get(id) >= rc2.get(uuids.get(i + 1)));
            }
        }
        //The 2 criteria are ascending
        c1 = new ElementComparator(rc);
        c1.append(rc2, true);
        Collections.sort(uuids, c1);

        for (int i = 0; i < uuids.size() - 1; i++) {
            UUID id = uuids.get(i);
            Assert.assertTrue(rc.get(id) <= rc.get(uuids.get(i + 1)));
            if (rc.get(id) == rc.get(uuids.get(i + 1))) {
                Assert.assertTrue(rc2.get(id) <= rc2.get(uuids.get(i + 1)));
            }
        }
    }
}
