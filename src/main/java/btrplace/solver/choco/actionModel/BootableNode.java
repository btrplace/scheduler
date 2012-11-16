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

import btrplace.plan.Action;
import btrplace.solver.choco.ActionModel;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.List;
import java.util.UUID;

/**
 * Model an action that allow a node to be booted if necessary.
 *
 * @author Fabien Hermenier
 */
public class BootableNode extends ActionModel {

    @Override
    public IntDomainVar getStart() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public IntDomainVar getEnd() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public IntDomainVar getDuration() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public IntDomainVar getGlobalCost() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Slice getCSlice() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Slice getDSlice() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UUID getSubject() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Action> getResultingActions(ReconfigurationProblem rp) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
