/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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

package btrplace.solver.choco.actionModel;

import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ActionModel;
import btrplace.solver.choco.Slice;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.UUID;

/**
 * Model an action that let a node offline.
 *
 * @author Fabien Hermenier
 */
public class StayOfflineNodeModel implements ActionModel {

    private UUID node;

    /**
     * Make a new model.
     *
     * @param e the node managed by the action
     * @throws SolverException if an error occurred
     */
    public StayOfflineNodeModel(UUID e) throws SolverException {
        node = e;
    }

    @Override
    public boolean insertActions(ReconfigurationPlan plan) {
        return true;
    }

    @Override
    public IntDomainVar getStart() {
        return null;
    }

    @Override
    public IntDomainVar getEnd() {
        return null;
    }

    @Override
    public IntDomainVar getDuration() {
        return null;
    }

    @Override
    public Slice getCSlice() {
        return null;
    }

    @Override
    public Slice getDSlice() {
        return null;
    }

    @Override
    public IntDomainVar getState() {
        return null;
    }

    /**
     * Get the node manipulated by the action.
     *
     * @return the node identifier
     */
    public UUID getNode() {
        return node;
    }

    @Override
    public void visit(ActionModelVisitor v) {
        v.visit(this);
    }

}
