/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.runner.disjoint.splitter;

import gnu.trove.map.hash.TIntIntHashMap;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Instance;
import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Among;
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.DefaultParameters;
import org.btrplace.scheduler.runner.disjoint.FixedNodeSetsPartitioning;
import org.btrplace.scheduler.runner.disjoint.Instances;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link AmongSplitter}.
 *
 * @author Fabien Hermenier
 */
public class AmongSplitterTest {

    public static final AmongSplitter splitter = new AmongSplitter();

    public static final Model mo = new DefaultModel();
    public static final VM vm1 = mo.newVM();
    public static final VM vm2 = mo.newVM();
    public static final VM vm3 = mo.newVM();
    public static final VM vm4 = mo.newVM();
    public static final VM vm5 = mo.newVM();

    public static final Node n1 = mo.newNode();
    public static final Node n2 = mo.newNode();
    public static final Node n3 = mo.newNode();
    public static final Node n4 = mo.newNode();
    public static final Node n5 = mo.newNode();


    public static final Mapping map = mo.getMapping()
            .on(n1, n2, n3, n4)
            .run(n1, vm1, vm2)
            .run(n2, vm3)
            .run(n3, vm4)
            .run(n4, vm5);

    @Test
    public void testSplittable() throws SchedulerException {

        List<VM> vms = Arrays.asList(vm1, vm2, vm3);
        Collection<Collection<Node>> parts = new ArrayList<>();
        parts.add(Arrays.asList(n1, n2));
        parts.add(Collections.singletonList(n3));
        parts.add(Collections.singletonList(n4));

        Among single = new Among(vms, parts);

        /*
         N1 v1 v2
         N2 v3
         ---
         N3 v4
         --
         N4 v5
         */
        FixedNodeSetsPartitioning partitionner = new FixedNodeSetsPartitioning(parts);
        partitionner.setPartitions(parts);
        List<Instance> instances = partitionner.split(new DefaultParameters(),
                new Instance(mo, Collections.emptyList(), new MinMTTR()));

        TIntIntHashMap vmIndex = Instances.makeVMIndex(instances);
        TIntIntHashMap nodeIndex = Instances.makeNodeIndex(instances);
        splitter.split(single, new Instance(mo, new MinMTTR()), instances, vmIndex, nodeIndex);
        Among a = (Among) instances.get(0).getSatConstraints().iterator().next();
        Assert.assertEquals(a.getGroupsOfNodes().size(), 1);
        Assert.assertEquals(a.getInvolvedNodes(), Arrays.asList(n1, n2));
        for (Instance i : instances) {
            System.out.println(i.getSatConstraints());
        }
    }
}
