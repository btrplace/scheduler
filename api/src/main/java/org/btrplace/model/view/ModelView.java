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

package org.btrplace.model.view;

import org.btrplace.model.VM;

/**
 * A view provides some domain-specific information about the elements of a model.
 * A view must have a unique identifier
 *
 * @author Fabien Hermenier
 */
public interface ModelView extends Cloneable {

    /**
     * Get the view identifier.
     *
     * @return a non-empty String
     */
    String getIdentifier();

    /**
     * Copy the view.
     *
     * @return a new view that is equals to the current one.
     */
    ModelView clone();

    /**
     * Notify the view a VM that already exist
     * will be substituted by another VM during the reconfiguration process.
     *
     * @param curId  the current VM identifier
     * @param nextId the new VM identifier
     * @return {@code true} iff the operation succeeded
     */
    boolean substituteVM(VM curId, VM nextId);
}
