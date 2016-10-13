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

package org.btrplace.scheduler.choco.view;

import org.btrplace.model.Model;
import org.btrplace.scheduler.SchedulerException;

import java.util.*;

/**
 * Utility class to manipulate collection of {@link ChocoView}.
 */
public class ChocoViews {

    /**
     * Utility class. No instantiations.
     */
    private ChocoViews() {

    }
    /**
     * Flatten the views while considering their dependencies.
     * Operations over the views that respect the iteration order, satisfies the dependencies.
     *
     * @param mo    the model
     * @param views the associated solver views
     * @return the list of views, dependency-free
     * @throws SchedulerException if there is a cyclic dependency
     */
    public static List<ChocoView> resolveDependencies(Model mo, List<ChocoView> views, Collection<String> base) throws SchedulerException {
        Set<String> done = new HashSet<>(base);
        List<ChocoView> remaining = new ArrayList<>(views);
        List<ChocoView> solved = new ArrayList<>();
        while (!remaining.isEmpty()) {
            ListIterator<ChocoView> ite = remaining.listIterator();
            boolean blocked = true;
            while (ite.hasNext()) {
                ChocoView s = ite.next();
                if (done.containsAll(s.getDependencies())) {
                    ite.remove();
                    done.add(s.getIdentifier());
                    solved.add(s);
                    blocked = false;
                }
            }
            if (blocked) {
                throw new SchedulerException(mo, "Missing dependencies or cyclic dependencies prevent from using: " + remaining);
            }
        }
        return solved;
    }
}
