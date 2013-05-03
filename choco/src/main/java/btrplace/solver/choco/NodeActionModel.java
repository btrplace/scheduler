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

import btrplace.solver.choco.actionModel.ActionModel;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.UUID;

/**
 * Interface to specify an action model that manipulate a node.
 *
 * @author Fabien Hermenier
 */
public interface NodeActionModel extends ActionModel {

    /**
     * Get the node manipulated by the action.
     *
     * @return the node identifier
     */
    UUID getNode();

    /**
     * Get the moment the node is being capable of hosting VMs.
     *
     * @return a variable
     */
    IntDomainVar getHostingStart();

    /**
     * Get the moment the node is no longer capable of hosting VMs.
     *
     * @return a variable
     */
    IntDomainVar getHostingEnd();

    /**
     * Get the moment the server has been powered up.
     *
     * @return a variable
     */
    IntDomainVar getPoweringStart();

    /**
     * Get the moment the server has been powered down.
     *
     * @return a variable
     */
    IntDomainVar getPoweringEnd();
}
