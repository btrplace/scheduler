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

package org.btrplace.plan.event;

import org.btrplace.model.Node;

/**
 * An interface to indicate an event that
 * will place a running VM on a node.
 *
 * @author Fabien Hermenier
 */
public interface RunningVMPlacement extends VMEvent {

    /**
     * Get the destination node for the VM.
     *
     * @return the node identifier
     */
    Node getDestinationNode();
}
