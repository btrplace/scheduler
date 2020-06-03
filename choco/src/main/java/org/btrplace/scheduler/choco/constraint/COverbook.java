/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint;


import org.btrplace.model.Instance;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Overbook;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.view.CShareableResource;

import java.util.Collections;
import java.util.Set;


/**
 * Choco implementation of {@link Overbook}.
 *
 * @author Fabien Hermenier
 */
public class COverbook implements ChocoConstraint {

  private final Overbook cstr;

    /**
     * Make a new constraint.
     *
     * @param o the constraint to rely on
     */
    public COverbook(Overbook o) {
        cstr = o;
    }

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem rp) throws SchedulerException {

        CShareableResource rcm = (CShareableResource) rp.getRequiredView(ShareableResource.getIdentifier(cstr.getResource()));

        Node u = cstr.getInvolvedNodes().iterator().next();
        rcm.capOverbookRatio(rp.getNode(u), cstr.getRatio());
        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Instance i) {
        //Handled by CShareableResource
        return Collections.emptySet();
    }

    @Override
    public String toString() {
        return cstr.toString();
    }
}
