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
import btrplace.solver.choco.ChocoSatConstraint;
import btrplace.solver.choco.ChocoSatConstraintBuilder;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import choco.kernel.solver.ContradictionException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Choco implementation of the constraint {@link Ban}.
 *
 * @author Fabien Hermenier
 */
public class CBan implements ChocoSatConstraint {

    private Ban ban;

    /**
     * Make a new constraint.
     *
     * @param b the ban constraint to rely on
     */
    public CBan(Ban b) {
        ban = b;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) {
        Collection<UUID> nodes = ban.getInvolvedNodes();
        Collection<UUID> vms = ban.getInvolvedVMs();
        int[] nodesIdx = new int[nodes.size()];
        int i = 0;
        for (UUID n : ban.getInvolvedNodes()) {
            nodesIdx[i++] = rp.getNode(n);
        }

        for (UUID vm : vms) {
            if (rp.getFutureRunningVMs().contains(vm)) {
                Slice t = rp.getVMAction(vm).getDSlice();
                if (t != null) {
                    for (int x : nodesIdx) {
                        try {
                            t.getHoster().remVal(x);
                        } catch (ContradictionException e) {
                            rp.getLogger().error("Unable to disallow VM '{}' to be running on '{}': {}", vm, rp.getNode(x), e.getMessage());
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public Set<UUID> getMisPlacedVMs(Model m) {
        Mapping map = m.getMapping();

        Set<UUID> bad = new HashSet<UUID>();
        for (UUID vm : ban.getInvolvedVMs()) {
            if (map.getRunningVMs().contains(vm) && ban.getInvolvedNodes().contains(map.getVMLocation(vm))) {
                bad.add(vm);
            }
        }
        return bad;
    }

    @Override
    public String toString() {
        return ban.toString();
    }

    /**
     * Builder associated to the constraint.
     */
    public static class Builder implements ChocoSatConstraintBuilder {
        @Override
        public Class<? extends SatConstraint> getKey() {
            return Ban.class;
        }

        @Override
        public CBan build(SatConstraint cstr) {
            return new CBan((Ban) cstr);
        }
    }
}
