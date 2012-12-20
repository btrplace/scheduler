/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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

package btrplace.solver.choco.constraint;

import btrplace.model.*;
import btrplace.model.constraint.Ban;
import btrplace.model.constraint.Online;
import btrplace.model.constraint.Running;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Unit tests for {@link CBan}.
 *
 * @author Fabien Hermenier
 */
public class CBanTest {

/*    @Test
    public void testInstantiation() {
        Ban b = new Ban(Collections.singleton(UUID.randomUUID()), Collections.singleton(UUID.randomUUID()));
        CBan c = new CBan(b);
    }*/

    @Test
    public void testBasic() throws SolverException {
        UUID[] nodes = new UUID[5];
        UUID[] vms = new UUID[5];
        Mapping m = new DefaultMapping();
        Set<UUID> sVMs = new HashSet<UUID>();
        Set<UUID> sNodes = new HashSet<UUID>();
        for (int i = 0; i < vms.length; i++) {
            nodes[i] = UUID.randomUUID();
            vms[i] = UUID.randomUUID();
            m.addOnlineNode(nodes[i]);
            m.addRunningVM(vms[i], nodes[i]);
            if (i % 2 == 0) {
                sVMs.add(vms[i]);
                sNodes.add(nodes[i]);
            }
        }

        Model mo = new DefaultModel(m);
        Ban b = new Ban(sVMs, sNodes);
        Collection<SatConstraint> s = new HashSet<SatConstraint>();
        s.add(b);
        s.add(new Running(m.getAllVMs()));
        s.add(new Online(m.getAllNodes()));

        DefaultChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.labelVariables(true);
        cra.setTimeLimit(-1);
        ReconfigurationPlan p = cra.solve(mo, s);
        Assert.assertEquals(SatConstraint.Sat.SATISFIED, b.isSatisfied(p.getResult()));
        Assert.assertEquals(3, p.getSize());
    }

    /**
     * Test getMisPlaced() in various situations.
     */
    @Test
    public void testGetMisPlaced() {
        Mapping m = new DefaultMapping();
        UUID n1 = UUID.randomUUID();
        UUID n2 = UUID.randomUUID();
        UUID n3 = UUID.randomUUID();
        UUID n4 = UUID.randomUUID();
        UUID n5 = UUID.randomUUID();

        UUID vm1 = UUID.randomUUID();
        UUID vm2 = UUID.randomUUID();
        UUID vm3 = UUID.randomUUID();
        UUID vm4 = UUID.randomUUID();
        UUID vm5 = UUID.randomUUID();

        m.addOnlineNode(n1);
        m.addOnlineNode(n2);
        m.addOnlineNode(n3);
        m.addOnlineNode(n4);
        m.addOfflineNode(n5);

        m.addRunningVM(vm1, n1);
        m.addRunningVM(vm2, n1);
        m.addRunningVM(vm3, n2);
        m.addRunningVM(vm4, n3);
        m.addSleepingVM(vm5, n4);

        Set<UUID> vms = new HashSet<UUID>();
        Set<UUID> ns = new HashSet<UUID>();

        vms.add(vm1);
        vms.add(vm2);
        ns.add(n3);
        ns.add(n4);
        CBan c = new CBan(new Ban(vms, ns));
        Model mo = new DefaultModel(m);
        org.testng.Assert.assertTrue(c.getMisPlacedVMs(mo).isEmpty());
        ns.add(vm4);
        org.testng.Assert.assertTrue(c.getMisPlacedVMs(mo).isEmpty());
        vms.add(vm5);
        org.testng.Assert.assertTrue(c.getMisPlacedVMs(mo).isEmpty());
        ns.add(n1);
        Set<UUID> bad = c.getMisPlacedVMs(mo);
        org.testng.Assert.assertEquals(2, bad.size());
        org.testng.Assert.assertTrue(bad.contains(vm1) && bad.contains(vm2));
    }
}
