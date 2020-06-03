/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.extensions;


import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

/**
 * A constraint to enforce {@code a == b / divider} where {@code divider} is a real and {@code a} and {@code b} are
 * both integers.
 * The division is rounded up to the smallest integer.
 * <p>
 * In practice, the constraint maintains:
 * <ul>
 * <li>{@code a = Math.ceil(b / divider)}</li>
 * <li>{@code b = ((a - 1 )* divider) % 1 == 0 ? [(a - 1)*divider + 1; Math.floor(a * divider)] : [Math.ceil((a -1)*divider); Math.floor(a * divider)]}</li>
 * </ul>
 *
 * @author Fabien Hermenier
 */
public class RoundedUpDivision extends Constraint {

    /**
     * Make a new constraint.
     *
     * @param a the variable to divide
     * @param b the resulting ratio
     * @param d the divider
     */
    public RoundedUpDivision(IntVar a, IntVar b, double d) {
        super("RoundedUpDivision", new RoundedUpDivisionPropagator(a, b, d));
    }

    static class RoundedUpDivisionPropagator extends Propagator<IntVar> {

        private final double divider;

        /**
         * New propagator
         *
         * @param a the variable to divide
         * @param b the resulting ratio
         * @param d the divider
         */
        public RoundedUpDivisionPropagator(IntVar a, IntVar b, double d) {
            super(new IntVar[]{a, b}, PropagatorPriority.BINARY, true);
            this.divider = d;
        }

        @Override
        public int getPropagationConditions(int vIdx) {
            return IntEventType.DECUPP.getMask() + IntEventType.INCLOW.getMask() + IntEventType.INSTANTIATE.getMask();
        }

        @Override
        public ESat isEntailed() {
            if (vars[0].getDomainSize() == 1 && vars[1].getDomainSize() == 1) {
                return ESat.eval(vars[0].getValue() == (int) Math.ceil(vars[1].getValue() / divider));
            }
            return ESat.UNDEFINED;
        }

        private int div(int b) {
            return (int) Math.ceil(b / divider);
        }

        private int multLB(int a) {
            if ((a - 1 * divider) % 1 == 0) {
                return (int) ((a - 1) * divider + 1);
            }
            return (int) Math.ceil(divider * (a - 1));
        }

        @Override
        public void propagate(int evtMask) throws ContradictionException {
            filter();
            if (vars[0].getLB() != div(vars[1].getLB())
                    || vars[0].getUB() != div(vars[1].getUB())) {
                this.fails();
            }
        }

        private boolean filter() throws ContradictionException {
            boolean fix = awakeOnInf(0);
            fix |= awakeOnSup(0);
            fix |= awakeOnInf(1);
            fix |= awakeOnSup(1);
            return fix;
        }

        @Override
        public void propagate(int idx, int mask) throws ContradictionException {
            do {
            } while (filter());
        }


        public boolean awakeOnInf(int i) throws ContradictionException {
            if (i == 1) {
                return vars[0].updateLowerBound(div(vars[1].getLB()), this);
            }
            return vars[1].updateLowerBound(multLB(vars[0].getLB()), this);
        }

        public boolean awakeOnSup(int i) throws ContradictionException {
            if (i == 1) {
                return vars[0].updateUpperBound(div(vars[1].getUB()), this);
            }
            return vars[1].updateUpperBound((int) Math.floor(divider * vars[0].getUB()), this);
        }
    }
}
