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
import btrplace.model.constraint.Fence;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

/**
 * Unit tests for {@link btrplace.json.model.constraint.FenceConverter}.
 *
 * @author Fabien Hermenier
 */
public class FenceConverterTest implements PremadeElements {

    private static FenceConverter conv = new FenceConverter();

    @Test
    public void testViables() throws JSONConverterException {
        Fence d = new Fence(new HashSet<UUID>(Arrays.asList(vm1, vm2, vm3)),
                new HashSet<UUID>(Arrays.asList(n1, n2, n3)));

        Assert.assertEquals(conv.fromJSON(conv.toJSON(d)), d);
    }
}
