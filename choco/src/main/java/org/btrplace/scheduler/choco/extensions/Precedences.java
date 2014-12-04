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

package org.btrplace.scheduler.choco.extensions;


import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Kind of a precedence constraint when there is multiple resources.
 *
 * @author Fabien Hermenier
 */
public class Precedences extends Constraint {

    /**
     * Make a new constraint.
     *
     * @param h  the task host
     * @param st the moment the task arrives on resources h
     * @param oh the host of all the other tasks
     * @param oe the moment each of the other tasks leave their resource
     */
    public Precedences(IntVar h, IntVar st, int[] oh, IntVar[] oe) {
        super("precedences", new PrecedencesPropagator(h, st, oh, oe));
    }

    static class PrecedencesPropagator extends Propagator<IntVar> {

        private static final Logger LOGGER = LoggerFactory.getLogger("solver");
        private IntVar host;

        private IntVar start;

        private int[] othersHost;

        private IntVar[] othersEnd;

        private int[][] endsByHost;

        /**
         * The horizon lower bound for each resource.
         */
        private IStateInt[] horizonLB;

        /**
         * The horizon upper bound for each resource.
         */
        private IStateInt[] horizonUB;


        public PrecedencesPropagator(IntVar h, IntVar st, int[] oh, IntVar[] oe) {
            super(ArrayUtils.append(new IntVar[]{h, st}, oe), PropagatorPriority.LINEAR, true);
            this.host = h;
            this.start = st;
            this.othersHost = oh;
            this.othersEnd = oe;
        }

        @Override
        protected int getPropagationConditions(int idx) {
            switch (idx) {
                case 0:
                    return IntEventType.INSTANTIATE.getMask();
                default:
                    return IntEventType.INCLOW.getMask() + IntEventType.DECUPP.getMask() + IntEventType.INSTANTIATE.getMask();
            }
        }

        @Override
        public void propagate(int m) throws ContradictionException {
            awake();
            propagate();
        }

        @Override
        public void propagate(int idx, int mask) throws ContradictionException {
            if (IntEventType.isInstantiate(mask)) {
                awakeOnInst(idx);
            }
            if (IntEventType.isDecupp(mask)) {
                awakeOnSup(idx);
            }
            if (IntEventType.isInclow(mask)) {
                awakeOnInf(idx);
            }

        }

        @Override
        public ESat isEntailed() {
            int h = vars[0].getValue();
            int st = vars[1].getValue();

            for (int i = 0; i < othersHost.length; i++) {
                if (othersHost[i] == h && vars[2 + i].getValue() > st) {
                    return ESat.FALSE;
                }
            }
            return ESat.TRUE;
        }

        public void awake() throws ContradictionException {
            //TODO: reduce the array size, to reduce memory footprint
            horizonLB = new IStateInt[host.getUB() + 1];
            horizonUB = new IStateInt[host.getUB() + 1];
            endsByHost = new int[host.getUB() + 1][];

            TIntArrayList[] l = new TIntArrayList[endsByHost.length];

            IEnvironment env = getSolver().getEnvironment();
            for (int i = 0; i < horizonUB.length; i++) {
                horizonLB[i] = env.makeInt(0);
                horizonUB[i] = env.makeInt(0);
                l[i] = new TIntArrayList();
            }

            for (int i = 0; i < othersHost.length; i++) {
                int p = othersHost[i];
                int lb = othersEnd[i].getLB();
                int ub = othersEnd[i].getUB();
                if (p < horizonUB.length) {
                    //The other is on a possible host
                    horizonLB[p].set(Math.max(lb, horizonLB[p].get()));
                    horizonUB[p].set(Math.max(ub, horizonUB[p].get()));
                    l[p].add(i);
                }
            }
            for (int i = 0; i < l.length; i++) {
                endsByHost[i] = l[i].toArray();
            }

            if (host.isInstantiated()) {
                start.updateLowerBound(horizonLB[host.getValue()].get(), aCause);
            }
        }

