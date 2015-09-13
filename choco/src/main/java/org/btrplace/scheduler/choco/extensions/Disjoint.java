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


import org.chocosolver.memory.IStateBitSet;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.procedure.UnaryIntProcedure;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.BitSet;

/**
 * Enforces two sets of variables values to be disjoint
 * created sofdem - 08/09/11
 *
 * @author Sophie Demassey
 */
public class Disjoint extends Constraint {

    /**
     * @param x first set of variables (group 0)
     * @param y second set of variables (group 1)
     * @param c max variable value + 1
     */
    public Disjoint(IntVar[] x, IntVar[] y, int c) {
        super("Disjoint", new DisjointPropagator(x, y, c));
    }

    static class DisjointPropagator extends Propagator<IntVar> {

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
        private IStateInt[][] candidates;
        /**
         * required[g].get(v) iff at least one variable in the group 'g' is assigned to the value 'v',
         * with g = 0 || 1 and 0 <= v < nbValues
         */
        private IStateBitSet[] required;

        private IIntDeltaMonitor[] idms;

        private boolean first = true;

        private RemProc remProc;

        private IntVar[][] groups;

        /**
         * @param x first set of variables (group 0)
         * @param y second set of variables (group 1)
         * @param c max variable value + 1
         */
        public DisjointPropagator(IntVar[] x, IntVar[] y, int c) {
            super(ArrayUtils.append(x, y), PropagatorPriority.VERY_SLOW, true);
            this.nbX = x.length;
            this.nbValues = c;
            candidates = new IStateInt[2][c];
            required = new IStateBitSet[2];
            required[0] = getSolver().getEnvironment().makeBitSet(c);
            required[1] = getSolver().getEnvironment().makeBitSet(c);
            for (int v = 0; v < c; v++) {
                candidates[0][v] = getSolver().getEnvironment().makeInt(0);
                candidates[1][v] = getSolver().getEnvironment().makeInt(0);
            }
            idms = new IIntDeltaMonitor[vars.length];
            int i = 0;
            for (IntVar v : vars) {
                idms[i++] = v.monitorDelta(aCause);
            }
            remProc = new RemProc();
            groups = new IntVar[2][];
            groups[0] = x;
            groups[1] = y;
        }

        @Override
        protected int getPropagationConditions(int vIdx) {
            return IntEventType.BOUND.getMask() + IntEventType.INSTANTIATE.getMask();
        }

        @Override
        public void propagate(int m) throws ContradictionException {
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
                    if (v.isInstantiated()) {
                        filterInst(i, otherGroup);
                    }
                }
                i++;
            }
            //remove the candidates for the delta domain
            idms[idx + g * nbX].freeze();
            idms[idx + g * nbX].forEachRemVal(remProc.set(idx + g * nbX));
            idms[idx + g * nbX].unfreeze();
        }

        @Override
        public void propagate(int idx, int mask) throws ContradictionException {
            if (IntEventType.isInstantiate(mask)) {
                int group = (idx < nbX) ? 0 : 1;
                filterInst(idx - group * nbX, group);
            } else if (IntEventType.isBound(mask)) {
                idms[idx].freeze();
                idms[idx].forEachRemVal(remProc.set(idx));
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
            if (var.isInstantiated()) {
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
}
