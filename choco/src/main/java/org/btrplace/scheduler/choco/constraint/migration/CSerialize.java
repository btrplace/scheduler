/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Choco implementation of the {@link org.btrplace.model.constraint.migration.Serialize} constraint.
 *
 * @author Vincent Kherbache
 */
public class CSerialize implements ChocoConstraint {

    private final Serialize ser;

    /**
     * The list of VMs to serialize.
     */
    private final List<RelocatableVM> migrationList;

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
        Model csp = rp.getModel();

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
        IntVar[] heights = new IntVar[tasks.size()];
        Arrays.fill(heights, csp.intVar(1));
        csp.post(csp.cumulative(
                tasks.toArray(new Task[tasks.size()]),
                heights,
                csp.intVar(1),
                true
        ));

        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Instance i) {
        return Collections.emptySet();
    }
}
