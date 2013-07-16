/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
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
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.Ban;
import btrplace.model.constraint.Constraint;
import btrplace.model.constraint.Quarantine;
import btrplace.model.constraint.Root;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Choco implementation of {@link btrplace.model.constraint.Quarantine}.
 *
 * @author Fabien Hermenier
 */
public class CQuarantine implements ChocoConstraint {

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
        Set<VM> toRoot = new HashSet<>();
        Set<VM> toBan = new HashSet<>();
        Collection<Node> zone = cstr.getInvolvedNodes();
        for (VM vm : rp.getFutureRunningVMs()) {
            if (zone.contains(map.getVMLocation(vm))) {
                toRoot.add(vm);
            } else {
                toBan.add(vm);
            }
        }

        map.getRunningVMs(cstr.getInvolvedNodes());

        CRoot r = new CRoot(new Root(toRoot));
        CBan b = new CBan(new Ban(toBan, new HashSet<>(zone)));
        return (r.inject(rp) && b.inject(rp));

    }

    @Override
    public Set<VM> getMisPlacedVMs(Model m) {
        return Collections.emptySet();
    }

    @Override
    public String toString() {
        return cstr.toString();
    }

    /**
     * Builder associated to the constraint.
     */
    public static class Builder implements ChocoConstraintBuilder {
        @Override
        public Class<? extends Constraint> getKey() {
            return Quarantine.class;
        }

        @Override
        public CQuarantine build(Constraint cstr) {
            return new CQuarantine((Quarantine) cstr);
        }
    }
}
