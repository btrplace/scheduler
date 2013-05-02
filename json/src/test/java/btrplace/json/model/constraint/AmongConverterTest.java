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

package btrplace.json.model.constraint;

import btrplace.json.JSONConverterException;
import btrplace.model.constraint.Among;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Unit tests for {@link btrplace.json.model.constraint.AmongConverter}.
 *
 * @author Fabien Hermenier
 */
public class AmongConverterTest implements PremadeElements {

    private static AmongConverter conv = new AmongConverter();

    @Test
    public void testViables() throws JSONConverterException {
        Set<UUID> s1 = new HashSet<>(Arrays.asList(vm1, vm2, vm3));
        Set<UUID> p1 = new HashSet<>(Arrays.asList(n1, n2));
        Set<UUID> p2 = new HashSet<>(Arrays.asList(n4, n5));
        Set<UUID> p3 = new HashSet<>(Arrays.asList(n3));

        Set<Set<UUID>> pgrps = new HashSet<>(Arrays.asList(p1, p2, p3));

        Among d = new Among(s1, pgrps, false);
        Among c = new Among(s1, pgrps, true);
        Assert.assertEquals(conv.fromJSON(conv.toJSON(d)), d);
        Assert.assertEquals(conv.fromJSON(conv.toJSON(c)), c);
    }
}
