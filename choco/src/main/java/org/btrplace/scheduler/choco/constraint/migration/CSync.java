/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
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

    private Sync sec;

    /**
     * The list of migrations to synchronize.
     */
    private List<RelocatableVM> migrationList;

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

        Solver s = rp.getSolver();

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

                s.post(ICF.arithm(firstMigSync, "=", secondMigSync));

            }
        }

        return true;
    }
}
