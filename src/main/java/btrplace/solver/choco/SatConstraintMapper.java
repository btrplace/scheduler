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

import btrplace.model.SatConstraint;
import btrplace.model.constraint.Ban;
import btrplace.model.constraint.Offline;
import btrplace.model.constraint.Online;
import btrplace.model.constraint.Spread;
import btrplace.solver.choco.constraint.CBan;
import btrplace.solver.choco.constraint.CContinuousSpread;
import btrplace.solver.choco.constraint.COffline;
import btrplace.solver.choco.constraint.COnline;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapper that allow to convert {@link SatConstraint} to {@link ChocoSatConstraint}.
 *
 * @author Fabien Hermenier
 */
public class SatConstraintMapper {

    private Map<Class<? extends SatConstraint>, ChocoConstraintBuilder> builders;

    /**
     * Make a new mapper.
     */
    public SatConstraintMapper() {
        builders = new HashMap<Class<? extends SatConstraint>, ChocoConstraintBuilder>();

        builders.put(Spread.class, new CContinuousSpread.Builder());
        builders.put(Ban.class, new CBan.Builder());
        builders.put(Online.class, new COnline.Builder());
        builders.put(Offline.class, new COffline.Builder());
    }

    /**
     * Register a constraint builder.
     *
     * @param ccb the builder to register
     * @return {@code true} if no builder previously registered for the given constraint was deleted
     */
    public boolean register(ChocoConstraintBuilder ccb) {
        return builders.put(ccb.getKey(), ccb) == null;
    }

    /**
     * Un-register the builder associated to a given {@link SatConstraint}.
     *
     * @param c the class of the {@link SatConstraint} to un-register
     * @return {@code true} if a builder was registered
     */
    public boolean unregister(Class<? extends SatConstraint> c) {
        return builders.remove(c) != null;
    }

    /**
     * Check if a {@link ChocoConstraintBuilder} is registered for a given {@link SatConstraint}.
     *
     * @param c the constraint to check
     * @return {@code true} iff a builder is registered
     */
    public boolean isRegistered(Class<? extends SatConstraint> c) {
        return builders.containsKey(c);
    }

    /**
     * Get the builder associated to a {@link SatConstraint}.
     *
     * @param c the constraint
     * @return the associated builder if exists. {@code null} otherwise
     */
    public ChocoConstraintBuilder getBuilder(Class<? extends SatConstraint> c) {
        return builders.get(c);
    }

    /**
     * Map the given {@link SatConstraint} to a {@link ChocoSatConstraint} if possible.
     *
     * @param c the constraint to map
     * @return the mapping result or {@null} if no {@link ChocoSatConstraint} was available
     */
    public ChocoSatConstraint map(SatConstraint c) {
        ChocoConstraintBuilder b = builders.get(c.getClass());
        if (b != null) {
            return b.build(c);
        }
        return null;
    }
}
