/*
 * Copyright  2023 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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
import org.chocosolver.util.procedure.UnaryIntProcedure;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Enforces multiple sets of variables values to be disjoint
 * created sofdem - 08/09/11
 *
 * @author Sophie Demassey
 */
public class Disjoint extends Constraint {

    /**
     * @param vs sets of variables
     * @param c  max variable value + 1
     */
    public Disjoint(IntVar[][] vs, int c) {
        super("Disjoint", new DisjointPropagator(vs, c));
    }

    static class DisjointPropagator extends Propagator<IntVar> {

        /**
         * the variable domains must be included in [0, nbValues-1]
         */
        private final int nbValues;

        /**
         * the number of groups
         */
        private final int nbGroups;

        /**
         * indices of variables in group 'g' is between groupIdx[g] and groupIdx[g+1]
         * with 0 <= g < nbGroups
         */
        private final int[] groupIdx;

        /**
         * candidates[g][v] = number of variables in group 'g' which can be assigned to value 'v',
         * with 0 <= g < nbGroups and 0 <= v < nbValues
         */
        private final IStateInt[][] candidates;

        /**
         * required[g].get(v) iff at least one variable in group 'g' is assigned to value 'v',
         * with 0 <= g < nbGroups and 0 <= v < nbValues
         */
        private final IStateBitSet[] required;

        private final IIntDeltaMonitor[] idms;

        private boolean first = true;

        private final RemProc remProc;

        /**
         * @param vs sets of variables
         * @param c  max variable value + 1
         */
        public DisjointPropagator(IntVar[][] vs, int c) {
            super(ArrayUtils.flatten(vs), PropagatorPriority.VERY_SLOW, true);
            nbValues = c;
            nbGroups = vs.length;
            groupIdx = new int[nbGroups + 1];
            candidates = new IStateInt[nbGroups][c];
            required = new IStateBitSet[nbGroups];
            groupIdx[0] = 0;
            int idx = 0;
            for (int g = 0; g < nbGroups; g++) {
                idx += vs[g].length;
                groupIdx[g + 1] = idx;
                required[g] = getModel().getEnvironment().makeBitSet(c);
                for (int v = 0; v < c; v++) {
                    candidates[g][v] = getModel().getEnvironment().makeInt(0);
                }
            }

            idms = new IIntDeltaMonitor[vars.length];
            int i = 0;
            for (IntVar v : vars) {
                idms[i++] = v.monitorDelta(this);
            }
            remProc = new RemProc();
        }

        @Override
        public int getPropagationConditions(int vIdx) {
            //TODO: REMOVE should be fine
            return IntEventType.all();
        }

        @Override
        public ESat isEntailed() {
            // Track the group using every value with an offset of 1. i.e. group 0 uses marks the value with 1. The
            // offset prevents from filling the array with a neutral value (-1).
            final int[] usedBy = new int[nbValues];
            // Default: no group.
            for (int i = 0; i < vars.length; i++) {
                // Get the group id with the offset.
                int g = getGroup(i) + 1;
                int v = vars[i].getValue();
                if (usedBy[v] == 0) {
                    // The value is not used by any group, mark it for this group.
                    usedBy[v] = g;
                } else if (usedBy[v] != g) {
                    // The value is used by another group. Failure.
                    return ESat.FALSE;
                }
                // The value is used by the current group. This is fine.
            }
            return ESat.TRUE;
        }

        /**
         * Initialise required and candidate for a given variable that belong to a given group.
         *
         * @param vv    the variable
         * @param group the group of the variable
         */
        private void initVar(IntVar vv, int group) {
            if (vv.isInstantiated()) {
                // The value is required in this group.
                required[group].set(vv.getValue());
                return;
            }
            // The variable is not instantiated, this populates the candidate values for this group.
            int ub = vv.getUB();
            for (int val = vv.getLB(); val <= ub; val = vv.nextValue(val)) {
                candidates[group][val].add(1);
            }
        }

        @Override
        public void propagate(int m) throws ContradictionException {
            if (first) {
                first = false;
                int i = 0;
                for (int g = 0; g < nbGroups; g++) {
                    for (; i < groupIdx[g + 1]; i++) {
                        initVar(vars[i], g);
                    }
                }
            }
            for (int v = 0; v < nbValues; v++) {
                for (int g = 0; g < nbGroups; g++) {
                    if (required[g].get(v)) {
                        setRequired(g, v);
                    }
                }
            }
            for (final IIntDeltaMonitor dm : idms) {
                dm.startMonitoring();
            }
        }

        @Override
        public void propagate(int idx, int mask) throws ContradictionException {
            if (IntEventType.isInstantiate(mask)) {
                int group = getGroup(idx);
                if (!required[group].get(vars[idx].getValue())) {
                    setRequired(group, vars[idx].getValue());
                }
            }
            if (IntEventType.isRemove(mask)) {
                idms[idx].forEachRemVal(remProc.set(idx));
            }
        }

        /**
         * update the internal data and filter when a variable is newly instantiated
         * 1) fail if a variable in the other group is already instantiated to this value
         * 2) remove the value of the domains of all the variables of the other group
         *
         * @param group the group of the new instantiated variable
         * @param val   the new assigned value
         * @throws ContradictionException when some variables in both groups are instantiated to the same value
         */
        public void setRequired(int group, int val) throws ContradictionException {
            required[group].set(val);
            for (int g = 0; g < nbGroups; g++) {
                if (g == group) {
                    continue;
                }
                if (required[g].get(val)) {
                    //The value is used in the other group. It's a contradiction.
                    fails();
                }
                if (candidates[g][val].get() == 0) {
                    // The value is not possible in this group. Nothing to remove.
                    continue;
                }
                //The value is possible for this group. Remove it.
                for (int i = groupIdx[g]; i < groupIdx[g + 1]; i++) {
                    if (vars[i].removeValue(val, this)) {
                        candidates[g][val].add(-1);
                        if (vars[i].isInstantiated() && !required[g].get(vars[i].getValue())) {
                            setRequired(g, vars[i].getValue());
                        }
                    }
                }
            }
        }

        /**
         * Get the group of a given variable.
         * This lookup is done recursively.
         *
         * @param idx the variable.
         * @return the group.
         */
        private int getGroup(int idx) {
            return getGroup(idx, 0, nbGroups);
        }

        private int getGroup(int idx, int s, int e) {
            assert e > s && groupIdx[s] <= idx && idx < groupIdx[e];
            if (e == s + 1) {
                return s;
            }
            // Complicated average computation that should prevent an overflow
            //from findbug point of view.
            int m = (s + e) >>> 1;
            if (idx >= groupIdx[m]) {
                return getGroup(idx, m, e);
            }
            return getGroup(idx, s, m);

        }

        private class RemProc implements UnaryIntProcedure<Integer> {
            private int group;

            @Override
            public UnaryIntProcedure<Integer> set(Integer idxVar) {
                this.group = getGroup(idxVar);
                return this;
            }

            @Override
            public void execute(int val) {
                candidates[group][val].add(-1);
            }
        }
    }
}
