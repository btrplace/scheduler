/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

package org.btrplace.scheduler.choco.constraint.mttr;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Preserve;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vincent Kherbache
 */
public class CMinMTTRTest {

    /**
     * The DC is heavily loaded.
     * Provoked a large amount of backtracks when we relied on a random search
     * @throws Exception
     */
    /*@Test*/
    public void testHeavyLoad() throws Exception {
        Model mo = new DefaultModel();
        ShareableResource cpu = new ShareableResource("core", 7, 1);
        ShareableResource mem = new ShareableResource("mem", 20, 2);
        for (int i = 0; i < 50; i++) {
            Node n = mo.newNode();
            mo.getMapping().addOnlineNode(n);
            for (int j = 0; j < 4; j++) {
                VM v = mo.newVM();
                mo.getMapping().addRunningVM(v, n);
                if (j % 2 == 0) {
                    mem.setConsumption(v, 1);
                }
            }

        }
        List<SatConstraint> l = new ArrayList<>();
        for (Node n : mo.getMapping().getAllNodes()) {
            if (n.id() % 3 == 0) {
                l.addAll(Preserve.newPreserve(mo.getMapping().getRunningVMs(n), "core", 2));
            }
        }
        mo.attach(cpu);
        mo.attach(mem);
        DefaultChocoScheduler sched = new DefaultChocoScheduler();
        //sched.setVerbosity(2);
        ReconfigurationPlan p = sched.solve(mo, l);
        Assert.assertNotNull(p);
        System.err.println(sched.getStatistics());
        //TODO: fragile. Usefull ?
        Assert.assertTrue(sched.getStatistics().getNbBacktracks() < 100);
        System.err.flush();
    }
}
