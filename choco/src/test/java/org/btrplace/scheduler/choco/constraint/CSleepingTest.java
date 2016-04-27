/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.model.constraint.Sleeping;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

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
        mo.getMapping().on(n1).ready(vm1).run(n1, vm2).sleep(n1, vm3);
        Instance i = new Instance(mo, Collections.emptyList(), new MinMTTR());
        CSleeping k = new CSleeping(new Sleeping(vm3));
        Assert.assertEquals(0, k.getMisPlacedVMs(i).size());

        k = new CSleeping(new Sleeping(vm1));
        Assert.assertEquals(1, k.getMisPlacedVMs(i).size());
        Assert.assertTrue(k.getMisPlacedVMs(i).contains(vm1));

        k = new CSleeping(new Sleeping(vm3));
        Assert.assertEquals(0, k.getMisPlacedVMs(i).size());
    }
}
