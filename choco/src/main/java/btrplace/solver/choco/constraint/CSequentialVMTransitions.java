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
import btrplace.model.constraint.SatConstraint;
import btrplace.model.constraint.SequentialVMTransitions;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.actionModel.ActionModel;
import btrplace.solver.choco.actionModel.RelocatableVMModel;
import btrplace.solver.choco.actionModel.StayAwayVMModel;
import btrplace.solver.choco.actionModel.StayRunningVMModel;
import choco.cp.solver.CPSolver;

import java.util.*;

/**
 * Choco implementation of {@link btrplace.model.constraint.SequentialVMTransitions}.
 *
 * @author Fabien Hermenier
 */
public class CSequentialVMTransitions implements ChocoSatConstraint {

    private SequentialVMTransitions cstr;

    /**
     * Make a new constraint.
     *
     * @param c the constraint to rely on
     */
    public CSequentialVMTransitions(SequentialVMTransitions c) {
        cstr = c;
    }

    @Override
    public String toString() {
        return cstr.toString();
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SolverException {
        List<UUID> seq = cstr.getInvolvedVMs();

        List<ActionModel> ams = new ArrayList<>();
        for (UUID vmId : seq) {
            ActionModel am = rp.getVMAction(vmId);

            //Avoid VMs with no action model or ActionModel that do not denotes a state transition
            if (am == null || am instanceof StayRunningVMModel
                    || am instanceof StayAwayVMModel || am instanceof RelocatableVMModel) {
                continue;
            }
            ams.add(am);
        }
        if (ams.size() > 1) {
            Iterator<ActionModel> ite = ams.iterator();
            ActionModel prev = ite.next();
            CPSolver s = rp.getSolver();
            while (ite.hasNext()) {
                ActionModel cur = ite.next();
                s.post(s.leq(prev.getEnd(), cur.getStart()));
                prev = cur;
            }
        }
        return true;
    }

    @Override
    public Set<UUID> getMisPlacedVMs(Model m) {
        return Collections.emptySet();
    }

    /**
     * Builder associated to the constraint.
     */
    public static class Builder implements ChocoSatConstraintBuilder {
        @Override
        public Class<? extends SatConstraint> getKey() {
            return SequentialVMTransitions.class;
        }

        @Override
        public CSequentialVMTransitions build(SatConstraint cstr) {
            return new CSequentialVMTransitions((SequentialVMTransitions) cstr);
        }
    }
}
