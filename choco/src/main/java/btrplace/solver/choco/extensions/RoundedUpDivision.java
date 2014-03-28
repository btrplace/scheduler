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


import solver.constraints.IntConstraint;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import util.ESat;

/**
 * A constraint to enforce {@code a == b / divider} where {@code divider} is a real and {@code a} and {@code b} are
 * both integers.
 * The division is rounded up to the smallest integer.
 * <p/>
 * In practice, the constraint maintains:
 * <ul>
 * <li>{@code a = Math.ceil(b / divider)}</li>
 * <li>{@code b = ((a - 1 )* divider) % 1 == 0 ? [(a - 1)*divider + 1; Math.floor(a * divider)] : [Math.ceil((a -1)*divider); Math.floor(a * divider)]}</li>
 * </ul>
 *
 * @author Fabien Hermenier
 */
public class RoundedUpDivision extends IntConstraint<IntVar> {

    private double qq;

    /**
     * Make a new constraint.
     * @param a the variable to divide
     * @param b the resulting ratio
     * @param d the divider
     */
    public RoundedUpDivision(IntVar a, IntVar b, double d) {
        super(new IntVar[]{a, b}, a.getSolver());
        qq = d;
        setPropagators(new RoundedUpDivisionPropagator(vars, d));
    }

    @Override
    public ESat isSatisfied(int[] values) {
        return ESat.eval(values[0] == (int) Math.ceil((double) values[1] / qq));
    }

    @Override
    public String toString() {
        return vars[0].toString() + " = " + vars[1].toString() + '/' + qq;
    }

    class RoundedUpDivisionPropagator extends Propagator<IntVar> {

        private double divider;

        /**
         * New propagator
         *
         * @param vs the variables
         * @param d  the divider
         */
        public RoundedUpDivisionPropagator(IntVar[] vs, double d) {
            super(vs, PropagatorPriority.BINARY, true);
            this.divider = d;
        }

        @Override
        protected int getPropagationConditions(int vIdx) {
            return EventType.DECUPP.mask + EventType.INCLOW.mask + EventType.INSTANTIATE.mask;
        }

        @Override
        public ESat isEntailed() {
            if (vars[0].getDomainSize() == 1 && vars[1].getDomainSize() == 1) {
                return ESat.eval(vars[0].getValue() == (int) Math.ceil((double) vars[1].getValue() / divider));
            }
            return ESat.UNDEFINED;
        }

        private int div(int b) {
            return (int) Math.ceil((double) b / divider);
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
                this.contradiction(null, "");
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
                return vars[0].updateLowerBound(div(vars[1].getLB()), aCause);
            } else {
                return vars[1].updateLowerBound(multLB(vars[0].getLB()), aCause);
            }
        }

        public boolean awakeOnSup(int i) throws ContradictionException {
            if (i == 1) {
                return vars[0].updateUpperBound(div(vars[1].getUB()), aCause);
            } else {
                return vars[1].updateUpperBound((int) Math.floor(divider * vars[0].getUB()), aCause);
            }
        }
    }
}
