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
import btrplace.model.constraint.Ban;
import btrplace.model.constraint.Fence;
import btrplace.model.constraint.SatConstraint;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import choco.kernel.solver.ContradictionException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * Choco implementation of {@link btrplace.model.constraint.Fence}.
 *
 * @author Fabien Hermenier
 */
public class CFence implements ChocoSatConstraint {

    private Fence cstr;

    /**
     * Make a new constraint.
     *
     * @param c the constraint to rely on
     */
    public CFence(Fence c) {
        cstr = c;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) {

        Set<Integer> runnings = new HashSet<>();
        for (int vm : cstr.getInvolvedVMs()) {
            if (rp.getFutureRunningVMs().contains(vm)) {
                runnings.add(vm);
            }
        }
        Collection<Integer> nodes = cstr.getInvolvedNodes();
        if (!runnings.isEmpty()) {
            if (nodes.size() == 1) {
                //Only 1 possible destination node, so we directly instantiate the variable.
                for (int vm : runnings) {
                    Slice t = rp.getVMAction(vm).getDSlice();
                    int n = nodes.iterator().next();
                    try {
                        t.getHoster().setVal(rp.getNodeIdx(n));
                    } catch (ContradictionException e) {
                        rp.getLogger().error("Unable to force VM '{}' to be running on '{}': {}", vm, n, e.getMessage());
                        return false;
                    }
                }
            } else {
                //Transformation to a ban constraint that disallow all the other nodes
                Set<Integer> otherNodes = new HashSet<>(rp.getNodes().length - nodes.size());
                for (int n : rp.getNodes()) {
                    if (!nodes.contains(n)) {
                        otherNodes.add(n);
                    }
                }
                return new CBan(new Ban(runnings, otherNodes)).inject(rp);

            }
        }
        return true;
    }

    @Override
    public Set<Integer> getMisPlacedVMs(Model m) {
        Mapping map = m.getMapping();
        Set<Integer> bad = new HashSet<>();
        for (int vm : cstr.getInvolvedVMs()) {
            if (map.getRunningVMs().contains(vm) && !cstr.getInvolvedNodes().contains(map.getVMLocation(vm))) {
                bad.add(vm);
            }
        }
        return bad;
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
            return Fence.class;
        }

        @Override
        public CFence build(SatConstraint cstr) {
            return new CFence((Fence) cstr);
        }
    }
}
