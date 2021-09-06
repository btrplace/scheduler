/*
 * Copyright  2021 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.Instance;
import org.btrplace.model.Mapping;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Offline;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.Slice;
import org.btrplace.scheduler.choco.transition.NodeTransition;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;

import java.util.Set;


/**
 * Choco implementation of {@link Offline}.
 *
 * @author Fabien Hermenier
 */
public class COffline implements ChocoConstraint {

  private final Offline cstr;

    /**
     * Make a new constraint.
     *
     * @param o the {@link Offline} constraint to rely on
     */
    public COffline(Offline o) {
        this.cstr = o;
    }

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem rp) throws SchedulerException {
        if (cstr.isContinuous() && !cstr.getChecker().startsWith(rp.getSourceModel())) {
            rp.getLogger().debug("Constraint {} is not satisfied initially", cstr);
            return false;
        }

        Node nId = cstr.getInvolvedNodes().iterator().next();
        int id = rp.getNode(nId);
        NodeTransition m = rp.getNodeAction(nId);
        try {
            m.getState().instantiateTo(0, Cause.Null);
            if (rp.getSourceModel().getMapping().isOffline(nId)) {
                m.getStart().instantiateTo(0, Cause.Null);
            }
        } catch (ContradictionException ex) {
            rp.getLogger().debug("Unable to force node '" + nId + "' at being offline", ex);
            return false;
        }
        for (VMTransition am : rp.getVMActions()) {
            Slice s = am.getDSlice();
            if (s != null) {
                try {
                    s.getHoster().removeValue(id, Cause.Null);
                } catch (ContradictionException e) {
                    rp.getLogger().debug("Unable to remove " + am.getVM() + " of node " + nId, e);
                }
            }
        }
        return true;

    }

    @Override
    public Set<VM> getMisPlacedVMs(Instance i) {
        Mapping mapping = i.getModel().getMapping();
        return mapping.getRunningVMs(cstr.getInvolvedNodes().iterator().next());
    }

    @Override
    public String toString() {
        return cstr.toString();
    }
}
