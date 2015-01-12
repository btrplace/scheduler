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

package org.btrplace.examples;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.MaxOnline;
import org.btrplace.model.constraint.Offline;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Decommissionning implements Example {

    @Override
    public boolean run() {
        int ratio = 1;
        int nbPCPUs = 4;
        int nbNodes = 2;

        //The current DC
        Model mo = new DefaultModel();
        for (int i = 0; i < nbNodes; i++) {
            Node n = mo.newNode();
            mo.getMapping().addOnlineNode(n);

            //16 VMs on it
            for (int j = 0; j < ratio * nbPCPUs; j++) {
                VM v = mo.newVM();
                mo.getMapping().addRunningVM(v, n);
            }
        }

        //Resource allocation
        ShareableResource rc = new ShareableResource("cpu", 8, 1);
        mo.attach(rc);

        //The new DC
        for (int i = 0; i < nbNodes; i++) {
            Node n = mo.newNode();
            mo.getMapping().addOfflineNode(n);
            rc.setCapacity(n, 10);
        }

        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.addAll(Offline.newOffline(mo.getMapping().getOnlineNodes()));
        MaxOnline m = new MaxOnline(mo.getMapping().getAllNodes(), nbNodes + 1, true);
        cstrs.add(m);

        ChocoScheduler cra = new DefaultChocoScheduler();
        cra.setMaxEnd(3);
        try {
            ReconfigurationPlan p = cra.solve(mo, cstrs);
            System.out.println(p);
        } catch (SchedulerException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
        return true;
    }
}
