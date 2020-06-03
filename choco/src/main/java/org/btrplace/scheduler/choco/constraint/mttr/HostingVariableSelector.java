/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint.mttr;

import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.variables.IntVar;

/**
 * A variable selector that focuses the placement variables of slices.
 * Choco will try to instantiate the variables following the slice ordering
 *
 * @author Fabien Hermenier
 */
public class HostingVariableSelector extends FirstFail {

  private final OnStableNodeFirst schedHeuristic;

    /**
     * Make a new heuristic.
     * By default, the heuristic doesn't touch the scheduling constraints.
     *
     * @param s the csp to solve
     * @param sched the scheduling heuristic to notify when the placement is invalidated
     */
    public HostingVariableSelector(org.chocosolver.solver.Model s, OnStableNodeFirst sched) {
        super(s);
        this.schedHeuristic = sched;
    }

    /**
     * @param hosts the variables denoting the VMs next host
     */
    @Override
    public IntVar getVariable(IntVar[] hosts) {
        IntVar v = super.getVariable(hosts);
        if (schedHeuristic != null) {
            schedHeuristic.invalidPlacement();
        }
        return v;
    }

}
