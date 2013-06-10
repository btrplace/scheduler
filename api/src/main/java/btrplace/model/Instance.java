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

package btrplace.model;

import btrplace.model.constraint.OptConstraint;
import btrplace.model.constraint.SatConstraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * An instance aggregates a model and a list of constraints.
 *
 * @author Fabien Hermenier
 */
public class Instance {

    private Model mo;

    private List<SatConstraint> cstrs;

    private OptConstraint opt;

    /**
     * Make a new instance.
     *
     * @param mo the model to use
     * @param cs the list of constraints
     * @param o  the optimization constraint
     */
    public Instance(Model mo, Collection<SatConstraint> cs, OptConstraint o) {
        cstrs = new ArrayList<>(cs);
        this.mo = mo;
        this.opt = o;
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

    /**
     * Get the declared optimization constraint.
     *
     * @return the optimization constraint to consider
     */
    public OptConstraint getOptimizationConstraint() {
        return opt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Instance instance = (Instance) o;

        return (cstrs.equals(instance.cstrs) && mo.equals(instance.mo) && opt.equals(instance.opt));
    }

    @Override
    public int hashCode() {
        return Objects.hash(mo, cstrs);
    }
}
