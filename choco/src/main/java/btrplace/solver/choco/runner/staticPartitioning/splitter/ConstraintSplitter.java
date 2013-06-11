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

package btrplace.solver.choco.runner.staticPartitioning.splitter;

import btrplace.model.Instance;
import btrplace.model.constraint.Constraint;

import java.util.List;

/**
 * Interface to specify a splitter for a constraint.
 * With a set of given infrastructure partition,
 * the splitter may split the constraint, if necessary, to make it fit in one unique partition.
 *
 * @author Fabien Hermenier
 */
public interface ConstraintSplitter<C extends Constraint> {

    /**
     * Get the class of the Constraint associated to the splitter.
     *
     * @return a Class derived from {@link btrplace.model.constraint.Constraint}
     */
    Class<C> getKey();

    /**
     * Ensure a given constraint fit into a single partition.
     * If necessary, the constraint may have be splitted.
     * <b>this call inserts the constrain (or its subdivisions) inside their respective instances</b>
     *
     * @param cstr       the model constraint
     * @param partitions the possible partitions
     * @return {@code true} iff the constraint fits a single partition.
     */
    boolean split(C cstr, List<Instance> partitions);

}
