/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.plan;

import btrplace.model.Model;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Default implementation for {@link ReconfigurationPlan}.
 *
 * @author Fabien Hermenier
 */
public class DefaultReconfigurationPlan implements ReconfigurationPlan {

    private Model src;

    private Set<Action> actions;

    /**
     * A comparator to sort the actions in the increasing order of their end moment.
     */
    private static Comparator<Action> endFirstComparator = new Comparator<Action>() {
        @Override
        public int compare(Action a1, Action a2) {
            int d = a1.getEnd() - a2.getEnd();
            if (d == 0) {
                if (a1.equals(a2)) {
                    return 0;
                } else {
                    //At this level, we don't care, we just want to be sure the actions will be added
                    return -1;
                }
            } else {
                return d;
            }
        }
    };

    /**
     * Make a new plan that starts for a given model.
     *
     * @param src the source model
     */
    public DefaultReconfigurationPlan(Model src) {
        this.src = src;
        this.actions = new TreeSet<Action>(endFirstComparator);
    }

    @Override
    public Model getOrigin() {
        return src;
    }

    @Override
    public boolean add(Action a) {
        return this.actions.add(a);
    }

    @Override
    public int size() {
        return actions.size();
    }

    @Override
    public int getDuration() {
        int m = 0;
        for (Action a : actions) {
            if (a.getEnd() > m) {
                m = a.getEnd();
            }
        }
        return m;
    }

    @Override
    public Set<Action> getActions() {
        return actions;
    }

    @Override
    public Iterator<Action> iterator() {
        return actions.iterator();
    }

    @Override
    public Model getResult() {
        Model res = src.clone();
        for (Action a : actions) {
            if (!a.apply(res)) {
                return null;
            }
        }
        return res;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Action a : actions) {
            b.append(a.getStart()).append(':').append(a.getEnd()).append(" ").append(a.toString()).append('\n');
        }
        return b.toString();
    }
}
