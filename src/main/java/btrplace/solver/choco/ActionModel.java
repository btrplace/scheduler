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

package btrplace.solver.choco;

import btrplace.plan.Action;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.List;
import java.util.UUID;

/**
 * Model an action.
 *
 * @author Fabien Hermenier
 */
public abstract class ActionModel {

    protected UUID subject;

    protected IntDomainVar start;

    protected IntDomainVar end;

    protected IntDomainVar duration;

    protected Slice cSlice;

    protected Slice dSlice;

    protected IntDomainVar cost;

    public ActionModel(UUID e) {
        this.subject = e;
    }

    public IntDomainVar getStart() {
        return start;
    }

    public IntDomainVar getEnd() {
        return end;
    }

    public IntDomainVar getDuration() {
        return duration;
    }

    public Slice getCSlice() {
        return cSlice;
    }

    public Slice getDSlice() {
        return dSlice;
    }

    public UUID getSubject() {
        return subject;
    }

    public IntDomainVar getGlobalCost() {
        return cost;
    }

    public abstract List<Action> getResultingActions(ReconfigurationProblem rp);

}
