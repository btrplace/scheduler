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

package org.btrplace.scheduler.choco.constraint.migration;

import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.migration.Precedence;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.constraint.ChocoConstraint;
import org.btrplace.scheduler.choco.transition.RelocatableVM;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Arithmetic;
import org.chocosolver.solver.constraints.Operator;

import java.util.*;

/**
 * Choco implementation of the {@link org.btrplace.model.constraint.migration.Precedence} constraint.
 *
 * @author Vincent Kherbache
 */
public class CPrecedence implements ChocoConstraint {

    private Precedence pr;

    /**
     * The list of involved migrations.
     */
    private List<RelocatableVM> migrationList;

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

        // Get the solver
        Solver s = rp.getSolver();

        // Not enough / too much VMs
        if(pr.getInvolvedVMs().size() != 2) {
            rp.getLogger().error("Unable to inject the constraint '" + pr + "', the amount of involved VMs must be 2.");
            return false;
        }

        // Get all migrations involved
        for (Iterator<VM> ite = pr.getInvolvedVMs().iterator(); ite.hasNext();) {
            VM vm = ite.next();
            VMTransition vt = rp.getVMAction(vm);
            if (vt instanceof RelocatableVM) {
                migrationList.add((RelocatableVM)vt);
            }
        }

        // Not enough migrations
        if (migrationList.size() < 2) {
            rp.getLogger().error("Unable to inject the constraint '" + pr + "', the involved VMs are not migrating..");
            return false;
        }

        // Post the precedence constraint (involved VMs need to be ordered)
        s.post(new Arithmetic(migrationList.get(0).getEnd(), Operator.LE, migrationList.get(1).getStart()));

        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Model m) {
        return Collections.emptySet();
    }

}
