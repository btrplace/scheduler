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

package org.btrplace.scheduler.choco.extensions;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.IVariableMonitor;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;

/**
 * Monitor to maintain start + duration = end
 * @author Fabien Hermenier
 */
public class TaskMonitor implements IVariableMonitor<IntVar> {

    private IntVar start;
    private IntVar duration;
    private IntVar end;

    /**
     * Make a new monitor.
     *
     * @param start    the task start moment
     * @param duration the task duration
     * @param end      the task end
     */
    public TaskMonitor(IntVar start, IntVar duration, IntVar end) {
        this.start = start;
        this.duration = duration;
        this.end = end;

        this.start.addMonitor(this);
        this.duration.addMonitor(this);
        this.end.addMonitor(this);
    }

    @Override
    public void onUpdate(IntVar var, IEventType evt) throws ContradictionException {
        // start
        start.updateBounds(end.getLB() - duration.getUB(), end.getUB() - duration.getLB(), this);
        // end
        end.updateBounds(start.getLB() + duration.getLB(), start.getUB() + duration.getUB(), this);
        // duration
        duration.updateBounds(end.getLB() - start.getUB(), end.getUB() - start.getLB(), this);
    }

    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        boolean nrules = false;
        if (var == start) {
            if (evt == IntEventType.INCLOW) {
                nrules = ruleStore.addLowerBoundRule(end);
                nrules |= ruleStore.addUpperBoundRule(duration);
            } else if (evt == IntEventType.DECUPP) {
                nrules = ruleStore.addUpperBoundRule(end);
                nrules |= ruleStore.addLowerBoundRule(duration);
            } else {
                throw new SolverException("TaskMonitor exception");
            }
        } else if (var == end) {
            if (evt == IntEventType.INCLOW) {
                nrules = ruleStore.addLowerBoundRule(start);
                nrules |= ruleStore.addLowerBoundRule(duration);
            } else if (evt == IntEventType.DECUPP) {
                nrules = ruleStore.addUpperBoundRule(start);
                nrules |= ruleStore.addUpperBoundRule(duration);
            } else {
                throw new SolverException("TaskMonitor exception");
            }
        } else if (var == duration) {
            if (evt == IntEventType.INCLOW) {
                nrules = ruleStore.addLowerBoundRule(end);
                nrules |= ruleStore.addUpperBoundRule(start);
            } else if (evt == IntEventType.DECUPP) {
                nrules = ruleStore.addLowerBoundRule(start);
                nrules |= ruleStore.addUpperBoundRule(end);
            } else {
                throw new SolverException("TaskMonitor exception");
            }
        }
        return nrules;
    }
}