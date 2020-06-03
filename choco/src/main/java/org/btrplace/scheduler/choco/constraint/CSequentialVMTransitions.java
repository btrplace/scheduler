/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.Instance;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Seq;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.transition.RelocatableVM;
import org.btrplace.scheduler.choco.transition.StayAwayVM;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Choco implementation of {@link org.btrplace.model.constraint.Seq}.
 *
 * @author Fabien Hermenier
 */
public class CSequentialVMTransitions implements ChocoConstraint {

  private final Seq cstr;

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
    public boolean inject(Parameters ps, ReconfigurationProblem rp) throws SchedulerException {
        List<VM> seq = cstr.getInvolvedVMs();

        List<VMTransition> ams = new ArrayList<>();
        for (VM vmId : seq) {
            VMTransition am = rp.getVMAction(vmId);

            //Avoid VMs with no action model or Transition that do not denotes a state transition
            if (am == null || am instanceof StayAwayVM || am instanceof RelocatableVM) {
                continue;
            }
            ams.add(am);
        }
        if (ams.size() > 1) {
            Iterator<VMTransition> ite = ams.iterator();
            VMTransition prev = ite.next();
            Model csp = rp.getModel();
            while (ite.hasNext()) {
                VMTransition cur = ite.next();
                csp.post(csp.arithm(prev.getEnd(), "<=", cur.getStart()));
                prev = cur;
            }
        }
        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Instance i) {
        return Collections.emptySet();
    }
}
