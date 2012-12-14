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

import java.util.Collection;
import java.util.UUID;

/**
 * Interface to characterize a satisfaction-oriented constraint that impose a restriction
 * on some components of a model.
 * <p/>
 * A constraint may or may be not be satisfied for a given model and it is not
 * always possible to state about its feasibility by relying only on a model.
 * <p/>
 * If a constraint is not satisfied, a reconfiguration algorithm may be used
 * to compute a new model that will lead to the constraint satisfaction. In this case,
 * the restriction provided by the constraint may be either discrete or continuous.
 * If the restriction is discrete, then the constraint must only be satisfied at the end of the reconfiguration.
 * If the restriction is continuous and if the constraint is already satisfied before the reconfiguration,
 * then the constraint will have to be satisfied at any moment of the reconfiguration process.
 * <p/>
 * Stating about the restriction type is optional and may not be supported by every constraint or reconfiguration
 * algorithm.
 *
 * @author Fabien Hermenier
 */
public abstract class SatConstraint {

    private boolean lazy = false;

    /**
     * An enumeration to indicate the satisfaction of a constraint
     * with regards to a model
     */
    public enum Sat {
        /**
         * The constraint is satisfied
         */
        SATISFIED,
        /**
         * The constraint is not satisfied
         */
        UNSATISFIED,
        /**
         * It is not possible to state about the constraint satisfaction
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
     */
    public SatConstraint(Collection<UUID> vms, Collection<UUID> nodes) {
        this.vms = vms;
        this.nodes = nodes;
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
     * Check if an instance violate the constraint.
     *
     * @param i the instance to check
     * @return {@code true} iff the constraint is not violated
     */
    public abstract Sat isSatisfied(Model i);

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
     * continuous if possible.
     *
     * @param b {@code true} to ask for a continuous satisfaction. {@code false} for a discrete satisfaction.
     * @return {@code true} iff the parameter has been considered
     */
    public boolean setContinuous(boolean b) {
        lazy = b;
        return true;
    }

    /**
     * Check if the constraint restriction shall be continuous or not.
     *
     * @return {@code true} for a continuous satisfaction
     */
    public boolean isContinuous() {
        return lazy;
    }
}
