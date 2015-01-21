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

package org.btrplace.scheduler.choco.view;

import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.transition.BootableNode;
import org.btrplace.scheduler.choco.transition.NodeTransition;
import org.btrplace.scheduler.choco.transition.ShutdownableNode;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;

import java.util.HashMap;
import java.util.Map;

/**
 * A solver-side view to store variables that
 * indicate the moment a node is powered on or off.
 * <p>
 * User: Tu Huynh Dang
 * Date: 6/4/13
 * Time: 9:17 PM
 */
public class CPowerView implements ChocoView {

    /**
     * The view identifier.
     */
    public static final String VIEW_ID = "PowerTime";

    private Map<Integer, IntVar> powerStarts;
    private Map<Integer, IntVar> powerEnds;

    /**
     * Make a new view.
     *
     * @param rp the problem to rely on
     */
    public CPowerView(ReconfigurationProblem rp) {
        powerStarts = new HashMap<>(rp.getNodes().length);
        powerEnds = new HashMap<>(rp.getNodes().length);

        for (Node n : rp.getNodes()) {
            NodeTransition na = rp.getNodeAction(n);
            if (na instanceof ShutdownableNode) {
                powerStarts.put(rp.getNode(n), rp.getStart());
                IntVar powerEnd = rp.makeUnboundedDuration("NodeActionType(", n, ").Pe");
                VF.task(na.getHostingEnd(), na.getDuration(), powerEnd);
                powerEnds.put(rp.getNode(n), powerEnd);
                rp.getSolver().post(ICF.arithm(powerEnd,"<=",rp.getEnd()));
            } else if (na instanceof BootableNode) {
                powerStarts.put(rp.getNode(n), na.getStart());
                powerEnds.put(rp.getNode(n), /*na.getHostingEnd()*/rp.getEnd());
            }
        }
    }

    /**
     * Get the moment a given node is on.
     *
     * @param idx the node index
     * @return the variable denoting the moment
     */
    public IntVar getPowerStart(int idx) {
        return powerStarts.get(idx);
    }

    /**
     * Get the moment a given node is off.
     *
     * @param idx the node index
     * @return the variable denoting the moment.
     */
    public IntVar getPowerEnd(int idx) {
        return powerEnds.get(idx);
    }

    @Override
    public String getIdentifier() {
        return VIEW_ID;
    }

    @Override
    public boolean beforeSolve(ReconfigurationProblem rp) {
        return true;
    }

    @Override
    public boolean insertActions(ReconfigurationProblem rp, ReconfigurationPlan p) {
        return true;
    }

    @Override
    public boolean cloneVM(VM vm, VM clone) {
        return true;
    }
}
