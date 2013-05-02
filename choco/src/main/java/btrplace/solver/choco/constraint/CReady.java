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
import btrplace.model.constraint.SatConstraint;
import btrplace.model.constraint.Ready;
import btrplace.model.constraint.Sleeping;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoSatConstraint;
import btrplace.solver.choco.ChocoSatConstraintBuilder;
import btrplace.solver.choco.ReconfigurationProblem;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Naive implementation of {@link btrplace.model.constraint.Ready}.
 * This constraint is just a stub to be consistent with the model. It does not state any constraint
 * as the state has already been expressed inside {@link btrplace.solver.choco.ReconfigurationProblem}.
 *
 * @author Fabien Hermenier
 */
public class CReady implements ChocoSatConstraint {

    private Ready cstr;

    /**
     * Make a new constraint.
     *
     * @param c the constraint to rely on
     */
    public CReady(Ready c) {
        cstr = c;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SolverException {
        return true;
    }

    @Override
    public Set<UUID> getMisPlacedVMs(Model m) {
        Set<UUID> bad = new HashSet<>();
        Mapping map = m.getMapping();
        for (UUID vm : cstr.getInvolvedVMs()) {
            if (!map.getReadyVMs().contains(vm)) {
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
            return Sleeping.class;
        }

        @Override
        public CReady build(SatConstraint cstr) {
            return new CReady((Ready) cstr);
        }
    }
}
