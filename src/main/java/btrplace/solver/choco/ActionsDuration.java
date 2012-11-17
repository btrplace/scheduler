/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.solver.choco;

import btrplace.plan.actions.BootNode;
import btrplace.plan.actions.MigrateVM;
import btrplace.plan.actions.ShutdownNode;
import btrplace.solver.choco.actionModel.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to store the {@link DurationEvaluator} associated to each of the possible actions.
 *
 * @author Fabien Hermenier
 */
public class ActionsDuration {

    private Map<Class, DurationEvaluator> durations;

    public ActionsDuration() {
        durations = new HashMap<Class, DurationEvaluator>();


        //Default constructors
        durations.put(MigrateVM.class, new ConstantDuration(MigrateVM.class, 1));
        durations.put(RunVM.class, new ConstantDuration(RunVM.class, 1));
        durations.put(StopVM.class, new ConstantDuration(StopVM.class, 1));
        durations.put(SuspendVM.class, new ConstantDuration(SuspendVM.class, 1));
        durations.put(ResumeVM.class, new ConstantDuration(ResumeVM.class, 1));
        durations.put(InstantiateVM.class, new ConstantDuration(InstantiateVM.class, 1));
        durations.put(ShutdownNode.class, new ConstantDuration(ShutdownNode.class, 1));
        durations.put(BootNode.class, new ConstantDuration(BootNode.class, 1));
    }

    public boolean register(DurationEvaluator e) {
        return durations.put(e.getKey(), e) != null;
    }

    public DurationEvaluator getEvaluator(Class cl) {
        return durations.get(cl);
    }
}
