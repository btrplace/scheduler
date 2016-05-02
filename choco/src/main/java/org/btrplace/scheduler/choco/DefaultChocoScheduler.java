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

package org.btrplace.scheduler.choco;

import org.btrplace.model.Instance;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.*;
import org.btrplace.model.view.network.Network;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.constraint.ChocoMapper;
import org.btrplace.scheduler.choco.duration.DurationEvaluators;
import org.btrplace.scheduler.choco.runner.InstanceSolver;
import org.btrplace.scheduler.choco.runner.SolvingStatistics;
import org.btrplace.scheduler.choco.runner.StagedSolvingStatistics;
import org.btrplace.scheduler.choco.runner.single.SingleRunner;
import org.btrplace.scheduler.choco.transition.TransitionFactory;
import org.btrplace.scheduler.choco.view.ChocoView;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link ChocoScheduler}.
 * A same instance cannot be used to solve multiple problems simultaneously.
 * <p>
 * By default, the algorithm relies on a {@link SingleRunner} solver.
 *
 * @author Fabien Hermenier
 */
public class DefaultChocoScheduler implements ChocoScheduler {

    private Parameters params;

    private InstanceSolver runner;

    private StagedSolvingStatistics stages;
    /**
     * Make a new algorithm.
     *
     * @param ps the parameters to use to configure the algorithm
     */
    public DefaultChocoScheduler(DefaultParameters ps) {
        params = ps;
        runner = new SingleRunner();
    }

    /**
     * Make a new algorithm with default parameters.
     */
    public DefaultChocoScheduler() {
        this(new DefaultParameters());
    }

    @Override
    public Parameters doOptimize(boolean b) {
        return params.doOptimize(b);
    }

    @Override
    public boolean doOptimize() {
        return params.doOptimize();
    }

    @Override
    public Parameters setTimeLimit(int t) {
        return params.setTimeLimit(t);
    }

    @Override
    public int getTimeLimit() {
        return params.getTimeLimit();
    }

    @Override
    public Parameters doRepair(boolean b) {
        return params.doRepair(b);
    }

    @Override
    public boolean doRepair() {
        return params.doRepair();
    }

    @Override
    public ReconfigurationPlan solve(Model i, Collection<? extends SatConstraint> cstrs) throws SchedulerException {
        return solve(i, cstrs, new MinMTTR());
    }

    @Override
    public ReconfigurationPlan solve(Instance i) throws SchedulerException {
        return solve(i.getModel(), i.getSatConstraints(), i.getOptConstraint());
    }

    @Override
    public DefaultChocoScheduler setParameters(Parameters p) {
        params = p;
        return this;
    }

    @Override
    public Parameters getParameters() {
        return params;
    }

    @Override
    public ReconfigurationPlan solve(Model i, Collection<? extends SatConstraint> cstrs, OptConstraint opt) throws SchedulerException {
        // If a network view is attached, ensure that all the migrations' destination node are defined
        Network net = Network.get(i);
        stages = null;
        if  (net != null) {
            stages = new StagedSolvingStatistics();
            // The network view is useless to take placement decisions
            i.detach(net);

            // Solve a first time using placement oriented MinMTTR optimisation constraint
            ReconfigurationPlan p = runner.solve(params, new Instance(i, cstrs, new MinMTTR()));
            stages.append(runner.getStatistics());
            if (p == null) {
                return null;
            }

            // Add Fence constraints for each destination node chosen
            List<SatConstraint> newCstrs = p.getActions().stream()
                    .filter(a -> a instanceof MigrateVM)
                    .map(a -> new Fence(((MigrateVM) a).getVM(),
                            Collections.singleton(((MigrateVM) a).getDestinationNode())))
                    .collect(Collectors.toList());

            Model result = p.getResult();
            if (result == null) {
                throw new SchedulerException(p.getOrigin(), "The plan is not viable");
            }
            // Add Root constraints to all staying VMs
            newCstrs.addAll(i.getMapping().getRunningVMs().stream().filter(v -> p.getOrigin().getMapping().getVMLocation(v).id() ==
                    result.getMapping().getVMLocation(v).id()).map(Root::new).collect(Collectors.toList()));


            // Add the old constraints
            newCstrs.addAll(cstrs);
            // Re-attach the network view
            i.attach(net);

            //New timeout value = elapsed time - initial timeout value
            Parameters ps = new DefaultParameters(params);
            if (ps.getTimeLimit() > 0) {
                float timeout = params.getTimeLimit() - runner.getStatistics().getMeasures().getTimeCount();
                ps.setTimeLimit((int) timeout);
            }

            return runner.solve(ps, new Instance(i, newCstrs, opt));
        }
        // Solve and return the computed plan
        return runner.solve(params, new Instance(i, cstrs, opt));
    }

    @Override
    public ChocoMapper getMapper() {
        return params.getMapper();
    }

    @Override
    public DurationEvaluators getDurationEvaluators() {
        return params.getDurationEvaluators();
    }

    @Override
    public SolvingStatistics getStatistics() throws SchedulerException {
        if (stages == null) {
            return runner.getStatistics();
        }
        stages.append(runner.getStatistics());
        return stages;
    }

    @Override
    public Parameters setMaxEnd(int end) {
        return params.setMaxEnd(end);
    }

    @Override
    public int getMaxEnd() {
        return params.getMaxEnd();
    }

    @Override
    public Parameters setVerbosity(int lvl) {
        return params.setVerbosity(lvl);
    }

    @Override
    public Parameters setMapper(ChocoMapper map) {
        return params.setMapper(map);
    }

    @Override
    public Parameters setDurationEvaluators(DurationEvaluators d) {
        return params.setDurationEvaluators(d);
    }

    @Override
    public int getVerbosity() {
        return params.getVerbosity();
    }

    @Override
    public InstanceSolver getInstanceSolver() {
        return runner;
    }

    @Override
    public void setInstanceSolver(InstanceSolver p) {
        runner = p;
    }

    @Override
    public Parameters setTransitionFactory(TransitionFactory amf) {
        params.setTransitionFactory(amf);
        return this;
    }

    @Override
    public TransitionFactory getTransitionFactory() {
        return params.getTransitionFactory();
    }

    @Override
    public Parameters setRandomSeed(long s) {
        return params.setRandomSeed(s);
    }

    @Override
    public long getRandomSeed() {
        return params.getRandomSeed();
    }

    @Override
    public boolean addChocoView(Class<? extends ChocoView> v) {
        return params.addChocoView(v);
    }

    @Override
    public boolean removeChocoView(Class<? extends ChocoView> v) {
        return params.removeChocoView(v);
    }

    @Override
    public List<Class<? extends ChocoView>> getChocoViews() {
        return params.getChocoViews();
    }
}
