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

package btrplace.solver.choco;

import btrplace.solver.choco.constraint.ConstraintMapper;
import btrplace.solver.choco.duration.DurationEvaluators;
import btrplace.solver.choco.transition.TransitionFactory;
import btrplace.solver.choco.view.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link ChocoReconfigurationAlgorithmParams}.
 * <ul>
 * <li>repair mode is disabled</li>
 * <li>no time limit</li>
 * <li>the transition factory comes from {@link btrplace.solver.choco.transition.TransitionFactory#newBundle()}</li>
 * <li>the view mapper comes from {@link btrplace.solver.choco.view.ModelViewMapper#newBundle()}</li>
 * <li>the duration evaluator is {@link btrplace.solver.choco.duration.DurationEvaluators#newBundle()}</li>
 * <li>the constraint mapper is {@link btrplace.solver.choco.constraint.ConstraintMapper#newBundle()}</li>
 * <li>the {@link btrplace.solver.choco.view.Packing} constraint is {@link btrplace.solver.choco.view.DefaultPacking}</li>
 * <li>the {@link btrplace.solver.choco.view.Cumulatives} view is {@link btrplace.solver.choco.view.DefaultCumulatives}</li>
 * <li>the {@link btrplace.solver.choco.view.AliasedCumulatives} view is {@link btrplace.solver.choco.view.DefaultAliasedCumulatives}</li>
 * </ul>
 *
 * @author Fabien Hermenier
 */
public class DefaultChocoReconfigurationAlgorithmParams implements ChocoReconfigurationAlgorithmParams {

    private ModelViewMapper viewMapper;

    private ConstraintMapper cstrMapper;

    private TransitionFactory amf;

    private boolean optimize = false;

    /**
     * No time limit by default.
     */
    private int timeLimit = 0;

    private boolean repair = false;

    private DurationEvaluators durationEvaluators;

    private int maxEnd = DefaultReconfigurationProblem.DEFAULT_MAX_TIME;

    private int verbosityLevel;

    private Map<String, SolverViewBuilder> solverViewsBuilder;

    /**
     * New set of parameters.
     */
    public DefaultChocoReconfigurationAlgorithmParams() {
        cstrMapper = ConstraintMapper.newBundle();
        durationEvaluators = DurationEvaluators.newBundle();
        viewMapper = ModelViewMapper.newBundle();
        amf = TransitionFactory.newBundle();
        solverViewsBuilder = new HashMap<>();
        //Default solver views
        solverViewsBuilder.put(Packing.VIEW_ID, new DefaultPacking.Builder());
        solverViewsBuilder.put(Cumulatives.VIEW_ID, new DefaultCumulatives.Builder());
        solverViewsBuilder.put(AliasedCumulatives.VIEW_ID, new DefaultAliasedCumulatives.Builder());

    }

    @Override
    public ChocoReconfigurationAlgorithmParams doRepair(boolean b) {
        repair = b;
        return this;
    }

    @Override
    public boolean doRepair() {
        return repair;
    }

    @Override
    public ChocoReconfigurationAlgorithmParams doOptimize(boolean b) {
        optimize = b;
        return this;
    }

    @Override
    public boolean doOptimize() {
        return optimize;
    }

    @Override
    public ModelViewMapper getViewMapper() {
        return viewMapper;
    }

    @Override
    public ChocoReconfigurationAlgorithmParams setViewMapper(ModelViewMapper m) {
        viewMapper = m;
        return this;
    }

    @Override
    public ChocoReconfigurationAlgorithmParams setTimeLimit(int t) {
        timeLimit = t;
        return this;
    }

    @Override
    public int getTimeLimit() {
        return timeLimit;
    }

    @Override
    public ConstraintMapper getConstraintMapper() {
        return cstrMapper;
    }

    @Override
    public ChocoReconfigurationAlgorithmParams setConstraintMapper(ConstraintMapper map) {
        cstrMapper = map;
        return this;
    }

    @Override
    public DurationEvaluators getDurationEvaluators() {
        return durationEvaluators;
    }

    @Override
    public ChocoReconfigurationAlgorithmParams setDurationEvaluators(DurationEvaluators dev) {
        durationEvaluators = dev;
        return this;
    }

    @Override
    public ChocoReconfigurationAlgorithmParams setMaxEnd(int end) {
        maxEnd = end;
        return this;
    }

    @Override
    public int getMaxEnd() {
        return maxEnd;
    }

    @Override
    public ChocoReconfigurationAlgorithmParams setVerbosity(int lvl) {
        verbosityLevel = lvl;
        return this;
    }

    @Override
    public int getVerbosity() {
        return verbosityLevel;
    }

    @Override
    public void setTransitionFactory(TransitionFactory f) {
        this.amf = f;
    }

    @Override
    public TransitionFactory getTransitionFactory() {
        return this.amf;
    }

    @Override
    public void addSolverViewBuilder(SolverViewBuilder b) {
        solverViewsBuilder.put(b.getKey(), b);
    }

    @Override
    public boolean removeSolverViewBuilder(SolverViewBuilder b) {
        return solverViewsBuilder.remove(b.getKey()) != null;
    }

    @Override
    public Collection<SolverViewBuilder> getSolverViews() {
        return solverViewsBuilder.values();
    }
}
