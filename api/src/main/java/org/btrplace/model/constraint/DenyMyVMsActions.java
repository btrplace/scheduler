/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.model.constraint;

import org.btrplace.plan.event.*;

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
    public DenyMyVMsActions(C s) {
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
