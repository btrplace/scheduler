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

package btrplace.solver.choco.runner.single;

import btrplace.model.Instance;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithmParams;
import btrplace.solver.choco.runner.InstanceResult;
import btrplace.solver.choco.runner.InstanceSolver;

/**
 * A simple runner that solve in one stage a whole instance.
 *
 * @author Fabien Hermenier
 */
public class SingleRunner implements InstanceSolver {

    @Override
    public InstanceResult solve(ChocoReconfigurationAlgorithmParams cra,
                                Instance i) throws SolverException {
        InstanceSolverRunner r = new InstanceSolverRunner(cra, i);
        return r.call();

    }
}
