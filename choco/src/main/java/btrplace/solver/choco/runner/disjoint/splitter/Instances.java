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

package btrplace.solver.choco.runner.disjoint.splitter;

import btrplace.model.Instance;
import btrplace.model.Mapping;
import btrplace.model.Node;
import btrplace.model.VM;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Collection;

/**
 * Utility class to manipulate multiple instances.
 *
 * @author Fabien Hermenier
 */
public final class Instances {

    /**
     * Utility class, no instantiation.
     */
    private Instances() {
    }

    /**
     * Make an index revealing the position of each VM in a collection
     * of disjoint instances
     *
     * @param instances the collection to browse. Instances are supposed to be disjoint
     * @return the index of every VM. Format {@code VM#id() -> position}
     */
    public static TIntIntHashMap makeVMIndex(Collection<Instance> instances) {
        TIntIntHashMap index = new TIntIntHashMap();
        int p = 0;
        for (Instance i : instances) {
            Mapping m = i.getModel().getMapping();
            for (Node n : m.getOnlineNodes()) {
                for (VM v : m.getRunningVMs(n)) {
                    index.put(v.id(), p);
                }
                for (VM v : m.getSleepingVMs(n)) {
                    index.put(v.id(), p);
                }
            }
            for (VM v : m.getReadyVMs()) {
                index.put(v.id(), p);
            }
            p++;
        }
        return index;
    }

    /**
     * Make an index revealing the position of each node in a collection
     * of disjoint instances
     *
     * @param instances the collection to browse. Instances are supposed to be disjoint
     * @return the index of every node. Format {@code Node#id() -> position}
     */
    public static TIntIntHashMap makeNodeIndex(Collection<Instance> instances) {
        TIntIntHashMap index = new TIntIntHashMap();
        int p = 0;
        for (Instance i : instances) {
            Mapping m = i.getModel().getMapping();
            for (Node n : m.getOfflineNodes()) {
                index.put(n.id(), p);
            }
            for (Node n : m.getOnlineNodes()) {
                index.put(n.id(), p);
            }
            p++;
        }
        return index;
    }
}
