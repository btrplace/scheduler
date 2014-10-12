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

package org.btrplace.scheduler.choco.view;

import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ReconfigurationProblem;

import java.util.List;

/**
 * Kind of a builder that is used when {@link org.btrplace.scheduler.choco.view.ChocoModelViewBuilder} cannot
 * instantiate {@link org.btrplace.scheduler.choco.view.SolverViewBuilder} out of the box.
 *
 * @author Fabien Hermenier
 */
public abstract class DelegatedBuilder extends SolverViewBuilder {

    private String key;

    private List<String> deps;

    /**
     * New builder.
     *
     * @param k the view identifier.
     * @param d the dependencies for the building process
     */
    public DelegatedBuilder(String k, List<String> d) {
        this.key = k;
        this.deps = d;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public abstract ChocoView build(ReconfigurationProblem rp) throws SchedulerException;

    @Override
    public List<String> getDependencies() {
        return deps;
    }
}
