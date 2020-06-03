/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.Instance;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Preserve;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.view.CShareableResource;

import java.util.Collections;
import java.util.Set;


/**
 * Choco implementation of {@link org.btrplace.model.constraint.Preserve}.
 *
 * @author Fabien Hermenier
 */
public class CPreserve implements ChocoConstraint {

  private final Preserve cstr;

    /**
     * Make a new constraint.
     *
     * @param p the constraint to rely on
     */
    public CPreserve(Preserve p) {
        cstr = p;
    }

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem rp) throws SchedulerException {
        CShareableResource map = (CShareableResource) rp.getRequiredView(ShareableResource.getIdentifier(cstr.getResource()));
        VM vm = cstr.getInvolvedVMs().iterator().next();
        if (rp.getFutureRunningVMs().contains(vm)) {
            int idx = rp.getVM(vm);
            map.minVMAllocation(idx, cstr.getAmount());
        }
        return true;
    }

    /**
     * {@inheritDoc}.
     * This implementation is just a stub. A proper estimation will be made directly by {@link CShareableResource#getMisPlacedVMs(Instance)}.
     *
     * @param i the instance to inspect
     * @return an empty set
     */
    @Override
    public Set<VM> getMisPlacedVMs(Instance i) {
        return Collections.emptySet();
    }

    @Override
    public String toString() {
        return cstr.toString();
    }
}
