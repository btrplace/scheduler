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
 * Created by fhermeni on 01/04/2016.
 */
public class TaskMonitor implements IVariableMonitor<IntVar> {

    private IntVar S;
    private IntVar D;
    private IntVar E;

    public TaskMonitor(IntVar S, IntVar D, IntVar E) {
        this.S = S;
        this.D = D;
        this.E = E;

        S.addMonitor(this);
        D.addMonitor(this);
        E.addMonitor(this);
    }

    @Override
    public void onUpdate(IntVar var, IEventType evt) throws ContradictionException {
        // start
        S.updateBounds(E.getLB() - D.getUB(), E.getUB() - D.getLB(), this);
        // end
        E.updateBounds(S.getLB() + D.getLB(), S.getUB() + D.getUB(), this);
        // duration
        D.updateBounds(E.getLB() - S.getUB(), E.getUB() - S.getLB(), this);
    }

    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        boolean nrules = false;
        if (var == S) {
            if (evt == IntEventType.INCLOW) {
                nrules = ruleStore.addLowerBoundRule(E);
                nrules |= ruleStore.addUpperBoundRule(D);
            } else if (evt == IntEventType.DECUPP) {
                nrules = ruleStore.addUpperBoundRule(E);
                nrules |= ruleStore.addLowerBoundRule(D);
            } else {
                throw new SolverException("TaskMonitor exception");
            }
        } else if (var == E) {
            if (evt == IntEventType.INCLOW) {
                nrules = ruleStore.addLowerBoundRule(S);
                nrules |= ruleStore.addLowerBoundRule(D);
            } else if (evt == IntEventType.DECUPP) {
                nrules = ruleStore.addUpperBoundRule(S);
                nrules |= ruleStore.addUpperBoundRule(D);
            } else {
                throw new SolverException("TaskMonitor exception");
            }
        } else if (var == D) {
            if (evt == IntEventType.INCLOW) {
                nrules = ruleStore.addLowerBoundRule(E);
                nrules |= ruleStore.addUpperBoundRule(S);
            } else if (evt == IntEventType.DECUPP) {
                nrules = ruleStore.addLowerBoundRule(S);
                nrules |= ruleStore.addUpperBoundRule(E);
            } else {
                throw new SolverException("TaskMonitor exception");
            }
        }
        return nrules;
    }
}