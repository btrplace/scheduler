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

package btrplace.solver.choco.constraint;

import btrplace.solver.choco.ActionModel;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.chocoUtil.LocalTaskScheduler;
import btrplace.solver.choco.chocoUtil.TaskScheduler;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A global constraint to help to plan all the slices in a reconfiguration problem.
 * <p/>
 * TODO: Should be able to work on a part only of the infra to be aligned with overbook signature.
 *
 * @author Fabien Hermenier
 */
public class RunningScheduler {

    public RunningScheduler() {
    }

    public void inject(ReconfigurationProblem rp) {
        List<Slice> dS = new LinkedList<Slice>();
        List<Slice> cS = new LinkedList<Slice>();

        List<int[]> linked = new ArrayList<int[]>();
        int dIdx = 0;
        int cIdx = 0;
        ActionModel[] allActions = rp.getVMActions();

        if (allActions.length == 0) {
            return;
        }

        //The boolean variable to indicate if a d-slice will be exclusive or not
        IntDomainVar[] excls = new IntDomainVar[rp.getNodes().length];
        int[] exclSlice = new int[rp.getNodes().length];
        for (int sliceIdx = 0; sliceIdx < allActions.length; sliceIdx++) {
            ActionModel na = allActions[sliceIdx];
            Slice cs = na.getCSlice();
            Slice ds = na.getDSlice();

            if (ds != null && cs != null) {
                linked.add(new int[]{dIdx, cIdx});
            }
            if (ds != null) {
                dS.add(dIdx, ds);
                //Check for the exclusive flag for Demanding Slice
                IntDomainVar excl = ds.isExclusive();
                //TODO: exclusive slice
                if (excl != null) {
                    if (rp.getNode(ds.getSubject()) >= 0) {
                        excls[rp.getNode(ds.getSubject())] = ds.isExclusive();
                        exclSlice[rp.getNode(ds.getSubject())] = dIdx;
                    }
                }
                dIdx++;
            }

            if (cs != null) {
                cS.add(cIdx, cs);
                cIdx++;
            }
        }
        Slice[] dSlices = dS.toArray(new Slice[dS.size()]);
        Slice[] cSlices = cS.toArray(new Slice[cS.size()]);


        int[] cUsages, dUsages;
        cUsages = new int[cSlices.length];

        IntDomainVar[] cHosters = new IntDomainVar[cSlices.length];
        IntDomainVar[] cEnds = new IntDomainVar[cSlices.length];
        for (int i = 0; i < cSlices.length; i++) {
            Slice c = cSlices[i];
            cHosters[i] = c.getHoster();
            cEnds[i] = c.getEnd();
            cUsages[i] = 1;
        }

        dUsages = new int[dSlices.length];
        IntDomainVar[] dHosters = new IntDomainVar[dSlices.length];
        IntDomainVar[] dStart = new IntDomainVar[dSlices.length];
        for (int i = 0; i < dSlices.length; i++) {
            Slice d = dSlices[i];
            dHosters[i] = d.getHoster();
            dStart[i] = d.getStart();
            //TODO: e is not necessarily a VM, seems to get to an API failure here
            dUsages[i] = 1;
        }

        int[] associations = new int[dHosters.length];
        for (int i = 0; i < associations.length; i++) {
            associations[i] = LocalTaskScheduler.NO_ASSOCIATIONS; //No associations task
        }
        for (int i = 0; i < linked.size(); i++) {
            int[] assoc = linked.get(i);
            associations[assoc[0]] = assoc[1];
        }

        int[] capacities = new int[rp.getNodes().length];
        for (int idx = 0; idx < rp.getNodes().length; idx++) {
            capacities[idx] = rp.getVMsCountOnNodes()[idx].getSup();
        }
        CPSolver s = rp.getSolver();
        s.post(new TaskScheduler(s.getEnvironment(),
                new int[][]{capacities}, cHosters, new int[][]{cUsages}, cEnds,
                dHosters, new int[][]{dUsages}, dStart, associations, excls, exclSlice));
    }
}
