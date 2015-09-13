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
import org.btrplace.model.constraint.Seq;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.transition.RelocatableVM;
import org.btrplace.scheduler.choco.transition.StayAwayVM;
import org.btrplace.scheduler.choco.transition.Transition;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;

import java.util.*;

/**
 * Choco implementation of {@link org.btrplace.model.constraint.Seq}.
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
    public boolean inject(ReconfigurationProblem rp) throws SchedulerException {
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
