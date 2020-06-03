/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Instance;
import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Ban;
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.model.constraint.Online;
import org.btrplace.model.constraint.Running;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link CBan}.
 *
 * @author Fabien Hermenier
 */
public class CBanTest {

    @Test
    public void testBasic() throws SchedulerException {
        Node[] nodes = new Node[5];
        VM[] vms = new VM[5];
        Model mo = new DefaultModel();
        Mapping m = mo.getMapping();
        Set<VM> sVMs = new HashSet<>();
        Set<Node> sNodes = new HashSet<>();
        for (int i = 0; i < vms.length; i++) {
            nodes[i] = mo.newNode();
            vms[i] = mo.newVM();
            m.addOnlineNode(nodes[i]);
            m.addRunningVM(vms[i], nodes[i]);
            if (i % 2 == 0) {
                sVMs.add(vms[i]);
                sNodes.add(nodes[i]);
            }
        }
        Ban b = new Ban(vms[0], sNodes);
        Collection<SatConstraint> s = new HashSet<>();
        s.add(b);
        s.addAll(Running.newRunning(m.getAllVMs()));
        s.addAll(Online.newOnline(m.getAllNodes()));

        DefaultChocoScheduler cra = new DefaultChocoScheduler();
        cra.setTimeLimit(-1);
        ReconfigurationPlan p = cra.solve(mo, s);
        Assert.assertNotNull(p);
        Assert.assertEquals(1, p.getSize());
    }

    /**
     * Test getMisPlaced() in various situations.
     */
    @Test
    public void testGetMisPlaced() {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();
        Node n4 = mo.newNode();
        Node n5 = mo.newNode();
        mo.getMapping().on(n1, n2, n3, n4, n5)
                .run(n1, vm1, vm2)
                .run(n2, vm3)
                .run(n3, vm4)
                .sleep(n4, vm5);

        Set<Node> ns = new HashSet<>(Arrays.asList(n3, n4));

        CBan c = new CBan(new Ban(vm1, ns));
        Instance i = new Instance(mo, Collections.emptyList(), new MinMTTR());
        org.testng.Assert.assertTrue(c.getMisPlacedVMs(i).isEmpty());
        ns.add(mo.newNode());
        org.testng.Assert.assertTrue(c.getMisPlacedVMs(i).isEmpty());
        ns.add(n1);
        Set<VM> bad = c.getMisPlacedVMs(i);
        org.testng.Assert.assertEquals(1, bad.size());
        org.testng.Assert.assertTrue(bad.contains(vm1));
    }
}