        //@Override
        public void awakeOnInst(int idx) throws ContradictionException {
            switch (idx) {
                case 0:
                    //The host variable has been instantiated, so its LB can be updated to the LB of the host.
                    start.updateLowerBound(horizonLB[idx].get(), aCause);
                    break;
                case 1:
                    //The moment the task starts has been instantiated
                    //For each possible host, we update the UB of the other ends to ensure the non-overlapping
                    int st = start.getValue();
                    DisposableValueIterator it = host.getValueIterator(true);
                    try {
                        while (it.hasNext()) {
                            int h = it.next();
                            for (int i : endsByHost[h]) {
                                //The task can go on the resource
                                //the other task must end after this one, so we adjust its UB
                                othersEnd[i].updateUpperBound(st, aCause);
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
                        start.updateLowerBound(horizonLB[h].get(), aCause);
                    } else if (host.contains(h)) {
                        //Browse the horizon for each of the possible host to update the LB
                        DisposableValueIterator it2 = host.getValueIterator(true);
                        int min = Integer.MAX_VALUE;
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
                        start.updateLowerBound(min, aCause);
                    }
            }
        }

        public void propagate() throws ContradictionException {
            assert checkHorizonConsistency();
            checkInvariant();
        }


        private void recomputeHorizonForHost(int h) {
            if (h < horizonUB.length) {
                int lb = 0, ub = 0;
                for (int id : endsByHost[h]) {
                    IntVar end = othersEnd[id];
                    lb = Math.max(end.getLB(), lb);
                    ub = Math.max(end.getUB(), ub);
                }
                horizonLB[h].set(lb);
                horizonUB[h].set(ub);
            }
        }

        //@Override
        public void awakeOnInf(int idx) throws ContradictionException {
            if (idx >= 2) {
                int o = idx - 2;
                int h = othersHost[o];
                recomputeHorizonForHost(h);
                if (host.isInstantiatedTo(h)) {
                    start.updateLowerBound(horizonLB[h].get(), aCause);
                }
            }
        }

        //@Override
        public void awakeOnSup(int idx) {
            if (idx >= 2) {
                int o = idx - 2;
                int h = othersHost[o];
                recomputeHorizonForHost(h);
            }
        }

        /**
         * Check the constraint invariant
         *
         * @throws ContradictionException if the invariant is violated
         */
        private void checkInvariant() throws ContradictionException {
            DisposableValueIterator it = host.getValueIterator(true);
            try {
                while (it.hasNext()) {
                    checkHorizonForHost(it.next());
                }
            } finally {
                it.dispose();
            }
        }

        private void checkHorizonForHost(int h) throws ContradictionException {
            if (start.getUB() < horizonLB[h].get()) {
                this.contradiction(start, "");
            }
        }

        private boolean checkHorizonConsistency() {
            boolean ret = true;
            int[] lbs = new int[horizonLB.length];
            int[] ubs = new int[horizonUB.length];
            for (int i = 0; i < othersEnd.length; i++) {
                IntVar end = othersEnd[i];
                int h = othersHost[i];
                //beware of h that can be out of the domain of the watched horizons
                if (h < lbs.length && end.getLB() > lbs[h]) {
                    lbs[h] = end.getLB();
                }
                if (h < ubs.length && end.getUB() > ubs[h]) {
                    ubs[h] = end.getUB();
                }
            }
            for (int i = 0; i < horizonUB.length; i++) {
                if (horizonUB[i].get() != ubs[i]) {
                    LOGGER.info("/!\\ horizonUB[" + i + "] = " + horizonUB[i].get() + ", expected=" + ubs[i]);
                    ret = false;
                }
                if (horizonLB[i].get() != lbs[i]) {
                    LOGGER.info("/!\\ horizonLB[" + i + "] = " + horizonLB[i].get() + ", expected=" + lbs[i]);
                    ret = false;
                }
            }
            return ret;
        }

        private void printOthers() {
            LOGGER.info("--- Others ---");
            for (int i = 0; i < othersEnd.length; i++) {
                LOGGER.info("Task " + i + " on " + othersHost[i] + " ends at " + othersEnd[i].toString());
            }
        }

        private void printEndsByHost() {
            LOGGER.info("--- EndsByHost ---");
            for (int i = 0; i < endsByHost.length; i++) {
                StringBuilder buf = new StringBuilder();
                buf.append("On ").append(i).append(':');
                for (int id : endsByHost[i]) {
                    buf.append(" ").append(othersEnd[id].toString());
                }
                LOGGER.info(buf.append(" lb=").append(horizonLB[i].get()).append(" ub=").append(horizonUB[i].get()).toString());
            }
            LOGGER.info("Mine placed on " + host.toString());
            LOGGER.info("Mine starts at " + start.toString());
        }

    }
}
