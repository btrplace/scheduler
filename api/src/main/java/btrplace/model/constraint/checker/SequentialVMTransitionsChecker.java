/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.model.constraint.checker;

import btrplace.model.Model;
import btrplace.model.constraint.SequentialVMTransitions;
import btrplace.plan.event.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Checker for the {@link btrplace.model.constraint.SequentialVMTransitions} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.SequentialVMTransitions
 */
public class SequentialVMTransitionsChecker extends AllowAllConstraintChecker<SequentialVMTransitions> {

    private Set<Integer> runnings;

    private List<Integer> order;

    private int pending;

    /**
     * Make a new checker.
     *
     * @param s the associated constraint
     */
    public SequentialVMTransitionsChecker(SequentialVMTransitions s) {
        super(s);
        order = new ArrayList<>(s.getInvolvedVMs());
        pending = -1;
    }

    @Override
    public boolean startsWith(Model mo) {
        runnings = new HashSet<>(mo.getMapping().getRunningVMs());
        track(runnings);
        return true;
    }

    @Override
    public boolean start(MigrateVM a) {
        return true;
    }

    private boolean makePending(int vm) {
        if (getVMs().contains(vm)) {
            if (pending == -1) {
                //Burn all the VMs in order that are before vm
                while (!order.isEmpty() && !order.get(0).equals(vm)) {
                    order.remove(0);
                }
                if (order.isEmpty()) {
                    return false;
                }
                pending = vm;
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean start(BootVM a) {
        return makePending(a.getVM());
    }

    @Override
    public boolean start(ShutdownVM a) {
        return makePending(a.getVM());
    }

    @Override
    public boolean start(ResumeVM a) {
        return makePending(a.getVM());
    }

    @Override
    public boolean start(SuspendVM a) {
        return makePending(a.getVM());
    }

    @Override
    public boolean start(KillVM a) {
        if (runnings.contains(a.getVM())) {
            return makePending(a.getVM());
        }
        return true;
    }

    @Override
    public boolean start(ForgeVM a) {
        return makePending(a.getVM());
    }

    @Override
    public boolean startRunningVMPlacement(RunningVMPlacement a) {
        return makePending(a.getVM());
    }

    @Override
    public void end(BootVM a) {
        if (a.getVM() == pending) {
            pending = -1;
        }
    }

    @Override
    public void end(ShutdownVM a) {
        if (a.getVM() == pending) {
            pending = -1;
        }
    }

    @Override
    public void end(ResumeVM a) {
        if (a.getVM() == pending) {
            pending = -1;
        }

    }

    @Override
    public void end(SuspendVM a) {
        if (a.getVM() == pending) {
            pending = -1;
        }

    }

    @Override
    public void end(KillVM a) {
        if (a.getVM() == pending) {
            pending = -1;
        }
    }

    @Override
    public void end(ForgeVM a) {
        if (a.getVM() == pending) {
            pending = -1;
        }
    }
}
