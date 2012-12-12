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
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: fhermeni
 * Date: 05/12/12
 * Time: 08:41
 * To change this template use File | Settings | File Templates.
 */
public class TaskSchedulerBuilder {

    private static TaskSchedulerBuilder instance = null;

    private ReconfigurationProblem rp;

    private List<IntDomainVar[]> capacities;

    private List<int[]> cUsages;

    private List<IntDomainVar[]> dUsages;

    private IntDomainVar[] excls;

    private int[] exclSlice;

    private int[] associations;

    private IntDomainVar[] cEnds;

    private IntDomainVar[] cHosters;

    private IntDomainVar[] dHosters;

    private IntDomainVar[] dStarts;

    private Slice[] cSlices;

    private Slice[] dSlices;

    private TaskSchedulerBuilder(ReconfigurationProblem rp) {
        this.rp = rp;
        capacities = new ArrayList<IntDomainVar[]>();
        cUsages = new ArrayList<int[]>();
        dUsages = new ArrayList<IntDomainVar[]>();

        List<Slice> dS = new ArrayList<Slice>();
        List<Slice> cS = new ArrayList<Slice>();


        List<int[]> linked = new ArrayList<int[]>();
        int dIdx = 0, cIdx = 0;
        excls = new IntDomainVar[rp.getNodes().length];
        exclSlice = new int[rp.getNodes().length];

        for (ActionModel a : rp.getVMActions()) {
            Slice c = a.getCSlice();
            Slice d = a.getDSlice();

            if (d != null && c != null) {
                linked.add(new int[]{dIdx, cIdx});
            }
            if (d != null) {
                dS.add(dIdx, d);
                //Check for the exclusive flag for Demanding Slice
                IntDomainVar excl = d.isExclusive();
                //TODO: exclusive slice
                if (excl != null) {
                    if (rp.getNode(d.getSubject()) >= 0) {
                        excls[rp.getNode(d.getSubject())] = d.isExclusive();
                        exclSlice[rp.getNode(d.getSubject())] = dIdx;
                    }
                }
                dIdx++;
            }

            if (c != null) {
                cS.add(cIdx, c);
                cIdx++;
            }
        }


        int i = 0;
        cSlices = new Slice[cS.size()];
        cHosters = new IntDomainVar[cS.size()];
        cEnds = new IntDomainVar[cS.size()];
        for (Slice s : cS) {
            cHosters[i] = s.getHoster();
            cEnds[i] = s.getEnd();
            cSlices[i] = s;
            i++;

        }

        i = 0;
        dSlices = new Slice[dS.size()];
        dStarts = new IntDomainVar[dS.size()];
        dHosters = new IntDomainVar[dS.size()];

        for (Slice s : dS) {
            dHosters[i] = s.getHoster();
            dStarts[i] = s.getStart();
            dSlices[i] = s;
            i++;
        }


        associations = new int[dHosters.length];
        for (i = 0; i < associations.length; i++) {
            associations[i] = LocalTaskScheduler.NO_ASSOCIATIONS; //No associations task
        }
        for (i = 0; i < linked.size(); i++) {
            int[] assoc = linked.get(i);
            associations[assoc[0]] = assoc[1];
        }
    }

    public static void begin(ReconfigurationProblem rp) {
        instance = new TaskSchedulerBuilder(rp);
    }

    public static TaskSchedulerBuilder getInstance() {
        return instance;
    }

    public void add(IntDomainVar[] capa, int[] cUse, IntDomainVar[] dUse) {
        capacities.add(capa);
        cUsages.add(cUse);
        dUsages.add(dUse);
    }

    public void commitConstraint() {
        CPSolver s = rp.getSolver();

        //We get the UB of the node capacity and the LB for the VM usage.
        int[][] capas = new int[capacities.size()][];
        int i = 0;
        for (IntDomainVar[] capaDim : capacities) {
            capas[i] = new int[capaDim.length];
            for (int j = 0; j < capaDim.length; j++) {
                capas[i][j] = capaDim[j].getSup();
            }
            i++;
        }
        i = 0;
        int[][] cUses = new int[cUsages.size()][];

        for (int[] cUseDim : cUsages) {
            cUses[i++] = cUseDim;
        }

        i = 0;

        int[][] dUses = new int[dUsages.size()][];
        for (IntDomainVar[] dUseDim : dUsages) {
            dUses[i] = new int[dUseDim.length];
            for (int j = 0; j < dUseDim.length; j++) {
                dUses[i][j] = dUseDim[j].getInf();
            }
            i++;
        }
        s.post(new TaskScheduler(s.getEnvironment(),
                capas,
                cHosters, cUses, cEnds,
                dHosters, dUses, dStarts,
                associations, excls, exclSlice));


        instance = null;
    }
}
