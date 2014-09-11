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
import btrplace.model.constraint.Seq;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.transition.RelocatableVM;
import btrplace.solver.choco.transition.StayAwayVM;
import btrplace.solver.choco.transition.Transition;
import solver.Solver;
import solver.constraints.IntConstraintFactory;

import java.util.*;

/**
 * Choco implementation of {@link btrplace.model.constraint.Seq}.
 *
 * @author Fabien Hermenier
 */
public class CSequentialVMTransitions implements ChocoConstraint {

    private Seq cstr;

    /**
     * Make a new constraint.
     *
     * @param c the constraint to rely on
     */
    public CSequentialVMTransitions(Seq c) {
        cstr = c;
    }

    @Override
    public String toString() {
        return cstr.toString();
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SolverException {
        List<VM> seq = cstr.getInvolvedVMs();

        List<Transition> ams = new ArrayList<>();
        for (VM vmId : seq) {
            Transition am = rp.getVMAction(vmId);

            //Avoid VMs with no action model or Transition that do not denotes a state transition
            if (am == null || am instanceof StayAwayVM || am instanceof RelocatableVM) {
                continue;
            }
            ams.add(am);
        }
        if (ams.size() > 1) {
            Iterator<Transition> ite = ams.iterator();
            Transition prev = ite.next();
            Solver s = rp.getSolver();
            while (ite.hasNext()) {
                Transition cur = ite.next();
                s.post(IntConstraintFactory.arithm(prev.getEnd(), "<=", cur.getStart()));
                prev = cur;
            }
        }
        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Model m) {
        return Collections.emptySet();
    }

    /**
     * Builder associated to the constraint.
     */
    public static class Builder implements ChocoConstraintBuilder {
        @Override
        public Class<? extends Constraint> getKey() {
            return Seq.class;
        }

        @Override
        public CSequentialVMTransitions build(Constraint c) {
            return new CSequentialVMTransitions((Seq) c);
        }
    }
}
