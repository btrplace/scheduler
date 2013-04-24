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

package btrplace.solver.choco;

import btrplace.model.ModelView;
import btrplace.model.view.ShareableResource;
import btrplace.solver.SolverException;
import btrplace.solver.choco.view.CShareableResource;

import java.util.HashMap;
import java.util.Map;

/**
 * A mapper to associate an implementation of ModelView instances from their identifier.
 *
 * @author Fabien Hermenier
 */
public class ModelViewMapper {

    private Map<Class<? extends ModelView>, ChocoModelViewBuilder> builders;

    /**
     * Make a new mapper.
     */
    public ModelViewMapper() {
        builders = new HashMap<>();
        builders.put(ShareableResource.class, new CShareableResource.Builder());
    }

    /**
     * Register a constraint builder.
     *
     * @param vb the builder to add
     * @return {@code true} if no builder previously that registered for the view it handles
     */
    public boolean register(ChocoModelViewBuilder vb) {
        return builders.put(vb.getKey(), vb) == null;
    }

    /**
     * Un-register the builder associated to a view.
     *
     * @param id the view identifier
     * @return {@code true} if a builder was registered
     */
    public boolean unregister(Class<? extends ModelView> id) {
        return builders.remove(id) != null;
    }

    /**
     * Check if a {@link ChocoModelViewBuilder} is registered to handle a view.
     *
     * @param id the view to check
     * @return {@code true} iff a builder is registered
     */
    public boolean isRegistered(Class<? extends ModelView> id) {
        return builders.containsKey(id);
    }

    /**
     * Get the builder registered for a given view.
     *
     * @param id the view identifier
     * @return the associated builder if exists. {@code null} otherwise
     */
    public ChocoModelViewBuilder getBuilder(Class<? extends ModelView> id) {
        return builders.get(id);
    }

    /**
     * Map the given {@link ModelView} to a {@link btrplace.solver.choco.ChocoModelView} if possible.
     *
     * @param rp the problem to customize
     * @param v  the view to map
     * @return the solver-side view if a mapping was possible, or {@code null} if no mapping was possible.
     * @throws SolverException if en error occurred while creating the view implementation
     */
    public ChocoModelView map(ReconfigurationProblem rp, ModelView v) throws SolverException {
        ChocoModelViewBuilder b = builders.get(v.getClass());
        if (b == null) {
            return null;
        }
        return b.build(rp, v);
    }
}
