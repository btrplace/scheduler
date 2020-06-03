/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.view;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.btrplace.model.Node;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.extensions.TaskMonitor;
import org.btrplace.scheduler.choco.transition.BootableNode;
import org.btrplace.scheduler.choco.transition.NodeTransition;
import org.btrplace.scheduler.choco.transition.ShutdownableNode;
import org.chocosolver.solver.variables.IntVar;

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

    private TIntObjectMap<IntVar> powerStarts;
    private TIntObjectMap<IntVar> powerEnds;

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem rp) throws SchedulerException {
        powerStarts = new TIntObjectHashMap<>(rp.getNodes().size());
        powerEnds = new TIntObjectHashMap<>(rp.getNodes().size());

        for (Node n : rp.getNodes()) {
            NodeTransition na = rp.getNodeAction(n);
            if (na instanceof ShutdownableNode) {
                powerStarts.put(rp.getNode(n), rp.getStart());
                IntVar powerEnd = rp.makeUnboundedDuration("NodeActionType(", n, ").Pe");
                TaskMonitor.build(na.getHostingEnd(), na.getDuration(), powerEnd);
                powerEnds.put(rp.getNode(n), powerEnd);
                rp.getModel().post(rp.getModel().arithm(powerEnd, "<=", rp.getEnd()));
            } else if (na instanceof BootableNode) {
                powerStarts.put(rp.getNode(n), na.getStart());
                powerEnds.put(rp.getNode(n), rp.getEnd());
            }
        }
        return true;
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

}
