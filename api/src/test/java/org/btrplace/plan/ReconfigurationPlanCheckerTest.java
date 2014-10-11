/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.plan;

import org.btrplace.model.*;
import org.btrplace.model.constraint.SatConstraintChecker;
import org.btrplace.plan.event.BootNode;
import org.btrplace.plan.event.BootVM;
import org.btrplace.plan.event.MigrateVM;
import org.mockito.InOrder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link ReconfigurationPlanChecker}.
 *
 * @author Fabien Hermenier
 */
public class ReconfigurationPlanCheckerTest {

    @Test
    public void tesAddandRemove() {
        ReconfigurationPlanChecker rc = new ReconfigurationPlanChecker();
        SatConstraintChecker<?> chk = mock(SatConstraintChecker.class);
        Assert.assertTrue(rc.addChecker(chk));
        Assert.assertTrue(rc.removeChecker(chk));
        Assert.assertFalse(rc.removeChecker(chk));
    }

    @Test(dependsOnMethods = {"tesAddandRemove"})
    public void testSequencing() throws ReconfigurationPlanCheckerException {
        Model mo = new DefaultModel();
        List<Node> ns = Util.newNodes(mo, 10);
        List<VM> vms = Util.newVMs(mo, 10);

        Mapping m = mo.getMapping();
        m.addOnlineNode(ns.get(0));
        m.addOnlineNode(ns.get(1));
        m.addOfflineNode(ns.get(3));
        m.addReadyVM(vms.get(1));
        m.addRunningVM(vms.get(0), ns.get(0));
        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);
        SatConstraintChecker chk = mock(SatConstraintChecker.class);
        MigrateVM m1 = new MigrateVM(vms.get(0), ns.get(0), ns.get(1), 0, 3);
        BootVM b1 = new BootVM(vms.get(1), ns.get(0), 1, 5);
        BootNode bn = new BootNode(ns.get(3), 3, 6);
        p.add(m1);
        p.add(b1);
        p.add(bn);
        Model res = p.getResult();
        Assert.assertNotNull(res);
        ReconfigurationPlanChecker rc = new ReconfigurationPlanChecker();

        rc.addChecker(chk);

        InOrder order = inOrder(chk);
        rc.check(p);
        order.verify(chk).startsWith(mo);
        order.verify(chk).start(m1);
        order.verify(chk).start(b1);
        order.verify(chk).end(m1);
        order.verify(chk).start(bn);
        order.verify(chk).end(b1);
        order.verify(chk).end(bn);
        order.verify(chk).endsWith(res);
    }

    @Test
    public void testWithNoActions() throws ReconfigurationPlanCheckerException {
        Model mo = new DefaultModel();
        Mapping m = mo.getMapping();
        List<Node> ns = Util.newNodes(mo, 10);
        List<VM> vms = Util.newVMs(mo, 10);
        m.addOnlineNode(ns.get(0));
        m.addOnlineNode(ns.get(1));
        m.addOfflineNode(ns.get(3));
        m.addReadyVM(vms.get(1));
        m.addRunningVM(vms.get(0), ns.get(0));
        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);
        SatConstraintChecker chk = mock(SatConstraintChecker.class);
        ReconfigurationPlanChecker rc = new ReconfigurationPlanChecker();
        Assert.assertTrue(rc.addChecker(chk));

        InOrder order = inOrder(chk);
        rc.check(p);
        order.verify(chk).startsWith(mo);
        order.verify(chk).endsWith(mo);

    }
}
