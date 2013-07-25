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
import btrplace.model.constraint.Among;
import btrplace.model.constraint.MinMTTR;
import btrplace.model.constraint.SatConstraint;
import btrplace.solver.SolverException;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithmParams;
import btrplace.solver.choco.MappingFiller;
import btrplace.solver.choco.runner.staticPartitioning.FixedNodeSetsPartitioning;
import btrplace.solver.choco.runner.staticPartitioning.SplittableIndexTest;
import gnu.trove.map.hash.TIntIntHashMap;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

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


    public static final Mapping map = new MappingFiller(mo.getMapping())
            .on(n1, n2, n3, n4)
            .run(n1, vm1, vm2)
            .run(n2, vm3)
            .run(n3, vm4)
            .run(n4, vm5).get();

    @Test
    public void testSplittable() throws SolverException {

        List<VM> vms = Arrays.asList(vm1, vm2, vm3);
        Collection<Collection<Node>> parts = new ArrayList<>();
        parts.add(Arrays.asList(n1, n2));
        parts.add(Arrays.asList(n3));
        parts.add(Arrays.asList(n4));

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
        List<Instance> instances = partitionner.split(new DefaultChocoReconfigurationAlgorithmParams(),
                new Instance(mo, Collections.<SatConstraint>emptyList(), new MinMTTR()));

        TIntIntHashMap vmIndex = SplittableIndexTest.makeVMIndex(instances);
        TIntIntHashMap nodeIndex = SplittableIndexTest.makeNodeIndex(instances);
        splitter.split(single, new Instance(mo, new MinMTTR()), instances, vmIndex, nodeIndex);
        Among a = (Among) instances.get(0).getConstraints().iterator().next();
        Assert.assertEquals(a.getGroupsOfNodes().size(), 1);
        Assert.assertEquals(a.getInvolvedNodes(), Arrays.asList(n1, n2));
        for (Instance i : instances) {
            System.out.println(i.getConstraints());
        }
    }
}
