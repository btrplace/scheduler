package org.btrplace.scheduler.choco.view;

import org.btrplace.model.Model;
import org.btrplace.scheduler.SchedulerException;

import java.util.*;

/**
 * Utility class to manipulate collection of {@link ChocoView}.
 */
public class ChocoViews {

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
                throw new SchedulerException(mo, "Cyclic dependencies among the following views: " + remaining);
            }
        }
        return solved;
    }
}
