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

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.constraint.Constraint;
import org.btrplace.scheduler.choco.constraint.mttr.CMinMTTR;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapper that allow to convert {@link org.btrplace.model.constraint.SatConstraint} and {@link org.btrplace.model.constraint.OptConstraint} to {@link ChocoConstraint}.
 *
 * @author Fabien Hermenier
 */
public class ConstraintMapper {

    private Map<Class<? extends Constraint>, ChocoConstraintBuilder> builders;

    /**
     * Make a new empty mapper.
     */
    public ConstraintMapper() {
        builders = new HashMap<>();

    }

    /**
     * Make a new {@code ConstraintMapper} and fulfill it
     * using a default mapper for each bundled constraint.
     *
     * @return a fulfilled mapper.
     */
    public static ConstraintMapper newBundle() {
        ConstraintMapper map = new ConstraintMapper();
        map.register(new CSpread.Builder());
        map.register(new CSplit.Builder());
        map.register(new CSplitAmong.Builder());
        map.register(new CAmong.Builder());
        map.register(new CQuarantine.Builder());
        map.register(new CBan.Builder());
        map.register(new CFence.Builder());
        map.register(new COnline.Builder());
        map.register(new COffline.Builder());
        map.register(new CRunningCapacity.Builder());
        map.register(new CResourceCapacity.Builder());
        map.register(new CPreserve.Builder());
        map.register(new COverbook.Builder());
        map.register(new CRoot.Builder());
        map.register(new CReady.Builder());
        map.register(new CRunning.Builder());
        map.register(new CSleeping.Builder());
        map.register(new CKilled.Builder());
        map.register(new CGather.Builder());
        map.register(new CLonely.Builder());
        map.register(new CSequentialVMTransitions.Builder());
        map.register(new CMaxOnline.Builder());
        map.register(new CMinMTTR.Builder());
        map.register(new CNoDelay.Builder());
        return map;
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
     * Un-register the builder associated to a given {@link Constraint}.
     *
     * @param c the class of the {@link Constraint} to un-register
     * @return {@code true} if a builder was registered
     */
    public boolean unRegister(Class<? extends Constraint> c) {
        return builders.remove(c) != null;
    }

    /**
     * Check if a {@link ChocoConstraintBuilder} is registered for a given {@link Constraint}.
     *
     * @param c the constraint to check
     * @return {@code true} iff a builder is registered
     */
    public boolean isRegistered(Class<? extends Constraint> c) {
        return builders.containsKey(c);
    }

    /**
     * Get the builder associated to a {@link Constraint}.
     *
     * @param c the constraint
     * @return the associated builder if exists. {@code null} otherwise
     */
    public ChocoConstraintBuilder getBuilder(Class<? extends Constraint> c) {
        return builders.get(c);
    }

    /**
     * Map the given {@link Constraint} to a {@link ChocoConstraint} if possible.
     *
     * @param c the constraint to map
     * @return the mapping result or {@code null} if no {@link ChocoConstraint} was available
     */
    public ChocoConstraint map(Constraint c) {
        ChocoConstraintBuilder b = builders.get(c.getClass());
        if (b != null) {
            return b.build(c);
        }
        return null;
    }
}
