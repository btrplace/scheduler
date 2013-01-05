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
import btrplace.model.constraint.Fence;
import btrplace.model.constraint.Quarantine;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import junit.framework.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Unit tests for {@link CQuarantine}.
 *
 * @author Fabien Hermenier
 */
public class CQuarantineTest {

    UUID vm1 = UUID.randomUUID();
    UUID vm2 = UUID.randomUUID();
    UUID vm3 = UUID.randomUUID();
    UUID vm4 = UUID.randomUUID();

    UUID n1 = UUID.randomUUID();
    UUID n2 = UUID.randomUUID();
    UUID n3 = UUID.randomUUID();

    @Test
    public void testWithSatisfiedModel() throws SolverException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n2);
        map.addRunningVM(vm3, n2);
        map.addRunningVM(vm4, n3);
        Model mo = new DefaultModel(map);
        Set<UUID> ns = new HashSet<UUID>();
        ns.add(n1);
        ns.add(n2);
        Quarantine q = new Quarantine(ns);
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan p = cra.solve(mo, Collections.<SatConstraint>singleton(q));
        Assert.assertNotNull(p);
    }

    /**
     * A VM try to come into the quarantine zone.
     *
     * @throws SolverException
     */
    @Test
    public void testWithNoSolution1() throws SolverException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n2);
        map.addRunningVM(vm3, n2);
        map.addRunningVM(vm4, n3);
        Model mo = new DefaultModel(map);
        Set<UUID> ns = new HashSet<UUID>();
        ns.add(n1);
        ns.add(n2);
        Quarantine q = new Quarantine(ns);
        List<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        cstrs.add(q);
        cstrs.add(new Fence(Collections.singleton(vm4), Collections.singleton(n1)));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNull(p);
    }

    /**
     * A VM try to leave the quarantine zone.
     *
     * @throws SolverException
     */
    @Test
    public void testWithNoSolution2() throws SolverException {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);

        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n2);
        map.addRunningVM(vm3, n2);
        map.addRunningVM(vm4, n3);
        Model mo = new DefaultModel(map);
        Set<UUID> ns = new HashSet<UUID>();
        ns.add(n1);
        ns.add(n2);
        Quarantine q = new Quarantine(ns);
        List<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        cstrs.add(q);
        cstrs.add(new Fence(Collections.singleton(vm1), Collections.singleton(n2)));
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        Assert.assertNull(p);
    }
}
