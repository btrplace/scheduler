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


import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

/**
 * A fast implementation for BVAR <=> VAR = CSTE
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
        super("IFFEq", new FastIFFEqProp(b, var, c));
    }

    /**
     * Propagator for {@link org.btrplace.scheduler.choco.extensions.FastIFFEq}
     */
    static class FastIFFEqProp extends Propagator<IntVar> {

        private final int constant;

        /**
         * Default constructor.
         *
         * @param b   the boolean variable
         * @param var the integer variable
         * @param c   the constant
         */
        public FastIFFEqProp(BoolVar b, IntVar var, int c) {
            super(new IntVar[]{b, var}, PropagatorPriority.BINARY, true);
            this.constant = c;
        }

        @Override
        public int getPropagationConditions(int idx) {
            if (idx == 0) {
                return IntEventType.INSTANTIATE.getMask();
            } else {
                if (vars[1].hasEnumeratedDomain()) {
                    return IntEventType.INSTANTIATE.getMask() + IntEventType.BOUND.getMask() + IntEventType.REMOVE.getMask();
                }
                return IntEventType.INSTANTIATE.getMask() + IntEventType.BOUND.getMask();
            }
        }

        @Override
        public void propagate(int mask) throws ContradictionException {
            if (vars[0].isInstantiated()) {
                if (vars[0].contains(0)) {
                    if (vars[1].removeValue(constant, aCause))
                        setPassive();
                } else {
                    vars[1].instantiateTo(constant, aCause);
                }
            } else if (vars[1].isInstantiatedTo(constant)) {
                vars[0].instantiateTo(1, aCause);
            } else if (!vars[1].contains(constant)) {
                vars[0].instantiateTo(0, aCause);
                setPassive();
            }
        }

        @Override
        public void propagate(int idx, int mask) throws ContradictionException {
            if (idx == 0) {
                assert IntEventType.isInstantiate(mask);
                if (vars[0].contains(0)) {
                    if (vars[1].removeValue(constant, aCause))
                        setPassive();
                } else {
                    vars[1].instantiateTo(constant, aCause);
                }
            } else {
                if (vars[1].isInstantiatedTo(constant)) {
                    vars[0].instantiateTo(1, aCause);
                } else if (!vars[1].contains(constant)) {
                    vars[0].instantiateTo(0, aCause);
                    setPassive();
                }
            }
        }

        /**
         * @return true if (b==0 && c\not\in D(x)) or (b=1 && x=c) and false if (b=1 && c\not\in D(x)) or (b==0 && x=c)
         */
        @Override
        public ESat isEntailed() {
            if (vars[0].isInstantiated()) {
                if (vars[0].contains(0)) {
                    return (!vars[1].contains(constant)) ? ESat.TRUE : (vars[1].isInstantiated()) ? ESat.FALSE : ESat.UNDEFINED;
                }
                return (!vars[1].contains(constant)) ? ESat.FALSE : (vars[1].isInstantiated()) ? ESat.TRUE : ESat.UNDEFINED;
            }
            return ESat.UNDEFINED;
        }


    }

}
