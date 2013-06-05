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

package btrplace.solver.choco.partitioner;

import btrplace.model.Instance;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.SolvingStatistics;

/**
 * @author Fabien Hermenier
 */
public class InstanceSolverRunner implements Runnable {

    private Instance i;

    private SolvingStatistics stats;

    private SolverException ex;

    private ReconfigurationPlan plan;

    private ChocoReconfigurationAlgorithm origAlgo;

    public InstanceSolverRunner(ChocoReconfigurationAlgorithm orig, Instance i) {
        this.i = i;
        stats = null;
        ex = null;
        plan = null;
        origAlgo = orig;
    }

    @Override
    public void run() {
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        //Copy the orig parameters
        cra.setVerbosity(origAlgo.getVerbosity());
        cra.setViewMapper(origAlgo.getViewMapper());
        cra.setMaxEnd(origAlgo.getMaxEnd());
        cra.setObjective(origAlgo.getObjective());
        cra.setDurationEvaluators(origAlgo.getDurationEvaluators());
        cra.setSatConstraintMapper(origAlgo.getSatConstraintMapper());
        cra.doOptimize(origAlgo.doOptimize());
        cra.doRepair(origAlgo.doRepair());
        cra.setTimeLimit(origAlgo.getTimeLimit());
        cra.labelVariables(origAlgo.areVariablesLabelled());
        try {
            cra.solve(i.getModel(), i.getConstraints());
        } catch (SolverException e) {
            ex = e;
        }
    }

    public ReconfigurationPlan getResult() throws SolverException {
        if (ex != null) {
            throw ex;
        }
        return plan;
    }

    public SolvingStatistics getSolvingStatistics() {
        return stats;
    }
}
