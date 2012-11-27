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
import btrplace.model.StackableResource;
import btrplace.model.constraint.SingleResourceCapacity;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoSatConstraint;
import btrplace.solver.choco.ChocoSatConstraintBuilder;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.ResourceMapping;
import choco.cp.solver.CPSolver;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Choco implementation of {@link btrplace.model.constraint.SingleResourceCapacity}.
 *
 * @author Fabien Hermenier
 */
public class CSingleResourceCapacity implements ChocoSatConstraint {

    private SingleResourceCapacity cstr;

    public CSingleResourceCapacity(SingleResourceCapacity c) {
        cstr = c;
    }

    @Override
    public void inject(ReconfigurationProblem rp) throws SolverException {
        ResourceMapping rcm = rp.getResourceMapping(cstr.getResource());
        if (rcm == null) {
            throw new SolverException(rp.getSourceModel(), "Unable to find a resource mapping for resource '" + cstr.getResource() + "'");
        }
        int amount = cstr.getAmount();
        CPSolver s = rp.getSolver();
        for (UUID n : cstr.getInvolvedNodes()) {
            s.post(s.leq(rcm.getRealNodeUsage()[rp.getNode(n)], amount));
        }
    }

    @Override
    public SatConstraint getAssociatedConstraint() {
        return cstr;
    }

    @Override
    public Set<UUID> getMisPlacedVMs(Model m) {
        Mapping map = m.getMapping();
        Set<UUID> bad = new HashSet<UUID>();
        StackableResource rc = m.getResource(cstr.getResource());
        for (UUID n : cstr.getInvolvedNodes()) {
            int remainder = cstr.getAmount();
            for (UUID v : map.getRunningVMs(n)) {
                remainder -= rc.get(v);
                if (remainder < 0) {
                    bad.addAll(map.getRunningVMs(n));
                    break;
                }
            }
        }
        return bad;
    }

    @Override
    public boolean isSatisfied(ReconfigurationPlan plan) {
        return cstr.isSatisfied(plan.getResult()).equals(SatConstraint.Sat.SATISFIED);
    }

    /**
     * The builder associated to that constraint.
     */
    public static class Builder implements ChocoSatConstraintBuilder {
        @Override
        public Class<? extends SatConstraint> getKey() {
            return SingleResourceCapacity.class;
        }

        @Override
        public CSingleResourceCapacity build(SatConstraint cstr) {
            return new CSingleResourceCapacity((SingleResourceCapacity) cstr);
        }
    }
}
