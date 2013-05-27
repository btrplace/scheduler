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

package btrplace.json.model.constraint;

import btrplace.json.JSONConverterException;
import btrplace.model.constraint.Split;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * Unit tests for {@link btrplace.json.model.constraint.SplitConverter}.
 *
 * @author Fabien Hermenier
 */
public class SplitConverterTest implements PremadeElements {

    private static SplitConverter conv = new SplitConverter();

    @Test
    public void testViables() throws JSONConverterException {
        Set<Integer> s1 = new HashSet<>(Arrays.asList(vm1, vm2, vm3));
        Set<Integer> s2 = new HashSet<>(Arrays.asList(vm4, vm5, vm6));
        Set<Integer> s3 = new HashSet<>(Arrays.asList(vm7, vm8));
        Set<Set<Integer>> vgrps = new HashSet<>(Arrays.asList(s1, s2, s3));
        Split d = new Split(vgrps, false);
        Split c = new Split(vgrps, true);
        Assert.assertEquals(conv.fromJSON(conv.toJSON(d)), d);
        Assert.assertEquals(conv.fromJSON(conv.toJSON(c)), c);
    }
}
