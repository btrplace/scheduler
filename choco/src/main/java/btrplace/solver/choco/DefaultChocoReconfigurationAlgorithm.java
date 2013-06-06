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

import btrplace.model.Instance;
import btrplace.model.Model;
import btrplace.model.constraint.MinMTTR;
import btrplace.model.constraint.OptimizationConstraint;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.constraint.ConstraintMapper;
import btrplace.solver.choco.durationEvaluator.DurationEvaluators;
import btrplace.solver.choco.runner.InstanceResult;
import btrplace.solver.choco.runner.InstanceSolver;
import btrplace.solver.choco.runner.SingleRunner;
import btrplace.solver.choco.view.ModelViewMapper;

import java.util.Collection;

/**
 * Default implementation of {@link ChocoReconfigurationAlgorithm}.
 * A same instance cannot be used to solve multiple problems simultaneously.
 * <p/>
 * By default, the algorithm relies on a {@link SingleRunner} solver.
 *
 * @author Fabien Hermenier
 */
public class DefaultChocoReconfigurationAlgorithm implements ChocoReconfigurationAlgorithm {

    private ChocoReconfigurationAlgorithmParams params;

    private InstanceSolver runner;

    private SolvingStatistics stats;

    /**
     * Make a new algorithm.
     */
    public DefaultChocoReconfigurationAlgorithm() {

        params = new DefaultChocoReconfigurationAlgorithParams();

        runner = new SingleRunner();
    }

    @Override
    public ChocoReconfigurationAlgorithmParams doOptimize(boolean b) {
        return params.doOptimize(b);
    }

    @Override
    public boolean doOptimize() {
        return params.doOptimize();
    }

    @Override
    public ChocoReconfigurationAlgorithmParams setTimeLimit(int t) {
        return params.setTimeLimit(t);
    }

    @Override
    public int getTimeLimit() {
        return params.getTimeLimit();
    }

    @Override
    public ChocoReconfigurationAlgorithmParams doRepair(boolean b) {
        return params.doRepair(b);
    }

    @Override
    public boolean doRepair() {
        return params.doRepair();
    }

    @Override
    public ChocoReconfigurationAlgorithmParams labelVariables(boolean b) {
        return params.labelVariables(b);
    }

    @Override
    public boolean areVariablesLabelled() {
        return params.areVariablesLabelled();
    }

    @Override
    public ReconfigurationPlan solve(Model i, Collection<SatConstraint> cstrs) throws SolverException {
        return solve(i, cstrs, new MinMTTR());
    }

    @Override
    public ReconfigurationPlan solve(Model i, Collection<SatConstraint> cstrs, OptimizationConstraint opt) throws SolverException {
        stats = null;
        InstanceResult res = runner.solve(params, new Instance(i, cstrs, opt));
        stats = res.getStatistics();
        return res.getPlan();
    }

    @Override
    public ConstraintMapper getConstraintMapper() {
        return params.getConstraintMapper();
    }

    @Override
    public DurationEvaluators getDurationEvaluators() {
        return params.getDurationEvaluators();
    }

    @Override
    public SolvingStatistics getStatistics() {
        if (stats == null) {
            return new SolvingStatistics(0, 0, 0, params.doOptimize(), getTimeLimit(), 0, 0, 0, 0, false, 0, 0);
        }
        return stats;
    }

    @Override
    public ChocoReconfigurationAlgorithmParams setMaxEnd(int end) {
        return params.setMaxEnd(end);
    }

    @Override
    public int getMaxEnd() {
        return params.getMaxEnd();
    }

    @Override
    public ModelViewMapper getViewMapper() {
        return params.getViewMapper();
    }

    @Override
    public ChocoReconfigurationAlgorithmParams setViewMapper(ModelViewMapper m) {
        return params.setViewMapper(m);
    }

    @Override
    public ChocoReconfigurationAlgorithmParams setVerbosity(int lvl) {
        return params.setVerbosity(lvl);
    }

    @Override
    public ChocoReconfigurationAlgorithmParams setConstraintMapper(ConstraintMapper map) {
        return params.setConstraintMapper(map);
    }

    @Override
    public ChocoReconfigurationAlgorithmParams setDurationEvaluators(DurationEvaluators d) {
        return params.setDurationEvaluators(d);
    }

    @Override
    public int getVerbosity() {
        return params.getVerbosity();
    }

    @Override
    public InstanceSolver getPartitionner() {
        return runner;
    }

    @Override
    public void setPartitioner(InstanceSolver p) {
        runner = p;
    }
}
