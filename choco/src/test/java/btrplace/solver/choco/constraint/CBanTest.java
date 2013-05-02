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
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import btrplace.solver.choco.MappingBuilder;
import btrplace.test.PremadeElements;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link CBan}.
 *
 * @author Fabien Hermenier
 */
public class CBanTest implements PremadeElements {

    @Test
    public void testBasic() throws SolverException {
        UUID[] nodes = new UUID[5];
        UUID[] vms = new UUID[5];
        Mapping m = new DefaultMapping();
        Set<UUID> sVMs = new HashSet<>();
        Set<UUID> sNodes = new HashSet<>();
        for (int i = 0; i < vms.length; i++) {
            nodes[i] = new UUID(1, i);
            vms[i] = new UUID(0, i);
            m.addOnlineNode(nodes[i]);
            m.addRunningVM(vms[i], nodes[i]);
            if (i % 2 == 0) {
                sVMs.add(vms[i]);
                sNodes.add(nodes[i]);
            }
        }

        Model mo = new DefaultModel(m);
        Ban b = new Ban(sVMs, sNodes);
        Collection<SatConstraint> s = new HashSet<>();
        s.add(b);
        s.add(new Running(m.getAllVMs()));
        s.add(new Online(m.getAllNodes()));

        DefaultChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.labelVariables(true);
        cra.setVerbosity(0);
        cra.setTimeLimit(-1);
        ReconfigurationPlan p = cra.solve(mo, s);
        Assert.assertNotNull(p);
        System.out.println(p);

        Assert.assertEquals(3, p.getSize());
    }

    /**
     * Test getMisPlaced() in various situations.
     */
    @Test
    public void testGetMisPlaced() {
        Mapping m = new MappingBuilder().on(n1, n2, n3, n4, n5)
                .run(n1, vm1, vm2)
                .run(n2, vm3)
                .run(n3, vm4)
                .sleep(n4, vm5).build();

        Set<UUID> vms = new HashSet<>(Arrays.asList(vm1, vm2));
        Set<UUID> ns = new HashSet<>(Arrays.asList(n3, n4));

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
