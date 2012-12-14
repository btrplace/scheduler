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

package btrplace.solver.choco;

import btrplace.solver.SolverException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link DefaultChocoReconfigurationAlgorithm}.
 *
 * @author Fabien Hermenier
 */
public class DefaultChocoReconfigurationAlgorithmTest {

    @Test
    public void testGetsAndSets() {
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();

        cra.setTimeLimit(10);
        Assert.assertEquals(cra.getTimeLimit(), 10);

        cra.doOptimize(false);
        Assert.assertEquals(cra.doOptimize(), false);

        cra.repair(true);
        Assert.assertEquals(cra.repair(), true);

        cra.labelVariables(true);
        Assert.assertEquals(cra.areVariablesLabelled(), true);

        ReconfigurationObjective obj = new ReconfigurationObjective() {
            @Override
            public void inject(ReconfigurationProblem rp) throws SolverException {

            }
        };
        cra.setObjective(obj);
        Assert.assertEquals(cra.getObjective(), obj);
    }
}
