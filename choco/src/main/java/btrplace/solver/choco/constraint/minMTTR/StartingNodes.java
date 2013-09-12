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

package btrplace.solver.choco.constraint.minMTTR;

import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.actionModel.NodeActionModel;
import choco.kernel.solver.search.integer.AbstractIntVarSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Very basic variable selector that focus the moment where node actions consume.
 *
 * @author Fabien Hermenier
 */
public class StartingNodes extends AbstractIntVarSelector {

    private NodeActionModel[] actions;

    private String lbl;

    private ReconfigurationProblem rp;

    /**
     * Make a new heuristic.
     *
     * @param l           the heuristic label (for debugging purpose)
     * @param p           the problem to consider
     * @param nodeActions the actions to consider
     */
    public StartingNodes(String l, ReconfigurationProblem p, NodeActionModel[] nodeActions) {
        super(p.getSolver());
        actions = nodeActions;
        this.lbl = l;
        this.rp = p;
    }

    @Override
    public IntDomainVar selectVar() {
        for (NodeActionModel na : actions) {
            if (!na.getStart().isInstantiated()) {
                return na.getStart();
            }
        }
        rp.getLogger().debug("{} - no more nodes to handle", lbl);
        return null;
    }
}
