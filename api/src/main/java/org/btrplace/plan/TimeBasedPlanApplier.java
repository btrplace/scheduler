/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan;

import org.btrplace.model.Model;
import org.btrplace.plan.event.Action;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * An applier that relies on the estimated start moment and
 * the duration of the actions.
 *
 * @author Fabien Hermenier
 */
public class TimeBasedPlanApplier extends DefaultPlanApplier {

  private static final Comparator<Action> startFirstComparator = new TimedBasedActionComparator();

    @Override
    public Model apply(ReconfigurationPlan p) {
        Model res = p.getOrigin().copy();
        List<Action> actions = new ArrayList<>(p.getActions());
        actions.sort(startFirstComparator);
        for (Action a : actions) {
            if (!a.apply(res)) {
                return null;
            }
            fireAction(a);
        }
        return res;
    }

    @Override
    public String toString(ReconfigurationPlan p) {
        Set<Action> sorted = new TreeSet<>(new TimedBasedActionComparator(true, true));
        sorted.addAll(p.getActions());
        StringBuilder b = new StringBuilder();
        for (Action a : sorted) {
            b.append(a.getStart()).append(':').append(a.getEnd()).append(' ').append(a.toString()).append('\n');
        }
        return b.toString();
    }
}
