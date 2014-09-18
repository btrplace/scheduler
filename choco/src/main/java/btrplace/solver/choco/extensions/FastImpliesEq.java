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

package btrplace.solver.choco.extensions;


import solver.constraints.Constraint;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.BoolVar;
import solver.variables.EventType;
import solver.variables.IntVar;
import util.ESat;

/**
 * A fast implementation for BVAR => VAR = CSTE
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29/06/11
 */
public class FastImpliesEq extends Constraint {


    /**
     * New instance.
     *
     * @param b   the boolean variable
     * @param var the variable
     * @param c   the constant to use to set the variable if the boolean variable is set to true
     */
    public FastImpliesEq(BoolVar b, IntVar var, int c) {
        super("FastImpliesEq", new FastImpliesEqProp(b, var, c));
    }

    /**
     * Propagator for {@link btrplace.solver.choco.extensions.FastImpliesEq}.
     */
    static class FastImpliesEqProp extends Propagator<IntVar> {

        private final int constant;

        /**
         * Default constructor.
         *
         * @param b the boolean variable
         * @param var the integer variable
         * @param c the constant
         */
        public FastImpliesEqProp(BoolVar b, IntVar var, int c) {
            super(new IntVar[]{b, var}, PropagatorPriority.BINARY, true);
            this.constant = c;
        }

        @Override
        public int getPropagationConditions(int idx) {
            if (idx == 0) {
                return EventType.INSTANTIATE.mask;
            } else {
                if (vars[1].hasEnumeratedDomain()) {
                    return EventType.INSTANTIATE.mask + EventType.BOUND.mask + EventType.REMOVE.mask;
                }
                return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
            }
        }

        @Override
        public void propagate(int mask) throws ContradictionException {
                if (vars[0].isInstantiated()) {
                    if (vars[0].contains(1)) {
                        vars[1].instantiateTo(constant, aCause);
                    }
                    setPassive();
                } else if (!vars[1].contains(constant)) {
                    vars[0].instantiateTo(0, aCause);
                    setPassive();
                }
        }

        @Override
        public void propagate(int idx, int mask) throws ContradictionException {
            if (idx == 0) {
                assert EventType.isInstantiate(mask);
                if (vars[0].contains(1)) {
                    vars[1].instantiateTo(constant, aCause);
                }
                setPassive();
            } else if (!vars[1].contains(constant)) {
                vars[0].instantiateTo(0, aCause);
                setPassive();
            }
        }

        /**
         * @return true if (b==0) or (b=1 && x=c) and false if (b=1 && c\not\in D(x))
         */
        @Override
        public ESat isEntailed() {
            if (vars[0].isInstantiated()) {
                if (vars[0].contains(0)) {
                    return ESat.TRUE;
                }
                return (!vars[1].contains(constant)) ? ESat.FALSE : (vars[1].isInstantiated()) ? ESat.TRUE : ESat.UNDEFINED;
            }
            return ESat.UNDEFINED;
        }
    }
}
