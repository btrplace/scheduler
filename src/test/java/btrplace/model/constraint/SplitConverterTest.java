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

import btrplace.JSONConverterException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Unit tests for {@link btrplace.model.constraint.SplitConverter}.
 *
 * @author Fabien Hermenier
 */
public class SplitConverterTest implements ConstraintTestMaterial {

    private static SplitConverter conv = new SplitConverter();

    @Test
    public void testViables() throws JSONConverterException {
        Set<UUID> s1 = new HashSet<UUID>(Arrays.asList(vm1, vm2, vm3));
        Set<UUID> s2 = new HashSet<UUID>(Arrays.asList(vm4, vm5, vm6));
        Set<UUID> s3 = new HashSet<UUID>(Arrays.asList(vm7, vm8));
        Set<Set<UUID>> vgrps = new HashSet<Set<UUID>>(Arrays.asList(s1, s2, s3));
        Split d = new Split(vgrps, false);
        Split c = new Split(vgrps, true);
        Assert.assertEquals(conv.fromJSON(conv.toJSON(d)), d);
        Assert.assertEquals(conv.fromJSON(conv.toJSON(c)), c);
    }
}
