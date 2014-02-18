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
 * A fast implementation for BVAR => VAR = CSTE
 *
 * @author Charles Prud'homme
 * @since 29/06/11
 */
public class FastIFFEq extends IntConstraint<IntVar> {

    private final int constant;

    /**
     * New constraint.
     *
     * @param b   the boolean variable.
     * @param var the variable that can be equals to c
     * @param c   the constraint
     */
    public FastIFFEq(BoolVar b, IntVar var, int c) {
        super(new IntVar[]{b, var}, b.getSolver());
        this.constant = c;
        setPropagators(new FastIFFEqProp(vars), new FastIFFEqProp(vars));
    }

    @Override
    public ESat isSatisfied(int[] tuple) {
        return ESat.eval((tuple[0] == 1 && tuple[1] == constant)
                || (tuple[0] == 0 && tuple[1] != constant));
    }

    @Override
    public String toString() {
        return vars[0].toString() + " <-> " + vars[1] + "=" + constant;
    }

    /**
     * Propagator for {@link btrplace.solver.choco.chocoUtil.FastIFFEq}
     */
    class FastIFFEqProp extends Propagator<IntVar> {

        /**
         * Default constructor.
         *
         * @param vs vs[0] is the boolean variable, vs[1] is the integer one
         */
        public FastIFFEqProp(IntVar[] vs) {
            super(vs, PropagatorPriority.BINARY, true);
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
            if (vars[0].instantiated()) {
                int val = vars[0].getValue();
                if (val == 0) {
                    vars[1].removeValue(constant, aCause);
                } else {
                    vars[1].instantiateTo(constant, aCause);
                }
            }
            if (vars[1].instantiatedTo(constant)) {
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
                if (vars[1].instantiatedTo(constant)) {
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
            if (vars[0].instantiated() || vars[1].instantiated()) {
                return ESat.eval((vars[0].instantiatedTo(0) && !vars[1].instantiatedTo(constant))
                        || (vars[0].instantiatedTo(1) && vars[1].instantiatedTo(constant)));
            }
            return ESat.UNDEFINED;
        }


    }

}
