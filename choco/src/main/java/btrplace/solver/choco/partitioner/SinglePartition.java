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

package btrplace.solver.choco.partitioner;

import btrplace.model.Instance;
import btrplace.model.Model;
import btrplace.model.constraint.OptimizationConstraint;
import btrplace.model.constraint.SatConstraint;

import java.util.Collection;
import java.util.Collections;

/**
 * A fake partitioner.
 * Create a single partition containing the whole original model and set of constraints.
 *
 * @author Fabien Hermenier
 */
public class SinglePartition implements Partitioner {

    /**
     * Make a new partitioner.
     */
    public SinglePartition() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public Collection<Instance> part(Model mo, Collection<SatConstraint> cstrs,
                                     OptimizationConstraint obj) {
        return Collections.singleton(new Instance(mo, cstrs, obj));
    }
}
