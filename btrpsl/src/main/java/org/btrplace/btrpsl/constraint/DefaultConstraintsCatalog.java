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

package org.btrplace.btrpsl.constraint;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of a {@link ConstraintsCatalog}.
 *
 * @author Fabien Hermenier
 */
public class DefaultConstraintsCatalog implements ConstraintsCatalog {

    /**
     * The map to get the builder associated to a constraint.
     */
    private Map<String, SatConstraintBuilder> builders;

    /**
     * Build a new empty catalog.
     */
    public DefaultConstraintsCatalog() {
        this.builders = new HashMap<>();

    }

    /**
     * Build a catalog with a builder for every constraints
     * in the current BtrPlace bundle.
     *
     * @return a fulfilled catalog
     */
    public static DefaultConstraintsCatalog newBundle() {
        DefaultConstraintsCatalog c = new DefaultConstraintsCatalog();
        c.add(new AmongBuilder());
        c.add(new BanBuilder());
        c.add(new ResourceCapacityBuilder());
        c.add(new RunningCapacityBuilder());
        c.add(new FenceBuilder());
        c.add(new GatherBuilder());
        c.add(new KilledBuilder());
        c.add(new LonelyBuilder());
        c.add(new OfflineBuilder());
        c.add(new OnlineBuilder());
        c.add(new OverbookBuilder());
        c.add(new PreserveBuilder());
        c.add(new QuarantineBuilder());
        c.add(new ReadyBuilder());
        c.add(new RootBuilder());
        c.add(new RunningBuilder());
        c.add(new SleepingBuilder());
        c.add(new SplitBuilder());
        c.add(new SplitAmongBuilder());
        c.add(new SpreadBuilder());
        c.add(new SeqBuilder());
        c.add(new MaxOnlineBuilder());
        c.add(new NoDelayBuilder());
        return c;
    }

    /**
     * Add a constraint builder to the catalog.
     * There must not be another builder with the same identifier in the catalog
     *
     * @param c the constraint to add
     * @return true if the builder has been added.
     */
    public boolean add(SatConstraintBuilder c) {
        if (this.builders.containsKey(c.getIdentifier())) {
            return false;
        }
        this.builders.put(c.getIdentifier(), c);
        return true;
    }

    @Override
    public Set<String> getAvailableConstraints() {
        return this.builders.keySet();
    }

    @Override
    public SatConstraintBuilder getConstraint(String id) {
        return builders.get(id);
    }
}
