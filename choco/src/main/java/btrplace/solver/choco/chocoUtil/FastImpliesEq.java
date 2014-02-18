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


import solver.constraints.IntConstraint;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.BoolVar;
import solver.variables.EventType;
import solver.variables.IntVar;
import util.ESat;

/**
 * A fast implementation for BVAR <=> VAR = CSTE
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29/06/11
 */
public class FastImpliesEq extends IntConstraint<IntVar> {

    private final int constante;

    /**
     * New instance.
     *
     * @param b        the boolean variable
     * @param var      the variable
     * @param constant the constant to use to set the variable if the boolean variable is set to true
     */
    public FastImpliesEq(BoolVar b, IntVar var, int constant) {
        super(new IntVar[]{b, var}, b.getSolver());
        this.constante = constant;
        setPropagators(new FastImpliesEqProp(vars), new FastImpliesEqProp(vars));

    }

    @Override
    public String toString() {
        return vars[0].toString() + " -> " + vars[1] + "=" + constante;
    }

    @Override
    public ESat isSatisfied(int[] tuple) {
        return ESat.eval((tuple[0] == 1 && tuple[1] == constante) || tuple[0] == 0);
    }


    /**
     * Propagator for {@link btrplace.solver.choco.chocoUtil.FastImpliesEq}.
     */
    class FastImpliesEqProp extends Propagator<IntVar> {

        /**
         * Default constructor.
         *
         * @param vs vs[0] is the boolean variable, vs[1] is the integer one
         */
        public FastImpliesEqProp(IntVar[] vs) {
            super(vs, PropagatorPriority.UNARY, true);
        }

        @Override
        public int getPropagationConditions(int idx) {
            if (idx == 0) {
                return EventType.INSTANTIATE.mask;
            } else {
                return EventType.REMOVE.mask + EventType.INSTANTIATE.mask + EventType.BOUND.mask;
            }
        }

        @Override
        public void propagate(int mask) throws ContradictionException {
            long s;
            do {
                s = vars[0].getDomainSize() + vars[1].getDomainSize();
                if (vars[0].instantiatedTo(1)) {
                    vars[1].instantiateTo(constante, aCause);
                }
                if (!vars[1].contains(constante)) {
                    vars[0].instantiateTo(0, aCause);
                }
                s -= (vars[0].getDomainSize() + vars[1].getDomainSize());
            } while (s > 0);
        }

        @Override
        public void propagate(int idx, int mask) throws ContradictionException {
            forcePropagate(EventType.INSTANTIATE);
        }

        @Override
        public ESat isEntailed() {
            if (vars[0].instantiated() && vars[1].instantiated()) {
                return ESat.eval((vars[0].getValue() == 1 && vars[1].getValue() == constante) || vars[0].getValue() == 0);
            }
            return ESat.UNDEFINED;
        }
    }
}
