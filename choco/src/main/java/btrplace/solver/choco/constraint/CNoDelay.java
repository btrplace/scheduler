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

package btrplace.solver.choco.constraint;

import btrplace.model.Model;
import btrplace.model.VM;
import btrplace.model.constraint.Constraint;
import btrplace.model.constraint.NoDelay;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.extensions.ChocoUtils;
import btrplace.solver.choco.transition.RelocatableVM;
import btrplace.solver.choco.transition.VMTransition;
import solver.Cause;
import solver.Solver;
import solver.constraints.Arithmetic;
import solver.constraints.Operator;
import solver.exception.ContradictionException;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VF;

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
    public CNoDelay(NoDelay nd) { noDelay = nd; }

    @Override
    public Set<VM> getMisPlacedVMs(Model model) {
        return Collections.emptySet();
        //return new HashSet<VM>(noDelay.getInvolvedVMs());
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) {

        // Get the solver
        Solver s = rp.getSolver();

        VM v = noDelay.getInvolvedVMs().iterator().next();
        // For each vm involved in the constraint
        VMTransition vt = rp.getVMAction(v);
        // Get the VMTransition start time
        IntVar start = vt.getStart();

                /*
                //TODO: Something wrong with "vt.getDuration().getValue()" (not instanciated)
                // Special case of a 'possible' migration
                if (vt instanceof RelocatableVM) {

                    if (vt.getDuration().instantiated()) {
                        // Check if the Transition duration is > 0 (effective migration) and set a boolean accordingly
                        BoolVar b = (vt.getDuration().getValue() > 0 ? VariableFactory.one(s) : VariableFactory.zero(s));

                        // Add the constraint "(duration > 0) => start = 0" to the solver
                        s.post(new FastImpliesEq(b, start, 0));
                    }

                } else {
                */
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
            solver.constraints.Constraint c = new Arithmetic(d.getStart(), Operator.EQ, 0);
            BoolVar move = VF.not(((RelocatableVM) vt).isStaying());
            ChocoUtils.postImplies(s, move, c);
        }
        return true;
    }

    /**
     * Builder associated to the constraint.
     */
    public static class Builder implements ChocoConstraintBuilder {
        @Override
        public Class<? extends Constraint> getKey() {
            return NoDelay.class;
        }

        @Override
        public CNoDelay build(Constraint c) {
            return new CNoDelay((NoDelay) c);
        }
    }
}
