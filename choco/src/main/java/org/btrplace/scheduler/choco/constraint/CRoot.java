/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.Instance;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Root;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.Slice;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;

import java.util.Collections;
import java.util.Set;


/**
 * Choco implementation for {@link org.btrplace.model.constraint.Root}.
 *
 * @author Fabien Hermenier
 */
public class CRoot implements ChocoConstraint {

  private final Root cstr;

    /**
     * Make a new constraint.
     *
     * @param r the constraint to rely on
     */
    public CRoot(Root r) {
        cstr = r;
    }

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem rp) throws SchedulerException {
        VM vm = cstr.getInvolvedVMs().iterator().next();
        VMTransition m = rp.getVMAction(vm);
        Slice cSlice = m.getCSlice();
        Slice dSlice = m.getDSlice();
        if (cSlice != null && dSlice != null) {
            try {
                dSlice.getHoster().instantiateTo(cSlice.getHoster().getValue(), Cause.Null);
            } catch (ContradictionException ex) {
                Node n = rp.getSourceModel().getMapping().getVMLocation(vm);
                rp.getLogger().error("Unable to force '" + vm + "' to be running on node '" + n + "'", ex);
                return false;
            }
        }
        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Instance i) {
        return Collections.emptySet();
    }

    @Override
    public String toString() {
        return cstr.toString();
    }
}
