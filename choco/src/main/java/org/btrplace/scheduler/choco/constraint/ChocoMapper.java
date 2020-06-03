/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.constraint.Among;
import org.btrplace.model.constraint.Ban;
import org.btrplace.model.constraint.Constraint;
import org.btrplace.model.constraint.Fence;
import org.btrplace.model.constraint.Gather;
import org.btrplace.model.constraint.Killed;
import org.btrplace.model.constraint.Lonely;
import org.btrplace.model.constraint.MaxOnline;
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.model.constraint.MinMigrations;
import org.btrplace.model.constraint.NoDelay;
import org.btrplace.model.constraint.Offline;
import org.btrplace.model.constraint.Online;
import org.btrplace.model.constraint.Overbook;
import org.btrplace.model.constraint.Preserve;
import org.btrplace.model.constraint.Quarantine;
import org.btrplace.model.constraint.Ready;
import org.btrplace.model.constraint.ResourceCapacity;
import org.btrplace.model.constraint.Root;
import org.btrplace.model.constraint.Running;
import org.btrplace.model.constraint.RunningCapacity;
import org.btrplace.model.constraint.Seq;
import org.btrplace.model.constraint.Sleeping;
import org.btrplace.model.constraint.Split;
import org.btrplace.model.constraint.SplitAmong;
import org.btrplace.model.constraint.Spread;
import org.btrplace.model.constraint.migration.Deadline;
import org.btrplace.model.constraint.migration.MinMTTRMig;
import org.btrplace.model.constraint.migration.Precedence;
import org.btrplace.model.constraint.migration.Serialize;
import org.btrplace.model.constraint.migration.Sync;
import org.btrplace.model.view.ModelView;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.model.view.network.Network;
import org.btrplace.scheduler.choco.constraint.migration.CDeadline;
import org.btrplace.scheduler.choco.constraint.migration.CMinMTTRMig;
import org.btrplace.scheduler.choco.constraint.migration.CMinMigrations;
import org.btrplace.scheduler.choco.constraint.migration.CPrecedence;
import org.btrplace.scheduler.choco.constraint.migration.CSerialize;
import org.btrplace.scheduler.choco.constraint.migration.CSync;
import org.btrplace.scheduler.choco.constraint.mttr.CMinMTTR;
import org.btrplace.scheduler.choco.view.CNetwork;
import org.btrplace.scheduler.choco.view.CShareableResource;
import org.btrplace.scheduler.choco.view.ChocoView;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapper that allow to map {@link org.btrplace.model.constraint.SatConstraint} and {@link org.btrplace.model.constraint.OptConstraint} to {@link ChocoConstraint}.
 *
 * @author Fabien Hermenier
 */
public class ChocoMapper {

  private final Map<Class<? extends Constraint>, Class<? extends ChocoConstraint>> constraints;
  private final Map<Class<? extends ModelView>, Class<? extends ChocoView>> views;

  /**
   * Make a new empty mapper.
   */
  public ChocoMapper() {
    constraints = new HashMap<>();
    views = new HashMap<>();
  }

  /**
     * Make a new {@code ChocoMapper} and fulfill it
     * using a default mapper for each bundled constraint.
     *
     * @return a fulfilled mapper.
     */
    public static ChocoMapper newBundle() {
        ChocoMapper map = new ChocoMapper();
        map.mapConstraint(Spread.class, CSpread.class);
        map.mapConstraint(Split.class, CSplit.class);
        map.mapConstraint(SplitAmong.class, CSplitAmong.class);
        map.mapConstraint(Among.class, CAmong.class);
        map.mapConstraint(Quarantine.class, CQuarantine.class);
        map.mapConstraint(Ban.class, CBan.class);
        map.mapConstraint(Fence.class, CFence.class);
        map.mapConstraint(Online.class, COnline.class);
        map.mapConstraint(Offline.class, COffline.class);
        map.mapConstraint(RunningCapacity.class, CRunningCapacity.class);
        map.mapConstraint(ResourceCapacity.class, CResourceCapacity.class);
        map.mapConstraint(Preserve.class, CPreserve.class);
        map.mapConstraint(Overbook.class, COverbook.class);
        map.mapConstraint(Root.class, CRoot.class);
        map.mapConstraint(Ready.class, CReady.class);
        map.mapConstraint(Running.class, CRunning.class);
        map.mapConstraint(Sleeping.class, CSleeping.class);
        map.mapConstraint(Killed.class, CKilled.class);
        map.mapConstraint(Gather.class, CGather.class);
        map.mapConstraint(Lonely.class, CLonely.class);
        map.mapConstraint(Seq.class, CSequentialVMTransitions.class);
        map.mapConstraint(MaxOnline.class, CMaxOnline.class);
        map.mapConstraint(MinMTTR.class, CMinMTTR.class);
        map.mapConstraint(MinMTTRMig.class, CMinMTTRMig.class);
        map.mapConstraint(MinMigrations.class, CMinMigrations.class);
        map.mapConstraint(NoDelay.class, CNoDelay.class);
        map.mapConstraint(Deadline.class, CDeadline.class);
        map.mapConstraint(Precedence.class, CPrecedence.class);
        map.mapConstraint(Serialize.class, CSerialize.class);
        map.mapConstraint(Sync.class, CSync.class);

        map.mapView(ShareableResource.class, CShareableResource.class);
        map.mapView(Network.class, CNetwork.class);

        return map;
    }

