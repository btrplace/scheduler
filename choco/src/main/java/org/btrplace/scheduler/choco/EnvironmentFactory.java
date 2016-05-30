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

package org.btrplace.scheduler.choco;

import org.btrplace.model.Model;
import org.chocosolver.memory.IEnvironment;

/**
 * Allows to choose a memory environment that is the most appropriate with regards
 * to the model to solve
 *
 * @author Fabien Hermenier
 */
@FunctionalInterface
public interface EnvironmentFactory {

    /**
     * Build the memory environment.
     *
     * @param mo the model that will be solved
     * @return the memory environment
     */
    IEnvironment build(Model mo);
}
