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

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.constraint.Ban;
import btrplace.model.constraint.Quarantine;
import btrplace.model.constraint.Root;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoSatConstraint;
import btrplace.solver.choco.ChocoSatConstraintBuilder;
import btrplace.solver.choco.ReconfigurationProblem;

import java.util.*;

/**
 * Choco implementation of {@link btrplace.model.constraint.Quarantine}.
 *
 * @author Fabien Hermenier
 */
public class CQuarantine implements ChocoSatConstraint {

    private Quarantine cstr;

    /**
     * Make a new constraint.
     *
     * @param q the quarantine constraint to rely on
     */
    public CQuarantine(Quarantine q) {
        this.cstr = q;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SolverException {
        // It is just a composition of a root constraint on the VMs on the given nodes (the zone)
        // plus a ban on the other VMs to prevent them for being hosted in the zone
        Mapping map = rp.getSourceModel().getMapping();
        Set<UUID> toRoot = new HashSet<UUID>();
        Set<UUID> toBan = new HashSet<UUID>();
        Collection<UUID> zone = cstr.getInvolvedNodes();
        for (UUID vm : rp.getFutureRunningVMs()) {
            if (zone.contains(map.getVMLocation(vm))) {
                toRoot.add(vm);
            } else {
                toBan.add(vm);
            }
        }

        map.getRunningVMs(cstr.getInvolvedNodes());

        CRoot r = new CRoot(new Root(toRoot));
        CBan b = new CBan(new Ban(toBan, new HashSet<UUID>(zone)));
        return (r.inject(rp) && b.inject(rp));

    }

    @Override
    public Set<UUID> getMisPlacedVMs(Model m) {
        return Collections.emptySet();
    }

    @Override
    public String toString() {
        return cstr.toString();
    }

    /**
     * Builder associated to the constraint.
     */
    public static class Builder implements ChocoSatConstraintBuilder {
        @Override
        public Class<? extends SatConstraint> getKey() {
            return Quarantine.class;
        }

        @Override
        public CQuarantine build(SatConstraint cstr) {
            return new CQuarantine((Quarantine) cstr);
        }
    }
}
