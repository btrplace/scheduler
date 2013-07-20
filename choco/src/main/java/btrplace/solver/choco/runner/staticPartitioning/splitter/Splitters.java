/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
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

package btrplace.solver.choco.runner.staticPartitioning.splitter;

import btrplace.model.Mapping;
import btrplace.model.Node;
import btrplace.model.SubSet;
import btrplace.model.VM;
import gnu.trove.set.hash.THashSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Utility class to ease set splitting.
 *
 * @author Fabien Hermenier
 */
public final class Splitters {

    /**
     * No instantiation.
     */
    private Splitters() {
    }

    /**
     * Extract from a given set {@code s} the elements present in {@code in}.
     * This elements are removed for {@code s}
     *
     * @param s   the set to browse
     * @param in  the elements to search inside {@code s}
     * @param <T> the element type
     * @return the elements in {@code s} that was in {@code in}
     */
    public static <T> Set<T> extractInside(Collection<T> s, Collection<T> in) {
        /*Set<T> res = new HashSet<>();
        extractInside(s, in, res);*/
        System.err.println(s.size() + " restricted to " + in.size());
        return new SubSet<T>((Set<T>)s, (Set<T>)in);
        //return res;
    }

    /**
     * Extract some elements from a given set.
     *
     * @param s    the set to browse
     * @param base the elements to extract from {@code s}
     * @param res  the set where extracted elements while be putted
     * @param <T>  the element type
     */
    public static <T> void extractInside(Collection<T> s, Collection<T> base, Set<T> res) {
        for (Iterator<T> ite = s.iterator(); ite.hasNext(); ) {
            T v = ite.next();
            if (base.contains(v)) {
                ite.remove();
                res.add(v);
            }
        }
    }

    /**
     * Extract from a set of nodes, all nodes that are online or offline in a mapping.
     *
     * @param base the set of nodes to browse
     * @param m    the mapping that contains the nodes to extract from {@code base}
     * @return the extracted nodes.
     */
    public static Set<Node> extractNodesIn(Collection<Node> base, Mapping m) {
        Set<Node> res = new HashSet<>();
        //Cheaper that extracting from m.getAllNodes()
        extractInside(base, m.getOfflineNodes(), res);
        extractInside(base, m.getOnlineNodes(), res);
        return res;
    }

    /**
     * Extract from a set of VMs, all VMs that are running, sleeping ,or ready in a mapping.
     *
     * @param base the set of VMs to browse
     * @param m    the mapping that contains the VMs to extract from {@code base}
     * @return the extracted VMs.
     */
    public static Set<VM> extractVMsIn(Collection<VM> base, Mapping m) {
        Set<VM> res = new THashSet<>();
        //Cheaper that extracting from m.getAllVMs();
        extractInside(base, m.getRunningVMs(), res);
        extractInside(base, m.getSleepingVMs(), res);
        extractInside(base, m.getReadyVMs(), res);
        c++;
        return res;
    }

    public static int c = 0;
}
