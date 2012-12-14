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
import btrplace.model.constraint.Fence;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoSatConstraint;
import btrplace.solver.choco.ChocoSatConstraintBuilder;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import choco.kernel.solver.ContradictionException;
import gnu.trove.TIntHashSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
    public boolean inject(ReconfigurationProblem rp) throws SolverException {

        Set<UUID> runnings = new HashSet<UUID>();
        for (UUID vm : cstr.getInvolvedVMs()) {
            if (rp.getFutureRunningVMs().contains(vm)) {
                runnings.add(vm);
            }
        }
        Collection<UUID> nodes = cstr.getInvolvedNodes();
        if (!runnings.isEmpty()) {
            if (nodes.size() == 1) { //Only 1 possible destination node, so we directly instantiate the variable.
                for (UUID vm : runnings) {
                    Slice t = rp.getVMAction(vm).getDSlice();
                    if (t != null) {
                        UUID n = nodes.iterator().next();
                        try {
                            t.getHoster().setVal(rp.getNode(n));
                        } catch (ContradictionException e) {
                            rp.getLogger().error("Unable to force VM '{}' to be running on '{}': {}", vm, n, e.getMessage());
                            return false;
                        }
                    }
                }
            } else {
                int[] iExlude = new int[rp.getSourceModel().getMapping().getAllNodes().size()];
                TIntHashSet toKeep = new TIntHashSet(nodes.size());
                for (UUID n : nodes) {
                    toKeep.add(rp.getNode(n));
                }
                int i = 0;
                for (UUID n : rp.getSourceModel().getMapping().getOnlineNodes()) {
                    int idx = rp.getNode(n);
                    if (!toKeep.contains(idx)) {
                        iExlude[i++] = idx;
                    }
                }

                for (UUID n : rp.getSourceModel().getMapping().getOnlineNodes()) {
                    int idx = rp.getNode(n);
                    if (!toKeep.contains(idx)) {
                        iExlude[i++] = idx;
                    }
                }

                //Domain restriction. Remove all the non-involved nodes
                for (UUID vm : runnings) {
                    Slice t = rp.getVMAction(vm).getDSlice();
                    if (t != null) {
                        for (int a = 0; a < i; a++) {
                            try {
                                t.getHoster().remVal(iExlude[a]);
                            } catch (ContradictionException e) {
                                rp.getLogger().error("Unable to disallow VM '{}' to be running on '{}': {}", vm, rp.getNode(iExlude[a]), e.getMessage());
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public SatConstraint getAssociatedConstraint() {
        return cstr;
    }

    @Override
    public Set<UUID> getMisPlacedVMs(Model m) {
        Mapping map = m.getMapping();
        Set<UUID> bad = new HashSet<UUID>();
        for (UUID vm : cstr.getInvolvedVMs()) {
            if (map.getRunningVMs().contains(vm) && !cstr.getInvolvedNodes().contains(map.getVMLocation(vm))) {
                bad.add(vm);
            }
        }
        return bad;
    }

    @Override
    public boolean isSatisfied(ReconfigurationPlan plan) {
        Model r = plan.getResult();
        return r != null && cstr.isSatisfied(r).equals(SatConstraint.Sat.SATISFIED);
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
