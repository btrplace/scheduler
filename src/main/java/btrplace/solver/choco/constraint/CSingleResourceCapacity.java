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
import btrplace.model.ShareableResource;
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

    /**
     * Make a new constraint.
     *
     * @param c the constraint to rely on
     */
    public CSingleResourceCapacity(SingleResourceCapacity c) {
        cstr = c;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SolverException {
        ResourceMapping rcm = rp.getResourceMapping(cstr.getResource());
        if (rcm == null) {
            throw new SolverException(rp.getSourceModel(), "Unable to find a resource mapping for resource '" + cstr.getResource() + "'");
        }
        int amount = cstr.getAmount();
        CPSolver s = rp.getSolver();
        for (UUID n : cstr.getInvolvedNodes()) {
            s.post(s.leq(rcm.getRealNodeUsage()[rp.getNode(n)], amount));
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
        ShareableResource rc = m.getResource(cstr.getResource());
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
        Model r = plan.getResult();
        if (r == null) {
            return false;
        }
        return cstr.isSatisfied(r).equals(SatConstraint.Sat.SATISFIED);
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
            return SingleResourceCapacity.class;
        }

        @Override
        public CSingleResourceCapacity build(SatConstraint cstr) {
            return new CSingleResourceCapacity((SingleResourceCapacity) cstr);
        }
    }
}
