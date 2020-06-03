/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
@SuppressWarnings("squid:S106")
public class Decommissionning implements Example {

    @Override
    public void run() {
        int ratio = 1;
        int nbPCPUs = 4;
        int nbNodes = 2;

        //The current DC
        Model mo = new DefaultModel();
        for (int i = 0; i < nbNodes; i++) {
            Node n = mo.newNode();
            mo.getMapping().addOnlineNode(n);

            // 4 VMs per node
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
        cra.setVerbosity(1);
        ReconfigurationPlan p = cra.solve(mo, cstrs);
        System.out.println(p);
        System.out.println(cra.getStatistics());
    }
}
