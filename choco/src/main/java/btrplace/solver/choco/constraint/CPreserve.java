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

import btrplace.model.Model;
import btrplace.model.constraint.Preserve;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.view.ShareableResource;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.view.CShareableResource;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.HashSet;
import java.util.Set;


/**
 * Choco implementation of {@link btrplace.model.constraint.Preserve}.
 *
 * @author Fabien Hermenier
 */
public class CPreserve implements ChocoSatConstraint {

    private Preserve cstr;

    /**
     * Make a new constraint.
     *
     * @param p the constraint to rely on
     */
    public CPreserve(Preserve p) {
        cstr = p;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SolverException {
        CShareableResource map = (CShareableResource) rp.getView(ShareableResource.VIEW_ID_BASE + cstr.getResource());
        if (map == null) {
            throw new SolverException(rp.getSourceModel(), "Unable to get the resource mapper associated to '" +
                    cstr.getResource() + "'");
        }
        for (int vm : cstr.getInvolvedVMs()) {
            if (rp.getFutureRunningVMs().contains(vm)) {
                int idx = rp.getVMIdx(vm);
                IntDomainVar v = map.getVMsAllocation()[idx];
                try {
                    v.setInf(cstr.getAmount());
                } catch (ContradictionException ex) {
                    rp.getLogger().error("Unable to set the '{}' consumption for VM '{}' to '{}'", cstr.getResource(), cstr.getAmount(), ex.getMessage());
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Set<Integer> getMisPlacedVMs(Model m) {
        Set<Integer> bad = new HashSet<>();
        ShareableResource rc = (ShareableResource) m.getView(ShareableResource.VIEW_ID_BASE + cstr.getResource());
        if (rc == null) {
            bad.addAll(cstr.getInvolvedVMs());
        } else {
            for (int vm : cstr.getInvolvedVMs()) {
                int x = rc.getVMConsumption(vm);
                if (x < cstr.getAmount()) {
                    //TODO: Very inefficient. Resources may be  available
                    bad.add(vm);
                }
            }
        }
        return bad;
    }

    @Override
    public String toString() {
        return cstr.toString();
    }

    /**
     * The builder associated to that constraint.
     */
    public static class Builder implements ChocoSatConstraintBuilder {
        @Override
        public Class<? extends SatConstraint> getKey() {
            return Preserve.class;
        }

        @Override
        public CPreserve build(SatConstraint cstr) {
            return new CPreserve((Preserve) cstr);
        }
    }
}
