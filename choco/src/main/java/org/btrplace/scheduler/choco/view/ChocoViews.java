/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.view;

import org.btrplace.model.Model;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.SchedulerModelingException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

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
     * @param base the identifier of the core views.
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
                throw new SchedulerModelingException(mo, "Missing dependencies or cyclic dependencies prevent from using: " + remaining);
            }
        }
        return solved;
    }
}
