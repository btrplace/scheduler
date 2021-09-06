/*
 * Copyright  2021 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.Instance;
import org.btrplace.model.Mapping;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Running;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;

import java.util.Collections;
import java.util.Set;


/**
 * Naive implementation of {@link Running}.
 * This constraint is just a stub to be consistent with the model. It does not state any constraint
 * as the state has already been expressed inside {@link ReconfigurationProblem}.
 *
 * @author Fabien Hermenier
 */
public class CRunning implements ChocoConstraint {

  private final Running cstr;

    /**
     * Make a new constraint.
     *
     * @param c the constraint to rely on
     */
    public CRunning(Running c) {
        cstr = c;
    }

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem rp) throws SchedulerException {
        if (cstr.isContinuous() && !cstr.getChecker().startsWith(rp.getSourceModel())) {
            rp.getLogger().debug("Constraint {} is not satisfied initially", cstr);
            return false;
        }

        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Instance i) {
        VM vm = cstr.getInvolvedVMs().iterator().next();
        Mapping map = i.getModel().getMapping();
        if (!map.isRunning(vm)) {
            return Collections.singleton(vm);
        }
        return Collections.emptySet();
    }

    @Override
    public String toString() {
        return cstr.toString();
    }
}
