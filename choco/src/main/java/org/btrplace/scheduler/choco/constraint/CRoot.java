/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Constraint;
import org.btrplace.model.constraint.Root;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.Slice;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;

import java.util.Collections;
import java.util.Set;


/**
 * Choco implementation for {@link org.btrplace.model.constraint.Root}.
 *
 * @author Fabien Hermenier
 */
public class CRoot implements ChocoConstraint {

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
    public boolean inject(ReconfigurationProblem rp) throws SchedulerException {
        Solver s = rp.getSolver();
        VM vm = cstr.getInvolvedVMs().iterator().next();
        VMTransition m = rp.getVMAction(vm);
        Slice cSlice = m.getCSlice();
        Slice dSlice = m.getDSlice();
        if (cSlice != null && dSlice != null) {
            s.post(IntConstraintFactory.arithm(cSlice.getHoster(), "=", dSlice.getHoster()));
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
    public static class Builder implements ChocoConstraintBuilder {
        @Override
        public Class<? extends Constraint> getKey() {
            return Root.class;
        }

        @Override
        public CRoot build(Constraint c) {
            return new CRoot((Root) c);
        }
    }
}
