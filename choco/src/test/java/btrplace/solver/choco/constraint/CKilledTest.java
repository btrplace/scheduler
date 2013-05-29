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

package btrplace.solver.choco.constraint;

import btrplace.model.*;
import btrplace.model.constraint.Killed;
import btrplace.solver.choco.MappingFiller;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

/**
 * Unit tests for {@link CKilled}.
 *
 * @author Fabien Hermenier
 */
public class CKilledTest implements PremadeElements {

    @Test
    public void testGetMisplaced() {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm5 = mo.newVM();
        Node n1 = mo.newNode();
        Mapping m = new MappingFiller(mo.getMapping()).ready(vm1).on(n1).run(n1, vm2).get();

        CKilled k = new CKilled(new Killed(Collections.singleton(vm5)));
        Assert.assertTrue(k.getMisPlacedVMs(mo).isEmpty());
        k = new CKilled(new Killed(m.getAllVMs()));
        Assert.assertEquals(2, k.getMisPlacedVMs(mo).size());
    }
}
