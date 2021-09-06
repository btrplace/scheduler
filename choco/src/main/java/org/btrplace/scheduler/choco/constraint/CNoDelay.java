/*
 * Copyright  2021 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;

import java.util.Collections;
import java.util.Set;

/**
 * @author Vincent Kherbache
 */
public class CNoDelay implements ChocoConstraint {

  private final NoDelay noDelay;

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
                rp.getLogger().debug("Unable to prevent any delay on '" + v + "'", ex);
                return false;
            }
        } else {
            Constraint c = rp.getModel().arithm(d.getStart(), "=", 0);
            BoolVar move = ((RelocatableVM) vt).isStaying().not();
            ChocoUtils.postImplies(rp, move, c);
        }
        return true;
    }
}
