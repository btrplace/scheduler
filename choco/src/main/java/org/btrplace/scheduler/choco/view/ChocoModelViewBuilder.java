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

import org.btrplace.model.view.ModelView;
import org.btrplace.scheduler.SchedulerException;

/**
 * The builder that is used by {@link org.btrplace.scheduler.choco.view.ModelViewMapper} to create
 * the solver-side builder from from an api-side model.
 *
 * @author Fabien Hermenier
 */
public interface ChocoModelViewBuilder {

    /**
     * Get the class of the {@link ModelView} that is handled by this builder.
     *
     * @return a Class derived from {@link org.btrplace.model.view.ModelView}
     */
    Class<? extends ModelView> getKey();

    /**
     * Build the {@link ChocoView} associated to the {@link ModelView}
     * identified as key.
     *
     * @param v the model constraint
     * @throws org.btrplace.scheduler.SchedulerException if an error occurred while building the view
     */
    SolverViewBuilder build(ModelView v) throws SchedulerException;
}
