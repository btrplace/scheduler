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
import org.btrplace.model.constraint.Constraint;
import org.btrplace.model.constraint.migration.Deadline;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.constraint.ChocoConstraint;
import org.btrplace.scheduler.choco.constraint.ChocoConstraintBuilder;
import org.btrplace.scheduler.choco.transition.RelocatableVM;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.LCF;
import org.chocosolver.solver.variables.VF;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Choco implementation of the {@link org.btrplace.model.constraint.migration.Deadline} constraint.
 *
 * @author Vincent Kherbache
 */
public class CDeadline implements ChocoConstraint {

    private Deadline dl;

    /**
     * Make a new constraint.
     *
     * @param dl the Deadline constraint to rely on
     */
    public CDeadline(Deadline dl) {
        this.dl = dl;
    }

    /**
     * Convert an absolute timestamp (string) to either a relative or an absolute deadline (integer) .
     *
     * @param timestamp the timestamp to convert
     * @return  the deadline
     * @throws ParseException if the timestamp string doesn't match the format "hh:mm:ss" for an absolute timestamp
     *         or "+hh:mm:ss" for relative timestamp
     */
    private int convertTimestamp(String timestamp) throws ParseException {

        // Get the deadline from timestamp
        int deadline;
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
        Date parsedDate = null;

        // Relative timestamp
        if (timestamp.startsWith("+")) {
            parsedDate = dateFormat.parse(timestamp.replace("+", ""));
            Calendar c = Calendar.getInstance();
            c.setTime(parsedDate);
            deadline = (c.get(Calendar.SECOND) + c.get(Calendar.MINUTE) * 60 + c.get(Calendar.HOUR_OF_DAY) * 3600);
        }
        // Absolute timestamp
        else {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            Date now = dateFormat.parse(c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE)+":"+c.get(Calendar.SECOND));
            parsedDate = dateFormat.parse(timestamp);
            deadline = (int) ((parsedDate.getTime() - now.getTime()) / 1000);
            if (deadline < 0) {
                // Timestamp is for tomorrow
                deadline = (int) (long) ((parsedDate.getTime() + (24 * 3600 * 1000) - now.getTime()) / 1000);
            }
        }

        return deadline;
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SchedulerException {

        int deadline = 0;
        try {
            deadline = convertTimestamp(dl.getTimestamp());
        } catch (ParseException e) {
            throw new SchedulerException(rp.getSourceModel(), "Unable to parse the timestamp '" + dl.getTimestamp() + "'");
        }

        // Get all migrations involved
        for (Iterator<VM> ite = dl.getInvolvedVMs().iterator(); ite.hasNext();) {
            VM vm = ite.next();
            VMTransition vt = rp.getVMAction(vm);
            if (vt instanceof RelocatableVM) {
                LCF.ifThen(VF.not(((RelocatableVM)vt).isStaying()), ICF.arithm(vt.getEnd(), "<=", deadline));
            }
        }

        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Model m) {
        return Collections.emptySet();
    }

    /**
     * Builder associated to the constraint.
     */
    public static class Builder implements ChocoConstraintBuilder {
        @Override
        public Class<? extends Constraint> getKey() {
            return Deadline.class;
        }

        @Override
        public CDeadline build(Constraint c) {
            return new CDeadline((Deadline) c);
        }
    }
}
