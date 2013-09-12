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

package btrplace.plan;

import btrplace.model.Model;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.event.Action;

/**
 * Exception that notifies a constraint violation inside a reconfiguration plan.
 *
 * @author Fabien Hermenier
 */
public class ReconfigurationPlanCheckerException extends Exception {

    private SatConstraint cstr;

    private Action action;

    private Model mo;

    private boolean origin = true;

    /**
     * Declare a violation caused by an action.
     *
     * @param c the violated constraint
     * @param a the action provoking the violation
     */
    public ReconfigurationPlanCheckerException(SatConstraint c, Action a) {
        this.cstr = c;
        this.action = a;
    }

    /**
     * Declare a violation caused by a model.
     *
     * @param c     the violated constraint
     * @param model the model provoking the violation
     * @param o     {@code true} to indicate the model is the model at the origin of the plan. {@code false}
     *              to indicate the model that is reached once all the actions have been applied
     */
    public ReconfigurationPlanCheckerException(SatConstraint c, Model model, boolean o) {
        this.cstr = c;
        this.mo = model;
        this.origin = o;
    }

    /**
     * Get the violated constraint.
     *
     * @return a non-null constraint.
     */
    public SatConstraint getConstraint() {
        return cstr;
    }

    /**
     * Get the action that provoked the violation.
     *
     * @return an action. {@code null} if the violation was provoked by a model.
     */
    public Action getAction() {
        return action;
    }

    /**
     * Get the model that provoked the violation.
     *
     * @return a model. {@code null} if the violation was provoked by an action.
     */
    public Model getModel() {
        return mo;
    }

    /**
     * Indicates if a violation was provoked by the origin or the resulting model.
     * Do not consider the value when the violation is provoked by an action.
     *
     * @return {@code true} iff the violation was provoked by the origin model.
     */
    public boolean isOrigin() {
        return origin;
    }

    @Override
    public String toString() {
        if (action != null) {
            return new StringBuilder("Action '")
                    .append(action)
                    .append("' violates the constraint '")
                    .append(cstr).toString();
        }

        StringBuilder b = new StringBuilder("The following ");
        b.append(origin ? "origin" : "resulting");
        b.append(" model violates the constraint '").append(cstr).append("':\n");
        return b.append(mo).toString();
    }
}
