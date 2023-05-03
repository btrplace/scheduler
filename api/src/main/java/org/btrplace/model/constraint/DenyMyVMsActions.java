/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.plan.event.Allocate;
import org.btrplace.plan.event.AllocateEvent;
import org.btrplace.plan.event.ForgeVM;
import org.btrplace.plan.event.KillVM;
import org.btrplace.plan.event.RunningVMPlacement;
import org.btrplace.plan.event.ShutdownVM;
import org.btrplace.plan.event.SubstitutedVMEvent;
import org.btrplace.plan.event.SuspendVM;

/**
 * Basic checker that allow everything except all the actions on my VMs.
 *
 * @author Fabien Hermenier
 */
public abstract class DenyMyVMsActions<C extends SatConstraint> extends AllowAllConstraintChecker<C> {

    /**
     * New instance.
     *
     * @param s the constraint associated to the checker.
     */
    protected DenyMyVMsActions(C s) {
        super(s);
    }

    @Override
    public boolean start(ShutdownVM a) {
        return !getVMs().contains(a.getVM());
    }

    @Override
    public boolean start(SuspendVM a) {
        return !getVMs().contains(a.getVM());
    }

    @Override
    public boolean start(KillVM a) {
        return !getVMs().contains(a.getVM());
    }

    @Override
    public boolean start(Allocate a) {
        return !getVMs().contains(a.getVM());
    }

    @Override
    public boolean consume(SubstitutedVMEvent e) {
        return !getVMs().contains(e.getVM());
    }

    @Override
    public boolean consume(AllocateEvent e) {
        return !getVMs().contains(e.getVM());
    }

    @Override
    public boolean start(ForgeVM a) {
        return !getVMs().contains(a.getVM());
    }

    @Override
    public boolean startRunningVMPlacement(RunningVMPlacement a) {
        return !getVMs().contains(a.getVM());
    }
}
