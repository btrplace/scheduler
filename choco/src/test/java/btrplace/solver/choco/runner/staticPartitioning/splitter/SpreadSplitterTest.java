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

import btrplace.model.*;
import btrplace.model.constraint.MinMTTR;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.constraint.Spread;
import gnu.trove.map.hash.TIntIntHashMap;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link SpreadSplitter}.
 *
 * @author Fabien Hermenier
 */
public class SpreadSplitterTest {

    @Test
    public void simpleTest() {
        SpreadSplitter splitter = new SpreadSplitter();

        List<Instance> instances = new ArrayList<>();
        Model m0 = new DefaultModel();
        m0.getMapping().addReadyVM(m0.newVM(1));
        Node n1 = m0.newNode();
        m0.getMapping().addOnlineNode(n1);
        m0.getMapping().addRunningVM(m0.newVM(2), n1);
        Model m1 = new DefaultModel();
        m1.getMapping().addReadyVM(m1.newVM(3));
        Node n2 = m1.newNode();
        Node n3 = m1.newNode();
        m1.getMapping().addOnlineNode(n2);
        m1.getMapping().addOnlineNode(n3);
        m1.getMapping().addSleepingVM(m1.newVM(4), n2);
        m1.getMapping().addRunningVM(m1.newVM(5), n3);

        instances.add(new Instance(m0, new ArrayList<SatConstraint>(), new MinMTTR()));
        instances.add(new Instance(m1, new ArrayList<SatConstraint>(), new MinMTTR()));

        Set<VM> all = new HashSet<>(m0.getMapping().getAllVMs());
        all.addAll(m1.getMapping().getAllVMs());

        TIntIntHashMap index = makeIndex(instances);

        //Only VMs in m0
        Spread spreadSingle = new Spread(m0.getMapping().getAllVMs());
        Assert.assertTrue(splitter.split(spreadSingle, null, instances, index));
        Assert.assertTrue(instances.get(0).getConstraints().contains(spreadSingle));
        Assert.assertFalse(instances.get(1).getConstraints().contains(spreadSingle));

        //All the VMs, test the split
        Spread spreadAmong = new Spread(all, false);

        Assert.assertTrue(splitter.split(spreadAmong, null, instances, index));
        Assert.assertTrue(instances.get(0).getConstraints().contains(new Spread(m0.getMapping().getAllVMs(), false)));
        Assert.assertTrue(instances.get(1).getConstraints().contains(new Spread(m1.getMapping().getAllVMs(), false)));
    }

    public static TIntIntHashMap makeIndex(Collection<Instance> instances) {
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
}
