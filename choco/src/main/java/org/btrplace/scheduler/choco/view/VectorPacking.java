/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.view;

import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.List;


/*
 * View to generated the vector packing constraint.
 *
 * @author Sophie Demassey
 */
public class VectorPacking extends Packing {

    private List<List<IntVar>> loads;

    private List<IntVar[]> bins;

    private List<int[]> sizes;

    private List<String> names;

    private IStateInt[][] assignedLoad;

    private int dim;

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem rp) throws SchedulerException {
        loads = new ArrayList<>();
        bins = new ArrayList<>();
        sizes = new ArrayList<>();
        names = new ArrayList<>();
        dim = 0;
        return true;
    }

    @Override
    public void addDim(String name, List<IntVar> l, int[] s, IntVar[] b) {
        this.loads.add(l);
        this.sizes.add(s);
        this.bins.add(b);
        this.names.add(name);
        this.dim++;
    }

    @Override
    @SuppressWarnings("squid:S3346")
    public boolean beforeSolve(ReconfigurationProblem p) {
        super.beforeSolve(p);
        int[][] aSizes = new int[dim][sizes.get(0).length];
        IntVar[][] aLoads = new IntVar[dim][];
        String[] aNames = new String[dim];
        for (int d = 0; d < dim; d++) {
            aLoads[d] = loads.get(d).toArray(new IntVar[loads.get(d).size()]);
            assert bins.get(d).length == 0 || bins.get(d)[0].equals(bins.get(0)[0]);
            aNames[d] = names.get(d);
            int[] s = sizes.get(d);
            aSizes[d] = s;
        }
        if (!p.getFutureRunningVMs().isEmpty()) {
            org.btrplace.scheduler.choco.extensions.pack.VectorPacking c = new org.btrplace.scheduler.choco.extensions.pack.VectorPacking(aNames, aLoads, aSizes, bins.get(0));
            p.getModel().post(c);
            assignedLoad = c.assignedLoad();

        }
        return true;
    }

    public IStateInt[][] assignedLoad() {
        return assignedLoad;
    }
}
