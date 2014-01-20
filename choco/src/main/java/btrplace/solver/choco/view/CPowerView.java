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

package btrplace.solver.choco.view;

import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.actionModel.BootableNodeModel;
import btrplace.solver.choco.actionModel.NodeActionModel;
import btrplace.solver.choco.actionModel.ShutdownableNodeModel;
import solver.Solver;
import solver.variables.IntVar;

import java.util.HashMap;
import java.util.Map;

/**
 * A solver-side view to store variables that
 * indicate the moment a node is powered on or off.
 * <p/>
 * User: Tu Huynh Dang
 * Date: 6/4/13
 * Time: 9:17 PM
 */
public class CPowerView implements ChocoModelView {

    /**
     * The view identifier .
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
        Solver solver = rp.getSolver();
        powerStarts = new HashMap<>(rp.getNodes().length);
        powerEnds = new HashMap<>(rp.getNodes().length);

        for (Node n : rp.getNodes()) {
            NodeActionModel na = rp.getNodeAction(n);
            if (na instanceof ShutdownableNodeModel) {
                powerStarts.put(rp.getNode(n), rp.getStart());
                IntVar powerEnd = rp.makeUnboundedDuration("NodeAction(", n, ").Pe");
                solver.post(solver.eq(powerEnd, solver.plus(na.getHostingEnd(), na.getDuration())));
                powerEnds.put(rp.getNode(n), powerEnd);
            } else if (na instanceof BootableNodeModel) {
                powerStarts.put(rp.getNode(n), na.getStart());
                powerEnds.put(rp.getNode(n), na.getHostingEnd());
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