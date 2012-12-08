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
import btrplace.model.constraint.Running;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoSatConstraint;
import btrplace.solver.choco.ChocoSatConstraintBuilder;
import btrplace.solver.choco.ReconfigurationProblem;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Naive implementation of {@link Running}.
 * This constraint is just a stub to be consistent with the model. It does not state any constraint
 * as the state has already been expressed inside {@link ReconfigurationProblem}.
 *
 * @author Fabien Hermenier
 */
public class CRunning implements ChocoSatConstraint {

    private Running cstr;

    /**
     * Make a new constraint.
     *
     * @param c the constraint to rely on
     */
    public CRunning(Running c) {
        cstr = c;
    }

    @Override
    public void inject(ReconfigurationProblem rp) throws SolverException {

    }

    @Override
    public SatConstraint getAssociatedConstraint() {
        return cstr;
    }

    @Override
    public Set<UUID> getMisPlacedVMs(Model m) {
        Set<UUID> bad = new HashSet<UUID>();
        Mapping map = m.getMapping();
        for (UUID vm : cstr.getInvolvedVMs()) {
            if (!map.getRunningVMs().contains(vm)) {
                bad.add(vm);
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
     * Builder associated to the constraint.
     */
    public static class Builder implements ChocoSatConstraintBuilder {
        @Override
        public Class<? extends SatConstraint> getKey() {
            return Running.class;
        }

        @Override
        public CRunning build(SatConstraint cstr) {
            return new CRunning((Running) cstr);
        }
    }
}
