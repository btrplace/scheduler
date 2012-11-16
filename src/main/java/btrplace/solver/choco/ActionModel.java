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

    public abstract IntDomainVar getStart();

    public abstract IntDomainVar getEnd();

    public abstract IntDomainVar getDuration();

    public abstract IntDomainVar getGlobalCost();

    public abstract Slice getCSlice();

    public abstract Slice getDSlice();

    public abstract UUID getSubject();

    public abstract List<Action> getResultingActions(ReconfigurationProblem rp);
}
