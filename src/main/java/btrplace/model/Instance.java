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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An instance aggregates a model and a list of constraints.
 *
 * @author Fabien Hermenier
 */
public class Instance {

    private Model mo;

    private List<SatConstraint> cstrs;

    /**
     * Make a new instance.
     *
     * @param mo the model to use
     * @param cs the list of constraints
     */
    public Instance(Model mo, List<SatConstraint> cs) {
        cstrs = new ArrayList<SatConstraint>(cs);
        this.mo = mo;
    }

    /**
     * Get the model.
     *
     * @return a model
     */
    public Model getModel() {
        return mo;
    }

    /**
     * Get the declared constraints.
     *
     * @return a collection of constraints that may be empty
     */
    public Collection<SatConstraint> getConstraints() {
        return cstrs;
    }
}
