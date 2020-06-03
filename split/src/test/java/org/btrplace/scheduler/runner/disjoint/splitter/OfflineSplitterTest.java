/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.runner.disjoint.splitter;

import gnu.trove.map.hash.TIntIntHashMap;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Instance;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.model.constraint.Offline;
import org.btrplace.scheduler.runner.disjoint.Instances;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit test for {@link org.btrplace.scheduler.choco.runner.disjoint.splitter.OfflineSplitter}.
 *
 * @author Fabien Hermenier
 */
public class OfflineSplitterTest {

    @Test
    public void simpleTest() {
        OfflineSplitter splitter = new OfflineSplitter();

        List<Instance> instances = new ArrayList<>();
        Model m0 = new DefaultModel();
        Node n = m0.newNode();
        m0.getMapping().addOfflineNode(n);
        m0.getMapping().addOfflineNode(m0.newNode(1));

        Model m1 = new DefaultModel();

        m1.getMapping().addOfflineNode(m1.newNode(2));
        m1.getMapping().addOfflineNode(m1.newNode(3));

        instances.add(new Instance(m0, new ArrayList<>(), new MinMTTR()));
        instances.add(new Instance(m1, new ArrayList<>(), new MinMTTR()));

        Set<Node> all = new HashSet<>(m0.getMapping().getAllNodes());
        all.addAll(m1.getMapping().getAllNodes());

        TIntIntHashMap nodeIndex = Instances.makeNodeIndex(instances);

        //Only nodes in m0
        Offline oSimple = new Offline(n);
        Assert.assertTrue(splitter.split(oSimple, null, instances, new TIntIntHashMap(), nodeIndex));
        Assert.assertTrue(instances.get(0).getSatConstraints().contains(oSimple));
        Assert.assertFalse(instances.get(1).getSatConstraints().contains(oSimple));
    }
}
