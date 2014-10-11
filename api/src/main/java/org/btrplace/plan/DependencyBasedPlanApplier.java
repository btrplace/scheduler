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

package org.btrplace.plan;

import org.btrplace.model.Model;
import org.btrplace.plan.event.Action;

import java.util.HashSet;
import java.util.Set;

/**
 * A plan applier that relies on the dependencies between the actions composing the plan.
 * Only unblocked actions are executed. Once executed, the unblocked actions are executed.
 * <p>
 * This process is repeated until all the actions are executed. This process is ensure to finish
 * iff their is no cyclic dependencies.
 *
 * @author Fabien Hermenier
 */
public class DependencyBasedPlanApplier extends DefaultPlanApplier {


    /**
     * Make a new applier.
     */
    public DependencyBasedPlanApplier() {
        super();
    }

    @Override
    public Model apply(ReconfigurationPlan p) {
        int nbCommitted = 0;
        ReconfigurationPlanMonitor rpm = new DefaultReconfigurationPlanMonitor(p);
        Set<Action> feasible = new HashSet<>();
        for (Action a : p.getActions()) {
            if (!rpm.isBlocked(a)) {
                feasible.add(a);
            }
        }
        while (nbCommitted != p.getSize()) {
            Set<Action> newFeasible = new HashSet<>();
            for (Action a : feasible) {
                Set<Action> s = rpm.commit(a);
                if (s == null) {
                    return null;
                }
                fireAction(a);
                newFeasible.addAll(s);
                nbCommitted++;
            }
            feasible = newFeasible;
        }

        return rpm.getCurrentModel();
    }

    @Override
    public String toString(ReconfigurationPlan p) {
        StringBuilder b = new StringBuilder();
        for (Action a : p) {
            b.append(p.getDirectDependencies(a)).append(" -> ").append(a).append("\n");
        }
        return b.toString();
    }

}
