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

import choco.Choco;
import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import gnu.trove.TIntArrayList;

/**
 * Kind of a precedence constraint when there is multiple resources.
 *
 * @author Fabien Hermenier
 */
public class Precedences extends AbstractLargeIntSConstraint {

    private IntDomainVar host;

    private IntDomainVar start;

    private int[] othersHost;

    private IntDomainVar[] othersEnd;

    private int[][] endsByHost;

    /**
     * The horizon lower bound for each resource.
     */
    private IStateInt[] horizonLB;

    /**
     * The horizon upper bound for each resource.
     */
    private IStateInt[] horizonUB;

    private IEnvironment env;

    /**
     * Make a new constraint.
     *
     * @param e          the environment.
     * @param h          the task host
     * @param st         the moment the task arrives on resources h
     * @param othersHost the host of all the other tasks
     * @param othersEnd  the moment each of the other tasks leave their resource
     */
    public Precedences(IEnvironment e, IntDomainVar h, IntDomainVar st, int[] othersHost, IntDomainVar[] othersEnd) {
        super(ArrayUtils.append(new IntDomainVar[]{h, st}, othersEnd));
        this.host = h;
        this.start = st;
        this.othersHost = othersHost;
        this.othersEnd = othersEnd;
        env = e;
    }

    @Override
    public void awake() throws ContradictionException {
        //TODO: reduce the array size, to reduce memory footprint
        horizonLB = new IStateInt[host.getSup() + 1];
        horizonUB = new IStateInt[host.getSup() + 1];
        endsByHost = new int[host.getSup() + 1][];

        TIntArrayList[] l = new TIntArrayList[endsByHost.length];

        for (int i = 0; i < horizonUB.length; i++) {
            horizonLB[i] = env.makeInt(0);
            horizonUB[i] = env.makeInt(0);
            l[i] = new TIntArrayList();
        }

        for (int i = 0; i < othersHost.length; i++) {
            int p = othersHost[i];
            int lb = othersEnd[i].getInf();
            int ub = othersEnd[i].getSup();
            if (p < horizonUB.length) {
                //The other is on a possible host
                horizonLB[p].set(Math.max(lb, horizonLB[p].get()));
                horizonUB[p].set(Math.max(ub, horizonUB[p].get()));
                l[p].add(i);
            }
        }
        for (int i = 0; i < l.length; i++) {
            endsByHost[i] = l[i].toNativeArray();
        }

        if (host.isInstantiated()) {
            start.setInf(horizonLB[host.getVal()].get());
        }
        propagate();
    }

    @Override
    public void propagate() throws ContradictionException {
        assert checkHorizonConsistency();
        checkInvariant();
    }

    @Override
    public void awakeOnInst(int idx) throws ContradictionException {
        switch (idx) {
            case 0:
                //The host variable has been instantiated, so its LB can be updated to the LB of the host.
                start.setInf(horizonLB[idx].get());
                break;
            case 1:
                //The moment the task starts has been instantiated
                //For each possible host, we update the UB of the other ends to ensure the non-overlapping
                int st = start.getVal();
                DisposableIntIterator it = host.getDomain().getIterator();
                try {
                    while (it.hasNext()) {
                        int h = it.next();
                        for (int i : endsByHost[h]) {
                            //The task can go on the resource
                            //the other task must end after this one, so we adjust its UB
                            othersEnd[i].setSup(st);
                        }
                    }
                } finally {
                    it.dispose();
                }
                break;
            default:
                //The moment a placed tasks ends
                int o = idx - 2;
                int h = othersHost[o];
                recomputeHorizonForHost(h);
                //We recompute the horizon of the associated host

                if (host.isInstantiatedTo(h)) {
                    start.setInf(horizonLB[h].get());
                } else if (host.canBeInstantiatedTo(h)) {
                    //Browse the horizon for each of the possible host to update the LB
                    DisposableIntIterator it2 = host.getDomain().getIterator();
                    int min = Choco.MAX_UPPER_BOUND;
                    try {
                        while (it2.hasNext()) {
                            int candidate = it2.next();
                            if (horizonLB[candidate].get() < min) {
                                min = horizonLB[candidate].get();
                            }
                        }
                    } finally {
                        it2.dispose();
                    }
                    start.setInf(min);
                }
        }
        constAwake(false);
    }

