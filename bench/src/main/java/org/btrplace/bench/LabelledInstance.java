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

package org.btrplace.bench;

import org.btrplace.model.Instance;

/**
 * An instance with a label.
 * @author Fabien Hermenier
 */
public class LabelledInstance extends Instance {

    /**
     * The instance label.
     */
    public final String label;

    /**
     * Make a new instance.
     *
     * @param label the instance label
     * @param i     the underlying instance
     */
    public LabelledInstance(String label, Instance i) {
        super(i.getModel(), i.getSatConstraints(), i.getOptConstraint());
        this.label = label;
    }
}
