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

package btrplace.solver.choco.chocoUtil;

import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.VMActionModel;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.variables.integer.IntDomainVar;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder to create constraints where slices have to be placed on nodes
 * with regards to the slice and the nodes capacity.
 * <p/>
 * It differs from {@link btrplace.solver.choco.SliceSchedulerBuilder} as
 * a resource may in fact be an alias to another one. This allows
 * to create a fake resource that aggregate the capacity of each of
 * the aliased resources.
 *
 * @author Fabien Hermenier
 */
public class AliasedCumulativesBuilder {

    private ReconfigurationProblem rp;

    private int[] associations;

    private IntDomainVar[] cEnds;

    private IntDomainVar[] cHosters;

    private IntDomainVar[] dHosters;

    private IntDomainVar[] dStarts;

    private TIntArrayList capacities;

    private List<int[]> cUsages;

    private List<IntDomainVar[]> dUsages;

    private List<int[]> aliases;

    /**
     * Make a new builder.
     *
     * @param rp the associated problem
     */
    public AliasedCumulativesBuilder(ReconfigurationProblem rp) {
        this.rp = rp;

        List<Slice> dS = new ArrayList<Slice>();
        List<Slice> cS = new ArrayList<Slice>();

        cUsages = new ArrayList<int[]>();
        dUsages = new ArrayList<IntDomainVar[]>();
        aliases = new ArrayList<int[]>();
        capacities = new TIntArrayList();
        List<int[]> linked = new ArrayList<int[]>();
        int dIdx = 0, cIdx = 0;

        for (VMActionModel a : rp.getVMActions()) {
            Slice c = a.getCSlice();
            Slice d = a.getDSlice();

            if (d != null && c != null) {
                linked.add(new int[]{dIdx, cIdx});
            }
            if (d != null) {
                dS.add(dIdx, d);
                dIdx++;
            }

            if (c != null) {
                cS.add(cIdx, c);
                cIdx++;
            }
        }


        int i = 0;
        cHosters = new IntDomainVar[cS.size()];
        cEnds = new IntDomainVar[cS.size()];
        for (Slice s : cS) {
            cHosters[i] = s.getHoster();
            cEnds[i] = s.getEnd();
            i++;

        }

        i = 0;
        dStarts = new IntDomainVar[dS.size()];
        dHosters = new IntDomainVar[dS.size()];

        for (Slice s : dS) {
            dHosters[i] = s.getHoster();
            dStarts[i] = s.getStart();
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

    /**
     * Add a constraint
     *
     * @param capas the cumulated capacity of the aliased resources
     * @param cUse  the usage of each of the c-slices
     * @param dUse  the usage of each of the d-slices
     * @param alias the resource identifiers that compose the alias
     */
    public void add(int capas, int[] cUse, IntDomainVar[] dUse, int[] alias) {
        capacities.add(capas);
        cUsages.add(cUse);
        dUsages.add(dUse);
        aliases.add(alias);
    }

    /**
     * Get the generated constraints.
     *
     * @return a list of constraint that may be empty.
     */
    public List<AliasedCumulatives> getConstraints() {
        CPSolver s = rp.getSolver();
        List<AliasedCumulatives> cstrs = new ArrayList<AliasedCumulatives>();


        for (int i = 0; i < aliases.size(); i++) {
            int capa = capacities.get(i);
            int[] alias = aliases.get(i);
            int[] cUse = cUsages.get(i);
            int[] dUses = new int[dUsages.get(i).length];
            for (IntDomainVar dUseDim : dUsages.get(i)) {
                dUses[i++] = dUseDim.getInf();
            }
            cstrs.add(new AliasedCumulatives(s.getEnvironment(),
                    alias,
                    new int[]{capa},
                    cHosters, new int[][]{cUse}, cEnds,
                    dHosters, new int[][]{dUses}, dStarts,
                    associations));

        }
        return cstrs;
    }
}
