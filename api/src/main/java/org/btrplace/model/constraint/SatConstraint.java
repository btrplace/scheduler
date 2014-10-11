/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.ReconfigurationPlanChecker;
import org.btrplace.plan.ReconfigurationPlanCheckerException;

import java.util.Collection;
import java.util.Objects;

/**
 * Abstract class to characterize a satisfaction-oriented constraint
 * that impose a restriction on some components of a model.
 * <p>
 * The restriction provided by the constraint can be either discrete or continuous.
 * If the restriction is discrete, then the constraint imposes a restriction on a {@link org.btrplace.model.Model}.
 * If the restriction is continuous, then the constraint imposes also a restriction on a whole {@link ReconfigurationPlan}.
 * This may be the action schedule but also all the intermediary models that result from the application of the reconfiguration plan.
 * <p>
 * A constraint does not necessarily support both continuous or discrete restriction.
 *
 * @author Fabien Hermenier
 */
public abstract class SatConstraint implements Constraint {

    private boolean continuous;

    /**
     * The virtual machines involved in the constraint.
     */
    private Collection<VM> vms;

    /**
     * The nodes involved in the constraint.
     */
    private Collection<Node> nodes;

    /**
     * Make a new constraint.
     *
     * @param v the involved VMs
     * @param n the involved nodes
     * @param c {@code true} to indicate a continuous restriction
     */
    public SatConstraint(Collection<VM> v, Collection<Node> n, boolean c) {
        this.vms = v;
        this.nodes = n;
        this.continuous = c;
    }

    /**
     * Get the VMs involved in the constraint.
     *
     * @return a set of VM identifiers that may be empty
     */
    public Collection<VM> getInvolvedVMs() {
        return this.vms;
    }

    /**
     * Get the nodes involved in the constraint.
     *
     * @return a set of nodes identifiers that may be empty
     */
    public Collection<Node> getInvolvedNodes() {
        return this.nodes;
    }

    /**
     * Check if a model satisfies the constraint.
     * This method is used when the constraint provides only a discrete restriction.
     *
     * @param i the model to check
     * @return {@code true} iff the constraint is not violated
     */
    public boolean isSatisfied(Model i) {
        return getChecker().endsWith(i);
    }

    /**
     * Check if a plan satisfies the constraint.
     * This method is only considered when the constraint provides a continuous restriction.
     *
     * @param p the plan to inspect
     * @return {@code true} iff the plan satisfies the constraint
     */
    public boolean isSatisfied(ReconfigurationPlan p) {
        ReconfigurationPlanChecker chk = new ReconfigurationPlanChecker();
        chk.addChecker(getChecker());
        try {
            chk.check(p);
        } catch (ReconfigurationPlanCheckerException ex) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(vms, nodes, continuous);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SatConstraint that = (SatConstraint) o;
        return nodes.equals(that.nodes) && vms.equals(that.vms) && continuous == that.continuous;
    }

    /**
     * Indicates if the restriction provided by the constraint is continuous.
     *
     * @param b {@code true} to ask for a continuous satisfaction, {@code false} for a discrete satisfaction.
     * @return {@code true} iff the parameter has been considered
     */
    public boolean setContinuous(boolean b) {
        continuous = b;
        return true;
    }

    /**
     * Check if the restriction provided by the constraint is continuous.
     *
     * @return {@code true} for a continuous restriction
     */
    public boolean isContinuous() {
        return continuous;
    }

    /**
     * Get the validator used to check if a plan satisfies the constraint.
     *
     * @return a non-null {@link SatConstraintChecker}
     */
    public SatConstraintChecker<?> getChecker() {
        throw new UnsupportedOperationException();
    }

    /**
     * Pretty string for a restriction
     *
     * @return {@code "continuous"} or {@code "discrete"} depending on the restriction
     */
    public String restrictionToString() {
        return isContinuous() ? "continuous" : "discrete";
    }
}
