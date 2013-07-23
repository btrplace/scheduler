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

package btrplace.solver.choco.runner.staticPartitioning;

import btrplace.model.Element;

/**
 * A procedure to use on a set of contiguous values that
 * share the same index value.
 *
 * @author Fabien Hermenier
 */
public interface IndexEntryProcedure<E extends Element> {

    /**
     * The method to execute.
     *
     * @param index the splittable index to rely on
     * @param key   the index key
     * @param from  the value lower bound
     * @param to    the value upper bound (exclusive)
     */
    void extract(SplittableIndex<E> index, int key, int from, int to);
}
