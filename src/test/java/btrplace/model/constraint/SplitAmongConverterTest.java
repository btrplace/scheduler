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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Unit tests for {@link SplitAmongConverter}.
 *
 * @author Fabien Hermenier
 */
public class SplitAmongConverterTest implements ConstraintTestMaterial {

    private static SplitAmongConverter conv = new SplitAmongConverter();

    @Test
    public void testViables() {
        Set<UUID> s1 = new HashSet<UUID>(Arrays.asList(vm1, vm2, vm3));
        Set<UUID> s2 = new HashSet<UUID>(Arrays.asList(vm4, vm5, vm6));
        Set<UUID> s3 = new HashSet<UUID>(Arrays.asList(vm7, vm8));
        Set<Set<UUID>> vgrps = new HashSet<Set<UUID>>(Arrays.asList(s1, s2, s3));

        Set<UUID> p1 = new HashSet<UUID>(Arrays.asList(n1, n2));
        Set<UUID> p2 = new HashSet<UUID>(Arrays.asList(n4, n5));
        Set<UUID> p3 = new HashSet<UUID>(Arrays.asList(n3));
        Set<Set<UUID>> pgrps = new HashSet<Set<UUID>>(Arrays.asList(p1, p2, p3));

        SplitAmong d = new SplitAmong(vgrps, pgrps, false);
        SplitAmong c = new SplitAmong(vgrps, pgrps, true);
        Assert.assertEquals(conv.fromJSON(conv.toJSON(d)), d);
        Assert.assertEquals(conv.fromJSON(conv.toJSON(c)), c);
    }
}
