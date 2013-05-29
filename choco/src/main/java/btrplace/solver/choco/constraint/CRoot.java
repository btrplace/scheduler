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
import btrplace.model.VM;
import btrplace.model.constraint.Root;
import btrplace.model.constraint.SatConstraint;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.actionModel.VMActionModel;
import choco.cp.solver.CPSolver;

import java.util.Collections;
import java.util.Set;


/**
 * Choco implementation for {@link btrplace.model.constraint.Root}.
 *
 * @author Fabien Hermenier
 */
public class CRoot implements ChocoSatConstraint {

    private Root cstr;

    /**
     * Make a new constraint.
     *
     * @param r the constraint to rely on
     */
    public CRoot(Root r) {
        cstr = r;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SolverException {
        CPSolver s = rp.getSolver();
        for (VM vm : cstr.getInvolvedVMs()) {
            VMActionModel m = rp.getVMAction(vm);
            Slice cSlice = m.getCSlice();
            Slice dSlice = m.getDSlice();
            if (cSlice != null && dSlice != null) {
                s.post(s.eq(cSlice.getHoster(), dSlice.getHoster()));
            }
        }
        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Model m) {
        return Collections.emptySet();
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
            return Root.class;
        }

        @Override
        public CRoot build(SatConstraint cstr) {
            return new CRoot((Root) cstr);
        }
    }
}
