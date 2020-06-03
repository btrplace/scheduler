/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.plan.event.BootVM;
import org.btrplace.plan.event.ForgeVM;
import org.btrplace.plan.event.KillVM;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.plan.event.ResumeVM;
import org.btrplace.plan.event.RunningVMPlacement;
import org.btrplace.plan.event.ShutdownVM;
import org.btrplace.plan.event.SuspendVM;

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

  private final List<VM> order;

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
