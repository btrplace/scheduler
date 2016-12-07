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

package org.btrplace.scheduler.choco.runner;

import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.search.measure.Measures;

/**
 * @author Fabien Hermenier
 */
public class JoinableMeasuresRecorder extends Measures {


    public JoinableMeasuresRecorder(IMeasures mr) {
        super(mr.getModelName());

        nodeCount = mr.getNodeCount();
        readingTimeCount = (long) (mr.getReadingTimeCount() * 1.0E9F);
        timeCount = mr.getTimeCountInNanoSeconds();
        backtrackCount = mr.getBackTrackCount();
        failCount = mr.getFailCount();
        restartCount = mr.getRestartCount();
        timeCount = mr.getTimeCountInNanoSeconds();
        //hasObjective = mr.hasObjective();
        objectiveOptimal = mr.isObjectiveOptimal();
    }

    public JoinableMeasuresRecorder join(IMeasures m) {
        backtrackCount += m.getBackTrackCount();
        failCount += m.getFailCount();
        nodeCount += m.getNodeCount();
        readingTimeCount += m.getReadingTimeCount();
        restartCount += m.getRestartCount();
        timeCount += m.getTimeCountInNanoSeconds();
        objectiveOptimal = objectiveOptimal && m.isObjectiveOptimal();
        //hasObjective = hasObjective && m.hasObjective();
        return this;
    }
}