    private void recomputeHorizonForHost(int h) {
        if (h < horizonUB.length) {
            int lb = 0, ub = 0;
            for (int id : endsByHost[h]) {
                IntDomainVar end = othersEnd[id];
                lb = Math.max(end.getInf(), lb);
                ub = Math.max(end.getSup(), ub);
            }
            horizonLB[h].set(lb);
            horizonUB[h].set(ub);
        }
    }

    @Override
    public void awakeOnInf(int idx) throws ContradictionException {
        if (idx >= 2) {
            int o = idx - 2;
            int h = othersHost[o];
            recomputeHorizonForHost(h);
            if (host.isInstantiatedTo(h)) {
                start.setInf(horizonLB[h].get());
            }
        }
        constAwake(false);
    }

    @Override
    public void awakeOnSup(int idx) throws ContradictionException {
        if (idx >= 2) {
            int o = idx - 2;
            int h = othersHost[o];
            recomputeHorizonForHost(h);
        }
        constAwake(false);
    }

    @Override
    public int getFilteredEventMask(int idx) {
        switch (idx) {
            case 0:
                return IntVarEvent.INSTINT_MASK;
            default:
                return IntVarEvent.INCINF_MASK + IntVarEvent.DECSUP_MASK + IntVarEvent.INSTINT_MASK;
        }
    }

    /**
     * Check the constraint invariant
     *
     * @throws ContradictionException if the invariant is violated
     */
    private void checkInvariant() throws ContradictionException {
        DisposableIntIterator it = host.getDomain().getIterator();
        try {
            while (it.hasNext()) {
                checkHorizonForHost(it.next());
            }
        } finally {
            it.dispose();
        }
    }

    private void checkHorizonForHost(int h) throws ContradictionException {
        if (start.getSup() < horizonLB[h].get()) {
            fail();
        }
    }

    @Override
    public boolean isSatisfied(int[] tuple) {
        int h = tuple[0];
        int st = tuple[1];

        for (int i = 0; i < othersHost.length; i++) {
            if (othersHost[i] == h && tuple[2 + i] > st) {
                return false;
            }
        }
        return true;
    }

    private boolean checkHorizonConsistency() {
        boolean ret = true;
        int[] lbs = new int[horizonLB.length];
        int[] ubs = new int[horizonUB.length];
        for (int i = 0; i < othersEnd.length; i++) {
            IntDomainVar end = othersEnd[i];
            int h = othersHost[i];
            //beware of h that can be out of the domain of the watched horizons
            if (h < lbs.length && end.getInf() > lbs[h]) {
                lbs[h] = end.getInf();
            }
            if (h < ubs.length && end.getSup() > ubs[h]) {
                ubs[h] = end.getSup();
            }
        }
        for (int i = 0; i < horizonUB.length; i++) {
            if (horizonUB[i].get() != ubs[i]) {
                ChocoLogging.getBranchingLogger().info("/!\\ horizonUB[" + i + "] = " + horizonUB[i].get() + ", expected=" + ubs[i]);
                ret = false;
            }
            if (horizonLB[i].get() != lbs[i]) {
                ChocoLogging.getBranchingLogger().info("/!\\ horizonLB[" + i + "] = " + horizonLB[i].get() + ", expected=" + lbs[i]);
                ret = false;
            }
        }
        return ret;
    }

    private void printOthers() {
        ChocoLogging.getBranchingLogger().info("--- Others ---");
        for (int i = 0; i < othersEnd.length; i++) {
            ChocoLogging.getBranchingLogger().info("Task " + i + " on " + othersHost[i] + " ends at " + othersEnd[i].pretty());
        }
    }

    private void printEndsByHost() {
        ChocoLogging.getBranchingLogger().info("--- EndsByHost ---");
        for (int i = 0; i < endsByHost.length; i++) {
            StringBuilder buf = new StringBuilder();
            buf.append("On ").append(i).append(':');
            for (int id : endsByHost[i]) {
                buf.append(" ").append(othersEnd[id].pretty());
            }
            ChocoLogging.getBranchingLogger().info(buf.append(" lb=").append(horizonLB[i].get()).append(" ub=").append(horizonUB[i].get()).toString());
        }
        ChocoLogging.getBranchingLogger().info("Mine placed on " + host.pretty());
        ChocoLogging.getBranchingLogger().info("Mine starts at " + start.pretty());
    }

}
