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

package btrplace.solver.choco.durationEvaluator;

import btrplace.model.DefaultShareableResource;
import btrplace.model.ShareableResource;
import junit.framework.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * Unit tests for {@link LinearToAResourceDuration}.
 *
 * @author Fabien Hermenier
 */
public class LinearToAResourceDurationTest {

    @Test
    public void testSimple() {
        ShareableResource rc = new DefaultShareableResource("foo", 0);
        UUID u = UUID.randomUUID();
        rc.set(u, 3);
        LinearToAResourceDuration d = new LinearToAResourceDuration(rc, 3);
        Assert.assertEquals(d.evaluate(u), 9);
        Assert.assertEquals(d.evaluate(UUID.randomUUID()), 0);

        d = new LinearToAResourceDuration(rc, 3, 4);
        Assert.assertEquals(d.evaluate(u), 13);
        Assert.assertEquals(d.evaluate(UUID.randomUUID()), 4);
    }
}
