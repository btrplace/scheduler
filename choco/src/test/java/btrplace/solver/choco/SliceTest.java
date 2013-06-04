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

package btrplace.solver.choco;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.VM;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link Slice}.
 *
 * @author Fabien Hermenier
 */
public class SliceTest {

    /**
     * It's a container so we only tests the instantiation and the getters.
     */
    @Test
    public void testInstantiation() {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        CPSolver s = new CPSolver();
        IntDomainVar st = s.createIntegerConstant("start", 1);
        IntDomainVar ed = s.createIntegerConstant("end", 3);
        IntDomainVar duration = s.createIntegerConstant("duration", 2);
        IntDomainVar hoster = s.createIntegerConstant("hoster", 4);
        Slice sl = new Slice(vm1, st, ed, duration, hoster);
        Assert.assertEquals(vm1, sl.getSubject());
        Assert.assertEquals(st, sl.getStart());
        Assert.assertEquals(ed, sl.getEnd());
        Assert.assertEquals(hoster, sl.getHoster());
        Assert.assertEquals(duration, sl.getDuration());
        Assert.assertFalse(sl.toString().contains("null"));
        duration = s.createBoundIntVar("duration", 3, 5);
        sl = new Slice(vm1, st, ed, duration, hoster);
        Assert.assertFalse(sl.toString().contains("null"));
    }
}
