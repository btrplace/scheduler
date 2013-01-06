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
import btrplace.model.constraint.Among;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoSatConstraint;
import btrplace.solver.choco.ChocoSatConstraintBuilder;
import btrplace.solver.choco.ReconfigurationProblem;

import java.util.Set;
import java.util.UUID;

/**
 * Choco implementation of the {@link btrplace.model.constraint.Among} constraint.
 *
 * @author Fabien Hermenier
 */
public class CAmong implements ChocoSatConstraint {

    private Among cstr;

    /**
     * Make a new constraint.
     *
     * @param a the constraint to rely on
     */
    public CAmong(Among a) {
        cstr = a;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SolverException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Set<UUID> getMisPlacedVMs(Model m) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Builder associated to the constraint.
     */
    public static class Builder implements ChocoSatConstraintBuilder {
        @Override
        public Class<? extends SatConstraint> getKey() {
            return Among.class;
        }

        @Override
        public CAmong build(SatConstraint cstr) {
            return new CAmong((Among) cstr);
        }
    }
}
