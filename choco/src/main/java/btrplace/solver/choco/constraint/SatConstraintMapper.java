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

package btrplace.solver.choco.constraint;

import btrplace.model.SatConstraint;
import btrplace.model.constraint.*;
import btrplace.solver.choco.ChocoSatConstraint;
import btrplace.solver.choco.ChocoSatConstraintBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapper that allow to convert {@link SatConstraint} to {@link btrplace.solver.choco.ChocoSatConstraint}.
 *
 * @author Fabien Hermenier
 */
public class SatConstraintMapper {

    private Map<Class<? extends SatConstraint>, ChocoSatConstraintBuilder> builders;

    /**
     * Make a new mapper.
     */
    public SatConstraintMapper() {
        builders = new HashMap<>();

        builders.put(Spread.class, new CSpread.Builder());
        builders.put(Split.class, new CSplit.Builder());
        builders.put(SplitAmong.class, new CSplitAmong.Builder());
        builders.put(Among.class, new CAmong.Builder());
        builders.put(Quarantine.class, new CQuarantine.Builder());
        builders.put(Ban.class, new CBan.Builder());
        builders.put(Fence.class, new CFence.Builder());
        builders.put(Online.class, new COnline.Builder());
        builders.put(Offline.class, new COffline.Builder());
        builders.put(SingleRunningCapacity.class, new CSingleRunningCapacity.Builder());
        builders.put(CumulatedRunningCapacity.class, new CCumulatedRunningCapacity.Builder());
        builders.put(SingleResourceCapacity.class, new CSingleResourceCapacity.Builder());
        builders.put(CumulatedResourceCapacity.class, new CCumulatedResourceCapacity.Builder());
        builders.put(Preserve.class, new CPreserve.Builder());
        builders.put(Overbook.class, new COverbook.Builder());
        builders.put(Root.class, new CRoot.Builder());
        builders.put(Ready.class, new CReady.Builder());
        builders.put(Running.class, new CRunning.Builder());
        builders.put(Sleeping.class, new CSleeping.Builder());
        builders.put(Killed.class, new CKilled.Builder());
        builders.put(Gather.class, new CGather.Builder());
        builders.put(Lonely.class, new CLonely.Builder());
        builders.put(SequentialVMTransitions.class, new CSequentialVMTransitions.Builder());
    }

    /**
     * Register a constraint builder.
     *
     * @param ccb the builder to register
     * @return {@code true} if no builder previously registered for the given constraint was deleted
     */
    public boolean register(ChocoSatConstraintBuilder ccb) {
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
     * Check if a {@link ChocoSatConstraintBuilder} is registered for a given {@link SatConstraint}.
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
    public ChocoSatConstraintBuilder getBuilder(Class<? extends SatConstraint> c) {
        return builders.get(c);
    }

    /**
     * Map the given {@link SatConstraint} to a {@link btrplace.solver.choco.ChocoSatConstraint} if possible.
     *
     * @param c the constraint to map
     * @return the mapping result or {@code null} if no {@link btrplace.solver.choco.ChocoSatConstraint} was available
     */
    public ChocoSatConstraint map(SatConstraint c) {
        ChocoSatConstraintBuilder b = builders.get(c.getClass());
        if (b != null) {
            return b.build(c);
        }
        return null;
    }
}
