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
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

import java.util.Objects;

/**
 * A fast implementation for BVAR =&gt; VAR = CSTE
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
     * Propagator for {@link org.btrplace.scheduler.choco.extensions.FastImpliesEq}.
     */
    static class FastImpliesEqProp extends Propagator<IntVar> {

        private final int constant;

        /**
         * Default constructor.
         *
         * @param b   the boolean variable
         * @param var the integer variable
         * @param c   the constant
         */
        public FastImpliesEqProp(BoolVar b, IntVar var, int c) {
            super(new IntVar[]{b, var}, PropagatorPriority.BINARY, true);
            this.constant = c;
        }

        @Override
        public int getPropagationConditions(int idx) {
            if (idx == 0) {
                return IntEventType.INSTANTIATE.getMask();
            }
            if (vars[1].hasEnumeratedDomain()) {
                return IntEventType.INSTANTIATE.getMask() + IntEventType.BOUND.getMask() + IntEventType.REMOVE.getMask();
            }
            return IntEventType.INSTANTIATE.getMask() + IntEventType.BOUND.getMask();
        }

        @Override
        public void propagate(int mask) throws ContradictionException {
            if (vars[0].isInstantiated()) {
                if (vars[0].contains(1)) {
                    vars[1].instantiateTo(constant, this);
                }
                setPassive();
            } else if (!vars[1].contains(constant)) {
                vars[0].instantiateTo(0, this);
                setPassive();
            }
        }

        @Override
        @SuppressWarnings("squid:S3346")
        public void propagate(int idx, int mask) throws ContradictionException {
            if (idx == 0) {
                assert IntEventType.isInstantiate(mask);
                if (vars[0].contains(1)) {
                    vars[1].instantiateTo(constant, this);
                }
                setPassive();
            } else if (!vars[1].contains(constant)) {
                vars[0].instantiateTo(0, this);
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
                return !vars[1].contains(constant) ? ESat.FALSE : vars[1].isInstantiated() ? ESat.TRUE : ESat.UNDEFINED;
            }
            return ESat.UNDEFINED;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            FastImpliesEqProp that = (FastImpliesEqProp) o;
            return constant == that.constant;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), constant);
        }
    }
}
