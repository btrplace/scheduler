/*
 * Copyright  2021 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint.migration;

import org.btrplace.model.Instance;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.migration.Precedence;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.constraint.ChocoConstraint;
import org.btrplace.scheduler.choco.transition.RelocatableVM;
import org.btrplace.scheduler.choco.transition.VMTransition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Choco implementation of the {@link org.btrplace.model.constraint.migration.Precedence} constraint.
 *
 * @author Vincent Kherbache
 */
public class CPrecedence implements ChocoConstraint {

    private final Precedence pr;

    /**
     * The list of involved migrations.
     */
    private final List<RelocatableVM> migrationList;

    /**
     * Make a new constraint.
     *
     * @param pr the Precedence constraint to rely on
     */
    public CPrecedence(Precedence pr) {
        this.pr = pr;
        migrationList = new ArrayList<>();
    }

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem rp) throws SchedulerException {

        // Not enough / too much VMs
        if (pr.getInvolvedVMs().size() != 2) {
            rp.getLogger().debug("Unable to inject the constraint '{}', the amount of involved VMs must be 2.", pr);
            return false;
        }

        // Get all migrations involved
        for (VM vm : pr.getInvolvedVMs()) {
            VMTransition vt = rp.getVMAction(vm);
            if (vt instanceof RelocatableVM) {
                migrationList.add((RelocatableVM) vt);
            }
        }

        // Not enough migrations
        if (migrationList.size() < 2) {
            rp.getLogger().debug("Unable to inject the constraint '{}', the involved VMs are not migrating..", pr);
            return false;
        }

        // Post the precedence constraint (involved VMs need to be ordered)
        rp.getModel().post(rp.getModel().arithm(migrationList.get(0).getEnd(), "<=", migrationList.get(1).getStart()));

        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Instance i) {
        return Collections.emptySet();
    }

}
