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

package btrplace.safeplace.verification.btrplace;

import btrplace.model.Model;
import btrplace.model.VM;
import btrplace.model.constraint.Constraint;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.constraint.ChocoConstraint;
import btrplace.solver.choco.constraint.ChocoConstraintBuilder;
import btrplace.solver.choco.transition.Transition;
import solver.Cause;
import solver.exception.ContradictionException;

import java.util.Collections;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class CSchedule implements ChocoConstraint {

    private Schedule cstr;

    public CSchedule(Schedule s) {
        cstr = s;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SolverException {
        Transition am;

        if (cstr.getVM() != null) {
            am = rp.getVMAction(cstr.getVM());
        } else {
            am = rp.getNodeAction(cstr.getNode());
        }
        if (am == null) {
            return false;
        }
        try {
            am.getStart().instantiateTo(cstr.getStart(), Cause.Null);
            am.getEnd().instantiateTo(cstr.getEnd(), Cause.Null);
        } catch (ContradictionException ex) {
            rp.getLogger().error("Unable to force the schedule of " + am + " to " + cstr + ": " + ex.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Model m) {
        return Collections.emptySet();
    }

    public static class Builder implements ChocoConstraintBuilder {

        @Override
        public Class<? extends Constraint> getKey() {
            return Schedule.class;
        }

        @Override
        public CSchedule build(Constraint cstr) {
            return new CSchedule((Schedule) cstr);
        }
    }
}