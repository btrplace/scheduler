/*
 * Copyright (c) 2017 University Nice Sophia Antipolis
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

import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.choco.constraint.ChocoMapper;
import org.btrplace.scheduler.choco.duration.DurationEvaluators;
import org.btrplace.scheduler.choco.transition.TransitionFactory;
import org.btrplace.scheduler.choco.view.ChocoView;
import org.btrplace.scheduler.choco.view.DefaultAliasedCumulatives;
import org.btrplace.scheduler.choco.view.DefaultCumulatives;
import org.btrplace.scheduler.choco.view.VectorPacking;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.trailing.EnvironmentTrailing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Default implementation of {@link Parameters}.
 * <ul>
 * <li>repair mode is disabled</li>
 * <li>no time limit</li>
 * <li>a default horizon of 1 hour</li>
 * <li>the transition factory comes from {@link org.btrplace.scheduler.choco.transition.TransitionFactory#newBundle()}</li>
 * <li>the duration evaluator is {@link org.btrplace.scheduler.choco.duration.DurationEvaluators#newBundle()}</li>
 * <li>the api to choco element mapper is {@link ChocoMapper#newBundle()}</li>
 * <li>the {@link org.btrplace.scheduler.choco.view.Packing} constraint is {@link org.btrplace.scheduler.choco.view.VectorPacking}</li>
 * <li>the {@link org.btrplace.scheduler.choco.view.Cumulatives} view is {@link org.btrplace.scheduler.choco.view.DefaultCumulatives}</li>
 * <li>the {@link org.btrplace.scheduler.choco.view.AliasedCumulatives} view is {@link org.btrplace.scheduler.choco.view.DefaultAliasedCumulatives}</li>
 * <li>The {@link IEnvironment} is the default choco trailing environment. For large scale experiment, use</li>
 * </ul>
 *
 * @author Fabien Hermenier
 */
public class DefaultParameters implements Parameters {

    private ChocoMapper mapper;

    private TransitionFactory amf;

    private EnvironmentFactory envf;

    private boolean optimize = false;

    private long seed = 0;

    private List<Class<? extends ChocoView>> views;
    /**
     * No time limit by default.
     */
    private int timeLimit = -1;

    private boolean repair = false;

    private DurationEvaluators durationEvaluators;

    private List<BiConsumer<ReconfigurationProblem, ReconfigurationPlan>> solutionListeners;

    /**
     * Default horizon is one hour.
     */
    private int maxEnd = 3600;

    private int verbosityLevel;

    /**
     * New set of parameters.
     */
    public DefaultParameters() {
        mapper = ChocoMapper.newBundle();
        durationEvaluators = DurationEvaluators.newBundle();
        amf = TransitionFactory.newBundle();
        envf = mo -> new EnvironmentTrailing();
        //Default solver views
        views = new ArrayList<>();
        views.add(VectorPacking.class);
        views.add(DefaultCumulatives.class);
        views.add(DefaultAliasedCumulatives.class);

        solutionListeners = new ArrayList<>();
    }

    /**
     * Copy constructor for the parameters.
     *
     * @param ps the parameters to copy
     */
    public DefaultParameters(Parameters ps) {
        seed = ps.getRandomSeed();
        amf = ps.getTransitionFactory();
        optimize = ps.doOptimize();
        seed = ps.getRandomSeed();
        timeLimit = ps.getTimeLimit();
        repair = ps.doRepair();
        durationEvaluators = ps.getDurationEvaluators();
        maxEnd = ps.getMaxEnd();
        verbosityLevel = ps.getVerbosity();
        views = ps.getChocoViews();
        mapper = ps.getMapper();
        envf = ps.getEnvironmentFactory();
        solutionListeners = ps.solutionListeners();
    }

    @Override
    public DefaultParameters doRepair(boolean b) {
        repair = b;
        return this;
    }

    @Override
    public boolean doRepair() {
        return repair;
    }

    @Override
    public DefaultParameters doOptimize(boolean b) {
        optimize = b;
        return this;
    }

    @Override
    public boolean doOptimize() {
        return optimize;
    }

    @Override
    public DefaultParameters setRandomSeed(long s) {
        seed = s;
        return this;
    }

    @Override
    public long getRandomSeed() {
        return seed;
    }

    @Override
    public DefaultParameters setTimeLimit(int t) {
        timeLimit = t;
        return this;
    }

    @Override
    public int getTimeLimit() {
        return timeLimit;
    }

    @Override
    public ChocoMapper getMapper() {
        return mapper;
    }

    @Override
    public DefaultParameters setMapper(ChocoMapper map) {
        mapper = map;
        return this;
    }

    @Override
    public DurationEvaluators getDurationEvaluators() {
        return durationEvaluators;
    }

    @Override
    public DefaultParameters setDurationEvaluators(DurationEvaluators dev) {
        durationEvaluators = dev;
        return this;
    }

    @Override
    public DefaultParameters setMaxEnd(int end) {
        maxEnd = end;
        return this;
    }

    @Override
    public int getMaxEnd() {
        return maxEnd;
    }

    @Override
    public DefaultParameters setVerbosity(int lvl) {
        verbosityLevel = lvl;
        return this;
    }

    @Override
    public int getVerbosity() {
        return verbosityLevel;
    }

    @Override
    public DefaultParameters setTransitionFactory(TransitionFactory f) {
        this.amf = f;
        return this;
    }

    @Override
    public TransitionFactory getTransitionFactory() {
        return this.amf;
    }

    @Override
    public boolean addChocoView(Class<? extends ChocoView> c) {
        try {
            c.getDeclaredConstructor();
            return views.add(c);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("No default constructor available for '" + c.getName() + "'", e);
        }
    }

    @Override
    public boolean removeChocoView(Class<? extends ChocoView> v) {
        return views.remove(v);
    }

    @Override
    public List<Class<? extends ChocoView>> getChocoViews() {
        return views;
    }

    @Override
    public EnvironmentFactory getEnvironmentFactory() {
        return envf;
    }

    @Override
    public Parameters setEnvironmentFactory(EnvironmentFactory f) {
        envf = f;
        return this;
    }

    @Override
    public Parameters addSolutionListener(BiConsumer<ReconfigurationProblem, ReconfigurationPlan> consumer) {
        this.solutionListeners.add(consumer);
        return this;
    }

    @Override
    public List<BiConsumer<ReconfigurationProblem, ReconfigurationPlan>> solutionListeners() {
        return Collections.unmodifiableList(solutionListeners);
    }
}
