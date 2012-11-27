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
import btrplace.model.constraint.Root;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.*;
import choco.cp.solver.CPSolver;

import java.util.Set;
import java.util.UUID;

/**
 * Choco implementation for {@link btrplace.model.constraint.Root}.
 *
 * @author Fabien Hermenier
 */
public class CRoot implements ChocoSatConstraint {

    private Root cstr;

    public CRoot(Root r) {
        cstr = r;
    }

    @Override
    public void inject(ReconfigurationProblem rp) throws SolverException {
        CPSolver s = rp.getSolver();
        for (UUID vm : cstr.getInvolvedVMs()) {
            ActionModel m = rp.getVMActions()[rp.getVM(vm)];
            Slice cSlice = m.getCSlice();
            Slice dSlice = m.getDSlice();
            if (cSlice != null && dSlice != null) {
                s.post(s.eq(cSlice.getHoster(), dSlice.getHoster()));
            }
        }
    }

    @Override
    public Root getAssociatedConstraint() {
        return cstr;
    }

    @Override
    public Set<UUID> getMisPlacedVMs(Model m) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isSatisfied(ReconfigurationPlan plan) {
        Mapping dst = plan.getResult().getMapping();
        Mapping src = plan.getOrigin().getMapping();
        for (UUID vm : cstr.getInvolvedVMs()) {
            if (src.getRunningVMs().contains(vm) && dst.getRunningVMs().contains(vm)) {
                if (!src.getVMLocation(vm).equals(dst.getVMLocation(vm))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * The builder associated to that constraint.
     */
    public static class Builder implements ChocoSatConstraintBuilder {
        @Override
        public Class<? extends SatConstraint> getKey() {
            return Root.class;
        }

        @Override
        public CRoot build(SatConstraint cstr) {
            return new CRoot((Root) cstr);
        }
    }
}
