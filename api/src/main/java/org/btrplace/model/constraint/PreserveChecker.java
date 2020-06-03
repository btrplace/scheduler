/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.event.Allocate;
import org.btrplace.plan.event.AllocateEvent;


/**
 * Checker for the {@link org.btrplace.model.constraint.Preserve} constraint
 *
 * @author Fabien Hermenier
 * @see org.btrplace.model.constraint.Preserve
 */
public class PreserveChecker extends AllowAllConstraintChecker<Preserve> {

  private final int amount;

  private final String id;

    /**
     * Make a new checker.
     *
     * @param p the associated constraint
     */
    public PreserveChecker(Preserve p) {
        super(p);
        id = p.getResource();
        amount = p.getAmount();
    }

    @Override
    public boolean consume(AllocateEvent a) {
        if (getVMs().contains(a.getVM()) && a.getResourceId().equals(id)) {
            return a.getAmount() >= amount;
        }
        return true;
    }

    @Override
    public boolean start(Allocate a) {
        if (a.getResourceId().equals(id) && getVMs().contains(a.getVM())) {
            return a.getAmount() >= amount;
        }
        return true;
    }

    @Override
    public boolean startsWith(Model mo) {
        return true;
    }

    @Override
    public boolean endsWith(Model mo) {
        ShareableResource r = ShareableResource.get(mo, id);
        if (r == null) {
            return false;
        }
        for (VM vmId : getVMs()) {
            if (mo.getMapping().isRunning(vmId)) {
                int v = r.getConsumption(vmId);
                if (v < amount) {
                    return false;
                }
            }
        }
        return true;
    }
}
