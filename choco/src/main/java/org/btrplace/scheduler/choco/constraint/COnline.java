/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.Instance;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Online;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.transition.NodeTransition;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;

import java.util.Collections;
import java.util.Set;


/**
 * Choco implementation of {@link org.btrplace.model.constraint.Online}.
 *
 * @author Fabien Hermenier
 */
public class COnline implements ChocoConstraint {

  private final Online cstr;

    /**
     * Make a new constraint.
     *
     * @param o the {@link Online} to rely on
     */
    public COnline(Online o) {
        this.cstr = o;
    }

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem rp) throws SchedulerException {
        if (cstr.isContinuous() && !cstr.getChecker().startsWith(rp.getSourceModel())) {
            rp.getLogger().error("Constraint {} is not satisfied initially", cstr);
            return false;
        }
        Node nId = cstr.getInvolvedNodes().iterator().next();
        NodeTransition m = rp.getNodeAction(nId);
        try {
            m.getState().instantiateTo(1, Cause.Null);
            if (rp.getSourceModel().getMapping().isOnline(nId)) {
                m.getStart().instantiateTo(0, Cause.Null);
            }
        } catch (ContradictionException ex) {
            rp.getLogger().error("Unable to force node '" + nId + "' at being online", ex);
            return false;
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
