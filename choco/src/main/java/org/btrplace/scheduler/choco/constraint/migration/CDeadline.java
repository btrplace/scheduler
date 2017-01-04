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
import org.btrplace.model.constraint.migration.Deadline;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.constraint.ChocoConstraint;
import org.btrplace.scheduler.choco.transition.RelocatableVM;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.constraints.Arithmetic;
import org.chocosolver.solver.constraints.Operator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

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
    private static int convertTimestamp(String timestamp) throws ParseException {

        // Get the deadline from timestamp
        int deadline;
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
        Date parsedDate;

        // Relative timestamp
        if (timestamp.startsWith("+")) {
            parsedDate = dateFormat.parse(timestamp.replace("+", ""));
            Calendar c = Calendar.getInstance();
            c.setTime(parsedDate);
            deadline = c.get(Calendar.SECOND) + c.get(Calendar.MINUTE) * 60 + c.get(Calendar.HOUR_OF_DAY) * 3600;
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
                deadline = (int) ((parsedDate.getTime() + (24 * 3600 * 1000) - now.getTime()) / 1000);
            }
        }

        return deadline;
    }

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem rp) throws SchedulerException {

        int deadline = 0;
        try {
            deadline = convertTimestamp(dl.getTimestamp());
        } catch (ParseException e) {
            throw new SchedulerException(rp.getSourceModel(), "Unable to parse the timestamp '"+dl.getTimestamp()+"'");
        }

        // Get all migrations involved
        for (VM vm : dl.getInvolvedVMs()) {
            VMTransition vt = rp.getVMAction(vm);
            if (vt instanceof RelocatableVM) {
                rp.getModel().post(new Arithmetic(vt.getEnd(), Operator.LE, deadline));
            }
        }

        return true;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Instance i) {
        return Collections.emptySet();
    }
}
