/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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
            b.append(String.format("%s -> %s%n", p.getDirectDependencies(a), a));
        }
        return b.toString();
    }

}
