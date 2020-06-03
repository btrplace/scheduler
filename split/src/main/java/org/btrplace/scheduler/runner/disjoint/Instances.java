/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.runner.disjoint;

import gnu.trove.map.hash.TIntIntHashMap;
import org.btrplace.model.Instance;
import org.btrplace.model.Mapping;
import org.btrplace.model.Node;
import org.btrplace.model.VM;

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
