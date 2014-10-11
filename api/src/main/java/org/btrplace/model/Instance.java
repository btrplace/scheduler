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

package org.btrplace.model;

import org.btrplace.model.constraint.OptConstraint;
import org.btrplace.model.constraint.SatConstraint;

import java.util.*;

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
     * @param m  the model to use
     * @param cs the list of satisfaction oriented constraints to consider
     * @param o  the optimization constraint
     */
    public Instance(Model m, Collection<SatConstraint> cs, OptConstraint o) {
        cstrs = new ArrayList<>(cs);
        this.mo = m;
        this.opt = o;
    }

    /**
     * Make a new instance.
     *
     * @param m the model to use
     * @param o the optimization constraint
     */
    public Instance(Model m, OptConstraint o) {
        this(m, Collections.<SatConstraint>emptyList(), o);
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
     * @return a collection of satisfaction oriented constraints that may be empty
     */
    public Collection<SatConstraint> getSatConstraints() {
        return cstrs;
    }

    /**
     * Get the declared optimization constraint.
     *
     * @return the optimization constraint to consider (not null)
     */
    public OptConstraint getOptConstraint() {
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