    /**
     * Register a mapping between an api-side constraint and its choco implementation.
     * It is expected from the implementation to exhibit a constructor that takes the api-side constraint as argument.
     *
     * @param c  the api-side constraint
     * @param cc the choco implementation
     * @throws IllegalArgumentException if there is no suitable constructor for the choco implementation
     */
    public void mapConstraint(Class<? extends Constraint> c, Class<? extends ChocoConstraint> cc) {
        constraints.put(c, cc);
    }

    /**
     * Register a mapping between an api-side view and its choco implementation.
     * It is expected from the implementation to exhibit a constructor that takes the api-side constraint as argument.
     *
     * @param c  the api-side view
     * @param cc the choco implementation
     * @throws IllegalArgumentException if there is no suitable constructor for the choco implementation
     */
    public void mapView(Class<? extends ModelView> c, Class<? extends ChocoView> cc) {
        views.put(c, cc);
    }

    /**
     * Remove the mapping associated to a given {@link Constraint}.
     *
     * @param c the class of the {@link Constraint} to unMap
     * @return {@code true} if a mapping was registered
     */
    public boolean unMapConstraint(Class<? extends Constraint> c) {
        return constraints.remove(c) != null;
    }

    /**
     * Remove the mapping associated to a given {@link ModelView}.
     *
     * @param c the class of the {@link ModelView} to unMap
     * @return {@code true} if a mapping was registered
     */
    public boolean unMapView(Class<? extends ModelView> c) {
        return views.remove(c) != null;
    }

    /**
     * Check if a given mapping exists.
     *
     * @param c the constraint to check
     * @return {@code true} iff a mapping is established
     */
    public boolean constraintHasMapping(Class<? extends Constraint> c) {
        return constraints.containsKey(c);
    }

    /**
     * Check if a given mapping exists.
     *
     * @param c the view to check
     * @return {@code true} iff a mapping is established
     */
    public boolean viewHasMapping(Class<? extends ModelView> c) {
        return views.containsKey(c);
    }

    /**
     * Get the implementation of the given {@link Constraint}.
     *
     * @param c the constraint to translate
     * @return the associated {@link ChocoConstraint}, {@code null} if no mapping exists
     * @throws IllegalArgumentException if there is no suitable constructor for the choco implementation
     */
    public ChocoConstraint get(Constraint c) {
        Class<? extends ChocoConstraint> cc = constraints.get(c.getClass());
        if (cc == null) {
            return null;
        }

        try {

            return cc.getDeclaredConstructor(c.getClass()).newInstance(c);
        } catch (Exception ex) {
            throw new IllegalArgumentException("No constructor '" + cc.getSimpleName() + "(" + c.getClass().getSimpleName() + ")' available", ex);
        }
    }

    /**
     * Get the implementation of the given {@link ModelView}.
     *
     * @param c the view to translate
     * @return the associated {@link ChocoView}, {@code null} if no mapping exists
     * @throws IllegalArgumentException if there is no suitable constructor for the choco implementation
     */

    public ChocoView get(ModelView c) {
        Class<? extends ChocoView> cc = views.get(c.getClass());
        if (cc == null) {
            return null;
        }
        try {
            return cc.getDeclaredConstructor(c.getClass()).newInstance(c);
        } catch (Exception ex) {
            throw new IllegalArgumentException("No constructor '" + cc.getSimpleName() + "(" + c.getClass().getSimpleName() + ")' available", ex);
        }
    }
}
