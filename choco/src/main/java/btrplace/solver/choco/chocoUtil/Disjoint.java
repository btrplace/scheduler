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

package btrplace.solver.choco.chocoUtil;


import memory.IStateBitSet;
import memory.IStateInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static Logger LOGGER = LoggerFactory.getLogger("solver");

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
            return EventType.REMOVE.mask + EventType.INSTANTIATE.mask;
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
         * Initialise required and candidate for a given variable that belong to a given group.
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

        @Override
        public void propagate(int evtmask) throws ContradictionException {
            //LOGGER.info("propagate " + EventType.isInstantiate(evtmask) + " " + EventType.isBound(evtmask) + " "  + EventType.isRemove(evtmask));
            if (first) {
                first = false;
                int i = 0;
                for (; i < nbX; i++) {
                    initVar(vars[i], 0);
                }
                for (; i < vars.length; i++) {
                    initVar(vars[i], 1);
                }
                //LOGGER.info("Done with awake:");
            }

            for (int v = 0; v < nbValues; v++) {
                if (required[0].get(v)) {
                    setRequired(v, 0);
                }
                if (required[1].get(v)) {
                    setRequired(v, 1);
                }
            }
            //LOGGER.info("End for propagate");
        }

        @Override
        public void propagate(int idx, int mask) throws ContradictionException {
            //LOGGER.info("\n\n propagate " + idx + " " + EventType.isInstantiate(mask) + " " + EventType.isBound(mask) + " "  + EventType.isRemove(mask));
            if (EventType.isRemove(mask)) {
                //LOGGER.info("Deal with rem of " + vars[idx]);
                idms[idx].freeze();
                idms[idx].forEach(remProc.set(idx), EventType.REMOVE);
                idms[idx].unfreeze();
                //LOGGER.info("done with rem");
            }
            if (EventType.isInstantiate(mask)) {
                int group = (idx < nbX) ? 0 : 1;
                if (!required[group].get(vars[idx].getValue())) {
                    setRequired(vars[idx].getValue(), group);
                }
                //LOGGER.info("done for instantiation");
            }
            //checkConsistency();
        }

        /**
         * update the internal data and filter when a variable is newly instantiated
         * 1) fail if a variable in the other group is already instantiated to this value
         * 2) remove the value of the domains of all the variables of the other group
         *
         * @param val   the new assigned value
         * @param group the group of the new instantiated variable
         * @throws ContradictionException when some variables in both groups are instantiated to the same value
         */
        public boolean setRequired(int val, int group) throws ContradictionException {
            // LOGGER.info("Required " + val + " for group " + group);
            required[group].set(val);
            int other = 1 - group;
            if (required[other].get(val)) {
                //The value is used in the other group. It's a contradiction
                // LOGGER.info("! Already Required " + val + " by group " + other);
                contradiction(null, "");
            }
            if (candidates[other][val].get() > 0) {
                //The value was possible for the other group, so we remove it from its variable
                int i = (other == 0) ? 0 : nbX;
                int end = (other == 0) ? nbX : vars.length;
                for (; i < end; i++) {
                    if (vars[i].removeValue(val, aCause)) {
                        // LOGGER.info("Removed " + val + " from " + vars[i]);
                        candidates[other][val].add(-1);
                        if (vars[i].instantiated()) {
                            // LOGGER.info(vars[i] + " is now instantiated");
                            if (!required[other].get(vars[i].getValue())) {
                                setRequired(vars[i].getValue(), other);
                            }
                        }
                    }
                }
                return true;
            }
            return false;
        }
    }

    private class RemProc implements UnaryIntProcedure<Integer> {
        private int group;

        @Override
        public UnaryIntProcedure set(Integer idxVar) {
            group = (idxVar < nbX) ? 0 : 1;
            return this;
        }

        @Override
        public void execute(int val) throws ContradictionException {
            LOGGER.info("Remove one candidate for value " + val + " in group " + group);
            candidates[group][val].add(-1);
        }
    }

         /*private void prettyCandidates(int g) {
            StringBuilder b = new StringBuilder();
            int x = 0;
            for (IStateInt v : candidates[g]) {
                b.append(" value(").append(x++).append("):").append(v.get());
            }
            LOGGER.info("Candidates for group " + g + ": " + b.toString());
        }              */


           /*private void checkConsistency() {
            LOGGER.info(Arrays.toString(vars));
            prettyCandidates(0);
            prettyCandidates(1);
            int [][] cdts;
            cdts = new int[2][nbValues];
            int i = 0;
            for (IntVar v : vars) {
                DisposableValueIterator ite = v.getValueIterator(true);
                while (ite.hasNext()) {
                    int g = i < nbX ? 0 : 1;
                    int n = ite.next();
                    cdts[g][n]++;
                }
                i++;
                ite.dispose();
            }
            for (int g = 0; g < 2; g++) {
                for (int y = 0;  y < candidates[g].length; y++) {
                    if (candidates[g][y].get() != cdts[g][y]) {
                        LOGGER.error("Unconsistency about value " + y + " for group " + g + ": " + candidates[g][y].get() + "/=" + cdts[g][y]);
                        assert false;
                    }
                }
            }
        }            */

}
