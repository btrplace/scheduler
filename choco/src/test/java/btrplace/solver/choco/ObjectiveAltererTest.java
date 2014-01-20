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
import btrplace.solver.SolverException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link btrplace.solver.choco.ObjectiveAlterer}.
 *
 * @author Fabien Hermenier
 */
public class ObjectiveAltererTest {

    @Test
    public void testBasic() throws SolverException {
        Model mo = new DefaultModel();
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo).build();
        IntVar obj = rp.getSolver().createBoundIntVar("obj", 10, 1000);
        rp.getSolver().setObjective(obj);
        ObjectiveAlterer oa = new ObjectiveAlterer(rp) {
            @Override
            public int tryNewValue(int currentValue) {
                return currentValue * 2;
            }
        };
        Assert.assertEquals(oa.getObjective(), obj);
        Assert.assertEquals(oa.tryNewValue(25), 50);
        Assert.assertEquals(oa.tryNewValue(50), 100);
    }
}
