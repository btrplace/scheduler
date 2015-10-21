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

import java.util.HashMap;
import java.util.Map;

/**
 * A mapper to associate an {@link org.btrplace.scheduler.choco.view.ChocoView} to a {@link org.btrplace.model.view.ModelView}.
 *
 * @author Fabien Hermenier
 */
public class ModelViewMapper {

    private Map<Class<? extends ModelView>, ChocoModelViewBuilder> builders;

    /**
     * Make a new empty mapper.
     */
    public ModelViewMapper() {
        builders = new HashMap<>();
    }

    /**
     * Make a new {@code ModelViewMapper} and fulfill it
     * using a default mapper for each bundled view.
     *
     * @return a fulfilled mapper.
     */
    public static ModelViewMapper newBundle() {
        ModelViewMapper map = new ModelViewMapper();
        map.register(new CShareableResource.Builder());
        map.register(new CNetwork.Builder());
        return map;
    }

    /**
     * Register a constraint builder.
     *
     * @param vb the builder to addDim
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
    public boolean unRegister(Class<? extends ModelView> id) {
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
}
