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

package org.btrplace.scheduler.choco;

import org.btrplace.scheduler.choco.constraint.ChocoMapper;
import org.btrplace.scheduler.choco.duration.DurationEvaluators;
import org.btrplace.scheduler.choco.transition.TransitionFactory;
import org.btrplace.scheduler.choco.view.ChocoView;
import org.btrplace.scheduler.choco.view.DefaultAliasedCumulatives;
import org.btrplace.scheduler.choco.view.DefaultCumulatives;
import org.btrplace.scheduler.choco.view.VectorPacking;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of {@link Parameters}.
 * <ul>
 * <li>repair mode is disabled</li>
 * <li>no time limit</li>
 * <li>the transition factory comes from {@link org.btrplace.scheduler.choco.transition.TransitionFactory#newBundle()}</li>
 * <li>the duration evaluator is {@link org.btrplace.scheduler.choco.duration.DurationEvaluators#newBundle()}</li>
 * <li>the api to choco element mapper is {@link ChocoMapper#newBundle()}</li>
 * <li>the {@link org.btrplace.scheduler.choco.view.Packing} constraint is {@link org.btrplace.scheduler.choco.view.VectorPacking}</li>
 * <li>the {@link org.btrplace.scheduler.choco.view.Cumulatives} view is {@link org.btrplace.scheduler.choco.view.DefaultCumulatives}</li>
 * <li>the {@link org.btrplace.scheduler.choco.view.AliasedCumulatives} view is {@link org.btrplace.scheduler.choco.view.DefaultAliasedCumulatives}</li>
 * </ul>
 *
 * @author Fabien Hermenier
 */
public class DefaultParameters implements Parameters {

    private ChocoMapper mapper;

    private TransitionFactory amf;

    private boolean optimize = false;

    private long seed = 0;

    private List<Class<? extends ChocoView>> views;
    /**
     * No time limit by default.
     */
    private int timeLimit = -1;

    private boolean repair = false;

    private DurationEvaluators durationEvaluators;

    private int maxEnd = DefaultReconfigurationProblem.DEFAULT_MAX_TIME;

    private int verbosityLevel;

    /**
     * New set of parameters.
     */
    public DefaultParameters() {
        mapper = ChocoMapper.newBundle();
        durationEvaluators = DurationEvaluators.newBundle();
        amf = TransitionFactory.newBundle();
        //Default solver views
        views = new ArrayList<>();
        views.add(VectorPacking.class);
        views.add(DefaultCumulatives.class);
        views.add(DefaultAliasedCumulatives.class);
    }

    @Override
    public Parameters doRepair(boolean b) {
        repair = b;
        return this;
    }

    @Override
    public boolean doRepair() {
        return repair;
    }

    @Override
    public Parameters doOptimize(boolean b) {
        optimize = b;
        return this;
    }

    @Override
    public boolean doOptimize() {
        return optimize;
    }

    @Override
    public Parameters setRandomSeed(long s) {
        seed = s;
        return this;
    }

    @Override
    public long getRandomSeed() {
        return seed;
    }

    @Override
    public Parameters setTimeLimit(int t) {
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
    public Parameters setMapper(ChocoMapper map) {
        mapper = map;
        return this;
    }

    @Override
    public DurationEvaluators getDurationEvaluators() {
        return durationEvaluators;
    }

    @Override
    public Parameters setDurationEvaluators(DurationEvaluators dev) {
        durationEvaluators = dev;
        return this;
    }

    @Override
    public Parameters setMaxEnd(int end) {
        maxEnd = end;
        return this;
    }

    @Override
    public int getMaxEnd() {
        return maxEnd;
    }

    @Override
    public Parameters setVerbosity(int lvl) {
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
    public boolean addChocoView(Class<? extends ChocoView> c) {
        try {
            c.getDeclaredConstructor();
            return views.add(c);
        } catch (Exception e) {
            throw new IllegalArgumentException("No default constructor available for '" + c.getName() + "'");
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
}
