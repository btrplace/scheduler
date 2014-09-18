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
 *
 * @author Charles Prud'homme
 * @since 29/06/11
 */
public class FastIFFEq extends Constraint {

    /**
     * New constraint.
     *
     * @param b   the boolean variable.
     * @param var the variable that can be equals to c
     * @param c   the constraint
     */
    public FastIFFEq(BoolVar b, IntVar var, int c) {
        super("IFEq", new FastIFFEqProp(b, var, c), new FastIFFEqProp(b, var, c));
    }

    /**
     * Propagator for {@link btrplace.solver.choco.extensions.FastIFFEq}
     */
    static class FastIFFEqProp extends Propagator<IntVar> {

        private final int constant;

        /**
         * Default constructor.
         *
         * @param b the boolean variable
         * @param var the integer variable
         * @param c the constant
         */
        public FastIFFEqProp(BoolVar b, IntVar var, int c) {
            super(new IntVar[]{b, var}, PropagatorPriority.BINARY, true);
            this.constant = c;
        }

        @Override
        public int getPropagationConditions(int idx) {
            if (idx == 0) {
                return EventType.INSTANTIATE.mask;
            } else {
                if (vars[1].hasEnumeratedDomain()) {
                    return EventType.INSTANTIATE.mask + EventType.REMOVE.mask;
                }
                return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
            }
        }

        @Override
        public void propagate(int mask) throws ContradictionException {
            if (vars[0].isInstantiated()) {
                int val = vars[0].getValue();
                if (val == 0) {
                    vars[1].removeValue(constant, aCause);
                } else {
                    vars[1].instantiateTo(constant, aCause);
                }
            }
            if (vars[1].isInstantiatedTo(constant)) {
                vars[0].instantiateTo(1, aCause);
            } else if (!vars[1].contains(constant)) {
                vars[0].instantiateTo(0, aCause);
            }
        }

        @Override
        public void propagate(int idx, int mask) throws ContradictionException {
            if (EventType.isInstantiate(mask)) {
                awakeOnInst(idx);
            }
            if (EventType.isRemove(mask)) {
                if (!vars[1].contains(constant)) {
                    vars[0].instantiateTo(0, aCause);
                }
            }
            if (EventType.isDecupp(mask)) {
                awakeOnSup(idx);
            }
            if (EventType.isInclow(mask)) {
                awakeOnInf(idx);
            }
        }

        public void awakeOnInst(int idx) throws ContradictionException {
            if (idx == 0) {
                int val = vars[0].getValue();
                if (val == 0) {
                    vars[1].removeValue(constant, aCause);
                } else {
                    vars[1].instantiateTo(constant, aCause);
                }
            } else {
                if (vars[1].isInstantiatedTo(constant)) {
                    vars[0].instantiateTo(1, aCause);
                } else {
                    vars[0].instantiateTo(0, aCause);
                }
            }
        }

        public void awakeOnInf(int varIdx) throws ContradictionException {
            if (varIdx == 1) {
                if (!vars[1].contains(constant)) {
                    vars[0].instantiateTo(0, aCause);
                }
            }
        }

        public void awakeOnSup(int varIdx) throws ContradictionException {
            if (varIdx == 1) {
                if (!vars[1].contains(constant)) {
                    vars[0].instantiateTo(0, aCause);
                }
            }
        }

        @Override
        public ESat isEntailed() {
            if (vars[0].isInstantiated() || vars[1].isInstantiated()) {
                return ESat.eval((vars[0].isInstantiatedTo(0) && !vars[1].isInstantiatedTo(constant))
                        || (vars[0].isInstantiatedTo(1) && vars[1].isInstantiatedTo(constant)));
            }
            return ESat.UNDEFINED;
        }


    }

}
