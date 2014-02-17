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

package btrplace.examples;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.MaxOnline;
import btrplace.model.constraint.Offline;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;

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
        int nbNodes = 3;

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
        for (int i = 0; i < 2; i++) {
            Node n = mo.newNode();
            mo.getMapping().addOfflineNode(n);
            rc.setCapacity(n, 10);
        }

        List<SatConstraint> cstrs = new ArrayList<>();
        //cstrs.add(new Overbook(mo.getMapping().getAllNodes(), "cpu", 2, true));
        cstrs.addAll(Offline.newOfflines(mo.getMapping().getOnlineNodes()));
        MaxOnline m = new MaxOnline(mo.getMapping().getAllNodes(), nbNodes + 1);
        //m.setContinuous(true);
        cstrs.add(m);

        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.setMaxEnd(3);
        cra.setVerbosity(2);
        try {
            ReconfigurationPlan p = cra.solve(mo, cstrs);
            System.out.println(p);
        } catch (SolverException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
        return true;
    }
}
