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

import org.btrplace.model.constraint.*;
import org.btrplace.model.constraint.migration.*;
import org.btrplace.scheduler.choco.constraint.migration.*;
import org.btrplace.scheduler.choco.constraint.mttr.CMinMTTR;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapper that allow to map {@link org.btrplace.model.constraint.SatConstraint} and {@link org.btrplace.model.constraint.OptConstraint} to {@link ChocoConstraint}.
 *
 * @author Fabien Hermenier
 */
public class ConstraintMapper {

    private Map<Class<? extends Constraint>, Class<? extends ChocoConstraint>> map;
    /**
     * Make a new empty mapper.
     */
    public ConstraintMapper() {
        map = new HashMap<>();
    }

    /**
     * Make a new {@code ConstraintMapper} and fulfill it
     * using a default mapper for each bundled constraint.
     *
     * @return a fulfilled mapper.
     */
    public static ConstraintMapper newBundle() {
        ConstraintMapper map = new ConstraintMapper();
        map.register(Spread.class, CSpread.class);
        map.register(Split.class, CSplit.class);
        map.register(SplitAmong.class, CSplitAmong.class);
        map.register(Among.class, CAmong.class);
        map.register(Quarantine.class, CQuarantine.class);
        map.register(Ban.class, CBan.class);
        map.register(Fence.class, CFence.class);
        map.register(Online.class, COnline.class);
        map.register(Offline.class, COffline.class);
        map.register(RunningCapacity.class, CRunningCapacity.class);
        map.register(ResourceCapacity.class, CResourceCapacity.class);
        map.register(Preserve.class, CPreserve.class);
        map.register(Overbook.class, COverbook.class);
        map.register(Root.class, CRoot.class);
        map.register(Ready.class, CReady.class);
        map.register(Running.class, CRunning.class);
        map.register(Sleeping.class, CSleeping.class);
        map.register(Killed.class, CKilled.class);
        map.register(Gather.class, CGather.class);
        map.register(Lonely.class, CLonely.class);
        map.register(Seq.class, CSequentialVMTransitions.class);
        map.register(MaxOnline.class, CMaxOnline.class);
        map.register(MinMTTR.class, CMinMTTR.class);
        map.register(MinMTTRMig.class, CMinMTTRMig.class);
        map.register(NoDelay.class, CNoDelay.class);
        map.register(Deadline.class, CDeadline.class);
        map.register(Precedence.class, CPrecedence.class);
        map.register(Serialize.class, CSerialize.class);
        map.register(Sync.class, CSync.class);
        return map;
    }

    /**
     * Register a mapping between an api-side constraint and its choco implementation.
     * It is expected from the implementation to exhibit a constructor that takes the api-side constraint as argument.
     *
     * @param c the api-side constraint
     * @param cc the choco implementation
     * @throws IllegalArgumentException if there is no suitable constructor for the choco implementation
     */
    public void register(Class<? extends Constraint> c, Class<? extends ChocoConstraint> cc) {
        try {
            cc.getDeclaredConstructor(c);
            map.put(c, cc);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("No constructor '" + cc.getSimpleName() + "(" + c.getSimpleName() + ")' available");
        }
    }

    /**
     * Un-register the mapping associated to a given {@link Constraint}.
     *
     * @param c the class of the {@link Constraint} to un-register
     * @return {@code true} if a mapping was registered
     */
    public boolean unRegister(Class<? extends Constraint> c) {
        return map.remove(c) != null;
    }

    /**
     * Check if a mapping is registered for a given {@link Constraint}.
     *
     * @param c the constraint to check
     * @return {@code true} iff a mapping is registered
     */
    public boolean isRegistered(Class<? extends Constraint> c) {
        return map.containsKey(c);
    }

    /**
     * Map the given {@link Constraint} to a {@link ChocoConstraint} if possible.
     *
     * @param c the constraint to map
     * @return the associated {@link ChocoConstraint}, {@code null} if no mapping was mapped
     * @throws IllegalArgumentException if there is no suitable constructor for the choco implementation
     */
    public ChocoConstraint map(Constraint c) {
        Class<? extends ChocoConstraint> cc = map.get(c.getClass());
        if (cc == null) {
            return null;
        }
        try {
            return cc.getDeclaredConstructor(c.getClass()).newInstance(c);
        } catch (Exception ex) {
            throw new IllegalArgumentException("No constructor '" + cc.getSimpleName() + "(" + c.getClass().getSimpleName() + ")' available");
        }
    }
}
