/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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
import java.util.Collections;

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
public interface SatConstraint extends Constraint {

    /**
     * Get the nodes involved in the constraint.
     *
     * @return a set of nodes identifiers that may be empty
     */
    default Collection<Node> getInvolvedNodes() {
        return Collections.emptyList();
    }

    /**
     * Get the VMs involved in the constraint.
     *
     * @return a set of VM identifiers that may be empty
     */
    default Collection<VM> getInvolvedVMs() {
        return Collections.emptyList();
    }

    /**
     * Check if the restriction provided by the constraint is continuous.
     *
     * @return {@code true} for a continuous restriction
     */
    boolean isContinuous();

    /**
     * Indicates if the restriction provided by the constraint is continuous.
     *
     * @param b {@code true} to ask for a continuous satisfaction, {@code false} for a discrete satisfaction.
     * @return {@code true} iff the parameter has been considered
     */
    boolean setContinuous(boolean b);

    /**
     * Get the validator used to check if a plan satisfies the constraint.
     *
     * @return a non-null {@link SatConstraintChecker}
     */
    default SatConstraintChecker<? extends SatConstraint> getChecker() {
        return new AllowAllConstraintChecker<>(null);
    }

    /**
     * Check if a model satisfies the constraint.
     * This method is used when the constraint provides only a discrete restriction.
     *
     * @param i the model to check
     * @return {@code true} iff the constraint is not violated
     */
    default boolean isSatisfied(Model i) {
        return getChecker().endsWith(i);
    }

    /**
     * Check if a plan satisfies the constraint.
     * This method is only considered when the constraint provides a continuous restriction.
     *
     * @param p the plan to inspect
     * @return {@code true} iff the plan satisfies the constraint
     */
    default boolean isSatisfied(ReconfigurationPlan p) {
        ReconfigurationPlanChecker chk = new ReconfigurationPlanChecker();
        chk.addChecker(getChecker());
        try {
            chk.check(p);
        } catch (ReconfigurationPlanCheckerException ex) {
            return false;
        }
        return true;
    }

}
