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
import org.btrplace.model.constraint.migration.Serialize;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.constraint.ChocoConstraint;
import org.btrplace.scheduler.choco.transition.RelocatableVM;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.VF;

import java.util.*;

/**
 * Choco implementation of the {@link org.btrplace.model.constraint.migration.Serialize} constraint.
 *
 * @author Vincent Kherbache
 */
public class CSerialize implements ChocoConstraint {

    private Serialize ser;

    /**
     * The list of VMs to serialize.
     */
    private List<RelocatableVM> migrationList;

    /**
     * Make a new constraint.
     *
     * @param ser the Serialize constraint to rely on
     */
    public CSerialize(Serialize ser) {
        this.ser = ser;
        migrationList = new ArrayList<>();
    }

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem rp) throws SchedulerException {

        // Get the solver
        Solver s = rp.getSolver();

        // Not enough VMs
        if (ser.getInvolvedVMs().size() < 2) {
            return true;
        }

        // Get all migrations involved
        for (VM vm : ser.getInvolvedVMs()) {
            VMTransition vt = rp.getVMAction(vm);
            if (vt instanceof RelocatableVM) {
                migrationList.add((RelocatableVM) vt);
            }
        }

        // Not enough migrations
        if (migrationList.size() < 2) {
            return true;
        }

        // Using a cumulative
        List<Task> tasks = new ArrayList<>();
        for (RelocatableVM mig : migrationList) {
            tasks.add(new Task(mig.getStart(), mig.getDuration(), mig.getEnd()));
        }
        IntVar heights[] = new IntVar[tasks.size()];
        Arrays.fill(heights, VF.fixed(1, s));
        s.post(ICF.cumulative(
                tasks.toArray(new Task[tasks.size()]),
                heights,
                VF.fixed(1, s),
                true
        ));

        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Instance i) {
        return Collections.emptySet();
    }
}
