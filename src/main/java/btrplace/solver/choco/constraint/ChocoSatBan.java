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

import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.constraint.Ban;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.SolverException;
import btrplace.solver.choco.ChocoConstraintBuilder;
import btrplace.solver.choco.ChocoSatConstraint;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;

import java.util.Set;
import java.util.UUID;

/**
 * Choco implementation of the constraint {@link Ban}.
 *
 * @author Fabien Hermenier
 */
public class ChocoSatBan implements ChocoSatConstraint {

    private Ban ban;

    public static class ChocoBanBuilder implements ChocoConstraintBuilder {
        @Override
        public Class<? extends SatConstraint> getKey() {
            return Ban.class;
        }

        @Override
        public ChocoSatBan build(SatConstraint cstr) {
            return new ChocoSatBan((Ban) cstr);
        }
    }

    public ChocoSatBan(Ban b) {
        ban = b;
    }

    @Override
    public void inject(ReconfigurationProblem rp) throws SolverException {
        Set<UUID> nodes = ban.getInvolvedNodes();
        Set<UUID> vms = ban.getInvolvedVMs();
        int[] nodesIdx = new int[nodes.size()];
        int i = 0;
        for (UUID n : ban.getInvolvedNodes()) {
            nodesIdx[i++] = rp.getNode(n);
        }

        for (UUID vm : vms) {
            if (rp.getFutureRunnings().contains(vm)) {
                Slice t = rp.getVMAction(rp.getVM(vm)).getDSlice();
                if (t != null) {
                    for (int x : nodesIdx) {
                        try {
                            t.getHoster().remVal(x);
                        } catch (Exception e) {
                            throw new SolverException(null, "Unable to disallow VM '" + vm + "' to be running on '" + rp.getNode(x) + "'");
                        }
                    }
                }
            }
        }
    }

    @Override
    public Ban getAssociatedConstraint() {
        return ban;
    }

    @Override
    public Set<UUID> getMisPlacedVMs(Model m) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isSatisfied(ReconfigurationPlan plan) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
