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

    protected ReconfigurationProblem rp;

    protected IntDomainVar state;

    /**
     * Make a new action on an element.
     *
     * @param e the element
     */
    public ActionModel(ReconfigurationProblem rp, UUID e) {
        this.rp = rp;
        this.subject = e;
    }

    /**
     * Get the moment the action starts.
     *
     * @return a variable that must be positive
     */
    public IntDomainVar getStart() {
        return start;
    }

    /**
     * Get the moment the action ends.
     *
     * @return a variable that must be greater than {@link #getStart()}
     */
    public IntDomainVar getEnd() {
        return end;
    }

    /**
     * Get the action duration.
     *
     * @return a duration equals to {@code getEnd() - getStart()}
     */
    public IntDomainVar getDuration() {
        return duration;
    }

    /**
     * Get the slice denoting the possible current placement of the subject on a node.
     *
     * @return a {@link Slice} that may be {@code null}
     */
    public Slice getCSlice() {
        return cSlice;
    }

    /**
     * Get the slice denoting the possible future placement off the subject
     *
     * @return a {@link Slice} that may be {@code null}
     */
    public Slice getDSlice() {
        return dSlice;
    }

    /**
     * Get the subject of the slice. Usually a VM or a node.
     *
     * @return an element identifier
     */
    public UUID getSubject() {
        return subject;
    }

    /**
     * Get the cost of the action.
     *
     * @return a variable
     */
    public IntDomainVar getGlobalCost() {
        return cost;
    }

    /**
     * Get the action that are generated for the model variables.
     *
     * @param rp the current problem
     * @return a list of {@link Action} that may be empty.
     */
    public abstract List<Action> getResultingActions(ReconfigurationProblem rp);

    /**
     * Get the next state of the subject manipulated by the action.
     *
     * @return {@code 0} for offline, {@code 1} for online.
     */
    public IntDomainVar getState() {
        return state;
    }

}
