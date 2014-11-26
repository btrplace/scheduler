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

import java.util.*;

/**
 * An applier that relies on the estimated start moment and
 * the duration of the actions.
 *
 * @author Fabien Hermenier
 */
public class TimeBasedPlanApplier extends DefaultPlanApplier {

    private static Comparator<Action> startFirstComparator = new TimedBasedActionComparator();

    /**
     * Make a new applier.
     */
    public TimeBasedPlanApplier() {
        super();
    }

    @Override
    public Model apply(ReconfigurationPlan p) {
        Model res = p.getOrigin().clone();
        List<Action> actions = new ArrayList<>(p.getActions());
        Collections.sort(actions, startFirstComparator);
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
