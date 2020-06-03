/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint.migration;

import org.btrplace.model.Instance;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.migration.Sync;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.constraint.ChocoConstraint;
import org.btrplace.scheduler.choco.transition.RelocatableVM;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Choco implementation of the {@link org.btrplace.model.constraint.migration.Sync} constraint.
 *
 * @author Vincent Kherbache
 */
public class CSync implements ChocoConstraint {

    private final Sync sec;

    /**
     * The list of migrations to synchronize.
     */
    private final List<RelocatableVM> migrationList;

    /**
     * Make a new constraint
     *
     * @param sec the Sync constraint to rely on
     */
    public CSync(Sync sec) {
        this.sec = sec;
        migrationList = new ArrayList<>();
    }

    @Override
    public Set<VM> getMisPlacedVMs(Instance model) {
        return Collections.emptySet();
    }

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem rp) throws SchedulerException {

        Model csp = rp.getModel();

        // Get all migrations involved
        for (VM vm : sec.getInvolvedVMs()) {
            VMTransition vt = rp.getVMAction(vm);
            if (vt instanceof RelocatableVM) {
                migrationList.add((RelocatableVM) vt);
            }
        }

        for (int i=0 ; i < migrationList.size(); i++) {
            for (int j=i+1 ; j < migrationList.size(); j++) {
                RelocatableVM vm1 = migrationList.get(i);
                RelocatableVM vm2 = migrationList.get(j);

                // Sync the start or end depending of the migration algorithm
                //TODO: support VMs that are not necessarily moving
                IntVar firstMigSync = vm1.usesPostCopy() ? vm1.getStart() : vm1.getEnd();
                IntVar secondMigSync = vm2.usesPostCopy() ? vm2.getStart() : vm2.getEnd();

                csp.post(csp.arithm(firstMigSync, "=", secondMigSync));

            }
        }

        return true;
    }
}
