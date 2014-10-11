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

import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.plan.event.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Checker for the {@link Seq} constraint
 *
 * @author Fabien Hermenier
 * @see Seq
 */
public class SeqChecker extends AllowAllConstraintChecker<Seq> {

    private Set<VM> running;

    private List<VM> order;

    private VM pending;

    /**
     * Make a new checker.
     *
     * @param s the associated constraint
     */
    public SeqChecker(Seq s) {
        super(s);
        order = new ArrayList<>(s.getInvolvedVMs());
        pending = null;
    }

    @Override
    public boolean startsWith(Model mo) {
        running = new HashSet<>();
        for (Node n : mo.getMapping().getOnlineNodes()) {
            running.addAll(mo.getMapping().getRunningVMs(n));
        }
        track(running);
        return true;
    }

    @Override
    public boolean start(MigrateVM a) {
        return true;
    }

    private boolean makePending(VM vm) {
        if (getVMs().contains(vm)) {
            if (pending == null) {
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
        if (running.contains(a.getVM())) {
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
            pending = null;
        }
    }

    @Override
    public void end(ShutdownVM a) {
        if (a.getVM() == pending) {
            pending = null;
        }
    }

    @Override
    public void end(ResumeVM a) {
        if (a.getVM() == pending) {
            pending = null;
        }

    }

    @Override
    public void end(SuspendVM a) {
        if (a.getVM() == pending) {
            pending = null;
        }

    }

    @Override
    public void end(KillVM a) {
        if (a.getVM() == pending) {
            pending = null;
        }
    }

    @Override
    public void end(ForgeVM a) {
        if (a.getVM() == pending) {
            pending = null;
        }
    }
}
