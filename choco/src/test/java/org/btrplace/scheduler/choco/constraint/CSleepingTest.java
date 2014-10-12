/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.*;
import org.btrplace.model.constraint.Sleeping;
import org.btrplace.scheduler.choco.MappingFiller;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link CSleeping}.
 *
 * @author Fabien Hermenier
 */
public class CSleepingTest {

    @Test
    public void testGetMisplaced() {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        Node n1 = mo.newNode();
        Mapping m = new MappingFiller(mo.getMapping()).on(n1).ready(vm1).run(n1, vm2).sleep(n1, vm3).get();
        CSleeping k = new CSleeping(new Sleeping(vm3));
        Assert.assertEquals(0, k.getMisPlacedVMs(mo).size());
        //Assert.assertFalse(k.getMisPlacedVMs(mo).contains(vm3));

        k = new CSleeping(new Sleeping(vm1));
        Assert.assertEquals(1, k.getMisPlacedVMs(mo).size());
        Assert.assertTrue(k.getMisPlacedVMs(mo).contains(vm1));

        k = new CSleeping(new Sleeping(vm3));
        Assert.assertEquals(0, k.getMisPlacedVMs(mo).size());
    }
}
