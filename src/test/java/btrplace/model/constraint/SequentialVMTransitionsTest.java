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

package btrplace.model.constraint;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Unit tests for {@link SequentialVMTransitions}.
 *
 * @author Fabien Hermenier
 */
public class SequentialVMTransitionsTest {

    @Test
    public void testInstantiation() {

        List<UUID> l = new ArrayList<UUID>();
        l.add(UUID.randomUUID());
        l.add(UUID.randomUUID());
        l.add(UUID.randomUUID());
        SequentialVMTransitions c = new SequentialVMTransitions(l);
        Assert.assertEquals(l, c.getInvolvedVMs());
        Assert.assertTrue(c.getInvolvedNodes().isEmpty());
        Assert.assertFalse(c.toString().contains("null"));
    }

    @Test(dependsOnMethods = {"testInstantiation"})
    public void testEquals() {
        List<UUID> l = new ArrayList<UUID>();
        l.add(UUID.randomUUID());
        l.add(UUID.randomUUID());
        l.add(UUID.randomUUID());
        SequentialVMTransitions c = new SequentialVMTransitions(l);
        List<UUID> l2 = new ArrayList<UUID>(l);
        SequentialVMTransitions c2 = new SequentialVMTransitions(l2);
        Assert.assertTrue(c.equals(c2));
        Assert.assertEquals(c.hashCode(), c2.hashCode());
        l2.add(l2.remove(0)); //shift the list
        Assert.assertFalse(c.equals(c2));

    }
}
