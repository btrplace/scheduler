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

import org.btrplace.model.Instance;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.NoDelay;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.Slice;
import org.btrplace.scheduler.choco.extensions.ChocoUtils;
import org.btrplace.scheduler.choco.transition.RelocatableVM;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Arithmetic;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.VF;

import java.util.Collections;
import java.util.Set;

/**
 * Created by vkherbac on 01/09/14.
 */
public class CNoDelay implements ChocoConstraint {

    private NoDelay noDelay;

    /**
     * Make a new constraint
     *
     * @param nd the NoDelay constraint to rely on
     */
    public CNoDelay(NoDelay nd) {
        noDelay = nd;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Instance model) {
        return Collections.emptySet();
    }

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem rp) {

        // Get the solver
        Solver s = rp.getSolver();

        VM v = noDelay.getInvolvedVMs().iterator().next();
        // For each vm involved in the constraint
        VMTransition vt = rp.getVMAction(v);
        // Get the VMTransition start time
        // Add the constraint "start = 0" to the solver
        Slice d = vt.getDSlice();
        if (d == null) {
            return true;
        }
        if (!(vt instanceof RelocatableVM)) {
            try {

                d.getStart().instantiateTo(0, Cause.Null);
            } catch (ContradictionException ex) {
                rp.getLogger().info("Unable to prevent any delay on '" + v + "'");
                return false;
            }
        } else {
            Arithmetic c = new Arithmetic(d.getStart(), Operator.EQ, 0);
            BoolVar move = VF.not(((RelocatableVM) vt).isStaying());
            ChocoUtils.postImplies(s, move, c);
        }
        return true;
    }
}
