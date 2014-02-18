/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
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

package btrplace.solver.choco.extensions;


import memory.IStateBitSet;
import memory.IStateInt;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.IIntDeltaMonitor;
import util.ESat;
import util.iterators.DisposableValueIterator;
import util.procedure.UnaryIntProcedure;
import util.tools.ArrayUtils;

import java.util.BitSet;

/**
 * Enforces two sets of variables values to be disjoint
 * created sofdem - 08/09/11
 *
 * @author Sophie Demassey
 */
public class Disjoint extends IntConstraint<IntVar> {

    /**
     * number of variables in the first set (group 0)
     */
    private final int nbX;

    /**
     * the variable domains must be included in [0, nbValues-1]
     */
    private final int nbValues;

    /**
     * candidates[g][v] = number of variables in group 'g' which can be assigned to the value 'v',
     * with g = 0 || 1 and 0 <= v < nbValues
     */
    protected IStateInt[][] candidates;
    /**
     * required[g].get(v) iff at least one variable in the group 'g' is assigned to the value 'v',
     * with g = 0 || 1 and 0 <= v < nbValues
     */
    private IStateBitSet[] required;

    /**
     * @param s solver
     * @param x first set of variables (group 0)
     * @param y second set of variables (group 1)
     * @param c max variable value + 1
     */
    public Disjoint(Solver s, IntVar[] x, IntVar[] y, int c) {
        super(ArrayUtils.append(x, y), s);
        this.nbX = x.length;
        this.nbValues = c;
        candidates = new IStateInt[2][c];
        required = new IStateBitSet[2];
        required[0] = s.getEnvironment().makeBitSet(c);
        required[1] = s.getEnvironment().makeBitSet(c);
        for (int v = 0; v < c; v++) {
            candidates[0][v] = s.getEnvironment().makeInt(0);
            candidates[1][v] = s.getEnvironment().makeInt(0);
        }
        setPropagators(new DisjointPropagator(x, y));
    }

    @Override
    public ESat isSatisfied(int[] tuple) {
        BitSet valuesOne = new BitSet(nbValues);
        int i = 0;
        for (; i < nbX; i++) {
            valuesOne.set(tuple[i]);
        }
        for (; i < tuple.length; i++) {
            if (valuesOne.get(tuple[i])) {
                return ESat.FALSE;
            }
        }
        return ESat.TRUE;
    }

    class DisjointPropagator extends Propagator<IntVar> {

        private IIntDeltaMonitor[] idms;

        private boolean first = true;

        private RemProc remProc;

        private IntVar[][] groups;

        public DisjointPropagator(IntVar[] g1, IntVar[] g2) {
            super(ArrayUtils.append(g1, g2), PropagatorPriority.VERY_SLOW, true);
            idms = new IIntDeltaMonitor[vars.length];
            int i = 0;
            for (IntVar v : vars) {
                idms[i++] = v.monitorDelta(aCause);
            }
            remProc = new RemProc();
            groups = new IntVar[2][];
            groups[0] = g1;
            groups[1] = g2;
        }

        @Override
        protected int getPropagationConditions(int vIdx) {
            return EventType.BOUND.mask + EventType.INSTANTIATE.mask;
        }

        @Override
        public void propagate(int evtmask) throws ContradictionException {
            if (first) {
                awake();
                first = false;
            }

            for (int v = 0; v < nbValues; v++) {
                //Check if the value 'v' is required by a group
                if (required[0].get(v)) {
                    //Required by group 0
                    setRequired(v, 0, 1);
                }
                if (required[1].get(v)) {
                    setRequired(v, 1, 0);
                }
            }
        }

        private void filterInst(int idx, int g) throws ContradictionException {
            int val = groups[g][idx].getLB();
            int otherGroup = 1 - g;
            //Required in the group
            required[g].set(val);
            //Forbidden for the other
            required[otherGroup].clear(val);

            //remove the value for the domains of the other groups
            int i = 0;
            for (IntVar v : groups[otherGroup]) {
                if (v.removeValue(val, aCause)) {
                    candidates[otherGroup][val].add(-1);
                    if (v.instantiated()) {
                        filterInst(i, otherGroup);
                    }
                }
                i++;
            }
            //remove the candidates for the delta domain
            idms[idx + g * nbX].freeze();
            idms[idx + g * nbX].forEach(remProc.set(idx + g * nbX), EventType.REMOVE);
            idms[idx + g * nbX].unfreeze();
        }

        @Override
        public void propagate(int idx, int mask) throws ContradictionException {
            if (EventType.isInstantiate(mask)) {
                int group = (idx < nbX) ? 0 : 1;
                filterInst(idx - group * nbX, group);
            } else if (EventType.isBound(mask)) {
                idms[idx].freeze();
                idms[idx].forEach(remProc.set(idx), EventType.REMOVE);
                idms[idx].unfreeze();
            }
        }

        @Override
        public ESat isEntailed() {
            BitSet valuesOne = new BitSet(nbValues);
            int i = 0;
            for (; i < nbX; i++) {
                valuesOne.set(vars[i].getValue());
            }
            for (; i < vars.length; i++) {
                if (valuesOne.get(vars[i].getValue())) {
                    return ESat.FALSE;
                }
            }
            return ESat.TRUE;
        }

        /**
         * Initialise required and candidate for a given variable
         * that belong to a given group.
         *
         * @param var   the variable
         * @param group the group of the variable
         */
        private void initVar(IntVar var, int group) {
            if (var.instantiated()) {
                required[group].set(var.getValue());
            } else {
                DisposableValueIterator it = var.getValueIterator(true);
                try {
                    while (it.hasNext()) {
                        int val = it.next();
                        candidates[group][val].add(1);
                    }
                } finally {
                    it.dispose();
                }
            }
        }

        public void awake() {
            int i = 0;
            for (; i < nbX; i++) {
                initVar(vars[i], 0);
            }
            for (; i < vars.length; i++) {
                initVar(vars[i], 1);
            }
        }

        /**
         * update the internal data and filter when a variable is newly instantiated
         * 1) fail if a variable in the other group is already instantiated to this value
         * 2) remove the value of the domains of all the variables of the other group
         *
         * @param val   the new assigned value
         * @param group the group of the new instantiated variable
         * @param other the other group (other = 1-group)
         * @throws ContradictionException when some variables in both groups are instantiated to the same value
         */
        public boolean setRequired(int val, int group, int other) throws ContradictionException {

            if (required[other].get(val)) {
                //The value is used in the other group. It's a contradiction
                contradiction(null, "");
            }
            if (candidates[other][val].get() > 0) {
                //The value was possible for the other group, so we remove it from its variable
                //n is the number of variables that were updated
                int n = 0;
                int i = (other == 0) ? 0 : nbX;
                int end = (other == 0) ? nbX : vars.length;
                for (; i < end; i++) {
                    if (vars[i].removeValue(val, aCause)) {
                        n++;
                    }
                }
                assert n == candidates[other][val].get() : n + " variables in group '" + other + "' were updated for value '" + val + "' but candidate was equals to " + candidates[other][val].get();
                candidates[other][val].set(0);
                return true;
            }
            required[group].set(val);
            return false;
        }
    }

    private class RemProc implements UnaryIntProcedure<Integer> {
        private int var;

        @Override
        public UnaryIntProcedure set(Integer idxVar) {
            this.var = idxVar;
            return this;
        }

        @Override
        public void execute(int val) throws ContradictionException {
            int group = (var < nbX) ? 0 : 1;
            candidates[group][val].add(-1);
        }
    }

}
