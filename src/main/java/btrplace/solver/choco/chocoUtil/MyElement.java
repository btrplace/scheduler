/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractBinIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

public final class MyElement extends AbstractBinIntSConstraint {
    int[] lval;
    int cste;

    /**
     * To indicate the ordering of the index value
     */
    public static enum Sort {
        /**
         * Values are unordered
         */
        none,
        /**
         * Values are in the increasing order
         */
        ascending,
        /**
         * Values are in the decreasing order
         */
        descending,
        /**
         * Let the constraint detect the ordering, if any
         */
        detect
    }

    /**
     * The current ordering
     */
    private Sort s;

    public MyElement(IntDomainVar index, int[] values, IntDomainVar var, int offset, Sort s) {
        super(index, var);
        this.lval = values;
        this.cste = offset;
        this.s = s;
    }

    public MyElement(IntDomainVar index, int[] values, IntDomainVar var) {
        this(index, values, var, 0, Sort.none);
    }

    @Override
    public String toString() {
        return "Element";
    }

    @Override
    public int getFilteredEventMask(int idx) {
        if (idx == 0)
            return IntVarEvent.INSTINT_MASK + IntVarEvent.REMVAL_MASK;
        else return IntVarEvent.REMVAL_MASK;
    }

    /**
     * <i>Propagation:</i>
     * Propagating the constraint until local consistency is reached.
     *
     * @throws choco.kernel.solver.ContradictionException
     *          contradiction exception
     */
    @Override
    public void propagate() throws ContradictionException {
        this.updateIndexFromValue();
    }

    @Override
    public String pretty() {
        return (this.v1.pretty() + " = nth(" + this.v0.pretty() + ", " + StringUtils.pretty(this.lval) + ")");
    }

    protected void updateValueFromIndex() throws ContradictionException {
        int minVal = Integer.MAX_VALUE;
        int maxVal = Integer.MIN_VALUE;

        if (s == Sort.descending) {
            this.v1.updateInf(this.lval[v0.getSup() - cste], this, false);
            this.v1.updateSup(this.lval[v0.getInf() - cste], this, false);
        } else if (s == Sort.ascending) {
            this.v1.updateInf(this.lval[v0.getInf() - cste], this, false);
            this.v1.updateSup(this.lval[v0.getSup() - cste], this, false);

        } else {
            DisposableIntIterator iter = this.v0.getDomain().getIterator();
            boolean isDsc = true;
            boolean isAsc = true;
            int prev = this.lval[v0.getInf() - cste];
            //System.err.println("Parse from " + prev);
            try {
                while (iter.hasNext()) {
                    int index = iter.next();
                    int val = this.lval[index - cste];
                    //System.err.println("current: " + val + " prev=" + prev + " " + isDsc + " " + isAsc);
                    if (minVal > val) {
                        minVal = val;
                    }
                    if (maxVal < val) {
                        maxVal = val;
                    }
                    if (s == Sort.detect) {
                        if (val > prev) {
                            isDsc = false;
                        }
                        if (val < prev) {
                            isAsc = false;
                        }
                    }
                    prev = val;
                }
                if (s == Sort.detect && isDsc) {
                    //ChocoLogging.getBranchingLogger().warning(isDsc + " " + isAsc + " Index is sorted decreasingly:" + Arrays.toString(lval));
                    //System.exit(0);
                    s = Sort.descending;
                } else if (s == Sort.detect && isAsc) {
                    //ChocoLogging.getBranchingLogger().warning(isDsc + " " + isAsc + " Index is sorted increasingly:" + Arrays.toString(lval));
                    s = Sort.ascending;
                } else if (s == Sort.detect && !isAsc && !isDsc) {
                    //ChocoLogging.getBranchingLogger().warning(isDsc + " " + isAsc + " Index is not sorted "+ Arrays.toString(lval));
                    s = Sort.none;
                }
                this.v1.updateInf(minVal, this, false);
                this.v1.updateSup(maxVal, this, false);
            } finally {
                iter.dispose();
            }
        }
        // todo : <hcambaza> : why it does not perform AC on the value variable ?
    }

    protected void updateIndexFromValue() throws ContradictionException {
        int minFeasibleIndex = Math.max(0 + cste, this.v0.getInf());
        int maxFeasibleIndex = Math.min(this.v0.getSup(), lval.length - 1 + cste);

        if (minFeasibleIndex > maxFeasibleIndex) {
            this.fail();
        }

        boolean forceAwake = !this.v1.hasEnumeratedDomain();

        while ((this.v0.canBeInstantiatedTo(minFeasibleIndex))
                && !(this.v1.canBeInstantiatedTo(lval[minFeasibleIndex - this.cste])))
            minFeasibleIndex++;
        this.v0.updateInf(minFeasibleIndex, this, forceAwake);

        while ((this.v0.canBeInstantiatedTo(maxFeasibleIndex))
                && !(this.v1.canBeInstantiatedTo(lval[maxFeasibleIndex - this.cste])))
            maxFeasibleIndex--;
        this.v0.updateSup(maxFeasibleIndex, this, forceAwake);

        if (this.v0.hasEnumeratedDomain()) {
            for (int i = minFeasibleIndex + 1; i <= maxFeasibleIndex - 1; i++) {
                if (this.v0.canBeInstantiatedTo(i) && !(this.v1.canBeInstantiatedTo(this.lval[i - this.cste])))
                    this.v0.removeVal(i, this, forceAwake);
            }
        }
    }

    @Override
    public void awake() throws ContradictionException {
        this.updateIndexFromValue();
        this.updateValueFromIndex();
    }

    @Override
    public void awakeOnInst(int i) throws ContradictionException {
        if (i == 0) {
            this.v1.instantiate(this.lval[this.v0.getVal() - this.cste], this, false);
        }
    }

    @Override
    public void awakeOnRem(int i, int x) throws ContradictionException {
        if (i == 0)
            this.updateValueFromIndex();
        else
            this.updateIndexFromValue();
    }

    @Override
    public Boolean isEntailed() {
        if (this.v1.isInstantiated()) {
            boolean allVal = true;
            boolean oneVal = false;
            for (int val = v0.getInf(); val <= v0.getSup(); val = v0.getNextDomainValue(val)) {
                boolean b = (val - this.cste) >= 0
                        && (val - this.cste) < this.lval.length
                        && this.lval[val - this.cste] == this.v1.getVal();
                allVal &= b;
                oneVal |= b;
            }
            if (allVal) return Boolean.TRUE;
            if (oneVal) return null;
        } else {
            boolean b = false;
            for (int val = v0.getInf(); val <= v0.getSup() && !b; val = v0.getNextDomainValue(val)) {
                if ((val - this.cste) >= 0 &&
                        (val - this.cste) < this.lval.length) {
                    b = this.v1.canBeInstantiatedTo(this.lval[val - this.cste]);
                }
            }
            if (b) return null;
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isSatisfied(int[] tuple) {
        if (tuple[0] - this.cste >= lval.length ||
                tuple[0] - this.cste < 0) return false;
        return this.lval[tuple[0] - this.cste] == tuple[1];
    }
}
