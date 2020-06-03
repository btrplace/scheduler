/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.testing.verification.btrplace;

import org.btrplace.model.Instance;
import org.btrplace.model.VM;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.constraint.ChocoConstraint;
import org.btrplace.scheduler.choco.transition.Transition;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;

import java.util.Collections;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class CSchedule implements ChocoConstraint {

  private final Schedule cstr;

    public CSchedule(Schedule s) {
        cstr = s;
    }

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem rp) throws SolverException {
        Transition<?> am;

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
            rp.getLogger().error("Unable to force the schedule of " + am + " to " + cstr, ex);
            return false;
        }
        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Instance m) {
        return Collections.emptySet();
    }
}
