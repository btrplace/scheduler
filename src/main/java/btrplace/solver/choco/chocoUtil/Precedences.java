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

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

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

    /**
     * The horizon lower bound for each resource.
     */
    private IStateInt[] horizonLB;

    /**
     * The horizon upper bound for each resource.
     */
    private IStateInt[] horizonUB;

    private IEnvironment env;

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

        for (int i = 0; i < horizonUB.length; i++) {
            horizonLB[i] = env.makeInt(0);
            horizonUB[i] = env.makeInt(0);
        }

        for (int i = 0; i < othersHost.length; i++) {
            int p = othersHost[i];
            int lb = othersEnd[i].getInf();
            int ub = othersEnd[i].getSup();
            horizonLB[p].set(Math.max(lb, horizonLB[p].get()));
            horizonUB[p].set(Math.max(ub, horizonUB[p].get()));
        }
        propagate();
    }

    @Override
    public void propagate() throws ContradictionException {
        //printOthers();
        //printHorizons();
        assert checkHorizonConsistency();
        checkInvariant();
    }

    @Override
    public void awakeOnInst(int idx) throws ContradictionException {
        switch (idx) {
            case 0:
                //The host variable
                start.setInf(horizonLB[idx].get());
                break;
            case 1:
                //The moment the task starts
                //For every possible host, check if the start
                //moment > UB
                int st = start.getVal();
                for (int i = 0; i < othersHost.length; i++) {
                    //TODO: store the otherEnds by their location to not iterate over all of them
                    int h = othersHost[i];
                    if (host.canBeInstantiatedTo(h)) {
                        //The task can go on the resource
                        //the other task must end after this one, so we adjust its UB
                        othersEnd[i].setSup(st);
                    }
                }
                break;
            default:
                //The moment the placed task ends
                int o = idx - 2;
                int h = othersHost[o];

                //Update the horizon on the other task host
                horizonLB[h].set(Math.max(othersEnd[o].getVal(), horizonLB[h].get()));
                horizonUB[h].set(Math.max(othersEnd[o].getVal(), horizonUB[h].get()));

                if (host.isInstantiatedTo(h)) {
                    start.setInf(horizonLB[h].get());
                }
                //TODO: is it possible to increase start when host is not instantiated ?
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
        if (host.isInstantiated()) {
            int h = host.getVal();
            if (!checkHorizonForHost(h)) {
                fail();
            }
        } else {
            DisposableIntIterator it = host.getDomain().getIterator();
            try {
                while (it.hasNext()) {
                    int h = it.next();
                    if (!checkHorizonForHost(h)) {
                        fail();
                    }
                }
            } finally {
                it.dispose();
            }
        }
    }

    private boolean checkHorizonForHost(int h) {
        IStateInt ub = horizonUB[h];
        return start.getSup() >= ub.get();
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
            if (end.getInf() > lbs[h]) {
                lbs[h] = end.getInf();
            }
            if (end.getSup() > ubs[h]) {
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
        System.out.println("--- Others ---");
        for (int i = 0; i < othersEnd.length; i++) {
            System.out.println("Task " + i + " on " + othersHost[i] + " ends at " + othersEnd[i].getVal());
        }
    }

    private void printHorizons() {
        if (horizonUB != null) {
            System.out.println("--- Horizons ---");
            for (int i = 0; i < horizonUB.length; i++) {
                if (horizonUB[i] != null) {
                    System.out.println("On " + i + " lb=" + horizonLB[i].get() + " ub=" + horizonUB[i].get());
                } else {
                    System.out.println("On " + i + " ignored");
                }
            }
        }
    }
}
