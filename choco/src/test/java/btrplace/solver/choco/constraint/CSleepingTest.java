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

package btrplace.solver.choco.constraint;

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Sleeping;
import btrplace.solver.choco.MappingBuilder;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link CSleeping}.
 *
 * @author Fabien Hermenier
 */
public class CSleepingTest implements PremadeElements {

    @Test
    public void testGetMisplaced() {
        Mapping m = new MappingBuilder().on(n1).ready(vm1).run(n1, vm2).sleep(n1, vm3).build();
        Model mo = new DefaultModel(m);
        CSleeping k = new CSleeping(new Sleeping(m.getAllVMs()));
        Assert.assertEquals(2, k.getMisPlacedVMs(mo).size());
        Assert.assertFalse(k.getMisPlacedVMs(mo).contains(vm3));
    }
}
