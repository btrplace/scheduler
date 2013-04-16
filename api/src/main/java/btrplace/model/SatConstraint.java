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

package btrplace.model;

import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.ReconfigurationPlanValidator;

import java.util.Collection;
import java.util.UUID;

/**
 * Abstract class to characterize a satisfaction-oriented constraint
 * that impose a restriction on some components of a model.
 * <p/>
 * The restriction provided by the constraint can be either discrete or continuous.
 * If the restriction is discrete, then the constraint imposes a restriction on a {@link Model}.
 * If the restriction is continuous, then the constraint imposes also a restriction on a whole {@link ReconfigurationPlan}.
 * This may be the action schedule but also all the intermediary models that result from the application of the reconfiguration plan.
 * <p/>
 * A constraint does not necessarily support both continuous or discrete restriction.
 *
 * @author Fabien Hermenier
 */
public abstract class SatConstraint {

    private boolean continuous;

    /**
     * An enumeration to indicate the satisfaction of a constraint.
     */
    public enum Sat {
        /**
         * The constraint is satisfied.
         */
        SATISFIED,
        /**
         * The constraint is not satisfied .
         */
        UNSATISFIED,
        /**
         * It is not possible to state about the constraint satisfaction.
         */
        UNDEFINED
    }

    /**
     * The virtual machines involved in the constraint.
     */
    private Collection<UUID> vms;

    /**
     * The nodes involved in the constraint.
     */
    private Collection<UUID> nodes;

    /**
     * Make a new constraint.
     *
     * @param vms   the involved VMs
     * @param nodes the involved nodes
     * @param c     {@code true} to indicate a continuous restriction
     */
    public SatConstraint(Collection<UUID> vms, Collection<UUID> nodes, boolean c) {
        this.vms = vms;
        this.nodes = nodes;
        this.continuous = c;
    }

    /**
     * Get the VMs involved in the constraint.
     *
     * @return a set of VM identifiers that may be empty
     */
    public Collection<UUID> getInvolvedVMs() {
        return this.vms;
    }

    /**
     * Get the nodes involved in the constraint.
     *
     * @return a set of nodes identifiers that may be empty
     */
    public Collection<UUID> getInvolvedNodes() {
        return this.nodes;
    }

    /**
     * Check if a model satisfies the constraint.
     * This method is used when the constraint provides only a discrete restriction.
     *
     * @param i the model to check
     * @return {@code true} iff the constraint is not violated
     */
    public abstract Sat isSatisfied(Model i);

    /**
     * Check if a plan satisfies the constraint.
     * This method is only considered when the constraint provides a continuous restriction.
     * By default, the method checks that the result model satisfies the constraint
     *
     * @param p the plan to inspect
     * @return {@code true} iff the plan satisfies the constraint
     */
    public Sat isSatisfied(ReconfigurationPlan p) {
        Model m = p.getResult();
        if (m == null) {
            return Sat.UNSATISFIED;
        }
        return isSatisfied(m);
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
        return nodes.equals(that.nodes) && vms.equals(that.vms);
    }

    @Override
    public int hashCode() {
        return nodes.hashCode() + 31 * vms.hashCode();
    }

    /**
     * Indicates if the restriction provided by the constraint has to be
     * continuous if it is possible.
     *
     * @param b {@code true} to ask for a continuous satisfaction, {@code false} for a discrete satisfaction.
     * @return {@code true} iff the parameter has been considered
     */
    public boolean setContinuous(boolean b) {
        continuous = b;
        return true;
    }

    /**
     * Check if the restriction provided by the constraint be continuous.
     *
     * @return {@code true} for a continuous restriction
     */
    public boolean isContinuous() {
        return continuous;
    }

    /**
     * Get the validator to use to validate a plan with regards to the constraint.
     *
     * @return a non-null {@link ReconfigurationPlanValidator}
     */
    public ReconfigurationPlanValidator getValidator() {
        throw new UnsupportedOperationException();
    }
}
